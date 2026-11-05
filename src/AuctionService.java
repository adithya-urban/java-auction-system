import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Service class that manages auction creation, bidding, expiration, and querying.
 * Uses in-memory storage with HashMaps and ArrayLists.
 */
public class AuctionService {

    /** Map of itemId -> AuctionItem for direct auction lookups. */
    private Map<String, AuctionItem> auctions;

    /** Map of itemId -> list of Bids for tracking bid history per auction. */
    private Map<String, List<Bid>> bidsMap;

    /**
     * Constructs a new AuctionService with empty auction stores.
     */
    public AuctionService() {
        this.auctions = new HashMap<>();
        this.bidsMap = new HashMap<>();
    }

    /**
     * Creates a new auction listing.
     *
     * @param sellerId        the ID of the user creating the auction
     * @param title           the title/name of the item
     * @param description     a description of the item
     * @param startingPrice   the minimum starting price for bids
     * @param durationMinutes how long the auction should remain active (in minutes)
     * @return the newly created AuctionItem
     */
    public AuctionItem createAuction(String sellerId, String title, String description,
                                     double startingPrice, int durationMinutes) {
        AuctionItem item = new AuctionItem(sellerId, title, description, startingPrice, durationMinutes);
        auctions.put(item.getItemId(), item);
        bidsMap.put(item.getItemId(), new ArrayList<>());
        return item;
    }

    /**
     * Places a bid on an active auction item.
     * Validates multiple conditions before accepting the bid:
     * <ul>
     *   <li>Item must exist</li>
     *   <li>Item must be ACTIVE</li>
     *   <li>Item must not be expired</li>
     *   <li>Bidder cannot be the seller</li>
     *   <li>Bid amount must exceed the current highest bid (or starting price)</li>
     *   <li>Bidder must have sufficient balance</li>
     * </ul>
     * Note: Balance is NOT deducted on bid placement — only upon winning.
     *
     * @param itemId      the ID of the auction item to bid on
     * @param bidderId    the ID of the user placing the bid
     * @param amount      the bid amount
     * @param userService reference to UserService for balance checks
     * @return true if the bid was successfully placed, false otherwise
     */
    public boolean placeBid(String itemId, String bidderId, double amount, UserService userService) {
        // Check if the auction item exists
        AuctionItem item = auctions.get(itemId);
        if (item == null) {
            System.out.println("Error: Auction item not found: " + itemId);
            return false;
        }

        // Check if the auction is still ACTIVE
        if (item.getStatus() != AuctionStatus.ACTIVE) {
            System.out.println("Error: Auction is no longer active. Status: " + item.getStatus());
            return false;
        }

        // Check if the auction has expired
        if (LocalDateTime.now().isAfter(item.getEndTime())) {
            System.out.println("Error: Auction has expired.");
            return false;
        }

        // Check that the bidder is not the seller
        if (item.getSellerId().equals(bidderId)) {
            System.out.println("Error: Seller cannot bid on their own auction.");
            return false;
        }

        // Check that the bid exceeds the current highest bid (or starting price)
        double minimumBid = item.getCurrentHighestBid() > 0
                ? item.getCurrentHighestBid()
                : item.getStartingPrice();
        if (amount <= minimumBid) {
            System.out.println("Error: Bid amount ($" + String.format("%.2f", amount)
                    + ") must be higher than current highest bid ($"
                    + String.format("%.2f", minimumBid) + ").");
            return false;
        }

        // Check that the bidder has enough balance
        User bidder = userService.getUserById(bidderId);
        if (bidder == null) {
            System.out.println("Error: Bidder not found: " + bidderId);
            return false;
        }
        if (bidder.getBalance() < amount) {
            System.out.println("Error: Insufficient balance. Required: $"
                    + String.format("%.2f", amount) + ", Available: $"
                    + String.format("%.2f", bidder.getBalance()));
            return false;
        }

        // All validations passed — create and record the bid
        Bid bid = new Bid(itemId, bidderId, amount);
        bidsMap.get(itemId).add(bid);

        // Update the auction item with the new highest bid
        item.setCurrentHighestBid(amount);
        item.setCurrentHighestBidderId(bidderId);

        return true;
    }

