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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserServlet extends HttpServlet {
    private final IUserDatastoreService datastoreService = new InMemoryDatastoreServiceImpl();
    private final Gson gson = GsonProvider.getGson();

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
            
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && !pathInfo.equals("/")) {
                // Get specific user by ID
                String userId = pathInfo.substring(1);
                try {
                    Long id = Long.parseLong(userId);
                    User user = datastoreService.getUserById(id);
                    
                    if (user != null) {
                        result.put("success", true);
                        result.put("user", sanitizeUser(user));
                    } else {
                        result.put("success", false);
                        result.put("message", "User not found");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } catch (NumberFormatException e) {
                    result.put("success", false);
                    result.put("message", "Invalid user ID");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                // Get all users
                List<User> users = datastoreService.getAllUsers();
                List<Map<String, Object>> sanitizedUsers = new ArrayList<>();
                
                for (User user : users) {
                    sanitizedUsers.add(sanitizeUser(user));
                }
                
                result.put("success", true);
                result.put("users", sanitizedUsers);
                result.put("count", users.size());
            }
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error retrieving users: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

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
            
            // Read JSON body
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            
            User user = new User();
            user.setName(jsonObject.get("name").getAsString());
            user.setEmail(jsonObject.get("email").getAsString());
            user.setPassword(jsonObject.get("password").getAsString());
            
            if (jsonObject.has("phone")) {
                user.setPhone(jsonObject.get("phone").getAsString());
            }
            if (jsonObject.has("gender")) {
                user.setGender(jsonObject.get("gender").getAsString());
            }
            if (jsonObject.has("address")) {
                user.setAddress(jsonObject.get("address").getAsString());
            }
            if (jsonObject.has("dateOfBirth")) {
                user.setDateOfBirth(LocalDate.parse(jsonObject.get("dateOfBirth").getAsString()));
            }
            
            User savedUser = datastoreService.createUser(user);
            
            result.put("success", true);
            result.put("message", "User created successfully");
            result.put("user", sanitizeUser(savedUser));
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error creating user: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
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
            
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                result.put("success", false);
                result.put("message", "User ID is required");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            String userId = pathInfo.substring(1);
            try {
                Long id = Long.parseLong(userId);
                boolean deleted = datastoreService.deleteUser(String.valueOf(id));
                
                if (deleted) {
                    result.put("success", true);
                    result.put("message", "User deleted successfully");
                } else {
                    result.put("success", false);
                    result.put("message", "User not found");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "Invalid user ID");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error deleting user: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }

    private Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("id", user.getId());
        sanitized.put("name", user.getName());
        sanitized.put("email", user.getEmail());
        sanitized.put("phone", user.getPhone());
        sanitized.put("gender", user.getGender());
        sanitized.put("address", user.getAddress());
        sanitized.put("dateOfBirth", user.getDateOfBirth() != null 
                ? user.getDateOfBirth().toString() : null);
        // Note: password is not included
        return sanitized;
    }
}
