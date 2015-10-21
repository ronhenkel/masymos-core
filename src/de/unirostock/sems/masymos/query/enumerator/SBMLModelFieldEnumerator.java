package de.unirostock.sems.masymos.query.enumerator;

import de.unirostock.sems.masymos.configuration.Property;

public enum SBMLModelFieldEnumerator {

	// model index:
	NAME, REACTION, SPECIES, COMPARTMENT, CREATOR, AUTHOR, URI,

	//not specified
	NONE;
	
	
	public String getElementNameEquivalent() {
		switch (this) {
		case NAME:
			return Property.General.NAME;
		case REACTION:
			return Property.SBML.REACTION;
		case SPECIES:
			return Property.SBML.SPECIES;
		case COMPARTMENT:
			return Property.SBML.COMPARTMENT;
		case AUTHOR:
			return Property.Publication.AUTHOR;
		case CREATOR:
			return Property.General.CREATOR;
		case URI:
			return Property.General.URI;
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
		case REACTION:
			return 2;
		case SPECIES:
			return 2;
		case COMPARTMENT:
			return 2;
		case AUTHOR:
			return 2;
		case CREATOR:
			return 1;
		case URI:
			return 2;
		case NONE:
			return 1;	
		default:
			return 1;
		}
	}
}
