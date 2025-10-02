const API_BASE_URL = 'http://localhost:8080/api';

document.getElementById('forgot-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('email').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/forgot-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email
            })
        });

        const data = await response.text();

        if (response.ok) {
            showMessage('Reset code sent to your email!', 'success');

            localStorage.setItem('resetPasswordEmail', email);

            setTimeout(() => {
                window.location.href = `reset-password.html?email=${encodeURIComponent(email)}`;
            }, 2000);
        } else {
            showMessage(data || 'Failed to send reset code', 'error');
        }
    } catch (error) {
        console.error('Forgot password error:', error);
        showMessage('Network error. Please try again.', 'error');
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