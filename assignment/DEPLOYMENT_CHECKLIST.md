# 🚀 Deployment Checklist

## Pre-Deployment

### 1. Google Cloud Setup
- [ ] Create Google Cloud Project
- [ ] Enable billing on the project
- [ ] Install gcloud CLI
- [ ] Authenticate: `gcloud auth login`
- [ ] Set project: `gcloud config set project YOUR_PROJECT_ID`

### 2. Enable Required APIs
```bash
gcloud services enable appengine.googleapis.com
gcloud services enable datastore.googleapis.com
gcloud services enable bigquery.googleapis.com
```

### 3. Configure Application
- [ ] Update `src/main/webapp/WEB-INF/appengine-web.xml`
  - Replace `your-project-id` with actual project ID
  - Set `BIGQUERY_DATASET` (default: `user_data`)
  - Set `BIGQUERY_TABLE` (default: `User`)

### 4. Test Locally
- [ ] Build project: `mvn clean package`
- [ ] Run locally: `mvn appengine:run`
- [ ] Test upload at http://localhost:8080
- [ ] Test login functionality
- [ ] Test user directory
- [ ] Test migration

## Deployment Steps

### 1. Build for Production
```bash
mvn clean package
```

### 2. Deploy to App Engine
```bash
mvn appengine:deploy
```

### 3. Verify Deployment
- [ ] Access application: `https://YOUR_PROJECT_ID.appspot.com`
- [ ] Check App Engine dashboard
- [ ] View logs: `gcloud app logs tail -s default`

### 4. Test Production Features
- [ ] Upload Excel file
- [ ] Login with test credentials
- [ ] View users in directory
- [ ] Delete a user
- [ ] Execute migration to BigQuery

### 5. Verify Data
- [ ] Check Datastore: https://console.cloud.google.com/datastore
- [ ] Check BigQuery: https://console.cloud.google.com/bigquery
- [ ] Run query: `SELECT COUNT(*) FROM user_data.User`

## Post-Deployment

### 1. Security Hardening (Production)
- [ ] Implement password hashing
- [ ] Enable HTTPS only
- [ ] Add CSRF protection
- [ ] Implement rate limiting
- [ ] Add input validation
- [ ] Set up Cloud Armor

### 2. Monitoring Setup
- [ ] Enable Cloud Monitoring
- [ ] Set up error alerts
- [ ] Configure uptime checks
- [ ] Set up log-based metrics
- [ ] Create custom dashboards

### 3. Backup Strategy
- [ ] Export Datastore regularly
- [ ] Backup BigQuery tables
- [ ] Document recovery procedures

### 4. Performance Optimization
- [ ] Enable Cloud CDN
- [ ] Configure caching headers
- [ ] Optimize images/assets
- [ ] Enable compression
- [ ] Review instance scaling

## Configuration Checklist

### appengine-web.xml
- [x] Runtime: java17
- [x] Threadsafe: true
- [x] Sessions enabled: true
- [ ] Project ID: **UPDATE THIS**
- [x] Dataset name: user_data
- [x] Table name: User

### web.xml
- [x] All servlets mapped
- [x] Welcome file configured
- [x] Security constraints defined

### pom.xml
- [x] All dependencies included
- [x] App Engine plugin configured
- [x] Java 17 specified
- [x] WAR packaging

## Testing Checklist

### Functionality Tests
- [ ] Excel upload (.xlsx)
- [ ] Excel upload (.xls)
- [ ] Invalid file type rejection
- [ ] File size validation (>10MB)
- [ ] Valid user login
- [ ] Invalid login rejection
- [ ] Session persistence
- [ ] Session timeout (30 min)
- [ ] User search
- [ ] User filter by gender
- [ ] User deletion
- [ ] Migration to BigQuery
- [ ] Logout functionality

### Data Validation
- [ ] All user fields stored correctly
- [ ] Date format preserved
- [ ] Special characters handled
- [ ] Empty fields handled
- [ ] Data integrity in Datastore
- [ ] Data integrity in BigQuery
- [ ] Schema matching

