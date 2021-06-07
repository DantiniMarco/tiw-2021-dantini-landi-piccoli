package it.polimi.tiw.js.controllers;

import it.polimi.tiw.js.beans.User;
import it.polimi.tiw.js.dao.AuctionDAO;
import it.polimi.tiw.js.utils.ConnectionHandler;

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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/AuctionDetailsServletHelper")
public class AuctionDetailsServletHelper extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection con;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        con = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        AuctionDAO am = new AuctionDAO(con);
        String id_param = request.getParameter("auctionId");
        boolean bad_request = false, ready = true;
        int id = -1;
        String errorMsg = null;

        if(id_param==null || id_param.isEmpty()){
            bad_request = true;
        }

        try{
            id = Integer.parseInt(id_param);
        }catch (NumberFormatException ex){
            ex.printStackTrace();
            bad_request = true;
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter");
            throw new UnavailableException("Missing parameter");
        }

        try{
            if(am.findAuctionDeadlineById(id).after(Timestamp.valueOf(LocalDateTime.now()))){
                ready = false;
                System.out.println("Not ready");
            }
        }catch (SQLException sqle){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue from database");
            throw new UnavailableException("Issue from database");
        }

        String path = null;

        if(!ready){
            errorMsg = "wrongClosure";
            path = "AuctionDetailsServlet?auctionId=" + id + "&errorMsg=" +errorMsg;
        }else{
            path = "AuctionDetailsServlet?auctionId=" + id;
            try{
                am.closeAuction(id);
            }catch (SQLException sqle){
                sqle.printStackTrace();
                throw new UnavailableException("Issue from database");
            }
        }
        response.sendRedirect(path);

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