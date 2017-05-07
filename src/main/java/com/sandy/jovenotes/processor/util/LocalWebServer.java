package com.sandy.jovenotes.processor.util;

import java.io.File;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

public class LocalWebServer  {
    
    private String  host  = "localhost" ;
    private int     port  = 80 ;
    private File    root  = null ;
    private boolean quiet = true ;
    
    private SimpleWebServer server = null ;
    
    public void setHost  ( String  host  ) { this.host  = host  ; }
    public void setPort  ( int     port  ) { this.port  = port  ; }
    public void setQuiet ( boolean quiet ) { this.quiet = quiet ; }

    public LocalWebServer( String host, int port, File root, boolean quiet ) {
        
        this.host  = host ;
        this.port  = port ;
        this.root  = root ;
        this.quiet = quiet ;
    }
    
    public LocalWebServer( File root ) {
        
        this.root = root ;
        if( !this.root.exists() ) {
            this.root.mkdirs() ;
        }
    }
    
    public void initialize() throws Exception {
        server = new SimpleWebServer( host, port, root, quiet, null ) ;
        server.start( NanoHTTPD.SOCKET_READ_TIMEOUT, false ) ;
    }
    
    public void shutdown() throws Exception {
        server.stop() ;
    }
}
