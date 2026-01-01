// User management functionality
import API from './api.js';
import UI from './ui.js';
import { logout, requireAuth } from './auth.js';

let allUsers = [];
let filteredUsers = [];

document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication
    const isAuth = await requireAuth();
    if (!isAuth) return;

    // Initialize UI
    initializeUI();
    
    // Load users
    await loadUsers();

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

    // Search functionality
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }

    // Filter functionality
    const genderFilter = document.getElementById('genderFilter');
    if (genderFilter) {
        genderFilter.addEventListener('change', handleFilter);
    }

    // Refresh button
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadUsers);
    }
}

async function loadUsers() {
    UI.showSpinner('spinner');

    const result = await API.get('/api/users');

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        allUsers = result.data.users || [];
        filteredUsers = [...allUsers];
        
        updateStats();
        renderUsers();
        
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

function renderUsers() {
    const tbody = document.getElementById('userTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (filteredUsers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No users match your search</td></tr>';
        return;
    }

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

    // Apply current filter
    applyGenderFilter();
    
    updateStats();
    renderUsers();
}

function handleFilter() {
    applyGenderFilter();
    updateStats();
    renderUsers();
}

function applyGenderFilter() {
    const genderFilter = document.getElementById('genderFilter');
    const selectedGender = genderFilter ? genderFilter.value : '';

    if (selectedGender) {
        filteredUsers = filteredUsers.filter(user => user.gender === selectedGender);
    }
}

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

function showEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const tableContainer = document.getElementById('userTableContainer');

    if (emptyState) emptyState.style.display = 'block';
    if (tableContainer) tableContainer.style.display = 'none';
}

function hideEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const tableContainer = document.getElementById('userTableContainer');

    if (emptyState) emptyState.style.display = 'none';
    if (tableContainer) tableContainer.style.display = 'block';
}

// Delete user function (exposed globally)
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
