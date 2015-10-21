package de.unirostock.sems.masymos.configuration;

import org.neo4j.graphdb.RelationshipType;


public class Relation {

	public static enum SbmlRelTypes implements RelationshipType
	{   
	    /*
	     * Relations to describe SBML models
	     */
	    HAS_COMPARTMENT, 
	    HAS_SPECIES, 
	    HAS_PARAMETER,
	    IS_LOCATED_IN, 
	    CONTAINS_SPECIES,
	    CONTAINS_REACTION,
	    IS_MODIFIER, 
	    HAS_REACTION, 
	    HAS_MODIFIER, 
	    HAS_PRODUCT, 
	    IS_PRODUCT, 
	    HAS_REACTANT, 
	    IS_REACTANT, 
	    HAS_RULE, 
	    HAS_FUNCTION, 
	    HAS_EVENT, 
	    HAS_SBOTERM
	    
	}
	
	public static enum DocumentRelTypes implements RelationshipType
	{
		 /*
	     * Relations to describe documents
	     */
	    HAS_DOCUMENT,
	    HAS_MODEL,
	    HAS_SEDML,
	    HAS_PUBLICATION,
	    HAS_AUTHOR,
	    IS_CREATOR 
		
	}
	
	public static enum DatabaseRelTypes implements RelationshipType
	{
		 /*
	     * Relations to describe database
	     */
	    BELONGS_TO,
	    HAS_SUCCESSOR,
	    HAS_PREDECESSOR
		
	}
	
	public static enum CellmlRelTypes implements RelationshipType
	{
	    /*
	     * Relations to describe CellML documents
	     */
	    HAS_COMPONENT,
	    HAS_VARIABLE,
	    HAS_GROUP,
	    HAS_REACTION,
	    IS_CONNECTED_TO,
	    IS_MAPPED_TO,
	    IS_DELTA_VAR,
	    HAS_DELTA_VAR
	    
		
	}
	
	public static enum XmlRelTypes implements RelationshipType
	{
		/*
		 * Relations to describe XML Parent-Child relations
		 */
	    CHILD,
	    PARENT,
	    ATTRIBUTE,
	    VALUE,
	    ELEMENT
	}    
	
	public static enum AnnotationRelTypes implements RelationshipType
	{
		/*
		 * Relations to describe annotation relations
		 * !!! some annotations, i.e. BioModels qualifiers, are
		 * generated on the fly to errors when changes on the 
		 * qualifiers occur
		 */
	    HAS_ANNOTATION
	} 
	
	public static enum SedmlRelTypes implements RelationshipType
	{
	    
	    HAS_MODELREFERENCE,
	    HAS_OUTPUT,
	    HAS_SURFACE,
	    HAS_CURVE,
	    HAS_DATASET,
	    HAS_SIMULATION,
	    HAS_DATAGENERATOR,
	    HAS_TASK,
	    HAS_VARIABLE,
	    IS_ENTITY_OF,
	    SIMULATES,
	    IS_SIMULATED,
	    USED_IN_DATAGENERATOR,
	    CALCULATES_MODEL,
	    IS_REFERENCED_IN_TASK,
	    REFERENCES_SIMULATION,
	    REFERENCES_MODEL
	}

	public static enum SimulationLinkRelTypes implements RelationshipType
	{   
	    /*
	     * Relations to describe relation
	     */
	    LINKS_TO_MODEL,
	    LINKS_TO_SIMULATION
	    
	}
	
	
	
	
}
