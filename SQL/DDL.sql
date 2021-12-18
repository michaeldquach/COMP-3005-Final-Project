DROP TABLE IF EXISTS suborders CASCADE;
DROP TABLE IF EXISTS book_author CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS phones CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS publishers CASCADE;

CREATE TABLE publishers (
	publisher_id			SERIAL,
	publisher_name			VARCHAR(50) NOT NULL,
	publisher_email			VARCHAR(50) UNIQUE NOT NULL,
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

CREATE TABLE phones (
	phone_number			VARCHAR(20),
	publisher_id			SERIAL,
	PRIMARY KEY (phone_number),
	FOREIGN KEY (publisher_id) REFERENCES publishers
		ON DELETE CASCADE
		ON UPDATE CASCADE
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
	publisher_percentage	DECIMAL(10,2) NOT NULL CHECK (publisher_percentage >= 0),
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


ALTER SEQUENCE orders_tracking_number_seq RESTART WITH 10000;


CREATE or REPLACE VIEW sales_per_day as
SELECT date, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures FROM 
orders NATURAL JOIN suborders NATURAL JOIN books
GROUP BY date;

CREATE or REPLACE VIEW sales_per_genre_by_day as
SELECT date, genre_name, sum(quantity) as total_purchased, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures FROM 
orders NATURAL JOIN suborders NATURAL JOIN books NATURAL JOIN genres
GROUP BY date, genre_name;

CREATE or REPLACE VIEW sales_per_author_by_day as
SELECT date, author_id, author_name, sum(quantity) as total_purchased, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures FROM 
orders NATURAL JOIN suborders NATURAL JOIN books NATURAL JOIN book_author NATURAL JOIN authors
GROUP BY date, author_id, author_name;

CREATE or REPLACE VIEW sales_per_book_by_day as
SELECT date, ISBN, title, sum(quantity) as total_purchased FROM
orders NATURAL JOIN suborders NATURAL JOIN books
GROUP BY date, ISBN, title;


-- Returns books that match each parameter. Ignores null search parameters.
CREATE or REPLACE function search_book (search_title VARCHAR(50), search_author_name VARCHAR(50), search_ISBN VARCHAR(30), search_genre VARCHAR(50))
	returns table (
		book_title VARCHAR(50),
		book_ISBN VARCHAR(30),
		book_price DECIMAL(10,2),
		book_stock INTEGER
	)
	language plpgsql
as $$
BEGIN
	return query 
		SELECT DISTINCT
			title,
			ISBN,
			price,
			stock
		FROM 
			books NATURAL JOIN book_author NATURAL JOIN authors NATURAL JOIN genres
		WHERE 
			(title = search_title or search_title is NULL) and
			(author_name = search_author_name or search_author_name is NULL) and
			(ISBN = search_ISBN or search_ISBN is NULL) and
			(genre_name = search_genre or search_genre is NULL) and available = true;
END;$$;

-- Updates storefront availablity of book by ISBN. Used by bookstore owner to "remove" book from collection
CREATE or REPLACE FUNCTION update_availability (ISBN_input VARCHAR(30), availability BOOLEAN)
	returns void 	
	LANGUAGE plpgsql
as $$
BEGIN
	UPDATE 
		books
	SET 
		available = availability
	WHERE 
		books.ISBN = ISBN_input;
END;$$;

-- Aggregates all sales within input period
CREATE or REPLACE function sales_per_period (start_date date, end_date date)
	returns table (
		total_sales DECIMAL(10,2),
		total_expenditures DECIMAL(10,2)
	)
	language plpgsql
as $$
BEGIN
	return query 
		SELECT 
			sum(sales_per_day.total_sales),
			sum(sales_per_day.total_expenditures)
		FROM 
			sales_per_day
		WHERE 
			date >= start_date and date <= end_date;
END;$$;

-- Aggregates sales grouped by genre within input period
CREATE or REPLACE FUNCTION sales_per_genre (start_date DATE, end_date DATE)
	returns table (
		genre_name VARCHAR(50),
		total_sales DECIMAL(10,2),
		total_expenditures DECIMAL(10,2)
	)
	language plpgsql
as $$
BEGIN
	return query 
		SELECT 
			sales_per_genre_by_day.genre_name, 
			sum(sales_per_genre_by_day.total_sales), 
			sum(sales_per_genre_by_day.total_expenditures) 
		FROM 
			sales_per_genre_by_day
		WHERE 
			date >= start_date and date <= end_date
		GROUP BY 
			sales_per_genre_by_day.genre_name;
END;$$;

-- Aggregates sales grouped by author within input period
CREATE or REPLACE FUNCTION sales_per_author (start_date DATE, end_date DATE)
	returns table (
		author_name VARCHAR(50),
		total_sales DECIMAL(10,2),
		total_expenditures DECIMAL(10,2)
	)
	language plpgsql
as $$
BEGIN
	return query 
		SELECT 
			sales_per_author_by_day.author_name, 
			sum(sales_per_author_by_day.total_sales), 
			sum(sales_per_author_by_day.total_expenditures) 
		FROM 
			sales_per_author_by_day
		WHERE 
			date >= start_date and date <= end_date
		GROUP BY 
			sales_per_author_by_day.author_id,
			sales_per_author_by_day.author_name;
END;$$;

-- Aggregates sales of specific book within input period
CREATE or REPLACE FUNCTION sales_per_book (book_ISBN VARCHAR(30), start_date DATE, end_date DATE)
	returns table (
		ISBN VARCHAR(30),
		title VARCHAR(50),
		total_purchased DECIMAL(10,2)
	)
	language plpgsql
as $$
BEGIN
	return query 
		SELECT 
			sales_per_book_by_day.ISBN, 
			sales_per_book_by_day.title,
			sum(sales_per_book_by_day.total_purchased) 
		FROM 
			sales_per_book_by_day
		WHERE 
			sales_per_book_by_day.ISBN = book_ISBN and date >= start_date and date <= end_date
		GROUP BY 
			sales_per_book_by_day.ISBN,
			sales_per_book_by_day.title;
END;$$;

-- Defines trigger to restock books when stock has dropped below threshold
CREATE or REPLACE FUNCTION restock()
RETURNS TRIGGER
AS
$$
BEGIN
	IF NEW.stock < 10 THEN
		-- Reorders books equal to number of books sold in the last month
		IF NEW.stock + (SELECT total_purchased FROM sales_per_book(NEW.ISBN, current_date - 30, current_date)) >= 10
		THEN
			UPDATE books
			SET stock = stock + (SELECT total_purchased FROM sales_per_book(ISBN, current_date - 30, current_date))
			WHERE ISBN = NEW.ISBN;
		-- If the number of books sold in the last month is 0 or would sum to less than 10, simply restock books to threshold
		ELSE
			UPDATE books
			SET stock = 10
			WHERE ISBN = NEW.ISBN;
		END IF;
	END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Attaches trigger to books relation
CREATE TRIGGER book_restock
AFTER UPDATE of stock on books
FOR EACH ROW
WHEN (OLD.stock IS DISTINCT FROM NEW.stock)
EXECUTE PROCEDURE restock();

-- Defines trigger to update stock of book when suborder is made
CREATE or REPLACE FUNCTION update_stock_total()
RETURNS TRIGGER
AS
$$
BEGIN	
	-- Updates books to reflect quantity after suborder
	UPDATE books
	SET stock = stock - NEW.quantity
	WHERE ISBN = NEW.ISBN;
	-- Updates orders to reflect new total after suborder
	UPDATE orders
	SET total = total + NEW.quantity * (SELECT price FROM books WHERE NEW.ISBN = books.ISBN)
	WHERE order_id = NEW.order_id;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Attaches trigger to suborders relation
CREATE TRIGGER suborder_update_stock
AFTER INSERT OR UPDATE on suborders
FOR EACH ROW
EXECUTE PROCEDURE update_stock_total();


INSERT INTO users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	VALUES('User1', 'U1F', 'U1L', 'U1@gmail.com', 'U1 Billing Lane', 'U1 Shipping Lane');

INSERT INTO users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	VALUES('User2', 'U2F', 'U2L', 'U2@gmail.com', 'U2 Billing Lane', 'U2 Shipping Lane');

INSERT INTO users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	VALUES('User3', 'U3F', 'U3L', 'U3@gmail.com', 'U3 Billing Lane', 'U3 Shipping Lane');

INSERT INTO users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	VALUES('User4', 'U4F', 'U4L', 'U4@gmail.com', 'U4 Billing Lane', 'U4 Shipping Lane');


INSERT INTO genres (genre_name)
	VALUES('Fantasy');

INSERT INTO genres (genre_name)
	VALUES('Science Fiction');

INSERT INTO genres (genre_name)
	VALUES('Action & Adventure');

INSERT INTO genres (genre_name)
	VALUES('Mystery');

INSERT INTO genres (genre_name)
	VALUES('Romance');

INSERT INTO genres (genre_name)
	VALUES('Young Adult');

INSERT INTO genres (genre_name)
	VALUES('Food & Drink');

INSERT INTO genres (genre_name)
	VALUES('Art & Photography');

INSERT INTO genres (genre_name)
	VALUES('History');

INSERT INTO genres (genre_name)
	VALUES('Autobiography');


INSERT INTO publishers (publisher_name, publisher_email, publisher_address, publisher_bank_account)
	VALUES('Publisher1', 'P1@gmail.com', 'P1 Address', 'P1 Bank Number');

INSERT INTO publishers (publisher_name, publisher_email, publisher_address, publisher_bank_account)
	VALUES('Publisher2', 'P2@gmail.com', 'P2 Address', 'P2 Bank Number');

INSERT INTO publishers (publisher_name, publisher_email, publisher_address, publisher_bank_account)
	VALUES('Publisher3', 'P3@gmail.com', 'P3 Address', 'P3 Bank Number');


INSERT INTO phones (phone_number, publisher_id)
	VALUES('613-1111-111', 1);

INSERT INTO phones (phone_number, publisher_id)
	VALUES('613-0011-111', 1);

INSERT INTO phones (phone_number, publisher_id)
	VALUES('613-2222-222', 2);

INSERT INTO phones (phone_number, publisher_id)
	VALUES('613-3333-333', 3);

INSERT INTO phones (phone_number, publisher_id)
	VALUES('613-0033-333', 3);

INSERT INTO phones (phone_number, publisher_id)
	VALUES('613-3333-003', 3);


INSERT INTO authors (author_name)
	VALUES('Frank Herbert');

INSERT INTO authors (author_name)
	VALUES('Robert Jordan');
	
INSERT INTO authors (author_name)
	VALUES('Brian Sanderson');
	
INSERT INTO authors (author_name)
	VALUES('Irma Rombauer');
	
INSERT INTO authors (author_name)
	VALUES('A5F A5L');


INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-1', 'Dune', 2, 1, 896, true, 50, 11.70, 0.05);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-2', 'Dune Messiah', 2, 1, 352, true, 50, 12.99, 0.05);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-3', 'Children of Dune', 2, 1, 624, true, 50, 12.99, 0.05);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-4', 'God Emperor of Dune', 2, 1, 608, true, 40, 12.99, 0.05);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-5', 'Heretics of Dune', 2, 1, 688, true, 30, 12.99, 0.05);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-6', 'Chapterhouse: Dune', 2, 1, 624, true, 20, 12.99, 0.05);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-7', 'Wheel of Time', 1, 2, 782, true, 20, 15.99, 0.07);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-8', 'The Great Hunt', 1, 2, 681, true, 20, 15.99, 0.07);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-111-9', 'The Dragon Reborn', 1, 2, 675, true, 20, 15.99, 0.07);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-0', 'Shadow Rising', 1, 2, 981, true, 15, 16.99, 0.07);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-1', 'The Fires of Heaven', 1, 2, 963, true, 15, 16.99, 0.07);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-2', 'Lord of Chaos', 1, 2, 987, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-3', 'A Crown of Swords', 1, 2, 856, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-4', 'The Path of Daggers', 1, 2, 672, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-5', 'Winters Heart', 1, 2, 766, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-6', 'Crossroads of Twilight', 1, 2, 822, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-7', 'Knife of Dreams', 1, 2, 837, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-8', 'The Gathering Storm', 1, 2, 766, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-112-9', 'Towers of Midnight', 1, 2, 864, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-113-0', 'A Memory of Light', 1, 2, 912, true, 15, 16.99, 0.08);

INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-113-1', 'Joy of Cooking', 8, 3, 1152, true, 11, 16.99, 0.04);


INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-1', 1);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-2', 1);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-3', 1);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-4', 1);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-5', 1);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-6', 1);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-7', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-8', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-111-9', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-0', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-1', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-2', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-3', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-4', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-5', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-6', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-7', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-8', 3);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-112-9', 3);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-113-0', 2);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-113-0', 3);

INSERT INTO book_author (ISBN, author_id)
	VALUES('111-1-11111-113-1', 4);


INSERT INTO orders (user_id, total, billing_address, shipping_address, order_status, date)
	VALUES('User1', 0, 'Order1 Billing', 'Order1 Shipping', 'Completed', '2021-12-16');

INSERT INTO orders (user_id, total, billing_address, shipping_address, order_status, date)
	VALUES('User2', 0, 'Order2 Billing', 'Order2 Shipping', 'Completed', '2021-12-17');

INSERT INTO orders (user_id, total, billing_address, shipping_address, order_status, date)
	VALUES('User3', 0, 'Order3 Billing', 'Order3 Shipping', 'Completed', '2021-10-17');


INSERT INTO suborders (suborder_id, order_id, ISBN, quantity)
	VALUES(1, 1, '111-1-11111-113-1', 1);

