package com.sandy.jovenotes.processor.core.notes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement;
import com.sandy.jovenotes.processor.util.ConfigManager;
import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.xtext.joveNotes.JoveNotes;
import com.sandy.xtext.joveNotes.NotesElement;

public class Chapter {
	
	private static final Logger log = Logger.getLogger( Chapter.class ) ;

	private ConfigManager config = null ;
	
	private File srcFile = null ;
	private String syllabusName = null ;
	private JoveNotes notesAST = null ;
	private ArrayList<AbstractNotesElement> notesElements = null ;
	
	public Chapter( File srcFile, JoveNotes notesAST ) {
		
		this.config = com.sandy.jovenotes.processor.JoveNotes.config ;
		
		this.notesElements = new ArrayList<AbstractNotesElement>() ;
		this.srcFile = srcFile ;
		this.syllabusName = getSyllabusName( srcFile ) ;
		this.notesAST = notesAST ;
		
		log.debug( "\tObject transforming chapter - " + getChapterFQN() );
		
		for( NotesElement element : this.notesAST.getNotesElements() ) {
			notesElements.add( NotesElements.build( this, element ) ) ;
		}
	}
	
	public List<AbstractNotesElement> getNotesElements() {
		return this.notesElements ;
	}
	
	public boolean isTestPaper() {
		return notesAST.getChapterDetails().getTestPaper() != null ;
	}
	
	public String getSyllabusName() {
		return this.syllabusName ;
	}
	
	public String getSubjectName() {
		return notesAST.getChapterDetails().getSubjectName() ;
	}
	
	public int getChapterNumber() {
		return notesAST.getChapterDetails().getChapterNumber() ;
	}
	
	public int getSubChapterNumber() {
		return notesAST.getChapterDetails().getSubChapterNumber() ;
	}
	
	public String getChapterName() {
		return notesAST.getChapterDetails().getChapterName() ;
	}
	
	public String getChapterFQN() {
		return syllabusName + "/" +
	           getSubjectName() + "/" +
			   getChapterNumber() + "/" +
	           getSubChapterNumber() + "/" +
			   getChapterName() ;
	}
	
	public File getSourceFile() { return this.srcFile ; }
	
	public File getMediaDirectory() {
		
		File dir =  new File( config.getDestMediaRootDir(), 
				         syllabusName + File.separator + 
				         getSubjectName() + File.separator + 
				         getChapterNumber() + File.separator + 
				         getSubChapterNumber() ) ;
		if( !dir.exists() ) {
			dir.mkdirs() ;
		}
		return dir ;
	}
	
	public File getSrcImagesFolder() { 
		return new File( this.srcFile.getParentFile(), "img" ) ;
	}
	
	public void processNotesElementContents() 
		throws Exception {
		
		log.debug( "\tProcessing notes elements" ) ;
		ArrayList<File> existingMediaFiles = new ArrayList<File>() ;
		existingMediaFiles.addAll( FileUtils.listFiles( getMediaDirectory(), null, true ) ) ;
		log.debug( "\tRetrieved existing media files" ) ;
		
		JNTextProcessor textProcessor = new JNTextProcessor( this, existingMediaFiles ) ;
				
		for( AbstractNotesElement ne : this.notesElements ) {
			ne.processNotesContent( textProcessor ) ;
		}
		
		// Now delete the media files that were present but not have been found
		// relevant in this version of source processing.
		for( File redundantFile : existingMediaFiles ) {
			log.debug( "Deleting redundant file - " + redundantFile.getAbsolutePath() ) ;
			FileUtils.deleteQuietly( redundantFile ) ;
		}
	}
	
	public String toString() {
		return getChapterFQN() ;
	}
	
	private String getSyllabusName( File file ) {
		
		String srcDirPath = config.getSrcDir().getAbsolutePath() ;
		String filePath   = file.getAbsolutePath() ;
		String relPath    = filePath.substring( srcDirPath.length() + 1 ) ;
		
		return relPath.substring( 0, relPath.indexOf( File.separatorChar ) ) ;
	}
}
