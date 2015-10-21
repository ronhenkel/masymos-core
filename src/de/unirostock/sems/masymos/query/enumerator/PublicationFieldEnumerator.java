package de.unirostock.sems.masymos.query.enumerator;

import de.unirostock.sems.masymos.configuration.Property;

public enum PublicationFieldEnumerator {

	// publication index
	ABSTRACT, AFFILIATION, AUTHOR,
	JOURNAL, TITLE, YEAR, PUBID,

	//not specified
	NONE;
	
	
	public String getElementNameEquivalent() {
		switch (this) {
		case AUTHOR:
			return Property.Publication.AUTHOR;
		case ABSTRACT:
			return Property.Publication.ABSTRACT;
		case AFFILIATION:
			return Property.Publication.AFFILIATION;
		case JOURNAL:
			return Property.Publication.JOURNAL;
		case TITLE:
			return Property.Publication.TITLE;
		case YEAR:
			return Property.Publication.YEAR;	
		case PUBID:
			return Property.Publication.ID;	
		case NONE:
			return "NONE";	
		default:
			return "NONE";
		}
	}
	
	public int getElementWeightEquivalent() {
		switch (this) {
		case AUTHOR:
			return 3;
		case ABSTRACT:
			return 2;
		case AFFILIATION:
			return 1;
		case JOURNAL:
			return 1;
		case TITLE:
			return 3;
		case YEAR:
			return 1;
		case PUBID:
			return 5;	
		case NONE:
			return 1;	
		default:
			return 1;
		}
	}

}
