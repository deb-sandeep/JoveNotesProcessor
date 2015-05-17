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
	
	private Connection conn = null ;
	
	public Database( String driver, String url, String user, String password ) 
		throws Exception {
		
		Class.forName( driver ) ;
		this.conn = DriverManager.getConnection( url, user, password ) ;
	}
}
