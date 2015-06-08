package com.sandy.jovenotes.processor.junit.poc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MJRegexpPOC {

	private static final Logger log = Logger.getLogger( MJRegexpPOC.class ) ;
	
	public static void main( String[] args ) {
		
		// String to be scanned to find the pattern.
		String line = "This is a $$N = \\{ 1, 2, $3, ... $\\}$$ MJ expression";
		
		String pattern = "\\$\\$(.*)\\$\\$" ;

		// Create a Pattern object
		Pattern r = Pattern.compile( pattern ) ;

		// Now create matcher object.
		Matcher m = r.matcher(line) ;
		
		while( m.find() ) {
			log.info( "" + m.group(1) + 
					   ", start=" + m.start() + ", end=" + m.end() ) ;
		}
	}
}
