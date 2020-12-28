package com.sandy.jovenotes.processor.db.dbo;

import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.cards.AbstractCard ;
import com.sandy.jovenotes.processor.db.dao.CardDAO ;

public class CardDBO {
    
    private static final Logger log = Logger.getLogger( CardDBO.class ) ;

    private int     cardId          = -1 ;
    private int     notesElementId  = -1 ;
    private int     chapterId       = -1 ;
    private String  cardType        = null ;
    private int     difficultyLevel = 0 ;
    private String  content         = null ;
    private String  objCorrelId     = null ;
    private boolean ready           = true ;
    
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
    
    public CardDBO( ResultSet rs ) throws Exception {
        
        cardId          = rs.getInt    ( "card_id"          ) ;
        notesElementId  = rs.getInt    ( "notes_element_id" ) ;
        chapterId       = rs.getInt    ( "chapter_id"       ) ;
        cardType        = rs.getString ( "card_type"        ) ;
        difficultyLevel = rs.getInt    ( "difficulty_level" ) ;
        content         = rs.getString ( "content"          ) ;
        objCorrelId     = rs.getString ( "obj_correl_id"    ) ;        
        ready           = rs.getBoolean( "ready"            ) ;        
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
            CardDAO.create( this ) ;
        }
        else if( isModified ) {
            log.info( "\t    Card will be updated. id=" + getCardId() ) ;
            CardDAO.update( this ) ;
        }
        else if( !sourceTrace ) {
            log.info( "\t    Card will be deleted. id=" + getCardId() ) ;
            CardDAO.delete( this ) ;
        }
    }

    // ------------------------------ Bean getter setters ----------------------
    public int getCardId() { return cardId; }
    public void setCardId(int val) { this.cardId = val; }
    
    public int getNotesElementId() { return notesElementId; }
    public void setNotesElementId(int val) { this.notesElementId = val; }
    
    public int getChapterId() { return chapterId; }
    public void setChapterId(int val) { this.chapterId = val; }
    
    public String getCardType() { return cardType; }
    public void setCardType(String val) { this.cardType = val; }
    
    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int val) { this.difficultyLevel = val; }
    
    public String getContent() { return content; }
    public void setContent(String val) { this.content = val; }
    
    public String getObjCorrelId() { return objCorrelId; }
    public void setObjCorrelId(String val) { this.objCorrelId = val; }    
    
    public ChapterDBO getChapter() { return chapter; }
    public void setChapter(ChapterDBO val) { this.chapter = val; }
    
    public NotesElementDBO getNotesElement() { return notesElement; }
    public void setNotesElement(NotesElementDBO val) { this.notesElement = val; }
    
    public boolean isDeleted() { return isDeleted ; }    
    public void setDeleted( boolean val ) { this.isDeleted = val ; }
    
    public boolean isSourceTrace() { return sourceTrace; }
    public boolean isModified() { return isModified; }    

    public boolean isReady() { return ready ; }
}
