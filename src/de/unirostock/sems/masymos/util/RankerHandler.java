package de.unirostock.sems.masymos.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.query.results.VersionResultSet;

/**
 * A class for the basic operations on rankers. 
 * 
 * Copyright 2016 Mariam Nassar (GPL v3)
 * @author Mariam Nassar
 *
 */
public class RankerHandler {
	/**
	 * Maps the unique version Ids to the version objects.
	 */
	private LinkedHashMap<String, VersionResultSet> rankerMap = new LinkedHashMap<String, VersionResultSet>();
	/**
	 * Maps the unique version Ids onto the ranking of the version.
	 */
	private LinkedHashMap<String, Integer> uniqueVerionIdRanking = new LinkedHashMap<String, Integer>();
	/**
	 * A list with of the sorted unique version Ids.
	 */
	private ArrayList<String> uniqueVersionIDList = new ArrayList<String>();
	
	/**
	 * Constructor.
	 * @param rankerList A list of versions.
	 */
	public RankerHandler(List<VersionResultSet> rankerList){
		int count = 1;
		
		if(rankerList != null)
			for(VersionResultSet version: rankerList){
				VersionResultSet newVersion = version.copyVersionResultSet();
				rankerMap.put(version.getUniqueVersionId(), newVersion);
				uniqueVerionIdRanking.put(version.getUniqueVersionId(), count);
				uniqueVersionIDList.add(version.getUniqueVersionId());
				count++;
			}
	}
	
	/**
	 * 
	 * @return A sorted list of versions.
	 */
	public ArrayList<String> getUniqueVersionIDList(){
		return this.uniqueVersionIDList;
	}
	
	/**
	 * 
	 * @return The number of versions in the ranker.
	 */
	public int getRankerSize(){
		return this.uniqueVersionIDList.size();
	}
	
	/**
	 * Search for the ranking of a version by uniqueVersionId.
	 * 
	 * @param uniqueVersionID
	 * @return The ranking of the version with the given uniqueVersionID if the ranker contains the version
	 * and -1 otherwise.
	 */
	public int getRankingByUniqueVersionID(String uniqueVersionID){
		
		if(uniqueVerionIdRanking.containsKey(uniqueVersionID))
			return uniqueVerionIdRanking.get(uniqueVersionID);
		else return -1;
	}
	
	/**
	 * Tests if the ranker contains the version by uniqueVersionId.
	 * 
	 * @param uniqueVersionId
	 * @return true, if the ranker contains the version, and false else.
	 */
	public boolean containsByUniqueVersionID(String uniqueVersionId){ 
		if(uniqueVerionIdRanking.containsKey(uniqueVersionId))
			return true;
		else
			return false;
	}
	
	/**
	 * Searches for the score of a version by uniqueVersionId.
	 * 
	 * @param modelID
	 * @return the score of the version with the given uniqueVersionId if the ranker contains the version. 
	 * And -1 otherwise.
	 */
	public float getScoreByUniqueVersionID(String uniqueVersionId){
		
		if(this.rankerMap.containsKey(uniqueVersionId))
			return this.rankerMap.get(uniqueVersionId).getScore();
		else 
			return -1;
	}
	
	/**
	 * Updates the score of a version by uniqueVersionId.
	 * 
	 * @param uniqueVersionId
	 * @param newScore
	 */
	public void updateScoreByModelID(String uniqueVersionId, float newScore){
		if(this.rankerMap.containsKey(uniqueVersionId)){
			VersionResultSet version = this.rankerMap.get(uniqueVersionId);
			version.setScore(newScore);
			this.rankerMap.put(uniqueVersionId, version);
		}
	}
	
	/**
	 * Swaps two versions in the ranker by uniqueVersionIds.
	 * 
	 * @param uniqueVersionId1
	 * @param uniqueVersionId2
	 */
	public void swap(String uniqueVersionId1, String uniqueVersionId2){ 
		int rankingOfModel1 = this.uniqueVerionIdRanking.get(uniqueVersionId1);
		int rankingOfModel2 = this.uniqueVerionIdRanking.get(uniqueVersionId2);
	
		this.uniqueVerionIdRanking.put(uniqueVersionId1, rankingOfModel2);
		this.uniqueVerionIdRanking.put(uniqueVersionId2, rankingOfModel1);
		this.uniqueVersionIDList.set(rankingOfModel1 - 1, uniqueVersionId2);
		this.uniqueVersionIDList.set(rankingOfModel2 - 1, uniqueVersionId1);
	}
	
	
	/*public RankerHandler getDifferenceTo(RankerHandler r2){  //returns all the elements in 'this' but not in 'r2' {{this} - {r2}}
		RankerHandler diff = new RankerHandler(null);
		int count = 1;
		
		for(String modelId: this.modelIDList)
			if (!r2.containsByModelID(modelId)){  //if r2 doesn't contain 'model'
				ModelResultSet model = this.rankerMap.get(modelId);
				ModelResultSet newModel = model.copyModelResultSet();
				diff.rankerMap.put(model.getModelID(), newModel);
				diff.modelIDRankingMap.put(model.getModelID(), count);
				diff.modelIDList.add(model.getModelID());
				count++;
		}
		
		return diff;
	}*/
	
	/**
	 * 
	 * @return A list of sorted versions.
	 */
	public List<VersionResultSet> makeResultsList(){
		List<VersionResultSet> verionsList = new LinkedList<VersionResultSet>();
		for(String uniqueVersionId: uniqueVersionIDList){
			VersionResultSet version = this.rankerMap.get(uniqueVersionId);
			verionsList.add(version);
		}
		return verionsList;
	}
	
	/**
	 * Sets all scores to -1 when score are not relevant. For not score-based aggregation methods.
	 */
	public void setScoresToNAN(){
		for(VersionResultSet version: this.rankerMap.values())
			version.setScore(-1);
		
	}

}
