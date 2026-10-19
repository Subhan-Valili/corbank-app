// Qeydiyyat səhifəsi: 1) məlumatlar -> 2) emailə gələn OTP -> avtomatik giriş.
if (!AuthUi.redirectIfLoggedIn()) initRegister();

function initRegister() {
  AuthUi.eyeButtons();

  const RESEND_SECONDS = 60;   // backend cooldown: 1 dəqiqə
  const stepForm = document.getElementById('stepForm');
  const stepOtp = document.getElementById('stepOtp');
  const form = document.getElementById('registerForm');
  const otpForm = document.getElementById('otpForm');
  const submitBtn = document.getElementById('submitBtn');
  const otpBtn = document.getElementById('otpBtn');
  const resendBtn = document.getElementById('resendBtn');
  const otpInput = document.getElementById('otp');
  const val = id => document.getElementById(id).value;

  let sessionId = null;
  let timer = null;

  // ---------- 1-ci addım ----------
  function validate() {
    AuthUi.clearFieldErrors(form);
    let ok = true;
    const bad = (f, m) => { AuthUi.setFieldError(f, m); ok = false; };

    if (!val('name').trim()) bad('name', 'Adınızı daxil edin');
    if (!val('surname').trim()) bad('surname', 'Soyadınızı daxil edin');

    const email = val('email').trim();
    if (!email) bad('email', 'Email daxil edin');
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) bad('email', 'Email düzgün deyil');

    const mobile = val('mobileNumber').trim().replace(/[\s()-]/g, '');
    if (!mobile) bad('mobileNumber', 'Mobil nömrə daxil edin');
    else if (!/^\+?\d{9,15}$/.test(mobile)) bad('mobileNumber', 'Nömrə düzgün deyil, məs: +994501112233');

    if (!val('pinCode').trim()) bad('pinCode', 'PIN kodu daxil edin');

    const pwd = val('password');
    if (pwd.length < 8 || pwd.length > 64) bad('password', 'Şifrə 8–64 simvol olmalıdır');
    if (val('password2') !== pwd) bad('password2', 'Şifrələr eyni deyil');
    return ok;
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    AuthUi.showAlert('alert', '');
    if (!validate()) { form.querySelector('.invalid input').focus(); return; }

    AuthUi.busy(submitBtn, true, 'Yoxlanılır...');
    try {
      const res = await Auth.call('/v1/auth/register', {
        name: val('name').trim(),
        surname: val('surname').trim(),
        email: val('email').trim().toLowerCase(),
        password: val('password'),
        mobileNumber: val('mobileNumber').trim().replace(/[\s()-]/g, ''),
        pinCode: val('pinCode').trim()
      });
      sessionId = res.signUpSessionId;
      showOtpStep();
    } catch (err) {
      // PIN xətası PIN sahəsində, qalanı üst xəbərdarlıqda göstərilir
      if (err.status === 422) AuthUi.setFieldError('pinCode', err.message);
      else AuthUi.showAlert('alert', err.message);
      if (err.status === 422) document.getElementById('pinCode').focus();
    } finally {
      AuthUi.busy(submitBtn, false);
    }
  });

  // ---------- 2-ci addım ----------
  function showOtpStep() {
    document.getElementById('sentTo').textContent = val('email').trim().toLowerCase();
    stepForm.hidden = true;
    stepOtp.hidden = false;
    otpInput.value = '';
    AuthUi.showAlert('otpAlert', '');
    startCooldown();
    otpInput.focus();
  }

  function startCooldown() {
    let left = RESEND_SECONDS;
    clearInterval(timer);
    const tick = () => {
      if (left > 0) { resendBtn.disabled = true; resendBtn.textContent = `Yenidən göndər (${left} san)`; left--; }
      else { clearInterval(timer); resendBtn.disabled = false; resendBtn.textContent = 'Kodu yenidən göndər'; }
    };
    tick();
    timer = setInterval(tick, 1000);
  }

  otpInput.addEventListener('input', () => { otpInput.value = otpInput.value.replace(/\D/g, '').slice(0, 6); });

  resendBtn.addEventListener('click', async () => {
    resendBtn.disabled = true;
    AuthUi.showAlert('otpAlert', '');
    try {
      await Auth.call('/v1/auth/resend-otp', { signUpSessionId: sessionId });
      AuthUi.showAlert('otpAlert', 'Yeni kod göndərildi.', 'info');
      startCooldown();
    } catch (err) {
      AuthUi.showAlert('otpAlert', err.message);
      // sessiya bitibsə, məlumatlara qayıt
      if (/sessiya/i.test(err.message)) backToForm();
      else resendBtn.disabled = false;
    }
  });

  function backToForm() {
    clearInterval(timer);
    sessionId = null;
    stepOtp.hidden = true;
    stepForm.hidden = false;
    AuthUi.showAlert('alert', 'Qeydiyyat sessiyasının vaxtı bitdi. Məlumatları yenidən göndərin.');
  }
  document.getElementById('backBtn').addEventListener('click', () => {
    clearInterval(timer);
    sessionId = null;
    stepOtp.hidden = true;
    stepForm.hidden = false;
    AuthUi.showAlert('alert', '');
  });

  otpForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    AuthUi.clearFieldErrors(otpForm);
    AuthUi.showAlert('otpAlert', '');
    const otp = otpInput.value;
    if (!/^\d{6}$/.test(otp)) { AuthUi.setFieldError('otp', 'Kod 6 rəqəmdən ibarət olmalıdır'); otpInput.focus(); return; }

    AuthUi.busy(otpBtn, true, 'Təsdiqlənir...');
    try {
      await Auth.call('/v1/auth/verify', { signUpSessionId: sessionId, otp });
    } catch (err) {
      AuthUi.showAlert('otpAlert', err.message);
      otpInput.select();
      AuthUi.busy(otpBtn, false);
      return;
    }

    // Qeydiyyat tamamdır — eyni məlumatlarla avtomatik giriş
    try {
      await Auth.login(val('email').trim().toLowerCase(), val('password'));
      location.replace(await AuthUi.afterLogin());
    } catch (err) {
      location.replace('login.html?registered=1');
    }
  });
}
