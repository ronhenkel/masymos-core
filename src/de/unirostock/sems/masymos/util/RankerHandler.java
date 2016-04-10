package de.unirostock.sems.masymos.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.query.results.ModelResultSet;

public class RankerHandler {
	private LinkedHashMap<String, ModelResultSet> rankerMap;
	private LinkedHashMap<String, Integer> modelIDRankingMap;
	private ArrayList<String> modelIDList;
	
	public ArrayList<String> getModelIDList(){
		return this.modelIDList;
	}
	
	
	public RankerHandler(List<ModelResultSet> rankerList){
		LinkedHashMap<String, ModelResultSet> rankerMap = new LinkedHashMap<String, ModelResultSet>();
		LinkedHashMap<String, Integer> modelIDRankingMap = new LinkedHashMap<String, Integer>();
		ArrayList<String> modelIDList = new ArrayList<String>();
		
		int count = 1;
		
		if(rankerList != null)
			for(ModelResultSet model: rankerList){
				ModelResultSet newModel = model.copyModelResultSet();
				rankerMap.put(model.getModelID(), newModel);
				modelIDRankingMap.put(model.getModelID(), count);
				modelIDList.add(model.getModelID());
				count++;
			}
		
		this.rankerMap = rankerMap;
		this.modelIDRankingMap = modelIDRankingMap;
		this.modelIDList = modelIDList;
	}
	
	
	public int getLength(){
		return this.rankerMap.keySet().size();
	}
	

	//returns the ranking of 'model' in 'ranker' if 'ranker' contains 'modelID' and -1 otherwise
	public int getRankingByModelID(String modelID){
		
		if(modelIDRankingMap.get(modelID) != null)
			return modelIDRankingMap.get(modelID);
		else return -1;
	}
	
	
	public float getScoreByModelID(String modelID){
		
		if(this.rankerMap.get(modelID) != null)
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
	
	
	public List<ModelResultSet> makeResultsList(){
		List<ModelResultSet> modelsList = new LinkedList<ModelResultSet>();
		for(String modelID: modelIDList){
			ModelResultSet model = this.rankerMap.get(modelID)/*.copyModelResultSet()*/;
			modelsList.add(model);
		}
		return modelsList;
	}
	
	
	public RankerHandler copyRankerHandler(){
		RankerHandler newRankerHandler = new RankerHandler(this.makeResultsList());
		return newRankerHandler;
	}
	
	
	public void setScoresToNAN(){
		for(ModelResultSet model: this.rankerMap.values())
			model.setScore(-1);
		
	}

}
