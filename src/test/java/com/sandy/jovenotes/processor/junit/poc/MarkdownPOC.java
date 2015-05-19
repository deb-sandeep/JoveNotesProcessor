// To run this class, we need markdown as a dependency. Also uncomment some 
// of the markdown specific lines below.
// Since a decision has been taken to go with pegdown instead of markdown4j,
// the markdown dependency has been removed from this project.
//
//		<dependency>
//		    <groupId>org.commonjava.googlecode.markdown4j</groupId>
//		    <artifactId>markdown4j</artifactId>
//		    <version>2.2-cj-1.0</version>
//		</dependency>


package com.sandy.jovenotes.processor.junit.poc;

import org.apache.log4j.Logger;
//import org.markdown4j.Markdown4jProcessor;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

public class MarkdownPOC {
	
	private static final Logger log = Logger.getLogger( MarkdownPOC.class ) ;
	
//	private Markdown4jProcessor md4jProcessor = new Markdown4jProcessor() ;
	private PegDownProcessor    pdProcessor   = new PegDownProcessor( Extensions.ALL & ~Extensions.HARDWRAPS ) ;

	private void runTests() throws Exception {
		process( "Emphasis", 
				 "This is an *emphasized* text with **bold** and ~~strikeout~~." ) ;
		
		process( "Escape"  , 
				 "&copy; AT&T" ) ;
		
		process( "Line breaks",
				 "First line\nSecond line\n\nSecond paragraph." ) ;
		
		process( "Headers",
				 "#H1\n##H2\n###H3" ) ;
		
		process( "Ordered Lists",
				 "1. First\n2. Second\n3. Third" ) ;
		
		process( "Unordered Lists",
				"* First\n* Second\n* Third" ) ;
		
		process( "Tables",
				"\n\n" +
				"Header 1  | Header 2\n" +
				"--------- | ---------\n" +
				"Cell **1**    | Cell 2\n" +
				"Cell 3    | Cell 4\n" +
				"\n") ;
		
		process( "Custom JoveNotes markers",
				 "This is an **emphasis** text with {{@img x.png}} marker." ) ;
						
	}
	
	private void process( String testDescr, String input ) throws Exception {
		log.debug( "\n---------------------------------------------------------" ) ;
		log.debug( "Test -- " + testDescr ) ;
		log.debug( "Input-- " + input + "\n" ) ;
//		log.debug( "MD4J :: " + md4jProcessor.process( input ) ) ;
		log.debug( "PD   :: " + pdProcessor.markdownToHtml( input ) ) ;
	}
	
	public static void main(String[] args) throws Exception {
		MarkdownPOC poc = new MarkdownPOC() ;
		poc.runTests() ;
	}
}
