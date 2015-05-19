package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement;

public class NotesElementDBO {
	
	private static final Logger log = Logger.getLogger( NotesElementDBO.class ) ;

	private int    notesElementId  = 0 ;
	private int    chapterId       = 0 ;
	private String elementType     = null ;
	private int    difficultyLevel = 0 ;
	private String content         = null ;
	private String objCorrelId     = null ;
	
	private ChapterDBO chapter = null ;
	private List<CardDBO> cards = new ArrayList<CardDBO>() ;
	
	public NotesElementDBO( AbstractNotesElement ne ) {
		
		elementType     = ne.getType() ;
		difficultyLevel = ne.getDifficultyLevel() ;
		content         = ne.getContent() ;
		objCorrelId     = ne.getObjId() ;
	}
	
	private NotesElementDBO( ChapterDBO chapter, ResultSet rs ) throws Exception {
		
		this.chapter = chapter ;
		
		notesElementId  = rs.getInt    ( "notes_element_id" ) ;
		chapterId       = rs.getInt    ( "chapter_id"       ) ;
		elementType     = rs.getString ( "element_type"     ) ;
		difficultyLevel = rs.getInt    ( "difficulty_level" ) ;
		content         = rs.getString ( "content"          ) ;
		objCorrelId     = rs.getString ( "obj_correl_id"    ) ;		
	}
	
	public int getNotesElementId() {
		return notesElementId;
	}

	public void setNotesElementId(int notesElementId) {
		this.notesElementId = notesElementId;
	}

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
	}

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public int getDifficultyLevel() {
		return difficultyLevel;
	}

	public void setDifficultyLevel(int difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getObjCorrelId() {
		return objCorrelId;
	}

	public void setObjCorrelId(String objCorrelId) {
		this.objCorrelId = objCorrelId;
	}
	
	public ChapterDBO getChapter() throws Exception {
		if( this.chapter == null ) {
			this.chapter = ChapterDBO.get( getChapterId() ) ;
		}
		return this.chapter ;
	}

	public static List<NotesElementDBO> getAll( ChapterDBO chapter )
			throws Exception {
			
		ArrayList<NotesElementDBO> elements = new ArrayList<NotesElementDBO>() ;
		
		final String sql = 
			"SELECT " +
			"`notes_element`.`notes_element_id`, " +
			"`notes_element`.`chapter_id`, " +
			"`notes_element`.`element_type`, " +
			"`notes_element`.`difficulty_level`, " +
			"`notes_element`.`content`, " +
			"`notes_element`.`obj_correl_id` " +
			"FROM " +
			"`jove_notes`.`notes_element` " +
			"ORDER BY " + 
			"`notes_element`.`notes_element_id` ASC ";

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			ResultSet rs = psmt.executeQuery() ;
			
			while( rs.next() ) {
				elements.add( new NotesElementDBO( chapter, rs ) ) ;
			}
			
			loadAndAssociateCardsWithNotesElements( chapter, elements ) ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		
		return elements ;
	}
	
	private static void loadAndAssociateCardsWithNotesElements( 
			ChapterDBO chapter, ArrayList<NotesElementDBO> notesElements ) 
		throws Exception {
		
		HashMap<Integer, NotesElementDBO> notesElementMap = null ;
		notesElementMap = new HashMap<Integer, NotesElementDBO>() ;
		
		for( NotesElementDBO notesElement : notesElements ) {
			notesElementMap.put( notesElement.notesElementId, notesElement ) ;
		}
		
		for( CardDBO card : CardDBO.getAll( chapter.getChapterId() ) ) {
			NotesElementDBO ne = notesElementMap.get( card.getNotesElementId() ) ;
			if( ne != null ){
				ne.cards.add( card ) ;
				card.setNotesElement( ne ) ;
				card.setChapter( chapter ) ;
			}
			else {
				log.error( "Orphan card found!! card id = " + card.getCardId() ) ;
			}
		}
	}

	public int create() throws Exception {

		final String sql = 
		"INSERT INTO `jove_notes`.`notes_element` " +
		"(`chapter_id`, `element_type`, `difficulty_level`, `content`, `obj_correl_id`) " +
		"VALUES " +
		"( ?, ?, ?, ?, ? )" ;

		int generatedId = -1 ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql, 
					                         Statement.RETURN_GENERATED_KEYS ) ;
			
			psmt.setInt    ( 1, getChapterId() ) ;
			psmt.setString ( 2, getElementType() ) ;
			psmt.setInt    ( 3, getDifficultyLevel() ) ;
			psmt.setString ( 4, getContent() ) ;
			psmt.setString ( 5, getObjCorrelId() ) ;
			
			psmt.executeUpdate() ;
			ResultSet rs = psmt.getGeneratedKeys() ;
			if( null != rs && rs.next()) {
			     generatedId = (int)rs.getLong( 1 ) ;
			}
			else {
				throw new Exception( "Autogenerated key not obtained for notes element." ) ;
			}
			setNotesElementId( generatedId ) ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		return generatedId ;
	}
	
	public void update() throws Exception {
		
		final String sql = 
			"UPDATE `jove_notes`.`notes_elements` " +
			"SET " +
			"`chapter_id` = ? " +
			"`element_type` = ? " +
			"`difficulty_level` = ? " +
			"`content` = ? " +
			"`obj_correl_id` = ? " +
			"WHERE `notes_element_id` = ? " ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt    ( 1, getChapterId() ) ;
			psmt.setString ( 2, getElementType() ) ;
			psmt.setInt    ( 3, getDifficultyLevel() ) ;
			psmt.setString ( 4, getContent() ) ;
			psmt.setString ( 5, getObjCorrelId() ) ;
			psmt.setInt    ( 6, getNotesElementId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
	}

	public void deleteChapter() throws Exception {
		
		final String sql = 
			"DELETE FROM `jove_notes`.`notes_elements` WHERE `notes_element_id` = ?" ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt ( 1, getNotesElementId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
	}
}
