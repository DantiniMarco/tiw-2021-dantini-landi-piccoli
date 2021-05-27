package it.polimi.tiw.js.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.js.beans.AuctionStatus;
import it.polimi.tiw.js.beans.ExtendedAuction;
import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.AuctionDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;

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
import java.util.Map;

@WebServlet("/SellServlet")
public class SellServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    @Override
    public void init() throws ServletException{
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        User user = (User) request.getSession().getAttribute("user");
        AuctionDAO am= new AuctionDAO(connection);
        List<ExtendedAuction> openAuctions;
        List<ExtendedAuction> closedAuctions;
        Map<String, Object> sellPageInfo = new HashMap<>();

        try{
            openAuctions = am.findAuctionsByIdAndStatus(user.getIdUser(), AuctionStatus.OPEN);
        }catch(SQLException sql){
            sql.printStackTrace();
            throw new UnavailableException("Error executing query");
        }

        try{
            closedAuctions = am.findAuctionsByIdAndStatus(user.getIdUser(), AuctionStatus.CLOSED);
        }catch(SQLException sql){
            sql.printStackTrace();
            throw new UnavailableException("Error executing query");
        }

        sellPageInfo.put("openAuctions", openAuctions);
        sellPageInfo.put("closedAuctions", closedAuctions);

        Gson gson = new GsonBuilder()
                .setDateFormat("dd MMM yyyy HH:mm:ss").create();
        String json = gson.toJson(sellPageInfo);


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
            if(connection !=null){
                connection.close();
            }
        }catch (SQLException sql){
            sql.printStackTrace();
        }
    }
}
