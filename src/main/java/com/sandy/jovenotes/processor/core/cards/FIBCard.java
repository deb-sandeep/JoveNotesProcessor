package com.sandy.jovenotes.processor.core.cards;

import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

public class FIBCard extends AbstractCard {
    
    private static final double DIFFICULTY_FACTOR = 0.3 ;
    private static final double X_SHIFT = -0.1 ;
    private static final double MAX_LIMIT = 0.15 ;
    
    private String       fmtQuestion = null ;
    private List<String> fmtAnswers  = null ;
    
    public FIBCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE,
                    String rawQ, List<String> fmtAnsList, 
                    JNTextProcessor textProcessor )
        throws Exception {
        
        super( ne, rtcNE, FIB ) ;
        this.fmtAnswers    = fmtAnsList ;
        
        if( getRawRTCCaption() != null ) {
            this.fmtQuestion = "<blockquote>" + 
                               textProcessor.processText( getRawRTCCaption() ) + 
                               "</blockquote>\n\n" + 
                               textProcessor.processText( rawQ ) ;
        }
        else {
            this.fmtQuestion = textProcessor.processText( rawQ ) ;
        }
    }
    
    public String getObjIdSeed() { 
        StringBuilder seed = new StringBuilder() ;
        for( String answer : fmtAnswers ) {
            seed.append( answer ) ;
        }
        return seed.toString() ;
    }
    
    public int getDifficultyLevel() {
        int x = fmtAnswers.size() ;
        double nDiff = (2*(1/(1+Math.exp(-1*DIFFICULTY_FACTOR*(x-X_SHIFT)))-.5))*MAX_LIMIT ;
        return (int)Math.ceil( nDiff*100 ) ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ) 
        throws Exception {
        
        map.put( "question", fmtQuestion ) ;
        map.put( "answers",  fmtAnswers ) ;
    }
}

