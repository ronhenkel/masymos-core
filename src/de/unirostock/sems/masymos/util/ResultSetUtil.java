package de.unirostock.sems.masymos.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.unirostock.sems.masymos.query.results.ModelResultSet;
import de.unirostock.sems.masymos.query.results.SedmlResultSet;


public class ResultSetUtil {
	/**
	 * This comparator orders the models by score descending
	 * @author Ron Henkel
	 *
	 */
	private static class ModelResultSetScoreComperator implements Comparator<ModelResultSet>{

		@Override
		public int compare(ModelResultSet rs1, ModelResultSet rs2) {
			if ((rs1==null) || (rs2==null)) return 0; 
			
			if (rs1.getScore() < rs2.getScore())  return 1;
			if (rs1.getScore() > rs2.getScore()) return -1;
			
			return 0;
		}
		
	}
	
	
	private static class SedmlResultSetScoreComperator implements Comparator<SedmlResultSet>{
	
		@Override
		public int compare(SedmlResultSet rs1, SedmlResultSet rs2) {
			if ((rs1==null) || (rs2==null)) return 0; 
			
			if (rs1.getScore() > rs2.getScore())  return 1;
			if (rs1.getScore() == rs2.getScore()) return -1;
			
			return 0;
		}
		
	}

	public static List<ModelResultSet> sortModelResultSetByScore(List<ModelResultSet> rsList){

		Collections.sort(rsList, new ModelResultSetScoreComperator());
		return rsList;
	}
	
	public static List<SedmlResultSet> sortSedmlResultSetByScore(List<SedmlResultSet> rsList){
	
		Collections.sort(rsList, new SedmlResultSetScoreComperator());
		return rsList;
	}


	
//	public static List<IResultSetInterface> collateByDatabaseId(List<SedmlResultSet> rsList){
//		HashMap<Long, SedmlResultSet> toBeKept = new HashMap<Long, SedmlResultSet>();
//		for (Iterator<SedmlResultSet> rsListIt = rsList.iterator(); rsListIt.hasNext();) {
//			SedmlResultSet resultSet = (SedmlResultSet) rsListIt.next();
//			if (toBeKept.keySet().contains(resultSet.getDatabaseId())){
//				SedmlResultSet toBeKeptResultSet = toBeKept.get(resultSet.getDatabaseId());
//				if (toBeKeptResultSet.getScore() < resultSet.getScore()) toBeKept.put(resultSet.getDatabaseId(), resultSet); 
//			} else {
//				toBeKept.put(resultSet.getDatabaseId(), resultSet);
//			}
//		}
//		return sortByDatabaseId(new LinkedList<IResultSetInterface>(toBeKept.values()));
//	}

	public static List<ModelResultSet> collateModelResultSetByModelId(List<ModelResultSet> rsList){
		Map<String, ModelResultSet> toBeKept = new HashMap<String, ModelResultSet>();
		Map<String, Integer> freq = new HashMap<String, Integer>();		
		int count; 
		for (Iterator<ModelResultSet> rsListIt = rsList.iterator(); rsListIt.hasNext();) {
			ModelResultSet resultSet = (ModelResultSet) rsListIt.next();
			if (toBeKept.keySet().contains(resultSet.getModelId())){
				//count occurrences of a model
				count = freq.containsKey(resultSet.getModelId()) ? freq.get(resultSet.getModelId()) : 0;
				freq.put(resultSet.getModelId(), count + 1);
				
				//sum up scores
				ModelResultSet toBeKeptResultSet = toBeKept.get(resultSet.getModelId());
				float score = toBeKeptResultSet.getScore() + resultSet.getScore();
				toBeKeptResultSet.setScore(score);
				
				//check if from same index
				if (StringUtils.isEmpty(toBeKeptResultSet.getIndexSource()) 
					|| !toBeKeptResultSet.getIndexSource().equals(resultSet.getIndexSource())){
					toBeKeptResultSet.setIndexSource("n/a");
				}				
				//store model to list
				toBeKept.put(resultSet.getModelId(), toBeKeptResultSet); 
			} else {
				toBeKept.put(resultSet.getModelId(), resultSet);
			}
		}
		
		int maxCount = 1;
		for (Iterator<Integer> countIt = freq.values().iterator(); countIt.hasNext();) {
			int c = countIt.next();
			if (c > maxCount) maxCount = c;
		}
		for (Iterator<ModelResultSet> iterator = toBeKept.values().iterator(); iterator.hasNext();) {
			ModelResultSet modelResultSet = (ModelResultSet) iterator.next();
			modelResultSet.setScore(modelResultSet.getScore()/maxCount);			
		}
		return sortModelResultSetByScore(new LinkedList<ModelResultSet>(toBeKept.values()));
	}


}
