package de.unirostock.sems.masymos.query.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.configuration.RankAggregationType;
import de.unirostock.sems.masymos.query.results.ModelResultSet;
import de.unirostock.sems.masymos.util.RankerHandler;
import de.unirostock.sems.masymos.util.ResultSetUtil;

public class RankAggregation {
		
	
	public static List<ModelResultSet> aggregate(List<List<ModelResultSet>> rankersList, List<ModelResultSet> aggregateRanker, RankAggregationType.Types type, int rankersWeights){

		if (aggregateRanker.isEmpty()) return aggregateRanker;
		
		RankerHandler aggregateRankerH = new RankerHandler(aggregateRanker);
		List<RankerHandler> rankersListH = new LinkedList<RankerHandler>();
		for(List<ModelResultSet> ranker: rankersList){
			RankerHandler rankerH = new RankerHandler(ranker);
			rankersListH.add(rankerH);
		}
		
		switch(type){ 
			
		case ADJACENT_PAIRS: 
			return optimisedAdj(rankersListH, aggregateRankerH); 
		case COMB_MNZ:
			return combMNZ(rankersListH, aggregateRankerH);
		case LOCAL_KEMENIZATION:
			return localKemenization(rankersListH, aggregateRankerH);
		case SUPERVISED_LOCAL_KEMENIZATION: 
			return supervisedLocalKemenization(rankersListH, aggregateRankerH, rankersWeights);
		case DEFAULT: 
			return aggregateRanker;
		default: 
			return aggregateRanker;
		}
	}
	

	//The Kendall tau ranking distance between aggregateRanker and ranker_i
	private static int distance(RankerHandler aggregateRankerH, RankerHandler ranker_iH){
		
		int k = aggregateRankerH.getLength();
		int sumOfDisagreements = 0;
		int ranking2OfModel1;
		int ranking2OfModel2;
		String modelID1;
		String modelID2;
		ArrayList<String> modelIDList = aggregateRankerH.getModelIDList();
		
		if(ranker_iH.getLength() != 0)
			for(int i = 0; i < k; i++)
				for(int j = i+1; j < k; j++){
					modelID1 = modelIDList.get(i);
					modelID2 = modelIDList.get(j);
					ranking2OfModel1 = ranker_iH.getRankingByModelID(modelID1);
					ranking2OfModel2 = ranker_iH.getRankingByModelID(modelID2);
					
					if (ranking2OfModel1 == -1)
						ranking2OfModel1 = k+1;
					
					if (ranking2OfModel2 == -1)
						ranking2OfModel2 = k+1;
					
					if(ranking2OfModel1 > ranking2OfModel2)
						sumOfDisagreements++;
					}
		
		return sumOfDisagreements;	
	}

	
	//The average distance between the aggregate ranker and all other rankers in rankersList
	private static double distanceAvg(List<RankerHandler> rankersListH, RankerHandler aggregateRankerH){
		int s = rankersListH.size();
		double sumDistance = 0;
		
		for(int i = 0; i < s; i++){
			RankerHandler ranker_iH = rankersListH.get(i);
			sumDistance += distance(aggregateRankerH, ranker_iH);
		}
		
		if(s > 0)
			sumDistance = sumDistance / s;
		return sumDistance;
	}
	
	
	private static List<ModelResultSet> adj (List<RankerHandler>rankersListH, RankerHandler aggregateRankerH){ //adjacent pairs, based on Ke-tau
		double epsilon_min = Integer.MAX_VALUE;
		int count = 0;
		//boolean changed = false;
		ArrayList<String> modelIDList = aggregateRankerH.getModelIDList();
		
		long startTime = System.currentTimeMillis();
		
		while (count < 100){ //repeat for-loop until no further reductions can be performed
			//changed = false;
			for(int i = 0; i < aggregateRankerH.getLength() - 2; i++){
				aggregateRankerH.swap(modelIDList.get(i), modelIDList.get(i+1));
				double epsilon_av = distanceAvg(rankersListH, aggregateRankerH);
				if (epsilon_av < epsilon_min){
					epsilon_min = epsilon_av;
					//changed = true;
				}
				else
					aggregateRankerH.swap(modelIDList.get(i), modelIDList.get(i+1)); //reset
			}
			/*
			if (changed){
				
				count = 0;
			}
			else*/ 
				count++;
		}
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	      
		//set new scores
		aggregateRankerH.setScoresToNAN();
		List<ModelResultSet> results = aggregateRankerH.makeResultsList(); 
		return results;
	}
	
