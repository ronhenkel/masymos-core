package de.unirostock.sems.masymos.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.query.results.ModelResultSet;

public class RankerHandler {
	private LinkedHashMap<String, ModelResultSet> rankerMap = new LinkedHashMap<String, ModelResultSet>();
	private LinkedHashMap<String, Integer> modelIDRankingMap = new LinkedHashMap<String, Integer>();
	private ArrayList<String> modelIDList = new ArrayList<String>();
	
	public ArrayList<String> getModelIDList(){
		return this.modelIDList;
	}
	
	
	public RankerHandler(List<ModelResultSet> rankerList){
		int count = 1;
		
		if(rankerList != null)
			for(ModelResultSet model: rankerList){
				ModelResultSet newModel = model.copyModelResultSet();
				rankerMap.put(model.getModelID(), newModel);
				modelIDRankingMap.put(model.getModelID(), count);
				modelIDList.add(model.getModelID());
				count++;
			}
	}
	
	
	public int getLength(){
		return this.rankerMap.keySet().size();
	}
	

	//returns the ranking of 'model' in 'ranker' if 'ranker' contains 'modelID' and -1 otherwise
	public int getRankingByModelID(String modelID){
		
		if(modelIDRankingMap.containsKey(modelID))
			return modelIDRankingMap.get(modelID);
		else return -1;
	}
	
	
	public boolean containsByModelID(String modelID){ 
		if(modelIDRankingMap.containsKey(modelID))
			return true;
		else
			return false;
	}
	
	
	public float getScoreByModelID(String modelID){
		
		if(this.rankerMap.containsKey(modelID))
			return this.rankerMap.get(modelID).getScore();
		else 
			return -1;
	}
	
	
	public void updateScoreByModelID(String modelID, float newScore){
		ModelResultSet model = this.rankerMap.get(modelID);
		model.setScore(newScore);
		this.rankerMap.put(modelID, model);
	}
	
	
	public void swap(String modelID1, String modelID2){ 
		int rankingOfModel1 = this.modelIDRankingMap.get(modelID1);
		int rankingOfModel2 = this.modelIDRankingMap.get(modelID2);
	
		this.modelIDRankingMap.put(modelID1, rankingOfModel2);
		this.modelIDRankingMap.put(modelID2, rankingOfModel1);
		this.modelIDList.set(rankingOfModel1 - 1, modelID2);
		this.modelIDList.set(rankingOfModel2 - 1, modelID1);
	}
	
	
	public RankerHandler getDifferenceTo(RankerHandler r2){  //returns all the elements in 'this' but not in 'r2' {{this} - {r2}}
		RankerHandler diff = new RankerHandler(null);
		int count = 1;
		
		for(String modelId: this.modelIDList)
			if (r2.containsByModelID(modelId)){  //if r2 doesn't contain 'model'
				ModelResultSet model = this.rankerMap.get(modelId);
				ModelResultSet newModel = model.copyModelResultSet();
				diff.rankerMap.put(model.getModelID(), newModel);
				diff.modelIDRankingMap.put(model.getModelID(), count);
				diff.modelIDList.add(model.getModelID());
				count++;
		}
		
		return diff;
	}
	
	
	public List<ModelResultSet> makeResultsList(){
		List<ModelResultSet> modelsList = new LinkedList<ModelResultSet>();
		for(String modelID: modelIDList){
			ModelResultSet model = this.rankerMap.get(modelID);
			modelsList.add(model);
		}
		return modelsList;
	}
	
	
	public void setScoresToNAN(){
		for(ModelResultSet model: this.rankerMap.values())
			model.setScore(-1);
		
	}

}
