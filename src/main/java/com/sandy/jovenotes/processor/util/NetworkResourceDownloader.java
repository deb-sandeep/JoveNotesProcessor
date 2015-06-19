package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

public class NetworkResourceDownloader {

	private static final Logger log = Logger.getLogger( NetworkResourceDownloader.class ) ; 
			
    private URI                 uri = null ;
    private int                 statusCode = 0 ;
    private String              reasonPhrase = null ;
    private HttpResponse        response = null ;
    private CloseableHttpClient httpClient = null ;
    
    public NetworkResourceDownloader( String url ) throws Exception {

        this.uri = new URI( url ) ;
        this.httpClient = HttpClients.createDefault() ;
    }
    
    public int execute() throws Exception {
        
    	log.warn( "\t\t\tDownloading URL - " + uri ) ;
        response     = httpClient.execute( new HttpGet( uri ) ) ;
        statusCode   = response.getStatusLine().getStatusCode() ;
        reasonPhrase = response.getStatusLine().getReasonPhrase() ;
        return statusCode ;
    }
    
    public int getStatusCode() {
        return this.statusCode ;
    }
    
    public String getReasonPhrase() {
        return this.reasonPhrase ;
    }
    
    public void saveResponseToFile( File outputFile ) 
        throws Exception {
        
        HttpEntity       entity = response.getEntity() ;
        InputStream      rIn    = entity.getContent() ;
        FileOutputStream fOut   = new FileOutputStream( outputFile ) ;
        
        IOUtils.copy( rIn, fOut ) ;
        fOut.close() ; 
        httpClient.close() ;
    }
    
    public String getResponseAsString() 
        throws Exception {

        HttpEntity  entity = response.getEntity() ;
        InputStream rIn    = entity.getContent() ;
        String      content = IOUtils.toString( rIn ) ;
        
        httpClient.close() ;
        
        return content ;
    }
}
