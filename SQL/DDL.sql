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
	author_first_name		VARCHAR(50) NOT NULL,
	author_last_name		VARCHAR(50) NOT NULL,
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
	tracking_number			VARCHAR(50) UNIQUE NOT NULL,
	total					DECIMAL(10,2) NOT NULL,
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
	stock					INT NOT NULL,
	price					DECIMAL(10,2) NOT NULL,
	publisher_percentage	DECIMAL(10,2) NOT NULL,
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
	quantity				INT NOT NULL,
	PRIMARY KEY (suborder_id, order_id),
	FOREIGN KEY (order_id) REFERENCES orders
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY (ISBN) REFERENCES books
		ON UPDATE CASCADE
);

INSERT into users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	values('User1', 'U1F', 'U1L', 'U1@gmail.com', 'U1 Billing Lane', 'U1 Shipping Lane');

INSERT into users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	values('User2', 'U2F', 'U2L', 'U2@gmail.com', 'U2 Billing Lane', 'U2 Shipping Lane');

INSERT into users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	values('User3', 'U3F', 'U3L', 'U3@gmail.com', 'U3 Billing Lane', 'U3 Shipping Lane');

INSERT into users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address)
	values('User4', 'U4F', 'U4L', 'U4@gmail.com', 'U4 Billing Lane', 'U4 Shipping Lane');


INSERT into genres (genre_id, genre_name)
	values(1, 'Fantasy');

INSERT into genres (genre_id, genre_name)
	values(2, 'Science Fiction');

INSERT into genres (genre_id, genre_name)
	values(3, 'Action & Adventure');

INSERT into genres (genre_id, genre_name)
	values(4, 'Mystery');

INSERT into genres (genre_id, genre_name)
	values(5, 'Romance');

INSERT into genres (genre_id, genre_name)
	values(6, 'Young Adult');

INSERT into genres (genre_id, genre_name)
	values(7, 'Food & Drink');

INSERT into genres (genre_id, genre_name)
	values(8, 'Art & Photography');

INSERT into genres (genre_id, genre_name)
	values(9, 'History');

INSERT into genres (genre_id, genre_name)
	values(10, 'Autobiography');


INSERT into publishers (publisher_id, publisher_name, publisher_email, publisher_address, publisher_bank_account)
	values(1, 'Publisher1', 'P1@gmail.com', 'P1 Address', 'P1 Bank Number');

INSERT into publishers (publisher_id, publisher_name, publisher_email, publisher_address, publisher_bank_account)
	values(2, 'Publisher2', 'P2@gmail.com', 'P2 Address', 'P2 Bank Number');

INSERT into publishers (publisher_id, publisher_name, publisher_email, publisher_address, publisher_bank_account)
	values(3, 'Publisher3', 'P3@gmail.com', 'P3 Address', 'P3 Bank Number');


INSERT into phones (phone_number, publisher_id)
	values('613-1111-111', 1);

INSERT into phones (phone_number, publisher_id)
	values('613-0011-111', 1);

INSERT into phones (phone_number, publisher_id)
	values('613-2222-222', 2);

INSERT into phones (phone_number, publisher_id)
	values('613-3333-333', 3);

INSERT into phones (phone_number, publisher_id)
	values('613-0033-333', 3);

INSERT into phones (phone_number, publisher_id)
	values('613-3333-003', 3);


INSERT into authors (author_id, author_first_name, author_last_name)
	values(1, 'Frank', 'Herbert');

INSERT into authors (author_id, author_first_name, author_last_name)
	values(2, 'Robert', 'Jordan');
	
INSERT into authors (author_id, author_first_name, author_last_name)
	values(3, 'Brian', 'Sanderson');
	
INSERT into authors (author_id, author_first_name, author_last_name)
	values(4, 'Irma', 'Rombauer');
	
INSERT into authors (author_id, author_first_name, author_last_name)
	values(5, 'A5F', 'A5L');


INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-1', 'Dune', 2, 1, 896, true, 50, 11.70, 0.05);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-2', 'Dune Messiah', 2, 1, 352, true, 50, 12.99, 0.05);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-3', 'Children of Dune', 2, 1, 624, true, 50, 12.99, 0.05);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-4', 'God Emperor of Dune', 2, 1, 608, true, 40, 12.99, 0.05);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-5', 'Heretics of Dune', 2, 1, 688, true, 30, 12.99, 0.05);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-6', 'Chapterhouse: Dune', 2, 1, 624, true, 20, 12.99, 0.05);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-7', 'Wheel of Time', 1, 2, 782, true, 20, 15.99, 0.07);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-8', 'The Great Hunt', 1, 2, 681, true, 20, 15.99, 0.07);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-111-9', 'The Dragon Reborn', 1, 2, 675, true, 20, 15.99, 0.07);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-0', 'Shadow Rising', 1, 2, 981, true, 15, 16.99, 0.07);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-1', 'The Fires of Heaven', 1, 2, 963, true, 15, 16.99, 0.07);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-2', 'Lord of Chaos', 1, 2, 987, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-3', 'A Crown of Swords', 1, 2, 856, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-4', 'The Path of Daggers', 1, 2, 672, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-5', 'Winters Heart', 1, 2, 766, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-6', 'Crossroads of Twilight', 1, 2, 822, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-7', 'Knife of Dreams', 1, 2, 837, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-8', 'The Gathering Storm', 1, 2, 766, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-112-9', 'Towers of Midnight', 1, 2, 864, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-113-0', 'A Memory of Light', 1, 2, 912, true, 15, 16.99, 0.08);

INSERT into books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage)
	values('111-1-11111-113-1', 'Joy of Cooking', 8, 3, 1152, true, 11, 16.99, 0.04);


INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-1', 1);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-2', 1);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-3', 1);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-4', 1);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-5', 1);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-6', 1);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-7', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-8', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-111-9', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-0', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-1', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-2', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-3', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-4', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-5', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-6', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-7', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-8', 3);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-112-9', 3);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-113-0', 2);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-113-0', 3);

INSERT into book_author (ISBN, author_id)
	values('111-1-11111-113-1', 4);


INSERT into orders (order_id, user_id, tracking_number, total, billing_address, shipping_address, order_status, date)
	values(1, 'User1', '00001', 16.99, 'Order1 Billing', 'Order1 Shipping', 'Completed', '2021-12-16');

INSERT into orders (order_id, user_id, tracking_number, total, billing_address, shipping_address, order_status, date)
	values(2, 'User2', '00002', 28.69, 'Order2 Billing', 'Order2 Shipping', 'Completed', '2021-12-17');


INSERT into suborders (suborder_id, order_id, ISBN, quantity)
	values(1, 1, '111-1-11111-113-1', 1);

INSERT into suborders (suborder_id, order_id, ISBN, quantity)
	values(1, 2, '111-1-11111-113-1', 1);

INSERT into suborders (suborder_id, order_id, ISBN, quantity)
	values(2, 2, '111-1-11111-111-1', 1);


CREATE VIEW sales_per_day as
SELECT date, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures from 
orders natural join suborders natural join books
GROUP BY date;

CREATE VIEW sales_per_genre_by_day as
SELECT date, genre_name, sum(quantity) as total_purchased, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures from 
orders natural join suborders natural join books natural join genres
GROUP BY date, genre_name;

CREATE VIEW sales_per_author_by_day as
SELECT date, author_id, author_first_name, author_last_name, sum(quantity) as total_purchased, sum(quantity * price) as total_sales, sum(quantity * price * publisher_percentage) as total_expenditures from 
orders natural join suborders natural join books natural join book_author natural join authors
GROUP BY date, author_id, author_first_name, author_last_name;

CREATE or REPLACE function sales_per_period (start_date date, end_date date)
	returns table (
		total_sales DECIMAL(10,2),
		total_expenditures DECIMAL(10,2)
	)
	language plpgsql
as $$
begin
	return query 
		SELECT 
			sum(sales_per_day.total_sales),
			sum(sales_per_day.total_expenditures)
		from 
			sales_per_day
		WHERE 
			date >= start_date and date <= end_date;
end;$$;

CREATE or REPLACE function sales_per_genre (start_date date, end_date date)
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
end;$$;

CREATE or REPLACE function sales_per_author (start_date date, end_date date)
	returns table (
		author_first_name VARCHAR(50),
		author_last_name VARCHAR(50),
		total_sales DECIMAL(10,2),
		total_expenditures DECIMAL(10,2)
	)
	language plpgsql
as $$
BEGIN
	return query 
		SELECT 
			sales_per_author_by_day.author_first_name, 
			sales_per_author_by_day.author_last_name,
			sum(sales_per_author_by_day.total_sales), 
			sum(sales_per_author_by_day.total_expenditures) 
		FROM 
			sales_per_author_by_day
		WHERE 
			date >= start_date and date <= end_date
		GROUP BY 
			sales_per_author_by_day.author_id,
			sales_per_author_by_day.author_first_name,
			sales_per_author_by_day.author_last_name;
end;$$;

SELECT * from sales_per_period('2021-12-17','2021-12-31');
SELECT * from sales_per_genre('2021-12-17','2021-12-31');
SELECT * from sales_per_author('2021-12-17','2021-12-31');