// UI utility functions
const UI = {
    showMessage(elementId, message, type = 'info') {
        const messageEl = document.getElementById(elementId);
        if (!messageEl) return;
        
        messageEl.textContent = message;
        messageEl.className = `message ${type} show`;
        
        setTimeout(() => {
            messageEl.className = 'message';
        }, 5000);
    },

    showSpinner(elementId) {
        const spinner = document.getElementById(elementId);
        if (spinner) {
            spinner.classList.add('show');
        }
    },

    hideSpinner(elementId) {
        const spinner = document.getElementById(elementId);
        if (spinner) {
            spinner.classList.remove('show');
        }
    },

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
