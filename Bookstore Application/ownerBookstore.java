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

public class OwnerBookstore extends Bookstore {
    private JList<Book> booksList;
    private Vector<Genre> genreList;
    private Vector<Publisher> publisherList;
    private Vector<Author> authorList;
    private JPanel mainPane;
    private JLabel titleInfo, ISBNInfo, priceInfo, stockInfo, authorInfo, genreInfo, publisherInfo, pagesInfo, availableInfo;

    public OwnerBookstore(){
        this.genreList = new Vector<>();
        this.publisherList = new Vector<>();
        this.authorList = new Vector<>();
    }

    //Queries database for book collection and populates booklist
    public Vector<Book> getBooks(){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT title, ISBN, price, stock FROM books;"
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

    //Queries database for specific information about selected book's ISBN
    public void getBookInformation(String ISBN){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT books.title, books.ISBN, authors.author_name, genres.genre_name, publishers.publisher_name, books.num_pages, books.price, books.stock, books.available FROM books natural join genres natural join book_author natural join authors natural join publishers WHERE books.ISBN = ?;"
            );
            pStmt.setString(1, ISBN);
            ResultSet rset = pStmt.executeQuery();

            int i = 0;

            while(rset.next()){
                if(i <= 0){
                    titleInfo.setText(rset.getString("title"));
                    ISBNInfo.setText(rset.getString("ISBN"));
                    priceInfo.setText(String.format("$%.2f", rset.getDouble("Price")));
                    stockInfo.setText(Integer.toString(rset.getInt("Stock")));
                    authorInfo.setText(rset.getString("author_name"));
                    genreInfo.setText(rset.getString("genre_name"));
                    publisherInfo.setText(rset.getString("publisher_name"));
                    pagesInfo.setText(Integer.toString(rset.getInt("num_pages")));
                    if(rset.getBoolean("available")){
                        availableInfo.setText("Available in storefront");
                    }
                    else{
                        availableInfo.setText("Removed from storefront");
                    }
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

    //Toggles book's availability in storefront by ISBN
    //Updating its availability to false "removes" the book from the store
    public void toggleAvailability(String ISBN, boolean availability){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(             
                "SELECT * FROM update_availability(?, ?);"
            );
            pStmt.setString(1, ISBN);
            pStmt.setBoolean(2, availability);
            pStmt.execute();
            getBookInformation(ISBN);
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Displays popup for owner to input details for new genre
    public void showAddGenrePopup(){
        JTextField genreField = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Enter new genre name"), genreField
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Add new genre", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if(genreField.getText() != null && !genreField.getText().equals("")){
                if(addGenre(genreField.getText())){
                    JOptionPane.showConfirmDialog(null, new JLabel("Genre " + genreField.getText() + " successfully added."), "Genre Added", JOptionPane.PLAIN_MESSAGE);
                    retrieveGenres();
                }
                else{
                    JOptionPane.showConfirmDialog(null, new JLabel("Genre already exists."), "Genre not added", JOptionPane.PLAIN_MESSAGE);                    
                }
            }
            else{                
                JOptionPane.showConfirmDialog(null, new JLabel("Please fill in the required field."), "Genre not added", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    //Queries database to add new genre. Name must be unique
    public boolean addGenre(String genreName){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(             
                "INSERT INTO genres (genre_name) VALUES(?) RETURNING true;"
            );
            pStmt.setString(1, genreName);
            ResultSet rSet = pStmt.executeQuery();
            return rSet.next();
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }    
        return false;  
    }

    //Displays popup for owner to input details for new publisher
    public void showAddPublisherPopup(){
        JTextField publisherField = new JTextField();
        JTextField pubEmailField = new JTextField();
        JTextField pubPhoneField = new JTextField();
        JTextField pubAddressField = new JTextField();
        JTextField pubBankField = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Enter new publisher name"), publisherField,
                new JLabel("Enter email"), pubEmailField,
                new JLabel("Enter phone"), pubPhoneField,
                new JLabel("Enter address"), pubAddressField,
                new JLabel("Enter bank account"), pubBankField,
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Add new publisher", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if(publisherField.getText() != null && !publisherField.getText().equals("") 
            && pubEmailField.getText() != null && !pubEmailField.getText().equals("") 
            && pubPhoneField.getText() != null && !pubPhoneField.getText().equals("") 
            && pubAddressField.getText() != null && !pubAddressField.getText().equals("") 
            && pubBankField.getText() != null && !pubBankField.getText().equals("")){
                if(addPublisher(publisherField.getText(), pubEmailField.getText(), pubPhoneField.getText(), pubAddressField.getText(), pubBankField.getText())){
                    JOptionPane.showConfirmDialog(null, new JLabel("Publisher " + publisherField.getText() + " successfully added."), "Publisher Added", JOptionPane.PLAIN_MESSAGE);
                    retrievePublishers();
                }
                else{
                    JOptionPane.showConfirmDialog(null, new JLabel("Publisher already exists."), "Publisher not added", JOptionPane.PLAIN_MESSAGE);                    
                }
            }
            else{                
                JOptionPane.showConfirmDialog(null, new JLabel("Please fill in the required fields."), "Publisher not added", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    //Queries database to add new publisher. Name, email, bank account must be unique
    public boolean addPublisher(String publisherName, String publisherEmail, String publisherPhone, String publisherAddress, String publisherBankaccount){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(             
                "INSERT INTO publishers (publisher_name, publisher_email, publisher_phone, publisher_address, publisher_bank_account) VALUES(?, ?, ?, ?, ?) RETURNING true;"
            );
            pStmt.setString(1, publisherName);
            pStmt.setString(2, publisherEmail);
            pStmt.setString(3, publisherPhone);
            pStmt.setString(4, publisherAddress);
            pStmt.setString(5, publisherBankaccount);
            ResultSet rSet = pStmt.executeQuery();
            return rSet.next();
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }    
        return false;  
    }

    //Displays popup for owner to input details for new author
    public void showAddAuthorPopup(){
        JTextField authorField = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Enter new author name"), authorField
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Add new author", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if(authorField.getText() != null && !authorField.getText().equals("")){
                if(addAuthor(authorField.getText())){
                    JOptionPane.showConfirmDialog(null, new JLabel("Author " + authorField.getText() + " successfully added."), "Author Added", JOptionPane.PLAIN_MESSAGE);
                    retrieveAuthors();
                }
                else{
                    JOptionPane.showConfirmDialog(null, new JLabel("Author already exists."), "Author not added", JOptionPane.PLAIN_MESSAGE);                    
                }
            }
            else{                
                JOptionPane.showConfirmDialog(null, new JLabel("Please fill in the required field."), "Author not added", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    //Queries database to add new author. Name must be unique
    public boolean addAuthor(String authorName){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(             
                "INSERT INTO authors (author_name) VALUES(?) RETURNING true;"
            );
            pStmt.setString(1, authorName);
            ResultSet rSet = pStmt.executeQuery();
            return rSet.next();
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }    
        return false;  
    }

    //Displays popup for owner to fill in details to add new book to database. Must select from existing genres, publishers, authors
    //Can add genres, publishers, authors before adding new books
    //All fields must be filled in to add a new book
    public void showAddNewBookPopup(){
        retrieveGenres();
        retrieveAuthors();
        retrievePublishers();
        JTextField ISBNField = new JTextField();
        JTextField titleField = new JTextField();
        JComboBox<Genre> genreField = new JComboBox<>(genreList);
        JList<Author> authorField = new JList<>(authorList);
        authorField.setVisibleRowCount(5);
        JComboBox<Publisher> publisherField = new JComboBox<>(publisherList);
        JTextField pagesField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField publisherPercentageField = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Enter ISBN"), ISBNField,
                new JLabel("Enter Title"), titleField,
                new JLabel("Enter Genre"), genreField,
                new JLabel("Enter Author"), authorField,
                new JLabel("Enter Publisher"), publisherField,
                new JLabel("Enter Number of Pages"), pagesField,
                new JLabel("Enter Stock Quantity"), stockField,
                new JLabel("Enter Price"), priceField,
                new JLabel("Enter Publisher Percentage"), publisherPercentageField,
                new JScrollPane(authorField)
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Add new book", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            //Ensure no field is null
            if(ISBNField.getText() != null && !ISBNField.getText().equals("") 
            && titleField.getText() != null && !titleField.getText().equals("") 
            && genreField.getSelectedItem() != null && publisherField.getSelectedItem() != null && !authorField.getSelectedValuesList().isEmpty()
            && pagesField.getText() != null && !pagesField.getText().equals("") 
            && stockField.getText() != null && !stockField.getText().equals("") 
            && priceField.getText() != null && !priceField.getText().equals("") 
            && publisherPercentageField.getText() != null && !publisherPercentageField.getText().equals("")
            ){
                //Add book with selected values
                if(addNewBook(ISBNField.getText(), titleField.getText(), ((Genre) genreField.getSelectedItem()).getGenreID(), ((Publisher) publisherField.getSelectedItem()).getPublisherID(),
                Integer.parseInt(pagesField.getText()), true, Integer.parseInt(stockField.getText()), Double.parseDouble(priceField.getText()), Double.parseDouble(publisherPercentageField.getText())
                )){                  
                    //Add each author selected  
                    for(int i = 0; i < authorField.getSelectedValuesList().size(); i++){
                        addBookAuthor(ISBNField.getText(), authorField.getSelectedValuesList().get(i).getAuthorID());
                    }
                    JOptionPane.showConfirmDialog(null, new JLabel("Book " + titleField.getText() + " successfully added."), "Book Added", JOptionPane.PLAIN_MESSAGE);
                    booksList.setListData(getBooks());
                }
                else{
                    JOptionPane.showConfirmDialog(null, new JLabel("Book already exists."), "Book not added", JOptionPane.PLAIN_MESSAGE);                    
                }
            }
            else{                
                JOptionPane.showConfirmDialog(null, new JLabel("Please fill in the required fields."), "Book not added", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    //Queries database to add author to specific book by ISBN
    public boolean addBookAuthor(String ISBN, int authorID){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(             
                "INSERT INTO book_author (ISBN, author_id) VALUES(?, ?) RETURNING true;"
            );
            pStmt.setString(1, ISBN);
            pStmt.setInt(2, authorID);
            ResultSet rSet = pStmt.executeQuery();
            return rSet.next();
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }    
        return false;
    }

    //Queries database to add new book according to owner input. ISBN must be unique, all fields must be filled.
    public boolean addNewBook(String ISBN, String title, int genreID, int publisherID, int num_pages, boolean available, int stock, double price, double publisherPercentage){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(             
                "INSERT INTO books (ISBN, title, genre_id, publisher_id, num_pages, available, stock, price, publisher_percentage) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING true;"
            );
            pStmt.setString(1, ISBN);
            pStmt.setString(2, title);
            pStmt.setInt(3, genreID);
            pStmt.setInt(4, publisherID);
            pStmt.setInt(5, num_pages);
            pStmt.setBoolean(6, available);
            pStmt.setInt(7, stock);
            pStmt.setDouble(8, price);
            pStmt.setDouble(9, publisherPercentage);
            ResultSet rSet = pStmt.executeQuery();
            return rSet.next();
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }    
        return false;  
    }

    //Queries database to obtain total sales report and display information in popup
    public void viewTotalSalesReport(Date startDate, Date endDate){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT sum(sales_per_day.total_sales) as total_sales, sum(sales_per_day.total_expenditures) as total_expenditures FROM sales_per_day WHERE date >= ? and date <= ?;"
            );  
            pStmt.setDate(1, startDate);
            pStmt.setDate(2, endDate);
            ResultSet rset = pStmt.executeQuery();

            Vector<String> report = new Vector<>();
            report.add(String.format("%-31s %-30s", "Total Sales", "Total Expenditures"));

            while(rset.next()){
                report.add(String.format("$%-30.2f $%-30.2f", rset.getDouble("total_sales"), rset.getDouble("total_expenditures")));
            }
            
            //Handling popup
            JList<String> reportList = new JList<>(report);
            reportList.setFont( new Font("monospaced", Font.PLAIN, 12));
            JOptionPane.showConfirmDialog(null, reportList, "Viewing Report: Total Sales vs Expenditures", JOptionPane.PLAIN_MESSAGE);
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Queries database to obtain total sales per genre report and display information in popup
    public void viewGenreSalesReport(Date startDate, Date endDate){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT sales_per_genre_by_day.genre_name, sum(sales_per_genre_by_day.total_sales) as total_sales, sum(sales_per_genre_by_day.total_expenditures) as total_expenditures FROM sales_per_genre_by_day WHERE date >= ? and date <= ? GROUP BY sales_per_genre_by_day.genre_name;"
            );  
            pStmt.setDate(1, startDate);
            pStmt.setDate(2, endDate);
            ResultSet rset = pStmt.executeQuery();

            Vector<String> report = new Vector<>();        
            report.add(String.format("%-30s %-31s %-30s", "Genre", "Total Sales", "Total Expenditures"));

            while(rset.next()){
                report.add(String.format("%-30s $%-30.2f $%-30.2f", rset.getString("genre_name"), rset.getDouble("total_sales"), rset.getDouble("total_expenditures")));
            }
            
            //Handling popup
            JList<String> reportList = new JList<>(report);
            reportList.setFont( new Font("monospaced", Font.PLAIN, 12));
            JOptionPane.showConfirmDialog(null, reportList, "Viewing Report: Total Sales vs Expenditures By Genre", JOptionPane.PLAIN_MESSAGE);
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Queries database to obtain total sales per author report and display information in popup
    public void viewAuthorSalesReport(Date startDate, Date endDate){
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT sales_per_author_by_day.author_name, sum(sales_per_author_by_day.total_sales) as total_sales, sum(sales_per_author_by_day.total_expenditures) as total_expenditures FROM sales_per_author_by_day WHERE date >= ? and date <= ? GROUP BY sales_per_author_by_day.author_name;"
            );  
            pStmt.setDate(1, startDate);
            pStmt.setDate(2, endDate);
            ResultSet rset = pStmt.executeQuery();

            Vector<String> report = new Vector<>();
            report.add(String.format("%-30s %-31s %-30s", "Author", "Total Sales", "Total Expenditures"));

            while(rset.next()){
                report.add(String.format("%-30s $%-30.2f $%-30.2f", rset.getString("author_name"), rset.getDouble("total_sales"), rset.getDouble("total_expenditures")));
            }
            
            //Handling popup
            JList<String> reportList = new JList<>(report);
            reportList.setFont( new Font("monospaced", Font.PLAIN, 12));
            JOptionPane.showConfirmDialog(null, reportList, "Viewing Report: Total Sales vs Expenditures By Author", JOptionPane.PLAIN_MESSAGE);
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Populates list of genres
    public void retrieveGenres(){
        genreList.clear();
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM genres;"
            );
            ResultSet rset = pStmt.executeQuery();

            while(rset.next()){
                genreList.add(new Genre(rset.getInt("genre_id"), rset.getString("genre_name")));
            }
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Populates list of authors
    public void retrieveAuthors(){
        authorList.clear();
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM authors;"
            );
            ResultSet rset = pStmt.executeQuery();

            while(rset.next()){
                authorList.add(new Author(rset.getInt("author_id"), rset.getString("author_name")));
            }
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Populates list of publishers
    public void retrievePublishers(){
        publisherList.clear();
        try{
            Connection conn = DriverManager.getConnection(url, userid, password);   
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM publishers;"
            );
            ResultSet rset = pStmt.executeQuery();

            while(rset.next()){
                publisherList.add(new Publisher(rset.getInt("publisher_id"), rset.getString("publisher_name"), rset.getString("publisher_email"), rset.getString("publisher_phone"), rset.getString("publisher_address"), rset.getString("publisher_bank_account")));
            }
        }
        catch (Exception sqlException){
            System.out.println("Exception: " + sqlException);
        }
    }

    //Initializes view components and handling of user inputs
    public void initializeView(){
        frame = new JFrame("Look Inna Book - Owner Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        mainPane = new JPanel(new GridLayout(2, 1));
        mainPane.setSize(800, 600);
        frame.add(mainPane);

        initializeBookCollectionPanel();
        initializeOwnerInterfacePanel();

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
        JPanel bookDetails = new JPanel(new GridLayout(9,2));
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

        bookDetails.add(new JLabel("Availability: "), 16);
        availableInfo = new JLabel("");
        bookDetails.add(availableInfo, 17);
    }

    public void initializeOwnerInterfacePanel(){
        JPanel ownerInterfacePanel = new JPanel(new GridLayout(1, 2));
        mainPane.add(ownerInterfacePanel);

        JPanel manageCollectionPanel = new JPanel(new GridLayout(3, 1));
        ownerInterfacePanel.add(manageCollectionPanel);

        //Handling book availability management
        JPanel manageAvailabilityPanel = new JPanel(new GridLayout(1, 2));
        manageCollectionPanel.add(manageAvailabilityPanel);

        JButton addToStorefront = new JButton("Add Book to Storefront");
        manageAvailabilityPanel.add(addToStorefront);
        addToStorefront.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                if(booksList.getSelectedValue() != null){
                    toggleAvailability(booksList.getSelectedValue().getISBN(), true);
                }
            }  
        });  

        JButton removeFromStorefront = new JButton("Remove Book from Storefront");
        manageAvailabilityPanel.add(removeFromStorefront);
        removeFromStorefront.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                if(booksList.getSelectedValue() != null){
                    toggleAvailability(booksList.getSelectedValue().getISBN(), false);
                }
            }  
        });  

        //Handling genre, publisher, author management
        JPanel manageRelatedEntitiesPanel = new JPanel(new GridLayout(1, 3));
        manageCollectionPanel.add(manageRelatedEntitiesPanel);

        JButton addGenre = new JButton("Add Genre");
        manageRelatedEntitiesPanel.add(addGenre);
        addGenre.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                showAddGenrePopup();
            }  
        });  

