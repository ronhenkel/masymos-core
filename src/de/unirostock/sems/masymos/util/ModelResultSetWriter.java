package de.unirostock.sems.masymos.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.unirostock.sems.masymos.query.IQueryInterface;
import de.unirostock.sems.masymos.query.results.ModelResultSet;

public class ModelResultSetWriter {
	
	final static Logger logger = LoggerFactory.getLogger(ModelResultSetWriter.class);
	
	public static void writeModelResults(List<ModelResultSet> mrs, List<IQueryInterface> qL, String path){
		
		String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(Calendar.getInstance().getTime());
	    File query = new File(path + timeLog + ".query");
	    File result = new File(path + timeLog + ".result");
	    
		Gson gson = new Gson();
		
		try {
			writeOut(result, gson.toJson(mrs));
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		StringBuilder sb = new StringBuilder();
		for (Iterator<IQueryInterface> iterator = qL.iterator(); iterator.hasNext();) {
			IQueryInterface Iquery = (IQueryInterface) iterator.next();
			sb.append(Iquery.getQuery().toString());
			sb.append(System.lineSeparator());
		}
		
		try {
			writeOut(query, sb.toString());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private static void writeOut(File f, String content) throws IOException{
		BufferedWriter bw = null;
		try {
			  
			// if file doesn't exists, then create it
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			
		} catch(IOException e){
            throw e;						
		}		
	}

}
