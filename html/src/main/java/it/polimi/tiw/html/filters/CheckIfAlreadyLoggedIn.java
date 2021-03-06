package it.polimi.tiw.html.filters;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CheckIfAlreadyLoggedIn implements Filter {
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        System.out.print("Login checker filter executing ...\n");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String homepath = req.getServletContext().getContextPath() + "/Home";

        HttpSession s = req.getSession();
        if (!(s.isNew() || s.getAttribute("user") == null)) {
            res.sendRedirect(homepath);
            return;
        }
        // pass the request along the filter chain
        chain.doFilter(request, response);
    }

}
