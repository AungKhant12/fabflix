import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            logToServer("SingleMovieServlet", "init", "Attempting to create data source");
            String dbUrl = "java:comp/env/jdbc/moviedb";
            dataSource = (DataSource) new InitialContext().lookup(dbUrl);
            logToServer("SingleMovieServlet", "init", "Created data source");
        }
        catch (Exception e) {
            logToServer("SingleMovieServlet", "init", "Exception while attempting to create data source");
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logToServer("SingleMovieServlet", "doGet", "Entered method");
        final int OK_CODE = 200, INTERNAL_SERVER_ERROR_CODE = 500;

        response.setContentType("application/json");
        String movieIdParam = request.getParameter("id");
        logToServer("SingleMovieServlet", "doGet", "received movie id parameter: " + movieIdParam);

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            logToServer("SingleMovieServlet", "doGet", "Created DB Connection");

            String query = getMySQLQuery();
            PreparedStatement statement  = conn.prepareStatement(query);
            statement.setString(1, movieIdParam);
            ResultSet resultSet = statement.executeQuery();
            logToServer("SingleMovieServlet", "doGet", "Executed query");

            JsonArray resultsArr = new JsonArray();

            while (resultSet.next()) {
                String movieTitle    = resultSet.getString("movie_title");
                String movieId       = resultSet.getString("movie_id");
                String movieYear     = resultSet.getString("movie_year");
                String movieRating   = resultSet.getString("movie_rating");
                String movieDirector = resultSet.getString("movie_director");
                String starNameList  = resultSet.getString("star_name_list");
                String starIdList    = resultSet.getString("star_id_list");
                String genreNameList = resultSet.getString("genre_name_list");
                String genreIdList   = resultSet.getString("genre_id_list");
                logToServer("SingleMovieServlet", "doGet", "Harvested set of result");

                JsonObject resultObj = new JsonObject();

                resultObj.addProperty("movie_title", movieTitle);
                resultObj.addProperty("movie_id", movieId);
                resultObj.addProperty("movie_year", movieYear);
                resultObj.addProperty("movie_rating", movieRating);
                resultObj.addProperty("movie_director", movieDirector);
                resultObj.addProperty("star_name_list", starNameList);
                resultObj.addProperty("star_id_list", starIdList);
                resultObj.addProperty("genre_name_list", genreNameList);
                resultObj.addProperty("genre_id_list", genreIdList);

                resultsArr.add(resultObj);
            }
            resultSet.close();
            statement.close();

            request.getServletContext().log(String.format("harvested %d results", resultsArr.size()));
            out.write(resultsArr.toString());
            logToServer("SingleMovieServlet", "doGet", String.format("Wrote %d results to response", resultsArr.size()));
            response.setStatus(OK_CODE);
        }
        catch (Exception e) {
            logToServer("SingleMovieServlet", "doGet", "Exception");
            JsonObject errObj = new JsonObject();
            errObj.addProperty("errorMessage", e.getMessage());

            out.write(errObj.toString());
            request.getServletContext().log("Error:", e);

            response.setStatus(INTERNAL_SERVER_ERROR_CODE);
        }
        finally {
            out.close();
        }
    }

    //---[ Helper Methods ]----------------------------------------------------
    public String getMySQLQuery() {
        return "SELECT msg_tab.title as movie_title, msg_tab.id as movie_id, msg_tab.year as movie_year, msg_tab.director as movie_director, msg_tab.starNames as star_name_list, msg_tab.starIds as star_id_list, msg_tab.genreNames as genre_name_list, msg_tab.genreIds as genre_id_list, r.rating as movie_rating FROM ( SELECT ms_tab.id, ms_tab.title, ms_tab.year, ms_tab.director, ms_tab.starNames, ms_tab.starIds, GROUP_CONCAT(g.name) as genreNames, GROUP_CONCAT(g.id) as genreIds FROM ( SELECT   m.id, m.title, m.year, m.director, GROUP_CONCAT(s.name) AS starNames, GROUP_CONCAT(s.id) as starIds FROM     movies AS m JOIN stars_in_movies AS sim ON m.id = sim.movieId JOIN stars AS s on s.id = sim.starId WHERE    m.id = ? GROUP BY m.id ) AS ms_tab JOIN genres_in_movies AS gim ON ms_tab.id = gim.movieId JOIN genres AS g ON g.id = gim.genreId GROUP BY ms_tab.id ) as msg_tab JOIN ratings AS r ON msg_tab.id = r.movieId;";
    }

    public void logToServer(String module, String func, String msg) {
        System.out.println(module + " - " + func + ": " + msg);
    }
    //---[ Helper Methods ]----------------------------------------------------
}
