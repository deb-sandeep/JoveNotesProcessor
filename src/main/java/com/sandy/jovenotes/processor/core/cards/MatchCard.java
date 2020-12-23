package com.sandy.jovenotes.processor.core.cards;

import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

public class MatchCard extends AbstractCard {
    
    private static final double DIFFICULTY_FACTOR = 0.26954 ;
    private static final double X_SHIFT = 0.0 ;
    private static final double MAX_LIMIT = 0.7 ;
    
    private List<List<String>> fmtMatchPairs = null ;
    private String matchCaption = null ;
    
    private String objIdSeed = null ;
    
    public MatchCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE, 
                      String objIdSeed, String matchCaption,
                      List<List<String>> fmtMatchPairs, 
                      JNTextProcessor textProcessor ) 
        throws Exception {
        
        super( ne, rtcNE, MATCHING ) ;
        
        this.objIdSeed     = objIdSeed ;
        this.fmtMatchPairs = fmtMatchPairs ;
        this.matchCaption  = matchCaption ;
        
        if( getRawRTCCaption() != null ) {
            this.matchCaption = "<blockquote>" + 
                                textProcessor.processText( getRawRTCCaption() ) + 
                                "</blockquote>\n\n" + 
                                this.matchCaption ;
        }
    }
    
    public String getObjIdSeed() { 
        return objIdSeed ;
    }
    
    public int getDifficultyLevel() {
        int x = fmtMatchPairs.size() ;
        double nDiff = (2*(1/(1+Math.exp(-1*DIFFICULTY_FACTOR*(x-X_SHIFT)))-.5))*MAX_LIMIT ;
        return (int)Math.ceil( nDiff*100 ) ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ) 
        throws Exception {
        map.put( "caption", matchCaption ) ;
        map.put( "matchData", fmtMatchPairs ) ;
    }
}

