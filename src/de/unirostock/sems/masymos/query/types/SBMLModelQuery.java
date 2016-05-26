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
import de.unirostock.sems.masymos.query.enumerator.SBMLModelFieldEnumerator;
import de.unirostock.sems.masymos.query.results.ModelResultSet;

public class SBMLModelQuery implements IQueryInterface {
	private final Analyzer analyzer = AnalyzerHandler.getModelindexanalyzer();
	private final Index<Node> index = Manager.instance().getModelIndex();
	private final String[] indexedFields = { 	Property.General.ID,
												Property.General.NAME,
												Property.SBML.COMPARTMENT,
												Property.SBML.SPECIES,
												Property.SBML.REACTION,											  
												Property.General.CREATOR, 
												Property.General.AUTHOR
												//Property.General.URI
												};

	private Map<SBMLModelFieldEnumerator, List<String>> queryMap =  new HashMap<SBMLModelFieldEnumerator, List<String>>();
	
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

//	@Override
//	public List<ModelResultSet> getResults() {
//		return this.getModelResults();
//	}
	
	@Override
	public List<ModelResultSet> getModelResults() {
		return retrieveModelResults();
	}

	@Override
	public SBMLModelFieldEnumerator[] getIndexFields() {
		return SBMLModelFieldEnumerator.class.getEnumConstants();
	}

	public void addQueryClause(SBMLModelFieldEnumerator field, String queryString) {
		
		if (SBMLModelFieldEnumerator.NONE.equals(field)){
			//if a NONE field was provided skip the list and expand to all
			queryMap = new HashMap<SBMLModelFieldEnumerator, List<String>>();
			SBMLModelFieldEnumerator[] pe = getIndexFields();
			for (int i = 0; i < pe.length; i++) {
				SBMLModelFieldEnumerator e = pe[i];
				if (e.equals(SBMLModelFieldEnumerator.NONE)) continue;
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
		for (Iterator<SBMLModelFieldEnumerator> queryMapIt = queryMap.keySet().iterator(); queryMapIt.hasNext();) {
			SBMLModelFieldEnumerator pe = (SBMLModelFieldEnumerator) queryMapIt.next();
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
	
	private List<ModelResultSet> retrieveModelResults(){
		if (queryMap.isEmpty()) return new LinkedList<ModelResultSet>();
		
		Query q = null;
		try {
			q = createQueryFromQueryMap();
		} catch (ParseException e) {
			// TODO log me
			return new LinkedList<ModelResultSet>();
		}
		
		IndexHits<Node> hits = index.query(q);

		if ((hits == null) || (hits.size() == 0)) {
			return new LinkedList<ModelResultSet>();
		}
		List<ModelResultSet> result = new LinkedList<ModelResultSet>();

		for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {		
			Node node = (Node) hitsIt.next();
			if (node.hasLabel(NodeLabel.Types.SBML_MODEL)){
				result.addAll(ModelTraverser.getModelResultSetFromNode(node, hits.currentScore(), "ModelIndex"));				
			}
		}
		return result;
	}

}
