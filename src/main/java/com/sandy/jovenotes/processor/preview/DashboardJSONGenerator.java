/**
 * 
 */
package com.sandy.jovenotes.processor.preview;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.processor.dao.ChapterDBO;
import com.sandy.jovenotes.processor.util.TemplateUtil;

/**
 * This class is responsible for generating the Dashboard JSON file
 * 
 * @author Vivek Kant
 *
 */
public class DashboardJSONGenerator {

    private static final Logger log = Logger.getLogger( DashboardJSONGenerator.class ) ;
    
    private static final String JSON_FILE_SUFFIX = ".json" ;
    private static final String DASHBOARD_TEMPLATE = "ST/dashboard.st" ;
    
    List<ChapterDBO> chapters = null ;

    public DashboardJSONGenerator() throws Exception {
        chapters = ChapterDBO.getAll() ;
    }
    
    public void generate( File baseDir ) throws Exception {
        
        log.info( "Generating dashboard JSON " );
        File dest = new File( baseDir, "dashboard" + JSON_FILE_SUFFIX ) ;
        
        TemplateUtil.renderClasspathTemplateToFile( DASHBOARD_TEMPLATE, 
                                                    getDashboardAttrs(), 
                                                    dest );
    }

    private Map<String, Object> getDashboardAttrs() throws Exception {
        
        Map<String, Object> attrs = new HashMap<String, Object>() ;
        Map<String, Syllabus> syllabuses = new HashMap<String, Syllabus>() ;
        
        for( ChapterDBO chapter : chapters ) {
            String syllabusName = chapter.getSyllabusName() ;
            String subjectName = chapter.getSubjectName() ;
            
            Syllabus syllabus = syllabuses.get( syllabusName ) ;
            if ( syllabus == null ) {
                syllabus = new Syllabus() ;
                syllabus.setSyllabusName( syllabusName ) ;
                syllabuses.put( syllabusName, syllabus ) ;
            }
            
            Subject subject = syllabus.getSubjects().get( subjectName ) ;
            if ( subject == null ) {
                subject = new Subject() ;
                subject.setSubjectName( subjectName ) ;
                syllabus.getSubjects().put( subjectName, subject ) ;
            }
            
            subject.chapters.add( chapter ) ;
            
        }
        
        attrs.put( "syllabuses", new ArrayList<Syllabus>( syllabuses.values() ) ) ;
        
        return attrs;
    }
    
    public class Subject {
        
        String subjectName ;
        List<ChapterDBO> chapters = new ArrayList<ChapterDBO>();
        
        public String getSubjectName() {
            return subjectName;
        }
        
        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }
        
        public List<ChapterDBO> getChapters() {
            return chapters;
        }
        
        public void setChapters(List<ChapterDBO> chapters) {
            this.chapters = chapters;
        }
        
    }
    
    public class Syllabus {
        
        String syllabusName ;
        Map<String, Subject> subjects = new HashMap<String, Subject>() ;
        
        public String getSyllabusName() {
            return syllabusName;
        }
        
        public Collection<Subject> getSubjectList() {
            return subjects.values() ;
        }
        
        public void setSyllabusName(String syllabusName) {
            this.syllabusName = syllabusName;
        }
        
        public Map<String, Subject> getSubjects() {
            return subjects;
        }
        
        public void setSubjects(Map<String, Subject> subjects) {
            this.subjects = subjects;
        }
        
    }
}
