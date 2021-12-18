import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class bookStore {
    static String url = "jdbc:postgresql://localhost:5432/bookstore";
    static String userid = "postgres";
    static String password = "Modular1/1!";
    static JList<Book> booksList;
    static JList<Book> cartList;
    static Vector<Book> cart;
    static JLabel titleValue, ISBNValue, priceValue, stockValue, authorValue, genreValue, publisherValue, pagesValue;

    boolean isOwner;

    public bookStore(){
        isOwner = true;
    }
    
    public static void connectToDatabase(String url, String userid, String passwd){
        try{
            Connection conn = DriverManager.getConnection(url, userid, passwd);   
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    public static void main(String args[]){
        //IMPORTANT: Please change this to connect to your database with your own credentials
        //connectToDatabase("jdbc:postgresql://localhost:5432/bookstore", "postgres", "Modular1/1!");
        initiateView();
    }

    public static Vector<Book> getBooks(){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT title, ISBN, price, stock FROM books WHERE books.available = true;"
            );
            ResultSet rset = pStmt.executeQuery();
            Vector<Book> bookList = new Vector<Book>();

            while(rset.next()){
                bookList.add(new Book(rset.getString("title"), rset.getString("ISBN"), rset.getDouble("price"), rset.getInt("stock")));
            }

            return bookList;
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
        return null;
    }

    public static Vector<Book> searchBooks(String title, String author, String ISBN, String genre){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM search_book(?, ?, ?, ?);"
            );
            pStmt.setString(1, title);
            pStmt.setString(2, author);
            pStmt.setString(3, ISBN);
            pStmt.setString(4, genre);
            ResultSet rset = pStmt.executeQuery();
            Vector<Book> bookList = new Vector<Book>();

            while(rset.next()){
                bookList.add(new Book(rset.getString("book_title"), rset.getString("book_ISBN"), rset.getDouble("book_price"), rset.getInt("book_stock")));
            }

            return bookList;
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
        return null;

    }

    public static void getBookInformation(String ISBN){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT books.title, books.ISBN, authors.author_name, genres.genre_name, publishers.publisher_name, books.num_pages, books.price, books.stock FROM books natural join genres natural join book_author natural join authors natural join publishers WHERE books.available = true and books.ISBN = ?;"
            );
            pStmt.setString(1, ISBN);
            ResultSet rset = pStmt.executeQuery();

            int i = 0;

            while(rset.next()){
                if(i <= 0){
                    titleValue.setText(rset.getString("title"));
                    ISBNValue.setText(rset.getString("ISBN"));
                    priceValue.setText(Double.toString(rset.getDouble("Price")));
                    stockValue.setText(Integer.toString(rset.getInt("Stock")));
                    authorValue.setText(rset.getString("author_name"));
                    genreValue.setText(rset.getString("genre_name"));
                    publisherValue.setText(rset.getString("publisher_name"));
                    pagesValue.setText(Integer.toString(rset.getInt("num_pages")));
                }
                else{
                    //Multiple tuples returned when more than one author - append each additional author
                    authorValue.setText(authorValue.getText() + ", " + rset.getString("author_name"));
                }
                i++;
            }

        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    public static void initiateView(){
        JFrame frame = new JFrame("Look Inna Book");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel main = new JPanel(new GridLayout(2, 1));
        main.setSize(800, 600);
        frame.add(main);

        JPanel top = new JPanel(new GridLayout(1, 2));
        main.add(top);

        booksList = new JList<Book>();
        booksList.setBounds(30, 40, 200, 300);
        booksList.setVisibleRowCount(10);
        booksList.setCellRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(renderer instanceof JLabel && value instanceof Book){
                    ((JLabel) renderer).setText(((Book) value).getTitle());
                }
                return renderer;
            }
        });
        booksList.setListData(getBooks());

        booksList.addListSelectionListener((ListSelectionListener) new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg) {
                if (!arg.getValueIsAdjusting()) {
                    if(booksList.getSelectedValue() != null){
                        getBookInformation(booksList.getSelectedValue().getISBN());
                    }
                }
            }
        });

        top.add(booksList);
        top.add(new JScrollPane(booksList));

        JPanel bookDetails = new JPanel(new GridLayout(8,2));
        top.add(bookDetails);
     
        JLabel informationLabel = new JLabel("Book Information:");
        bookDetails.add(informationLabel, 0);

        titleValue = new JLabel("");
        bookDetails.add(titleValue, 1);

        JLabel ISBNLabel = new JLabel("ISBN: ");
        bookDetails.add(ISBNLabel, 2);

        ISBNValue = new JLabel("");
        bookDetails.add(ISBNValue, 3);

        JLabel priceLabel = new JLabel("Price: ");
        bookDetails.add(priceLabel, 4);

        priceValue = new JLabel("");
        bookDetails.add(priceValue, 5);

        JLabel stockLabel = new JLabel("Stock: ");
        bookDetails.add(stockLabel, 6);

        stockValue = new JLabel("");
        bookDetails.add(stockValue, 7);

        JLabel authorLabel = new JLabel("Author: ");
        bookDetails.add(authorLabel, 8);

        authorValue = new JLabel("");
        bookDetails.add(authorValue, 9);

        JLabel genreLabel = new JLabel("Genre: ");
        bookDetails.add(genreLabel, 10);

        genreValue = new JLabel("");
        bookDetails.add(genreValue, 11);

        JLabel publisherLabel = new JLabel("Publisher: ");
        bookDetails.add(publisherLabel, 12);

        publisherValue = new JLabel("");
        bookDetails.add(publisherValue, 13);

        JLabel pagesLabel = new JLabel("Pages: ");
        bookDetails.add(pagesLabel, 14);

        pagesValue = new JLabel("");
        bookDetails.add(pagesValue, 15);



        JPanel middle = new JPanel(new GridLayout(3,3));
        main.add(middle);

        JPanel searchPanel = new JPanel(new GridLayout(2,5));
        middle.add(searchPanel);

        JLabel searchLabel = new JLabel("Search by:");
        searchPanel.add(searchLabel);

        JLabel titleSearchLabel = new JLabel("Title: ");
        searchPanel.add(titleSearchLabel);

        JLabel authorSearchLabel = new JLabel("Author: ");
        searchPanel.add(authorSearchLabel);

        JLabel ISBNSearchLabel = new JLabel("ISBN: ");
        searchPanel.add(ISBNSearchLabel);

        JLabel genreSearchLabel = new JLabel("Genre: ");
        searchPanel.add(genreSearchLabel);

        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        JTextField searchTitle = new JTextField();
        searchTitle.setToolTipText("Title: ");
        searchPanel.add(searchTitle);

        JTextField searchAuthor = new JTextField();
        searchAuthor.setToolTipText("Author: ");
        searchPanel.add(searchAuthor);

        JTextField searchISBN = new JTextField();
        searchISBN.setToolTipText("ISBN: ");
        searchPanel.add(searchISBN);

        JTextField searchGenre = new JTextField();
        searchGenre.setToolTipText("Genre: ");
        searchPanel.add(searchGenre);

        searchButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {      
                String title = null, author = null, ISBN = null, genre = null;
                if(!searchTitle.getText().equals("")){
                    title = searchTitle.getText();
                }
                if(!searchAuthor.getText().equals("")){
                    author = searchAuthor.getText();
                }
                if(!searchISBN.getText().equals("")){
                    ISBN = searchISBN.getText();
                }
                if(!searchGenre.getText().equals("")){
                    genre = searchGenre.getText();
                }
                booksList.setListData(searchBooks(title, author, ISBN, genre));
            }  
        });  

        JPanel cartOuterPanel = new JPanel(new GridLayout(1, 2));
        middle.add(cartOuterPanel);

        JPanel cartButtonsPanel = new JPanel(new GridLayout(5, 1));
        cartOuterPanel.add(cartButtonsPanel);

        JButton addToCartButton = new JButton("Add to Cart");
        cartButtonsPanel.add(addToCartButton);

        addToCartButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {      
                if(booksList.getSelectedValue() != null){
                    boolean alreadyAdded = false;
                    for(int i = 0; i < cart.size(); i++){
                        if(cart.get(i).getISBN().equals(booksList.getSelectedValue().getISBN())){
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if(!alreadyAdded){
                        cart.add(booksList.getSelectedValue());
                        booksList.getSelectedValue().addToCart();
                    }
                    cartList.setListData(cart);
                }
            }  
        });  

        JButton increaseQuantityCartButton = new JButton("Increase quantity in Cart");
        increaseQuantityCartButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {  
                if(cartList.getSelectedValue() != null){
                    if(cartList.getSelectedValue().getStock() > cartList.getSelectedValue().getInCart()){
                        cartList.getSelectedValue().addToCart();
                        cartList.setListData(cart);
                    }
                }
            }  
        });  
        cartButtonsPanel.add(increaseQuantityCartButton);  

        JButton decreaseQuantityCartButton = new JButton("Decrease quantity in Cart");
        decreaseQuantityCartButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {  
                if(cartList.getSelectedValue() != null){
                    cartList.getSelectedValue().removeFromCart();
                    if(cartList.getSelectedValue().getInCart() <= 0){
                        cart.remove(cartList.getSelectedValue());
                    }
                    cartList.setListData(cart);
                }
            }  
        });  
        cartButtonsPanel.add(decreaseQuantityCartButton);        

        cart = new Vector<Book>();
        cartList = new JList<Book>();        
        cartList.setBounds(30, 40, 200, 300);
        cartList.setVisibleRowCount(10);
        cartList.setListData(cart);
        cartOuterPanel.add(cartList);


        frame.setVisible(true);
    }
}