package de.unirostock.sems.masymos.util;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

/**
 * Normalize and shrink text blocks before committing to Lucene
 *
 * Copyright 2016 Ron Henkel
 * @author ronhenkel
 */
public class IndexText {

	private static int max = 32766;
	
	public static String shrinkTextToLuceneTermLength(String text) throws Exception{
		if (!isTextToLong(text)) return text;
		
		String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
		String shortenedText = normalizedText.replaceAll("[^\\x00-\\x7F]", "");

		//if (!isTextToLong(shortenedText)) return shortenedText;
		int subtractor = 10000;
		while (isTextToLong(shortenedText)) {
			String nonSubtracted = shortenedText;
			shortenedText = StringUtils.left(shortenedText, shortenedText.length() - subtractor);
			if (StringUtils.length(nonSubtracted)<=StringUtils.length(shortenedText)) subtractor = subtractor / 2;
		}
		
		return shortenedText;
	}
	
	public static boolean isTextToLong(String text) throws UnsupportedEncodingException{
		if (StringUtils.isBlank(text)) return false;
		
		final byte[] utf8Bytes = text.getBytes("UTF-8");
		return (utf8Bytes.length > (max - 1));
	}
	
	
	public static String expandTermsSpecialChars(String in){
		if ((StringUtils.length(in)<5) || !StringUtils.containsAny(in, '_', ':', '.', '-')) return in;
		
		LinkedList<String> termList = new LinkedList<String>();
		String[] terms = StringUtils.split(in);
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i];
			termList.add(term);
			if (!StringUtils.containsAny(term, '_', ':', '.', '-')) continue;
			
			String[] uSplit = StringUtils.splitByCharacterType(StringUtils.lowerCase(term));
			termList.addAll(java.util.Arrays.asList(uSplit));
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(" ");
		for (Iterator<String> iterator = termList.iterator(); iterator.hasNext();) {
			String term = (String) iterator.next();
			if ((StringUtils.length(term) < 2) || StringUtils.isBlank(term)  || StringUtils.containsAny(term, '_', ':', '.', '-')) continue;
			sb.append(term);
			sb.append(" ");
		}
		
		return sb.toString();
	}

}
