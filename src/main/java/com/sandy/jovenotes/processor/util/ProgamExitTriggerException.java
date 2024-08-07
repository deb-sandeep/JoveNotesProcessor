package com.sandy.jovenotes.processor.util;

public class ProgamExitTriggerException extends Exception {
    
    public static final int EC_NORMAL                   = 0 ;
    public static final int EC_UNKNOWN                  = 1 ;
    public static final int EC_INIT_FAILURE             = 2 ;
    public static final int EC_FILE_PROCESSING_FAILURE  = 3 ;
    public static final int EC_PERSISTED_CMD_FAILURE    = 4 ;
    
    public static final ProgamExitTriggerException EX_NORMAL       = new ProgamExitTriggerException( EC_NORMAL ) ;
    public static final ProgamExitTriggerException EX_UNKNOWN      = new ProgamExitTriggerException( EC_UNKNOWN ) ;
    public static final ProgamExitTriggerException EX_INIT_FAILURE = new ProgamExitTriggerException( EC_INIT_FAILURE ) ;

    private int errorCode = 1 ;
    
    public ProgamExitTriggerException( int errorCode ) {
        this.errorCode = errorCode ;
    }
    
    public int getErrorCode() {
        return this.errorCode ;
    }
}