	private static int optimisedDistance(RankerHandler aggregateRankerH, RankerHandler ranker_iH){
		
		int k = ranker_iH.getLength();
		int sumOfDisagreements = 0;
		int ranking2OfModel1;
		int ranking2OfModel2;
		String modelID1;
		String modelID2;
		ArrayList<String> modelIDList = ranker_iH.getModelIDList();
		RankerHandler diff = aggregateRankerH.getDifferenceTo(ranker_iH);
		
		for(int i = 0; i < k; i++){
			modelID1 = modelIDList.get(i);
			
			for(int j = i+1; j < k; j++){
				modelID2 = modelIDList.get(j);
				ranking2OfModel1 = aggregateRankerH.getRankingByModelID(modelID1);
				ranking2OfModel2 = aggregateRankerH.getRankingByModelID(modelID2);
				
				if(ranking2OfModel1 > ranking2OfModel2)
					sumOfDisagreements++;
				}
			sumOfDisagreements += diff.getRankingByModelID(modelID1);
		}
			
		return sumOfDisagreements;	
	}
	
	
	//The average distance between the aggregate ranker and all other rankers in rankersList
	private static double optimisedDistanceAvg(List<RankerHandler> rankersListH, RankerHandler aggregateRankerH, String modelID1, String modelID2, double[] distanceToRankers){
		int s = rankersListH.size();
		double sumDistance = 0;
		
		for(int i = 0; i < s; i++){
			RankerHandler ranker_iH = rankersListH.get(i);
			if (ranker_iH.containsByModelID(modelID1) && ranker_iH.containsByModelID(modelID2)){
				if (ranker_iH.getRankingByModelID(modelID1) > ranker_iH.getRankingByModelID(modelID2))
					distanceToRankers[i]++;
				else 
					distanceToRankers[i]--;
			}
				//distanceToRankers[i] = optimisedDistance(aggregateRankerH, ranker_iH);
			if ((! ranker_iH.containsByModelID(modelID1)) && ranker_iH.containsByModelID(modelID2))
				distanceToRankers[i]++;
			sumDistance += distanceToRankers[i];
		}
		
		if(s > 0)
			sumDistance = sumDistance / s;
		return sumDistance;
	}
	
		
	private static List<ModelResultSet> optimisedAdj (List<RankerHandler>rankersListH, RankerHandler aggregateRankerH){ //adjacent pairs, based on Ke-tau
		double epsilon_min = Integer.MAX_VALUE;
		int count = 0;
		//boolean changed = false;
		ArrayList<String> modelIDList = aggregateRankerH.getModelIDList();
		double[] distanceToRankers = new double[4];
		double[] tempDistanceToRankers = new double[4];
		
		long startTime = System.currentTimeMillis();
		
		while (count < 100){ //repeat for-loop until no further reductions can be performed
			//changed = false;
			for(int i = 0; i < aggregateRankerH.getLength() - 2; i++){
				aggregateRankerH.swap(modelIDList.get(i), modelIDList.get(i+1));
				for(int r = 0; r < 4; r++)
					tempDistanceToRankers[r] = distanceToRankers[r];
				double epsilon_av = optimisedDistanceAvg(rankersListH, aggregateRankerH, modelIDList.get(i), modelIDList.get(i+1), tempDistanceToRankers);
				if (epsilon_av < epsilon_min){
					epsilon_min = epsilon_av;
					for(int r = 0; r < 4; r++)
						distanceToRankers[r] = tempDistanceToRankers[r];
					//changed = true;
				}
				else{
					aggregateRankerH.swap(modelIDList.get(i), modelIDList.get(i+1)); //reset
				}
			}
			/*
			if (changed){
				
				count = 0;
			}
			else*/ 
				count++;
		}
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	    
		//set new scores
		aggregateRankerH.setScoresToNAN();
		List<ModelResultSet> results = aggregateRankerH.makeResultsList(); 
		return results;
	}
	
