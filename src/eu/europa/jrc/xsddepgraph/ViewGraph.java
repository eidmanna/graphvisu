/**
 * 
 */
package eu.europa.jrc.xsddepgraph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencarto.test.SimpleViewer;


/**
 * @author julien Gaffuri
 *
 */
public class ViewGraph extends JFrame{
	private static final long serialVersionUID = 1L;


	public static void main(String[] args) {
		String path;
		try {
			path = args[0];
		} catch (Exception e) {
			path = "out.txt";
		}
		new ViewGraph(path).setVisible(true);
	}


	String path = null;
	Graph graph;
	SimpleViewer panel;
	JPanel toolBar;

	int nodeSize = 10;
	float linkWidth = 1f;
	private boolean viewNodes = true;
	protected boolean viewNodeLabels = false;
	private boolean viewLinks = true;
	private boolean viewLinkArrows = false;
	protected boolean viewSCCS = false;

	public ViewGraph(String path_){
		super();
		this.path = path_;

		this.graph = new Graph(path);
		graph.decomposeAndConstrain();
		graph.startDeformation();

		panel = new SimpleViewer(){
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				if(viewSCCS){
					//draw strongly connected components
					g2.setColor(StronglyConnectedComponent.col);
					for(StronglyConnectedComponent scc : graph.getSCCS()){
						if(scc.nodes.size()==1)
							continue;
						DrawingUtil.fillPolygon(scc.getGeometry(),this,g2);
					}
				}

				if(over != null){
					//draw background graph
					g2.setColor(Color.LIGHT_GRAY);
					g2.setStroke(new BasicStroke(linkWidth));
					//links
					if(viewLinks){
						for(Link lk : graph.links)
							drawLink(g2,lk,viewLinkArrows);
					}
					//nodes
					if(viewNodes){
						for(Node n : graph.nodes){
							if(n==over) continue;
							if(n.isTail() || n.isCorridor() || n.isSingle()){
								g2.fillOval((int)(coordToPixX(n.c.x)-nodeSize/6), (int)(coordToPixY(n.c.y)-nodeSize/6), nodeSize/3, nodeSize/3);
							} else {
								g2.fillOval((int)(coordToPixX(n.c.x)-nodeSize*0.5), (int)(coordToPixY(n.c.y)-nodeSize*0.5), nodeSize, nodeSize);
							}
						}
					}

					//draw over
					g2.setStroke(new BasicStroke(linkWidth*2));
					//links
					g2.setColor(Color.GREEN);
					for(Link lk : over.linksIn)
						drawLink(g2,lk,true);
					g2.setColor(Color.RED);
					for(Link lk : over.linksOut)
						drawLink(g2,lk,true);
					g2.setStroke(new BasicStroke(linkWidth));
					//nodes
					g2.setColor(Color.GREEN);
					for(Link lk : over.linksIn)
						g2.fillOval((int)(coordToPixX(lk.n1.c.x)-nodeSize*0.5), (int)(coordToPixY(lk.n1.c.y)-nodeSize*0.5), (int)(nodeSize), (int)(nodeSize));
					g2.setColor(Color.RED);
					for(Link lk : over.linksOut)
						g2.fillOval((int)(coordToPixX(lk.n2.c.x)-nodeSize*0.5), (int)(coordToPixY(lk.n2.c.y)-nodeSize*0.5), (int)(nodeSize), (int)(nodeSize));
					g2.setColor(Color.MAGENTA);
					g2.fillOval((int)(coordToPixX(over.c.x)-nodeSize*0.75), (int)(coordToPixY(over.c.y)-nodeSize*0.75), (int)(nodeSize*1.5), (int)(nodeSize*1.5));
					//labels
					g2.setFont(new Font("Arial", Font.BOLD, 10));
					g2.setColor(Color.GREEN);
					for(Link lk : over.linksIn)
						g2.drawString(lk.n1.label, (int)coordToPixX(lk.n1.c.x)+6, (int)coordToPixY(lk.n1.c.y)-6);
					g2.setColor(Color.RED);
					for(Link lk : over.linksOut)
						g2.drawString(lk.n2.label, (int)coordToPixX(lk.n2.c.x)+6, (int)coordToPixY(lk.n2.c.y)-6);
					g2.setColor(Color.MAGENTA);
					g2.setFont(new Font("Arial", Font.BOLD, 14));
					g2.drawString(over.label, (int)coordToPixX(over.c.x)+6, (int)coordToPixY(over.c.y)-6);

				} else {
					//links
					if(viewLinks){
						g2.setStroke(new BasicStroke(linkWidth));
						for(Link lk : graph.links){
							if(lk.isTail()) {
								g2.setColor(Color.LIGHT_GRAY);
								//} else if(lk.hasOpposite()){
								//	g2.setColor(Color.MAGENTA);
							} else {
								g2.setColor(Color.DARK_GRAY);
							}
							drawLink(g2,lk,viewLinkArrows);
						}
					}
					//nodes
					if(viewNodes){
						for(Node n : graph.nodes){
							if(n.isTail() || n.isCorridor() || n.isSingle()){
								g2.setColor(Color.BLACK);
								g2.fillOval((int)(coordToPixX(n.c.x)-nodeSize/6), (int)(coordToPixY(n.c.y)-nodeSize/6), nodeSize/3, nodeSize/3);
							} else if (n.isPureBottom()) {
								g2.setColor(Color.GREEN);
								g2.fillOval((int)(coordToPixX(n.c.x)-nodeSize*0.5), (int)(coordToPixY(n.c.y)-nodeSize*0.5), nodeSize, nodeSize);
							} else if (n.isPureTop()) {
								g2.setColor(Color.RED);
								g2.fillOval((int)(coordToPixX(n.c.x)-nodeSize*0.5), (int)(coordToPixY(n.c.y)-nodeSize*0.5), nodeSize, nodeSize);
							} else {
								g2.setColor(Color.BLACK);
								g2.fillOval((int)(coordToPixX(n.c.x)-nodeSize*0.5), (int)(coordToPixY(n.c.y)-nodeSize*0.5), nodeSize, nodeSize);
							}
						}
					}
					//labels
					if(viewNodeLabels){
						g2.setColor(Color.BLUE);
						g2.setFont(new Font("Arial", Font.PLAIN, 10));
						for(Node n : graph.nodes){
							//if(n.isTail() || n.isCorridor() || n.isSingle())
							//	continue;
							g2.drawString(n.label, (int)coordToPixX(n.c.x)+6, (int)coordToPixY(n.c.y)-6);
						}
					}
				}
			}


			private void drawLink(Graphics2D g, Link lk, boolean withArrow){
				g.drawLine((int)coordToPixX(lk.n1.c.x), (int)coordToPixY(lk.n1.c.y), (int)coordToPixX(lk.n2.c.x), (int)coordToPixY(lk.n2.c.y));

				if(!withArrow) return;

				int s = 5;
				double n1x = coordToPixX(lk.n1.c.x), n1y = coordToPixY(lk.n1.c.y);
				double n2x = coordToPixX(lk.n2.c.x), n2y = coordToPixY(lk.n2.c.y);
				double n = Math.hypot(n2x-n1x, n2y-n1y);
				double ux = (n2x-n1x)/n, uy = (n2y-n1y)/n;
				double mx = (n1x + n2x)*0.5, my = (n1y + n2y)*0.5;

				double m1x = mx - s*(ux*0.5+uy);
				double m1y = my - s*(uy*0.5-ux);
				double m2x = mx - s*(ux*0.5-uy);
				double m2y = my - s*(uy*0.5+ux);
				double m3x = mx + s*ux*0.5;
				double m3y = my + s*uy*0.5;

				g.drawLine((int)m3x, (int)m3y, (int)m1x, (int)m1y);
				g.drawLine((int)m3x, (int)m3y, (int)m2x, (int)m2y);
			}


			Node over;
			Node select;

			@Override
			public void mouseReleased(MouseEvent me) {
				super.mouseReleased(me);

				if(me.getButton()==MouseEvent.BUTTON3) {
					//move the selected coordinate to the click
					if(this.select != null) {
						this.select.c.x = pixToCoordX(me.getX());
						this.select.c.y = pixToCoordY(me.getY());
						this.select.sp.getPoint().setFrozen(false);
						this.select = null;
						repaint();
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent me) {
				super.mousePressed(me);

				if(me.getButton()==MouseEvent.BUTTON3) {
					//get the selected coordinate
					this.select = null;
					double dist, minDist = Double.MAX_VALUE;
					Point pt = new Point(0,0);
					for(Node n : graph.nodes) {
						transform(n.c, pt);
						dist = Math.hypot(me.getX()-pt.x, me.getY()-pt.y);
						if(dist<10 && dist<minDist) {
							minDist = dist;
							this.select = n;
						}
					}

					//move the selected coordinate to the click
					if(this.select != null) {
						this.select.sp.getPoint().setFrozen(true);
						this.select.c.x = pixToCoordX(me.getX());
						this.select.c.y = pixToCoordY(me.getY());
						repaint();
					}
				}

			}

			@Override
			public void mouseDragged(MouseEvent me) {
				super.mouseDragged(me);

				//move the selected coordinate to the click
				if(this.select != null) {
					this.select.c.x = pixToCoordX(me.getX());
					this.select.c.y = pixToCoordY(me.getY());
					repaint();
				}	
			}

			@Override
			public void mouseMoved(MouseEvent me) {
				super.mouseMoved(me);

				//get the coordinate under the pointer
				this.over = null;
				double dist, minDist = Double.MAX_VALUE;
				Point pt = new Point(0,0);
				for(Node n : graph.nodes) {
					transform(n.c, pt);
					dist = Math.hypot(me.getX()-pt.x, me.getY()-pt.y);
					if(dist<20 && dist<minDist) {
						minDist = dist;
						this.over = n;
					}
				}
			}
		};

		//center
		double sX=0,sY=0;
		for(Node n:graph.nodes){
			sX+=n.c.x;
			sY+=n.c.y;
		}
		panel.centre.x = sX/graph.nodes.size();
		panel.centre.y = sY/graph.nodes.size();

		panel.visuScale = 5000;
		panel.setPreferredSize(new Dimension(800, 500));

		//thread for repaint
		final Thread thr=new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					panel.repaint();
					try { Thread.sleep(40); } catch (InterruptedException e) {}
				}
			}
		});
		thr.start();

		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				graph.stopDeformation();
				System.exit(0);
			}
		});


		toolBar = new JPanel();

		/*JButton reloadButton = new JButton("Reload");
		reloadButton.setToolTipText("Reload graph from file " + path);
		reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewGraph.this.graph = new Graph(path);
				ViewGraph.this.graph.makeDeformable();
			}
		});
		toolBar.add(reloadButton);*/

		final JCheckBox nodesCB = new JCheckBox("Nodes", ViewGraph.this.viewNodes);
		nodesCB.setToolTipText("See the nodes");
		nodesCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewGraph.this.viewNodes = nodesCB.isSelected();
			}
		});
		toolBar.add(nodesCB);

		final JCheckBox nodeLabelsCB = new JCheckBox("Node labels", ViewGraph.this.viewNodeLabels);
		nodeLabelsCB.setToolTipText("See the node labels");
		nodeLabelsCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewGraph.this.viewNodeLabels = nodeLabelsCB.isSelected();
			}
		});
		toolBar.add(nodeLabelsCB);

		final JCheckBox linksCB = new JCheckBox("Links", ViewGraph.this.viewLinks);
		linksCB.setToolTipText("See the links");
		linksCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewGraph.this.viewLinks = linksCB.isSelected();
			}
		});
		toolBar.add(linksCB);

		final JCheckBox linkArrowsCB = new JCheckBox("Link arrows", ViewGraph.this.viewLinkArrows);
		linkArrowsCB.setToolTipText("See the link arrows");
		linkArrowsCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewGraph.this.viewLinkArrows = linkArrowsCB.isSelected();
			}
		});
		toolBar.add(linkArrowsCB);

		final JCheckBox sccsCB = new JCheckBox("Cycles", ViewGraph.this.viewSCCS);
		sccsCB.setToolTipText("See the 'strongly connected components'");
		sccsCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewGraph.this.viewSCCS = sccsCB.isSelected();
			}
		});
		toolBar.add(sccsCB);

		final JCheckBox contractionCB = new JCheckBox("Contraction", ViewGraph.this.graph.contracted);
		contractionCB.setToolTipText("Toggle graph contraction");
		contractionCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewGraph.this.graph.setIsContracted(contractionCB.isSelected());
			}
		});
		toolBar.add(contractionCB);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		add(toolBar, BorderLayout.NORTH);
		pack();
	}

}
