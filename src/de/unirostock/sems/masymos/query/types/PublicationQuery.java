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
import de.unirostock.sems.masymos.data.PersonWrapper;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.database.traverse.ModelTraverser;
import de.unirostock.sems.masymos.query.IQueryInterface;
import de.unirostock.sems.masymos.query.enumerator.PublicationFieldEnumerator;
import de.unirostock.sems.masymos.query.results.ModelResultSet;
import de.unirostock.sems.masymos.query.results.PublicationResultSet;

public class PublicationQuery implements IQueryInterface {

	private final Analyzer analyzer = AnalyzerHandler.getPublicationindexanalyzer();
	private final Index<Node> index = Manager.instance().getPublicationIndex();
	private final String[] indexedFields = {Property.Publication.ABSTRACT,
											Property.Publication.AFFILIATION, Property.Publication.AUTHOR, 
											Property.Publication.JOURNAL, Property.Publication.TITLE, 
											Property.Publication.YEAR};
	private Map<PublicationFieldEnumerator, List<String>> queryMap =  new HashMap<PublicationFieldEnumerator, List<String>>();
	
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
	public PublicationFieldEnumerator[] getIndexFields() {
		return PublicationFieldEnumerator.class.getEnumConstants();
	}
	
	@Override
	public List<ModelResultSet> getModelResults() {
		return retrieveModelResultsByPublication();
	}

	public List<PublicationResultSet> getResults(){
		return retrievePublicationResults();
	}

	public void addQueryClause(PublicationFieldEnumerator field, String queryString) {
		
		if (PublicationFieldEnumerator.NONE.equals(field)){
			//if a NONE field was provided skip the list and expand to all
			queryMap = new HashMap<PublicationFieldEnumerator, List<String>>();
			PublicationFieldEnumerator[] pe = getIndexFields();
			for (int i = 0; i < pe.length; i++) {
				PublicationFieldEnumerator e = pe[i];
				if (e.equals(PublicationFieldEnumerator.NONE)) continue;
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
		QueryParser qp = new QueryParser(Property.Publication.TITLE, analyzer);
		StringBuffer q = new StringBuffer();
		for (Iterator<PublicationFieldEnumerator> queryMapIt = queryMap.keySet().iterator(); queryMapIt.hasNext();) {
			PublicationFieldEnumerator pe = (PublicationFieldEnumerator) queryMapIt.next();
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
	
	private List<ModelResultSet> retrieveModelResultsByPublication(){
		
		IndexHits<Node> hits = retrieveHits();

		if ((hits == null) || (hits.size() == 0)) {
			return new LinkedList<ModelResultSet>();
		}
		List<ModelResultSet> result = new LinkedList<ModelResultSet>();

		for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {		
			Node node = (Node) hitsIt.next();
			if (node.hasLabel(NodeLabel.Types.PUBLICATION)){
				result.addAll(ModelTraverser.getModelResultSetFromNode(node, hits.currentScore(),"PublicationIndex"));				
			}
		}
		return result;
	}
	
	private List<PublicationResultSet> retrievePublicationResults(){
		
		IndexHits<Node> hits = retrieveHits();
		if ((hits == null) || (hits.size() == 0)) {
			return new LinkedList<PublicationResultSet>();
		}
		List<PublicationResultSet> resultList = new LinkedList<PublicationResultSet>();
				
		for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {
			Node node = (Node) hitsIt.next();
			float score = hits.currentScore();
			//end loop if bestN or threshold are met
			if (node.hasLabel(NodeLabel.Types.PUBLICATION)){
				PublicationResultSet prs = new PublicationResultSet(score,
									(String)node.getProperty(Property.Publication.TITLE, ""),
									(String)node.getProperty(Property.Publication.JOURNAL, ""),
									(String)node.getProperty(Property.Publication.AFFILIATION, ""),
									(String)node.getProperty(Property.Publication.YEAR, ""));
				List<PersonWrapper> personList = new LinkedList<PersonWrapper>();
				List<Node> personNodes = ModelTraverser.getPersonFromPublication(node);
				for (Iterator<Node> personNodeIt = personNodes.iterator(); personNodeIt.hasNext();) {
					Node personNode = (Node) personNodeIt.next();
					if (personNode.hasLabel(NodeLabel.Types.PERSON)){
						PersonWrapper pw = new PersonWrapper((String)personNode.getProperty(Property.Person.GIVENNAME, ""),
						(String)personNode.getProperty(Property.Person.FAMILYNAME, ""),
						(String)personNode.getProperty(Property.Person.EMAIL, ""),
						(String)personNode.getProperty(Property.Person.ORGANIZATION, ""));
						personList.add(pw);
					}
				}
				prs.setAuthors(personList);
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
