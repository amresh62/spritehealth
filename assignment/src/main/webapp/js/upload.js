// Upload functionality
import UI from './ui.js';

let selectedFile = null;

// Initialize upload functionality
document.addEventListener('DOMContentLoaded', () => {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    const uploadBtn = document.getElementById('uploadBtn');
    const cancelBtn = document.getElementById('cancelBtn');

    // Click to browse
    uploadArea.addEventListener('click', () => {
        fileInput.click();
    });

    // File selection
    fileInput.addEventListener('change', (e) => {
        handleFileSelect(e.target.files[0]);
    });

    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragging');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragging');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragging');
        
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileSelect(files[0]);
        }
    });

    // Upload button
    uploadBtn.addEventListener('click', uploadFile);

    // Cancel button
    cancelBtn.addEventListener('click', () => {
        selectedFile = null;
        document.getElementById('previewSection').style.display = 'none';
        document.getElementById('uploadArea').style.display = 'block';
        fileInput.value = '';
    });
});

function handleFileSelect(file) {
    if (!file) return;

    // Validate file type
    const validTypes = [
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-excel'
    ];

    if (!validTypes.includes(file.type) && !file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
        UI.showMessage('message', 'Please select a valid Excel file (.xlsx or .xls)', 'error');
        return;
    }

    // Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
        UI.showMessage('message', 'File size must be less than 10MB', 'error');
        return;
    }

    selectedFile = file;
    document.getElementById('fileName').textContent = `Selected: ${file.name} (${formatFileSize(file.size)})`;
    document.getElementById('uploadArea').style.display = 'none';
    document.getElementById('previewSection').style.display = 'block';
}

async function uploadFile() {
    if (!selectedFile) {
        UI.showMessage('message', 'Please select a file first', 'error');
        return;
    }

    const uploadBtn = document.getElementById('uploadBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    
    uploadBtn.disabled = true;
    cancelBtn.disabled = true;
    UI.showSpinner('spinner');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
        const response = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();
        UI.hideSpinner('spinner');

        if (result.success) {
            UI.showMessage('message', result.message, 'success');
            document.getElementById('previewSection').style.display = 'none';
            document.getElementById('resultSection').style.display = 'block';
            document.getElementById('uploadCount').textContent = result.count;
        } else {
            UI.showMessage('message', result.message || 'Upload failed', 'error');
            uploadBtn.disabled = false;
            cancelBtn.disabled = false;
        }
    } catch (error) {
        UI.hideSpinner('spinner');
        UI.showMessage('message', 'Error uploading file: ' + error.message, 'error');
        uploadBtn.disabled = false;
        cancelBtn.disabled = false;
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}
