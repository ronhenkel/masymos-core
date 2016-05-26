package de.unirostock.sems.masymos.extractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation;
import de.unirostock.sems.masymos.configuration.Relation.DatabaseRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DocumentRelTypes;
import de.unirostock.sems.masymos.data.PersonWrapper;
import de.unirostock.sems.masymos.data.PublicationWrapper;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.extractor.CellML.CellMLExtractor;
import de.unirostock.sems.masymos.extractor.SBML.SBMLExtractor;
import de.unirostock.sems.masymos.extractor.SedML.SEDMLExtractor;
import de.unirostock.sems.masymos.extractor.XML.XMLExtractor;
import de.unirostock.sems.masymos.query.enumerator.PersonFieldEnumerator;
import de.unirostock.sems.masymos.query.enumerator.PublicationFieldEnumerator;
import de.unirostock.sems.masymos.query.types.PersonQuery;
import de.unirostock.sems.masymos.query.types.PublicationQuery;

public abstract class Extractor {
	protected static Index<Node> publicationIndex = Manager.instance().getPublicationIndex();
	protected static Index<Node> personIndex = Manager.instance().getPersonIndex();
	protected static Index<Node> modelIndex = Manager.instance().getModelIndex();	
	//protected static Index<Relationship> relationshipIndex = Manager.instance().getRelationshipIndex();
	protected static Index<Node> annotationIndex = Manager.instance().getAnnotationIndex();
	//protected static Index<Node> constitientIndex = Manager.instance().getConstituentIndex();
	protected static GraphDatabaseService graphDB = Manager.instance().getDatabase();
	protected static Index<Node> sedmlIndex = Manager.instance().getSedmlIndex();
	
	public static Node setExternalDocumentInformation(Node documentNode, Map<String, String> propertyMap){
		if (propertyMap==null) return documentNode;
		
		for (Iterator<String> iterator = propertyMap.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			
			try (Transaction tx = graphDB.beginTx()){
				documentNode.setProperty(key, (String) propertyMap.get(key));
				tx.success();
			}
			
		}
		return documentNode;
	}
	
	public static Node setExternalDocumentInformation(Node documentNode, String filepath, String documentURI ){
		if (StringUtils.isEmpty(documentURI)) documentURI = "";
		if (StringUtils.isEmpty(filepath)) filepath = "";
			
			try (Transaction tx = graphDB.beginTx()) {
				documentNode.setProperty(Property.General.FILENAME, filepath);
				documentNode.setProperty(Property.General.URI, documentURI);
				tx.success();
			}
			

		return documentNode;
	}
	
	public static Node setDocumentUID(Node documentNode, Long uID){
		if (uID==null) return documentNode;
		
		try (Transaction tx = graphDB.beginTx()){
				documentNode.setProperty(Property.General.UID, uID);
				tx.success();
		}
			
		return documentNode;
	}
	
	
	public static Node extractStoreIndex(String path, String modelType, Long uID)
			throws XMLStreamException, IOException {
		switch(modelType){
		case 	Property.ModelType.SBML : return SBMLExtractor.extractStoreIndexSBML(new FileInputStream(path), null, uID);									 
		case 	Property.ModelType.CELLML : return CellMLExtractor.extractStoreIndexCellML(path, null, uID);
		case 	Property.ModelType.SEDML : return SEDMLExtractor.extractStoreIndexSEDML(new File(path), null, uID);
		default : return XMLExtractor.extractStoreIndex(new FileInputStream(path), null);
		}		

	}

