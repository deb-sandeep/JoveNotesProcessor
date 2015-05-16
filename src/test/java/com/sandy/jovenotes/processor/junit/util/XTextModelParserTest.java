package com.sandy.jovenotes.processor.junit.util;

import java.net.URL;

import junit.framework.TestCase;

import com.sandy.jovenotes.processor.util.XTextModelParser;
import com.sandy.xtext.joveNotes.impl.JoveNotesImpl;

public class XTextModelParserTest extends TestCase {
	
	private static String BOOTSTRAP = "com.sandy.xtext.JoveNotesStandaloneSetup" ; 

	public void testXTextModelParserConstruction() throws Exception {
		new XTextModelParser( BOOTSTRAP ) ;
	}
	
	public void testParseSimpleNotes() throws Exception {
		
		XTextModelParser parser = new XTextModelParser( BOOTSTRAP ) ;
		URL url = XTextModelParserTest.class.getResource( 
				  "/com/sandy/jovenotes/processor/junit/util/simpleNotes.jn" ) ;
		
		JoveNotesImpl ast = ( JoveNotesImpl )parser.parseURL( url ) ;
		
		assertEquals( "TestSubject", ast.getChapterDetails().getSubjectName() ) ;
		assertEquals( "TestChapter", ast.getChapterDetails().getChapterName() ) ;
		assertEquals( 4, ast.getChapterDetails().getChapterNumber() ) ;
		assertEquals( 5, ast.getChapterDetails().getSubChapterNumber() ) ;
	}
}
