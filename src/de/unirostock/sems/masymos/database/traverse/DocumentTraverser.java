package de.unirostock.sems.masymos.database.traverse;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.MultipleFoundException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.database.Manager;

public class DocumentTraverser {

	private static GraphDatabaseService graphDB = Manager.instance().getDatabase();

	public static Node getDocumentByUID(Long uID) {
		Node node = null;
		try (Transaction tx = graphDB.beginTx()){
			node = graphDB.findNode(NodeLabel.Types.DOCUMENT, Property.General.UID, uID);
			tx.success();
		} catch (MultipleFoundException e) {
			return null;
		}
		return node;
	}

	public static List<Node> getAllNodesWithLabel(Label label) {
		List<Node> nodes = new LinkedList<Node>();
		try (Transaction tx = Manager.instance().getDatabase().beginTx()) {
			for (ResourceIterator<Node> resourceNodeListIterator = graphDB.findNodes(label); resourceNodeListIterator.hasNext();) {
				nodes.add((Node) resourceNodeListIterator.next());
			}

			tx.success();
		}
		return nodes;
	}

	public static Node findSingleResourceNodeByURI(String uri) {
		Node node = null;
		try (Transaction tx = graphDB.beginTx()) {
			node =graphDB.findNode(NodeLabel.Types.RESOURCE, Property.General.URI, uri);
			tx.success();
		} catch (MultipleFoundException e) {
			return null;
		}
		return node;
	}
	
	public static List<String> getAllStoredDocumentURIs(){
		List<String> uriList = new LinkedList<>();
		List<Node> documentNodes = getAllNodesWithLabel(NodeLabel.Types.DOCUMENT);
		for (Iterator<Node> iterator = documentNodes.iterator(); iterator.hasNext();) {
			Node docNode = (Node) iterator.next();
			uriList.add((String)docNode.getProperty(Property.General.URI, ""));
		}
		return uriList;
	}
	
	public static List<String> getAllStoredDocumentFilenames(){
		List<String> uriList = new LinkedList<>();
		List<Node> documentNodes = getAllNodesWithLabel(NodeLabel.Types.DOCUMENT);
		for (Iterator<Node> iterator = documentNodes.iterator(); iterator.hasNext();) {
			Node docNode = (Node) iterator.next();
			uriList.add((String)docNode.getProperty(Property.General.FILENAME, ""));
		}
		return uriList;
	}

}