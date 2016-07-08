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
import de.unirostock.sems.masymos.query.enumerator.PersonFieldEnumerator;
import de.unirostock.sems.masymos.query.results.VersionResultSet;
import de.unirostock.sems.masymos.query.results.PersonResultSet;

public class PersonQuery implements IQueryInterface{

	private final Analyzer analyzer = AnalyzerHandler.getPersonindexanalyzer();
	private final Index<Node> index = Manager.instance().getPersonIndex();
	private final String[] indexedFields = {	Property.Person.FAMILYNAME,
												Property.Person.GIVENNAME,
												Property.Person.EMAIL,
												Property.Person.ORGANIZATION};
	
	private Map<PersonFieldEnumerator, List<String>> queryMap =  new HashMap<PersonFieldEnumerator, List<String>>();
	
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
	
	@Override
	public PersonFieldEnumerator[] getIndexFields() {
		return PersonFieldEnumerator.class.getEnumConstants();
	}
	
	@Override
	public List<VersionResultSet> getModelResults() {
		return retrieveModelResultsByPerson();
	}

	public List<PersonResultSet> getResults() {
		return retrievePersonResults();
	}


	public void addQueryClause(PersonFieldEnumerator field, String queryString) {
		
		if (PersonFieldEnumerator.NONE.equals(field)){
			//if a NONE field was provided skip the list and expand to all
			queryMap = new HashMap<PersonFieldEnumerator, List<String>>();
			PersonFieldEnumerator[] pe = getIndexFields();
			for (int i = 0; i < pe.length; i++) {
				PersonFieldEnumerator e = pe[i];
				if (e.equals(PersonFieldEnumerator.NONE)) continue;
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
		QueryParser qp = new QueryParser(Property.Person.FAMILYNAME, analyzer);
		StringBuffer q = new StringBuffer();
		for (Iterator<PersonFieldEnumerator> queryMapIt = queryMap.keySet().iterator(); queryMapIt.hasNext();) {
			PersonFieldEnumerator pe = (PersonFieldEnumerator) queryMapIt.next();
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
	
	private List<VersionResultSet> retrieveModelResultsByPerson(){

		IndexHits<Node> hits = retrieveHits();

		if ((hits == null) || (hits.size() == 0)) {
			return new LinkedList<VersionResultSet>();
		}
		List<VersionResultSet> result = new LinkedList<VersionResultSet>();

		for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {		
			Node node = (Node) hitsIt.next();
			if (node.hasLabel(NodeLabel.Types.PERSON)){
				result.addAll(ModelTraverser.getModelResultSetFromNode(node, hits.currentScore(),"PersonIndex"));				
			}
		}
		return result;
	}
	
	private List<PersonResultSet> retrievePersonResults(){
		
		IndexHits<Node> hits = retrieveHits();
		if ((hits == null) || (hits.size() == 0)) {
			return new LinkedList<PersonResultSet>();
		}
		List<PersonResultSet> resultList = new LinkedList<PersonResultSet>();
				
		for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {
			Node node = (Node) hitsIt.next();
			float score = hits.currentScore();
			//end loop if bestN or threshold are met
			if (node.hasLabel(NodeLabel.Types.PERSON)){
				PersonResultSet prs = new PersonResultSet(score,
									(String)node.getProperty(Property.Person.FAMILYNAME, ""),
									(String)node.getProperty(Property.Person.GIVENNAME, ""),
									(String)node.getProperty(Property.Person.EMAIL, ""),
									(String)node.getProperty(Property.Person.ORGANIZATION, ""));
				List<Node> documentNodes = ModelTraverser.getDocumentsFromNode(node);
				for (Iterator<Node> docNodeIt = documentNodes.iterator(); docNodeIt.hasNext();) {
					Node docNode = (Node) docNodeIt.next();
					if (docNode.hasProperty(Property.General.URI)) prs.addRelatedModelURI((String)docNode.getProperty(Property.General.URI));
				}
				resultList.add(prs);				
			}
		}		
		return resultList;
	}


	private IndexHits<Node> retrieveHits(){
		if (queryMap.isEmpty()) return null;
		
		Query q = null;
		try {
			q = createQueryFromQueryMap();
		} catch (ParseException e) {
			// TODO log me
			return null;
		}
		
		return index.query(q);
	}

}
