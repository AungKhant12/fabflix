import java.util.Set;
import java.util.HashSet;

public class Star {
    private String       DBID;
    private String       name;
    private int          birthYear;
    private Set<String>  movieIdSet;

    // Constructors
    public Star() {
        name = "";
        birthYear = -1;
        movieIdSet  = new HashSet<String>();
    }

    // Accessors
    public String      getDBID() { return DBID; }
    public String      getName() { return name; }
    public int         getBirthYear() { return birthYear; }
    public Set<String> getMovieIdSet() { return movieIdSet; }

    // Mutators
    public void setDBID(String id) { this.DBID = id; }
    public void setName(String n) { this.name = n; }
    public void setBirthYear(String yearStr) {
        try {
            int y = Integer.parseInt(yearStr);
            this.birthYear = y;
        }
        catch (Exception e) {
            throw e;
        }
    }
    public void setBirthYear(int y) {
        this.birthYear = y;
    }

    public void addMovieId(String mId) {
        this.movieIdSet.add(mId);
    }

    // repr
    public String toString() {
        String repr = "Star:";
        repr += "\n\tID: " + this.DBID;
        repr += "\n\tName: " + this.name;
        if (birthYear != -1)
            repr += "\n\tYear: " + this.birthYear;

        if (this.movieIdSet.size() > 0)
            repr += "\n\tMovie Ids: ";
        for (String movieId : this.movieIdSet) {
            repr += movieId + ", ";
        }

        return repr;
    }
}
