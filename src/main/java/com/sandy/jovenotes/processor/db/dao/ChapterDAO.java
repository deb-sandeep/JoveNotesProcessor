package com.sandy.jovenotes.processor.db.dao;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.sql.Statement ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.processor.JoveNotes ;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.db.dbo.ChapterDBO ;
import com.sandy.jovenotes.processor.db.dbo.NotesElementDBO ;

public class ChapterDAO extends AbstractDAO {

    private static final Logger log = Logger.getLogger( ChapterDAO.class ) ;
    
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

    public static int create( ChapterDBO chapter ) throws Exception {

        log.info( "\tCreating chapter - " + chapter.getFQN() ) ;
        
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
            
            psmt.setBoolean( 1, chapter.isExerciseBank() ) ;
            psmt.setString ( 2, chapter.getSyllabusName() ) ;
            psmt.setString ( 3, chapter.getSubjectName() ) ;
            psmt.setInt    ( 4, chapter.getChapterNum() ) ;
            psmt.setInt    ( 5, chapter.getSubChapterNum() ) ;
            psmt.setString ( 6, chapter.getChapterName() ) ;
            psmt.setString ( 7, chapter.getScriptBody() ) ;
            
            psmt.executeUpdate() ;
            ResultSet rs = psmt.getGeneratedKeys() ;
            if( null != rs && rs.next()) {
                 generatedId = (int)rs.getLong( 1 ) ;
            }
            else {
                throw new Exception( "Autogenerated key not obtained for chapter." ) ;
            }
            chapter.setChapterId( generatedId ) ;
            for( NotesElementDBO ne : chapter.getNotesElements() ) {
                NotesElementDAO.create( ne ) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        return generatedId ;
    }
    
    public static void update( ChapterDBO chapter ) throws Exception {
        
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
            psmt.setString ( 1, chapter.getChapterName() ) ;
            psmt.setString ( 2, chapter.getScriptBody() ) ;
            psmt.setBoolean( 3, chapter.isExerciseBank() ) ;
            psmt.setInt    ( 4, chapter.getChapterId() ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
    }

    public static void delete( ChapterDBO chapter ) throws Exception {
        
        final String sql = 
            "DELETE FROM `jove_notes`.`chapter` WHERE `chapter_id` = ?" ;

        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::delete", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt ( 1, chapter.getChapterId() ) ;
            
            psmt.executeUpdate() ;
            chapter.setDeleted( true ) ;
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
    }
    
}
