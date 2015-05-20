package com.sandy.jovenotes.processor.core.notes;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import com.sandy.jovenotes.processor.util.JNTextProcessor;
import com.sandy.jovenotes.processor.util.StringUtil;

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
		
		private String type = null ;
		private String objId = null ;
		
		public AbstractCard( String type ) {
			this.type = type ;
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
		
		private JNTextProcessor textProcessor = null ;
		
		public QACard( String rawQ, String rawA, JNTextProcessor textProcessor ) 
				throws Exception {
			this( rawQ, rawA, null, textProcessor ) ;
		}
	
		public QACard( String rawQ, String rawA, String cmapImg, 
				       JNTextProcessor textProcessor ) throws Exception {
			
			super( QA ) ;
			this.textProcessor = textProcessor ;
			this.rawQuestion   = rawQ ;
			this.rawAnswer     = rawA ;
			
			if( cmapImg != null ) {
				this.rawAnswer += "<p>{{@img " + cmapImg + "}}" ;
			}
		}
		
		public String getObjIdSeed() { 
			return this.rawQuestion ;
		}
		
		public int getDifficultyLevel() {
			int numWords = rawQuestion.split( "\\s+" ).length ;
			double nDiff = (2/( 1 + Math.exp( -1*DIFFICULTY_FACTOR*numWords ))) - 1 ;
			
			return (int)Math.ceil( nDiff*100 ) ;
		}
		
		public void collectContentAttributes( Map<String, Object> map ) 
			throws Exception {
			
			map.put( "question", textProcessor.processText( rawQuestion ) ) ;
			map.put( "answer",   textProcessor.processText( rawAnswer ) ) ;
		}
	}
}
