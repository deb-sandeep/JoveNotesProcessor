package com.sandy.jovenotes.processor.core.cards;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.Exercise ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;


/**
 * The exercise card has the following JSON structure
 * 
 * {
 *   question          : "Question text",
 *   answer            : "Answer text",
 *   hints             : [ "Hint1", "Hint2" ],
 * }
 */
public class ExerciseCard extends AbstractCard {
    
    private String       objIdSeed   = null ;
    private Exercise     ast         = null ;
    private String       fmtQuestion = null ;
    private String       fmtAnswer   = null ;
    private List<String> fmtHints    = new ArrayList<String>() ;
    
    
    public ExerciseCard( AbstractNotesElement ne, 
                         RefToContextNotesElement rtcNE, 
                         String objIdSeed, 
                         Exercise ast, 
                         JNTextProcessor textProcessor ) 
        throws Exception {
        
        super( ne, rtcNE, EXERCISE ) ;
        this.objIdSeed = objIdSeed ;
        this.ast = ast ;
        initialize( ast, textProcessor ) ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public int getDifficultyLevel() { return ast.getMarks() ; }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        
        map.put( "question", fmtQuestion ) ;
        map.put( "answer",   fmtAnswer   ) ;
        map.put( "hints",    fmtHints    ) ;
    }
    
    private void initialize( Exercise ast, JNTextProcessor textProcessor ) 
            throws Exception {

        fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
        fmtAnswer   = textProcessor.processText( ast.getAnswer() ) ;
        for( String hint : ast.getHints() ) {
            fmtHints.add( textProcessor.processText( hint ) ) ;
        }
    }
}


