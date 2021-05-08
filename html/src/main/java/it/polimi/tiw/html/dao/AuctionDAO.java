package it.polimi.tiw.html.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.html.beans.Auction;
import it.polimi.tiw.html.beans.AuctionStatus;


public class AuctionDAO {
    private Connection con;
    private ArrayList<Auction> searchedList;

    public AuctionDAO(Connection connection, int idUser) {
        this.con = connection;
    }

    /**
     * this query returns the list of sorted by date auctions filtered by keyword
     * @author Marco
     * @param keyword
     * @return
     * @throws SQLException
     */
    public ArrayList<Auction> findOpenAuction(String keyword) throws SQLException {
        ArrayList<Auction> searchedList = null;

        String query = "SELECT idauction, deadline, minraise, initialprice FROM " +
                "(item JOIN auction ON idauction = iditem ) WHERE item.name LIKE = ? OR " +
                "item.description LIKE = ? HAVING auction.deadline >= " +
                "getDate() ORDER BY auction.deadline DESC";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, keyword);
            pstatement.setString(2, keyword);
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
                    searchedList.add(auction);
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
     * @param status
     * @return
     */
        public List<Auction> findAuctionsByStatus(AuctionStatus status) {
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
