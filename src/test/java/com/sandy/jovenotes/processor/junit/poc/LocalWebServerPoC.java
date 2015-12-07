package com.sandy.jovenotes.processor.junit.poc;

import com.sandy.jovenotes.processor.util.LocalWebServer;

public class LocalWebServerPoC {

    public static void main(String[] args) throws Exception {
        
        LocalWebServer server = new LocalWebServer( "d:/temp/ws" ) ;
        server.initialize() ;

    }

}
