package com.sandy.jovenotes.processor.core.cards;

import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

public class ImageLabelCard extends AbstractCard {
    
    private static final double DIFFICULTY_FACTOR = 0.26954 ;
    private static final double X_SHIFT = 0.0 ;
    private static final double MAX_LIMIT = 0.60 ;
    
    private String objIdSeed = null ;
    private Map<String, Object> contentAttributes = null ;
    
    public ImageLabelCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE,
                           String objIdSeed, Map<String, Object> contentAttributes )
        throws Exception {
        
        super( ne, rtcNE, IMGLABEL ) ;
        this.objIdSeed = objIdSeed ;
        this.contentAttributes = contentAttributes ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    @SuppressWarnings("unchecked")
    public int getDifficultyLevel() { 
        int x = ((List<List<Object>>)contentAttributes.get( "hotSpots" )).size() ;
        double nDiff = (2*(1/(1+Math.exp(-1*DIFFICULTY_FACTOR*(x-X_SHIFT)))-.5))*MAX_LIMIT ;
        return (int)Math.ceil( nDiff*100 ) ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ){
        map.putAll( contentAttributes ) ;
    } 
}

