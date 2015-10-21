package de.unirostock.sems.masymos.analyzer;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import de.unirostock.sems.masymos.configuration.Property;

public class SedmlndexAnalyzer extends Analyzer{
	

	protected final static PerFieldAnalyzerWrapper sedmlFullIndexAnalyzer =  createSedmlFullIndexAnalyzer();
	
	private final static PerFieldAnalyzerWrapper createSedmlFullIndexAnalyzer() {
		Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
		analyzers.put(Property.General.URI, new KeywordAnalyzer());	
		//analyzers.put(Property.SEDML.MODELSOURCE, new KeywordAnalyzer());	
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_31), analyzers);
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return sedmlFullIndexAnalyzer.tokenStream(fieldName, reader);
	}

	public static PerFieldAnalyzerWrapper getSedmlFullIndexAnalyzer() {
		return sedmlFullIndexAnalyzer;
	}

}
