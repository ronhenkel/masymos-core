package de.unirostock.sems.masymos.query.types;

import org.apache.lucene.queryParser.ParseException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import de.unirostock.sems.masymos.analyzer.SedmlndexAnalyzer;
import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.database.traverse.DBSedmlTraverser;
import de.unirostock.sems.masymos.query.IQueryInterface;
import de.unirostock.sems.masymos.query.enumerator.SedmlFieldEnumerator;
import de.unirostock.sems.masymos.query.results.ModelResultSet;
import de.unirostock.sems.masymos.query.results.SedmlResultSet;

public class SedmlQuery implements IQueryInterface {
	
	private final Analyzer analyzer = SedmlndexAnalyzer.getSedmlFullIndexAnalyzer();
	private final Index<Node> index = Manager.instance().getSedmlIndex();
	private final String[] indexedFields = { Property.SEDML.NAME, Property.SEDML.MODELSOURCE,
											Property.SEDML.OUTPUT_TYPE, Property.SEDML.DATALABEL,
											Property.SEDML.SIM_KISAO, Property.SEDML.SIM_TYPE,
											Property.SEDML.MATH, Property.General.URI};

	private Map<SedmlFieldEnumerator, List<String>> queryMap =  new HashMap<SedmlFieldEnumerator, List<String>>();
	
	@Override
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	@Override
	public Index<Node> getIndex() {
		return index;
	}

	@Override
	public String[] getIndexedFields() {
		return indexedFields;
	}

	@Override
	public Query getQuery() {
		Query q = null;
		try {
			q = createQueryFromQueryMap();
		} catch (ParseException e) {
			//TODO log me
			q = null;
		}
		return q;
	}

	
	public List<SedmlResultSet> getResults() {
		return retrieveResults();
	}

	@Override
	public SedmlFieldEnumerator[] getIndexFields() {
		return SedmlFieldEnumerator.class.getEnumConstants();
	}

	public void addQueryClause(SedmlFieldEnumerator field, String queryString) {
		
		if (SedmlFieldEnumerator.NONE.equals(field)){
			//if a NONE field was provided skip the list and expand to all
			queryMap = new HashMap<SedmlFieldEnumerator, List<String>>();
			SedmlFieldEnumerator[] pe = getIndexFields();
			for (int i = 0; i < pe.length; i++) {
				SedmlFieldEnumerator e = pe[i];
				if (e.equals(SedmlFieldEnumerator.NONE)) continue;
				List<String> termList = new LinkedList<String>();
				termList.add(queryString);
				queryMap.put(e, termList);
			}
		} else {
			//add single field -> string pair
			List<String> termList = null; 
			if (queryMap.keySet().contains(field)){
				termList = queryMap.get(field);
			} else {
				termList = new LinkedList<String>();
			}
			termList.add(queryString);
			
			queryMap.put(field, termList);
		}
	}
	
	private Query createQueryFromQueryMap() throws ParseException{
		if (queryMap.isEmpty()) return null;
		
		StringBuffer q = new StringBuffer();
		for (Iterator<SedmlFieldEnumerator> queryMapIt = queryMap.keySet().iterator(); queryMapIt.hasNext();) {
			SedmlFieldEnumerator pe = (SedmlFieldEnumerator) queryMapIt.next();
			List<String> termList = queryMap.get(pe);
			for (Iterator<String> termListIt = termList.iterator(); termListIt.hasNext();) {
				String term = (String) termListIt.next();
				if (StringUtils.isEmpty(term)) continue;
				q.append(pe.getElementNameEquivalent());
				q.append(":(");
				q.append(term);
				q.append(")^");
				q.append(pe.getElementWeightEquivalent());
				q.append(" ");

			}
		}		
		QueryParser qp = new QueryParser(Version.LUCENE_31, Property.SEDML.MODELSOURCE, analyzer);		 
		return qp.parse(q.toString());		
	}
	
	private List<SedmlResultSet> retrieveResults(){
		if (queryMap.isEmpty()) return new LinkedList<SedmlResultSet>();
		
		Query q = null;
		try {
			q = createQueryFromQueryMap();
		} catch (ParseException e) {
			// TODO log me
			return new LinkedList<SedmlResultSet>();
		}
		
		IndexHits<Node> hits = index.query(q);

		if ((hits == null) || (hits.size() == 0)) {
			return new LinkedList<SedmlResultSet>();
		}
		List<SedmlResultSet> result = new LinkedList<SedmlResultSet>();

		for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {		
			Node node = (Node) hitsIt.next();
			if (node.hasLabel(NodeLabel.Types.SEDML)){
				result.add(DBSedmlTraverser.getResultSetSedmlFromNode(node, hits.currentScore()));				
			}
		}
		return result;
	}

	@Override
	public List<ModelResultSet> getModelResults() {
		// TODO Models connected to Sedmls
		return new LinkedList<ModelResultSet>();
	}

}
