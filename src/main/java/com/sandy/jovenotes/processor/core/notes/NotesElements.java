package com.sandy.jovenotes.processor.core.notes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.async.SpellbeeCmd;
import com.sandy.jovenotes.processor.core.notes.Cards.AbstractCard;
import com.sandy.jovenotes.processor.core.notes.Cards.FIBCard;
import com.sandy.jovenotes.processor.core.notes.Cards.MatchCard;
import com.sandy.jovenotes.processor.core.notes.Cards.QACard;
import com.sandy.jovenotes.processor.core.notes.Cards.SpellbeeCard;
import com.sandy.jovenotes.processor.core.notes.Cards.TrueFalseCard;
import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.jovenotes.processor.util.StringUtil;
import com.sandy.xtext.joveNotes.Character;
import com.sandy.xtext.joveNotes.Definition;
import com.sandy.xtext.joveNotes.Event;
import com.sandy.xtext.joveNotes.MatchPair;
import com.sandy.xtext.joveNotes.Matching;
import com.sandy.xtext.joveNotes.NotesElement;
import com.sandy.xtext.joveNotes.QuestionAnswer;
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
		
		log.debug( "\t  Built notes element. objId = " + notesElement.getObjId() + 
				   ", type = " + notesElement.getType() );
		return notesElement ;
	}

	// -------------------------------------------------------------------------
	public static abstract class AbstractNotesElement {
		
		private String type            = null ;
		private String objId           = null ;
		private int    difficultyLevel = -1 ;
		private NotesElement ast       = null ;
		protected boolean ready        = true ;
		
		protected Chapter chapter = null ;
		protected List<AbstractCard> cards = new ArrayList<AbstractCard>() ;
		
		public AbstractNotesElement( String type, Chapter chapter, NotesElement ast ) {
			this.type    = type ;
			this.chapter = chapter ;
			this.ast     = ast ;
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
		
		public final String getObjId() {
			
			if( this.objId == null ){
				this.objId = StringUtil.getHash( "NE" + getType() + getObjIdSeed() ) ; 
			}
			return this.objId ;
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
			cards.add( new QACard( ast.getQuestion(), ast.getAnswer(), 
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
			String mqQ = "_Which word means_\n\n" + ast.getMeaning() ;
			
			cards.add( new QACard( wmQ, ast.getMeaning(), textProcessor ) ) ;
			cards.add( new QACard( mqQ, ast.getWord(), textProcessor ) ) ;
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

			String fmtQ = "_Define_'**" + ast.getTerm() + "**'" ;
			cards.add( new QACard( fmtQ, ast.getDefinition(), 
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

			String fmtQ = "_Give an estimate of_ '**" + ast.getCharacter() + "**'" ;
			cards.add( new QACard( fmtQ, ast.getEstimate(), 
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
			
			String fmtTE = "_What happened in_ **" + ast.getTime() + "** ?" ;
			String fmtET = "_What did the following happen_ **" + ast.getEvent() + "** ?" ;
			
			cards.add( new QACard( fmtTE, ast.getEvent(), textProcessor ) ) ;
			cards.add( new QACard( fmtET, ast.getTime(), textProcessor ) ) ;
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
		
		public FIBElement( Chapter chapter, com.sandy.xtext.joveNotes.FIB ast ) {
			super( FIB, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.fmtQuestion = textProcessor.processText( ast.getQuestion() ) ;
			for( String ans : ast.getAnswers() ) {
				this.rawAnswers.add( ans ) ;
			}
			cards.add( new FIBCard( ast.getQuestion(), rawAnswers, textProcessor ) ) ;
		}
		
		public String getObjIdSeed() { 
			StringBuilder seed = new StringBuilder() ;
			for( String answer : rawAnswers ) {
				seed.append( answer ) ;
			}
			return seed.toString() ;
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
		private String objIdSeed = "" ;
		
		public MatchElement( Chapter chapter, Matching ast ) {
			super( MATCHING, chapter, ast ) ;
			this.ast = ast ;
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			for( MatchPair pair : ast.getPairs() ) {
				
				objIdSeed += pair.getMatchQuestion() ;
				List<String> pairList = new ArrayList<String>() ;
				pairList.add( textProcessor.processText( pair.getMatchQuestion() ) ) ;
				pairList.add( textProcessor.processText( pair.getMatchAnswer() ) ) ;
				this.pairs.add( pairList ) ;
			}
			
			cards.add( new MatchCard( objIdSeed, pairs ) ) ;
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
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			statement = textProcessor.processText( ast.getStatement() ) ;
			truthValue = Boolean.parseBoolean( ast.getTruthValue() ) ;
			if( ast.getJustification() != null ) {
				justification = textProcessor.processText( ast.getJustification() ) ;
			}
			
			objIdSeed = ast.getStatement() ;
			
			cards.add( new TrueFalseCard( objIdSeed, statement, truthValue, justification ) ) ;
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
		}
		
		public void initialize( JNTextProcessor textProcessor ) 
				throws Exception {
			
			SpellbeeCard card = new SpellbeeCard( objIdSeed ) ;
			SpellbeeCmd  cmd = new SpellbeeCmd( chapter, word, super.getObjId(), card.getObjId() ) ;
			
			log.debug( "\tPersisting async spellbee command." ) ;
			super.ready = false ;
			JoveNotes.persistentQueue.add( cmd ) ;
			cards.add( card ) ;
		}
		
		public String getObjIdSeed() { return objIdSeed ; }
		
		public void collectContentAttributes( Map<String, Object> map ){}
	}
}
