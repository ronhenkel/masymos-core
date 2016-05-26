package de.unirostock.sems.masymos.database;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.index.lucene.ValueContext;

import de.unirostock.sems.masymos.configuration.Property;

public class IdFactory {

	private static IdFactory INSTANCE = null;
	private GraphDatabaseService  graphDb = null;
	private Index<Node> nodeDeleteIndex = null;
	private Index<Relationship> relationshipDeleteIndex = null;
	private final String queryString = "MERGE (id:GlobalUniqueId) ON CREATE SET id.count = 1 ON MATCH SET id.count = id.count + 1	RETURN id.count AS generated_id";
	
	public static synchronized IdFactory instance() {
		if (INSTANCE == null) {
			INSTANCE = new IdFactory();
		}
		return INSTANCE;
	}
	
	private IdFactory(){
		graphDb = Manager.instance().getDatabase();
		nodeDeleteIndex = Manager.instance().getNodeDeleteIndex();
		relationshipDeleteIndex = Manager.instance().getRelationshipDeleteIndex();
	}
	
	public Long getID(){
		Long uuID = Long.MIN_VALUE; 
		try ( Transaction tx = graphDb.beginTx() )
		{		
		    ResourceIterator<Long> resultIterator = graphDb.execute( queryString).columnAs( "generated_id" );
		    uuID = resultIterator.next();
		    resultIterator.close();
		    tx.success();
		}
		return uuID;
	}
	
	public Node addToNodeDeleteIndex(Node n, Long uID){
		try ( Transaction tx = graphDb.beginTx() )
		{
			nodeDeleteIndex.add(n, Property.General.UID, new ValueContext(uID).indexNumeric());
			tx.success();
		}
		return n;
	}
	
	public Relationship addToRelationshipDeleteIndex(Relationship r, Long uID){
		try ( Transaction tx = graphDb.beginTx() )
		{
			relationshipDeleteIndex.add(r, Property.General.UID, new ValueContext(uID).indexNumeric());
			tx.success();
		}
		return r;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
