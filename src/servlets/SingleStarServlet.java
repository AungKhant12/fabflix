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

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final int OK_STATUS_CODE = 200, INTERNAL_SERVER_ERROR = 500;

        response.setContentType("application/json");
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * from stars as s, stars_in_movies as sim, movies as m where m.id = sim.movieId and sim.starId = s.id and s.id = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String starId        = rs.getString("starId");
                String starName      = rs.getString("name");
                String starDob       = rs.getString("birthYear");
                String movieId       = rs.getString("movieId");
                String movieTitle    = rs.getString("title");
                String movieYear     = rs.getString("year");
                String movieDirector = rs.getString("director");

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", starName);

                // if star dob is null, put N/A
                if (starDob == null){
                    jsonObject.addProperty("star_dob", "N/A");
                }
                else{
                    jsonObject.addProperty("star_dob", starDob);
                }
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(OK_STATUS_CODE);

        }
        catch (Exception e) {
            // send error message to browser
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // log
            request.getServletContext().log("Error:", e);
            response.setStatus(INTERNAL_SERVER_ERROR);
        }
        finally {
            out.close();
        }
    }
}
