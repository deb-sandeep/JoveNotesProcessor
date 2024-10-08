package com.sandy.jovenotes.processor;

import java.io.File;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.async.RefreshChapterCmd;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.db.dao.ChapterDAO ;
import com.sandy.jovenotes.processor.db.dbo.ChapterDBO ;
import com.sandy.jovenotes.processor.util.XTextModelParser;
import com.sandy.jovenotes.processor.util.stat.Stats ;
import com.sandy.xtext.joveNotes.JoveNotes;
import com.sandy.xtext.joveNotes.ProcessingHints;

public class SourceFileProcessor {
    
    private static final Logger log = Logger.getLogger( SourceFileProcessor.class ) ;
    
    public void process( File baseDir, File file, XTextModelParser modelParser ) 
            throws Exception {
        
        JoveNotes ast = ( JoveNotes )modelParser.parseFile( file ) ;
        log.debug( "\tAST created." ) ;
        if( shouldSkipProcessing( ast ) ) {
            return ;
        }
        
        // Create the source side object model. This includes source translation
        // and any required content transformation recursively for notes elements
        // and associated cards.
        Chapter chapter = new Chapter( baseDir, file, ast ) ;

        // Retrieve the database object model if one exists
        ChapterDBO chapterDBO = ChapterDAO.get( chapter ) ;
        
        // Create the chapter statistics
        boolean nextLevelTraceRequired = true ;
        
        if( chapterDBO == null ) {
            log.info( "\tChapter " + chapter + " does not exist." ) ;
            Stats.newChapterBeingProcessed( chapter ) ;
            chapterDBO = insertNewChapter( chapter ) ;
        }
        else {
            log.debug( "\tChapter " + chapter + " is present in database." ) ;
            nextLevelTraceRequired = chapterDBO.trace( chapter ) ;
            
            if( nextLevelTraceRequired ){
                log.info( "\tChapter update required. Processing trace." ) ;
                Stats.updatedChapterBeingProcessed( chapter ) ;
                chapterDBO.processTrace() ;
            }
            else if( chapterDBO.isModified() ) {
                Stats.updatedChapterBeingProcessed( chapter ) ;
                ChapterDAO.update( chapterDBO ) ;
            }
        }
        
        if( nextLevelTraceRequired ) {
            log.info( "\tChapter has been updated. Refreshing meta data." ) ;
            com.sandy.jovenotes.processor.JoveNotesProcessor.persistentQueue.add( 
                 new RefreshChapterCmd( chapter, chapterDBO.getChapterId() ) ) ;
        }
    }
    
    private ChapterDBO insertNewChapter( Chapter chapter ) 
        throws Exception {
        
        log.info( "\tInserting new chapter." ) ;
        ChapterDBO chapterDBO = new ChapterDBO( chapter ) ;
        ChapterDAO.create( chapterDBO ) ;
        log.debug( "\tNew chapter created. id = " + chapterDBO.getChapterId() );
        return chapterDBO ;
    }
    
    private boolean shouldSkipProcessing( JoveNotes notesAST ) {
        ProcessingHints hints = notesAST.getProcessingHints() ;
        String runMode = com.sandy.jovenotes.processor.JoveNotesProcessor.config.getRunMode() ;
        
        boolean shouldSkipProcess = false ;
        if( hints != null ) {
            
            // If @skip_generation is specified, we skip processing both in 
            // development and production
            if( hints.getSkipGeneration() != null ) {
                shouldSkipProcess = true ;
                log.info( "\tProcessing skipped as @skip_processing is on." ) ;
            }
            else {
                // If @skip_generation_in_production is specified, we skip
                // processing only if we are operating in production run mode
                if( hints.getSkipGenerationInProduction() != null && 
                    runMode.equalsIgnoreCase( "production" ) ) {
                    
                    shouldSkipProcess = true ;
                    log.info( "\tProcessing skipped as @skip_processing_in_production is on." ) ;
                }
            }
        }
        return shouldSkipProcess ;
    }
}
