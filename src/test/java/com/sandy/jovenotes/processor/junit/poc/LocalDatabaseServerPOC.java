package com.sandy.jovenotes.processor.junit.poc;

import java.io.File;

import com.sandy.jovenotes.processor.util.LocalDatabaseServer;

public class LocalDatabaseServerPOC {
    
    private static LocalDatabaseServer server = null ;
    private static String path = System.getProperty( "java.io.tmpdir" ) + 
                                 File.separator + "jove_local_db" ;
    

    public static void main( String[] args ) throws Exception {
        
        server = new LocalDatabaseServer( path, "jove_local_db", 544 ) ;
        server.startup() ;
    }

}
