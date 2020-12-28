package com.sandy.jovenotes.processor.core.notes.element;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.VoiceToTextCard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.VoiceToText ;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.* ;

public class VoiceToTextElement extends AbstractNotesElement {
    
    private String      objIdSeed  = null ;
    private VoiceToText ast        = null ;
    private String      fmtAnswer  = null ;
    
    public VoiceToTextElement( Chapter chapter, VoiceToText ast, 
                               RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( VOICE2TEXT, chapter, ast, rtcNE ) ;
        this.objIdSeed = ast.getText() ;
        this.ast = ast ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        textProcessor.processAudio( ast.getClipName() + ".mp3" ) ;
        fmtAnswer = textProcessor.processText( ast.getText() ) ;
        cards.add( new VoiceToTextCard( this, rtcNE, objIdSeed, 
                                        this.ast, textProcessor ) ) ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ){
        map.put( "clipName", ast.getClipName() ) ;
        map.put( "text",     fmtAnswer     ) ;
    }
}
