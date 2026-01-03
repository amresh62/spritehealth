// Migration functionality
import API from './api.js';
import UI from './ui.js';
import { logout, requireAuth } from './auth.js';

// Array to hold users that need to be migrated
let usersToMigrate = [];

// Main entry point: runs when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', async () => {
    // Check if the user is authenticated
    const isAuth = await requireAuth();
    if (!isAuth) return;

    // Initialize UI elements (e.g., user email)
    initializeUI();
    
    // Load the preview of users to be migrated
    await loadMigrationPreview();

    // Set up event listeners for buttons
    setupEventListeners();
});

// Display the logged-in user's email in the navigation bar
function initializeUI() {
    const user = JSON.parse(sessionStorage.getItem('user') || '{}');
    const userEmailEl = document.getElementById('userEmail');
    if (userEmailEl && user.email) {
        userEmailEl.textContent = user.email;
    }
}

// Attach event listeners to logout and migrate buttons
function setupEventListeners() {
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // Migration button
    const migrateBtn = document.getElementById('migrateBtn');
    if (migrateBtn) {
        migrateBtn.addEventListener('click', startMigration);
    }
}

// Fetch and display the list of users to be migrated
async function loadMigrationPreview() {
    UI.showSpinner('spinner');

    const result = await API.get('/api/migrate');

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        usersToMigrate = result.data.users || [];
        
        // Update stats and render preview table
        updateStats(usersToMigrate.length, 0);
        renderPreview();

        // Show or hide empty state based on user count
        if (usersToMigrate.length === 0) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    } else {
        UI.showMessage('message', 'Failed to load migration preview', 'error');
        showEmptyState();
    }
}

// Render the preview table of users to be migrated
function renderPreview() {
    const tbody = document.getElementById('previewTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (usersToMigrate.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No users to migrate</td></tr>';
        return;
    }

    // Add a row for each user
    usersToMigrate.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${UI.escapeHtml(user.name || 'N/A')}</td>
            <td>${UI.escapeHtml(user.email || 'N/A')}</td>
            <td>${UI.escapeHtml(user.phone || 'N/A')}</td>
            <td>${UI.escapeHtml(user.gender || 'N/A')}</td>
        `;
        tbody.appendChild(row);
    });
}

// Start the migration process when the user clicks the migrate button
async function startMigration() {
    if (usersToMigrate.length === 0) {
        UI.showMessage('message', 'No users to migrate', 'error');
        return;
    }

    // Confirm migration action with the user
    if (!confirm(`Are you sure you want to migrate ${usersToMigrate.length} users to BigQuery?`)) {
        return;
    }

    const migrateBtn = document.getElementById('migrateBtn');
    migrateBtn.disabled = true;
    UI.showSpinner('spinner');

    // Call the API to start migration
    const result = await API.post('/api/migrate', {});

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        const migratedCount = result.data.migrated || 0;
        // After migration, usersToMigrate should be empty
        usersToMigrate = [];
        // Update stats: 0 left to migrate, migratedCount migrated
        updateStats(0, migratedCount);
        UI.showMessage('message', result.data.message, 'success');
        // Show results section and hide preview/migrate button
        document.getElementById('migrationPreview').style.display = 'none';
        document.getElementById('migrationResults').style.display = 'block';
        document.getElementById('migrateBtn').style.display = 'none';
        const successMessage = document.getElementById('successMessage');
        if (successMessage) {
            successMessage.textContent = `Successfully migrated ${migratedCount} users to BigQuery!`;
        }
    } else {
        UI.showMessage('message', result.data?.message || 'Migration failed', 'error');
        migrateBtn.disabled = false;
    }
}

// Update the displayed statistics for users to migrate and migrated users
function updateStats(totalCount, migratedCount) {
    const datastoreCountEl = document.getElementById('datastoreCount');
    const migratedCountEl = document.getElementById('migratedCount');

    if (datastoreCountEl) {
        datastoreCountEl.textContent = totalCount;
    }

    if (migratedCountEl) {
        migratedCountEl.textContent = migratedCount;
    }
}

// Show the empty state UI when there are no users to migrate
function showEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const migrationPreview = document.getElementById('migrationPreview');
    const migrateBtn = document.getElementById('migrateBtn');

    if (emptyState) emptyState.style.display = 'block';
    if (migrationPreview) migrationPreview.style.display = 'none';
    if (migrateBtn) migrateBtn.style.display = 'none';
}

// Hide the empty state UI and show the migration preview and button
function hideEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const migrationPreview = document.getElementById('migrationPreview');
    const migrateBtn = document.getElementById('migrateBtn');

    if (emptyState) emptyState.style.display = 'none';
    if (migrationPreview) migrationPreview.style.display = 'block';
    if (migrateBtn) migrateBtn.style.display = 'block';
}
