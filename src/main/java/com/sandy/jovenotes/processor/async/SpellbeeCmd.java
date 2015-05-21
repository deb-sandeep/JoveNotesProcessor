package com.sandy.jovenotes.processor.async;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.core.notes.Chapter;
import com.sandy.jovenotes.processor.util.StringUtil;

public class SpellbeeCmd extends PersistedCmd implements Serializable {
	
	private static final long serialVersionUID = -7123291882777033563L ;

	private transient static Logger log = Logger.getLogger( SpellbeeCmd.class ) ;
	
	private String word = null ;
	private String neObjId = null ;
	private String cardObjId = null ;
	
	public SpellbeeCmd( Chapter chapter, String word, 
			            String neObjId, String cardObjId ) {
		
		super( chapter ) ;
		
		this.word = word ;
		this.neObjId = neObjId ;
		this.cardObjId = cardObjId ;
	}

	public void execute() throws Exception {
		log.debug( "\tExecuting SpellbeeCmd for word - " + word ) ;
		throw new Exception( "Test" ) ;
	}
	
	public String getUID() {
		return StringUtil.getHash( syllabusName + subjectName + chapterNum + 
				                   subChapterNum + word + neObjId + cardObjId );
	}
	
	public String toString(){
		return "SpellbeeCmd [" + 
	           "word = "      + word      + ", " + 
			   "neObjId = "   + neObjId   + ", " + 
	           "cardObjId = " + cardObjId + "]" ;
	}
}
