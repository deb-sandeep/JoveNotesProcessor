package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.FIB ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.FIBCard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;

public class FIBElement extends AbstractNotesElement {
    
    private com.sandy.xtext.joveNotes.FIB ast = null ;
    
    private List<String> rawAnswers  = new ArrayList<String>() ;
    private List<String> fmtAnswers  = new ArrayList<String>() ;
    private String       fmtQuestion = null ;
    private String       objIdSeed   = null ;
    
    public FIBElement( Chapter chapter, com.sandy.xtext.joveNotes.FIB ast, 
                       RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( FIB, chapter, ast, rtcNE ) ;
        this.ast = ast ;
        
        StringBuilder seed = new StringBuilder( this.ast.getQuestion() ) ;
        for( String ans : ast.getAnswers() ) {
            this.rawAnswers.add( ans ) ;
            seed.append( ans ) ;
        }
        this.objIdSeed = seed.toString() ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
        for( String rawAns : this.rawAnswers ) {
            this.fmtAnswers.add( textProcessor.processText( rawAns ) ) ;
        }
        cards.add( new FIBCard( this, rtcNE, 
                                ast.getQuestion(), fmtAnswers, 
                                textProcessor ) ) ;
    }
    
    public String getObjIdSeed() { 
        return this.objIdSeed ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "question", fmtQuestion ) ;
        map.put( "answers",  fmtAnswers ) ;
    }
}

