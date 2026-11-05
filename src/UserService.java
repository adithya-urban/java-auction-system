import java.util.*;

/**
 * Service class that manages user registration, authentication, and balance operations.
 * Uses in-memory storage with HashMaps for fast lookups by both user ID and username.
 */
public class UserService {

    /** Map of userId -> User for direct ID lookups. */
    private Map<String, User> users;

    /** Map of username -> userId for username-based lookups. */
    private Map<String, String> usernameToId;

    /**
     * Constructs a new UserService with empty user stores.
     */
    public UserService() {
        this.users = new HashMap<>();
        this.usernameToId = new HashMap<>();
    }

    /**
     * Registers a new user with the given credentials.
     * Validates that the username is unique before creating the account.
     *
     * @param username the desired username (must be unique)
     * @param password the user's password
     * @param email    the user's email address
     * @return the newly created User object
     * @throws IllegalArgumentException if the username is already taken
     */
    public User register(String username, String password, String email) {
        if (usernameToId.containsKey(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        User user = new User(username, password, email);
        users.put(user.getUserId(), user);
        usernameToId.put(username, user.getUserId());
        return user;
    }

    /**
     * Authenticates a user by matching username and password.
     *
     * @param username the username to authenticate
     * @param password the password to verify
     * @return the User object if credentials are valid, null otherwise
     */
    public User login(String username, String password) {
        String userId = usernameToId.get(username);
        if (userId == null) {
            return null;
        }

        User user = users.get(userId);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the user's unique identifier
     * @return the User object, or null if not found
     */
    public User getUserById(String userId) {
        return users.get(userId);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to look up
     * @return the User object, or null if not found
     */
    public User getUserByUsername(String username) {
        String userId = usernameToId.get(username);
        if (userId == null) {
            return null;
        }
        return users.get(userId);
    }

    /**
     * Deposits money into a user's account balance.
     *
     * @param userId the ID of the user to credit
     * @param amount the amount to deposit (must be positive)
     * @throws IllegalArgumentException if the amount is not positive
     */
    public void deposit(String userId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        user.setBalance(user.getBalance() + amount);
    }

    /**
     * Returns a list of all registered users.
     *
     * @return an unmodifiable snapshot of all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
