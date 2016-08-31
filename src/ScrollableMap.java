/**

 * ScrollableMap.java

 * Class for a scrollable roadmap that responds to user actions.

 * For CS 10 Lab Assignment 4.

 * 

 * @author Yu-Han Lyu, Tom Cormen, and YOU

 */



import java.awt.*;


import java.awt.event.*;
import java.beans.Visibility;

import javax.swing.*;
import javax.swing.Box.Filler;
import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.xml.transform.Source;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import net.datastructures.*;



public class ScrollableMap extends JLabel implements Scrollable,

MouseMotionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	// The first two instance variables are independent of our roadmap application.

	private int maxUnitIncrement = 1;         // increment for scrolling by dragging

	private boolean missingPicture = false;   // do we have an image to display?



	private JLabel infoLabel;                 // where to display the result, in words

	private JButton destButton;               // the destination button, so that it can be enabled

	private RoadMap roadmap;                  // the roadmap

	// ADD OTHER INSTANCE VARIABLES AS NEEDED.

	private boolean findingSource = true;   // shows whether we're finding a source or a dest
	private boolean usingTime = false;  // shows whether we're using the time weight or distance weight
	private Vertex<City> source = null; // initialize source to null
	private Vertex<City> dest = null; // initialize dest to null
	private Map<Vertex<City>, Vertex<City>> predecessorMap;  // map whose keys are vertices and values are predecessors
	private Map<Vertex<City>, Double> cityDistances;
	private Vertex<City> center = null;
	
	
	// method that uses Dijkstra's algorithm to compute the shortest path using edge weights
	public Map<Vertex<City>, Vertex<City>> dijkstra(Vertex<City> s){
		Map<Vertex<City>, Vertex<City>> predecessors = new HashMap<Vertex<City>, Vertex<City>>(); // maps vertices to predecessors
		cityDistances = new HashMap<Vertex<City>, Double>(); // maps vertices to their distances from the source
		Map<Edge<Highway>, Double> edgeWeights = new HashMap<Edge<Highway>, Double>(); // maps edges to their weights
		
		Iterator<Edge<Highway>> edgeIter = roadmap.edgeIter(); // iterator over the edges
		Iterator<Vertex<City>> vertIter = roadmap.vertIter(); // iterator over the vertices
		
		// the min priority queue to hold the vertices and their distances from the source
		HeapPriorityQueue<Double, Vertex<City>> pq = new HeapPriorityQueue<Double, Vertex<City>>();
		
		// fill up the edgeWeights map
		while(edgeIter.hasNext()){
			Edge<Highway> anEdge = edgeIter.next();	
			if(usingTime)  // make the map with time weights
				edgeWeights.put(anEdge, anEdge.getElement().getTime());
			else   // make the map with distance weights
				edgeWeights.put(anEdge, anEdge.getElement().getDisance());
		}
		
		// fill up the cityDistances map with initial distance of 0 if the source, infinity if not
		// also fill up the predecessors map with null values
		while(vertIter.hasNext()){
			Vertex<City> aCity = vertIter.next();

			if(aCity == s){
				cityDistances.put(aCity, 0.0);
				pq.insert(0.0, aCity);
			}
			else{
				cityDistances.put(aCity, Double.POSITIVE_INFINITY);
				pq.insert(Double.POSITIVE_INFINITY, aCity);
			}
			predecessors.put(aCity, null);
		}
		
		// Dijkstra's algorithm
		while(!pq.isEmpty()){
			Entry<Double, Vertex<City>> entry = pq.removeMin();
			Double distance = entry.getKey();
			Vertex<City> u = entry.getValue();
			
			// find the adjacent vertices by following all of the outgoing edges
			Iterable<Edge<Highway>> outgoing = roadmap.getRoadmap().outgoingEdges(u);
			for(Edge<Highway> e : outgoing){
				Vertex<City> v = roadmap.getRoadmap().endVertices(e)[1];
				if(distance + edgeWeights.get(e) < cityDistances.get(v)){
					cityDistances.put(v, distance + edgeWeights.get(e) );
					predecessors.put(v, u);
					pq.insert(distance + edgeWeights.get(e), v);
				}
			}
		}
		return predecessors;
	}
	
	

	/**

	 * Constructor.

	 * @param i the highway roadmap image

	 * @param m increment for scrolling by dragging

	 * @param infoLabel where to display the result

	 * @param destButton the destination button

	 * @param roadmap the RoadMap object, a graph
	 * @throws InterruptedException 

	 */

	public ScrollableMap(ImageIcon i, int m, JLabel infoLabel, JButton destButton, RoadMap roadmap) throws InterruptedException {

		super(i);

		if (i == null) {

			missingPicture = true;

			setText("No picture found.");

			setHorizontalAlignment(CENTER);

			setOpaque(true);

			setBackground(Color.white);

		}

		maxUnitIncrement = m;

		this.infoLabel = infoLabel;

		this.destButton = destButton;

		this.roadmap = roadmap;



		// Let the user scroll by dragging to outside the window.

		setAutoscrolls(true);         // enable synthetic drag events

		addMouseMotionListener(this); // handle mouse drags

		addMouseListener(this);
		
		this.requestFocus();
	


		findSource();     // start off by having the user click a source city

	}



	// Methods required by the MouseMotionListener interface:

	@Override

	public void mouseMoved(MouseEvent e) { 	}



	@Override

	public void mouseDragged(MouseEvent e) {

		// The user is dragging us, so scroll!

		Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);

		scrollRectToVisible(r);

	}


	// Draws the map and shortest paths, as appropriate.

	// If shortest paths have been computed, draws either the entire shortest-path tree

	// or just a shortest path from the source vertex to the destination vertex.
	
	@Override

	public void paintComponent(Graphics page) {

		Graphics2D page2D = (Graphics2D) page;

		setRenderingHints(page2D);

		super.paintComponent(page2D);

		Stroke oldStroke = page2D.getStroke();  // save the current stroke

		page2D.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT,

				BasicStroke.JOIN_MITER));



		// YOU FILL IN THIS PART.

		// If shortest paths have been computed and there is a destination vertex, draw

		// a shortest path from the source vertex to the destination vertex.

		// If shortest paths have been computed and there is not a destination vertex,

		// draw the entire shortest-path tree.

		// If shortest paths have not been computed, draw nothing.
		
		if(center != null)
			page.drawOval(center.getElement().getLocation().x, center.getElement().getLocation().y, 50, 50);
		
		if(source != null && dest != null){
			Vertex<City> current = dest;
			while(predecessorMap.get(current) != null){
				
				Point p1 = current.getElement().getLocation();
				Point p2 = predecessorMap.get(current).getElement().getLocation();
				page.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
				current = predecessorMap.get(current);
				

			}
		}
		
		if(source != null && dest == null){
			Iterator<Vertex<City>> iterator = predecessorMap.keySet().iterator();
			
			while(iterator.hasNext()){
				Vertex<City> current = iterator.next();

				while(predecessorMap.get(current) != null){
					Point p1 = current.getElement().getLocation();
					Point p2 = predecessorMap.get(current).getElement().getLocation();
					page.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
					current = predecessorMap.get(current);
				}
			}
		}
		
		else{}
		
		
		page2D.setStroke(oldStroke);    // restore the saved stroke

	}



	// Enable all rendering hints to enhance the quality.

	public static void setRenderingHints(Graphics2D page) {

		page.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,

				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		page.setRenderingHint(RenderingHints.KEY_ANTIALIASING,

				RenderingHints.VALUE_ANTIALIAS_ON);

		page.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,

				RenderingHints.VALUE_COLOR_RENDER_QUALITY);

		page.setRenderingHint(RenderingHints.KEY_INTERPOLATION,

				RenderingHints.VALUE_INTERPOLATION_BICUBIC);

	}



	// Methods required by the MouseListener interface.



	// When the mouse is clicked, find which vertex it's over.

	// If it's over a vertex and we're finding the source,

	// record the source, clear the destination, enable the destination

	// button, and find and draw the shortest paths from the source.

	// If it's over a vertex and we're finding the destination, record

	// the destination, and find and draw a shortest path from the source

	// to the destination.

	public void mouseClicked(MouseEvent e) {
		Vertex<City> clickedOn = roadmap.cityAt(e.getPoint());

		if(clickedOn != null && findingSource){
			dest = null;
			source = clickedOn;
			destButton.setEnabled(true);
			predecessorMap = dijkstra(source);
			infoLabel.setText("Origin city is: " + clickedOn.getElement().getName());
			repaint();
			
		}

		if(clickedOn != null && !findingSource){
			dest = clickedOn;
			predecessorMap = dijkstra(source);
			String conditional = (usingTime ? cityDistances.get(dest)+" hours." : cityDistances.get(dest)+" miles.");
			infoLabel.setText("From "+source.getElement().getName()+" to "+dest.getElement().getName()+" is " + conditional);
			repaint();
		}
	}



	public void mousePressed(MouseEvent e) { }

	public void mouseReleased(MouseEvent e) { }

	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e) { }



	// Return the preferred size of this component.

	@Override

	public Dimension getPreferredSize() {

		if (missingPicture)

			return new Dimension(320, 480);

		else

			return super.getPreferredSize();

	}



	// Needs to be here.

	@Override

	public Dimension getPreferredScrollableViewportSize() {

		return getPreferredSize();

	}



	// Needs to be here.

	@Override

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,

			int direction) {

		// Get the current position.

		int currentPosition = 0;

		if (orientation == SwingConstants.HORIZONTAL)

			currentPosition = visibleRect.x;

		else

			currentPosition = visibleRect.y;



		// Return the number of pixels between currentPosition

		// and the nearest tick mark in the indicated direction.

		if (direction < 0) {

			int newPosition = currentPosition - (currentPosition / maxUnitIncrement)

					* maxUnitIncrement;

			return (newPosition == 0) ? maxUnitIncrement : newPosition;

		}

		else

			return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement

					- currentPosition;

	}



	// Needs to be here.

	@Override

	public int getScrollableBlockIncrement(Rectangle visibleRect,

			int orientation, int direction) {

		if (orientation == SwingConstants.HORIZONTAL)

			return visibleRect.width - maxUnitIncrement;

		else

			return visibleRect.height - maxUnitIncrement;

	}



	// Needs to be here.

	@Override

	public boolean getScrollableTracksViewportWidth() {

		return false;

	}



	// Needs to be here.

	@Override

	public boolean getScrollableTracksViewportHeight() {

		return false;

	}



	// Needs to be here.

	public void setMaxUnitIncrement(int pixels) {

		maxUnitIncrement = pixels;

	}



	// Called when the source button is pressed.
	// reset the state of the graph and tell the program we are looking for a source
	public void findSource() throws InterruptedException{
		source = null;
		dest = null;
		findingSource = true;
	}



	// Called when the destination button is pressed.
	// reset the destination and tell the program we are looking for a destination
	public void findDest() {
		dest = null;
		findingSource = false;
	}



	// Called when the time button is pressed.  Tells the roadmap to use time

	// for edge weights, and finds and draws shortest paths.

	public void useTime() {
		usingTime = true;
		if(source != null)
			predecessorMap = dijkstra(source);
		repaint();

	}



	// Called when the distance button is pressed.  Tells the roadmap to use distance

	// for edge weights, and finds and draws shortest paths.

	public void useDistance() {
		usingTime = false;
		if(source != null)
			predecessorMap = dijkstra(source);
		repaint();
	}
	
	public void findDiameter(){
		Iterator<Vertex<City>> vertexIter = roadmap.vertIter();
		Vertex<City> origin = null;
		Vertex<City> max = null;
		Map<Vertex<City>, Double> tempCityDistances = null;
		for(Vertex<City> u = vertexIter.next() ; vertexIter.hasNext(); u = vertexIter.next()){
			predecessorMap = dijkstra(u);
			if(max == null)
				max = u;
				tempCityDistances = cityDistances;
			
			Iterator<Vertex<City>> keyIter = cityDistances.keySet().iterator();
			for(Vertex<City> v = keyIter.next(); keyIter.hasNext(); v = keyIter.next()){
				if(tempCityDistances.get(max) < cityDistances.get(v) && cityDistances.get(v)!=Double.POSITIVE_INFINITY){
					max = v;
					origin = u;
					tempCityDistances = cityDistances;
				}
			}
		}
		source = origin;
		dest = max;
		infoLabel.setText("The diameter of the graph is "+ tempCityDistances.get(dest) + " miles.");
		repaint();
	}
	
