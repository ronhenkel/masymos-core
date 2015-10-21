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

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation.DatabaseRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.DocumentRelTypes;
import de.unirostock.sems.masymos.configuration.Relation.SedmlRelTypes;
import de.unirostock.sems.masymos.extractor.Extractor;


public class SEDMLExtractor extends Extractor{
	
	

	public static Node extractStoreIndex(File sedFile, String versionID) throws XMLStreamException, IOException{
		 
		//TODO: include publication wrapper
         Node documentNode = null;   
         try (Transaction tx = graphDB.beginTx()){
                 documentNode = extractFromSEDML(IOUtils.toString(sedFile.toURI()), versionID);
                 tx.success();
         } catch (XMLStreamException e) {
                 documentNode = null;
                 //TODO Log me
                 System.out.println("Error XMLStreamException while parsing model");
                 System.out.println(e.getMessage());
         }
         return documentNode;
 }

	public static Node extractStoreIndex(String sedFile, String versionID) throws XMLStreamException, IOException{
		 
		//TODO: include publication wrapper
         Node documentNode = null;   
         try (Transaction tx = graphDB.beginTx()){
                 documentNode = extractFromSEDML(sedFile, versionID);
                 tx.success();
         } catch (XMLStreamException e) {
                 documentNode = null;
                 //TODO Log me
                 System.out.println("Error XMLStreamException while parsing model");
                 System.out.println(e.getMessage());
         }
         return documentNode;
 }	
	

