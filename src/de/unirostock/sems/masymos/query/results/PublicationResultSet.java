package de.unirostock.sems.masymos.query.results;

import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.data.PersonWrapper;
import de.unirostock.sems.masymos.data.PublicationWrapper;
import de.unirostock.sems.masymos.query.IResourceResultSetInterface;

public class PublicationResultSet implements IResourceResultSetInterface{

	private float score;
	private String explanation;
	private PublicationWrapper publication;
	private List<String> relatedModelsURI = new LinkedList<String>();
	
	
	public PublicationResultSet(float score, String title, String jounral, String affiliation, String year){
		this.score= score;
		this.publication = new PublicationWrapper(title, jounral, "", affiliation, year, null, null);
	}

	@Override
	public float getScore() {
		return score;
	}

	@Override
	public String getSearchExplanation() {
		return explanation;
	}
	
	public String getTitle() {
		return publication.getTitle();
	}

	public String getJounral() {
		return publication.getJounral();
	}

	public String getAffiliation() {
		return publication.getAffiliation();
	}

	public String getYear() {
		return publication.getYear();
	}

	public List<PersonWrapper> getAuthors() {
		return publication.getAuthors();
	}
	
	public void setAuthors(List<PersonWrapper> authors){
		publication.setAuthors(authors);
	}

	public List<String> getRelatedModelsURI() {
		return relatedModelsURI;
	}
	
	public void addRelatedModelURI(String uri){
		relatedModelsURI.add(uri);
	}

}
