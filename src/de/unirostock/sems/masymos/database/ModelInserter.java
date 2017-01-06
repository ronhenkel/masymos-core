package de.unirostock.sems.masymos.database;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unirostock.sems.masymos.annotation.AnnotationResolverUtil;
import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.configuration.Relation;
import de.unirostock.sems.masymos.extractor.Extractor;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class ModelInserter {
	
	final static Logger logger = LoggerFactory.getLogger(ModelInserter.class);

	private static GraphDatabaseService graphDB = Manager.instance().getDatabase();

	public static Long addModelVersion(String fileID, String versionId,
			Map<String, List<String>> predVersionIdMap, URL XMLdoc,
			String meta, String modelType) {
		Long uID = IdFactory.instance().getID();
		
		Node documentNode = null;
		try {
			documentNode = Extractor.extractStoreIndex(XMLdoc, modelType, versionId, uID);
		} catch (Exception e) {
			logger.error(e.getMessage());
			documentNode = null;
		}
		
		try ( Transaction tx = graphDB.beginTx() ) {
			if( documentNode == null ) {
				documentNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
				documentNode.addLabel(NodeLabel.Types.DOCUMENT);
			}
			
			Map<String, String> propertyMap = new HashMap<String, String>();
	
			propertyMap.put(Property.General.META, meta);
			propertyMap.put(Property.General.XMLDOC, XMLdoc.toString());
			propertyMap.put(Property.General.URI, XMLdoc.toString());
			propertyMap.put(Property.General.FILEID, fileID);
			Extractor.setExternalDocumentInformation(documentNode, propertyMap);
			Extractor.setDocumentUID(documentNode, uID);
			
			tx.success();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
			
		
		if (documentNode == null)
			return Long.MIN_VALUE;

		if ((predVersionIdMap != null) && !predVersionIdMap.isEmpty()) {

			for (Iterator<String> fidIterator = predVersionIdMap.keySet()
					.iterator(); fidIterator.hasNext();) {
				String fid = (String) fidIterator.next();
				for (Iterator<String> vIdIterator = predVersionIdMap.get(fid).iterator(); vIdIterator.hasNext();) {
					String vid = (String) vIdIterator.next();
					Node predNode = ModelLookup.getDocumentVersionNode(fid, vid);
					try (Transaction tx = graphDB.beginTx()) {
						predNode.createRelationshipTo(documentNode, Relation.DatabaseRelTypes.HAS_SUCCESSOR);
						documentNode.createRelationshipTo(predNode, Relation.DatabaseRelTypes.HAS_PREDECESSOR);
						tx.success();
					} catch (Exception e) {
						logger.error(e.getMessage());
						// TODO: handle exception
					}
				}
			}

		}
		return uID;
	}
	
	
	public static Long addModel(String fileID, URL url, String modelType) {
		Long uID = IdFactory.instance().getID();
		modelType = StringUtils.upperCase(modelType);
		
		Node documentNode = null;
		try {
			documentNode = Extractor.extractStoreIndex(url, modelType, uID);
		} catch (Exception e) {
			logger.error(e.getMessage());
			documentNode = null;
		}
		
		try ( Transaction tx = graphDB.beginTx() ) {
			if( documentNode == null ) {
				documentNode = IdFactory.instance().addToNodeDeleteIndex(graphDB.createNode(), uID);
				documentNode.addLabel(NodeLabel.Types.DOCUMENT);
			}
			
			Map<String, String> propertyMap = new HashMap<String, String>();
	
			propertyMap.put(Property.General.URI, url.toString());
			propertyMap.put(Property.General.FILEID, fileID);
			Extractor.setExternalDocumentInformation(documentNode, propertyMap);
			Extractor.setDocumentUID(documentNode, uID);
			
			tx.success();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Long.MIN_VALUE;
		}
		
		return uID;
	}
	
	public static Boolean buildIndex(Boolean dropExistingIndex){
		AnnotationResolverUtil.instance().setIndexLocked(true);
		if (dropExistingIndex) {
			try(Transaction ignore = graphDB.beginTx()){
				Index<Node> annotationIndex = Manager.instance().getAnnotationIndex();
				annotationIndex.delete();
				//annotationIndex = Manager.instance().getDatabase().index().forNodes("annotationIndex", MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "analyzer", AnnotationIndexAnalyzer.class.getName()));
				//((LuceneIndex<Node>) annotationIndex).setCacheCapacity( Property.General.URI, 30000 );
				ignore.success();
			}
			Manager.instance().createAnnotationIndex();
			
		}
		AnnotationResolverUtil.instance().setIndexLocked(false);
		AnnotationResolverUtil.instance().fillAnnotationFullTextIndex();

		return !AnnotationResolverUtil.instance().isIndexLocked();
	}

}
