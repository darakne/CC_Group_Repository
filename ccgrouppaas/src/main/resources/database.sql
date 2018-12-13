
-- USE dbgradient;

-- DROP TABLE colortable;

CREATE TABLE colortable(
	colorid  INTEGER NOT NULL AUTO_INCREMENT,
	imgid  INTEGER NOT NULL,
	colorvalue VARCHAR(20) NOT NULL,
    occurence INTEGER,
   primary key (colorid)
);

 INSERT INTO colortable (imgid, colorvalue, occurence) VALUES (12, 12345453, 1);
 UPDATE colortable SET occurence = 2 WHERE imgid= 12 AND colorvalue=1235; 
DELETE FROM colortable WHERE imgid=12;

 -- 
 SELECT * FROM colortable;
SELECT imgid FROM colortable;
-- SELECT * FROM colortable WHERE imgid=12 ORDER BY colorvalue;



