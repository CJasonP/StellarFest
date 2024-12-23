package database;

public class UserSession {
	private static String userId;  // Store the user ID

    // Method to set the user ID
    public static void setUserId(String id) {
        userId = id;
    }

    // Method to get the stored user ID
    public static String getUserId() {
        return userId;
    }

    // Optional: Method to clear the user ID (for logging out)
    public static void clearUserId() {
        userId = null;
    }
}
