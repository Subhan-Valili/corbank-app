document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('index.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());
document.getElementById('todayLabel').textContent = 'Bugün, ' + Fmt.today();
document.querySelectorAll('[data-chev]').forEach(el => { el.innerHTML = Icons.chevron; });

// ---------------------------------------------------------------
// Page-specific icons
// ---------------------------------------------------------------
const dbSvg = (paths, size = 24, sw = 1.8) =>
  `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="${sw}" stroke-linecap="round" stroke-linejoin="round">${paths}</svg>`;

const DbIcons = {
  coins:   dbSvg('<circle cx="9" cy="9" r="5.5"/><circle cx="15.5" cy="15.5" r="5.5"/><path d="M9 6.6v4.6"/><path d="M15.5 13.1v4.6"/>'),
  percent: dbSvg('<path d="M19 5L5 19"/><circle cx="7" cy="7" r="2.6"/><circle cx="17" cy="17" r="2.6"/>'),
  users:   dbSvg('<circle cx="9" cy="8" r="3.6"/><path d="M2.5 20c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6"/><circle cx="17.2" cy="8.6" r="2.7"/><path d="M17.5 14c2.7.2 4.4 2 4.4 5"/>'),
  doc:     dbSvg('<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M8 13h8M8 17h8"/>'),
  card:    dbSvg('<rect x="2.5" y="5" width="19" height="14" rx="2.5"/><path d="M2.5 10h19"/>'),
};

// ---------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------
function esc(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// "AZ21CORB00000000123456789012" -> "AZ21 CORB 0000 0000 1234 5678 9012"
function formatIban(iban) {
  const clean = String(iban || '').replace(/\s+/g, '');
  return clean.replace(/(.{4})/g, '$1 ').trim();
}

// 75000 -> "75 000 AZN" (no ",00" for whole numbers), 64320.5 -> "64 320,50 AZN"
function moneyShort(value, currency) {
  const n = Number(value || 0);
  if (Number.isInteger(n)) return `${String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')} ${currency}`;
  return Fmt.money(n, currency);
}

function chevBtn() {
  return `<span class="db-chev">${Icons.chevron}</span>`;
}

// ---------------------------------------------------------------
// Renderers
// ---------------------------------------------------------------
function accountCardHtml(acc) {
  const [intPart, decPart] = Fmt.moneySplit(acc.balance);
  return `
  <a class="db-account" href="${accountUrl(acc.id)}">
    <div class="db-account-top">
      <div class="db-account-id">${flagImg(acc.currency, 44)}<span>${esc(accountLabel(acc))}</span></div>
      <span class="db-status"><span class="dot"></span>${esc(acc.status)}</span>
    </div>
    <div class="db-account-mid">
      <div class="db-balance">${intPart}<small>,${decPart} ${esc(acc.currency)}</small></div>
      ${chevBtn()}
    </div>
    <div class="db-iban"><span>IBAN</span><span>${esc(formatIban(acc.iban))}</span></div>
  </a>`;
}

// "Bütün banklar" rejimi: hər valyuta üçün bir kart — banklar üzrə qalıqlar və cəm
const CUR_ORDER = ['AZN', 'USD', 'EUR'];
function currencySumCardsHtml(accounts) {
  const groups = {};
  accounts.forEach(a => { (groups[a.currency] = groups[a.currency] || []).push(a); });
  const curs = Object.keys(groups).sort((a, b) =>
    (CUR_ORDER.indexOf(a) + 1 || 99) - (CUR_ORDER.indexOf(b) + 1 || 99));
  return curs.map(cur => {
    const list = groups[cur].slice().sort((a, b) => Number(b.balance) - Number(a.balance));
    const total = Math.round(list.reduce((s, a) => s + Number(a.balance || 0), 0) * 100) / 100;
    const [intPart, decPart] = Fmt.moneySplit(total);
    const rows = list.map(a => {
      const share = total > 0 ? Math.max(2, Number(a.balance) / total * 100) : 0;
      return `
      <a class="db-sum-row" href="${accountUrl(a.id)}" title="${esc(accountLabel(a))}">
        <span class="db-sum-bank">${bankLogoImg(bankTitle(a.bank), 22)}<span>${esc(bankTitle(a.bank) || a.name)}</span></span>
        <span class="db-sum-amount">${Fmt.money(a.balance, cur)}</span>
      </a>
      ${list.length > 1 ? `<div class="db-sum-share"><span style="width:${share.toFixed(1)}%"></span></div>` : ''}`;
    }).join('');
    return `
    <div class="db-account db-account-sum">
      <div class="db-account-top">
        <div class="db-account-id">${flagImg(cur, 44)}<span>${esc(cur)} · Bütün banklar</span></div>
        <span class="db-sum-count">${list.length} hesab</span>
      </div>
      <div class="db-account-mid">
        <div class="db-balance">${intPart}<small>,${decPart} ${esc(cur)}</small></div>
      </div>
      <div class="db-sum-rows">${rows}</div>
    </div>`;
  }).join('');
}

function projectCardHtml(p) {
  const iconMap = { credit: DbIcons.coins, deposit: DbIcons.percent, payroll: DbIcons.users };
  const labelMap = { credit: 'Biznes krediti', deposit: 'Müddətli depozit', payroll: 'Əmək haqqı layihəsi' };
  const value = p.meta ? p.meta : moneyShort(p.amount, p.currency);
  return `
  <a class="db-project" href="project.html?id=${esc(p.id)}" data-project-id="${esc(p.id)}" data-project-type="${esc(p.type)}">
    <div class="db-project-icon">${iconMap[p.type] || DbIcons.coins}</div>
    <div class="db-project-text">
      <div class="db-project-label">${esc(p.name || labelMap[p.type])}</div>
      <div class="db-project-value">${esc(value)}</div>
    </div>
    ${chevBtn()}
  </a>`;
}

const OP_STATUS = {
  completed: { label: 'Yerinə yetirildi', cls: '' },
  pending:   { label: 'Gözləmədə', cls: 'pending' },
  rejected:  { label: 'Rədd edildi', cls: 'rejected' },
};
const opStatus = st => OP_STATUS[st] || OP_STATUS.completed;

function opRowHtml(op, i) {
  const isIn = op.direction === 'in';
  const st = opStatus(op.status);
  return `
  <tr class="db-op-row" data-op-idx="${i}" tabindex="0" role="button" title="Ətraflı məlumat üçün klikləyin">
    <td class="db-date">${Fmt.dateAz(op.date)}</td>
    <td>
      <div class="op-counterparty">
        <div class="op-icon ${isIn ? 'in' : 'out'}">${isIn ? Icons.arrowDown : Icons.arrowUp}</div>
        <div>
          <div class="op-name">${esc(op.counterparty)}</div>
          <div class="op-desc">${esc(op.description)}</div>
        </div>
      </div>
    </td>
    <td>${opBankHtml(op.bank)}</td>
    <td class="amount ${isIn ? 'in' : 'out'}">${isIn ? '+' : '−'}${Fmt.money(op.amount, op.currency)}</td>
    <td><span class="status-chip ${st.cls}"><span class="dot"></span>${st.label}</span></td>
  </tr>`;
}

function bankRowHtml(b) {
  return `
  <div class="db-bank">
    <div class="db-bank-left">
      ${bankLogoImg(b.name, 36)}
      <span class="db-bank-name">${esc(b.name)}</span>
    </div>
    <div class="db-bank-right">
      <span class="db-bank-amount">${Fmt.money(b.balance, b.currency)}</span>
      ${chevBtn()}
    </div>
  </div>`;
}

// Qoşulmuş bank: klikləyəndə "Hesablarım" və "Son əməliyyatlar" həmin banka keçir; aktiv olana yenidən klik = hamısı
// `connected` yalnız yeni ms-corbank qaytarır; köhnə build-də sahə yoxdur — onda hesab sayına görə qərar veririk
const bankOn = b => (b.connected === undefined ? Number(b.accountCount) > 0 : !!b.connected);

function connectedBankRowHtml(b, active) {
  if (!bankOn(b)) {
    return `
  <div class="db-bank db-bank-off" title="Bu bank hazırda hesab qaytarmır (servis işləmir və ya hesab yoxdur)">
    <div class="db-bank-left">
      ${bankLogoImg(b.name, 36)}
      <span class="db-bank-name">${esc(b.name)}</span>
    </div>
    <div class="db-bank-right"><span class="db-bank-count">Qoşulmayıb</span></div>
  </div>`;
  }
  return `
  <div class="db-bank db-bank-switch${active ? ' is-active' : ''}" role="radio" tabindex="0"
       data-bank="${esc(b.code)}" aria-checked="${active}">
    <div class="db-bank-left">
      ${bankLogoImg(b.name, 36)}
      <span class="db-bank-name">${esc(b.name)}</span>
    </div>
    <div class="db-bank-right">
      <span class="db-bank-count">${b.accountCount} hesab</span>
      <span class="db-bank-check" aria-hidden="true">${active ? '✓' : ''}</span>
    </div>
  </div>`;
}

const ALL_BANKS_ICON = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l9 4.5-9 4.5-9-4.5z"/><path d="M3 12l9 4.5 9-4.5"/><path d="M3 16.5l9 4.5 9-4.5"/></svg>`;

function allBanksRowHtml(total, active) {
  return `
  <div class="db-bank db-bank-switch${active ? ' is-active' : ''}" role="button" tabindex="0"
       data-bank="${BankSel.ALL}" role="radio" aria-checked="${active}">
    <div class="db-bank-left">
      <span class="db-allbanks-icon">${ALL_BANKS_ICON}</span>
      <span class="db-bank-name">Bütün banklar</span>
    </div>
    <div class="db-bank-right">
      <span class="db-bank-count">${total} hesab</span>
      <span class="db-bank-check" aria-hidden="true">${active ? '✓' : ''}</span>
    </div>
  </div>`;
}

function renderBanks() {
  const connected = dash.connectedBanks || [];
  const others = dash.otherBanks || [];
  const on = connected.filter(bankOn);
  const isAll = BankSel.get() === BankSel.ALL;
  const total = on.reduce((s, b) => s + Number(b.accountCount || 0), 0);
  const hintText = isAll
    ? 'Bütün banklar üzrə valyuta cəmləri göstərilir. Konkret bankın hesabları üçün həmin bankı seçin.'
    : 'Yalnız seçilmiş bankın hesabları və əməliyyatları göstərilir.';
  const multi = on.length > 1;
  document.getElementById('banksList').innerHTML =
    (multi ? allBanksRowHtml(total, isAll) : '') +
    connected.map(b => connectedBankRowHtml(b, !isAll && b.code === dash.selectedBank)).join('') +
    (multi ? `<div class="db-bank-hint">${hintText}</div>` : '') +
    others.map(bankRowHtml).join('');
}

// Seçim məcburidir: həmişə ya "Bütün banklar", ya da bir bank seçili olur. Seçili olana klik heç nə dəyişmir.
function switchBank(code) {
  if (!code || BankSel.get() === code) return;
  BankSel.set(code);
  load(true);
}

// Məzmunu yumşaq dəyiş: köhnəsi solur, blokun hündürlüyü yeni məzmuna axıcı keçir, yenisi görünür
const reduceMotion = () => window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const wait = ms => new Promise(r => setTimeout(r, ms));
async function swapContent(box, target, html, animate) {
  if (!animate || reduceMotion()) { target.innerHTML = html; return; }
  const h0 = box.offsetHeight;
  box.style.height = h0 + 'px';
  box.style.overflow = 'hidden';
  target.classList.add('db-swap', 'db-fading');
  await wait(180);
  target.innerHTML = html;
  box.style.height = 'auto';
  const h1 = box.offsetHeight;
  box.style.height = h0 + 'px';
  void box.offsetHeight;                       // reflow — keçid başlasın
  box.style.transition = 'height .4s cubic-bezier(.4, 0, .2, 1)';
  box.style.height = h1 + 'px';
  requestAnimationFrame(() => target.classList.remove('db-fading'));
  await wait(420);
  box.style.height = ''; box.style.overflow = ''; box.style.transition = '';
}

function fxRowHtml(r) {
  return `
  <div class="db-fx-row">
    <div class="db-fx-cur">${flagImg(r.currency, 36)}<span>${esc(r.currency)}</span></div>
    <div>${Number(r.buy).toFixed(4).replace('.', ',')}</div>
    <div>${Number(r.sell).toFixed(4).replace('.', ',')}</div>
  </div>`;
}

// ---------------------------------------------------------------
// Sifarişlər (orders)
// ---------------------------------------------------------------
const ORDERS = [
  { type: 'account', icon: DbIcons.doc,     title: 'Cari hesab',  sub: 'Tez və rahat açın' },
  { type: 'credit',  icon: DbIcons.percent, title: 'Kredit',      sub: 'Biznesinizi inkişaf etdirin' },
  { type: 'deposit', icon: DbIcons.coins,   title: 'Depozit',     sub: 'Vəsaitlərinizi dəyərləndirin' },
  { type: 'card',    icon: DbIcons.card,    title: 'Biznes kart', sub: 'Rahat ödəniş imkanı' },
  { type: 'payroll', icon: DbIcons.users,   title: 'Əmək haqqı',  sub: 'Komandanız üçün' },
];

document.getElementById('ordersGrid').innerHTML = ORDERS.map((o, i) => `
  <button class="db-order" type="button" data-idx="${i}">
    <span class="db-order-icon">${o.icon}</span>
    <span class="db-order-text">
      <span class="db-order-title">${o.title}</span>
      <span class="db-order-sub">${o.sub}</span>
    </span>
  </button>`).join('');

document.querySelectorAll('.db-order').forEach(btn => {
  btn.addEventListener('click', () => openOrderModal(ORDERS[Number(btn.dataset.idx)].type));
});

// ---------------------------------------------------------------
// Modallar: layihə təfərrüatı, digər banklar, valyuta məzənnələri
// ---------------------------------------------------------------
let dash = null;   // /dashboard cavabı (şirkətə görə miqyaslanmış)

const PROJECT_ORDER_TYPE = { credit: 'credit', deposit: 'deposit', payroll: 'payroll' };

async function openProjectModal(id, fallbackType) {
  let p;
  try {
    p = await Api.get(`/projects/${encodeURIComponent(id)}`);
  } catch (e) {
    console.error(e);
    showToast('Layihə məlumatlarını yükləmək mümkün olmadı', 'error');
    return;
  }
  const type = p.type || fallbackType;
  const rows = (p.details && p.details.length)
    ? p.details
    : [{ label: p.amount != null ? 'Məbləğ' : 'Məlumat', value: p.meta ? p.meta : moneyShort(p.amount, p.currency) }];

  openModal({
    title: esc(p.name || 'Layihə'),
    subtitle: 'COR Bank · Demo məlumatlar',
    bodyHtml: `<div class="detail-list">${rows.map(r => `
      <div class="detail-row"><span class="k">${esc(r.label)}</span><span class="v">${esc(r.value)}</span></div>`).join('')}
    </div>`,
    footerHtml: `<button class="btn-primary btn-block" id="projNewReqBtn" type="button">Yeni müraciət</button>`
  });
  document.getElementById('projNewReqBtn').addEventListener('click', () => {
    openOrderModal(PROJECT_ORDER_TYPE[type] || 'account');
  });
}

function openBanksModal() {
  if (!dash) return;
  const updated = Fmt.dateAz(Fmt.isoDate(demoNow()));
  openModal({
    title: 'Digər banklardakı hesablar',
    subtitle: 'COR Bank · Demo məlumatlar',
    bodyHtml: `
      <div class="info-note">Nümunə hesab məlumatlarıdır. Canlı bank bağlantısı yoxdur.</div>
      <div class="bank-acc-list">
        ${(dash.connectedBanks || []).map(b => `
          <div class="bank-acc">
            <div class="bank-acc-left">
              ${bankLogoImg(b.name, 40)}
              <div>
                <div class="bank-acc-name">${esc(b.name)}</div>
                <div class="bank-acc-sub">${bankOn(b) ? `Qoşulub · ${b.accountCount} hesab` : 'Qoşulmayıb'}</div>
              </div>
            </div>
          </div>`).join('')}
        ${dash.otherBanks.map(b => `
          <div class="bank-acc">
            <div class="bank-acc-left">
              ${bankLogoImg(b.name, 40)}
              <div>
                <div class="bank-acc-name">${esc(b.name)}</div>
                <div class="bank-acc-sub">${esc(b.currency)} cari hesabı · Son demo yenilənmə: ${updated}</div>
              </div>
            </div>
            <div class="bank-acc-amount">${Fmt.money(b.balance, b.currency)}</div>
          </div>`).join('')}
      </div>`,
    footerHtml: `<button class="btn-primary btn-block" type="button" onclick="closeModal()">Bağla</button>`
  });
}

function openFxModal() {
  if (!dash) return;
  const rates = dash.exchangeRates || [];
  const fmt4 = n => Number(n).toFixed(4).replace('.', ',');
  const azn = (dash.accounts.find(a => a.currency === 'AZN') || dash.accounts[0] || { id: 'azn-001' }).id;
  openModal({
    title: 'Valyuta məzənnələri',
    subtitle: 'COR Bank · Demo məlumatlar',
    bodyHtml: `
      <div class="fx-date">${Fmt.dateAz(Fmt.isoDate(demoNow()))} · Demo məzənnələr (AZN)</div>
      <table class="fx-table">
        <thead><tr><th>Valyuta</th><th>Alış</th><th>Satış</th></tr></thead>
        <tbody>${rates.map(r => `<tr><td>${esc(r.currency)}</td><td>${fmt4(r.buy)}</td><td>${fmt4(r.sell)}</td></tr>`).join('')}</tbody>
      </table>
      <div class="info-note">Demo əməliyyat hesablamasında vahid çevirmə kursu: ${rates.map(r => `${esc(r.currency)} ${fmt4(r.buy)}`).join(', ')} AZN. Alış/satış cədvəli məlumat xarakterlidir.</div>`,
    footerHtml: `<button class="btn-primary btn-block" id="fxGoBtn" type="button">Valyuta mübadiləsinə keç</button>`
  });
  document.getElementById('fxGoBtn').addEventListener('click', () => {
    rememberAccount(azn);
    location.href = accountUrl(azn, '&action=fx');
  });
}

// ---------------------------------------------------------------
// Əməliyyatın ətraflı məlumatı (Son əməliyyatlar → sətrə klik)
// ---------------------------------------------------------------
// Demo əməliyyat id-sindən sabit 10 rəqəmli istinad nömrəsi
function opReference(op) {
  let h = 2166136261;
  for (const ch of String(op.id)) { h ^= ch.charCodeAt(0); h = Math.imul(h, 16777619); }
  const d = op.date ? op.date.replace(/-/g, '').slice(2) : '';
  return `TRX-${d}-${String(h >>> 0).padStart(10, '0').slice(-6)}`;
}

function openOpModal(op) {
  if (!op) return;
  const isIn = op.direction === 'in';
  const st = opStatus(op.status);
  const acc = (dash.accounts || []).find(a => a.id === op.accountId);
  const bankName = bankTitle(op.bank || (acc && acc.bank)) || '—';
  const sign = isIn ? '+' : '−';
  const rows = [
    ['İstinad nömrəsi', opReference(op)],
    ['Tarix', Fmt.dateAz(op.date)],
    ['Əməliyyatın növü', isIn ? 'Mədaxil (daxilolma)' : 'Məxaric (ödəniş)'],
    [isIn ? 'Göndərən' : 'Alan', esc(op.counterparty)],
    ['Təyinat', esc(op.description || '—')],
    ['Bank', `<span class="op-bank">${bankLogoImg(bankName, 22)}<span>${esc(bankName)}</span></span>`],
    [isIn ? 'Mədaxil olunan hesab' : 'Məxaric olunan hesab', acc ? esc(accountLabel(acc)) : '—'],
    ['IBAN', acc ? esc(formatIban(acc.iban)) : '—'],
    ['Valyuta', esc(op.currency)],
    ['Status', `<span class="status-chip ${st.cls}"><span class="dot"></span>${st.label}</span>`],
  ];
  openModal({
    title: 'Əməliyyat haqqında',
    subtitle: `${esc(bankName)} · ${Fmt.dateAz(op.date)}`,
    bodyHtml: `
      <div class="op-detail-head">
        <div class="op-icon ${isIn ? 'in' : 'out'}">${isIn ? Icons.arrowDown : Icons.arrowUp}</div>
        <div class="op-detail-main">
          <div class="op-detail-name">${esc(op.counterparty)}</div>
          <div class="op-detail-desc">${esc(op.description || '')}</div>
        </div>
        <div class="op-detail-amount ${isIn ? 'in' : 'out'}">${sign}${Fmt.money(op.amount, op.currency)}</div>
      </div>
      <div class="op-detail-rows">
        ${rows.map(([k, v]) => `<div class="review-row"><span class="k">${k}</span><span class="v">${v}</span></div>`).join('')}
      </div>
      <div class="info-note">Demo məlumatdır.</div>`,
    footerHtml: `
      <button class="btn-ghost" type="button" onclick="closeModal()">Bağla</button>
      ${acc ? `<a class="btn-primary" href="${accountUrl(acc.id)}">Hesaba keç</a>` : ''}`,
  });
}

const opsBodyEl = document.getElementById('opsBody');
opsBodyEl.addEventListener('click', (e) => {
  const row = e.target.closest('.db-op-row');
  if (row) openOpModal(dash.recentOperations[Number(row.dataset.opIdx)]);
});
opsBodyEl.addEventListener('keydown', (e) => {
  if (e.key !== 'Enter' && e.key !== ' ') return;
  const row = e.target.closest('.db-op-row');
  if (row) { e.preventDefault(); openOpModal(dash.recentOperations[Number(row.dataset.opIdx)]); }
});

// Klik hadisələri (dinamik yaradılan kartlar üçün delegation)
document.getElementById('projectsGrid').addEventListener('click', (e) => {
  const card = e.target.closest('.db-project');
  if (!card) return;
  e.preventDefault();   // href="project.html?id=..." ehtiyat üçün qalır (orta klik / yeni tab)
  openProjectModal(card.dataset.projectId, card.dataset.projectType);
});
document.getElementById('banksList').addEventListener('click', (e) => {
  const sw = e.target.closest('.db-bank-switch');
  if (sw) { switchBank(sw.dataset.bank); return; }
  if (e.target.closest('.db-bank')) openBanksModal();
});
document.getElementById('banksList').addEventListener('keydown', (e) => {
  if (e.key !== 'Enter' && e.key !== ' ') return;
  const sw = e.target.closest('.db-bank-switch');
  if (sw) { e.preventDefault(); switchBank(sw.dataset.bank); }
});
document.getElementById('banksLink').addEventListener('click', (e) => { e.preventDefault(); openBanksModal(); });
document.getElementById('fxLink').addEventListener('click', (e) => { e.preventDefault(); openFxModal(); });

// ---------------------------------------------------------------
// Load
// ---------------------------------------------------------------
// Guest (login olmayan): yalnız açıq məlumat — valyuta məzənnələri. Şəxsi data yoxdur.
async function loadGuest() {
  const page = document.querySelector('.db-page');
  page.classList.add('db-guest');
  document.querySelector('.db-head h1').textContent = 'COR Bank Business';
  document.querySelector('.db-head p').textContent = 'Hesablarınızı görmək üçün daxil olun.';
  const hide = el => { if (el) el.style.display = 'none'; };   // [hidden] CSS-dəki display ilə üstələnir
  hide(document.querySelector('#accountsGrid').closest('.db-panel').querySelector('.db-link'));
  hide(document.getElementById('fxLink'));
  [document.getElementById('projectsGrid').closest('.db-collapse'),
   document.getElementById('opsBody').closest('.db-panel'),
   document.getElementById('banksList').closest('.db-panel'),
   document.getElementById('ordersGrid').closest('.db-collapse')
  ].forEach(hide);

  document.getElementById('accountsGrid').innerHTML = `
    <div class="db-guest-cta">
      <div>
        <h3>Hesablarınız burada görünəcək</h3>
        <p>Daxil olduqdan sonra yalnız sizə aid hesabları, son əməliyyatları və ödənişləri görəcəksiniz.</p>
      </div>
      <div class="db-guest-actions">
        <a class="btn-primary" href="login.html">Daxil ol</a>
        <a class="btn-ghost" href="register.html">Qeydiyyat</a>
      </div>
    </div>`;
  try {
    const rates = await Api.publicGet('/public/fx-rates');
    document.getElementById('fxList').innerHTML = rates.map(fxRowHtml).join('');
  } catch (e) {
    console.error(e);
    document.getElementById('fxList').innerHTML = '<p style="color:var(--muted);padding:8px 0">Məzənnələri yükləmək mümkün olmadı.</p>';
  }
}

function noBanksHtml() {
  return `
    <div class="db-guest-cta">
      <div>
        <h3>Hələ heç bir bank qoşulmayıb</h3>
        <p>Hesablarınızı və balanslarınızı görmək üçün bankınızdan aldığınız Open API Key-i əlavə edin.</p>
      </div>
      <div class="db-guest-actions">
        <a class="btn-primary" href="banklar.html">Bank qoş</a>
      </div>
    </div>`;
}

async function load(animate = false) {
  if (!Auth.isLoggedIn()) return loadGuest();
  try {
    // Seçim yoxdursa (ilk giriş) — standart olaraq "Bütün banklar"
    if (!BankSel.get()) BankSel.set(BankSel.ALL);
    dash = await Api.get('/dashboard' + BankSel.query());
    // Saxlanmış bank artıq qoşulu deyilsə (məs. Pasha söndürülüb) — "Bütün banklar"a qayıt
    if (BankSel.get() !== BankSel.ALL && dash.selectedBank !== BankSel.get()) {
      BankSel.set(BankSel.ALL);
      dash = await Api.get('/dashboard');
    }
    const isAll = BankSel.get() === BankSel.ALL;

    // "Bütün banklar": yalnız Hesablarım (valyuta cəmləri) və Son əməliyyatlar qalır, digər bölmələr yumşaq gizlənir
    document.querySelector('.db-page').classList.toggle('db-mode-all', isAll);
    const accGrid = document.getElementById('accountsGrid');
    const accHtml = !dash.accounts.length ? noBanksHtml()
      : (isAll ? currencySumCardsHtml(dash.accounts) : dash.accounts.map(accountCardHtml).join(''));
    const opsBody = document.getElementById('opsBody');
    document.getElementById('projectsGrid').innerHTML = dash.otherProjects.map(projectCardHtml).join('');
    renderBanks();
    await Promise.all([
      swapContent(accGrid, accGrid, accHtml, animate),
      swapContent(opsBody.closest('.db-table-wrap'), opsBody, dash.recentOperations.map((op, i) => opRowHtml(op, i)).join(''), animate),
    ]);
    document.getElementById('fxList').innerHTML = dash.exchangeRates.map(fxRowHtml).join('');
  } catch (e) {
    console.error(e);
    document.querySelector('.db-page').insertAdjacentHTML('afterbegin',
      '<p style="color:#d84343">Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı?</p>');
  }
}

// Şirkət dəyişəndə səhifəni yeniləmədən məlumatı yenidən çək
document.addEventListener('corbank:company-changed', (e) => { e.preventDefault(); load(); });

load();
