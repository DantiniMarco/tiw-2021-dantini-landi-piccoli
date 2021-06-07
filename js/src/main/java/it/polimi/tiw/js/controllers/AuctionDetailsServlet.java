package it.polimi.tiw.js.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.js.beans.AuctionStatus;
import it.polimi.tiw.js.dao.AuctionDAO;
import it.polimi.tiw.js.dao.BidDAO;
import it.polimi.tiw.js.dao.UserDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;
import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.beans.ExtendedBid;
import it.polimi.tiw.js.beans.ExtendedAuction;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@WebServlet("/AuctionDetailsServlet")
public class AuctionDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection con;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        con = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuctionDAO am = new AuctionDAO(con);
        String id_param = request.getParameter("auctionId");
        boolean bad_request=false;
        int id = -1;
        int winnerId = -1;
        User winner = null;
        List<ExtendedBid> bids = null;
        HashMap<String, Object> auctionDetailsMap = new HashMap<>();

        if(id_param==null || id_param.isEmpty()){
            bad_request=true;
        }

        try{
            id = Integer.parseInt(id_param);
        }catch(NumberFormatException ex){
            ex.printStackTrace();
            bad_request=true;
        }

        User user = (User) request.getSession().getAttribute("user");
        List<Integer> ids;
        try{
            ids = am.findAuctionIdsByUsernameId(user.getIdUser());
        }catch(SQLException sqle){
            sqle.printStackTrace();
            throw new UnavailableException("Issue from database");
        }

        if(!ids.contains(id)){
            bad_request = true;
        }

        if(bad_request==true){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            throw new UnavailableException("Id parameter missing");
        }

        ExtendedAuction auction;
        BidDAO bm = new BidDAO(con);
        UserDAO um = new UserDAO(con);

        try{
           auction = am.findAuctionById(id);
           auctionDetailsMap.put("auctionData", auction);
        }catch(SQLException sqle1){
            sqle1.printStackTrace();
            throw new UnavailableException("Issue from database");
        }
        if(auction.getStatus().getValue() == AuctionStatus.OPEN.getValue()){
            try{
                bids = bm.findBidsByIdAuction(id);
                auctionDetailsMap.put("bids", bids);
            }catch (SQLException sqle2){
                throw new UnavailableException("Issue from database");
            }
        }else{
            try{
                con.setAutoCommit(false);
                try{
                    winnerId = bm.findWinnerIdByAuctionId(id);
                    if(winnerId>0){
                        winner = um.getUserById(winnerId);
                    }
                    auctionDetailsMap.put("winner", winner);
                }catch (SQLException sqle1){
                    con.rollback();
                    throw new UnavailableException("Issue from database");
                }finally{
                    con.setAutoCommit(true);
                }
            }catch (SQLException sqle2){
                sqle2.printStackTrace();
                throw new UnavailableException("Issue form database");
            }
        }

        String errorMsg = request.getParameter("errorMsg");
        System.out.println(errorMsg);
        if(errorMsg!=null){
            System.out.println(errorMsg.equals("wrongClosure"));
            if(errorMsg.equals("wrongClosure")){
            }
        }

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(gson);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void destroy(){
        try{
            ConnectionHandler.closeConnection(con);
        }catch (SQLException sql){
            sql.printStackTrace();
        }
    }

}