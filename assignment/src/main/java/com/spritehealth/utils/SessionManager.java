package com.spritehealth.utils;

import com.google.cloud.datastore.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static final String SESSION_KIND = "UserSession";
    private static final int SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds
    private final Datastore datastore;

    public SessionManager() {
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId == null || projectId.isEmpty()) {
            projectId = "api-7355075667102536099-806743";
        }
        this.datastore = DatastoreOptions.newBuilder().setProjectId(projectId).build().getService();
    }

    public String createSession(Long userId, String userEmail, String userName) {
        String sessionId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + SESSION_TIMEOUT;

        Key key = datastore.newKeyFactory().setKind(SESSION_KIND).newKey(sessionId);
        Entity session = Entity.newBuilder(key)
                .set("userId", userId)
                .set("userEmail", userEmail)
                .set("userName", userName)
                .set("createdAt", currentTime)
                .set("expiryTime", expiryTime)
                .build();

        datastore.put(session);
        System.out.println("Session stored in Datastore: " + sessionId);
        return sessionId;
    }

    public Map<String, Object> getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        try {
            Key key = datastore.newKeyFactory().setKind(SESSION_KIND).newKey(sessionId);
            Entity session = datastore.get(key);

            if (session == null) {
                System.out.println("Session not found: " + sessionId);
                return null;
            }

            long expiryTime = session.getLong("expiryTime");
            if (System.currentTimeMillis() > expiryTime) {
                System.out.println("Session expired: " + sessionId);
                deleteSession(sessionId);
                return null;
            }

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

    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        try {
            Key key = datastore.newKeyFactory().setKind(SESSION_KIND).newKey(sessionId);
            datastore.delete(key);
            System.out.println("Session deleted: " + sessionId);
        } catch (Exception e) {
            System.err.println("Error deleting session: " + e.getMessage());
        }
    }
}
