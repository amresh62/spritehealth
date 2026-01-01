package com.spritehealth.services.interfaces;

import com.spritehealth.models.User;
import java.util.List;
import java.util.Map;

/**
 * Interface for BigQuery operations
 */
public interface IBigQueryService {
    
    /**
     * Create dataset if it doesn't exist
     */
    void createDatasetIfNotExists();
    
    /**
     * Create table if it doesn't exist
     */
    void createTableIfNotExists();
    
    /**
     * Check if table exists
     * @return true if table exists, false otherwise
     */
    boolean tableExists();
    
    /**
     * Migrate users from Datastore to BigQuery
     * @param users List of users to migrate
     * @return Map with migration results
     */
    Map<String, Object> migrateUsers(List<User> users);
    
    /**
     * Query users from BigQuery
     * @param limit Maximum number of users to retrieve
     * @return List of users
     */
    List<User> queryUsers(int limit);
}
