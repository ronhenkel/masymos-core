package de.unirostock.sems.masymos.database;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.ValueContext;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation;
import de.unirostock.sems.masymos.database.traverse.DocumentTraverser;

public class ModelDeleter {
	
	private static GraphDatabaseService graphDB = Manager.instance().getDatabase();
	private static Index<Node> nodeDeleteIndex = Manager.instance().getNodeDeleteIndex();
	private static Index<Relationship> relationshipDeleteIndex = Manager.instance().getRelationshipDeleteIndex();
	
	
	private static List<Node> getNodesToBeDeleted(Long uID){
		List<Node> delList = new LinkedList<Node>();
		if (uID==null) return delList;
		
		try (Transaction tx = graphDB.beginTx()) {
			
			IndexHits<Node> hits = nodeDeleteIndex.get(Property.General.UID, new ValueContext(uID).indexNumeric());
			
			if ((hits == null) || (hits.size() == 0)) {
				return delList;
			}
			for (Iterator<Node> hitsIt = hits.iterator(); hitsIt.hasNext();) {		
				Node node = (Node) hitsIt.next();
				delList.add(node);
			}			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			delList.clear();
			return delList;
		}
		
		return delList;
	}
	
	private static List<Relationship> getRelationshipsToBeDeleted(Long uID){
		List<Relationship> delList = new LinkedList<Relationship>();
		if (uID==null) return delList;
		
		try (Transaction tx = graphDB.beginTx()) {
			
			IndexHits<Relationship> hits = relationshipDeleteIndex.get(Property.General.UID, new ValueContext(uID).indexNumeric());
			
			if ((hits == null) || (hits.size() == 0)) {
				return delList;
			}
			for (Iterator<Relationship> hitsIt = hits.iterator(); hitsIt.hasNext();) {		
				Relationship rel = (Relationship) hitsIt.next();
				delList.add(rel);
			}			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			delList.clear();
			return delList;
		}
		
		return delList;
	}
	
	public static Map<String, String> deleteDocument(String fileId, Long uID){
		HashMap<String, String> msg = new HashMap<String, String>();
		if (StringUtils.isBlank(fileId) || uID == null || uID < 0) {
			msg.put("Exception", "No parameter can be NULL or empty and uID can not be zero or less");
			return msg;
		}
		Node doc = DocumentTraverser.getDocumentByUID(uID);
		if (doc==null) {
			msg.put("document not found", "No document with UID " + uID.toString());
			return msg;
		}
		try (Transaction tx = graphDB.beginTx()){
			if (StringUtils.equals((String) doc.getProperty(Property.General.FILEID, ""), fileId)) {
				msg.put("fileId not found", "fileId " + fileId + " does not match for uID " + uID.toString() );
				return msg;
			}
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			msg.put("Error" , e.getMessage());
			return msg;
		}				
		return doDelete(uID);
	}
	
	
	public static Map<String, String> deleteDocument(String fileId, String versionId, Long uID){
		HashMap<String, String> msg = new HashMap<String, String>();
		if (StringUtils.isBlank(fileId) || StringUtils.isBlank(versionId) || uID == null || uID < 0) {
			msg.put("Exception", "No parameter can be NULL or empty and uID can not be zero or less");
			return msg;
		}
		Node doc = DocumentTraverser.getDocumentByUID(uID);
		if (doc==null) {
			msg.put("document not found", "No document with UID " + uID.toString());
			return msg;
		}
		
		try (Transaction tx = graphDB.beginTx()){
			if (StringUtils.equals((String) doc.getProperty(Property.General.FILEID, ""), fileId)) {
				msg.put("fileId not found", "fileId " + fileId + " does not match for uID " + uID.toString() );
				return msg;
			}
			if (StringUtils.equals((String) doc.getProperty(Property.General.VERSIONID, ""), versionId)) {
				msg.put("versionId not found", "versionId " + versionId + " does not match for uID " + uID.toString() + "and fileId " + fileId );
				return msg;
			}
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			msg.put("Error" , e.getMessage());
			return msg;
		}
		return doDelete(uID);		
	}

	public static Map<String, String> deleteDocument(Long uID){
		HashMap<String, String> msg = new HashMap<String, String>();
		if (uID==null || uID < 0) {
			msg.put("Exception", "UID can not be NULL or negative!");
			return msg;
		}
		
		Node doc = DocumentTraverser.getDocumentByUID(uID);
		if (doc==null) {
			msg.put("document not found", "No document with UID " + uID.toString());
			return msg;
		}
		return doDelete(uID);
	}	
		
