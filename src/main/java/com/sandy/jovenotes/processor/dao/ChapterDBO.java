package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;

public class ChapterDBO {

	private static final Logger log = Logger.getLogger( ChapterDBO.class ) ;
	
	private int    chapterId      = -1 ;
	private String syllabusName   = null ;
	private int    chapterNum     = 0 ;
	private int    subChapterNum  = 0 ;
	private String chapterName    = null ;
	
	private List<NotesElementDBO> notesElements = null ;
	
	private ChapterDBO( ResultSet rs ) throws Exception {
		
		chapterId     = rs.getInt    ( "chapter_id"      ) ;
		syllabusName  = rs.getString ( "syllabus_name"   ) ;
		chapterNum    = rs.getInt    ( "chapter_num"     ) ;
		subChapterNum = rs.getInt    ( "sub_chapter_num" ) ;
		chapterName   = rs.getString ( "chapter_name"    ) ;
	}
	
	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId( int chapterId ) {
		this.chapterId = chapterId;
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
		
		final String sql = "SELECT `chapter`.`chapter_id`," + 
                           " `chapter`.`syllabus_name`," +
                           " `chapter`.`chapter_num`," +
                           " `chapter`.`sub_chapter_num`," +
                           " `chapter`.`chapter_name`" + 
                           " FROM `jove_notes`.`chapter`" ;
		
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
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
	
	public static ChapterDBO get( int chapterId ) throws Exception {
		
		final String sql = "SELECT `chapter`.`chapter_id`," + 
			                " `chapter`.`syllabus_name`," +
			                " `chapter`.`chapter_num`," +
			                " `chapter`.`sub_chapter_num`," +
			                " `chapter`.`chapter_name`" + 
			                " FROM `jove_notes`.`chapter` " +
			                " WHERE `chapter`.`chapter_id`=?" ;
		
		ChapterDBO chapter = null ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
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

		final String sql = 
		"INSERT INTO `jove_notes`.`chapter` " +
		"(`syllabus_name`, `chapter_num`, `sub_chapter_num`, `chapter_name`) " +
		"VALUES " +
		"( ?, ?, ?, ? )" ;

		int generatedId = -1 ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setString ( 1, getSyllabusName() ) ;
			psmt.setInt    ( 2, getChapterNum() ) ;
			psmt.setInt    ( 3, getSubChapterNum() ) ;
			psmt.setString ( 4, getChapterName() ) ;
			
			psmt.executeUpdate() ;
			generatedId = psmt.getGeneratedKeys().getInt( 1 ) ;
			setChapterId( generatedId ) ;
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
			"`syllabus_name` = ? " +
			"`chapter_num` = ? " +
			"`sub_chapter_num` = ? " +
			"`chapter_name` = ? " +
			"WHERE `chapter_id` = ? " ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setString ( 1, getSyllabusName() ) ;
			psmt.setInt    ( 2, getChapterNum() ) ;
			psmt.setInt    ( 3, getSubChapterNum() ) ;
			psmt.setString ( 4, getChapterName() ) ;
			psmt.setInt    ( 5, getChapterId() ) ;
			
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
			log.debug( "Firing query - " + sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt ( 1, getChapterId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.closeConnection( conn ) ;
		}
	}
}
