package de.unirostock.sems.masymos.extractor.Owl;

import java.io.File;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.util.OntologyFactory;

public class Ontology {
	
	public static void extractOntology(String path, String ontologyName){
		
	   OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
		File file = new File(path);
		OWLOntology o;
		try {
			o = manager.loadOntologyFromOntologyDocument(file);
			if (o!=null) importOntology(o,ontologyName);
		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		} 
		
		
	}

	private static void importOntology(OWLOntology ontology, String ontologyName) throws Exception {

	    //OWLReasonerFactory reasonerFactory = new JFactFactory();
	    //OWLReasonerConfiguration config = new SimpleConfiguration(50000);    
		//OWLReasoner reasoner = reasonerFactory.createReasoner(ontology,config);
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

		if (!reasoner.isConsistent()) {
			// logger.error("Ontology is inconsistent");
			// throw your exception of choice here
			throw new Exception("Ontology is inconsistent");
		}
		
		Node thingNode = null;
		try (Transaction tx = Manager.instance().createNewTransaction()) {

			thingNode = getOrCreateNodeWithUniqueFactory("owl:" + ontologyName, ontologyName );
			tx.success();
		}
			long counter = 0;
			long classSize = ontology.getClassesInSignature().size();
			System.out.println("The ontology contains " + ontology.getLogicalAxiomCount() 
					+ " axioms, "+ classSize + " classes, and "
					+ ontology.getObjectPropertiesInSignature().size()+ " properties");
			System.out.print("Processed:  " + ++counter);
			for (OWLClass c : ontology.getClassesInSignature()) {//ontology.getClassesInSignature(true)
				if ((counter % 50) == 0)System.out.println("..." + counter + " of " + classSize);
				counter++;
				
				try (Transaction tx = Manager.instance().createNewTransaction()) {
			
				String classString = c.toString();
				if (classString.contains("#")) {
					classString = classString.substring(
							classString.indexOf("#") + 1,
							classString.lastIndexOf(">"));
				}
				Node classNode = getOrCreateNodeWithUniqueFactory(classString, ontologyName);
				NodeSet<OWLClass> superclasses = reasoner.getSuperClasses(c,
						true);

				if (superclasses.isEmpty()) {
					classNode.createRelationshipTo(thingNode,
							DynamicRelationshipType.withName("isA"));
				} else {
					for (org.semanticweb.owlapi.reasoner.Node<OWLClass> parentOWLNode : superclasses) {
						if (parentOWLNode.getSize()==0) continue;
						OWLClassExpression parent = parentOWLNode.getRepresentativeElement();
						String parentString = parent.toString();

						if (parentString.contains("#")) {
							parentString = parentString.substring(
									parentString.indexOf("#") + 1,
									parentString.lastIndexOf(">"));
						}
						Node parentNode = getOrCreateNodeWithUniqueFactory(parentString, ontologyName);
						classNode.createRelationshipTo(parentNode,
								DynamicRelationshipType.withName("isA"));
					}
				}

				for (org.semanticweb.owlapi.reasoner.Node<OWLNamedIndividual> in : reasoner
						.getInstances(c, true)) {
					OWLNamedIndividual i = in.getRepresentativeElement();
					String indString = i.toString();
					if (indString.contains("#")) {
						indString = indString.substring(
								indString.indexOf("#") + 1,
								indString.lastIndexOf(">"));
					}
					Node individualNode = getOrCreateNodeWithUniqueFactory(indString, ontologyName);

					individualNode.createRelationshipTo(classNode,
							DynamicRelationshipType.withName("isA"));

					for (OWLObjectPropertyExpression objectProperty : ontology
							.getObjectPropertiesInSignature()) {

						for (org.semanticweb.owlapi.reasoner.Node<OWLNamedIndividual> object : reasoner
								.getObjectPropertyValues(i, objectProperty)) {
							String reltype = objectProperty.toString();
							reltype = reltype.substring(
									reltype.indexOf("#") + 1,
									reltype.lastIndexOf(">"));

							String s = object.getRepresentativeElement()
									.toString();
							s = s.substring(s.indexOf("#") + 1,
									s.lastIndexOf(">"));
							Node objectNode = getOrCreateNodeWithUniqueFactory(s, ontologyName);
							individualNode.createRelationshipTo(objectNode,
									DynamicRelationshipType.withName(reltype));
						}
					}

					for (OWLDataPropertyExpression dataProperty : ontology
							.getDataPropertiesInSignature()) {

						for (OWLLiteral object : reasoner
								.getDataPropertyValues(i,
										dataProperty.asOWLDataProperty())) {
							String reltype = dataProperty.asOWLDataProperty()
									.toString();
							reltype = reltype.substring(
									reltype.indexOf("#") + 1,
									reltype.lastIndexOf(">"));

							String s = object.toString();
							individualNode.setProperty(reltype, s);
						}
					}
				}
				
				tx.success();	
			}
			
		}
		System.out.println("...done");
	}

    public static Node getOrCreateNodeWithUniqueFactory(String idValue, String ontologyName)
    {
             
        return OntologyFactory.getFactory(ontologyName).getOrCreate( "id", idValue );
    }
	
}
