package com.sandy.jovenotes.processor.core.notes;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.notes.element.AbstractNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.CharacterElement ;
import com.sandy.jovenotes.processor.core.notes.element.ChemCompoundElement ;
import com.sandy.jovenotes.processor.core.notes.element.ChemEquationElement ;
import com.sandy.jovenotes.processor.core.notes.element.DefinitionElement ;
import com.sandy.jovenotes.processor.core.notes.element.EquationElement ;
import com.sandy.jovenotes.processor.core.notes.element.EventElement ;
import com.sandy.jovenotes.processor.core.notes.element.ExerciseElement ;
import com.sandy.jovenotes.processor.core.notes.element.FIBElement ;
import com.sandy.jovenotes.processor.core.notes.element.ImageLabelElement ;
import com.sandy.jovenotes.processor.core.notes.element.MatchElement ;
import com.sandy.jovenotes.processor.core.notes.element.MultiChoiceElement ;
import com.sandy.jovenotes.processor.core.notes.element.QAElement ;
import com.sandy.jovenotes.processor.core.notes.element.RefToContextNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.SpellbeeElement ;
import com.sandy.jovenotes.processor.core.notes.element.TeacherNotesElement ;
import com.sandy.jovenotes.processor.core.notes.element.TrueFalseElement ;
import com.sandy.jovenotes.processor.core.notes.element.VoiceToTextElement ;
import com.sandy.jovenotes.processor.core.notes.element.WMElement ;
import com.sandy.xtext.joveNotes.Character ;
import com.sandy.xtext.joveNotes.ChemCompound ;
import com.sandy.xtext.joveNotes.ChemEquation ;
import com.sandy.xtext.joveNotes.Definition ;
import com.sandy.xtext.joveNotes.Equation ;
import com.sandy.xtext.joveNotes.Event ;
import com.sandy.xtext.joveNotes.Exercise ;
import com.sandy.xtext.joveNotes.ImageLabel ;
import com.sandy.xtext.joveNotes.Matching ;
import com.sandy.xtext.joveNotes.MultiChoice ;
import com.sandy.xtext.joveNotes.NotesElement ;
import com.sandy.xtext.joveNotes.QuestionAnswer ;
import com.sandy.xtext.joveNotes.RefToContext ;
import com.sandy.xtext.joveNotes.Spellbee ;
import com.sandy.xtext.joveNotes.TeacherNote ;
import com.sandy.xtext.joveNotes.TrueFalse ;
import com.sandy.xtext.joveNotes.VoiceToText ;
import com.sandy.xtext.joveNotes.WordMeaning ;

public class NoteElementBuilder {
    
    public static AbstractNotesElement build( Chapter c,  
                                              NotesElement ast, 
                                              RefToContextNotesElement rtcNE ) 
        throws Exception {
        
        AbstractNotesElement ne = null ;
        
        if( ast instanceof QuestionAnswer ){
            ne = new QAElement(c, (QuestionAnswer)ast, rtcNE );
        }
        else if( ast instanceof WordMeaning ){
            ne = new WMElement(c, (WordMeaning)ast, rtcNE );
        }
        else if( ast instanceof Definition ){
            ne = new DefinitionElement(c, (Definition)ast, rtcNE );
        }
        else if( ast instanceof TeacherNote ){
            ne = new TeacherNotesElement(c, (TeacherNote)ast, rtcNE );
        }
        else if( ast instanceof Character ){
            ne = new CharacterElement(c, (Character)ast, rtcNE );
        }
        else if( ast instanceof Event ){
            ne = new EventElement(c, (Event)ast, rtcNE );
        }
        else if( ast instanceof com.sandy.xtext.joveNotes.FIB ){
            ne = new FIBElement(c, (com.sandy.xtext.joveNotes.FIB)ast, rtcNE);
        }
        else if( ast instanceof Matching ){
            ne = new MatchElement(c, (Matching)ast, rtcNE );
        }
        else if( ast instanceof TrueFalse ){
            ne = new TrueFalseElement(c, (TrueFalse)ast, rtcNE );
        }
        else if( ast instanceof Spellbee ){
            ne = new SpellbeeElement(c, (Spellbee)ast, rtcNE );
        }
        else if( ast instanceof ImageLabel ){
            ne = new ImageLabelElement(c, (ImageLabel)ast, rtcNE );
        }
        else if( ast instanceof ChemCompound ){
            ne = new ChemCompoundElement(c, (ChemCompound)ast, rtcNE );
        }
        else if( ast instanceof Equation ){
            ne = new EquationElement(c, (Equation)ast, rtcNE );
        }
        else if( ast instanceof ChemEquation ){
            ne = new ChemEquationElement(c, (ChemEquation)ast, rtcNE );
        }
        else if( ast instanceof RefToContext ){
            ne = new RefToContextNotesElement(c, (RefToContext)ast );
        }
        else if( ast instanceof MultiChoice ){
            ne = new MultiChoiceElement(c, (MultiChoice)ast, rtcNE );
        }
        else if( ast instanceof Exercise ) {
            ne = new ExerciseElement(c, (Exercise)ast, rtcNE ) ;
        }
        else if( ast instanceof VoiceToText ) {
            ne = new VoiceToTextElement( c, (VoiceToText)ast, rtcNE ) ;
        }
        
        return ne ;
    }
}
       
        
