package com.sandy.jovenotes.processor.util ;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utility methods on String.
 * 
 * @author Sandeep
 */
public final class StringUtil {

    public static boolean isEmptyOrNull( final String str ) {
        return ( str == null || "".equals( str.trim() ) ) ;
    }

    public static boolean isNotEmptyOrNull( final String str ) {
        return !isEmptyOrNull( str ) ;
    }
    
    public static String getHash( String input ) {
        return new String( Hex.encodeHex( DigestUtils.md5( input ) ) ) ;
    }
}