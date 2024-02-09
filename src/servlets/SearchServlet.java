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

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            logToServer("init", "Attempting to create new data source");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            logToServer("init", "Created new data source");
        } catch (Exception e) {
            logToServer("init", "Exception in init");
            e.printStackTrace();
        }
    }


    //---[ HTTP Methods ]------------------------------------------------------
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response)
            throws IOException {
        final String FUNC = "doGet";
        logToServer(FUNC, "Entered method successfully");

        response.setContentType("application/json");

        MovieParams paramObj = new MovieParams();
        paramObj.fillParams(request);

        logToServer(FUNC, "parameters received: ");

        // page parameter
        logToServer(FUNC, "page: " + paramObj.getPage());
        int offsetVal = paramObj.calcOffsetVal();
        logToServer(FUNC, "Calculated offset value: " + offsetVal);

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            logToServer(FUNC, "Created driver Connection");

            final String QUERY = getMySQLQuery(paramObj);
            PreparedStatement statement = connection.prepareStatement(QUERY);
            logToServer(FUNC, "Statement created");
            statement.setInt(1, Integer.parseInt(paramObj.getLimit()));
            statement.setInt(2, offsetVal);
            ResultSet resultSet = statement.executeQuery();
            logToServer(FUNC, "Executed query");

            JsonArray resultsArr = new JsonArray();

            logToServer(FUNC, "Beginning to harvest DB query results");
            while (resultSet.next()) {
                JsonObject resObj = new JsonObject();

                // harvest
                String movieTitle    = resultSet.getString("movie_title");
                String movieId       = resultSet.getString("movie_id");
                String movieYear     = resultSet.getString("movie_year");
                String movieDirector = resultSet.getString("movie_director");
                String starNameList  = resultSet.getString("star_name_list");
                String starIdList    = resultSet.getString("star_id_list");
                String genreNameList = resultSet.getString("genre_name_list");
                String genreIdList   = resultSet.getString("genre_id_list");
                String rating        = resultSet.getString("movie_rating");

                // hoist
                resObj.addProperty("movie_id", movieId);
                resObj.addProperty("movie_title", movieTitle);
                resObj.addProperty("movie_year", movieYear);
                resObj.addProperty("movie_director", movieDirector);
                resObj.addProperty("movie_rating", rating);

                String[] splitArr = starNameList.split(",");
                resObj.addProperty("star_name_list", String.join(",", slice(splitArr, 0, 3)));
                splitArr = starIdList.split(",");
                resObj.addProperty("star_id_list", String.join(",", slice(splitArr, 0, 3)));
                splitArr = genreNameList.split(",");
                resObj.addProperty("genre_name_list", String.join(", ", slice(splitArr, 0, 3)));
                splitArr = genreIdList.split(",");
                resObj.addProperty("genre_id_list", String.join(", ", slice(splitArr, 0, 3)));

                // include data
                resultsArr.add(resObj);
            }
            logToServer(FUNC, "Finished harvesting DB query results");

            int totalResults = -1;

            statement = connection.prepareStatement(getTotalCount(paramObj));
            logToServer(FUNC, "Statement created");
            resultSet = statement.executeQuery();
            logToServer(FUNC, "Executed query");

            boolean onFirstPage = true, onLastPage = true;
            int totalMoviesCount = 0;
            while (resultSet.next()) {
                totalMoviesCount = resultSet.getInt("totalCount");
                logToServer(FUNC, "Total movies count: " + totalMoviesCount);
            }

            onFirstPage = offsetVal < Integer.parseInt(paramObj.getLimit());
            onLastPage  = offsetVal + Integer.parseInt(paramObj.getLimit()) >= totalMoviesCount;

            if (onFirstPage) {System.out.println("on first page");  }
            else { System.out.println("not on first page"); }

            if (onLastPage) { System.out.println("on last page"); }
            else { System.out.println("not on last page"); }

            resultSet.close();
            statement.close();

            out.write(resultsArr.toString());

            logToServer(FUNC, String.format("wrote %d results to response", resultsArr.size()));
            request.getServletContext().log("harvested " + resultsArr.size() + "results");

            response.setStatus(response.SC_OK);
        }
        catch (Exception e) {
            logToServer(FUNC, "Entered exception");
            JsonObject errObj = new JsonObject();
            errObj.addProperty("errorMessage", e.getMessage());

            out.write(errObj.toString());
            request.getServletContext().log("Error:", e);

            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            logToServer(FUNC, "Reached finally clause");
            out.close();
        }
    }
    //---[ HTTP Methods ]------------------------------------------------------

    //---[ Helper Methods ]----------------------------------------------------
    private String getMySQLQuery(MovieParams paramObj) {
        final String FUNC = "getMySQLQuery";
        logToServer(FUNC, "Entered method");

        String query = "SELECT msg_tab.title as movie_title, msg_tab.id as movie_id, msg_tab.year as movie_year, msg_tab.director as movie_director, msg_tab.starNames as star_name_list, msg_tab.starIds as star_id_list, msg_tab.genreNames as genre_name_list, msg_tab.genreIds as genre_id_list, r.rating as movie_rating " +
                "FROM ( " +
                    "\tSELECT ms_tab.id, ms_tab.title, ms_tab.year, ms_tab.director, ms_tab.starNames, ms_tab.starIds, GROUP_CONCAT(distinct g.name order by g.name) as genreNames, GROUP_CONCAT(distinct g.id order by g.name) as genreIds" +
                    "\tFROM (" +
                        "\t\tSELECT m.id, m.title, m.year, m.director, GROUP_CONCAT(s.name) AS starNames, GROUP_CONCAT(s.id) as starIds" +
                        "\t\tFROM   movies AS m JOIN stars_in_movies AS sim ON m.id = sim.movieId JOIN stars AS s on s.id = sim.starId \n";
        int numPrevParam = 0;

        // movie title matching
        if (paramObj.titleNotEmpty()) {
            query += "WHERE m.title LIKE \"%" + paramObj.getTitle() + "%\" ";
            numPrevParam++;
        }
        // movie year matching
        if (paramObj.yearNotEmpty()) {
            if (numPrevParam == 0) { query += "WHERE "; }
            else { query += " AND "; }
            query += "m.year = " + paramObj.getYear() + " ";
            numPrevParam++;
        }
        // movie director matching
        if (paramObj.directorNotEmpty()) {
            if (numPrevParam == 0) { query += "WHERE "; }
            else { query += " AND "; }
            query += "m.director LIKE \"%" + paramObj.getDirector() + "%\" ";
        }
        query += "GROUP BY m.id ) AS ms_tab JOIN ";
        // star name matching
        if (paramObj.starNameNotEmpty()) {
            query += "( \nSELECT DISTINCT m.id FROM movies AS m JOIN stars_in_movies AS sim ON m.id = sim.movieId JOIN stars AS s on s.id = sim.starId WHERE s.name LIKE \"%" + paramObj.getStarName() + "%\" ) as ms_filter ON ms_tab.id = ms_filter.id JOIN ";
        }
        query += " genres_in_movies AS gim ON ms_tab.id = gim.movieId JOIN genres AS g ON g.id = gim.genreId GROUP BY ms_tab.id ) as msg_tab JOIN ratings AS r ON msg_tab.id = r.movieId ORDER BY ";

        final String RATING_FIRST = "rating";
        if (paramObj.isSortedByRating()) {
            query += "r.rating " + paramObj.getRatingOrder() + ", msg_tab.title " + paramObj.getTitleOrder() + "\n";
        }
        else {
            query += "msg_tab.title " + paramObj.getTitleOrder() + ", r.rating " + paramObj.getRatingOrder() + "\n";
        }

        query += "\nLIMIT ? ";

        query += "\nOFFSET ?;";

        logToServer(FUNC, "Query: ");
        System.out.println(query);

        return query;
    }

    private String getTotalCount(MovieParams paramsObj) {
        final String FUNC = "getTotalCount";
        logToServer(FUNC, "Entered method");

        int numPrevParam = 0;
        String countQuery = "select count(*) as totalCount\n" +
                        "from (\n" +
                        "\tselect distinct m.id, GROUP_CONCAT(s.name) as starNames\n" +
                        "    from   movies m join stars_in_movies sim on m.id = sim.movieId join stars s on s.id = sim.starId\n";

        if (paramsObj.titleNotEmpty()) {
            logToServer(FUNC, "Title parameter not empty");

            countQuery += "where  m.title like \"%" + paramsObj.getTitle() + "%\" ";
            numPrevParam++;
        }
        if (paramsObj.yearNotEmpty()) {
            logToServer(FUNC, "Year parameter not empty");

            if (numPrevParam == 0) { countQuery += "WHERE "; }
            else { countQuery += "AND "; }

            countQuery += "m.year = " + paramsObj.getYear() + " ";
            numPrevParam++;
        }
        if (paramsObj.directorNotEmpty()) {
            if (numPrevParam == 0) { countQuery += "WHERE "; }
            else { countQuery += "AND "; }

            countQuery += "m.director LIKE \"%" + paramsObj.getDirector() + "%\" ";
        }

        countQuery += " group by m.id ) as ms_tab ";

        if (paramsObj.starNameNotEmpty()) {
            countQuery += "join ( SELECT DISTINCT m.id " +
            "FROM movies m JOIN stars_in_movies sim ON m.id = sim.movieId JOIN stars s on s.id = sim.starId\n" +
            "WHERE s.name LIKE \"%" + paramsObj.getStarName() + "%\"\n" +
            ") AS ms_filter.id ";
        }

        countQuery += "JOIN ratings r ON ms_tab.id = r.movieId;";

        logToServer(FUNC, "Count query:");
        System.out.println(countQuery);
        return countQuery;
    }

    private String[] slice(String[] src, int left, int right) {
        final int NEW_LEN = Math.min(src.length, right - left);
        String[] newSlice = new String[NEW_LEN];

        for (int i = 0; i < NEW_LEN; i++) {
            newSlice[i] = src[left + i];
        }

        return newSlice;
    }

    //---[ Helper Methods ]----------------------------------------------------


    //---[ Logging ]-----------------------------------------------------------
    public void logToServer(String funcName, String msg) {
        final String MODULE_NAME = this.getClass().getSimpleName();
        System.out.printf("%s - %s: %s%n", MODULE_NAME, funcName, msg);
    }

    //---[ Logging ]-----------------------------------------------------------
}