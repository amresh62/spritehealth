package com.spritehealth.services.impl;

import com.google.cloud.bigquery.*;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IBigQueryService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BigQuery implementation for data analytics and migration.
 * Handles dataset/table creation, user migration, and querying.
 */
public class BigQueryServiceImpl implements IBigQueryService {
    // BigQuery client instance
    private final BigQuery bigQuery;
    // Google Cloud project ID
    private final String projectId;
    // BigQuery dataset name
    private final String datasetName;
    // BigQuery table name
    private final String tableName;

    /**
     * Default constructor.
     * Initializes BigQuery client and reads configuration from environment variables.
     */
    public BigQueryServiceImpl() {
        this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
        this.projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        this.datasetName = System.getenv().getOrDefault("BIGQUERY_DATASET", "user_data");
        this.tableName = System.getenv().getOrDefault("BIGQUERY_TABLE", "User");
    }

    /**
     * Constructor with explicit configuration.
     * @param projectId Google Cloud project ID
     * @param datasetName BigQuery dataset name
     * @param tableName BigQuery table name
     */
    public BigQueryServiceImpl(String projectId, String datasetName, String tableName) {
        this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
        this.projectId = projectId;
        this.datasetName = datasetName;
        this.tableName = tableName;
    }

    /**
     * Creates the dataset if it does not exist.
     */
    @Override
    public void createDatasetIfNotExists() {
        DatasetId datasetId = DatasetId.of(projectId, datasetName);
        Dataset dataset = bigQuery.getDataset(datasetId);

        // Create dataset if not present
        if (dataset == null) {
            DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetId)
                    .setDescription("User data storage")
                    .setLocation("US")
                    .build();
            bigQuery.create(datasetInfo);
        }
    }

    /**
     * Creates the user table with the required schema if it does not exist.
     */
    @Override
    public void createTableIfNotExists() {
        TableId tableId = TableId.of(projectId, datasetName, tableName);
        Table table = bigQuery.getTable(tableId);

        // Create table if not present
        if (table == null) {
            Schema schema = Schema.of(
                    Field.of("id", StandardSQLTypeName.INT64),
                    Field.of("name", StandardSQLTypeName.STRING),
                    Field.of("dateOfBirth", StandardSQLTypeName.STRING),
                    Field.of("email", StandardSQLTypeName.STRING),
                    Field.of("password", StandardSQLTypeName.STRING),
                    Field.of("phone", StandardSQLTypeName.STRING),
                    Field.of("gender", StandardSQLTypeName.STRING),
                    Field.of("address", StandardSQLTypeName.STRING)
            );

            TableDefinition tableDefinition = StandardTableDefinition.of(schema);
            TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
            bigQuery.create(tableInfo);
        }
    }

    /**
     * Checks if the user table exists in BigQuery.
     * @return true if table exists, false otherwise
     */
    @Override
    public boolean tableExists() {
        TableId tableId = TableId.of(projectId, datasetName, tableName);
        Table table = bigQuery.getTable(tableId);
        return table != null;
    }

    /**
     * Migrates a list of users to BigQuery.
     * Ensures dataset and table exist, then inserts user data.
     * @param users List of users to migrate
     * @return Map with migration result details
     */
    @Override
    public Map<String, Object> migrateUsers(List<User> users) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Ensure dataset and table exist
            createDatasetIfNotExists();
            createTableIfNotExists();

            TableId tableId = TableId.of(projectId, datasetName, tableName);

            // Convert users to BigQuery rows
            List<InsertAllRequest.RowToInsert> rows = new ArrayList<>();

            for (User user : users) {
                Map<String, Object> rowContent = new HashMap<>();
                rowContent.put("id", user.getId());
                rowContent.put("name", user.getName());
                rowContent.put("dateOfBirth", user.getDateOfBirth() != null
                        ? user.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        : "");
                rowContent.put("email", user.getEmail());
                rowContent.put("password", user.getPassword());
                rowContent.put("phone", user.getPhone());
                rowContent.put("gender", user.getGender());
                rowContent.put("address", user.getAddress());

                rows.add(InsertAllRequest.RowToInsert.of(rowContent));
            }

            // Insert rows into BigQuery
            InsertAllRequest insertRequest = InsertAllRequest.newBuilder(tableId)
                    .setRows(rows)
                    .build();

            InsertAllResponse response = bigQuery.insertAll(insertRequest);

            // Handle insertion result
            if (response.hasErrors()) {
                Map<Long, List<BigQueryError>> errors = response.getInsertErrors();
                int migratedCount = users.size() - errors.size();
                result.put("success", true);
                result.put("migrated", migratedCount);
                result.put("total", users.size());
                result.put("message", migratedCount + " users migrated to BigQuery with some errors");
            } else {
                result.put("success", true);
                result.put("migrated", users.size());
                result.put("total", users.size());
                result.put("message", users.size() + " users successfully migrated to BigQuery");
            }

        } catch (Exception e) {
            // Handle migration errors
            System.err.println("Error during migration: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Migration failed: " + e.getMessage());
            result.put("migrated", 0);
            result.put("total", users.size());
        }
        return result;
    }

    /**
     * Queries a limited number of users from BigQuery.
     * @param limit Maximum number of users to return
     * @return List of users retrieved from BigQuery
     */
    @Override
    public List<User> queryUsers(int limit) {
        try {
            // Build SQL query for users
            String query = String.format(
                    "SELECT id, name, dateOfBirth, email, phone, gender, address FROM `%s.%s.%s` LIMIT %d",
                    projectId, datasetName, tableName, limit
            );

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);

            List<User> users = new ArrayList<>();
            // Map query result rows to User objects
            for (FieldValueList row : result.iterateAll()) {
                User user = new User();
                user.setId(row.get("id").getLongValue());
                user.setName(row.get("name").getStringValue());
                user.setEmail(row.get("email").getStringValue());
                user.setPhone(row.get("phone").getStringValue());
                user.setGender(row.get("gender").getStringValue());
                user.setAddress(row.get("address").getStringValue());
                users.add(user);
            }

            return users;
        } catch (Exception e) {
            // Handle query errors
            System.err.println("Error querying users: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
