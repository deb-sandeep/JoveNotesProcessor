package com.sandy.jovenotes.processor.junit.poc;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup ;

public class JSoupPOC {

    private static final Logger log = Logger.getLogger( JSoupPOC.class ) ;
    
    public static void main( String[] args ) {
        
        String line = "<p align=\"left\"><b><i>Hello</i><b></p>";
        log.info( Jsoup.parse( line ).text() ) ;
    }
}
