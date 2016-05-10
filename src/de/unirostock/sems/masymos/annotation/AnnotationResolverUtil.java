package de.unirostock.sems.masymos.annotation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import de.unirostock.sems.masymos.configuration.NodeLabel;
import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.database.traverse.DBModelTraverser;

public class AnnotationResolverUtil {
	
	private static AnnotationResolverUtil INSTANCE = null;
	
	private volatile Boolean indexLocked = false;
	
	private AtomicLong urlCount= null;
	private ExecutorService urlThreadPool = Executors.newFixedThreadPool(10);
	private ExecutorService uriThreadPool = Executors.newFixedThreadPool(5);
	
	public static synchronized AnnotationResolverUtil instance() {
		if (INSTANCE == null) {
			INSTANCE = new AnnotationResolverUtil();
		}
		return INSTANCE;
	}
	
	private AnnotationResolverUtil(){
		Manager.instance().getDatabase();
		urlCount= new AtomicLong(0);
	}
 
	private class WaitThreat extends Thread{
		
		@Override
		public void run() {
			try {
				sleep(5000);
			} catch (InterruptedException e) {
				
			}
		}
	}

	
	public void fillAnnotationFullTextIndex(){
		
		if (AnnotationResolverUtil.instance().isIndexLocked()){
			while (AnnotationResolverUtil.instance().isIndexLocked()){
				ExecutorService executor = Executors.newSingleThreadExecutor();
				WaitThreat wt = new WaitThreat();
				try {
					//Runtime.getRuntime().addShutdownHook(wt);
					executor.submit(wt).get();
				} catch (InterruptedException | ExecutionException e) {

				}
			}
		} else {
			AnnotationResolverUtil.instance().setIndexLocked(true);
		}
		
		System.out.println("Creating URI list from DB...");
	
		long uriCounter = 0;	
		String uri = "";
		//ReadableIndex<Node> autoNodeIndex = Manager.instance().getAutoNodeIndex();
		//IndexHits<Node> resourceNodeList = autoNodeIndex.get(Property.General.NODETYPE, Property.NodeType.RESOURCE);
		List<Node> nodes = DBModelTraverser.getAllNodesWithLabel(NodeLabel.Types.RESOURCE); 
		List<String> uriList = new LinkedList<String>();
		
		try (Transaction tx = Manager.instance().getDatabase().beginTx())
		{
			for (Iterator<Node> nodeIt = nodes.iterator(); nodeIt.hasNext();) {
				Node node = (Node) nodeIt.next();				
				if (!(Boolean)node.getProperty(Property.General.IS_INDEXED, false)) {	
					uriList.add((String)node.getProperty(Property.General.URI));						
				}
				
			}
			tx.success();
		}	
			
			for (Iterator<String> uriIt = uriList.iterator(); uriIt.hasNext();) {
				uri = (String) uriIt.next();
				ResolveThread rt = new ResolveThread(uri, ++uriCounter);
				rt.setName(uri);
				uriThreadPool.execute(rt);										
			}
			
			System.out.println("Started to resolve " + uriCounter + " URIs...");		
	
			uriThreadPool.shutdown();
			// Wait until all threads are finish
			while (!uriThreadPool.isTerminated()) {
	
			}
			

			uriThreadPool = Executors.newFixedThreadPool(5);
			//TODO implement logging!
			//TODO really, implement logging for this!!!
			
			System.out.println("done!");		
			
			System.out.println("Started to retrieve " + urlCount + " URLs...");
					
	
			urlThreadPool.shutdown();
			// Wait until all threads are finish
			while (!urlThreadPool.isTerminated()) {
		
			}
	
		urlThreadPool = Executors.newFixedThreadPool(10);
		System.out.println("done!");
		
		System.out.println("Annotation Index created.");
		AnnotationResolverUtil.instance().setIndexLocked(false);
	}
	
	
	public synchronized void  addToUrlThreadPool(String uri, String url){		
		FetchThread ft = new FetchThread(uri, url, urlCount.getAndIncrement());
		ft.setName(url);
		urlThreadPool.execute(ft);
	}

	public Boolean isIndexLocked() {
		return indexLocked;
	}

	public void setIndexLocked(Boolean indexLocked) {
		this.indexLocked = indexLocked;
	}


}
