// User management functionality

// Import required modules
import API from './api.js';
import UI from './ui.js';
import { logout, requireAuth } from './auth.js';

// Arrays to hold all users and filtered users
let allUsers = [];
let filteredUsers = [];

// Main entry point: runs when DOM is loaded
document.addEventListener('DOMContentLoaded', async () => {
    // Check if user is authenticated
    const isAuth = await requireAuth();
    if (!isAuth) return;

    // Initialize UI elements (e.g., user email in nav)
    initializeUI();
    
    // Load users from API
    await loadUsers();

    // Set up event listeners for UI controls
    setupEventListeners();
});

// Display logged-in user's email in the navigation bar
function initializeUI() {
    const user = JSON.parse(sessionStorage.getItem('user') || '{}');
    const userEmailEl = document.getElementById('userEmail');
    if (userEmailEl && user.email) {
        userEmailEl.textContent = user.email;
    }
}

// Attach event listeners to buttons and inputs
function setupEventListeners() {
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // Search input for filtering users
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }

    // Gender filter dropdown
    const genderFilter = document.getElementById('genderFilter');
    if (genderFilter) {
        genderFilter.addEventListener('change', handleFilter);
    }

    // Refresh button to reload users
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadUsers);
    }
}

// Fetch users from API and update UI
async function loadUsers() {
    UI.showSpinner('spinner');

    const result = await API.get('/api/users');

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        // Store users and initialize filtered list
        allUsers = result.data.users || [];
        filteredUsers = [...allUsers];
        
        updateStats();
        renderUsers();
        
        // Show or hide empty state based on user count
        if (allUsers.length === 0) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    } else {
        UI.showMessage('message', 'Failed to load users', 'error');
        showEmptyState();
    }
}

// Render the user table based on filteredUsers array
function renderUsers() {
    const tbody = document.getElementById('userTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    // Show message if no users match the filter/search
    if (filteredUsers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No users match your search</td></tr>';
        return;
    }

    // Create a table row for each user
    filteredUsers.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${UI.escapeHtml(user.name || 'N/A')}</td>
            <td>${UI.formatDate(user.dateOfBirth)}</td>
            <td>${UI.escapeHtml(user.email || 'N/A')}</td>
            <td>${UI.escapeHtml(user.phone || 'N/A')}</td>
            <td>${UI.escapeHtml(user.gender || 'N/A')}</td>
            <td>${UI.escapeHtml(user.address || 'N/A')}</td>
            <td class="actions">
                <button class="btn btn-danger" onclick="window.deleteUser(${user.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Handle search input: filter users by name, email, phone, or address
function handleSearch(e) {
    const searchTerm = e.target.value.toLowerCase().trim();

    if (!searchTerm) {
        filteredUsers = [...allUsers];
    } else {
        filteredUsers = allUsers.filter(user => {
            return (
                (user.name && user.name.toLowerCase().includes(searchTerm)) ||
                (user.email && user.email.toLowerCase().includes(searchTerm)) ||
                (user.phone && user.phone.toLowerCase().includes(searchTerm)) ||
                (user.address && user.address.toLowerCase().includes(searchTerm))
            );
        });
    }

    // Apply gender filter after search
    applyGenderFilter();
    
    updateStats();
    renderUsers();
}

// Handle gender filter dropdown change
function handleFilter() {
    applyGenderFilter();
    updateStats();
    renderUsers();
}

// Filter users by selected gender
function applyGenderFilter() {
    const genderFilter = document.getElementById('genderFilter');
    const selectedGender = genderFilter ? genderFilter.value : '';

    if (selectedGender) {
        filteredUsers = filteredUsers.filter(user => user.gender === selectedGender);
    }
}

// Update statistics (total and filtered user counts)
function updateStats() {
    const totalUsersEl = document.getElementById('totalUsers');
    const filteredUsersEl = document.getElementById('filteredUsers');

    if (totalUsersEl) {
        totalUsersEl.textContent = allUsers.length;
    }

    if (filteredUsersEl) {
        filteredUsersEl.textContent = filteredUsers.length;
    }
}

// Show empty state message and hide user table
function showEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const tableContainer = document.getElementById('userTableContainer');

    if (emptyState) emptyState.style.display = 'block';
    if (tableContainer) tableContainer.style.display = 'none';
}

// Hide empty state message and show user table
function hideEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const tableContainer = document.getElementById('userTableContainer');

    if (emptyState) emptyState.style.display = 'none';
    if (tableContainer) tableContainer.style.display = 'block';
}

// Delete user function (exposed globally for use in HTML onclick)
window.deleteUser = async function(userId) {
    if (!confirm('Are you sure you want to delete this user?')) {
        return;
    }

    UI.showSpinner('spinner');

    const result = await API.delete(`/api/users/${userId}`);

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        UI.showMessage('message', 'User deleted successfully', 'success');
        await loadUsers();
    } else {
        UI.showMessage('message', result.data?.message || 'Failed to delete user', 'error');
    }
};
