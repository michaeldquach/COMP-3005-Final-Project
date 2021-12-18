import java.sql.Connection;
import java.sql.Date;
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

public class BookStore {
    static String url = "jdbc:postgresql://localhost:5432/bookstore";
    static String userid = "postgres";
    static String password = "Modular1/1!";
    static JList<Book> booksList;
    static JList<Book> cartList;
    static Vector<Book> cart;
    static JFrame frame;
    static JLabel titleValue, ISBNValue, priceValue, stockValue, authorValue, genreValue, publisherValue, pagesValue, usernameValue;
    static String usernameLoggedIn;

    boolean isOwner;

    public BookStore(){
        isOwner = true;
    }

    public static void main(String args[]){
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

    public static void confirmOrder(){
        if(cart.size() <= 0){
            return;
        }

        JTextField shippingAddress = new JTextField();
        JTextField billingAddress = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Shipping Address"), shippingAddress,
                new JLabel("Billing Address"), billingAddress
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Confirm Order", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if(usernameLoggedIn == null){
                JOptionPane.showConfirmDialog(null, new JLabel("You need to be logged in to place an order."), "Order Not Completed", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            if((!shippingAddress.getText().equals(null) && !shippingAddress.getText().equals("")) && (!billingAddress.getText().equals(null) && !billingAddress.getText().equals(""))){
                int[] tracking = sendOrder(usernameLoggedIn, shippingAddress.getText(), billingAddress.getText());
                if(tracking[0] != -1 && tracking[1] != -1){
                    JOptionPane.showConfirmDialog(null, new JLabel("Order successfully placed. Your order number is " + tracking[0] + ", your tracking number is " + tracking[1]), "Order Confirmation", JOptionPane.PLAIN_MESSAGE);
                }
            }
            else{
                JOptionPane.showConfirmDialog(null, new JLabel("Please fill in order details."), "Order Not Completed", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    public static int[] sendOrder(String userID, String billing, String shipping){
        int[] result = {-1, -1};
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement orderStmt = conn.prepareStatement(
                "INSERT INTO orders (user_id, total, billing_address, shipping_address, order_status, date) VALUES(?, 0, ?, ?, 'Order Placed', ?) RETURNING order_id, tracking_number;"
            );
            orderStmt.setString(1, userID);
            orderStmt.setString(2, billing);
            orderStmt.setString(3, shipping);
            orderStmt.setDate(4, new java.sql.Date(new java.util.Date().getTime()));
            ResultSet orderRset = orderStmt.executeQuery();

            int orderID = -1;            
            int tracking_number = -1;
            while(orderRset.next()){
                orderID = orderRset.getInt("order_id");
                tracking_number = orderRset.getInt("tracking_number");
            }

            PreparedStatement suborderStmt;
            for(int i = 0; i < cart.size(); i++){
                suborderStmt = conn.prepareStatement(
                    "INSERT INTO suborders (suborder_id, order_id, ISBN, quantity) VALUES(?, ?, ?, ?);"
                );
                suborderStmt.setInt(1, i + 1);
                suborderStmt.setInt(2, orderID);
                suborderStmt.setString(3, cart.get(i).getISBN());
                suborderStmt.setInt(4, cart.get(i).getInCart());
                suborderStmt.execute();
            }

            emptyCart();
            cartList.setListData(cart);
            booksList.setListData(getBooks());    
            result[0] = orderID;
            result[1] = tracking_number;   
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }     
        return result;
    }

    public static void showLogInPopup(){
        JTextField usernameTextField = new JTextField();
        JPasswordField passwordTextField = new JPasswordField();
        final JComponent[] inputs = new JComponent[] {
            new JLabel("Username"),
            usernameTextField,
            new JLabel("Password"),
            passwordTextField
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "User Login", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if(userLogin(usernameTextField.getText())){       
                usernameLoggedIn = usernameTextField.getText();
                JOptionPane.showConfirmDialog(null, new JLabel("Logged in as " + usernameLoggedIn), "Successful Login", JOptionPane.PLAIN_MESSAGE);
                usernameValue.setText("Logged in as: " + usernameLoggedIn);
                emptyCart();
                cartList.setListData(cart);
            }
            else{                
                JOptionPane.showConfirmDialog(null, new JLabel("Please enter a correct username and password."), "Login Failed", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    public static boolean userLogin(String username){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement orderStmt = conn.prepareStatement(
                "SELECT * FROM users WHERE users.user_id = ?"
            );
            orderStmt.setString(1, username);
            ResultSet orderRset = orderStmt.executeQuery();

            return orderRset.next();
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
        return false;
    }

    public static void showRegisterUserPopUp(){
        JTextField regUsernameField = new JTextField();
        JPasswordField regPasswordField = new JPasswordField();
        JTextField regFirstName = new JTextField();
        JTextField regLastName = new JTextField();
        JTextField regEmail = new JTextField();
        JTextField regBilling = new JTextField();
        JTextField regShipping = new JTextField();
        final JComponent[] inputs = new JComponent[] {
            new JLabel("Username: "), regUsernameField,
            new JLabel("Password"), regPasswordField,
            new JLabel("First Name: "), regFirstName,
            new JLabel("Last Name: "), regLastName,
            new JLabel("Email: "), regEmail,
            new JLabel("Billing Address: "), regBilling,
            new JLabel("Shipping Address: "),regShipping
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Register New User", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if(!regUsernameField.getText().equals(null) && !regUsernameField.getText().equals("") && !regFirstName.getText().equals(null) && !regFirstName.getText().equals("")
            && !regLastName.getText().equals(null) && !regLastName.getText().equals("")  && !regEmail.getText().equals(null) && !regEmail.getText().equals("")
            && !regBilling.getText().equals(null) && !regBilling.getText().equals("") && !regShipping.getText().equals(null) && !regShipping.getText().equals("")){
                if(registerUser(regUsernameField.getText(), regFirstName.getText(), regLastName.getText(), regEmail.getText(), regBilling.getText(), regShipping.getText())){
                    JOptionPane.showConfirmDialog(null, new JLabel("User successfully registered."), "User Registration Success", JOptionPane.PLAIN_MESSAGE);
                }
                else{
                    JOptionPane.showConfirmDialog(null, new JLabel("User with this username or email already exists."), "User Registration Failed", JOptionPane.PLAIN_MESSAGE);
                }
            }
            else{
                JOptionPane.showConfirmDialog(null, new JLabel("Please fill in all required fields."), "User Registration Failed", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    public static boolean registerUser(String username, String firstName, String lastName, String email, String billing, String shipping){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement orderStmt = conn.prepareStatement(
                "INSERT INTO users (user_id, user_first_name, user_last_name, user_email, user_billing_address, user_shipping_address) VALUES(?, ?, ?, ?, ?, ?);"
            );
            orderStmt.setString(1, username);
            orderStmt.setString(2, firstName);
            orderStmt.setString(3, lastName);
            orderStmt.setString(4, email);
            orderStmt.setString(5, billing);
            orderStmt.setString(6, shipping);
            orderStmt.execute();
            return true;
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
        return false;
    }

    public static void trackOrder(int orderNumber){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement trackStmt = conn.prepareStatement(
                "SELECT * FROM orders NATURAL JOIN suborders NATURAL JOIN books WHERE order_id = ?"
            );
            trackStmt.setInt(1, orderNumber);
            ResultSet trackRset = trackStmt.executeQuery();

            if(!trackRset.next()){
                JOptionPane.showConfirmDialog(null, new JLabel("Could not find order with this order number."), "Track Order Error", JOptionPane.PLAIN_MESSAGE);
            }

            double total = trackRset.getDouble("total");
            int trackingNumber = trackRset.getInt("tracking_number");
            String shippingAddress = trackRset.getString("shipping_address");
            String orderStatus = trackRset.getString("order_status");
            Date date = trackRset.getDate("date");
            Vector<String> orderedBooks = new Vector<>();
            orderedBooks.add(trackRset.getString("title") + ", " + trackRset.getInt("quantity"));

            while(trackRset.next()){
                orderedBooks.add(trackRset.getString("title") + ", " + trackRset.getInt("quantity"));
            }

            final JComponent[] inputs = new JComponent[] {
                    new JLabel("Order ID: " + orderNumber),
                    new JLabel("Tracking ID: " + trackingNumber),
                    new JLabel("Total: $" + total),
                    new JLabel("Shipping Address: " + shippingAddress),
                    new JLabel("Order Status: " + orderStatus),
                    new JLabel("Placed On: " + date),
                    new JComboBox<String>(orderedBooks)
            };
            JOptionPane.showConfirmDialog(null, inputs, "Track Order", JOptionPane.PLAIN_MESSAGE);
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    public static void emptyCart(){
        for(int i = 0; i < cart.size(); i++){
            cart.get(i).empty();
        }
        cart.clear();
    }

    public static void initiateView(){
        frame = new JFrame("Look Inna Book - Customer Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel main = new JPanel(new GridLayout(2, 1));
        main.setSize(800, 600);
        frame.add(main);

        JPanel bookCollectionPanel = new JPanel(new GridLayout(1, 2));
        main.add(bookCollectionPanel);

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

        bookCollectionPanel.add(booksList);
        bookCollectionPanel.add(new JScrollPane(booksList));

        JPanel bookDetails = new JPanel(new GridLayout(8,2));
        bookCollectionPanel.add(bookDetails);
     
        JLabel informationLabel = new JLabel("Book Information:");
        bookDetails.add(informationLabel, 0);

        titleValue = new JLabel("");
        bookDetails.add(titleValue, 1);

        bookDetails.add(new JLabel("ISBN: "), 2);

        ISBNValue = new JLabel("");
        bookDetails.add(ISBNValue, 3);

        bookDetails.add(new JLabel("Price: "), 4);

        priceValue = new JLabel("");
        bookDetails.add(priceValue, 5);

        bookDetails.add(new JLabel("Stock: "), 6);

        stockValue = new JLabel("");
        bookDetails.add(stockValue, 7);

        bookDetails.add(new JLabel("Author: "), 8);

        authorValue = new JLabel("");
        bookDetails.add(authorValue, 9);

        bookDetails.add(new JLabel("Genre: "), 10);

        genreValue = new JLabel("");
        bookDetails.add(genreValue, 11);
        
        bookDetails.add(new JLabel("Publisher: "), 12);

        publisherValue = new JLabel("");
        bookDetails.add(publisherValue, 13);

        bookDetails.add(new JLabel("Pages: "), 14);

        pagesValue = new JLabel("");
        bookDetails.add(pagesValue, 15);

        JPanel customerInterfacePanel = new JPanel(new GridLayout(2,1));
        main.add(customerInterfacePanel);

        JPanel searchPanel = new JPanel(new GridLayout(2,5));
        customerInterfacePanel.add(searchPanel);

        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(new JLabel("Title: "));
        searchPanel.add(new JLabel("Author: "));
        searchPanel.add(new JLabel("ISBN: "));
        searchPanel.add(new JLabel("Genre: "));

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

        JPanel cartPanel = new JPanel(new GridLayout(1, 2));
        customerInterfacePanel.add(cartPanel);

        JPanel cartButtonsPanel = new JPanel(new GridLayout(6, 1));
        cartPanel.add(cartButtonsPanel);        

        JPanel userPanel = new JPanel(new GridLayout(1,3));
        cartButtonsPanel.add(userPanel);

        JButton loginUserButton = new JButton("Login");
        userPanel.add(loginUserButton);
        loginUserButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {      
                showLogInPopup();
            }  
        }); 

        JButton registerUserButton = new JButton("Register User");
        registerUserButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                showRegisterUserPopUp();
            }  
        });  
        userPanel.add(registerUserButton);

        usernameValue = new JLabel("Not Logged In");
        userPanel.add(usernameValue);

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
        cartPanel.add(cartList);        

        JButton orderButton = new JButton("Order");
        orderButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                confirmOrder();
            }  
        });  
        cartButtonsPanel.add(orderButton);               

        JPanel trackingPanel = new JPanel(new GridLayout(1,3));
        cartButtonsPanel.add(trackingPanel);        

        JTextField trackOrderField = new JTextField();
        trackOrderField.setToolTipText("Enter Order Number: ");
        trackingPanel.add(trackOrderField);

        JButton trackOrderButton = new JButton("Track Order");
        trackingPanel.add(trackOrderButton);
        trackOrderButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                try{
                    trackOrder(Integer.parseInt(trackOrderField.getText()));
                }
                catch(Exception numberFormatException){
                    System.out.println("Exception: " + numberFormatException);
                }
            }  
        });  

        frame.setVisible(true);
    }
}