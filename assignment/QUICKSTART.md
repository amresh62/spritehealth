# SpriteHealth - Quick Start Guide

## ⚡ Quick Setup (5 minutes)

### Step 1: Prerequisites Check
```bash
# Check Java version (need 17+)
java -version

# Check Maven version (need 3.6+)
mvn -version

# Check gcloud CLI
gcloud version
```

### Step 2: Google Cloud Setup
```bash
# Login to Google Cloud
gcloud auth login

# Set your project (replace with your project ID)
gcloud config set project YOUR_PROJECT_ID

# Enable required APIs
gcloud services enable datastore.googleapis.com
gcloud services enable bigquery.googleapis.com
gcloud services enable appengine.googleapis.com

# Authenticate for local development
gcloud auth application-default login
```

### Step 3: Configure the Project

1. Open `src/main/webapp/WEB-INF/appengine-web.xml`
2. Replace `your-project-id` with your actual Google Cloud Project ID:
   ```xml
   <env-var name="GOOGLE_CLOUD_PROJECT" value="YOUR_ACTUAL_PROJECT_ID" />
   ```

### Step 4: Build and Run

```bash
# Navigate to project directory
cd assignment

# Clean and build
mvn clean package

# Generate sample Excel data (optional)
mvn exec:java -Dexec.mainClass="com.spritehealth.utils.SampleDataGenerator"

# Run locally
mvn appengine:run
```

### Step 5: Test the Application

1. Open browser: `http://localhost:8080`
2. Upload the generated `sample_users.xlsx` file
3. Login with any user credentials from the Excel file
4. Explore the User Directory and Migration features

## 🚀 Deploy to Production

```bash
# Deploy to App Engine
mvn appengine:deploy

# Access your app
# https://YOUR_PROJECT_ID.appspot.com
```

## 📝 Sample User Credentials

If you generated sample data, you can login with any of these patterns:
- Email: `firstname.lastname###@example.com`
- Password: `Password####`

Example:
- Email: `john.smith123@example.com`
- Password: `Password5678`

## 🔧 Common Commands

```bash
# View logs (production)
gcloud app logs tail -s default

# View Datastore entities
gcloud datastore indexes list

# Query BigQuery
bq query --use_legacy_sql=false 'SELECT * FROM user_data.User LIMIT 10'

# Stop local dev server
# Press Ctrl+C in terminal
```

## 🎯 Features to Test

### 1. Upload (index.html)
- [ ] Upload Excel file
- [ ] View upload results
- [ ] Check stats

### 2. Login (login.html)
- [ ] Login with valid credentials
- [ ] Try invalid credentials
- [ ] Logout

### 3. User Directory (users.html)
- [ ] View all users
- [ ] Search by name
- [ ] Filter by gender
- [ ] Delete a user
- [ ] Refresh list

### 4. Migration (migration.html)
- [ ] Preview migration
- [ ] Execute migration
- [ ] Verify in BigQuery console

## 🐛 Quick Troubleshooting

### App won't start
```bash
# Check if port 8080 is in use
netstat -ano | findstr :8080

# Kill process if needed (Windows)
taskkill /PID <PID> /F
```

### Authentication errors
```bash
# Re-authenticate
gcloud auth application-default login
gcloud auth login
```

### Build errors
```bash
# Clean and rebuild
mvn clean install -U
```

## 📚 Next Steps

1. ✅ Review the full README.md for detailed documentation
2. ✅ Customize the UI styles in `css/styles.css`
3. ✅ Add validation rules in servlets
4. ✅ Implement password hashing for security
5. ✅ Add more search filters
6. ✅ Set up monitoring and alerts

## 🎉 Success Criteria

Your setup is successful if you can:
- ✅ Upload an Excel file with users
- ✅ Login with uploaded credentials
- ✅ View and manage users
- ✅ Migrate data to BigQuery
- ✅ See data in Datastore console
- ✅ Query data in BigQuery console

## 📞 Support

For issues or questions:
1. Check the main README.md
2. Review servlet logs in terminal
3. Check Google Cloud console logs
4. Verify API permissions

---

**🎓 Educational Project**: This is a complete implementation of the SpriteHealth assignment requirements.

**⚠️ Security Note**: This implementation is for educational purposes. In production, add password hashing, HTTPS, CSRF protection, and input validation.
