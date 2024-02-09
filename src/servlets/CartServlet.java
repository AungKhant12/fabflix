import com.google.gson.JsonArray;
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

@WebServlet(name = "CartServlet", urlPatterns = "/api/Cart")
public class CartServlet extends HttpServlet {

    public static final String QUERY = "SELECT email, password from customers as c where c.email = ?";
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
        String action = request.getParameter("action");
        String id     = request.getParameter("id");
        String title  = request.getParameter("title");

        Cart shoppingCart = getUserCart(request);
        JsonObject responseJsonObject = new JsonObject();

        // action values: 0 = view cart, 1 = increase, 2 = decrease, 3 = delete
        final String VIEW_CART = "0";

        if (action.equals(VIEW_CART)) {
            responseJsonObject.addProperty("items", shoppingCart.getCartItems());
            responseJsonObject.addProperty("total", shoppingCart.getTotal());
            responseJsonObject.addProperty("status", "success");
        }
        else {
            responseJsonObject.addProperty("status", "fail");
        }

        response.getWriter().write(responseJsonObject.toString());
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String FUNC = "doPost";
        logToServer(FUNC, "Entered Method");

        String action = request.getParameter("action");
        String id     = request.getParameter("id");
        String title  = request.getParameter("title");

        Cart shoppingCart = getUserCart(request);

        JsonObject responseJsonObject = new JsonObject();
        // action values: 0 = view cart, 1 = increase, 2 = decrease, 3 = delete
        final String VIEW_CART = "0", INC_ITEM = "1", DEC_ITEM = "2", CLEAR_ITEM = "3";

        if (action.equals(VIEW_CART)) {
            logToServer(FUNC, "Viewing Cart");
            responseJsonObject.addProperty("items", shoppingCart.getCartItems());
            responseJsonObject.addProperty("total", shoppingCart.getTotal());
            responseJsonObject.addProperty("status", "success");
        }
        else if (action.equals(INC_ITEM)) {
            logToServer(FUNC, "Adding to Cart: " + title + ", " + id);
            shoppingCart.increaseItemCount(title, id);
            logToServer(FUNC, "Added to Cart! " + title + ", " + id);
            responseJsonObject.addProperty("status", "success");
        }
        else if (action.equals(DEC_ITEM)) {
            logToServer(FUNC, "Decrementing from Cart: " + title);
            shoppingCart.decreaseItemCount(id);
            logToServer(FUNC, "Decremented from Cart: " + title);
            responseJsonObject.addProperty("status", "success");
        }
        else if (action.equals(CLEAR_ITEM)) {
            logToServer(FUNC, "Deleting from Cart: " + title);
            shoppingCart.deleteItem(id);
            logToServer(FUNC, "Deleted from Cart: " + title);
            responseJsonObject.addProperty("status", "success");
        }
        else {
            logToServer(FUNC, "Error: Invalid action parameter");
            responseJsonObject.addProperty("status", "fail");
        }

        logToServer(FUNC, "Finished Modifying Cart");
        response.getWriter().write(responseJsonObject.toString());
    }

    //---[ Helper Methods ]----------------------------------------------------
    public Cart getUserCart(HttpServletRequest request) {
        Cart shoppingCart = (Cart) request.getSession().getAttribute("cart");

        if (shoppingCart == null) {
            request.getSession().setAttribute("cart", new Cart());
            shoppingCart = (Cart) request.getSession().getAttribute("cart");
        }

        return shoppingCart;
    }
    //---[ Helper Methods ]----------------------------------------------------


    //---[ Logging ]-----------------------------------------------------------
    public void logToServer(String funcName, String msg) {
        final String MODULE_NAME = this.getClass().getSimpleName();
        System.out.printf("%s - %s: %s%n", MODULE_NAME, funcName, msg);
    }
    //---[ Logging ]-----------------------------------------------------------
}
