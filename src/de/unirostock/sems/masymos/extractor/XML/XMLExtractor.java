package de.unirostock.sems.masymos.extractor.XML;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import de.unirostock.sems.masymos.extractor.Extractor;

public class XMLExtractor extends Extractor{

	
	public static Node extractStoreIndex(InputStream stream, String versionID) throws XMLStreamException, IOException{
		
		Node documentNode = null;	
		try (Transaction tx = graphDB.beginTx()){
			documentNode = extractFromXML(stream, versionID);
			tx.success();
		} catch (XMLStreamException e) {
			documentNode = null;
			//TODO Log me
			System.out.println("Error XMLStreamException while parsing model");
			System.out.println(e.getMessage());
		}
		return documentNode;
	}
	

	private static Node extractFromXML(InputStream stream, String versionID) throws XMLStreamException {
		throw new XMLStreamException("Format not supported, yet");
		
	}
	
}
