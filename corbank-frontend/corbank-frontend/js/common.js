// Shared helpers used by both dashboard.js and account.js

// Backend base URL. When the frontend is served by the Spring Boot app itself, '/api' (relative)
// works as-is. When you open these files standalone (separate static server, file://, a different
// port, etc.), point this at wherever corbank-backend is running.
const API_BASE_URL = window.CORBANK_API_BASE_URL || 'http://localhost:8080/api';

// ---------- Şirkət seçimi (topbar: ALFA MMC / BETA MMC) ----------
// Backend şirkət anlayışını bilmir, ona görə BETA MMC demo görünüşüdür: eyni backend məlumatı
// factor-a vurulur (BETA = ALFA-nın 40%-i). Backend şirkətə görə məlumat verəndə yalnız
// aşağıdakı scale/toBackend hissəsini silmək kifayətdir.
const Company = {
  KEY: 'corbank_company',
  list: [
    { id: 'alfa', name: 'ALFA MMC', factor: 1 },
    { id: 'beta', name: 'BETA MMC', factor: 1 },   // əvvəl 0.4 idi: yazdığınız məbləğ /0.4 ilə bazaya düşürdü. Backend şirkət bilmir — məlumat eynidir.
  ],
  _mem: null,
  MONEY_KEYS: new Set(['balance', 'amount', 'income', 'expense', 'net', 'creditAmount']),
  current() {
    let id = this._mem;
    if (!id) { try { id = localStorage.getItem(this.KEY); } catch (e) { /* brauzer icazə vermir */ } }
    return this.list.find(c => c.id === id) || this.list[0];
  },
  set(id) {
    this._mem = id;
    try { localStorage.setItem(this.KEY, id); } catch (e) { /* brauzer icazə vermir */ }
  },
  round2(n) { return Math.round(n * 100) / 100; },
  // "75 000 AZN" -> "30 000 AZN" (mətn daxilindəki pul məbləğlərini vurur)
  scaleText(str, f) {
    return str.replace(/(\d+(?:[ \u00a0]\d{3})*(?:,\d+)?)(\s*)(AZN|USD|EUR)/g, (m, num, sp, cur) => {
      const n = Number(num.replace(/[ \u00a0]/g, '').replace(',', '.'));
      return Fmt.moneyShort(this.round2(n * f), cur).replace(/ (AZN|USD|EUR)$/, sp + cur);
    });
  },
  scale(node, f) {
    if (Array.isArray(node)) return node.map(n => this.scale(n, f));
    if (node && typeof node === 'object') {
      const out = {};
      for (const [k, v] of Object.entries(node)) {
        if (typeof v === 'number' && this.MONEY_KEYS.has(k)) out[k] = this.round2(v * f);
        else if (typeof v === 'string' && k === 'value') out[k] = this.scaleText(v, f);          // layihə təfərrüatları
        else if (typeof v === 'string' && k === 'meta') out[k] = v.replace(/\d+/, n => Math.max(1, Math.round(Number(n) * f))); // "24 əməkdaş"
        else out[k] = this.scale(v, f);
      }
      return out;
    }
    return node;
  },
  toBackend(path, body) {
    const c = this.current();
    if (c.factor === 1 || !/^\/payments/.test(path)) return body;
    const b = { ...body };
    ['amount', 'creditAmount'].forEach(k => { if (typeof b[k] === 'number') b[k] = this.round2(b[k] / c.factor); });
    return b;
  }
};

const Api = {
  base: API_BASE_URL,
  async get(path) {
    const res = await fetch(this.base + path);
    if (!res.ok) throw new Error('Request failed: ' + path + ' (HTTP ' + res.status + ')');
    const json = await res.json();
    const c = Company.current();
    return (c.factor !== 1 && /^\/(dashboard|accounts|projects)/.test(path)) ? Company.scale(json, c.factor) : json;
  },
  async post(path, body) {
    const res = await fetch(this.base + path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(Company.toBackend(path, body))
    });
    if (!res.ok) throw new Error('Request failed: ' + path + ' (HTTP ' + res.status + ')');
    return res.json();
  }
};

// ---------- Demo tarix (config.js -> CORBANK_DEMO_TODAY) ----------
const DEMO_TODAY = window.CORBANK_DEMO_TODAY || '';
function demoNow() {
  if (DEMO_TODAY) {
    const [y, m, d] = DEMO_TODAY.split('-').map(Number);
    return new Date(y, m - 1, d);
  }
  return new Date();
}