        JButton addPublisher = new JButton("Add Publisher");
        manageRelatedEntitiesPanel.add(addPublisher);
        addPublisher.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                showAddPublisherPopup();
            }  
        });  

        JButton addAuthor = new JButton("Add Author");
        manageRelatedEntitiesPanel.add(addAuthor);
        addAuthor.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                showAddAuthorPopup();
            }  
        });  

        JButton addNewBook = new JButton("Add New Book");
        manageCollectionPanel.add(addNewBook);
        addNewBook.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                showAddNewBookPopup();
            }  
        });  

        //Handling report viewing
        JPanel manageReportPanel = new JPanel(new GridLayout(3, 1));
        ownerInterfacePanel.add(manageReportPanel);

        String[] reportTypes = {"Sales vs Expenditures", "Sales per Genre", "Sales per Author"};
        JComboBox<String> selectReportType = new JComboBox<>(reportTypes);
        manageReportPanel.add(selectReportType);

        JPanel manageDatePanel = new JPanel(new GridLayout(2,2));
        manageReportPanel.add(manageDatePanel);

        manageDatePanel.add(new JLabel("Enter Start Date: "));
        manageDatePanel.add(new JLabel("Enter End Date: "));

        JTextField startDateField = new JTextField();
        manageDatePanel.add(startDateField);

        JTextField endDateField = new JTextField();
        manageDatePanel.add(endDateField);

        JButton viewReportButton = new JButton("View Report");
        manageReportPanel.add(viewReportButton);
        viewReportButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) { 
                try{
                    switch(selectReportType.getSelectedIndex()){
                        case(0):
                            viewTotalSalesReport(Date.valueOf(startDateField.getText()), Date.valueOf(endDateField.getText()));
                            break;
                        
                        case(1):
                            viewGenreSalesReport(Date.valueOf(startDateField.getText()), Date.valueOf(endDateField.getText()));
                            break;
                        
                        case(2):
                            viewAuthorSalesReport(Date.valueOf(startDateField.getText()), Date.valueOf(endDateField.getText()));
                            break;
                        
                        default:
                            viewTotalSalesReport(Date.valueOf(startDateField.getText()), Date.valueOf(endDateField.getText()));
                            break;
                    }
                }
                catch(Exception err){
                    System.out.println("Invalid Date");
                }
            }  
        });  
    }
}