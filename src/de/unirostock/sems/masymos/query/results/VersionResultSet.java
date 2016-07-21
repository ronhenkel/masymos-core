package de.unirostock.sems.masymos.query.results;


import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import de.unirostock.sems.masymos.query.IDocumentResultSetInterface;

public class VersionResultSet implements IDocumentResultSetInterface {
	private String modelName;
	private float score;
	private String modelID;
	private String explanation;
	private String versionID;
	private String documentURI;
	private String xmldoc;
	private String filename;
	private String fileId;
	private String indexSource;
	
	public String getUniqueVersionId(){
		StringBuilder uniqueIdBuilder = new StringBuilder();
		uniqueIdBuilder.append(this.getFileId());
		uniqueIdBuilder.append(this.getVersionId());
		String uniqueId = uniqueIdBuilder.toString();
		return uniqueId;
	}

	public String getIndexSource() {
		return indexSource;
	}


	public void setIndexSource(String indexSource) {
		this.indexSource = indexSource;
	}


	public String getFileId() {
		return fileId;
	}


	public void setFileId(String fileId) {
		this.fileId = fileId;
	}


	public String getModelID() {
		return modelID;
	}


	public String getVersionID() {
		return versionID;
	}
	
	public VersionResultSet copyVersionResultSet(){
		VersionResultSet newSet = new VersionResultSet();
		newSet.modelName = this.getModelName();
		newSet.score = this.getScore();
		newSet.modelID = this.getModelID();
		newSet.versionID = this.getVersionId();
		newSet.documentURI = this.getDocumentURI();
		newSet.xmldoc = this.getXmldoc();
		newSet.filename = this.getFilename();
		newSet.fileId = this.getFileId();
		newSet.indexSource = this.getIndexSource();
		
		return newSet;
	}
	
	protected VersionResultSet(){
		
	}


	public VersionResultSet(float score, String modelId, String modelName, String versionID, String documentURI, String filename, String explanation, String indexSource){
		this.modelName = modelName;
		this.score = score;
		this.modelID = modelId;
		this.explanation = explanation;
		this.versionID = versionID;
		this.documentURI = documentURI;
		this.filename = filename;
		this.indexSource = indexSource;
	}
	
	
	public VersionResultSet(float score, String modelId, String modelName, String indexSource){
		this.modelName = modelName;
		this.score = score;
		this.modelID = modelId;
		this.indexSource = indexSource;
	}
	
	public VersionResultSet(float score, String modelId, String modelName, String documentURI, String filename, String indexSource){
		this.modelName = modelName;
		this.score = score;
		this.modelID = modelId;
		this.documentURI = documentURI;
		this.filename = filename;
		this.indexSource = indexSource;
	}	
	

	public ModelResultSet makeModel(){
		LinkedList<VersionResultSet> versions = new LinkedList<VersionResultSet>();
		versions.add(this);
		ModelResultSet model = new ModelResultSet(this.score, this.modelID, this.modelName, versions, this.documentURI, this.filename, this.explanation, this.indexSource);
		return model;
	} 
	
	public String getModelName() {
		return modelName;
	}
	
	public String getModelId() {
		return modelID;
	}

	@Override
	public float getScore() {
		return score;
	}

	@Override
	public String getSearchExplanation() {
		return explanation;
	}

	public String getVersionId() {
		return versionID;
	}
	
	public void setVersionID(String versionID) {
		this.versionID = versionID;
	}
	
	
	public String getDocumentURI() {
		return documentURI;
	}

	public void setDocumentURI(String documentURI) {
		this.documentURI = documentURI;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public void setXmldoc(String xmldoc) {
		this.xmldoc = xmldoc; 		
	}
	
	public String getXmldoc() {
		return xmldoc; 		
	}


	@Override
	public boolean equals(Object resultSet) {
		VersionResultSet rs; 
		if ((resultSet==null) || !(resultSet instanceof VersionResultSet)) return false;
		else rs = (VersionResultSet) resultSet; 
		
		if (!StringUtils.equals(this.modelID, rs.getModelId())) return false;
		
		if (!StringUtils.equals(this.documentURI, rs.getDocumentURI())) return false;
		
		if (!StringUtils.equals(this.filename, rs.getFilename())) return false;
		
		if (!StringUtils.equals(this.modelName, rs.getModelName())) return false;
		
		if (!StringUtils.equals(this.explanation, rs.getSearchExplanation())) return false;
		
		if (!StringUtils.equals(this.xmldoc, rs.getXmldoc())) return false;
		
		if (this.score != rs.score) return false;
		
		if (!StringUtils.equals(this.versionID, rs.getVersionId())) return false;
		
		return true;
	}



}
