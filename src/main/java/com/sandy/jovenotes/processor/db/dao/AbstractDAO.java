package com.sandy.jovenotes.processor.db.dao;

import org.apache.log4j.Logger;

public abstract class AbstractDAO {

    private static final Logger log = Logger.getLogger( AbstractDAO.class ) ;
    private static boolean logQuery = false ;
    
    protected static void logQuery( String marker, String query ) {
        if( logQuery ) {
            log.debug( marker + " :: " + query ) ;
        }
    }
}
