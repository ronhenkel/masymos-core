package de.unirostock.sems.masymos.query.enumerator;

import de.unirostock.sems.masymos.configuration.Property;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/

public enum AnnotationFieldEnumerator {

	// annotation index
	URI, RESOURCETEXT, NONRDF,

	//not specified
	NONE;
	
	
	public String getElementNameEquivalent() {
		switch (this) {
		case URI:
			return Property.General.URI;
		case RESOURCETEXT:
			return Property.General.RESOURCETEXT;
		case NONRDF:
			return Property.General.NONRDF;	
		case NONE:
			return "NONE";	
		default:
			return "NONE";
		}
	}
	
	public int getElementWeightEquivalent(){
		switch (this) {
		case URI:
			return 4;
		case RESOURCETEXT:
			return 3;
		case NONRDF:
			return 2;	
		case NONE:
			return 1;	
		default:
			return 1;
	}
}

}
