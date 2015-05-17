package com.sandy.jovenotes.processor.junit.poc;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.util.Markdown;

public class Markdown4JTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger( Markdown4JTest.class ) ;

	public void testMD4JSimple() throws Exception {
		
		Markdown processor = new Markdown() ;
		String out = processor.process( "This is  $$@img Hello.png$$." ) ;
		
		logger.debug( out ) ;
	}
}
