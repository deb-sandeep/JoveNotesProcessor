package com.sandy.jovenotes.processor.junit.poc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RegexpPOC {

	private static final Logger log = Logger.getLogger( RegexpPOC.class ) ;
	
	public static void main( String[] args ) {
		
		// String to be scanned to find the pattern.
		String line = "This is {{@img 	Hello.png}} and {{@txt Hello There}} text {{@formula a{2}}}.";
		
		String pattern = "\\{\\{@([a-zA-Z0-9]*)\\s+((.(?!\\{\\{))*)\\}\\}" ;

		// Create a Pattern object
		Pattern r = Pattern.compile( pattern ) ;

		// Now create matcher object.
		Matcher m = r.matcher(line) ;
		
		while( m.find() ) {
			log.debug( "\nMatch-" + m.group(0) + ", start=" + m.start() + ", end=" + m.end() ) ;
			log.debug( m.group(1) ) ;
			log.debug( m.group(2) ) ;
		}
	}
}
