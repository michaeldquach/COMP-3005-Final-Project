public class Book {
    private String title;
    private String ISBN;
    private double price;
    private int stock;
    private int inCart;

    public Book(String title, String ISBN, double price, int stock){
        this.title = title;
        this.ISBN = ISBN;
        this.price = price;
        this.stock = stock;
        this.inCart = 0;
    }

    public void addToCart(){
        this.inCart++;
    }

    public void removeFromCart(){
        if(this.inCart > 0){
            this.inCart--;
        }
    }

    public void empty(){
        this.inCart = 0;
    }

    public String toString(){
        //return String.format("%s, %s, %.2f, %d", title, ISBN, price, stock);
        return title + ", In cart: " + inCart;
    }

    public String getTitle(){
        return title;
    }

    public String getISBN(){
        return ISBN;
    }

    public int getStock(){
        return stock;
    }

    public int getInCart(){
        return inCart;
    }
}
