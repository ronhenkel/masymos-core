package de.unirostock.sems.masymos.database;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.helpers.collection.MapUtil;

import de.unirostock.sems.masymos.analyzer.AnnotationIndexAnalyzer;
import de.unirostock.sems.masymos.analyzer.ConstituentIndexAnalyzer;
import de.unirostock.sems.masymos.analyzer.ModelIndexAnalyzer;
import de.unirostock.sems.masymos.analyzer.PersonIndexAnalyzer;
import de.unirostock.sems.masymos.analyzer.PublicationIndexAnalyzer;
import de.unirostock.sems.masymos.analyzer.SedmlndexAnalyzer;
import de.unirostock.sems.masymos.configuration.Config;

public class Manager {

	private static Manager INSTANCE = null;
	private GraphDatabaseService  graphDb = null;
 
	private Index<Node> modelIndex = null;
	private Index<Node> annotationIndex = null;
	private Index<Relationship> relationshipIndex = null;
	private Index<Node> publicationIndex = null;
	private Index<Node> personIndex = null;
	private Index<Node> constituentIndex = null;
	private Index<Node> sedmlIndex = null;
//	private ReadableIndex<Node> autoNodeIndex = null;
	
	private Map<String, ReadableIndex<?>> nodeIndexMap = null; 
	private Map<String, ReadableIndex<?>> relationshipIndexMap = null;

	private Manager(){
		if (Config.instance().isWebSeverInstance()){
			initializeManager(Config.instance().getDb());			
		} else {
			initializeManager(Config.instance().getDbPath(), Config.instance().isEmbedded());	
		}
		initializeIndex();
	}
	
	private void initializeManager(String path, Boolean isEmbedded) {
		
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(path) );				   		
		registerShutdownHook(graphDb);		
		
	}
	
	

	private void initializeManager(GraphDatabaseService serverDB) {		
		graphDb = serverDB;
	}
	
	private void initializeIndex() {
				
		try (Transaction tx = graphDb.beginTx())
		{
			modelIndex = graphDb.index().forNodes("modelIndex", MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "analyzer", ModelIndexAnalyzer.class.getName()));
			constituentIndex = graphDb.index().forNodes("constituentIndex", MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "analyzer", ConstituentIndexAnalyzer.class.getName()));
			annotationIndex = graphDb.index().forNodes("annotationIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", AnnotationIndexAnalyzer.class.getName()));	
			publicationIndex = graphDb.index().forNodes("publicationIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", PublicationIndexAnalyzer.class.getName()));
			personIndex = graphDb.index().forNodes("personIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", PersonIndexAnalyzer.class.getName()));
			sedmlIndex = graphDb.index().forNodes("sedmlIndex", MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "analyzer", SedmlndexAnalyzer.class.getName()));
			
			relationshipIndex = graphDb.index().forRelationships("relationshipIndex");
			
			tx.success();
		}
		
		nodeIndexMap = new HashMap<String, ReadableIndex<?>>();
		nodeIndexMap.put("modelIndex", modelIndex);
		nodeIndexMap.put("constituentIndex", constituentIndex);
		nodeIndexMap.put("annotationIndex", annotationIndex);
		nodeIndexMap.put("publicationIndex", publicationIndex);
		nodeIndexMap.put("personIndex", personIndex);
		nodeIndexMap.put("sedmlIndex", sedmlIndex);

		relationshipIndexMap = new HashMap<String, ReadableIndex<?>>();
		relationshipIndexMap.put("relationshipIndex", relationshipIndex);
	}
	

	public static synchronized Manager instance() {
		if (INSTANCE == null) {
			INSTANCE = new Manager();
		}
		return INSTANCE;
	}
	
	public GraphDatabaseService  getDatabase(){
		return graphDb;
	}

	public Index<Node> getModelIndex() {
		return modelIndex;
	}


	public Index<Node> getAnnotationIndex() {
		return annotationIndex;
	}


	public Index<Relationship> getRelationshipIndex() {
		return relationshipIndex;
	}


	public Index<Node> getPublicationIndex() {
		return publicationIndex;
	}


	public Index<Node> getPersonIndex() {
		return personIndex;
	}


	public Index<Node> getConstituentIndex() {
		return constituentIndex;
	}
	
	public Index<Node> getSedmlIndex() {
		return sedmlIndex;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}

	public Map<String, ReadableIndex<?>> getIndexMap() {
		return nodeIndexMap;
	}

}
