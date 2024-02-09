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

@WebServlet(name = "TitleSuggestionServlet", urlPatterns = "/api/title-suggestion")
public class TitleSuggestionServlet extends HttpServlet {
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
            HttpServletRequest  request,
            HttpServletResponse response
    ) throws IOException {
        final String FUNC = "doGet";
        logToServer(FUNC, "Entered method successfully");

        response.setContentType("application/json");

        String queryParams = request.getParameter("query");
        logToServer(FUNC, "parameters received: ");
        System.out.println(queryParams);

        PrintWriter out = response.getWriter();
        try (Connection connection = dataSource.getConnection()) {
            logToServer(FUNC, "Created driver Connection");

            final String QUERY = getFulltextQuery(queryParams);
            PreparedStatement statement = connection.prepareStatement(QUERY);
            logToServer(FUNC, "Statement created");
            ResultSet resultSet = statement.executeQuery();
            logToServer(FUNC, "Executed query");

            JsonArray resultsArr = new JsonArray();

            logToServer(FUNC, "Beginning to harvest DB query results");
            while (resultSet.next()) {
                JsonObject resObj  = new JsonObject();
                JsonObject dataObj = new JsonObject();

                // harvest
                String movieTitle = resultSet.getString("title");
                String movieId    = resultSet.getString("id");

                // hoist
                resObj.addProperty("value", movieTitle);
                dataObj.addProperty("movie_id", movieId);
                resObj.add("data", dataObj);

                // include data
                resultsArr.add(resObj);
            }
            logToServer(FUNC, "Finished harvesting DB query results");

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
    private String getFulltextQuery(String queryParams) {
        final String FUNC = "getFulltextQuery";
        logToServer(FUNC, "Entered method");

        String query =
            "SELECT title, id, rating\n" +
            "FROM   movies m join ratings r\n" +
            "ON     m.id = r.movieId\n";

        if (queryParams != null && !queryParams.equals("null") && !queryParams.isBlank()) {
            String[] queryParamArr = queryParams.replaceAll(" ", "%20").split("%20");

            query += "WHERE MATCH (title) AGAINST (\"";
            for (int i = 0; i < queryParamArr.length; i++) {
                query += String.format("+%s*", queryParamArr[i]);

                if (i < queryParamArr.length - 1) {
                    query += " ";
                }
            }
            query += "\" IN BOOLEAN MODE)";
        }

        query += "ORDER BY r.rating DESC\n" +
            "LIMIT 10;";

        return query;
    }

    //---[ Helper Methods ]----------------------------------------------------


    //---[ Logging ]-----------------------------------------------------------
    public void logToServer(String funcName, String msg) {
        final String MODULE_NAME = this.getClass().getSimpleName();
        System.out.printf("%s - %s: %s%n", MODULE_NAME, funcName, msg);
    }

    //---[ Logging ]-----------------------------------------------------------
}
