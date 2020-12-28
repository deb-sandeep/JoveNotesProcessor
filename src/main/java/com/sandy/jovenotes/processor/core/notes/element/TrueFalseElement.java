package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.TRUE_FALSE ;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.TrueFalseCard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.TrueFalse ;

public class TrueFalseElement extends AbstractNotesElement {
    
    private TrueFalse ast = null ;
    
    private String  statement     = null ;
    private boolean truthValue    = false ;
    private String  justification = null ;
    
    private String objIdSeed = null ;
    
    public TrueFalseElement( Chapter chapter, TrueFalse ast, 
                             RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( TRUE_FALSE, chapter, ast, rtcNE ) ;
        this.ast = ast ;
        this.objIdSeed = ast.getStatement() ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        statement = textProcessor.processText( ast.getStatement() ) ;
        truthValue = Boolean.parseBoolean( ast.getTruthValue() ) ;
        if( ast.getJustification() != null ) {
            justification = textProcessor.processText( ast.getJustification() ) ;
        }
        
        cards.add( new TrueFalseCard( this, rtcNE, objIdSeed,  
                                      statement, truthValue, 
                                      justification, textProcessor ) ) ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "statement", statement ) ;
        map.put( "truthValue", Boolean.valueOf( truthValue ) ) ;
        if( justification != null ) {
            map.put( "justification", justification ) ; 
        }
    }
}


