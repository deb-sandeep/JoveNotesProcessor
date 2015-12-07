package com.sandy.jovenotes.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.PathMatcher ;
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
import com.sandy.jovenotes.processor.util.LocalDatabase;
import com.sandy.jovenotes.processor.util.LocalWebServer;
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
    
    public static LocalWebServer ws = null ;
    
    private SourceProcessingJournal journal = null ;
    private XTextModelParser modelParser = null ;
    
    private List<PathMatcher> includePathMatchers = null ;
    private List<PathMatcher> excludePathMatchers = null ;
    
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
        
        if( config.getRunMode().isPreview() ) {
            log.info( "\tstarting and connecting to local database" ) ;
            db = new LocalDatabase( config.getLocalDatabasePath(), 
                                    config.getLocalDbName(), 
                                    config.getLocalDbPort() ) ;
        }
        else {
            log.info( "\tconnecting to remote database" ) ;
            db = new Database( config.getDatabaseDriverName(), 
                               config.getDatabaseURL(), 
                               config.getDatabaseUser(), 
                               config.getDatabasePassword() ) ;
        }
        db.initialize() ;
        db.returnConnection( db.getConnection() ) ;
        log.debug( "\tDatabase initialized." ) ;
        
        if( config.getRunMode().isPreview() ) {
            
            log.info( "\tstarting local web server at port: " + config.getLocalWsPort() ) ;
            
            ws = new LocalWebServer( config.getLocalWebserverPath() ) ;
            ws.setPort( config.getLocalWsPort() );
            ws.initialize() ;
        }
        
        persistentQueue = new PersistentQueue() ;
        log.debug( "\tPersistent Queue initialized." ) ;
        
        File journalFile = new File( config.getWorkspaceDir(), "jove_notes.journal" ) ;
        journal = new SourceProcessingJournal( journalFile ) ;
        log.debug( "\tSource processing journal initialized." ) ;
        
        includePathMatchers = config.getIncludePathMatchers() ;
        excludePathMatchers = config.getExcludePathMatchers() ;
        log.debug( "\tInclude and exclude path matchers obtained." ) ;
        
        log.info( "JoveNotes processor - initialized." ) ;
        log.info( "" ) ;
    }
    
    private void start() throws Exception {
        
        journal.clean() ;
        
        List<File> srcDirs = config.getSrcDirs() ;
        for( File srcDir : srcDirs ) {
            
            log.info( "\nLoading files from : " + srcDir.getAbsolutePath() ) ;
            log.info( "--------------------------------------------------------" );
            List<File> filesForProcessing = getFilesForProcessing( srcDir ) ;
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
    }
    
    private List<File> getFilesForProcessing( File srcDir ) throws Exception {
        
        List<File> filesForProcessing = new ArrayList<File>() ;
            
        Collection<File> allFiles = FileUtils.listFiles( srcDir, 
                                                new String[]{"jn"}, true ) ;
        for( File file : allFiles ) {
            
            if( !shouldConsiderFile( file ) ) continue ;
            
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
        
        log.info("\n") ;
        
        return filesForProcessing ;
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
    
    private boolean shouldConsiderFile( File file ) {
        
        // By default a file is included.
        boolean shouldConsider = true ;
        
        // We check the exclude patterns first. If there are exclude patterns
        // and if one of them matches the file, the file is excluded.
        if( !excludePathMatchers.isEmpty() ) {
            for( PathMatcher matcher : excludePathMatchers ) {
                if( matcher.matches( file.toPath() ) ) {
                    //log.debug( "Rejecting file as it matches exclusion filter. " + 
                    //           matcher.toString() );
                    shouldConsider = false ;
                    break ;
                }
            }
        }
        
        // If the file has not been already excluded and we have include include
        // matchers, we see if the file matches any of the matchers.
        if( !includePathMatchers.isEmpty() && shouldConsider ) {
            boolean includePatternMatch = false ;
            for( PathMatcher matcher : includePathMatchers ) {
                if( matcher.matches( file.toPath() ) ) {
                    shouldConsider = true ;
                    includePatternMatch = true ;
                    break ;
                }
            }
            
            // If we have include matchers, but none of them match the file,
            // it should be excluded.
            if( !includePatternMatch ) {
                //log.debug( "Rejecting file as it does not match any inclusion filter." ) ;
                shouldConsider = false ;
            }
        }
        
        return shouldConsider ;
    }
    
    public static void main( String[] args ) throws Exception {
        log.info( "Starting JoveNotes processor." ) ;
        
        JoveNotes processor = new JoveNotes( args ) ;
        processor.start() ;
    }
}
