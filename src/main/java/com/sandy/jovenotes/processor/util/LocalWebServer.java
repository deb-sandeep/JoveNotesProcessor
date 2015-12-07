package com.sandy.jovenotes.processor.util;

import java.io.File;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

public class LocalWebServer  {
    
    private String  host    = "localhost" ;
    private int     port    = 80 ;
    private File    root    = null ;
    private boolean quite   = false ;
    private String  cors    = "*" ;
    
    private SimpleWebServer server = null ;
    
    public void setHost( String host )      { this.host = host ; }
    public void setPort( int port )         { this.port = port ; }
    public void setQuite( boolean quite )   { this.quite = quite ; }
    public void setCors( String cors )      { this.cors = cors ; }

    public LocalWebServer( String host, int port, File root, boolean quite, String cors ) {
        
        this.host = host ;
        this.port = port ;
        this.root = root ;
        this.quite = quite ;
        this.cors = cors ;
        
    }
    
    public LocalWebServer( String rootPath ) throws Exception {
        
        this.root = new File( rootPath ) ;
        if( !this.root.exists() ) {
            this.root.mkdirs() ;
        }
    }
    
    public void initialize() throws Exception {
        server = new SimpleWebServer( host, port, root, quite, cors ) ;
        server.start( NanoHTTPD.SOCKET_READ_TIMEOUT, false ) ;
    }
    
    public void shutdown() throws Exception {
        server.stop() ;
    }

}
