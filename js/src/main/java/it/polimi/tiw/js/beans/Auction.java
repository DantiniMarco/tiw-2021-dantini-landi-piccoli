package it.polimi.tiw.js.beans;

import java.time.ZonedDateTime;
import java.util.Date;

public class Auction {
    private int idAuction;
    private float initialPrice;
    private float minRaise;
    private ZonedDateTime deadline;
    private int idCreator;
    private int idItem;
    private AuctionStatus status;

    public int getIdAuction() {
        return idAuction;
    }

    public void setIdAuction(int idAuction) {
        this.idAuction = idAuction;
    }

    public float getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(float initialPrice) {
        this.initialPrice = initialPrice;
    }

    public float getMinRaise() {
        return minRaise;
    }

    public void setMinRaise(float minRaise) {
        this.minRaise = minRaise;
    }

    public ZonedDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(ZonedDateTime deadline) {
        this.deadline = deadline;
    }

    public int getIdCreator() {
        return idCreator;
    }

    public void setIdCreator(int idCreator) {
        this.idCreator = idCreator;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
