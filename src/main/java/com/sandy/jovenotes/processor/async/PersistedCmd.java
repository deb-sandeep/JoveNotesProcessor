package com.sandy.jovenotes.processor.async;

import java.io.Serializable;

import com.sandy.jovenotes.processor.core.notes.Chapter;

/**
 * Superclass of all command data which are stored and processed via 
 * PersistentQueue.
 * 
 * There are certain activities like downloading sound clips, word meanings
 * and pronounciation which can't be guaranteed to complete in a standard run.
 * For example, if the network connection is patchy or the server is not available.
 * 
 * To solve this design challenge, we convert such activities into persisted
 * commands and process them via an asynchronous processor feeding off the 
 * persisted queue. This way, if these commands don't get processed during a
 * standard run, they will be picked up in the next run.
 * 
 * @author Sandeep
 */
public abstract class PersistedCmd implements Serializable {

	private static final long serialVersionUID = -1919148238331312275L;
	
	protected String syllabusName = null ;
	protected String subjectName  = null ;
	protected int    chapterNum   = -1 ;
	protected int    subChapterNum= -1 ;
	
	protected PersistedCmd( Chapter chapter ) {
		this.syllabusName = chapter.getSyllabusName() ;
		this.subjectName  = chapter.getSubjectName() ;
		this.chapterNum   = chapter.getChapterNumber() ;
		this.subChapterNum= chapter.getSubChapterNumber() ;
	}

	public abstract void execute() throws Exception ;
	
	public abstract String getUID() ;
}
