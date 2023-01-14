package com.sandy.jovenotes.processor.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.notes.NoteElementBuilder ;
import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.util.ConfigManager;
import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.jovenotes.processor.util.StringUtil ;
import com.sandy.xtext.joveNotes.ChapterDetails ;
import com.sandy.xtext.joveNotes.ChapterSection ;
import com.sandy.xtext.joveNotes.CompilerBreak ;
import com.sandy.xtext.joveNotes.Exercise ;
import com.sandy.xtext.joveNotes.JoveNotes;
import com.sandy.xtext.joveNotes.NotesElement;

public class Chapter {
    
    private static final Logger log = Logger.getLogger( Chapter.class ) ;

    private ConfigManager config = null ;
    
    private File      srcFile        = null ;
    private String    syllabusName   = null ;
    private JoveNotes notesAST       = null ;
    private String    scriptBody     = null ;
    private boolean   isExerciseBank = false ;
    
    private ChapterDetails chapterDetails = null ;
    
    private ArrayList<AbstractNotesElement> notesElements = null ;
    
    private Map<String, Boolean> distinctNEMap = new HashMap<String, Boolean>() ;
    
    public Chapter( File baseDir, File srcFile, JoveNotes notesAST ) 
        throws Exception {
        
        this.config = com.sandy.jovenotes.processor.JoveNotesProcessor.config ;
        this.chapterDetails = notesAST.getChapterDetails() ;
        
        this.notesElements = new ArrayList<AbstractNotesElement>() ;
        this.srcFile       = srcFile ;
        this.syllabusName  = getSyllabusName( baseDir, srcFile ) ;
        this.notesAST      = notesAST ;
        this.scriptBody    = getScriptBody() ;
    
        this.isExerciseBank = ( chapterDetails.getExerciseBank() != null ) ; 
        
        log.debug( "\tObject transforming chapter - " + getChapterFQN() ) ;
        
        String currentSectionName = "00 - Default" ;
        
        for( NotesElement element : this.notesAST.getNotesElements() ) {
            
            if( !this.isExerciseBank && ( element instanceof Exercise ) ) {
                throw new Exception( 
                        "Found an @exercise element in a source " + 
                        "file, which is not marked as @exercise_bank." ) ;
            }
            
            if( element instanceof CompilerBreak ) {
                log.info( "\t  Compiler break encountered. Skipping further parsing." ) ;
                break ;
            }
            
            if( element instanceof ChapterSection ) {
                ChapterSection section = ( ChapterSection )element ;
                currentSectionName = section.getSectionName() ;
                
                log.info( "\t  Chapter section encountered. " +
                          "[" + currentSectionName + "]" ) ;
                
                if( StringUtil.isEmptyOrNull( currentSectionName ) ) {
                    throw new Exception( 
                            "Chapter section encountered but section name is " +
                            "empty or null." ) ;
                }
                continue ;
            }
            
            AbstractNotesElement ne = NoteElementBuilder.build( this, element, currentSectionName, null ) ; 
            String distinctKey = ne.getType() + "-" + ne.getObjIdSeed() ;
            
            if( distinctNEMap.containsKey( distinctKey ) ) {
                log.warn( "\t  Duplicate notes element found. Skipping. " +
                          "key = " + distinctKey ) ;
            }
            else {
                distinctNEMap.put( distinctKey, Boolean.TRUE ) ;
                notesElements.add(  ne ) ;
            }
        }
        initializeNotesElements() ;
    }
    
    private void initializeNotesElements() 
            throws Exception {
            
        log.debug( "\tProcessing notes elements" ) ;
        ArrayList<File> existingMediaFiles = new ArrayList<File>() ;
        existingMediaFiles.addAll( FileUtils.listFiles( getMediaDirectory(), null, true ) ) ;
        log.debug( "\tRetrieved existing media files" ) ;
        
        JNTextProcessor textProcessor = new JNTextProcessor( this, existingMediaFiles ) ;
                
        for( AbstractNotesElement ne : this.notesElements ) {
            ne.initialize( textProcessor ) ;
        }
        
        // Now delete the media files that were present but not have been found
        // relevant in this version of source processing.
        for( File redundantFile : existingMediaFiles ) {
            log.info( "\tDeleting redundant file - " + redundantFile.getAbsolutePath() ) ;
            FileUtils.deleteQuietly( redundantFile ) ;
        }
    }
        
    public List<AbstractNotesElement> getNotesElements() {
        return this.notesElements ;
    }
    
    public boolean isExerciseBank() {
        return this.isExerciseBank ;
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
    
    public String getScriptBody() {
        if( this.scriptBody != null ) {
            return this.scriptBody ;
        }
        else if( notesAST.getChapterDetails().getScriptBody() != null ) {
            return notesAST.getChapterDetails().getScriptBody().getScript() ;
        }
        return null ;
    }
    
    public String getChapterFQN() {
        return getSyllabusName() + "/" +
               getSubjectName() + "/" +
               getChapterNumber() + "/" +
               getSubChapterNumber() + "/" +
               getChapterName() ;
    }
    
    public File getSourceFile() { return this.srcFile ; }
    
    public File getMediaDirectory() {
        
        File dir =  new File( config.getDestMediaRootDir(), 
                              getSyllabusName() + File.separator + 
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
    
    public File getSrcAudioFolder() { 
        return new File( this.srcFile.getParentFile(), "audio" ) ;
    }
    
    public File getSrcDocFolder() { 
        return new File( this.srcFile.getParentFile(), "doc" ) ;
    }
    
    public String toString() {
        return getChapterFQN() ;
    }
    
    private String getSyllabusName( File baseDir, File file ) {
        
        String srcDirPath = baseDir.getAbsolutePath() ;
        String filePath   = file.getAbsolutePath() ;
        String relPath    = filePath.substring( srcDirPath.length() + 1 ) ;
        
        return relPath.substring( 0, relPath.indexOf( File.separatorChar ) ) ;
    }
}
