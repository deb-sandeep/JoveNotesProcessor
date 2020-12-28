package com.sandy.jovenotes.processor.core.notes.element;

import java.util.Map ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.QACard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.jovenotes.processor.util.StringUtil ;
import com.sandy.xtext.joveNotes.ChemCompound ;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.* ;

public class ChemCompoundElement extends AbstractNotesElement {
    
    private ChemCompound ast       = null ;
    private String       objIdSeed = null ;
    private String       symbol    = null ;
    
    public ChemCompoundElement( Chapter chapter, ChemCompound ast, 
                                RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( CHEM_COMPOUND, chapter, ast, rtcNE ) ;
        this.ast = ast ;
        this.objIdSeed = ast.getSymbol() ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        symbol = "$$\\ce{" + ast.getSymbol() + "}$$" ;
        
        if( StringUtil.isNotEmptyOrNull( ast.getChemicalName() ) ) {
            cards.add( new QACard( this, rtcNE, textProcessor, 
                    "_What is the **formulae** for_\n\n" + ast.getChemicalName(),
                    symbol, null ) ) ;
            
            cards.add( new QACard( this, rtcNE, textProcessor, 
                    "_What is the **chemical name** of_\n\n" + symbol, 
                    ast.getChemicalName(), null ) ) ;
        }
        
        if( StringUtil.isNotEmptyOrNull( ast.getCommonName() ) ) {
            cards.add( new QACard( this, rtcNE, textProcessor, 
                    "_What is the **formulae** for_\n\n" + ast.getCommonName(),
                    symbol, null ) ) ;
            
            cards.add( new QACard( this, rtcNE, textProcessor, 
                    "_What is the **common name** for_\n\n" + symbol,
                    ast.getCommonName(), null ) ) ;
            
            if( StringUtil.isNotEmptyOrNull( ast.getChemicalName() ) ) {
                
                cards.add( new QACard( this, rtcNE, textProcessor, 
                "_What is the **chemical name** of_\n\n" + ast.getCommonName(), 
                ast.getChemicalName(), null ) ) ;
                
                cards.add( new QACard( this, rtcNE, textProcessor, 
                "_What is the **common name** of_\n\n" + ast.getChemicalName(), 
                ast.getCommonName(), null ) ) ;
            }
        }
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ){
        map.put( "symbol", symbol ) ;
        map.put( "chemicalName", ast.getChemicalName() ) ;
        map.put( "commonName",   ast.getCommonName() ) ;
    }
}


