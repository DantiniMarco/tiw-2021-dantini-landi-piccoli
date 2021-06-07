package it.polimi.tiw.js.controllers;

import it.polimi.tiw.js.beans.AuctionStatus;
import it.polimi.tiw.js.beans.ExtendedAuction;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        ServletContext servletContext = getServletContext();

        List<String> timeLeftOpen = calculateTime(openAuctions);
        List<String> timeLeftClosed = calculateTime(closedAuctions);
        LocalDateTime dateLowerBound = LocalDateTime.now();
        dateLowerBound = dateLowerBound.plusDays(1);
        DateTimeFormatter lowerBoundFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm");
        String lowerBoundFormatted = dateLowerBound.format(lowerBoundFormatter);
        LocalDateTime dateUpperBound = LocalDateTime.now();
        dateUpperBound = dateUpperBound.plusWeeks(2);
        DateTimeFormatter upperBoundFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm");
        String upperBoundFormatted = dateUpperBound.format(upperBoundFormatter);

    }

    private List<String> calculateTime(List<ExtendedAuction> auctionList) {
        List<String> timeLeftlist = new ArrayList<>();
        for (ExtendedAuction auction : auctionList) {
            long diff = auction.getDeadline().getTime() - new Date().getTime();

            if (diff <= 3600) {
                if (diff < 1) {
                    timeLeftlist.add("Expired");
                } else {
                    timeLeftlist.add("Less than an hour");
                }
            } else {
                long diffHours = diff / (60 * 60 * 1000) % 24;
                long diffDays = diff / (24 * 60 * 60 * 1000);
                timeLeftlist.add(((diffDays > 0)?diffDays + " days and ":"")+ diffHours + " hours");
            }
        }
        return timeLeftlist;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void destroy(){
        try{
            ConnectionHandler.closeConnection(connection);
        }catch (SQLException sql){
            sql.printStackTrace();
        }
    }
}