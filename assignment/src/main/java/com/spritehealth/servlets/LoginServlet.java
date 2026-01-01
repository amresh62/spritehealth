package com.spritehealth.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IUserDatastoreService;
import com.spritehealth.services.impl.InMemoryDatastoreServiceImpl;
import com.spritehealth.utils.GsonProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginServlet extends HttpServlet {
    private final IUserDatastoreService datastoreService = new InMemoryDatastoreServiceImpl();
    private final Gson gson = GsonProvider.getGson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Read JSON body
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            
            String email = jsonObject.get("email").getAsString();
            String password = jsonObject.get("password").getAsString();
            
            // Authenticate user
            User user = datastoreService.authenticateUser(email, password);
            
            if (user != null) {
                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", user.getId());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userName", user.getName());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
                
                // Prepare user data without password
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
                result.put("success", false);
                result.put("message", "Invalid email or password");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error during login: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("userId") != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", session.getAttribute("userId"));
            userData.put("email", session.getAttribute("userEmail"));
            userData.put("name", session.getAttribute("userName"));
            
            result.put("authenticated", true);
            result.put("user", userData);
        } else {
            result.put("authenticated", false);
        }
        
        response.getWriter().write(gson.toJson(result));
    }
}
