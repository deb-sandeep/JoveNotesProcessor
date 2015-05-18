package com.sandy.jovenotes.processor.core.notes;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement;
import com.sandy.jovenotes.processor.util.ConfigManager;
import com.sandy.jovenotes.processor.util.StringUtil;
import com.sandy.xtext.joveNotes.JoveNotes;
import com.sandy.xtext.joveNotes.NotesElement;
import com.sandy.xtext.joveNotes.ProcessingHints;

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
	
	public boolean shouldSkipProcessing() {
		ProcessingHints hints = notesAST.getProcessingHints() ;
		return ( hints != null ) && ( hints.getSkipGeneration() != null ) ;
	}
	
	public boolean isTestPaper() {
		return notesAST.getChapterDetails().getTestPaper() != null ;
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
	
	public String getUID() {
		return StringUtil.getHash( syllabusName + "/" +
								   getSubjectName() + "/" +
			                       getChapterNumber() + "/" +
	                               getSubChapterNumber() ) ;
	}
	
	public File getSourceFile() { return this.srcFile ; }
	
	public File getImagesFolder() { 
		return new File( this.srcFile.getParentFile(), "img" ) ;
	}
	
	private String getSyllabusName( File file ) {
		
		String srcDirPath = config.getSrcDir().getAbsolutePath() ;
		String filePath   = file.getAbsolutePath() ;
		String relPath    = filePath.substring( srcDirPath.length() + 1 ) ;
		
		return relPath.substring( 0, relPath.indexOf( File.separatorChar ) ) ;
	}
}
