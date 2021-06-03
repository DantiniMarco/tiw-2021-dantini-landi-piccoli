package it.polimi.tiw.js.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.js.beans.Bid;
import it.polimi.tiw.js.beans.ExtendedBid;
import it.polimi.tiw.js.beans.Item;
import it.polimi.tiw.js.dao.BidDAO;
import it.polimi.tiw.js.dao.ItemDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GoToBidPage")
public class GoToBidPage extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GoToBidPage() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idAuction;
        Item item;
        List<ExtendedBid> bids;
        Map<String, Object> bidPageInfo = new HashMap<>();
        BidDAO bidDAO = new BidDAO(connection);
        float currMaxPrice;
        float minRaise=-1;

        try {
            idAuction = Integer.parseInt(request.getParameter("idauction"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "incorrect param values");
            return;
        }
        try {
            minRaise = bidDAO.findMinRaise(idAuction);
            if(minRaise >=0)
            bidPageInfo.put("minRaise", minRaise);
        } catch (SQLException sqle) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
        }


        try {
            ItemDAO itemDAO = new ItemDAO(connection);
            item = itemDAO.getItemById(idAuction);
            if (item == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("No info for this article found.");
                return;
            } else {
                bidPageInfo.put("item", item);
            }
        } catch (SQLException e) {
            response.sendError(500, "Database access failed");
        }
        try {
            bids = bidDAO.findBidsByIdAuction(idAuction);
            if(bids!=null){
                bidPageInfo.put("bids", bids);
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database access failed");
            return;
        }

        Gson gson = new GsonBuilder()
                .setDateFormat("dd MMM yyyy HH:mm:ss").create();
        String json = gson.toJson(bidPageInfo);


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    @Override
    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
