package de.unirostock.sems.masymos.query;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import de.unirostock.sems.masymos.query.results.VersionResultSet;


public interface IQueryInterface {
	
	public Analyzer getAnalyzer();
	
	public Index<Node> getIndex();
	
	public String[] getIndexedFields();
	
	public Enum<?>[] getIndexFields();
	
	public Query getQuery();
	
	public List<VersionResultSet> getModelResults();

}
