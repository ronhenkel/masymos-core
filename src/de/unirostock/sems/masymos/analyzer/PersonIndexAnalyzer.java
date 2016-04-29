package de.unirostock.sems.masymos.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import de.unirostock.sems.masymos.configuration.Property;

public class PersonIndexAnalyzer{
	

	protected final static PerFieldAnalyzerWrapper personIndexAnalyzer =  createpersonExactAnalyzer();
	private final static PerFieldAnalyzerWrapper createpersonExactAnalyzer() {		
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.Person.FAMILYNAME, LowerCaseKeywordAnalyzer.getLowerCaseKeywordAnalyzer());
		map.put(Property.Person.GIVENNAME, LowerCaseKeywordAnalyzer.getLowerCaseKeywordAnalyzer());
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), map);
	}
	
		

		public static PerFieldAnalyzerWrapper getPersonIndexAnalyzer() {
			return personIndexAnalyzer;
		}



}
