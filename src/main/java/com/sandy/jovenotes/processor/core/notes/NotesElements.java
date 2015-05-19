package com.sandy.jovenotes.processor.core.notes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.notes.Cards.AbstractCard;
import com.sandy.jovenotes.processor.core.notes.Cards.QACard;
import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.jovenotes.processor.util.StringUtil;
import com.sandy.xtext.joveNotes.NotesElement;
import com.sandy.xtext.joveNotes.QuestionAnswer;

public class NotesElements {
	
	private static Logger log = Logger.getLogger( NotesElements.class ) ;
	
	public static final String WM            = "word_meaning" ;
	public static final String QA            = "question_answer" ;
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
		
		log.debug( "Built notes element. objId = " + notesElement.getObjId() + 
				   ", type = " + notesElement.getType() );
		return notesElement ;
	}

	// -------------------------------------------------------------------------
	public static abstract class AbstractNotesElement {
		
		private String objId           = null ;
		private int    difficultyLevel = -1 ;
		
		protected Chapter chapter = null ;
		
		public AbstractNotesElement( Chapter chapter ) {
			this.chapter = chapter ;
		}
		
		public Chapter getChapter() { 
			return this.chapter ; 
		}
		
		public final String getObjId() {
			
			if( this.objId == null ){
				this.objId = StringUtil.getHash( getObjIdSeed() ) ; 
			}
			return this.objId ;
		} ;
		
		public int getDifficultyLevel() {
			
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

		public String getContent() {
			log.error( "TODO: To implement NotesElements::getContent()" ) ;
			return "TODO" ;
		}
		
		public abstract void processNotesContent( JNTextProcessor textProcessor ) 
				throws Exception ;
		
		public abstract String getType() ;
		
		public abstract String getObjIdSeed() ;
		
		public abstract List<AbstractCard> getCards() ;
	}
	
	// -------------------------------------------------------------------------
	public static class QANotesElement extends AbstractNotesElement {
		
		private QuestionAnswer ast = null ;
		
		private String question = null ;
		private String answer   = null ;
		
		private List<AbstractCard> cards = null ;
		
		public QANotesElement( Chapter chapter, QuestionAnswer ast ) {
			super( chapter ) ;
			this.ast = ast ;
		}
		
		public String getType() { return QA ; }
		public String getObjIdSeed() { return this.ast.getQuestion() ; }
		
		public void processNotesContent( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.question = textProcessor.processText( ast.getQuestion() ) ;
			this.answer   = textProcessor.processText( ast.getAnswer() ) ;
			
			log.debug( "Processed question = " + question ) ;
			log.debug( "Processed answer   = " + answer ) ;
		}
		
		public List<AbstractCard> getCards() {
			
			if( cards == null ) {
				cards = new ArrayList<AbstractCard>() ;
				cards.add( new QACard( this, question, answer ) ) ;
			}
			return cards ;
		}
	}
}
