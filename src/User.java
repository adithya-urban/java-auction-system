import java.util.UUID;

/**
 * Represents a registered user in the Online Auction System.
 * Each user has a unique ID, credentials, and a wallet balance
 * that can be used for placing bids and receiving payments.
 */
public class User {

    /** Unique identifier for this user (UUID). */
    private String userId;

    /** The user's chosen username. */
    private String username;

    /** The user's password. */
    private String password;

    /** The user's email address. */
    private String email;

    /** The user's wallet balance, defaults to 1000.0. */
    private double balance;

    /**
     * Constructs a new User with the given credentials and a default balance of 1000.0.
     *
     * @param username the username for this user
     * @param password the password for this user
     * @param email    the email address for this user
     */
    public User(String username, String password, String email) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.email = email;
        this.balance = 1000.0;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return the unique user ID */
    public String getUserId() {
        return userId;
    }

    /** @return the username */
    public String getUsername() {
        return username;
    }

    /** @return the password */
    public String getPassword() {
        return password;
    }

    /** @return the email address */
    public String getEmail() {
        return email;
    }

    /** @return the current wallet balance */
    public double getBalance() {
        return balance;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    /** @param userId the unique user ID to set */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @param username the username to set */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @param password the password to set */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @param email the email address to set */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @param balance the wallet balance to set */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    // -------------------------------------------------------------------------
    // Wallet Operations
    // -------------------------------------------------------------------------

    /**
     * Deposits the specified amount into the user's wallet.
     *
     * @param amount the amount to deposit (must be positive)
     */
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    /**
     * Withdraws the specified amount from the user's wallet.
     *
     * @param amount the amount to withdraw
     * @return {@code true} if the withdrawal was successful,
     *         {@code false} if the balance is insufficient
     */
    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this user, showing
     * userId, username, email, and balance.
     *
     * @return formatted string with user details
     */
    @Override
    public String toString() {
        return String.format("User[userId=%s, username=%s, email=%s, balance=%.2f]",
                userId, username, email, balance);
    }
}
