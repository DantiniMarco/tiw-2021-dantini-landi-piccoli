package it.polimi.tiw.js.controllers;

import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.BidDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/CreateBid")
@MultipartConfig
public class CreateBid extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CreateBid() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        float currMaxPrice;

        User user = (User) request.getSession().getAttribute("user");
        int idBidder = user.getIdUser();

        int idAuction;
        float fPrice;
        String price = request.getParameter("price");
        fPrice = Float.parseFloat(price);
        BidDAO bidDAO = new BidDAO(connection);


        // FIXME: add fixed currmax
        currMaxPrice = Float.parseFloat("0");
        try {
            idAuction = Integer.parseInt(request.getParameter("idauction"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request");
            return;
        }
        try {
            fPrice = Float.parseFloat(price);
            idAuction = Integer.parseInt(request.getParameter("idauction"));
            try {
                BidDAO bidDAO1 = new BidDAO(connection);
                currMaxPrice = bidDAO1.findPriceForNewBid(idAuction);
            } catch (SQLException sqle) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            }
            if (price.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
                return;
            } else if (fPrice <= currMaxPrice) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("This price is too low.");
                return;
            } else {
                try {
                    bidDAO.insertNewBid(fPrice, idBidder, idAuction);
                } catch (SQLException sqle) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
                    return;
                }
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error");
            return;
        }
    }
}
