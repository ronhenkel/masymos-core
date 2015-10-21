package de.unirostock.sems.masymos.database;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import de.unirostock.sems.masymos.analyzer.AnnotationIndexAnalyzer;
import de.unirostock.sems.masymos.annotation.AnnotationResolverUtil;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation;
import de.unirostock.sems.masymos.extractor.Extractor;

public class ModelInserter {

	static GraphDatabaseService graphDB = Manager.instance().getDatabase();

	public static Boolean addModel(String fileID, String versionId,
			Map<String, List<String>> predVersionIdMap, URL XMLdoc,
			String meta, String modelType) {

		//Node modelNode = null; 
		Node documentNode = null;
		try {
			documentNode = Extractor.extractStoreIndex(XMLdoc, modelType, versionId);

			Map<String, String> propertyMap = new HashMap<String, String>();

			propertyMap.put(Property.General.META, meta);
			propertyMap.put(Property.General.XMLDOC, XMLdoc.toString());
			propertyMap.put(Property.General.URI, XMLdoc.toString());
			propertyMap.put(Property.General.FILEID, fileID);
			Extractor.setExternalDocumentInformation(documentNode, propertyMap);
			
		} catch (Exception e) {
			documentNode = null;
		}
		if (documentNode == null)
			return false;

		if ((predVersionIdMap != null) && !predVersionIdMap.isEmpty()) {

			for (Iterator<String> fidIterator = predVersionIdMap.keySet()
					.iterator(); fidIterator.hasNext();) {
				String fid = (String) fidIterator.next();
				for (Iterator<String> vIdIterator = predVersionIdMap.get(fid).iterator(); vIdIterator.hasNext();) {
					String vid = (String) vIdIterator.next();
					Node predNode = ModelLookup.getModelVersionNode(fid, vid);
					try (Transaction tx = graphDB.beginTx()) {
						predNode.createRelationshipTo(documentNode, Relation.DatabaseRelTypes.HAS_SUCCESSOR);
						documentNode.createRelationshipTo(predNode, Relation.DatabaseRelTypes.HAS_PREDECESSOR);
						tx.success();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}

		}
		return (documentNode != null);
	}
	
	public static Boolean buildIndex(Boolean dropExistingIndex){
		AnnotationResolverUtil.instance().setIndexLocked(true);
		if (dropExistingIndex) {
			try(Transaction ignore = graphDB.beginTx()){
				Index<Node> annotationIndex = Manager.instance().getAnnotationIndex();
				annotationIndex.delete();
				annotationIndex = Manager.instance().getDatabase().index().forNodes("annotationIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", AnnotationIndexAnalyzer.class.getName()));
				//((LuceneIndex<Node>) annotationIndex).setCacheCapacity( Property.General.URI, 30000 );
				ignore.success();
			}
		}
		AnnotationResolverUtil.instance().setIndexLocked(false);
		AnnotationResolverUtil.instance().fillAnnotationFullTextIndex();

		return !AnnotationResolverUtil.instance().isIndexLocked();
	}

}
