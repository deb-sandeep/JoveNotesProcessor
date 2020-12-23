package com.sandy.jovenotes.processor.core.cards;

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.MultiChoice ;
import com.sandy.xtext.joveNotes.Option ;

import static com.sandy.jovenotes.processor.core.cards.CardType.* ;

/**
 * The multi-choice card has the following JSON structure
 * 
 * {
 *   question          : "Question text",
 *   options           : [[ "Option1", false ], [ "Option2", true ]],
 *   numCorrectAnswers : 2,
 *   explanation       : "Explanation text"
 * }
 */
public class MultiChoiceCard extends AbstractCard {
    
    private String objIdSeed = null ;
    private int    numCorrectAnswers = 0 ;
    
    private Map<String, Object> contentAttributes = 
                                       new LinkedHashMap<String, Object>() ;
    
    public MultiChoiceCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE, 
                            String objIdSeed, MultiChoice ast, 
                            JNTextProcessor textProcessor ) 
        throws Exception {
        
        super( ne, rtcNE, MULTI_CHOICE ) ;
        this.objIdSeed = objIdSeed ;
        initialize( ast, textProcessor ) ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public int getDifficultyLevel() { 
        if( numCorrectAnswers <= 3 ) {
            return 5*numCorrectAnswers ;
        }
        return 20 ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.putAll( contentAttributes ) ;
    }
    
    private void initialize( MultiChoice ast, JNTextProcessor textProcessor ) 
            throws Exception {
        
        String fmtQ = textProcessor.processText( ast.getQuestion() ) ;
        String fmtE = "" ;
        int    numOptionsToShow = ast.getNumOptionsToShow() ;
        int    numOptionsPerRow = ast.getNumOptionsPerRow() ;
        List<List<Object>> options = new ArrayList<List<Object>>() ;
        
        if( ast.getExplanation() != null ) {
            fmtE = textProcessor.processText( ast.getExplanation() ) ;
        }
        
        for( int i=0; i<ast.getOptions().size(); i++ ) {
            
            Option       opt        = ast.getOptions().get( i ) ;
            String       optValue   = textProcessor.processText( opt.getOptionValue() ) ;
            Boolean      correct    = Boolean.FALSE ;
            List<Object> optionPair = new ArrayList<Object>() ;
            
            if( opt.getCorrectOption() != null ) {
                correct = Boolean.TRUE ;
                numCorrectAnswers++ ;
            }
            optionPair.add( optValue ) ;
            optionPair.add( correct ) ;
            
            options.add( optionPair ) ;
        }
        
        numOptionsToShow = ( numOptionsToShow == 0 ) ? ast.getOptions().size() : numOptionsToShow ;
        numOptionsPerRow = ( numOptionsPerRow == 0 ) ? ast.getOptions().size() : numOptionsPerRow ;
        if( numOptionsPerRow > numOptionsToShow ) {
            numOptionsPerRow = numOptionsToShow ;
        }
        
        if( numOptionsToShow < numCorrectAnswers ) {
            throw new Exception( "Number of options to show in a MCQ can't " + 
                                 "be less than the number of correct answers." ) ;
        }
        
        if( numOptionsPerRow <= 0 ) {
            throw new Exception( "Number of options to show per row " + 
                                 "can't be less than or equal to zero." ) ;
        }
        
        if( getRawRTCCaption() != null ) {
            fmtQ = "<blockquote>" + 
                   textProcessor.processText( getRawRTCCaption() ) + 
                   "</blockquote>\n\n" + fmtQ ;
        }
        
        contentAttributes.put( "question",          fmtQ ) ;
        contentAttributes.put( "options",           options ) ;
        contentAttributes.put( "numCorrectAnswers", numCorrectAnswers ) ;
        contentAttributes.put( "explanation",       fmtE ) ;
        contentAttributes.put( "numOptionsToShow",  numOptionsToShow ) ;
        contentAttributes.put( "numOptionsPerRow",  numOptionsPerRow ) ;
    }
}

