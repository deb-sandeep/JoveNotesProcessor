package com.sandy.jovenotes.processor.preview;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.dao.ChapterDBO;
import com.sandy.jovenotes.processor.dao.NotesElementDBO;
import com.sandy.jovenotes.processor.util.TemplateUtil;

/**
 * This class is responsible for generating the JSON file for a Chapter
 * 
 * @author Vivek Kant
 */
public class ChapterJSONGenerator {
    
    private static final Logger log = Logger.getLogger( ChapterJSONGenerator.class ) ;
    
    private static final String JSON_FILE_SUFFIX = ".json" ;
    private static final String CHAPTER_TEMPLATE = "ST/chapter.st" ;
    
    private ChapterDBO chapter = null ;
    
    public ChapterJSONGenerator( int chapterId ) throws Exception {        
        chapter = ChapterDBO.get( chapterId ) ;
    }
    
    public void generate( File baseDir ) throws Exception {
        
        log.info( "Generating chapter JSON with id " + chapter.getChapterId() );
        File dest = new File( baseDir, chapter.getChapterId() + JSON_FILE_SUFFIX ) ;
        
        TemplateUtil.renderClasspathTemplateToFile( CHAPTER_TEMPLATE, 
                                                    getChapterAttrs(), 
                                                    dest );
    }
    
    public Map<String, Object> getChapterAttrs() throws Exception {
        Map<String, Object> attrs = new HashMap<String, Object>() ;
        attrs.put( "chapterDetails", getChapterDetailAttrs() ) ;
        attrs.put( "notesElements", getNotestElementList() ) ;
        
        return attrs ;
    }

    private Map<String, Object> getChapterDetailAttrs() {
        Map<String, Object> chapterDetails = new HashMap<String, Object>();
        
        chapterDetails.put( "chapterId", chapter.getChapterId() ) ;
        chapterDetails.put( "syllabusName", chapter.getSyllabusName() ) ;
        chapterDetails.put( "subjectName", chapter.getSubjectName() ) ;
        chapterDetails.put( "chapterNum", chapter.getChapterNum() ) ;
        chapterDetails.put( "subChapterNum", chapter.getSubChapterNum() ) ;
        String scriptBody = ( chapter.getScriptBody() != null ) ?
                                chapter.getScriptBody().replace( "\n", "\\n" ) :
                                null ;
        chapterDetails.put( "scriptBody", scriptBody ) ;
        
        return chapterDetails ;
    }
    
    private List<Map<String, Object>> getNotestElementList() throws Exception {
        List<Map<String, Object>> elementList = new ArrayList<Map<String, Object>>() ;
        
        for ( NotesElementDBO ne : chapter.getNotesElements() ) {
            Map<String, Object> neMap = new HashMap<String, Object>() ;
            
            neMap.put( "notesElementId", ne.getNotesElementId() ) ;
            neMap.put( "elementType", ne.getElementType() ) ;
            neMap.put( "difficultyLevel", ne.getDifficultyLevel() ) ;
            neMap.put( "evalVars", ne.getEvalVars() ) ;
            neMap.put( "scriptBody", ne.getScriptBody() ) ;
            
            String content = ( ne.getContent() != null ) ? 
                                    ne.getContent().trim() :
                                    "";
            content = StringUtils.stripStart( content, "{" ) ;
            content = StringUtils.stripEnd( content, "}" ) ;
            neMap.put( "content", content ) ;
            
            elementList.add( neMap ) ;
        }
        
        return elementList ;
    }

}
