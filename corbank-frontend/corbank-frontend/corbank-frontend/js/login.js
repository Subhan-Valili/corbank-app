// Giriş səhifəsi — common.js (Auth) və auth-ui.js istifadə edir.
if (!AuthUi.redirectIfLoggedIn()) initLogin();

function initLogin() {
  AuthUi.eyeButtons();

  // login-dən sonra qayıdılacaq səhifəni qeydiyyata da ötür
  const next = new URLSearchParams(location.search).get('next');
  if (next) document.getElementById('toRegister').href = 'register.html?next=' + encodeURIComponent(next);

  const form = document.getElementById('loginForm');
  const btn = document.getElementById('submitBtn');
  const email = document.getElementById('email');
  const password = document.getElementById('password');

  if (new URLSearchParams(location.search).get('registered')) {
    AuthUi.showAlert('alert', 'Qeydiyyat tamamlandı. İndi daxil ola bilərsiniz.', 'info');
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    AuthUi.clearFieldErrors(form);
    AuthUi.showAlert('alert', '');

    const emailVal = email.value.trim().toLowerCase();
    let ok = true;
    if (!emailVal) { AuthUi.setFieldError('email', 'Email daxil edin'); ok = false; }
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailVal)) { AuthUi.setFieldError('email', 'Email düzgün deyil'); ok = false; }
    if (!password.value) { AuthUi.setFieldError('password', 'Şifrə daxil edin'); ok = false; }
    if (!ok) { (form.querySelector('.invalid input') || email).focus(); return; }

    AuthUi.busy(btn, true, 'Daxil olunur...');
    try {
      await Auth.login(emailVal, password.value);
      location.replace(await AuthUi.afterLogin());
    } catch (err) {
      AuthUi.showAlert('alert', err.message);
      password.value = '';
      password.focus();
      AuthUi.busy(btn, false);
    }
  });
}
