package com.sandy.jovenotes.processor.core.cards;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

public class QACard extends AbstractCard {
    
    private static final double DIFFICULTY_FACTOR = 0.037505 ;
    
    private String fmtAnswer   = null ;
    private String fmtQuestion = null ;
    
    // REMOVE: This is a hack to preserve backward compatibility of RTC nested 
    // question introduction. This should be removed in April 2016. 
    // objIdSeed should be equal to the raqwQuestion.
    private String objIdSeed = null ;
    
    private JNTextProcessor textProcessor = null ;
    
    public QACard( AbstractNotesElement ne, RefToContextNotesElement rtcNE, 
                   JNTextProcessor textProcessor,
                   String rawQ, String rawA, String cmapImg  ) 
        throws Exception {
        
        super( ne, rtcNE, QA ) ;
        this.textProcessor = textProcessor ;
        this.fmtQuestion   = null ;
        this.objIdSeed     = rawQ ;
        
        String rawAnswer = rawA ;
        
        if( getRawRTCCaption() != null ) {
            this.objIdSeed = "<blockquote>" + 
                             getRawRTCCaption() + 
                             "</blockquote>\n\n" + 
                             rawQ ;
            
            this.fmtQuestion = "<blockquote>" + 
                               textProcessor.processText( getRawRTCCaption() ) + 
                               "</blockquote>\n\n" + 
                               textProcessor.processText( rawQ ) ;
        }
        else {
            this.fmtQuestion = textProcessor.processText( rawQ ) ;
        }
        
        if( cmapImg != null ) {
            rawAnswer += "<p>{{@img " + cmapImg + "}}" ;
        }
        
        this.fmtAnswer = textProcessor.processText( rawAnswer ) ;
    }
    
    public String getObjIdSeed() { 
        return this.objIdSeed ;
    }
    
    public int getDifficultyLevel() {
        
        int numWords = textProcessor.getNormalizedWordsInFormattedText( fmtAnswer ) ;
        double nDiff = (2/( 1 + Math.exp( -1*DIFFICULTY_FACTOR*numWords ))) - 1 ;
        
        return (int)Math.ceil( nDiff*100 ) ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ) 
        throws Exception {
        
        map.put( "question", fmtQuestion ) ;
        map.put( "answer",   fmtAnswer ) ;
    }
}

