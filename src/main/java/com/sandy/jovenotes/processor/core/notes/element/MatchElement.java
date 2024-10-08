package com.sandy.jovenotes.processor.core.notes.element;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.MatchCard ;
import com.sandy.jovenotes.processor.core.notes.NoteElementType ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.MatchMCQConfig ;
import com.sandy.xtext.joveNotes.MatchPair ;
import com.sandy.xtext.joveNotes.Matching ;
import com.sandy.xtext.joveNotes.MultiChoice ;
import com.sandy.xtext.joveNotes.Option ;
import com.sandy.xtext.joveNotes.impl.JoveNotesFactoryImpl ;

public class MatchElement extends AbstractNotesElement {
    
    private Matching ast = null ;
    
    private String matchFmtCaption = "" ;
    private List<List<String>> pairs = new ArrayList<List<String>>() ;
    private List<List<String>> pairsReverse = new ArrayList<List<String>>() ;
    private String objIdSeed = "" ;
    private String objIdSeedReverse = "" ;
    private boolean generateReverseQuestion = true ;
    
    public MatchElement( Chapter chapter, Matching ast, 
                         RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( NoteElementType.MATCHING, chapter, ast, rtcNE ) ;
        this.ast = ast ;
        this.generateReverseQuestion = 
                   ( ast.getSkipReverseQuestion() == null ) ? true : false ; 
        
        for( MatchPair pair : ast.getPairs() ) {
            this.objIdSeed        += ( pair.getMatchQuestion() + pair.getMatchAnswer() ) ;
            this.objIdSeedReverse += ( pair.getMatchAnswer() + pair.getMatchQuestion() ) ;
        }
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.matchFmtCaption = ( ast.getQuestion() == null ) ? 
                          "Match the following" : 
                          textProcessor.processText( ast.getQuestion() ) ;
        
        for( MatchPair pair : ast.getPairs() ) {
            List<String> pairList = new ArrayList<String>() ;
            List<String> pairListReverse = new ArrayList<String>() ;
            
            pairList.add( textProcessor.processText( pair.getMatchQuestion() ) ) ;
            pairList.add( textProcessor.processText( pair.getMatchAnswer() ) ) ;
            
            pairListReverse.add( textProcessor.processText( pair.getMatchAnswer() ) ) ;
            pairListReverse.add( textProcessor.processText( pair.getMatchQuestion() ) ) ;
            
            this.pairs.add( pairList ) ;
            this.pairsReverse.add( pairListReverse ) ;
        }
        
        cards.add( new MatchCard( this, rtcNE, objIdSeed, 
                                  matchFmtCaption, pairs, textProcessor ) ) ;
        
        if( this.generateReverseQuestion ) {
            cards.add( new MatchCard( this, rtcNE, objIdSeedReverse, 
                                      matchFmtCaption, 
                                      pairsReverse, textProcessor ) ) ;
        }
        
        if( this.ast.getMcqConfig() != null ) {
            addMCQuestions( textProcessor ) ;
        }
    }
    
    public String getObjIdSeed() { 
        return objIdSeed ;
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "caption", matchFmtCaption ) ;
        map.put( "matchData", pairs ) ;
    }
    
    private void addMCQuestions( JNTextProcessor textProcessor ) 
        throws Exception {
        
        Map<String, String> fwdMatchPairs = new HashMap<String, String>() ;
        Map<String, String> revMatchPairs = new HashMap<String, String>() ;
        
        for( MatchPair pair : ast.getPairs() ) {
            fwdMatchPairs.put( pair.getMatchQuestion(), pair.getMatchAnswer()   ) ;
            revMatchPairs.put( pair.getMatchAnswer(),   pair.getMatchQuestion() ) ;
        }
        
        MatchMCQConfig mcqCfg = ast.getMcqConfig() ;
        int numOptionsToShow = mcqCfg.getNumOptionsToShow() ;
        int numOptionsPerRow = mcqCfg.getNumOptionsPerRow() ;
        
        if( numOptionsToShow == 0 ) numOptionsToShow = 4 ;
        if( numOptionsPerRow == 0 ) numOptionsPerRow = 4 ;
        
        addMCQsForMatchPairs( fwdMatchPairs, textProcessor, 
                              ast.getMcqConfig().getForwardCaption(),
                              numOptionsToShow, numOptionsPerRow ) ;
        if( this.generateReverseQuestion ) {
            
            String caption = ast.getMcqConfig().getReverseCaption() ;
            if( caption == null ) {
                caption = ast.getMcqConfig().getForwardCaption() ;
            }
            addMCQsForMatchPairs( revMatchPairs, textProcessor, caption,
                                  numOptionsToShow, numOptionsPerRow ) ;
        }
    }
    
    private void addMCQsForMatchPairs( Map<String, String> matchPairs, 
                                       JNTextProcessor textProcessor,
                                       String caption,
                                       int numOptionsToShow, 
                                       int numOptionsPerRow ) 
        throws Exception {
        
        JoveNotesFactoryImpl factory = new JoveNotesFactoryImpl() ;
        
        for( Entry<String, String> pair : matchPairs.entrySet() ) {
            
            MultiChoice mcqAST = factory.createMultiChoice() ;
            
            mcqAST.setQuestion( caption + "  \n### **" + pair.getKey() + "**" );
            mcqAST.setExplanation( "" ) ; 
            mcqAST.setHideFromView( "hide" ) ;
            mcqAST.setNumOptionsToShow( numOptionsToShow ) ;
            mcqAST.setNumOptionsPerRow( numOptionsPerRow ) ;
            
            List<Option> options = mcqAST.getOptions() ;
            for( String value : matchPairs.values() ) {
                Option optAST = factory.createOption() ;
                optAST.setOptionValue( value ) ;
                if( value.equals( pair.getValue() ) ) {
                    optAST.setCorrectOption( "correct" ) ;
                }
                options.add( optAST ) ;
            }
            
            MultiChoiceElement mcqElement = null ;
            mcqElement = new MultiChoiceElement( chapter, mcqAST, rtcNE ) ;
            mcqElement.setSection( super.getSection() );
            mcqElement.initialize( textProcessor ) ;
            
            cards.addAll( mcqElement.getCards() ) ;
        }
    }
}



