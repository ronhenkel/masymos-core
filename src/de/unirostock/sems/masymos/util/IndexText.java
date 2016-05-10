package de.unirostock.sems.masymos.util;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;

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

}
