package de.unirostock.sems.masymos.query.enumerator;


/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/

public enum IndexEnumerator {

	MODELINDEX,
	ANNOTATIONINDEX,
	PUBLICATIONINDEX,
	PERSONINDEX,
	CONSTITUENTINDEX,
	SEDMLINDEX;
	
	public String getElementNameEquivalent() {
		switch (this) {
		case MODELINDEX:
			return "MODELINDEX";
		case ANNOTATIONINDEX:
			return "ANNOTATIONINDEX";
		case PUBLICATIONINDEX:
			return "PUBLICATIONINDEX";
		case PERSONINDEX:
			return "PERSONINDEX";	
		case CONSTITUENTINDEX:
			return "CONSTITUENTINDEX";	
		case SEDMLINDEX:
			return "SEDMLINDEX";	
		default:
			return "unknown";
		}
	} 
}
