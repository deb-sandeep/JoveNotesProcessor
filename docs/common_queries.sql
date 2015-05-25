select * from jove_notes.chapter ;
select * from jove_notes.card;
select * from jove_notes.notes_element;

delete from jove_notes.chapter ;
delete from jove_notes.persistent_queue ;

ALTER TABLE jove_notes.chapter AUTO_INCREMENT = 1 ;
ALTER TABLE jove_notes.card AUTO_INCREMENT = 1 ;
ALTER TABLE jove_notes.notes_element AUTO_INCREMENT = 1 ;
ALTER TABLE jove_notes.persistent_queue AUTO_INCREMENT = 1 ;