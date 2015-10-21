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

public class PublicationIndexAnalyzer extends Analyzer{
	

	protected final static PerFieldAnalyzerWrapper publicationIndexAnalyzer =  createPublicationFullTextIndexAnalyzer();
	private final static PerFieldAnalyzerWrapper createPublicationFullTextIndexAnalyzer() {		
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.Publication.YEAR, new KeywordAnalyzer());
		map.put(Property.Publication.ID, new KeywordAnalyzer());
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_35), map);
	}
	


		@Override
		public TokenStream tokenStream(String fieldName, Reader reader) {
			return publicationIndexAnalyzer.tokenStream(fieldName, reader);
		}
		



	public static PerFieldAnalyzerWrapper getPublicationIndexAnalyzer() {
		return publicationIndexAnalyzer;
	}



}
