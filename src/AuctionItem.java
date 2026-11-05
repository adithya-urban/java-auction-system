import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Represents an item being auctioned in the Online Auction System.
 * Each auction item has a seller, a starting price, a time window,
 * and tracks the current highest bid.
 */
public class AuctionItem {

    /** Unique identifier for this auction item (UUID). */
    private String itemId;

    /** The user ID of the seller who listed this item. */
    private String sellerId;

    /** The title/name of the auction item. */
    private String title;

    /** A detailed description of the auction item. */
    private String description;

    /** The minimum starting price for the auction. */
    private double startingPrice;

    /** The current highest bid amount (initialized to startingPrice). */
    private double currentHighestBid;

    /** The user ID of the current highest bidder, or null if no bids yet. */
    private String currentHighestBidderId;

    /** The date and time when the auction started. */
    private LocalDateTime startTime;

    /** The date and time when the auction ends. */
    private LocalDateTime endTime;

    /** The current status of this auction. */
    private AuctionStatus status;

    /**
     * Constructs a new AuctionItem with the given details.
     * The item is set to ACTIVE status, the start time is now,
     * and the end time is calculated from the specified duration.
     *
     * @param sellerId        the user ID of the seller
     * @param title           the title of the item
     * @param description     a description of the item
     * @param startingPrice   the minimum starting price
     * @param durationMinutes the auction duration in minutes
     */
    public AuctionItem(String sellerId, String title, String description,
                       double startingPrice, int durationMinutes) {
        this.itemId = UUID.randomUUID().toString();
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.currentHighestBidderId = null;
        this.startTime = LocalDateTime.now();
        this.endTime = this.startTime.plusMinutes(durationMinutes);
        this.status = AuctionStatus.ACTIVE;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return the unique item ID */
    public String getItemId() {
        return itemId;
    }

    /** @return the seller's user ID */
    public String getSellerId() {
        return sellerId;
    }

    /** @return the item title */
    public String getTitle() {
        return title;
    }

    /** @return the item description */
    public String getDescription() {
        return description;
    }

    /** @return the starting price */
    public double getStartingPrice() {
        return startingPrice;
    }

    /** @return the current highest bid amount */
    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    /** @return the user ID of the current highest bidder, or null */
    public String getCurrentHighestBidderId() {
        return currentHighestBidderId;
    }

    /** @return the auction start time */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /** @return the auction end time */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /** @return the current auction status */
    public AuctionStatus getStatus() {
        return status;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    /** @param itemId the item ID to set */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /** @param sellerId the seller's user ID to set */
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    /** @param title the item title to set */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @param description the item description to set */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @param startingPrice the starting price to set */
    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    /** @param currentHighestBid the current highest bid to set */
    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    /** @param currentHighestBidderId the highest bidder's user ID to set */
    public void setCurrentHighestBidderId(String currentHighestBidderId) {
        this.currentHighestBidderId = currentHighestBidderId;
    }

    /** @param startTime the auction start time to set */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /** @param endTime the auction end time to set */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /** @param status the auction status to set */
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    // -------------------------------------------------------------------------
    // Auction Time Methods
    // -------------------------------------------------------------------------

    /**
     * Checks whether this auction has expired (i.e., the current time
     * is after the end time).
     *
     * @return {@code true} if the auction has expired, {@code false} otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * Returns the number of seconds remaining until the auction ends.
     * Returns 0 if the auction has already expired.
     *
     * @return seconds remaining, or 0 if expired
     */
    public long getRemainingSeconds() {
        long remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Returns a string representation of this auction item, showing
     * key details including item ID, title, price info, and status.
     *
     * @return formatted string with auction item details
     */
    @Override
    public String toString() {
        return String.format(
                "AuctionItem[itemId=%s, title=%s, startingPrice=%.2f, " +
                "currentHighestBid=%.2f, status=%s, endTime=%s]",
                itemId, title, startingPrice, currentHighestBid, status, endTime);
    }
}
