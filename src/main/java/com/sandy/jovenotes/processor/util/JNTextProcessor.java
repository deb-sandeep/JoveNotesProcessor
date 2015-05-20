package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import com.sandy.jcmap.util.CMapBuilder;
import com.sandy.jcmap.util.CMapDotSerializer;
import com.sandy.jcmap.util.CMapElement;
import com.sandy.jcmap.util.GraphvizAdapter;
import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.core.notes.Chapter;
import com.sandy.xtext.joveNotes.CMap;

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
		
		// If the image name ends with .cmap.png, we do nothing. This is so 
		// because cmap files are generated and stored in the media directory.
		// CMap image files are not expected in the source folder.
		if( imgName.endsWith( ".cmap.png" ) ) {
			return null ;
		}
		
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

	public String processCMap( CMap ast )
			throws Exception {
		
		// If there is no CMap in the source, nothing is to be done.
		if( ast == null )return null ;

		String cmapContent = ast.getContent() ;
		File imgFile = getCMapDestImageFilePath( cmapContent ) ;

		// If the image file exists, we do not regenerate. We have named the file
		// based on its content hash. Which implies, if the content would have
		// changed, the file name would change too.
		if( imgFile.exists() ) {
			this.existingMediaFiles.remove( imgFile ) ;
			return imgFile.getName() ;
		}

		log.debug( "\tGenerating cmap image. " + imgFile.getName() );
		CMapElement cmap = new CMapBuilder().buildCMapElement( cmapContent ) ;
        CMapDotSerializer dotSerializer = new CMapDotSerializer( cmap ) ;
        
        File dotFile = new File( JoveNotes.config.getWorkspaceDir(), "temp.dot" ) ;

        String fileContent = dotSerializer.convertCMaptoDot() ;
        FileUtils.writeStringToFile( dotFile, fileContent, "UTF-8" ) ;
        
        File dotExecFile = JoveNotes.config.getGraphvizDotPath() ;
        GraphvizAdapter gvAdapter = new GraphvizAdapter( dotExecFile.getAbsolutePath() ) ;
        gvAdapter.generateGraph( dotFile, imgFile ) ;
        
        dotFile.delete() ;
        
        return imgFile.getName() ;
	}
	
	private File getCMapDestImageFilePath( String cmapContent ) {
		String imgName = StringUtil.getHash( cmapContent ) + ".cmap.png" ;
		File destFile = new File( chapter.getMediaDirectory(), "img" + File.separator + imgName ) ;
		return destFile ;
	}
}
