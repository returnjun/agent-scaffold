const loginForm = document.querySelector('#login-form');
const usernameInput = document.querySelector('#username');
const passwordInput = document.querySelector('#password');
const passwordToggle = document.querySelector('.password-toggle');
const loginMessage = document.querySelector('#login-message');
const submitButton = loginForm.querySelector('.submit-button');

passwordToggle.addEventListener('click', () => {
    const shouldShow = passwordInput.type === 'password';
    passwordInput.type = shouldShow ? 'text' : 'password';
    passwordToggle.setAttribute('aria-pressed', String(shouldShow));
    passwordToggle.setAttribute('aria-label', shouldShow ? '隐藏密码' : '显示密码');
    passwordInput.focus();
});

loginForm.addEventListener('submit', (event) => {
    event.preventDefault();

    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    loginForm.classList.remove('is-error');
    loginMessage.textContent = '';

    if (!username || !password) {
        showError('请输入账号和密码');
        (!username ? usernameInput : passwordInput).focus();
        return;
    }

    if (username !== 'admin' || password !== 'admin') {
        showError('账号或密码错误，请使用 admin / admin');
        passwordInput.select();
        return;
    }

    sessionStorage.setItem('agentConsoleUser', JSON.stringify({
        userId: username,
        signedInAt: Date.now()
    }));

    submitButton.disabled = true;
    submitButton.querySelector('span').textContent = '正在进入…';
    window.location.replace('./index.html');
});

function showError(message) {
    loginMessage.textContent = message;
    void loginForm.offsetWidth;
    loginForm.classList.add('is-error');
}
