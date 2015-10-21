package de.unirostock.sems.masymos.extractor.SBML;

import java.net.URL;
import java.util.concurrent.Callable;

import org.neo4j.graphdb.Node;


public class SBMLExtractorThread  implements Callable<Node> {

	private String filePath;
	private String versionID;
	
	public SBMLExtractorThread(String filePath, String versionID){
		this.versionID = versionID;
		this.filePath = filePath;
	}
	
	public SBMLExtractorThread(String filePath){
		this.versionID = null;
		this.filePath = filePath;
	}
	
	@Override
	public Node call() throws Exception {
		URL url = new URL(filePath);
		return SBMLExtractor.extractStoreIndex(url.openStream(),versionID);
	}
}
