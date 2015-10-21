package de.unirostock.sems.masymos.extractor.Owl;
/*
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.UniqueFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Optional;

import de.unirostock.sems.masymos.configuration.Config;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Property.Ontology;
import de.unirostock.sems.masymos.database.Manager;
*/
public class OwlToNeo4j {
/*
	protected static GraphDatabaseService graphDB;

	static OWLOntologyManager owlManager = OWLManager
			.createOWLOntologyManager();
	static OWLDataFactory dataFactory = owlManager.getOWLDataFactory();
	static UniqueFactory<Node> neoFactory;

	public static void main(String[] args) {

		String owlPath = new String();
		String owlRoot = new String();

		// parse arguments
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dbPath")) {
				Config.instance().setDbPath(args[++i]);
			}

			if (args[i].equals("-owl")) {
				owlPath = args[++i];
			}

			if (args[i].equals("-root")) {
				owlRoot = args[++i];
			}
		}
		
		
		// init DB
		initializeDatabase();
		neoFactory = initFactory();

		// fill DB with owl terms
		if (owlPath.isEmpty()){
			System.out.println("ERROR: owlPath missing");
		}
		else if (owlRoot.isEmpty()){
			System.out.println("ERROR: owlRoot missing");
		}
		else {
				OwlToNeo4j.extractFromOwl(owlPath, owlRoot);
		}

		// call exit explicitly in case there a zombi threads
		System.exit(0);
	}

	public static UniqueFactory<Node> initFactory(){
		
		try(Transaction transaction = graphDB.beginTx();)
		{
		    UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory( graphDB, "users" )
		    {

				@Override
				protected void initialize(Node created,
						Map<String, Object> properties) {
		            created.setProperty(Property.Ontology.OntologyLongID, properties.get(Property.Ontology.OntologyLongID));
					
				}
		    };
		    return factory;
		}
	}
	
	public static void extractFromOwl(String owlPath, String rootTerm) {

		try {
			
			// set owl file
			File inputfile = new File(owlPath);
			
			OWLOntology ontology = owlManager
					.loadOntologyFromOntologyDocument(inputfile);
			Optional<IRI> ontologyIRI = ontology.getOntologyID().getOntologyIRI();

			

			try(Transaction tx = graphDB.beginTx();) {
				Node rootNode = graphDB.createNode();
				String rootID = ontologyIRI + rootTerm;

				rootNode.setProperty(Property.Ontology.OntologyLongID, rootID);
				rootNode.setProperty(Property.Ontology.TermID, rootTerm);

				System.out.println("Root initialised");
				connectParentAndChildren(rootNode, ontology, 0);
				tx.success();
			}

		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error: OWLOntologyCreationException");
		}
	}

	public static void initializeDatabase() {
		// create neo4j database

		System.out.println("Started at: " + new Date());
		System.out.print("Getting manager...");
		Manager.instance();
		System.out.println("done");

		graphDB = Manager.instance().getDatabase();
	}

	private static void connectParentAndChildren(Node parentNode,
			OWLOntology ontology, int depth) {

		String parentID = (String) parentNode.getProperty(Ontology.OntologyLongID);

		Set<OWLClassExpression> children = (dataFactory.getOWLClass(
				IRI.create(parentID))).getSubClasses(ontology);
		
		parentNode.setProperty(Property.Ontology.depth, depth);

		if (children.isEmpty()) { 
			parentNode.setProperty(Property.Ontology.isLeaf, true);

		}

		else {
			for (Iterator<OWLClassExpression> i = children.iterator(); i
					.hasNext();) {
				OWLClassExpression owlElement = i.next();

				String iriValue = owlElement.toString().substring(1, owlElement.toString().length() - 1);
				Node childNode = createUniqueNode(Property.Ontology.OntologyLongID, iriValue);
				initProperties(childNode, owlElement.toString());
			
				/*	ReadableIndex<Node> autoNodeIndex = graphDB.index()
						.getNodeAutoIndexer().getAutoIndex();

				//Test for existing Node with same IRI

				IndexHits<Node> sboNodes = autoNodeIndex.get(Property.Ontology.OntologyLongID, i.next().toString().substring(1, (i.next()).toString().length() - 1));

				if(!sboNodes.hasNext()){
					childNode = graphDB.createNode();
					initProperties(childNode, i.next().toString());
				} else {
				    childNode = sboNodes.next();
				}*/
/*				
				childNode.createRelationshipTo(parentNode, DynamicRelationshipType.withName("isA"));
				System.out.println(childNode.getProperty(Ontology.OntologyLongID)
						+ " created");
				connectParentAndChildren(childNode, ontology, depth+1);
			}
		}
	}

	public static void initProperties(Node node, String iri) {

		// General Properties
		node.setProperty(Ontology.OntologyLongID, iri.substring(1, iri.length() - 1));

		// SBO Properties
		if (iri.contains("SBO")) {
			node.setProperty(Property.Ontology.TermID,
					iri.substring(iri.length() - 12, iri.length() - 1));
		}
	}
	
	public static Node createUniqueNode(String property, String propertyValue){
		
		try(Transaction transaction = graphDB.beginTx();)
		{
		    Node node = neoFactory.getOrCreate( property, propertyValue );
		    transaction.success();
		    return node;
		}
	}
*/
}