	private static Map<String, String> doDelete(Long uID){	
		List<Node> nodesToBeDeleted = getNodesToBeDeleted(uID);
		List<Relationship> relToBeDeleted = getRelationshipsToBeDeleted(uID);
		Long relCount = 0L;
		Long nodeCount = 0L;
		try (Transaction tx = graphDB.beginTx()) {
			
			//remove all marked relationships from DB
			for (Iterator<Relationship> relationshipIterator = relToBeDeleted.iterator(); relationshipIterator.hasNext();) {
				Relationship rel = (Relationship) relationshipIterator.next();
				
				//remove from all relationship indices
				Map<String, Index<Relationship>> relIndexMap = Manager.instance().getRelationshipIndexMap();
				for (Iterator<Index<Relationship>> indexIterator = relIndexMap.values().iterator(); indexIterator.hasNext();) {
					Index<Relationship> idx = (Index<Relationship>) indexIterator.next();
					if (idx!=null) idx.remove(rel);
				}
				//and from the relationShipDeleteIndex
				relationshipDeleteIndex.remove(rel);
				
				rel.delete();
				relCount++;
			}
			
			//remove all marked nodes from DB
			for (Iterator<Node> nodeIterator = nodesToBeDeleted.iterator(); nodeIterator.hasNext();) {
				Node node = (Node) nodeIterator.next();
				
				if (!isNodePrepared(node)) continue;				
				
				//remove from all node indices
				Map<String, Index<Node>> nodeIndexMap = Manager.instance().getNodeIndexMap();
				for (Iterator<Index<Node>> indexIterator = nodeIndexMap.values().iterator(); indexIterator.hasNext();) {
					Index<Node> idx = (Index<Node>) indexIterator.next();
					if (idx!=null) idx.remove(node);
				}
				//and from the relationShipDeleteIndex
				nodeDeleteIndex.remove(node);
				
				node.delete();
				nodeCount++;
			}
			
			//tidy up resources
			String queryString = "MATCH (n:RESOURCE) where NOT ((n)--()) RETURN n AS res";
			for (Iterator<Node> iterator = graphDB.execute( queryString ).columnAs( "res" ); iterator.hasNext();) {
				Node node = (Node) iterator.next();
				Index<Node> idx  = Manager.instance().getAnnotationIndex();
				if (idx!=null) idx.remove(node);
				nodeDeleteIndex.remove(node);
				
				node.delete();
				nodeCount++;
			}
			
			//tidy up publications
			queryString = "MATCH (n:PUBLICATION) where NOT ((n)-->(:ANNOTATION)) RETURN n AS pub";
			for (Iterator<Node> iterator = graphDB.execute( queryString ).columnAs( "pub" ); iterator.hasNext();) {
				Node node = (Node) iterator.next();
				
				//if publication does not belong to any other annotation, remove it
				Iterable<Relationship> relIter = node.getRelationships();
				for (Iterator<Relationship> relIt = relIter.iterator(); relIt.hasNext();) {
					Relationship rel = (Relationship) relIt.next();
					rel.delete();
				}
				Index<Node> idx  = Manager.instance().getPublicationIndex();
				if (idx!=null) idx.remove(node);
				nodeDeleteIndex.remove(node);
				
				node.delete();
				nodeCount++;
			}
			
			//tidy up person
			queryString = "MATCH (n:PERSON) where NOT ((n)--()) RETURN n AS pers";
			for (Iterator<Node> iterator = graphDB.execute( queryString ).columnAs( "pers" ); iterator.hasNext();) {
				Node node = (Node) iterator.next();
				Index<Node> idx = Manager.instance().getPublicationIndex();
				if (idx!=null) idx.remove(node);
				nodeDeleteIndex.remove(node);
				
				node.delete();
				nodeCount++;
			}
			
			
			tx.success();
		} catch (Exception e){
			e.printStackTrace();
			Map<String, String> msg = new HashMap<String, String>();
			msg.put("Exception", e.getMessage());
			return msg;
		}
		
		Map<String, String> msg = new HashMap<String, String>();
		msg.put("successful", Boolean.TRUE.toString());
		msg.put("uID", uID.toString());
		msg.put("nodes deleted", nodeCount.toString());
		msg.put("relations deleted", relCount.toString());
		
		return msg; 
	}

