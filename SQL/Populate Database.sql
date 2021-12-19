
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


INSERT INTO publishers (publisher_name, publisher_email, publisher_phone, publisher_address, publisher_bank_account)
	VALUES('Publisher1', 'P1@gmail.com', '613-1111-111', 'P1 Address', 'P1 Bank Number');

INSERT INTO publishers (publisher_name, publisher_email, publisher_phone, publisher_address, publisher_bank_account)
	VALUES('Publisher2', 'P2@gmail.com', '613-2222-222', 'P2 Address', 'P2 Bank Number');

INSERT INTO publishers (publisher_name, publisher_email, publisher_phone, publisher_address, publisher_bank_account)
	VALUES('Publisher3', 'P3@gmail.com', '613-3333-333', 'P3 Address', 'P3 Bank Number');


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
