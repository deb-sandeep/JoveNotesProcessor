package com.sandy.jovenotes.processor.jnsrc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A journal of source files that are processed by JoveNotes processor. 
 * 
 * @author Sandeep
 */
public class SourceProcessingJournal {

	private static final Logger log = Logger.getLogger( SourceProcessingJournal.class ) ;
	
	private Properties journal     = new Properties() ;
	private File       journalFile = null ;
	
	public SourceProcessingJournal( File journalFile ) 
		throws Exception {
		
		if( journalFile.exists() ) {
			this.journalFile = journalFile ;
			this.journal.load( new FileReader( journalFile ) ) ;
		}
		else {
			log.info( "Journal file " + journalFile.getAbsolutePath() + 
					  " does not exist. Creating a new file." ) ;
			if( !journalFile.createNewFile() ) {
				throw new Exception( "Journal file " + journalFile.getAbsolutePath() +
						             " could not be created." ) ;
			}
			else {
				this.journalFile = journalFile ;
			}
		}
	}
	
	/**
	 * Returns true under the following situations:
	 * 
	 * a) If the file has been successfully processed before by JoveNotes and  
	 * the last modified timestamp of the file is greater than the last processed
	 * timestamp.
	 * 
	 * b) If the file has been processed before but not successfully
	 * 
	 * c) If the file has never been processed by JoveNotes
	 */
	public boolean hasFileChanged( File file ) {
		
		String absPath = file.getAbsolutePath() ;
		
		if( journal.containsKey( absPath ) ) {
			long lpt = Long.parseLong( journal.getProperty( absPath ) ) ;
			long lmt = file.lastModified() ;
			
			if( lpt == -1 ) {
				// This implies that the file was processed but not successfully
				return true ;
			}
			else if( lmt > lpt ) {
				return true ;
			}
			else {
				return false ;
			}
		}
		else {
			// The file has never been processed by JoveNotes
			return true ;
		}
	}
	
	public void updateSuccessfulProcessingStatus( File file ) {
		updateAndSaveProperties( file, new Date().getTime() ) ;
	}
	
	public void updateFailureProcessingStatus( File file ) {
		updateAndSaveProperties( file, -1 ) ;
	}
	
	private void updateAndSaveProperties( File file, long processingValue ) {
		
		journal.setProperty( file.getAbsolutePath(), 
	                         Long.toString( processingValue ) ) ;
		try {
			Writer writer = new FileWriter( this.journalFile ) ;
			journal.store( writer, null ) ; 
			writer.close() ;
		}
		catch( Exception e ) {
			log.error( "Could not save journal.", e ) ;
		}
	}
}
