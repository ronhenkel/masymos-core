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

public class AnnotationIndexAnalyzer extends Analyzer{
	

	protected final static PerFieldAnalyzerWrapper annotationIndexAnalyzer =  createAnnotationIndexAnalyzer();
	private final static PerFieldAnalyzerWrapper createAnnotationIndexAnalyzer() {
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.General.URI, new KeywordAnalyzer());
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_36), map);
	}
	
	
		
		@Override
		public TokenStream tokenStream(String fieldName, Reader reader) {
			return annotationIndexAnalyzer.tokenStream(fieldName, reader);
		}
		
	


	public static PerFieldAnalyzerWrapper getAnnotationIndexAnalyzer() {
		return annotationIndexAnalyzer;
	}


}
