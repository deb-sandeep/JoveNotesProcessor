package com.sandy.jovenotes.processor.util;

import java.lang.reflect.Method ;

import org.eclipse.emf.ecore.EObject ;

import com.sandy.xtext.joveNotes.Script ;

public class ASTReflector {
    
    private EObject astObject = null ;
    private Class<?> astClass = null ;
    
    public ASTReflector( EObject astObject ) {
        this.astObject = astObject ;
        this.astClass  = astObject.getClass() ;
    }
    
    public Script getScript() throws Exception {
        
        Method method = this.astClass.getMethod( "getScript" ) ; 
        Object retVal = method.invoke( this.astObject ) ;
        
        return ( Script )retVal ;
    }
    
    public String getHideFromView() throws Exception {
        
        Method method = this.astClass.getMethod( "getHideFromView" ) ;
        Object retVal = method.invoke( this.astObject ) ;
        
        return ( String )retVal ;
    }
}
