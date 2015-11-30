package de.unirostock.sems.masymos.extractor.SBML;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.History;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation;
import de.unirostock.sems.masymos.configuration.Relation.AnnotationRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DatabaseRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DocumentRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.SbmlRelTypes;
import de.unirostock.sems.masymos.data.PersonWrapper;
import de.unirostock.sems.masymos.database.traverse.DBModelTraverser;
import de.unirostock.sems.masymos.extractor.Extractor;


public class SBMLExtractor extends Extractor{
	
	private static SBMLReader reader = new SBMLReader();

	public static Node extractStoreIndex(InputStream stream, String versionID) throws XMLStreamException, IOException{
		
		Node documentNode = null;		
		try (Transaction tx = graphDB.beginTx()) {
			documentNode = extractFromSBML(stream, versionID);
			tx.success();
		} catch (XMLStreamException e) {
			documentNode = null;
			//TODO Log me
			System.out.println("Error XMLStreamException while parsing model");
			System.out.println(e.getMessage());
		} 
		return documentNode;
	}





	private static void extractSBOTerm(String sboTerm, Node referenceNode){
		if (StringUtils.isEmpty(sboTerm)) return;
		
		String sboUri = "urn:miriam:biomodels.sbo:" + sboTerm;
		
		Node annotationNode = DBModelTraverser.fromNodeToAnnotation(referenceNode);
		
		//create if not already existing
		if (annotationNode==null) {
			annotationNode = graphDB.createNode();
			annotationNode.addLabel(NodeLabel.Types.ANNOTATION);
			annotationNode.createRelationshipTo(referenceNode, DatabaseRelTypes.BELONGS_TO);
			referenceNode.createRelationshipTo(annotationNode, AnnotationRelTypes.HAS_ANNOTATION);
		}
		
		Node resource = annotationIndex.get(Property.General.URI, sboUri).getSingle();
		if (resource==null){
			resource = graphDB.createNode();
			resource.setProperty(Property.General.URI, sboUri);
			resource.addLabel(NodeLabel.Types.RESOURCE);
			annotationIndex.add(resource, Property.General.URI, sboUri);
			//old way of creating annotation index
			//annotationFullTextIndex.add(resource, Property.General.RESOURCE, AnnotationResolverUtil.getSingleURIFullText(sboUri));
		}
		//create a dynamic relationship based on the qualifier
		annotationNode.createRelationshipTo(resource, SbmlRelTypes.HAS_SBOTERM);
		resource.createRelationshipTo(annotationNode, DatabaseRelTypes.BELONGS_TO);
	}

