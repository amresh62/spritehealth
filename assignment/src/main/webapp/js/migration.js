// Migration functionality
import API from './api.js';
import UI from './ui.js';
import { logout, requireAuth } from './auth.js';

let usersToMigrate = [];

document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication
    const isAuth = await requireAuth();
    if (!isAuth) return;

    // Initialize UI
    initializeUI();
    
    // Load migration preview
    await loadMigrationPreview();

    // Set up event listeners
    setupEventListeners();
});

function initializeUI() {
    // Display user email in nav
    const user = JSON.parse(sessionStorage.getItem('user') || '{}');
    const userEmailEl = document.getElementById('userEmail');
    if (userEmailEl && user.email) {
        userEmailEl.textContent = user.email;
    }
}

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

async function loadMigrationPreview() {
    UI.showSpinner('spinner');

    const result = await API.get('/api/migrate');

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        usersToMigrate = result.data.users || [];
        
        updateStats(usersToMigrate.length, 0);
        renderPreview();

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

function renderPreview() {
    const tbody = document.getElementById('previewTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (usersToMigrate.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No users to migrate</td></tr>';
        return;
    }

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

async function startMigration() {
    if (usersToMigrate.length === 0) {
        UI.showMessage('message', 'No users to migrate', 'error');
        return;
    }

    if (!confirm(`Are you sure you want to migrate ${usersToMigrate.length} users to BigQuery?`)) {
        return;
    }

    const migrateBtn = document.getElementById('migrateBtn');
    migrateBtn.disabled = true;
    UI.showSpinner('spinner');

    const result = await API.post('/api/migrate', {});

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        const migratedCount = result.data.migratedCount || 0;
        
        updateStats(usersToMigrate.length, migratedCount);
        
        UI.showMessage('message', result.data.message, 'success');
        
        // Show results section
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

function showEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const migrationPreview = document.getElementById('migrationPreview');
    const migrateBtn = document.getElementById('migrateBtn');

    if (emptyState) emptyState.style.display = 'block';
    if (migrationPreview) migrationPreview.style.display = 'none';
    if (migrateBtn) migrateBtn.style.display = 'none';
}

function hideEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const migrationPreview = document.getElementById('migrationPreview');
    const migrateBtn = document.getElementById('migrateBtn');

    if (emptyState) emptyState.style.display = 'none';
    if (migrationPreview) migrationPreview.style.display = 'block';
    if (migrateBtn) migrateBtn.style.display = 'block';
}
