package de.unirostock.sems.masymos.analyzer;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;

public class LowerCaseKeywordAnalyzer{

	protected final static Analyzer lowerCaseKeywordAnalyzer =  createLowerCaseKeywordAnalyzer();
	private final static Analyzer createLowerCaseKeywordAnalyzer() {	
		Boolean createdCustom = false;
		Analyzer custom = null;
		try {
			custom = CustomAnalyzer.builder()
								.withTokenizer(KeywordTokenizerFactory.class)
								.addTokenFilter(LowerCaseFilterFactory.class)
								.build();
			createdCustom = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			createdCustom = false;
		} finally {
			if (!createdCustom) custom = new KeywordAnalyzer();
		}
		
		return  custom;
		
	}
	
	public static Analyzer getLowerCaseKeywordAnalyzer() {
		return lowerCaseKeywordAnalyzer;
	}

	
	
	
//	@Override
//	public TokenStream tokenStream(String fileName, Reader reader) {
//		return new LowerCaseFilter( Version.LUCENE_36, new KeywordTokenizer( reader ) );
//	}

	

}
