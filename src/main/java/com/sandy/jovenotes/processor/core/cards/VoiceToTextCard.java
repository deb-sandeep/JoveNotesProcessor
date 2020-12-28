package com.sandy.jovenotes.processor.core.cards;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.VoiceToText ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

/**
 * The voice2text card has the following JSON structure
 * 
 * {
 *   clipname          : "<The name of the clip, not including .mp3>",
 *   text              : "<The text of the clip>"
 * }
 * 
 * It is assumed that the clip is in the audio chapter directory.
 */
public class VoiceToTextCard extends AbstractCard {
    
    private String          objIdSeed   = null ;
    private VoiceToText     ast         = null ;
    private String          fmtAnswer   = null ;
    private JNTextProcessor txtProc     = null ;
    
    public VoiceToTextCard( AbstractNotesElement ne, 
                            RefToContextNotesElement rtcNE, 
                            String objIdSeed, 
                            VoiceToText ast, 
                            JNTextProcessor textProcessor ) 
        throws Exception {
        
        super( ne, rtcNE, VOICE2TEXT ) ;
        this.objIdSeed = objIdSeed ;
        this.ast = ast ;
        this.fmtAnswer = textProcessor.processText( ast.getText() ) ;
        this.txtProc = textProcessor ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public int getDifficultyLevel() { 
        
        int textLen = txtProc.getNormalizedWordsInFormattedText( fmtAnswer ) ;
        int difficulty = 0 ;
        
        if( textLen > 15 ) {
            difficulty = 100 ;
        }
        else if( textLen > 10 ) {
            difficulty = 75 ;
        }
        else if( textLen > 7 ) {
            difficulty = 50 ;
        }
        else {
            difficulty = 30 ;
        }
        return difficulty ; 
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        
        map.put( "clipName", ast.getClipName() ) ;
        map.put( "text",     fmtAnswer         ) ;
    }
}
