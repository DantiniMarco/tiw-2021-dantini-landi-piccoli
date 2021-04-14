package it.polimi.tiw.html.beans;

public enum AuctionStatus {
    OPEN(0), CLOSED(1);

    private final int value;

    AuctionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuctionStatus getAuctionStatusFromInt(int value) {
        switch (value){
            case 0:
                return AuctionStatus.OPEN;
            case 1:
                return AuctionStatus.CLOSED;
        }
        return null;
    }
}