function escHtml(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, ch =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));
}

const Fmt = {
  money(value, currency) {
    const n = Number(value || 0);
    const parts = n.toFixed(2).split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    return `${parts[0]},${parts[1]} ${currency}`;
  },
  moneySplit(value) {
    const n = Number(value || 0);
    const parts = n.toFixed(2).split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    return parts; // [intPart, decPart]
  },
  // 75000 -> "75 000 AZN" (tam ədəddə ",00" yoxdur), 64320.5 -> "64 320,50 AZN"
  moneyShort(value, currency) {
    const n = Number(value || 0);
    if (Number.isInteger(n)) return `${String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')} ${currency}`;
    return this.money(n, currency);
  },
  // Lokal tarixi YYYY-MM-DD kimi (toISOString UTC-yə çevirdiyi üçün günü sürüşdürə bilər)
  isoDate(d) {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  },
  dateAz(isoDate) {
    if (!isoDate) return '';
    const [y, m, d] = isoDate.split('-');
    return `${d}.${m}.${y}`;
  },
  today() {
    const d = demoNow();
    const days = ['Bazar', 'Bazar ertəsi', 'Çərşənbə axşamı', 'Çərşənbə', 'Cümə axşamı', 'Cümə', 'Şənbə'];
    const months = ['yanvar','fevral','mart','aprel','may','iyun','iyul','avqust','sentyabr','oktyabr','noyabr','dekabr'];
    return `${d.getDate()} ${months[d.getMonth()]} ${d.getFullYear()}, ${days[d.getDay()]}`;
  }
};

const Icons = {
  home: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/></svg>`,
  book: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M8 13h8M8 17h8"/></svg>`,
  chart: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><path d="M7 14l4-4 3 3 5-6"/></svg>`,
  tag: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 12l-8 8-9-9V4h7l10 8z"/><circle cx="7" cy="7" r="1"/></svg>`,
  settings: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.87l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.7 1.7 0 0 0-1.87-.34 1.7 1.7 0 0 0-1 1.55V21a2 2 0 0 1-4 0v-.09A1.7 1.7 0 0 0 9 19.4a1.7 1.7 0 0 0-1.87.34l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.7 1.7 0 0 0 4.6 15a1.7 1.7 0 0 0-1.55-1H3a2 2 0 0 1 0-4h.09A1.7 1.7 0 0 0 4.6 9a1.7 1.7 0 0 0-.34-1.87l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1-1.55V3a2 2 0 0 1 4 0v.09a1.7 1.7 0 0 0 1 1.55 1.7 1.7 0 0 0 1.87-.34l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.7 1.7 0 0 0 19.4 9a1.7 1.7 0 0 0 1.55 1H21a2 2 0 0 1 0 4h-.09a1.7 1.7 0 0 0-1.55 1z"/></svg>`,
  headset: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 18v-6a9 9 0 0 1 18 0v6"/><path d="M21 19a2 2 0 0 1-2 2h-1v-6h3z"/><path d="M3 19a2 2 0 0 0 2 2h1v-6H3z"/></svg>`,
  logout: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></svg>`,
  chevron: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg>`,
  swap: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h13l-4-4"/><path d="M17 17H4l4 4"/></svg>`,
  send: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4z"/></svg>`,
  globe: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20"/><path d="M12 2a15 15 0 0 1 0 20 15 15 0 0 1 0-20z"/></svg>`,
  bolt: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L3 14h8l-1 8 10-12h-8l1-8z"/></svg>`,
  refresh: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 4v6h-6"/><path d="M1 20v-6h6"/><path d="M3.5 9a9 9 0 0 1 14.85-3.36L23 10"/><path d="M20.5 15a9 9 0 0 1-14.85 3.36L1 14"/></svg>`,
  bank: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 21h18"/><path d="M4 21V9l8-6 8 6v12"/><path d="M9 21v-6h6v6"/></svg>`,
  bulb: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18h6"/><path d="M10 22h4"/><path d="M12 2a7 7 0 0 0-4 12.7V17h8v-2.3A7 7 0 0 0 12 2z"/></svg>`,
  copy: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>`,
  clock: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>`,
  bookmark: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>`,
  arrowDown: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M17 7L7 17"/><path d="M17 17H7V7"/></svg>`,
  arrowUp: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M7 17L17 7"/><path d="M7 7h10v10"/></svg>`,
  eyeOff: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a18.5 18.5 0 0 1 5.06-5.94M9.9 4.24A10.94 10.94 0 0 1 12 4c7 0 11 7 11 7a18.5 18.5 0 0 1-2.16 3.19M14.12 14.12a3 3 0 1 1-4.24-4.24"/><path d="M1 1l22 22"/></svg>`,
  download: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="M7 10l5 5 5-5"/><path d="M12 15V3"/></svg>`,
  fileText: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/></svg>`,
  company: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="3" width="14" height="18" rx="1.5"/><path d="M9 7.5h.01M12 7.5h.01M15 7.5h.01M9 11h.01M12 11h.01M15 11h.01" stroke-width="2.4"/><path d="M10 21v-4h4v4"/></svg>`,
  bell: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>`,
  arrowLeft: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>`,
  check: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>`,
  chevDown: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg>`,
  card: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/><path d="M6 15h4"/></svg>`,
};