INSERT INTO suborders (suborder_id, order_id, ISBN, quantity)
	VALUES(1, 2, '111-1-11111-113-1', 1);

INSERT INTO suborders (suborder_id, order_id, ISBN, quantity)
	VALUES(2, 2, '111-1-11111-111-1', 1);

INSERT INTO suborders (suborder_id, order_id, ISBN, quantity)
	VALUES(1, 3, '111-1-11111-111-1', 1);


/*
-- Access general sales vs expenditures report over given time
SELECT * FROM sales_per_period('2021-12-17','2021-12-31');

-- Access sales vs expenditures report per genre over given time
SELECT * FROM sales_per_genre('2021-12-17','2021-12-31');

-- Access sales vs expenditures report per author over given time
SELECT * FROM sales_per_author('2021-12-17','2021-12-31');

-- Access total sales of specific book within last 30 days
SELECT * FROM sales_per_book('111-1-11111-111-1', current_date - 30, current_date);
*/


UPDATE books 
set stock = 8
where books.title = 'Joy of Cooking';

-- Removes specific book from storefront
SELECT * FROM update_availability('111-1-11111-113-0', true);

-- General query to browse books
SELECT title, ISBN, price, stock FROM
books
WHERE books.available = true;

-- Search for book by specific parameter(s). Null parameters are ignored.
SELECT * FROM search_book(null, null, null, null);

