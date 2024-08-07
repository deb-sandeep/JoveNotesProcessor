package com.sandy.jovenotes.processor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class CommandLineExec {
    
    private static final Logger log = Logger.getLogger( CommandLineExec.class ) ;
    
    private CommandLineExec() {}
    
    private static String getCommandAsString( String[] cmdParts ) {
        StringBuilder builder = new StringBuilder();
        for( String part : cmdParts ) {
            builder.append( part ).append( " " ) ;
        }
        return builder.toString().trim() ;
    }
    
    public static int executeCommand( String[] command, ArrayList<String> outputBuffer ) {
        return executeCommand( command, outputBuffer, null ) ;
    }
    
    public static int executeCommand( String[] command, ArrayList<String> outputBuffer, File workingDir ) {
        
        int retVal = -1 ;
        try {
            log.debug( "Executing command = " + getCommandAsString(command) ) ;
            
            Runtime rt = Runtime.getRuntime() ;
            Process pr = rt.exec( command, null, workingDir ) ;
            
            InputStream is = pr.getInputStream() ;
            InputStreamReader isr = new InputStreamReader( is ) ;
            BufferedReader input = new BufferedReader( isr ) ;
            
            String line ;
            while( ( line = input.readLine() ) != null ) {
                log.debug( "output : " + line ) ;
                if( outputBuffer != null ) {
                    outputBuffer.add( line ) ;
                }
            }
            retVal = pr.waitFor() ;
            log.debug( "Command executed with return code = " + retVal ) ;
        }
        catch( Exception e ) {
            log.error( "Command execution error.", e ) ;
        }
        return retVal;
    }
}
