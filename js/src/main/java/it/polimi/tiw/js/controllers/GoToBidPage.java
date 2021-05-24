package it.polimi.tiw.js.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.js.beans.Bid;
import it.polimi.tiw.js.beans.Item;
import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.BidDAO;
import it.polimi.tiw.js.dao.ItemDAO;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GoToBidPage")
public class GoToBidPage extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private float currMaxPrice;
    private int idAuctionPub = 0;
    public GoToBidPage(){super();}

    @Override
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{
        int idAuction;
        Item item ;
        List<Bid> bids;
        Map<String, Object> bidPageInfo = new HashMap<>();
        HttpSession s = request.getSession();
        ServletContext servletContext = getServletContext();

        try{
            idAuction = Integer.parseInt(request.getParameter("idauction"));
            idAuctionPub = idAuction;
        } catch ( NumberFormatException | NullPointerException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "incorrect param values");
            return;
        }


        try{
            ItemDAO itemDAO = new ItemDAO(connection);
            item = itemDAO.getItemById(idAuction);
            if(item == null){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("No info for this article found.");
                return;
            }else {
                bidPageInfo.put("item", item);
            }
        }catch (SQLException e){
            response.sendError(500, "Database access failed");
        }
        try{
            BidDAO bidDAO = new BidDAO(connection);
            bids = bidDAO.findBidsByIdAuction(idAuction);
            if(bids == null || bids.isEmpty()) {
                currMaxPrice = (float) 0;
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("No bids for this article found.");
                return;
            }else
                for(Bid b: bids){
                    if(b.getBidPrice()>currMaxPrice)
                        currMaxPrice = b.getBidPrice();
                }
                bidPageInfo.put("bids", bids);
                bidPageInfo.put("currMax", currMaxPrice);
        }catch(SQLException e){
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
        ServletContext servletContext = getServletContext();
        String price = request.getParameter("price");
        Float fPrice = (float) 0;
        BidDAO bidDAO = new BidDAO(connection);
        User user = (User) request.getSession().getAttribute("user");
        //Auction auction = (Auction) request.getSession().getAttribute("auction");
        int idBidder = user.getIdUser();
        int idAuction = idAuctionPub;

        if ( price == null || price.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter missing");
        }
        try{
            fPrice = Float.parseFloat(price);
        }catch(NumberFormatException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Format wrong!");
            return;
        }
        if(currMaxPrice< fPrice){
            try{
                bidDAO.insertNewBid(fPrice, idBidder,idAuction);
            } catch (SQLException sqle) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error");
            }

        }else{
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("This price is too low.");
            return;
        }
    }

    @Override
    public void destroy(){
        try{
            if(connection != null){
                connection.close();
            }
        }catch (SQLException sqle){}
    }
}