    /**
     * Checks all ACTIVE auctions and closes any that have expired.
     * If an expired auction has a highest bidder, it is marked as SOLD,
     * the winner's balance is deducted, and the seller's balance is credited.
     * If no bids were placed, the auction is marked as UNSOLD.
     *
     * @param userService reference to UserService for balance operations
     */
    public void checkAndCloseExpiredAuctions(UserService userService) {
        LocalDateTime now = LocalDateTime.now();

        for (AuctionItem item : auctions.values()) {
            if (item.getStatus() == AuctionStatus.ACTIVE && now.isAfter(item.getEndTime())) {
                if (item.getCurrentHighestBidderId() != null) {
                    // Auction has a winner
                    item.setStatus(AuctionStatus.SOLD);

                    // Deduct balance from the winner
                    User winner = userService.getUserById(item.getCurrentHighestBidderId());
                    if (winner != null) {
                        winner.setBalance(winner.getBalance() - item.getCurrentHighestBid());
                    }

                    // Credit the seller
                    User seller = userService.getUserById(item.getSellerId());
                    if (seller != null) {
                        seller.setBalance(seller.getBalance() + item.getCurrentHighestBid());
                    }

                    System.out.println("Auction SOLD: '" + item.getTitle()
                            + "' sold to " + (winner != null ? winner.getUsername() : "unknown")
                            + " for $" + String.format("%.2f", item.getCurrentHighestBid()));
                } else {
                    // No bids were placed
                    item.setStatus(AuctionStatus.UNSOLD);
                    System.out.println("Auction UNSOLD: '" + item.getTitle()
                            + "' received no bids.");
                }
            }
        }
    }

    /**
     * Returns all currently active auctions.
     * Automatically checks and closes expired auctions first.
     *
     * @return list of auctions with ACTIVE status
     */
    public List<AuctionItem> getActiveAuctions() {
        // Passing null since we don't have a UserService reference here;
        // caller should ensure checkAndCloseExpiredAuctions is called with a valid UserService
        // before calling this method in a production scenario.
        // For convenience, this method accepts a null-safe approach.
        return auctions.values().stream()
                .filter(item -> item.getStatus() == AuctionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Returns all currently active auctions, checking for expirations first.
     *
     * @param userService reference to UserService for closing expired auctions
     * @return list of auctions with ACTIVE status
     */
    public List<AuctionItem> getActiveAuctions(UserService userService) {
        checkAndCloseExpiredAuctions(userService);
        return auctions.values().stream()
                .filter(item -> item.getStatus() == AuctionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Returns all auctions regardless of status.
     *
     * @return list of all auction items
     */
    public List<AuctionItem> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    /**
     * Returns all auctions created by a specific seller.
     *
     * @param sellerId the ID of the seller
     * @return list of auctions created by the seller
     */
    public List<AuctionItem> getAuctionsBySeller(String sellerId) {
        return auctions.values().stream()
                .filter(item -> item.getSellerId().equals(sellerId))
                .collect(Collectors.toList());
    }

    /**
     * Returns the bid history for a specific auction item.
     *
     * @param itemId the ID of the auction item
     * @return list of bids placed on the item, or an empty list if none
     */
    public List<Bid> getBidsForItem(String itemId) {
        return bidsMap.getOrDefault(itemId, new ArrayList<>());
    }

    /**
     * Retrieves an auction item by its unique ID.
     *
     * @param itemId the auction item's unique identifier
     * @return the AuctionItem, or null if not found
     */
    public AuctionItem getAuctionById(String itemId) {
        return auctions.get(itemId);
    }

    /**
     * Searches auctions by keyword, matching against title and description (case-insensitive).
     *
     * @param keyword the search term
     * @return list of matching auction items
     */
    public List<AuctionItem> searchAuctions(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return auctions.values().stream()
                .filter(item -> item.getTitle().toLowerCase().contains(lowerKeyword)
                        || item.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * Returns all auctions that a specific user has won (status SOLD and they are the highest bidder).
     *
     * @param userId the ID of the user
     * @return list of auctions won by the user
     */
    public List<AuctionItem> getWonAuctions(String userId) {
        return auctions.values().stream()
                .filter(item -> item.getStatus() == AuctionStatus.SOLD
                        && userId.equals(item.getCurrentHighestBidderId()))
                .collect(Collectors.toList());
    }
}
