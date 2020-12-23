package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.IMAGE_LABEL ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.ImageLabelCard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.HotSpot ;
import com.sandy.xtext.joveNotes.ImageLabel ;

public class ImageLabelElement extends AbstractNotesElement {
    
    private ImageLabel ast       = null ;
    private String     objIdSeed = null ;
    
    private Map<String, Object> cardJSONAttrs = new HashMap<String, Object>() ;
    private Map<String, Object> neJSONAttrs   = new HashMap<String, Object>() ;
    
    public ImageLabelElement( Chapter chapter, ImageLabel ast, 
                              RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( IMAGE_LABEL, chapter, ast, rtcNE ) ;
        this.ast = ast ;
        this.objIdSeed = ast.getImageName() + ast.getHotspots().size() + 
                         ast.getHotspots().get(0).getLabel() ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        textProcessor.processImg( ast.getImageName() ) ;
        
        List<List<Object>> hsArray = new ArrayList<List<Object>>() ;
        for( HotSpot hs : ast.getHotspots() ) {
            List<Object> hsElement = new ArrayList<Object>() ;
            hsElement.add( hs.getX() ) ;
            hsElement.add( hs.getY() ) ;
            hsElement.add( hs.getLabel() ) ;
            hsArray.add( hsElement ) ;
        }
        
        String imgLabelCaption = null ;
        if( ast.getCaption() == null ) {
            imgLabelCaption = "Label the image" ;
        }
        else {
            imgLabelCaption = textProcessor.processText( ast.getCaption() ) ;
        }
        
        String cardImgLabelCaption = imgLabelCaption ;
        if( getRawRTCCaption() != null ) {
            cardImgLabelCaption = "<blockquote>" + 
                                  textProcessor.processText( getRawRTCCaption() ) + 
                                  "</blockquote>\n\n" +
                                  imgLabelCaption ;
        }
        
        cardJSONAttrs.put( "caption",   cardImgLabelCaption ) ;
        cardJSONAttrs.put( "imageName", ast.getImageName() ) ;
        cardJSONAttrs.put( "hotSpots",  hsArray ) ;
        
        neJSONAttrs.put( "caption",   imgLabelCaption ) ;
        neJSONAttrs.put( "imageName", ast.getImageName() ) ;
        neJSONAttrs.put( "hotSpots",  hsArray ) ;
        
        cards.add( new ImageLabelCard( this, rtcNE, objIdSeed, cardJSONAttrs ) ) ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ){
        map.putAll( neJSONAttrs ) ;
    }
}


