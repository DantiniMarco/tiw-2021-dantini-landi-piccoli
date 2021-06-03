package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.beans.User;
import it.polimi.tiw.html.dao.BidDAO;
import it.polimi.tiw.html.utils.ConnectionHandler;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
@WebServlet("/CreateBid")
public class CreateBid extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CreateBid(){super();}

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
        Float currMaxPrice = null;

        User user = (User) request.getSession().getAttribute("user");
        int idBidder = user.getIdUser();

        Integer idAuction;
        Float fPrice;
        String errorString = "";
        String price = request.getParameter("price");
        try {
            idAuction = Integer.parseInt(request.getParameter("idauction"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request");
            return;
        }
        try{
            fPrice = Float.parseFloat(price);
            idAuction = Integer.parseInt(request.getParameter("idauction"));
            try{
                BidDAO bidDAO1 = new BidDAO(connection);
                currMaxPrice = bidDAO1.findPriceForNewBid(idAuction);
            } catch (SQLException sqle) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            }
            if(currMaxPrice > fPrice){
                errorString = "&error=lowPrice";
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
        }catch (NumberFormatException | NullPointerException e){
            errorString = "&error=wrongFormat";
        }

        response.sendRedirect(getServletContext().getContextPath() + "/GoToBidPage?idauction=" + idAuction + errorString);
    }
}
