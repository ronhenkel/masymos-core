package de.unirostock.sems.masymos.extractor.SBML;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import de.unirostock.sems.masymos.database.IdFactory;
import de.unirostock.sems.masymos.database.traverse.ModelTraverser;
import de.unirostock.sems.masymos.extractor.Extractor;
import de.unirostock.sems.masymos.util.IndexText;


public class SBMLExtractor extends Extractor{

	final static Logger logger = LoggerFactory.getLogger(SBMLExtractor.class);
	
	private static SBMLReader reader = new SBMLReader();

	public static Node extractStoreIndexSBML(InputStream stream, String versionID, Long uID) throws XMLStreamException, IOException{
		
		Node documentNode = null;		
		try (Transaction tx = graphDB.beginTx()) {
			documentNode = extractFromSBML(stream, versionID, uID);
			tx.success();
		} catch (XMLStreamException e) {
			documentNode = null;
			logger.error("Error XMLStreamException while parsing model");
			logger.error(e.getMessage());
		} 
		return documentNode;
	}





	private static void extractSBOTerm(String sboTerm, Node referenceNode, Long uID){
		if (StringUtils.isBlank(sboTerm)) return;
		
		String sboUri = "urn:miriam:biomodels.sbo:" + sboTerm;
		
		Node annotationNode = ModelTraverser.fromNodeToAnnotation(referenceNode);
		
		//create if not already existing
		if (annotationNode==null) {
			annotationNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			annotationNode.addLabel(NodeLabel.Types.ANNOTATION);
			IdFactory.instance().addToRelationshipDeleteIndex(annotationNode.createRelationshipTo(referenceNode, DatabaseRelTypes.BELONGS_TO), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(referenceNode.createRelationshipTo(annotationNode, AnnotationRelTypes.HAS_ANNOTATION), uID);
		}
		
		Node resource = annotationIndex.get(Property.General.URI, sboUri).getSingle();
		if (resource==null){
			resource = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			resource.setProperty(Property.General.URI, sboUri);
			resource.addLabel(NodeLabel.Types.RESOURCE);
			annotationIndex.add(resource, Property.General.URI, sboUri);
			//old way of creating annotation index
			//annotationFullTextIndex.add(resource, Property.General.RESOURCE, AnnotationResolverUtil.getSingleURIFullText(sboUri));
		}
		//create a dynamic relationship based on the qualifier
		IdFactory.instance().addToRelationshipDeleteIndex(annotationNode.createRelationshipTo(resource, SbmlRelTypes.HAS_SBOTERM), uID);
		IdFactory.instance().addToRelationshipDeleteIndex(resource.createRelationshipTo(annotationNode, DatabaseRelTypes.BELONGS_TO), uID);
	}

	private static void extractAnnotationNodes(Annotation annotation, Node referenceNode, Node modelNode, Long uID) {
		//if no annotation is present return
		if ((annotation==null) ||  (annotation.isEmpty())) return;
	
		//create annotation node
		Node annotationNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
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
				}
				
			}	
		}
		
		//store and index non RDF omitted because of Lucene errors
