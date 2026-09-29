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

function opRowHtml(op) {
  const isIn = op.direction === 'in';
  return `
  <tr>
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
    <td class="amount ${isIn ? 'in' : 'out'}">${isIn ? '+' : '−'}${Fmt.money(op.amount, op.currency)}</td>
    <td><span class="status-chip"><span class="dot"></span>Yerinə yetirildi</span></td>
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
  <div class="db-bank db-bank-switch${active ? ' is-active' : ''}" role="button" tabindex="0"
       data-bank="${esc(b.code)}" aria-pressed="${active}">
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

function renderBanks() {
  const connected = dash.connectedBanks || [];
  const others = dash.otherBanks || [];
  const hint = connected.filter(bankOn).length > 1
    ? `<div class="db-bank-hint">${dash.selectedBank ? 'Yalnız seçilmiş bankın hesabları göstərilir. Hamısı üçün seçilmiş banka yenidən klikləyin.' : 'Bankı seçin — hesablar və əməliyyatlar həmin banka görə göstəriləcək.'}</div>`
    : '';
  document.getElementById('banksList').innerHTML =
    connected.map(b => connectedBankRowHtml(b, b.code === dash.selectedBank)).join('') + hint + others.map(bankRowHtml).join('');
}

function switchBank(code) {
  BankSel.set(BankSel.get() === code ? '' : code);
  load();
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
async function load() {
  try {
    dash = await Api.get('/dashboard' + BankSel.query());
    // Saxlanmış bank artıq qoşulu deyilsə (məs. Pasha söndürülüb) seçimi təmizlə
    if (BankSel.get() && dash.selectedBank !== BankSel.get()) BankSel.set('');
    document.getElementById('accountsGrid').innerHTML = dash.accounts.map(accountCardHtml).join('');
    document.getElementById('projectsGrid').innerHTML = dash.otherProjects.map(projectCardHtml).join('');
    document.getElementById('opsBody').innerHTML = dash.recentOperations.map(opRowHtml).join('');
    renderBanks();
    document.getElementById('fxList').innerHTML = dash.exchangeRates.map(fxRowHtml).join('');
  } catch (e) {
    console.error(e);
    document.querySelector('.db-page').insertAdjacentHTML('afterbegin',
      '<p style="color:#d84343">Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı? (mvn spring-boot:run)</p>');
  }
}

// Şirkət dəyişəndə səhifəni yeniləmədən məlumatı yenidən çək
document.addEventListener('corbank:company-changed', (e) => { e.preventDefault(); load(); });

load();