	private static List<ModelResultSet> combMNZ(List<RankerHandler>rankersListH, RankerHandler aggregateRankerH){
		int s = rankersListH.size();
		
		for (String modelID : aggregateRankerH.getModelIDList()) {
			int h = 0; //denotes the number of times model appears in the rankers
			float brn_sum = 0; //Borda rank normalization for the model

			for (int i = 0; i < s; i++) { //compute h
				RankerHandler ranker_iH = rankersListH.get(i);
				
				int ranking = ranker_iH.getRankingByModelID(modelID);
				if (ranking != -1){ //if 'ranker_i' contains model
					h++;
					brn_sum += 1 - ((double) (ranking - 1) / aggregateRankerH.getLength());
				}
			}

			float newScore = brn_sum * h;
			aggregateRankerH.updateScoreByModelID(modelID, newScore);
		}
		
		List<ModelResultSet> results = aggregateRankerH.makeResultsList();
		ResultSetUtil.sortModelResultSetByScore(results);
		return results;
	}
	
	
	private static List<ModelResultSet> localKemenization(List<RankerHandler>rankersListH, RankerHandler aggregateRankerH){
		int rankersListLength = rankersListH.size();
		int aggregateRankerLength = aggregateRankerH.getLength();
		int rankingOfModel1;
		int rankingOfModel2;
		ArrayList<String> modelIDList = aggregateRankerH.getModelIDList();
		
		long startTime = System.currentTimeMillis();
		
		for(int i = 1; i < aggregateRankerLength; i++){
			String modelID2 = modelIDList.get(i);
			
			for(int j = i-1; j >= 0; j--){
				int pro = 0;
				int con = 0;
				String modelID1 = modelIDList.get(j);
				
				//Compare the rankings of model1 and model2 in each ranker 
				for(int l = 0; l < rankersListLength; l++){
					RankerHandler ranker_i = rankersListH.get(l);
					
					rankingOfModel1 = ranker_i.getRankingByModelID(modelID1);
					if(rankingOfModel1 == -1)
						rankingOfModel1 = Integer.MAX_VALUE; 
					
					rankingOfModel2 = ranker_i.getRankingByModelID(modelID2);
					if(rankingOfModel2 == -1)
						rankingOfModel2 = Integer.MAX_VALUE; 
					
					//update pro if the ranking is the same as in the initial aggregate ranker
					//update cons otherwise
					if(rankingOfModel2 > rankingOfModel1)
						pro++;
					else if (rankingOfModel2 < rankingOfModel1)
						con++;
				}
				
				//swap model1 and model2 if the majority of the rankers prefer model2 to model1 (if ranking of model2 < ranking of model1)
				if(con > pro){
					aggregateRankerH.swap(modelID2, modelID1);
				}
				else if (pro >= con)
					break;
			}
		}
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
		
		//set new scores
		aggregateRankerH.setScoresToNAN();
		List<ModelResultSet> results = aggregateRankerH.makeResultsList(); 
		return results;
	}
	
	
	private static List<ModelResultSet> supervisedLocalKemenization (List<RankerHandler>rankersListH, RankerHandler aggregateRankerH, int rankersWeights){
		
		HashMap<Integer, Integer> weights = new HashMap<Integer, Integer>();
		
		for(int i = 0; i < 4; i++){
			weights.put(i, rankersWeights % 100);
			rankersWeights = rankersWeights / 100;
		}
		
		int numberOfRankers = rankersListH.size();
		int aggregateRankerLength = aggregateRankerH.getLength();
		boolean[][] M = new boolean[aggregateRankerLength][aggregateRankerLength];
	
		int rankingOfModel1;
		int rankingOfModel2;
		ArrayList<String> modelIDList = aggregateRankerH.getModelIDList();
		
		int weightsSum = 0;
		for(int o: weights.values())
			weightsSum += o;
	
		
		for(int i = 0; i < aggregateRankerLength; i++){
			for(int j = i + 1 ; j < aggregateRankerLength; j++){
				String modelID1 = modelIDList.get(i);
				String modelID2 = modelIDList.get(j);
				int score = 0;
				
				for(int l = 0; l < numberOfRankers; l++){
					RankerHandler ranker_iH = rankersListH.get(l);
					
					rankingOfModel1 = ranker_iH.getRankingByModelID(modelID1);
					if(rankingOfModel1 == -1)
						rankingOfModel1 = Integer.MAX_VALUE; 
					
					rankingOfModel2 = ranker_iH.getRankingByModelID(modelID2);
					if(rankingOfModel2 == -1)
						rankingOfModel2 = Integer.MAX_VALUE; 
					
					if(rankingOfModel1 < rankingOfModel2)
						score += weights.get(l);
				}
				
				if (score >= 0.5 * weightsSum){
					M[i][j] = true;
					M[j][i] = false;
				}
			}
		}
		
		for(int i = 1; i < aggregateRankerLength; i++){
			String modelID2 = modelIDList.get(i);
			
			for(int j = i-1; j >= 0; j--){
				String modelID1 = modelIDList.get(j);
				
				if(M[j][i] == false)
					aggregateRankerH.swap(modelID1, modelID2);
			}
		}
		
		//set new scores
		aggregateRankerH.setScoresToNAN();
		List<ModelResultSet> results = aggregateRankerH.makeResultsList(); 
		return results;
	}
	

}
