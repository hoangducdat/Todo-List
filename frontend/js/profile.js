const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    loadUserProfile();
    setupEventListeners();
});

async function loadUserProfile() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const profile = await response.json();
            populateProfileForm(profile);
            updateWelcomeMessage(profile.username);
        } else {
            showMessage('Failed to load profile', 'error');
            if (response.status === 401) {
                localStorage.clear();
                window.location.href = 'login.html';
            }
        }
    } catch (error) {
        console.error('Load profile error:', error);
        showMessage('Network error. Please try again.', 'error');
    }
}

function populateProfileForm(profile) {
    document.getElementById('username').value = profile.username || '';
    document.getElementById('email').value = profile.email || '';
    document.getElementById('firstName').value = profile.firstName || '';
    document.getElementById('lastName').value = profile.lastName || '';
    document.getElementById('phone').value = profile.phone || '';

    const accountStatus = document.getElementById('account-status');
    const verificationStatus = document.getElementById('verification-status');

    accountStatus.textContent = profile.isActive ? 'Active' : 'Inactive';
    accountStatus.className = `status-badge ${profile.isActive ? 'status-active' : 'status-inactive'}`;

    verificationStatus.textContent = profile.isVerified ? 'Verified' : 'Not Verified';
    verificationStatus.className = `status-badge ${profile.isVerified ? 'status-verified' : 'status-unverified'}`;
}

function updateWelcomeMessage(username) {
    const welcomeMsg = document.getElementById('welcome-message');
    welcomeMsg.textContent = `Welcome, ${username}`;
}

function setupEventListeners() {
    document.getElementById('profile-form').addEventListener('submit', handleProfileUpdate);

    document.getElementById('password-form').addEventListener('submit', handlePasswordChange);

    document.getElementById('confirmPassword').addEventListener('input', validatePasswordConfirmation);

    document.getElementById('delete-account-btn').addEventListener('click', showDeleteModal);
    document.getElementById('cancel-delete').addEventListener('click', hideDeleteModal);
    document.getElementById('confirm-delete').addEventListener('click', handleAccountDeletion);

    document.getElementById('logout-btn').addEventListener('click', handleLogout);
}

async function handleProfileUpdate(e) {
    e.preventDefault();

    const formData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        phone: document.getElementById('phone').value
    };

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const message = await response.text();

        if (response.ok) {
            showMessage(message, 'success');
            if (formData.username !== localStorage.getItem('username')) {
                localStorage.setItem('username', formData.username);
            }
            setTimeout(() => {
                loadUserProfile();
            }, 1000);
        } else {
            showMessage(message, 'error');
        }
    } catch (error) {
        console.error('Profile update error:', error);
        showMessage('Network error. Please try again.', 'error');
    }
}

async function handlePasswordChange(e) {
    e.preventDefault();

    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showMessage('New passwords do not match', 'error');
        return;
    }

    const formData = {
        currentPassword,
        newPassword,
        confirmPassword
    };

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/profile/change-password`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const message = await response.text();

        if (response.ok) {
            showMessage(message, 'success');
            document.getElementById('password-form').reset();
            setTimeout(() => {
                localStorage.clear();
                window.location.href = 'login.html';
            }, 2000);
        } else {
            showMessage(message, 'error');
        }
    } catch (error) {
        console.error('Password change error:', error);
        showMessage('Network error. Please try again.', 'error');
    }
}

function validatePasswordConfirmation() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (confirmPassword && newPassword !== confirmPassword) {
        document.getElementById('confirmPassword').setCustomValidity('Passwords do not match');
    } else {
        document.getElementById('confirmPassword').setCustomValidity('');
    }
}

function showDeleteModal() {
    document.getElementById('delete-modal').style.display = 'block';
}

function hideDeleteModal() {
    document.getElementById('delete-modal').style.display = 'none';
}

async function handleAccountDeletion() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const message = await response.text();

        if (response.ok) {
            showMessage(message, 'success');
            setTimeout(() => {
                localStorage.clear();
                window.location.href = 'login.html';
            }, 2000);
        } else {
            showMessage(message, 'error');
        }
    } catch (error) {
        console.error('Account deletion error:', error);
        showMessage('Network error. Please try again.', 'error');
    } finally {
        hideDeleteModal();
    }
}

async function handleLogout() {
    try {
        const token = localStorage.getItem('token');
        await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
    } catch (error) {
        console.error('Logout error:', error);
    } finally {
        localStorage.clear();
        window.location.href = 'login.html';
    }
}

function showMessage(message, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = message;
    messageDiv.className = `message ${type === 'error' ? 'error-message' : 'success-message'}`;
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}