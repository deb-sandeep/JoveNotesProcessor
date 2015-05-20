package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * The configuration manager for JoveNotes processor. All the configuration 
 * entities are accessible by getter methods.
 * 
 * @author Sandeep
 */
public class ConfigManager{

	private static Logger log = Logger.getLogger(ConfigManager.class);
	
	private static String CK_SRC_DIR       = "source.dir" ;
	private static String CK_WKSP_DIR      = "workspace.dir" ;
	private static String CK_DEST_ROOT_DIR = "destination.media.root.dir" ;
	private static String CK_DB_DRIVER     = "db.driver" ;
	private static String CK_DB_URL        = "db.url" ;
	private static String CK_DB_USER       = "db.user" ;
	private static String CK_DB_PWD        = "db.password" ;
	private static String CK_GRAPHVIZ_PATH = "graphviz.dot.path" ;
	
	private boolean showUsage            = false ;
	private boolean showUI               = false ;
	private boolean forceProcessAllFiles = false ;
	private File    srcDir               = null ;
	private File    wkspDir              = null ;
	private File    destMediaRootDir     = null ;
	private File    graphvizDotPath      = null ;
	
	private String  databaseURL        = null ;
	private String  databaseDriverName = null ;
	private String  databaseUser       = null ;
	private String  databasePassword   = null ;

	public boolean isShowUsage()           { return this.showUsage; }
	public boolean isShowUI()              { return this.showUI; }
	public boolean isForceProcessAllFiles(){ return this.forceProcessAllFiles; }
	public File    getSrcDir()             { return this.srcDir; }
	public File    getWorkspaceDir()       { return this.wkspDir ; }
	public File    getDestMediaRootDir()   { return this.destMediaRootDir ; }
	public File    getGraphvizDotPath()    { return this.graphvizDotPath; }
	
	public String getDatabaseURL()        { return this.databaseURL; }
	public String getDatabaseDriverName() { return this.databaseDriverName; }
	public String getDatabaseUser()       { return this.databaseUser; }
	public String getDatabasePassword()   { return this.databasePassword; }
	
	// ------------------------------------------------------------------------
	private Options clOptions = null ;
	private boolean logCLP    = false ;
	
	public ConfigManager( String[] args ) throws Exception {
		
		this.clOptions = prepareOptions() ;
		parseCLP( args ) ;
		if( this.showUsage )return ;
		
		PropertiesConfiguration propCfg = new PropertiesConfiguration() ;
		URL cfgURL = ConfigManager.class.getResource( "/config.properties" ) ;
		if( cfgURL == null ) {
			throw new Exception( "config.properties not found in classpath." ) ;
		}
		propCfg.load( cfgURL );
		parseConfig( propCfg ) ;
	}
	
	private void parseConfig( PropertiesConfiguration config ) 
		throws Exception {
		
		parseSrcDir( config ) ;
		parseDatabaseConfig( config ) ;
	}
	
	private void parseSrcDir( PropertiesConfiguration config ) 
		throws Exception {
		
		srcDir  = getMandatoryDirFromConfig( CK_SRC_DIR, config ) ;
		wkspDir = getMandatoryDirFromConfig( CK_WKSP_DIR, config ) ;
		destMediaRootDir = getMandatoryDirFromConfig( CK_DEST_ROOT_DIR, config) ;
		graphvizDotPath  = getMandatoryDirFromConfig( CK_GRAPHVIZ_PATH, config ) ;
	}
	
	private void parseDatabaseConfig( PropertiesConfiguration config ) 
		throws Exception {
		
		this.databaseDriverName = getMandatoryConfig( CK_DB_DRIVER, config ) ;
		this.databaseURL        = getMandatoryConfig( CK_DB_URL, config ) ;
		
		if( StringUtil.isEmptyOrNull( this.databaseUser ) ) {
			this.databaseUser = config.getString( CK_DB_USER ) ;
		}
		
		if( StringUtil.isEmptyOrNull( this.databasePassword ) ) {
			this.databasePassword = config.getString( CK_DB_PWD ) ;
		}
	}
	
	private File getMandatoryDirFromConfig( String key, 
											PropertiesConfiguration config ) 
		throws Exception {
		
		String path = getMandatoryConfig( key, config ) ;
		File file = new File( path ) ;
		if( !file.exists() ) {
			throw new Exception( "Folder referred to by " + key + 
					" configuration does not exist." ) ;
		}
		return file ;
	}
	
	private String getMandatoryConfig( String key, PropertiesConfiguration config ) 
		throws Exception {
		
		String value = config.getString( key ) ;
		if( StringUtil.isEmptyOrNull( value ) ) {
			throw new Exception( key + " configuration is missing." ) ;
		}
		return value ;
	}
	
    public void printUsage() {
    	
    	String usageStr = "JoveNotes [huf] [--dbUser <database user>] " + 
    	                  "[--dbPassword <database password>]" ;
    	
        HelpFormatter helpFormatter = new HelpFormatter() ;
        helpFormatter.printHelp( 80, usageStr, null, this.clOptions, null ) ;
    }

    private Options prepareOptions() {

        final Options options = new Options() ;
        options.addOption( "h", "Print this usage and exit." ) ;
        options.addOption( "i", "Show graphical user interface." ) ;
        options.addOption( "f", "Force process all files." ) ;
        options.addOption( null, "dbUser", true, "The database user name" ) ;
        options.addOption( null, "dbPassword", true, "The database password" ) ;

        return options ;
    }
    
    private void parseCLP( String[] args ) throws Exception {

        if( this.logCLP ) {
            StringBuilder str = new StringBuilder() ;
            for( String arg : args ) {
                str.append( arg ).append( " " ) ;
            }
            log.debug( "Parsing CL args = " + str ) ;
        }
        
        try {
            CommandLine cmdLine = new DefaultParser().parse( this.clOptions, args ) ;
            
            if( cmdLine.hasOption( 'h' ) ) { this.showUsage = true ; }
            if( cmdLine.hasOption( 'i' ) ) { this.showUI = true ; }
            if( cmdLine.hasOption( 'f' ) ) { this.forceProcessAllFiles = true ; }
            
        	this.databaseUser     = cmdLine.getOptionValue( "dbUser" ) ;
        	this.databasePassword = cmdLine.getOptionValue( "dbPassword" ) ;
        }
        catch ( Exception e ) {
            log.error( "Error parsing command line arguments.", e ) ;
            printUsage() ;
            throw e ;
        }
    }
}
