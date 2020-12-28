package com.sandy.jovenotes.processor.db.dbo;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.db.dao.ChapterDAO ;
import com.sandy.jovenotes.processor.db.dao.NotesElementDAO ;

public class ChapterDBO {

    private static final Logger log = Logger.getLogger( ChapterDBO.class ) ;
    
    private int     chapterId      = -1 ;
    private String  syllabusName   = null ;
    private String  subjectName    = null ;
    private int     chapterNum     = 0 ;
    private int     subChapterNum  = 0 ;
    private String  chapterName    = null ;
    private boolean isExerciseBank = false ;
    private String  scriptBody     = null ;
    private String  chapterFQN     = null ;
    
    private List<NotesElementDBO> notesElements = null ;
    boolean tracedToSourceObjModel = false ;
    
    boolean isModified = false ;
    boolean isDeleted  = false ;
    
    public ChapterDBO( Chapter chapter ) throws Exception {
        
        chapterFQN     = chapter.getChapterFQN() ;
        
        syllabusName   = chapter.getSyllabusName() ;
        subjectName    = chapter.getSubjectName() ;
        chapterNum     = chapter.getChapterNumber() ;
        subChapterNum  = chapter.getSubChapterNumber() ;
        chapterName    = chapter.getChapterName() ;
        isExerciseBank = chapter.isExerciseBank() ;
        scriptBody     = chapter.getScriptBody() ;
        
        notesElements = new ArrayList<NotesElementDBO>() ;
        for( AbstractNotesElement ne : chapter.getNotesElements() ) {
            notesElements.add( new NotesElementDBO( ne ) ) ;
        }
    }
    
    public ChapterDBO( ResultSet rs ) throws Exception {
        
        chapterId     = rs.getInt    ( "chapter_id"      ) ;
        syllabusName  = rs.getString ( "syllabus_name"   ) ;
        subjectName   = rs.getString ( "subject_name"    ) ;
        chapterNum    = rs.getInt    ( "chapter_num"     ) ;
        subChapterNum = rs.getInt    ( "sub_chapter_num" ) ;
        chapterName   = rs.getString ( "chapter_name"    ) ;
        isExerciseBank= rs.getBoolean( "is_exercise_bank") ;
        scriptBody    = rs.getString ( "script_body"     ) ;
        
        notesElements = NotesElementDAO.getAll( this ) ;
    }
    
    /**
     * Traces the source object model into the database object model. The tracing
     * process can have the following consequences:
     * 
     * 1. Each of NotesElementDBO and CardDBO will have their 'sourceTrace'
     *    flag set to true or false depending on whether the corresponding source
     *    object was found.
     *    
     * 1.1 If for a source object a database object correlation is not found,
     *    a new DBO object is added to the databse object tree. Note that the
     *    new DBO objects will have their foreign key reference fields and thei
     *    own ids set as -1 (since they don't exist in the database yet). For
     *    these new DBOs, the sourceTrace flag will be false.
     *    
     * 2. If the 'sourceTrace' is true (source mapping found), a corresponding
     *    flag 'isModified' is set to true if it is found that the source object
     *    content and the database object content do not match.
     *    
     * @param chapter The source object model
     * 
     * @return true if an update to the chapter is required, false otherwise.
     */
    public boolean trace( Chapter chapter ) throws Exception {
        
        log.debug( "\tTracing source model to database model." ) ;
        
        boolean updateRequired = false ;
        
        Map<String, NotesElementDBO> dboMap = new HashMap<String, NotesElementDBO>() ;
        for( NotesElementDBO dbo : notesElements ) {
            dboMap.put( dbo.getObjCorrelId(), dbo ) ;
        }
        
        for( AbstractNotesElement ne : chapter.getNotesElements() ) {
            
            String objId = ne.getObjId() ;
            NotesElementDBO dbo = dboMap.get( objId ) ;
            
            if( dbo == null ) {
                dbo = new NotesElementDBO( ne ) ;
                dbo.setChapterId( this.getChapterId() ) ;
                notesElements.add( dbo ) ;
                if( !updateRequired ) updateRequired = true ;
                log.info( "\t    New notes element found. " + ne.getType() ) ;
            }
            else {
                boolean bool = dbo.trace( ne ) ;
                if( !updateRequired && bool )updateRequired = true ;
            }
        }
        
        checkIfChapterDetailsHaveChanged( chapter ) ;
        
        if( hasNumNotesElementsChanged( chapter ) ) {
            log.info( "\t    Number of cards have changed. " + 
                       "#C[old] = " + notesElements.size() + ", " + 
                       "#C[new] = " + chapter.getNotesElements().size() ) ;
            updateRequired = true ;
        }
        
        tracedToSourceObjModel = true ;
        return updateRequired ;
    }
    