	private static Node extractFromSEDML(String sedFile, String versionID) throws XMLStreamException {
		
		SEDMLDocument doc = null;
		try {
			doc = Libsedml.readDocumentFromString(sedFile);
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//create SEDML document
		Node documentNode = graphDB.createNode();
		documentNode.addLabel(NodeLabel.Types.DOCUMENT);
		//documentNode.setProperty(Property.SEDML.VERSION, doc.getVersion());
		if (versionID!=null) documentNode.setProperty(Property.General.VERSIONID, versionID);

		SedML sed = doc.getSedMLModel(); 
		
		//create SEDML node
		Node sedmlNode = graphDB.createNode();
		documentNode.createRelationshipTo(sedmlNode, DocumentRelTypes.HAS_SEDML);
		sedmlNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO);
		sedmlNode.addLabel(NodeLabel.Types.SEDML);
		
		
		//create Modelnodes
		Map<String, Node> modelNodes = extractSEDMLModels(sed.getModels(),sedmlNode);
		
		//create Simulationnodes
		Map<String, Node> simulationNodes = extractSEDMLSimulations(sed.getSimulations(), sedmlNode);
		//create Task-Relationships
		Map<String,Node[]> taskNodes= extractSEDMLTasks(sed.getTasks(), sedmlNode, modelNodes, simulationNodes);
		//create DataGenerators
		Map<String,Node> datageneratorNodes = extractSEDMLDataGenerators(sed.getDataGenerators(), sedmlNode, taskNodes);
		//create Outputnodes
		extractSEDMLOutputs(sed.getOutputs(),sedmlNode,datageneratorNodes);	
	 
	//TODO !!!!!!! Fehler abfangen etc.
	return documentNode;
}
	
		
	
		
	private static Map<String, Node> extractSEDMLModels(List<Model> listOfModelReferences, Node sedmlNode) {
		
		Map<String, Node> modelNodes = new HashMap<String, Node>();
		
		for (Model model : listOfModelReferences){
			Node modelNode = graphDB.createNode();
			sedmlNode.createRelationshipTo(modelNode, SedmlRelTypes.HAS_MODELREFERENCE);
			modelNode.createRelationshipTo(sedmlNode, DatabaseRelTypes.BELONGS_TO);
			
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
	

	private static void extractSEDMLOutputs(List<Output> listOfOutputs, Node documentNode, Map<String,Node> datageneratorNodes) {
		
		for (Output output : listOfOutputs){
			Node outputNode = graphDB.createNode();
			documentNode.createRelationshipTo(outputNode, SedmlRelTypes.HAS_OUTPUT);
			outputNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO);
			outputNode.setProperty(Property.SEDML.OUTPUT_TYPE, output.getKind());
			outputNode.addLabel(NodeLabel.Types.SEDML_OUTPUT);
			
			
			//index model name and id
			//nodeFullTextIndex.add(outputNode, Property.SBML.NAME, output.getName());
			//nodeFullTextIndex.add(outputNode, Property.General.ID, output.getId());
			
			switch (output.getKind()) {
				case "Plot2D": 	 
								extractSEDMLOutCurves(((Plot2D)output).getListOfCurves(), outputNode, datageneratorNodes);
								break;
				case "Plot3D": 	extractSEDMLOutSurfaces(((Plot3D)output).getListOfSurfaces(), outputNode);
								break;
				case "Report": 	extractSEDMLOutDatasets(((Report)output).getListOfDataSets(), outputNode);
								break;
				default:  break;
			}
		
			
			//private static void extractSEDMLCurves(ListOf<Reaction> listOfReaction, Node modelNode, Map<String, Node> compartmentList, Map<String, Node> speciesList)
		}	
	}
	
	private static void extractSEDMLOutCurves(List<Curve> listOfCurve, Node outputNode, Map<String,Node> datageneratorNodes) {
		
		for (Curve curve : listOfCurve){
			Node curveNode = graphDB.createNode();
			outputNode.createRelationshipTo(curveNode, SedmlRelTypes.HAS_CURVE);
			curveNode.createRelationshipTo(outputNode, DatabaseRelTypes.BELONGS_TO);
						
			curveNode.addLabel(NodeLabel.Types.SEDML_CURVE);
			
			if ((curve.getXDataReference()).equalsIgnoreCase("time")) {curveNode.setProperty(Property.SEDML.XDATA, curve.getXDataReference());}
				else 
					for (Relationship rel : ((datageneratorNodes.get(curve.getXDataReference())).getRelationships(SedmlRelTypes.CALCULATES_MODEL)))
					{
						Node modelNode = rel.getEndNode();
						curveNode.createRelationshipTo(modelNode, SedmlRelTypes.IS_ENTITY_OF);
						//modelNode.createRelationshipTo(curveNode, DatabaseRelTypes.BELONGS_TO_CURVE);
					}
			
			if ((curve.getYDataReference()).equalsIgnoreCase("time")) {curveNode.setProperty(Property.SEDML.YDATA, curve.getYDataReference());}
			else 
			{	
				for (Relationship rel : ((datageneratorNodes.get(curve.getYDataReference())).getRelationships(SedmlRelTypes.CALCULATES_MODEL)))
				{
					Node modelNode = rel.getEndNode();
					curveNode.createRelationshipTo(modelNode, SedmlRelTypes.IS_ENTITY_OF);
					//modelNode.createRelationshipTo(curveNode, RelTypes.BELONGS_TO_CURVE);
				}
			}
		}	
	}
	
	private static void extractSEDMLOutSurfaces(List<Surface> listOfSurface, Node outputNode) {
		
		for (Surface surface : listOfSurface){
			Node surfaceNode = graphDB.createNode();
			outputNode.createRelationshipTo(surfaceNode, SedmlRelTypes.HAS_SURFACE);
			surfaceNode.createRelationshipTo(outputNode, DatabaseRelTypes.BELONGS_TO);
			surfaceNode.setProperty(Property.SEDML.XDATA, surface.getXDataReference());
			surfaceNode.setProperty(Property.SEDML.YDATA, surface.getYDataReference());
			surfaceNode.setProperty(Property.SEDML.ZDATA, surface.getZDataReference());
			
			surfaceNode.addLabel(NodeLabel.Types.SEDML_SURFACE);
			//index model name and id
			//nodeFullTextIndex.add(outputNode, Property.SBML.NAME, output.getName());
			//nodeFullTextIndex.add(outputNode, Property.General.ID, output.getId());
		}	
	}
	
	private static void extractSEDMLOutDatasets(List<DataSet> listOfDataSet, Node outputNode) {
		
		for (DataSet dataset : listOfDataSet){
			Node datasetNode = graphDB.createNode();
			outputNode.createRelationshipTo(datasetNode, SedmlRelTypes.HAS_DATASET);
			datasetNode.createRelationshipTo(outputNode, DatabaseRelTypes.BELONGS_TO);
			datasetNode.setProperty(Property.SEDML.DATALABEL, dataset.getLabel());
			
			datasetNode.addLabel(NodeLabel.Types.SEDML_DATASET);

			//index model name and id
			//nodeFullTextIndex.add(outputNode, Property.SBML.NAME, output.getName());
			//nodeFullTextIndex.add(outputNode, Property.General.ID, output.getId());
		}	
	}
	
	private static Map<String, Node> extractSEDMLSimulations(List<Simulation> listOfSimulation, Node documentNode) {
		
		Map<String, Node> simulationNodes = new HashMap<String, Node>();
		
		for (Simulation simulation : listOfSimulation){
			Node simulationNode = graphDB.createNode();
			documentNode.createRelationshipTo(simulationNode, SedmlRelTypes.HAS_SIMULATION);
			simulationNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO);
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
	
	
private static Map<String,Node> extractSEDMLDataGenerators(List<DataGenerator> listOfDataGenerator, Node documentNode, Map<String,Node[]> taskNodes) {
		
		Map<String,Node> datageneratorNodes = new HashMap<String,Node>();
	
	
		for (DataGenerator datagenerator : listOfDataGenerator){
			Node datageneratorNode = graphDB.createNode();
			documentNode.createRelationshipTo(datageneratorNode, SedmlRelTypes.HAS_DATAGENERATOR);
			datageneratorNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO);			
			datageneratorNode.addLabel(NodeLabel.Types.SEDML_DATAGENERATOR);
			datageneratorNode.setProperty(Property.SEDML.MATH, datagenerator.getMath().toString());
			//datageneratorNode.setProperty(Property.General.NAME, datagenerator.getName());
			datageneratorNode.setProperty(Property.General.ID, datagenerator.getId());
			
			for (Variable var : datagenerator.getListOfVariables()){
				Node variableNode = graphDB.createNode();
				//variableNode.setProperty(Property.General.NAME, var.getName());
				variableNode.setProperty(Property.General.ID, var.getId());
				if (!StringUtils.isEmpty(var.getTarget())) variableNode.setProperty(Property.SEDML.TARGET, var.getTarget());
				variableNode.createRelationshipTo(datageneratorNode, DatabaseRelTypes.BELONGS_TO);				
				datageneratorNode.createRelationshipTo(variableNode,SedmlRelTypes.HAS_VARIABLE);
				variableNode.addLabel(NodeLabel.Types.SEDML_VARIABLE);
								
				Node modelNode = (taskNodes.get(var.getReference()))[0]; 
				variableNode.createRelationshipTo(modelNode, SedmlRelTypes.CALCULATES_MODEL);
				modelNode.createRelationshipTo(variableNode, SedmlRelTypes.USED_IN_DATAGENERATOR);
				
			}
			
			datageneratorNodes.put(datagenerator.getId(), datageneratorNode);
			
		}
		
		return datageneratorNodes;
	}

private static Map<String, Node[]> extractSEDMLTasks(List<Task> listOfTask, Node documentNode, Map<String,Node> modelNodes, Map<String,Node> simulationNodes){
	
	Map<String,Node[]> tasks = new HashMap<String,Node[]>();
	
	
		for (Task task : listOfTask){
		
			Node simNode = simulationNodes.get(task.getSimulationReference());
			Node modelNode = modelNodes.get(task.getModelReference());
		
			Node taskNode = graphDB.createNode();
			documentNode.createRelationshipTo(taskNode, SedmlRelTypes.HAS_TASK);
			taskNode.createRelationshipTo(documentNode, DatabaseRelTypes.BELONGS_TO);
			
			taskNode.addLabel(NodeLabel.Types.SEDML_TASK);

			//TODO names
			simNode.createRelationshipTo(taskNode, SedmlRelTypes.IS_REFERENCED_IN_TASK);
			modelNode.createRelationshipTo(taskNode, SedmlRelTypes.IS_REFERENCED_IN_TASK);
			taskNode.createRelationshipTo(modelNode, SedmlRelTypes.REFERENCES_MODEL);
			taskNode.createRelationshipTo(simNode, SedmlRelTypes.REFERENCES_SIMULATION);
			simNode.createRelationshipTo(modelNode, SedmlRelTypes.SIMULATES);
			modelNode.createRelationshipTo(simNode, SedmlRelTypes.IS_SIMULATED);
			
			Node[] array = new Node[2];
			array[0] = modelNode;
			array[1] = simNode;
			tasks.put(task.getId(), array);
			
		}	
	
		return tasks;
	}



}


