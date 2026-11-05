/**
 * Represents the possible states of an auction item
 * in the Online Auction System.
 */
public enum AuctionStatus {

    /** The auction is currently accepting bids. */
    ACTIVE("Active"),

    /** The auction has ended and the item was sold to the highest bidder. */
    SOLD("Sold"),

    /** The auction has ended without any qualifying bids. */
    UNSOLD("Unsold"),

    /** The auction was cancelled by the seller or an administrator. */
    CANCELLED("Cancelled");

    /** Human-readable display label for this status. */
    private final String label;

    /**
     * Constructs an AuctionStatus with the given display label.
     *
     * @param label the human-readable label
     */
    AuctionStatus(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable display label for this status.
     *
     * @return the display label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the display label as the string representation.
     *
     * @return the display label
     */
    @Override
    public String toString() {
        return label;
    }
}