    private void checkIfChapterDetailsHaveChanged( Chapter chapter ) {
        
        if( hasChapterNameChanged( chapter ) ) {
            log.info( "\t    Chapter name found changed. " + getChapterName() ) ;
            isModified = true ;
            this.chapterName = chapter.getChapterName() ;
        }
        
        if( hasScriptBodyChanged( chapter ) ) {
            log.info( "\t    Chapter script body found changed. " ) ;
            isModified = true ;
            this.scriptBody = chapter.getScriptBody() ;
        }
        
        if( hasExerciseBankFlagChanged( chapter ) ) {
            log.info( "\t    Chapter exercise bank flag changed. " ) ;
            isModified = true ;
            this.isExerciseBank = chapter.isExerciseBank() ;
        }
    }
    
    private boolean hasScriptBodyChanged( Chapter chapter ) {
        String chapSB = chapter.getScriptBody() ;
        String thisSB = getScriptBody() ;
        return !( chapSB == null ? thisSB == null : chapSB.equals( thisSB ) ) ;
    }
    
    private boolean hasChapterNameChanged( Chapter chapter ) {
        return !chapter.getChapterName().equals( getChapterName() ) ;
    }
    
    private boolean hasExerciseBankFlagChanged( Chapter chapter ) {
        return chapter.isExerciseBank() != isExerciseBank() ;
    }
    
    private boolean hasNumNotesElementsChanged( Chapter chapter ) {
        return chapter.getNotesElements().size() != notesElements.size() ;
    }
    
    /**
     * This function processes the modifications done to the object tree by 
     * the trace function.
     */
    public void processTrace() throws Exception {
        
        // Safeguard - enforcing that the object model be traced first before
        // this method is called. Else this method will wipe out all the data
        // since all the elements will be found not traced to source.
        if( !tracedToSourceObjModel ) {
            throw new Exception( "Can't process. Trace has not been called." ) ;
        }
        
        if( isModified ) {
            log.debug( "\t  Chapter name being updated. id=" + getChapterId() ) ;
            ChapterDAO.update( this ) ;
        }
        
        for( NotesElementDBO neDBO : notesElements ) {
            neDBO.processTrace() ;
        }
    }
    
    // -------------------- Bean Getter/Setter ---------------------------------
    public boolean isExerciseBank() {
        return isExerciseBank ;
    }
    
    public boolean isModified() {
        return this.isModified ;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId( int chapterId ) {
        this.chapterId = chapterId;
        for( NotesElementDBO ne : this.notesElements ) {
            ne.setChapterId( chapterId ) ; 
        }
    }

    public String getSyllabusName() {
        return syllabusName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getChapterNum() {
        return chapterNum;
    }

    public int getSubChapterNum() {
        return subChapterNum;
    }

    public String getChapterName() {
        return chapterName;
    }

    public List<NotesElementDBO> getNotesElements() throws Exception {
        return this.notesElements ;
    }

    public boolean isDeleted() {
        return isDeleted ;
    }
    
    public void setDeleted( boolean deleted ) {
        this.isDeleted = true ;
    }
    
    public String getScriptBody() {
        return this.scriptBody ;
    }
    
    public String getFQN() {
        return this.chapterFQN ;
    }
}
