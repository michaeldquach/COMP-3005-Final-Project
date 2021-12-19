public abstract class Bookstore {
    protected static final String url = "jdbc:postgresql://localhost:5432/bookstore";
    protected static final String userid = "postgres";
    protected static final String password = "password";
    public abstract void initializeView();
    public abstract void close();
}
