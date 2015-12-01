package com.sandy.jovenotes.processor.junit.poc;

import java.io.File;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.util.LocalDatabase;

public class LocalDatabaseServerPOC {
    
    private static LocalDatabase server = null ;
    private static String path = System.getProperty( "java.io.tmpdir" ) + 
                                 File.separator + "jove_local_db" ;
    

    public static void main( String[] args ) throws Exception {
        
        server = new LocalDatabase( path, "jove_local_db", 544 ) ;
        JoveNotes.db = server ;
        server.initialize() ;
    }

}
