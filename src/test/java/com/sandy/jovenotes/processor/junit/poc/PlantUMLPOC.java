package com.sandy.jovenotes.processor.junit.poc;

import java.io.File ;

import org.apache.log4j.Logger ;

import net.sourceforge.plantuml.SourceStringReader ;

public class PlantUMLPOC {
    
    private static Logger logger = Logger.getLogger( PlantUMLPOC.class ) ;
    
    public static void main( String[] args ) throws Exception {
        String source = "" ;
        source += "@startuml \n" ;
        source += "(*) --> if Some Test then \n" ;
        source += "-->[true] activity 1 \n" ;
        source += "if  then \n" ;
        source += "-> \"activity 3\" as a3 \n" ;
        source += "else \n" ;
        source += "if \"Other test\" then \n" ;
        source += "-left-> activity 5 \n" ;
        source += "else \n" ;
        source += "--> activity 6 \n" ;
        source += "endif \n" ;
        source += "endif \n" ;
        source += "else \n" ;
        source += "->[false] activity 2 \n" ;
        source += "endif \n" ;
        source += "a3 --> if \"last test\" then \n" ;
        source += "--> activity 7 \n" ;
        source += "else \n" ;
        source += "-> activity 8 \n" ;
        source += "endif \n" ;
        source += "@enduml \n" ;
        
        SourceStringReader reader = new SourceStringReader( source ) ;
        logger.info( "Generating UML diagram" );
        reader.generateImage( new File( "/home/sandeep/temp/plantuml.png" ) ) ;
        logger.info( "UML diagram generated" );
    }
}
 