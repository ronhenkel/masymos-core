package de.unirostock.sems.masymos.analyzer;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import de.unirostock.sems.masymos.configuration.Property;

public class ConstituentIndexAnalyzer extends Analyzer{
	

	protected final static PerFieldAnalyzerWrapper constituentsIndexAnalyzer =  createConstituentsIndexAnalyzer();
	private final static PerFieldAnalyzerWrapper createConstituentsIndexAnalyzer() {
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.General.URI, new LowerCaseKeywordAnalyzer());		
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_35), map);
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return constituentsIndexAnalyzer.tokenStream(fieldName, reader);
	}

	public static PerFieldAnalyzerWrapper getNodeFullTextIndexAnalyzer() {
		return constituentsIndexAnalyzer;
	}

}
