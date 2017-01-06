package de.unirostock.sems.masymos.configuration;


/**
*
* Copyright 2016 Ron Henkel, Mariam Nassar (GPL v3)
* @author ronhenkel, Mariam Nassar
*/
public class RankAggregationType {

	public static enum Types
	{   
		
		/*
		 * Properties to determine the rank aggregation
		 */	
		DEFAULT,		
		ADJACENT_PAIRS,
		COMB_MNZ, 	
		LOCAL_KEMENIZATION, 		
		SUPERVISED_LOCAL_KEMENIZATION
	}
	
	public static Types stringToRankAggregationType (String type){
		switch(type){ 
		
		case "ADJACENT_PAIRS": 
			return Types.ADJACENT_PAIRS; 
		case "COMB_MNZ":
			return Types.COMB_MNZ;
		case "LOCAL_KEMENIZATION":
			return Types.LOCAL_KEMENIZATION;
		case "SUPERVISED_LOCAL_KEMENIZATION": 
			return Types.SUPERVISED_LOCAL_KEMENIZATION;
		case "DEFAULT": 
			return Types.DEFAULT;
		default: 
			return Types.DEFAULT;
		}
			
	}
	
	public static String rankAggregationTypeToString (Types type){
		
		switch(type){ 		
		case ADJACENT_PAIRS: 
			return "ADJACENT_PAIRS"; 
		case COMB_MNZ:
			return "COMB_MNZ";
		case LOCAL_KEMENIZATION:
			return "LOCAL_KEMENIZATION";
		case SUPERVISED_LOCAL_KEMENIZATION: 
			return "SUPERVISED_LOCAL_KEMENIZATION";
		case DEFAULT: 
			return "DEFAULT";
		default: 
			return "DEFAULT";
		}
			
	}
}
