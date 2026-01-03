package com.spritehealth.servlets;

import com.google.gson.Gson;
import com.spritehealth.utils.GsonProvider;
import com.spritehealth.utils.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet to handle user logout functionality.
 */
public class LogoutServlet extends HttpServlet {
    // SessionManager instance to manage user sessions
    private final SessionManager sessionManager = new SessionManager();
    // Gson instance for JSON serialization
    private final Gson gson = GsonProvider.getGson();

    /**
     * Handles POST requests for logging out the user.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Set response content type and encoding
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Prepare result map to hold response data
        Map<String, Object> result = new HashMap<>();
        
        // Retrieve session ID from cookies
        String sessionId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Look for the session cookie by name
                if ("USER_SESSION_ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        
        // Delete the session from the datastore if session ID is found
        if (sessionId != null) {
            sessionManager.deleteSession(sessionId);
        }
        
        // Clear the session cookie on the client side
        Cookie sessionCookie = new Cookie("USER_SESSION_ID", "");
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0); // Invalidate the cookie immediately
        response.addCookie(sessionCookie);
        
        // Prepare success response
        result.put("success", true);
        result.put("message", "Logged out successfully");
        
        // Write JSON response
        response.getWriter().write(gson.toJson(result));
    }
}
