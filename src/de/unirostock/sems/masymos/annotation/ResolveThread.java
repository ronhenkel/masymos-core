package de.unirostock.sems.masymos.annotation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.miriam.lib.MiriamLink;


/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class ResolveThread extends Thread {
	
	final Logger logger = LoggerFactory.getLogger(ResolveThread.class);

	private String uri;
	private long number;
	
	public ResolveThread(String uri, long number) {
		super(uri);
		this.uri = uri;
		this.number = number;
	}

	@Override
	public void run() {
		String[] res = {};
		String originalURI = uri;
		try {
			MiriamLink link = AnnotationResolverUtil.instance().getMiriamLink();
			
			if (StringUtils.startsWith(uri, "http")) {
				uri = link.convertURL(uri);
				logger.info("Miriam request #" + number + " Identifier.org URL " + originalURI + " mapped to Miriam URN " + uri);
			}
			if (StringUtils.isBlank(uri)) {
				uri = link.getMiriamURI(originalURI);
				logger.warn("Miriam request #" + number + " Retrieving equivalent for invalid " + originalURI +" --> " + uri);
			}
			res = link.getLocations(uri); 
		} catch (Exception e1) {
			logger.error(e1.getMessage());
		}
		if ((res == null) || (res.length == 0)) {
			logger.info("Miriam request #" + number +" returned no results for " + uri);
			return;
		}
		logger.info("Miriam request #" + number +" returned " + res.length + " results for " + uri);
		
		for (int i = 0; i < res.length; i++) {
			//TODO this oldURI is a hack until Identifiers.org provides a proper interface
			AnnotationResolverUtil.instance().addToUrlThreadPool(originalURI, res[i]);
		}
		
		logger.info("Miriam request #" + number +" finished");

	}

}
