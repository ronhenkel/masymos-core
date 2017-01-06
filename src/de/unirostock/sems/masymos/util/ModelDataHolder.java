package de.unirostock.sems.masymos.util;

import java.util.List;
import java.util.Map;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public final class ModelDataHolder {

	private String fileId;
	private String versionId;
	private String xmldoc;
	private String modelType;
	private Map<String,List<String>> parentMap;
	private Map<String, String> metaMap;
	
	public ModelDataHolder(String fileId, String versionId, String xmldoc,
			Map<String, List<String>> parentMap, Map<String, String> metaMap, String modelType) {
		super();
		this.fileId = fileId; //intern ID for 2MT
		this.versionId = versionId; //identifier for model version (BioModels: name of release, PMR2: commithashtag
		this.xmldoc = xmldoc;  // URI where to download the model
		this.parentMap = parentMap; //versionId of parents
		this.metaMap = metaMap; //key, value ModelCrawler specific
		this.modelType = modelType; //sbml or cellml or sedml
	}

	public Map<String, String> getMetaMap() {
		return metaMap;
	}
	public void setMetaMap(Map<String, String> metaMap) {
		this.metaMap = metaMap;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getVersionId() {
		return versionId;
	}
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
	public String getXmldoc() {
		return xmldoc;
	}
	public void setXmldoc(String xmldoc) {
		this.xmldoc = xmldoc;
	}
	public Map<String, List<String>> getParentMap() {
		return parentMap;
	}
	public void setParentMap(Map<String, List<String>> parentMap) {
		this.parentMap = parentMap;
	}

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	
}
