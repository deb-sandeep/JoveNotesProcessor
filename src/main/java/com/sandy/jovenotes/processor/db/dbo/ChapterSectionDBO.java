package com.sandy.jovenotes.processor.db.dbo;

import java.sql.ResultSet;

public class ChapterSectionDBO {

    private int     chapterId = -1 ;
    private String  section   = null ;
    private boolean selected  = false ;
    
    public ChapterSectionDBO( int chapterId, String section, boolean sel ) {
        
        this.chapterId = chapterId ;
        this.section   = section ;
        this.selected  = sel ;
    }
    
    public ChapterSectionDBO( ResultSet rs ) throws Exception {
        
        chapterId     = rs.getInt    ( "chapter_id"      ) ;
        section       = rs.getString ( "section"         ) ;
        selected      = rs.getBoolean( "selected"        ) ;
    }

    // -------------------- Bean Getter/Setter ---------------------------------
    public int getChapterId() {
        return chapterId ;
    }

    public String getSection() {
        return section ;
    }

    public boolean isSelected() {
        return selected ;
    }
}
