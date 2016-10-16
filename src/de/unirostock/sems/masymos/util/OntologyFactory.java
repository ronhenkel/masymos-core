package de.unirostock.sems.masymos.util;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.UniqueFactory;

import de.unirostock.sems.masymos.database.Manager;

public class OntologyFactory {


	static UniqueFactory<Node> SBOFactory;   
	final static String indexSBO = "SBOOntology";

	static UniqueFactory<Node> KISAOFactory;   
	final static String indexKISAO = "KISAOOntology";

	static UniqueFactory<Node> GOFactory;
	final static String indexGO = "GOOntology";

	static UniqueFactory<Node> ChebiFactory;
	final static String indexChebi = "ChebiOntology";

	static Map<String, UniqueFactory<Node>> factoryMap = new HashMap<>();

	public static UniqueFactory<Node> getFactory(String ontologyName) {

		switch (ontologyName) {
		case indexSBO: return getSBOFactory();

		case indexGO: return getGOFactory();

		case indexKISAO: return getKISAOFactory();

		case indexChebi: return getChebiFactory();

		default: return getOrCreateFactoryByName(ontologyName);
		}

	}


	private static UniqueFactory<Node> getChebiFactory() {
		if (ChebiFactory == null){

			ChebiFactory = new UniqueFactory.UniqueNodeFactory( Manager.instance().getDatabase(), indexChebi )
			{
				@Override
				protected void initialize( Node created, Map<String, Object> properties )
				{
					created.setProperty( "id", properties.get( "id" ) );
					created.addLabel(Label.label(indexChebi));
				}
			};
		}

		return ChebiFactory;
	}


	private static UniqueFactory<Node> getSBOFactory() {

		if (SBOFactory == null){

			SBOFactory = new UniqueFactory.UniqueNodeFactory( Manager.instance().getDatabase(), indexSBO )
			{
				@Override
				protected void initialize( Node created, Map<String, Object> properties )
				{
					created.setProperty( "id", properties.get( "id" ) );
					created.addLabel(Label.label(indexSBO));
				}
			};
		}

		return SBOFactory;
	}



	private static UniqueFactory<Node> getGOFactory() {

		if (GOFactory == null){

			GOFactory = new UniqueFactory.UniqueNodeFactory( Manager.instance().getDatabase(), indexGO )
			{
				@Override
				protected void initialize( Node created, Map<String, Object> properties )
				{
					created.setProperty( "id", properties.get( "id" ) );
					created.addLabel(Label.label(indexGO));
				}
			};
		}


		return GOFactory;
	}


	private static UniqueFactory<Node> getKISAOFactory() {

		if (KISAOFactory == null){

			KISAOFactory = new UniqueFactory.UniqueNodeFactory( Manager.instance().getDatabase(), indexKISAO )
			{
				@Override
				protected void initialize( Node created, Map<String, Object> properties )
				{
					created.setProperty( "id", properties.get( "id" ) );
					created.addLabel(Label.label(indexKISAO));
				}
			};
		}

		return KISAOFactory;
	}

	private static UniqueFactory<Node> getOrCreateFactoryByName( String ontologyName ) {
		
		if( ontologyName == null || ontologyName.isEmpty() )
			throw new IllegalArgumentException("Ontology is not allowed to be empty or null.");
		
		// clean up name
		ontologyName = ontologyName.replaceAll("[^a-zA-Z0-9_]", "-");
		
		// check if factory was previously created
		if( factoryMap.containsKey(ontologyName) )
			return factoryMap.get(ontologyName);
		else {
			// creates new factory with ontology as name
			UniqueFactory<Node> factory = new DynamicUniqueNodeFactory( Manager.instance().getDatabase(), ontologyName );
			factoryMap.put(ontologyName, factory);
			return factory;
		}
		
	}

	/**
	 * UniqueNodeFactory, storing the index name.
	 * To generate dynamic factories for prior unknown ontologies
	 * 
	 * @author martin
	 *
	 */
	public static class DynamicUniqueNodeFactory extends UniqueFactory.UniqueNodeFactory {
		
		protected String indexName = null;
		
		public DynamicUniqueNodeFactory(GraphDatabaseService graphdb, String index) {
			super(graphdb, index);
			this.indexName = index;
		}
		
		public DynamicUniqueNodeFactory( Index<Node> index ) {
            super( index );
            this.indexName = index.getName();
        }

		@Override
		protected void initialize(Node created, Map<String, Object> properties) {
			if( properties.containsKey("id") )
				created.setProperty( "id", properties.get( "id" ) );
			
			created.addLabel( Label.label(this.indexName) );
		}
		
	}


}
