package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

@WebServlet("/SellHelperServlet")
public class SellHelperServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection con;

    public void init() throws ServletException{
        con = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String itemName = request.getParameter("itemName");
        String itemImage = request.getParameter("itemImage");
        String itemDescription = request.getParameter("itemDescription");
        String initialPrice_param = request.getParameter("initialPrice");
        String minRaise_param = request.getParameter("minRaise");
        String deadline_param = request.getParameter("deadline");
        int bad_request=0;
        float initialPrice=0;
        float minRaise =0;
        Date deadline= null;
        AuctionDAO am = new AuctionDAO(con);

        if(itemName==null || itemName.isEmpty() || itemImage==null || itemImage.isEmpty() || itemDescription==null || itemDescription.isEmpty()
        || initialPrice_param==null || initialPrice_param.isEmpty() || minRaise_param==null || minRaise_param.isEmpty() || deadline_param==null
        || deadline_param.isEmpty()){
            bad_request=1;
            /*response.sendError();
            throw new UnavailableException("Parameter/s missing");*/
        }

        try{
            initialPrice= Float.parseFloat(initialPrice_param);
            minRaise = Float.parseFloat(minRaise_param);
            //deadline = deadline_param;
        }catch(NumberFormatException e){
            bad_request=1;
        }

        if(bad_request==1){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter/s missing");
        }

        //Per ottenere idCreator serve parte Marco
        int idCreator = 0;
        try{
            am.insertNewAuction(itemName,itemImage, itemDescription, initialPrice, minRaise, deadline, idCreator);
        }catch (SQLException sqle){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue from database");
        }

        response.sendRedirect("SellServlet");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }


    @Override
    public void destroy() {
        try{
            if(con!=null){
                con.close();
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }
    }
}
