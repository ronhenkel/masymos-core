package de.unirostock.sems.masymos.util;

import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.query.results.VersionResultSet;

/**
*
* Copyright 2016 Mariam Nassar, Ron Henkel (GPL v3)
* @author ronhenkel, Mariam Nassar
*/
public class RankAggregationUtil {
	
	public static List<List<VersionResultSet>> splitModelResultSetByIndex(List<VersionResultSet> toBeSplit){
		
		List<List<VersionResultSet>> rankersList = new LinkedList<List<VersionResultSet>> ();
		
		List<VersionResultSet> modelRanker = new LinkedList<VersionResultSet>();
		List<VersionResultSet> annotationRanker = new LinkedList<VersionResultSet>();
		List<VersionResultSet> personRanker = new LinkedList<VersionResultSet>();
		List<VersionResultSet> publicationRanker = new LinkedList<VersionResultSet>();
		
		
		for(VersionResultSet version : toBeSplit){
			VersionResultSet newVersion = version.copyVersionResultSet();
			if (version.getIndexSource().equals("ModelIndex"))
				modelRanker.add(newVersion);
			if (version.getIndexSource().equals("AnnotationIndex"))
				annotationRanker.add(newVersion);
			if (version.getIndexSource().equals("PersonIndex"))
				personRanker.add(newVersion);
			if (version.getIndexSource().equals("PublicationIndex"))
				publicationRanker.add(newVersion);
		}
		
		modelRanker = ResultSetUtil.collateModelResultSetByModelId(modelRanker);
		annotationRanker = ResultSetUtil.collateModelResultSetByModelId(annotationRanker);
		personRanker = ResultSetUtil.collateModelResultSetByModelId(personRanker);
		publicationRanker = ResultSetUtil.collateModelResultSetByModelId(publicationRanker);		
		
		rankersList.add(0, modelRanker);
		rankersList.add(1, annotationRanker);
		rankersList.add(2, personRanker);
		rankersList.add(3, publicationRanker);
		
		return rankersList;
		
	}
	
	/*
	 * results = new LinkedList<ModelResultSet>(); 
		HashMap<Integer, Integer> weights = new HashMap<Integer, Integer>();
		weights.put(0, 4);
		weights.put(1, 3);
		weights.put(2, 1);
		weights.put(3, 1);
	
		final long timeStart = System.currentTimeMillis(); 
		
		if(!(rankersList.isEmpty())){
			//results = adj(rankersList, initialAggregateRanker); //rank aggregation, adjacent pairs (using Kendal-Tau distance)
			//results = combMNZ(rankersList, initialAggregateRanker); //rank aggregation
			//results = localKemenization(rankersList, initialAggregateRanker); //rank aggregation
			results = supervisedLocalKemenization(rankersList, initialAggregateRanker, weights); //rank aggregation
		}
		
		final long timeEnd = System.currentTimeMillis();
		final long time = timeEnd - timeStart;
		
		printModelResults(results);
	 */

}
