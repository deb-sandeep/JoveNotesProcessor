package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.notes.Chapter;
import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement;

public class ChapterDBO extends AbstractDBO {

	private static final Logger log = Logger.getLogger( ChapterDBO.class ) ;
	
	private int    chapterId      = -1 ;
	private String syllabusName   = null ;
	private int    chapterNum     = 0 ;
	private int    subChapterNum  = 0 ;
	private String chapterName    = null ;
	private boolean isTestPaper   = false ;
	
	private String chapterFQN = null ;
	
	private List<NotesElementDBO> notesElements = null ;
	
	public ChapterDBO( Chapter chapter ) {
		
		chapterFQN   = chapter.getChapterFQN() ;
		
		syllabusName = chapter.getSyllabusName() ;
		chapterNum   = chapter.getChapterNumber() ;
		subChapterNum= chapter.getSubChapterNumber() ;
		chapterName  = chapter.getChapterName() ;
		
		notesElements = new ArrayList<NotesElementDBO>() ;
		for( AbstractNotesElement ne : chapter.getNotesElements() ) {
			notesElements.add( new NotesElementDBO( ne ) ) ;
		}
	}
	
	private ChapterDBO( ResultSet rs ) throws Exception {
		
		chapterId     = rs.getInt    ( "chapter_id"      ) ;
		syllabusName  = rs.getString ( "syllabus_name"   ) ;
		chapterNum    = rs.getInt    ( "chapter_num"     ) ;
		subChapterNum = rs.getInt    ( "sub_chapter_num" ) ;
		chapterName   = rs.getString ( "chapter_name"    ) ;
		isTestPaper   = rs.getBoolean( "is_test_paper"   ) ;
	}
	
	public boolean isTestPaper() {
		return isTestPaper;
	}

	public void setTestPaper(boolean isTestPaper) {
		this.isTestPaper = isTestPaper;
	}

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId( int chapterId ) {
		this.chapterId = chapterId;
		for( NotesElementDBO ne : this.notesElements ) {
			ne.setChapterId( chapterId ) ; 
		}
	}

	public String getSyllabusName() {
		return syllabusName;
	}

	public void setSyllabusName(String syllabusName) {
		this.syllabusName = syllabusName;
	}

	public int getChapterNum() {
		return chapterNum;
	}

	public void setChapterNum(int chapterNum) {
		this.chapterNum = chapterNum;
	}

	public int getSubChapterNum() {
		return subChapterNum;
	}

	public void setSubChapterNum(int subChapterNum) {
		this.subChapterNum = subChapterNum;
	}

	public String getChapterName() {
		return chapterName;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}
	
	public List<NotesElementDBO> getNotesElements() throws Exception {
		
		if( this.notesElements == null ) {
			this.notesElements = NotesElementDBO.getAll( this ) ;
		}
		return this.notesElements ;
	}

	public static List<ChapterDBO> getAll() throws Exception {
		
		ArrayList<ChapterDBO> chapters = new ArrayList<ChapterDBO>() ;
		
		final String sql = "SELECT " + 
		                   " `chapter`.`chapter_id`," + 
		                   " `chapter`.`is_test_paper`," + 
                           " `chapter`.`syllabus_name`," +
                           " `chapter`.`chapter_num`," +
                           " `chapter`.`sub_chapter_num`," +
                           " `chapter`.`chapter_name`" + 
                           " FROM `jove_notes`.`chapter`" ;
		
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "ChapterDBO::getAll", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			ResultSet rs = psmt.executeQuery() ;
			
			while( rs.next() ) {
				chapters.add( new ChapterDBO( rs ) ) ;
			}
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		
		return chapters ;
	}
	
