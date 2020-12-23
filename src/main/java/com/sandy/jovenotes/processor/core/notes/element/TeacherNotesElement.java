package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.TEACHER_NOTE ;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.jovenotes.processor.util.StringUtil ;
import com.sandy.xtext.joveNotes.TeacherNote ;

public class TeacherNotesElement extends AbstractNotesElement {
    
    private TeacherNote ast = null ;
    
    private String caption    = null ;
    private String note       = null ;
    private String cmapImg    = null ;
    
    public TeacherNotesElement( Chapter chapter, TeacherNote ast, 
                                RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( TEACHER_NOTE, chapter, ast, rtcNE ) ;
        this.ast = ast ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        this.caption = textProcessor.processText( ast.getCaption() ) ;
        this.cmapImg = textProcessor.processCMapAST( ast.getCmap() ) ;
        this.note    = textProcessor.processText( ast.getNote() ) ;
        
        if( cmapImg != null ) {
            this.note += "<p>{{@img " + this.cmapImg + "}}" ;
        }
        
        if( StringUtil.isEmptyOrNull( this.caption ) ) {
            this.caption = "Note" ;
        }
    }
    
    public String getObjIdSeed() { 
        return this.ast.getNote() ; 
    }
    
    public void collectContentAttributes( Map<String, Object> map ) {
        map.put( "caption", this.caption ) ;
        map.put( "note", this.note ) ;
    }
}

