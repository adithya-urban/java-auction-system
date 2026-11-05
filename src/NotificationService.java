import java.util.*;

/**
 * Simple in-memory notification service for the auction system.
 * Stores notification messages per user and supports retrieval with auto-clearing.
 */
public class NotificationService {

    /** Map of userId -> list of pending notification message strings. */
    private Map<String, List<String>> notifications;

    /**
     * Constructs a new NotificationService with an empty notification store.
     */
    public NotificationService() {
        this.notifications = new HashMap<>();
    }

    /**
     * Adds a notification message for a specific user.
     *
     * @param userId  the ID of the user to notify
     * @param message the notification message text
     */
    public void addNotification(String userId, String message) {
        notifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    /**
     * Retrieves and clears all pending notifications for a specific user.
     * After calling this method, the user's notification list will be empty.
     *
     * @param userId the ID of the user
     * @return list of notification messages, or an empty list if none pending
     */
    public List<String> getNotifications(String userId) {
        List<String> userNotifications = notifications.getOrDefault(userId, new ArrayList<>());
        // Create a copy to return before clearing
        List<String> result = new ArrayList<>(userNotifications);
        // Clear the notifications after retrieval
        notifications.remove(userId);
        return result;
    }

    /**
     * Checks whether a user has any pending notifications.
     *
     * @param userId the ID of the user to check
     * @return true if there are pending notifications, false otherwise
     */
    public boolean hasNotifications(String userId) {
        List<String> userNotifications = notifications.get(userId);
        return userNotifications != null && !userNotifications.isEmpty();
    }

    /**
     * Returns the count of pending notifications for a specific user.
     *
     * @param userId the ID of the user
     * @return the number of pending notifications
     */
    public int getNotificationCount(String userId) {
        List<String> userNotifications = notifications.get(userId);
        return (userNotifications != null) ? userNotifications.size() : 0;
    }
}
