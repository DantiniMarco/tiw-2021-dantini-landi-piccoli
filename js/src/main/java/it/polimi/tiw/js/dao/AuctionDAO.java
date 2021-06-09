package it.polimi.tiw.js.dao;

import it.polimi.tiw.js.beans.Auction;
import it.polimi.tiw.js.beans.AuctionStatus;
import it.polimi.tiw.js.beans.ExtendedAuction;
import it.polimi.tiw.js.beans.Item;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AuctionDAO {
    private Connection con;
    private ArrayList<Auction> searchedList;

    public AuctionDAO(Connection connection) {
        this.con = connection;
    }

    /**
     * this query returns the list of sorted by date auctions filtered by keyword
     * @author Marco D'Antini
     * @param keyword
     * @return
     * @throws SQLException
     */
    public List<ExtendedAuction> findOpenAuction(String keyword, int userid) throws SQLException {
        List<ExtendedAuction> searchedList= new ArrayList<>();

        String query = "SELECT idauction, UNIX_TIMESTAMP(deadline) AS deadline, minraise, initialprice, name FROM " +
                "(item NATURAL JOIN auction) WHERE auction.idcreator != ? AND (item.name LIKE ? OR " +
                "item.description LIKE ?) AND auction.status = 0 AND auction.deadline >= CURDATE() ORDER BY auction.deadline DESC";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, userid);
            pstatement.setString(2, "%" + keyword + "%");
            pstatement.setString(3, "%" + keyword + "%");

            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results
                    return null;
                else {
                    while (result.next()) {
                        ExtendedAuction auction = new ExtendedAuction();
                        auction.setInitialPrice(result.getInt("initialprice"));
                        auction.setMinRaise(result.getInt("minraise"));
                        auction.setDeadline(ZonedDateTime.ofInstant(Instant.ofEpochSecond(result.getLong("deadline")), ZoneOffset.UTC));
                        auction.setIdAuction(result.getInt("idauction"));
                        auction.setItemName(result.getString("name"));
                        searchedList.add(auction);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
        return searchedList;
    }

    /**
     * @author Alfredo Landi
     * @param idUser of the logged user in the current session
     * @param status of an auction, it can be "0" -> CLOSED or "1" -> OPEN
     * @return a list of auctions for the selected status in ascending order by deadline
     */
        public List<ExtendedAuction> findAuctionsByIdAndStatus(int idUser, AuctionStatus status) throws SQLException{
            List<ExtendedAuction> auctions = new ArrayList<>();
            //FIXME: must fix SQL query
            String query = "SELECT auction.idauction, item.name, item.image, item.description, max(bid.bidprice) AS price, auction.minraise, UNIX_TIMESTAMP(auction.deadline) AS deadline FROM auction NATURAL JOIN item LEFT JOIN bid ON auction.idauction=bid.idauction JOIN user ON auction.idcreator=user.iduser WHERE user.iduser=? AND auction.status=? GROUP BY auction.idauction ORDER BY deadline ASC";
            PreparedStatement pstatement = null;
            ResultSet result = null;

            try {
                pstatement = con.prepareStatement(query);
                pstatement.setInt(1, idUser);
                pstatement.setInt(2, status.getValue());
                result = pstatement.executeQuery();
                while (result.next()) {
                    ExtendedAuction auction = new ExtendedAuction();
                    auction.setIdAuction(result.getInt("idauction"));
                    auction.setItemName(result.getString("name"));
                    auction.setItemImage(result.getString("image"));
                    auction.setItemDescription(result.getString("description"));
                    auction.setPrice(result.getFloat("price"));
                    auction.setMinRaise(result.getFloat("minraise"));
                    auction.setDeadline(ZonedDateTime.ofInstant(Instant.ofEpochSecond(result.getLong("deadline")), ZoneOffset.UTC));
                    auctions.add(auction);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Sono nella catch di resultset e pstatement");
                throw new SQLException(e);
            }finally {
                try{
                    if(result!=null){
                        result.close();
                    }
                }catch(SQLException e1){
                    System.out.println("Non riesco a chiudere il resultset");
                    e1.printStackTrace();
                }
                try{
                    if(pstatement!=null){
                        pstatement.close();
                    }
                }catch(SQLException e2){
                    System.out.println("Non riesco a chiudere il pstatement");
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
        public int insertNewAuction(String itemName, String itemImage, String itemDescription, float initialPrice, float minRaise, Timestamp deadline, int idCreator) throws SQLException{
            int itemId;
            Item newItem = new Item(itemName, itemImage, itemDescription);
            ItemDAO im = new ItemDAO(con);
            PreparedStatement pstatement = null;
            int result = 0;
            String query= "INSERT INTO auction (initialprice, minraise, deadline, idcreator, iditem, status) VALUES (?,?,?,?,?,?)";
            con.setAutoCommit(false);
            try{
                itemId=im.insertNewItem(newItem);
                if(itemId==-1){
                    throw new SQLException();
                }
                pstatement = con.prepareStatement(query);
                pstatement.setFloat(1,initialPrice);
                pstatement.setFloat(2, minRaise);
                pstatement.setTimestamp(3, deadline);
                pstatement.setInt(4, idCreator);
                pstatement.setInt(5, itemId);
                pstatement.setInt(6, AuctionStatus.OPEN.getValue());
                result= pstatement.executeUpdate();
                con.commit();
            }catch(SQLException sqle){
                sqle.printStackTrace();
                con.rollback();
            }finally {
                try{
                    if(pstatement!=null){
                        pstatement.close();
                    }
                }catch(SQLException e1){
                    e1.printStackTrace();
                }
                con.setAutoCommit(true);
            }

            return result;
        }

    /***
     * @author Alfredo Landi
     * Set CLOSED (1) the attribute "status" of the selected auction
     * @param auctionId of the auction to close
     * @return 0 for success, 1 for unsuccess
     */
    public int closeAuction(int auctionId) throws SQLException{
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
            throw new SQLException(sqle);
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
    public ExtendedAuction findAuctionById(int auctionId) throws SQLException{
        String query = "SELECT item.name, item.image, item.description, max(bid.bidprice) AS price, auction.minraise, UNIX_TIMESTAMP(auction.deadline) AS deadline, auction.status FROM auction NATURAL JOIN item LEFT JOIN bid ON auction.idauction=bid.idauction WHERE auction.idauction = ?";
        PreparedStatement pstatement = null;
        ResultSet result = null;
        ExtendedAuction auction = new ExtendedAuction();

        try{
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, auctionId);
            result=pstatement.executeQuery();
            auction.setIdAuction(auctionId);
            while(result.next()){
                auction.setItemName(result.getString("name"));
                auction.setItemImage(result.getString("image"));
                auction.setItemDescription(result.getString("description"));
                auction.setPrice(result.getFloat("price"));
                auction.setMinRaise(result.getFloat("minraise"));
                auction.setDeadline(ZonedDateTime.ofInstant(Instant.ofEpochSecond(result.getLong("deadline")), ZoneOffset.UTC));
                auction.setStatus(AuctionStatus.getAuctionStatusFromInt(result.getInt("status")));
            }
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

    /***
     * @author Alfredo Landi
     * @param usernameId of the current user
     * @return a list of auction ids for the selected user
     * @throws SQLException
     */
    public List<Integer> findAuctionIdsByUsernameId(int usernameId) throws SQLException{
        String query = "SELECT idauction FROM auction WHERE idcreator = ?";
        ResultSet result;
        List<Integer> ids = new ArrayList<>();
        int id = 0;
        try (PreparedStatement pstatement = con.prepareStatement(query)){
            pstatement.setInt(1, usernameId);
            result = pstatement.executeQuery();
            while(result.next()){
                id = result.getInt("idauction");
                ids.add(id);
            }
        }catch (SQLException sqle){
            throw new SQLException(sqle);
        }

        return ids;
    }

    /***
     * @author Alfredo Landi
     * @param id of selected auction
     * @return the deadline of the selected auction in timestamp format
     * @throws SQLException in case of an issue from database
     */
    public Timestamp findAuctionDeadlineById(int id) throws SQLException{
        String query = "SELECT deadline FROM auction WHERE idauction = ?";
        ResultSet result = null;
        Timestamp deadline = null;

        try (PreparedStatement pstatement = con.prepareStatement(query)){
            pstatement.setInt(1, id);
            result = pstatement.executeQuery();
            while(result.next()){
                deadline = result.getTimestamp("deadline");
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();

        }finally {
            if(result!=null){
                result.close();
            }
        }

        return deadline;
    }

    /**
     * @author Marco D'Antini
     * used to prevent not legit usage of database by the user
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<Integer> findLegitIdsBid(int userId) throws SQLException{
        String query = "SELECT idauction from auction WHERE idcreator != ? AND status = 0";
        List<Integer> idList = new ArrayList<>();
        ResultSet result = null;

        try(PreparedStatement pstatement = con.prepareStatement(query)){
            pstatement.setInt(1, userId);
            result = pstatement.executeQuery();
            while(result.next()){
                idList.add(result.getInt("idauction"));
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
        } finally {
            if(result!=null){
                result.close();
            }
        }
        return idList;
    }
}