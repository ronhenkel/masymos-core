package de.unirostock.sems.masymos.query.aggregation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.query.results.ModelResultSet;
import de.unirostock.sems.masymos.query.results.VersionResultSet;

public class GroupVersions {
	public static List<ModelResultSet> groupVersions(List<VersionResultSet> versions){
		List<ModelResultSet> groupedResults = new LinkedList<ModelResultSet>();
		HashMap<String, ModelResultSet> fileIdModelsMap = new HashMap<String, ModelResultSet>();
		List<String> rankedFileIdList = new LinkedList<String>();
		for(VersionResultSet version: versions){
			ModelResultSet newModel;
			if(fileIdModelsMap.containsKey(version.getFileId())){ //if model already exists
				newModel = fileIdModelsMap.get(version.getFileId());
				newModel.addVersion(version); //add the version to the versions list
				fileIdModelsMap.put(version.getFileId(), newModel);
			}
			else{ //if the model doesn't already exist
				newModel = version.makeModel();  //make a new model out of the version
				fileIdModelsMap.put(version.getFileId(), newModel);
				rankedFileIdList.add(version.getFileId());
			}
		}
		for(String fileId: rankedFileIdList){  //make a ranked list of models 
			groupedResults.add(fileIdModelsMap.get(fileId));
		}
		
		return groupedResults;
	}

}
