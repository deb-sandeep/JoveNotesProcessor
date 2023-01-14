package com.sandy.jovenotes.processor.core.notes.element;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.eclipse.xtext.nodemodel.ICompositeNode ;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils ;
import org.json.simple.JSONValue ;

import com.sandy.jovenotes.processor.core.Chapter ;
import com.sandy.jovenotes.processor.core.cards.AbstractCard ;
import com.sandy.jovenotes.processor.util.ASTReflector ;
import com.sandy.jovenotes.processor.util.JNTextProcessor ;
import com.sandy.jovenotes.processor.util.StringUtil ;
import com.sandy.xtext.joveNotes.EvalVar ;
import com.sandy.xtext.joveNotes.NotesElement ;
import com.sandy.xtext.joveNotes.Script ;

public abstract class AbstractNotesElement {
    
    private static final Logger log = Logger.getLogger( AbstractNotesElement.class ) ;
    
    private   String       type            = null ;
    private   int          difficultyLevel = -1 ;
    private   String       section         = null ;
    private   NotesElement ast             = null ;
    private   String       scriptBody      = null ;
    private   String       sourceText      = null ;
    
    protected boolean      ready           = true ;
    protected boolean      hiddenFromView  = false ;
    
    
    protected RefToContextNotesElement rtcNE = null ;
    protected Chapter chapter = null ;
    protected List<AbstractCard> cards = new ArrayList<AbstractCard>() ;
    
    private Map<String, String> evalVars = new HashMap<String, String>() ;
    
    public AbstractNotesElement( String type, Chapter chapter, 
                                 NotesElement ast, 
                                 RefToContextNotesElement rtcNE ) 
        throws Exception {
        
        this.type    = type ;
        this.chapter = chapter ;
        this.ast     = ast ;
        this.rtcNE   = rtcNE ;
        
        ICompositeNode cmpNode = NodeModelUtils.getNode( ast ) ;
        if( cmpNode != null ) {
            sourceText = cmpNode.getText() ;
        }
        
        ASTReflector reflector = new ASTReflector( ast ) ;
        
        Script script = reflector.getScript() ;
        if( script != null ) {
            
            if( script.getEvalVars() != null ) {
                for( EvalVar var : script.getEvalVars() ) {
                    if( evalVars.containsKey( var.getVarName() ) ) {
                        log.warn( "Script eval var " + var.getVarName() + 
                                  " is declared multiple times." ) ;
                    }
                    evalVars.put( var.getVarName(), var.getVarExpression() ) ;
                }
            }
            
            if( script.getScriptBody() != null ) {
                this.scriptBody = script.getScriptBody().getScript() ;
            }
        }
        
        if( reflector.getHideFromView() != null ) {
            this.hiddenFromView = true ;
        }
    }
    
    public String getRawRTCCaption() {
        if( this.rtcNE != null ) {
            return this.rtcNE.getRawRTCCaption() ;
        }
        return null ;
    }
    
    public String getType() {
        return this.type ;
    }
    
    public void setSection( String name ) {
        if( name != null ) {
            this.section = name.trim() ;
        }
    }
    
    public String getSection() {
        return this.section ;
    }
    
    public Chapter getChapter() { 
        return this.chapter ; 
    }
    
    public NotesElement getAST() {
        return this.ast ;
    }
    
    public boolean isReady() {
        return this.ready ;
    }
    
    public boolean isHiddenFromView() {
        return this.hiddenFromView ;
    }
    
    public String getScriptBody() {
        return this.scriptBody ;
    }
    
    public Map<String, String> getEvalVars() {
        return this.evalVars ;
    }
    
    public String getSourceText() {
        return this.sourceText ;
    }
    
    public final String getObjId() {
        return StringUtil.getHash( chapter.getChapterFQN() + "NE" + getType() + getObjIdSeed() ) ; 
    } ;
    
    public final int getDifficultyLevel() {
        
        if( difficultyLevel == -1 ) {
            List<AbstractCard> cards = getCards() ;
            if( !cards.isEmpty() ) {
                difficultyLevel = 0 ;
                for( AbstractCard card : cards ) {
                    difficultyLevel += card.getDifficultyLevel() ;
                }
                difficultyLevel = difficultyLevel/cards.size() ;
            }
        }
        return difficultyLevel ;
    }

    public final String getContent() {
        Map<String, Object> map = new LinkedHashMap<String, Object>() ;
        
        collectContentAttributes( map ) ;
        String content = JSONValue.toJSONString( map ) ;
        
        return content ;
    }
    
    public final String getEvalVarsAsJSON() {
        return JSONValue.toJSONString( this.evalVars ) ;
    }
    
    /**
     * This function will be called during the source object model 
     * construction immediately following the creation of the notes elements.
     * 
     * This gives a chance to the concrete notes elements to initialize 
     * themselves before they are called into active service. Initialization
     * might involve activities like:
     * 1. Transforming content text
     * 2. Creation of associated cards
     * 3. Initialization of associated cards
     * 
     * @param textProcessor The text processor instance which has the context
     *        of processing the chapter to which this notes element belongs.
     *        
     * @throws Exception
     */
    public abstract void initialize( JNTextProcessor textProcessor ) 
            throws Exception ;
    
    public List<AbstractCard> getCards() {
        // It is assumed that the concrete sublcasses will have their cards
        // initialized and added to the 'cards' array during initialization.
        return this.cards ;
    }
    
    public abstract String getObjIdSeed() ;
    
    protected abstract void collectContentAttributes( Map<String, Object> map ) ; 
}

