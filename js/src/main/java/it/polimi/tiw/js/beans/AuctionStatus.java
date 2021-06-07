package it.polimi.tiw.js.beans;

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
        if (value == 0) {
            return AuctionStatus.OPEN;
        } else if (value == 1) {
            return AuctionStatus.CLOSED;
        }
        throw new IllegalStateException("Unexpected value: " + value);
    }
}
