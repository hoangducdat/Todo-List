const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get('email') || localStorage.getItem('pendingVerificationEmail');

    if (!email) {
        window.location.href = 'register.html';
        return;
    }

    document.getElementById('email').value = email;
});

document.getElementById('verify-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const otp = document.getElementById('otp').value;

    if (otp.length !== 6 || !/^\d{6}$/.test(otp)) {
        showMessage('Please enter a valid 6-digit verification code', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/verify`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                otp: otp
            })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            localStorage.setItem('email', data.email);

            localStorage.removeItem('pendingVerificationEmail');

            showMessage('Account verified successfully! Redirecting...', 'success');

            setTimeout(() => {
                window.location.href = 'index.html';
            }, 2000);
        } else {
            showMessage(data.message || 'Verification failed', 'error');
        }
    } catch (error) {
        console.error('Verification error:', error);
        showMessage('Network error. Please try again.', 'error');
    }
});

document.getElementById('resend-btn').addEventListener('click', async function() {
    const email = document.getElementById('email').value;

    try {
        this.disabled = true;
        this.textContent = 'Sending...';

        const response = await fetch(`${API_BASE_URL}/auth/resend-verification?email=${encodeURIComponent(email)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        const data = await response.text();

        if (response.ok) {
            showMessage('Verification code resent successfully!', 'success');
            startResendCooldown();
        } else {
            showMessage(data || 'Failed to resend code', 'error');
            this.disabled = false;
            this.textContent = 'Resend Code';
        }
    } catch (error) {
        console.error('Resend error:', error);
        showMessage('Network error. Please try again.', 'error');
        this.disabled = false;
        this.textContent = 'Resend Code';
    }
});

function startResendCooldown() {
    const resendBtn = document.getElementById('resend-btn');
    let seconds = 60;

    resendBtn.disabled = true;

    const interval = setInterval(() => {
        resendBtn.textContent = `Resend Code (${seconds}s)`;
        seconds--;

        if (seconds < 0) {
            clearInterval(interval);
            resendBtn.disabled = false;
            resendBtn.textContent = 'Resend Code';
        }
    }, 1000);
}

document.getElementById('otp').addEventListener('input', function(e) {
    this.value = this.value.replace(/\D/g, '');

    if (this.value.length === 6) {
        document.getElementById('verify-form').dispatchEvent(new Event('submit'));
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