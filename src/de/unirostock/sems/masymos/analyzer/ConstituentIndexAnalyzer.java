package de.unirostock.sems.masymos.analyzer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import de.unirostock.sems.masymos.configuration.Property;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class ConstituentIndexAnalyzer extends DelegatingAnalyzerWrapper{
	

	private final Analyzer defaultAnalyzer;
	private final Map<String, Analyzer> fieldAnalyzers;

	public ConstituentIndexAnalyzer(Analyzer defaultAnalyzer) {
		this(defaultAnalyzer, null);
	}

	public ConstituentIndexAnalyzer(Analyzer defaultAnalyzer, Map<String, Analyzer> fieldAnalyzers) {
		super(PER_FIELD_REUSE_STRATEGY);
		this.defaultAnalyzer = defaultAnalyzer;
		this.fieldAnalyzers = (fieldAnalyzers != null) ? fieldAnalyzers : Collections.<String, Analyzer> emptyMap();
	}
	
	public ConstituentIndexAnalyzer() {
		super(PER_FIELD_REUSE_STRATEGY);
		Map<String, Analyzer> map = new HashMap<String, Analyzer>();
		map.put(Property.General.URI, AnalyzerHandler.getLowercasekeywordanalyzer());
		this.defaultAnalyzer = new StandardAnalyzer();
		this.fieldAnalyzers = map;
		
	}

	@Override
	protected Analyzer getWrappedAnalyzer(String fieldName) {
		Analyzer analyzer = fieldAnalyzers.get(fieldName);
		return (analyzer != null) ? analyzer : defaultAnalyzer;
	}

	@Override
	public String toString() {
		return "PerFieldAnalyzerWrapper(" + fieldAnalyzers + ", default=" + defaultAnalyzer + ")";
	}
	
}