	public static ChapterDBO get( Chapter chapter ) throws Exception {
		
		final String sql = 
				"SELECT " + 
				" chapter_id, " + 
			    " is_test_paper, " +
				" syllabus_name, " +
				" chapter_num, " +
				" sub_chapter_num, " +
				" chapter_name " + 
				"FROM " + 
				" jove_notes.chapter " +
				"WHERE " + 
				" syllabus_name   = ? and " + 
				" chapter_num     = ? and " + 
				" sub_chapter_num = ? " ;

		ChapterDBO dbo = null ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "ChapterDBO::get", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setString ( 1, chapter.getSyllabusName() ) ;
			psmt.setInt    ( 2, chapter.getChapterNumber() ) ;
			psmt.setInt    ( 3, chapter.getSubChapterNumber() );
			
			ResultSet rs = psmt.executeQuery() ;
			if( rs.next() ) {
				dbo = new ChapterDBO( rs ) ;
			}
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		
		return dbo ;
	}
	
	public static ChapterDBO get( int chapterId ) throws Exception {
		
		final String sql = "SELECT " + 
		                    " `chapter`.`chapter_id`," + 
		                    " `chapter`.`is_test_paper`," + 
			                " `chapter`.`syllabus_name`," +
			                " `chapter`.`chapter_num`," +
			                " `chapter`.`sub_chapter_num`," +
			                " `chapter`.`chapter_name`" + 
			                " FROM `jove_notes`.`chapter` " +
			                " WHERE `chapter`.`chapter_id`=?" ;
		
		ChapterDBO chapter = null ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "ChapterDBO::get", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt( 1, chapterId ) ;
			
			ResultSet rs = psmt.executeQuery() ;
			
			if( rs.next() ) {
				chapter = new ChapterDBO( rs ) ;
			}
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		
		return chapter ;
	}
	
	public int create() throws Exception {

		log.debug( "\tCreating chapter - " + chapterFQN ) ;
		
		final String sql = 
		"INSERT INTO `jove_notes`.`chapter` " +
		"(`is_test_paper`,`syllabus_name`, `chapter_num`, `sub_chapter_num`, `chapter_name`) " +
		"VALUES " +
		"( ?, ?, ?, ?, ? )" ;

		int generatedId = -1 ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "ChapterDBO::create", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql, 
					                         Statement.RETURN_GENERATED_KEYS ) ;
			
			psmt.setBoolean( 1, isTestPaper() ) ;
			psmt.setString ( 2, getSyllabusName() ) ;
			psmt.setInt    ( 3, getChapterNum() ) ;
			psmt.setInt    ( 4, getSubChapterNum() ) ;
			psmt.setString ( 5, getChapterName() ) ;
			
			psmt.executeUpdate() ;
			ResultSet rs = psmt.getGeneratedKeys() ;
			if( null != rs && rs.next()) {
			     generatedId = (int)rs.getLong( 1 ) ;
			}
			else {
				throw new Exception( "Autogenerated key not obtained for chapter." ) ;
			}
			setChapterId( generatedId ) ;
			for( NotesElementDBO ne : this.notesElements ) {
				ne.create() ;
			}
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
		return generatedId ;
	}
	
	public void update() throws Exception {
		
		final String sql = 
			"UPDATE `jove_notes`.`chapter` " +
			"SET " +
			"`is_test_paper` = ? " +
			"`syllabus_name` = ? " +
			"`chapter_num` = ? " +
			"`sub_chapter_num` = ? " +
			"`chapter_name` = ? " +
			"WHERE `chapter_id` = ? " ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "ChapterDBO::update", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setBoolean( 1, isTestPaper() ) ;
			psmt.setString ( 2, getSyllabusName() ) ;
			psmt.setInt    ( 3, getChapterNum() ) ;
			psmt.setInt    ( 4, getSubChapterNum() ) ;
			psmt.setString ( 5, getChapterName() ) ;
			psmt.setInt    ( 6, getChapterId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
	}

	public void delete() throws Exception {
		
		final String sql = 
			"DELETE FROM `jove_notes`.`chapter` WHERE `chapter_id` = ?" ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "ChapterDBO::delete", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt ( 1, getChapterId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
	}
}
