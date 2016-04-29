package de.unirostock.sems.masymos.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import de.unirostock.sems.masymos.configuration.Property;

public class ConstituentIndexAnalyzer{
	

	protected final static PerFieldAnalyzerWrapper constituentsIndexAnalyzer =  createConstituentsIndexAnalyzer();
	private final static PerFieldAnalyzerWrapper createConstituentsIndexAnalyzer() {
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.General.URI, LowerCaseKeywordAnalyzer.getLowerCaseKeywordAnalyzer());		
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), map);
	}
	

	public static PerFieldAnalyzerWrapper getNodeFullTextIndexAnalyzer() {
		return constituentsIndexAnalyzer;
	}

}
