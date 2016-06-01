package de.unirostock.sems.masymos.extractor.SedML;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;

import org.neo4j.graphdb.Node;

import de.unirostock.sems.masymos.database.IdFactory;
import de.unirostock.sems.masymos.extractor.Extractor;


public class SEDMLExtractorThread  implements Callable<Node> {

	private String filePath;
	private String versionID;
	private Long uID;
	
	public SEDMLExtractorThread(String filePath, String versionID){
		this.versionID = versionID;
		this.filePath = filePath;
		this.uID = IdFactory.instance().getID();
	}
	
	public SEDMLExtractorThread(String filePath){
		this.versionID = null;
		this.filePath = filePath;
		this.uID = IdFactory.instance().getID();
	}
	
	@Override
	public Node call() throws Exception {
		URL url = new URL(filePath);
		StringBuffer sb = new StringBuffer();
	       try {

	            InputStream is = url.openStream();
	            BufferedReader br = new BufferedReader(new InputStreamReader(is));
	             
	            String line;
	            while ( (line = br.readLine()) != null)
	                sb.append(line);
	             
	            br.close();
	            is.close();
	             
	        } catch (Exception e) {
	            e.printStackTrace();
	        }   
	    Node documentNode = SEDMLExtractor.extractStoreIndexSEDML(sb.toString(),versionID, uID);
	    Extractor.setDocumentUID(documentNode, uID);
	    return documentNode;
	}
}