package com.sandy.jovenotes.processor.core.notes.element;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.ChemEquation ;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.* ;

public class ChemEquationElement extends AbstractNotesElement {
    
    private String objIdSeed = null ;
    private String reactants = null ;
    private String products  = null ;
    private String produces  = null ;
    private String equation  = null ;
    
    private String description = null ;
    private String fmtDescr    = null ;
    
    public ChemEquationElement( Chapter chapter, ChemEquation ast, 
                                RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( CHEM_EQUATION, chapter, ast, rtcNE ) ;
        reactants   = ast.getReactants() ;
        produces    = (ast.getProduces() == null)? "->" : ast.getProduces();
        products    = ast.getProducts() ;
        description = ast.getDescription() ;
        
        equation = reactants + " " + produces + " " + products ;
        
        this.objIdSeed = reactants + produces ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        if( description != null ) {
            fmtDescr = textProcessor.processText( description ) ;
            cards.add( new QACard( this, rtcNE, textProcessor, 
                "_Write the chemical equation described by:_\n\n" + description,
                "$$\\ce{" + equation + "}$$", null ) ) ;
        }
        
        cards.add( new QACard( this, rtcNE, textProcessor, 
                "$$\\ce{" + reactants + " " + produces + "} " + getBlanks( products ) + "$$",
                "$$\\ce{" + equation + "}$$", null ) ) ;
        
        cards.add( new QACard( this, rtcNE, textProcessor, 
                "$$" + getBlanks( reactants ) + " \\ce{" + produces + " " + products + "}$$",
                "$$\\ce{" + equation + "}$$", null ) ) ;
    }
    
    private String getBlanks( String string ) {
        
        StringBuilder buffer = new StringBuilder() ;
        String[] parts = string.split( "\\s+\\+\\s+" ) ;
        for( int i=0; i<parts.length; i++ ) {
            buffer.append( "\\_\\_\\_\\_" ) ;
            if( i < parts.length-1 ) {
                buffer.append( " + " ) ;
            }
        }
        return buffer.toString() ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ){
        
        map.put( "equation", "$$\\ce{" + equation + "}$$" ) ;
        map.put( "description", fmtDescr ) ;
    }
}

