package com.sandy.jovenotes.processor.junit.util;

import org.junit.Test;

import com.sandy.jovenotes.processor.util.RunMode;

public class RunModeTest {

    @Test(expected=Exception.class)
    public void testInvalidMode() throws Exception {
            new RunMode( "invalidmode" ) ;
    }
    
    @Test
    public void testSingleRunMode() throws Exception {
        new RunMode( "development" ) ;
    }
    
    @Test
    public void testCompatibleRunMode() throws Exception {
        new RunMode( "development, preview" ) ;
    }
    
    @Test
    public void testDefaultRunMode() throws Exception {
        new RunMode( null ) ;
        new RunMode( "" ) ;
    }
    
    @Test(expected=Exception.class)
    public void testIncompatibleRunMode() throws Exception {
        new RunMode( "development,production" ) ;
    }
    
}