//		if (annotation.getNonRDFannotation() != null){	
//			
//			try {
//				annotationNode.setProperty(Property.General.NONRDF, annotation.getNonRDFannotation().toXMLString());
//				annotationIndex.add(annotationNode, Property.General.NONRDF, annotation.getNonRDFannotation().toXMLString());
//			} catch (XMLStreamException e) {
//				logger.error(e.getMessage());
//			}
//			
//		}
		
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
					resource = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
					resource.setProperty(Property.General.URI, res);
					resource.addLabel(NodeLabel.Types.RESOURCE);
					annotationIndex.add(resource, Property.General.URI, res);
				}
				//create a dynamic relationship based on the qualifier
				IdFactory.instance().addToRelationshipDeleteIndex(annotationNode.createRelationshipTo(resource, RelationshipType.withName(q.getElementNameEquivalent())), uID);
				IdFactory.instance().addToRelationshipDeleteIndex(resource.createRelationshipTo(annotationNode, DatabaseRelTypes.BELONGS_TO), uID);
			}
		}
	
		IdFactory.instance().addToRelationshipDeleteIndex(annotationNode.createRelationshipTo(referenceNode, DatabaseRelTypes.BELONGS_TO), uID);
		IdFactory.instance().addToRelationshipDeleteIndex(referenceNode.createRelationshipTo(annotationNode, AnnotationRelTypes.HAS_ANNOTATION), uID);
	
	}

	private static Node extractFromSBML(InputStream stream, String versionID, Long uID) throws XMLStreamException {
		
		SBMLDocument doc = null;
		Model model = null;
		doc = reader.readSBMLFromStream(stream);
		
		//create SBML document
		Node documentNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
		documentNode.addLabel(NodeLabel.Types.DOCUMENT);
		documentNode.setProperty(Property.SBML.LEVEL, doc.getLevel());
		documentNode.setProperty(Property.SBML.VERSION, doc.getVersion());
		if (versionID!=null) documentNode.setProperty(Property.General.VERSIONID, versionID);

		
		//create SBML model
		model = doc.getModel();
		Node modelNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
		IdFactory.instance().addToRelationshipDeleteIndex(documentNode.createRelationshipTo(modelNode, DocumentRelTypes.HAS_MODEL), uID);
		IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO), uID);
		modelNode.setProperty(Property.General.NAME, model.getName());
		modelNode.setProperty(Property.General.ID, model.getId());
		modelNode.addLabel(NodeLabel.Types.MODEL);
		modelNode.addLabel(NodeLabel.Types.SBML_MODEL);
		//index model name and id
		modelIndex.add(modelNode, Property.General.NAME, IndexText.expandTermsSpecialChars(model.getName()));
		modelIndex.add(modelNode, Property.General.ID, IndexText.expandTermsSpecialChars(model.getId()));

		//process annotations and link them to the model
		extractAnnotationNodes(model.getAnnotation(), modelNode, modelNode, uID);
		//process the SBO term
		extractSBOTerm(model.getSBOTermID(), modelNode, uID);
		//process the compartments
		Map<String, Node> compartmentNodes = extractSBMLCompartments(model.getListOfCompartments(), modelNode, uID);
		//process the species
		Map<String, Node> speciesNodes = extractSBMLSpecies(model.getListOfSpecies(), modelNode, compartmentNodes, uID);
		//process the reactions
		extractSBMLReactions(model.getListOfReactions(), modelNode, compartmentNodes, speciesNodes, uID);
		//process the parameters
		extractSBMLParameters(model.getListOfParameters(), modelNode, uID);
		//process the rules
		extractSBMLRules(model.getListOfRules(), modelNode, uID);
		//process the events
		extractSBMLEvents(model.getListOfEvents(), modelNode, uID);
		//process the functions
		extractSBMLFunctions(model.getListOfFunctionDefinitions(), modelNode, uID);	
		
		return documentNode;
	}
	

	private static void extractSBMLReactions(ListOf<Reaction> listOfReaction,
			Node modelNode, Map<String, Node> compartmentList, Map<String, Node> speciesList, Long uID) {

		for (Iterator<Reaction> itReac = listOfReaction.iterator(); itReac
				.hasNext();) {
			
			//iterate through reactions 
			Reaction reaction = (Reaction) itReac.next();
			Node reactionNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(reactionNode, SbmlRelTypes.HAS_REACTION), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(reactionNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO), uID);
			
			reactionNode.setProperty(Property.General.NAME, reaction.getName());
			reactionNode.setProperty(Property.General.ID, reaction.getId());
			reactionNode.addLabel(NodeLabel.Types.SBML_REACTION);
			
			//index mapping reaction properties to model
			modelIndex.add(modelNode, Property.SBML.REACTION, IndexText.expandTermsSpecialChars(reaction.getName()));
			modelIndex.add(modelNode, Property.SBML.REACTION, IndexText.expandTermsSpecialChars(reaction.getId()));
			
			//link reaction and compartment (used in SBML???)
			Node compartmentNode = compartmentList.get(reaction.getCompartment());
			if (compartmentNode!=null)
			{
				IdFactory.instance().addToRelationshipDeleteIndex(reactionNode.createRelationshipTo(compartmentNode, SbmlRelTypes.IS_LOCATED_IN), uID);
				IdFactory.instance().addToRelationshipDeleteIndex(compartmentNode.createRelationshipTo(reactionNode, SbmlRelTypes.CONTAINS_REACTION), uID);
			}
			
			//link species used as modifier to reaction
			ListOf<ModifierSpeciesReference> lom = reaction.getListOfModifiers();
			for (Iterator<ModifierSpeciesReference> itLom = lom.iterator(); itLom.hasNext();) {
				ModifierSpeciesReference msr = (ModifierSpeciesReference) itLom.next();
				if ((msr.getSpecies()!=null) && speciesList.containsKey(msr.getSpecies())) {
					IdFactory.instance().addToRelationshipDeleteIndex(reactionNode.createRelationshipTo(speciesList.get(msr.getSpecies()), SbmlRelTypes.HAS_MODIFIER), uID);
					IdFactory.instance().addToRelationshipDeleteIndex(speciesList.get(msr.getSpecies()).createRelationshipTo(reactionNode, SbmlRelTypes.IS_MODIFIER), uID);
				}
			}
			
			//link species used as product to reaction
			ListOf<SpeciesReference> lop = reaction.getListOfProducts();
			for (Iterator<SpeciesReference> itLop = lop.iterator(); itLop.hasNext();) {
				SpeciesReference msr = (SpeciesReference) itLop.next();
				if ((msr.getSpecies()!=null) && speciesList.containsKey(msr.getSpecies())) {
					IdFactory.instance().addToRelationshipDeleteIndex(reactionNode.createRelationshipTo(speciesList.get(msr.getSpecies()), SbmlRelTypes.HAS_PRODUCT), uID);
					IdFactory.instance().addToRelationshipDeleteIndex(speciesList.get(msr.getSpecies()).createRelationshipTo(reactionNode, SbmlRelTypes.IS_PRODUCT), uID);				}
			}
			
			//link species used as reactant to reaction
			ListOf<SpeciesReference> lor = reaction.getListOfReactants();
			for (Iterator<SpeciesReference> itLor = lor.iterator(); itLor.hasNext();) {
				SpeciesReference msr = (SpeciesReference) itLor.next();
				if ((msr.getSpecies()!=null) && speciesList.containsKey(msr.getSpecies())) {
					IdFactory.instance().addToRelationshipDeleteIndex(reactionNode.createRelationshipTo(speciesList.get(msr.getSpecies()), SbmlRelTypes.HAS_REACTANT), uID);
					IdFactory.instance().addToRelationshipDeleteIndex(speciesList.get(msr.getSpecies()).createRelationshipTo(reactionNode, SbmlRelTypes.IS_REACTANT), uID);
				}
			}
			
			//extract annotation and link to reaction node
			extractAnnotationNodes(reaction.getAnnotation(), reactionNode, modelNode, uID);
			//process the SBO term
			extractSBOTerm(reaction.getSBOTermID(), reactionNode, uID);
		}
	}


	private static Map<String, Node> extractSBMLSpecies(ListOf<Species> listOfSpecies,
			Node modelNode, Map<String, Node> compartmentList, Long uID) {

		Map<String, Node> speciesNodes = new HashMap<String, Node>();

		for (Iterator<Species> iterator = listOfSpecies.iterator(); iterator
				.hasNext();) {
			
			//iterate through species and link back to model
			Species species = (Species) iterator.next();
			Node speciesNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(speciesNode, SbmlRelTypes.HAS_SPECIES), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(speciesNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO), uID);
			
			speciesNode.setProperty(Property.General.NAME, species.getName());
			speciesNode.setProperty(Property.General.ID, species.getId());
			speciesNode.addLabel(NodeLabel.Types.SBML_SPECIES);
			
			//index mapping model and species properties
			modelIndex.add(modelNode, Property.SBML.SPECIES, IndexText.expandTermsSpecialChars(species.getName()));
			modelIndex.add(modelNode, Property.SBML.SPECIES, IndexText.expandTermsSpecialChars(species.getId()));
			
			//create relation between compartment and species
			Node compartmentNode = compartmentList.get(species.getCompartment());
			if (compartmentNode!=null)
			{
				IdFactory.instance().addToRelationshipDeleteIndex(speciesNode.createRelationshipTo(compartmentNode, SbmlRelTypes.IS_LOCATED_IN), uID);
				IdFactory.instance().addToRelationshipDeleteIndex( compartmentNode.createRelationshipTo(speciesNode, SbmlRelTypes.CONTAINS_SPECIES), uID);
			}
					
			//map species id to species for linking reactions
			speciesNodes.put(species.getId(), speciesNode);
			
			//extract annotation an link to species 
			extractAnnotationNodes(species.getAnnotation(), speciesNode, modelNode, uID);
			//process the SBO term
			extractSBOTerm(species.getSBOTermID(), speciesNode, uID);
		}
		return speciesNodes;
	}

	private static Map<String, Node> extractSBMLCompartments(
			ListOf<Compartment> listOfCompartments, Node modelNode, Long uID) {
		Map<String, Node> compartmentNodes = new HashMap<String, Node>();
		
		for (Iterator<Compartment> iterator = listOfCompartments.iterator(); iterator
				.hasNext();) {
			
			//iterate through compartments and link back to model
			Compartment compartment = (Compartment) iterator.next();
			Node compartmentNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(compartmentNode, SbmlRelTypes.HAS_COMPARTMENT), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(compartmentNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO), uID);
			
			compartmentNode.setProperty(Property.General.NAME, compartment.getName());
			compartmentNode.setProperty(Property.General.ID, compartment.getId());
			compartmentNode.addLabel(NodeLabel.Types.SBML_COMPARTMENT);
			
			//index to map model to compartment properties
			modelIndex.add(modelNode, Property.SBML.COMPARTMENT, IndexText.expandTermsSpecialChars(compartment.getName()));
			modelIndex.add(modelNode, Property.SBML.COMPARTMENT, IndexText.expandTermsSpecialChars(compartment.getId()));
			
			//map compartment id to node for linking species
			compartmentNodes.put(compartment.getId(), compartmentNode);
			
			//extract annotation and link to compartment
			extractAnnotationNodes(compartment.getAnnotation(), compartmentNode, modelNode, uID);
			//process the SBO term
			extractSBOTerm(compartment.getSBOTermID(), compartmentNode, uID);
		}
		
		return compartmentNodes;
	}
	
	private static void extractSBMLParameters(
			ListOf<Parameter> listOfParameters, Node modelNode, Long uID) {
		for (Iterator<Parameter> iterator = listOfParameters.iterator(); iterator
				.hasNext();) {
			Parameter parameter = (Parameter) iterator.next();
			Node parameterNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(parameterNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(parameterNode, Relation.SbmlRelTypes.HAS_PARAMETER), uID);
			parameterNode.addLabel(NodeLabel.Types.SBML_PARAMETER);
			
			//extract parameter properties
			parameterNode.setProperty(Property.General.NAME, parameter.getName());
			parameterNode.setProperty(Property.General.ID, parameter.getId());
			
			modelIndex.add(modelNode, Property.SBML.PARAMETER, parameter.getName());
			modelIndex.add(modelNode, Property.SBML.PARAMETER, parameter.getId());
			
			extractAnnotationNodes(parameter.getAnnotation(), parameterNode, modelNode, uID);
			//process the SBO term
			extractSBOTerm(parameter.getSBOTermID(), parameterNode, uID);
		}
		
	}
	

	private static void extractSBMLRules(ListOf<Rule> listOfRules,
			Node modelNode, Long uID) {
		for (Iterator<Rule> iterator = listOfRules.iterator(); iterator.hasNext();) {
			Rule rule = (Rule) iterator.next();
			Node ruleNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(ruleNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(ruleNode, SbmlRelTypes.HAS_RULE), uID);
			ruleNode.addLabel(NodeLabel.Types.SBML_RULE);
			
			//extract rule properties
			//TODO continue
			
			extractAnnotationNodes(rule.getAnnotation(), ruleNode, modelNode, uID);
			//process the SBO term
			extractSBOTerm(rule.getSBOTermID(), ruleNode, uID);
		}
		
	}
	

	private static void extractSBMLFunctions(
			ListOf<FunctionDefinition> listOfFunctionDefinitions, Node modelNode, Long uID) {
		for (Iterator<FunctionDefinition> iterator = listOfFunctionDefinitions.iterator(); iterator
				.hasNext();) {
			FunctionDefinition functionDefinition = (FunctionDefinition) iterator
					.next();
			Node functionNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(functionNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(functionNode, SbmlRelTypes.HAS_FUNCTION), uID);
			functionNode.addLabel(NodeLabel.Types.SBML_FUNCTION);
			//process function properties
			functionNode.setProperty(Property.General.NAME, functionDefinition.getName());
			functionNode.setProperty(Property.General.ID, functionDefinition.getId());
			//TODO continue
			extractAnnotationNodes(functionDefinition.getAnnotation(), functionNode, modelNode, uID);
			//process the SBO term
			extractSBOTerm(functionDefinition.getSBOTermID(), functionNode, uID);
		}
				
	}

	private static void extractSBMLEvents(ListOf<Event> listOfEvents,
			Node modelNode, Long uID) {
		for (Iterator<Event> iterator = listOfEvents.iterator(); iterator
				.hasNext();) {
			Event event = (Event) iterator.next();
			Node eventNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(eventNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(eventNode, SbmlRelTypes.HAS_EVENT), uID);
			eventNode.addLabel(NodeLabel.Types.SBML_EVENT);
			//process event properties
			eventNode.setProperty(Property.General.NAME, event.getName());
			eventNode.setProperty(Property.General.ID, event.getId());
			//TODO continue
			extractAnnotationNodes(event.getAnnotation(), eventNode, modelNode, uID);
			//process the SBO term
			extractSBOTerm(event.getSBOTermID(), eventNode, uID);
		}
		
		
	}
	
}
