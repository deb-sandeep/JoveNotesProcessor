package com.sandy.jovenotes.processor.core;

import java.io.File;

import org.apache.log4j.Logger;

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
		
		// Create the source side object model
		Chapter chapter = new Chapter( file, ast ) ;
		chapter.processNotesElementContents() ;
		
		ChapterDBO chapterDBO = ChapterDBO.get( chapter ) ;
		if( chapterDBO == null ) {
			log.debug( "\tChapter " + chapter + " does not exist." ) ;
			insertNewChapter( chapter ) ;
		}
		else {
			log.debug( "\tChapter " + chapter + " is present in database." ) ;
		}
		
		// Check if the chapter exists in the database - 
		//   If not, then the full object model will get created in the database
		//   If exists
		//      Get database object model
		//      Trace database->source models
		// 		From the traced models, determine
		// 			1. New elements (notes, cards) 
		// 			2. Changed elements (notes, cards)
		// 			3. Deleted elements (notes, cards)
	}
	
	private void insertNewChapter( Chapter chapter ) 
		throws Exception {
		
		log.debug( "\tInserting new chapter." ) ;
		ChapterDBO chapterDBO = new ChapterDBO( chapter ) ;
		chapterDBO.create() ;
		log.debug( "\tNew chapter created. id = " + chapterDBO.getChapterId() );
	}
	
	private boolean shouldSkipProcessing( JoveNotes notesAST ) {
		ProcessingHints hints = notesAST.getProcessingHints() ;
		return ( hints != null ) && ( hints.getSkipGeneration() != null ) ;
	}
}
