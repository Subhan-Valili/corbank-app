// Bankları qoş səhifəsi: banklar siyahısı + hər biri üçün Open API Key sahəsi + Finish.
// Backend: GET/POST /api/bank-connections, DELETE /api/bank-connections/{BANK}
const LOGOS = { PASHA: 'img/bank-pasha.svg', ABB: 'img/bank-abb.svg' };
// Backend-də hələ olmayan, amma planlanan banklar — yalnız göstərmək üçün (qoşmaq olmur)
const COMING_SOON = [{ code: 'KAPITAL', name: 'Kapital Bank' }];

const listEl = document.getElementById('bankList');
const formEl = document.getElementById('bankForm');
const finishBtn = document.getElementById('finishBtn');
const loadingEl = document.getElementById('loading');
const nextUrl = AuthUi.nextUrl();
let banks = [];

document.getElementById('skipLink').href = nextUrl;

async function apiJson(path, opts) {
  const res = await Api._fetch(path, opts);
  let data = null;
  try { data = await res.json(); } catch (e) { /* boş cavab */ }
  if (!res.ok) {
    throw new ApiError((data && data.message) || Auth.fallbackMessage(res.status, path), res.status, data && data.message);
  }
  return data;
}

function logoHtml(b) {
  return LOGOS[b.code]
    ? `<img class="bk-logo" src="${LOGOS[b.code]}" alt="">`
    : `<span class="bk-badge-letter">${escHtml(b.name.charAt(0))}</span>`;
}

function itemHtml(b) {
  if (b.soon) {
    return `<div class="bk-item is-disabled">
      <div class="bk-top">${logoHtml(b)}<span class="bk-name">${escHtml(b.name)}</span><span class="bk-chip">Tezliklə</span></div>
    </div>`;
  }
  if (b.connected) {
    return `<div class="bk-item is-connected" data-bank="${b.code}">
      <div class="bk-top">${logoHtml(b)}<span class="bk-name">${escHtml(b.name)}</span>
        <span class="bk-chip ok">Qoşulub</span>
        <button type="button" class="bk-unlink" data-unlink="${b.code}">Ayır</button></div>
    </div>`;
  }
  if (!b.apiKeySupported) {
    return `<div class="bk-item is-disabled" data-bank="${b.code}">
      <div class="bk-top">${logoHtml(b)}<span class="bk-name">${escHtml(b.name)}</span><span class="bk-chip">Tezliklə</span></div>
      <div class="bk-note">Bu bank üçün Open API Key ilə qoşulma hələ aktiv deyil.</div>
    </div>`;
  }
  return `<div class="bk-item" data-bank="${b.code}">
    <div class="bk-top">${logoHtml(b)}<span class="bk-name">${escHtml(b.name)}</span></div>
    <div class="au-field">
      <label for="key-${b.code}">Open API Key</label>
      <div class="au-input-wrap">
        <input id="key-${b.code}" data-key="${b.code}" type="password" autocomplete="off" spellcheck="false"
               placeholder="Açarı bura yapışdırın (yoxdursa boş buraxın)">
        <button class="au-eye" type="button" data-toggle="key-${b.code}" aria-label="Açarı göstər" aria-pressed="false"></button>
      </div>
    </div>
  </div>`;
}

function render() {
  const all = banks.concat(COMING_SOON.map(b => ({ ...b, soon: true })));
  listEl.innerHTML = all.map(itemHtml).join('');
  AuthUi.eyeButtons();
  const anyConnected = banks.some(b => b.connected);
  document.getElementById('title').textContent = anyConnected ? 'Qoşulmuş banklar' : 'Bankları qoşun';
}

async function load() {
  try {
    banks = await apiJson('/bank-connections');
    render();
    loadingEl.hidden = true;
    formEl.hidden = false;
  } catch (err) {
    loadingEl.hidden = true;
    AuthUi.showAlert('alert', err.message);
  }
}

formEl.addEventListener('submit', async (e) => {
  e.preventDefault();
  AuthUi.showAlert('alert', '');
  const connections = [...listEl.querySelectorAll('input[data-key]')]
    .map(i => ({ bank: i.dataset.key, apiKey: i.value.trim() }))
    .filter(c => c.apiKey);

  if (!connections.length) {
    // artıq qoşulu bank varsa, sadəcə davam et
    if (banks.some(b => b.connected)) { location.replace(nextUrl); return; }
    AuthUi.showAlert('alert', 'Ən azı bir bankın Open API Key-ini daxil edin');
    return;
  }

  AuthUi.busy(finishBtn, true, 'Bankdan məlumat çəkilir...');
  try {
    await apiJson('/bank-connections', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ connections })
    });
    try { sessionStorage.removeItem('corbank_bank'); localStorage.removeItem('corbank_bank'); } catch (x) { /* yaddaş bağlıdır */ }
    location.replace(nextUrl);
  } catch (err) {
    AuthUi.showAlert('alert', err.message);
    AuthUi.busy(finishBtn, false);
  }
});

listEl.addEventListener('click', async (e) => {
  const btn = e.target.closest('[data-unlink]');
  if (!btn) return;
  if (!confirm('Bu bankı ayırmaq istəyirsiniz? Hesabları paneldən yox olacaq.')) return;
  btn.disabled = true;
  try {
    const res = await Api._fetch('/bank-connections/' + encodeURIComponent(btn.dataset.unlink), { method: 'DELETE' });
    if (!res.ok) throw new ApiError('Bankı ayırmaq alınmadı', res.status);
    await load();
  } catch (err) {
    AuthUi.showAlert('alert', err.message);
    btn.disabled = false;
  }
});

load();
