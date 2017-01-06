package de.unirostock.sems.masymos.query.results;

import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.query.IResourceResultSetInterface;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/

public class AnnotationResultSet implements IResourceResultSetInterface{

	private float score;
	private String explanation;
	private String uri;
	private List<String> relatedModelsURI = new LinkedList<String>();
	
	@Override
	public float getScore() {
		return score;
	}

	@Override
	public String getSearchExplanation() {
		return explanation;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public AnnotationResultSet(float score, String uri, String explanation){
		this.score = score;
		this.uri = uri;
		this.explanation = explanation;
	}
	
	public AnnotationResultSet(float score, String uri){
		this.score = score;
		this.uri = uri;
		this.explanation = null;
	}

	public List<String> getRelatedModelsURI() {
		return relatedModelsURI;
	}
	
	public void addRelatedModelURI(String uri){
		relatedModelsURI.add(uri);
	}


}
