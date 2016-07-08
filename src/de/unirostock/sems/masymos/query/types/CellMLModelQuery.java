package de.unirostock.sems.masymos.query.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import de.unirostock.sems.masymos.analyzer.AnalyzerHandler;
import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.database.traverse.ModelTraverser;
import de.unirostock.sems.masymos.query.IQueryInterface;
import de.unirostock.sems.masymos.query.enumerator.CellMLModelFieldEnumerator;
import de.unirostock.sems.masymos.query.results.VersionResultSet;

public class CellMLModelQuery implements IQueryInterface {
	private final Analyzer analyzer = AnalyzerHandler.getModelindexanalyzer();
	private final Index<Node> index = Manager.instance().getModelIndex();
	private final String[] indexedFields = {	Property.General.ID,  	
												Property.General.NAME,
												Property.CellML.COMPONENT,
												Property.CellML.VARIABLE, 
												Property.General.CREATOR, 
												Property.General.AUTHOR, 
												//Property.General.URI
												};

	private Map<CellMLModelFieldEnumerator, List<String>> queryMap =  new HashMap<CellMLModelFieldEnumerator, List<String>>();
	
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


	public List<VersionResultSet> getResults() {
		//As this query retruns model results anyway, this call is referred
		return this.getModelResults();
	}

	@Override
	public List<VersionResultSet> getModelResults() {
		return retrieveModelResults();
	}		
	
	@Override
	public CellMLModelFieldEnumerator[] getIndexFields() {
		return CellMLModelFieldEnumerator.class.getEnumConstants();
	}

	public void addQueryClause(CellMLModelFieldEnumerator field, String queryString) {
		
		if (CellMLModelFieldEnumerator.NONE.equals(field)){
			//if a NONE field was provided skip the list and expand to all
			queryMap = new HashMap<CellMLModelFieldEnumerator, List<String>>();
			CellMLModelFieldEnumerator[] pe = getIndexFields();
			for (int i = 0; i < pe.length; i++) {
				CellMLModelFieldEnumerator e = pe[i];
				if (e.equals(CellMLModelFieldEnumerator.NONE)) continue;
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
		QueryParser qp = new QueryParser(Property.General.NAME, analyzer);
		StringBuffer q = new StringBuffer();
		for (Iterator<CellMLModelFieldEnumerator> queryMapIt = queryMap.keySet().iterator(); queryMapIt.hasNext();) {
			CellMLModelFieldEnumerator pe = (CellMLModelFieldEnumerator) queryMapIt.next();
			List<String> termList = queryMap.get(pe);
			for (Iterator<String> termListIt = termList.iterator(); termListIt.hasNext();) {
				String term = (String) termListIt.next();
				if (StringUtils.isEmpty(term)) continue;
				try {
					//try to parse term
					qp.parse(term);
				} catch (ParseException e) {
					//if it fails, escape term
					term = QueryParser.escape(term);
				}
				q.append(pe.getElementNameEquivalent());
				q.append(":(");
				q.append(term);
				q.append(")^");
				q.append(pe.getElementWeightEquivalent());
				q.append(" ");

			}
		}						 
		return qp.parse(q.toString());		
	}
	
	private List<VersionResultSet> retrieveModelResults(){
		if (queryMap.isEmpty()) return new LinkedList<VersionResultSet>();
		
		Query q = null;
		try {
			q = createQueryFromQueryMap();
		} catch (ParseException e) {
			// TODO log me
			return new LinkedList<VersionResultSet>();
		}
		
		IndexHits<Node> hits = index.query(q);

		if ((hits == null) || (hits.size() == 0)) {
			return new LinkedList<VersionResultSet>();
		}

		List<VersionResultSet> result = new LinkedList<VersionResultSet>();

		for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {		
			Node node = (Node) hitsIt.next();
			if (node.hasLabel(NodeLabel.Types.CELLML_MODEL)){
				result.addAll(ModelTraverser.getModelResultSetFromNode(node, hits.currentScore(), "ModelIndex"));				
			}
		}
		return result;
	}
	


}
