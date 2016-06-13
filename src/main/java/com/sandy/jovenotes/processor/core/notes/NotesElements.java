package com.sandy.jovenotes.processor.core.notes;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.json.simple.JSONValue ;

import com.sandy.jovenotes.processor.JoveNotes ;
import com.sandy.jovenotes.processor.async.SpellbeeCmd ;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.Cards.AbstractCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.ExerciseCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.FIBCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.ImageLabelCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.MatchCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.MultiChoiceCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.QACard ;
import com.sandy.jovenotes.processor.core.cards.Cards.SpellbeeCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.TrueFalseCard ;
import com.sandy.jovenotes.processor.core.cards.Cards.VoiceToTextCard ;
import com.sandy.jovenotes.processor.util.ASTReflector ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.jovenotes.processor.util.StringUtil ;
import com.sandy.xtext.joveNotes.Character ;
import com.sandy.xtext.joveNotes.ChemCompound ;
import com.sandy.xtext.joveNotes.ChemEquation ;
import com.sandy.xtext.joveNotes.Definition ;
import com.sandy.xtext.joveNotes.EqSymbol ;
import com.sandy.xtext.joveNotes.Equation ;
import com.sandy.xtext.joveNotes.EvalVar ;
import com.sandy.xtext.joveNotes.Event ;
import com.sandy.xtext.joveNotes.Exercise ;
import com.sandy.xtext.joveNotes.HotSpot ;
import com.sandy.xtext.joveNotes.ImageLabel ;
import com.sandy.xtext.joveNotes.Matching ;
import com.sandy.xtext.joveNotes.MultiChoice ;
import com.sandy.xtext.joveNotes.NotesElement ;
import com.sandy.xtext.joveNotes.Option ;
import com.sandy.xtext.joveNotes.QuestionAnswer ;
import com.sandy.xtext.joveNotes.RefToContext ;
import com.sandy.xtext.joveNotes.Script ;
import com.sandy.xtext.joveNotes.Spellbee ;
import com.sandy.xtext.joveNotes.TeacherNote ;
import com.sandy.xtext.joveNotes.TrueFalse ;
import com.sandy.xtext.joveNotes.VoiceToText ;
import com.sandy.xtext.joveNotes.WordMeaning ;

public class NotesElements {
    
    private static Logger log = Logger.getLogger( NotesElements.class ) ;
    
    public static final String QA            = "question_answer" ;
    public static final String WM            = "word_meaning" ;
    public static final String FIB           = "fib" ;
    public static final String DEFINITION    = "definition" ;
    public static final String CHARACTER     = "character" ;
    public static final String TEACHER_NOTE  = "teacher_note" ;
    public static final String MATCHING      = "matching" ;
    public static final String EVENT         = "event" ;
    public static final String TRUE_FALSE    = "true_false" ;
    public static final String CHEM_EQUATION = "chem_equation" ;
    public static final String CHEM_COMPOUND = "chem_compound" ;
    public static final String SPELLBEE      = "spellbee" ;
    public static final String IMAGE_LABEL   = "image_label" ;
    public static final String EQUATION      = "equation" ; 
    public static final String RTC           = "rtc" ;  
    public static final String MULTI_CHOICE  = "multi_choice" ;
    public static final String EXERCISE      = "exercise" ;
    public static final String VOICE2TEXT    = "voice2text" ;
    
