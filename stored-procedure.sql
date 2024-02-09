use moviedb;
drop procedure if exists add_movie;

-- Change DELIMITER to $$ 
DELIMITER $$ 

CREATE PROCEDURE add_movie (IN title VARCHAR(100), IN year INT, IN director VARCHAR(100), IN star_name VARCHAR(100), IN genre_name VARCHAR(100))
BEGIN

DECLARE movie_count INT DEFAULT 0;
DECLARE star_count INT DEFAULT 0;
DECLARE genre_count INT DEFAULT 0;
DECLARE new_star_id VARCHAR(10);
DECLARE new_movie_id VARCHAR(10);

-- Get count of movie to see if it exists already
SELECT COUNT(*) INTO movie_count from movies as m where m.title = title and m.year = year and m.director = director;
-- Get count of star to see if star already exists
SELECT COUNT(*) INTO star_count from stars as s where s.name = star_name;
-- Get count of genre to see if genre already exists
SELECT COUNT(*) INTO genre_count from genres as g where g.name = genre_name;

-- Check if movie already exists, if so do nothing
IF (movie_count > 0) THEN
	SELECT "Movie already exists in the database. No changes were made." as answer;
    
ELSE
	-- insert new star into stars table if they do not exist
	IF (star_count <= 0) THEN
		select CONCAT('nm', SUBSTRING(max(id),3) + 1) INTO new_star_id from stars;
		INSERT INTO stars (id, name) VALUES(new_star_id, star_name);
        -- SELECT (select CONCAT('nm', SUBSTRING(max(id),3) + 1) from stars), star as answer;
    END IF;
    
    -- insert new genre into genres table if it does not exist
    IF (genre_count <= 0) THEN
		INSERT INTO genres (name) VALUES(genre_name);
        -- SELECT genre as answer;
	END IF;
    
    
    -- make new movie id
    SELECT CONCAT ('tt', SUBSTRING(max(id),3) + 1) INTO new_movie_id FROM movies;
    
    -- Insert new data into related tables
	INSERT INTO movies (id, title, year, director) VALUES (new_movie_id, title, year, director);
    INSERT INTO genres_in_movies (genreId, movieId) VALUES ((select g.id from genres as g where g.name = genre_name), (select m.id from movies as m where m.title = title and m.year = year and m.director = director));
    INSERT INTO stars_in_movies (starId, movieId) VALUES ((select s.id from stars as s where s.name = star_name limit 1), (select m.id from movies as m where m.title = title and m.year = year and m.director = director));
    SELECT CONCAT('SUCCESS! MOVIE ID: ', (select m.id from movies as m where m.title = title and m.year = year and m.director = director), ', STAR ID: ',  (select s.id from stars as s where s.name = star_name), ', GENRE ID: ', (select g.id from genres as g where g.name = genre_name)) as answer;
END IF;
END
$$

-- Change back DELIMITER to ; 
DELIMITER ; 