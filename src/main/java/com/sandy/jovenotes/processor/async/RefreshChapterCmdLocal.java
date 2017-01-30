package com.sandy.jovenotes.processor.async;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.util.StringUtil;

/**
 * This command refreshes the total number of cards, num of cards at each 
 * difficulty level for the given chapter.
 */
public class RefreshChapterCmdLocal extends PersistedCmd implements Serializable {
	
	private static final long serialVersionUID = -7123291882777033563L ;

	private transient static Logger log = Logger.getLogger( RefreshChapterCmd.class ) ;
	
	private int chapterId = 0 ;
	
	public RefreshChapterCmdLocal( Chapter chapter, int chapterId ) {
		super( chapter ) ;
		this.chapterId = chapterId ;
	}

	public void execute() throws Exception {
	    
	    log.info( "\tExecuting RefreshChapterCmdLocal for id - " + chapterId ) ;
		try {
			refreshMetaData() ;
		} 
		catch( Exception e ){
			log.error( "Could not process refresh chapter command local.", e ) ;
			throw e ;
		}
	}
	
	private void refreshMetaData() throws Exception {
		
		String sql = 
        "select count(*) " +
        "from " +
                "jove_notes.card " +
        "where " +
                "chapter_id = ? and " +
                "ready = 1 ";
		
		int numCards = 0 ;
		
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "RefreshChapterCmdLocal::refreshMetaData", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt( 1, chapterId ) ;
			
			ResultSet rs = psmt.executeQuery() ;
			while( rs.next() ) {
				int    count = rs.getInt(1) ;
				numCards += count;
			}
			
			updateMetaData( numCards ) ;
		}
		finally {
			JoveNotes.db.returnConnection( conn ) ;
		}
	}
	
	/**
	 * Refreshes chapter level meta data such as number of (ready) cards, 
	 * num cards at different levels of difficulty (VE, E, M, H, VH).
	 */
	public void updateMetaData( int numCards ) 
	    throws Exception {
		
		String sql = 
				"update jove_notes.chapter " + 
		        "set" +
				" num_cards = ? " +
				"where " + 
		        " chapter_id = ? " ;
		
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "RefreshChapterCmd::updateMetaData", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt( 1, numCards ) ;
			psmt.setInt( 2, chapterId ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.returnConnection( conn ) ;
		}
	}
	
	public String getUID() {
		return StringUtil.getHash( Integer.toString( chapterId ) );
	}
	
	public String toString(){
		return "SpellbeeCmd [ chapterId=" + chapterId + "]" ; 
	}
}
