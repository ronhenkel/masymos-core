package de.unirostock.sems.masymos.extractor.CellML;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.google.gson.Gson;

import de.unirostock.sems.bives.cellml.algorithm.CellMLValidator;
import de.unirostock.sems.bives.cellml.parser.CellMLComponent;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.cellml.parser.CellMLModel;
import de.unirostock.sems.bives.cellml.parser.CellMLReaction;
import de.unirostock.sems.bives.cellml.parser.CellMLVariable;
import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation.AnnotationRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.CellmlRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DatabaseRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DocumentRelTypes;
import de.unirostock.sems.masymos.data.PersonWrapper;
import de.unirostock.sems.masymos.data.PublicationWrapper;
import de.unirostock.sems.masymos.extractor.Extractor;
import de.unirostock.sems.masymos.util.CmetaContainer;



public class CellMLExtractor extends Extractor{
	
	public static Node extractStoreIndex(String filePath, String versionID) throws XMLStreamException, IOException{
		Node documentNode = null;
		
		try (Transaction tx = graphDB.beginTx()){
			URL url = new URL(filePath);
			documentNode = extractFromCellML(url, versionID);
			tx.success();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		//in case no node was generated, generate an empty one
		if (documentNode==null) {

			try (Transaction tx = graphDB.beginTx()){
				documentNode = graphDB.createNode();				
				documentNode.addLabel(NodeLabel.Types.DOCUMENT);
				tx.success();
			}
		}
		return documentNode;
	}


	private static Node extractFromCellML(URL url, String versionID){
		
		Node documentNode = graphDB.createNode();
		
		documentNode.addLabel(NodeLabel.Types.DOCUMENT);
		//try to parse CellML model
		CellMLValidator validator = new CellMLValidator();


		// is that document valid?
		if (!validator.validate(url))		   
		   System.err.println (validator.getError());

		// get the document
		CellMLDocument doc = validator.getDocument();
		if (doc==null) return documentNode;
			
		CellMLModel model = doc.getModel ();
		
//		try {
//			//TODO remove that!
//			filePath = StringUtils.replace(filePath, "models.cellml.org", "184.169.251.126");			
//			model = CellMLReader.loadFromURL(cLoader.getCellMLBootstrap(), filePath);
//		} catch (RuntimeException rte) { 
//			return documentNode;
//		} 
		
		
		//documentNode.setProperty(Property.CellML.VERSION, );		
		if (versionID!=null) documentNode.setProperty(Property.General.VERSIONID, versionID);
		
		
			
		Node modelNode = graphDB.createNode();
		documentNode.createRelationshipTo(modelNode, DocumentRelTypes.HAS_MODEL);
		modelNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO);
		modelNode.setProperty(Property.General.NAME, model.getName());

		modelIndex.add(modelNode, Property.General.NAME, model.getName());
		modelNode.addLabel(NodeLabel.Types.MODEL);
		modelNode.addLabel(NodeLabel.Types.CELLML_MODEL);
		

		if (model.getMetaId()!=null) {
			modelNode.setProperty(Property.General.ID, model.getMetaId());
			modelIndex.add(modelNode, Property.General.ID, model.getMetaId()); 		
		}
		
		extractModelMetadata(modelNode, url);

		
		Map<String, Map<Node, Map<String, Node>>> componentNodes = extractCellmlComponents(model.getComponents(), modelNode);
		
		
		extractCellmlConnections(model.getComponents(), componentNodes);
		componentNodes.clear();
		
		return documentNode;
	}



