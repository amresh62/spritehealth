# Testing with In-Memory Datastore

The application has been configured to use **in-memory storage** for local testing, which means:

✅ **No Google Cloud setup required**
✅ **No authentication needed**
✅ **Instant startup**
❌ **Data is NOT persistent** - all data is lost when the server stops

## Quick Start

1. **Run the server:**
   ```
   run-local.bat
   ```
   
2. **Open your browser:**
   ```
   http://localhost:8080
   ```

3. **Upload the sample file:**
   - The sample file `sample_users.xlsx` (100 users) is already generated in the project folder
   - Use the upload interface to test the functionality

## What's Configured

- **UploadServlet** → Uses `InMemoryDatastoreService`
- **UserServlet** → Uses `InMemoryDatastoreService`
- **LoginServlet** → Uses `InMemoryDatastoreService`
- **MigrationServlet** → Uses `InMemoryDatastoreService` (BigQuery features disabled for local testing)

## Switching to Real Cloud Datastore

When you're ready to deploy to Google Cloud:

1. **Update the servlets** to use `DatastoreService` instead of `InMemoryDatastoreService`
2. **Set up a Google Cloud Project:**
   - Create a project at https://console.cloud.google.com
   - Enable Cloud Datastore API
   - Enable BigQuery API (for migration features)

3. **Update `appengine-web.xml`:**
   ```xml
   <env-var name="GOOGLE_CLOUD_PROJECT" value="your-actual-project-id" />
   ```

4. **Deploy:**
   ```
   mvn appengine:deploy
   ```

## Testing the Application

### 1. Upload Users
- Navigate to http://localhost:8080
- Drag and drop `sample_users.xlsx` or click to browse
- View the 100 uploaded users

### 2. Search & Filter
- Use the search bar to find users by name
- Click column headers to sort
- Use the filter dropdown for advanced filtering

### 3. Delete Users
- Click the delete button (🗑️) on any user row
- Confirm the deletion

### 4. Login (for testing authentication)
- Use any email from the uploaded users
- Default password for sample users: `password123`

## Known Limitations (Local Mode)

- Data is lost when server restarts
- BigQuery migration features won't work (requires real Google Cloud setup)
- No data persistence or backup
- All users share the same in-memory storage

## Files

- `sample_users.xlsx` - Pre-generated test data (100 users)
- `run-local.bat` - Quick start script for local development
- `start-local.bat` - Advanced script with Datastore emulator (requires Cloud SDK)

---

**Ready to test!** Run `run-local.bat` and open http://localhost:8080
