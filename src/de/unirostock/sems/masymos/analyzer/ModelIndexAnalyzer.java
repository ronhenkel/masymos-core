package de.unirostock.sems.masymos.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import de.unirostock.sems.masymos.configuration.Property;

public class ModelIndexAnalyzer{
	

	protected final static PerFieldAnalyzerWrapper modelIndexAnalyzer =  createModelIndexAnalyzer();
	private final static PerFieldAnalyzerWrapper createModelIndexAnalyzer() {
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.General.URI, LowerCaseKeywordAnalyzer.getLowerCaseKeywordAnalyzer());		
		map.put(Property.General.ID, LowerCaseKeywordAnalyzer.getLowerCaseKeywordAnalyzer());	
		return new PerFieldAnalyzerWrapper(new SimpleAnalyzer(), map);
		//return new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_35), map);
	}
	
	public static PerFieldAnalyzerWrapper getModelIndexAnalyzer() {
		return modelIndexAnalyzer;
	}

}
