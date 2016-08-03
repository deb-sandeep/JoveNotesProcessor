package com.sandy.jovenotes.processor.util;

import java.io.BufferedReader ;
import java.io.File ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import net.sourceforge.plantuml.SourceStringReader ;

import org.apache.commons.codec.binary.Base64 ;
import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;
import org.jsoup.Jsoup ;
import org.pegdown.Extensions ;
import org.pegdown.PegDownProcessor ;

import com.sandy.jcmap.util.CMapBuilder ;
import com.sandy.jcmap.util.CMapDotSerializer ;
import com.sandy.jcmap.util.CMapElement ;
import com.sandy.jcmap.util.GraphvizAdapter ;
import com.sandy.jovenotes.processor.JoveNotes ;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.util.tagprocessor.CarouselTagProcessor ;
import com.sandy.jovenotes.processor.util.tagprocessor.TableTagProcessor ;
import com.sandy.xtext.joveNotes.CMap ;

public class JNTextProcessor {

    private static final Logger log = Logger.getLogger(JNTextProcessor.class) ;
    
    private static PegDownProcessor pdProcessor   = 
                new PegDownProcessor( Extensions.ALL & 
                                     ~Extensions.HARDWRAPS & 
                                     ~Extensions.ANCHORLINKS ) ;
    
    private static final String JN_MARKER_PATTERN = 
                             "\\{\\{@([a-zA-Z0-9]*)\\s+((.(?!\\{\\{))*)\\}\\}" ;
    
    private static final String MJ_BLOCK_MARKER_PATTERN = 
                             "\\$\\$.*?\\$\\$" ;
    
    private static final String MJ_INLINE_MARKER_PATTERN = 
                             "\\\\\\(.*?\\\\\\)" ;
    
    private Chapter chapter = null ;
    private ArrayList<File> existingMediaFiles = null ;
    
    private static Map<String, Boolean> stopWords = new HashMap<String, Boolean>() ;
    
    {
        try {
            loadStopWords() ;
        }
        catch( Exception e ) {
            log.error( "Could not load stop words. ", e ) ;
        }
    }
    
    private static void loadStopWords() throws Exception {
        
        InputStream is = JNTextProcessor.class.getResourceAsStream( "/stopwords.txt" ) ;
        if( is != null ) {
            BufferedReader br = new BufferedReader( new InputStreamReader( is ) ) ;
            String line = null ;
            while( ( line = br.readLine() ) != null ) {
                if( StringUtil.isNotEmptyOrNull( line ) ) {
                    stopWords.put( line.toLowerCase(), Boolean.TRUE ) ;
                }
            }
        }
        else {
            throw new Exception( "stopwords list not found." ) ;
        }
    }
    
    public JNTextProcessor( Chapter chapter, ArrayList<File> existingMediaFiles ) {
        
        this.chapter = chapter ;
        this.existingMediaFiles = existingMediaFiles ;
    }
    
    public int getNormalizedWordsInFormattedText( String text ) {
        
        String normAnswer = text.toLowerCase().replaceAll( JN_MARKER_PATTERN, "" ) ; 
        normAnswer = Jsoup.parse( normAnswer )
                                 .text().toLowerCase()
                                 .replaceAll( "[^A-Za-z0-9\u0900-\u097F ]", "" ) ;
        
        String[] wordsInAnswer = normAnswer.split( "\\s+" ) ;
        int numNormalizedWords = 0 ;
        
        for( String word : wordsInAnswer ) {
            if( StringUtil.isNotEmptyOrNull( word ) ) {
                if( !stopWords.containsKey( word.trim() ) ) {
                    numNormalizedWords++ ;
                }
            }
        }
        return numNormalizedWords ;
    }
    
    public String processText( String input ) 
        throws Exception {
        
        if( input == null ) return null ;
        
        String output = null ;
        output = processJoveNotesMarkers( input ) ;
        output = processBlockMathJaxMarkers( output ) ;
        output = processInlineMathJaxMarkers( output ) ;
        
        output = processMarkDown( output ) ;
        
        // Let's piggy back on bootstrap formatting of tables.
        String customTableTag = "<table class=\"pure-table pure-table-horizontal\">" ;
        output = output.replaceAll( "<table>", customTableTag ) ;
        output = output.replaceAll( "\\\\\\\\", "\\\\" ) ;
        
        return output ;
    }
    
