package com.sandy.jovenotes.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.async.PersistedCmd;
import com.sandy.jovenotes.processor.async.PersistentQueue;
import com.sandy.jovenotes.processor.async.PersistentQueue.QueueElement;
import com.sandy.jovenotes.processor.core.SourceFileProcessor;
import com.sandy.jovenotes.processor.core.SourceProcessingJournal;
import com.sandy.jovenotes.processor.util.ConfigManager;
import com.sandy.jovenotes.processor.util.Database;
import com.sandy.jovenotes.processor.util.XTextModelParser;

/**
 * Main class for the JoveNotes processor application.
 * 
 * @author Sandeep
 */
public class JoveNotes {
    
    private static final Logger log = Logger.getLogger( JoveNotes.class ) ;
    
    public static ConfigManager config = null ;
    public static Database db = null ;
    public static PersistentQueue persistentQueue = null ;
    
    private SourceProcessingJournal journal = null ;
    private XTextModelParser modelParser = null ;
    
    private JoveNotes( String[] args ) throws Exception {
        initialize( args ) ;
    }
    
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
        log.info( "\tExecuting JoveNotes processor in " + config.getRunMode() + " mode." );
        
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
        
        log.info( "JoveNotes processor - initialized." ) ;
        log.info( "" ) ;
    }
    
    private void start() throws Exception {
        
        List<File> filesForProcessing = getFilesForProcessing() ;
        log.debug( "Processing files.." ) ;
        
        for( File file : filesForProcessing ) {
            log.info( "  Processing " + 
                     file.getAbsolutePath()
                         .substring( config.getSrcDir()
                                           .getAbsolutePath()
                                           .length() ) ) ;
            try{
                SourceFileProcessor processor = new SourceFileProcessor() ;
                processor.process( file, modelParser ) ;
                journal.updateSuccessfulProcessingStatus( file ) ;
            }
            catch( Exception e ) {
                log.error( "Failure in processing " + file.getAbsolutePath(), e ) ;
                journal.updateFailureProcessingStatus( file ) ;
            }
        }
        
        try {
            processPersistedCommands() ;
        } 
        catch (Exception e) {
            log.error( "Error processing perissted cmd", e ) ;
        }
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
    
    private List<File> getFilesForProcessing() throws Exception {
        
        log.debug( "Selecting files for processing." ) ;
        
        Collection<File> allFiles = FileUtils.listFiles( 
                                config.getSrcDir(), new String[]{"jn"}, true ) ;
        List<File> filesForProcessing = new ArrayList<File>() ;
        
        if( config.isForceProcessAllFiles() ) {
            journal.clean() ;
        }
        
        for( File file : allFiles ) {
            if( config.isForceProcessAllFiles() ) {
                filesForProcessing.add( file ) ;
                log.info( "  Selecting file - " + file.getAbsolutePath() ) ;
            }
            else if( journal.hasFileChanged( file ) ) {
                filesForProcessing.add( file ) ;
                log.info( "  Selecting file - " + file.getAbsolutePath() ) ;
            }
            else {
                log.debug( "  Ignoring file - " + file.getAbsolutePath() ) ;
            }
        }
        log.debug( "" ) ;
        return filesForProcessing ;
    }

    public static void main( String[] args ) throws Exception {
        log.info( "Starting JoveNotes processor." ) ;
        
        JoveNotes processor = new JoveNotes( args ) ;
        processor.start() ;
    }
}
