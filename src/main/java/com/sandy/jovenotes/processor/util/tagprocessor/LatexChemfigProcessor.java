package com.sandy.jovenotes.processor.util.tagprocessor;

import com.sandy.jovenotes.processor.JoveNotesProcessor;
import com.sandy.jovenotes.processor.db.dao.ChapterSectionDAO;
import com.sandy.jovenotes.processor.util.CommandLineExec;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class LatexChemfigProcessor {
    
    private static final Logger log = Logger.getLogger( ChapterSectionDAO.class ) ;
    private static final String I1 = "  "  ;
    private static final String I2 = I1 + "  "  ;
    private static final String I3 = I2 + "  "  ;
    
    private static final String LATEX_TEMPLATE =
        "\\documentclass[preview]{standalone}\n" +
        "\\usepackage{chemfig}\n" +
        "\n" +
        "\\standaloneconfig{border=5px}\n" +
        "\n" +
        "\\renewcommand * \\printatom[1]{\\Large\\ensuremath{\\mathrm{#1}}}\n" +
        "\n" +
        "\\begin{document}\n" +
        "\\chemfig{OPTIONS}{\n" +
        "{SNIPPET}\n" +
        "}\n" +
        "\\end{document}\n" ;
    
    private final File workingDir ;
    private final File latexFile ;
    private final File pdfFile ;
    private final File imgFile ;
    
    public LatexChemfigProcessor() throws Exception {
        this.workingDir = getWorkingDir() ;
        this.latexFile  = new File( this.workingDir, "cf.tex" ) ;
        this.pdfFile    = new File( this.workingDir, "cf.pdf" ) ;
        this.imgFile    = new File( this.workingDir, "cf.png" ) ;
    }
    
    public void processSnippet( String chemfigSnippet, File targetImgFile ) throws Exception {
        
        log.debug( "Processing chemfig snippet:" ) ;
        log.debug( chemfigSnippet ) ;
        
        generateLatexFile( chemfigSnippet ) ;
        generatePDFFile() ;
        generateImgFile() ;
        
        log.debug( I1 + "Copying image to target file" ) ;
        FileUtils.copyFile( imgFile, targetImgFile ) ;
    }
    
    // Returns an empty working directory in which latex file can
    // be processed.
    private File getWorkingDir() throws Exception {
        
        File wkspDir = JoveNotesProcessor.config.getWorkspaceDir() ;
        File workingDir = new File( wkspDir, "chemfig" ) ;

        FileUtils.deleteDirectory( workingDir ) ;
        workingDir.mkdirs() ;
        
        return workingDir ;
    }
    
    private void generateLatexFile( String chemfigSnippet ) throws Exception {
        
        String options = "" ;
        String moleculeDef = "" ;
        
        if( chemfigSnippet.startsWith( "[[" ) ) {
            int optionEndIndex = chemfigSnippet.indexOf( "]]" ) ;
            if( optionEndIndex == -1 ) {
                throw new Exception( "Chemfig options section not closed." ) ;
            }
            else {
                options = "[" + chemfigSnippet.substring( 2, optionEndIndex ).trim() + "]" ;
                moleculeDef = chemfigSnippet.substring( optionEndIndex+2 ).trim() ;
            }
        }
        else {
            moleculeDef = chemfigSnippet.trim() ;
        }
        
        log.debug( I1 + "Generating Latex file from snippet" ) ;
        String fileContent = LATEX_TEMPLATE.replace( "{SNIPPET}", moleculeDef )
                                           .replace( "{OPTIONS}", options ) ;
        FileUtils.writeStringToFile( latexFile, fileContent ) ;
    }
    
    private void generatePDFFile() throws Exception {
        
        log.debug( I1 + "Generating PDF from Latex file" ) ;
        ArrayList<String> cmdParts = new ArrayList<>();
        cmdParts.add( "pdflatex" ) ;
        cmdParts.add( "-file-line-error" ) ;
        cmdParts.add( "-halt-on-error" ) ;
        cmdParts.add( "-output-format=pdf" ) ;
        cmdParts.add( "cf.tex" ) ;
        
        executeCmd( cmdParts ) ;
    }
    
    private void generateImgFile() throws Exception {
        
        log.debug( I1 + "Generating PDF to PNG file" ) ;
        ArrayList<String> cmdParts = new ArrayList<>() ;
        cmdParts.add( "convert" ) ;
        cmdParts.add( "-verbose" ) ;
        cmdParts.add( "-density" ) ;
        cmdParts.add( "82" ) ;
        cmdParts.add( "cf.pdf" ) ;
        cmdParts.add( "cf.png" ) ;
        
        executeCmd( cmdParts ) ;
    }
    
    private void executeCmd( ArrayList<String> cmdParts ) throws Exception {
        
        String[] cmd = cmdParts.toArray( new String[2] );
        ArrayList<String> outputBuffer = new ArrayList<>() ;
        if( CommandLineExec.executeCommand( cmd, outputBuffer, workingDir ) != 0 ) {
            StringBuilder output = new StringBuilder();
            for( String str : outputBuffer ) {
                output.append( str ).append( "\n" ) ;
            }
            throw new Exception( "Could not execute command. " + Arrays.toString( cmd ) +
                                 "Output:\n" + output ) ;
        }
    }
}
