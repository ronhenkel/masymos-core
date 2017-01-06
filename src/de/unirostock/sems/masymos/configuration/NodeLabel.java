package de.unirostock.sems.masymos.configuration;

import org.neo4j.graphdb.Label;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class NodeLabel {

	public static enum Types implements Label
	{   
		
		/*
		 * Properties to determine the type of a node in the DB
		 */	
		MODEL,		
		SEDML,
		DOCUMENT, 	
		ANNOTATION, 		
		PERSON, 		
		RESOURCE, 
		PUBLICATION, 
		
		//SBML specific
		SBML_MODEL,
		SBML_COMPARTMENT,
		SBML_REACTION, 
		SBML_SPECIES, 
		SBML_PARAMETER,
		SBML_RULE, 
		SBML_FUNCTION, 
		SBML_EVENT, 
		
		//CellML specific
		CELLML_MODEL,
		CELLML_COMPONENT, 
		CELLML_REACTION	,
		 
		CELLML_VARIABLE, 
			

		//SEDML specific
		SEDML_OUTPUT, 
		SEDML_CURVE, 
		SEDML_SURFACE, 
		SEDML_DATASET, 
		SEDML_SIMULATION, 
		SEDML_DATAGENERATOR, 
		SEDML_VARIABLE,
		SEDML_TASK, 
		SEDML_MODELREFERENCE 

	    
	}
	
	
}
