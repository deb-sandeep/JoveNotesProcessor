package com.sandy.jovenotes.processor.util;

import java.io.File ;
import java.net.URL ;
import java.nio.file.FileSystems ;
import java.nio.file.PathMatcher ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.DefaultParser ;
import org.apache.commons.cli.HelpFormatter ;
import org.apache.commons.cli.Options ;
import org.apache.commons.configuration.PropertiesConfiguration ;
import org.apache.log4j.Logger ;

/**
 * The configuration manager for JoveNotes processor. All the configuration 
 * entities are accessible by getter methods.
 * 
 * @author Sandeep
 */
/**
 * 
 * @author Sandeep
 *
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
    private static String CK_LOCAL_DB_NAME = "local.db.name" ;
    private static String CK_LOCAL_DB_PORT = "local.db.port" ;
    private static String CK_LOCAL_WS_NAME = "local.webserver.name" ;
    private static String CK_LOCAL_WS_PORT = "local.webserver.port" ;
    private static String CK_WORDNIC_API   = "wordnic.api.key" ;
    private static String CK_INCL_GLOBS    = "include.file.globs" ;
    private static String CK_EXCL_GLOBS    = "exclude.file.globs" ;
    
    private boolean     showUsage            = false ;
    private boolean     showUI               = false ;
    private boolean     forceProcessAllFiles = false ;
    private File        wkspDir              = null ;
    private File        destMediaRootDir     = null ;
    private File        graphvizDotPath      = null ;
    private File        localDatabasePath    = null ;
    private File        localWebserverPath   = null ;
    private List<File>  srcDirs              = new ArrayList<File>() ;
    
    private List<PathMatcher> includePathMatchers = new ArrayList<PathMatcher>() ;
    private List<PathMatcher> excludePathMatchers = new ArrayList<PathMatcher>() ;
    
    private String  databaseURL        = null ;
    private String  databaseDriverName = null ;
    private String  databaseUser       = null ;
    private String  databasePassword   = null ;
    private String  wordnicAPIKey      = null ;
    private String  configPath         = null ;
    private String  localDbName        = null ;
    private String  localWsName        = null ;
    
    private int     localDbPort        = 544 ;
    private int     localWsPort        = 8080 ;

    private RunMode runMode            = null ;

    public boolean     isShowUsage()            { return this.showUsage; }
    public boolean     isShowUI()               { return this.showUI; }
    public boolean     isForceProcessAllFiles() { return this.forceProcessAllFiles; }
    public List<File>  getSrcDirs()             { return this.srcDirs; }
    public File        getWorkspaceDir()        { return this.wkspDir ; }
    public File        getDestMediaRootDir()    { return this.destMediaRootDir ; }
    public File        getGraphvizDotPath()     { return this.graphvizDotPath; }
    public File        getLocalDatabasePath()   { return this.localDatabasePath; }
    public File        getLocalWebserverPath()  { return this.localWebserverPath; }
    
    public List<PathMatcher> getIncludePathMatchers() { return this.includePathMatchers; }
    public List<PathMatcher> getExcludePathMatchers() { return this.excludePathMatchers; }
    
    public String   getDatabaseURL()        { return this.databaseURL; }
    public String   getDatabaseDriverName() { return this.databaseDriverName; }
    public String   getDatabaseUser()       { return this.databaseUser; }
    public String   getDatabasePassword()   { return this.databasePassword; }
    public String   getWordnicAPIKey()      { return this.wordnicAPIKey; }
    public String   getConfigFile()         { return this.configPath; }
    public String   getLocalDbName()        { return this.localDbName; }
    public String   getLocalWsName()        { return this.localDbName; }

    public RunMode  getRunMode()            { return this.runMode; }
    
    public int      getLocalDbPort()        { return this.localDbPort; }
    public int      getLocalWsPort()        { return this.localWsPort; }
    
    // ------------------------------------------------------------------------
    private Options clOptions = null ;
    private boolean logCLP    = false ;
    
    public ConfigManager( String[] args ) throws Exception {
        
        this.clOptions = prepareOptions() ;
        parseCLP( args ) ;
        if( this.showUsage ) return ;
        
        PropertiesConfiguration propCfg = new PropertiesConfiguration() ;
        loadConfig( propCfg ) ;
        parseConfig( propCfg ) ;
    }
    
    private void loadConfig( PropertiesConfiguration propCfg ) 
            throws Exception {
        
        boolean configFileFound = false ;
        
        if( this.configPath != null ) {
            propCfg.load( this.configPath ) ;
            configFileFound = true ;
        }
        
        File usrCfgFile = new File( System.getProperty( "user.home" ), 
                                    File.separator + ".jnp" + 
                                    File.separator + "config.properties" ) ;
        if( usrCfgFile.exists() ) {
            propCfg.load( usrCfgFile ) ;
            configFileFound = true ;
        }

        URL cpCfgURL = ConfigManager.class.getResource( "/config.properties" ) ;
        if( cpCfgURL != null ) {
            propCfg.load( cpCfgURL );
            configFileFound = true ;
        }
        
        if( !configFileFound ) {
            log.warn( "No configuration file found." ) ;
            log.warn( "  Executing assuming cmd line args will suffice." );
        }
    }
    
    private void parseConfig( PropertiesConfiguration config ) 
        throws Exception {
        
        parseSrcDir( config ) ;
        parseDatabaseConfig( config ) ;
        parseGlobsFromConfig( config ) ;
    }
    
    private void parseSrcDir( PropertiesConfiguration config ) 
        throws Exception {
        
        if( this.srcDirs.isEmpty() ) {
            String srcDirList = config.getString( CK_SRC_DIR ) ;
            if( StringUtil.isEmptyOrNull( srcDirList ) ) {
                throw new Exception( "Source directories not specified." ) ;
            }
            parseSourceDirs( srcDirList ) ;
        }
        
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
        
        if( StringUtil.isEmptyOrNull( this.wordnicAPIKey ) ) {
            this.wordnicAPIKey = config.getString( CK_WORDNIC_API ) ;
            if( this.wordnicAPIKey == null ) {
                throw new Exception( "Wordnic API key not configured." ) ;
            }
        }
        
        this.localDbName = config.getString( CK_LOCAL_DB_NAME, "jove_local_db" ) ;
        this.localDatabasePath = new File( this.wkspDir, this.localDbName ) ;
        this.localDbPort = config.getInt( CK_LOCAL_DB_PORT, this.localDbPort ) ;
        
        this.localWsName = config.getString( CK_LOCAL_WS_NAME, "jove_wwwroot" ) ;
        this.localWebserverPath = new File( this.wkspDir, this.localWsName ) ;
        this.localWsPort = config.getInt( CK_LOCAL_WS_PORT, this.localWsPort ) ;
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
    
    /**
     * NOTE:
     * 
     * 1. --runMode defaults to development
     * 2. If the same configuration exists in configuration file, then 
     *    command line parameters override the configuration values.
     * 3. The configuration file passed in the  command line parameters
     *    overrides the default configuration file in the package.
     */
    public void printUsage() {
        
        String usageStr = "JoveNotes [hif] [--dbUser <database user>] " +
                          "[--configPath <path to the config file>]" +
                          "[--dbPassword <database password>] " +
                          "[--wordnicKey <key>] " + 
                          "[--runMode CSV preview, development | production] " + 
                          "[--srcDirs <list of directories>] " + 
                          "[--inclGlobs <include files glob patterns>] " + 
                          "[--exclGlobs <exclude files glob patterns>]" ;
        
        HelpFormatter helpFormatter = new HelpFormatter() ;
        helpFormatter.printHelp( 80, usageStr, null, this.clOptions, null ) ;
    }

    private Options prepareOptions() {

        final Options options = new Options() ;
        options.addOption( "h", "Print this usage and exit." ) ;
        options.addOption( "i", "Show graphical user interface." ) ;
        options.addOption( "f", "Force process all files." ) ;
        options.addOption( null, "configPath", true, "The absolute path of the config file" ) ;
        options.addOption( null, "dbUser",     true, "The database user name" ) ;
        options.addOption( null, "dbPassword", true, "The database password" ) ;
        options.addOption( null, "wordnicKey", true, "Wordnic API key" ) ;
        options.addOption( null, "runMode",    true, "Run mode a csv with possible value: 'preview', 'development' or 'production'" ) ;
        options.addOption( null, "srcDirs",    true, "List of directories separated by path separator" ) ;
        options.addOption( null, "inclGlobs",  true, "List of path separator separated glob patterns to include" ) ;
        options.addOption( null, "exclGlobs",  true, "List of path separator separated glob patterns to exclude" ) ;

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
            this.wordnicAPIKey    = cmdLine.getOptionValue( "wordnicKey" ) ;

            this.runMode          = new RunMode ( cmdLine.getOptionValue( "runMode" ) ) ;

            this.configPath = cmdLine.getOptionValue( "configPath" ) ;
            if( this.configPath != null ) {
                File cfgFile = new File( this.configPath ) ;
                if( !cfgFile.exists() ) {
                    throw new Exception( "Configuration file " + this.configPath + 
                                          " does not exist. Please verify" ) ;
                }
            }

            String sourceDirs = cmdLine.getOptionValue( "srcDirs" ) ;
            if( sourceDirs != null ) {
                parseSourceDirs( sourceDirs ) ;
            }
            
            parseGlobsFromCommandLine( cmdLine ) ;
        }
        catch ( Exception e ) {
            log.error( "Error parsing command line arguments.", e ) ;
            printUsage() ;
            throw e ;
        }
    }
    
    private void parseSourceDirs( String dirsList ) throws Exception {
        
        String[] dirs = dirsList.split( File.pathSeparator ) ;
        for( String path : dirs ) {
            File file = new File( path ) ;
            if( !file.exists() ) {
                throw new Exception( "Folder referred to by " + path + 
                        " configuration does not exist." ) ;
            }
            else {
                if( !this.srcDirs.contains( file ) ) {
                    this.srcDirs.add( file ) ;
                }
            }
        }
    }
    
    private void parseGlobsFromCommandLine( CommandLine cmdLine ) 
            throws Exception {
        
        String inclGlobs = cmdLine.getOptionValue( "inclGlobs" ) ;
        String exclGlobs = cmdLine.getOptionValue( "exclGlobs" ) ;
        parseGlobs( inclGlobs, exclGlobs ) ;
    }

    private void parseGlobsFromConfig( PropertiesConfiguration config )
            throws Exception {
            
        String inclGlobs = config.getString( CK_INCL_GLOBS ) ;
        String exclGlobs = config.getString( CK_EXCL_GLOBS ) ;
        parseGlobs( inclGlobs, exclGlobs ) ;
    }
    
    private void parseGlobs( String inclGlobs, String exclGlobs ) 
        throws Exception {
        
        if( StringUtil.isNotEmptyOrNull( inclGlobs ) ) {
            this.includePathMatchers.addAll( convertToPathMatchers( inclGlobs ) ) ;
        }
        if( StringUtil.isNotEmptyOrNull( exclGlobs ) ) {
            this.excludePathMatchers.addAll( convertToPathMatchers( exclGlobs ) ) ;
        }
    }
    
    private List<PathMatcher> convertToPathMatchers( String globList )
        throws Exception {
        
        String[] globs = globList.split( File.pathSeparator ) ;
        List<PathMatcher> matchers = new ArrayList<PathMatcher>() ;
        
        for( String glob : globs ) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher( "glob:" + glob ) ;
            matchers.add( matcher ) ;
        }
        return matchers ;
    }
}
