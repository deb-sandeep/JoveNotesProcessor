package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.MULTI_CHOICE ;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.MultiChoiceCard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.MultiChoice ;
import com.sandy.xtext.joveNotes.Option ;

public class MultiChoiceElement extends AbstractNotesElement {
    
    private String objIdSeed  = null ;
    private MultiChoice ast   = null ;
    
    private String fmtQ = null ;
    private String fmtA = null ;
    
    public MultiChoiceElement( Chapter chapter, MultiChoice ast, 
                               RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( MULTI_CHOICE, chapter, ast, rtcNE ) ;
        this.objIdSeed = constructObjId( ast ) ;
        this.ast = ast ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        constructQuestionAndAnswerText( textProcessor ) ;
        cards.add( new MultiChoiceCard( this, rtcNE, objIdSeed, 
                                        this.ast, textProcessor ) ) ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ){
        map.put( "question", fmtQ ) ;
        map.put( "answer",   fmtA ) ;
    }
    
    /**
     * Unique identification of a multiple choice question is done by the
     * concatenation of the question string and all the correct answers.
     * 
     * This leaves the tolerance of modifying the explanation and any 
     * incorrect answers without impacting the identity of the question.
     */
    private String constructObjId( MultiChoice ast ) {
        
        StringBuilder buffer = new StringBuilder() ;
        buffer.append( ast.getQuestion() ) ;
        for( Option opt : ast.getOptions() ) {
            if( opt.getOptionValue() != null ) {
                buffer.append( opt.getOptionValue() ) ;
            }
        }
        return buffer.toString() ;
    }
    
    /**
     * Constructs the question text in the following format:
     *
     * [Question text]\n\n
     * * 1. [Option 1]
     * * 2. [Option 2]
     * 
     * Answer text is constructed as follows:
     * * 2. [Option 2]
     * 
     * [Explanation]
     */
    private void constructQuestionAndAnswerText( JNTextProcessor textProcessor ) 
        throws Exception {
        
        StringBuilder qBuffer = new StringBuilder() ;
        StringBuilder aBuffer = new StringBuilder() ;
        
        qBuffer.append( this.ast.getQuestion() ) ;
        qBuffer.append( "\n\n" ) ;
        
        for( int i=0; i<ast.getOptions().size(); i++ ) {
            
            Option opt = ast.getOptions().get( i ) ;
            String optValue = opt.getOptionValue() ;
            
            qBuffer.append( "* [" + (i+1) + "] " + optValue + "\n" ) ;
            if( opt.getCorrectOption() != null ) {
                aBuffer.append( "* [" + (i+1) + "] " + optValue + "\n" ) ;
            }
        }
        
        if( this.ast.getExplanation() != null ) {
            String fmtE = this.ast.getExplanation() ;
            aBuffer.append( "\n\n" + fmtE ) ;
        }
        
        this.fmtQ = textProcessor.processText( qBuffer.toString() ) ;
        this.fmtA = textProcessor.processText( aBuffer.toString() ) ;
    }
}

