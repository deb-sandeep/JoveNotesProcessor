package com.sandy.jovenotes.processor.async;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.util.RunMode;
import com.sandy.jovenotes.processor.util.StringUtil;

/**
 * This command refreshes the total number of cards, num of cards at each 
 * difficulty level for the given chapter.
 */
public class RefreshChapterCmd extends PersistedCmd implements Serializable {
	
	private static final long serialVersionUID = -7123291882777033563L ;

	private transient static Logger log = Logger.getLogger( RefreshChapterCmd.class ) ;
	
	private int chapterId = 0 ;
	
	public RefreshChapterCmd( Chapter chapter, int chapterId ) {
		super( chapter ) ;
		this.chapterId = chapterId ;
	}

	public void execute() throws Exception {
		
	    RunMode runMode = JoveNotes.config.getRunMode() ;
	    if ( runMode.isPreview() ) {
	        log.info( "\tSkipping RefreshChapterCmd in the preview mode") ;
	        return ;
	    }
	    
	    log.info( "\tExecuting RefreshChapterCmd for id - " + chapterId ) ;
		try {
			refreshMetaData() ;
		} 
		catch( Exception e ){
			log.error( "Could not process refresh chapter command.", e ) ;
			throw e ;
		}
	}
	
	private void refreshMetaData() throws Exception {
		
		String sql = 
        "select " +
            "if( difficulty_level < 30, 'VE', " +
               "if( difficulty_level < 50, 'E', " +
                  "if( difficulty_level < 75, 'M', " +
                     "if( difficulty_level < 90, 'H', 'VH') " +
                  ") " +
               ") " +
            ") as level, count(*) " +
        "from " +
                "jove_notes.card " +
        "where " +
                "chapter_id = ? and " +
                "ready = 1 " +
        "group by level " ;
		
		int numCards = 0 ;
		int numVE = 0, numE = 0, numM = 0, numH = 0, numVH = 0 ;
		
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "RefreshChapterCmd::refreshMetaData", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt( 1, chapterId ) ;
			
			ResultSet rs = psmt.executeQuery() ;
			while( rs.next() ) {
				String level = rs.getString(1) ;
				int    count = rs.getInt(2) ;
				
				numCards += count ;
				if     ( level.equals( "VE" ) ) numVE = count ;
				else if( level.equals( "E"  ) ) numE  = count ;
				else if( level.equals( "M"  ) ) numM  = count ;
				else if( level.equals( "H"  ) ) numH  = count ;
				else if( level.equals( "VH" ) ) numVH = count ;
			}
			
			updateMetaData( numCards, numVE, numE, numM, numH, numVH ) ;
		}
		finally {
			JoveNotes.db.returnConnection( conn ) ;
		}
	}
	
	/**
	 * Refreshes chapter level meta data such as number of (ready) cards, 
	 * num cards at different levels of difficulty (VE, E, M, H, VH).
	 */
	public void updateMetaData( int numCards, int numVE, int numE, int numM, 
			                    int numH, int numVH ) 
	    throws Exception {
		
		String sql = 
				"update jove_notes.chapter " + 
		        "set" +
				" num_cards = ?, " +
		        " num_VE = ?, " +
				" num_E = ?, " +
		        " num_M = ?, " +
				" num_H = ?, " + 
		        " num_VH = ? " +
				"where " + 
		        " chapter_id = ? " ;
		
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "RefreshChapterCmd::updateMetaData", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt( 1, numCards ) ;
			psmt.setInt( 2, numVE ) ;
			psmt.setInt( 3, numE ) ;
			psmt.setInt( 4, numM ) ;
			psmt.setInt( 5, numH ) ;
			psmt.setInt( 6, numVH ) ;
			psmt.setInt( 7, chapterId ) ;
			
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
