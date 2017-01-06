package de.unirostock.sems.masymos.query.enumerator;

import de.unirostock.sems.masymos.configuration.Property;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/

public enum SedmlFieldEnumerator {

	// SED-ML index:
	NAME, MODELSOURCE, OUTPUTTYPE, DATALABEL, SIMKISAO, SIMTYPE, MATH, URI,

	//not specified
	NONE;
	
	
	public String getElementNameEquivalent() {
		switch (this) {
		case NAME:
			return Property.SEDML.NAME;
		case MODELSOURCE:
			return Property.SEDML.MODELSOURCE;
		case OUTPUTTYPE:
			return Property.SEDML.OUTPUT_TYPE;
		case DATALABEL:
			return Property.SEDML.DATALABEL;
		case SIMKISAO:
			return Property.SEDML.SIM_KISAO;
		case SIMTYPE:
			return Property.SEDML.SIM_TYPE;
		case MATH:
			return Property.SEDML.MATH;
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
			return 1;
		case MODELSOURCE:
			return 1;
		case OUTPUTTYPE:
			return 1;
		case DATALABEL:
			return 1;
		case SIMKISAO:
			return 1;
		case SIMTYPE:
			return 1;
		case MATH:
			return 1;
		case URI:
			return 1;
		case NONE:
			return 1;	
		default:
			return 1;
		}
	}
}
