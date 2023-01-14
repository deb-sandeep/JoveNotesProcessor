package com.sandy.jovenotes.processor.db.dao;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.processor.JoveNotesProcessor ;
import com.sandy.jovenotes.processor.db.dbo.ChapterSectionDBO ;

public class ChapterSectionDAO extends AbstractDAO {

    private static final Logger log = Logger.getLogger( ChapterSectionDAO.class ) ;
    
    public static List<ChapterSectionDBO> getAll( int chapterId ) 
        throws Exception {
        
        ArrayList<ChapterSectionDBO> sections = new ArrayList<>() ;
        
        final String sql = "SELECT " + 
                           "  chapter_id, " +
                           "  section, " + 
                           "  selected " +
                           "FROM " + 
                           "  jove_notes.chapter_section " +
                           "WHERE " +
                           "  chapter_id = ? " ;
        
        Connection conn = JoveNotesProcessor.db.getConnection() ;
        
        try {
            logQuery( "ChapterSectionDBO::getAll", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt( 1, chapterId ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                sections.add( new ChapterSectionDBO( rs ) ) ;
            }
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
        return sections ;
    }
    
    public static List<String> extractSections( int chapterId )
        throws Exception {
        
        ArrayList<String> sections = new ArrayList<>() ;
        
        final String sql = 
            "SELECT distinct( section ) "
          + "FROM jove_notes.card "
          + "WHERE chapter_id=? "
          + "ORDER BY section ASC " ;
        
        Connection conn = JoveNotesProcessor.db.getConnection() ;
        
        try {
            logQuery( "ChapterSectionDBO::extractSections", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt( 1, chapterId ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                sections.add( rs.getString( "section" ) ) ;
            }
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
        return sections ;
    }
    
    public static void create( ChapterSectionDBO section ) throws Exception {

        log.info( "\t    Creating section - " + 
                  section.getChapterId() + " :: " + section.getSection() ) ;
        
        final String sql = "INSERT INTO jove_notes.chapter_section ( " +
                           "  chapter_id, " + 
                           "  section, " +
                           "  selected " + 
                           ") " +
                           "VALUES " +
                           "( ?, ?, ? )" ;

        Connection conn = JoveNotesProcessor.db.getConnection() ;
        try {
            logQuery( "ChapterSectionDBO::create", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            
            psmt.setInt    ( 1, section.getChapterId() ) ;
            psmt.setString ( 2, section.getSection() ) ;
            psmt.setBoolean( 3, section.isSelected() ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
    }
    
    public static void delete( int chapterId ) throws Exception {
        
        final String sql = "DELETE FROM jove_notes.chapter_section WHERE chapter_id = ?" ;

        Connection conn = JoveNotesProcessor.db.getConnection() ;
        try {
            logQuery( "ChapterSectionDBO::delete", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt ( 1, chapterId ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
    }
}
