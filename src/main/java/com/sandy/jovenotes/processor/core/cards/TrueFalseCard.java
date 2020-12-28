package com.sandy.jovenotes.processor.core.cards;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

public class TrueFalseCard extends AbstractCard {
    
    private String  statement     = null ;
    private boolean truthValue    = false ;
    private String  justification = null ;
    
    private String objIdSeed = null ;
    
    public TrueFalseCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE, 
                          String objIdSeed, 
                          String statement, boolean truthValue, 
                          String justification,
                          JNTextProcessor textProcessor ) 
                                  throws Exception {
        
        super( ne, rtcNE, TF ) ;
        this.objIdSeed     = objIdSeed ;
        this.statement     = statement ;
        this.truthValue    = truthValue ;
        this.justification = justification ;
        
        if( getRawRTCCaption() != null ) {
            this.statement = "<blockquote>" + 
                             textProcessor.processText( getRawRTCCaption() ) + 
                             "</blockquote>\n\n" + 
                             this.statement ;
        }
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public int getDifficultyLevel() { return 5 ; }
    
    public void collectContentAttributes( Map<String, Object> map ) 
        throws Exception {
        map.put( "statement", statement ) ;
        map.put( "truthValue", Boolean.valueOf( truthValue ) ) ;
        if( justification != null ) {
            map.put( "justification", justification ) ; 
        }
    }
}

