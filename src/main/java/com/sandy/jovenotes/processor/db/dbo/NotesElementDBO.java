package com.sandy.jovenotes.processor.db.dbo;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.cards.AbstractCard ;
import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.db.dao.ChapterDAO ;
import com.sandy.jovenotes.processor.db.dao.NotesElementDAO ;

public class NotesElementDBO {
    
    private static final Logger log = Logger.getLogger( NotesElementDBO.class ) ;

    private int     notesElementId  = -1 ;
    private int     chapterId       = -1 ;
    private String  section         = null ;
    private String  elementType     = null ;
    private int     difficultyLevel = 0 ;
    private String  content         = null ;
    private String  objCorrelId     = null ;
    private boolean ready           = true ;
    private boolean hiddenFromView  = false ;
    private String  scriptBody      = null ;
    private String  evalVars        = null ;
    
    private boolean sourceTrace = false ;
    private boolean isModified  = false ;
    private boolean isDeleted   = false ;
    
    private ChapterDBO chapter = null ;
    private List<CardDBO> cards = new ArrayList<CardDBO>() ;
    
    public NotesElementDBO( AbstractNotesElement ne ) throws Exception {
        
        section         = ne.getSection() ;
        elementType     = ne.getType() ;
        difficultyLevel = ne.getDifficultyLevel() ;
        content         = ne.getContent() ;
        objCorrelId     = ne.getObjId() ;
        ready           = ne.isReady() ;
        hiddenFromView  = ne.isHiddenFromView() ;
        scriptBody      = ne.getScriptBody() ;
        evalVars        = ne.getEvalVarsAsJSON() ;
        
        for( AbstractCard card : ne.getCards() ) {
            cards.add( new CardDBO( card ) ) ;
        }
    }
    
    public NotesElementDBO( ChapterDBO chapter, ResultSet rs ) throws Exception {
        
        this.chapter = chapter ;
        
        notesElementId  = rs.getInt    ( "notes_element_id" ) ;
        chapterId       = rs.getInt    ( "chapter_id"       ) ;
        section         = rs.getString ( "section"          ) ;
        elementType     = rs.getString ( "element_type"     ) ;
        difficultyLevel = rs.getInt    ( "difficulty_level" ) ;
        content         = rs.getString ( "content"          ) ;
        objCorrelId     = rs.getString ( "obj_correl_id"    ) ;
        ready           = rs.getBoolean( "ready"            ) ;
        hiddenFromView  = rs.getBoolean( "hidden_from_view" ) ;
        scriptBody      = rs.getString ( "script_body"      ) ;
        evalVars        = rs.getString ( "eval_vars"        ) ;
    }
    
    public int getNotesElementId() {
        return notesElementId;
    }

    public void setNotesElementId( int notesElementId ) {
        this.notesElementId = notesElementId ;
        for( CardDBO card : cards ) {
            card.setNotesElementId( notesElementId ) ;
        }
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
        for( CardDBO card : cards ) {
            card.setChapterId( chapterId ) ;
        }
    }
    
    public String getSection() {
        return this.section ;
    }

