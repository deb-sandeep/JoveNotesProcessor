package com.sandy.jovenotes.processor.util;

import org.apache.log4j.Logger;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import com.sandy.jovenotes.processor.core.notes.Chapter;

public class JNTextProcessor {

	private static final Logger log = Logger.getLogger( JNTextProcessor.class ) ;
	
	private static PegDownProcessor pdProcessor = new PegDownProcessor( Extensions.ALL ) ;
	
	public String processText( Chapter chapter, String input ) {
		
		String output = processJoveNotesMarkers( input ) ;
		output = pdProcessor.markdownToHtml( output ) ;
		
		return output ;
	}
	
	private String processJoveNotesMarkers( String input ) {
		
		String output = input ;
		processJNImgMarkers( output ) ;
		return input ;
	}
	
	private void processJNImgMarkers( String input ) {
		
	}
}
