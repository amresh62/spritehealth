// API utility functions
const API = {
    async request(url, options = {}) {
        try {
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            });
            
            const data = await response.json();
            return { success: response.ok, data, status: response.status };
        } catch (error) {
            console.error('API request failed:', error);
            return { success: false, error: error.message };
        }
    },

    async get(url) {
        return this.request(url, { method: 'GET' });
    },

    async post(url, body) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    async delete(url) {
        return this.request(url, { method: 'DELETE' });
    }
};

export default API;
