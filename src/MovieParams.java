import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class MovieParams {
    HashMap<String, String> params = new HashMap<>();

    private String movieTitleParam;
    private String movieYearParam;
    private String movieDirectorParam;
    private String starNameParam;
    private String genreIdParam;
    private String pageParam;
    private String limitParam;
    private String sortPriorityParam;
    private String ratingOrderParam;
    private String titleOrderParam;
    private String queryParam;

    public void fillParams(HttpServletRequest request) {
        String[] parameters = new String[] {
                "movieTitle",
                "movieYear",
                "movieDirector",
                "starName",
                "genreId",
                "page",
                "limit",
                "sortPriority",
                "ratingOrder",
                "titleOrder",
                "query"
        };

        for (String p : parameters) {
            System.out.println("checking parameter: " + p);
            String temp = request.getParameter(p);

            if (temp != null && !temp.equals("null")) {
                System.out.println("putting " + temp + " w/ " + p);
                this.params.put(p, temp);
            }
        }

        movieTitleParam    = request.getParameter("movieTitle");
        movieYearParam     = request.getParameter("movieYear");
        movieDirectorParam = request.getParameter("movieDirector");
        starNameParam      = request.getParameter("starName");
        genreIdParam       = request.getParameter("genreId");
        pageParam          = request.getParameter("page");
        limitParam         = request.getParameter("limit");
        sortPriorityParam  = request.getParameter("sortPriority");
        ratingOrderParam   = request.getParameter("ratingOrder");
        titleOrderParam    = request.getParameter("titleOrder");
        queryParam         = request.getParameter("query");

        if (movieTitleParam == null || movieTitleParam.equals("null")) { movieTitleParam = ""; }
        if (movieYearParam == null || movieYearParam.equals("null")) { movieYearParam = ""; }
        if (movieDirectorParam == null || movieDirectorParam.equals("null")) { movieDirectorParam = ""; }
        if (starNameParam == null || starNameParam.equals("null")) { starNameParam = ""; }
        if (genreIdParam == null || genreIdParam.equals("null")) { starNameParam = ""; }
        if (pageParam == null || pageParam.equals("null")) { pageParam = "1"; }
        if (limitParam == null || limitParam.equals("null")) { limitParam = "10"; }
        if (sortPriorityParam == null || sortPriorityParam.equals("null")) { sortPriorityParam = "rating"; }
        if (ratingOrderParam == null || ratingOrderParam.equals("null")) { ratingOrderParam = "desc"; }
        if (titleOrderParam == null || titleOrderParam.equals("null")) { titleOrderParam = "asc"; }
        if (queryParam == null || queryParam.equals("null")) { queryParam = ""; }
    }


    // Accessors
    public String getTitle() { return movieTitleParam; }
    public String getYear() { return movieYearParam; }
    public String getDirector() { return movieDirectorParam; }
    public String getStarName() { return starNameParam; }
    public String getGenreId() { return genreIdParam; }
    public String getPage() { return pageParam; }
    public String getLimit() { return limitParam; }
    public String getSortPriority() { return sortPriorityParam; }
    public String getRatingOrder() { return ratingOrderParam; }
    public String getTitleOrder() { return titleOrderParam; }
    public String getQuery() { return queryParam; }

    public boolean titleNotEmpty() { return !movieTitleParam.equals("null") && !movieTitleParam.isEmpty(); }
    public boolean yearNotEmpty() { return !movieYearParam.equals("null") && !movieYearParam.isEmpty(); }
    public boolean directorNotEmpty() { return !movieDirectorParam.equals("null") && !movieDirectorParam.isEmpty(); }
    public boolean starNameNotEmpty() { return !starNameParam.equals("null") && !starNameParam.isEmpty(); }
    public boolean queryNotEmpty() { return !queryParam.equals("null") && !queryParam.isEmpty(); }

    public boolean isSortedByRating() {
        final String RATING_FIRST = "rating";
        return sortPriorityParam.equals(RATING_FIRST);
    }

    public int calcOffsetVal() {
        int offset = 0;

        if (pageParam != null && !pageParam.equals("null")) {
            offset = Integer.parseInt(limitParam) * (Integer.parseInt(pageParam) - 1);
        }

        return offset;
    }

    public String toString() {
        String res = "";

        for (String k : params.keySet()) {
            res += String.format("%s: %s\n", k, params.get(k));
        }

        return res;
    }
}
