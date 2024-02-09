import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        final String FUNC = "init";
        try {
            logToServer(FUNC, "Attempting to create new data source");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            logToServer(FUNC, "Created new data source");
        }
        catch (Exception e) {
            logToServer(FUNC, "Exception in init");
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String FUNC = "doGet";
        logToServer(FUNC, "Entered method");

        response.setContentType("application/json");
        final int OK_CODE = 200, INTERNAL_SERVER_ERROR_CODE = 500;

        // page parameter
        String page = request.getParameter("page");
        String limit = request.getParameter("limit");
        logToServer(FUNC, "page: " + page);
        int offsetVal = 0;
        if (!page.equals("null")) {
            offsetVal = Integer.parseInt(limit) * (Integer.parseInt(page) - 1);
        }
        logToServer(FUNC, "Calculated offset value");

        // sort parameters
        String sortPriorityParam = request.getParameter("sortPriority");
        if (sortPriorityParam == null || sortPriorityParam.equals("null")) { sortPriorityParam = "rating"; }

        if (sortPriorityParam.equals("rating")) { sortPriorityParam = "1"; }
        else { sortPriorityParam = "2"; }
        logToServer(FUNC, "sortPriorityParam: " + sortPriorityParam);

        String ratingOrderParam = request.getParameter("ratingOrder");
        if (ratingOrderParam == null || ratingOrderParam.equals("null")) { ratingOrderParam = "desc"; }
        else if (!ratingOrderParam.equals("desc") && !ratingOrderParam.equals("asc")) { ratingOrderParam = "desc"; }
        logToServer(FUNC, "ratingOrderParam: " + ratingOrderParam);

        String titleOrderParam = request.getParameter("titleOrder");
        if (titleOrderParam == null || titleOrderParam.equals("null")) { titleOrderParam = "desc"; }
        else if (!titleOrderParam.equals("desc") && !titleOrderParam.equals("asc")) { titleOrderParam = "desc"; }
        logToServer(FUNC, "ratingOrderParam: " + titleOrderParam);

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            logToServer(FUNC, "Created driver connection");

            final String QUERY = getMySQLQuery(sortPriorityParam, ratingOrderParam, titleOrderParam);
            PreparedStatement statement = conn.prepareStatement(QUERY);
            statement.setInt(1, Integer.parseInt(limit));
            statement.setInt(2, offsetVal);


            ResultSet resultSet = statement.executeQuery();
            logToServer(FUNC, "Executed query");

            JsonArray resultsArr = new JsonArray();

            while (resultSet.next()) {
                String movieTitle    = resultSet.getString("movie_title");
                String movieId       = resultSet.getString("movie_id");
                String movieYear     = resultSet.getString("movie_year");
                String movieDirector = resultSet.getString("movie_director");
                String starNameList  = resultSet.getString("star_name_list");
                String starIdList    = resultSet.getString("star_id_list");
                String genreNameList = resultSet.getString("genre_name_list");
                String genreIdList   = resultSet.getString("genre_id_list");
                String rating        = resultSet.getString("movie_rating");

                JsonObject resultObj = new JsonObject();

                resultObj.addProperty("movie_id", movieId);
                resultObj.addProperty("movie_title", movieTitle);
                resultObj.addProperty("movie_year", movieYear);
                resultObj.addProperty("movie_director", movieDirector);
                resultObj.addProperty("movie_rating", rating);

                String[] splitArr = starNameList.split(",");
                resultObj.addProperty("star_name_list", String.join(",", slice(splitArr, 0, 3)));
                splitArr = starIdList.split(",");
                resultObj.addProperty("star_id_list", String.join(",", slice(splitArr, 0, 3)));
                splitArr = genreNameList.split(",");
                resultObj.addProperty("genre_name_list", String.join(", ", slice(splitArr, 0, 3)));
                splitArr = genreIdList.split(",");
                resultObj.addProperty("genre_id_list", String.join(", ", slice(splitArr, 0, 3)));

                resultsArr.add(resultObj);
            }
            resultSet.close();
            statement.close();

            request.getServletContext().log("getting " + resultsArr.size() + " results");
            out.write(resultsArr.toString());
            logToServer(FUNC, String.format("Wrote %d results to response", resultsArr.size()));
            response.setStatus(OK_CODE);
        }
        catch (Exception e) {
            logToServer(FUNC, "Entered exception");
            JsonObject errorObj = new JsonObject();
            errorObj.addProperty("errorMessage", e.getMessage());

            out.write(errorObj.toString());
            request.getServletContext().log("Error:", e);

            response.setStatus(INTERNAL_SERVER_ERROR_CODE);
        }
        finally {
            out.close();
        }
    }

    private String getMySQLQuery(String sortPriorityParam, String ratingOrder, String titleOrder) {
        final String RATING_FIRST = "1", TITLE_FIRST = "2";

        String query =  "SELECT ms_tab.title as movie_title, ms_tab.id as movie_id, ms_tab.year as movie_year, ms_tab.director as movie_director, ms_tab.starNames as star_name_list, ms_tab.starIds as star_id_list, mg_tab.genreNames as genre_name_list, mg_tab.genreIds as genre_id_list, r.rating as movie_rating\n" +
                "FROM \n" +
                "(SELECT m.id, m.title, m.year, m.director, GROUP_CONCAT(s.name) AS starNames, GROUP_CONCAT(s.id) as starIds\n" +
                "FROM   movies m JOIN stars_in_movies sim ON m.id = sim.movieId JOIN stars s ON s.id = sim.starId\n" +
                "GROUP BY movieId) AS ms_tab\n" +
                "JOIN (\n" +
                "SELECT m.id, GROUP_CONCAT(g.name order by g.name asc) as genreNames, GROUP_CONCAT(g.id order by g.name asc) as genreIds\n" +
                "FROM   movies m JOIN genres_in_movies gim ON m.id = gim.movieId JOIN genres g ON g.id = gim.genreId\n" +
                "GROUP BY m.id\n" +
                ") AS mg_tab ON ms_tab.id = mg_tab.id\n" +
                "JOIN ratings r ON ms_tab.id = r.movieId ORDER BY ";

        if (sortPriorityParam.equals(RATING_FIRST)) {
            query += "r.rating " + ratingOrder + ", ms_tab.title " + titleOrder + "\n";
        }
        else {
            query += "ms_tab.title " + titleOrder + ", r.rating " + ratingOrder + "\n";
        }
        query += " LIMIT ?\n" +
                " OFFSET ?;";

        return query;
    }

    private String[] slice(String[] src, int left, int right) {
        final int NEW_LEN = Math.min(src.length, right - left);
        String[] newSlice = new String[NEW_LEN];

        for (int i = 0; i < NEW_LEN; i++) {
            newSlice[i] = src[left + i];
        }

        return newSlice;
    }

    public void logToServer(String func, String msg) {
        final String MODULE = "MoviesServlet";
        System.out.println(MODULE + " - " + func + ": " + msg);
    }
}
