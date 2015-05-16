package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

/**
 * @author Sandeep
 */
public class XTextModelParser {

    private static final String DEF_EMF_REG_METHOD = "createInjectorAndDoEMFRegistration" ;

    private static Logger logger = Logger.getLogger( XTextModelParser.class ) ;
    
    public XTextModelParser( String bootstrapClassName )
    	throws Exception {
    	this( bootstrapClassName, DEF_EMF_REG_METHOD ) ;
    }
    
    public XTextModelParser( String bootstrapClassName, 
    		                 String bootstrapMethodName )
    	throws Exception { 
    	
    	logger.debug( "Registering EMF" ) ;
    	
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader() ; 
        Class<?> cls = classLoader.loadClass( bootstrapClassName ) ;
        Method   mth = cls.getMethod( bootstrapMethodName ) ;
        Object   obj = cls.newInstance() ;
        
        mth.invoke( obj ) ;
        logger.debug( "EMF registered" ) ;
    }
    
    public EObject parseFile( File file ) 
        throws Exception {
    	
    	return parseURL( file.toURI().toURL() ) ;
    }
    
    public EObject parseURL( URL url ) throws Exception {
    	
    	logger.debug( "Parsing url - " + url.toURI().toString() ) ;
    	
        ResourceSet rs       = new ResourceSetImpl();
        URI         emfURI   = URI.createURI( url.toURI().toString() ) ;
        Resource    resource = rs.getResource( emfURI, true ) ;
        EObject     eObj     = resource.getContents().get( 0 ) ;

        return eObj ;
    }
}
