import net.datastructures.*;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.channels.NonWritableChannelException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import javax.print.attribute.standard.RequestingUserName;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;


// Class to represent the interstate highway map as a weighted, directed graph where each
// edge appears in both directions.

public class RoadMap{
	// the roadmap, which is and AdjacenyMapGraph object
	private AdjacencyMapGraph<City, Highway> roadmap = new AdjacencyMapGraph<City, Highway>(true);
	private Vertex<City> clickedOn = null; // initial vertex is null
	
	// constructor that takes two file paths
	public RoadMap(String cityInfoPath, String linkInfoPath) throws IOException{
		// maps city names to vertices holding city objects
		Map<String, Vertex<City>> cityMap = new HashMap<String, Vertex<City>>();
		
		BufferedReader cityReader = new BufferedReader(new FileReader(cityInfoPath));
		BufferedReader highwayReader = new BufferedReader(new FileReader(linkInfoPath));
		
		try{
			String line;
			// read the city file one line at a time and add vertex objects to road map
			while((line = cityReader.readLine()) != null){
				String[] cityInfo = line.split(",");
				City city = new City(cityInfo[0], Integer.parseInt(cityInfo[1]), Integer.parseInt(cityInfo[2]));
				Vertex<City> vertex = roadmap.insertVertex(city);
				cityMap.put(cityInfo[0], vertex);
			}
			
			// read the highway file one line at a time and put two opposite directed edges
			// into the graph for each line
			while((line = highwayReader.readLine()) != null){
				String[] highwayInfo = line.split(",");
				Highway highway = new Highway(Double.parseDouble(highwayInfo[2]), Integer.parseInt(highwayInfo[3]), Integer.parseInt(highwayInfo[4]));
				
				roadmap.insertEdge(cityMap.get(highwayInfo[0]), cityMap.get(highwayInfo[1]), highway);
				roadmap.insertEdge(cityMap.get(highwayInfo[1]), cityMap.get(highwayInfo[0]), highway);
			}
		}
		finally{
			// clean up
			cityReader.close();
			highwayReader.close();
		}
	}
	
	// method to return the vertex at the point that was clicked, if there is one
	public Vertex<City> cityAt(Point p){
		Iterable<Vertex<City>> iterator = roadmap.vertices();  // iterable vertices
		
		// Spent a while learning this syntax before realizing there was a better way.
		// It works so I'm not messing with it.
		Consumer<Vertex<City>> consumer = (x) ->
		{
			if(x.getElement().isNear(p)) 
				clickedOn = x;
		};
		iterator.forEach(consumer);
		
		return clickedOn;
	}
	
	// returns an iterator over the vertices of the graph
	public Iterator<Vertex<City>> vertIter(){
		Iterator<Vertex<City>> iterator = roadmap.vertices().iterator();
		return iterator;
	}
	
	// returns an iterator over the edges of the graph
	public Iterator<Edge<Highway>> edgeIter(){
		Iterator<Edge<Highway>> iterator = roadmap.edges().iterator();
		return iterator;
	}
	
	// returns the roadmap AdjacencyMapGraph
	public AdjacencyMapGraph<City, Highway> getRoadmap(){
		return roadmap;
	}
}
