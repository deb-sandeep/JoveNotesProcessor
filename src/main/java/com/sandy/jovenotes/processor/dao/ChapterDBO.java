package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;

public class ChapterDBO extends AbstractDBO {

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
    
    private ChapterDBO( ResultSet rs ) throws Exception {
        
        chapterId     = rs.getInt    ( "chapter_id"      ) ;
        syllabusName  = rs.getString ( "syllabus_name"   ) ;
        subjectName   = rs.getString ( "subject_name"    ) ;
        chapterNum    = rs.getInt    ( "chapter_num"     ) ;
        subChapterNum = rs.getInt    ( "sub_chapter_num" ) ;
        chapterName   = rs.getString ( "chapter_name"    ) ;
        isExerciseBank= rs.getBoolean( "is_exercise_bank") ;
        scriptBody    = rs.getString ( "script_body"     ) ;
        
        notesElements = NotesElementDBO.getAll( this ) ;
    }
    
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
    
    public static List<ChapterDBO> getAll() throws Exception {
        
        ArrayList<ChapterDBO> chapters = new ArrayList<ChapterDBO>() ;
        
        final String sql = "SELECT " + 
                           " `chapter`.`chapter_id`," + 
                           " `chapter`.`is_exercise_bank`," + 
                           " `chapter`.`syllabus_name`," +
                           " `chapter`.`subject_name`," +
                           " `chapter`.`chapter_num`," +
                           " `chapter`.`sub_chapter_num`," +
                           " `chapter`.`chapter_name`," + 
                           " `chapter`.`script_body`" + 
                           " FROM `jove_notes`.`chapter`" ;
        
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::getAll", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                chapters.add( new ChapterDBO( rs ) ) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        
        return chapters ;
    }
    
    public static ChapterDBO get( Chapter chapter ) throws Exception {
        
        final String sql = 
                "SELECT " + 
                " chapter_id, " + 
                " is_exercise_bank, " +
                " syllabus_name, " +
                " subject_name, " +
                " chapter_num, " +
                " sub_chapter_num, " +
                " chapter_name, " +
                " script_body " +
                "FROM " + 
                " jove_notes.chapter " +
                "WHERE " + 
                " syllabus_name   = ? and " + 
                " subject_name    = ? and " + 
                " chapter_num     = ? and " + 
                " sub_chapter_num = ? " ;

        ChapterDBO dbo = null ;
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::get", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setString ( 1, chapter.getSyllabusName() ) ;
            psmt.setString ( 2, chapter.getSubjectName() ) ;
            psmt.setInt    ( 3, chapter.getChapterNumber() ) ;
            psmt.setInt    ( 4, chapter.getSubChapterNumber() );
            
            ResultSet rs = psmt.executeQuery() ;
            if( rs.next() ) {
                dbo = new ChapterDBO( rs ) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        
        return dbo ;
    }
    
    public static ChapterDBO get( int chapterId ) throws Exception {
        
        final String sql = "SELECT " + 
                            " `chapter`.`chapter_id`," + 
                            " `chapter`.`is_exercise_bank`," + 
                            " `chapter`.`syllabus_name`," +
                            " `chapter`.`subject_name`," +
                            " `chapter`.`chapter_num`," +
                            " `chapter`.`sub_chapter_num`," +
                            " `chapter`.`chapter_name`," + 
                            " `chapter`.`script_body`" + 
                            " FROM `jove_notes`.`chapter` " +
                            " WHERE `chapter`.`chapter_id`=?" ;
        
        ChapterDBO chapter = null ;
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::get", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt( 1, chapterId ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            if( rs.next() ) {
                chapter = new ChapterDBO( rs ) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        
        return chapter ;
    }
    
    public int create() throws Exception {

        log.info( "\tCreating chapter - " + chapterFQN ) ;
        
        final String sql = 
        "INSERT INTO `jove_notes`.`chapter` " +
        "(`is_exercise_bank`, `syllabus_name`, `subject_name`, " + 
        " `chapter_num`, `sub_chapter_num`, `chapter_name`, `script_body` ) " +
        "VALUES " +
        "( ?, ?, ?, ?, ?, ?, ? )" ;

        int generatedId = -1 ;
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::create", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql, 
                                             Statement.RETURN_GENERATED_KEYS ) ;
            
            psmt.setBoolean( 1, isExerciseBank() ) ;
            psmt.setString ( 2, getSyllabusName() ) ;
            psmt.setString ( 3, getSubjectName() ) ;
            psmt.setInt    ( 4, getChapterNum() ) ;
            psmt.setInt    ( 5, getSubChapterNum() ) ;
            psmt.setString ( 6, getChapterName() ) ;
            psmt.setString ( 7, getScriptBody() ) ;
            
            psmt.executeUpdate() ;
            ResultSet rs = psmt.getGeneratedKeys() ;
            if( null != rs && rs.next()) {
                 generatedId = (int)rs.getLong( 1 ) ;
            }
            else {
                throw new Exception( "Autogenerated key not obtained for chapter." ) ;
            }
            setChapterId( generatedId ) ;
            for( NotesElementDBO ne : this.notesElements ) {
                ne.create() ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        return generatedId ;
    }
    
    public void update() throws Exception {
        
        final String sql = 
            "UPDATE `jove_notes`.`chapter` " +
            "SET " +
            "`chapter_name`     = ?, " +
            "`script_body`      = ?, " +
            "`is_exercise_bank` = ? " +
            "WHERE `chapter_id` = ? " ;

        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::update", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setString ( 1, getChapterName() ) ;
            psmt.setString ( 2, getScriptBody() ) ;
            psmt.setBoolean( 3, isExerciseBank() ) ;
            psmt.setInt    ( 4, getChapterId() ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
    }

    public void delete() throws Exception {
        
        final String sql = 
            "DELETE FROM `jove_notes`.`chapter` WHERE `chapter_id` = ?" ;

        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::delete", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt ( 1, getChapterId() ) ;
            
            psmt.executeUpdate() ;
            setDeleted( true ) ;
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
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
            update();
        }
        
        for( NotesElementDBO neDBO : notesElements ) {
            neDBO.processTrace() ;
        }
    }
}
