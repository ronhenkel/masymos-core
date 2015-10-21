package de.unirostock.sems.masymos.query.enumerator;

import de.unirostock.sems.masymos.configuration.Property;

public enum CellMLModelFieldEnumerator {

	// model index:
	NAME, ID, COMPONENT, VARIABLE, CREATOR, AUTHOR,// URI,

	//not specified
	NONE;
	
	
	public String getElementNameEquivalent() {
		switch (this) {
		case NAME:
			return Property.General.NAME;
		case ID:
			return Property.General.ID;
		case COMPONENT:
			return Property.CellML.COMPONENT;
		case VARIABLE:
			return Property.CellML.VARIABLE;
		case AUTHOR:
			return Property.Publication.AUTHOR;
		case CREATOR:
			return Property.General.CREATOR;
//		case URI:
//			return Property.General.URI;
		case NONE:
			return "NONE";	
		default:
			return "NONE";
		}
	}
	
	public int getElementWeightEquivalent() {
		switch (this) {
		case NAME:
			return 3;
		case ID:
			return 3;
		case COMPONENT:
			return 2;
		case VARIABLE:
			return 2;
		case AUTHOR:
			return 2;
		case CREATOR:
			return 1;
//		case URI:
//			return 2;
		case NONE:
			return 1;	
		default:
			return 1;
		}
	}
}
