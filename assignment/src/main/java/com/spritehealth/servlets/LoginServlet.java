package com.spritehealth.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IUserDatastoreService;
import com.spritehealth.services.impl.CloudDatastoreServiceImpl;
import com.spritehealth.utils.GsonProvider;
import com.spritehealth.utils.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet for handling user login and authentication.
 * Supports POST for login and GET for session authentication check.
 */
public class LoginServlet extends HttpServlet {
    // Service for user authentication and datastore operations
    private final IUserDatastoreService datastoreService = new CloudDatastoreServiceImpl();
    // Session manager for handling user sessions
    private final SessionManager sessionManager = new SessionManager();
    // Gson instance for JSON parsing and serialization
    private final Gson gson = GsonProvider.getGson();

    /**
     * Handles POST requests for user login.
     * Expects JSON body with "email" and "password".
     * On successful authentication, creates a session and sets a cookie.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Read JSON body from request
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            
            // Extract email and password from JSON
            String email = jsonObject.get("email").getAsString();
            String password = jsonObject.get("password").getAsString();
            
            // Authenticate user using datastore service
            User user = datastoreService.authenticateUser(email, password);
            
            if (user != null) {
                // Create a new session for the authenticated user
                String sessionId = sessionManager.createSession(user.getId(), user.getEmail(), user.getName());
                
                // Set session ID in a cookie
                Cookie sessionCookie = new Cookie("USER_SESSION_ID", sessionId);
                sessionCookie.setPath("/");
                sessionCookie.setMaxAge(30 * 60); // 30 minutes
                sessionCookie.setHttpOnly(false); // Consider setting to true for security
                response.addCookie(sessionCookie);
                
                System.out.println("Login successful - Session created: " + sessionId);
                System.out.println("Login successful - User ID: " + user.getId());
                
                // Prepare user data to return (excluding password)
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("name", user.getName());
                userData.put("email", user.getEmail());
                userData.put("phone", user.getPhone());
                userData.put("gender", user.getGender());
                userData.put("address", user.getAddress());
                userData.put("dateOfBirth", user.getDateOfBirth() != null 
                        ? user.getDateOfBirth().toString() : null);
                
                result.put("success", true);
                result.put("message", "Login successful");
                result.put("user", userData);
                
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // Authentication failed
                result.put("success", false);
                result.put("message", "Invalid email or password");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            
            // Write JSON response
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            // Handle exceptions and return error response
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error during login: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

    /**
     * Handles GET requests to check if the user is authenticated.
     * Looks for session ID in cookies and validates the session.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
        Map<String, Object> result = new HashMap<>();
        
        // Retrieve session ID from cookies
        String sessionId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("USER_SESSION_ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        
        System.out.println("Auth check - Session ID from cookie: " + sessionId);
        
        if (sessionId != null) {
            // Validate session using session manager
            Map<String, Object> sessionData = sessionManager.getSession(sessionId);
            if (sessionData != null) {
                // Session is valid, user is authenticated
                result.put("authenticated", true);
                result.put("user", sessionData);
                System.out.println("Auth check - User authenticated: " + sessionData.get("userEmail"));
            } else {
                // Session is invalid or expired
                result.put("authenticated", false);
                System.out.println("Auth check - Session expired or invalid");
            }
        } else {
            // No session cookie found
            result.put("authenticated", false);
            System.out.println("Auth check - No session cookie found");
        }
        
        // Write JSON response
        response.getWriter().write(gson.toJson(result));
    }
}
