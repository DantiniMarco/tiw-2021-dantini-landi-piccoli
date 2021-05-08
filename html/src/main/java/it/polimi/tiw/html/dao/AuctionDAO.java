package it.polimi.tiw.html.dao;

import it.polimi.tiw.html.beans.Auction;
import it.polimi.tiw.html.beans.AuctionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {
    private Connection con;

    public AuctionDAO(Connection con){
        this.con=con;
    }

    public List<Auction> findAuctionsByStaus(AuctionStatus status) {
        List<Auction> auctions = new ArrayList<>();

        try {

            String query = "SELECT * FROM auction WHERE status=?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                Auction auction = new Auction();

                auctions.add(auction);
            }
        } catch (SQLException e) {

        }
        return auctions;
    }
}
