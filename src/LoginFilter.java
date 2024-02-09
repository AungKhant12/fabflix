import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> employeeURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        if (this.isEmployeeUrl(httpRequest.getRequestURI()) && httpRequest.getSession().getAttribute("employee") == null) {
            httpResponse.sendRedirect("employee-login.html");
        } else if (this.isEmployeeUrl(httpRequest.getRequestURI()) && httpRequest.getSession().getAttribute("employee") != null) {
            chain.doFilter(request, response);
        } else if (!this.isEmployeeUrl(httpRequest.getRequestURI()) && httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect("login.html");
        } else {
            chain.doFilter(request, response);
        }

//        // Redirect to login page if the "user" attribute doesn't exist in session
//        if (httpRequest.getSession().getAttribute("user") == null) {
//            httpResponse.sendRedirect("login.html");
//        } else {
//            chain.doFilter(request, response);
//        }
    }
    private boolean isEmployeeUrl(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return employeeURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }
    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login.css");
        allowedURIs.add("api/login");
        allowedURIs.add("employee-login.html");
        allowedURIs.add("employee-login.js");
        allowedURIs.add("employee-login.css");
        allowedURIs.add("api/employee-login");

        employeeURIs.add("dashboard.html");
        employeeURIs.add("dashboard.js");
        employeeURIs.add("dashboard.css");
        employeeURIs.add("api/fetch-metadata");
        employeeURIs.add("add-star.html");
        employeeURIs.add("add-star.js");
        employeeURIs.add("add-star.css");
        employeeURIs.add("api/add-star");
        employeeURIs.add("add-movie.html");
        employeeURIs.add("add-movie.js");
        employeeURIs.add("add-movie.css");
        employeeURIs.add("api/add-movie");
    }

    public void destroy() {
        // ignored.
    }

}
