package de.unirostock.sems.masymos.query.structure;

import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Notification;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.Iterators;

import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.database.Manager;


public class StructureQuery {
	
	public static void runCypherQuery(String cquery){
		
		GraphDatabaseService graphDB = Manager.instance().getDatabase();
		Result result = graphDB.execute(cquery); 
		List<String> columns = result.columns(); 
		for (Iterator<String> iterator = columns.iterator(); iterator.hasNext();) {
			String col = (String) iterator.next();
			Iterator<Node> n_column = result.columnAs( col );
			for ( Node node : Iterators.asIterable( n_column ) )
			{
			    System.out.print(node + ": " + node.getProperty( Property.General.ID,"-" ) + " " +
			    				node.getProperty(Property.General.NAME, "-"));
			}
			System.out.println();
		}
	}
	
public static void runCypherQueryAndNotification(String cquery){
		
		GraphDatabaseService graphDB = Manager.instance().getDatabase();
		Result result = graphDB.execute(cquery); 
		 
		for (Iterator<Notification> iterator = result.getNotifications().iterator(); iterator.hasNext();) {
			Notification note = (Notification) iterator.next();
			
			System.out.println(note.getDescription());
		}
	}

}
