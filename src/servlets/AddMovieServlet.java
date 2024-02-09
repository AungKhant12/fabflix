import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet {

    public static final String ADDMOVIEQUERY = "CALL add_movie(?,?,?,?,?)";
       private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            // Change: Attempting to connect only to master instance
            // dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genre = request.getParameter("genre");

        JsonObject responseJsonObject = new JsonObject();

        String error_string = "Please enter: ";
        Boolean missing_input = false;
        if (title.isBlank()) {
            missing_input = true;
            error_string += "Title ";
        }
        if (year.isBlank()) {
            missing_input = true;
            error_string += "Year ";
        }
        if (director.isBlank()) {
            missing_input = true;
            error_string += "Director ";
        }
        if (star.isBlank()) {
            missing_input = true;
            error_string += "Star ";
        }
        if (genre.isBlank()) {
            missing_input = true;
            error_string += "Genre ";
        }
        if (missing_input) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", error_string);
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(ADDMOVIEQUERY)) {
            statement.setString(1, title);
            statement.setString(2, year);
            statement.setString(3, director);
            statement.setString(4, star);
            statement.setString(5, genre);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String answer = rs.getString("answer");
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", answer);
                    response.getWriter().write(responseJsonObject.toString());
                    return;
                }
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
