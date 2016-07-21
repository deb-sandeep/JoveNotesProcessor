package com.sandy.jovenotes.processor.core.stat;

import java.util.HashMap ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.stat.Stats.ProcessType ;

class CardStat {
    
    private String cardType = null ;
    private int numCreated = 0 ;
    private int numDeleted = 0 ;
    private int numModified = 0 ;
    
    CardStat( String cardType ) {
        this.cardType = cardType ;
    }
    
    public String getCardType() {
        return cardType ;
    }
    
    public int getNumCreated() {
        return numCreated ;
    }
    
    public int getNumDeleted() {
        return numDeleted ;
    }
    
    public int getNumModified() {
        return numModified ;
    }

    public void incrementNumCreated() {
        this.numCreated++ ;
    }
    
    public void incrementNumDeleted() {
        this.numDeleted++ ;
    }
    
    public void incrementNumModified() {
        this.numModified++ ;
    }
}

public class ChapterStat {

    private String chapterFQN = null ;
    private boolean isNew   = false ;
    private Map<String, CardStat> cardStats = new HashMap<String, CardStat>() ;

    ChapterStat( String fqn, boolean isNew ) {
        this.chapterFQN = fqn ;
        this.isNew = isNew ;
    }
    
    public String getChapterFQN() {
        return chapterFQN ;
    }
    
    public boolean isNew() {
        return this.isNew ;
    }

    public void cardProcessed( String cardType, ProcessType processType ) {
        
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

    public int getNumNewCardsProcessed() {
        int numCards = 0 ;
        for( CardStat chStat : cardStats.values() ) {
            numCards += chStat.getNumCreated() ;
        }
        return numCards ;
    }
    
    public int getNumModifiedCardsProcessed() {
        int numCards = 0 ;
        for( CardStat chStat : cardStats.values() ) {
            numCards += chStat.getNumModified() ;
        }
        return numCards ;
    }
    
    public int getNumDeletedCardsProcessed() {
        int numCards = 0 ;
        for( CardStat chStat : cardStats.values() ) {
            numCards += chStat.getNumDeleted() ;
        }
        return numCards ;
    }
    
    public Map<String, CardStat> getCardStats() {
        return this.cardStats ;
    }
}
