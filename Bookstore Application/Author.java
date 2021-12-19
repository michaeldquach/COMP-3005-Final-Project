public class Author {
    private int authorID;
    private String authorName;

    public Author(int authorID, String authorName){
        this.authorID = authorID;
        this.authorName = authorName;
    }

    public String toString(){
        return authorName;
    }

    public int getAuthorID(){
        return authorID;
    }

    public String getAuthorName(){
        return authorName;
    }
}
