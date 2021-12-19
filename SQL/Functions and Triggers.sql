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