import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single bid placed on an auction item
 * in the Online Auction System.
 * Each bid records the bidder, the item, the amount, and
 * the timestamp when it was placed.
 */
public class Bid {

    /** Unique identifier for this bid (UUID). */
    private String bidId;

    /** The ID of the auction item this bid is placed on. */
    private String itemId;

    /** The user ID of the bidder. */
    private String bidderId;

    /** The bid amount. */
    private double amount;

    /** The date and time when this bid was placed. */
    private LocalDateTime timestamp;

    /**
     * Constructs a new Bid with auto-generated ID and current timestamp.
     *
     * @param itemId   the ID of the auction item being bid on
     * @param bidderId the user ID of the bidder
     * @param amount   the bid amount
     */
    public Bid(String itemId, String bidderId, double amount) {
        this.bidId = UUID.randomUUID().toString();
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return the unique bid ID */
    public String getBidId() {
        return bidId;
    }

    /** @return the auction item ID */
    public String getItemId() {
        return itemId;
    }

    /** @return the bidder's user ID */
    public String getBidderId() {
        return bidderId;
    }

    /** @return the bid amount */
    public double getAmount() {
        return amount;
    }

    /** @return the timestamp when this bid was placed */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a string representation of this bid, showing
     * bid ID, item ID, bidder ID, amount, and timestamp.
     *
     * @return formatted string with bid details
     */
    @Override
    public String toString() {
        return String.format("Bid[bidId=%s, itemId=%s, bidderId=%s, amount=%.2f, timestamp=%s]",
                bidId, itemId, bidderId, amount, timestamp);
    }
}
