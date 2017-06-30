/**
 * 
 */
package eu.europa.jrc.xsddepgraph;

import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import org.opencarto.server.algo.deformation.base.GPoint;
import org.opencarto.server.algo.deformation.submicro.GSinglePoint;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author julien Gaffuri
 *
 */
public class Node implements Comparable<Node>, Runnable {
	private static int COUNT = 1;

	int id = -1;
	String label = null;
	URL url = null;
	Coordinate c = null;
	ArrayList<Link> linksOut = null;
	ArrayList<Link> linksIn = null;
	String aux = null;

	public Integer index = null;
	public Integer lowLk = null;

	GSinglePoint sp;

	public Node(URL url){
		this.id = COUNT++;
		this.url = url;

		// /schemas/wfd/0.0/WaterFrameworkDirective.xsd
		String[] data = url.getFile().split("/");
		this.label = data[data.length-1];
		if (this.label.endsWith(".xsd"))
			this.label = this.label.substring(0, this.label.length()-4);
		String version = data[data.length-2];
		if(isVersionNumber(version))
			this.label = version + "/" + this.label;

		c = new Coordinate(0,0);
		linksOut = new ArrayList<Link>();
		linksIn = new ArrayList<Link>();
	}

	private static boolean isVersionNumber(String v) {
		/*String[] data = v.split(".");
		System.out.println("      " + v);
		for (String n : data) {
			System.out.println(n);
			Integer.parseInt(n);
		}*/
		return true;
	}

	public Link buildLinkTo(Node n){
		Link lk = new Link();
		lk.n1 = this;
		lk.n2 = n;
		linksOut.add(lk);
		n.linksIn.add(lk);
		return lk;
	}

	int getInDegree(){
		return linksIn.size();
	}

	int getOutDegree(){
		return linksOut.size();
	}

	int getDegree(){
		return getInDegree() + getOutDegree();
	}

	boolean isSingle(){
		return getDegree() == 0;
	}

	boolean isTail(){
		return getDegree() == 1;
	}

	boolean isCorridor(){
		return getInDegree() == 1 && getOutDegree() == 1;
	}

	boolean isPureTop(){
		return getOutDegree()>1 &&  getInDegree() == 0;
	}

	boolean isPureBottom(){
		return getInDegree()>1 &&  getOutDegree() == 0;
	}

	boolean isSuperPureTop(){
		return getOutDegree()>3 &&  getInDegree() == 0;
	}

	boolean isSuperPureBottom(){
		return getInDegree()>3 &&  getOutDegree() == 0;
	}

	public int compareTo(Node ob) {
		if (ob.id < id) return -1;
		else if (ob.id > id) return 1;
		return 0;
	}

	@Override
	public String toString() {
		StringBuffer st = new StringBuffer()
		.append(id)
		.append(" ")
		.append(label)
		.append(" ")
		.append(url)
		;
		return st.toString();
	}


	public Vector<Node> getOut() {
		Vector<Node> out = new Vector<Node>();
		for(Link lk : linksOut)
			out.add(lk.n2);
		return out;
	}


	@Override
	public void run() {
		GPoint gp = sp.getPoint();
		while(th!=null){
			if(gp.isFrozen()){
				try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
				continue;
			}
			gp.displace(gp.getdisplacement());
			try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}

	private Thread th=null;
	public void start() {
		th=new Thread(this);
		th.start();
	}

	public void stop() {
		th=null;
	}

}
