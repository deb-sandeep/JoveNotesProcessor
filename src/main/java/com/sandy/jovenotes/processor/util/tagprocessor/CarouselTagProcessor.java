package com.sandy.jovenotes.processor.util.tagprocessor;

import java.io.BufferedReader ;
import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.List ;

import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.jovenotes.processor.util.StringUtil ;

public class CarouselTagProcessor {

//    private static final Logger log = Logger.getLogger(JNTextProcessor.class) ;
    
    private static class ImageInfo {
        String imgName = null ;
        String caption = null ;
        
        ImageInfo( String imgName, String caption ) {
            this.imgName = imgName ;
            this.caption = caption ;
        }
    }
    
    private enum ProcessingState { IDLE, PROC_SLIDE } ;
    
    private String          markupData    = null ;
    private JNTextProcessor textProcessor = null ;
    
    private ProcessingState procState     = ProcessingState.IDLE ;
    private StringBuffer    currentString = new StringBuffer() ;
    
    private List<ImageInfo> images = new ArrayList<ImageInfo>() ;
    
    private String carouselId = "myCarousel" ;
    
    public CarouselTagProcessor( String markupData, JNTextProcessor textProcessor ) {
        this.markupData = markupData ;
        this.textProcessor = textProcessor ;
        this.carouselId = "carousel_" + Integer.toHexString( markupData.hashCode() ) ;
    }
    
    public String getProcessedText() throws Exception {
        parseMarkupData() ;
        return generateProcessedText() ;
    }
    
    private void parseMarkupData() throws Exception {
        
        BufferedReader br = new BufferedReader( new StringReader( this.markupData ) ) ;
        String line = null ;
        while( ( line = br.readLine() ) != null ) {
            
            line = line.trim() ;
            if( line.startsWith( "@slide" ) ) {
                newImgFound( line.substring( 6 ) ) ;
            }
            else {
                currentString.append( line ).append( "\n" ) ;
            }
        }
        carouselEndFound() ;
    }
    
    private void newImgFound( String line ) throws Exception {
        
        endPreviousProcessingState() ;
        currentString.append( line ).append( "\n" ) ;
        procState = ProcessingState.PROC_SLIDE ;
    }
    
    private void endPreviousProcessingState() throws Exception {
        
        if( procState != ProcessingState.IDLE ) {
            
            String imgName    = null ;
            String caption    = null ;
            String markupData = currentString.toString().trim() ;
            
            markupData = markupData.replace( "[[@", "{{@" ) ;
            markupData = markupData.replace( "]]", "}}" ) ;
            
            if( markupData.endsWith( "]" ) ) {
                int captionStartIndex = markupData.indexOf( "[" ) ;
                if( captionStartIndex != -1 ) {
                    imgName = markupData.substring( 0, captionStartIndex ).trim() ;
                    caption = markupData.substring( captionStartIndex+1, 
                                                    markupData.length()-1 ).trim() ;
                }
            }
            
            if( imgName == null ) {
                imgName = markupData ;
            }
            
            if( StringUtil.isNotEmptyOrNull( caption ) ) {
                caption = textProcessor.processText( caption ) ;
            }
            ImageInfo imgInfo = new ImageInfo( imgName, caption ) ;
            images.add( imgInfo ) ;
            
            currentString.setLength( 0 ) ;
            procState = ProcessingState.IDLE ;
        }
    }
    
    private void carouselEndFound() throws Exception {
        endPreviousProcessingState() ;
    }
    
    private String generateProcessedText() throws Exception {
        
        if( images.isEmpty() ) { return "" ; }
        
        String[][] attributes = {
             { "id",              carouselId },
             { "class",          "carousel slide" },
             { "data-ride",      "carousel" },
             { "align",          "center" },
             { "data-interval",  "false" },
             { "data-wrap",      "false" }
        };
        
        StringBuilder builder = new StringBuilder() ;
        builder.append( "<div class=\"container\"></div>" ) ;
        builder.append( "<div" + textProcessor.createAttributeString( attributes ) + ">" ) ;
        builder.append( getCarouselIndicators() ) ;
        builder.append( getWrapperForSlides() ) ;
        builder.append( getLeftAndRightControls() ) ;
        builder.append( "</div>" ) ;
        builder.append( "</div>" ) ;
        
        return builder.toString() ;
    }
    
    private StringBuilder getCarouselIndicators() {
        StringBuilder builder = new StringBuilder() ;
        
        builder.append( "<div align=\"center\">" ) ;
        builder.append( "<ol class=\"carousel-indicators\">" ) ;
        for( int i=0; i<images.size(); i++ ) {
            builder.append( "<li data-target=\"#" + carouselId + "\" " ) ;
            builder.append( "data-slide-to=\"" + i + "\" " ) ;
            if( i == 0 ) {
                builder.append( "class=\"active\"" ) ;
            }
            builder.append( "></li>" ) ;
        }
        builder.append( "</ol>" ) ;
        builder.append( "</div>" ) ;
        return builder ;
    }
    
    private StringBuilder getWrapperForSlides() throws Exception {
        StringBuilder builder = new StringBuilder() ;
        builder.append( "<div class=\"carousel-inner\" role=\"listbox\">" ) ;
        for( int i=0; i<images.size(); i++ ) {
            ImageInfo imgInfo = images.get( i ) ;
            
            
            String cls = ( i == 0 )? "item active" : "item" ;
            builder.append( "<div class=\"" + cls + "\">" ) ;
            if( StringUtil.isNotEmptyOrNull( imgInfo.imgName ) ) {
                String imgRef = "{{@img " + imgInfo.imgName + "}}" ;
                textProcessor.processText( imgRef ) ;
                builder.append( imgRef ) ;
            }
            if( imgInfo.caption != null ) {
                builder.append( "<div class=\"carousel-caption\">" ) ;
                builder.append( "<p>" + imgInfo.caption + "</p>" ) ;
                builder.append( "</div>" ) ;
            }
            builder.append( "</div>" ) ;
        }
        builder.append( "</div>" ) ;
        return builder ;
    }
    
    private StringBuilder getLeftAndRightControls() {
        StringBuilder builder = new StringBuilder() ;
        builder.append( "<a class=\"left carousel-control\" data-target=\"#" + carouselId + "\" role=\"button\" data-slide=\"prev\">" ) ;
        builder.append( "<span class=\"glyphicon glyphicon-chevron-left\"></span>" ) ;
        builder.append( "</a>" ) ;
        builder.append( "<a class=\"right carousel-control\" data-target=\"#" + carouselId + "\" role=\"button\" data-slide=\"next\">" ) ;
        builder.append( "<span class=\"glyphicon glyphicon-chevron-right\"></span>" ) ;
        builder.append( "</a>" ) ;
        return builder ;
    }
}
