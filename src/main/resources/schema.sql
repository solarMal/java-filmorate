DROP TABLE IF EXISTS Genre CASCADE;
DROP TABLE IF EXISTS FilmMPA CASCADE;
DROP TABLE IF EXISTS Film CASCADE;
DROP TABLE IF EXISTS FilmGenre;
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Friend;
DROP TABLE IF EXISTS Friendship_request;
DROP TABLE IF EXISTS filmLike;

CREATE TABLE IF NOT EXISTS Genre (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS FilmMPA (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS Film (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  releaseDate DATE NOT NULL,
  duration INT,
  film_genre_id INT,
  film_mpa_id INT,
  rate INT,
  FOREIGN KEY (film_mpa_id) REFERENCES FilmMPA(id)
);

CREATE TABLE IF NOT EXISTS FilmGenre (
  id INT PRIMARY KEY AUTO_INCREMENT,
  film_id INT,
  genre_id INT,
  FOREIGN KEY (film_id) REFERENCES Film(id),
  FOREIGN KEY (genre_id) REFERENCES Genre(id)
);

CREATE TABLE IF NOT EXISTS Users (
  id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  login VARCHAR(255) NOT NULL,
  name VARCHAR(255),
  email VARCHAR(255),
  birthday DATE
);

CREATE TABLE IF NOT EXISTS Friend (
  user_id INT,
  friend_id INT,
  PRIMARY KEY (user_id, friend_id),
  FOREIGN KEY (user_id) REFERENCES Users(id),
  FOREIGN KEY (friend_id) REFERENCES Users(id)
);

CREATE TABLE IF NOT EXISTS Friendship_request (
  user_id INT,
  friend_id INT,
  FOREIGN KEY (user_id) REFERENCES Users(id),
  FOREIGN KEY (friend_id) REFERENCES Users(id)
);

CREATE TABLE IF NOT EXISTS filmLike (
  id INT PRIMARY KEY AUTO_INCREMENT,
  filmId INT,
  userId INT,
  FOREIGN KEY (filmId) REFERENCES Film(id),
  FOREIGN KEY (userId) REFERENCES Users(id)
);

INSERT INTO Genre (name) VALUES
  ('Комедия'),
  ('Драма'),
  ('Мультфильм'),
  ('Триллер'),
  ('Документальный'),
  ('Боевик');

INSERT INTO FilmMPA (name) VALUES
  ('G'),
  ('PG'),
  ('PG-13'),
  ('R'),
  ('NC-17');
