package it.polimi.tiw.html.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import it.polimi.tiw.html.beans.Auction;
import it.polimi.tiw.html.beans.AuctionStatus;
import it.polimi.tiw.html.beans.Item;


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
    public LinkedHashMap<Auction, String> findOpenAuction(String keyword) throws SQLException {
        LinkedHashMap<Auction, String> searchedList= new LinkedHashMap<Auction, String>();

        String query = "SELECT idauction, UNIX_TIMESTAMP(deadline) AS deadline, minraise, initialprice, name FROM " +
                "(item NATURAL JOIN auction) WHERE (item.name LIKE ? OR " +
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
                    auction.setDeadline(new Date(result.getLong("deadline")*1000));
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
     * @author Alfredo Landi
     * @param status of an auction, it can be "0" -> CLOSED or "1" -> OPEN
     * @return a list of auctions for the selected status in ascending order by deadline
     */
        public List<Auction> findAuctionsByStatus(AuctionStatus status) throws SQLException{
            List<Auction> auctions = new ArrayList<>();
            String query = "SELECT * FROM auction WHERE status=? ORDER BY deadline ASC";
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
                    auction.setDeadline(new Date(result.getDate("deadline").getTime()));
                    auction.setIdCreator(result.getInt("idcreator"));
                    auction.setIdItem(result.getInt("iditem"));
                    auction.setStatus(AuctionStatus.getAuctionStatusFromInt(result.getInt("status")));
                    auctions.add(auction);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException(e);
            }finally {
                try{
                    if(result!=null){
                        result.close();
                    }
                }catch(SQLException e1){
                    e1.printStackTrace();
                }
                try{
                    if(pstatement!=null){
                        pstatement.close();
                    }
                }catch(SQLException e2){
                    e2.printStackTrace();
                }
            }
            return auctions;
        }

    /***
     * @author Alfredo Landi
     * This method prepares two queries, one to item relation to add a new item in the db
     * and another one to auction relation to initialize the new auction
     * @param itemName of new item added by user
     * @param itemImage of new item added by user
     * @param itemDescription of new item added by user
     * @param initialPrice of the auction selected by user
     * @param minRaise of the auction selected by user
     * @param deadline of the auction selected by user
     * @param idCreator of the user
     * @return code of success or unsuccess
     */
        public int insertNewAuction(String itemName, String itemImage, String itemDescription, float initialPrice, float minRaise, Date deadline, int idCreator) throws SQLException{
            int itemId;
            Item newItem = new Item(itemName, itemImage, itemDescription);
            ItemDAO im = new ItemDAO(con);
            PreparedStatement pstatement = null;
            int result = 0;
            String query= "INSERT INTO auction (initialprice, minraise, deadline, idcreator, iditem, status) VALUES (?,?,?,?,?,?)";
            try{
                con.setAutoCommit(false);
                itemId=im.insertNewItem(newItem);
                if(itemId==-1){
                    throw new SQLException();
                }
                pstatement = con.prepareStatement(query);
                pstatement.setFloat(1,initialPrice);
                pstatement.setFloat(2, minRaise);
                pstatement.setDate(3, (java.sql.Date)deadline);
                pstatement.setInt(4, idCreator);
                pstatement.setInt(5, itemId);
                pstatement.setObject(6, AuctionStatus.OPEN);
                result= pstatement.executeUpdate();
                con.commit();

            }catch(SQLException sqle){
                sqle.printStackTrace();
            }finally {
                try{
                    if(pstatement!=null){
                        pstatement.close();
                    }
                }catch(SQLException e1){
                    e1.printStackTrace();
                }
            }

            return result;
        }

    /***
     * @author Alfredo Landi
     * Set CLOSED (1) the attribute "status" of the selected auction
     * @param auctionId of the auction to close
     * @return 0 for success, 1 for unsuccess
     */
    public int closeAuction(int auctionId){
        int code=0;
        String query = "UPDATE auction SET status=? WHERE idauction = ?";
        PreparedStatement pstatement = null;
        try{
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, AuctionStatus.CLOSED.getValue());
            pstatement.setInt(2, auctionId);
            code = pstatement.executeUpdate();
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }finally {
            try{
                if(pstatement!=null){
                    pstatement.close();
                }
            }catch(SQLException e1){
                e1.printStackTrace();
            }
        }

        return code;
    }

    /***
     * @author Alfredo Landi
     * @param auctionId of the auction needed
     * @return the required auction
     */
    public Auction findAuctionById(int auctionId) throws SQLException{
        String query = "SELECT * FROM auction WHERE idauction = ?";
        PreparedStatement pstatement = null;
        ResultSet result = null;
        Auction auction = new Auction();

        try{
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, auctionId);
            result=pstatement.executeQuery();
            auction.setIdAuction(result.getInt("auctionId"));
            auction.setInitialPrice(result.getFloat("initialprice"));
            auction.setMinRaise(result.getFloat("minraise"));
            auction.setDeadline(result.getDate("deadline"));
            auction.setIdCreator(result.getInt("idcreator"));
            auction.setIdItem(result.getInt("iditem"));
            auction.setStatus(AuctionStatus.OPEN);
        }catch (SQLException sqle){
            sqle.printStackTrace();
            throw new SQLException(sqle);
        }finally {
            try{
                if(result!=null){
                    result.close();
                }
            }catch(SQLException e1){
                e1.printStackTrace();
            }
            try{
                if(pstatement!=null){
                    pstatement.close();
                }
            }catch(SQLException e2){
                e2.printStackTrace();
            }
        }

        return auction;
    }



}
