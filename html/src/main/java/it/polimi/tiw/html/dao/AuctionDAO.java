package it.polimi.tiw.html.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.polimi.tiw.html.beans.Auction;
import it.polimi.tiw.html.beans.AuctionStatus;


public class AuctionDAO {
    private Connection con;
    private ArrayList<Auction> searchedList;

    public AuctionDAO(Connection connection) {
        this.con = connection;
    }

    /**
     * this query returns the list of sorted by date auctions filtered by keyword
     * @author Marco
     * @param keyword
     * @return
     * @throws SQLException
     */
    public HashMap<Auction, String> findOpenAuction(String keyword) throws SQLException {
        HashMap<Auction, String> searchedList= new HashMap<Auction, String>();

        String query = "SELECT idauction, deadline, minraise, initialprice, name FROM " +
                "(item INNER JOIN auction) WHERE (item.name LIKE ? OR " +
                "item.description LIKE ?) AND auction.deadline >= " +
                "CURDATE() ORDER BY auction.deadline DESC";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, "%" + keyword + "%");
            pstatement.setString(2, "%" + keyword + "%");
            result = pstatement.executeQuery();
            if (!result.isBeforeFirst()) // no results
                return null;
            else {
                while (result.next()) {
                    Auction auction = new Auction();
                    auction.setInitialPrice(result.getInt("initialprice"));
                    auction.setMinRaise(result.getInt("minraise"));
                    auction.setDeadline(result.getDate("deadline"));
                    auction.setIdAuction(result.getInt("idauction"));
                    searchedList.put(auction, result.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException(e1);
            }
            try {
                pstatement.close();
            } catch (Exception e2) {
                throw new SQLException(e2);
            }
        }
        return searchedList;
    }

    /**
     * @author Alfredo
     * @param status of an auction, it can be "0" -> CLOSED or "1" -> OPEN
     * @return a list of auctions for the selected status
     */
        public List<Auction> findAuctionsByStatus(AuctionStatus status) throws SQLException{
            List<Auction> auctions = new ArrayList<>();
            String query = "SELECT * FROM auction WHERE status=?";
            PreparedStatement pstatement = null;
            ResultSet result = null;

            try {
                pstatement = con.prepareStatement(query);
                pstatement.setInt(1, status.getValue());
                result = pstatement.executeQuery();
                while (result.next()) {
                    Auction auction = new Auction();
                    auction.setIdAuction(result.getInt("idAuction"));
                    auction.setInitialPrice(result.getFloat("initialprice"));
                    auction.setMinRaise(result.getFloat("minraise"));
                    auction.setDeadline(result.getDate("deadline"));
                    auction.setIdCreator(result.getInt("idcreator"));
                    auction.setIdItem(result.getInt("iditem"));
                    auction.setStatus((AuctionStatus) result.getObject("status"));
                    auctions.add(auction);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException(e);
            }
            return auctions;
        }

    }
