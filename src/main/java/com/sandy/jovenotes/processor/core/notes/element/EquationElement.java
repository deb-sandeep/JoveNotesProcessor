package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.EQUATION ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.MatchCard ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.EqSymbol ;
import com.sandy.xtext.joveNotes.Equation ;

public class EquationElement extends AbstractNotesElement {
    
    private Equation ast       = null ;
    private String   objIdSeed = null ;
    
    private String equation = null ;
    private String descr    = null ;
    List<List<String>> symbols = new ArrayList<List<String>>() ;
    
    public EquationElement( Chapter chapter, Equation ast, 
                            RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( EQUATION, chapter, ast, rtcNE ) ;
        this.ast = ast ;
        this.objIdSeed = ast.getEquation() ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        // Wrapping the user supplied equation in $$ will ensure that it
        // gets rendered by MathJax on the client. It can be argued that
        // we should not be amalgamating view concerns in the core data.
        // It's a potent objection! As of now, we are already doing a lot of
        // view processing during source transformation (Markdown for example).
        // I do believe that the core data should be render hint agnostic.
        // Maybe this is one of the things I need to look at sometimes in the
        // future iterations.
        equation  = "$$" + ast.getEquation() + "$$" ;
        descr     = textProcessor.processText( ast.getDescription() ) ;
        
        for( EqSymbol symbol : ast.getSymbols() ) {
            List<String> pair = new ArrayList<String>() ;
            pair.add( "\\(" + symbol.getSymbol() + "\\)" ) ;
            pair.add( textProcessor.processText( symbol.getDescription() ) ) ;
            symbols.add( pair ) ; 
        }
        
        cards.add( new QACard( this, rtcNE, textProcessor, 
                               "_What is the equation for_\n\n" + descr, 
                               equation, null ) ) ;
        
        if( symbols.size() > 2 ) {
            String matchCaption = "For the following equation, match the " + 
                                  "symbols. " + equation ;
            cards.add( new MatchCard( this, rtcNE, objIdSeed, 
                                      matchCaption, symbols, textProcessor ) ) ;
        }
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ){

        map.put( "equation",    equation ) ;
        map.put( "description", descr ) ;
        map.put( "symbols",     symbols ) ;
    }
}

