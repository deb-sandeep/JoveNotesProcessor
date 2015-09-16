package com.sandy.jovenotes.processor.core.notes ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.Cards.QACard ;
import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.QuestionAnswer ;
import com.sandy.xtext.joveNotes.RefToContext ;

public class RefToContextNotesElement extends AbstractNotesElement {
    
    private String objIdSeed  = null ;
    private String context    = null ;
    private String fmtContext = null ;
    
    private List<List<String>> rawQAList = new ArrayList<List<String>>() ;
    private List<List<String>> fmtQAList = new ArrayList<List<String>>() ;
    
    public RefToContextNotesElement( Chapter chapter, RefToContext ast ) {
        
        super( NotesElements.RTC, chapter, ast ) ;
        
        this.context = ast.getContext() ;
        this.objIdSeed = ast.getContext().substring( 0, context.length()/5 ) ;
        
        for( QuestionAnswer astQ : ast.getQuestions() ) {
            
            List<String> aRawQA = new ArrayList<String>() ;
            aRawQA.add( astQ.getQuestion() ) ;
            aRawQA.add( astQ.getAnswerParts().get( 0 ) ) ;
            rawQAList.add( aRawQA ) ;
        }
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.fmtContext = textProcessor.processText( this.context ) ;
        
        for( List<String> aRawQA : rawQAList ) {
            List<String> aFmtQA = new ArrayList<String>() ;
            aFmtQA.add( textProcessor.processText( aRawQA.get(0) ) ) ;
            aFmtQA.add( textProcessor.processText( aRawQA.get(1) ) ) ;
            fmtQAList.add( aFmtQA ) ;
            
            cards.add( new QACard( this,
                    "<blockquote>" + context + "</blockquote>\n\n" + 
                    aRawQA.get(0), aRawQA.get(1), textProcessor ) ) ;
        }
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ){

        List<Map<String, String>> questionsObjArray = null ;
        
        questionsObjArray = new ArrayList<Map<String,String>>() ;
        
        for( List<String> aFmtQA : fmtQAList ) {
            Map<String, String> questionObj = new HashMap<String, String>() ;
            questionObj.put( "question", aFmtQA.get(0) ) ;
            questionObj.put( "answer",   aFmtQA.get(1) ) ;
            
            questionsObjArray.add( questionObj ) ;
        }
        
        map.put( "context",   this.fmtContext ) ;
        map.put( "questions", questionsObjArray ) ;
    }
}