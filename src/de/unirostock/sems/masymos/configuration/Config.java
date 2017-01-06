package de.unirostock.sems.masymos.configuration;

import org.neo4j.graphdb.GraphDatabaseService;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class Config {	

	private Config(){
		webSeverInstance = false;
	}
	
	private static Config INSTANCE = null;
		
	private String dbPath = null;
	private String cachePath = null;

	private GraphDatabaseService db = null;
	private Boolean embedded = false;
	private Boolean webSeverInstance = false;
	
	public static synchronized Config instance(){
		if (INSTANCE == null) {
			INSTANCE = new Config();
		}
		return INSTANCE;
		
	}

	public String getDbPath() {
		if (webSeverInstance) return null;
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		webSeverInstance = false;
		this.dbPath = dbPath;
	}

	public String getCachePath() {
		if (webSeverInstance) return null;
		return cachePath;
	}

	public void setCachePath(String cachePath) {
		webSeverInstance = false;
		this.cachePath = cachePath;
	}
	public GraphDatabaseService getDb() {
		if (!webSeverInstance) return null;
		return db;
	}

	public void setDb(GraphDatabaseService db) {
		webSeverInstance = true;
		this.db = db;
	}

	public Boolean isWebSeverInstance() {
		return webSeverInstance;
	}

	public Boolean isEmbedded() {
		return embedded;
	}

	public void setEmbedded(Boolean isEmbedded) {
		this.embedded = isEmbedded;
	}

	
}
