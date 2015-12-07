package com.sandy.jovenotes.processor.util;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

/**
 * A simple data access wrapper. This application's data access requirements are
 * quite small and straight forward and hence doesn't warrant the overhead
 * of ORM frameworks.
 *  
 * @author Sandeep
 */
public class Database {

      private static final Logger log = Logger.getLogger( Database.class ) ;
      
      private String url      = null ;
      private String user     = null ;
      private String password = null ;
      
      private GenericObjectPool<Connection> connectionPool = null ;
      
      public Database( String driver, String url, String user, String password ) 
            throws Exception {
            
            Class.forName( driver ) ;
            
            this.url      = url ;
            this.user     = user ;
            this.password = password ;
      }
      
      public void initialize() throws Exception {
          
       connectionPool = new GenericObjectPool<Connection>( 
                              new BasePoolableObjectFactory<Connection>() {
            
            public Connection makeObject() throws Exception {
                log.debug( "\tCreating a new database connection." ) ;
                return DriverManager.getConnection( 
                                        Database.this.url, Database.this.user, 
                                        Database.this.password ) ;
            }

            public void destroyObject( Connection conn ) throws Exception {
                log.debug( "Closing a database connection." ) ;
                conn.close() ;
            }
        }) ;
        connectionPool.setMaxActive( 5 );
      }
      
      public Connection getConnection() throws Exception {
          if ( connectionPool == null ) {
              throw new Exception( "Database not initialized" ) ;
          }
            return connectionPool.borrowObject() ;
      }
      
      public void returnConnection( Connection conn ) {
            try {
                  connectionPool.returnObject( conn ) ;
            } 
            catch (Exception e) {
                  log.error( "Could not return connection.", e ) ;
            }
      }
}
