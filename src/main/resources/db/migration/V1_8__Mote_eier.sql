-- ROLLBACK-START
------------------
-- ALTER TABLE MOTE DROP COLUMN EIER;
---------------
-- ROLLBACK-END

ALTER TABLE MOTE
ADD (eier VARCHAR(255));

update MOTE m1
  set m1.eier = (select m2.opprettet_av from MOTE m2 where m1.mote_id=m2.mote_id);

ALTER TABLE MOTE
MODIFY (eier NOT NULL);