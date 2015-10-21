package de.unirostock.sems.masymos.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Node;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.database.traverse.DBModelTraverser;

public class Database {
	
	public static List<String> getAllStoredDocumentURIs(){
		List<String> uriList = new LinkedList<>();
		List<Node> documentNodes = DBModelTraverser.getAllNodesWithLabel(NodeLabel.Types.DOCUMENT);
		for (Iterator<Node> iterator = documentNodes.iterator(); iterator.hasNext();) {
			Node docNode = (Node) iterator.next();
			uriList.add((String)docNode.getProperty(Property.General.URI, ""));
		}
		return uriList;
	}
	
	public static List<String> getAllStoredDocumentFilenames(){
		List<String> uriList = new LinkedList<>();
		List<Node> documentNodes = DBModelTraverser.getAllNodesWithLabel(NodeLabel.Types.DOCUMENT);
		for (Iterator<Node> iterator = documentNodes.iterator(); iterator.hasNext();) {
			Node docNode = (Node) iterator.next();
			uriList.add((String)docNode.getProperty(Property.General.FILENAME, ""));
		}
		return uriList;
	}

}
