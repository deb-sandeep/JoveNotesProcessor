package com.sandy.jovenotes.processor.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Wrapper class to define the Run Mode configuration and associated rules
 * 
 * @author Vivek Kant
 *
 */
public class RunMode {
    
    private static Logger log = Logger.getLogger(RunMode.class);
    
    public static final String PRODUCTION = "production" ;
    public static final String DEVELOPMENT = "development" ;
    public static final String PREVIEW = "preview" ;
    
    private final Map<String, Boolean> modes = new HashMap<String, Boolean>() ;
    
    public boolean isProduction()   { return modes.get( PRODUCTION ) ; }
    public boolean isDevelopment()  { return modes.get( DEVELOPMENT ) ; }
    public boolean isPreview()      { return modes.get( PREVIEW ) ; }

    public RunMode( String params ) throws Exception {
        
        modes.put( PRODUCTION, false ) ;
        modes.put( DEVELOPMENT, true ) ;
        modes.put( PREVIEW, false ) ;
        
        if ( params == null || "".equals( params ) ) {
            log.warn( "runMode is not specified, defaulting to development" ) ;
            return ;
        }
        
        String tokens[] = params.toLowerCase().split( "," ) ;
        for ( String token : tokens ) {
            validateAndAssign( token.trim() ) ;
        }
        
        validateRunModeCompatibility() ;
    }
    
    public void validateAndAssign( String mode ) throws Exception {
        
        Boolean value = modes.get( mode ) ;
        if ( value != null ) {
            modes.put( mode , true ) ;
        }
        else {
            throw new Exception( "Invalid value for Run Mode: " + mode ) ;
        }
    }
    
    public void validateRunModeCompatibility() throws Exception {

        if ( isProduction() && isDevelopment() ) {
            throw new Exception( "Run modes of Development & Production cannot execute together" ) ;
        }
    }

}
