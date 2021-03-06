public class Book {
    private String title;
    private String ISBN;
    private int stock;
    private int inCart;

    public Book(String title, String ISBN, double price, int stock){
        this.title = title;
        this.ISBN = ISBN;
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
