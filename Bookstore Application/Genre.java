public class Genre {
    private int genreID;
    private String genreName;

    public Genre(int genreID, String genreName){
        this.genreID = genreID;
        this.genreName = genreName;
    }

    public String toString(){
        return genreName;
    }

    public int getGenreID(){
        return genreID;
    }

    public String getGenreName(){
        return genreName;
    }
}
