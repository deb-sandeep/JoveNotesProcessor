package com.sandy.jovenotes.processor.core.cards;

import java.util.LinkedHashMap ;
import java.util.Map ;

import org.json.simple.JSONValue ;

import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.util.StringUtil ;

public abstract class AbstractCard {
    
    private AbstractNotesElement     ne    = null ;
    private RefToContextNotesElement rtcNE = null ;
    private String                   type  = null ;
    private String                   objId = null ;
    
    protected boolean ready   = true ;
    
    public AbstractCard( AbstractNotesElement ne, 
                         RefToContextNotesElement rtcNE, String type ) {
        this.ne    = ne ;
        this.type  = type ;
        this.rtcNE = rtcNE ;
    }
    
    public String getType() {
        return this.type ;
    }
    
    public boolean isReady() {
        return this.ready ;
    }
    
    public String getRawRTCCaption() {
        if( this.rtcNE != null ) {
            return this.rtcNE.getRawRTCCaption() ;
        }
        return null ;
    }
    
    public final String getObjId() {
        
        String parentObjIdSeed = ( rtcNE == null ) ? ne.getObjIdSeed() : 
                                                     rtcNE.getObjIdSeed() ;
        if( this.objId == null ){
            this.objId = StringUtil.getHash( parentObjIdSeed + "Card" + 
                                             getType() + getObjIdSeed() ) ; 
        }
        return this.objId ;
    } ;
    
    public String getContent() throws Exception {
        Map<String, Object> map = new LinkedHashMap<String, Object>() ;
        
        collectContentAttributes( map ) ;
        String content = JSONValue.toJSONString( map ) ;
        
        return content ;
    }
    
    protected abstract String getObjIdSeed() ;
    
    public abstract int getDifficultyLevel() ;
    
    protected abstract void collectContentAttributes( Map<String, Object> map ) 
        throws Exception ; 
}

