package com.sandy.jovenotes.processor.jnsrc;

import com.sandy.xtext.joveNotes.ProcessingHints;
import com.sandy.xtext.joveNotes.JoveNotes ;

public class JoveNotesASTWrapper {

	private JoveNotes ast = null ;
	
	public JoveNotesASTWrapper( JoveNotes jnAST ) {
		this.ast = jnAST ;
	}
	
	public boolean shouldSkipProcessing() {
		ProcessingHints hints = ast.getProcessingHints() ;
		return ( hints != null ) && ( hints.getSkipGeneration() != null ) ;
	}
	
	public boolean isTestPaper() {
		return ast.getChapterDetails().getTestPaper() != null ;
	}
	
	public String getSubjectName() {
		return ast.getChapterDetails().getSubjectName() ;
	}
	
	public int getChapterNumber() {
		return ast.getChapterDetails().getChapterNumber() ;
	}
	
	public int getSubChapterNumber() {
		return ast.getChapterDetails().getSubChapterNumber() ;
	}
	
	public String getChapterName() {
		return ast.getChapterDetails().getChapterName() ;
	}
}
