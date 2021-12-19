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

public class customerBookstore extends Bookstore{
    private JList<Book> booksList;
    private JList<Book> cartList;
    private Vector<Book> cart;
    private JFrame frame;
    private JPanel mainPane, customerInterfacePanel, cartPanel, cartButtonsPanel;
    private JLabel titleInfo, ISBNInfo, priceInfo, stockInfo, authorInfo, genreInfo, publisherInfo, pagesInfo, usernameInfo;
    private String usernameLoggedIn;

    public customerBookstore(){
        cart = new Vector<>();
    }

    //Queries database for book collection available to customer and populates booklist
    public Vector<Book> getBooks(){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT title, ISBN, price, stock FROM books WHERE books.available = true;"
            );
            ResultSet rset = pStmt.executeQuery();
            Vector<Book> books = new Vector<Book>();

            while(rset.next()){
                books.add(new Book(rset.getString("title"), rset.getString("ISBN"), rset.getDouble("price"), rset.getInt("stock")));
            }
            return books;
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
        return null;
    }

    //Queries database for book collection available to customer narrowed down to parameters and populates booklist
    public Vector<Book> searchBooks(String title, String author, String ISBN, String genre){
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
            Vector<Book> books = new Vector<Book>();

            while(rset.next()){
                books.add(new Book(rset.getString("book_title"), rset.getString("book_ISBN"), rset.getDouble("book_price"), rset.getInt("book_stock")));
            }

            return books;
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
        return null;

    }

    //Queries database for specific information about selected book's ISBN
    public void getBookInformation(String ISBN){
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
                    titleInfo.setText(rset.getString("title"));
                    ISBNInfo.setText(rset.getString("ISBN"));
                    priceInfo.setText(Double.toString(rset.getDouble("Price")));
                    stockInfo.setText(Integer.toString(rset.getInt("Stock")));
                    authorInfo.setText(rset.getString("author_name"));
                    genreInfo.setText(rset.getString("genre_name"));
                    publisherInfo.setText(rset.getString("publisher_name"));
                    pagesInfo.setText(Integer.toString(rset.getInt("num_pages")));
                }
                else{
                    //Multiple tuples returned when more than one author - append each additional author
                    authorInfo.setText(authorInfo.getText() + ", " + rset.getString("author_name"));
                }
                i++;
            }
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Shows popup to user to input shipping and billing details. Invokes sendOrder() if details are filled out
    public void showOrderConfirmationPopUp(){
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

    //Queries database to insert new order and corresponding suborders based on user's cart
    public int[] sendOrder(String userID, String billing, String shipping){
        int[] result = {-1, -1};
        try{
            //First we insert the order with the user details
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

            //Then for each item in the cart, we insert a suborder with the corresponding book and ordered quantities
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

            //Once the order and suborders have succeeded, we clear the cart and return to the user the order and tracking id
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

    //Displays a login popup to the user so that they can enter their username and password.
    //Note that password handling is out of scope for the project - users can simply log in by entering just their username
    public void showLogInPopup(){
        JTextField usernameTextField = new JTextField();
        JPasswordField passwordTextField = new JPasswordField();
        final JComponent[] inputs = new JComponent[] {
            new JLabel("Username"), usernameTextField,
            new JLabel("Password"), passwordTextField
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "User Login", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            //Invokes user login with inputted username
            if(userLogin(usernameTextField.getText())){       
                usernameLoggedIn = usernameTextField.getText();
                JOptionPane.showConfirmDialog(null, new JLabel("Logged in as " + usernameLoggedIn), "Successful Login", JOptionPane.PLAIN_MESSAGE);
                usernameInfo.setText("Logged in as: " + usernameLoggedIn);
                emptyCart();
                cartList.setListData(cart);
            }
            else{                
                JOptionPane.showConfirmDialog(null, new JLabel("Please enter a correct username and password."), "Login Failed", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    //Queries database to check if a user with the given username exists, and logs that user in.
    //Note that password handling is out of scope for the project - users can simply log in by entering just their username
    public boolean userLogin(String username){
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

    //Displays a popup where details for a new user can be input for registration
    //Invokes registerUser() with those inputs
    public void showRegisterUserPopUp(){
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
            //Ensure that no field is empty
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

    //Queries the database to insert a new user with given details for registration.
    //When registering a user, it must be the case that the username and/or email don't already exist in the database
    public boolean registerUser(String username, String firstName, String lastName, String email, String billing, String shipping){
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

    //Queries database to retrieve an order and its corresponing suborders based on a orderID input
    //Displays details on that order (e.g. tracking IDs, total, what books were ordered and how many)
    public void trackOrder(int orderNumber){
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

            //Add books from multiple suborders
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

    //Reset the user's cart, removing all items from cart
    //Invoked when logging in to new user, or when order has succeeded
    public void emptyCart(){
        for(int i = 0; i < cart.size(); i++){
            cart.get(i).empty();
        }
        cart.clear();
    }

    //Initializes view components and handling of user inputs
    public void initializeView(){
        frame = new JFrame("Look Inna Book - Customer Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        mainPane = new JPanel(new GridLayout(2, 1));
        mainPane.setSize(800, 600);
        frame.add(mainPane);

        initializeBookCollectionPanel();

        customerInterfacePanel = new JPanel(new GridLayout(2,1));
        mainPane.add(customerInterfacePanel);

        initializeSearchBarPanel();
        
        cartPanel = new JPanel(new GridLayout(1, 2));
        customerInterfacePanel.add(cartPanel);

        cartButtonsPanel = new JPanel(new GridLayout(6, 1));
        cartPanel.add(cartButtonsPanel);    

        initializeUserLoginPanel();
        initializeCartPanel();
        initializeTrackingPanel();

        frame.setVisible(true);
    }

    
    //Handling viewable book collection and book details
    public void initializeBookCollectionPanel(){
        JPanel bookCollectionPanel = new JPanel(new GridLayout(1, 2));
        mainPane.add(bookCollectionPanel);

        //Populates book collection with available books in the storefront
        booksList = new JList<Book>(getBooks());
        //Custom renderer so book titles are present in the list
        booksList.setCellRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(renderer instanceof JLabel && value instanceof Book){
                    ((JLabel) renderer).setText(((Book) value).getTitle());
                }
                return renderer;
            }
        });

        //Populates book information panel with selected book
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


        //Initialization of book detail labels
        JPanel bookDetails = new JPanel(new GridLayout(8,2));
        bookCollectionPanel.add(bookDetails);
     
        JLabel informationLabel = new JLabel("Book Information:");
        bookDetails.add(informationLabel, 0);

        titleInfo = new JLabel("");
        bookDetails.add(titleInfo, 1);

        bookDetails.add(new JLabel("ISBN: "), 2);
        ISBNInfo = new JLabel("");
        bookDetails.add(ISBNInfo, 3);

        bookDetails.add(new JLabel("Price: "), 4);
        priceInfo = new JLabel("");
        bookDetails.add(priceInfo, 5);

        bookDetails.add(new JLabel("Stock: "), 6);
        stockInfo = new JLabel("");
        bookDetails.add(stockInfo, 7);

        bookDetails.add(new JLabel("Author: "), 8);
        authorInfo = new JLabel("");
        bookDetails.add(authorInfo, 9);

        bookDetails.add(new JLabel("Genre: "), 10);
        genreInfo = new JLabel("");
        bookDetails.add(genreInfo, 11);
        
        bookDetails.add(new JLabel("Publisher: "), 12);
        publisherInfo = new JLabel("");
        bookDetails.add(publisherInfo, 13);

        bookDetails.add(new JLabel("Pages: "), 14);
        pagesInfo = new JLabel("");
        bookDetails.add(pagesInfo, 15);
    }

    //Handling of search bar
    public void initializeSearchBarPanel(){
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

        //Invokes searchBooks() based on user inputs
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
    }
 
    //Handling user login interface
    public void initializeUserLoginPanel(){     
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

        usernameInfo = new JLabel("Not Logged In");
        userPanel.add(usernameInfo);
    }
    
    //Handling of cart interface
    public void initializeCartPanel(){   
        JButton addToCartButton = new JButton("Add to Cart");
        cartButtonsPanel.add(addToCartButton);

        //Handles adding an item from the viewable storefront to the user's cart
        addToCartButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {      
                if(booksList.getSelectedValue() != null){
                    boolean alreadyAdded = false;
                    //Check that the item isn't already added
                    for(int i = 0; i < cart.size(); i++){
                        if(cart.get(i).getISBN().equals(booksList.getSelectedValue().getISBN())){
                            alreadyAdded = true;
                            break;
                        }
                    }
                    //If it isn't we add it to the cart and increment the amount in the cart
                    if(!alreadyAdded){
                        cart.add(booksList.getSelectedValue());
                        booksList.getSelectedValue().addToCart();
                    }
                    cartList.setListData(cart);
                }
            }  
        });  

        //Handles increasing cart quantity of selected book
        //Note that the book in the CART needs to be selected in order to increase its quantity - don't select the book in the storefront collection
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

        //Handles decreasing cart quantity of selected book
        //Note that the book in the CART needs to be selected in order to decrease its quantity - don't select the book in the storefront collection
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
                showOrderConfirmationPopUp();
            }  
        });  
        cartButtonsPanel.add(orderButton);   
    }

    //Handling tracking interface
    public void initializeTrackingPanel(){   
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
    }

    public void close(){
        frame.dispose();
    }
}