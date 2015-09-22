package com.sandy.jovenotes.processor.core.notes ;

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.NotesElement ;
import com.sandy.xtext.joveNotes.RTCElement ;
import com.sandy.xtext.joveNotes.RefToContext ;

public class RefToContextNotesElement extends AbstractNotesElement {
    
    private String objIdSeed     = null ;
    private String rtcCaption    = null ;
    private String fmtRTCCaption = null ;
    
    private List<AbstractNotesElement> noteElements = new ArrayList<AbstractNotesElement>() ;
    
    public RefToContextNotesElement( Chapter chapter, RefToContext ast ) 
        throws Exception {
        
        super( NotesElements.RTC, chapter, ast, null ) ;
        
        this.rtcCaption = ast.getContext() ;
        this.objIdSeed = ast.getContext().substring( 0, rtcCaption.length()/5 ) ;
        
        for( RTCElement rtcNE : ast.getRtcElement() ) {
            AbstractNotesElement ane = null ;
            ane = NotesElements.build( chapter, (NotesElement)rtcNE, this ) ;
            
            noteElements.add( ane ) ;
        }
    }
    
    public String getRawRTCCaption() {
        return this.rtcCaption ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.fmtRTCCaption = textProcessor.processText( this.rtcCaption ) ;
        
        for( AbstractNotesElement ane : noteElements ) {
            ane.initialize( textProcessor ) ;
            cards.addAll( ane.getCards() ) ;
        }
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ){

        List<Map<String, Object>> noteElementsObjArray = null ;
        
        noteElementsObjArray = new ArrayList<Map<String,Object>>() ;
        
        for( AbstractNotesElement ane : noteElements ) {
            Map<String, Object> aneAttributes = new LinkedHashMap<String, Object>() ;
            ane.collectContentAttributes( aneAttributes ) ;
            aneAttributes.put( "elementType", ane.getType() ) ;
            noteElementsObjArray.add( aneAttributes ) ;
        }
        
        map.put( "context",       this.fmtRTCCaption ) ;
        map.put( "notesElements", noteElementsObjArray ) ;
    }
}