	//returns false if node should not be deleted, otherwise it deletes relations and returns true
	private static Boolean isNodePrepared(Node node) {
		if (node.getDegree()==0) return true;
		Iterable<Label> labelIter = node.getLabels();
		for (Iterator<Label> labelIt = labelIter.iterator(); labelIt.hasNext();) {
			Label label = (Label) labelIt.next();
			String l = label.name();
			try {
			//a resource can be linked to multiple annotations, skip if other links exist
			//a resource can be linked to an ontology, cut that relations if necessary
			if (l.equals(NodeLabel.Types.RESOURCE.toString())){
				if (node.getRelationships(Relation.DatabaseRelTypes.BELONGS_TO, Direction.OUTGOING).iterator().hasNext()) return false;
				
				//if resource does not belong to any other annotation, remove possible links to ontologies
				Iterable<Relationship> relIter = node.getRelationships();
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//an annotation can be linked to persons or publications that might be kept
			if (l.equals(NodeLabel.Types.ANNOTATION.toString())){
								
				Iterable<Relationship> relIter = node.getRelationships();
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//a document can have a pred/successor
			if (l.equals(NodeLabel.Types.DOCUMENT.toString())){
				
				List<Node> preds = new LinkedList<Node>();
				List<Node> succs = new LinkedList<Node>();

				//collect all predecessors
				Iterable<Relationship> relIter = node.getRelationships(Direction.INCOMING, Relation.DatabaseRelTypes.HAS_SUCCESSOR);
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship predRel = (Relationship) iterator.next();
					preds.add(predRel.getStartNode());
				}
				//collect all successors
				relIter = node.getRelationships(Direction.OUTGOING, Relation.DatabaseRelTypes.HAS_SUCCESSOR);
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship succRel = (Relationship) iterator.next();
					succs.add(succRel.getEndNode());
				}
				
				if (preds.isEmpty() || succs.isEmpty()) return true;
				
				//reconnect pred and successor around the to be deleted node
				for (Iterator<Node> predIt = preds.iterator(); predIt.hasNext();) {
					Node pred = (Node) predIt.next();
					for (Iterator<Node> succIt = succs.iterator(); succIt.hasNext();) {
						Node succ = (Node) succIt.next();
						pred.createRelationshipTo(succ, Relation.DatabaseRelTypes.HAS_SUCCESSOR);
						succ.createRelationshipTo(pred, Relation.DatabaseRelTypes.HAS_PREDECESSOR);
					}
				}
				//delete pred and successor for the to be deleted node
				relIter = node.getRelationships(Relation.DatabaseRelTypes.HAS_SUCCESSOR, Relation.DatabaseRelTypes.HAS_PREDECESSOR);
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//models can be referenced in Simulations
			if (l.equals(NodeLabel.Types.SBML_MODEL.toString())){
				Iterable<Relationship> relIter = node.getRelationships(Relation.SimulationLinkRelTypes.LINKS_TO_MODEL, Relation.SimulationLinkRelTypes.LINKS_TO_SIMULATION);
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//models can be referenced in Simulations
			if (l.equals(NodeLabel.Types.CELLML_MODEL.toString())){
				Iterable<Relationship> relIter = node.getRelationships(Relation.SimulationLinkRelTypes.LINKS_TO_MODEL, Relation.SimulationLinkRelTypes.LINKS_TO_SIMULATION);
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//a modelreference can be linked to a model
			if (l.equals(NodeLabel.Types.SEDML_MODELREFERENCE.toString())){
				Iterable<Relationship> relIter = node.getRelationships(Relation.SimulationLinkRelTypes.LINKS_TO_MODEL, Relation.SimulationLinkRelTypes.LINKS_TO_SIMULATION);
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//species can be observed by SEDML Variables, just delete
			if (l.equals(NodeLabel.Types.SBML_SPECIES.toString())){
				Iterable<Relationship> relIter = node.getRelationships();
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//cellml variables can be observed by SEDML Variables, just delete
			if (l.equals(NodeLabel.Types.CELLML_VARIABLE.toString())){
				Iterable<Relationship> relIter = node.getRelationships();
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			//SEDML Variables can observe species, just delete
			if (l.equals(NodeLabel.Types.SEDML_VARIABLE.toString())){
				Iterable<Relationship> relIter = node.getRelationships();
				for (Iterator<Relationship> iterator = relIter.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					rel.delete();
				}
			}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		//looks like this node passed all tests, it can be deleted
		return true;
	}
	
	
}
