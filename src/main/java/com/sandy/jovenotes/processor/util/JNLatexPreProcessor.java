package com.sandy.jovenotes.processor.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  In .jn source files, {{@math|imath|chem|ichem ... }} content is embedded inside XText string
 *  literals. XText's STRINGValueConverter processes escape sequences identically to
 *  Java: \\ → \. This forces users to write \\eta, \\frac, \\times, etc. for every LaTeX
 *  command — doubling every backslash is inconvenient and unnatural.
 *  <p></p>
 *  The fix is a pre-processing step that reads the raw .jn file before XText sees it,
 *  finds {{@math|imath|chem|ichem ... }} blocks, and doubles any lone (single) backslash
 *  within them. This makes \eta in the source behave identically to \\eta. Existing \\
 *  pairs are left untouched, so old-style notes continue to work and a mix of old/new
 *  style in the same file is safe.
 */
public class JNLatexPreProcessor {

    private static final Pattern LATEX_BLOCK_PATTERN =
        Pattern.compile( "\\{\\{@(math|imath|chem|ichem)\\s+(.*?)\\}\\}", Pattern.DOTALL ) ;

    // Matches a lone \ — not preceded and not followed by another \
    private static final String LONE_BACKSLASH = "(?<!\\\\)\\\\(?!\\\\)" ;

    public static String process( String text ) {

        Matcher       m       = LATEX_BLOCK_PATTERN.matcher( text ) ;
        StringBuilder sb      = new StringBuilder() ;
        boolean       changed = false ;

        while( m.find() ) {
            String markerType  = m.group( 1 ) ;
            String original    = m.group( 2 ) ;
            String transformed = original.replaceAll( LONE_BACKSLASH, "\\\\\\\\" ) ;
            if( !transformed.equals( original ) ) changed = true ;
            m.appendReplacement( sb, "{{@" + markerType + " " +
                                     Matcher.quoteReplacement( transformed ) + "}}" ) ;
        }
        m.appendTail( sb ) ;

        return changed ? sb.toString() : text ;
    }
}
