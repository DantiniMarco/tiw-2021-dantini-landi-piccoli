package it.polimi.tiw.html.beans;

import java.time.ZonedDateTime;
import java.util.Date;

public class Bid {
    private int idBid;
    private float bidPrice;
    private ZonedDateTime dateTime;
    private int idBidder;
    private int idAuction;

    public int getIdBid() {
        return idBid;
    }

    public void setIdBid(int idBid) {
        this.idBid = idBid;
    }

    public float getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(float bidPrice) {
        this.bidPrice = bidPrice;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getIdBidder() {
        return idBidder;
    }

    public void setIdBidder(int idBidder) {
        this.idBidder = idBidder;
    }

    public int getIdAuction() {
        return idAuction;
    }

    public void setIdAuction(int idAuction) {
        this.idAuction = idAuction;
    }
}
