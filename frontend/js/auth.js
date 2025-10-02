const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (token && (window.location.pathname === '/login.html' || window.location.pathname === '/register.html')) {
        window.location.href = 'index.html';
    }
});

if (document.getElementById('login-form')) {
    document.getElementById('login-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    usernameOrEmail: username,
                    password: password
                })
            });

            const data = await response.json();

            if (response.ok) {
                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username);
                localStorage.setItem('email', data.email);

                window.location.href = 'index.html';
            } else {
                const message = data.message || 'Login failed';
                showError(message);

                if (message.includes('not verified')) {
                    const email = username.includes('@') ? username : null;
                    if (email) {
                        showVerificationOption(email);
                    }
                }
            }
        } catch (error) {
            console.error('Login error:', error);
            showError('Network error. Please try again.');
        }
    });
}

if (document.getElementById('register-form')) {
    document.getElementById('register-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirm-password').value;

        if (password !== confirmPassword) {
            showError('Passwords do not match');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    email: email,
                    password: password
                })
            });

            if (response.ok) {
                const data = await response.text();
                localStorage.setItem('pendingVerificationEmail', email);
                showSuccess(data);

                setTimeout(() => {
                    window.location.href = `verify.html?email=${encodeURIComponent(email)}`;
                }, 2000);
            } else {
                try {
                    const data = await response.json();
                    if (data.errors) {
                        const errorMessages = Object.values(data.errors).join(', ');
                        showError(errorMessages);
                    } else {
                        showError(data.message || 'Registration failed');
                    }
                } catch {
                    const data = await response.text();
                    showError(data || 'Registration failed');
                }
            }
        } catch (error) {
            console.error('Registration error:', error);
            showError('Network error. Please try again.');
        }
    });
}

function showError(message) {
    const errorDiv = document.getElementById('error-message');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';

        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }
}

function showSuccess(message) {
    const errorDiv = document.getElementById('error-message');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.className = 'success-message';
        errorDiv.style.display = 'block';

        setTimeout(() => {
            errorDiv.style.display = 'none';
            errorDiv.className = 'error-message';
        }, 3000);
    }
}

function showVerificationOption(email) {
    const errorDiv = document.getElementById('error-message');
    if (errorDiv) {
        errorDiv.innerHTML = `
            Account not verified. Please check your email for verification code.<br>
            <button onclick="redirectToVerification('${email}')" class="btn btn-secondary" style="margin-top: 10px;">
                Go to Verification
            </button>
        `;
        errorDiv.className = 'error-message';
        errorDiv.style.display = 'block';
    }
}

function redirectToVerification(email) {
    window.location.href = `verify.html?email=${encodeURIComponent(email)}`;
}