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

@WebServlet(name = "BrowseGenreServlet", urlPatterns = "/api/browse-genres")
public class BrowseGenreServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            logToServer("init", "Attempting to create new data source");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            logToServer("init","Created new data source");
        }
        catch (Exception e) {
            logToServer("init", "Exception in init");
            e.printStackTrace();
        }
    }

    //---[ HTTP Methods ]------------------------------------------------------
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final int OK_CODE = 200, INTERNAL_SERVER_ERROR_CODE = 500;
        final String METHOD_NAME = "doGet";

        logToServer(METHOD_NAME, "Entered method successfully");

        response.setContentType("application/json");
        MovieParams paramsObj = new MovieParams();
        paramsObj.fillParams(request);

        logToServer(METHOD_NAME, "Got genre id: " + paramsObj.getGenreId());
        logToServer(METHOD_NAME, "Got page value: " + paramsObj.getPage());
        logToServer(METHOD_NAME, "Calculated offset value: " + paramsObj.calcOffsetVal());
        request.getServletContext().log("Got genre id: " + paramsObj.getGenreId());

        String ratingOrderParam = request.getParameter("ratingOrder");
        if (ratingOrderParam.equals("null")) { ratingOrderParam = "desc"; }
        logToServer(METHOD_NAME, "Got ratingOrder param: " + ratingOrderParam);

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            logToServer(METHOD_NAME, "Created driver Connection");

            final String QUERY = getMySQLQuery(ratingOrderParam);
            PreparedStatement statement = conn.prepareStatement(QUERY);
            statement.setString(1, paramsObj.getGenreId());
            statement.setInt(2, Integer.parseInt(paramsObj.getLimit()));
            statement.setInt(3, paramsObj.calcOffsetVal());
            ResultSet resultSet = statement.executeQuery();
            logToServer(METHOD_NAME, "Executed query");

            JsonArray resultsArr = new JsonArray();

            logToServer(METHOD_NAME, "Beginning to harvest DB query results");
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
            logToServer(METHOD_NAME, "Finished harvesting DB query results");

            resultSet.close();
            statement.close();

            out.write(resultsArr.toString());
            logToServer(METHOD_NAME, String.format("wrote %d results to response", resultsArr.size()));
            request.getServletContext().log("harvested " + resultsArr.size() + "results");

            response.setStatus(OK_CODE);
        }
        catch (Exception e) {
            logToServer(METHOD_NAME, "Entered exception");
            JsonObject errObj = new JsonObject();
            errObj.addProperty("errorMessage", e.getMessage());

            out.write(errObj.toString());
            request.getServletContext().log("Error:", e);

            response.setStatus(INTERNAL_SERVER_ERROR_CODE);
        }
        finally {
            logToServer(METHOD_NAME, "Reached finally clause");
            out.close();
        }
    }

    //---[ Helper Methods ]----------------------------------------------------
    private String getMySQLQuery(String ratingOrderParam) {
        return
            "SELECT msg_tab.title as movie_title, msg_tab.id as movie_id, msg_tab.year as movie_year, msg_tab.director as movie_director, msg_tab.starNames as star_name_list, msg_tab.starIds as star_id_list, msg_tab.genreNames as genre_name_list, msg_tab.genreIds as genre_id_list, r.rating as movie_rating\n" +
            "FROM (\n" +
            "    SELECT ms_tab.id, ms_tab.title, ms_tab.year, ms_tab.director, ms_tab.starNames, ms_tab.starIds, GROUP_CONCAT(g.name) as genreNames, GROUP_CONCAT(g.id) as genreIds\n" +
            "    FROM (\n" +
            "       SELECT m.id, m.title, m.year, m.director, GROUP_CONCAT(s.name) AS starNames, GROUP_CONCAT(s.id) as starIds\n" +
            "       FROM movies AS m JOIN stars_in_movies AS sim ON m.id = sim.movieId JOIN stars AS s on s.id = sim.starId\n" +
            "       GROUP BY m.id\n" +
            "    ) AS ms_tab JOIN (\n" +
            "\t\tSELECT movieId\n" +
            "\t\tFROM   movies m JOIN genres_in_movies gim ON m.id = gim.movieId JOIN genres g ON g.id = gim.genreId\n" +
            "\t\tWHERE  g.id = ?\n" +
            "    ) as genreTab ON genreTab.movieId = ms_tab.id JOIN\n" +
            "    genres_in_movies AS gim ON ms_tab.id = gim.movieId JOIN genres AS g ON g.id = gim.genreId GROUP BY ms_tab.id\n" +
            " ) as msg_tab JOIN ratings AS r ON msg_tab.id = r.movieId\n" +
            " ORDER BY r.rating " + ratingOrderParam + "\n" +
            " LIMIT ?\n" +
            " OFFSET ?;";
    }

    private String[] slice(String[] src, int left, int right) {
        final int NEW_LEN = Math.min(src.length, right - left);
        String[] newSlice = new String[NEW_LEN];

        for (int i = 0; i < NEW_LEN; i++) {
            newSlice[i] = src[left + i];
        }

        return newSlice;
    }
    public void logToServer(String funcName, String msg) {
        final String MODULE_NAME = this.getClass().getSimpleName();
        System.out.printf("%s - %s: %s%n", MODULE_NAME, funcName, msg);
    }
}
