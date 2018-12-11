-- CREATE DATABASE gradient; 
-- USE gradient;

/*
CREATE TABLE Image(
	id int NOT NULL PRIMARY KEY
);
*/
-- DROP TABLE Color;

/*
CREATE TABLE Color(
	imgid int NOT NULL,
    colorid int NOT NULL PRIMARY KEY,
	colorValue int,
    occurence int,
    FOREIGN KEY (imgid) REFERENCES Image(id)    
);
*/
-- INSERT INTO Image (id) VALUES (12);

-- INSERT INTO Color (imgid, colorid, colorValue, occurence) VALUES (13, 3, 12345453, 1);
-- INSERT INTO Color (imgid, colorid, colorValue, occurence) VALUES (13, 4, 1235, 2);
UPDATE Color SET occurence = 2 WHERE imgid= 12 AND colorValue=1235; 
-- DELETE FROM Color WHERE imgid=12;
-- DELETE FROM Image WHERE id=12;
SELECT * FROM Image;
SELECT * FROM Color WHERE imgid=12 ORDER BY colorValue;



