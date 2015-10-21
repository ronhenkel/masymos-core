package de.unirostock.sems.masymos.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;




public class ModelLookup {

	static GraphDatabaseService graphDB =  Manager.instance().getDatabase() ;
	
	public static List<String> getModelHistory(String fileID){
	
		Result result;
		List<String> versionIdList = new LinkedList<String>();
		try ( Transaction ignored =graphDB.beginTx() )
		{
		    result =  graphDB.execute( "match (d:DOCUMENT) where d.FILEID=\""+ fileID+"\" return d.VERSIONID as VersionId" );
			    
			
			for (ResourceIterator<String> iterator = result.columnAs("VersionId"); iterator.hasNext();)
			{
				String s = (String) iterator.next();
				versionIdList.add(s);
			}
			ignored.success();
		}
		return versionIdList;		
	}

	public static Map<String,String>getModelVersion(String fileID, String versionID){
		 
		Result result;
		Map<String, String> modelMap = new HashMap<String, String>();
		try ( Transaction ignored =graphDB.beginTx() )
		{
		    result = graphDB.execute( "match (d:DOCUMENT) where (d.FILEID=\""+ fileID+"\") AND (d.VERSIONID=\""+ versionID +"\") return d" );
			   
			
			modelMap.put("fileId", fileID);
			modelMap.put("versionId", versionID);
			
			for (ResourceIterator<Node> iterator = result.columnAs("d"); iterator.hasNext();)
			{
				Node n = (Node) iterator.next();
				
			    modelMap.put("meta", (String) n.getProperty("META", ""));
			    modelMap.put("xmldoc", (String) n.getProperty("XMLDOC", ""));
			  //TODO ensure uniqueness...something like iterator.next() -> throw exception?
			}
			ignored.success();
		}
		return modelMap;		
	}
	
	public static Map<String, String> getModel(String fileID){

		Result result;
		Map<String, String> modelMap = new HashMap<String, String>();
		try ( Transaction ignored =graphDB.beginTx() )
		{
			
			result = graphDB.execute( "match (d:DOCUMENT) where (d.FILEID=\""+ fileID+"\") AND NOT((d)-[:HAS_SUCCESSOR]->()) return d" );

			//check if result is empty
			modelMap.put("fileId", fileID);
					
			for (ResourceIterator<Node> iterator = result.columnAs("d"); iterator.hasNext();)
			{
				Node n = (Node) iterator.next();
				
			    modelMap.put("versionId", (String) n.getProperty("VERSIONID", ""));
			    modelMap.put("meta", (String) n.getProperty("META", ""));
			    modelMap.put("xmldoc", (String) n.getProperty("XMLDOC", ""));
			  //TODO ensure uniqueness...something like iterator.next() -> throw exception?
			}
			 ignored.success();
		}
		return modelMap;		
				
	}
	
	public static List<Node> getModelHistoryNodes(String fileID){
		
		Result result;
		List<Node> versionIdList = new LinkedList<Node>();
		try ( Transaction ignored =graphDB.beginTx() )
		{
		    result = graphDB.execute( "match (d:DOCUMENT) where d.FILEID=\""+ fileID+"\" return d" );
			    
			
			for (ResourceIterator<Node> iterator = result.columnAs("d"); iterator.hasNext();)
			{
				Node n = (Node) iterator.next();
				versionIdList.add(n);
			}
			ignored.success();
		}
		return versionIdList;		
	}

	public static Node getModelVersionNode(String fileID, String versionID){
		 
		Result result;
		Node n = null;
		try ( Transaction ignored =graphDB.beginTx() )
		{
		    result = graphDB.execute( "match (d:DOCUMENT) where (d.FILEID=\""+ fileID+"\") AND (d.VERSIONID=\""+ versionID +"\") return d" );
		    
			for (ResourceIterator<Node> iterator = result.columnAs("d"); iterator.hasNext();)
			{
				n = (Node) iterator.next();
				//TODO ensure uniqueness...something like iterator.next() -> throw exception?
			}
			ignored.success();
		}
		return n;		
	}
	
	public static Node getModelNode(String fileID){

		Result result;
		Node n = null;
		try ( Transaction ignored =graphDB.beginTx() )
		{
			
			result = graphDB.execute( "match (d:DOCUMENT) where (d.FILEID=\""+ fileID+"\") AND NOT((d)-[:HAS_SUCCESSOR]->()) return d" );
					
			for (ResourceIterator<Node> iterator = result.columnAs("d"); iterator.hasNext();)
			{
				n = (Node) iterator.next();
				
			  //TODO ensure uniqueness...something like iterator.next() -> throw exception?
			}
			 ignored.success();
		}
		return n;		
				
	}
	
	
}
