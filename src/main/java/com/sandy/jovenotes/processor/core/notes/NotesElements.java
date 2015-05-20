package com.sandy.jovenotes.processor.core.notes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import com.sandy.jovenotes.processor.core.notes.Cards.AbstractCard;
import com.sandy.jovenotes.processor.core.notes.Cards.QACard;
import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.jovenotes.processor.util.StringUtil;
import com.sandy.xtext.joveNotes.Definition;
import com.sandy.xtext.joveNotes.NotesElement;
import com.sandy.xtext.joveNotes.QuestionAnswer;
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
			notesElement = new QANotesElement( chapter, ( QuestionAnswer )ast ) ;
		}
		else if( ast instanceof WordMeaning ){
			notesElement = new WMNotesElement( chapter, ( WordMeaning )ast ) ;
		}
		else if( ast instanceof Definition ){
			notesElement = new DefinitionNotesElement( chapter, ( Definition )ast ) ;
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
	public static class QANotesElement extends AbstractNotesElement {
		
		private QuestionAnswer ast = null ;
		
		private String question = null ;
		private String answer   = null ;
		private String cmapImg  = null ;
		
		public QANotesElement( Chapter chapter, QuestionAnswer ast ) {
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
	public static class WMNotesElement extends AbstractNotesElement {
		
		private WordMeaning ast = null ;
		
		private String word = null ;
		private String meaning   = null ;
		
		public WMNotesElement( Chapter chapter, WordMeaning ast ) {
			super( QA, chapter, ast ) ;
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
	public static class DefinitionNotesElement extends AbstractNotesElement {
		
		private Definition ast = null ;
		
		private String term       = null ;
		private String definition = null ;
		private String cmapImg    = null ;
		
		public DefinitionNotesElement( Chapter chapter, Definition ast ) {
			super( QA, chapter, ast ) ;
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
}
