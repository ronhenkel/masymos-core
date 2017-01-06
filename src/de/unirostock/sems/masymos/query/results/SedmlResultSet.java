package de.unirostock.sems.masymos.query.results;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.unirostock.sems.masymos.query.IDocumentResultSetInterface;

/**
*
* Copyright 2016 Rebekka Alm, Ron Henkel (GPL v3)
* @author Rebekka Alm, ronhenkel
*/


public class SedmlResultSet implements IDocumentResultSetInterface{
	
	private float score;
	private String explanation;
	private String versionID;
	private List<String> modelreferences;
	private String filepath;
    private String filename;
	
    public SedmlResultSet(float score, String versionID, String explanation, List<String> modelreferences, String filepath, String filename){

		this.score = score;
		this.versionID = versionID;
		this.explanation = explanation;
		this.modelreferences = modelreferences;
        this.filepath = filepath;
        this.filename = filename;
}
    
    
//	public ResultSetSedml(float score, Long databaseID, String explanation, List<String> modelreferences){
//		
//		this.score = score;
//		this.databaseID = databaseID;
//		this.explanation = explanation;
//		this.modelreferences = modelreferences;
//
//	}

	public float getScore() {
		return score;
	}

	public String getSearchExplanation() {
		return explanation;
	}

	public List<String> getModelreferences() {
		return modelreferences;
	}
	
	 public String getFilepath() {
         return filepath;
 }

 public String getVersionId() {
		return versionID;
	}


public void setFilepath(String filepath) {
         this.filepath = filepath;
 }
 
 public String getFilename() {
         return filename;
 }

 public void setFilename(String filename) {
         this.filename = filename;
 }
	
	@Override
	public boolean equals(Object resultSet) {
		SedmlResultSet rs; 
		if ((resultSet==null) || !(resultSet instanceof SedmlResultSet)) return false;
		else rs = (SedmlResultSet) resultSet; 

		if (!StringUtils.equals(this.versionID, rs.getVersionId())) return false;
		
		if (!StringUtils.equals(this.filepath, rs.getFilepath())) return false;
        
        if (!StringUtils.equals(this.filename, rs.getFilename())) return false;
		
		if (!StringUtils.equals(this.explanation, rs.getSearchExplanation())) return false;
		
		if (this.score != rs.score) return false;
		
		return true;
	}


}
