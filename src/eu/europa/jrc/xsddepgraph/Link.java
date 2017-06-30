/**
 * 
 */
package eu.europa.jrc.xsddepgraph;

import org.opencarto.server.algo.deformation.submicro.GSegment;

/**
 * @author julien Gaffuri
 *
 */
public class Link {
	Node n1;
	Node n2;

	GSegment s;

	Link getOpposite(){
		for(Link lk : n2.linksOut){
			if(lk.n2 == n1) return lk;
		}
		return null;
	}

	boolean hasOpposite(){
		return (getOpposite() != null);
	}

	Boolean tail = null;
	boolean isTail(){
		if(tail == null) {
			if (n1.isTail() || n2.isTail())
				tail = new Boolean(true);
			/*else{
				if(!n1.isCorridor() && !n2.isCorridor()){
					tail = new Boolean(false);
				} else {
					Node n = n1.isCorridor()?n1:n2;
					Link lk1 = n.linksIn.get(0);
					Link lk2 = n.linksOut.get(0);
					Link lk = lk1==this?lk2:lk1;
					tail = new Boolean(lk.isTail());
				}
			}*/
			else
				tail = new Boolean(false);
		}
		return tail.booleanValue();
	}

}
