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
import java.sql.SQLException;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/add-star")
public class AddStarServlet extends HttpServlet {

    public static final String ADDSTARWITHBIRTHYEARQUERY = "INSERT INTO stars (id, name, birthyear) VALUES(?, ?, ?)";
    public static final String ADDSTARNOBIRTHYEARQUERY = "INSERT INTO stars (id, name) VALUES(?, ?)";
    public static final String NEWIDQUERY = "SELECT CONCAT('nm', SUBSTRING(max(id),3) + 1) as newid from stars";
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
        String starname = request.getParameter("starname");
        String birthyear = request.getParameter("birthyear");

        if (starname.isBlank()){
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Please enter Star Name");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        String newstarid = "";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(NEWIDQUERY)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()){
                    newstarid = rs.getString("newid");
                }
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        if (birthyear.isBlank()){
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement statement = conn.prepareStatement(ADDSTARNOBIRTHYEARQUERY)) {
                statement.setString(1, newstarid);
                statement.setString(2, starname);
                addStarAndSendResponse(response, newstarid, statement);
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                response.getWriter().write(jsonObject.toString());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        else{
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement statement = conn.prepareStatement(ADDSTARWITHBIRTHYEARQUERY)) {
                statement.setString(1, newstarid);
                statement.setString(2, starname);
                statement.setString(3, birthyear);
                addStarAndSendResponse(response, newstarid, statement);
                return;
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                response.getWriter().write(jsonObject.toString());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void addStarAndSendResponse(HttpServletResponse response, String newstarid, PreparedStatement statement) throws SQLException, IOException {
        int rs = statement.executeUpdate();
        JsonObject responseJsonObject = new JsonObject();
        if (rs > 0){
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Star id: " + newstarid + " added to database.");
        }
        else{
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Failed to add Star");
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
