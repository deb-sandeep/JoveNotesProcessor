package com.sandy.jovenotes.processor.core.cards;

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import org.json.simple.JSONValue ;

import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.jovenotes.processor.util.StringUtil ;
import com.sandy.xtext.joveNotes.Exercise ;
import com.sandy.xtext.joveNotes.MultiChoice ;
import com.sandy.xtext.joveNotes.Option ;
import com.sandy.xtext.joveNotes.VoiceToText ;

public class Cards {

    //private static Logger log = Logger.getLogger( Cards.class ) ;
    public static final String QA            = "question_answer" ;
    public static final String FIB           = "fib" ;
    public static final String TF            = "true_false" ;
    public static final String MATCHING      = "matching" ;
    public static final String IMGLABEL      = "image_label" ;
    public static final String SPELLBEE      = "spellbee" ;
    public static final String MULTI_CHOICE  = "multi_choice" ;  
    public static final String EXERCISE      = "exercise" ;
    public static final String VOICE2TEXT    = "voice2text" ;
    
    // -------------------------------------------------------------------------
    public static abstract class AbstractCard {
        
        private AbstractNotesElement     ne    = null ;
        private RefToContextNotesElement rtcNE = null ;
        private String                   type  = null ;
        private String                   objId = null ;
        
        protected boolean ready   = true ;
        
        public AbstractCard( AbstractNotesElement ne, 
                             RefToContextNotesElement rtcNE, String type ) {
            this.ne    = ne ;
            this.type  = type ;
            this.rtcNE = rtcNE ;
        }
        
        public String getType() {
            return this.type ;
        }
        
        public boolean isReady() {
            return this.ready ;
        }
        
        public String getRawRTCCaption() {
            if( this.rtcNE != null ) {
                return this.rtcNE.getRawRTCCaption() ;
            }
            return null ;
        }
        
