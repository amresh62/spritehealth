// Authentication functionality

// Import API and UI utility modules
import API from './api.js';
import UI from './ui.js';

// Wait for the DOM to be fully loaded before attaching event listeners
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    
    // Attach login handler if login form exists
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Check authentication status on page load
    checkAuthStatus();
});

/**
 * Handles the login form submission.
 * Validates input, sends login request, and processes the response.
 */
async function handleLogin(e) {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    // Validate input fields
    if (!email || !password) {
        UI.showMessage('message', 'Please enter both email and password', 'error');
        return;
    }

    UI.showSpinner('spinner');

    // Send login request to API
    const result = await API.post('/api/login', { email, password });

    UI.hideSpinner('spinner');

    // Handle login response
    if (result.success && result.data.success) {
        UI.showMessage('message', 'Login successful! Redirecting...', 'success');
        
        // Store user info in session storage
        sessionStorage.setItem('user', JSON.stringify(result.data.user));
        
        // Redirect to users page after a short delay
        setTimeout(() => {
            window.location.href = 'users.html';
        }, 1000);
    } else {
        // Show error message if login failed
        UI.showMessage('message', result.data?.message || 'Login failed', 'error');
    }
}

/**
 * Checks if the user is already authenticated.
 * Redirects to users page if authenticated.
 */
async function checkAuthStatus() {
    const result = await API.get('/api/login');
    
    if (result.success && result.data.authenticated) {
        // Already logged in, redirect to users page
        if (window.location.pathname.endsWith('login.html')) {
            window.location.href = 'users.html';
        }
    }
}

/**
 * Logs out the current user.
 * Clears session storage and redirects to login page.
 */
export async function logout() {
    UI.showSpinner('spinner');
    
    // Send logout request to API
    const result = await API.post('/api/logout', {});
    
    UI.hideSpinner('spinner');
    
    // Remove user info from session storage
    sessionStorage.removeItem('user');
    // Redirect to login page
    window.location.href = 'login.html';
}

/**
 * Checks if the user is authenticated for protected pages.
 * Redirects to login page if not authenticated.
 * @returns {Promise<boolean>} true if authenticated, false otherwise
 */
export async function requireAuth() {
    try {
        const result = await API.get('/api/login');
        
        console.log('Auth check result:', result);
        
        if (!result.success || !result.data || !result.data.authenticated) {
            console.log('Not authenticated, redirecting to login');
            window.location.href = 'login.html';
            return false;
        }
        
        console.log('Authenticated successfully');
        return true;
    } catch (error) {
        console.error('Auth check error:', error);
        window.location.href = 'login.html';
        return false;
    }
}