-- View detailed information of specific book by ISBN
SELECT books.title, books.ISBN, authors.author_name, genres.genre_name, publishers.publisher_name, books.num_pages, books.price, books.stock FROM
books natural join genres natural join book_author natural join authors natural join publishers
WHERE books.available = true and books.ISBN = '111-1-11111-113-0';

/*RAISE NOTICE 'test (%)', NEW.stock;*/


-- Adds new genre
INSERT INTO genres (genre_name)
	VALUES('Suspense');

-- Adds new author
INSERT INTO authors (author_name)
	VALUES('Robert Frost');

-- Adds new publisher
INSERT INTO publishers (publisher_name, publisher_email, publisher_address, publisher_bank_account)
	VALUES('Publisher4', 'P4@gmail.com', 'P4 Address', 'P4 Bank Account');

-- Get genres
SELECT * FROM genres;

-- Get authors
SELECT * FROM authors;

-- Get publishers
SELECT * FROM publishers;

INSERT INTO suborders (suborder_id, order_id, ISBN, quantity)
	VALUES(3, 2, '111-1-11111-111-1', 45);

SELECT * FROM books;

/*
INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	VALUES('111-1-11111-113-1', 'Joy of Cooking', 8, 3, 1152, true, 11, 16.99, 0.04);*/