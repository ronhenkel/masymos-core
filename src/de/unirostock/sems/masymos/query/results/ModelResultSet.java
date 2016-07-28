package de.unirostock.sems.masymos.query.results;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class ModelResultSet {
	private String modelName;
	private float score;
	private String modelID;
	private String explanation;
	private String documentURI;
	private String xmldoc;
	private String filename;
	private String fileId;
	private String indexSource;
	private List<VersionResultSet> versions = new LinkedList<VersionResultSet>();
	

	public List<VersionResultSet> getVersions() {
		return versions;
	}


	public void setVersions(List<VersionResultSet> versions) {
		this.versions = versions;
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

	
	public ModelResultSet copyModelResultSet(){
		ModelResultSet newSet = new ModelResultSet();
		newSet.modelName = this.getModelName();
		newSet.score = this.getScore();
		newSet.modelID = this.getModelID();
		newSet.documentURI = this.getDocumentURI();
		newSet.xmldoc = this.getXmldoc();
		newSet.filename = this.getFilename();
		newSet.fileId = this.getFileId();
		newSet.indexSource = this.getIndexSource();
		List<VersionResultSet> newVersions = new LinkedList<VersionResultSet>();
		Collections.copy(newVersions, this.versions);
		
		return newSet;
	}
	
	protected ModelResultSet(){
		
	}


	public ModelResultSet(float score, String modelId, String modelName, List<VersionResultSet> versions, String documentURI, String filename, String explanation, String indexSource){
		this.modelName = modelName;
		this.score = score;
		this.modelID = modelId;
		this.explanation = explanation;
		this.documentURI = documentURI;
		this.filename = filename;
		this.indexSource = indexSource;
		this.versions = versions;
	}
	
	
	public ModelResultSet(float score, String modelId, String modelName, String indexSource){
		this.modelName = modelName;
		this.score = score;
		this.modelID = modelId;
		this.indexSource = indexSource;
	}
	
	public ModelResultSet(float score, String modelId, String modelName, String documentURI, String filename, String indexSource){
		this.modelName = modelName;
		this.score = score;
		this.modelID = modelId;
		this.documentURI = documentURI;
		this.filename = filename;
		this.indexSource = indexSource;
	}
	
	public void addVersion(VersionResultSet newVersion){
		this.versions.add(newVersion);
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public String getModelId() {
		return modelID;
	}

	
	public float getScore() {
		return score;
	}

	
	public String getSearchExplanation() {
		return explanation;
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
		ModelResultSet rs; 
		if ((resultSet==null) || !(resultSet instanceof ModelResultSet)) return false;
		else rs = (ModelResultSet) resultSet; 
		
		if (!StringUtils.equals(this.modelID, rs.getModelId())) return false;
		
		if (!StringUtils.equals(this.documentURI, rs.getDocumentURI())) return false;
		
		if (!StringUtils.equals(this.filename, rs.getFilename())) return false;
		
		if (!StringUtils.equals(this.modelName, rs.getModelName())) return false;
		
		if (!StringUtils.equals(this.explanation, rs.getSearchExplanation())) return false;
		
		if (!StringUtils.equals(this.xmldoc, rs.getXmldoc())) return false;
		
		if (this.score != rs.score) return false;
		
		if (!this.versions.equals(rs.getVersions())) return false;
		
		return true;
	}


}