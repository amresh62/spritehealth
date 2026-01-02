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

public class LogoutServlet extends HttpServlet {
    private final SessionManager sessionManager = new SessionManager();
    private final Gson gson = GsonProvider.getGson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        // Get session ID from cookie
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
        
        // Delete session from Datastore
        if (sessionId != null) {
            sessionManager.deleteSession(sessionId);
        }
        
        // Clear cookie
        Cookie sessionCookie = new Cookie("USER_SESSION_ID", "");
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        response.addCookie(sessionCookie);
        
        result.put("success", true);
        result.put("message", "Logged out successfully");
        
        response.getWriter().write(gson.toJson(result));
    }
}
