package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.WM ;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.WordMeaning ;

public class WMElement extends AbstractNotesElement {
    
    private WordMeaning ast = null ;
    
    private String word = null ;
    private String meaning   = null ;
    
    public WMElement( Chapter chapter, WordMeaning ast, 
                      RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( WM, chapter, ast, rtcNE ) ;
        this.ast = ast ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.word    = textProcessor.processText( ast.getWord() ) ;
        this.meaning = textProcessor.processText( ast.getMeaning() ) ;
        
        String wmQ = "_What is the meaning of_\n\n**" + this.word + "**" ;
        String mqQ = "_Which word means_\n\n**" + this.meaning + "**" ;
        
        cards.add( new QACard( this, rtcNE, textProcessor, wmQ, ast.getMeaning(), null ) ) ;
        cards.add( new QACard( this, rtcNE, textProcessor, mqQ, ast.getWord(), null  ) ) ;
    }
    
    public String getObjIdSeed() { 
        return this.ast.getWord() ; 
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "word", word ) ;
        map.put( "meaning", meaning ) ;
    }
}

