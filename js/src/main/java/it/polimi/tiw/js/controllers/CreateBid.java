package it.polimi.tiw.js.controllers;

import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.BidDAO;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
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
@MultipartConfig
public class CreateBid extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CreateBid(){super();}

    public void init() throws ServletException {
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
        String errorString = "";
        String price = request.getParameter("price");
        fPrice = Float.parseFloat(price);
        try{
            idAuction = Integer.parseInt(request.getParameter("idauction"));
        }catch (NumberFormatException | NullPointerException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
            return;
        }
        // FIXME: add fixed currmax
        currMaxPrice = Float.parseFloat("0");
        /*try{
             currMaxPrice = Float.parseFloat(request.getParameter("currMax"));
        }catch (NumberFormatException | NullPointerException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
            return;
        }*/
        if (price.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
            return;
        }
        else if(currMaxPrice>= fPrice){
            //request.setAttribute("errorMsg", "This price is too low. You may insert an offer higher than the current max.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("This price is too low.");
            return;
        }
        else {
            try {
                BidDAO bidDAO = new BidDAO(connection);
                bidDAO.insertNewBid(fPrice, idBidder, idAuction);
            } catch (SQLException sqle) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
