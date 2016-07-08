package de.unirostock.sems.masymos.query;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.query.results.AnnotationResultSet;
import de.unirostock.sems.masymos.query.results.VersionResultSet;
import de.unirostock.sems.masymos.query.results.PersonResultSet;
import de.unirostock.sems.masymos.query.results.PublicationResultSet;
import de.unirostock.sems.masymos.query.results.SedmlResultSet;
import de.unirostock.sems.masymos.query.types.AnnotationQuery;
import de.unirostock.sems.masymos.query.types.PersonQuery;
import de.unirostock.sems.masymos.query.types.PublicationQuery;
import de.unirostock.sems.masymos.query.types.SedmlQuery;
import de.unirostock.sems.masymos.util.ResultSetUtil;

public class QueryAdapter {
	
	private static GraphDatabaseService graphDB = Manager.instance().getDatabase();

	
	public static List<VersionResultSet> executeSingleQueryForModels(IQueryInterface iq){
		List<VersionResultSet> mrs = new LinkedList<VersionResultSet>();
		try (Transaction tx = graphDB.beginTx())
		{
			mrs = iq.getModelResults();
			tx.success();
		} 
		 return mrs;
		
	}
	
	public static List<AnnotationResultSet> executeAnnotationQuery(AnnotationQuery aq){
		List<AnnotationResultSet> ars = new LinkedList<AnnotationResultSet>();
		try (Transaction tx = graphDB.beginTx())
		{
			ars =  aq.getResults();
			tx.success();
		} 
		return ars;
		
		
	}
	
	public static List<PersonResultSet> executePersonQuery(PersonQuery persq){
		List<PersonResultSet> prs = new LinkedList<PersonResultSet>();
		try (Transaction tx = graphDB.beginTx())
		{
			prs =  persq.getResults();
			tx.success();
		} 
		
		return prs;
		
	}
	
	public static List<PublicationResultSet> executePublicationQuery(PublicationQuery pubq){
		List<PublicationResultSet> prs = new LinkedList<PublicationResultSet>();
		try (Transaction tx = graphDB.beginTx())
		{
			prs = pubq.getResults();
			tx.success();
			
		} 
		return prs;
		
	}
	
	public static List<VersionResultSet> executeMultipleQueriesForModels(List<IQueryInterface> iqList){
		List<VersionResultSet> rs = new LinkedList<VersionResultSet>();
		for (Iterator<IQueryInterface> iqIt = iqList.iterator(); iqIt.hasNext();) {
			IQueryInterface interfaceQuery = (IQueryInterface) iqIt.next();	
			try (Transaction tx = graphDB.beginTx())
			{
				rs.addAll(interfaceQuery.getModelResults());
				tx.success();
			} 
		}
		rs = ResultSetUtil.sortModelResultSetByScore(rs);
		return rs;
	}
	
	public static List<SedmlResultSet> executeSedmlQuery(SedmlQuery sedq){
		List<SedmlResultSet> srs = new LinkedList<SedmlResultSet>();
		try (Transaction tx = graphDB.beginTx())
		{
			srs =  sedq.getResults();
			tx.success();
		}
		return srs;
		
	}
	
	public static List<VersionResultSet> executeSedmlQueryForModels(SedmlQuery sedq){
		List<VersionResultSet> mrs = new LinkedList<VersionResultSet>();
		try (Transaction tx = graphDB.beginTx())
		{
			mrs = sedq.getModelResults();
			tx.success();
		} 
		return mrs;				
	}

}
