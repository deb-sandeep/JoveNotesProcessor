package com.sandy.jovenotes.processor.core.notes;

import java.util.LinkedHashMap;
import java.util.List;
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
		
		protected boolean ready = true ;
		
		public AbstractCard( String type ) {
			this.type = type ;
		}
		
		public String getType() {
			return this.type ;
		}
		
		public boolean isReady() {
			return this.ready ;
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
			int numWords = rawAnswer.split( "\\s+" ).length ;
			double nDiff = (2/( 1 + Math.exp( -1*DIFFICULTY_FACTOR*numWords ))) - 1 ;
			
			return (int)Math.ceil( nDiff*100 ) ;
		}
		
		public void collectContentAttributes( Map<String, Object> map ) 
			throws Exception {
			
			map.put( "question", textProcessor.processText( rawQuestion ) ) ;
			map.put( "answer",   textProcessor.processText( rawAnswer ) ) ;
		}
	}

	// -------------------------------------------------------------------------
	public static class FIBCard extends AbstractCard {
		
		private static final double DIFFICULTY_FACTOR = 0.67535 ;
		private static final double X_SHIFT = -0.1 ;
		private static final double MAX_LIMIT = 0.6 ;
		
		private String       rawQuestion = null ;
		private List<String> rawAnswers  = null ;
		
		private JNTextProcessor textProcessor = null ;
		
		public FIBCard( String rawQ, List<String> answers, 
				        JNTextProcessor textProcessor ) throws Exception {
			
			super( FIB ) ;
			this.textProcessor = textProcessor ;
			this.rawQuestion   = rawQ ;
			this.rawAnswers    = answers ;
		}
		
		public String getObjIdSeed() { 
			StringBuilder seed = new StringBuilder() ;
			for( String answer : rawAnswers ) {
				seed.append( answer ) ;
			}
			return seed.toString() ;
		}
		
		public int getDifficultyLevel() {
			int x = rawAnswers.size() ;
			double nDiff = (2*(1/(1+Math.exp(-1*DIFFICULTY_FACTOR*(x-X_SHIFT)))-.5))*MAX_LIMIT ;
			return (int)Math.ceil( nDiff*100 ) ;
		}
		
		public void collectContentAttributes( Map<String, Object> map ) 
			throws Exception {
			
			map.put( "question", textProcessor.processText( rawQuestion ) ) ;
			map.put( "answers",  rawAnswers ) ;
		}
	}

	// -------------------------------------------------------------------------
	public static class MatchCard extends AbstractCard {
		
		private static final double DIFFICULTY_FACTOR = 0.26954 ;
		private static final double X_SHIFT = 0.0 ;
		private static final double MAX_LIMIT = 0.75 ;
		
		private List<List<String>> fmtMatchPairs = null ;
		private String caption = null ;
		
		private String objIdSeed = null ;
		
		public MatchCard( String objIdSeed, String caption,
				          List<List<String>> fmtMatchPairs ) throws Exception {
			
			super( MATCHING ) ;
			this.objIdSeed = objIdSeed ;
			this.fmtMatchPairs = fmtMatchPairs ;
			this.caption = ( caption == null ) ? "Match the following" : caption ;
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
			map.put( "caption", caption ) ;
			map.put( "matchData", fmtMatchPairs ) ;
		}
	}

	// -------------------------------------------------------------------------
	public static class TrueFalseCard extends AbstractCard {
		
		private String  statement     = null ;
		private boolean truthValue    = false ;
		private String  justification = null ;
		
		private String objIdSeed = null ;
		
		public TrueFalseCard( String objIdSeed, String statement,
				              boolean truthValue, String justification ) 
				            		  throws Exception {
			super( TF ) ;
			this.objIdSeed     = objIdSeed ;
			this.statement     = statement ;
			this.truthValue    = truthValue ;
			this.justification = justification ;
		}
		
		public String getObjIdSeed() { return objIdSeed ; }
		
		public int getDifficultyLevel() { return 20 ; }
		
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
		
		public SpellbeeCard( String objIdSeed ) throws Exception {
			super( SPELLBEE ) ;
			this.objIdSeed = objIdSeed ;
			super.ready = false ;
		}
		
		public String getObjIdSeed() { return objIdSeed ; }
		public int getDifficultyLevel() { return objIdSeed.length()*3 ; }
		public void collectContentAttributes( Map<String, Object> map ){} 
	}

	// -------------------------------------------------------------------------
	public static class ImageLabelCard extends AbstractCard {
		
		private static final double DIFFICULTY_FACTOR = 0.26954 ;
		private static final double X_SHIFT = 0.0 ;
		private static final double MAX_LIMIT = 0.75 ;
		
		private String objIdSeed = null ;
		private Map<String, Object> contentAttributes = null ;
		
		public ImageLabelCard( String objIdSeed,
				               Map<String, Object> contentAttributes ) 
		    throws Exception {
			
			super( IMGLABEL ) ;
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
}
