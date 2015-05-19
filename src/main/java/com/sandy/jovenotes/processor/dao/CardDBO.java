package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;

public class CardDBO {
	
	private static final Logger log = Logger.getLogger( CardDBO.class ) ;

	private int    cardId          = 0 ;
	private int    notesElementId  = 0 ;
	private int    chapterId       = 0 ;
	private String cardType        = null ;
	private int    difficultyLevel = 0 ;
	private String content         = null ;
	private String objCorrelId     = null ;
	
	private ChapterDBO chapter = null ;
	private NotesElementDBO notesElement = null ;
	
	private CardDBO( ResultSet rs ) throws Exception {
		
		cardId          = rs.getInt    ( "card_id"          ) ;
		notesElementId  = rs.getInt    ( "notes_element_id" ) ;
		chapterId       = rs.getInt    ( "chapter_id"       ) ;
		cardType        = rs.getString ( "card_type"        ) ;
		difficultyLevel = rs.getInt    ( "difficulty_level" ) ;
		content         = rs.getString ( "content"          ) ;
		objCorrelId     = rs.getString ( "obj_correl_id"    ) ;		
	}
	
	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
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

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String elementType) {
		this.cardType = elementType;
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
	
	public ChapterDBO getChapter() {
		return chapter;
	}

	public void setChapter(ChapterDBO chapter) {
		this.chapter = chapter;
	}

	public NotesElementDBO getNotesElement() {
		return notesElement;
	}

	public void setNotesElement(NotesElementDBO notesElement) {
		this.notesElement = notesElement;
	}

	public static List<CardDBO> getAll( int chapterId )
			throws Exception {
			
		ArrayList<CardDBO> cards = new ArrayList<CardDBO>() ;
		
		final String sql = 
				" SELECT  " +
				" 	`card`.`card_id`, " +
				"   `card`.`notes_element_id`, " +
				"   `card`.`chapter_id`, " +
				"   `card`.`card_type`, " +
				"   `card`.`difficulty_level`, " +
				"   `card`.`content`, " +
				"   `card`.`obj_correl_id` " +
				" FROM " +
				"   `jove_notes`.`card` " +
				" WHERE " +
				"   `card`.`chapter_id` = ? " + 
				" ORDER BY " + 
				"   `card`.`card_id` ASC";

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt( 1, chapterId ) ;
			
			ResultSet rs = psmt.executeQuery() ;
			while( rs.next() ) {
				cards.add( new CardDBO( rs ) ) ;
			}
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		return cards ;
	}

	public int create() throws Exception {

		final String sql = 
		"INSERT INTO `jove_notes`.`card` " +
		"(`notes_element_id`, `chapter_id`, `card_type`, `difficulty_level`, `content`, `obj_correl_id`) " +
		"VALUES " +
		"(?, ?, ?, ?, ?, ? )" ;

		int generatedId = -1 ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt    ( 1, getNotesElementId() ) ;
			psmt.setInt    ( 2, getChapterId() ) ;
			psmt.setString ( 3, getCardType() ) ;
			psmt.setInt    ( 4, getDifficultyLevel() ) ;
			psmt.setString ( 5, getContent() ) ;
			psmt.setString ( 6, getObjCorrelId()  ) ;
			
			psmt.executeUpdate() ;
			generatedId = psmt.getGeneratedKeys().getInt( 1 ) ;
			setCardId( generatedId ) ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		return generatedId ;
	}
	
	public void update() throws Exception {
		
		final String sql = 
			"UPDATE `jove_notes`.`card` " +
			"SET " +
			"`notes_element_id` = ? " +
			"`chapter_id` = ? " +
			"`card_type` = ? " +
			"`difficulty_level` = ? " +
			"`content` = ? " +
			"`obj_correl_id` = ? " +
			"WHERE `card_id` = ? " ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt    ( 1, getNotesElementId() ) ;
			psmt.setInt    ( 2, getChapterId() ) ;
			psmt.setString ( 3, getCardType() ) ;
			psmt.setInt    ( 4, getDifficultyLevel() ) ;
			psmt.setString ( 5, getContent() ) ;
			psmt.setString ( 6, getObjCorrelId() ) ;
			psmt.setInt    ( 7, getCardId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
	}

	public void delete() throws Exception {
		
		final String sql = 
			"DELETE FROM `jove_notes`.`card` WHERE `card_id` = ?" ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt ( 1, getCardId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
	}
}
