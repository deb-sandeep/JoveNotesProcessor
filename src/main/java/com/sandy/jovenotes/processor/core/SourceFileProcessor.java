package com.sandy.jovenotes.processor.core;

import java.io.File;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.async.RefreshChapterCmd;
import com.sandy.jovenotes.processor.core.notes.Chapter;
import com.sandy.jovenotes.processor.dao.ChapterDBO;
import com.sandy.jovenotes.processor.util.XTextModelParser;
import com.sandy.xtext.joveNotes.JoveNotes;
import com.sandy.xtext.joveNotes.ProcessingHints;

public class SourceFileProcessor {
	
	private static final Logger log = Logger.getLogger( SourceFileProcessor.class ) ;
	
	public void process( File file, XTextModelParser modelParser ) 
			throws Exception {
		
		JoveNotes ast = ( JoveNotes )modelParser.parseFile( file ) ;
		log.debug( "\tAST created." ) ;
		if( shouldSkipProcessing( ast ) ) {
			log.debug( "\tProcessing skipped as @skip_processing is on." ) ;
			return ;
		}
		
		// Create the source side object model. This includes source translation
		// and any required content transformation recursively for notes elements
		// and associated cards.
		Chapter chapter = new Chapter( file, ast ) ;

		// Retrieve the database object model if one exists
		ChapterDBO chapterDBO = ChapterDBO.get( chapter ) ;
		
		boolean chapterUpdateRequired = true ;
		if( chapterDBO == null ) {
			log.debug( "\tChapter " + chapter + " does not exist." ) ;
			chapterDBO = insertNewChapter( chapter ) ;
		}
		else {
			log.debug( "\tChapter " + chapter + " is present in database." ) ;
			chapterUpdateRequired = chapterDBO.trace( chapter ) ;
			if( chapterUpdateRequired ){
				log.debug( "\tChapter update required. Processing trace." ) ;
				chapterDBO.processTrace() ;
			}
		}
		
		if( chapterUpdateRequired ) {
			log.debug( "\tChapter has been updated. Refreshing meta data." ) ;
			com.sandy.jovenotes.processor.JoveNotes.persistentQueue.add( 
				 new RefreshChapterCmd( chapter, chapterDBO.getChapterId() ) ) ;
		}
	}
	
	private ChapterDBO insertNewChapter( Chapter chapter ) 
		throws Exception {
		
		log.debug( "\tInserting new chapter." ) ;
		ChapterDBO chapterDBO = new ChapterDBO( chapter ) ;
		chapterDBO.create() ;
		log.debug( "\tNew chapter created. id = " + chapterDBO.getChapterId() );
		return chapterDBO ;
	}
	
	private boolean shouldSkipProcessing( JoveNotes notesAST ) {
		ProcessingHints hints = notesAST.getProcessingHints() ;
		return ( hints != null ) && ( hints.getSkipGeneration() != null ) ;
	}
}
