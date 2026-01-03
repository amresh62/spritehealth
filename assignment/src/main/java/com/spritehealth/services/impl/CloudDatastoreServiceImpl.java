package com.spritehealth.services.impl;

import com.google.cloud.datastore.*;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IUserDatastoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Cloud Datastore implementation for user storage.
 * Provides CRUD operations for User entities using Google Cloud Datastore.
 */
public class CloudDatastoreServiceImpl implements IUserDatastoreService {
    private static final String KIND = "User"; // Datastore kind for User entities
    private final Datastore datastore;         // Datastore client instance

    /**
     * Constructor initializes the Datastore client.
     * Uses emulator if DATASTORE_EMULATOR_HOST is set, otherwise connects to Cloud Datastore.
     */
    public CloudDatastoreServiceImpl() {
        // Get project ID from environment variable or fallback to default
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId == null || projectId.isEmpty()) {
            projectId = "api-7355075667102536099-806743"; // Fallback project ID
        }

        DatastoreOptions.Builder builder = DatastoreOptions.newBuilder().setProjectId(projectId);

        // Use emulator if configured
        String emulatorHost = System.getenv("DATASTORE_EMULATOR_HOST");
        if (emulatorHost != null && !emulatorHost.isEmpty()) {
            builder.setHost(emulatorHost);
            System.out.println("Using Datastore Emulator at: " + emulatorHost);
        } else {
            // Use production Cloud Datastore
            System.out.println("Using Cloud Datastore for project: " + projectId);
        }

        this.datastore = builder.build().getService();
    }

    /**
     * Creates a new user in Datastore.
     * @param user User object to create
     * @return Created User with assigned ID
     */
    @Override
    public User createUser(User user) {
        KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND);
        IncompleteKey incompleteKey = keyFactory.newKey(); // Create incomplete key for auto-ID
        Key key = datastore.allocateId(incompleteKey);      // Allocate unique ID

        Entity entity = user.toEntityBuilder(key).build();  // Convert User to Entity
        Entity savedEntity = datastore.put(entity);         // Save entity to Datastore

        return User.fromEntity(savedEntity);                // Convert back to User
    }

    /**
     * Creates multiple users in Datastore.
     * @param users List of User objects to create
     * @return List of created Users with assigned IDs
     */
    @Override
    public List<User> createUsers(List<User> users) {
        List<Entity> entities = new ArrayList<>();
        KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND);

        // Convert each User to Entity with allocated ID
        for (User user : users) {
            IncompleteKey incompleteKey = keyFactory.newKey();
            Key key = datastore.allocateId(incompleteKey);
            Entity entity = user.toEntityBuilder(key).build();
            entities.add(entity);
        }

        // Save all entities in batch
        List<Entity> savedEntities = datastore.put(entities.toArray(new Entity[0]));

        // Convert saved entities back to User objects
        return savedEntities.stream()
                .map(User::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a user by their ID.
     * @param id User ID
     * @return User object if found, otherwise null
     */
    @Override
    public User getUserById(Long id) {
        Key key = datastore.newKeyFactory().setKind(KIND).newKey(id);
        Entity entity = datastore.get(key);

        if (entity == null) {
            return null;
        }

        return User.fromEntity(entity);
    }

    /**
     * Retrieves a user by their email address.
     * @param email User email
     * @return User object if found, otherwise null
     */
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

    /**
     * Retrieves all users from Datastore.
     * @return List of all User objects
     */
    @Override
    public List<User> getAllUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .build();

        QueryResults<Entity> results = datastore.run(query);

        // Convert all entities to User objects
        return StreamSupport.stream(
                ((Iterable<Entity>) () -> results).spliterator(), false)
                .map(User::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Queries users by name (case-insensitive, in-memory filter).
     * @param name Name to search for
     * @return List of matching User objects
     */
    @Override
    public List<User> queryUsersByName(String name) {
        // Note: Datastore doesn't support case-insensitive queries efficiently
        // For production, consider using full-text search or filtering in code
        List<User> allUsers = getAllUsers();
        return allUsers.stream()
                .filter(user -> user.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing user in Datastore.
     * @param user User object with updated data
     * @return Updated User object, or null if not found
     */
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

    /**
     * Deletes a user by their ID.
     * @param id User ID as String
     * @return true if deleted, false if not found or invalid ID
     */
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

    /**
     * Authenticates a user by email and password.
     * @param email User email
     * @param password User password
     * @return User object if authentication succeeds, otherwise null
     */
    @Override
    public User authenticateUser(String email, String password) {
        User user = getUserByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }
}
