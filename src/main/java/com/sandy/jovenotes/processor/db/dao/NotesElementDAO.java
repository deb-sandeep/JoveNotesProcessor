package com.sandy.jovenotes.processor.db.dao;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.TEACHER_NOTE ;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.sql.Statement ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.processor.JoveNotesProcessor ;
import com.sandy.jovenotes.processor.core.stat.Stats ;
import com.sandy.jovenotes.processor.db.dbo.CardDBO ;
import com.sandy.jovenotes.processor.db.dbo.ChapterDBO ;
import com.sandy.jovenotes.processor.db.dbo.NotesElementDBO ;

public class NotesElementDAO extends AbstractDAO {
    
    private static final Logger log = Logger.getLogger( NotesElementDAO.class ) ;

    public static List<NotesElementDBO> getAll( ChapterDBO chapter )
            throws Exception {
            
        ArrayList<NotesElementDBO> elements = new ArrayList<NotesElementDBO>() ;
        
        final String sql = 
            "SELECT " +
            "`notes_element`.`notes_element_id`, " +
            "`notes_element`.`chapter_id`, " +
            "`notes_element`.`element_type`, " +
            "`notes_element`.`difficulty_level`, " +
            "`notes_element`.`content`, " +
            "`notes_element`.`eval_vars`, " +
            "`notes_element`.`script_body`, " +
            "`notes_element`.`obj_correl_id`, " +
            "`notes_element`.`ready`, " +
            "`notes_element`.`hidden_from_view` " +
            "FROM " +
            "`jove_notes`.`notes_element` " +
            "WHERE " + 
            "`notes_element`.`chapter_id` = ? " +
            "ORDER BY " + 
            "`notes_element`.`notes_element_id` ASC ";

        Connection conn = JoveNotesProcessor.db.getConnection() ;
        try {
            logQuery( "NotesElementDBO::getAll", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt( 1, chapter.getChapterId() ) ;
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                elements.add( new NotesElementDBO( chapter, rs ) ) ;
            }
            
            loadAndAssociateCardsWithNotesElements( chapter, elements ) ;
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
        
        return elements ;
    }
    
    private static void loadAndAssociateCardsWithNotesElements( 
            ChapterDBO chapter, ArrayList<NotesElementDBO> notesElements ) 
        throws Exception {
        
        HashMap<Integer, NotesElementDBO> notesElementMap = null ;
        notesElementMap = new HashMap<Integer, NotesElementDBO>() ;
        
        for( NotesElementDBO notesElement : notesElements ) {
            notesElementMap.put( notesElement.getNotesElementId(), notesElement ) ;
        }
        
        for( CardDBO card : CardDAO.getAll( chapter.getChapterId() ) ) {
            NotesElementDBO ne = notesElementMap.get( card.getNotesElementId() ) ;
            if( ne != null ){
                ne.getCards().add( card ) ;
                card.setNotesElement( ne ) ;
                card.setChapter( chapter ) ;
            }
            else {
                log.error( "Orphan card found!! card id = " + card.getCardId() ) ;
            }
        }
    }

    public static int create( NotesElementDBO note ) throws Exception {

        log.info( "\t  Creating notes element - " + 
                  note.getElementType() + "::" + note.getObjCorrelId() ) ;
        
        final String sql = 
        "INSERT INTO `jove_notes`.`notes_element` " +
        "(`chapter_id`, `element_type`, `difficulty_level`, " + 
        "`content`, `eval_vars`, `script_body`, `obj_correl_id`, `ready`, " + 
        "`hidden_from_view` ) " +
        "VALUES " +
        "( ?, ?, ?, ?, ?, ?, ?, ?, ? )" ;

        int generatedId = -1 ;
        Connection conn = JoveNotesProcessor.db.getConnection() ;
        try {
            logQuery( "NotesElementDBO::create", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql, 
                                             Statement.RETURN_GENERATED_KEYS ) ;
            
            psmt.setInt    ( 1, note.getChapterId() ) ;
            psmt.setString ( 2, note.getElementType() ) ;
            psmt.setInt    ( 3, note.getDifficultyLevel() ) ;
            psmt.setString ( 4, note.getContent() ) ;
            psmt.setString ( 5, note.getEvalVars() ) ;
            psmt.setString ( 6, note.getScriptBody() ) ;
            psmt.setString ( 7, note.getObjCorrelId() ) ;
            psmt.setBoolean( 8, note.isReady() ) ;
            psmt.setBoolean( 9, note.isHiddenFromView() ) ;
            
            psmt.executeUpdate() ;
            ResultSet rs = psmt.getGeneratedKeys() ;
            if( null != rs && rs.next()) {
                 generatedId = (int)rs.getLong( 1 ) ;
            }
            else {
                throw new Exception( "Autogenerated key not obtained for notes element." ) ;
            }
            note.setNotesElementId( generatedId ) ;
            
            if( note.getElementType().equals( TEACHER_NOTE ) ) {
                Stats.cardCreated( TEACHER_NOTE ) ;
            }
            
            for( CardDBO card : note.getCards() ) {
                CardDAO.create( card ) ;
            }
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
        return generatedId ;
    }
    
    public static void update( NotesElementDBO note ) throws Exception {
        
        final String sql = 
            "UPDATE `jove_notes`.`notes_element` " +
            "SET " +
            "`difficulty_level` = ?, " +
            "`content` = ?, " +
            "`eval_vars` = ?, " +
            "`script_body` = ?, " +
            "`hidden_from_view` = ? " +
            "WHERE `notes_element_id` = ? " ;

        Connection conn = JoveNotesProcessor.db.getConnection() ;
        try {
            logQuery( "NotesElementDBO::update", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt    ( 1, note.getDifficultyLevel() ) ;
            psmt.setString ( 2, note.getContent() ) ;
            psmt.setString ( 3, note.getEvalVars() ) ;
            psmt.setString ( 4, note.getScriptBody() ) ;
            psmt.setBoolean( 5, note.isHiddenFromView() ) ;
            psmt.setInt    ( 6, note.getNotesElementId() ) ;
            
            psmt.executeUpdate() ;
            
            if( note.getElementType().equals( TEACHER_NOTE ) ) {
                Stats.cardUpdated( TEACHER_NOTE ) ;
            }
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
    }

    public static void delete( NotesElementDBO note ) throws Exception {
        
        final String sql = 
            "DELETE FROM `jove_notes`.`notes_element` WHERE `notes_element_id` = ?" ;

        Connection conn = JoveNotesProcessor.db.getConnection() ;
        try {
            logQuery( "NotesElementDBO::delete", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt ( 1, note.getNotesElementId() ) ;
            
            psmt.executeUpdate() ;
            note.setDeleted( true ) ;
            
            if( note.getElementType().equals( TEACHER_NOTE ) ) {
                Stats.cardDeleted( TEACHER_NOTE ) ;
            }
            
            for( CardDBO card : note.getCards() ) {
                Stats.cardDeleted( card.getCardType() ) ;
            }
        }
        finally {
            JoveNotesProcessor.db.returnConnection( conn ) ;
        }
    }
}
