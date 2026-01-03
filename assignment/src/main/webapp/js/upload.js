// Upload functionality
import UI from './ui.js';

let selectedFile = null; // Holds the currently selected file

// Initialize upload functionality when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    const uploadBtn = document.getElementById('uploadBtn');
    const cancelBtn = document.getElementById('cancelBtn');

    // Allow clicking the upload area to open file dialog
    uploadArea.addEventListener('click', () => {
        fileInput.click();
    });

    // Handle file selection via file dialog
    fileInput.addEventListener('change', (e) => {
        handleFileSelect(e.target.files[0]);
    });

    // Highlight upload area on drag over
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragging');
    });

    // Remove highlight when drag leaves upload area
    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragging');
    });

    // Handle file drop into upload area
    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragging');
        
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileSelect(files[0]);
        }
    });

    // Start upload when upload button is clicked
    uploadBtn.addEventListener('click', uploadFile);

    // Cancel upload and reset UI when cancel button is clicked
    cancelBtn.addEventListener('click', () => {
        selectedFile = null;
        document.getElementById('previewSection').style.display = 'none';
        document.getElementById('uploadArea').style.display = 'block';
        fileInput.value = '';
    });
});

/**
 * Handles file selection and validation.
 * @param {File} file - The selected file.
 */
function handleFileSelect(file) {
    if (!file) return;

    // Allowed MIME types for Excel files
    const validTypes = [
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-excel'
    ];

    // Validate file type by MIME or extension
    if (!validTypes.includes(file.type) && !file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
        UI.showMessage('message', 'Please select a valid Excel file (.xlsx or .xls)', 'error');
        return;
    }

    // Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
        UI.showMessage('message', 'File size must be less than 10MB', 'error');
        return;
    }

    // Store selected file and update UI
    selectedFile = file;
    document.getElementById('fileName').textContent = `Selected: ${file.name} (${formatFileSize(file.size)})`;
    document.getElementById('uploadArea').style.display = 'none';
    document.getElementById('previewSection').style.display = 'block';
}

/**
 * Uploads the selected file to the server.
 */
async function uploadFile() {
    if (!selectedFile) {
        UI.showMessage('message', 'Please select a file first', 'error');
        return;
    }

    const uploadBtn = document.getElementById('uploadBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    
    // Disable buttons and show spinner during upload
    uploadBtn.disabled = true;
    cancelBtn.disabled = true;
    UI.showSpinner('spinner');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
        // Send file to server via POST request
        const response = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();
        UI.hideSpinner('spinner');

        if (result.success) {
            // Show success message and update UI with result
            UI.showMessage('message', result.message, 'success');
            document.getElementById('previewSection').style.display = 'none';
            document.getElementById('resultSection').style.display = 'block';
            document.getElementById('uploadCount').textContent = result.count;
        } else {
            // Show error message and re-enable buttons
            UI.showMessage('message', result.message || 'Upload failed', 'error');
            uploadBtn.disabled = false;
            cancelBtn.disabled = false;
        }
    } catch (error) {
        // Handle network or server errors
        UI.hideSpinner('spinner');
        UI.showMessage('message', 'Error uploading file: ' + error.message, 'error');
        uploadBtn.disabled = false;
        cancelBtn.disabled = false;
    }
}

/**
 * Formats file size in bytes to a human-readable string.
 * @param {number} bytes - File size in bytes.
 * @returns {string} - Formatted file size.
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}
