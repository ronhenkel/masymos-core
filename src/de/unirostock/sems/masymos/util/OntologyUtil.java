package de.unirostock.sems.masymos.util;



import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import de.unirostock.sems.masymos.database.Manager;
/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class OntologyUtil {
	
	static GraphDatabaseService graphDB =  Manager.instance().getDatabase() ;
	
	public static Node getNodeById(String ontologyPrefix, String id) {
		Result result;
		Node n = null;
		try(Transaction tx = Manager.instance().getDatabase().beginTx()){
			result = graphDB.execute("match (s1:"+ ontologyPrefix +"Ontology)"+
					"where (s1.id='"+id+"')" +
					"return s1 limit 1");
			ResourceIterator<Node> iLca = result.columnAs("s1");
			n = iLca.next();
			iLca.close();
			tx.success();
		}
		
		return n;
	}
	
	
	public static Integer getShortestPathLength(Node n1, Node n2){
		int length = -1;
		try(Transaction tx = Manager.instance().getDatabase().beginTx()){
		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forTypeAndDirection( RelationshipType.withName("isA"), Direction.BOTH), 15 );
	        Path path = finder.findSinglePath( n1, n2 );
	        length = path.length();
		 tx.success();
		}    
		return length;
	}
	
	public static Integer getDepth(Node concept, Node root){
		Integer length = new Integer(-1);
		try(Transaction tx = Manager.instance().getDatabase().beginTx()){
		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forTypeAndDirection( RelationshipType.withName("isA"), Direction.OUTGOING), 15 );
	        Path path = finder.findSinglePath(concept, root);
	        if (path!=null) length = new Integer(path.length());
		 tx.success();
		}    
		return length;
	}
	
	public static double getSimilarity(String ontologyPrefix, Node concept1, Node concept2){
		double sim = -1;
		int sp = getShortestPathLength(concept1, concept2);
		float alpha = 0.2f;
		float beta = 0.6f;
		Node root = getRoot(ontologyPrefix);
		int lcaDepth = getDepth(getLca(ontologyPrefix, concept1, concept2), root);
		
		sim = Math.exp(-1d * alpha * sp) * //f1(l)
			(Math.exp(beta * lcaDepth) - Math.exp(-1d * beta * lcaDepth)) / //f2(h) zaehler
			(Math.exp(beta * lcaDepth) + Math.exp(-1d * beta * lcaDepth));  //f2(h) nenner
		
		return sim;
	}
	
	public static Node getLca(String ontologyPrefix, Node concept1, Node concept2) {
		Node lca = null;
		Result result;
		try(Transaction tx = Manager.instance().getDatabase().beginTx()){
			String idC1 = (String) concept1.getProperty("id");
			String idC2 = (String) concept2.getProperty("id");
			
			result = graphDB.execute("match (s1:"+ ontologyPrefix +"Ontology),(s2:"+ ontologyPrefix +"Ontology) " +
					"where (s1.id='"+idC1+"') AND (s2.id='"+idC2+"')" +
					"with s1 as c1, s2 as c2 match c1-[:isA*]->lcs<-[:isA*]-c2  return lcs limit 1");
			ResourceIterator<Node> iLca = result.columnAs("lcs");
			lca = iLca.next();
			iLca.close();
			tx.success();
		}
		return lca;
	}
	
	public static Node getRoot(String ontologyPrefix){
		Node root = null;
		Result result;
		try(Transaction tx = Manager.instance().getDatabase().beginTx()){
			result = graphDB.execute("match (o:" + ontologyPrefix + "Ontology) where (o.id='owl:" + ontologyPrefix + "Ontology') return o");
			ResourceIterator<Node> iRoot = result.columnAs("o");
			root = iRoot.next();
			iRoot.close();
		}
		return root;
	}

}
