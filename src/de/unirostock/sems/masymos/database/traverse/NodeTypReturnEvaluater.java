package de.unirostock.sems.masymos.database.traverse;



import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class NodeTypReturnEvaluater implements Evaluator{
	private Label nodeType;
	
	public NodeTypReturnEvaluater(Label nodeType){
		this.nodeType = nodeType;
	}
	
//	@Override
//	public boolean isReturnableNode(TraversalPosition pos) {
//		Node node = pos.currentNode();
//		if (node.hasProperty(Property.General.NODE_TYPE) && StringUtils.equals((String) node.getProperty(Property.General.NODE_TYPE), nodeType)){	
//		  return true;
//		} else return false;
//	}

	@Override
	public Evaluation evaluate(Path path) {
		Node node = path.endNode();
		if (node.hasLabel(nodeType)){
			return Evaluation.INCLUDE_AND_PRUNE;
		} else return Evaluation.EXCLUDE_AND_CONTINUE;	
	}


	
}
