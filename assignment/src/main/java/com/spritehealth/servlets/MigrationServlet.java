package com.spritehealth.servlets;

import com.google.gson.Gson;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IUserDatastoreService;
import com.spritehealth.services.interfaces.IBigQueryService;
import com.spritehealth.services.impl.BigQueryServiceImpl;
import com.spritehealth.services.impl.CloudDatastoreServiceImpl;
import com.spritehealth.utils.GsonProvider;
import com.spritehealth.utils.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet for handling migration of users from Datastore to BigQuery.
 * Provides endpoints for previewing and executing the migration.
 */
public class MigrationServlet extends HttpServlet {
    // Service for interacting with Datastore
    private final IUserDatastoreService datastoreService = new CloudDatastoreServiceImpl();
    // Service for interacting with BigQuery
    private final IBigQueryService bigQueryService = new BigQueryServiceImpl();
    // Session manager for authentication
    private final SessionManager sessionManager = new SessionManager();
    // Gson instance for JSON serialization
    private final Gson gson = GsonProvider.getGson();

    /**
     * Handles GET requests to preview migration data.
     * Returns all users from Datastore and BigQuery table status.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if user is authenticated
            if (!isAuthenticated(request)) {
                result.put("success", false);
                result.put("message", "Unauthorized");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            // Get all users from Datastore for migration preview
            List<User> users = datastoreService.getAllUsers();
            
            result.put("success", true);
            result.put("users", users);
            result.put("count", users.size());
            // Check if the BigQuery table exists
            result.put("bigQueryTableExists", bigQueryService.tableExists());
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error retrieving migration data: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

    /**
     * Handles POST requests to execute the migration.
     * Migrates all users from Datastore to BigQuery.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if user is authenticated
            if (!isAuthenticated(request)) {
                result.put("success", false);
                result.put("message", "Unauthorized");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            // Get all users from Datastore
            List<User> users = datastoreService.getAllUsers();
            
            // If no users found, return error
            if (users.isEmpty()) {
                result.put("success", false);
                result.put("message", "No users found in Datastore to migrate");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            // Migrate users to BigQuery and get migration result
            Map<String, Object> migrationResult = bigQueryService.migrateUsers(users);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(migrationResult));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error during migration: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

    /**
     * Checks if the user is authenticated by verifying the session cookie.
     * @param request HttpServletRequest object
     * @return true if authenticated, false otherwise
     */
    private boolean isAuthenticated(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("USER_SESSION_ID".equals(cookie.getName())) {
                    // Retrieve session data using the session manager
                    Map<String, Object> sessionData = sessionManager.getSession(cookie.getValue());
                    return sessionData != null;
                }
            }
        }
        return false;
    }
}
