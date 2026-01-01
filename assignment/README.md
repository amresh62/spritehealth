# SpriteHealth User Management System

A Google App Engine Java application for managing user data with Excel upload, Google Cloud Datastore persistence, and BigQuery migration capabilities.

## 🎯 Features

### Milestone 1: Data Ingestion & User Management
- ✅ Excel file upload functionality (.xlsx format)
- ✅ Parse and validate user data (Name, DOB, Email, Password, Phone, Gender, Address)
- ✅ Store users in Google Cloud Datastore
- ✅ User Directory UI with comprehensive management features:
  - Display all users in a table
  - Real-time search across multiple fields
  - Filter by gender
  - Delete individual users
- ✅ User authentication with login/logout

### Milestone 2: BigQuery Migration
- ✅ Migration preview UI showing all records
- ✅ Bulk migration from Datastore to BigQuery
- ✅ Data integrity validation
- ✅ Support for ~100 records (scalable)

## 🏗️ Architecture

### Technology Stack
- **Backend**: Java 17, Servlet API
- **App Engine**: Standard Environment (Java 17)
- **Storage**: Google Cloud Datastore
- **Analytics**: Google BigQuery
- **Excel Processing**: Apache POI
- **JSON**: Gson
- **Frontend**: Native HTML, CSS, JavaScript (ES6 Modules)

### Project Structure
```
assignment/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/spritehealth/
│   │   │       ├── models/
│   │   │       │   └── User.java
│   │   │       ├── services/
│   │   │       │   ├── DatastoreService.java
│   │   │       │   └── BigQueryService.java
│   │   │       ├── servlets/
│   │   │       │   ├── UploadServlet.java
│   │   │       │   ├── LoginServlet.java
│   │   │       │   ├── LogoutServlet.java
│   │   │       │   ├── UserServlet.java
│   │   │       │   └── MigrationServlet.java
│   │   │       └── utils/
│   │   │           └── SampleDataGenerator.java
│   │   ├── webapp/
│   │   │   ├── WEB-INF/
│   │   │   │   ├── appengine-web.xml
│   │   │   │   └── web.xml
│   │   │   ├── css/
│   │   │   │   └── styles.css
│   │   │   ├── js/
│   │   │   │   ├── api.js
│   │   │   │   ├── ui.js
│   │   │   │   ├── auth.js
│   │   │   │   ├── upload.js
│   │   │   │   ├── users.js
│   │   │   │   └── migration.js
│   │   │   ├── index.html
│   │   │   ├── login.html
│   │   │   ├── users.html
│   │   │   └── migration.html
│   │   └── resources/
│   └── test/
└── target/
```

## 🚀 Setup & Deployment

### Prerequisites
1. Java 17 or higher
2. Maven 3.6 or higher
3. Google Cloud SDK
4. Google Cloud Project with:
   - App Engine enabled
   - Datastore API enabled
   - BigQuery API enabled

### Local Development

1. **Clone and navigate to the project**
   ```bash
   cd assignment
   ```

2. **Configure Google Cloud Project**
   
   Edit `src/main/webapp/WEB-INF/appengine-web.xml`:
   ```xml
   <env-var name="GOOGLE_CLOUD_PROJECT" value="your-project-id" />
   <env-var name="BIGQUERY_DATASET" value="user_data" />
   <env-var name="BIGQUERY_TABLE" value="User" />
   ```

3. **Authenticate with Google Cloud**
   ```bash
   gcloud auth login
   gcloud config set project YOUR_PROJECT_ID
   gcloud auth application-default login
   ```

4. **Build the project**
   ```bash
   mvn clean package
   ```

5. **Generate sample Excel file (optional)**
   ```bash
   mvn exec:java -Dexec.mainClass="com.spritehealth.utils.SampleDataGenerator"
   ```
   This creates `sample_users.xlsx` with 100 sample users.

6. **Run locally with App Engine Dev Server**
   ```bash
   mvn appengine:run
   ```
   
   Access the application at: `http://localhost:8080`

### Deploy to Google App Engine

1. **Deploy the application**
   ```bash
   mvn appengine:deploy
   ```

2. **Access your deployed application**
   ```
   https://YOUR_PROJECT_ID.appspot.com
   ```

## 📋 Usage Guide

### 1. Upload Users

1. Navigate to the home page (`index.html`)
2. Click or drag-and-drop an Excel file (.xlsx)
3. The Excel file should have these columns:
   - **Name**: User's full name
   - **DOB**: Date of birth (YYYY-MM-DD format)
   - **Email**: User's email address
   - **Password**: User's password
   - **Phone**: Phone number
   - **Gender**: Male/Female/Other
   - **Address**: Full address
