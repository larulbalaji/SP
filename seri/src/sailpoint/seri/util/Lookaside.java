package sailpoint.seri.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.tools.GeneralException;
import sailpoint.tools.RFC4180LineIterator;
import sailpoint.tools.RFC4180LineParser;

public class Lookaside {
	
	private static Log log = LogFactory.getLog(Lookaside.class);

	
	private enum LoadType{
		LIST,
		MAP
	}

	public static Map load(String filename) throws GeneralException {

		return load(filename, ",");

	}

	public static Map load(String filename, String separator) throws GeneralException {
		return load(filename, separator, null, null);
	}

	public static Collection<Map<String,String>> loadList(String filename, String separator) throws GeneralException {
		return loadList(filename, separator, null);
	}
	public static Collection<Map<String,String>> loadList(String filename, String separator, List<String> tokens) throws GeneralException {
		
		List<Map<String,String>> lst=(List<Map<String,String>>)doLoad(filename, separator, tokens, null, LoadType.LIST);
		return lst;
		
	}
	public static Map<String,Map<String,String>> load(String filename, String separator, List<String> tokens, String idToken) throws GeneralException {

		Map<String,Map<String,String>> map=(Map<String,Map<String,String>>)doLoad(filename, separator, tokens, null, LoadType.MAP);
		return map;
	}
	
	public static Object doLoad(String filename, String separator, List<String> tokens, String idToken, LoadType type) throws GeneralException {
		
		String encoding="UTF8";
		Map<String,Map<String,String>> mapVal=null;
		List<Map<String,String>> listVal=null;
		
		RFC4180LineIterator lines = null;
		
		if(type==LoadType.LIST) {
			listVal=new ArrayList<Map<String,String>>();
		}
		if(type==LoadType.MAP) {
			mapVal=new HashMap<String,Map<String,String>>();
		}
		
		try {
			File file = new File(filename);
			BufferedInputStream bis   = new BufferedInputStream(new FileInputStream(file));
			InputStream stream        = (InputStream) bis;
			lines = new RFC4180LineIterator( new BufferedReader(new InputStreamReader(stream, encoding)));
		} catch (FileNotFoundException e) {
			throw new GeneralException("ERROR: Lookaside file does not exist["+filename+"]");
		} catch (UnsupportedEncodingException e) {
			throw new GeneralException("ERROR: Encoding '"+encoding+"' is unsupported");
		}

		RFC4180LineParser lineparser = new RFC4180LineParser( separator );

		try {

			if ( lines != null ) {
				int recnum  = 0;
				String line = "";

				// Iterate over the Data Records
				// -----------------------------
				if(tokens==null) {
					// read header
					line = lines.readLine();
					List<String> toTrimTokens=lineparser.parseLine(line);
					tokens=new ArrayList<String>();
					for(String trimToken: toTrimTokens) {
						if(trimToken==null) tokens.add(null);
						else tokens.add(trimToken.trim());
					}
					if(idToken==null) {
						idToken=tokens.get(0);
					}
				}

				// read lines
				line = lines.readLine();
				while (null != line) {

					List<String> values=lineparser.parseLine(line);

					Map<String,String> mapLine=new HashMap<String,String>(); 
					if (tokens!=null && values!=null) {
						// if we have less columns in data row, use data row size
						int size=values.size();
						if (tokens.size()<values.size()) size=tokens.size();
						for(int i=0;i<tokens.size();i++) {
							String tok=tokens.get(i);
							String val=values.get(i);
							mapLine.put(tok, val);
						}
						if(type==LoadType.LIST) {
							listVal.add(mapLine);
						}
						if(type==LoadType.MAP) {
							mapVal.put(mapLine.get(idToken), mapLine);
						}

					}
					line = lines.readLine();
				}
			}
		} catch (IOException e) {
			throw new GeneralException("ERROR: IOException reading ["+filename+"]", e);
		} finally {
			if ( lines != null ) 
				lines.close();
		}
		if(type==LoadType.LIST) {
			log.debug("Lookaside.load: returning "+listVal.size()+" records");
			return listVal;
		}
		if(type==LoadType.MAP) {
			log.debug("Lookaside.load: returning "+mapVal.size()+" records");
			return mapVal;
		}
		return null;

	}

}
