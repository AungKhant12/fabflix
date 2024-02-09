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
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    public static final String QUERY = "SELECT email, password, id from customers as c where c.email = ?";
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String android = request.getParameter("android");
        System.out.println("this is android response: " + android);
        if (android == null) {
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

            // Verify reCAPTCHA
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "ReCaptcha Verification Failed");
                response.getWriter().write(responseJsonObject.toString());
                return;
            }
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Boolean username_correct = false;
        Boolean password_correct = false;
        String user_id = "";
        System.out.println("username: " + username);
        System.out.println("password: " + password);
        System.out.println("this is right before database connection");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(QUERY)) {
             statement.setString(1, username);
             try (ResultSet rs = statement.executeQuery()) {
                 while (rs.next()){
                     if (username.equals(rs.getString("email"))){
                         username_correct = true;
                         System.out.println("username is true");
                     }
                     System.out.println("this is before password check");
                     password_correct = new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"));
                     System.out.println("after password check");
                     user_id = rs.getString("id");
                 }
             }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            System.out.println(e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        JsonObject responseJsonObject = new JsonObject();
        if (username_correct && password_correct) {
            // Login success:

            // set this user into the session
            request.getSession().setAttribute("user", new User(username, user_id));

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");

        } else {
            // Login fail
            responseJsonObject.addProperty("status", "fail");
            // Log to localhost log
            request.getServletContext().log("Login failed");
            // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
            if (!username_correct) {
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            } else {
                responseJsonObject.addProperty("message", "incorrect password");
            }
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
