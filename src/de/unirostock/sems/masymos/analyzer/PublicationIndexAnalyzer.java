package de.unirostock.sems.masymos.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import de.unirostock.sems.masymos.configuration.Property;

public class PublicationIndexAnalyzer{
	

	protected final static PerFieldAnalyzerWrapper publicationIndexAnalyzer =  createPublicationFullTextIndexAnalyzer();
	private final static PerFieldAnalyzerWrapper createPublicationFullTextIndexAnalyzer() {		
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.Publication.YEAR, new KeywordAnalyzer());
		map.put(Property.Publication.ID, new KeywordAnalyzer());
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), map);
	}

	public static PerFieldAnalyzerWrapper getPublicationIndexAnalyzer() {
		return publicationIndexAnalyzer;
	}



}
