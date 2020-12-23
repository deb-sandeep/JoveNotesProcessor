package com.sandy.jovenotes.processor;

import java.io.File ;
import java.nio.file.PathMatcher ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;

import com.sandy.jovenotes.processor.util.ConfigManager ;

public class SourceFileFinder {
    
    private static final Logger log = Logger.getLogger( SourceFileFinder.class ) ;

    private ConfigManager config = null ;
    private SourceProcessingJournal journal = null ;

    private List<PathMatcher> includePathMatchers = null ;
    private List<PathMatcher> excludePathMatchers = null ;

    public SourceFileFinder( ConfigManager config, SourceProcessingJournal journal ) {
        
        this.config = config ;
        this.journal = journal ;
        this.includePathMatchers = config.getIncludePathMatchers() ;
        this.excludePathMatchers = config.getExcludePathMatchers() ;
    }
    
    
    public List<File> getFilesForProcessing( File srcDir ) throws Exception {
        
        List<File> filesForProcessing = new ArrayList<File>() ;
            
        Collection<File> allFiles = FileUtils.listFiles( srcDir, 
                                                new String[]{"jn"}, true ) ;
        for( File file : allFiles ) {
            
            if( !shouldConsiderFile( file ) ) continue ;
            
            if( config.isForceProcessAllFiles() ) {
                filesForProcessing.add( file ) ;
                log.info( "  Selecting file - " + file.getAbsolutePath() ) ;
            }
            else if( journal.hasFileChanged( file ) ) {
                filesForProcessing.add( file ) ;
                log.info( "  Selecting file - " + file.getAbsolutePath() ) ;
            }
            else {
                log.debug( "  Ignoring file - " + file.getAbsolutePath() ) ;
            }
        }
        
        log.info("\n") ;
        
        return filesForProcessing ;
    }

    private boolean shouldConsiderFile( File file ) {
        
        // By default a file is included.
        boolean shouldConsider = true ;
        
        // We check the exclude patterns first. If there are exclude patterns
        // and if one of them matches the file, the file is excluded.
        if( !excludePathMatchers.isEmpty() ) {
            for( PathMatcher matcher : excludePathMatchers ) {
                if( matcher.matches( file.toPath() ) ) {
                    //log.debug( "Rejecting file as it matches exclusion filter. " + 
                    //           matcher.toString() );
                    shouldConsider = false ;
                    break ;
                }
            }
        }
        
        // If the file has not been already excluded and we have include include
        // matchers, we see if the file matches any of the matchers.
        if( !includePathMatchers.isEmpty() && shouldConsider ) {
            boolean includePatternMatch = false ;
            for( PathMatcher matcher : includePathMatchers ) {
                if( matcher.matches( file.toPath() ) ) {
                    shouldConsider = true ;
                    includePatternMatch = true ;
                    break ;
                }
            }
            
            // If we have include matchers, but none of them match the file,
            // it should be excluded.
            if( !includePatternMatch ) {
                //log.debug( "Rejecting file as it does not match any inclusion filter." ) ;
                shouldConsider = false ;
            }
        }
        
        return shouldConsider ;
    }
}