### UI/UX Tests
- [ ] Responsive design on mobile
- [ ] Responsive design on tablet
- [ ] Responsive design on desktop
- [ ] Loading indicators working
- [ ] Error messages displayed
- [ ] Success messages displayed
- [ ] Navigation working
- [ ] Logout working

### Security Tests
- [ ] Protected pages require auth
- [ ] Session expires correctly
- [ ] No password in responses
- [ ] HTML escaping working
- [ ] Unauthorized access blocked

## Troubleshooting Commands

### View Application Logs
```bash
# Live logs
gcloud app logs tail -s default

# Recent logs
gcloud app logs read --limit=50
```

### Check Datastore
```bash
# List entities
gcloud datastore indexes list

# Query (using gcloud datastore)
# Or use Console: https://console.cloud.google.com/datastore
```

### Query BigQuery
```bash
# List datasets
bq ls

# Query users
bq query --use_legacy_sql=false \
  'SELECT COUNT(*) as total FROM user_data.User'

# View table schema
bq show user_data.User
```

### App Engine Management
```bash
# View versions
gcloud app versions list

# Stop a version
gcloud app versions stop VERSION_ID

# Delete a version
gcloud app versions delete VERSION_ID

# View service details
gcloud app describe
```

### Billing Check
```bash
# View current costs
gcloud billing accounts list
gcloud billing projects describe YOUR_PROJECT_ID
```

## Rollback Plan

### If Deployment Fails
1. Check build errors: `mvn clean package`
2. Verify configuration files
3. Check gcloud authentication
4. Review deployment logs
5. Revert to previous version if needed

### Rollback Command
```bash
# List versions
gcloud app versions list

# Route traffic to previous version
gcloud app services set-traffic default --splits=PREVIOUS_VERSION=1
```

## Performance Benchmarks

### Expected Performance
- Upload 100 users: < 5 seconds
- Login: < 1 second
- Load user directory: < 2 seconds
- Search/filter: < 500ms
- Delete user: < 1 second
- Migration to BigQuery: < 10 seconds

### If Performance Issues
- [ ] Check instance scaling settings
- [ ] Review Datastore indexes
- [ ] Optimize queries
- [ ] Enable caching
- [ ] Increase instance class

## Cost Estimation

### Free Tier Limits (App Engine Standard)
- 28 instance hours per day
- 1 GB outbound data per day
- Shared memcache
- 1 GB Cloud Datastore storage
- BigQuery: 1 TB queries/month

### Estimated Monthly Cost (Light Usage)
- App Engine: $0-25
- Datastore: $0-10
- BigQuery: $0-5
- **Total**: $0-40/month

## Success Criteria

### Deployment Successful If:
- ✅ Application accessible at appspot.com URL
- ✅ Can upload Excel files
- ✅ Users stored in Datastore
- ✅ Can login with uploaded credentials
- ✅ User directory displays all users
- ✅ Search and filter working
- ✅ Can delete users
- ✅ Migration to BigQuery successful
- ✅ Data visible in BigQuery console
- ✅ No critical errors in logs

## Final Steps

### 1. Documentation
- [ ] Update README with production URL
- [ ] Document any environment-specific changes
- [ ] Share credentials securely
- [ ] Create user manual if needed

### 2. Handoff
- [ ] Demo the application
- [ ] Explain architecture
- [ ] Share access to GCP project
- [ ] Provide support contact

### 3. Maintenance Plan
- [ ] Schedule for updates
- [ ] Monitor error rates
- [ ] Review usage patterns
- [ ] Plan for scaling

---

## Quick Deploy Command
```bash
# One-line deploy (after configuration)
mvn clean package && mvn appengine:deploy
```

## Emergency Contacts
- Google Cloud Support: https://cloud.google.com/support
- App Engine Documentation: https://cloud.google.com/appengine/docs
- Community Forum: https://stackoverflow.com/questions/tagged/google-app-engine

---

**Last Updated**: 2025-12-31  
**Version**: 1.0.0  
**Status**: ✅ Ready for Deployment
