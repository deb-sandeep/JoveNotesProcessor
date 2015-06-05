package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.notes.Cards.AbstractCard;
import com.sandy.jovenotes.processor.core.notes.NotesElements.AbstractNotesElement;

public class NotesElementDBO extends AbstractDBO {
	
	private static final Logger log = Logger.getLogger( NotesElementDBO.class ) ;

	private int    notesElementId  = -1 ;
	private int    chapterId       = -1 ;
	private String elementType     = null ;
	private int    difficultyLevel = 0 ;
	private String content         = null ;
	private String objCorrelId     = null ;
	private boolean ready          = true ;
	private boolean hiddenFromView = false ;
	
	private boolean sourceTrace = false ;
	private boolean isModified  = false ;
	private boolean isDeleted   = false ;
	
	private ChapterDBO chapter = null ;
	private List<CardDBO> cards = new ArrayList<CardDBO>() ;
	
	public NotesElementDBO( AbstractNotesElement ne ) throws Exception {
		
		elementType     = ne.getType() ;
		difficultyLevel = ne.getDifficultyLevel() ;
		content         = ne.getContent() ;
		objCorrelId     = ne.getObjId() ;
		ready           = ne.isReady() ;
		hiddenFromView  = ne.isHiddenFromView() ;
		
		for( AbstractCard card : ne.getCards() ) {
			cards.add( new CardDBO( card ) ) ;
		}
	}
	
	private NotesElementDBO( ChapterDBO chapter, ResultSet rs ) throws Exception {
		
		this.chapter = chapter ;
		
		notesElementId  = rs.getInt    ( "notes_element_id" ) ;
		chapterId       = rs.getInt    ( "chapter_id"       ) ;
		elementType     = rs.getString ( "element_type"     ) ;
		difficultyLevel = rs.getInt    ( "difficulty_level" ) ;
		content         = rs.getString ( "content"          ) ;
		objCorrelId     = rs.getString ( "obj_correl_id"    ) ;
		ready           = rs.getBoolean( "ready"            ) ;
		hiddenFromView  = rs.getBoolean( "hidden_from_view" ) ;
		
		log.debug( "\t  Loaded notes element " + notesElementId + " from DB" ) ;
	}
	
	public int getNotesElementId() {
		return notesElementId;
	}

	public void setNotesElementId( int notesElementId ) {
		this.notesElementId = notesElementId ;
		for( CardDBO card : cards ) {
			card.setNotesElementId( notesElementId ) ;
		}
	}

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
		for( CardDBO card : cards ) {
			card.setChapterId( chapterId ) ;
		}
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
	
	public boolean isSourceTrace() {
		return sourceTrace;
	}

	public boolean isModified() {
		return isModified;
	}
	
	public boolean isReady() {
		return ready ;
	}
	
	public boolean isHiddenFromView() {
		return this.hiddenFromView ;
	}
	
	public List<CardDBO> getCards() {
		return this.cards ;
	}

	public boolean isDeleted() {
		return isDeleted ;
	}
	
	private void setDeleted( boolean deleted ) {
		isDeleted = true ;
		for( CardDBO card : cards ) {
			card.setDeleted( true ) ;
		}
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
			"`notes_element`.`obj_correl_id`, " +
			"`notes_element`.`ready`, " +
			"`notes_element`.`hidden_from_view` " +
			"FROM " +
			"`jove_notes`.`notes_element` " +
			"WHERE " + 
			"`notes_element`.`chapter_id` = ? " +
			"ORDER BY " + 
			"`notes_element`.`notes_element_id` ASC ";

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "NotesElementDBO::getAll", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt( 1, chapter.getChapterId() ) ;
			ResultSet rs = psmt.executeQuery() ;
			
			while( rs.next() ) {
				elements.add( new NotesElementDBO( chapter, rs ) ) ;
			}
			
			loadAndAssociateCardsWithNotesElements( chapter, elements ) ;
		}
		finally {
			JoveNotes.db.returnConnection( conn ) ;
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

		log.debug( "\t  Creating notes element - " + 
		           getElementType() + "::" + getObjCorrelId() ) ;
		
		final String sql = 
		"INSERT INTO `jove_notes`.`notes_element` " +
		"(`chapter_id`, `element_type`, `difficulty_level`, " + 
		"`content`, `obj_correl_id`, `ready`, `hidden_from_view` ) " +
		"VALUES " +
		"( ?, ?, ?, ?, ?, ?, ? )" ;

		int generatedId = -1 ;
		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "NotesElementDBO::create", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql, 
					                         Statement.RETURN_GENERATED_KEYS ) ;
			
			psmt.setInt    ( 1, getChapterId() ) ;
			psmt.setString ( 2, getElementType() ) ;
			psmt.setInt    ( 3, getDifficultyLevel() ) ;
			psmt.setString ( 4, getContent() ) ;
			psmt.setString ( 5, getObjCorrelId() ) ;
			psmt.setBoolean( 6, isReady() ) ;
			psmt.setBoolean( 7, isHiddenFromView() ) ;
			
