package com.sandy.jovenotes.processor.junit.poc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MJRegexpPOC {

	private static final Logger log = Logger.getLogger( MJRegexpPOC.class ) ;
	
    private static final String MJ_INLINE_MARKER_PATTERN = 
            "(?<!<table.*)\\\\\\(.*?\\\\\\)" ;

	public static void main( String[] args ) throws Exception {
		
		// String to be scanned to find the pattern.
		String line = "This is a \\( a + b = c \\) MJ expression";
	
		log.info( "Input = " + line ) ;
		String output1 = processInlineMathJaxMarkers( line ) ;
		log.info( "Process 1 = " + output1 );
        String output2 = processInlineMathJaxMarkers( output1 ) ;
        log.info( "Process 1 = " + output2 );
	}
	
    private static String processInlineMathJaxMarkers( String input ) 
            throws Exception {
        
        StringBuilder outputBuffer = new StringBuilder() ;
        
        Pattern r = Pattern.compile( MJ_INLINE_MARKER_PATTERN ) ;
        Matcher m = r.matcher( input ) ;
        
        int lastEndMarker = 0 ;
        
        while( m.find() ) {
            int start = m.start() ;
            int end   = m.end() ;
            
            String markerData = m.group( 0 ) ;
            String processedString = markerData.replace( "\\", "\\\\" ) ;
            
            outputBuffer.append( input.substring( lastEndMarker, start ) ) ;
            outputBuffer.append( processedString ) ;
            
            lastEndMarker = end ;
        }
        outputBuffer.append( input.substring(lastEndMarker, input.length() ) ) ;
        return outputBuffer.toString() ;
    }
	
}
