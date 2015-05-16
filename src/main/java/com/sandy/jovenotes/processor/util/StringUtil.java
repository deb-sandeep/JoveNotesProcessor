package com.sandy.jovenotes.processor.util ;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utility methods on String.
 * 
 * @author Sandeep
 */
public final class StringUtil {

    private StringUtil(){}

    public static boolean isEmptyOrNull( final String str ) {
        if ( str == null || "".equals( str.trim() ) ) {
            return true ;
        }
        return false ;
    }

    public static boolean isNotEmptyOrNull( final String str ) {
        return !isEmptyOrNull( str ) ;
    }
    
    /** Returns the hex encoded MD5 hash of the given string. */
    public String getSerialVersionUID( String input ) {
    	
        byte[] hash = DigestUtils.md5( input ) ;
        byte[] newHash = new byte[hash.length/2] ;
        
        for( int i=0; i<hash.length; i+=2 ) {
            newHash[i/2] = hash[i] ;
        }
        return new String( Hex.encodeHex( newHash ) ) ;
    }
}