import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Movie {
    // Attributes
    private String movieId = "";
    private String movieIdDB = "";
    private String movieTitle = "";
    private int    movieYear = 0;
    private String movieDirector = "";
    private List<String> genreList;
    private boolean hasStars = false;


    // Constructors
    public Movie() { genreList = new ArrayList<String>(); }
    public Movie(String i, String t, int y, String d) {
        this.movieId = i;
        this.movieTitle = t;
        this.movieYear = y;
        this.movieDirector = d;

        genreList = new ArrayList<String>();
    }

    // Accessors
    public String getMovieId() { return movieId; }
    public String getMovieIdDB() { return movieIdDB; }
    public String getMovieTitle() { return movieTitle; }
    public int getMovieYear() { return movieYear; }
    public String getMovieDirector() { return movieDirector; }
    public List<String> getGenres() { return genreList; }
    public boolean hasNoGenres() { return genreList.isEmpty(); }
    public boolean getHasStars() { return hasStars; }

    // Mutators
    public void setMovieId(String id) { this.movieId = id; }
    public void setMovieIdDB(String DBID) { this.movieIdDB = DBID; }
    public void setMovieTitle(String title) { this.movieTitle = title; }
    public void setMovieYear(int year) { this.movieYear = year; }
    public void setMovieDirector(String director) { this.movieDirector = director; }
    public void addGenre(String genre) {
        if (!genre.isBlank())
            this.genreList.add(genre);
    }

    public void foundStar() { hasStars = true; }

    // repr
    public String toString() {
        String repr = "Movie:";
        repr += "\n\tTitle: " + this.movieTitle;
        repr += "\n\tId: " + this.movieId;
        repr += "\n\tYear: " + Integer.toString(this.movieYear);
        repr += "\n\tDirector: " + this.movieDirector;
        repr += "\n\tGenres: ";

        for (String genre : genreList) {
            repr += genre + ", ";
        }

        return repr;
    }
}
