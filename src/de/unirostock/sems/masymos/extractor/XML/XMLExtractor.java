package de.unirostock.sems.masymos.extractor.XML;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unirostock.sems.masymos.extractor.Extractor;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/


public class XMLExtractor extends Extractor{

	final static Logger logger = LoggerFactory.getLogger(XMLExtractor.class);
	
	public static Node extractStoreIndex(InputStream stream, String versionID) throws XMLStreamException, IOException{
		
		Node documentNode = null;	
		try (Transaction tx = graphDB.beginTx()){
			documentNode = extractFromXML(stream, versionID);
			tx.success();
		} catch (XMLStreamException e) {
			documentNode = null;
			logger.error("Error XMLStreamException while parsing model");
			logger.error(e.getMessage());
		}
		return documentNode;
	}
	

	private static Node extractFromXML(InputStream stream, String versionID) throws XMLStreamException {
		throw new XMLStreamException("Format not supported, yet");
		
	}
	
}
