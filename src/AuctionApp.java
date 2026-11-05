import java.util.List;
import java.util.Scanner;

/**
 * AuctionApp - Main console-based UI for the Online Auction System.
 *
 * Provides a text menu for registering, logging in, browsing and
 * searching auctions, placing bids, creating auctions, viewing wallets,
 * notifications, and more.  Demo data (two users and two auctions) is
 * seeded automatically at startup.
 *
 * @author Adithya
 */
public class AuctionApp {

    // ------------------ Services ------------------
    private static final UserService userService = new UserService();
    private static final AuctionService auctionService = new AuctionService();
    private static final NotificationService notificationService = new NotificationService();

    /** The currently logged-in user, or null when logged out. */
    private static User currentUser = null;

    /** Shared Scanner for all console input. */
    private static final Scanner scanner = new Scanner(System.in);

    // ==============================================
    //  MAIN
    // ==============================================

    /**
     * Application entry point.  Seeds demo data, then enters the
     * welcome-screen loop.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        seedDemoData();

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = showWelcomeMenu();
            } else {
                showLoggedInMenu();
            }
        }

        System.out.println("\nThank you for using the Online Auction System. Goodbye!");
        scanner.close();
    }

    // ==============================================
    //  DEMO DATA
    // ==============================================

    /**
     * Seeds two demo users and two demo auction items so the
     * application is not empty on first launch.
     */
    private static void seedDemoData() {
        try {
            // --- Demo users ---
            User alice = userService.register("alice", "pass123", "alice@email.com");
            User bob = userService.register("bob", "pass456", "bob@email.com");

            // --- Demo auctions (use userId as sellerId) ---
            auctionService.createAuction(
                    alice.getUserId(),
                    "Vintage Watch",
                    "A beautiful vintage watch from the 1960s",
                    100.0,
                    30   // 30 minutes
            );

            auctionService.createAuction(
                    bob.getUserId(),
                    "Gaming Laptop",
                    "High-performance gaming laptop, RTX 4070",
                    500.0,
                    45   // 45 minutes
            );

            System.out.println("[System] Demo data loaded successfully.");
        } catch (Exception e) {
            System.out.println("[System] Warning - could not seed demo data: " + e.getMessage());
        }
    }

    // ==============================================
    //  WELCOME (NOT LOGGED IN)
    // ==============================================

