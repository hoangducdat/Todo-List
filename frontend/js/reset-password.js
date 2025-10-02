const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get('email') || localStorage.getItem('resetPasswordEmail');

    if (!email) {
        window.location.href = 'forgot-password.html';
        return;
    }

    document.getElementById('email').value = email;
});

document.getElementById('reset-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const otp = document.getElementById('otp').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (otp.length !== 6 || !/^\d{6}$/.test(otp)) {
        showMessage('Please enter a valid 6-digit verification code', 'error');
        return;
    }

    if (newPassword.length < 6) {
        showMessage('Password must be at least 6 characters long', 'error');
        return;
    }

    if (newPassword !== confirmPassword) {
        showMessage('Passwords do not match', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                otp: otp,
                newPassword: newPassword
            })
        });

        const data = await response.text();

        if (response.ok) {
            showMessage('Password reset successfully! Redirecting to login...', 'success');

            localStorage.removeItem('resetPasswordEmail');

            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        } else {
            showMessage(data || 'Password reset failed', 'error');
        }
    } catch (error) {
        console.error('Reset password error:', error);
        showMessage('Network error. Please try again.', 'error');
    }
});

document.getElementById('otp').addEventListener('input', function(e) {
    this.value = this.value.replace(/\D/g, '');
});

document.getElementById('confirm-password').addEventListener('input', function() {
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = this.value;

    if (confirmPassword && newPassword !== confirmPassword) {
        this.setCustomValidity('Passwords do not match');
    } else {
        this.setCustomValidity('');
    }
});

function showMessage(message, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = message;
    messageDiv.className = `message ${type === 'error' ? 'error-message' : 'success-message'}`;
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}