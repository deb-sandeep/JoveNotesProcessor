package com.sandy.jovenotes.processor.async;

import java.io.File;
import java.io.Serializable;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.util.NetworkResourceDownloader;
import com.sandy.jovenotes.processor.util.StringUtil;
import com.sandy.jovenotes.processor.util.WordnicAdapter;

/**
 * This command downloads the sound clip, definitions and pronunciation of the 
 * word provided.
 * 
 * After successful download, it updates the notes element and card content
 * in the database and marks them as ready for pickup by the study module.
 */
public class SpellbeeCmd extends PersistedCmd implements Serializable {
    
    private static final long serialVersionUID = -7123291882777033563L ;

    private transient static Logger log = Logger.getLogger( SpellbeeCmd.class ) ;
    
    private static String GOOGLE_CLIP_URL_TEMPLATE = 
          "https://ssl.gstatic.com/dictionary/static/sounds/de/0/{{word}}.mp3" ;
    private static String DICTCOM_CLIP_URL_TEMPLATE = 
          "http://dictionary.reference.com/browse/{{word}}?s=t" ;
    
    private String word = null ;
    private int difficultyLevel = 0 ;
    private String neObjId = null ;
    private String cardObjId = null ;
    
    private transient int chapterId = 0 ;
    private transient int existingDifficulty = 0 ;
    private transient String existingContent = null ;
    
    public SpellbeeCmd( Chapter chapter, String word, int difficultyLevel, 
                        String neObjId, String cardObjId ) {
        
        super( chapter ) ;
        
        this.word = word ;
        this.difficultyLevel = difficultyLevel ;
        this.neObjId = neObjId ;
        this.cardObjId = cardObjId ;
    }

    public void execute() throws Exception {
        log.info( "\tExecuting SpellbeeCmd for word - " + word ) ;
        
        File mediaDir = new File( JoveNotes.config.getDestMediaRootDir(), "_spellbee" ) ;
        File clipFile = new File( mediaDir, word.toLowerCase() + ".mp3" ) ;
        File descFile = new File( mediaDir, word.toLowerCase() + ".descr" ) ;
        File pronunciationFile = new File( mediaDir, word.toLowerCase() + ".pronunciation" ) ;
        
        try {
            downloadSoundClip( clipFile ) ;
            downloadDescription( descFile ) ;
            
            loadExistingData() ;
            updateContentAndDifficultyLevel( pronunciationFile ) ;
        } 
        catch( Exception e ){
            log.error( "Could not process spellbee command.", e ) ;
            throw e ;
        }
    }
    
    private void downloadSoundClip( File clipFile ) throws Exception {

        if( !clipFile.exists() ) {
            log.info( "\t\tDownloading sound clip." ) ;
            if( !downloadClipFromGoogle( clipFile ) ) {
                if( !downloadClipFromDictionaryDotCom( clipFile ) ) {
                    throw new Exception( "Could not download sound clip." ) ;
                }
            }
        }
    }
    
    private boolean downloadClipFromGoogle( File outputFile ) 
            throws Exception {
            
        log.debug( "\t\t\tDownloading clip from google.com" ) ;
        
        boolean result = false ;
        NetworkResourceDownloader downloader = null ;
        
        String url = GOOGLE_CLIP_URL_TEMPLATE.replace( 
                        "{{word}}", 
                        URLEncoder.encode( word.toLowerCase(), "UTF-8" ) ) ;
        
        downloader = new NetworkResourceDownloader( url ) ;
        if( downloader.execute() == 200 ) {
            downloader.saveResponseToFile( outputFile ) ;
            result = true ;
        }
        else {
            String msg = "Could not download sound clip from Google. msg=" + 
                         downloader.getStatusCode() + downloader.getReasonPhrase() ;
            log.info( "\t\t\t" + msg ) ;
        }
        return result ;
    }
        
    private boolean downloadClipFromDictionaryDotCom( File outputFile )
        throws Exception {
        
        log.debug( "\t\t\tDownloading clip from dictionary.com" ) ;
        
        boolean result = false ;
        NetworkResourceDownloader downloader = null ;
            
        String url = DICTCOM_CLIP_URL_TEMPLATE.replace( 
                        "{{word}}", 
                        URLEncoder.encode( word, "UTF-8" ) ) ;
        
        downloader = new NetworkResourceDownloader( url ) ;
        if( downloader.execute() == 200 ) {
            
            String content = downloader.getResponseAsString() ;
            int endIndex = content.indexOf( ".mp3" ) ;
            if( endIndex != -1 ) {
                int startIndex = content.lastIndexOf( '"', endIndex ) ;
                String soundURL = content.substring( startIndex+1, endIndex+4 ) ;
                
                downloader = new NetworkResourceDownloader( soundURL ) ;
                downloader.execute() ;
                downloader.saveResponseToFile( outputFile ) ;
                result = true ;
            }
        }
        else {
            String msg = "Could not download sound clip from Dictionary.com. msg=" + 
                         downloader.getStatusCode() + downloader.getReasonPhrase() ;
            log.info( "\t\t\t" + msg ) ;
        }
        return result ;
    }
    
