package de.unirostock.sems.masymos.data;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class PublicationWrapper {

	
	private String title;
	private String journal;
	private String affiliation;
	private String synopsis;
	private String year;
	private String pubid;
	private List<PersonWrapper> authors = new LinkedList<PersonWrapper>();
	
	public void addAuthor(PersonWrapper author){
		authors.add(author);
	}
	
	public PublicationWrapper(String title, String journal, String synopsis, String affiliation, String year, String pubid, List<PersonWrapper> authors){
		this.affiliation = affiliation;
		this.journal = journal;
		this.synopsis = synopsis;
		this.title = title;
		this.year = year;
		this.setPubid(pubid);
		if (authors!=null) this.authors = authors;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String titel) {
		this.title = titel;
	}

	public String getJounral() {
		return journal;
	}

	public void setJournal(String jounral) {
		this.journal = jounral;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getSynopsis() {
		return synopsis;
	}

	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getPubid() {
		return pubid;
	}

	public void setPubid(String pubid) {
		this.pubid = pubid;
	}

	public List<PersonWrapper> getAuthors() {
		return authors;
	}

	public void setAuthors(List<PersonWrapper> authors) {
		this.authors = authors;
	}

	//call to ensure that all properties are set, avoiding error when storing in neo4j
	public void repairNullStrings(){
		if (StringUtils.isEmpty(affiliation)) affiliation="";
		if (StringUtils.isEmpty(journal)) journal = "";
		if (StringUtils.isEmpty(synopsis)) synopsis="";
		if (StringUtils.isEmpty(title)) title="";
		if (StringUtils.isEmpty(year)) year="";
		if (StringUtils.isEmpty(pubid)) pubid="";		

	}
}
