package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.stringtemplate.v4.ST;

/**
 * A utility class to act as a utility wrapper for templating using String 
 * Templates.
 * 
 * @author Vivek Kant
 *
 */
public class TemplateUtil {
    
    public static String renderTemplate( String template, Map<String, Object> attrs ) {
            
        ST st = new ST( template ) ;
        for( String key : attrs.keySet() ) {
            st.add( key, attrs.get( key ) ) ;
        }
        
        return st.render() ;
    }
    
    public static String renderClasspathTemplate( String templatePath, 
                                                  Map<String, Object> attrs )
                                                  throws Exception {
        
        ClassLoader cl = TemplateUtil.class.getClassLoader() ;
        URL templateUrl = cl.getResource( templatePath ) ;
        String template = IOUtils.toString( templateUrl ) ;
        return renderTemplate( template , attrs ) ;
        
    }
    
    public static void renderClasspathTemplateToFile( String templatePath,
                                                      Map<String, Object> attrs,
                                                      File dest )
                                                      throws Exception {
        ClassLoader cl = TemplateUtil.class.getClassLoader() ;
        String template = IOUtils.toString( cl.getResourceAsStream( templatePath ) ) ;
        String out = renderTemplate( template , attrs ) ;
        FileUtils.writeStringToFile( dest, out ) ;
    }

}