    // -------------------------------------------------------------------------
    public static AbstractNotesElement build( Chapter c,  
                                              NotesElement ast, 
                                              RefToContextNotesElement rtcNE ) 
        throws Exception {
        
        AbstractNotesElement ne = null ;
        
        if( ast instanceof QuestionAnswer ){
            ne = new QAElement(c, (QuestionAnswer)ast, rtcNE );
        }
        else if( ast instanceof WordMeaning ){
            ne = new WMElement(c, (WordMeaning)ast, rtcNE );
        }
        else if( ast instanceof Definition ){
            ne = new DefinitionElement(c, (Definition)ast, rtcNE );
        }
        else if( ast instanceof TeacherNote ){
            ne = new TeacherNotesElement(c, (TeacherNote)ast, rtcNE );
        }
        else if( ast instanceof Character ){
            ne = new CharacterElement(c, (Character)ast, rtcNE );
        }
        else if( ast instanceof Event ){
            ne = new EventElement(c, (Event)ast, rtcNE );
        }
        else if( ast instanceof com.sandy.xtext.joveNotes.FIB ){
            ne = new FIBElement(c, (com.sandy.xtext.joveNotes.FIB)ast, rtcNE);
        }
        else if( ast instanceof Matching ){
            ne = new MatchElement(c, (Matching)ast, rtcNE );
        }
        else if( ast instanceof TrueFalse ){
            ne = new TrueFalseElement(c, (TrueFalse)ast, rtcNE );
        }
        else if( ast instanceof Spellbee ){
            ne = new SpellbeeElement(c, (Spellbee)ast, rtcNE );
        }
        else if( ast instanceof ImageLabel ){
            ne = new ImageLabelElement(c, (ImageLabel)ast, rtcNE );
        }
        else if( ast instanceof ChemCompound ){
            ne = new ChemCompoundElement(c, (ChemCompound)ast, rtcNE );
        }
        else if( ast instanceof Equation ){
            ne = new EquationElement(c, (Equation)ast, rtcNE );
        }
        else if( ast instanceof ChemEquation ){
            ne = new ChemEquationElement(c, (ChemEquation)ast, rtcNE );
        }
        else if( ast instanceof RefToContext ){
            ne = new RefToContextNotesElement(c, (RefToContext)ast );
        }
        else if( ast instanceof MultiChoice ){
            ne = new MultiChoiceElement(c, (MultiChoice)ast, rtcNE );
        }
        else if( ast instanceof Exercise ) {
            ne = new ExerciseElement(c, (Exercise)ast, rtcNE ) ;
        }
        else if( ast instanceof VoiceToText ) {
            ne = new VoiceToTextElement( c, (VoiceToText)ast, rtcNE ) ;
        }
        
        return ne ;
    }

    // -------------------------------------------------------------------------
    public static abstract class AbstractNotesElement {
        
        private   String       type              = null ;
        private   int          difficultyLevel   = -1 ;
        private   NotesElement ast               = null ;
        private   String       scriptBody        = null ;
        protected boolean      ready             = true ;
        protected boolean      hiddenFromView    = false ;
        
        protected RefToContextNotesElement rtcNE = null ;
        protected Chapter chapter = null ;
        protected List<AbstractCard> cards = new ArrayList<AbstractCard>() ;
        
        private Map<String, String> evalVars = new HashMap<String, String>() ;
        
        public AbstractNotesElement( String type, Chapter chapter, 
                                     NotesElement ast, 
                                     RefToContextNotesElement rtcNE ) 
            throws Exception {
            
            this.type    = type ;
            this.chapter = chapter ;
            this.ast     = ast ;
            this.rtcNE   = rtcNE ;
            
            ASTReflector reflector = new ASTReflector( ast ) ;
            
            Script script = reflector.getScript() ;
            if( script != null ) {
                
                if( script.getEvalVars() != null ) {
                    for( EvalVar var : script.getEvalVars() ) {
                        if( evalVars.containsKey( var.getVarName() ) ) {
                            log.warn( "Script eval var " + var.getVarName() + 
                                      " is declared multiple times." ) ;
                        }
                        evalVars.put( var.getVarName(), var.getVarExpression() ) ;
                    }
                }
                
                if( script.getScriptBody() != null ) {
                    this.scriptBody = script.getScriptBody().getScript() ;
                }
            }
            
            if( reflector.getHideFromView() != null ) {
                this.hiddenFromView = true ;
            }
        }
        
        public String getRawRTCCaption() {
            if( this.rtcNE != null ) {
                return this.rtcNE.getRawRTCCaption() ;
            }
            return null ;
        }
        
        public String getType() {
            return this.type ;
        }
        
        public Chapter getChapter() { 
            return this.chapter ; 
        }
        
        public NotesElement getAST() {
            return this.ast ;
        }
        
        public boolean isReady() {
            return this.ready ;
        }
        
        public boolean isHiddenFromView() {
            return this.hiddenFromView ;
        }
        
        public String getScriptBody() {
            return this.scriptBody ;
        }
        
        public Map<String, String> getEvalVars() {
            return this.evalVars ;
        }
        
        public final String getObjId() {
            return StringUtil.getHash( chapter.getChapterFQN() + "NE" + getType() + getObjIdSeed() ) ; 
        } ;
        
