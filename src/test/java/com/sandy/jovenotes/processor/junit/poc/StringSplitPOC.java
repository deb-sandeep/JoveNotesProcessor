package com.sandy.jovenotes.processor.junit.poc;

import org.apache.log4j.Logger;

public class StringSplitPOC {

	private static final Logger log = Logger.getLogger( StringSplitPOC.class ) ;
	
	public static void main( String[] args ) {
		
		String line = "Al(s) + H2SO4(aq)";
		String[] parts = line.split( "\\s+\\+\\s+" ) ;
		for( String part : parts ) {
			log.debug( part ) ;
		}
	}
}
