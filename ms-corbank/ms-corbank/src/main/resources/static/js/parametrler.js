// Parametrlər page — uses helpers from common.js (Prefs, renderSidebar, renderTopbar, showToast)

document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('parametrler.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());

const prefs = Prefs.get();

const nameInput = document.getElementById('prName');
const emailInput = document.getElementById('prEmail');
const hideAmountsSwitch = document.getElementById('hideAmountsSwitch');
const notifDotSwitch = document.getElementById('notifDotSwitch');
const saveBtn = document.getElementById('saveBtn');

nameInput.value = prefs.name;
emailInput.value = prefs.email;
setSwitch(hideAmountsSwitch, prefs.hideAmounts);
setSwitch(notifDotSwitch, prefs.notifDot);

function setSwitch(btn, on) {
  btn.classList.toggle('on', !!on);
  btn.setAttribute('aria-checked', on ? 'true' : 'false');
}

function isOn(btn) {
  return btn.getAttribute('aria-checked') === 'true';
}

hideAmountsSwitch.addEventListener('click', () => {
  setSwitch(hideAmountsSwitch, !isOn(hideAmountsSwitch));
});

notifDotSwitch.addEventListener('click', () => {
  setSwitch(notifDotSwitch, !isOn(notifDotSwitch));
});

saveBtn.addEventListener('click', () => {
  const next = Prefs.set({
    name: nameInput.value.trim() || Prefs.defaults.name,
    email: emailInput.value.trim() || Prefs.defaults.email,
    hideAmounts: isOn(hideAmountsSwitch),
    notifDot: isOn(notifDotSwitch),
  });

  // Cari səhifədə dərhal əks etdir (avatar hərfləri, gizlətmə effekti)
  applyAmountVisibility();
  document.querySelector('.topbar-avatar').textContent = initialsFrom(next.name);
  const dot = document.querySelector('.topbar-bell-dot');
  if (next.notifDot && !dot) {
    document.querySelector('.topbar-bell').insertAdjacentHTML('beforeend', '<span class="topbar-bell-dot"></span>');
  } else if (!next.notifDot && dot) {
    dot.remove();
  }

  showToast('Dəyişikliklər saxlanıldı', 'success');
});
