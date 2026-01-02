package com.spritehealth.services.impl;

import com.google.cloud.datastore.*;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IUserDatastoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Cloud Datastore implementation for user storage
 */
public class CloudDatastoreServiceImpl implements IUserDatastoreService {
    private static final String KIND = "User";
    private final Datastore datastore;

    public CloudDatastoreServiceImpl() {
        // Get project ID from environment
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId == null || projectId.isEmpty()) {
            projectId = "api-7355075667102536099-806743"; // Fallback to actual project ID
        }
        
        DatastoreOptions.Builder builder = DatastoreOptions.newBuilder().setProjectId(projectId);
        
        // Only use emulator if explicitly configured via environment variable
        String emulatorHost = System.getenv("DATASTORE_EMULATOR_HOST");
        if (emulatorHost != null && !emulatorHost.isEmpty()) {
            builder.setHost(emulatorHost);
            System.out.println("Using Datastore Emulator at: " + emulatorHost);
        } else {
            // Production mode - use Cloud Datastore
            System.out.println("Using Cloud Datastore for project: " + projectId);
        }
        
        this.datastore = builder.build().getService();
    }

    @Override
    public User createUser(User user) {
        KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND);
        IncompleteKey incompleteKey = keyFactory.newKey();
        Key key = datastore.allocateId(incompleteKey);
        
        Entity entity = user.toEntityBuilder(key).build();
        Entity savedEntity = datastore.put(entity);
        
        return User.fromEntity(savedEntity);
    }

    @Override
    public List<User> createUsers(List<User> users) {
        List<Entity> entities = new ArrayList<>();
        KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND);
        
        for (User user : users) {
            IncompleteKey incompleteKey = keyFactory.newKey();
            Key key = datastore.allocateId(incompleteKey);
            Entity entity = user.toEntityBuilder(key).build();
            entities.add(entity);
        }
        
        List<Entity> savedEntities = datastore.put(entities.toArray(new Entity[0]));
        
        return savedEntities.stream()
                .map(User::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long id) {
        Key key = datastore.newKeyFactory().setKind(KIND).newKey(id);
        Entity entity = datastore.get(key);
        
        if (entity == null) {
            return null;
        }
        
        return User.fromEntity(entity);
    }

    @Override
    public User getUserByEmail(String email) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq("email", email))
                .setLimit(1)
                .build();
        
        QueryResults<Entity> results = datastore.run(query);
        
        if (results.hasNext()) {
            return User.fromEntity(results.next());
        }
        
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .build();
        
        QueryResults<Entity> results = datastore.run(query);
        
        return StreamSupport.stream(
                ((Iterable<Entity>) () -> results).spliterator(), false)
                .map(User::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> queryUsersByName(String name) {
        // Note: Datastore doesn't support case-insensitive queries efficiently
        // For production, consider using full-text search or filtering in code
        List<User> allUsers = getAllUsers();
        return allUsers.stream()
                .filter(user -> user.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null) {
            return null;
        }
        
        Key key = datastore.newKeyFactory().setKind(KIND).newKey(user.getId());
        Entity existingEntity = datastore.get(key);
        
        if (existingEntity == null) {
            return null;
        }
        
        Entity entity = user.toEntityBuilder(key).build();
        datastore.update(entity);
        
        return User.fromEntity(entity);
    }

    @Override
    public boolean deleteUser(String id) {
        try {
            Long longId = Long.parseLong(id);
            Key key = datastore.newKeyFactory().setKind(KIND).newKey(longId);
            Entity entity = datastore.get(key);
            
            if (entity == null) {
                return false;
            }
            
            datastore.delete(key);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public User authenticateUser(String email, String password) {
        User user = getUserByEmail(email);
        
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        
        return null;
    }
}
