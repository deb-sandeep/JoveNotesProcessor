package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.cards.Cards.AbstractCard ;

public class CardDBO extends AbstractDBO {
    
    private static final Logger log = Logger.getLogger( CardDBO.class ) ;

    private int    cardId          = -1 ;
    private int    notesElementId  = -1 ;
    private int    chapterId       = -1 ;
    private String cardType        = null ;
    private int    difficultyLevel = 0 ;
    private String content         = null ;
    private String objCorrelId     = null ;
    private boolean ready          = true ;
    
    private ChapterDBO chapter = null ;
    private NotesElementDBO notesElement = null ;
    
    private boolean sourceTrace = false ;
    private boolean isModified  = false ;
    private boolean isDeleted   = false ;
    
    public CardDBO( AbstractCard card ) throws Exception {
        
        this.cardType        = card.getType() ;
        this.difficultyLevel = card.getDifficultyLevel() ;
        this.content         = card.getContent() ;
        this.objCorrelId     = card.getObjId() ;
        this.ready           = card.isReady() ;
    }
    
    private CardDBO( ResultSet rs ) throws Exception {
        
        cardId          = rs.getInt    ( "card_id"          ) ;
        notesElementId  = rs.getInt    ( "notes_element_id" ) ;
        chapterId       = rs.getInt    ( "chapter_id"       ) ;
        cardType        = rs.getString ( "card_type"        ) ;
        difficultyLevel = rs.getInt    ( "difficulty_level" ) ;
        content         = rs.getString ( "content"          ) ;
        objCorrelId     = rs.getString ( "obj_correl_id"    ) ;        
        ready           = rs.getBoolean( "ready"            ) ;        
    }
    
    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getNotesElementId() {
        return notesElementId;
    }

    public void setNotesElementId(int notesElementId) {
        this.notesElementId = notesElementId;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String elementType) {
        this.cardType = elementType;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getObjCorrelId() {
        return objCorrelId;
    }

    public void setObjCorrelId(String objCorrelId) {
        this.objCorrelId = objCorrelId;
    }
    
    public ChapterDBO getChapter() {
        return chapter;
    }

    public void setChapter(ChapterDBO chapter) {
        this.chapter = chapter;
    }

    public NotesElementDBO getNotesElement() {
        return notesElement;
    }

    public void setNotesElement(NotesElementDBO notesElement) {
        this.notesElement = notesElement;
    }

    public boolean isSourceTrace() {
        return sourceTrace;
    }

    public boolean isModified() {
        return isModified;
    }
    
    public boolean isDeleted() {
        return isDeleted ;
    }
    
    public void setDeleted( boolean deleted ) {
        this.isDeleted = deleted ;
    }
    
    public boolean isReady() {
        return ready ;
    }

    public static List<CardDBO> getAll( int chapterId )
            throws Exception {
            
        ArrayList<CardDBO> cards = new ArrayList<CardDBO>() ;
        
        final String sql = 
                " SELECT  " +
                "   `card`.`card_id`, " +
                "   `card`.`notes_element_id`, " +
                "   `card`.`chapter_id`, " +
                "   `card`.`card_type`, " +
                "   `card`.`difficulty_level`, " +
                "   `card`.`content`, " +
                "   `card`.`obj_correl_id`, " +
                "   `card`.`ready` " +
                " FROM " +
                "   `jove_notes`.`card` " +
                " WHERE " +
                "   `card`.`chapter_id` = ? " + 
                " ORDER BY " + 
                "   `card`.`card_id` ASC";

        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "CardDBO::getAll", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt( 1, chapterId ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            while( rs.next() ) {
                cards.add( new CardDBO( rs ) ) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        return cards ;
    }

    public int create() throws Exception {

        log.info( "\t    Creating card - " + 
                   getCardType() + "::" + getObjCorrelId() ) ;
        
        final String sql = 
        "INSERT INTO `jove_notes`.`card` " +
        "(`notes_element_id`, `chapter_id`, `card_type`, `difficulty_level`, " +
        " `content`, `obj_correl_id`, `ready`) " +
        "VALUES " +
        "(?, ?, ?, ?, ?, ?, ? )" ;

        int generatedId = -1 ;
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "CardDBO::create", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql, 
                                             Statement.RETURN_GENERATED_KEYS ) ;
            
            psmt.setInt    ( 1, getNotesElementId() ) ;
            psmt.setInt    ( 2, getChapterId() ) ;
            psmt.setString ( 3, getCardType() ) ;
            psmt.setInt    ( 4, getDifficultyLevel() ) ;
            psmt.setString ( 5, getContent() ) ;
            psmt.setString ( 6, getObjCorrelId()  ) ;
            psmt.setBoolean( 7, isReady() ) ;
            
            psmt.executeUpdate() ;
            ResultSet rs = psmt.getGeneratedKeys() ;
            if( null != rs && rs.next()) {
                 generatedId = (int)rs.getLong( 1 ) ;
            }
            else {
                throw new Exception( "Autogenerated key not obtained for card." ) ;
            }
            setCardId( generatedId ) ;
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        return generatedId ;
    }
    
    public void update() throws Exception {
        
        final String sql = 
            "UPDATE `jove_notes`.`card` " +
            "SET " +
            "`difficulty_level` = ?, " +
            "`content` = ? " +
            "WHERE `card_id` = ? " ;

        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "CardDBO::update", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt    ( 1, getDifficultyLevel() ) ;
            psmt.setString ( 2, getContent() ) ;
            psmt.setInt    ( 3, getCardId() ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
    }

    public void delete() throws Exception {
        
        final String sql = 
            "DELETE FROM `jove_notes`.`card` WHERE `card_id` = ?" ;

        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "CardDBO::delete", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt ( 1, getCardId() ) ;
            
            psmt.executeUpdate() ;
            this.isDeleted = true ;
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
    }
    
    public boolean trace( AbstractCard card ) throws Exception {
        
        if( !getObjCorrelId().equals( card.getObjId() ) ) {
            throw new Exception( "Correlation id for CardDBO and Card don't match." ) ;
        }
        
        this.sourceTrace = true ;

        // Only if the notes is ready - implying that it's content will not be 
        // modified beyond this point, do we check for modification. This is 
        // a special case and applies for special elements such as spell bee.
        // In case of spell bee, the pronunciation is downloaded offline and 
        // hence at this point, the card is not ready - consequently we don't
        // have to check for modification.
        if( card.isReady() ) {
            
            boolean contentEquals    = getContent().equals( card.getContent() ) ;
            boolean difficultyEquals = getDifficultyLevel() == card.getDifficultyLevel() ;
            
            if( !( contentEquals && difficultyEquals ) ) {
                log.info( "\t           Card found modififed. id=" + getCardId() ) ;
                this.isModified = true ;
                this.content = card.getContent() ;
                this.difficultyLevel = card.getDifficultyLevel() ;
                return true ;
            }
        }
        
        return false ;
    }
    
    /**
     * This function processes the modifications done to the object tree by 
     * the trace function.
     */
    public void processTrace() throws Exception {

        if( getCardId() == -1 ) {
            log.info( "\t    Card will be created." ) ;
            create() ;
        }
        else if( isModified ) {
            log.info( "\t    Card will be updated. id=" + getCardId() ) ;
            update() ;
        }
        else if( !sourceTrace ) {
            log.info( "\t    Card will be deleted. id=" + getCardId() ) ;
            delete() ;
        }
    }
}
