package com.sandy.jovenotes.processor.util ;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {
	
    private FileUtil() { super() ; }

    public static List<File> getFilesList( final File baseDir, final String extn,
                                           final boolean recursive ) {
        final List<File> result = new ArrayList<File>() ;
        final File files[] = baseDir.listFiles() ;

        if ( files != null ) {
            for ( int index = 0; index < files.length; index++ ) {
                if ( files[index].isDirectory() ) {
                    if ( recursive ) {
                        result.addAll( FileUtil.getFilesList( files[index],
                                                              extn,
                                                              recursive ) ) ;
                    }
                }
                else if ( files[index].getName().endsWith( extn ) ) {
                    result.add( files[index] ) ;
                }
            }
        }
        return result ;
    }
}
