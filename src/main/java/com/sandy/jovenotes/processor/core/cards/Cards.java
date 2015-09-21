package com.sandy.jovenotes.processor.core.cards;

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import org.json.simple.JSONValue ;

import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.jovenotes.processor.util.StringUtil ;
import com.sandy.xtext.joveNotes.MultiChoice ;
import com.sandy.xtext.joveNotes.Option ;

public class Cards {

    //private static Logger log = Logger.getLogger( Cards.class ) ;
    public static final String QA            = "question_answer" ;
    public static final String FIB           = "fib" ;
    public static final String TF            = "true_false" ;
    public static final String MATCHING      = "matching" ;
    public static final String IMGLABEL      = "image_label" ;
    public static final String SPELLBEE      = "spellbee" ;
    public static final String MULTI_CHOICE  = "multi_choice" ;  
    
    // -------------------------------------------------------------------------
    public static abstract class AbstractCard {
        
        private AbstractNotesElement ne    = null ;
        private String               type  = null ;
        private String               objId = null ;
        
        protected String  caption = null ;
        protected boolean ready   = true ;
        
        public AbstractCard( AbstractNotesElement ne, String caption, String type ) {
            this.ne      = ne ;
            this.type    = type ;
            this.caption = caption ;
        }
        
        public String getType() {
            return this.type ;
        }
        
        public boolean isReady() {
            return this.ready ;
        }
        
        public final String getObjId() {
            
            if( this.objId == null ){
                this.objId = StringUtil.getHash( ne.getObjIdSeed() + "Card" + 
                                                 getType() + getObjIdSeed() ) ; 
            }
            return this.objId ;
        } ;
        
        public String getContent() throws Exception {
            Map<String, Object> map = new LinkedHashMap<String, Object>() ;
            
            collectContentAttributes( map ) ;
            String content = JSONValue.toJSONString( map ) ;
            
            return content ;
        }
        
        protected abstract String getObjIdSeed() ;
        
        public abstract int getDifficultyLevel() ;
        
        protected abstract void collectContentAttributes( Map<String, Object> map ) 
            throws Exception ; 
    }
    
    // -------------------------------------------------------------------------
    public static class QACard extends AbstractCard {
        
        private static final double DIFFICULTY_FACTOR = 0.037505 ;
        
        private String rawQuestion = null ;
        private String rawAnswer = null ;
        private String fmtAnswer = null ;
        
        private JNTextProcessor textProcessor = null ;
        
        public QACard( AbstractNotesElement ne, JNTextProcessor textProcessor,
                       String caption, String rawQ, String rawA, 
                       String cmapImg  ) 
            throws Exception {
            
            super( ne, caption, QA ) ;
            this.textProcessor = textProcessor ;
            this.rawQuestion   = rawQ ;
            this.rawAnswer     = rawA ;
            
            if( caption != null ) {
                this.rawQuestion = "<blockquote>" + caption + "</blockquote>\n\n" + 
                                   this.rawQuestion ;
            }
            
            if( cmapImg != null ) {
                this.rawAnswer += "<p>{{@img " + cmapImg + "}}" ;
            }
            
            this.fmtAnswer = textProcessor.processText( this.rawAnswer ) ;
        }
        
        public String getObjIdSeed() { 
            return this.rawQuestion ;
        }
        
