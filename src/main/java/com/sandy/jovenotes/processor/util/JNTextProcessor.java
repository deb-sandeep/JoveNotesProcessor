package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import com.sandy.jovenotes.processor.core.notes.Chapter;

public class JNTextProcessor {

	private static final Logger log = Logger.getLogger(JNTextProcessor.class) ;
	
	private static PegDownProcessor pdProcessor   = 
			    new PegDownProcessor( Extensions.ALL & ~Extensions.HARDWRAPS ) ;
	
	private static final String JN_MARKER_PATTERN = 
			                 "\\{\\{@([a-zA-Z0-9]*)\\s+((.(?!\\{\\{))*)\\}\\}" ;
	
	private Chapter chapter = null ;
	private ArrayList<File> existingMediaFiles = null ;
	
	public JNTextProcessor( Chapter chapter, ArrayList<File> existingMediaFiles ) {
		
		this.chapter = chapter ;
		this.existingMediaFiles = existingMediaFiles ;
	}
	
	public String processText( String input ) 
		throws Exception {
		
		String output = processJoveNotesMarkers( input ) ;
		output = pdProcessor.markdownToHtml( output ) ;
		return output ;
	}
	
	private String processJoveNotesMarkers( String input ) 
		throws Exception {
		
		StringBuilder outputBuffer = new StringBuilder() ;
		
		Pattern r = Pattern.compile( JN_MARKER_PATTERN ) ;
		Matcher m = r.matcher( input ) ;
		
		int lastEndMarker = 0 ;
		
		while( m.find() ) {
			int start = m.start() ;
			int end   = m.end() ;
			
			String markerType = m.group( 1 ) ;
			String markerData = m.group( 2 ) ;
			
			String processedString = processMarker( markerType, markerData ) ;
			if( processedString != null ) {
				outputBuffer.append( input.substring( lastEndMarker, start ) ) ;
				outputBuffer.append( processedString ) ;
				lastEndMarker = end ;
			}
		}
		outputBuffer.append( input.substring(lastEndMarker, input.length() ) ) ;
		return outputBuffer.toString() ;
	}
	
	private String processMarker( String type, String data ) 
		throws Exception {
		
		if( type.equals( "img" ) ) {
			return processImgMarker( data ) ;
		}
		return null ;
	}
	
	private String processImgMarker( String imgName ) 
		throws Exception {
		
		File srcFile  = new File( chapter.getSrcImagesFolder(), imgName ) ;
		File destFile = new File( chapter.getMediaDirectory(), "img" + File.separator + imgName ) ;
		
		if( !srcFile.exists() ) {
			log.error( "Source image file " + srcFile.getAbsolutePath() + 
					   " does not exist." ) ;
			return null ;
		}
		
		if( destFile.exists() ) {
			if( ( srcFile.length() != destFile.length() ) || 
				( srcFile.lastModified() > destFile.lastModified() ) ) {
				FileUtils.copyFile( srcFile, destFile ) ;
			}
		}
		else {
			FileUtils.copyFile( srcFile, destFile ) ;
		}
		existingMediaFiles.remove( destFile ) ;
		return null ;
	}
}