			psmt.executeUpdate() ;
			ResultSet rs = psmt.getGeneratedKeys() ;
			if( null != rs && rs.next()) {
			     generatedId = (int)rs.getLong( 1 ) ;
			}
			else {
				throw new Exception( "Autogenerated key not obtained for notes element." ) ;
			}
			setNotesElementId( generatedId ) ;
			for( CardDBO card : this.cards ) {
				card.create() ;
			}
		}
		finally {
			JoveNotes.db.returnConnection( conn ) ;
		}
		return generatedId ;
	}
	
	public void update() throws Exception {
		
		final String sql = 
			"UPDATE `jove_notes`.`notes_element` " +
			"SET " +
			"`difficulty_level` = ?, " +
			"`content` = ?, " +
			"`hidden_from_view` = ? " +
			"WHERE `notes_element_id` = ? " ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "NotesElementDBO::update", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt    ( 1, getDifficultyLevel() ) ;
			psmt.setString ( 2, getContent() ) ;
			psmt.setBoolean( 3, isHiddenFromView() ) ;
			psmt.setInt    ( 4, getNotesElementId() ) ;
			
			psmt.executeUpdate() ;
		}
		finally {
			JoveNotes.db.returnConnection( conn ) ;
		}
	}

	public void delete() throws Exception {
		
		final String sql = 
			"DELETE FROM `jove_notes`.`notes_element` WHERE `notes_element_id` = ?" ;

		Connection conn = JoveNotes.db.getConnection() ;
		try {
			logQuery( "NotesElementDBO::delete", sql ) ;
			PreparedStatement psmt = conn.prepareStatement( sql ) ;
			psmt.setInt ( 1, getNotesElementId() ) ;
			
			psmt.executeUpdate() ;
			setDeleted( true ) ;
		}
		finally {
			JoveNotes.db.returnConnection( conn ) ;
		}
	}
	
	public boolean trace( AbstractNotesElement ne ) throws Exception {
		
		if( !getObjCorrelId().equals( ne.getObjId() ) ) {
			throw new Exception( "Correlation id for NEDBO and NE don't match." ) ;
		}
		
		log.debug( "\t    Existing notes element found. id=" + getNotesElementId() ) ;
		log.debug( "\t      Tracing cards..." ) ;
		
		boolean updateRequired = false ;
		this.sourceTrace = true ;
		Map<String, CardDBO> dboMap = new HashMap<String, CardDBO>() ;
		for( CardDBO dbo : cards ) {
			dboMap.put( dbo.getObjCorrelId(), dbo ) ;
		}
		
		for( AbstractCard card : ne.getCards() ) {
			CardDBO cardDbo = dboMap.get( card.getObjId() ) ;
			if( cardDbo == null ) {
				cardDbo = new CardDBO( card ) ;
				cardDbo.setChapterId( getChapterId() ) ;
				cardDbo.setNotesElementId( getNotesElementId() ) ;
				cards.add( cardDbo ) ;
				if( !updateRequired ) updateRequired = true ;
				log.debug( "\t      New card found..." ) ;
			}
			else {
				boolean bool = cardDbo.trace( card ) ;
				if( !updateRequired && bool ) updateRequired = true ;
			}
		}

		// Only if the notes is ready - implying that it's content will not be 
		// modified beyond this point, do we check for modification. This is 
		// a special case and applies for special elements such as spell bee.
		// In case of spell bee, the pronunciation is downloaded offline and 
		// hence at this point, the card is not ready - consequently we don't
		// have to check for modification.
		if( ne.isReady() ) {
			
			boolean contentEquals    = getContent().equals( ne.getContent() ) ;
			boolean difficultyEquals = getDifficultyLevel() == ne.getDifficultyLevel() ;
			boolean hiddenEquals     = isHiddenFromView()   == ne.isHiddenFromView() ;
			
			if( !( contentEquals && difficultyEquals && hiddenEquals ) ) {
				log.debug( "\t      Notes element found modfied.. id=" + getNotesElementId() ) ;
				this.isModified = true ;
				this.content = ne.getContent() ;
				this.hiddenFromView = ne.isHiddenFromView() ;
				this.difficultyLevel = ne.getDifficultyLevel() ;
				if( !updateRequired ) updateRequired = true ;
			}
		}
		
		return updateRequired ;
	}
	
	/**
	 * This function processes the modifications done to the object tree by 
	 * the trace function.
	 */
	public void processTrace() throws Exception {

		log.debug( "\t  Processing trace for NEDBO id = " + getNotesElementId() ) ;
		if( getNotesElementId() == -1 ) {
			log.debug( "\t    Notes element will be created." ) ;
			create() ;
			return ;
		}
		else if( isModified ) {
			log.debug( "\t    Notes element will be updated. id=" + getNotesElementId() ) ;
			update() ;
		}
		else if( !sourceTrace ) {
			log.debug( "\t    Notes element will be deleted. id=" + getNotesElementId() ) ;
			delete() ;
			return ;
			// The associated cards will be cascade deleted at the database.
			// No need to delete them explicitly.
		}
		
		for( CardDBO dbo : cards ) {
			dbo.processTrace() ;
		}
	}
}
