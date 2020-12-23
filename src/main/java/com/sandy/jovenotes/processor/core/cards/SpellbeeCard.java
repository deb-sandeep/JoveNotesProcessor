package com.sandy.jovenotes.processor.core.cards;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

public class SpellbeeCard extends AbstractCard {
    
    private String objIdSeed = null ;
    
    public SpellbeeCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE,
                         String objIdSeed ) 
        throws Exception {
        
        super( ne, rtcNE, SPELLBEE ) ;
        this.objIdSeed = objIdSeed ;
        super.ready = false ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    public int getDifficultyLevel() { return (int)(objIdSeed.length()*1.5) ; }
    public void collectContentAttributes( Map<String, Object> map ){} 
}

