package de.unirostock.sems.masymos.annotation;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.miriam.lib.MiriamLink;


public class ResolveThread extends Thread {

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
		String oldUri = uri;
		try {
			MiriamLink link = new MiriamLink();
			link.setAddress("http://www.ebi.ac.uk/miriamws/main/MiriamWebServices");
			if (StringUtils.startsWith(uri, "http")) {
				uri = link.convertURL(uri);
				System.out.println("Identifier.org URL " + oldUri + " mapped to Miriam URN " + uri);
			}
			
			res = link.getLocations(uri); 
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if ((res == null) || (res.length == 0)) {
			System.out.println("Miriam request #" + number +" returned no results for " + uri);
			return;
		}
		System.out.println("Miriam request #" + number +" returned " + res.length + "results for " + uri);
		
		for (int i = 0; i < res.length; i++) {
			//TODO this oldURI is a hack until Identifiers.org provides a proper interface
			AnnotationResolverUtil.instance().addToUrlThreadPool(oldUri, res[i]);
		}
		
		System.out.println("Miriam request #" + number +" finished");

	}

}
