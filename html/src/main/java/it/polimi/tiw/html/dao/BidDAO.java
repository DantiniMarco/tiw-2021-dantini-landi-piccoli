package it.polimi.tiw.html.dao;

import it.polimi.tiw.html.beans.Bid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public List<Bid> findBidsByIdAuction(int auctionId) throws SQLException {
        List<Bid> bids = new ArrayList<>();
        String query = "SELECT idbid, bidprice, UNIX_TIMESTAMP(datetime) AS datetime, idbidder, idauction" +
                " FROM bid WHERE idauction = ? ORDER BY datetime DESC";
        PreparedStatement pstatement = null;
        ResultSet result = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, auctionId);
            result = pstatement.executeQuery();
            while (result.next()) {
                Bid bid = new Bid();
                bid.setIdBid(result.getInt("idbid"));
                bid.setBidPrice(result.getFloat("bidprice"));
                bid.setDateTime(new Date(result.getLong("datetime")*1000));
                bid.setIdBidder(result.getInt("idbidder"));
                bid.setIdAuction(result.getInt("idauction"));
                bids.add(bid);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }finally {
            try{
                if(result!=null){
                    result.close();
                }
            }catch(SQLException e1){
                e1.printStackTrace();
                throw new SQLException(e1);
            }
            try{
                if(pstatement!=null){
                    pstatement.close();
                }
            }catch(SQLException e2){
                e2.printStackTrace();
                throw new SQLException(e2);
            }
        }

        return bids;
    }
}
