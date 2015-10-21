package de.unirostock.sems.masymos.database;


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
	private ReadableIndex<Node> autoNodeIndex = null;
	
	private Map<String, ReadableIndex<?>> indexMap = null; 

	private Manager(){
		if (Config.instance().isWebSeverInstance()){
			initializeManager(Config.instance().getDb());			
		} else {
			initializeManager(Config.instance().getDbPath(), Config.instance().isEmbedded());	
		}
		initializeIndex();
	}
	
	private void initializeManager(String path, Boolean isEmbedded) {
		//if (isEmbedded) {
			graphDb = new GraphDatabaseFactory().
				    newEmbeddedDatabaseBuilder( path ).
				    //setConfig( GraphDatabaseSettings.node_keys_indexable, Property.General.NODETYPE).
				    //setConfig( GraphDatabaseSettings.node_auto_indexing, "true" ).			    
				    newGraphDatabase();	
//		} else {	 
//			
//			Map<String, String> conf = new HashMap<String, String>();
//			conf.put("ha.cluster_server", ":5001-5003");
//			conf.put("ha.initial_hosts", ":5001,:5002,:5003");
//			conf.put("online_backup_enabled", "false");
//			
//	
//			graphDb = new HighlyAvailableGraphDatabaseFactory().
//				newHighlyAvailableDatabaseBuilder(path).
//				setConfig( GraphDatabaseSettings.node_keys_indexable, Property.General.NODETYPE).
//			    setConfig( GraphDatabaseSettings.node_auto_indexing, "true" ).
//			    setConfig( HaSettings.server_id, "2" ).
//			    setConfig( HaSettings.ha_server, ":6002").			    
//			    setConfig( conf ).
//			    newGraphDatabase();
//		    		    
//		}
		registerShutdownHook(graphDb);		
		
	}
	
	

	private void initializeManager(GraphDatabaseService serverDB) {		
		graphDb = serverDB;
		//set auto-index properties
		//...think about it...
	}
	
	private void initializeIndex() {
				
		try (Transaction tx = graphDb.beginTx())
		{

			modelIndex = graphDb.index().forNodes("modelIndex", MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "analyzer", ModelIndexAnalyzer.class.getName()));
			constituentIndex = graphDb.index().forNodes("constituentIndex", MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "analyzer", ConstituentIndexAnalyzer.class.getName()));
			relationshipIndex = graphDb.index().forRelationships("relationshipIndex");
			annotationIndex = graphDb.index().forNodes("annotationIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", AnnotationIndexAnalyzer.class.getName()));	
			publicationIndex = graphDb.index().forNodes("publicationIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", PublicationIndexAnalyzer.class.getName()));
			personIndex = graphDb.index().forNodes("personIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", PersonIndexAnalyzer.class.getName()));
			sedmlIndex = graphDb.index().forNodes("sedmlIndex", MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "analyzer", SedmlndexAnalyzer.class.getName()));
			autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
			
			//((LuceneIndex<Node>) modelIndex).setCacheCapacity( Property.General.NAME, 3000 );
			//((LuceneIndex<Node>) modelIndex).setCacheCapacity( Property.General.ID, 3000 );
			//((LuceneIndex<Node>) annotationIndex).setCacheCapacity( Property.General.URI, 30000 );
			//((LuceneIndex<Node>) personIndex).setCacheCapacity( Property.Person.FAMILYNAME, 1000 );
			//((LuceneIndex<Node>) personIndex).setCacheCapacity( Property.Person.GIVENNAME, 1000 );
			//((LuceneIndex<Node>) publicationIndex).setCacheCapacity( Property.Publication.ABSTRACT, 1000 );
			//((LuceneIndex<Node>) publicationIndex).setCacheCapacity( Property.Publication.TITLE, 1000 );
			
			tx.success();
		}
		
		indexMap = new HashMap<String, ReadableIndex<?>>();
		indexMap.put("modelIndex", modelIndex);
		indexMap.put("constituentIndex", constituentIndex);
		indexMap.put("relationshipIndex", publicationIndex);
		indexMap.put("annotationIndex", annotationIndex);
		indexMap.put("publicationIndex", publicationIndex);
		indexMap.put("personIndex", personIndex);
		indexMap.put("sedmlIndex", sedmlIndex);
		indexMap.put("autoNodeIndex", autoNodeIndex);
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

/*	
	public ExecutionEngine  getExecutionEngine(){
		//lazy initialization
		if (this.engine==null) this.engine = new ExecutionEngine(graphDb);
		return engine;
	}
*/	

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

	public ReadableIndex<Node> getAutoNodeIndex() {
		return autoNodeIndex;
	}


	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	public Map<String, ReadableIndex<?>> getIndexMap() {
		return indexMap;
	}
	
	public Transaction createNewTransaction() {
		return graphDb.beginTx();
	}

}