        public final int getDifficultyLevel() {
            
            if( difficultyLevel == -1 ) {
                List<AbstractCard> cards = getCards() ;
                if( !cards.isEmpty() ) {
                    difficultyLevel = 0 ;
                    for( AbstractCard card : cards ) {
                        difficultyLevel += card.getDifficultyLevel() ;
                    }
                    difficultyLevel = difficultyLevel/cards.size() ;
                }
            }
            return difficultyLevel ;
        }

        public final String getContent() {
            Map<String, Object> map = new LinkedHashMap<String, Object>() ;
            
            collectContentAttributes( map ) ;
            String content = JSONValue.toJSONString( map ) ;
            
            return content ;
        }
        
        public final String getEvalVarsAsJSON() {
            return JSONValue.toJSONString( this.evalVars ) ;
        }
        
        /**
         * This function will be called during the source object model 
         * construction immediately following the creation of the notes elements.
         * 
         * This gives a chance to the concrete notes elements to initialize 
         * themselves before they are called into active service. Initialization
         * might involve activities like:
         * 1. Transforming content text
         * 2. Creation of associated cards
         * 3. Initialization of associated cards
         * 
         * @param textProcessor The text processor instance which has the context
         *        of processing the chapter to which this notes element belongs.
         *        
         * @throws Exception
         */
        public abstract void initialize( JNTextProcessor textProcessor ) 
                throws Exception ;
        
        public List<AbstractCard> getCards() {
            // It is assumed that the concrete sublcasses will have their cards
            // initialized and added to the 'cards' array during initialization.
            return this.cards ;
        }
        
        public abstract String getObjIdSeed() ;
        
        protected abstract void collectContentAttributes( Map<String, Object> map ) ; 
    }
    
    // -------------------------------------------------------------------------
    public static class QAElement extends AbstractNotesElement {
        
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

    // -------------------------------------------------------------------------
    public static class WMElement extends AbstractNotesElement {
        
        private WordMeaning ast = null ;
        
        private String word = null ;
        private String meaning   = null ;
        
        public WMElement( Chapter chapter, WordMeaning ast, 
                          RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( WM, chapter, ast, rtcNE ) ;
            this.ast = ast ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            this.word    = textProcessor.processText( ast.getWord() ) ;
            this.meaning = textProcessor.processText( ast.getMeaning() ) ;
            
            String wmQ = "_What is the meaning of_\n\n**" + this.word + "**" ;
            String mqQ = "_Which word means_\n\n**" + this.meaning + "**" ;
            
            cards.add( new QACard( this, rtcNE, textProcessor, wmQ, ast.getMeaning(), null ) ) ;
            cards.add( new QACard( this, rtcNE, textProcessor, mqQ, ast.getWord(), null  ) ) ;
        }
        
        public String getObjIdSeed() { 
            return this.ast.getWord() ; 
        }
        
