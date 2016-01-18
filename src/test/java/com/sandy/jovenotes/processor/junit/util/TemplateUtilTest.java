package com.sandy.jovenotes.processor.junit.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.sandy.jovenotes.processor.util.TemplateUtil;

public class TemplateUtilTest {

    @Test
    public void testRenderTemplate() {
        String template = "Hello, <name>" ;
        Map<String, Object> attrs = new HashMap<String, Object>() ;
        attrs.put( "name", "John" ) ;
        String out = TemplateUtil.renderTemplate( template, attrs ) ;
        assertEquals( "Hello, John", out ) ;
    }

    @Test
    public void testRenderClasspathTemplate() throws Exception {
        String templatePath = "com/sandy/jovenotes/processor/junit/util/sample.st" ;
        Map<String, Object> attrs = new HashMap<String, Object>() ;
        attrs.put( "name", "John" ) ;
        String out = TemplateUtil.renderClasspathTemplate( templatePath, attrs ) ;
        assertEquals( "Hello, John", out ) ;
    }
    
    public void testRenderClasspathTemplateToFile() throws Exception {
        String templatePath = "com/sandy/jovenotes/processor/junit/util/sample.st" ;
        Map<String, Object> attrs = new HashMap<String, Object>() ;
        attrs.put( "name", "John" ) ;
        
        File outfile = new File( FileUtils.getTempDirectory(), "sample.txt" ) ;
        if( outfile.exists() ) outfile.delete() ;
        TemplateUtil.renderClasspathTemplateToFile( templatePath, attrs, outfile ) ;
        
        String out = FileUtils.readFileToString( outfile ) ;
        assertEquals( "Hello, John", out ) ;
    }
}
