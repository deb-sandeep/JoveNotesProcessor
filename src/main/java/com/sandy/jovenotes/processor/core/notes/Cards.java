package com.sandy.jovenotes.processor.core.notes;

import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement;

public class Cards {

//	private static Logger log = Logger.getLogger( Cards.class ) ;
	
	public static final String FIB      = "fib" ;
	public static final String QA       = "question_answer" ;
	public static final String TF       = "true_false" ;
	public static final String MATCHING = "matching" ;
	public static final String IMGLABEL = "image_label" ;
	public static final String SPELLBEE = "spellbee" ;
	
	// -------------------------------------------------------------------------
	public static abstract class AbstractCard {
		
		private AbstractNotesElement notesElement = null ;
		
		public AbstractCard( AbstractNotesElement notesElement ) {
			this.notesElement = notesElement ;
		}
		
		public abstract String getType() ;
		
		public AbstractNotesElement getNotesElement(){ return this.notesElement; }
	}
	
	// -------------------------------------------------------------------------
	public static class QACard extends AbstractCard {
		
		public QACard( AbstractNotesElement notesElement, 
				       String question, String answer ) {
			
			super( notesElement ) ;
		}
		
		public String getType(){ return QA; }
	}
}