        public void collectContentAttributes( Map<String, Object> map ) {
            map.put( "word", word ) ;
            map.put( "meaning", meaning ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class DefinitionElement extends AbstractNotesElement {
        
        private Definition ast = null ;
        
        private String term       = null ;
        private String definition = null ;
        private String cmapImg    = null ;
        
        public DefinitionElement( Chapter chapter, Definition ast, 
                                  RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( DEFINITION, chapter, ast, rtcNE ) ;
            this.ast = ast ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            this.cmapImg    = textProcessor.processCMapAST( ast.getCmap() ) ;
            this.term       = textProcessor.processText( ast.getTerm() ) ;
            this.definition = textProcessor.processText( ast.getDefinition() ) ; 
            
            if( cmapImg != null ) {
                this.definition += "<p>{{@img " + this.cmapImg + "}}" ;
            }

            String fmtQ = "_Define_\n\n'**" + ast.getTerm().trim() + "**'" ;
            cards.add( new QACard( this, rtcNE, textProcessor,
                                   fmtQ, ast.getDefinition(), cmapImg  ) ) ;
        }
        
        public String getObjIdSeed() { 
            return this.ast.getTerm() ; 
        }
        
        public void collectContentAttributes( Map<String, Object> map ) {
            map.put( "term", term ) ;
            map.put( "definition", definition ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class TeacherNotesElement extends AbstractNotesElement {
        
        private TeacherNote ast = null ;
        
        private String caption    = null ;
        private String note       = null ;
        private String cmapImg    = null ;
        
        public TeacherNotesElement( Chapter chapter, TeacherNote ast, 
                                    RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( TEACHER_NOTE, chapter, ast, rtcNE ) ;
            this.ast = ast ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            this.caption = textProcessor.processText( ast.getCaption() ) ;
            this.cmapImg = textProcessor.processCMapAST( ast.getCmap() ) ;
            this.note    = textProcessor.processText( ast.getNote() ) ;
            
            if( cmapImg != null ) {
                this.note += "<p>{{@img " + this.cmapImg + "}}" ;
            }
            
            if( StringUtil.isEmptyOrNull( this.caption ) ) {
                this.caption = "Note" ;
            }
        }
        
        public String getObjIdSeed() { 
            return this.ast.getNote() ; 
        }
        
        public void collectContentAttributes( Map<String, Object> map ) {
            map.put( "caption", this.caption ) ;
            map.put( "note", this.note ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class CharacterElement extends AbstractNotesElement {
        
        private Character ast = null ;
        
        private String character  = null ;
        private String estimate   = null ;
        private String cmapImg    = null ;
        
        public CharacterElement( Chapter chapter, Character ast, 
                                 RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( CHARACTER, chapter, ast, rtcNE ) ;
            this.ast = ast ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            this.cmapImg   = textProcessor.processCMapAST( ast.getCmap() ) ;
            this.character = textProcessor.processText( ast.getCharacter() ) ;
            this.estimate  = textProcessor.processText( ast.getEstimate() ) ; 
            
            if( cmapImg != null ) {
                this.estimate += "<p>{{@img " + this.cmapImg + "}}" ;
            }

            String fmtQ = "_Give an estimate of_\n\n" + 
                          "'**" + ast.getCharacter() + "**'" ;
            cards.add( new QACard( this, rtcNE, textProcessor,
                                   fmtQ, ast.getEstimate(), cmapImg  ) ) ;
        }
        
        public String getObjIdSeed() { 
            return this.ast.getCharacter() ; 
        }
        
        public void collectContentAttributes( Map<String, Object> map ) {
            map.put( "character", character ) ;
            map.put( "estimate",  estimate ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class EventElement extends AbstractNotesElement {
        
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

    
    // -------------------------------------------------------------------------
    public static class FIBElement extends AbstractNotesElement {
        
        private com.sandy.xtext.joveNotes.FIB ast = null ;
        
        private List<String> rawAnswers  = new ArrayList<String>() ;
        private List<String> fmtAnswers  = new ArrayList<String>() ;
        private String       fmtQuestion = null ;
        private String       objIdSeed   = null ;
        
        public FIBElement( Chapter chapter, com.sandy.xtext.joveNotes.FIB ast, 
                           RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( FIB, chapter, ast, rtcNE ) ;
            this.ast = ast ;
            
            StringBuilder seed = new StringBuilder( this.ast.getQuestion() ) ;
            for( String ans : ast.getAnswers() ) {
                this.rawAnswers.add( ans ) ;
                seed.append( ans ) ;
            }
            this.objIdSeed = seed.toString() ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            this.fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
            for( String rawAns : this.rawAnswers ) {
                this.fmtAnswers.add( textProcessor.processText( rawAns ) ) ;
            }
            cards.add( new FIBCard( this, rtcNE, 
                                    ast.getQuestion(), fmtAnswers, 
                                    textProcessor ) ) ;
        }
        
        public String getObjIdSeed() { 
            return this.objIdSeed ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ) {
            map.put( "question", fmtQuestion ) ;
            map.put( "answers",  fmtAnswers ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class TrueFalseElement extends AbstractNotesElement {
        
        private TrueFalse ast = null ;
        
        private String  statement     = null ;
        private boolean truthValue    = false ;
        private String  justification = null ;
        
        private String objIdSeed = null ;
        
        public TrueFalseElement( Chapter chapter, TrueFalse ast, 
                                 RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( TRUE_FALSE, chapter, ast, rtcNE ) ;
            this.ast = ast ;
            this.objIdSeed = ast.getStatement() ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            statement = textProcessor.processText( ast.getStatement() ) ;
            truthValue = Boolean.parseBoolean( ast.getTruthValue() ) ;
            if( ast.getJustification() != null ) {
                justification = textProcessor.processText( ast.getJustification() ) ;
            }
            
            cards.add( new TrueFalseCard( this, rtcNE, objIdSeed,  
                                          statement, truthValue, 
                                          justification, textProcessor ) ) ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void collectContentAttributes( Map<String, Object> map ) {
            map.put( "statement", statement ) ;
            map.put( "truthValue", new Boolean( truthValue ) ) ;
            if( justification != null ) {
                map.put( "justification", justification ) ; 
            }
        }
    }

    
    // -------------------------------------------------------------------------
    public static class SpellbeeElement extends AbstractNotesElement {
        
        private String word = null ;
        private String objIdSeed = null ;
        private Chapter chapter = null ;
        
        public SpellbeeElement( Chapter chapter, Spellbee ast, 
                                RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( SPELLBEE, chapter, ast, rtcNE ) ;
            this.chapter = chapter ;
            this.word = ast.getWord() ;
            this.objIdSeed = this.word ;
            super.ready = false ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            SpellbeeCard card = new SpellbeeCard( this, rtcNE, objIdSeed ) ;
            
            SpellbeeCmd  cmd = new SpellbeeCmd( chapter, word, 
                                                card.getDifficultyLevel(), 
                                                super.getObjId(), 
                                                card.getObjId() ) ;
            
            JoveNotes.persistentQueue.add( cmd ) ;
            cards.add( card ) ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void collectContentAttributes( Map<String, Object> map ){}
    }

    
    // -------------------------------------------------------------------------
    public static class ImageLabelElement extends AbstractNotesElement {
        
        private ImageLabel ast       = null ;
        private String     objIdSeed = null ;
        
        private Map<String, Object> cardJSONAttrs = new HashMap<String, Object>() ;
        private Map<String, Object> neJSONAttrs   = new HashMap<String, Object>() ;
        
        public ImageLabelElement( Chapter chapter, ImageLabel ast, 
                                  RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( IMAGE_LABEL, chapter, ast, rtcNE ) ;
            this.ast = ast ;
            this.objIdSeed = ast.getImageName() + ast.getHotspots().size() + 
                             ast.getHotspots().get(0).getLabel() ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            textProcessor.processImg( ast.getImageName() ) ;
            
            List<List<Object>> hsArray = new ArrayList<List<Object>>() ;
            for( HotSpot hs : ast.getHotspots() ) {
                List<Object> hsElement = new ArrayList<Object>() ;
                hsElement.add( hs.getX() ) ;
                hsElement.add( hs.getY() ) ;
                hsElement.add( hs.getLabel() ) ;
                hsArray.add( hsElement ) ;
            }
            
            String imgLabelCaption = null ;
            if( ast.getCaption() == null ) {
                imgLabelCaption = "Label the image" ;
            }
            else {
                imgLabelCaption = textProcessor.processText( ast.getCaption() ) ;
            }
            
            String cardImgLabelCaption = imgLabelCaption ;
            if( getRawRTCCaption() != null ) {
                cardImgLabelCaption = "<blockquote>" + 
                                      textProcessor.processText( getRawRTCCaption() ) + 
                                      "</blockquote>\n\n" +
                                      imgLabelCaption ;
            }
            
            cardJSONAttrs.put( "caption",   cardImgLabelCaption ) ;
            cardJSONAttrs.put( "imageName", ast.getImageName() ) ;
            cardJSONAttrs.put( "hotSpots",  hsArray ) ;
            
            neJSONAttrs.put( "caption",   imgLabelCaption ) ;
            neJSONAttrs.put( "imageName", ast.getImageName() ) ;
            neJSONAttrs.put( "hotSpots",  hsArray ) ;
            
            cards.add( new ImageLabelCard( this, rtcNE, objIdSeed, cardJSONAttrs ) ) ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void collectContentAttributes( Map<String, Object> map ){
            map.putAll( neJSONAttrs ) ;
        }
    }

    
    // -------------------------------------------------------------------------
    public static class ChemCompoundElement extends AbstractNotesElement {
        
        private ChemCompound ast       = null ;
        private String       objIdSeed = null ;
        private String       symbol    = null ;
        
        public ChemCompoundElement( Chapter chapter, ChemCompound ast, 
                                    RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( CHEM_COMPOUND, chapter, ast, rtcNE ) ;
            this.ast = ast ;
            this.objIdSeed = ast.getSymbol() ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            symbol = "$$\\ce{" + ast.getSymbol() + "}$$" ;
            
            if( StringUtil.isNotEmptyOrNull( ast.getChemicalName() ) ) {
                cards.add( new QACard( this, rtcNE, textProcessor, 
                        "_What is the **formulae** for_\n\n" + ast.getChemicalName(),
                        symbol, null ) ) ;
                
                cards.add( new QACard( this, rtcNE, textProcessor, 
                        "_What is the **chemical name** of_\n\n" + symbol, 
                        ast.getChemicalName(), null ) ) ;
            }
            
            if( StringUtil.isNotEmptyOrNull( ast.getCommonName() ) ) {
                cards.add( new QACard( this, rtcNE, textProcessor, 
                        "_What is the **formulae** for_\n\n" + ast.getCommonName(),
                        symbol, null ) ) ;
                
                cards.add( new QACard( this, rtcNE, textProcessor, 
                        "_What is the **common name** for_\n\n" + symbol,
                        ast.getCommonName(), null ) ) ;
                
                if( StringUtil.isNotEmptyOrNull( ast.getChemicalName() ) ) {
                    
                    cards.add( new QACard( this, rtcNE, textProcessor, 
                    "_What is the **chemical name** of_\n\n" + ast.getCommonName(), 
                    ast.getChemicalName(), null ) ) ;
                    
                    cards.add( new QACard( this, rtcNE, textProcessor, 
                    "_What is the **common name** of_\n\n" + ast.getChemicalName(), 
                    ast.getCommonName(), null ) ) ;
                }
            }
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void collectContentAttributes( Map<String, Object> map ){
            map.put( "symbol", symbol ) ;
            map.put( "chemicalName", ast.getChemicalName() ) ;
            map.put( "commonName",   ast.getCommonName() ) ;
        }
    }

    
    // -------------------------------------------------------------------------
    public static class EquationElement extends AbstractNotesElement {
        
        private Equation ast       = null ;
        private String   objIdSeed = null ;
        
        private String equation = null ;
        private String descr    = null ;
        List<List<String>> symbols = new ArrayList<List<String>>() ;
        
        public EquationElement( Chapter chapter, Equation ast, 
                                RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( EQUATION, chapter, ast, rtcNE ) ;
            this.ast = ast ;
            this.objIdSeed = ast.getEquation() ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            // Wrapping the user supplied equation in $$ will ensure that it
            // gets rendered by MathJax on the client. It can be argued that
            // we should not be amalgamating view concerns in the core data.
            // It's a potent objection! As of now, we are already doing a lot of
            // view processing during source transformation (Markdown for example).
            // I do believe that the core data should be render hint agnostic.
            // Maybe this is one of the things I need to look at sometimes in the
            // future iterations.
            equation  = "$$" + ast.getEquation() + "$$" ;
            descr     = textProcessor.processText( ast.getDescription() ) ;
            
            for( EqSymbol symbol : ast.getSymbols() ) {
                List<String> pair = new ArrayList<String>() ;
                pair.add( "\\(" + symbol.getSymbol() + "\\)" ) ;
                pair.add( textProcessor.processText( symbol.getDescription() ) ) ;
                symbols.add( pair ) ; 
            }
            
            cards.add( new QACard( this, rtcNE, textProcessor, 
                                   "_What is the equation for_\n\n" + descr, 
                                   equation, null ) ) ;
            
            if( symbols.size() > 2 ) {
                String matchCaption = "For the following equation, match the " + 
                                      "symbols. " + equation ;
                cards.add( new MatchCard( this, rtcNE, objIdSeed, 
                                          matchCaption, symbols, textProcessor ) ) ;
            }
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void collectContentAttributes( Map<String, Object> map ){

            map.put( "equation",    equation ) ;
            map.put( "description", descr ) ;
            map.put( "symbols",     symbols ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class ChemEquationElement extends AbstractNotesElement {
        
        private String objIdSeed = null ;
        private String reactants = null ;
        private String products  = null ;
        private String produces  = null ;
        private String equation  = null ;
        
        private String description = null ;
        private String fmtDescr    = null ;
        
        public ChemEquationElement( Chapter chapter, ChemEquation ast, 
                                    RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( CHEM_EQUATION, chapter, ast, rtcNE ) ;
            reactants   = ast.getReactants() ;
            produces    = (ast.getProduces() == null)? "->" : ast.getProduces();
            products    = ast.getProducts() ;
            description = ast.getDescription() ;
            
            equation = reactants + " " + produces + " " + products ;
            
            this.objIdSeed = reactants + produces ;
        }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            if( description != null ) {
                fmtDescr = textProcessor.processText( description ) ;
                cards.add( new QACard( this, rtcNE, textProcessor, 
                    "_Write the chemical equation described by:_\n\n" + description,
                    "$$\\ce{" + equation + "}$$", null ) ) ;
            }
            
            cards.add( new QACard( this, rtcNE, textProcessor, 
                    "$$\\ce{" + reactants + " " + produces + "} " + getBlanks( products ) + "$$",
                    "$$\\ce{" + equation + "}$$", null ) ) ;
            
            cards.add( new QACard( this, rtcNE, textProcessor, 
                    "$$" + getBlanks( reactants ) + " \\ce{" + produces + " " + products + "}$$",
                    "$$\\ce{" + equation + "}$$", null ) ) ;
        }
        
        private String getBlanks( String string ) {
            
            StringBuilder buffer = new StringBuilder() ;
            String[] parts = string.split( "\\s+\\+\\s+" ) ;
            for( int i=0; i<parts.length; i++ ) {
                buffer.append( "\\_\\_\\_\\_" ) ;
                if( i < parts.length-1 ) {
                    buffer.append( " + " ) ;
                }
            }
            return buffer.toString() ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void collectContentAttributes( Map<String, Object> map ){
            
            map.put( "equation", "$$\\ce{" + equation + "}$$" ) ;
            map.put( "description", fmtDescr ) ;
        }
    }

    // -------------------------------------------------------------------------
    public static class MultiChoiceElement extends AbstractNotesElement {
        
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

    // -------------------------------------------------------------------------
    public static class ExerciseElement extends AbstractNotesElement {
        
        private String   objIdSeed  = null ;
        private Exercise ast        = null ;
        
        private String       fmtQuestion = null ;
        private String       fmtAnswer   = null ;
        private List<String> fmtHints    = new ArrayList<String>() ;
        
        public ExerciseElement( Chapter chapter, Exercise ast, 
                                RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( EXERCISE, chapter, ast, rtcNE ) ;
            this.objIdSeed = constructObjId( ast ) ;
            this.ast = ast ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
            fmtAnswer   = textProcessor.processText( ast.getAnswer() ) ;
            for( String hint : ast.getHints() ) {
                fmtHints.add( textProcessor.processText( hint ) ) ;
            }
            
            cards.add( new ExerciseCard( this, rtcNE, objIdSeed, 
                                         this.ast, textProcessor ) ) ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ){
            
            map.put( "question", fmtQuestion ) ;
            map.put( "answer",   fmtAnswer   ) ;
            map.put( "hints",    fmtHints    ) ;
        }
        
        /**
         * UID is made by processing the question and answer. This implies that
         * the attributes (hide, marks, hints) can be modified without changing
         * the element identity.
         */
        private String constructObjId( Exercise ast ) {
            StringBuilder buffer = new StringBuilder() ;
            buffer.append( ast.getQuestion() )
                  .append( ast.getAnswer() ) ;
            return buffer.toString() ;
        }
    }


    // -------------------------------------------------------------------------
    public static class VoiceToTextElement extends AbstractNotesElement {
        
        private String      objIdSeed  = null ;
        private VoiceToText ast        = null ;
        private String      fmtAnswer  = null ;
        
        public VoiceToTextElement( Chapter chapter, VoiceToText ast, 
                                   RefToContextNotesElement rtcNE )  
                throws Exception {
            
            super( VOICE2TEXT, chapter, ast, rtcNE ) ;
            this.objIdSeed = ast.getText() ;
            this.ast = ast ;
        }
        
        public String getObjIdSeed() { return objIdSeed ; }
        
        public void initialize( JNTextProcessor textProcessor ) 
                throws Exception {
            
            textProcessor.processAudio( ast.getClipName() + ".mp3" ) ;
            fmtAnswer = textProcessor.processText( ast.getText() ) ;
            cards.add( new VoiceToTextCard( this, rtcNE, objIdSeed, 
                                            this.ast, textProcessor ) ) ;
        }
        
        public void collectContentAttributes( Map<String, Object> map ){
            map.put( "clipName", ast.getClipName() ) ;
            map.put( "text",     fmtAnswer     ) ;
        }
    }
}
       
        
