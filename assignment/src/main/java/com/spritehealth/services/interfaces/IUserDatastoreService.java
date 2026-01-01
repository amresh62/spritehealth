package com.spritehealth.services.interfaces;

import com.spritehealth.models.User;
import java.util.List;

/**
 * Interface for user datastore operations
 */
public interface IUserDatastoreService {
    
    /**
     * Create a new user
     * @param user User to create
     * @return Created user with assigned ID
     */
    User createUser(User user);
    
    /**
     * Create multiple users in batch
     * @param users List of users to create
     * @return List of created users with assigned IDs
     */
    List<User> createUsers(List<User> users);
    
    /**
     * Get user by ID
     * @param id User ID
     * @return User if found, null otherwise
     */
    User getUserById(Long id);
    
    /**
     * Get user by email
     * @param email User email
     * @return User if found, null otherwise
     */
    User getUserByEmail(String email);
    
    /**
     * Get all users
     * @return List of all users
     */
    List<User> getAllUsers();
    
    /**
     * Query users by name
     * @param name Name to search for
     * @return List of matching users
     */
    List<User> queryUsersByName(String name);
    
    /**
     * Update user
     * @param user User to update
     * @return Updated user
     */
    User updateUser(User user);
    
    /**
     * Delete user by ID
     * @param id User ID as string
     * @return true if deleted, false otherwise
     */
    boolean deleteUser(String id);
    
    /**
     * Authenticate user with email and password
     * @param email User email
     * @param password User password
     * @return User if authenticated, null otherwise
     */
    User authenticateUser(String email, String password);
}
