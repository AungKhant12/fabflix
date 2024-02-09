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

@WebServlet(name = "FetchMetadataServlet", urlPatterns = "/_dashboard/api/fetch-metadata")
public class FetchMetadataServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
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

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String FUNC = "doGet";
        final int OK_CODE = 200, INTERNAL_SERVER_ERROR_CODE = 500;

        logToServer(FUNC, "Entered method");

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            logToServer(FUNC, "Created driver connection");
            String[] tables = {"movies", "genres", "stars", "genres_in_movies", "stars_in_movies", "creditcards", "customers", "sales", "ratings", "employees"};

            String queryRoot = "describe ";

            JsonArray resArr = new JsonArray();

            for (int i = 0; i < tables.length; i++) {
                String query = queryRoot + tables[i];
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();
                JsonObject table_metadata = new JsonObject();
                JsonArray metadata_list = new JsonArray();

                while (resultSet.next()){
                    JsonObject data = new JsonObject();
                    data.addProperty("attribute",resultSet.getString("Field"));
                    data.addProperty("type", resultSet.getString("Type"));
                    metadata_list.add(data);
                }

                table_metadata.addProperty("table_name", tables[i]);
                table_metadata.add("table_metadata", metadata_list);
                resArr.add(table_metadata);
                logToServer(FUNC, "Executed query for: " + tables[i]);
                resultSet.close();
                statement.close();
            }
            
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
    public void logToServer(String func, String msg) {
        String module = this.getClass().getSimpleName();
        System.out.printf("%s - %s: %s%n", module, func, msg);
    }
}