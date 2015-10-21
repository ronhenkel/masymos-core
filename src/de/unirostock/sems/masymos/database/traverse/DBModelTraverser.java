package de.unirostock.sems.masymos.database.traverse;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation.AnnotationRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DatabaseRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DocumentRelTypes;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.query.results.ModelResultSet;

public class DBModelTraverser {

	private static GraphDatabaseService graphDB = Manager.instance().getDatabase();
	
	public static Node getDocumentFromModel(Node modelNode) {
		if (modelNode==null) return null;
		Node docNode = null;
		if ((modelNode!=null) && modelNode.hasLabel(NodeLabel.Types.MODEL)){
			docNode = modelNode.getSingleRelationship(DatabaseRelTypes.BELONGS_TO, Direction.OUTGOING).getEndNode();
		} 
		if ((docNode!=null) && docNode.hasLabel(NodeLabel.Types.DOCUMENT)){
			return docNode;
		}
		return null;
		
	}
	
	

	public static Node fromNodeToAnnotation(Node node){
		if (node==null) return null;
		Relationship rs = node.getSingleRelationship(AnnotationRelTypes.HAS_ANNOTATION, Direction.OUTGOING);
		
		if (rs==null) return null;
		Node annoNode = rs.getEndNode();
		
		if ((annoNode!=null) && annoNode.hasLabel(NodeLabel.Types.ANNOTATION)){
			return annoNode;
		}
		return null;
	}
	
	public static List<Node> getModelsFromNode(Node node){
		LinkedList<Node> modelList = new LinkedList<Node>();
		if (node==null) return modelList;
		
		TraversalDescription td = graphDB.traversalDescription()				
				.depthFirst()
	            .relationships( DatabaseRelTypes.BELONGS_TO, Direction.OUTGOING )
	            .evaluator( new NodeTypReturnEvaluater(NodeLabel.Types.MODEL));
		
		Traverser t = td.traverse(node);
		for (Iterator<Path> it = t.iterator(); it.hasNext();) {
			Node tNode = ((Path) it.next()).endNode();
			modelList.add(tNode);
		}		
		return modelList;
	}
	
	public static List<Node> getDocumentsFromNode(Node node){
		List<Node> modelList = getModelsFromNode(node);
		List<Node> documentList = new LinkedList<Node>();
		
		for (Iterator<Node> iterator = modelList.iterator(); iterator.hasNext();) {
			Node modelNode = (Node) iterator.next();
			documentList.add(getDocumentFromModel(modelNode));
		}		
		return documentList;
	}

	public static List<Node> getPersonFromPublication(Node publicationNode) {
		LinkedList<Node> personList = new LinkedList<Node>();
		Iterable<Relationship> ir = publicationNode.getRelationships(DocumentRelTypes.HAS_AUTHOR, Direction.OUTGOING);
		for (Iterator<Relationship> iterator = ir.iterator(); iterator.hasNext();) {
			Relationship r = (Relationship) iterator.next();
			personList.add(r.getOtherNode(publicationNode));			
		}
		return personList;
	}

	
	public static List<ModelResultSet> getModelResultSetFromNode(Node node, float score, String indexSource) {
		List<ModelResultSet> result = new LinkedList<ModelResultSet>();
		List<Node> modelList = getModelsFromNode(node);
		for (Iterator<Node> iterator = modelList.iterator(); iterator.hasNext();) {
			Node modelNode = (Node) iterator.next();
			Node docNode = DBModelTraverser.getDocumentFromModel(modelNode);
			//TODO make this generic somehow, for now it is bound to SBML
			
			// check if model ID is present
			String modelId = null;
			if( modelNode.hasProperty(Property.General.ID) )
				modelId = (String)modelNode.getProperty(Property.General.ID);
			
			// check if model NAME is present
			String modelName = null;
			if( modelNode.hasProperty(Property.General.NAME) )
				modelName = (String)modelNode.getProperty(Property.General.NAME);
			
			// Neither modelId nor modelName is set
			if( (modelId == null || modelId.isEmpty()) && (modelName == null || modelName.isEmpty()) )
				// skip this one
				continue;
				
			ModelResultSet rs = new ModelResultSet(score, modelId, modelName, indexSource);
			if (docNode.hasProperty(Property.General.VERSIONID)) rs.setVersionID((String)docNode.getProperty(Property.General.VERSIONID));
			if (docNode.hasProperty(Property.General.URI)) rs.setDocumentURI((String)docNode.getProperty(Property.General.URI));
			if (docNode.hasProperty(Property.General.FILENAME)) rs.setFilename((String)docNode.getProperty(Property.General.FILENAME));
			if (docNode.hasProperty(Property.General.FILEID)) rs.setFileId((String)docNode.getProperty(Property.General.FILEID));
			if (docNode.hasProperty(Property.General.XMLDOC)) rs.setXmldoc((String)docNode.getProperty(Property.General.XMLDOC));
			result.add(rs);
			}
		return result;
	}
	
	public static List<Node> getAllNodesWithLabel(Label label){
		List<Node> nodes = new LinkedList<Node>();		
		try (Transaction tx = Manager.instance().createNewTransaction())
		{
			for (ResourceIterator<Node> resourceNodeListIterator = Manager.instance().getDatabase().findNodes(label); resourceNodeListIterator.hasNext();) {
				nodes.add( (Node) resourceNodeListIterator.next());			
			}
			
			tx.success();
		}

		return nodes;
	}




}
