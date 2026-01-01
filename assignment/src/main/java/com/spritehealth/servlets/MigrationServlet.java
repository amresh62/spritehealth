package com.spritehealth.servlets;

import com.google.gson.Gson;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IUserDatastoreService;
import com.spritehealth.services.interfaces.IBigQueryService;
import com.spritehealth.services.impl.InMemoryDatastoreServiceImpl;
import com.spritehealth.services.impl.BigQueryServiceImpl;
import com.spritehealth.utils.GsonProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationServlet extends HttpServlet {
    private final IUserDatastoreService datastoreService = new InMemoryDatastoreServiceImpl();
    private final IBigQueryService bigQueryService = new BigQueryServiceImpl();
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
            
            // Get all users from Datastore for migration preview
            List<User> users = datastoreService.getAllUsers();
            
            result.put("success", true);
            result.put("users", users);
            result.put("count", users.size());
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
            
            if (users.isEmpty()) {
                result.put("success", false);
                result.put("message", "No users found in Datastore to migrate");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            // Migrate to BigQuery
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

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }
}