// ---------- Flags & logos (SVG files in /img — swap in official files there if you have them) ----------
const FLAG_FILES = { AZN: 'img/flag-azn.svg', USD: 'img/flag-usd.svg', EUR: 'img/flag-eur.svg' };
const BANK_FILES = [
  { match: 'pasha',  file: 'img/bank-pasha.svg' },
  { match: 'abb',    file: 'img/bank-abb.svg' },
  { match: 'ziraat', file: 'img/bank-ziraat.svg' },
];

function flagImg(currency, size = 36) {
  const src = FLAG_FILES[currency];
  if (!src) return `<span class="flag-fallback" style="width:${size}px;height:${size}px">${currency || ''}</span>`;
  return `<img class="flag-img" src="${src}" alt="${currency}" width="${size}" height="${size}">`;
}

function bankLogoImg(name, size = 36) {
  const n = String(name || '').toLowerCase();
  const hit = BANK_FILES.find(b => n.includes(b.match));
  const src = hit ? hit.file : 'img/bank.svg';
  return `<img class="bank-img" src="${src}" alt="${name || ''}" width="${size}" height="${size}">`;
}

// Hesabın adı + hansı bankdadırsa (backend `bank` sahəsi: PASHA | ABB) — iki bankın eyni adlı hesabları qarışmasın.
// Hesab səhifəsinə keçid. Bəzi dev-serverlər / brauzerlər redirect zamanı "?id=..." hissəsini itirir
// (səhifə onda default AZN hesabını açırdı) — ona görə id həm query-də, həm hash-də (#id=...) verilir və
// klikdə sessionStorage-də də yadda saxlanılır; account.js bunların hamısına baxır.
const LAST_ACCOUNT_KEY = 'corbank_last_account';
function accountUrl(id, extraQuery = '') {
  const enc = encodeURIComponent(id);
  return `account.html?id=${enc}${extraQuery}#id=${enc}`;
}
function rememberAccount(id) { try { sessionStorage.setItem(LAST_ACCOUNT_KEY, id); } catch (e) { /* storage bağlıdır */ } }
document.addEventListener('click', (e) => {
  const a = e.target.closest && e.target.closest('a[href*="account.html"]');
  if (!a) return;
  const m = a.getAttribute('href').match(/[?&]id=([^&#]+)/);
  if (m) { try { rememberAccount(decodeURIComponent(m[1])); } catch (err) { /* səhv kodlaşma */ } }
}, true);

// Seçilmiş bank (PASHA | ABB | '' = hamısı) — dashboard və "Bütün hesablar" səhifələri paylaşır.
const BankSel = {
  KEY: 'corbank_bank',
  get() { try { return localStorage.getItem(this.KEY) || ''; } catch (e) { return ''; } },
  set(v) { try { v ? localStorage.setItem(this.KEY, v) : localStorage.removeItem(this.KEY); } catch (e) { /* storage bağlıdır */ } },
  query() { const v = this.get(); return v ? '?bank=' + encodeURIComponent(v) : ''; }
};

function bankTitle(bank) {
  return ({ PASHA: 'PASHA Bank', ABB: 'ABB' })[String(bank || '').toUpperCase()] || '';
}
function accountLabel(acc) {
  const b = bankTitle(acc && acc.bank);
  return b ? `${acc.name} · ${b}` : acc.name;
}
// Öz hesablar arası köçürmə / mübadilə yalnız eyni bankın daxilində mümkündür (hər bank öz ledger-ini yeniləyir).
function sameBank(a, b) {
  return String((a && a.bank) || '') === String((b && b.bank) || '');
}

function flagFor(currency) {
  return { AZN: '🇦🇿', USD: '🇺🇸', EUR: '🇪🇺' }[currency] || '💳';
}

// ---------- Modal ----------
function ensureOverlayRoot() {
  let root = document.getElementById('modalRoot');
  if (!root) {
    root = document.createElement('div');
    root.id = 'modalRoot';
    document.body.appendChild(root);
  }
  return root;
}

function openModal({ title, subtitle, bodyHtml, footerHtml }) {
  const root = ensureOverlayRoot();
  root.innerHTML = `
    <div class="modal-overlay" id="modalOverlay">
      <div class="modal-box" role="dialog" aria-modal="true">
        <div class="modal-head">
          <div class="modal-titles">
            <h3>${title}</h3>
            ${subtitle ? `<div class="modal-sub">${subtitle}</div>` : ''}
          </div>
          <button class="modal-close" id="modalCloseBtn" type="button" aria-label="Bağla">&times;</button>
        </div>
        <div class="modal-body">${bodyHtml}</div>
        ${footerHtml ? `<div class="modal-footer">${footerHtml}</div>` : ''}
      </div>
    </div>`;
  document.getElementById('modalCloseBtn').addEventListener('click', closeModal);
  document.getElementById('modalOverlay').addEventListener('click', (e) => {
    if (e.target.id === 'modalOverlay') closeModal();
  });
  return root;
}

function closeModal() {
  const root = document.getElementById('modalRoot');
  if (root) root.innerHTML = '';
}
window.closeModal = closeModal;

// ---------- Toast ----------
function ensureToastRoot() {
  let root = document.getElementById('toastRoot');
  if (!root) {
    root = document.createElement('div');
    root.id = 'toastRoot';
    document.body.appendChild(root);
  }
  return root;
}

function showToast(message, kind = 'success') {
  const root = ensureToastRoot();
  const el = document.createElement('div');
  el.className = `toast toast-${kind}`;
  el.textContent = message;
  root.appendChild(el);
  requestAnimationFrame(() => el.classList.add('show'));
  setTimeout(() => {
    el.classList.remove('show');
    setTimeout(() => el.remove(), 250);
  }, 3200);
}

function renderSidebar(activeHref) {
  const items = [
    { href: 'index.html', icon: Icons.home, label: 'Əsas səhifə' },
    { href: 'muhasibatliq.html', icon: Icons.book, label: 'Mühasibatlıq' },
    { href: 'analitika.html', icon: Icons.chart, label: 'Analitika' },
    { href: 'tariflar.html', icon: Icons.tag, label: 'Tariflər' },
    { href: 'parametrler.html', icon: Icons.settings, label: 'Parametrlər' },
    { href: 'destek.html', icon: Icons.headset, label: 'Əlaqə və dəstək' },
  ];
  const nav = items.map(it => `
    <a class="nav-item ${it.href === activeHref ? 'active' : ''}" href="${it.href}">
      ${it.icon}<span>${it.label}</span>
    </a>`).join('');

  return `
  <aside class="sidebar">
    <div class="brand">
      <img class="brand-logo" src="img/logo.svg" alt="COR Bank">
      <div class="brand-text">
        <div class="name">COR Bank</div>
        <div class="sub">Business</div>
      </div>
    </div>
    <nav class="nav">${nav}</nav>
    <div class="nav-footer">
      <a class="nav-item" href="index.html">${Icons.logout}<span>Çıxış</span></a>
    </div>
  </aside>`;
}

function companyMenuHtml() {
  const cur = Company.current();
  return Company.list.map(co => `
    <button type="button" role="option" class="company-item ${co.id === cur.id ? 'selected' : ''}"
            data-company="${co.id}" aria-selected="${co.id === cur.id}">
      <span>${co.name}</span>${co.id === cur.id ? Icons.check : ''}
    </button>`).join('');
}

function renderTopbar() {
  const prefs = Prefs.get();
  const cur = Company.current();
  return `
  <header class="topbar">
    <div class="topbar-company-wrap">
      <button class="topbar-company" type="button" aria-haspopup="listbox" aria-expanded="false">
        ${Icons.company}
        <span class="topbar-company-name">${cur.name}</span>
        <span class="topbar-chev">${Icons.chevDown}</span>
      </button>
      <div class="company-menu" role="listbox" hidden>${companyMenuHtml()}</div>
    </div>
    <button class="topbar-bell" type="button" title="Bildirişlər">
      ${Icons.bell}
      ${prefs.notifDot ? '<span class="topbar-bell-dot"></span>' : ''}
    </button>
    <button class="topbar-user" type="button" title="Profil">
      <span class="topbar-avatar">${initialsFrom(prefs.name)}</span>
      ${Icons.chevDown}
    </button>
  </header>`;
}

function initialsFrom(name) {
  const parts = String(name || 'FA').trim().split(/\s+/).filter(Boolean);
  if (!parts.length) return 'FA';
  return (parts[0][0] + (parts[1] ? parts[1][0] : '')).toUpperCase();
}

// ---------- User preferences (Parametrlər səhifəsi) ----------
// localStorage-da saxlanılır ki, "brauzer icazə verdikdə saxlanılır" sözü doğru olsun.
const PREFS_KEY = 'corbank_prefs';
const Prefs = {
  defaults: { name: 'Farid Abbasovdsa', email: 'farid@alfa.azdsa', hideAmounts: false, notifDot: true },
  get() {
    try {
      const raw = localStorage.getItem(PREFS_KEY);
      if (!raw) return { ...this.defaults };
      return { ...this.defaults, ...JSON.parse(raw) };
    } catch (e) {
      return { ...this.defaults };
    }
  },
  set(partial) {
    const next = { ...this.get(), ...partial };
    try { localStorage.setItem(PREFS_KEY, JSON.stringify(next)); } catch (e) { /* brauzer icazə vermir */ }
    return next;
  }
};

// Bütün səhifələrdə "Məbləğləri gizlət" seçimini tətbiq et
function applyAmountVisibility() {
  document.body.classList.toggle('hide-amounts', !!Prefs.get().hideAmounts);
}
applyAmountVisibility();

// ---------- Topbar davranışı: şirkət seçimi + profil modalı ----------
function closeCompanyMenu() {
  const menu = document.querySelector('.company-menu');
  const btn = document.querySelector('.topbar-company');
  if (menu) menu.hidden = true;
  if (btn) btn.setAttribute('aria-expanded', 'false');
}

function toggleCompanyMenu() {
  const menu = document.querySelector('.company-menu');
  const btn = document.querySelector('.topbar-company');
  if (!menu || !btn) return;
  const open = menu.hidden;
  menu.hidden = !open;
  btn.setAttribute('aria-expanded', open ? 'true' : 'false');
}

function syncTopbarCompany() {
  const nameEl = document.querySelector('.topbar-company-name');
  const menu = document.querySelector('.company-menu');
  if (nameEl) nameEl.textContent = Company.current().name;
  if (menu) menu.innerHTML = companyMenuHtml();
}

// Səhifə "corbank:company-changed" hadisəsini dinləyib preventDefault() çağırsa, özü yenidən çəkir;
// əks halda səhifə sadəcə yenilənir (məlumat yenidən yüklənir).
function selectCompany(id) {
  closeCompanyMenu();
  if (id === Company.current().id) return;
  Company.set(id);
  const ev = new CustomEvent('corbank:company-changed', { cancelable: true, detail: Company.current() });
  if (document.dispatchEvent(ev)) { location.reload(); return; }
  syncTopbarCompany();
}

function openProfileModal() {
  const p = Prefs.get();
  openModal({
    title: 'İstifadəçi profili',
    subtitle: 'COR Bank · Demo məlumatlar',
    bodyHtml: `
      <div class="profile-box">
        <div class="profile-avatar">${escHtml(initialsFrom(p.name))}</div>
        <div class="profile-name">${escHtml(p.name)}</div>
        <div class="profile-email">${escHtml(p.email)}</div>
        <div class="profile-role">${escHtml(Company.current().name)} · Demo administrator</div>
      </div>`,
    footerHtml: `<button class="btn-primary btn-block" id="profileEditBtn" type="button">Profili redaktə et</button>`
  });
  document.getElementById('profileEditBtn').addEventListener('click', () => { location.href = 'parametrler.html'; });
}

document.addEventListener('click', (e) => {
  if (e.target.closest('.topbar-company')) { toggleCompanyMenu(); return; }
  const item = e.target.closest('.company-item');
  if (item) { selectCompany(item.dataset.company); return; }
  if (e.target.closest('.topbar-user')) { closeCompanyMenu(); openProfileModal(); return; }
  if (!e.target.closest('.topbar-company-wrap')) closeCompanyMenu();
});

document.addEventListener('keydown', (e) => {
  if (e.key !== 'Escape') return;
  closeCompanyMenu();
  closeModal();
});
