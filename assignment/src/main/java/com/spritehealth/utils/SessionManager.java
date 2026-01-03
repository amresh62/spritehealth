package com.spritehealth.utils;

import com.google.cloud.datastore.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SessionManager handles creation, retrieval, and deletion of user sessions
 * using Google Cloud Datastore as the backend.
 */
public class SessionManager {
    // Datastore kind for storing sessions
    private static final String SESSION_KIND = "UserSession";
    // Session timeout duration (30 minutes in milliseconds)
    private static final int SESSION_TIMEOUT = 30 * 60 * 1000;
    private final Datastore datastore;

    /**
     * Initializes the Datastore client using the project ID from environment variable,
     * or a default project ID if not set.
     */
    public SessionManager() {
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId == null || projectId.isEmpty()) {
            projectId = "api-7355075667102536099-806743";
        }
        this.datastore = DatastoreOptions.newBuilder().setProjectId(projectId).build().getService();
    }

    /**
     * Creates a new session for a user and stores it in Datastore.
     *
     * @param userId    The user's ID.
     * @param userEmail The user's email.
     * @param userName  The user's name.
     * @return The generated session ID.
     */
    public String createSession(Long userId, String userEmail, String userName) {
        String sessionId = UUID.randomUUID().toString(); // Generate unique session ID
        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + SESSION_TIMEOUT;

        // Create a Datastore key for the session
        Key key = datastore.newKeyFactory().setKind(SESSION_KIND).newKey(sessionId);
        // Build the session entity
        Entity session = Entity.newBuilder(key)
                .set("userId", userId)
                .set("userEmail", userEmail)
                .set("userName", userName)
                .set("createdAt", currentTime)
                .set("expiryTime", expiryTime)
                .build();

        // Store the session in Datastore
        datastore.put(session);
        System.out.println("Session stored in Datastore: " + sessionId);
        return sessionId;
    }

    /**
     * Retrieves a session from Datastore by session ID.
     * If the session is expired or not found, returns null.
     *
     * @param sessionId The session ID.
     * @return A map containing session data, or null if not found/expired.
     */
    public Map<String, Object> getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        try {
            // Build the key and fetch the session entity
            Key key = datastore.newKeyFactory().setKind(SESSION_KIND).newKey(sessionId);
            Entity session = datastore.get(key);

            if (session == null) {
                System.out.println("Session not found: " + sessionId);
                return null;
            }

            long expiryTime = session.getLong("expiryTime");
            // Check if the session has expired
            if (System.currentTimeMillis() > expiryTime) {
                System.out.println("Session expired: " + sessionId);
                deleteSession(sessionId);
                return null;
            }

            // Populate session data into a map
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", session.getLong("userId"));
            sessionData.put("userEmail", session.getString("userEmail"));
            sessionData.put("userName", session.getString("userName"));

            System.out.println("Session retrieved: " + sessionId);
            return sessionData;
        } catch (Exception e) {
            System.err.println("Error retrieving session: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a session from Datastore by session ID.
     *
     * @param sessionId The session ID to delete.
     */
    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        try {
            // Build the key and delete the session entity
            Key key = datastore.newKeyFactory().setKind(SESSION_KIND).newKey(sessionId);
            datastore.delete(key);
            System.out.println("Session deleted: " + sessionId);
        } catch (Exception e) {
            System.err.println("Error deleting session: " + e.getMessage());
        }
    }
}
