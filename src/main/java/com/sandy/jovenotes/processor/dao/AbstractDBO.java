package com.sandy.jovenotes.processor.dao;

import org.apache.log4j.Logger;

public abstract class AbstractDBO {

    private static final Logger log = Logger.getLogger( AbstractDBO.class ) ;
    private static boolean logQuery = false ;
    
    protected static void logQuery( String marker, String query ) {
        if( logQuery ) {
            log.debug( marker + " :: " + query ) ;
        }
    }
}