	private static void extractAnnotationNodes(Annotation annotation, Node referenceNode, Node modelNode) {
		//if no annotation is present return
		if ((annotation==null) ||  (annotation.isEmpty())) return;
	
		//create annotation node
		Node annotationNode = graphDB.createNode();
		annotationNode.addLabel(NodeLabel.Types.ANNOTATION);
		History history = annotation.getHistory();
		
		//parse dates and creators
		if (history!=null) {
			if (history.isSetCreatedDate()) {
				annotationNode.setProperty(Property.General.CREATED, history.getCreatedDate().toString());
				modelIndex.add(modelNode, Property.General.CREATED, history.getCreatedDate());
			}
			if (history.isSetModifiedDate()) {
				annotationNode.setProperty(Property.General.MODIFIED, history.getModifiedDate().toString());
				modelIndex.add(modelNode, Property.General.MODIFIED, history.getModifiedDate());
			}
			if ((history.getListOfCreators() != null) && !(history.getListOfCreators().isEmpty())){
				for (Iterator<Creator> iterator = history.getListOfCreators().iterator(); iterator
						.hasNext();) {
					Creator creator = (Creator) iterator.next();
					PersonWrapper person = new PersonWrapper(creator.getFamilyName(), creator.getGivenName(), creator.getEmail(), creator.getOrganization());
					processPerson(person, modelNode, annotationNode, Relation.DocumentRelTypes.IS_CREATOR);
// the following was out-sourced					
//					try {
//						PersonQuery pq = new PersonQuery();
//						pq.addQueryClause(PersonFieldEnumerator.FAMILYNAME, creator.getFamilyName());
//						pq.addQueryClause(PersonFieldEnumerator.GIVENNAME, creator.getGivenName());
//						//pq.addQueryClause(PersonFieldEnumerator.ORGANIZATION, creator.getOrganization());
//						personNode = personIndex.query(pq.getQuery()).getSingle();
//					} catch (NoSuchElementException  e) {
//						personNode = null;
//					}					
//					//create a node for each creator & link persons between models
//					if (personNode==null){
//						personNode= graphDB.createNode();
//						personNode.setProperty(Property.General.NODETYPE, Property.NodeType.PERSON);
//						personNode.setProperty(Property.Person.GIVENNAME, creator.getGivenName());
//						personNode.setProperty(Property.Person.FAMILYNAME, creator.getFamilyName());
//						//add to person index
//						personIndex.add(personNode, Property.Person.FAMILYNAME, creator.getFamilyName());
//						personIndex.add(personNode, Property.Person.GIVENNAME, creator.getGivenName());
//						personIndex.add(personNode, Property.Person.ORGANIZATION, creator.getOrganization());
//	
//						//add to node index
//						modelIndex.add(modelNode, Property.General.CREATOR, creator.getGivenName());
//						modelIndex.add(modelNode, Property.General.CREATOR, creator.getFamilyName());
//					}
//					//set Email in case Person originates form a publication
//					if (!personNode.hasProperty(Property.Person.EMAIL)){
//						personNode.setProperty(Property.Person.EMAIL, creator.getEmail());
//						modelIndex.add(modelNode, Property.General.EMAIL, creator.getEmail());
//						personIndex.add(personNode, Property.Person.EMAIL, creator.getEmail());
//					}
//					//set relationships
//					personNode.createRelationshipTo(annotationNode, Relation.DatabaseRelTypes.BELONGS_TO);
//					annotationNode.createRelationshipTo(personNode, Relation.SbmlRelTypes.IS_CREATOR);
				}
				
			}	
		}
		
		//store and index non RDF 
		if (annotation.getNonRDFannotation() != null){			
			try {
				annotationNode.setProperty(Property.General.NONRDF, annotation.getNonRDFannotation().toXMLString());
				annotationIndex.add(annotationNode, Property.General.NONRDF, annotation.getNonRDFannotation().toXMLString());
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
			}
		}
		
		//get list of controlled vocabulary terms
		List<CVTerm> cvtList = annotation.getListOfCVTerms();
		for (Iterator<CVTerm> cvtIt = cvtList.iterator(); cvtIt.hasNext();) {
			CVTerm cvTerm = (CVTerm) cvtIt.next();
			Qualifier q = null;
			//identify the qualifier
			if (cvTerm.isBiologicalQualifier()){
				q = cvTerm.getBiologicalQualifierType();				
			} else if (cvTerm.isModelQualifier()){
				q = cvTerm.getModelQualifierType();
			} 
			
			//get the resources (URI) 
			List<String> resList = cvTerm.getResources();
			for (Iterator<String> resIt = resList.iterator(); resIt.hasNext();) {
				String res = (String) resIt.next();
				//test if resource already exists
				Node resource = annotationIndex.get(Property.General.URI, res).getSingle();
				if (resource==null){
					resource = graphDB.createNode();
					resource.setProperty(Property.General.URI, res);
					resource.addLabel(NodeLabel.Types.RESOURCE);
					annotationIndex.add(resource, Property.General.URI, res);
				}
				//create a dynamic relationship based on the qualifier
				annotationNode.createRelationshipTo(resource, DynamicRelationshipType.withName(q.getElementNameEquivalent()));
				resource.createRelationshipTo(annotationNode, DatabaseRelTypes.BELONGS_TO);
			}
		}
	
		annotationNode.createRelationshipTo(referenceNode, DatabaseRelTypes.BELONGS_TO);
		referenceNode.createRelationshipTo(annotationNode, AnnotationRelTypes.HAS_ANNOTATION);
	
	}

