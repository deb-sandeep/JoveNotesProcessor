package com.sandy.jovenotes.processor.junit.util;

import static org.junit.Assert.* ;

import java.io.InputStream ;

import org.apache.commons.configuration.PropertiesConfiguration ;
import org.eclipse.xtext.util.StringInputStream ;
import org.junit.Test ;

public class PropertiesConfigLoadTest {

    @Test
    public void propertyOverride() throws Exception {
        
        String prop1 = "keyA=value1" ;
        String prop2 = "keyB=value2" ;
        String prop3 = "keyC=value3" ;
        
        PropertiesConfiguration config = new PropertiesConfiguration() ;
        load( config, prop1 ) ;
        load( config, prop2 ) ;
        load( config, prop3 ) ;
        
        assertEquals( "value3", config.getString( "keyC" ) );
    }
    
    private void load( PropertiesConfiguration config, String propResource ) 
        throws Exception {
        
        InputStream is = new StringInputStream( propResource ) ;
        config.load( is ) ;
    }
}
