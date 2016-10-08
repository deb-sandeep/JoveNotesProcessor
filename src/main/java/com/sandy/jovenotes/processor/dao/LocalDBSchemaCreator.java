package com.sandy.jovenotes.processor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.sandy.jovenotes.processor.JoveNotes;

/**
 * Utility class to create schema in the local database
 * 
 * @author Vivek Kant
 */
public class LocalDBSchemaCreator extends AbstractDBO {
    
    public void checkAndCreate() throws Exception {
        
        if ( JoveNotes.db == null ) {
            throw new Exception( "The database is not initialized" ) ;
        }
        
        checkAndCreateSchema() ;
        checkAndCreateChapterTable() ;
        checkAndCreateCardTable() ;
        checkAndCreateNotesElementTable() ;
        checkAndCreatePersistentQueueTable() ;
    }
    
    private void checkAndCreateSchema() throws Exception {
        
        final String checkSql =  "SELECT SCHEMA_NAME " + 
                            "FROM INFORMATION_SCHEMA.SCHEMATA " +
                            "WHERE SCHEMA_NAME = 'JOVE_NOTES'" ;
        
        final String createSql = "CREATE SCHEMA JOVE_NOTES" ;
        
        checkAndCreate( checkSql, createSql ) ;
        
    }
    
    private void checkAndCreateChapterTable() throws Exception {
        
        final String checkSql =  
            "SELECT TABLE_NAME " + 
            "FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_NAME = 'CHAPTER'" ;
        
        final String createSql =  
            "CREATE TABLE jove_notes.chapter ( " +
                    "chapter_id      INTEGER IDENTITY NOT NULL, " +
                    "is_exercise_bank   BIT(1) DEFAULT B'0' NOT NULL," +
                    "is_test_paper   BIT(1) DEFAULT B'0' NOT NULL," +
                    "syllabus_name   VARCHAR(256) NOT NULL," +
                    "subject_name    VARCHAR(45) NOT NULL," +
                    "chapter_num     INTEGER NOT NULL," +
                    "sub_chapter_num INTEGER NOT NULL," +
                    "chapter_name    VARCHAR(65535) NOT NULL," +
                    "script_body     VARCHAR(65535)," +
                    "num_cards       INTEGER," +
                    "num_VE          INTEGER," +
                    "num_E           INTEGER," +
                    "num_M           INTEGER," +
                    "num_H           INTEGER," +
                    "num_VH          INTEGER" +
               ")" ;
        
       checkAndCreate( checkSql, createSql ) ;
        
    }
    
    private void checkAndCreateCardTable() throws Exception {
        
        final String checkSql =  
            "SELECT TABLE_NAME " + 
            "FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_NAME = 'CARD'" ;
        
        final String createSql =  
            "CREATE TABLE jove_notes.card (" +
                    "card_id          INTEGER IDENTITY NOT NULL," +
                    "notes_element_id INTEGER NOT NULL," +
                    "chapter_id       INTEGER NOT NULL," +
                    "card_type        VARCHAR(45) NOT NULL," +
                    "difficulty_level INTEGER NOT NULL," +
                    "content          VARCHAR(65535) NOT NULL," +
                    "obj_correl_id    VARCHAR(45) NOT NULL," +
                    "ready            BIT(1) DEFAULT B'1' NOT NULL" +
            ")" ;
        
       checkAndCreate( checkSql, createSql ) ;
    }
    
    private void checkAndCreateNotesElementTable() throws Exception {
        
        final String checkSql =  
            "SELECT TABLE_NAME " + 
            "FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_NAME = 'NOTES_ELEMENT'" ;
        
        final String createSql =  
            "CREATE TABLE jove_notes.notes_element (" +
                "notes_element_id  INTEGER IDENTITY NOT NULL," +
                "chapter_id        INTEGER NOT NULL," +
                "element_type      VARCHAR(45) NOT NULL," +
                "difficulty_level  INTEGER NOT NULL," +
                "content           VARCHAR(65535) NOT NULL," +
                "eval_vars         VARCHAR(65535)," +
                "script_body       VARCHAR(65535)," +
                "obj_correl_id     VARCHAR(45) NOT NULL," +
                "ready             BIT(1) DEFAULT B'1' NOT NULL," +
                "hidden_from_view  BIT(1) DEFAULT B'0' NOT NULL," +
                "marked_for_review BIT(1) DEFAULT B'0' NOT NULL" +
            ")" ;
        
       checkAndCreate( checkSql, createSql ) ;
    }
    
    private void checkAndCreatePersistentQueueTable() throws Exception {
        
        final String checkSql =  
            "SELECT TABLE_NAME " + 
            "FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_NAME = 'PERSISTENT_QUEUE'" ;
        
        final String createSql =  
            "CREATE TABLE jove_notes.persistent_queue (" +
                "id                  INTEGER IDENTITY NOT NULL," +
                "uid                 VARCHAR(45)," +
                "creation_time       TIMESTAMP NOT NULL," +
                "serialized_obj      LONGVARBINARY NOT NULL," +
                "num_times_processed INTEGER," +
                "last_process_time   TIMESTAMP" +
            ")" ;
        
        checkAndCreate( checkSql, createSql ) ;
    }
    
    private void checkAndCreate( String checkSql, String createSql ) 
                throws Exception {
        
        Connection conn = JoveNotes.db.getConnection() ;
        try {
            
            PreparedStatement psmt = conn.prepareStatement( checkSql ) ;
            ResultSet rs = psmt.executeQuery() ;

            if ( rs == null || !rs.next() ) {
                psmt = conn.prepareStatement( createSql ) ;
                psmt.executeUpdate() ;
            }
            conn.commit() ; 
        }
        finally {
            JoveNotes.db.returnConnection( conn ) ;
        } 
    } 
}