	private static Node extractFromSBML(InputStream stream, String versionID) throws XMLStreamException {
		
		SBMLDocument doc = null;
		Model model = null;
		doc = reader.readSBMLFromStream(stream);
		
		//create SBML document
		Node documentNode = graphDB.createNode();
		documentNode.addLabel(NodeLabel.Types.DOCUMENT);
		documentNode.setProperty(Property.SBML.LEVEL, doc.getLevel());
		documentNode.setProperty(Property.SBML.VERSION, doc.getVersion());
		if (versionID!=null) documentNode.setProperty(Property.General.VERSIONID, versionID);

		
		//create SBML model
		model = doc.getModel();
		Node modelNode = graphDB.createNode();
		documentNode.createRelationshipTo(modelNode, DocumentRelTypes.HAS_MODEL);
		modelNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO);
		modelNode.setProperty(Property.General.NAME, model.getName());
		modelNode.setProperty(Property.General.ID, model.getId());
		modelNode.addLabel(NodeLabel.Types.MODEL);
		modelNode.addLabel(NodeLabel.Types.SBML_MODEL);
		//index model name and id
		modelIndex.add(modelNode, Property.General.NAME, model.getName());
		modelIndex.add(modelNode, Property.General.ID, model.getId());

		//process annotations and link them to the model
		extractAnnotationNodes(model.getAnnotation(), modelNode, modelNode);
		//process the SBO term
		extractSBOTerm(model.getSBOTermID(), modelNode);
		//process the compartments
		Map<String, Node> compartmentNodes = extractSBMLCompartments(model.getListOfCompartments(), modelNode);
		//process the species
		Map<String, Node> speciesNodes = extractSBMLSpecies(model.getListOfSpecies(), modelNode, compartmentNodes);
		//process the reactions
		extractSBMLReactions(model.getListOfReactions(), modelNode, compartmentNodes, speciesNodes);
		//process the parameters
		extractSBMLParameters(model.getListOfParameters(), modelNode);
		//process the rules
		extractSBMLRules(model.getListOfRules(), modelNode);
		//process the events
		extractSBMLEvents(model.getListOfEvents(), modelNode);
		//process the functions
		extractSBMLFunctions(model.getListOfFunctionDefinitions(), modelNode);
		

//		if (publication!=null) {
//			Node annotationNode = DBModelTraverser.fromNodeToAnnotation(modelNode);
//			if (annotationNode==null){
//				annotationNode = graphDB.createNode();
//				annotationNode.addLabel(NodeLabel.Types.ANNOTATION);
//				annotationNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
//				modelNode.createRelationshipTo(annotationNode, AnnotationRelTypes.HAS_ANNOTATION);
//			} 
//			Extractor.processPublication(publication, annotationNode, modelNode);
//				
//		}
//TODO code some pubmed wrapper		
		
