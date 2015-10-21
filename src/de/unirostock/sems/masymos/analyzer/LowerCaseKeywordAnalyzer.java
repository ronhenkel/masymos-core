package de.unirostock.sems.masymos.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

public class LowerCaseKeywordAnalyzer extends Analyzer{

	@Override
	public TokenStream tokenStream(String fileName, Reader reader) {
		return new LowerCaseFilter( Version.LUCENE_35, new KeywordTokenizer( reader ) );
	}

}
