package de.unirostock.sems.masymos.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import de.unirostock.sems.masymos.configuration.Property;

public class AnnotationIndexAnalyzer{
	

	protected final static PerFieldAnalyzerWrapper annotationIndexAnalyzer =  createAnnotationIndexAnalyzer();
	
	private final static PerFieldAnalyzerWrapper createAnnotationIndexAnalyzer() {
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.General.URI, new KeywordAnalyzer());
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), map);
	}	


	public static PerFieldAnalyzerWrapper getAnnotationIndexAnalyzer() {
		return annotationIndexAnalyzer;
	}


}
