
// Class to represent an edge of a graph as a highway with a distance and total travel time

public class Highway {
	private double myTime;   // time weight for the edge
	private double myDistance;  // distance weight for the edge
	
	public Highway(double distance, int hours, int minutes){
		myDistance = distance;
		myTime = hours + (minutes/60.0);  // time in hours represented as a double
	}
	 // getter methods
	public double getDisance(){
		return myDistance;
	}
	
	public double getTime(){
		return myTime;
	}

}