    private void downloadDescription( File descFile ) throws Exception {
        
        if( !descFile.exists() ) {
            log.info( "\t\tDownloading word meaning." ) ;
            List<String> defs = new WordnicAdapter().getDefinitions( word ) ;
            if( defs.isEmpty() ) {
                throw new Exception( "Word meaning could not be downloaded." ) ;
            }
            else {
                StringBuilder buffer = new StringBuilder( "<ul>" ) ;
                for( String def : defs ) {
                    buffer.append( "<li>" + def + "</li>" ) ;
                }
                buffer.append( "</ul>" ) ;
                FileUtils.writeStringToFile( descFile, buffer.toString() );
            }
        }
    }
    
    private void loadExistingData() throws Exception {
        
        chapterId = getChapterId() ;
        final String sql = 
                "SELECT content, difficulty_level " +
                "FROM jove_notes.card " +
                "WHERE " +
                " chapter_id = ? and obj_correl_id = ?" ;
        
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::getAll", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setInt ( 1, chapterId ) ;
            psmt.setString( 2, cardObjId ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            if( rs.next() ) {
                existingContent = rs.getString(1).trim() ;
                existingDifficulty = rs.getInt(2) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
    }
    
    private void updateContentAndDifficultyLevel( File pronunciationFile ) 
            throws Exception {
        
        String json = existingContent ;
        int    diff = existingDifficulty ;
        boolean updateReq    = false ;
        
        if( existingContent.equals( "{}" ) ) {
            String pronunciation = null ;
            
            if( !pronunciationFile.exists() ) {
                log.info( "\t\tDownloading pronunciation." ) ;
                pronunciation = new WordnicAdapter().getPronounciation( word ) ;
                pronunciation = ( pronunciation==null ) ? "" : pronunciation ;
                FileUtils.writeStringToFile( pronunciationFile, pronunciation, 
                                             "UTF-8" ) ;
            }
            else {
                log.info( "\t\tLoading pronunciation from cache." ) ;
                pronunciation = FileUtils.readFileToString( pronunciationFile, 
                                                            "UTF-8" ) ;
            }
            
            Map<String, String> jsonAttrs = new HashMap<String, String>() ;
            jsonAttrs.put( "word", word.toLowerCase() ) ;
            jsonAttrs.put( "pronunciation", pronunciation ) ;
            json = JSONValue.toJSONString( jsonAttrs ) ;
            updateReq = true ;
        }
        
        if( difficultyLevel != existingDifficulty ) {
            diff = difficultyLevel ;
            if( !updateReq ) updateReq = true ;
        }
        
        if( updateReq ) {
            updateTable( chapterId, "notes_element", neObjId,   json, diff ) ;
            updateTable( chapterId, "card",          cardObjId, json, diff ) ;
        }
    }
    
    private void updateTable( int chapterId, String tableName, String uid, 
                              String json, int diffLevel ) 
        throws Exception {
        
        log.debug( "\t\tUpdating table " + tableName ) ;
        
        final String sql = 
                "UPDATE jove_notes." + tableName + " " +
                "SET " +
                " content = ?, " +
                " ready=1 ," +
                " difficulty_level=? " +
                "WHERE " +
                " chapter_id = ? and " +
                " obj_correl_id  = ?" ;
        
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::updateTable", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setString( 1, json ) ;
            psmt.setInt   ( 2, diffLevel ) ;
            psmt.setInt   ( 3, chapterId ) ;
            psmt.setString( 4, uid ) ;
            
            if( psmt.executeUpdate() == 0 ) {
                log.error( "Could not update " + tableName + "." ) ;
                log.error( "\tchapter_id    = " + chapterId ) ;
                log.error( "\tobj_correl_id = " + uid ) ;
                throw new Exception( "Could not update " + tableName ) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
    }
    
    private int getChapterId() throws Exception {
        
        final String sql = 
                "SELECT chapter_id " +
                "FROM jove_notes.chapter " +
                "WHERE " +
                " syllabus_name = ? and " +
                " subject_name  = ? and " +
                " chapter_num   = ? and " +
                " sub_chapter_num=?" ;
        
        int chapterId = -1 ;
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            logQuery( "ChapterDBO::getAll", sql ) ;
            PreparedStatement psmt = conn.prepareStatement( sql ) ;
            psmt.setString( 1, super.syllabusName ) ;
            psmt.setString( 2, super.subjectName ) ;
            psmt.setInt   ( 3, super.chapterNum ) ;
            psmt.setInt   ( 4, super.subChapterNum ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            if( rs.next() ) {
                chapterId = rs.getInt( 1 ) ;
            }
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        }
        return chapterId ;
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