    /**
     * Displays the welcome banner and pre-login menu.
     *
     * @return false if the user chose to exit, true otherwise
     */
    private static boolean showWelcomeMenu() {
        printSeparator();
        System.out.println("       === ONLINE AUCTION SYSTEM ===");
        printSeparator();
        System.out.println("  1. Register");
        System.out.println("  2. Login");
        System.out.println("  3. Exit");
        printSeparator();
        System.out.print("Choose an option: ");

        String choice = readLine();
        switch (choice) {
            case "1":
                handleRegister();
                break;
            case "2":
                handleLogin();
                break;
            case "3":
                return false; // signal to stop
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return true;
    }

    // ==============================================
    //  LOGGED-IN MENU
    // ==============================================

    /**
     * Displays the post-login menu and dispatches the chosen action.
     * Automatically closes any expired auctions before each display.
     */
    private static void showLoggedInMenu() {
        // Close expired auctions before every menu cycle
        auctionService.checkAndCloseExpiredAuctions(userService);

        int notifCount = notificationService.getNotificationCount(currentUser.getUserId());

        printSeparator();
        System.out.println("  Welcome, " + currentUser.getUsername()
                + "! [You have " + notifCount + " notification"
                + (notifCount == 1 ? "" : "s") + "]");
        printSeparator();
        System.out.println("  1.  Browse Active Auctions");
        System.out.println("  2.  Search Auctions");
        System.out.println("  3.  Create Auction (Sell an Item)");
        System.out.println("  4.  Place a Bid");
        System.out.println("  5.  View My Auctions (as seller)");
        System.out.println("  6.  View Won Auctions");
        System.out.println("  7.  View Bid History for an Item");
        System.out.println("  8.  View My Wallet");
        System.out.println("  9.  View Notifications");
        System.out.println("  10. Logout");
        printSeparator();
        System.out.print("Choose an option: ");

        String choice = readLine();
        switch (choice) {
            case "1":  handleBrowseAuctions();    break;
            case "2":  handleSearchAuctions();    break;
            case "3":  handleCreateAuction();     break;
            case "4":  handlePlaceBid();          break;
            case "5":  handleViewMyAuctions();    break;
            case "6":  handleViewWonAuctions();   break;
            case "7":  handleViewBidHistory();    break;
            case "8":  handleViewWallet();        break;
            case "9":  handleViewNotifications(); break;
            case "10": handleLogout();            break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    // ==============================================
    //  HANDLER - Register
    // ==============================================

    /** Prompts for username, password, email and registers a new user. */
    private static void handleRegister() {
        printSeparator();
        System.out.println("  >> REGISTER");
        printSeparator();

        System.out.print("Username : ");
        String username = readLine();
        System.out.print("Password : ");
        String password = readLine();
        System.out.print("Email    : ");
        String email = readLine();

        try {
            userService.register(username, password, email);
            System.out.println("\n[OK] Registration successful! You can now log in.");
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] Registration failed: " + e.getMessage());
        }
    }

    // ==============================================
    //  HANDLER - Login
    // ==============================================

    /** Prompts for credentials and attempts to log in. */
    private static void handleLogin() {
        printSeparator();
        System.out.println("  >> LOGIN");
        printSeparator();

        System.out.print("Username : ");
        String username = readLine();
        System.out.print("Password : ");
        String password = readLine();

        User user = userService.login(username, password);
        if (user != null) {
            currentUser = user;
            System.out.println("\n[OK] Login successful! Welcome, " + currentUser.getUsername() + ".");
        } else {
            System.out.println("\n[ERROR] Login failed: Invalid username or password.");
        }
    }

    // ==============================================
    //  HANDLER - Browse Active Auctions
    // ==============================================

    /** Lists all currently active (non-ended) auctions in a formatted table. */
    private static void handleBrowseAuctions() {
        printSeparator();
        System.out.println("  >> ACTIVE AUCTIONS");
        printSeparator();

        List<AuctionItem> active = auctionService.getActiveAuctions();
        if (active.isEmpty()) {
            System.out.println("  No active auctions.");
            return;
        }
        printAuctionTable(active);
    }

    // ==============================================
    //  HANDLER - Search Auctions
    // ==============================================

    /** Prompts for a keyword and displays matching auctions. */
    private static void handleSearchAuctions() {
        printSeparator();
        System.out.println("  >> SEARCH AUCTIONS");
        printSeparator();

        System.out.print("Enter search keyword: ");
        String keyword = readLine();

        List<AuctionItem> results = auctionService.searchAuctions(keyword);
        if (results.isEmpty()) {
            System.out.println("  No auctions found matching \"" + keyword + "\".");
            return;
        }
        printAuctionTable(results);
    }

    // ==============================================
    //  HANDLER - Create Auction
    // ==============================================

    /** Walks the user through creating a new auction listing. */
    private static void handleCreateAuction() {
        printSeparator();
        System.out.println("  >> CREATE AUCTION");
        printSeparator();

        System.out.print("Item title       : ");
        String title = readLine();
        System.out.print("Description      : ");
        String description = readLine();

        double startingPrice;
        try {
            System.out.print("Starting price ($): ");
            startingPrice = Double.parseDouble(readLine());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid price. Auction not created.");
            return;
        }

        int durationMinutes;
        try {
            System.out.print("Duration (minutes): ");
            durationMinutes = Integer.parseInt(readLine());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid duration. Auction not created.");
            return;
        }

        AuctionItem item = auctionService.createAuction(
                currentUser.getUserId(), title, description, startingPrice, durationMinutes);
        System.out.println("\n[OK] Auction created successfully!");
        System.out.println("  Item ID : " + item.getItemId());
        System.out.println("  Title   : " + item.getTitle());
    }

    // ==============================================
    //  HANDLER - Place a Bid
    // ==============================================

    /**
     * Shows active auctions, lets the user pick one by index,
     * then prompts for a bid amount.
     */
    private static void handlePlaceBid() {
        printSeparator();
        System.out.println("  >> PLACE A BID");
        printSeparator();

        List<AuctionItem> active = auctionService.getActiveAuctions();
        if (active.isEmpty()) {
            System.out.println("  No active auctions to bid on.");
            return;
        }
        printAuctionTable(active);

        int index;
        try {
            System.out.print("\nEnter item number (#): ");
            index = Integer.parseInt(readLine()) - 1; // convert to 0-based
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid number.");
            return;
        }

        if (index < 0 || index >= active.size()) {
            System.out.println("[ERROR] Item number out of range.");
            return;
        }

        AuctionItem chosen = active.get(index);

        double bidAmount;
        try {
            System.out.print("Enter your bid amount ($): ");
            bidAmount = Double.parseDouble(readLine());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid bid amount.");
            return;
        }

        boolean success = auctionService.placeBid(
                chosen.getItemId(), currentUser.getUserId(), bidAmount, userService);
        if (success) {
            System.out.println("\n[OK] Bid of $" + String.format("%.2f", bidAmount)
                    + " placed successfully on \"" + chosen.getTitle() + "\"!");
        }
        // Error messages are printed inside placeBid()
    }

    // ==============================================
    //  HANDLER - View My Auctions (as seller)
    // ==============================================

    /** Lists all auctions created by the current user. */
    private static void handleViewMyAuctions() {
        printSeparator();
        System.out.println("  >> MY AUCTIONS (Seller)");
        printSeparator();

        List<AuctionItem> mine = auctionService.getAuctionsBySeller(currentUser.getUserId());
        if (mine.isEmpty()) {
            System.out.println("  You have not created any auctions.");
            return;
        }

        System.out.printf("  %-4s %-22s %-12s %-14s %-10s%n",
                "#", "Title", "Start Price", "Current Bid", "Status");
        System.out.println("  " + repeat('-', 66));

        int i = 1;
        for (AuctionItem item : mine) {
            String currentBid = (item.getCurrentHighestBidderId() != null)
                    ? String.format("$%.2f", item.getCurrentHighestBid())
                    : "No bids";
            String status = (item.getStatus() == AuctionStatus.ACTIVE)
                    ? "ACTIVE (" + formatTimeRemaining(item.getRemainingSeconds()) + ")"
                    : item.getStatus().getLabel();

            System.out.printf("  %-4d %-22s $%-11.2f %-14s %-10s%n",
                    i++,
                    truncate(item.getTitle(), 20),
                    item.getStartingPrice(),
                    currentBid,
                    status);
        }
    }

    // ==============================================
    //  HANDLER - View Won Auctions
    // ==============================================

    /** Lists auctions the current user has won. */
    private static void handleViewWonAuctions() {
        printSeparator();
        System.out.println("  >> WON AUCTIONS");
        printSeparator();

        List<AuctionItem> won = auctionService.getWonAuctions(currentUser.getUserId());
        if (won.isEmpty()) {
            System.out.println("  You have not won any auctions yet.");
            return;
        }

        System.out.printf("  %-4s %-24s %-14s %-16s%n",
                "#", "Title", "Winning Bid", "Seller ID");
        System.out.println("  " + repeat('-', 62));

        int i = 1;
        for (AuctionItem item : won) {
            // Look up seller username
            User seller = userService.getUserById(item.getSellerId());
            String sellerName = (seller != null) ? seller.getUsername() : item.getSellerId();

            System.out.printf("  %-4d %-24s $%-13.2f %-16s%n",
                    i++,
                    truncate(item.getTitle(), 22),
                    item.getCurrentHighestBid(),
                    sellerName);
        }
    }

    // ==============================================
    //  HANDLER - View Bid History
    // ==============================================

    /**
     * Shows all auctions, lets the user pick one, then displays
     * the full bid history sorted by time.
     */
    private static void handleViewBidHistory() {
        printSeparator();
        System.out.println("  >> BID HISTORY");
        printSeparator();

        List<AuctionItem> all = auctionService.getAllAuctions();

        if (all.isEmpty()) {
            System.out.println("  No auctions available.");
            return;
        }

        // List auctions for selection
        System.out.printf("  %-4s %-30s %-10s%n", "#", "Title", "Status");
        System.out.println("  " + repeat('-', 48));
        int i = 1;
        for (AuctionItem item : all) {
            System.out.printf("  %-4d %-30s %-10s%n",
                    i++,
                    truncate(item.getTitle(), 28),
                    item.getStatus().getLabel());
        }

        int index;
        try {
            System.out.print("\nSelect auction (#): ");
            index = Integer.parseInt(readLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid number.");
            return;
        }

        if (index < 0 || index >= all.size()) {
            System.out.println("[ERROR] Number out of range.");
            return;
        }

        AuctionItem chosen = all.get(index);
        List<Bid> bids = auctionService.getBidsForItem(chosen.getItemId());

        System.out.println();
        printSeparator();
        System.out.println("  Bid History for: " + chosen.getTitle());
        printSeparator();

        if (bids.isEmpty()) {
            System.out.println("  No bids have been placed on this item.");
            return;
        }

        System.out.printf("  %-4s %-16s %-14s %-22s%n", "#", "Bidder", "Amount", "Time");
        System.out.println("  " + repeat('-', 58));

        int j = 1;
        for (Bid bid : bids) {
            // Look up bidder username
            User bidder = userService.getUserById(bid.getBidderId());
            String bidderName = (bidder != null) ? bidder.getUsername() : bid.getBidderId();

            System.out.printf("  %-4d %-16s $%-13.2f %-22s%n",
                    j++,
                    bidderName,
                    bid.getAmount(),
                    bid.getTimestamp().toString());
        }
    }

    // ==============================================
    //  HANDLER - View Wallet
    // ==============================================

    /** Shows the wallet balance and offers a deposit option. */
    private static void handleViewWallet() {
        printSeparator();
        System.out.println("  >> MY WALLET");
        printSeparator();

        System.out.printf("  Balance: $%.2f%n", currentUser.getBalance());

        System.out.print("\n  Would you like to deposit funds? (y/n): ");
        String answer = readLine().trim().toLowerCase();

        if ("y".equals(answer) || "yes".equals(answer)) {
            try {
                System.out.print("  Deposit amount ($): ");
                double amount = Double.parseDouble(readLine());
                if (amount <= 0) {
                    System.out.println("  [ERROR] Amount must be positive.");
                    return;
                }
                currentUser.deposit(amount);
                System.out.printf("\n  [OK] Deposited $%.2f. New balance: $%.2f%n",
                        amount, currentUser.getBalance());
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid amount.");
            }
        }
    }

    // ==============================================
    //  HANDLER - View Notifications
    // ==============================================

    /** Displays all notifications for the current user (and clears them). */
    private static void handleViewNotifications() {
        printSeparator();
        System.out.println("  >> NOTIFICATIONS");
        printSeparator();

        List<String> notifications =
                notificationService.getNotifications(currentUser.getUserId());

        if (notifications.isEmpty()) {
            System.out.println("  No notifications.");
            return;
        }

        int i = 1;
        for (String msg : notifications) {
            System.out.printf("  %d. %s%n", i++, msg);
        }
    }

    // ==============================================
    //  HANDLER - Logout
    // ==============================================

    /** Logs the current user out and returns to the welcome screen. */
    private static void handleLogout() {
        System.out.println("\n  Goodbye, " + currentUser.getUsername() + "!");
        currentUser = null;
    }

    // ==============================================
    //  FORMATTING HELPERS
    // ==============================================

    /**
     * Prints a decorative separator line to the console.
     */
    private static void printSeparator() {
        System.out.println("========================================================");
    }

    /**
     * Formats a remaining-seconds value as a human-readable duration.
     *
     * @param seconds remaining seconds (negative or zero means ended)
     * @return a string like "1h 23m 45s", or "ENDED" if time has expired
     */
    private static String formatTimeRemaining(long seconds) {
        if (seconds <= 0) {
            return "ENDED";
        }
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%dh %dm %ds", h, m, s);
    }

    /**
     * Prints a nicely formatted table of auction items.
     *
     * @param items the list of auction items to display
     */
    private static void printAuctionTable(List<AuctionItem> items) {
        System.out.printf("  %-4s %-22s %-12s %-14s %-16s %-10s%n",
                "#", "Title", "Start Price", "Current Bid", "Time Remaining", "Seller");
        System.out.println("  " + repeat('-', 80));

        int i = 1;
        for (AuctionItem item : items) {
            String currentBid = (item.getCurrentHighestBidderId() != null)
                    ? String.format("$%.2f", item.getCurrentHighestBid())
                    : "No bids";
            String timeLeft = formatTimeRemaining(item.getRemainingSeconds());

            // Look up seller username from userId
            User seller = userService.getUserById(item.getSellerId());
            String sellerName = (seller != null) ? seller.getUsername() : item.getSellerId();

            System.out.printf("  %-4d %-22s $%-11.2f %-14s %-16s %-10s%n",
                    i++,
                    truncate(item.getTitle(), 20),
                    item.getStartingPrice(),
                    currentBid,
                    timeLeft,
                    sellerName);
        }
    }

    /**
     * Truncates a string to a maximum length, appending "..." if shortened.
     *
     * @param text      the original text
     * @param maxLength maximum allowed length
     * @return the (possibly truncated) text
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Creates a string by repeating a character n times.
     * (Java 8 compatible alternative to String.repeat())
     *
     * @param ch    the character to repeat
     * @param count number of repetitions
     * @return the repeated string
     */
    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Reads a line of input from the console, trimming whitespace.
     *
     * @return the trimmed user input
     */
    private static String readLine() {
        return scanner.nextLine().trim();
    }
}
