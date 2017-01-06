package de.unirostock.sems.masymos.extractor.SBML;

import java.net.URL;
import java.util.concurrent.Callable;

import org.neo4j.graphdb.Node;

import de.unirostock.sems.masymos.database.IdFactory;
import de.unirostock.sems.masymos.extractor.Extractor;


/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/


public class SBMLExtractorThread  implements Callable<Node> {

	private String filePath;
	private String versionID;
	private Long uID;
	
	public SBMLExtractorThread(String filePath, String versionID){
		this.versionID = versionID;
		this.filePath = filePath;
		this.uID = IdFactory.instance().getID();
	}
	
	public SBMLExtractorThread(String filePath){
		this.versionID = null;
		this.filePath = filePath;
		this.uID = IdFactory.instance().getID();
	}
	
	@Override
	public Node call() throws Exception {
		URL url = new URL(filePath);
		Node documentNode = SBMLExtractor.extractStoreIndexSBML(url.openStream(),versionID, uID);
		Extractor.setDocumentUID(documentNode, uID);
		return documentNode;
	}
}
