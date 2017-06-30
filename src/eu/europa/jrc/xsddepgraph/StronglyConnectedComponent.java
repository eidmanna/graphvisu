/**
 * 
 */
package eu.europa.jrc.xsddepgraph;

import java.awt.Color;
import java.util.ArrayList;

import org.opencarto.server.algo.deformation.base.GPoint;
import org.opencarto.server.algo.deformation.constraint.SegmentLength;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class StronglyConnectedComponent {
	final static Color col = new Color(0,0,0,50);
	private final static int bufDist=7;
	private final static int erosionDist=30;
	private final static int bufPtNb=2;

	ArrayList<Node> nodes = new ArrayList<Node>();

	private Coordinate c = new Coordinate(0,0);
	private void updatePosition(){
		double sX=0,sY=0;
		for(Node n:nodes){
			sX+=n.c.x; sY+=n.c.y;
		}
		c.x=sX/nodes.size();
		c.y=sY/nodes.size();
	}

	GPoint gp = new GPoint(c);
	ArrayList<SegmentLength> slc = new ArrayList<SegmentLength>();

	public Polygon getGeometry(){
		GeometryFactory gf = new GeometryFactory();
		Geometry geom = gf.createGeometryCollection(new Geometry[0]);
		for(Node n1:nodes)
			for(Link lk : n1.linksOut){
				Node n2=lk.n2;
				if(!nodes.contains(n2)) continue;
				LineString ls = gf.createLineString(new Coordinate[]{n1.c,n2.c});
				geom = geom.union(ls.buffer(bufDist,bufPtNb));
			}
		Polygon p = gf.createPolygon((LinearRing) ((Polygon)geom).getExteriorRing(), null);
		try {
			return (Polygon)p.buffer(erosionDist,bufPtNb).buffer(-erosionDist,bufPtNb);
		} catch (Exception e) {
			return p;
		}
	}

	public void contract() {
		updatePosition();
		for(SegmentLength sl:slc)
			sl.setImportance(5000);
	}

	public void decontract() {
		for(SegmentLength sl:slc)
			sl.setImportance(0.0000001);
	}

}
