-- The following queries are those used in the application. 
-- These queries are examples and are not meant to be run to initialize the database 

-- Queries in user interface:

/*
-- Selects books available to customer
SELECT title, ISBN, price, stock FROM books WHERE books.available = true;

-- Invokes search function (see Functions and Triggers) to select books according to these parameters
SELECT * FROM search_book(title, author_name, ISBN, genre);

-- Selects specific information about book matching ISBN input
-- ? = ISBN input
SELECT books.title, books.ISBN, authors.author_name, genres.genre_name, publishers.publisher_name, books.num_pages, books.price, books.stock FROM books natural join genres natural join book_author natural join authors natural join publishers WHERE books.available = true and books.ISBN = ?;

-- Inserts new order with given input fields
-- ? = user_id, billing_address, shipping_address, date inputs
INSERT INTO orders (user_id, total, billing_address, shipping_address, order_status, date) VALUES(?, 0, ?, ?, 'Order Placed', ?) RETURNING order_id, tracking_number;

-- Inserts new suborder matching given order_id, containing specified book and quantity
-- ? = suborder_id, order_id, ISBN, quantity inputs
INSERT INTO suborders (suborder_id, order_id, ISBN, quantity) VALUES(?, ?, ?, ?);

-- Selects user matching user_id input
-- ? = user_id input
SELECT * FROM users WHERE users.user_id = ?

-- Inserts new user with given input fields
-- ? = user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address inputs
INSERT INTO users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address) VALUES(?, ?, ?, ?, ?, ?);

-- Selects all suborders with associated order_id as well as order, book, and quantity information
-- ? = order_id input
SELECT * FROM orders NATURAL JOIN suborders NATURAL JOIN books WHERE order_id = ?

-- Queries in owner interface:

-- Selects all books (available or not) for owner usage
SELECT title, ISBN, price, stock FROM books;

-- Selects specific information about book matching ISBN input
-- ? = ISBN input
SELECT books.title, books.ISBN, authors.author_name, genres.genre_name, publishers.publisher_name, books.num_pages, books.price, books.stock, books.available FROM books natural join genres natural join book_author natural join authors natural join publishers WHERE books.ISBN = ?;

-- Updates book's availability according to boolean input by invoking update_availability (see Functions and Triggers)
-- ? = ISBN, availability boolean inputs
SELECT * FROM update_availability(?, ?);

-- Adds new genre. True return value is used to determine if insert succeeded
-- ? = genre_name input
INSERT INTO genres (genre_name) VALUES(?) RETURNING true;

-- Adds new publisher. True return value is used to determine if insert succeeded
-- ? = publisher_name, publisher_email, publisher_phone, publisher_address, publisher_bank_account inputs
INSERT INTO publishers (publisher_name, publisher_email, publisher_phone, publisher_address, publisher_bank_account) VALUES(?, ?, ?, ?, ?) RETURNING true;

-- Adds new author. True return value is used to determine if insert succeeded
-- ? = author_name input
INSERT INTO authors (author_name) VALUES(?) RETURNING true;

-- Adds author to given book in book_author relation. True return value is used to determine if insert succeeded
-- ? = ISBN, author_id inputs
INSERT INTO book_author (ISBN, author_id) VALUES(?, ?) RETURNING true;

-- Adds new book with given values. True return value is used to determine if insert succeeded
-- ? = ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage
INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING true;

-- Access general sales vs expenditures report over given time
-- ? = startDate, endDate inputs
SELECT sum(sales_per_day.total_sales) as total_sales, sum(sales_per_day.total_expenditures) as total_expenditures FROM sales_per_day WHERE date >= ? and date <= ?;

-- Access sales vs expenditures report per genre over given time
-- ? = startDate, endDate inputs
SELECT sales_per_genre_by_day.genre_name, sum(sales_per_genre_by_day.total_sales) as total_sales, sum(sales_per_genre_by_day.total_expenditures) as total_expenditures FROM sales_per_genre_by_day WHERE date >= ? and date <= ? GROUP BY sales_per_genre_by_day.genre_name;

-- Access sales vs expenditures report per author over given time
-- ? = startDate, endDate inputs
SELECT sales_per_author_by_day.author_name, sum(sales_per_author_by_day.total_sales) as total_sales, sum(sales_per_author_by_day.total_expenditures) as total_expenditures FROM sales_per_author_by_day WHERE date >= ? and date <= ? GROUP BY sales_per_author_by_day.author_name;

-- Selects all genres
SELECT * FROM genres;

-- Selects all authors
SELECT * FROM authors;

-- Selects all publishers
SELECT * FROM publishers;
*/