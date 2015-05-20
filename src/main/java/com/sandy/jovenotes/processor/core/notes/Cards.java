package com.sandy.jovenotes.processor.core.notes;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement;
import com.sandy.jovenotes.processor.core.notes.NotesElements.QANotesElement;
import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.jovenotes.processor.util.StringUtil;
import com.sandy.xtext.joveNotes.QuestionAnswer;

public class Cards {

	//private static Logger log = Logger.getLogger( Cards.class ) ;
	
	public static final String QA       = "question_answer" ;
	public static final String FIB      = "fib" ;
	public static final String TF       = "true_false" ;
	public static final String MATCHING = "matching" ;
	public static final String IMGLABEL = "image_label" ;
	public static final String SPELLBEE = "spellbee" ;
	
	// -------------------------------------------------------------------------
	public static abstract class AbstractCard {
		
		private AbstractNotesElement notesElement = null ;
		private String type = null ;
		private String objId = null ;
		
		public AbstractCard( String type, AbstractNotesElement notesElement ) {
			this.type = type ;
			this.notesElement = notesElement ;
		}
		
		public AbstractNotesElement getNotesElement(){ 
			return this.notesElement; 
		}
		
		public String getType() {
			return this.type ;
		}
		
		public final String getObjId() {
			
			if( this.objId == null ){
				this.objId = StringUtil.getHash( "Card" + getType() + getObjIdSeed() ) ; 
			}
			return this.objId ;
		} ;
		
		public String getContent() {
			Map<String, Object> map = new LinkedHashMap<String, Object>() ;
			
			collectContentAttributes( map ) ;
			String content = JSONValue.toJSONString( map ) ;
			
			return content ;
		}
		
		protected abstract String getObjIdSeed() ;
		
		public abstract int getDifficultyLevel() ;
		
		protected abstract void collectContentAttributes( Map<String, Object> map ) ; 
	}
	
	// -------------------------------------------------------------------------
	public static class QACard extends AbstractCard {
		
		private static final double DIFFICULTY_FACTOR = 0.037505 ;
		
		private QuestionAnswer ast = null ;
		private String question = null ;
		private String answer = null ;
		
		public QACard( QANotesElement notesElement, QuestionAnswer ast, 
				       JNTextProcessor textProcessor ) 
			throws Exception {
			
			super( QA, notesElement ) ;
			this.ast      = ast ;
			this.question = textProcessor.processText( ast.getQuestion() ) ;
			this.answer   = textProcessor.processText( ast.getAnswer() ) ;
		}
		
		public String getObjIdSeed() { 
			return ast.getQuestion() ;
		}
		
		public int getDifficultyLevel() {
			int numWords = ast.getAnswer().split( "\\s+" ).length ;
			double nDiff = (2/( 1 + Math.exp( -1*DIFFICULTY_FACTOR*numWords ))) - 1 ;
			
			return (int)Math.ceil( nDiff*100 ) ;
		}
		
		public void collectContentAttributes( Map<String, Object> map ) {
			map.put( "question", question ) ;
			map.put( "answer", answer ) ;
		}
	}
}
