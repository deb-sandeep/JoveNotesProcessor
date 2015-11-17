package com.sandy.jovenotes.processor.util.tagprocessor;

import java.io.BufferedReader ;
import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.List ;

import com.sandy.jovenotes.processor.util.JNTextProcessor ;

public class TableTagProcessor {

//    private static final Logger log = Logger.getLogger(JNTextProcessor.class) ;
    
    private enum ProcessingState { IDLE, PROC_TH, PROC_TD } ;
    
    private String          markupData    = null ;
    private JNTextProcessor textProcessor = null ;
    
    private ProcessingState procState     = ProcessingState.IDLE ;
    private StringBuffer    currentString = new StringBuffer() ;
    private List<String>    currRow       = null ;
    
    private List<String>       tableHeaders = new ArrayList<String>() ;
    private List<List<String>> tableRows    = new ArrayList<List<String>>() ;
    
    private int currentCellNumber = 0 ;
    
    public TableTagProcessor( String markupData, JNTextProcessor textProcessor ) {
        this.markupData = markupData ;
        this.textProcessor = textProcessor ;
    }
    
    public String getProcessedText() throws Exception {
        
        parseMarkupData() ;
        return generateTable() ;
    }
    
    private void parseMarkupData() throws Exception {
        
        BufferedReader br = new BufferedReader( new StringReader( this.markupData ) ) ;
        String line = null ;
        while( ( line = br.readLine() ) != null ) {
            
            if( line.startsWith( "@th" ) ) {
                newHeaderFound( line.substring( 3 ) ) ;
            }
            else if( line.startsWith( "@td" ) ) {
                newCellFound( line.substring( 3 ) ) ;
            }
            else {
                continuationStringFound( line ) ;
            }
        }
        tableEndFound() ;
    }
    
    private void endPreviousProcessingState() throws Exception {
        
        String markupData = currentString.toString().trim() ;
        markupData = markupData.replace( "[[@", "{{@" ) ;
        markupData = markupData.replace( "]]", "}}" ) ;
        markupData = markupData.replace( "#td ", "@td " ) ;
        markupData = markupData.replace( "#th ", "@th " ) ;
        
        String procStr = this.textProcessor.processText( markupData ) ;
        
        currentString.setLength( 0 ) ;
        
        if( procState == ProcessingState.PROC_TH ) {
            tableHeaders.add( procStr ) ;
        }
        else if( procState == ProcessingState.PROC_TD ) {
            currRow.add( procStr ) ;
        }
    }
    
    private void newHeaderFound( String line ) throws Exception {
        
        endPreviousProcessingState() ;
        currentString.append( line ).append( "\n" ) ;
        procState = ProcessingState.PROC_TH ;
    }
    
    private void newCellFound( String line ) throws Exception {
        
        endPreviousProcessingState() ;
        currentCellNumber++ ;
        
        if( tableHeaders.size() == 1 ) {
            List<String> newRow = new ArrayList<String>() ;
            tableRows.add( newRow ) ;
            currRow = newRow ;
        }
        else if( currentCellNumber % tableHeaders.size() == 1 ) {
            List<String> newRow = new ArrayList<String>() ;
            tableRows.add( newRow ) ;
            currRow = newRow ;
        }
        
        currentString.append( line ).append( "\n" ) ;
        procState = ProcessingState.PROC_TD ;
    }
    
    private void continuationStringFound( String line ) {
        currentString.append( line ).append( "\n" ) ;
    }
    
    private void tableEndFound() throws Exception {
        endPreviousProcessingState() ;
    }
    
    private String generateTable() {
        
        if( tableHeaders.isEmpty() && tableRows.isEmpty() ) {
            return "" ;
        }
        
        StringBuilder builder = new StringBuilder() ;
        builder.append( "<table class='pure-table pure-table-horizontal'>\n" ) ;
        appendTableHeader( builder ) ;
        appendTableRows( builder ) ;
        builder.append( "</table>\n" ) ;
        
        return builder.toString() ;
    }
    
    private void appendTableHeader( StringBuilder buffer ) {
        if( !tableHeaders.isEmpty() ) {
            buffer.append( "<thead><tr>\n" ) ;
            for( String th : tableHeaders ) {
                buffer.append( "<th>" + th + "</th>\n" ) ;
            }
            buffer.append( "</tr></thead>\n" ) ;
        }
    }

    private void appendTableRows( StringBuilder buffer ) {
        if( !tableRows.isEmpty() ) {
            for( List<String> tr : tableRows ) {
                buffer.append( "<tr>\n" ) ;
                for( String td : tr ) {
                    buffer.append( "<td>" + td + "</td>\n" ) ;
                }
                buffer.append( "</tr>\n" ) ;
            }
        }
    }
}
