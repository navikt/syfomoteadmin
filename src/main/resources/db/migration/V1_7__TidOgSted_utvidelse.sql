-- ROLLBACK-START
------------------
-- ALTER TABLE TID_STED DROP COLUMN CREATED;
---------------
-- ROLLBACK-END

ALTER TABLE TID_STED
ADD (created TIMESTAMP);

update TID_STED
  set TID_STED.created = (select MOTE.created from MOTE where MOTE.mote_id=TID_STED.mote_id);

ALTER TABLE TID_STED
MODIFY (created NOT NULL);
