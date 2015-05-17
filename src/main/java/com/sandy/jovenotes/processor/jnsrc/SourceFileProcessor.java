package com.sandy.jovenotes.processor.jnsrc;

import java.io.File;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.util.XTextModelParser;
import com.sandy.xtext.joveNotes.JoveNotes ;

public class SourceFileProcessor {
	
	private static final Logger log = Logger.getLogger( SourceFileProcessor.class ) ;
	
	private JoveNotesASTWrapper astWrapper = null ;

	public void process( File file, XTextModelParser modelParser ) 
			throws Exception {
		
		JoveNotes ast = ( JoveNotes )modelParser.parseFile( file ) ;
		log.debug( "\tAST created." ) ;
		
		astWrapper = new JoveNotesASTWrapper( ast ) ;
		
		if( astWrapper.shouldSkipProcessing() ) {
			log.debug( "\tProcessing skipped as @skip_processing is on." ) ;
			return ;
		}
		
		
	}
}
