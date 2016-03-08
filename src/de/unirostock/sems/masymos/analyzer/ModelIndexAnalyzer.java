package de.unirostock.sems.masymos.analyzer;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import de.unirostock.sems.masymos.configuration.Property;

public class ModelIndexAnalyzer extends Analyzer{
	

	protected final static PerFieldAnalyzerWrapper modelIndexAnalyzer =  createModelIndexAnalyzer();
	private final static PerFieldAnalyzerWrapper createModelIndexAnalyzer() {
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.General.URI, new LowerCaseKeywordAnalyzer());		
		map.put(Property.General.ID, new LowerCaseKeywordAnalyzer());	
		return new PerFieldAnalyzerWrapper(new SimpleAnalyzer(Version.LUCENE_36), map);
		//return new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_35), map);
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return modelIndexAnalyzer.tokenStream(fieldName, reader);
	}

	public static PerFieldAnalyzerWrapper getModelIndexAnalyzer() {
		return modelIndexAnalyzer;
	}

}
