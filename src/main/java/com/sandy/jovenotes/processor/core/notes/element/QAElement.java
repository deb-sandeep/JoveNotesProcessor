package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.QA ;

import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.QuestionAnswer ;

public class QAElement extends AbstractNotesElement {
    
    private QuestionAnswer ast = null ;
    
    private String fmtQuestion = null ;
    private String fmtAnswer   = null ;
    private String cmapImg  = null ;
    
    public QAElement( Chapter chapter, QuestionAnswer ast, 
                      RefToContextNotesElement rtcNE ) 
        throws Exception {
        
        super( QA, chapter, ast, rtcNE ) ;
        this.ast = ast ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        String consRawA = consolidateAnswerParts( this.ast.getAnswerParts(),
                                                  textProcessor ) ;
        
        this.cmapImg     = textProcessor.processCMapAST( ast.getCmap() ) ;
        this.fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
        this.fmtAnswer   = textProcessor.processText( consRawA ) ; 
        
        if( cmapImg != null ) {
            this.fmtAnswer += "<p>{{@img " + this.cmapImg + "}}" ;
        }
        cards.add( new QACard( this, rtcNE, textProcessor, 
                               ast.getQuestion(), consRawA, cmapImg ) ) ;
    }
    
    public String getObjIdSeed() { 
        return this.ast.getQuestion() ; 
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "question", fmtQuestion ) ;
        map.put( "answer",   fmtAnswer ) ;
    }
    
    private String consolidateAnswerParts( List<String> textParts,
                                           JNTextProcessor textProcessor ) 
        throws Exception {

        String consolidatedAnswer = "No answer text provided" ;
        int numAnswerParts = textParts.size() ;

        if( numAnswerParts == 1 ) {
            consolidatedAnswer = textParts.get( 0 ) ;
        }
        else if( numAnswerParts > 1 ){
            StringBuilder buffer = new StringBuilder() ;
            
            buffer.append( "<table class=\"cons_ans\">" ) ;
            buffer.append( "<tr>" ) ; 
            for( String part : textParts ) {
                buffer.append( "<td style=\"vertical-align:top\">" )
                      .append( textProcessor.processText( part ) )
                      .append( "</td>" ) ; 
            }
            buffer.append( "</tr>" ) ; 
            buffer.append( "</table>" ) ;
            
            consolidatedAnswer = buffer.toString() ;
        }
        
        return consolidatedAnswer ;
    }       
}

