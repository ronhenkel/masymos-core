package de.unirostock.sems.masymos.util;


public class StructureQueryUtil {
	
//	public static String[] getConstituentTypes(){
//		String[] s = {NodeType.ANNOTATION, NodeType.CELLML_COMPONENT,  
//				      NodeType.CELLML_REACTION,	NodeType.CELLML_VARIABLE, NodeType.MODEL, 
//				      NodeType.PERSON, NodeType.PUBLICATION, NodeType.RESOURCE, 
//				      NodeType.SBML_COMPARTMENT, NodeType.SBML_FUNCTION, NodeType.SBML_PARAMETER,
//				      NodeType.SBML_REACTION, NodeType.SBML_RULE, NodeType.SBML_SPECIES};
//		return s;
//		
//	}
//	
//	public static String[] getConstituentProperties(String constituentType){
//		List<String> properties = new LinkedList<String>();
//		switch (constituentType) {
//		case NodeType.ANNOTATION:
//			break;
//		case NodeType.CELLML_COMPONENT:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.CELLML_REACTION:
//			properties.add(CellML.REVERSIBLE);
//			break;
//		case NodeType.CELLML_VARIABLE:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.MODEL:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.PERSON:
//			properties.add(Person.EMAIL);
//			properties.add(Person.FAMILYNAME);
//			properties.add(Person.GIVENNAME);
//			properties.add(Person.ORGANIZATION);
//			break;
//		case NodeType.PUBLICATION:
//			properties.add(Publication.ABSTRACT);
//			properties.add(Publication.AFFILIATION);
//			properties.add(Publication.JOURNAL);
//			properties.add(Publication.SYNOPSIS);
//			properties.add(Publication.TITLE);
//			properties.add(Publication.YEAR);
//			break;
//		case NodeType.RESOURCE:
//			properties.add(General.URI);
//			break;
//		case NodeType.SBML_COMPARTMENT:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.SBML_FUNCTION:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.SBML_PARAMETER:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.SBML_REACTION:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.SBML_RULE:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.SBML_SPECIES:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		default:
//			break;
//		}
//		
//		return properties.toArray(new String[0]);
//	}
//	
//	public static String[] getRelationshipProperties(String constituentType, boolean in){
//		List<String> properties = new LinkedList<String>();
//		switch (constituentType) {
//		case NodeType.ANNOTATION:
//			if (in) {
//				properties.add(AnnotationRelTypes.HAS_ANNOTATION.toString());
//			} else {
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.CELLML_COMPONENT:
//			if (in) {
//				properties.add(CellmlRelTypes.HAS_COMPONENT.toString());
//				properties.add(CellmlRelTypes.HAS_REACTION.toString());
//				properties.add(CellmlRelTypes.IS_CONNECTED_TO.toString());
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			} else {
//				properties.add(CellmlRelTypes.IS_CONNECTED_TO.toString());
//				properties.add(CellmlRelTypes.HAS_VARIABLE.toString());
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.CELLML_REACTION:
//			if (in) {
//				properties.add(CellmlRelTypes.HAS_REACTION.toString());
//			} else {
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.CELLML_VARIABLE:
//			if (in) {
//				properties.add(CellmlRelTypes.HAS_VARIABLE.toString());
//				properties.add(CellmlRelTypes.IS_MAPPED_TO.toString());
//				properties.add(CellmlRelTypes.IS_DELTA_VAR.toString());
//				properties.add(CellmlRelTypes.HAS_DELTA_VAR.toString());
//				
//			} else {
//				properties.add(CellmlRelTypes.IS_DELTA_VAR.toString());
//				properties.add(CellmlRelTypes.HAS_DELTA_VAR.toString());
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.MODEL:
//			properties.add(General.ID);
//			properties.add(General.NAME);
//			break;
//		case NodeType.PERSON:
//			properties.add(Person.EMAIL);
//			properties.add(Person.FAMILYNAME);
//			properties.add(Person.GIVENNAME);
//			properties.add(Person.ORGANIZATION);
//			break;
//		case NodeType.PUBLICATION:
//			properties.add(Publication.ABSTRACT);
//			properties.add(Publication.AFFILIATION);
//			properties.add(Publication.JOURNAL);
//			properties.add(Publication.SYNOPSIS);
//			properties.add(Publication.TITLE);
//			properties.add(Publication.YEAR);
//			break;
//		case NodeType.RESOURCE:
//			if (in) {
//				//biomodels.net qualifier
//				
//			} else {
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.SBML_COMPARTMENT:
//			if (in) {
//				properties.add(SbmlRelTypes.HAS_COMPARTMENT.toString());
//				properties.add(SbmlRelTypes.IS_LOCATED_IN.toString());
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//				
//			} else {
//				properties.add(SbmlRelTypes.CONTAINS_SPECIES.toString());
//				properties.add(SbmlRelTypes.CONTAINS_REACTION.toString());
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.SBML_FUNCTION:
//			if (in) {
//				properties.add(SbmlRelTypes.HAS_FUNCTION.toString());				
//			} else {
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.SBML_PARAMETER:
//			if (in) {
//				properties.add(SbmlRelTypes.HAS_PARAMETER.toString());				
//			} else {
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.SBML_REACTION:
//			if (in) {
//				properties.add(SbmlRelTypes.HAS_REACTION.toString());
//				properties.add(SbmlRelTypes.IS_MODIFIER.toString());
//				properties.add(SbmlRelTypes.IS_PRODUCT.toString());
//				properties.add(SbmlRelTypes.IS_REACTANT.toString());
//				properties.add(SbmlRelTypes.CONTAINS_REACTION.toString());
//				
//			} else {
//				properties.add(SbmlRelTypes.HAS_MODIFIER.toString());
//				properties.add(SbmlRelTypes.HAS_PRODUCT.toString());
//				properties.add(SbmlRelTypes.HAS_REACTANT.toString());
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.SBML_RULE:
//			if (in) {
//				properties.add(SbmlRelTypes.HAS_RULE.toString());				
//			} else {
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		case NodeType.SBML_SPECIES:
//			if (in) {
//				properties.add(SbmlRelTypes.HAS_SPECIES.toString());
//				properties.add(SbmlRelTypes.CONTAINS_SPECIES.toString());
//				properties.add(SbmlRelTypes.HAS_MODIFIER.toString());
//				properties.add(SbmlRelTypes.HAS_PRODUCT.toString());
//				properties.add(SbmlRelTypes.HAS_REACTANT.toString());
//				
//			} else {
//				properties.add(SbmlRelTypes.IS_MODIFIER.toString());
//				properties.add(SbmlRelTypes.IS_PRODUCT.toString());
//				properties.add(SbmlRelTypes.IS_REACTANT.toString());
//				properties.add(DatabaseRelTypes.BELONGS_TO.toString());
//			}
//			break;
//		default:
//			break;
//		}
//		
//		return properties.toArray(new String[0]);
//	}
//	
//	public static String[] getAllRelationshipProperties(){
//		List<String> properties = new LinkedList<String>();
//		
//		for (DocumentRelTypes rel : DocumentRelTypes.values()) {
//			properties.add(rel.toString());
//		}
//		
//		for (SbmlRelTypes rel : SbmlRelTypes.values()) {
//			properties.add(rel.toString());
//		}
//		
//		for (CellmlRelTypes rel : CellmlRelTypes.values()) {
//			properties.add(rel.toString());
//		}
//		
//		for (DatabaseRelTypes rel : DatabaseRelTypes.values()) {
//			properties.add(rel.toString());
//		}
//		for (AnnotationRelTypes rel : AnnotationRelTypes.values()) {
//			properties.add(rel.toString());
//		}
//		
//		return properties.toArray(new String[0]);
//	}

}
