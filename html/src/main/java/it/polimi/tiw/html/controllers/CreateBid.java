package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.beans.Item;
import it.polimi.tiw.html.beans.User;
import it.polimi.tiw.html.dao.BidDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
@WebServlet("/CreateBid")
public class CreateBid extends HttpServlet {
    private TemplateEngine templateEngine;
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CreateBid(){super();}

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        try {
            ServletContext context = getServletContext();
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Couldn't get db connection");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        Float currMaxPrice = null;
        HttpSession session = request.getSession();
        ServletContext servletContext = getServletContext();

        User user = (User) request.getSession().getAttribute("user");
        int idBidder = user.getIdUser();

        Integer idAuction = null;
        Float fPrice;
        String price = request.getParameter("price");
        fPrice = Float.parseFloat(price);
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        try{
            idAuction = Integer.parseInt(request.getParameter("idauction"));
        }catch (NumberFormatException | NullPointerException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
        }
        try{
             currMaxPrice = Float.parseFloat(request.getParameter("currMax"));
        }catch (NumberFormatException | NullPointerException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
        }
        if (price.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
        }
        else if(currMaxPrice> fPrice){
            ctx.setVariable("errorMsg",
                    "This price is too low. You may insert an offer higher than the current max.");
            response.sendRedirect("GoToBidPage");

        }
        else {
            try {
                BidDAO bidDAO = new BidDAO(connection);
                bidDAO.insertNewBid(fPrice, idBidder, idAuction);
            } catch (SQLException sqle) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            }
            response.sendRedirect("GoToBidPage");
        }

        String path = "/WEB-INF/GoToBidPage.html";
        templateEngine.process(path, ctx, response.getWriter());
    }
}