	public static Node extractStoreIndex(byte[] byteArray, String modelType, String versionId, Long uID)
			throws XMLStreamException, IOException {
		switch(modelType){
		case 	Property.ModelType.SBML : return SBMLExtractor.extractStoreIndexSBML(new ByteArrayInputStream(byteArray), versionId, uID);									 
		case 	Property.ModelType.CELLML : {
			File temp = File.createTempFile("graphstore_model_temp", "xml");
			FileUtils.writeByteArrayToFile(temp, byteArray);			
			Node n = CellMLExtractor.extractStoreIndexCellML(temp.getPath(), versionId, uID);
			temp.delete();
			return n;
		}
		case 	Property.ModelType.SEDML : {
			File temp = File.createTempFile("graphstore_sed_temp", "xml");
			FileUtils.writeByteArrayToFile(temp, byteArray);			
			Node n = SEDMLExtractor.extractStoreIndexSEDML(temp, versionId, uID);
			temp.delete();
			return n;
		}
		default : return XMLExtractor.extractStoreIndex(new ByteArrayInputStream(byteArray), null);
		}	
		
	}
	
	public static Node extractStoreIndex(URL url, String modelType, String versionId, Long uID)
			throws XMLStreamException, IOException {
		switch(modelType){

		case 	Property.ModelType.SBML : return SBMLExtractor.extractStoreIndexSBML(url.openStream(), versionId, uID);									 
		case 	Property.ModelType.CELLML : return CellMLExtractor.extractStoreIndexCellML(url.toString(), versionId, uID);
		case 	Property.ModelType.SEDML : {		
					File temp = File.createTempFile(url.getFile(), "xml");
					FileUtils.copyURLToFile(url, temp);			
					Node n = SEDMLExtractor.extractStoreIndexSEDML(temp, versionId, uID);
					temp.delete();
					return n;
			} 
		default : return XMLExtractor.extractStoreIndex(new FileInputStream(url.getFile()), versionId);
		}	
		
	}
	
	public static Node extractStoreIndex(URL url, String modelType, Long uID)
			throws XMLStreamException, IOException {
		switch(modelType){

		case 	Property.ModelType.SBML : return SBMLExtractor.extractStoreIndexSBML(url.openStream(), null, uID);									 
		case 	Property.ModelType.CELLML : return CellMLExtractor.extractStoreIndexCellML(url.toString(), null, uID);
		case 	Property.ModelType.SEDML : {		
					File temp = File.createTempFile(url.getFile(), "xml");
					FileUtils.copyURLToFile(url, temp);			
					Node n = SEDMLExtractor.extractStoreIndexSEDML(temp, null, uID);
					temp.delete();
					return n;
			} 
		default : return XMLExtractor.extractStoreIndex(new FileInputStream(url.getFile()), null);
		}	
		
	}
	
