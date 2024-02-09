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

import java.sql.SQLException;
import java.util.ArrayList;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            logToServer("init", "Attempting to create new data source");
            // Change: Attempting to connect only to master instance
            // dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
            logToServer("init", "Created new data source");
        } catch (Exception e) {
            logToServer("init", "Exception in init");
            e.printStackTrace();
        }
    }

    //---[ HTTP Methods ]------------------------------------------------------
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
            throws IOException {
        final String FUNC = "doPost";
        logToServer(FUNC, "Entered method successfully");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        PaymentParams payment = getPaymentObj(request);

        if (payment.notAllFieldsFilled()) {
            logToServer(FUNC, "Empty field found.");
            JsonObject errObj = new JsonObject();

            errObj.addProperty("errorMessage", "Please fill in all fields.");
            out.write(errObj.toString());
            out.close();
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            logToServer(FUNC, "Created driver Connection");

            // check count
            final String QUERY = checkCreditCardQuery();
            PreparedStatement statement = connection.prepareStatement(QUERY);
            logToServer(FUNC, "Statement created");
            setCountSQLStatementVals(statement, payment);
            ResultSet resultSet = statement.executeQuery();
            logToServer(FUNC, "Executed query");

            // check customer existence
            int num = 0;
            while (resultSet.next()) {
                num = Integer.parseInt(resultSet.getString("count"));
            }
            if (num == 0) {
                logToServer(FUNC, "Invalid information.");
                JsonObject errObj = new JsonObject();

                errObj.addProperty("errorMessage", "Invalid information. Please try again.");
                out.write(errObj.toString());
                out.close();
                return;
            }
            
            User user = (User) request.getSession().getAttribute("user");
            String customerId = user.getid();
            logToServer(FUNC, "Customer id harvested: " + customerId);

            // get shopping cart. create new Cart if not found
            Cart shoppingCart = (Cart) request.getSession().getAttribute("cart");
            if (shoppingCart == null) {
                request.getSession().setAttribute("cart", new Cart());
                shoppingCart = (Cart) request.getSession().getAttribute("cart");
            }

            ArrayList<Item> items = shoppingCart.getItemsAsString();
            for (int i = 0; i < items.size(); i++) {
                Item movieItem = items.get(i);
                String val = createSalesInsertVal(customerId, movieItem);
                String insertQuery = insertIntoSalesQuery() + val;

                logToServer(FUNC, "Query to add item:\n" + insertQuery);
                statement = connection.prepareStatement(insertQuery);
                logToServer(FUNC, "Attempting to insert sales");
                statement.executeUpdate();
            }

            JsonArray resultsArr = new JsonArray();
            logToServer(FUNC, "Beginning to harvest DB query results");

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

    //---[ SQL Methods ]----------------------------------------------------
    public String checkCreditCardQuery() {
        final String FUNC = "checkCreditCardQuery";
        final String QUERY = "SELECT count(*) as count\n" +
                "FROM creditcards as cc \n" +
                "WHERE cc.firstName = ? AND cc.lastName = ? AND cc.id = ? AND cc.expiration = ?;";

        logToServer(FUNC, "Query to execute:");
        System.out.println(QUERY);

        return QUERY;
    }

    public String insertIntoSalesQuery() {
        // (customerId, movieId, saleDate, quantity)
        final String QUERY = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUE";
        return QUERY;
    }
    //---[ SQL Methods ]----------------------------------------------------



    //---[ Helper Methods ]----------------------------------------------------
    public PaymentParams getPaymentObj(HttpServletRequest request) {
        final String FUNC = "getPaymentObj";
        PaymentParams payment = new PaymentParams();

        payment.setFirstName(request.getParameter("firstName"));
        payment.setLastName(request.getParameter("lastName"));
        payment.setCreditCard(request.getParameter("creditCard"));
        payment.setExpirationDate(request.getParameter("expirationDate"));

        logToServer(FUNC, "Harvested payment parameters:");
        print("firstName: " + payment.getFirstName());
        print("lastName: " + payment.getLastName());
        print("creditCard: " + payment.getCreditCard());
        print("expirationDate: " + payment.getExpirationDate());

        return payment;
    }

    public void setCountSQLStatementVals(PreparedStatement statement, PaymentParams payment) throws SQLException {
        statement.setString(1, payment.getFirstName());
        statement.setString(2, payment.getLastName());
        statement.setString(3, payment.getCreditCard());
        statement.setString(4, payment.getExpirationDate());
    }

    public String getCurrentDate() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime timeNow = LocalDateTime.now();
        String currentDate = dateFormatter.format(timeNow);

        return currentDate;
    }

    public String createSalesInsertVal(String customerId, Item movie) {
        final String FUNC = "createSalesInsertVal";

        // (customerId, movieId, saleDate, quantity)
        String val = "(";
        val += String.format("%s, ", customerId);
        val += String.format("\"%s\", ", movie.getId());
        val += String.format("\"%s\", ", getCurrentDate());
        val += movie.getCount();
        val += ")";

        logToServer(FUNC, "Insert value created: " + val);
        return val;
    }
    //---[ Helper Methods ]----------------------------------------------------



    //---[ Logging ]-----------------------------------------------------------
    private void print(String msg) {
        System.out.println(msg);
    }

    private void logToServer(String func, String msg) {
        final String MOD = "PaymentServlet";
        String log = String.format("%s: %s - %s", MOD, func, msg);
        System.out.println(log);
    }
    //---[ Logging ]-----------------------------------------------------------
}
