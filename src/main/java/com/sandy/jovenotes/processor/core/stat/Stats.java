package com.sandy.jovenotes.processor.core.stat;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.dao.NotesElementDBO ;

public class Stats {

    private static final Logger log = Logger.getLogger( NotesElementDBO.class ) ;
    
    public static enum ProcessType { NEW, DELETE, UPDATE } ;
    private static Stats instance = new Stats() ;
    
    private static int LOG_LINE_SIZE = 80 ;
    private static int INDENT_L0     = 0 ;
    private static int INDENT_L1     = 4 ;
    
    private static int LG_KEY        = 40 ;
    private static int INT_PAD       = 5 ;
    
    private List<ChapterStat> chapterStats = new ArrayList<ChapterStat>() ;
    private Map<String, CardStat> cardStats = new HashMap<String, CardStat>() ;
    private ChapterStat currentChapter = null ;
    private int currentIndent = 0 ;
    
    public static Stats getInstance() {
        return instance ;
    }

    public static void newChapterBeingProcessed( Chapter chapter ) {
        instance.chapterBeingProcessed( chapter, true ) ;
    }

    public static void updatedChapterBeingProcessed( Chapter chapter ) {
        instance.chapterBeingProcessed( chapter, false ) ;
    }
    
    public static void cardCreated( String cardType ) {
        instance.cardProcessed( cardType, ProcessType.NEW ) ;
    }
    
    public static void cardUpdated( String cardType ) {
        instance.cardProcessed( cardType, ProcessType.UPDATE ) ;
    }
    
    public static void cardDeleted( String cardType ) {
        instance.cardProcessed( cardType, ProcessType.DELETE ) ;
    }
    
    public static void printStats() {
        instance.logStatistics() ;
    }
    
    private void chapterBeingProcessed( Chapter chapter, boolean isNew ) {
        currentChapter = new ChapterStat( chapter.getChapterFQN(), isNew ) ;
        chapterStats.add( currentChapter ) ;
    }

    private void cardProcessed( String cardType, ProcessType processType ) {
        currentChapter.cardProcessed( cardType, processType ) ;
        
        CardStat cardStat = cardStats.get( cardType ) ;
        if( cardStat == null ) {
            cardStat = new CardStat( cardType ) ;
            cardStats.put( cardType, cardStat ) ;
        }
        
        switch( processType ) {
            case NEW:
                cardStat.incrementNumCreated() ;
                break ;
            case DELETE:
                cardStat.incrementNumDeleted() ;
                break ;
            case UPDATE:
                cardStat.incrementNumModified() ;
                break ;
        }
    }
    
    private int getNumNewCardsProcessed() {
        int numCards = 0 ;
        for( ChapterStat chStat : chapterStats ) {
            numCards += chStat.getNumNewCardsProcessed() ;
        }
        return numCards ;
    }
    
    private int getNumModifiedCardsProcessed() {
        int numCards = 0 ;
        for( ChapterStat chStat : chapterStats ) {
            numCards += chStat.getNumModifiedCardsProcessed() ;
        }
        return numCards ;
    }
    
    private int getNumDeletedCardsProcessed() {
        int numCards = 0 ;
        for( ChapterStat chStat : chapterStats ) {
            numCards += chStat.getNumDeletedCardsProcessed() ;
        }
        return numCards ;
    }
    
    private void logStatistics() {
        
        log( StringUtils.repeat( "=", LOG_LINE_SIZE ) ) ;
        log( StringUtils.center( "JoveNotes Processing Statistics", LOG_LINE_SIZE ) ) ;
        log( StringUtils.repeat( "=", LOG_LINE_SIZE ) ) ;
        
        log( LG_KEY, "Number of files processed",          chapterStats.size() ) ;

        if( cardStats.size() > 0 ) {
            logFileByFileStats() ;
            log( StringUtils.repeat( "=", LOG_LINE_SIZE ) ) ;
            log( "" ) ;
            log( LG_KEY, "Number of files processed",          chapterStats.size() ) ;
            log( LG_KEY, "Number of new cards processed",      getNumNewCardsProcessed() ) ;
            log( LG_KEY, "Number of modified cards processed", getNumModifiedCardsProcessed() ) ;
            log( LG_KEY, "Number of deleted cards processed",  getNumDeletedCardsProcessed() ) ;
            log( "" ) ;
            logCardStats( cardStats ) ;
        }
    }
    
    private void logCardStats( Map<String, CardStat> stats ) {
        
        int numNew = 0 ;
        int numDel = 0 ;
        int numUpd = 0 ;
        
        StringBuffer header = new StringBuffer() ;
        header.append( StringUtils.rightPad( "Card type", 25 ) )
              .append( " | " )
              .append( StringUtils.center( "New", 5 ) )
              .append( " | " )
              .append( StringUtils.center( "Mod", 5 ) )
              .append( " | " )
              .append( StringUtils.center( "Del", 5 ) )
              .append( " | " ) ;
        
        log( header.toString() ) ;
        log( StringUtils.repeat( ".", header.length() ) ) ;
        
        for( CardStat stat : stats.values() ) {
            
            numNew += stat.getNumCreated() ;
            numUpd += stat.getNumModified() ;
            numDel += stat.getNumDeleted() ;
            
            StringBuffer row = new StringBuffer() ;
            row.append( StringUtils.rightPad( stat.getCardType(), 25 ) )
                  .append( " | " )
                  .append( StringUtils.leftPad( "" + stat.getNumCreated(), 5 ) )
                  .append( " | " )
                  .append( StringUtils.leftPad( "" + stat.getNumModified(), 5 ) )
                  .append( " | " )
                  .append( StringUtils.leftPad( "" + stat.getNumDeleted(), 5 ) )
                  .append( " | " ) ;
            log( row.toString() ) ;
        }
        log( StringUtils.repeat( ".", header.length() ) ) ;

        StringBuffer footer = new StringBuffer() ;
        footer.append( StringUtils.rightPad( "Total", 25 ) )
              .append( " | " )
              .append( StringUtils.leftPad( "" + numNew, 5 ) )
              .append( " | " )
              .append( StringUtils.leftPad( "" + numUpd, 5 ) )
              .append( " | " )
              .append( StringUtils.leftPad( "" + numDel, 5 ) )
              .append( " | " ) ;
        
        log( footer.toString() ) ;
        log( StringUtils.repeat( ".", footer.length() ) ) ;
    }
    
    private void logFileByFileStats() {
        for( ChapterStat stat : chapterStats ) {
            printChapterStats( stat ) ;
        }
    }
    
    private void printChapterStats( ChapterStat stat ) {
        log( "" ) ;
        log( StringUtils.repeat( "-", LOG_LINE_SIZE ) ) ;
        log( stat.getChapterFQN() ) ;
        log( "" ) ;
        setIndent( INDENT_L1 ) ;
        logCardStats( stat.getCardStats() ) ;
        setIndent( INDENT_L0 ) ;
    }
    
    private void setIndent( int indent ) {
        currentIndent = indent ;
    }
    
    private void log( String text ) {
        log.info( StringUtils.repeat( " ", currentIndent ) + text ) ;
    }
    
    private void log( int keySz, String key, int value ) {
        log( keySz, key, StringUtils.leftPad( "" + value, INT_PAD ) ) ;
    }
    
    private void log( int keySz, String key, String value ) {
        log( StringUtils.rightPad( key, keySz ) + " = " + value ) ;
    }
}
