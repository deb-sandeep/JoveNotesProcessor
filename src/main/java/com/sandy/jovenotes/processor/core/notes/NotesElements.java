package com.sandy.jovenotes.processor.core.notes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.async.SpellbeeCmd;
import com.sandy.jovenotes.processor.core.notes.Cards.AbstractCard;
import com.sandy.jovenotes.processor.core.notes.Cards.FIBCard;
import com.sandy.jovenotes.processor.core.notes.Cards.ImageLabelCard;
import com.sandy.jovenotes.processor.core.notes.Cards.MatchCard;
import com.sandy.jovenotes.processor.core.notes.Cards.QACard;
import com.sandy.jovenotes.processor.core.notes.Cards.SpellbeeCard;
import com.sandy.jovenotes.processor.core.notes.Cards.TrueFalseCard;
import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.jovenotes.processor.util.StringUtil;
import com.sandy.xtext.joveNotes.Character;
import com.sandy.xtext.joveNotes.ChemCompound;
import com.sandy.xtext.joveNotes.ChemEquation;
import com.sandy.xtext.joveNotes.Definition;
import com.sandy.xtext.joveNotes.EqSymbol;
import com.sandy.xtext.joveNotes.Equation;
import com.sandy.xtext.joveNotes.Event;
import com.sandy.xtext.joveNotes.HotSpot;
import com.sandy.xtext.joveNotes.ImageLabel;
import com.sandy.xtext.joveNotes.MatchPair;
import com.sandy.xtext.joveNotes.Matching;
import com.sandy.xtext.joveNotes.NotesElement;
import com.sandy.xtext.joveNotes.QuestionAnswer;
import com.sandy.xtext.joveNotes.RefToContext;
import com.sandy.xtext.joveNotes.Spellbee;
import com.sandy.xtext.joveNotes.TeacherNote;
import com.sandy.xtext.joveNotes.TrueFalse;
import com.sandy.xtext.joveNotes.WordMeaning;

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
	
	// -------------------------------------------------------------------------
	public static AbstractNotesElement build( Chapter chapter, NotesElement ast ) {
		AbstractNotesElement notesElement = null ;
		
		if( ast instanceof QuestionAnswer ){
			notesElement = new QAElement( chapter, ( QuestionAnswer )ast ) ;
		}
		else if( ast instanceof WordMeaning ){
			notesElement = new WMElement( chapter, ( WordMeaning )ast ) ;
		}
		else if( ast instanceof Definition ){
			notesElement = new DefinitionElement( chapter, ( Definition )ast ) ;
		}
		else if( ast instanceof TeacherNote ){
			notesElement = new TeacherNotesElement( chapter, ( TeacherNote )ast ) ;
		}
		else if( ast instanceof Character ){
			notesElement = new CharacterElement( chapter, ( Character )ast ) ;
		}
		else if( ast instanceof Event ){
			notesElement = new EventElement( chapter, ( Event )ast ) ;
		}
		else if( ast instanceof com.sandy.xtext.joveNotes.FIB ){
			notesElement = new FIBElement( chapter, ( com.sandy.xtext.joveNotes.FIB )ast ) ;
		}
		else if( ast instanceof Matching ){
			notesElement = new MatchElement( chapter, ( Matching )ast ) ;
		}
		else if( ast instanceof TrueFalse ){
			notesElement = new TrueFalseElement( chapter, ( TrueFalse )ast ) ;
		}
		else if( ast instanceof Spellbee ){
			notesElement = new SpellbeeElement( chapter, ( Spellbee )ast ) ;
		}
		else if( ast instanceof ImageLabel ){
			notesElement = new ImageLabelElement( chapter, ( ImageLabel )ast ) ;
		}
		else if( ast instanceof ChemCompound ){
			notesElement = new ChemCompoundElement( chapter, ( ChemCompound )ast ) ;
		}
		else if( ast instanceof Equation ){
			notesElement = new EquationElement( chapter, ( Equation )ast ) ;
		}
		else if( ast instanceof ChemEquation ){
			notesElement = new ChemEquationElement( chapter, ( ChemEquation )ast ) ;
		}
		else if( ast instanceof RefToContext ){
			notesElement = new RefToContextElement( chapter, ( RefToContext )ast ) ;
		}
		
		log.debug( "\t  Built notes element. type = " + notesElement.getType() );
		return notesElement ;
	}

	// -------------------------------------------------------------------------
	public static abstract class AbstractNotesElement {
		
		private String type              = null ;
		private int    difficultyLevel   = -1 ;
		private NotesElement ast         = null ;
		protected boolean ready          = true ;
		protected boolean hiddenFromView = false ;
		
		protected Chapter chapter = null ;
		protected List<AbstractCard> cards = new ArrayList<AbstractCard>() ;
		
		public AbstractNotesElement( String type, Chapter chapter, NotesElement ast ) {
			this.type    = type ;
			this.chapter = chapter ;
			this.ast     = ast ;
			if( ast.getHideFromView() != null ) {
				this.hiddenFromView = true ;
			}
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
		
		protected abstract String getObjIdSeed() ;
		
		protected abstract void collectContentAttributes( Map<String, Object> map ) ; 
	}
	
	// -------------------------------------------------------------------------
	public static class QAElement extends AbstractNotesElement {
		
		private QuestionAnswer ast = null ;
		
		private String question = null ;
		private String answer   = null ;
		private String cmapImg  = null ;
		
		public QAElement( Chapter chapter, QuestionAnswer ast ) {
			super( QA, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.cmapImg  = textProcessor.processCMap( ast.getCmap() ) ;
			this.question = textProcessor.processText( ast.getQuestion() ) ;
			this.answer   = textProcessor.processText( ast.getAnswer() ) ; 
			
			if( cmapImg != null ) {
				this.answer += "<p>{{@img " + this.cmapImg + "}}" ;
			}
			cards.add( new QACard( this, ast.getQuestion(), ast.getAnswer(), 
					               cmapImg, textProcessor ) ) ;
		}
		
		public String getObjIdSeed() { 
			return this.ast.getQuestion() ; 
		}
		
		public void collectContentAttributes( Map<String, Object> map ) {
			map.put( "question", question ) ;
			map.put( "answer", answer ) ;
		}
	}

	// -------------------------------------------------------------------------
	public static class WMElement extends AbstractNotesElement {
		
		private WordMeaning ast = null ;
		
		private String word = null ;
		private String meaning   = null ;
		
		public WMElement( Chapter chapter, WordMeaning ast ) {
			super( WM, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.word    = textProcessor.processText( ast.getWord() ) ;
			this.meaning = textProcessor.processText( ast.getMeaning() ) ;
			
			String wmQ = "_What is the meaning of_\n\n**" + ast.getWord() + "**" ;
			String mqQ = "_Which word means_\n\n**" + ast.getMeaning() + "**" ;
			
			cards.add( new QACard( this, wmQ, ast.getMeaning(), textProcessor ) ) ;
			cards.add( new QACard( this, mqQ, ast.getWord(), textProcessor ) ) ;
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
		
		public DefinitionElement( Chapter chapter, Definition ast ) {
			super( DEFINITION, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.cmapImg    = textProcessor.processCMap( ast.getCmap() ) ;
			this.term       = textProcessor.processText( ast.getTerm() ) ;
			this.definition = textProcessor.processText( ast.getDefinition() ) ; 
			
			if( cmapImg != null ) {
				this.definition += "<p>{{@img " + this.cmapImg + "}}" ;
			}

			String fmtQ = "_Define_\n\n'**" + ast.getTerm() + "**'" ;
			cards.add( new QACard( this, fmtQ, ast.getDefinition(), 
					               cmapImg, textProcessor ) ) ;
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
		
		private String note       = null ;
		private String cmapImg    = null ;
		
		public TeacherNotesElement( Chapter chapter, TeacherNote ast ) {
			super( TEACHER_NOTE, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.cmapImg = textProcessor.processCMap( ast.getCmap() ) ;
			this.note    = textProcessor.processText( ast.getNote() ) ;
			
			if( cmapImg != null ) {
				this.note += "<p>{{@img " + this.cmapImg + "}}" ;
			}
		}
		
		public String getObjIdSeed() { 
			return this.ast.getNote() ; 
		}
		
		public void collectContentAttributes( Map<String, Object> map ) {
			map.put( "note", this.note ) ;
		}
	}

	// -------------------------------------------------------------------------
	public static class CharacterElement extends AbstractNotesElement {
		
		private Character ast = null ;
		
		private String character  = null ;
		private String estimate   = null ;
		private String cmapImg    = null ;
		
		public CharacterElement( Chapter chapter, Character ast ) {
			super( CHARACTER, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.cmapImg   = textProcessor.processCMap( ast.getCmap() ) ;
			this.character = textProcessor.processText( ast.getCharacter() ) ;
			this.estimate  = textProcessor.processText( ast.getEstimate() ) ; 
			
			if( cmapImg != null ) {
				this.estimate += "<p>{{@img " + this.cmapImg + "}}" ;
			}

			String fmtQ = "_Give an estimate of_\n\n" + 
			              "'**" + ast.getCharacter() + "**'" ;
			cards.add( new QACard( this, fmtQ, ast.getEstimate(), 
					               cmapImg, textProcessor ) ) ;
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
		
		public EventElement( Chapter chapter, Event ast ) {
			super( EVENT, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.time  = textProcessor.processText( ast.getTime() ) ;
			this.event = textProcessor.processText( ast.getEvent() ) ;
			
			String fmtTE = "_What happened in_\n\n**" + ast.getTime() + "** ?" ;
			String fmtET = "_When did the following happen_\n\n" + 
			               "**" + ast.getEvent() + "** ?" ;
			
			cards.add( new QACard( this, fmtTE, ast.getEvent(), textProcessor ) ) ;
			cards.add( new QACard( this, fmtET, ast.getTime(), textProcessor ) ) ;
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
		private String       fmtQuestion = null ;
		private String       objIdSeed   = null ;
		
		public FIBElement( Chapter chapter, com.sandy.xtext.joveNotes.FIB ast ) {
			super( FIB, chapter, ast ) ;
			this.ast = ast ;
			
			StringBuilder seed = new StringBuilder() ;
			for( String ans : ast.getAnswers() ) {
				this.rawAnswers.add( ans ) ;
				seed.append( ans ) ;
			}
			this.objIdSeed = seed.toString() ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
			cards.add( new FIBCard( this, ast.getQuestion(), 
					                rawAnswers, textProcessor ) ) ;
		}
		
		public String getObjIdSeed() { 
			return this.objIdSeed ;
		}
		
		public void collectContentAttributes( Map<String, Object> map ) {
			map.put( "question", fmtQuestion ) ;
			map.put( "answers",  rawAnswers ) ;
		}
	}

	
	// -------------------------------------------------------------------------
	public static class MatchElement extends AbstractNotesElement {
		
		private Matching ast = null ;
		
		private List<List<String>> pairs = new ArrayList<List<String>>() ;
		private List<List<String>> pairsReverse = new ArrayList<List<String>>() ;
		private String objIdSeed = "" ;
		private String objIdSeedReverse = "" ;
		private boolean generateReverseQuestion = true ;
		
		public MatchElement( Chapter chapter, Matching ast ) {
			super( MATCHING, chapter, ast ) ;
			this.ast = ast ;
			this.generateReverseQuestion = 
					   ( ast.getSkipReverseQuestion() == null ) ? true : false ; 
			
			for( MatchPair pair : ast.getPairs() ) {
				this.objIdSeed        += pair.getMatchQuestion() ;
				this.objIdSeedReverse += pair.getMatchAnswer() ;
			}
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
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
			
			cards.add( new MatchCard( this, objIdSeed, ast.getQuestion(), pairs ) ) ;
			
			if( this.generateReverseQuestion ) {
				cards.add( new MatchCard( this, objIdSeedReverse, 
						                  ast.getQuestion(), 
						                  pairsReverse ) ) ;
			}
		}
		
		public String getObjIdSeed() { 
			return objIdSeed ;
		}
		
		public void collectContentAttributes( Map<String, Object> map ) {
			map.put( "matchData", pairs ) ;
		}
	}

	
	// -------------------------------------------------------------------------
	public static class TrueFalseElement extends AbstractNotesElement {
		
		private TrueFalse ast = null ;
		
		private String  statement     = null ;
		private boolean truthValue    = false ;
		private String  justification = null ;
		
		private String objIdSeed = null ;
		
		public TrueFalseElement( Chapter chapter, TrueFalse ast ) {
			super( TRUE_FALSE, chapter, ast ) ;
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
			
			cards.add( new TrueFalseCard( this, objIdSeed, statement, 
					                      truthValue, justification ) ) ;
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
		
		public SpellbeeElement( Chapter chapter, Spellbee ast ) {
			super( SPELLBEE, chapter, ast ) ;
			this.chapter = chapter ;
			this.word = ast.getWord() ;
			this.objIdSeed = this.word ;
			super.ready = false ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			SpellbeeCard card = new SpellbeeCard( this, objIdSeed ) ;
			
			SpellbeeCmd  cmd = new SpellbeeCmd( chapter, word, 
												card.getDifficultyLevel(), 
												super.getObjId(), 
												card.getObjId() ) ;
			
			log.debug( "\tPersisting async spellbee command." ) ;
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
		
		private Map<String, Object> jsonAttrs = new HashMap<String, Object>() ;
		
		public ImageLabelElement( Chapter chapter, ImageLabel ast ) {
			super( IMAGE_LABEL, chapter, ast ) ;
			this.ast = ast ;
			this.objIdSeed = ast.getImageName() + ast.getHotspots().size() + 
				             ast.getHotspots().get(0).getLabel() ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			log.debug( "\t\tInitializing image label notes element." ) ;
			
			textProcessor.processImg( ast.getImageName() ) ;
			
			List<List<Object>> hsArray = new ArrayList<List<Object>>() ;
			for( HotSpot hs : ast.getHotspots() ) {
				List<Object> hsElement = new ArrayList<Object>() ;
				hsElement.add( hs.getX() ) ;
				hsElement.add( hs.getY() ) ;
				hsElement.add( hs.getLabel() ) ;
				hsArray.add( hsElement ) ;
			}
			
			String caption = null ;
			if( ast.getCaption() == null ) {
				caption = "Label the image" ;
			}
			else {
				caption = ast.getCaption() ;
			}
			
			jsonAttrs.put( "caption",   caption ) ;
			jsonAttrs.put( "imageName", ast.getImageName() ) ;
			jsonAttrs.put( "hotSpots",  hsArray ) ;
			
			cards.add( new ImageLabelCard( this, objIdSeed, jsonAttrs ) ) ;
		}
		
		public String getObjIdSeed() { return objIdSeed ; }
		
		public void collectContentAttributes( Map<String, Object> map ){
			map.putAll( jsonAttrs ) ;
		}
	}

	
	// -------------------------------------------------------------------------
	public static class ChemCompoundElement extends AbstractNotesElement {
		
		private ChemCompound ast       = null ;
		private String       objIdSeed = null ;
		private String       symbol    = null ;
		
		public ChemCompoundElement( Chapter chapter, ChemCompound ast ) {
			super( CHEM_COMPOUND, chapter, ast ) ;
			this.ast = ast ;
			this.objIdSeed = ast.getSymbol() ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			log.debug( "\t\tInitializing ChemCompound notes element." ) ;
			symbol    = "$$\\ce{" + ast.getSymbol() + "}$$" ;
			
			if( StringUtil.isNotEmptyOrNull( ast.getChemicalName() ) ) {
				cards.add( new QACard( this,
						"_What is the **formulae** for_\n\n" + ast.getChemicalName(),
						symbol, textProcessor ) ) ;
				
				cards.add( new QACard( this,
						"_What is the **chemical name** of_\n\n" + symbol, 
						ast.getChemicalName(), textProcessor ) ) ;
			}
			
			if( StringUtil.isNotEmptyOrNull( ast.getCommonName() ) ) {
				cards.add( new QACard( this,
						"_What is the **formulae** for_\n\n" + ast.getCommonName(),
						symbol, textProcessor ) ) ;
				
				cards.add( new QACard( this,
						"_What is the **chemical name** of_\n\n" + ast.getCommonName(), 
						ast.getChemicalName(), textProcessor ) ) ;
				
				cards.add( new QACard( this,
						"_What is the **common name** of_\n\n" + ast.getChemicalName(), 
						ast.getCommonName(), textProcessor ) ) ;
				
				cards.add( new QACard( this,
						"_What is the **common name** for_\n\n" + symbol,
						ast.getCommonName(), textProcessor ) ) ;
				
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
		
		public EquationElement( Chapter chapter, Equation ast ) {
			super( EQUATION, chapter, ast ) ;
			this.ast = ast ;
			this.objIdSeed = ast.getEquation() ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			log.debug( "\t\tInitializing Equation notes element." ) ;
			
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
			
			cards.add( new QACard( this, "_What is the equation for_\n\n" + descr, 
					               equation, textProcessor ) ) ;
			
			String caption = "For the following equation, match the symbols. " + 
			                 equation ;
			cards.add( new MatchCard( this, objIdSeed, caption, symbols ) ) ;
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
		
		public ChemEquationElement( Chapter chapter, ChemEquation ast ) {
			
			super( CHEM_EQUATION, chapter, ast ) ;
			reactants   = ast.getReactants() ;
			produces    = (ast.getProduces() == null)? "->" : ast.getProduces();
			products    = ast.getProducts() ;
			description = ast.getDescription() ;
			
			equation = reactants + " " + produces + " " + products ;
			
			this.objIdSeed = reactants + produces ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			log.debug( "\t\tInitializing ChemEquation notes element." ) ;
			if( description != null ) {
				fmtDescr = textProcessor.processText( description ) ;
				cards.add( new QACard( this,
					"_Write the chemical equation described by:_\n\n" + description,
				    "$$\\ce{" + equation + "}$$", textProcessor ) ) ;
			}
			
			cards.add( new QACard( this,
					"$$\\ce{" + reactants + " " + produces + "} " + getBlanks( products ) + "$$",
					"$$\\ce{" + equation + "}$$", textProcessor ) ) ;
			
			cards.add( new QACard( this,
					"$$" + getBlanks( reactants ) + " \\ce{" + produces + " " + products + "}$$",
					"$$\\ce{" + equation + "}$$", textProcessor ) ) ;
		}
		
		private String getBlanks( String string ) {
			
			StringBuilder buffer = new StringBuilder() ;
			String[] parts = string.split( "\\s+\\+\\s+" ) ;
			for( int i=0; i<parts.length; i++ ) {
				buffer.append( "\\\\_\\\\_\\\\_\\\\_" ) ;
				if( i < parts.length-1 ) {
					buffer.append( " \\+ " ) ;
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
	public static class RefToContextElement extends AbstractNotesElement {
		
		private String objIdSeed  = null ;
		private String context    = null ;
		private String fmtContext = null ;
		
		private List<List<String>> rawQAList = new ArrayList<List<String>>() ;
		private List<List<String>> fmtQAList = new ArrayList<List<String>>() ;
		
		public RefToContextElement( Chapter chapter, RefToContext ast ) {
			
			super( RTC, chapter, ast ) ;
			this.objIdSeed = ast.getContext().substring( 0, 
					                             ast.getContext().length()/5 ) ;
			this.context = ast.getContext() ;
			for( QuestionAnswer astQ : ast.getQuestions() ) {
				List<String> aRawQA = new ArrayList<String>() ;
				aRawQA.add( astQ.getQuestion() ) ;
				aRawQA.add( astQ.getAnswer() ) ;
				rawQAList.add( aRawQA ) ;
			}
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			log.debug( "\t\tInitializing RefToContext notes element." ) ;
			this.fmtContext = textProcessor.processText( this.context ) ;
			for( List<String> aRawQA : rawQAList ) {
				List<String> aFmtQA = new ArrayList<String>() ;
				aFmtQA.add( aRawQA.get(0) ) ;
				aFmtQA.add( aRawQA.get(1) ) ;
				fmtQAList.add( aFmtQA ) ;
				
				cards.add( new QACard( this,
						"<blockquote>" + context + "</blockquote>\n\n" + 
						aRawQA.get(0), aRawQA.get(1), textProcessor ) ) ;
			}
		}
		
		public String getObjIdSeed() { return objIdSeed ; }
		
		public void collectContentAttributes( Map<String, Object> map ){

			List<Map<String, String>> questionsObjArray = new ArrayList<Map<String,String>>() ;
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
}