4. Click "Upload to Datastore"
5. Users are now stored in Google Cloud Datastore

### 2. Login

1. Navigate to the login page
2. Use credentials from the uploaded Excel file
3. Upon successful login, you'll be redirected to the User Directory

### 3. User Directory

- **View**: See all users in a table format
- **Search**: Use the search box to find users by name, email, phone, or address
- **Filter**: Filter users by gender using the dropdown
- **Delete**: Click the delete button to remove a user from Datastore
- **Refresh**: Reload the user list

### 4. BigQuery Migration

1. Navigate to the Migration page
2. Review the list of users that will be migrated
3. Click "Start Migration to BigQuery"
4. All users from Datastore are copied to BigQuery
5. Data can be queried in BigQuery for analytics

## 🔌 API Endpoints

### Upload API
- **POST** `/api/upload`
  - Upload Excel file with user data
  - Content-Type: `multipart/form-data`

### Authentication API
- **POST** `/api/login`
  - Body: `{ "email": "user@example.com", "password": "password123" }`
- **GET** `/api/login`
  - Check authentication status
- **POST** `/api/logout`
  - Logout current user

### User Management API
- **GET** `/api/users`
  - Retrieve all users
- **GET** `/api/users/{id}`
  - Retrieve specific user
- **POST** `/api/users`
  - Create new user
  - Body: User JSON object
- **DELETE** `/api/users/{id}`
  - Delete user

### Migration API
- **GET** `/api/migrate`
  - Preview users for migration
- **POST** `/api/migrate`
  - Execute migration to BigQuery

## 🗄️ Data Models

### User Entity (Datastore)
```java
{
  "id": Long,
  "name": String,
  "dateOfBirth": LocalDate,
  "email": String,
  "password": String,
  "phone": String,
  "gender": String,
  "address": String
}
```

### BigQuery Schema
```sql
CREATE TABLE user_data.User (
  id INT64,
  name STRING,
  dateOfBirth STRING,
  email STRING,
  password STRING,
  phone STRING,
  gender STRING,
  address STRING
);
```

## 🔒 Security Considerations

- Passwords are stored as plain text (for demo purposes only)
- In production, implement:
  - Password hashing (BCrypt, Argon2)
  - HTTPS enforcement
  - CSRF protection
  - Input validation and sanitization
  - Rate limiting
  - Role-based access control

## 🧪 Testing

### Generate Test Data
```bash
mvn exec:java -Dexec.mainClass="com.spritehealth.utils.SampleDataGenerator"
```

### Manual Testing Checklist
- [ ] Upload Excel file with valid data
- [ ] Upload Excel file with invalid data
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Search users by different fields
- [ ] Filter users by gender
- [ ] Delete a user
- [ ] Migrate users to BigQuery
- [ ] Verify data in Datastore console
- [ ] Verify data in BigQuery console

## 📊 Monitoring

### View Logs
```bash
gcloud app logs tail -s default
```

### Datastore Console
```
https://console.cloud.google.com/datastore/entities
```

### BigQuery Console
```
https://console.cloud.google.com/bigquery
```

## 🐛 Troubleshooting

### Issue: "Project ID not found"
**Solution**: Set the `GOOGLE_CLOUD_PROJECT` environment variable in `appengine-web.xml`

### Issue: "Permission denied"
**Solution**: Ensure your service account has these roles:
- Datastore User
- BigQuery Data Editor
- BigQuery Job User

### Issue: "Excel parsing failed"
**Solution**: Verify Excel format:
- Use .xlsx format (Excel 2007+)
- Ensure headers match expected columns
- Check date format in DOB column

### Issue: "Session timeout"
**Solution**: Session expires after 30 minutes of inactivity. Re-login required.

## 📝 Technical Constraints Met

✅ Servlet-based architecture (no Spring Boot)  
✅ Native JavaScript (no React/Angular)  
✅ Google Cloud Datastore for persistence  
✅ BigQuery for analytics storage  
✅ No enterprise frameworks  
✅ Modular JavaScript design  
✅ Excel file processing with Apache POI  
✅ Session-based authentication  

## 🎓 Learning Resources

- [Google App Engine Documentation](https://cloud.google.com/appengine/docs)
- [Google Cloud Datastore Guide](https://cloud.google.com/datastore/docs)
- [BigQuery Documentation](https://cloud.google.com/bigquery/docs)
- [Apache POI Tutorial](https://poi.apache.org/components/spreadsheet/)

## 📄 License

This project is for educational purposes as part of the SpriteHealth assignment.

## 👨‍💻 Author

Created for SpriteHealth User Management System Assignment

---

**Note**: Remember to configure your Google Cloud Project ID in `appengine-web.xml` before deploying!