	public static Node extractStoreIndex(File file, String modelType, String versionId, Long uID)
			throws XMLStreamException, IOException {
		switch(modelType){
		case 	Property.ModelType.SBML : return SBMLExtractor.extractStoreIndexSBML(new FileInputStream(file),  versionId, uID);									 
		case 	Property.ModelType.CELLML : return CellMLExtractor.extractStoreIndexCellML(file.getPath(),  versionId, uID);
		case 	Property.ModelType.SEDML : return SEDMLExtractor.extractStoreIndexSEDML(file,  versionId, uID);
		default : return XMLExtractor.extractStoreIndex(new FileInputStream(file),  versionId);
		}			
	}
	


	
	protected static void processPublication(PublicationWrapper publication, Node referenceNode, Node modelNode) {
		//all Strings null -> ""
		publication.repairNullStrings();
		
		Node publicationNode = null;
		try {
			PublicationQuery pq = new PublicationQuery();
			pq.addQueryClause(PublicationFieldEnumerator.PUBID, publication.getPubid());			
			publicationNode = publicationIndex.query(pq.getQuery()).getSingle();
		} catch (NoSuchElementException  e) {
			publicationNode = null;
		}
		if (publicationNode==null) {
			publicationNode = graphDB.createNode();	
			
			publicationNode.setProperty(Property.Publication.ID, publication.getPubid());
			publicationIndex.add(publicationNode,Property.Publication.ID, publication.getPubid());
			publicationNode.setProperty(Property.Publication.ABSTRACT, publication.getSynopsis());
			publicationIndex.add(publicationNode,Property.Publication.ABSTRACT, publication.getSynopsis());
			publicationNode.setProperty(Property.Publication.ABSTRACT, publication.getSynopsis());
			publicationIndex.add(publicationNode,Property.Publication.ABSTRACT, publication.getSynopsis());
			publicationNode.setProperty(Property.Publication.AFFILIATION, publication.getAffiliation());
			publicationIndex.add(publicationNode,Property.Publication.AFFILIATION, publication.getAffiliation());
			publicationNode.setProperty(Property.Publication.JOURNAL, publication.getJounral());
			publicationIndex.add(publicationNode,Property.Publication.JOURNAL, publication.getJounral());
			publicationNode.setProperty(Property.Publication.TITLE, publication.getTitle());
			publicationIndex.add(publicationNode,Property.Publication.TITLE, publication.getTitle());
			publicationNode.setProperty(Property.Publication.YEAR, publication.getYear());
			publicationIndex.add(publicationNode,Property.Publication.YEAR, publication.getYear());
			
			for (Iterator<PersonWrapper> iterator = publication.getAuthors().iterator(); iterator.hasNext();) {
				PersonWrapper author = (PersonWrapper) iterator.next();
				processPerson(author, modelNode, publicationNode, DocumentRelTypes.HAS_AUTHOR);			
			}
			publicationNode.addLabel(NodeLabel.Types.PUBLICATION);
			publicationNode.createRelationshipTo(referenceNode, DatabaseRelTypes.BELONGS_TO);
			referenceNode.createRelationshipTo(publicationNode, DocumentRelTypes.HAS_PUBLICATION);
		}
	}
	
	protected static void processPerson(PersonWrapper person, Node modelNode, Node referenceNode, RelationshipType relationToReference){
		Node personNode = null;
		person.repairNullStrings();
		
		if (!person.isValid()) return;
		try {
			PersonQuery pq = new PersonQuery();
			pq.addQueryClause(PersonFieldEnumerator.FAMILYNAME, person.getLastName());
			pq.addQueryClause(PersonFieldEnumerator.GIVENNAME, person.getFirstName());
			personNode = personIndex.query(pq.getQuery()).getSingle();
		} catch (NoSuchElementException  e) {
			personNode = null;
		}					
		//create a node for each creator & link persons between models
		if (personNode==null){
			personNode= graphDB.createNode();
			personNode.addLabel(NodeLabel.Types.PERSON);
			personNode.setProperty(Property.Person.GIVENNAME, person.getFirstName());
			personNode.setProperty(Property.Person.FAMILYNAME, person.getLastName());
			personNode.setProperty(Property.Person.ORGANIZATION, person.getOrganization());
			personNode.setProperty(Property.Person.EMAIL, person.getEmail());
			//add to person index
			personIndex.add(personNode, Property.Person.FAMILYNAME, person.getLastName());
			personIndex.add(personNode, Property.Person.GIVENNAME, person.getFirstName());
			personIndex.add(personNode, Property.Person.EMAIL, person.getEmail());
			personIndex.add(personNode, Property.Person.ORGANIZATION, person.getOrganization());

			//add to node index
			if (relationToReference.equals(DocumentRelTypes.IS_CREATOR)) {
				modelIndex.add(modelNode, Property.General.CREATOR, person.getFirstName());
				modelIndex.add(modelNode, Property.General.CREATOR, person.getLastName());
			} else {
				modelIndex.add(modelNode, Property.General.AUTHOR, person.getFirstName());
				modelIndex.add(modelNode, Property.General.AUTHOR, person.getLastName());
			}
		}

		//set relationships
		personNode.createRelationshipTo(referenceNode, Relation.DatabaseRelTypes.BELONGS_TO);
		referenceNode.createRelationshipTo(personNode, relationToReference);
	}
	


}
