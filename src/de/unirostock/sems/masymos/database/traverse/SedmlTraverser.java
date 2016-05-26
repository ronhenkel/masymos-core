package de.unirostock.sems.masymos.database.traverse;


import java.util.Iterator;
import java.util.LinkedList;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation.DatabaseRelTypes;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.query.results.SedmlResultSet;


public class SedmlTraverser {
	
	private static GraphDatabaseService graphDB = Manager.instance().getDatabase();
	
	public static Node fromSedmlToDocument(Node sedmlNode) {
		if (sedmlNode==null) return null;
		Node docNode = null;
		if ((sedmlNode!=null) && sedmlNode.hasLabel(NodeLabel.Types.SEDML)){
			docNode = sedmlNode.getSingleRelationship(DatabaseRelTypes.BELONGS_TO, Direction.OUTGOING).getEndNode();
		} 
//		if ((sedmlNode!=null) && sedmlNode.hasProperty(Property.General.NODETYPE) && StringUtils.equals(Property.NodeType.MODELREFERENCE,(String) sedmlNode.getProperty(Property.General.NODETYPE))){
//			Node node = sedmlNode.getSingleRelationship(DatabaseRelTypes.BELONGS_TO, Direction.OUTGOING).getEndNode();
//			docNode = node.getSingleRelationship(DatabaseRelTypes.BELONGS_TO, Direction.OUTGOING).getEndNode();
//			
//		} 
		if ((docNode!=null) && docNode.hasLabel(NodeLabel.Types.DOCUMENT)){
			return docNode;
		}
		return null;
		
	}
	
	public static LinkedList<String> getModelreferencesFromNode(Node node){
		LinkedList<String> modelList = new LinkedList<String>();
		if (node==null) return modelList;
		TraversalDescription td = graphDB.traversalDescription()
				.depthFirst()
				.relationships( DatabaseRelTypes.BELONGS_TO, Direction.INCOMING )
				.evaluator( new NodeTypReturnEvaluater(NodeLabel.Types.SEDML_MODELREFERENCE));

		Traverser t = td.traverse(node);
		
		//TODO check and return list of nodes instead of strings
		
		for (Iterator<Path> it = t.iterator(); it.hasNext();) {
			Node tNode = ((Path) it.next()).endNode();
			modelList.add((String)tNode.getProperty(Property.SEDML.MODELSOURCE));
		}	
		
//				
		return modelList;
	}
	

	
	
	public static SedmlResultSet getResultSetSedmlFromNode(Node node, float score) {
		//List<ResultSetSedml> result = new LinkedList<ResultSetSedml>();
		
			Node docNode = SedmlTraverser.fromSedmlToDocument(node);
			String versionId = null;
			if (docNode.hasProperty(Property.General.VERSIONID)) versionId = (String)docNode.getProperty(Property.General.VERSIONID);
			SedmlResultSet rs = new SedmlResultSet(score,versionId,null, getModelreferencesFromNode(docNode),(String)docNode.getProperty(Property.General.URI),(String)docNode.getProperty(Property.General.FILENAME));
		//	result.add(rs);
			
		return rs;
	}
	


}
