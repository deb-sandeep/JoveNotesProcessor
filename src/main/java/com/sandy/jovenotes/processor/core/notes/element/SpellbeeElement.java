package com.sandy.jovenotes.processor.core.notes.element;

import static com.sandy.jovenotes.processor.core.notes.NoteElementType.SPELLBEE ;

import java.util.Map ;

import com.sandy.jovenotes.processor.JoveNotes ;
import com.sandy.jovenotes.processor.async.SpellbeeCmd ;
import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.SpellbeeCard ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.xtext.joveNotes.Spellbee ;

public class SpellbeeElement extends AbstractNotesElement {
    
    private String word = null ;
    private String objIdSeed = null ;
    private Chapter chapter = null ;
    
    public SpellbeeElement( Chapter chapter, Spellbee ast, 
                            RefToContextNotesElement rtcNE )  
            throws Exception {
        
        super( SPELLBEE, chapter, ast, rtcNE ) ;
        this.chapter = chapter ;
        this.word = ast.getWord() ;
        this.objIdSeed = this.word ;
        super.ready = false ;
    }
    
    public void initialize( JNTextProcessor textProcessor ) 
            throws Exception {
        
        SpellbeeCard card = new SpellbeeCard( this, rtcNE, objIdSeed ) ;
        
        SpellbeeCmd  cmd = new SpellbeeCmd( chapter, word, 
                                            card.getDifficultyLevel(), 
                                            super.getObjId(), 
                                            card.getObjId() ) ;
        
        JoveNotes.persistentQueue.add( cmd ) ;
        cards.add( card ) ;
    }
    
    public String getObjIdSeed() { return objIdSeed ; }
    
    public void collectContentAttributes( Map<String, Object> map ){}
}