    public String getElementType() {
        return elementType;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public String getContent() {
        return content;
    }

    public String getObjCorrelId() {
        return objCorrelId;
    }
    
    public String getScriptBody() {
        return this.scriptBody ;
    }
    
    public String getEvalVars() {
        return this.evalVars ;
    }

    public ChapterDBO getChapter() throws Exception {
        if( this.chapter == null ) {
            this.chapter = ChapterDAO.get( getChapterId() ) ;
        }
        return this.chapter ;
    }
    
    public boolean isSourceTrace() {
        return sourceTrace;
    }

    public boolean isModified() {
        return isModified;
    }
    
    public boolean isReady() {
        return ready ;
    }
    
    public boolean isHiddenFromView() {
        return this.hiddenFromView ;
    }
    
    public List<CardDBO> getCards() {
        return this.cards ;
    }

    public boolean isDeleted() {
        return isDeleted ;
    }
    
    public void setDeleted( boolean deleted ) {
        isDeleted = true ;
        for( CardDBO card : cards ) {
            card.setDeleted( true ) ;
        }
    }
    
    public boolean trace( AbstractNotesElement ne ) throws Exception {
        
        if( !getObjCorrelId().equals( ne.getObjId() ) ) {
            throw new Exception( "Correlation id for NEDBO and NE don't match." ) ;
        }
        
        boolean updateRequired = false ;
        this.sourceTrace = true ;
        
        Map<String, CardDBO> dboMap = new HashMap<String, CardDBO>() ;
        for( CardDBO dbo : cards ) {
            dboMap.put( dbo.getObjCorrelId(), dbo ) ;
        }
        
        for( AbstractCard card : ne.getCards() ) {
            CardDBO cardDbo = dboMap.get( card.getObjId() ) ;
            if( cardDbo == null ) {
                cardDbo = new CardDBO( card ) ;
                cardDbo.setChapterId( getChapterId() ) ;
                cardDbo.setNotesElementId( getNotesElementId() ) ;
                cards.add( cardDbo ) ;
                if( !updateRequired ) updateRequired = true ;
                log.info( "\t      New card found for notes element " + 
                           getNotesElementId() ) ;
            }
            else {
                boolean bool = cardDbo.trace( card ) ;
                if( !updateRequired && bool ) updateRequired = true ;
            }
        }

        // Only if the notes is ready - implying that it's content will not be 
        // modified beyond this point, do we check for modification. This is 
        // a special case and applies for special elements such as spell bee.
        // In case of spell bee, the pronunciation is downloaded offline and 
        // hence at this point, the card is not ready - consequently we don't
        // have to check for modification.
        if( ne.isReady() ) {
            
            boolean contentEquals    = getContent().equals( ne.getContent() ) ;
            boolean difficultyEquals = getDifficultyLevel() == ne.getDifficultyLevel() ;
            boolean hiddenEquals     = isHiddenFromView()   == ne.isHiddenFromView() ;
            boolean scriptBodyEquals = ( ne.getScriptBody() == null ) ? 
                                       getScriptBody() == null : 
                                       ne.getScriptBody().equals( getScriptBody() ) ;
            boolean evalVarsEquals   = ( ne.getEvalVarsAsJSON() == null ) ?
                                       getEvalVars() == null :
                                       ne.getEvalVarsAsJSON().equals( getEvalVars() ) ;
            
            boolean sectionEquals    = ( this.section == null && ne.getSection() == null ) ;
            
            if( this.section != null && ne.getSection() != null ) {
                sectionEquals = this.section.equals( ne.getSection() ) ;
            }
            
            if( !( contentEquals && difficultyEquals && 
                   hiddenEquals && scriptBodyEquals && 
                   evalVarsEquals && sectionEquals ) ) {
                
                log.info( "\t      Notes element found modfied.. id=" + 
                           getNotesElementId() ) ;
                
                this.isModified      = true ;
                this.content         = ne.getContent() ;
                this.section         = ne.getSection() ;
                this.hiddenFromView  = ne.isHiddenFromView() ;
                this.difficultyLevel = ne.getDifficultyLevel() ;
                this.evalVars        = ne.getEvalVarsAsJSON() ;
                this.scriptBody      = ne.getScriptBody() ;
                
                updateRequired = true ;
            }
        }
        
        if( ne.getCards().size() != cards.size() ) {
            updateRequired = true ;
        }
        
        return updateRequired ;
    }
    
    /**
     * This function processes the modifications done to the object tree by 
     * the trace function.
     */
    public void processTrace() throws Exception {

        log.debug( "\t  Processing trace for NEDBO id = " + 
                   getNotesElementId() ) ;
        
        if( getNotesElementId() == -1 ) {
            log.info( "\t    Notes element will be created." ) ;
            try {
                NotesElementDAO.create( this ) ;
            }
            catch( Exception e ) {
                log.error( "Exception creating notes element.", e ) ;
                log.error( this.content ) ;
                throw e ;
            }
            return ;
        }
        else if( isModified ) {
            log.info( "\t    Notes element will be updated. id=" + 
                       getNotesElementId() ) ;
            try {
                NotesElementDAO.update( this ) ;
            }
            catch( Exception e ) {
                log.error( "Exception updating notes element.", e ) ;
                log.error( this.content ) ;
                throw e ;
            }
        }
        else if( !sourceTrace ) {
            log.info( "\t    Notes element will be deleted. id=" + 
                       getNotesElementId() ) ;
            try {
                NotesElementDAO.delete( this ) ;
            }
            catch( Exception e ) {
                log.error( "Exception deleting notes element.", e ) ;
                log.error( this.content ) ;
                throw e ;
            }
            return ;
            // The associated cards will be cascade deleted at the database.
            // No need to delete them explicitly.
        }
        
        for( CardDBO dbo : cards ) {
            dbo.processTrace() ;
        }
    }
}
