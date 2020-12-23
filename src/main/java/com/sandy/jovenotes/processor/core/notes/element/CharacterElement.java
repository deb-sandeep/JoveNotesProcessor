package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.CHARACTER ;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.Character ;

public class CharacterElement extends AbstractNotesElement {
    
    private Character ast = null ;
    
    private String character  = null ;
    private String estimate   = null ;
    private String cmapImg    = null ;
    
    public CharacterElement( Chapter chapter, Character ast, 
                             RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( CHARACTER, chapter, ast, rtcNE ) ;
        this.ast = ast ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.cmapImg   = textProcessor.processCMapAST( ast.getCmap() ) ;
        this.character = textProcessor.processText( ast.getCharacter() ) ;
        this.estimate  = textProcessor.processText( ast.getEstimate() ) ; 
        
        if( cmapImg != null ) {
            this.estimate += "<p>{{@img " + this.cmapImg + "}}" ;
        }

        String fmtQ = "_Give an estimate of_\n\n" + 
                      "'**" + ast.getCharacter() + "**'" ;
        cards.add( new QACard( this, rtcNE, textProcessor,
                               fmtQ, ast.getEstimate(), cmapImg  ) ) ;
    }
    
    public String getObjIdSeed() { 
        return this.ast.getCharacter() ; 
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "character", character ) ;
        map.put( "estimate",  estimate ) ;
    }
}

