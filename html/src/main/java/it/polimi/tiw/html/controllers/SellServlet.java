package it.polimi.tiw.html.controllers;

import it.polimi.tiw.html.beans.Auction;
import it.polimi.tiw.html.beans.AuctionStatus;
import it.polimi.tiw.html.dao.AuctionDAO;
import it.polimi.tiw.html.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.UnavailableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SellServlet extends HttpServlet {
    private Connection con;

    public void init() throws ServletException{
        con = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
        AuctionDAO am= new AuctionDAO(con);
        List<Auction> auctions;
        try{
            auctions = am.findAuctionsByStatus(AuctionStatus.CLOSED);
        }catch(SQLException sql){
            sql.printStackTrace();
            throw new UnavailableException("Error executing query");
        }

        for(Auction auction : auctions){
            System.out.println(auction.toString());
        }

    }
}