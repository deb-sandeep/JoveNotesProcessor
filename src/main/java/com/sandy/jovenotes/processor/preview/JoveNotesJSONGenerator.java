/**
 * 
 */
package com.sandy.jovenotes.processor.preview;

import java.io.File;
import java.util.List;

import com.sandy.jovenotes.processor.JoveNotes;
import com.sandy.jovenotes.processor.util.ConfigManager;

/**
 * As part of preview processor this class is responsible for generation of
 * various JSON files required to run the preview.
 * 
 * @author Vivek Kant
 *
 */
public class JoveNotesJSONGenerator {
    
    public static final String JSON_ROOT_DIR = "json" ;
    public static final String JSON_CHAPTER_DIR = "chapter" ;
    
    public JoveNotesJSONGenerator() {
    }
    
    public void generateJSON( List<Integer> updatedChapters ) throws Exception {
        for ( int chapterId : updatedChapters ) {
            if ( chapterId != -1 ) {
                ChapterJSONGenerator chapJSONGen = 
                        new ChapterJSONGenerator(chapterId ) ;
                chapJSONGen.generate( getJSONChapterDir() ) ;                
            }
        }
        
        DashboardJSONGenerator dashJSONGen = new DashboardJSONGenerator() ;
        dashJSONGen.generate( getJSONRootDir() );
    }
    
    private File getJSONRootDir() {
        File wwwroot = JoveNotes.config.getLocalWebserverPath() ;
        File jsonRootDir = new File( wwwroot, JSON_ROOT_DIR ) ;
        if ( !jsonRootDir.exists() ) {
            jsonRootDir.mkdirs() ;
        }
        return jsonRootDir ;
    }
    
    private File getJSONChapterDir() {
        File jsonRootDir = getJSONRootDir() ;
        
        File jsonChapterDir = new File( jsonRootDir, JSON_CHAPTER_DIR ) ;
        if ( !jsonChapterDir.exists() ) {
            jsonChapterDir.mkdirs() ;
        }
        return jsonChapterDir ;
    }
}
