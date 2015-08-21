package com.sandy.jovenotes.processor.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
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
            
            String[] valParts = journal.getProperty( absPath ).split( "," ) ;
            
            long lpt     = Long.parseLong( valParts[0] ) ;
            long lastCRC = Long.parseLong( valParts[1] ) ;
            
            if( lpt == -1 ) {
                // This implies that the file was processed but not successfully
                return true ;
            }
            else if( FileUtils.isFileNewer( file, lpt ) ) {
                try {
                    long curCRC = FileUtils.checksumCRC32( file ) ;
                    return curCRC != lastCRC ;
                }
                catch( Exception e ) {
                    return true ;
                }
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
    
    public void updateSuccessfulProcessingStatus( File file ) 
        throws Exception {
        
        updateAndSaveProperties( file, new Date().getTime() ) ;
    }
    
    public void updateFailureProcessingStatus( File file ) {
        
        try {
            updateAndSaveProperties( file, -1 ) ;
        } 
        catch( Exception e ) {
            log.info( "Error saving processing status.", e ) ;
        }
    }
    
    private void updateAndSaveProperties( File file, long processingValue ) 
        throws IOException {
        
        journal.setProperty( file.getAbsolutePath(), 
                             Long.toString( processingValue ) + "," + 
                             FileUtils.checksumCRC32( file ) ) ;
        
        Writer writer = new FileWriter( this.journalFile ) ;
        journal.store( writer, null ) ; 
        writer.close() ;
    }
    
    public void clean() throws IOException {

        Writer writer = new FileWriter( this.journalFile ) ;
        journal.clear() ;
        journal.store( writer, null ) ; 
        writer.close() ;
    }
}
