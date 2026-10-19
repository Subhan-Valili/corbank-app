// login.html və register.html üçün ortaq köməkçilər (common.js-dən sonra yüklənir).
const AuthUi = {
  // yalnız tətbiqin öz səhifələrinə qayıt (open-redirect olmasın)
  nextUrl() {
    const n = new URLSearchParams(location.search).get('next') || '';
    return /^[a-z0-9_-]+\.html(\?[^#\s]*)?(#\S*)?$/i.test(n) ? n : 'index.html';
  },
  // artıq login olubsa, auth səhifəsində qalmasın
  redirectIfLoggedIn() {
    if (Auth.isLoggedIn()) { location.replace(this.nextUrl()); return true; }
    return false;
  },
  // Login/qeydiyyatdan sonra: heç bir bank qoşulmayıbsa "Bankları qoş" ekranına, qoşulubsa əsas səhifəyə.
  async afterLogin() {
    const next = this.nextUrl();
    try {
      const res = await Api._fetch('/bank-connections');
      if (res.ok) {
        const banks = await res.json();
        if (!banks.some(b => b.connected)) return 'banklar.html?next=' + encodeURIComponent(next);
      }
    } catch (e) { /* yoxlama alınmasa adi yola davam */ }
    return next;
  },
  eyeButtons() {
    document.querySelectorAll('.au-eye').forEach(btn => {
      btn.innerHTML = Icons.eyeOff;
      btn.addEventListener('click', () => {
        const input = document.getElementById(btn.dataset.toggle || 'password');
        const show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        btn.setAttribute('aria-pressed', show ? 'true' : 'false');
        btn.setAttribute('aria-label', show ? 'Şifrəni gizlət' : 'Şifrəni göstər');
      });
    });
  },
  setFieldError(name, message) {
    const f = document.querySelector(`[data-field="${name}"]`);
    if (!f) return;
    f.classList.toggle('invalid', !!message);
    const err = f.querySelector('[data-error]');
    if (err) err.textContent = message || '';
    const input = f.querySelector('input');
    if (input) input.setAttribute('aria-invalid', message ? 'true' : 'false');
  },
  clearFieldErrors(root) {
    (root || document).querySelectorAll('[data-field]').forEach(f => this.setFieldError(f.dataset.field, ''));
  },
  showAlert(id, message, kind) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = message || '';
    el.classList.toggle('show', !!message);
    el.classList.toggle('info', kind === 'info');
  },
  busy(btn, on, busyText) {
    if (on) { btn.dataset.label = btn.textContent; btn.textContent = busyText; btn.disabled = true; }
    else { btn.textContent = btn.dataset.label || btn.textContent; btn.disabled = false; }
  }
};