    private String processMarkDown( String input ) {
        String output = pdProcessor.markdownToHtml( input ) ;
        if( output.startsWith( "<p>" ) && output.endsWith( "</p>" ) ) {
            output = output.substring( 3, output.length()-4 ) ;
        }
        return output ;
    }
    
    private String processJoveNotesMarkers( String input ) 
        throws Exception {
        
        StringBuilder outputBuffer = new StringBuilder() ;
        
        Pattern r = Pattern.compile( JN_MARKER_PATTERN, Pattern.DOTALL ) ;
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
    
    private String processBlockMathJaxMarkers( String input ) 
            throws Exception {
        
        StringBuilder outputBuffer = new StringBuilder() ;
        
        Pattern r = Pattern.compile( MJ_BLOCK_MARKER_PATTERN ) ;
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
    
    private String processInlineMathJaxMarkers( String input ) 
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
    
    private String processMarker( String type, String data ) 
        throws Exception {
        
        if( type.equals( "img" ) ) {
            processImg( data ) ;
            return null ;
        }
        else if( type.equals( "audio" ) ) {
            processAudio( data ) ;
            return null ;
        }
        else if( type.equals( "doc" ) ) {
            processDoc( data ) ;
            return null ;
        }
        else if( type.equals( "cmap" ) ) {
            return "<p>{{@img " + processCMapContent( data ) + "}}<p>" ;
        }
        else if( type.equals( "uml" ) ) {
            return "<p>{{@img " + processUMLContent( data ) + "}}<p>" ;
        }
        else if( type.equals( "youtube" ) ) {
            return processYouTubeVideoId( data ) ;
        }
        else if( type.equals( "eval" ) ) {
            return processEval( data ) ;
        }
        else if( type.equals( "table" ) ) {
            TableTagProcessor processor = new TableTagProcessor( data, this ) ;
            return processor.getProcessedText() ;
        }
        else if( type.equals( "ichem" ) ) {
            return "\\( \\ce{" + data + "} \\)" ;
        }
        else if( type.equals( "imath" ) ) {
            return "\\( " + data + " \\)" ;
        }
        else if( type.equals( "chem" ) ) {
            return "$$ \\ce{" + data + "} $$" ;
        }
        else if( type.equals( "math" ) ) {
            return processMathTagContents( data ) ;
        }
        else if( type.equals( "carousel" ) ) {
            CarouselTagProcessor processor = new CarouselTagProcessor( data, this ) ;
            return processor.getProcessedText() ;
        }
        else if( type.equals( "red" ) ) {
            return "<span class='red'>" + processMarkDown( data ) + "</span>" ;
        }
        else if( type.equals( "green" ) ) {
            return "<span class='green'>" + processMarkDown( data ) + "</span>" ;
        }
        else if( type.equals( "blue" ) ) {
            return "<span class='blue'>" + processMarkDown( data ) + "</span>" ;
        }
        
        return null ;
    }
    
    public void processImg( String imgName ) 
        throws Exception {
        
        // If the image name ends with .cmap.png or .uml.png, we do nothing. 
        // This is so because cmap and uml files are generated and stored in 
        // the media directory. They are not expected in the source folder.
        if( imgName.endsWith( ".cmap.png" ) || imgName.endsWith( ".uml.png" ) ) {
            return ;
        }
        
        File srcFile  = new File( chapter.getSrcImagesFolder(), imgName ) ;
        File destFile = new File( chapter.getMediaDirectory(), 
                                  "img" + File.separator + imgName ) ;
        
        processSrcMediaFile( srcFile, destFile ) ;
    }

    public void processAudio( String audioClipName ) 
            throws Exception {
            
        File srcFile  = new File( chapter.getSrcAudioFolder(), audioClipName ) ;
        File destFile = new File( chapter.getMediaDirectory(), 
                                  "audio" + File.separator + audioClipName ) ;
        
        processSrcMediaFile( srcFile, destFile ) ;
    }

    public void processDoc( String docName ) 
            throws Exception {
        
        if( docName.indexOf( '|' ) != -1 ) {
            docName = docName.substring( 0, docName.indexOf( '|' ) ).trim() ;
        }
            
        File srcFile  = new File( chapter.getSrcDocFolder(), docName ) ;
        File destFile = new File( chapter.getMediaDirectory(), 
                                  "doc" + File.separator + docName ) ;
        
        processSrcMediaFile( srcFile, destFile ) ;
    }
    
    
    private void processSrcMediaFile( File srcFile, File destFile ) 
        throws Exception {
        
        if( !srcFile.exists() ) {
            String msg = "Source media file " + srcFile.getAbsolutePath() + 
                         " does not exist." ;
            throw new Exception( msg ) ;
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
    }
    
    public String processCMapAST( CMap ast ) throws Exception {
        // If there is no CMap in the source, nothing is to be done.
        if( ast == null )return null ;
        return processCMapContent( ast.getContent() ) ;
    }
    
    private String processCMapContent( String cmapContent ) 
        throws Exception {
        
        File imgFile = getCMapDestImageFilePath( cmapContent ) ;

        // If the image file exists, we do not regenerate. We have named the file
        // based on its content hash. Which implies, if the content would have
        // changed, the file name would change too.
        if( imgFile.exists() ) {
            this.existingMediaFiles.remove( imgFile ) ;
            return imgFile.getName() ;
        }

        log.info( "\tGenerating cmap image. " + imgFile.getName() );
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

    private String processUMLContent( String umlContent ) 
            throws Exception {
            
        File imgFile = getUMLDestImageFilePath( umlContent ) ;

        // If the image file exists, we do not regenerate. We have named the file
        // based on its content hash. Which implies, if the content would have
        // changed, the file name would change too.
        if( imgFile.exists() ) {
            this.existingMediaFiles.remove( imgFile ) ;
            return imgFile.getName() ;
        }

        log.info( "\tGenerating uml image. " + imgFile.getName() );
        String processedUMLContent = preProcessUMLContent( umlContent ) ;
        SourceStringReader reader = new SourceStringReader( processedUMLContent ) ;
        
        if( !imgFile.getParentFile().exists() ) {
            imgFile.getParentFile().mkdirs() ;
        }
        
        reader.generateImage( imgFile ) ;
        
        return imgFile.getName() ;
    }
    
    private String preProcessUMLContent( String umlContent ) {
        return "@startuml\n" + umlContent + "\n@enduml" ;
    }
    
    private File getUMLDestImageFilePath( String umlContent ) {
        String imgName = StringUtil.getHash( umlContent ) + ".uml.png" ;
        File destFile = new File( chapter.getMediaDirectory(), "img" + File.separator + imgName ) ;
        return destFile ;
    }
    
    private String processYouTubeVideoId( String youTubeVideoId ) {
        StringBuilder buffer = new StringBuilder() ;
        buffer.append( "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/" ) ;
        buffer.append( youTubeVideoId.trim() ) ;
        buffer.append( "\" frameborder=\"0\" allowfullscreen></iframe>" ) ;
        return buffer.toString() ;
    }
    
    private String processEval( String expression ) {
        String b64Encoded = Base64.encodeBase64String( expression.getBytes() ) ;
        return "{{@eval " + b64Encoded + "}}" ;
    }
    
    private String processMathTagContents( String content ) {
        String[] lines = content.split( "\n" ) ;
        StringBuffer buffer = new StringBuffer() ;
        if( lines.length > 1 ) {
            for( String line : lines ) {
                buffer.append( "\\( " + line + " \\)\n\n" ) ;
            }
        }
        else {
            buffer.append( "$$ " + content + " $$" ) ;
        }
        return buffer.toString() ;
    }
    
    public String createAttributeString( String[][] attributes ) {
        StringBuilder buffer = new StringBuilder() ;
        
        if( attributes != null && attributes.length > 0 ) {
            for( String[] attr : attributes ) {
                buffer.append( " " ) ;
                buffer.append( attr[0] ) ;
                if( attr[1] != null ) {
                    buffer.append( "=\"" ).append( attr[1] ).append( "\"" ) ; 
                }
            }
            buffer.append( " " ) ;
        }
        
        return buffer.toString() ;
    }
}
