package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.EVENT ;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.Event ;

public class EventElement extends AbstractNotesElement {
    
    private Event ast = null ;
    
    private String time  = null ;
    private String event = null ;
    
    public EventElement( Chapter chapter, Event ast, 
                         RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( EVENT, chapter, ast, rtcNE ) ;
        this.ast = ast ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.time  = textProcessor.processText( ast.getTime() ) ;
        this.event = textProcessor.processText( ast.getEvent() ) ;
        
        String fmtTE = "_What happened in_\n\n**" + ast.getTime() + "** ?" ;
        String fmtET = "_When did the following happen_\n\n" + 
                       "**" + ast.getEvent() + "** ?" ;
        
        cards.add( new QACard( this, rtcNE, textProcessor, fmtTE, ast.getEvent(), null ) ) ;
        cards.add( new QACard( this, rtcNE, textProcessor, fmtET, ast.getTime(), null ) ) ;
    }
    
    public String getObjIdSeed() { 
        return this.ast.getEvent() ; 
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "time",  time ) ;
        map.put( "event", event ) ;
    }
}