	private static void extractModelMetadata(Node modelNode, URL url) {
	
		CmetaContainer cmeta = readCmeta(url);
		if (cmeta==null) return;

		//create new annotation if there is something...
		Node annotationNode = graphDB.createNode();
		annotationNode.addLabel(NodeLabel.Types.ANNOTATION);
		annotationNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
		modelNode.createRelationshipTo(annotationNode, AnnotationRelTypes.HAS_ANNOTATION);
		
		String[] modelAuthor = StringUtils.split(cmeta.getModel_author());
		if ((modelAuthor!=null) && (modelAuthor.length > 0)){		
			String firstName = modelAuthor[0];
			String lastName = null;
			if (modelAuthor.length>1) {
				lastName = modelAuthor[1];
			} else {
				//use the only name provided as last name
				lastName = firstName;
				firstName = null;
			}
			PersonWrapper person = new PersonWrapper(firstName, lastName, null, cmeta.getModel_author_org());
			processPerson(person, modelNode, annotationNode, DocumentRelTypes.IS_CREATOR);
		}
		if (cmeta.getCitation_authors()!=null){
			List<PersonWrapper> authorList = new LinkedList<PersonWrapper>();
			for (Iterator<List<String>> iterator = cmeta.getCitation_authors().iterator(); iterator.hasNext();) {
				List<String> pubAuthor = (List<String>) iterator.next();
				String firstName = null;
				String lastName = null;
				try {
					lastName = pubAuthor.get(0);
					firstName = pubAuthor.get(1);
				} catch (IndexOutOfBoundsException e) {
				
				}
				if (StringUtils.isEmpty(lastName)) continue;
				authorList.add(new PersonWrapper(firstName, lastName, null, null));
			}
			if (!StringUtils.isEmpty(cmeta.getCitation_title())){
				PublicationWrapper publication = new PublicationWrapper(cmeta.getCitation_title(), cmeta.getCitation_journal(), null, null, null, cmeta.getCitation_id(), authorList);
				processPublication(publication, annotationNode, modelNode);
			}
		}
		
		if (!StringUtils.isEmpty(cmeta.getCitation_id())){
			String res = cmeta.getCitation_id();
			Node resource = annotationIndex.get(Property.General.URI, res).getSingle();
			if (resource==null){
				resource = graphDB.createNode();
				resource.setProperty(Property.General.URI, res);
				resource.addLabel(NodeLabel.Types.RESOURCE);
				annotationIndex.add(resource, Property.General.URI, res);
			}
			//create a dynamic relationship based on the qualifier
			annotationNode.createRelationshipTo(resource, DynamicRelationshipType.withName("isDescribedBy"));
			resource.createRelationshipTo(annotationNode, DatabaseRelTypes.BELONGS_TO);
		}
		
	}

	private static void extractCellmlConnections(HashMap<String, CellMLComponent> components,
			Map<String, Map<Node, Map<String, Node>>> componentNodes) {
		
		for (Iterator<CellMLComponent> iComp = components.values().iterator(); iComp.hasNext();) {
			CellMLComponent comp = (CellMLComponent) iComp.next();
			
			Node in, out;
			List<CellMLVariable> cvar;
			
			HashMap<String, CellMLVariable> variables = comp.getVariables();
			for (Iterator<CellMLVariable> iVar = variables.values().iterator(); iVar.hasNext();) {
				CellMLVariable var = (CellMLVariable) iVar.next();
				
				//private interfaces
				cvar = var.getPrivateInterfaceConnections();
				for (Iterator<CellMLVariable> iterator = cvar.iterator(); iterator.hasNext();) {
					CellMLVariable connectedVar = (CellMLVariable) iterator.next();
					CellMLComponent connectedComponent = connectedVar.getComponent();
					
					if (var.getPrivateInterface() == CellMLVariable.INTERFACE_IN) {
						in  = componentNodes.get(comp.getName()).values().iterator().next().get(var.getName());
						out = componentNodes.get(connectedComponent.getName()).values().iterator().next().get(connectedVar.getName()); 	
						out.createRelationshipTo(in, CellmlRelTypes.IS_CONNECTED_TO).setProperty(Property.CellML.ISPRIVATECONNECTION, true);
					}										
				}
				
				//public interfaces
				cvar = var.getPublicInterfaceConnections();
				for (Iterator<CellMLVariable> iterator = cvar.iterator(); iterator.hasNext();) {
					CellMLVariable connectedVar = (CellMLVariable) iterator.next();
					CellMLComponent connectedComponent = connectedVar.getComponent();
					
					if (var.getPublicInterface() == CellMLVariable.INTERFACE_IN) {
						in  = componentNodes.get(comp.getName()).values().iterator().next().get(var.getName());
						out = componentNodes.get(connectedComponent.getName()).values().iterator().next().get(connectedVar.getName()); 	
						
						out.createRelationshipTo(in, CellmlRelTypes.IS_CONNECTED_TO).setProperty(Property.CellML.ISPRIVATECONNECTION, false);
					}										
				}
			}
		}
		

	}

