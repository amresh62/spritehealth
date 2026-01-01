// Authentication functionality
import API from './api.js';
import UI from './ui.js';

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Check if already logged in
    checkAuthStatus();
});

async function handleLogin(e) {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    if (!email || !password) {
        UI.showMessage('message', 'Please enter both email and password', 'error');
        return;
    }

    UI.showSpinner('spinner');

    const result = await API.post('/api/login', { email, password });

    UI.hideSpinner('spinner');

    if (result.success && result.data.success) {
        UI.showMessage('message', 'Login successful! Redirecting...', 'success');
        
        // Store user info
        sessionStorage.setItem('user', JSON.stringify(result.data.user));
        
        // Redirect to users page
        setTimeout(() => {
            window.location.href = 'users.html';
        }, 1000);
    } else {
        UI.showMessage('message', result.data?.message || 'Login failed', 'error');
    }
}

async function checkAuthStatus() {
    const result = await API.get('/api/login');
    
    if (result.success && result.data.authenticated) {
        // Already logged in, redirect to users page
        if (window.location.pathname.endsWith('login.html')) {
            window.location.href = 'users.html';
        }
    }
}

// Logout functionality
export async function logout() {
    UI.showSpinner('spinner');
    
    const result = await API.post('/api/logout', {});
    
    UI.hideSpinner('spinner');
    
    sessionStorage.removeItem('user');
    window.location.href = 'login.html';
}

// Check if user is authenticated (for protected pages)
export async function requireAuth() {
    const result = await API.get('/api/login');
    
    if (!result.success || !result.data.authenticated) {
        window.location.href = 'login.html';
        return false;
    }
    
    return true;
}
