// API utility functions for making HTTP requests to the backend
const API = {
    /**
     * Generic request method for making HTTP requests using fetch.
     * @param {string} url - The endpoint URL.
     * @param {object} options - Fetch options (method, headers, body, etc.).
     * @returns {Promise<object>} - An object containing success status, data, and HTTP status code.
     */
    async request(url, options = {}) {
        try {
            const response = await fetch(url, {
                ...options,
                credentials: 'same-origin',  // Include cookies in requests for same-origin
                headers: {
                    'Content-Type': 'application/json', // Set request content type to JSON
                    ...options.headers
                }
            });
            
            const data = await response.json(); // Parse response as JSON
            return { success: response.ok, data, status: response.status }; // Return result object
        } catch (error) {
            console.error('API request failed:', error); // Log any errors
            return { success: false, error: error.message }; // Return error object
        }
    },

    /**
     * Convenience method for GET requests.
     * @param {string} url - The endpoint URL.
     * @returns {Promise<object>} - The result of the request.
     */
    async get(url) {
        return this.request(url, { method: 'GET' });
    },

    /**
     * Convenience method for POST requests.
     * @param {string} url - The endpoint URL.
     * @param {object} body - The request payload.
     * @returns {Promise<object>} - The result of the request.
     */
    async post(url, body) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(body) // Convert body to JSON string
        });
    },

    /**
     * Convenience method for DELETE requests.
     * @param {string} url - The endpoint URL.
     * @returns {Promise<object>} - The result of the request.
     */
    async delete(url) {
        return this.request(url, { method: 'DELETE' });
    }
};

export default API;
