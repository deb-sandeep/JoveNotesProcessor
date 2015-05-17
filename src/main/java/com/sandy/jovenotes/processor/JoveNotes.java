package com.sandy.jovenotes.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.async.PersistedCmd;
import com.sandy.jovenotes.processor.jnsrc.SourceFileProcessor;
import com.sandy.jovenotes.processor.jnsrc.SourceProcessingJournal;
import com.sandy.jovenotes.processor.util.ConfigManager;
import com.sandy.jovenotes.processor.util.Database;
import com.sandy.jovenotes.processor.util.FileUtil;
import com.sandy.jovenotes.processor.util.PersistentQueue;
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
	public static PersistentQueue<PersistedCmd> persistentQueue = null ;
	
	private SourceProcessingJournal journal = null ;
	private XTextModelParser modelParser = null ;
	
	private JoveNotes( String[] args ) throws Exception {
		initialize( args ) ;
	}
	
	private void initialize( String[] args ) throws Exception {
		log.debug( "Initializing JoveNotes processor." ) ;
		
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
		log.debug( "\tDatabase initialized." ) ;
		
		File pqFile = new File( config.getWorkspaceDir(), "jove_notes.pq" ) ;
		persistentQueue = new PersistentQueue<PersistedCmd>( pqFile ) ;
		log.debug( "\tPersistent Queue initialized." ) ;
		
		File journalFile = new File( config.getWorkspaceDir(), "jove_notes.journal" ) ;
		journal = new SourceProcessingJournal( journalFile ) ;
		log.debug( "\tSource processing journal initialized." ) ;
		
		log.debug( "JoveNotes processor - initialized." ) ;
		log.debug( "" ) ;
	}
	
	private void start(){
		
		List<File> filesForProcessing = getFilesForProcessing() ;
		log.debug( "Processing files.." ) ;
		
		for( File file : filesForProcessing ) {
			log.debug( "  Processing " + file.getAbsolutePath() ) ;
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
	}
	
	private List<File> getFilesForProcessing() {
		
		log.debug( "Selecting files for processing." ) ;
		
		List<File> allFiles = FileUtil.getFilesList( config.getSrcDir(), ".jn", true ) ;
		List<File> filesForProcessing = new ArrayList<File>() ;
		
		for( File file : allFiles ) {
			if( config.isForceProcessAllFiles() ) {
				filesForProcessing.add( file ) ;
				log.debug( "  Selecting file - " + file.getAbsolutePath() ) ;
			}
			else if( journal.hasFileChanged( file ) ) {
				filesForProcessing.add( file ) ;
				log.debug( "  Selecting file - " + file.getAbsolutePath() ) ;
			}
			else {
				log.debug( "  Ignoring file - " + file.getAbsolutePath() ) ;
			}
		}
		log.debug( "" ) ;
		return filesForProcessing ;
	}

	public static void main( String[] args ) throws Exception {
		log.debug( "Starting JoveNotes processor." ) ;
		
		JoveNotes processor = new JoveNotes( args ) ;
		processor.start() ;
	}
}
