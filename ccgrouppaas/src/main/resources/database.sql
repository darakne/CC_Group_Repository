-- CREATE DATABASE gradient; 
-- USE gradient;

-- DROP TABLE colortable;

/*
CREATE TABLE colortable(
	colorid  INTEGER NOT NULL AUTO_INCREMENT,
	imgid  INTEGER NOT NULL,
	colorvalue INTEGER NOT NULL,
    occurence INTEGER,
    PRIMARY KEY(colorid)
);
*/

-- INSERT INTO colortable (imgid, colorvalue, occurence) VALUES (12, 12345453, 1);
-- INSERT INTO Color (imgid, colorid, colorvalue, occurence) VALUES (13, 4, 1235, 2);
-- UPDATE Color SET occurence = 2 WHERE imgid= 12 AND colorvalue=1235; 
-- DELETE FROM Color WHERE imgid=12;
-- DELETE FROM Image WHERE id=12;

 -- 
SELECT imgid FROM colortable;
-- SELECT * FROM colortable WHERE imgid=12 ORDER BY colorvalue;



