package de.unirostock.sems.masymos.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import de.unirostock.sems.masymos.configuration.Property;

public class SedmlndexAnalyzer{
	

	protected final static PerFieldAnalyzerWrapper sedmlFullIndexAnalyzer =  createSedmlFullIndexAnalyzer();
	
	private final static PerFieldAnalyzerWrapper createSedmlFullIndexAnalyzer() {
		Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
		analyzers.put(Property.General.URI, new KeywordAnalyzer());	
		//analyzers.put(Property.SEDML.MODELSOURCE, new KeywordAnalyzer());	
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzers);
	}

	public static PerFieldAnalyzerWrapper getSedmlFullIndexAnalyzer() {
		return sedmlFullIndexAnalyzer;
	}

}
