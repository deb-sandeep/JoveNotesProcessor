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
		
		log.debug( "Built notes element. UID = " + notesElement.getUID() + 
				   ", type = " + notesElement.getType() );
		return notesElement ;
	}

	// -------------------------------------------------------------------------
	public static abstract class AbstractNotesElement {
		
		private String uid = null ;
		
		protected Chapter chapter = null ;
		
		public AbstractNotesElement( Chapter chapter ) {
			this.chapter = chapter ;
		}
		
		public Chapter getChapter() { 
			return this.chapter ; 
		}
		
		public final String getUID() {
			if( this.uid != null ) return this.uid ;
			return StringUtil.getHash( this.chapter.getUID() + getUIDSeed() ) ;
		} ;
		
		public abstract String getType() ;
		public abstract String getUIDSeed() ;
		public abstract void processNotesContent( JNTextProcessor textProcessor ) 
				throws Exception ;
		public abstract List<AbstractCard> getCards() ;
	}
	
	// -------------------------------------------------------------------------
	public static class QANotesElement extends AbstractNotesElement {
		
		private QuestionAnswer ast = null ;
		
		private String question = null ;
		private String answer   = null ;
		
		public QANotesElement( Chapter chapter, QuestionAnswer ast ) {
			super( chapter ) ;
			this.ast = ast ;
		}
		
		public String getType() { return QA ; }
		public String getUIDSeed() { return this.ast.getQuestion() ; }
		
		public void processNotesContent( JNTextProcessor textProcessor ) 
				throws Exception {
			
			this.question = textProcessor.processText( ast.getQuestion() ) ;
			this.answer   = textProcessor.processText( ast.getAnswer() ) ;
			
			log.debug( "Processed question = " + question ) ;
			log.debug( "Processed answer   = " + answer ) ;
		}
		
		public List<AbstractCard> getCards() {
			
			List<AbstractCard> cards = new ArrayList<AbstractCard>() ;
			cards.add( new QACard( this, question, answer ) ) ;
			return cards ;
		}
	}
}
