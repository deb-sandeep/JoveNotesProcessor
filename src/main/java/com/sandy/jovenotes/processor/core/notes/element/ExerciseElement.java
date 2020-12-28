package com.sandy.jovenotes.processor.core.notes.element;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.ExerciseCard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.Exercise ;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.* ;

public class ExerciseElement extends AbstractNotesElement {
    
    private String   objIdSeed  = null ;
    private Exercise ast        = null ;
    
    private String       fmtQuestion = null ;
    private String       fmtAnswer   = null ;
    private List<String> fmtHints    = new ArrayList<String>() ;
    
    public ExerciseElement( Chapter chapter, Exercise ast, 
                            RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( EXERCISE, chapter, ast, rtcNE ) ;
        this.objIdSeed = constructObjId( ast ) ;
        this.ast = ast ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
        fmtAnswer   = textProcessor.processText( ast.getAnswer() ) ;
        for( String hint : ast.getHints() ) {
            fmtHints.add( textProcessor.processText( hint ) ) ;
        }
        
        cards.add( new ExerciseCard( this, rtcNE, objIdSeed, 
                                     this.ast, textProcessor ) ) ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ){
        
        map.put( "question", fmtQuestion ) ;
        map.put( "answer",   fmtAnswer   ) ;
        map.put( "hints",    fmtHints    ) ;
    }
    
    /**
     * UID is made by processing the question and answer. This implies that
     * the attributes (hide, marks, hints) can be modified without changing
     * the element identity.
     */
    private String constructObjId( Exercise ast ) {
        StringBuilder buffer = new StringBuilder() ;
        buffer.append( ast.getQuestion() )
              .append( ast.getAnswer() ) ;
        return buffer.toString() ;
    }
}