        public int getDifficultyLevel() {
            
            int numWords = textProcessor.getNormalizedWordsInFormattedText( fmtAnswer ) ;
            double nDiff = (2/( 1 + Math.exp( -1*DIFFICULTY_FACTOR*numWords ))) - 1 ;
            
            return (int)Math.ceil( nDiff*100 ) ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ) 
            throws Exception {
            
            map.put( "question", textProcessor.processText( rawQuestion ) ) ;
            map.put( "answer",   textProcessor.processText( rawAnswer ) ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class FIBCard extends AbstractCard {
        
        private static final double DIFFICULTY_FACTOR = 0.67535 ;
        private static final double X_SHIFT = -0.1 ;
        private static final double MAX_LIMIT = 0.6 ;
        
        private String       rawQuestion = null ;
        private List<String> fmtAnswers  = null ;
        
        private JNTextProcessor textProcessor = null ;
        
        public FIBCard( AbstractNotesElement ne, 
                        String caption, String rawQ, List<String> fmtAnsList, 
                        JNTextProcessor textProcessor )
            throws Exception {
            
            super( ne, caption, FIB ) ;
            this.textProcessor = textProcessor ;
            this.rawQuestion   = rawQ ;
            this.fmtAnswers    = fmtAnsList ;
            
            if( caption != null ) {
                this.rawQuestion = "<blockquote>" + caption + "</blockquote>\n\n" + 
                                   this.rawQuestion ;
            }
        }
        
        public String getObjIdSeed() { 
            StringBuilder seed = new StringBuilder() ;
            for( String answer : fmtAnswers ) {
                seed.append( answer ) ;
            }
            return seed.toString() ;
        }
        
        public int getDifficultyLevel() {
            int x = fmtAnswers.size() ;
            double nDiff = (2*(1/(1+Math.exp(-1*DIFFICULTY_FACTOR*(x-X_SHIFT)))-.5))*MAX_LIMIT ;
            return (int)Math.ceil( nDiff*100 ) ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ) 
            throws Exception {
            
            map.put( "question", textProcessor.processText( rawQuestion ) ) ;
            map.put( "answers",  fmtAnswers ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class MatchCard extends AbstractCard {
        
        private static final double DIFFICULTY_FACTOR = 0.26954 ;
        private static final double X_SHIFT = 0.0 ;
        private static final double MAX_LIMIT = 0.7 ;
        
        private List<List<String>> fmtMatchPairs = null ;
        private String matchCaption = null ;
        
        private String objIdSeed = null ;
        
        public MatchCard( AbstractNotesElement ne, String objIdSeed, 
                          String rtcCaption, String matchCaption, 
                          List<List<String>> fmtMatchPairs ) 
            throws Exception {
            
            super( ne, rtcCaption, MATCHING ) ;
            
            this.objIdSeed     = objIdSeed ;
            this.fmtMatchPairs = fmtMatchPairs ;
            this.matchCaption  = matchCaption ;
            
            if( rtcCaption != null ) {
                this.matchCaption = "<blockquote>" + rtcCaption + "</blockquote>\n\n" + 
                                    this.matchCaption ;
            }
        }
        
        public String getObjIdSeed() { 
            return objIdSeed ;
        }
        
        public int getDifficultyLevel() {
            int x = fmtMatchPairs.size() ;
            double nDiff = (2*(1/(1+Math.exp(-1*DIFFICULTY_FACTOR*(x-X_SHIFT)))-.5))*MAX_LIMIT ;
            return (int)Math.ceil( nDiff*100 ) ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ) 
            throws Exception {
            map.put( "caption", matchCaption ) ;
            map.put( "matchData", fmtMatchPairs ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class TrueFalseCard extends AbstractCard {
        
        private String  statement     = null ;
        private boolean truthValue    = false ;
        private String  justification = null ;
        
        private String objIdSeed = null ;
        
        public TrueFalseCard( AbstractNotesElement ne, String objIdSeed, 
                              String caption, String statement, boolean truthValue, 
                              String justification ) 
                                      throws Exception {
            super( ne, caption, TF ) ;
            this.objIdSeed     = objIdSeed ;
            this.statement     = statement ;
            this.truthValue    = truthValue ;
            this.justification = justification ;
            
            if( caption != null ) {
                this.statement = "<blockquote>" + caption + "</blockquote>\n\n" + 
                                 this.statement ;
            }
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public int getDifficultyLevel() { return 10 ; }
        
        public void collectContentAttributes( Map<String, Object> map ) 
            throws Exception {
            map.put( "statement", statement ) ;
            map.put( "truthValue", new Boolean( truthValue ) ) ;
            if( justification != null ) {
                map.put( "justification", justification ) ; 
            }
        }
    }

    // -------------------------------------------------------------------------
    public static class SpellbeeCard extends AbstractCard {
        
        private String objIdSeed = null ;
        
        public SpellbeeCard( AbstractNotesElement ne, String caption, String objIdSeed ) 
            throws Exception {
            
            super( ne, caption, SPELLBEE ) ;
            this.objIdSeed = objIdSeed ;
            super.ready = false ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        public int getDifficultyLevel() { return (int)(objIdSeed.length()*1.5) ; }
        public void collectContentAttributes( Map<String, Object> map ){} 
    }

    // -------------------------------------------------------------------------
    public static class ImageLabelCard extends AbstractCard {
        
        private static final double DIFFICULTY_FACTOR = 0.26954 ;
        private static final double X_SHIFT = 0.0 ;
        private static final double MAX_LIMIT = 0.60 ;
        
        private String objIdSeed = null ;
        private Map<String, Object> contentAttributes = null ;
        
        public ImageLabelCard( AbstractNotesElement ne, String objIdSeed,
                               String caption, Map<String, Object> contentAttributes ) 
            throws Exception {
            
            super( ne, caption, IMGLABEL ) ;
            this.objIdSeed = objIdSeed ;
            this.contentAttributes = contentAttributes ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        @SuppressWarnings("unchecked")
        public int getDifficultyLevel() { 
            int x = ((List<List<Object>>)contentAttributes.get( "hotSpots" )).size() ;
            double nDiff = (2*(1/(1+Math.exp(-1*DIFFICULTY_FACTOR*(x-X_SHIFT)))-.5))*MAX_LIMIT ;
            return (int)Math.ceil( nDiff*100 ) ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ){
            map.putAll( contentAttributes ) ;
        } 
    }

    // -------------------------------------------------------------------------
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
    public static class MultiChoiceCard extends AbstractCard {
        
        private String objIdSeed = null ;
        private int    numCorrectAnswers = 0 ;
        
        private Map<String, Object> contentAttributes = 
                                           new LinkedHashMap<String, Object>() ;
        
        public MultiChoiceCard( AbstractNotesElement ne, String objIdSeed,
                                String caption, MultiChoice ast, 
                                JNTextProcessor textProcessor ) 
            throws Exception {
            
            super( ne, caption, MULTI_CHOICE ) ;
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
            
            if( super.caption != null ) {
                fmtQ = "<blockquote>" + caption + "</blockquote>\n\n" + fmtQ ;
            }
            
            contentAttributes.put( "question",          fmtQ ) ;
            contentAttributes.put( "options",           options ) ;
            contentAttributes.put( "numCorrectAnswers", numCorrectAnswers ) ;
            contentAttributes.put( "explanation",       fmtE ) ;
            contentAttributes.put( "numOptionsToShow",  numOptionsToShow ) ;
            contentAttributes.put( "numOptionsPerRow",  numOptionsPerRow ) ;
        }
    }
}
