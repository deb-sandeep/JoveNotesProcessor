package com.sandy.jovenotes.processor.util;

import java.io.File;

import org.apache.log4j.Logger;
import org.hsqldb.server.Server;

import com.sandy.jovenotes.processor.dao.LocalDBSchemaCreator;

/**
 * An extension to the data access wrapper to use a local database 
 * instead of a remote database. The utility will also have capability to 
 * manage the start, stop and other operations of the local database.
 * 
 * @author Vivek Kant
 */
public class LocalDatabase extends Database {
    
    private static final Logger log = Logger.getLogger( LocalDatabase.class ) ;
    
    private Server  db          = new Server() ;
    private File    dbDir       = null ;
    private String  dbName      = null ;
    private int     port        = 544 ;
    
    private Database dbClient   = null ;
    
    public File     getDbDir()      { return dbDir ; }
    public String   getDbName()     { return dbName ; }
    public Database getDbClient()   { return dbClient ; }
    
    public LocalDatabase( File dbDir, String dbName, int port )
            throws Exception {
        
        super( "org.hsqldb.jdbcDriver", getURL( port, dbName ), "SA", "" ) ;

        this.dbName = dbName ;
        this.port   = port ;        
        this.dbDir = dbDir ;
    }

    public void initialize() throws Exception {
        
        if ( !dbDir.exists() ) {
            log.info( "Creating new local database at " + dbDir.getAbsolutePath() );
            dbDir.mkdirs() ;
        }
        
        db.setAddress( "localhost" ) ;
        db.setDatabaseName( 0, dbName ) ;
        db.setDatabasePath( 0, "file:"  + dbDir.getAbsolutePath() 
                                        + File.separator + dbName );
        db.setPort( port ) ;
        
        db.start() ;
        
        super.initialize() ;
        
        LocalDBSchemaCreator schemaCreator = new LocalDBSchemaCreator() ;
        schemaCreator.checkAndCreate() ;
    }
    
    public void shutdown() throws Exception  {
        db.shutdown() ;
    }
    
    private static String getURL( int port, String dbName ) {
        String url = "jdbc:hsqldb:hsql://localhost:" + port + "/" + dbName ;
        return url ;
    }
}

