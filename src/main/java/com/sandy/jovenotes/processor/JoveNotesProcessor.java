package com.sandy.jovenotes.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.async.PersistedCmd;
import com.sandy.jovenotes.processor.async.PersistentQueue;
import com.sandy.jovenotes.processor.async.PersistentQueue.QueueElement;
import com.sandy.jovenotes.processor.util.ConfigManager;
import com.sandy.jovenotes.processor.util.Database;
import com.sandy.jovenotes.processor.util.XTextModelParser;
import com.sandy.jovenotes.processor.util.stat.Stats ;

/**
 * Main class for the JoveNotes processor application.
 * 
 * @author Sandeep
 */
public class JoveNotesProcessor {
    
    private static final Logger log = Logger.getLogger( JoveNotesProcessor.class ) ;
    
    public static void main( String[] args ) throws Exception {
        log.info( "Starting JoveNotes processor." ) ;
        JoveNotesProcessor processor = new JoveNotesProcessor() ;
        processor.initialize( args ) ;
        processor.start() ;
    }

    // -------------------------------------------------------------------------
    
    public static ConfigManager config = null ;
    public static Database db = null ;
    public static PersistentQueue persistentQueue = null ;
    
    private SourceFileFinder srcFileFinder = null ;
    private SourceProcessingJournal journal = null ;
    private XTextModelParser modelParser = null ;
    
    private void initialize( String[] args ) throws Exception {
        log.debug( "Initializing JoveNotes processor." ) ;
        
        java.util.logging.Logger.getLogger("").setLevel( Level.WARNING ) ;
        
        modelParser = new XTextModelParser( "com.sandy.xtext.JoveNotesStandaloneSetup" ) ;
        log.debug( "\tModel parser initialized." ) ;
        
        config = new ConfigManager( args ) ;
        if( config.isShowUsage() ) {
            config.printUsage() ;
            System.exit( 0 );
        }
        log.debug( "\tConfigManager initialized." ) ;
        
        db = new Database( config.getDatabaseDriverName(), 
                           config.getDatabaseURL(), 
                           config.getDatabaseUser(), 
                           config.getDatabasePassword() ) ;
        db.returnConnection( db.getConnection() ) ;
        log.debug( "\tDatabase initialized." ) ;
        
        persistentQueue = new PersistentQueue() ;
        log.debug( "\tPersistent Queue initialized." ) ;
        
        File journalFile = new File( config.getWorkspaceDir(), "jove_notes.journal" ) ;
        journal = new SourceProcessingJournal( journalFile ) ;
        log.debug( "\tSource processing journal initialized." ) ;
        
        srcFileFinder = new SourceFileFinder( config, journal ) ;
        log.debug( "\tSource file finder created." ) ;
        
        log.info( "JoveNotes processor - initialized." ) ;
        log.info( "" ) ;
    }
    
    private void start() throws Exception {
        
        log.info( "\tExecuting JoveNotes processor in " + config.getRunMode() + " mode." );
        journal.clean() ;
        
        List<File> srcDirs = config.getSrcDirs() ;
        for( File srcDir : srcDirs ) {
            
            log.info( "\nLoading files from : " + srcDir.getAbsolutePath() ) ;
            log.info( "--------------------------------------------------------" );
            List<File> filesForProcessing = srcFileFinder.getFilesForProcessing( srcDir ) ;
            log.debug( "Processing files.." ) ;
            
            for( File file : filesForProcessing ) {
                log.info( "  Processing " + file.getAbsolutePath() ) ;
                try{
                    SourceFileProcessor processor = new SourceFileProcessor() ;
                    processor.process( srcDir, file, modelParser ) ;
                    journal.updateSuccessfulProcessingStatus( file ) ;
                }
                catch( Exception e ) {
                    log.error( "Failure in processing " + file.getAbsolutePath(), e ) ;
                    journal.updateFailureProcessingStatus( file ) ;
                }
            }
        }
        
        try {
            processPersistedCommands() ;
        } 
        catch (Exception e) {
            log.error( "Error processing perissted cmd", e ) ;
        }
        
        Stats.printStats() ;
    }
    
    private void processPersistedCommands() throws Exception {
        
        log.info( "Processing persisted commands." ) ;
        
        int numCommands = persistentQueue.size() ;
        for( int i=0; i<numCommands; i++ ) {
            QueueElement qElement = null ;
            PersistedCmd cmd = null ;
            try {
                qElement = persistentQueue.remove() ;
                cmd = ( PersistedCmd )qElement.getObject() ;
                cmd.execute() ;
            } 
            catch( Exception e ){
                log.error( "\tPersisted command failed. Command = " + cmd, e ) ;
                if( cmd != null ) {
                    try {
                        qElement.reCreate() ;
                    } 
                    catch (IOException e1) {
                        log.error( "\tCould not re-persist command. " + cmd, e1 ) ;
                    } 
                }
            }
        }
    }
    
}
