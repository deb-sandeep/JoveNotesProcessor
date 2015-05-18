package com.sandy.jovenotes.processor.core;

import java.io.File;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.notes.Chapter;
import com.sandy.jovenotes.processor.util.XTextModelParser;
import com.sandy.xtext.joveNotes.JoveNotes;

public class SourceFileProcessor {
	
	private static final Logger log = Logger.getLogger( SourceFileProcessor.class ) ;
	
	public void process( File file, XTextModelParser modelParser ) 
			throws Exception {
		
		JoveNotes ast = ( JoveNotes )modelParser.parseFile( file ) ;
		log.debug( "\tAST created." ) ;
		
		Chapter chapter = new Chapter( file, ast ) ;
		if( chapter.shouldSkipProcessing() ) {
			log.debug( "\tProcessing skipped as @skip_processing is on." ) ;
			return ;
		}
	}
}