//	public void findCenter(){
//		Iterator<Vertex<City>> vertexIter = roadmap.vertIter();
//		Vertex<City> max;
//		
//		double radius = Double.POSITIVE_INFINITY;
//		double maxPathLength = 0;
//		
//		for(Vertex<City> u = vertexIter.next(); vertexIter.hasNext(); u = vertexIter.next()){
//			predecessorMap = dijkstra(u);
//			if(center == null){
//				center = u;
//			}
//
//			cityDistances.put(center, radius);
//			cityDistances.put(max, maxPathLength);
//			//System.out.println("hi");
//			Iterator<Vertex<City>> keyIter = cityDistances.keySet().iterator();
//			
//			for(Vertex<City> v = keyIter.next(); keyIter.hasNext(); v = keyIter.next()){
//				//System.out.println("bye");
//				if(cityDistances.get(v) > maxPathLength && cityDistances.get(v)!=Double.POSITIVE_INFINITY){
//					System.out.println("bonjour");
//					maxPathLength = cityDistances.get(v);
//					max = v;
//				}
//			}
//			if(cityDistances.get(max) < radius){
//				System.out.println("Au revoir");
//				radius = cityDistances.get(max);
//				System.out.println(radius);
//
//				center = max;
//			}
//			System.out.println(center.getElement().getName());
//
//		}
//	}




	
	public void findCenter(){
		Iterator<Vertex<City>> vertexIter = roadmap.vertIter();
		Vertex<City> origin = null;
		Vertex<City> tempCenter = null;
		Vertex<City> max = null;
		Vertex<City> maxMinPath = null;
		Map<Vertex<City>, Double> tempMaxCityDistances = null;
		Map<Vertex<City>, Double> tempCenterCityDistances = null; 
		
		for(Vertex<City> u = vertexIter.next(); vertexIter.hasNext(); u = vertexIter.next()){
			predecessorMap = dijkstra(u);
			if(max==null)
				max = u;
				tempMaxCityDistances = cityDistances;
	
			Iterator<Vertex<City>> keyIter = cityDistances.keySet().iterator();
			
			for(Vertex<City> v = keyIter.next(); keyIter.hasNext(); v = keyIter.next()){
				
				if(tempMaxCityDistances.get(max) < cityDistances.get(v) && cityDistances.get(v)!=Double.POSITIVE_INFINITY){
					max = v;
					origin = u;
					tempMaxCityDistances = cityDistances;
				}
				
				if(maxMinPath== null || tempCenterCityDistances.get(maxMinPath) > tempMaxCityDistances.get(max)){
					maxMinPath = max;
					tempCenter = origin;
					tempCenterCityDistances = tempMaxCityDistances;
				}
		}
	}
	center = tempCenter;


	}
	

}