package de.unirostock.sems.masymos.util;
import java.util.List;


public class CmetaContainer {
	
	public String model_author_org; 
	public List<List<String>> keywords;
	public String citation_title;
	public List<List<String>> citation_authors; 
	public String title;
	public String citation_journal;
	public String model_author;
	public String citation_id;
	
	
	public String getModel_author_org() {
		return model_author_org;
	}
	public void setModel_author_org(String model_author_org) {
		this.model_author_org = model_author_org;
	}
	public List<List<String>> getKeywords() {
		return keywords;
	}
	public void setKeywords(List<List<String>> keywords) {
		this.keywords = keywords;
	}
	public String getCitation_title() {
		return citation_title;
	}
	public void setCitation_title(String citation_title) {
		this.citation_title = citation_title;
	}
	public List<List<String>> getCitation_authors() {
		return citation_authors;
	}
	public void setCitation_authors(List<List<String>> citation_authors) {
		this.citation_authors = citation_authors;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCitation_journal() {
		return citation_journal;
	}
	public void setCitation_journal(String citation_journal) {
		this.citation_journal = citation_journal;
	}
	public String getModel_author() {
		return model_author;
	}
	public void setModel_author(String model_author) {
		this.model_author = model_author;
	}
	public String getCitation_id() {
		return citation_id;
	}
	public void setCitation_id(String citation_id) {
		this.citation_id = citation_id;
	}

}