		return documentNode;
	}
	

	private static void extractSBMLReactions(ListOf<Reaction> listOfReaction,
			Node modelNode, Map<String, Node> compartmentList, Map<String, Node> speciesList) {

		for (Iterator<Reaction> itReac = listOfReaction.iterator(); itReac
				.hasNext();) {
			
			//iterate through reactions 
			Reaction reaction = (Reaction) itReac.next();
			Node reactionNode = graphDB.createNode();
			modelNode.createRelationshipTo(reactionNode, SbmlRelTypes.HAS_REACTION);
			reactionNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			
			reactionNode.setProperty(Property.General.NAME, reaction.getName());
			reactionNode.setProperty(Property.General.ID, reaction.getId());
			reactionNode.addLabel(NodeLabel.Types.SBML_REACTION);
			
			//index mapping reaction properties to model
			modelIndex.add(modelNode, Property.SBML.REACTION, reaction.getName());
			modelIndex.add(modelNode, Property.SBML.REACTION, reaction.getId());
			
			//link reaction and compartment (used in SBML???)
			Node compartmentNode = compartmentList.get(reaction.getCompartment());
			if (compartmentNode!=null)
			{
				reactionNode.createRelationshipTo(compartmentNode, SbmlRelTypes.IS_LOCATED_IN);
			    compartmentNode.createRelationshipTo(reactionNode, SbmlRelTypes.CONTAINS_REACTION);
			}
			
			//link species used as modifier to reaction
			ListOf<ModifierSpeciesReference> lom = reaction.getListOfModifiers();
			for (Iterator<ModifierSpeciesReference> itLom = lom.iterator(); itLom.hasNext();) {
				ModifierSpeciesReference msr = (ModifierSpeciesReference) itLom.next();
				if ((msr.getSpecies()!=null) && speciesList.containsKey(msr.getSpecies())) {
					reactionNode.createRelationshipTo(speciesList.get(msr.getSpecies()), SbmlRelTypes.HAS_MODIFIER);
					speciesList.get(msr.getSpecies()).createRelationshipTo(reactionNode, SbmlRelTypes.IS_MODIFIER);
				}
			}
			
			//link species used as product to reaction
			ListOf<SpeciesReference> lop = reaction.getListOfProducts();
			for (Iterator<SpeciesReference> itLop = lop.iterator(); itLop.hasNext();) {
				SpeciesReference msr = (SpeciesReference) itLop.next();
				if ((msr.getSpecies()!=null) && speciesList.containsKey(msr.getSpecies())) {
					reactionNode.createRelationshipTo(speciesList.get(msr.getSpecies()), SbmlRelTypes.HAS_PRODUCT);
					speciesList.get(msr.getSpecies()).createRelationshipTo(reactionNode, SbmlRelTypes.IS_PRODUCT);
				}
			}
			
			//link species used as reactant to reaction
			ListOf<SpeciesReference> lor = reaction.getListOfReactants();
			for (Iterator<SpeciesReference> itLor = lor.iterator(); itLor.hasNext();) {
				SpeciesReference msr = (SpeciesReference) itLor.next();
				if ((msr.getSpecies()!=null) && speciesList.containsKey(msr.getSpecies())) {
					reactionNode.createRelationshipTo(speciesList.get(msr.getSpecies()), SbmlRelTypes.HAS_REACTANT);
					speciesList.get(msr.getSpecies()).createRelationshipTo(reactionNode, SbmlRelTypes.IS_REACTANT);
				}
			}
			
			//extract annotation and link to reaction node
			extractAnnotationNodes(reaction.getAnnotation(), reactionNode, modelNode);
			//process the SBO term
			extractSBOTerm(reaction.getSBOTermID(), reactionNode);
		}
	}


	private static Map<String, Node> extractSBMLSpecies(ListOf<Species> listOfSpecies,
			Node modelNode, Map<String, Node> compartmentList) {

		Map<String, Node> speciesNodes = new HashMap<String, Node>();

		for (Iterator<Species> iterator = listOfSpecies.iterator(); iterator
				.hasNext();) {
			
			//iterate through species and link back to model
			Species species = (Species) iterator.next();
			Node speciesNode = graphDB.createNode();
			modelNode.createRelationshipTo(speciesNode, SbmlRelTypes.HAS_SPECIES);
			speciesNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			
			speciesNode.setProperty(Property.General.NAME, species.getName());
			speciesNode.setProperty(Property.General.ID, species.getId());
			speciesNode.addLabel(NodeLabel.Types.SBML_SPECIES);
			
			//index mapping model and species properties
			modelIndex.add(modelNode, Property.SBML.SPECIES, species.getName());
			modelIndex.add(modelNode, Property.SBML.SPECIES, species.getId());
			
			//create relation between compartment and species
			Node compartmentNode = compartmentList.get(species.getCompartment());
			if (compartmentNode!=null)
			{
				speciesNode.createRelationshipTo(compartmentNode, SbmlRelTypes.IS_LOCATED_IN);
			    compartmentNode.createRelationshipTo(speciesNode, SbmlRelTypes.CONTAINS_SPECIES);
			}
					
			//map species id to species for linking reactions
			speciesNodes.put(species.getId(), speciesNode);
			
			//extract annotation an link to species 
			extractAnnotationNodes(species.getAnnotation(), speciesNode, modelNode);
			//process the SBO term
			extractSBOTerm(species.getSBOTermID(), speciesNode);
		}
		return speciesNodes;
	}

	private static Map<String, Node> extractSBMLCompartments(
			ListOf<Compartment> listOfCompartments, Node modelNode) {
		Map<String, Node> compartmentNodes = new HashMap<String, Node>();
		
		for (Iterator<Compartment> iterator = listOfCompartments.iterator(); iterator
				.hasNext();) {
			
			//iterate through compartments and link back to model
			Compartment compartment = (Compartment) iterator.next();
			Node compartmentNode = graphDB.createNode();
			modelNode.createRelationshipTo(compartmentNode, SbmlRelTypes.HAS_COMPARTMENT);
			compartmentNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			
			compartmentNode.setProperty(Property.General.NAME, compartment.getName());
			compartmentNode.setProperty(Property.General.ID, compartment.getId());
			compartmentNode.addLabel(NodeLabel.Types.SBML_COMPARTMENT);
			
			//index to map model to compartment properties
			modelIndex.add(modelNode, Property.SBML.COMPARTMENT, compartment.getName());
			modelIndex.add(modelNode, Property.SBML.COMPARTMENT, compartment.getId());
			
			//map compartment id to node for linking species
			compartmentNodes.put(compartment.getId(), compartmentNode);
			
			//extract annotation and link to compartment
			extractAnnotationNodes(compartment.getAnnotation(), compartmentNode, modelNode);
			//process the SBO term
			extractSBOTerm(compartment.getSBOTermID(), compartmentNode);
		}
		
		return compartmentNodes;
	}
	
	private static void extractSBMLParameters(
			ListOf<Parameter> listOfParameters, Node modelNode) {
		for (Iterator<Parameter> iterator = listOfParameters.iterator(); iterator
				.hasNext();) {
			Parameter parameter = (Parameter) iterator.next();
			Node parameterNode = graphDB.createNode();
			parameterNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			modelNode.createRelationshipTo(parameterNode, Relation.SbmlRelTypes.HAS_PARAMETER);
			parameterNode.addLabel(NodeLabel.Types.SBML_PARAMETER);
			
			//extract parameter properties
			parameterNode.setProperty(Property.General.NAME, parameter.getName());
			parameterNode.setProperty(Property.General.ID, parameter.getId());
			
			modelIndex.add(modelNode, Property.SBML.PARAMETER, parameter.getName());
			modelIndex.add(modelNode, Property.SBML.PARAMETER, parameter.getId());
			
			extractAnnotationNodes(parameter.getAnnotation(), parameterNode, modelNode);
			//process the SBO term
			extractSBOTerm(parameter.getSBOTermID(), parameterNode);
		}
		
	}
	

	private static void extractSBMLRules(ListOf<Rule> listOfRules,
			Node modelNode) {
		for (Iterator<Rule> iterator = listOfRules.iterator(); iterator.hasNext();) {
			Rule rule = (Rule) iterator.next();
			Node ruleNode = graphDB.createNode();
			ruleNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			modelNode.createRelationshipTo(ruleNode, SbmlRelTypes.HAS_RULE);
			ruleNode.addLabel(NodeLabel.Types.SBML_RULE);
			
			//extract rule properties
			//TODO continue
			
			extractAnnotationNodes(rule.getAnnotation(), ruleNode, modelNode);
			//process the SBO term
			extractSBOTerm(rule.getSBOTermID(), ruleNode);
		}
		
	}
	

	private static void extractSBMLFunctions(
			ListOf<FunctionDefinition> listOfFunctionDefinitions, Node modelNode) {
		for (Iterator<FunctionDefinition> iterator = listOfFunctionDefinitions.iterator(); iterator
				.hasNext();) {
			FunctionDefinition functionDefinition = (FunctionDefinition) iterator
					.next();
			Node functionNode = graphDB.createNode();
			functionNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			modelNode.createRelationshipTo(functionNode, SbmlRelTypes.HAS_FUNCTION);
			functionNode.addLabel(NodeLabel.Types.SBML_FUNCTION);
			//process function properties
			functionNode.setProperty(Property.General.NAME, functionDefinition.getName());
			functionNode.setProperty(Property.General.ID, functionDefinition.getId());
			//TODO continue
			extractAnnotationNodes(functionDefinition.getAnnotation(), functionNode, modelNode);
			//process the SBO term
			extractSBOTerm(functionDefinition.getSBOTermID(), functionNode);
		}
				
	}

	private static void extractSBMLEvents(ListOf<Event> listOfEvents,
			Node modelNode) {
		for (Iterator<Event> iterator = listOfEvents.iterator(); iterator
				.hasNext();) {
			Event event = (Event) iterator.next();
			Node eventNode = graphDB.createNode();
			eventNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			modelNode.createRelationshipTo(eventNode, SbmlRelTypes.HAS_EVENT);
			eventNode.addLabel(NodeLabel.Types.SBML_EVENT);
			//process event properties
			eventNode.setProperty(Property.General.NAME, event.getName());
			eventNode.setProperty(Property.General.ID, event.getId());
			//TODO continue
			extractAnnotationNodes(event.getAnnotation(), eventNode, modelNode);
			//process the SBO term
			extractSBOTerm(event.getSBOTermID(), eventNode);
		}
		
		
	}
	
}
