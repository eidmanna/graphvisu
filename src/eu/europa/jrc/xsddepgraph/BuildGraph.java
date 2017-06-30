/**
 * 
 */
package eu.europa.jrc.xsddepgraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author julien Gaffuri
 *
 */
public class BuildGraph {

	public static void main(String[] args) {
		try {
			//the file with the input urls
			String inPath;
			try {
				inPath = args[0];
			} catch (Exception e) {
				inPath = "in.txt";
			}

			//the file with the output graph
			String outPath;
			try {
				outPath = args[1];
			} catch (Exception e) {
				outPath = "out.txt";
			}

			//the file with the log messages
			String logPath;
			try {
				logPath = args[2];
			} catch (Exception e) {
				logPath = "log.txt";
			}

			//the node position optimisation time in sec
			int optTime;
			try {
				optTime = 1000*Integer.parseInt(args[3]);
			} catch (Exception e) {
				optTime = 5000;
			}


			System.out.println("Read input file...");

			ArrayList<URL> urls = new ArrayList<URL>();
			ArrayList<URL> urlsToIgnore = new ArrayList<URL>();

			BufferedReader reader = new BufferedReader(new FileReader(inPath));
			String line;
			while ((line = reader.readLine()) != null){
				if("".equals(line)) continue;

				//check if comment
				if("//".equals(line.substring(0, 2))) continue;

				//url to ignore
				if("-".equals(line.substring(0, 1)))
					urlsToIgnore.add(new URL(line.substring(1, line.length())));
				else
					urls.add(new URL(line));
			}
			reader.close();
			System.out.println("("+urls.size()+" urls to handle, "+urlsToIgnore.size()+" to ignore)");

			System.out.println("Build graph...");
			Graph graph = new Graph(urls, urlsToIgnore);

			System.out.println("Optimise node positions (takes "+((int)(optTime/1000))+"s)...");
			graph.decomposeAndConstrain();
			graph.startDeformation();
			try { Thread.sleep(optTime); } catch (InterruptedException e) { e.printStackTrace(); }
			graph.stopDeformation();

			System.out.println("Write graph file...");
			graph.save(outPath);

			System.out.println("Write log file...");
			graph.writeLog(logPath);

			System.out.println("Done.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
