package com.sandy.jovenotes.processor.core.notes.element;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.Definition ;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.* ;

public class DefinitionElement extends AbstractNotesElement {
    
    private Definition ast = null ;
    
    private String term       = null ;
    private String definition = null ;
    private String cmapImg    = null ;
    
    public DefinitionElement( Chapter chapter, Definition ast, 
                              RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( DEFINITION, chapter, ast, rtcNE ) ;
        this.ast = ast ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.cmapImg    = textProcessor.processCMapAST( ast.getCmap() ) ;
        this.term       = textProcessor.processText( ast.getTerm() ) ;
        this.definition = textProcessor.processText( ast.getDefinition() ) ; 
        
        if( cmapImg != null ) {
            this.definition += "<p>{{@img " + this.cmapImg + "}}" ;
        }

        String fmtQ = "_Define_\n\n'**" + ast.getTerm().trim() + "**'" ;
        cards.add( new QACard( this, rtcNE, textProcessor,
                               fmtQ, ast.getDefinition(), cmapImg  ) ) ;
    }
    
    public String getObjIdSeed() { 
        return this.ast.getTerm() ; 
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "term", term ) ;
        map.put( "definition", definition ) ;
    }
}

