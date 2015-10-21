package de.unirostock.sems.masymos.extractor.CellML;

import java.util.concurrent.Callable;

import org.neo4j.graphdb.Node;


public class CellMLExtractorThread implements Callable<Node> {

	private String filePath;
	private String versionID;
	
	public CellMLExtractorThread(String filePath, String versionID){
		this.versionID = versionID;
		this.filePath = filePath;
	}
	
	public CellMLExtractorThread(String filePath){
		this.versionID = null;
		this.filePath = filePath;
	}
	
	@Override
	public Node call() throws Exception {
		
		return CellMLExtractor.extractStoreIndex(filePath, versionID);
	}

}
