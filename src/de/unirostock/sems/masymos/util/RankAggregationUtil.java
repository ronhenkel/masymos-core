package de.unirostock.sems.masymos.util;

import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.query.results.ModelResultSet;

public class RankAggregationUtil {
	
	public static List<List<ModelResultSet>> splitModelResultSetByIndex(List<ModelResultSet> toBeSplit){
		
		List<List<ModelResultSet>> rankersList = new LinkedList<List<ModelResultSet>> ();
		
		List<ModelResultSet> modelRanker = new LinkedList<ModelResultSet>();
		List<ModelResultSet> annotationRanker = new LinkedList<ModelResultSet>();
		List<ModelResultSet> personRanker = new LinkedList<ModelResultSet>();
		List<ModelResultSet> publicationRanker = new LinkedList<ModelResultSet>();
		
		
		for(ModelResultSet e : toBeSplit){
			if (e.getIndexSource().equals("ModelIndex"))
				modelRanker.add(e);
			if (e.getIndexSource().equals("AnnotationIndex"))
				annotationRanker.add(e);
			if (e.getIndexSource().equals("PersonIndex"))
				personRanker.add(e);
			if (e.getIndexSource().equals("PublicationIndex"))
				publicationRanker.add(e);
		}
		
		modelRanker = ResultSetUtil.collateModelResultSetByModelId(modelRanker);
		annotationRanker = ResultSetUtil.collateModelResultSetByModelId(annotationRanker);
		personRanker = ResultSetUtil.collateModelResultSetByModelId(personRanker);
		publicationRanker = ResultSetUtil.collateModelResultSetByModelId(publicationRanker);		
		
		rankersList.add(modelRanker);
		rankersList.add(annotationRanker);
		rankersList.add(personRanker);
		rankersList.add(publicationRanker);
		
		
		
		
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
