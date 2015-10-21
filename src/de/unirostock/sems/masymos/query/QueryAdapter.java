package de.unirostock.sems.masymos.query;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Transaction;

import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.query.results.AnnotationResultSet;
import de.unirostock.sems.masymos.query.results.ModelResultSet;
import de.unirostock.sems.masymos.query.results.PersonResultSet;
import de.unirostock.sems.masymos.query.results.PublicationResultSet;
import de.unirostock.sems.masymos.query.results.SedmlResultSet;
import de.unirostock.sems.masymos.query.types.AnnotationQuery;
import de.unirostock.sems.masymos.query.types.PersonQuery;
import de.unirostock.sems.masymos.query.types.PublicationQuery;
import de.unirostock.sems.masymos.query.types.SedmlQuery;

public class QueryAdapter {

	
	public static List<ModelResultSet> executeSingleQueryForModels(IQueryInterface iq){
		Transaction tx = Manager.instance().createNewTransaction();
		try
		{
			return iq.getModelResults();
			
		} 
		finally
		{
			tx.success();
		}
		
		
	}
	
	public static List<AnnotationResultSet> executeAnnotationQuery(AnnotationQuery aq){
		Transaction tx = Manager.instance().createNewTransaction();
		try
		{
			return aq.getResults();
			
		} 
		finally
		{
			tx.success();
		}
		
		
	}
	
	public static List<PersonResultSet> executePersonQuery(PersonQuery persq){
		Transaction tx = Manager.instance().createNewTransaction();
		try
		{
			return persq.getResults();
			
		} 
		finally
		{
			tx.success();
		}
		
		
	}
	
	public static List<PublicationResultSet> executePublicationQuery(PublicationQuery pubq){
		Transaction tx = Manager.instance().createNewTransaction();
		try
		{
			return pubq.getResults();
			
		} 
		finally
		{
			tx.success();

		}
		
		
	}
	
	public static List<ModelResultSet> executeMultipleQueriesForModels(List<IQueryInterface> iqList){
		List<ModelResultSet> rs = new LinkedList<ModelResultSet>();
		for (Iterator<IQueryInterface> iqIt = iqList.iterator(); iqIt.hasNext();) {
			IQueryInterface interfaceQuery = (IQueryInterface) iqIt.next();	
			Transaction tx = Manager.instance().createNewTransaction();
			try
			{
				rs.addAll(interfaceQuery.getModelResults());
				
			} 
			finally
			{
				tx.success();

			}
			
		}
		return rs;
	}
	
	public static List<SedmlResultSet> executeSedmlQuery(SedmlQuery sedq){
		Transaction tx = Manager.instance().createNewTransaction();
		try
		{
			return sedq.getResults();
			
		} 
		finally
		{
			tx.success();

		}
		
		
	}
	
	public static List<ModelResultSet> executeSedmlQueryForModels(SedmlQuery sedq){
		Transaction tx = Manager.instance().createNewTransaction();
		try
		{
			return sedq.getModelResults();
			
		} 
		finally
		{
			tx.success();

		}
		
		
	}

/*
	private static Analyzer selectAnalyzerByEnum(IndexEnumerator e) {
		switch (e) {
		case MODELINDEX:
			return NodeFullTextIndexAnalyzer.getNodeFullTextIndexAnalyzer();
		case ANNOTATIONINDEX:
			return AnnotationIndexAnalyzer.getAnnotationIndexAnalyzer();
		case PUBLICATIONINDEX:
			return PublicationFullTextIndexAnalyzer.getPublicationFullTextIndexAnalyzer();
		case PERSONINDEX:
			return PersonExactIndexAnalyzer.getPersonExactIndexAnalyzer();	
		default:
			return null;
		}
	}

	private static Index<Node> selectIndexByEnum(IndexEnumerator e){
		switch (e) {
		case MODELINDEX:
			return nodeIndex;
		case ANNOTATIONINDEX:
			return annotationFull;
		case PUBLICATIONINDEX:
			return publicationFull;
		case PERSONINDEX:
			return personExact;	
		default:
			return null;
		}

	}

*/	
}
