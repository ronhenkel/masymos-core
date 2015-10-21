package de.unirostock.sems.masymos.query.enumerator;

import de.unirostock.sems.masymos.configuration.Property;

public enum PersonFieldEnumerator {


	// person index
	EMAIL, FAMILYNAME, GIVENNAME, ORGANIZATION,
	
	//not specified
	NONE;
	
	
	public String getElementNameEquivalent() {
		switch (this) {
		case EMAIL:
			return Property.Person.EMAIL;
		case FAMILYNAME:
			return Property.Person.FAMILYNAME;
		case GIVENNAME:
			return Property.Person.GIVENNAME;
		case ORGANIZATION:
			return Property.Person.ORGANIZATION;	
		case NONE:
			return "NONE";	
		default:
			return "NONE";
		}
	}

	
	public int getElementWeightEquivalent() {
		switch (this) {
		case EMAIL:
			return 1;
		case FAMILYNAME:
			return 1;
		case GIVENNAME:
			return 1;
		case NONE:
			return 1;	
		case ORGANIZATION:
			return 1;
		default:
			return 1;
		}
	}

	
}
