package com.sandy.jovenotes.processor.junit.poc;

import java.io.File;

import com.sandy.jovenotes.processor.util.LocalWebServer;

public class LocalWebServerPoC {

    public static void main(String[] args) throws Exception {
        
        LocalWebServer server = new LocalWebServer( new File( "d:/temp/ws" ) ) ;
        server.setPort( 8080 ) ;
        server.initialize() ;

    }

}
