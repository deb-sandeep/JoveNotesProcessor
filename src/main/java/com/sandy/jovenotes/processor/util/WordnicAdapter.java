package com.sandy.jovenotes.processor.util;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.jovenotes.processor.JoveNotes;
import com.wordnik.client.api.WordApi ;
import com.wordnik.client.model.Definition ;
import com.wordnik.client.model.TextPron;

public class WordnicAdapter {

    public WordnicAdapter() {
    }
    
    public List<String> getDefinitions( String word ) 
            throws Exception {
        return getDefinitions( word, 3 ) ;
    }
    
    private List<String> getDefinitions( String word, int maxNumDefs ) 
            throws Exception {
        
        List<String> definitions = new ArrayList<>() ;
        
        WordApi api = new WordApi() ;
        api.getInvoker().addDefaultHeader( "api_key", 
        		                           JoveNotes.config.getWordnicAPIKey() ) ;
        
        List<Definition> defs = api.getDefinitions(
                word,     
                null,     
                "all",    
                maxNumDefs,        
                "false",  
                "true",   
                "false"   
        ) ;
        
        for( Definition definition : defs ) {
            definitions.add( definition.getText() ) ;
        }
        
        return definitions ;
    }
    
    public String getPronounciation( String word ) throws Exception {
    	
    	
        WordApi api = new WordApi() ;
        api.getInvoker().addDefaultHeader( "api_key", 
        		                           JoveNotes.config.getWordnicAPIKey() ) ;
        
        List<TextPron> pronounciations = api.getTextPronunciations( 
        		                               word, null, null, "true", 1 ) ;
        if( !pronounciations.isEmpty() ) {
        	return pronounciations.get(0).getRaw() ;
        }
        return null ;
    }
}
