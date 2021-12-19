import javax.swing.JFrame;

public abstract class Bookstore {
    protected static final String url = "jdbc:postgresql://localhost:5432/bookstore";
    protected static final String userid = "postgres";
    protected static final String password = "password";
    protected JFrame frame;
    
    public abstract void initializeView();    

    public void close(){
        frame.dispose();
    }
}