        public final String getObjId() {
            
            String parentObjIdSeed = ( rtcNE == null ) ? ne.getObjIdSeed() : 
                                                         rtcNE.getObjIdSeed() ;
            if( this.objId == null ){
                this.objId = StringUtil.getHash( parentObjIdSeed + "Card" + 
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
        
        private String fmtAnswer   = null ;
        private String fmtQuestion = null ;
        
        // REMOVE: This is a hack to preserve backward compatibility of RTC nested 
        // question introduction. This should be removed in April 2016. 
        // objIdSeed should be equal to the raqwQuestion.
        private String objIdSeed = null ;
        
        private JNTextProcessor textProcessor = null ;
        
        public QACard( AbstractNotesElement ne, RefToContextNotesElement rtcNE, 
                       JNTextProcessor textProcessor,
                       String rawQ, String rawA, String cmapImg  ) 
            throws Exception {
            
            super( ne, rtcNE, QA ) ;
            this.textProcessor = textProcessor ;
            this.fmtQuestion   = null ;
            this.objIdSeed     = rawQ ;
            
            String rawAnswer = rawA ;
            
            if( getRawRTCCaption() != null ) {
                this.objIdSeed = "<blockquote>" + 
                                 getRawRTCCaption() + 
                                 "</blockquote>\n\n" + 
                                 rawQ ;
                
                this.fmtQuestion = "<blockquote>" + 
                                   textProcessor.processText( getRawRTCCaption() ) + 
                                   "</blockquote>\n\n" + 
                                   textProcessor.processText( rawQ ) ;
            }
            else {
                this.fmtQuestion = textProcessor.processText( rawQ ) ;
            }
            
            if( cmapImg != null ) {
                rawAnswer += "<p>{{@img " + cmapImg + "}}" ;
            }
            
            this.fmtAnswer = textProcessor.processText( rawAnswer ) ;
        }
        
        public String getObjIdSeed() { 
            return this.objIdSeed ;
        }
        
        public int getDifficultyLevel() {
            
            int numWords = textProcessor.getNormalizedWordsInFormattedText( fmtAnswer ) ;
            double nDiff = (2/( 1 + Math.exp( -1*DIFFICULTY_FACTOR*numWords ))) - 1 ;
            
            return (int)Math.ceil( nDiff*100 ) ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ) 
            throws Exception {
            
            map.put( "question", fmtQuestion ) ;
            map.put( "answer",   fmtAnswer ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class FIBCard extends AbstractCard {
        
        private static final double DIFFICULTY_FACTOR = 0.5 ;
        private static final double X_SHIFT = -0.1 ;
        private static final double MAX_LIMIT = 0.3 ;
        
        private String       fmtQuestion = null ;
        private List<String> fmtAnswers  = null ;
        
        public FIBCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE,
                        String rawQ, List<String> fmtAnsList, 
                        JNTextProcessor textProcessor )
            throws Exception {
            
            super( ne, rtcNE, FIB ) ;
            this.fmtAnswers    = fmtAnsList ;
            
            if( getRawRTCCaption() != null ) {
                this.fmtQuestion = "<blockquote>" + 
                                   textProcessor.processText( getRawRTCCaption() ) + 
                                   "</blockquote>\n\n" + 
                                   textProcessor.processText( rawQ ) ;
            }
            else {
                this.fmtQuestion = textProcessor.processText( rawQ ) ;
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
            
            map.put( "question", fmtQuestion ) ;
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
        
        public MatchCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE, 
                          String objIdSeed, String matchCaption,
                          List<List<String>> fmtMatchPairs, 
                          JNTextProcessor textProcessor ) 
            throws Exception {
            
            super( ne, rtcNE, MATCHING ) ;
            
            this.objIdSeed     = objIdSeed ;
            this.fmtMatchPairs = fmtMatchPairs ;
            this.matchCaption  = matchCaption ;
            
            if( getRawRTCCaption() != null ) {
                this.matchCaption = "<blockquote>" + 
                                    textProcessor.processText( getRawRTCCaption() ) + 
                                    "</blockquote>\n\n" + 
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
        
        public TrueFalseCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE, 
                              String objIdSeed, 
                              String statement, boolean truthValue, 
                              String justification,
                              JNTextProcessor textProcessor ) 
                                      throws Exception {
            
            super( ne, rtcNE, TF ) ;
            this.objIdSeed     = objIdSeed ;
            this.statement     = statement ;
            this.truthValue    = truthValue ;
            this.justification = justification ;
            
            if( getRawRTCCaption() != null ) {
                this.statement = "<blockquote>" + 
                                 textProcessor.processText( getRawRTCCaption() ) + 
                                 "</blockquote>\n\n" + 
                                 this.statement ;
            }
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public int getDifficultyLevel() { return 5 ; }
        
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
        
        public SpellbeeCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE,
                             String objIdSeed ) 
            throws Exception {
            
            super( ne, rtcNE, SPELLBEE ) ;
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
        
        public ImageLabelCard( AbstractNotesElement ne, RefToContextNotesElement rtcNE,
                               String objIdSeed, Map<String, Object> contentAttributes )
            throws Exception {
            
            super( ne, rtcNE, IMGLABEL ) ;
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

    // -------------------------------------------------------------------------
    /**
     * The exercise card has the following JSON structure
     * 
     * {
     *   question          : "Question text",
     *   answer            : "Answer text",
     *   hints             : [ "Hint1", "Hint2" ],
     * }
     */
    public static class ExerciseCard extends AbstractCard {
        
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


    // -------------------------------------------------------------------------
    /**
     * The voice2text card has the following JSON structure
     * 
     * {
     *   clipname          : "<The name of the clip, not including .mp3>",
     *   text              : "<The text of the clip>"
     * }
     * 
     * It is assumed that the clip is in the audio chapter directory.
     */
    public static class VoiceToTextCard extends AbstractCard {
        
        private String          objIdSeed   = null ;
        private VoiceToText     ast         = null ;
        private String          fmtAnswer   = null ;
        private JNTextProcessor txtProc     = null ;
        
        public VoiceToTextCard( AbstractNotesElement ne, 
                                RefToContextNotesElement rtcNE, 
                                String objIdSeed, 
                                VoiceToText ast, 
                                JNTextProcessor textProcessor ) 
            throws Exception {
            
            super( ne, rtcNE, VOICE2TEXT ) ;
            this.objIdSeed = objIdSeed ;
            this.ast = ast ;
            this.fmtAnswer = textProcessor.processText( ast.getText() ) ;
            this.txtProc = textProcessor ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public int getDifficultyLevel() { 
            
            int textLen = txtProc.getNormalizedWordsInFormattedText( fmtAnswer ) ;
            int difficulty = 0 ;
            
            if( textLen > 15 ) {
                difficulty = 100 ;
            }
            else if( textLen > 10 ) {
                difficulty = 75 ;
            }
            else if( textLen > 7 ) {
                difficulty = 50 ;
            }
            else {
                difficulty = 30 ;
            }
            return difficulty ; 
        }
        
        public void collectContentAttributes( Map<String, Object> map ) {
            
            map.put( "clipName", ast.getClipName() ) ;
            map.put( "text",     fmtAnswer         ) ;
        }
    }
}
