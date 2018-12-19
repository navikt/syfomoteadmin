-- ROLLBACK-START
------------------
-- DROP TABLE FEED;
-- DROP SEQUENCE FEED_ID_SEQ;
---------------
-- ROLLBACK-END


CREATE SEQUENCE FEED_ID_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE FEED (
  feed_id          NUMBER(19, 0) NOT NULL,
  uuid             VARCHAR(50)   NOT NULL,
  mote_id          NUMBER(19, 0) NOT NULL,
  created          TIMESTAMP     NOT NULL,
  sist_endret_av   VARCHAR(30),
  type             VARCHAR(30)   NOT NULL
);
