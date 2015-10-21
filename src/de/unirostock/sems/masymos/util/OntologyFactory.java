package de.unirostock.sems.masymos.util;

import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
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
    
	static UniqueFactory<Node> UnnamedFactory;   
    
    public static UniqueFactory<Node> getFactory(String ontologyName) {
    	
    	switch (ontologyName) {
		case indexSBO: return getSBOFactory();
			
		case indexGO: return getGOFactory();
		
		case indexKISAO: return getKISAOFactory();
		
		case indexChebi: return getChebiFactory();
			
		default: return getDefaultFactory();
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
    	           created.addLabel(DynamicLabel.label(indexChebi));
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
    	           created.addLabel(DynamicLabel.label(indexSBO));
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
    	           created.addLabel(DynamicLabel.label(indexGO));
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
    	           created.addLabel(DynamicLabel.label(indexKISAO));
    	        }
    	    };
    	}
    	
    	return KISAOFactory;
	}
	
	
	   private static UniqueFactory<Node> getDefaultFactory() {
					  
	    	if (UnnamedFactory == null){
	    		final  String indexUnnamed = Long.toString(System.currentTimeMillis());
	    		UnnamedFactory = new UniqueFactory.UniqueNodeFactory( Manager.instance().getDatabase(), indexUnnamed )
	    	    {
	    	        @Override
	    	        protected void initialize( Node created, Map<String, Object> properties )
	    	        {
	    	            created.setProperty( "id", properties.get( "id" ) );
	    	           created.addLabel(DynamicLabel.label(indexUnnamed));
	    	        }
	    	    };
	    	}
	    	
	    	return UnnamedFactory;
		}

    


}
