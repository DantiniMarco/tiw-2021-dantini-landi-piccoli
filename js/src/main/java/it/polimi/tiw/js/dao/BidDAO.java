package it.polimi.tiw.js.dao;

import it.polimi.tiw.js.beans.Bid;
import it.polimi.tiw.js.beans.ExtendedAuction;
import it.polimi.tiw.js.beans.ExtendedBid;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BidDAO {
    private Connection con;

    public BidDAO(Connection con){
        this.con=con;
    }

    /***
     * @author Alfredo Landi
     * @param auctionId of the current auction
     * @return a list o bids for the current auction
     */
    public List<ExtendedBid> findBidsByIdAuction(int auctionId) throws SQLException {
        List<ExtendedBid> bids = new ArrayList<>();
        String query = "SELECT username, bidprice, UNIX_TIMESTAMP(datetime) AS datetime FROM bid JOIN user ON idbidder=iduser WHERE idauction = ? ORDER BY datetime DESC";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, auctionId);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    ExtendedBid bid = new ExtendedBid();
                    bid.setBidderUsername(result.getString("username"));
                    bid.setBidPrice(result.getFloat("bidprice"));
                    bid.setDateTime(new Date(result.getLong("datetime") * 1000));
                    bids.add(bid);
                }
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return bids;
    }

    /**
     * @author Marco D'Antini
     * query used to find the ended auction awarded by the user
     */
    public ArrayList<ExtendedAuction> findAwardedBids(int idBidder)throws SQLException{
        ArrayList<ExtendedAuction> bidsAwarded = new ArrayList<>();
        String query = "SELECT bidprice, UNIX_TIMESTAMP(datetime) AS datetime, name, description, image " +
                "FROM (bid NATURAL JOIN auction NATURAL JOIN item) WHERE auction.status = 1 AND bid.idbidder = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, idBidder);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    ExtendedAuction exAuction = new ExtendedAuction();
                    exAuction.setPrice(result.getFloat("bidprice"));
                    exAuction.setItemName(result.getString("name"));
                    exAuction.setItemDescription(result.getString("description"));
                    exAuction.setItemImage(result.getString("image"));
                    bidsAwarded.add(exAuction);
                }
            }}
        catch (SQLException sqle){
                sqle.printStackTrace();
        }
            return bidsAwarded;
    }

    /**
     * @author Marco D'Antini
     * query used to insert a new legit Bid into the daatabase called by  GotoBidPage
     * @param bidPrice
     * @param idBidder
     * @param idAuction
     * @return the idBid of the bid added in the database, 0 in case of db error
     * @throws SQLException
     */
    public int insertNewBid(float bidPrice, int idBidder, int idAuction)throws SQLException{
        ResultSet result;
        Date date = new Date();
        Long dateTime = date.getTime();
        int idBid;
        String query = "INSERT INTO bid ( bidprice, datetime, idbidder, idauction) VALUES (?,now(),?,?)";
        PreparedStatement pstatement = null;

        try{
            pstatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstatement.setFloat(1, bidPrice);
            pstatement.setInt(2, idBidder);
            pstatement.setInt(3, idAuction);
            int affectedRows = pstatement.executeUpdate();
            if(affectedRows == 0){
                return -1;
            }
            result = pstatement.getGeneratedKeys();
            if(result!= null && result.next())
                return result.getInt(1);
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return -1;
    }
}
