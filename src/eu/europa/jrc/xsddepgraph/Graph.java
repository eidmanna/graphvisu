/**
 * 
 */
package eu.europa.jrc.xsddepgraph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opencarto.server.algo.deformation.base.GPoint;
import org.opencarto.server.algo.deformation.constraint.PointPointMinimalDistance;
import org.opencarto.server.algo.deformation.constraint.SegmentLength;
import org.opencarto.server.algo.deformation.constraint.SegmentMaximalLength;
import org.opencarto.server.algo.deformation.constraint.SegmentMinimalLength;
import org.opencarto.server.algo.deformation.constraint.SegmentOrientation;
import org.opencarto.server.algo.deformation.submicro.GSegment;
import org.opencarto.server.algo.deformation.submicro.GSinglePoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author julien Gaffuri
 *
 */
public class Graph {
	ArrayList<Node> nodes;
	ArrayList<Link> links = new ArrayList<Link>();

	public Graph(String path){
		this.nodes = new ArrayList<Node>();

		//load the nodes from file
		File file = new File(path);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.isEmpty()) continue;

				String[] data = line.split(",");

				Node n = new Node(new URL(data[2]));
				nodes.add(n);

				n.id = Integer.parseInt(data[0]);
				n.label = data[1];
				n.c.x = Double.parseDouble(data[3]);
				n.c.y = Double.parseDouble(data[4]);
				if(data.length == 6)
					n.aux = data[5];
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//link the nodes
		linkNodes();
	}

	public Graph(Collection<URL> urls, Collection<URL> urlsToIgnore){
		nodes = new ArrayList<Node>();
		for(URL url : urls){
			//if url to ignore, skip
			if(urlsToIgnore.contains(url))
				continue;
			//if node already in graph, skip
			Node n = getNode(url);
			if(n != null)
				continue;
			//else add new node and build graph from it
			n = new Node(url);
			nodes.add(n);
			buildGraph(n, urlsToIgnore);
		}
		initPosition();
	}

	//build the rest of the graph from a node
	private void buildGraph(Node n, Collection<URL> urlsToIgnore){
		System.out.println("\tProcessing " + n.url);

		if("http://www.w3.org/2001/xml.xsd".equals(n.url.toString())){
			//System.out.println("(skipped)");
			return;
		}

		//get the xml doc from the url
		Document XMLDoc = null;
		try {
			XMLDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( n.url.openStream() );
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("   Impossible to load XSD file from: " + n.url);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		if(XMLDoc == null)
			return;

		//get all "include" and "import" in the xml
		ArrayList<URL> urls = new ArrayList<URL>();
		urls.addAll( getURLS("include", XMLDoc, n.url) );
		urls.addAll( getURLS("import", XMLDoc, n.url) );
		urls.addAll( getURLS("xs:include", XMLDoc, n.url) );
		urls.addAll( getURLS("xs:import", XMLDoc, n.url) );
		urls.addAll( getURLS("xsd:import", XMLDoc, n.url) );

		//go through the urls
		for(URL url2 : urls){
			//if url to ignore, skip
			if(urlsToIgnore.contains(url2))
				continue;

			//check if node already exists
			Node n2 = getNode(url2);
			if(n2 != null){
				//Node already exists: just link to it
				links.add( n.buildLinkTo(n2) );
				continue;
			}
			//create new node with the url
			n2 = new Node(url2);
			nodes.add(n2);

			//link to it
			links.add( n.buildLinkTo(n2) );

			//recursive call
			buildGraph(n2, urlsToIgnore);
		}
	}

	private static ArrayList<URL> getURLS(String string, Document XMLDoc, URL context) {
		ArrayList<URL> urls = new ArrayList<URL>();
		NodeList ns;
		ns = XMLDoc.getElementsByTagName(string);
		for(int i=0; i<ns.getLength(); i++){
			Element n2 = (Element)ns.item(i); 
			try {
				urls.add( new URL(context, n2.getAttribute("schemaLocation") ) );
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return urls;
	}



	//link the nodes
	private void linkNodes(){
		for(Node n : nodes){
			if(n.aux == null || n.aux.isEmpty()) continue;
			for(String id : n.aux.split(";")){
				if(id.isEmpty()) continue;
				Node n2 = getNode(Integer.parseInt(id));
				if(n2 == null){
					System.err.println("Impossible to find node: " + id);
					continue;
				}
				links.add( n.buildLinkTo(n2) );
			}
			n.aux = null;
		}
	}



	private Node getNode(int id){
		for(Node n : nodes)
			if(id == n.id)
				return n;
		return null;
	}

	private Node getNode(URL url) {
		for(Node n : nodes)
			if(n.url.equals(url))
				return n;
		return null;
	}






	public void startDeformation() {
		for(final Node n : nodes)
			n.start();
	}

	public void stopDeformation() {
		for(final Node n : nodes)
			n.stop();
	}


	void decomposeAndConstrain() {

		//points
		for(Node n : nodes){
			n.sp = new GSinglePoint(new GPoint(n.c));
		}

		//distance between points
		for(int i=0; i<nodes.size(); i++){
			GSinglePoint ps1 = nodes.get(i).sp;
			for(int j=i+1; j<nodes.size(); j++){
				GSinglePoint ps2 = nodes.get(j).sp;
				new PointPointMinimalDistance(ps1, ps2, 100, 100);
			}
		}

		//segments
		for(Link lk : links){
			lk.s = new GSegment(lk.n1.sp.getPoint(), lk.n2.sp.getPoint());

			//segments look down
			new SegmentOrientation(lk.s, 10, -Math.PI*0.5);

			//length
			if(lk.isTail() ){
				new SegmentLength(lk.s, 100, 15);
			}
			//if(lk.hasOpposite() ){
			//new SegmentMinimalLength(lk.s, 100, 15);
			//}
			else{
				new SegmentMaximalLength(lk.s, 100, 500);
				new SegmentLength(lk.s, 10, 100);
				new SegmentMinimalLength(lk.s, 100, 15);
			}

		}

		//add scc contraction constraints
		for(StronglyConnectedComponent scc : getSCCS())
			for(Node n:scc.nodes){
				GSegment s = new GSegment(scc.gp, n.sp.getPoint());
				scc.slc.add(new SegmentLength(s, 0.0000001, 0));
			}
	}


	private void initPosition() {
		//int pos = 0;
		for(Node n:nodes){
			n.c.x = 100*Math.random();
			n.c.y = 100*Math.random();
		}
	}


	public void save(String path){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			for(Node n : nodes){
				StringBuffer st = new StringBuffer();
				st.append(n.id);
				st.append(",");
				st.append(n.label);
				st.append(",");
				st.append(n.url);
				st.append(",");
				st.append(n.c.x);
				st.append(",");
				st.append(n.c.y);
				st.append(",");
				for(Link lk : n.linksOut){
					st.append(lk.n2.id);
					st.append(";");
				}
				st.append("\n");
				out.write(st.toString());
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeLog(String logPath){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(logPath));

			out.write("Bidirectional links\n");
			ArrayList<Node[]> bid = new ArrayList<Node[]>();
			for(Link lk : links){
				if(!lk.hasOpposite())
					continue;
				//check if not already printed
				boolean already = false;
				for (Node[] nodes : bid) {
					Node n1 = nodes[0];
					Node n2 = nodes[1];
					if(n1 != lk.n2 || n2 != lk.n1)
						continue;
					already = true;
					break;
				}
				if(already) continue;
				bid.add(new Node[]{lk.n1,lk.n2});
				out.write(lk.n1.url + " <-> " + lk.n2.url+"\n");
			}
			bid=null;

			out.write("\n");
			out.write("Strongly connected components\n");
			for(StronglyConnectedComponent scc : getSCCS()){
				if(scc.nodes.size()==1)
					continue;
				out.write("component with "+scc.nodes.size()+" nodes\n");
				for(Node n:scc.nodes)
					out.write("\t"+n+"\n");
			}

			out.write("\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Vector<StronglyConnectedComponent> sccs = null;
	private int index = 0;
	Vector<StronglyConnectedComponent> getSCCS(){
		if(sccs==null){
			//tarjan's algorithm
			sccs = new Vector<StronglyConnectedComponent>();
			index = 0;
			Stack<Node> stack = new Stack<Node>();
			for(Node node : nodes) {
				if(node.index == null)
					strongConnect(node, stack);
			}
		}
		return sccs;
	}

	//tarjan's algorithm
	private void strongConnect(Node n, Stack<Node> stack) {
		stack.push(n);
		n.lowLk = index;
		n.index = index;
		index++;

		for(Node out : n.getOut()) {
			if(out.index == null) {
				strongConnect(out, stack);
				n.lowLk = Math.min(n.lowLk, out.lowLk);
			}
			else if(stack.contains(out)) {
				n.lowLk = Math.min(n.lowLk, out.index);
			}
		}

		if(n.lowLk == n.index) {
			StronglyConnectedComponent scc = new StronglyConnectedComponent();
			Node out = null;
			while(n != out) {
				out = stack.pop();
				scc.nodes.add(out);
			}
			sccs.add(scc);
		}

	}

	boolean contracted = false;
	public void setIsContracted(boolean c) {
		if(c==contracted) return;
		contracted = c;
		if(c)
			for(StronglyConnectedComponent scc : getSCCS())
				scc.contract();
		else
			for(StronglyConnectedComponent scc : getSCCS())
				scc.decontract();
	}

}
