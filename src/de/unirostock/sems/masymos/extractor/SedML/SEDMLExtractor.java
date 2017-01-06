package de.unirostock.sems.masymos.extractor.SedML;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jlibsedml.Curve;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.DataSet;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Model;
import org.jlibsedml.Output;
import org.jlibsedml.Plot2D;
import org.jlibsedml.Plot3D;
import org.jlibsedml.Report;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Simulation;
import org.jlibsedml.Surface;
import org.jlibsedml.Task;
import org.jlibsedml.Variable;
import org.jlibsedml.XMLException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation.DatabaseRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DocumentRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.SedmlRelTypes;
import de.unirostock.sems.masymos.database.IdFactory;
import de.unirostock.sems.masymos.extractor.Extractor;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class SEDMLExtractor extends Extractor{
	
	final static Logger logger = LoggerFactory.getLogger(SEDMLExtractor.class);
	

	public static Node extractStoreIndexSEDML(File sedFile, String versionID, Long uID) throws XMLStreamException, IOException{
		 
		//TODO: include publication wrapper
         Node documentNode = null;   
         try (Transaction tx = graphDB.beginTx()){
                 documentNode = extractFromSEDML(IOUtils.toString(sedFile.toURI()), versionID, uID);
                 tx.success();
         } catch (XMLStreamException e) {
                 documentNode = null;
                 logger.error("Error XMLStreamException while parsing model");
                 logger.error(e.getMessage());
         }
         return documentNode;
 }

	public static Node extractStoreIndexSEDML(String sedFile, String versionID, Long uID) throws XMLStreamException, IOException{
		 
		//TODO: include publication wrapper
         Node documentNode = null;   
         try (Transaction tx = graphDB.beginTx()){
                 documentNode = extractFromSEDML(sedFile, versionID, uID);
                 tx.success();
         } catch (XMLStreamException e) {
                 documentNode = null;
                 logger.error("Error XMLStreamException while parsing model");
                 logger.error(e.getMessage());
         }
         return documentNode;
 }	
	

	private static Node extractFromSEDML(String sedFile, String versionID, Long uID) throws XMLStreamException {
		
		SEDMLDocument doc = null;
		try {
			doc = Libsedml.readDocumentFromString(sedFile);
		} catch (XMLException e) {
			logger.error(e.getMessage());
		}
		//create SEDML document
		Node documentNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
		documentNode.addLabel(NodeLabel.Types.DOCUMENT);
		//documentNode.setProperty(Property.SEDML.VERSION, doc.getVersion());
		if (versionID!=null) documentNode.setProperty(Property.General.VERSIONID, versionID);

		SedML sed = doc.getSedMLModel(); 
		
		//create SEDML node
		Node sedmlNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
		IdFactory.instance().addToRelationshipDeleteIndex(documentNode.createRelationshipTo(sedmlNode, DocumentRelTypes.HAS_SEDML), uID);
		IdFactory.instance().addToRelationshipDeleteIndex(sedmlNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO), uID);
		sedmlNode.addLabel(NodeLabel.Types.SEDML);
		
		
		//create Modelnodes
		Map<String, Node> modelNodes = extractSEDMLModels(sed.getModels(),sedmlNode, uID);
		
		//create Simulationnodes
		Map<String, Node> simulationNodes = extractSEDMLSimulations(sed.getSimulations(), sedmlNode, uID);
		//create Task-Relationships
		Map<String,Node[]> taskNodes= extractSEDMLTasks(sed.getTasks(), sedmlNode, modelNodes, simulationNodes, uID);
		//create DataGenerators
		Map<String,Node> datageneratorNodes = extractSEDMLDataGenerators(sed.getDataGenerators(), sedmlNode, taskNodes, uID);
		//create Outputnodes
		extractSEDMLOutputs(sed.getOutputs(),sedmlNode,datageneratorNodes, uID);	
	 
	//TODO !!!!!!! Fehler abfangen etc.
	return documentNode;
}
	
		
	
		
	private static Map<String, Node> extractSEDMLModels(List<Model> listOfModelReferences, Node sedmlNode, Long uID) {
		
		Map<String, Node> modelNodes = new HashMap<String, Node>();
		
		for (Model model : listOfModelReferences){
			Node modelNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(sedmlNode.createRelationshipTo(modelNode, SedmlRelTypes.HAS_MODELREFERENCE), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(sedmlNode, DatabaseRelTypes.BELONGS_TO), uID);
			
			modelNode.setProperty(Property.SEDML.MODELCHANGED, model.hasChanges());
			modelNode.addLabel(NodeLabel.Types.SEDML_MODELREFERENCE);
			
			//map model id to node for linking
			modelNodes.put(model.getId(), modelNode);
			
			//dissolve variables in source property of changed models
			if (model.hasChanges())
			{
				modelNode.setProperty(Property.SEDML.MODELSOURCE, (modelNodes.get(model.getSource())).getProperty(Property.SEDML.MODELSOURCE));
			} 
			else modelNode.setProperty(Property.SEDML.MODELSOURCE, model.getSource());
			
			sedmlIndex.add(sedmlNode, Property.SEDML.MODELSOURCE, modelNode.getProperty(Property.SEDML.MODELSOURCE));
			sedmlIndex.add(sedmlNode, Property.General.URI, modelNode.getProperty(Property.SEDML.MODELSOURCE));
		}
		
		return modelNodes;
	}
	

	private static void extractSEDMLOutputs(List<Output> listOfOutputs, Node documentNode, Map<String,Node> datageneratorNodes, Long uID) {
		
		for (Output output : listOfOutputs){
			Node outputNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(documentNode.createRelationshipTo(outputNode, SedmlRelTypes.HAS_OUTPUT), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(outputNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO), uID);
			outputNode.setProperty(Property.SEDML.OUTPUT_TYPE, output.getKind());
			outputNode.addLabel(NodeLabel.Types.SEDML_OUTPUT);
			
			
			//index model name and id
			//nodeFullTextIndex.add(outputNode, Property.SBML.NAME, output.getName());
			//nodeFullTextIndex.add(outputNode, Property.General.ID, output.getId());
			
			switch (output.getKind()) {
				case "Plot2D": 	 
								extractSEDMLOutCurves(((Plot2D)output).getListOfCurves(), outputNode, datageneratorNodes, uID);
								break;
				case "Plot3D": 	extractSEDMLOutSurfaces(((Plot3D)output).getListOfSurfaces(), outputNode, uID);
								break;
				case "Report": 	extractSEDMLOutDatasets(((Report)output).getListOfDataSets(), outputNode, uID);
								break;
				default:  break;
			}
		
			
			//private static void extractSEDMLCurves(ListOf<Reaction> listOfReaction, Node modelNode, Map<String, Node> compartmentList, Map<String, Node> speciesList)
		}	
	}
	
	private static void extractSEDMLOutCurves(List<Curve> listOfCurve, Node outputNode, Map<String,Node> datageneratorNodes, Long uID) {
		
		for (Curve curve : listOfCurve){
			Node curveNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(outputNode.createRelationshipTo(curveNode, SedmlRelTypes.HAS_CURVE), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(curveNode.createRelationshipTo(outputNode, DatabaseRelTypes.BELONGS_TO), uID);
						
			curveNode.addLabel(NodeLabel.Types.SEDML_CURVE);
			
			if ((curve.getXDataReference()).equalsIgnoreCase("time")) {curveNode.setProperty(Property.SEDML.XDATA, curve.getXDataReference());}
				else 
					for (Relationship rel : ((datageneratorNodes.get(curve.getXDataReference())).getRelationships(SedmlRelTypes.CALCULATES_MODEL)))
					{
						Node modelNode = rel.getEndNode();
						IdFactory.instance().addToRelationshipDeleteIndex(curveNode.createRelationshipTo(modelNode, SedmlRelTypes.IS_ENTITY_OF), uID);
						//modelNode.createRelationshipTo(curveNode, DatabaseRelTypes.BELONGS_TO_CURVE);
					}
			
			if ((curve.getYDataReference()).equalsIgnoreCase("time")) {curveNode.setProperty(Property.SEDML.YDATA, curve.getYDataReference());}
			else 
			{	
				for (Relationship rel : ((datageneratorNodes.get(curve.getYDataReference())).getRelationships(SedmlRelTypes.CALCULATES_MODEL)))
				{
					Node modelNode = rel.getEndNode();
					IdFactory.instance().addToRelationshipDeleteIndex(curveNode.createRelationshipTo(modelNode, SedmlRelTypes.IS_ENTITY_OF), uID);
					//modelNode.createRelationshipTo(curveNode, RelTypes.BELONGS_TO_CURVE);
				}
			}
		}	
	}
	
	private static void extractSEDMLOutSurfaces(List<Surface> listOfSurface, Node outputNode, Long uID) {
		
		for (Surface surface : listOfSurface){
			Node surfaceNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(outputNode.createRelationshipTo(surfaceNode, SedmlRelTypes.HAS_SURFACE), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(surfaceNode.createRelationshipTo(outputNode, DatabaseRelTypes.BELONGS_TO), uID);
			surfaceNode.setProperty(Property.SEDML.XDATA, surface.getXDataReference());
			surfaceNode.setProperty(Property.SEDML.YDATA, surface.getYDataReference());
			surfaceNode.setProperty(Property.SEDML.ZDATA, surface.getZDataReference());
			
			surfaceNode.addLabel(NodeLabel.Types.SEDML_SURFACE);
			//index model name and id
			//nodeFullTextIndex.add(outputNode, Property.SBML.NAME, output.getName());
			//nodeFullTextIndex.add(outputNode, Property.General.ID, output.getId());
		}	
	}
	
	private static void extractSEDMLOutDatasets(List<DataSet> listOfDataSet, Node outputNode, Long uID) {
		
		for (DataSet dataset : listOfDataSet){
			Node datasetNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(outputNode.createRelationshipTo(datasetNode, SedmlRelTypes.HAS_DATASET), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(datasetNode.createRelationshipTo(outputNode, DatabaseRelTypes.BELONGS_TO), uID);
			datasetNode.setProperty(Property.SEDML.DATALABEL, dataset.getLabel());
			
			datasetNode.addLabel(NodeLabel.Types.SEDML_DATASET);

			//index model name and id
			//nodeFullTextIndex.add(outputNode, Property.SBML.NAME, output.getName());
			//nodeFullTextIndex.add(outputNode, Property.General.ID, output.getId());
		}	
	}
	
	private static Map<String, Node> extractSEDMLSimulations(List<Simulation> listOfSimulation, Node documentNode, Long uID) {
		
		Map<String, Node> simulationNodes = new HashMap<String, Node>();
		
		for (Simulation simulation : listOfSimulation){
			Node simulationNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(documentNode.createRelationshipTo(simulationNode, SedmlRelTypes.HAS_SIMULATION), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(simulationNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO), uID);
			simulationNode.setProperty(Property.SEDML.SIM_TYPE, simulation.getSimulationKind());
			simulationNode.setProperty(Property.SEDML.SIM_KISAO, simulation.getAlgorithm().getKisaoID());
			
			simulationNode.addLabel(NodeLabel.Types.SEDML_SIMULATION);
			//index simulation name and id
			//nodeFullTextIndex.add(simulationNode, Property.SBML.NAME, simulation.getName());
			//nodeFullTextIndex.add(simulationNode, Property.General.ID, simulation.getId());
			
			//map model id to node for linking
			simulationNodes.put(simulation.getId(), simulationNode);
		}	
		
		return simulationNodes;
	}
	
	
private static Map<String,Node> extractSEDMLDataGenerators(List<DataGenerator> listOfDataGenerator, Node documentNode, Map<String,Node[]> taskNodes, Long uID) {
		
		Map<String,Node> datageneratorNodes = new HashMap<String,Node>();
	
	
		for (DataGenerator datagenerator : listOfDataGenerator){
			Node datageneratorNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(documentNode.createRelationshipTo(datageneratorNode, SedmlRelTypes.HAS_DATAGENERATOR), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(datageneratorNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO), uID);			
			datageneratorNode.addLabel(NodeLabel.Types.SEDML_DATAGENERATOR);
			datageneratorNode.setProperty(Property.SEDML.MATH, datagenerator.getMath().toString());
			//datageneratorNode.setProperty(Property.General.NAME, datagenerator.getName());
			datageneratorNode.setProperty(Property.General.ID, datagenerator.getId());
			
			for (Variable var : datagenerator.getListOfVariables()){
				Node variableNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
				//variableNode.setProperty(Property.General.NAME, var.getName());
				variableNode.setProperty(Property.General.ID, var.getId());
				if (!StringUtils.isEmpty(var.getTarget())) variableNode.setProperty(Property.SEDML.TARGET, var.getTarget());
				IdFactory.instance().addToRelationshipDeleteIndex(variableNode.createRelationshipTo(datageneratorNode, DatabaseRelTypes.BELONGS_TO), uID);				
				IdFactory.instance().addToRelationshipDeleteIndex(datageneratorNode.createRelationshipTo(variableNode,SedmlRelTypes.HAS_VARIABLE), uID);
				variableNode.addLabel(NodeLabel.Types.SEDML_VARIABLE);
								
				Node modelNode = (taskNodes.get(var.getReference()))[0]; 
				IdFactory.instance().addToRelationshipDeleteIndex(variableNode.createRelationshipTo(modelNode, SedmlRelTypes.CALCULATES_MODEL), uID);
				IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(variableNode, SedmlRelTypes.USED_IN_DATAGENERATOR), uID);
				
			}
			
			datageneratorNodes.put(datagenerator.getId(), datageneratorNode);
			
		}
		
		return datageneratorNodes;
	}

private static Map<String, Node[]> extractSEDMLTasks(List<Task> listOfTask, Node documentNode, Map<String,Node> modelNodes, Map<String,Node> simulationNodes, Long uID){
	
	Map<String,Node[]> tasks = new HashMap<String,Node[]>();
	
	
		for (Task task : listOfTask){
		
			Node simNode = simulationNodes.get(task.getSimulationReference());
			Node modelNode = modelNodes.get(task.getModelReference());
		
			Node taskNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(documentNode.createRelationshipTo(taskNode, SedmlRelTypes.HAS_TASK), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(taskNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO), uID);
			
			taskNode.addLabel(NodeLabel.Types.SEDML_TASK);

			//TODO names
			IdFactory.instance().addToRelationshipDeleteIndex(simNode.createRelationshipTo(taskNode, SedmlRelTypes.IS_REFERENCED_IN_TASK), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(taskNode, SedmlRelTypes.IS_REFERENCED_IN_TASK), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(taskNode.createRelationshipTo(modelNode, SedmlRelTypes.REFERENCES_MODEL), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(taskNode.createRelationshipTo(simNode, SedmlRelTypes.REFERENCES_SIMULATION), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(simNode.createRelationshipTo(modelNode, SedmlRelTypes.SIMULATES), uID);
			IdFactory.instance().addToRelationshipDeleteIndex(modelNode.createRelationshipTo(simNode, SedmlRelTypes.IS_SIMULATED), uID);
			
			Node[] array = new Node[2];
			array[0] = modelNode;
			array[1] = simNode;
			tasks.put(task.getId(), array);
			
		}	
	
		return tasks;
	}



}


