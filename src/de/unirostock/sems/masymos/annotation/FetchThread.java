package de.unirostock.sems.masymos.annotation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import de.unirostock.sems.masymos.configuration.Property;
import de.unirostock.sems.masymos.database.Manager;

public class FetchThread extends Thread {

	private String url;
	private String uri;
	private long number;
	private GraphDatabaseService graphDB = Manager.instance().getDatabase();
	private Index<Node> annoFull = Manager.instance()
			.getAnnotationIndex();
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");
	private static final int timeout = 20 * 1000; // 20 seconds timeout

	public FetchThread(String uri, String url, long number) {
		super(url);
		this.url = url;
		this.uri = uri;
		this.number = number;
	}

	@Override
	public void run() {
		if (StringUtils.isEmpty(url))
			return;
		String text = null;

		InputStream stream = null;

		try {
			URL connection = new URL(url);

			URLConnection urlCon = connection.openConnection();
			// set some timeouts
			urlCon.setConnectTimeout(timeout);
			urlCon.setReadTimeout(timeout);

			stream = urlCon.getInputStream();
			text = IOUtils.toString(stream);
			Document doc = Jsoup.parse(text);
			text = doc.text();
		} catch (MalformedURLException mue) {
			System.out.println("Thread #" + number + ": Malformed URL: " + url);
			// do nothing
		} catch (IOException ioe) {
			// do nothing
			System.out.println("Thread #" + number
					+ ": I/O error or timeout when reading URL: " + url);
		} finally {
			IOUtils.closeQuietly(stream);
		}

		if (StringUtils.isEmpty(text)) return;

		

		try (Transaction tx = graphDB.beginTx()) 
		{
			Node resource = annoFull.get(Property.General.URI, uri).getSingle();
			annoFull.add(resource, Property.General.RESOURCETEXT, text);
			resource.setProperty(Property.General.IS_INDEXED, true);
			tx.success();

		} catch (NoSuchElementException e) {
			System.out.println("Thread #" + number + ": Thread " + url
					+ " FAILED!");
			System.out.println(e.getMessage());
		} finally {
		
			System.out.println("Thread #" + number + ": " + url
					+ " terminated at " + dateFormat.format(new Date()));
		}

		// try {
		//
		// BufferedWriter w = new BufferedWriter(new FileWriter("d:/temp/dump/"+
		// URLEncoder.encode(url, "UTF-8")+".txt"));
		// w.append(textBuffer.toString());
		// w.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

}
