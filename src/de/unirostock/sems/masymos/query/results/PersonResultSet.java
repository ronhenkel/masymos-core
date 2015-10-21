package de.unirostock.sems.masymos.query.results;

import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.masymos.data.PersonWrapper;
import de.unirostock.sems.masymos.query.IResourceResultSetInterface;

public class PersonResultSet implements IResourceResultSetInterface {

	private float score;
	private String explanation;
	private PersonWrapper person;
	private List<String> relatedModelsURI = new LinkedList<String>();
	
	
	public PersonResultSet(float score, String lastName, String firstName, String eMail, String organization){
		this.score = score;
		this.person = new PersonWrapper(firstName, lastName, eMail, organization);
	}

	@Override
	public float getScore() {
		return score;
	}

	@Override
	public String getSearchExplanation() {
		return explanation;
	}
	
	public String getFirstName() {
		return person.getFirstName();
	}

	public String getLastName() {
		return person.getLastName();
	}

	public String getEmail() {
		return person.getEmail();
	}

	
	public List<String> getRelatedModelsURI() {
		return relatedModelsURI;
	}
	
	public void addRelatedModelURI(String uri){
		relatedModelsURI.add(uri);
	}

}