	private static Map<String, Map<Node, Map<String, Node>>> extractCellmlComponents(
			HashMap<String, CellMLComponent> components, Node modelNode) {
		
		Map<String, Map<Node, Map<String, Node>>> componentNodes = new HashMap<String, Map<Node, Map<String, Node>>>();		
		
		for (Iterator<CellMLComponent> iterator = components.values().iterator(); iterator.hasNext();) {
			CellMLComponent comp = (CellMLComponent) iterator.next();
			Node componentNode = graphDB.createNode();			
			componentNode.createRelationshipTo(modelNode, DatabaseRelTypes.BELONGS_TO);
			modelNode.createRelationshipTo(componentNode, CellmlRelTypes.HAS_COMPONENT);
			componentNode.addLabel(NodeLabel.Types.CELLML_COMPONENT);
			
			componentNode.setProperty(Property.General.NAME, comp.getName());			
			modelIndex.add(modelNode, Property.CellML.COMPONENT, comp.getName());
			
			if (comp.getMetaId()!=null){
				componentNode.setProperty(Property.General.ID, comp.getMetaId());
				modelIndex.add(modelNode, Property.CellML.COMPONENT, comp.getMetaId());
			}
			
			Map<String, Node> variableNodes = extractCellmlVariables(comp.getVariables(), componentNode, modelNode);			
			Map<Node, Map<String, Node>> compNodeToVariables = new HashMap<Node, Map<String,Node>>();
			compNodeToVariables.put(componentNode, variableNodes);
			componentNodes.put(comp.getName(), compNodeToVariables);
			
			extractCellmlReactions(comp.getReactions(), componentNode);
			
			
		}

		return componentNodes;
	}

	private static void extractCellmlReactions(List<CellMLReaction> reactions,	Node componentNode) {

		for (Iterator<CellMLReaction> iterator = reactions.iterator(); iterator.hasNext();) {
			CellMLReaction react = (CellMLReaction) iterator.next();
			Node reactionNode = graphDB.createNode();
			reactionNode.addLabel(NodeLabel.Types.CELLML_REACTION);
			reactionNode.createRelationshipTo(componentNode, DatabaseRelTypes.BELONGS_TO);
			componentNode.createRelationshipTo(reactionNode, CellmlRelTypes.HAS_REACTION);
			
			reactionNode.setProperty( Property.CellML.REVERSIBLE, react.isReversible());
		}
		
	}

	private static Map<String, Node> extractCellmlVariables(HashMap<String, CellMLVariable>  variableSet,
			Node componentNode, Node modelNode) {
		Map<String, Node> variableNodes = new HashMap<String, Node>();
		
		for (Iterator<CellMLVariable> iterator = variableSet.values().iterator(); iterator.hasNext();) {
			CellMLVariable variable = (CellMLVariable) iterator.next();

			Node variableNode = graphDB.createNode();
			variableNode.createRelationshipTo(componentNode, DatabaseRelTypes.BELONGS_TO);
			componentNode.createRelationshipTo(variableNode, CellmlRelTypes.HAS_VARIABLE);
			variableNode.addLabel(NodeLabel.Types.CELLML_VARIABLE);
			variableNode.setProperty(Property.General.NAME, variable.getName());
			modelIndex.add(modelNode, Property.CellML.VARIABLE, variable.getName());
			
			if(variable.getMetaId()!=null){
				variableNode.setProperty(Property.General.ID, variable.getMetaId());
				modelIndex.add(modelNode, Property.CellML.VARIABLE, variable.getMetaId());
			}	
			variableNodes.put(variable.getName(), variableNode);			
		}

		return variableNodes;
		
	}
	
	private static CmetaContainer readCmeta(URL linkToModel){
		
		int timeout = 10000;
		
		URL url;
		URLConnection connection;
		InputStream stream;
		String text = "";
		try {
			url = new URL(linkToModel + "/@@cmeta");
			connection = url.openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestProperty("Content-Type", "application/vnd.physiome.pmr2.json.0");
			connection.setRequestProperty("Accept", "application/vnd.physiome.pmr2.json.0");

			stream = connection.getInputStream(); 
			text = IOUtils.toString(stream);
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		Gson gson = new Gson();
		CmetaContainer cmeta = null;
		
		try {
			cmeta = gson.fromJson(text, CmetaContainer.class);
		} catch (Exception e) {
			cmeta = null;
		}		
		return cmeta;	
	}
	
	
}
