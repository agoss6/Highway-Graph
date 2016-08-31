import java.awt.Point;
import java.util.Set;

// class to represent a City, which will be the data type in the Vertexes of the graph

public class City{
	private String myName;
	private int myX, myY;    // location
	private static final int TOLERANCE = 8; // tolerance for the isNear method
	
	public City(String name, int x, int y){
		myName = name;
		myX = x;
		myY = y;
	}
	
	public String getName(){
		return myName;
	}
	
	public Point getLocation(){
		return new Point(myX, myY);
	}
	
	// determines if a point p is near enough to the city to be considered on it
	public boolean isNear(Point p){
		Point myLocation = getLocation();
		return myLocation.distance(p) <= TOLERANCE;
	}
	
	
	

}
