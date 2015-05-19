package com.sandy.jovenotes.processor.junit.poc;

import java.util.zip.CRC32;

import org.apache.log4j.Logger;

public class ChecksumPOC {

	private static final Logger log = Logger.getLogger( ChecksumPOC.class ) ;
	
	public static void main( String[] args ) {
		
		String str1 = "A small string" ;
		String str2 = "A small string" ;
		String str3 = "A small string." ;
		
		log.debug( "CRC of '" + str1 + "' is " + getChecksum( str1 ) ) ;
		log.debug( "CRC of '" + str2 + "' is " + getChecksum( str2 ) ) ;
		log.debug( "CRC of '" + str3 + "' is " + getChecksum( str3 ) ) ;
	}
	
	private static long getChecksum( String input ) {
		CRC32 crc = new CRC32() ;
		crc.update( input.getBytes() ) ;
		return crc.getValue() ;
	}
}
