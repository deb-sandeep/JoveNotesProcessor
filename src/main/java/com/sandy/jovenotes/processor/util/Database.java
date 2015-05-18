package com.sandy.jovenotes.processor.util;

import java.sql.Connection;
import java.sql.DriverManager;

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
	
	public Database( String driver, String url, String user, String password ) 
		throws Exception {
		
		Class.forName( driver ) ;
		
		this.url      = url ;
		this.user     = user ;
		this.password = password ;
	}
	
	public Connection getConnection() throws Exception {
		return DriverManager.getConnection( url, user, password ) ;
	}
	
	public void closeConnection( Connection conn ) {
		try {
			conn.close() ;
		}
		catch( Exception e ) {
			log.error( "Error closing database connection.", e ) ;
		}
	}
}
