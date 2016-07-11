package de.unirostock.sems.masymos.analyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerHandler{
	
	final static Logger logger = LoggerFactory.getLogger(AnalyzerHandler.class);

	private final static Analyzer lowerCaseKeywordAnalyzer =  createLowerCaseKeywordAnalyzer();
	private final static Analyzer modelIndexAnalyzer = new ModelIndexAnalyzer();
	private final static Analyzer annotationIndexAnalyzer = new AnnotationIndexAnalyzer();
	private final static Analyzer constituentIndexAnalyzer = new ConstituentIndexAnalyzer();
	private final static Analyzer personIndexAnalyzer = new PersonIndexAnalyzer();
	private final static Analyzer publicationIndexAnalyzer = new PublicationIndexAnalyzer();
	private final static Analyzer sedmlIndexAnalyzer = new SedmlndexAnalyzer();
	private final static List<Analyzer> availableAnalyzers = Arrays.asList(lowerCaseKeywordAnalyzer, modelIndexAnalyzer, annotationIndexAnalyzer, constituentIndexAnalyzer, personIndexAnalyzer, publicationIndexAnalyzer, sedmlIndexAnalyzer);
		
	public static List<Analyzer> getAvailableanalyzers() {
		return availableAnalyzers;
	}

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
			logger.error(e.getMessage());
			createdCustom = false;
		} finally {
			if (!createdCustom) custom = new KeywordAnalyzer();
		}		
		return  custom;		
	}

	public static Analyzer getLowercasekeywordanalyzer() {
		return lowerCaseKeywordAnalyzer;
	}

	public static Analyzer getModelindexanalyzer() {
		return modelIndexAnalyzer;
	}

	public static Analyzer getAnnotationindexanalyzer() {
		return annotationIndexAnalyzer;
	}

	public static Analyzer getConstituentindexanalyzer() {
		return constituentIndexAnalyzer;
	}

	public static Analyzer getPersonindexanalyzer() {
		return personIndexAnalyzer;
	}

	public static Analyzer getPublicationindexanalyzer() {
		return publicationIndexAnalyzer;
	}

	public static Analyzer getSedmlindexanalyzer() {
		return sedmlIndexAnalyzer;
	}

	


}
