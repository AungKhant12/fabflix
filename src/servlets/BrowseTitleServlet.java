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

@WebServlet(name = "BrowseTitleServlet", urlPatterns = "/api/browse-title")
public class BrowseTitleServlet extends HttpServlet {
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

    //--[ HTTP Methods ]-------------------------------------------------------
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String FUNC = "doGet";
        final int OK_CODE = 200, INTERNAL_SERVER_ERROR_CODE = 500;

        logToServer(FUNC, "Entered method");

        String startLetterParam = req.getParameter("start");
        logToServer(FUNC, "Received start letter parameter: " + startLetterParam);

        // pagination
        String pageParam    = req.getParameter("page");
        logToServer(FUNC, "Got page value: " + pageParam);
        int offsetVal = 0;
        if (pageParam != null && !pageParam.equals("null")) {
            offsetVal = 20 * (Integer.parseInt(pageParam) - 1);
        }
        logToServer(FUNC, "Calculated offset value: " + offsetVal);

        // TODO: sorting (rating, title, priority)
        String sortParam = req.getParameter("sort");
        if (sortParam == null || sortParam.equals("null")) { sortParam = "desc"; }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            logToServer(FUNC, "Created driver connection");

            String query = getMySQLQuery(startLetterParam, sortParam, "desc", 2);

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, offsetVal);
//            statement.setString(1, startLetterParam);
            ResultSet resultSet = statement.executeQuery();
            logToServer(FUNC, "Executed query");

            JsonArray resArr = new JsonArray();

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
                resArr.add(resObj);
            }
            resultSet.close();
            statement.close();

            out.write(resArr.toString());

            String logMsg = String.format("Harvested %d results", resArr.size());
            logToServer(FUNC, logMsg);
            req.getServletContext().log(logMsg);

            resp.setStatus(OK_CODE);
        }
        catch (Exception e) {
            logToServer(FUNC, "Entered exception");

            JsonObject errObj = new JsonObject();
            errObj.addProperty("errorMessage", e.getMessage());

            out.write(errObj.toString());
            req.getServletContext().log("Error:", e);

            resp.setStatus(INTERNAL_SERVER_ERROR_CODE);
        }
        finally {
            out.close();
        }
    }

    //--[ Helper Functions ]---------------------------------------------------
    public String getMySQLQuery(String startLetterParam, String ratingSortParam, String titleSortParam, int sortPriority) {
        final String FUNC = "getMySQLQuery";
        logToServer(FUNC, "Entered method");
        logToServer(FUNC, "Start Letter Param: " + startLetterParam);

        final int TITLE_PRIORITY = 1;

        String query =
            "SELECT msg_tab.title as movie_title, msg_tab.id as movie_id, msg_tab.year as movie_year, msg_tab.director as movie_director, msg_tab.starNames as star_name_list, msg_tab.starIds as star_id_list, msg_tab.genreNames as genre_name_list, msg_tab.genreIds as genre_id_list, r.rating as movie_rating FROM ( SELECT ms_tab.id, ms_tab.title, ms_tab.year, ms_tab.director, ms_tab.starNames, ms_tab.starIds, GROUP_CONCAT(g.name) as genreNames, GROUP_CONCAT(g.id) as genreIds FROM ( SELECT m.id, m.title, m.year, m.director, GROUP_CONCAT(s.name) AS starNames, GROUP_CONCAT(s.id) as starIds FROM movies AS m JOIN stars_in_movies AS sim ON m.id = sim.movieId JOIN stars AS s on s.id = sim.starId GROUP BY m.id ) AS ms_tab JOIN genres_in_movies AS gim ON ms_tab.id = gim.movieId JOIN genres AS g ON g.id = gim.genreId GROUP BY ms_tab.id ) as msg_tab JOIN ratings AS r ON msg_tab.id = r.movieId ";

        // character start checking
            if (!startLetterParam.equals("*")) {
                logToServer(FUNC, "Normal query. Matching 1st character to alphanumeric char");
                query += "WHERE msg_tab.title LIKE \"" + startLetterParam + "%\" ";
            }
            else {
                logToServer(FUNC, "Different query. Matching non-English alphanumeric char");
                query += "WHERE msg_tab.title REGEXP '^[^A-Za-z0-9]' ";
            }

        // sort order checking
            query += "ORDER BY ";

            if (sortPriority == TITLE_PRIORITY) {
                query += "msg_tab.title " + titleSortParam + ", r.rating " + ratingSortParam + " ";
            }
            else {
                query += "r.rating " + ratingSortParam + ", msg_tab.title " + titleSortParam + " ";
            }

        // number of entries per page
            query += "LIMIT 20 ";

        // pagination
            query += "OFFSET ?;";

        return query;
    }

    public void logToServer(String func, String msg) {
        String module = this.getClass().getSimpleName();
        System.out.printf("%s - %s: %s%n", module, func, msg);
    }

    private String[] slice(String[] src, int left, int right) {
        final int NEW_LEN = Math.min(src.length, right - left);
        String[] newSlice = new String[NEW_LEN];

        for (int i = 0; i < NEW_LEN; i++) {
            newSlice[i] = src[left + i];
        }

        return newSlice;
    }
    //--[ Helper Functions ]---------------------------------------------------
}
