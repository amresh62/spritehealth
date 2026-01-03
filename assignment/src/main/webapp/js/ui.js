// UI utility functions
const UI = {
    /**
     * Displays a message in the specified element with a given type (info, error, etc.).
     * The message will automatically disappear after 5 seconds.
     * @param {string} elementId - The ID of the element to display the message in.
     * @param {string} message - The message to display.
     * @param {string} [type='info'] - The type of message (affects styling).
     */
    showMessage(elementId, message, type = 'info') {
        const messageEl = document.getElementById(elementId);
        if (!messageEl) return;
        
        messageEl.textContent = message;
        messageEl.className = `message ${type} show`;
        
        setTimeout(() => {
            messageEl.className = 'message';
        }, 5000);
    },

    /**
     * Shows a spinner (loading indicator) by adding the 'show' class to the element.
     * @param {string} elementId - The ID of the spinner element.
     */
    showSpinner(elementId) {
        const spinner = document.getElementById(elementId);
        if (spinner) {
            spinner.classList.add('show');
        }
    },

    /**
     * Hides a spinner (loading indicator) by removing the 'show' class from the element.
     * @param {string} elementId - The ID of the spinner element.
     */
    hideSpinner(elementId) {
        const spinner = document.getElementById(elementId);
        if (spinner) {
            spinner.classList.remove('show');
        }
    },

    /**
     * Formats a date string into a human-readable format (e.g., 'Jan 1, 2024').
     * Returns 'N/A' if the input is falsy or invalid.
     * @param {string} dateString - The date string to format.
     * @returns {string} - The formatted date or 'N/A'.
     */
    formatDate(dateString) {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch {
            return dateString;
        }
    },

    /**
     * Escapes HTML special characters in a string to prevent XSS attacks.
     * @param {string} text - The text to escape.
     * @returns {string} - The escaped string.
     */
    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return String(text).replace(/[&<>"']/g, m => map[m]);
    }
};

export default UI;
