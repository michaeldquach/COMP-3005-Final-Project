-- Creates tables and views for bookstore database

DROP TABLE IF EXISTS suborders CASCADE;
DROP TABLE IF EXISTS book_author CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS publishers CASCADE;

CREATE TABLE publishers (
	publisher_id			SERIAL,
	publisher_name			VARCHAR(50) NOT NULL,
	publisher_email			VARCHAR(50) UNIQUE NOT NULL,
	publisher_phone			VARCHAR(20) UNIQUE NOT NULL,
	publisher_address		VARCHAR(100) NOT NULL,
	publisher_bank_account	VARCHAR(50)	NOT NULL,
	PRIMARY KEY (publisher_id)
);

CREATE TABLE genres (
	genre_id				SERIAL,
	genre_name				VARCHAR(50)	UNIQUE NOT NULL,
	PRIMARY KEY (genre_id) 
);

CREATE TABLE authors (
	author_id				SERIAL,
	author_name				VARCHAR(50) NOT NULL,
	PRIMARY KEY (author_id)
);

CREATE TABLE users (
	user_id					VARCHAR(50),
	user_first_name			VARCHAR(50) NOT NULL,
	user_last_name			VARCHAR(50) NOT NULL,
	user_email				VARCHAR(50) UNIQUE NOT NULL,
	user_billing_address	VARCHAR(100),
	user_shipping_address	VARCHAR(100),
	PRIMARY KEY (user_id)	
);

CREATE TABLE orders (
	order_id				SERIAL,
	user_id					VARCHAR(50) NOT NULL,
	tracking_number			SERIAL UNIQUE NOT NULL,
	total					DECIMAL(10,2) NOT NULL CHECK (total >= 0),
	billing_address			VARCHAR(100) NOT NULL,
	shipping_address		VARCHAR(100) NOT NULL,
	order_status			VARCHAR(50) NOT NULL,
	date 					DATE NOT NULL,
	PRIMARY KEY (order_id),
	FOREIGN KEY (user_id) REFERENCES users
		ON UPDATE CASCADE
);

CREATE TABLE books (
	ISBN					VARCHAR(30),
	title					VARCHAR(50) NOT NULL,
	genre_id				SERIAL NOT NULL,
	publisher_id			SERIAL NOT NULL,
	num_pages				INT NOT NULL,
	available				BOOLEAN NOT NULL,
	stock					INT NOT NULL CHECK (stock >= 0),
	price					DECIMAL(10,2) NOT NULL CHECK (price >= 0),
	publisher_percentage	DECIMAL(10,2) NOT NULL CHECK (publisher_percentage >= 0 and publisher_percentage <= 1),
	PRIMARY KEY (ISBN),
	FOREIGN KEY (genre_id) REFERENCES genres
		ON UPDATE CASCADE,
	FOREIGN KEY (publisher_id) REFERENCES publishers
		ON UPDATE CASCADE
);

CREATE TABLE book_author (
	ISBN					VARCHAR(30),
	author_id				SERIAL NOT NULL,
	PRIMARY KEY (ISBN, author_id),
	FOREIGN KEY (ISBN) REFERENCES books
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (author_id) REFERENCES authors
		ON DELETE CASCADE
		ON UPDATE CASCADE
);

CREATE TABLE suborders (
	suborder_id				SERIAL,
	order_id 				SERIAL NOT NULL,
	ISBN					VARCHAR(30) NOT NULL,
	quantity				INT NOT NULL CHECK (quantity >= 0),
	PRIMARY KEY (suborder_id, order_id),
	FOREIGN KEY (order_id) REFERENCES orders
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (ISBN) REFERENCES books
		ON UPDATE CASCADE
);


-- Starts tracking id at 10000
ALTER SEQUENCE orders_tracking_number_seq RESTART WITH 10000;


-- Creates view of sales and expenditures of all books per day
CREATE or REPLACE VIEW sales_per_day as
SELECT date, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures FROM 
orders NATURAL JOIN suborders NATURAL JOIN books
GROUP BY date;

-- Creates view of sales and expenditures of books grouped by genre per day
CREATE or REPLACE VIEW sales_per_genre_by_day as
SELECT date, genre_name, sum(quantity) as total_purchased, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures FROM 
orders NATURAL JOIN suborders NATURAL JOIN books NATURAL JOIN genres
GROUP BY date, genre_name;

-- Creates view of sales and expenditures of books grouped by author per day
CREATE or REPLACE VIEW sales_per_author_by_day as
SELECT date, author_id, author_name, sum(quantity) as total_purchased, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures FROM 
orders NATURAL JOIN suborders NATURAL JOIN books NATURAL JOIN book_author NATURAL JOIN authors
GROUP BY date, author_id, author_name;

-- Creates view of sales and expenditures of books grouped by isbn per day
CREATE or REPLACE VIEW sales_per_book_by_day as
SELECT date, ISBN, title, sum(quantity) as total_purchased FROM
orders NATURAL JOIN suborders NATURAL JOIN books
GROUP BY date, ISBN, title;