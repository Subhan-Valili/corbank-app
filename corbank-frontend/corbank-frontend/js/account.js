document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar(''));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());

document.getElementById('iconCopy').innerHTML = Icons.copy;
document.getElementById('iconBalance').innerHTML = Icons.bookmark;
document.getElementById('iconIncome').innerHTML = Icons.arrowDown;
document.getElementById('iconExpense').innerHTML = Icons.arrowUp;
document.getElementById('iconNet').innerHTML = Icons.swap;
document.getElementById('iconCsv').innerHTML = Icons.download;
document.getElementById('iconPdf').innerHTML = Icons.fileText;
document.getElementById('iconBack').innerHTML = Icons.arrowLeft;
document.getElementById('hideBtn').innerHTML = `${Icons.eyeOff} Məbləğləri gizlət`;

const params = new URLSearchParams(location.search);
// id mənbələri: ?id=  ->  #id=  ->  sessionStorage (son klik edilən hesab)  ->  köhnə default
function idFromHash() { const m = location.hash.match(/[#&]id=([^&]+)/); try { return m ? decodeURIComponent(m[1]) : ''; } catch (e) { return ''; } }
function idFromSession() { try { return sessionStorage.getItem(LAST_ACCOUNT_KEY) || ''; } catch (e) { return ''; } }
let accountId = params.get('id') || idFromHash() || idFromSession() || 'azn-001';   // let: backend-də real id fərqlidirsə aşağıda düzəldilir

let hidden = false;
let account = null;
let lastOperations = [];      // last data.operations loaded for the "ops" tab (reused by CSV export + analytics)
let allAccounts = [];         // used by the "own accounts transfer" modal

// ---------------------------------------------------------------
// Services grid — each icon opens its own 3-step wizard (Məlumatlar → Yoxlama → Nəticə)
// ---------------------------------------------------------------
const SERVICES = [
  { icon: Icons.swap, label: 'Öz hesablarım arasında köçürmə', onClick: () => openOwnTransferModal() },
  { icon: Icons.send, label: 'Ölkədaxili köçürmə', onClick: () => openFieldWizard(domesticConfig()) },
  { icon: Icons.globe, label: 'Ölkəxarici köçürmə', onClick: () => openFieldWizard(internationalConfig()) },
  { icon: Icons.bolt, label: 'Ani ödəniş', onClick: () => openFieldWizard(instantConfig()) },
  { icon: Icons.card, label: 'Kartdan-karta köçürmə', onClick: () => openFieldWizard(cardToCardConfig()) },
  { icon: Icons.refresh, label: 'Valyuta mübadiləsi', onClick: () => openFxWizard() },
  { icon: Icons.bank, label: 'Büdcə və vergi ödənişləri', onClick: () => openFieldWizard(budgetConfig()) },
  { icon: Icons.bulb, label: 'Kommunal və digər xidmət ödənişləri', onClick: () => openFieldWizard(utilityConfig()) },
  { icon: Icons.copy, label: 'Kütləvi ödənişlər', onClick: () => openBulkModal() },
  { icon: Icons.clock, label: 'Daimi ödənişlər', onClick: () => switchTab('standing') },
  { icon: Icons.bookmark, label: 'Şablonlar', onClick: () => switchTab('templates') },
];

function renderServices() {
  document.getElementById('servicesGrid').innerHTML = SERVICES.map((s, i) => `
    <button class="service-item" data-idx="${i}">
      <div class="service-icon">${s.icon}</div>
      <span>${s.label}</span>
    </button>`).join('');

  document.querySelectorAll('.service-item').forEach(btn => {
    btn.addEventListener('click', () => {
      if (!account) { showToast('Məlumatlar hələ yüklənir, bir az gözləyin', 'error'); return; }
      SERVICES[Number(btn.dataset.idx)].onClick();
    });
  });
}

function copyIban() {
  if (account && account.iban) {
    navigator.clipboard.writeText(account.iban).then(
      () => showToast('IBAN kopyalandı'),
      () => showToast('Kopyalamaq mümkün olmadı', 'error')
    );
  }
}
window.copyIban = copyIban;

function maskOrShow(text) {
  return hidden ? text.replace(/[0-9]/g, '•') : text;
}

function statusLabel(status) {
  return ({ completed: 'Yerinə yetirildi', pending: 'Gözləmədə', rejected: 'Rədd edildi' })[status] || 'Yerinə yetirildi';
}
function statusClass(status) {
  return ({ completed: '', pending: 'pending', rejected: 'rejected' })[status] || '';
}

function opRowHtml(op) {
  const isIn = op.direction === 'in';
  return `
  <tr>
    <td>${Fmt.dateAz(op.date)}</td>
    <td>
      <div class="op-counterparty">
        <div class="op-icon ${isIn ? 'in' : 'out'}">${isIn ? Icons.arrowDown : Icons.arrowUp}</div>
        <div>
          <div class="op-name">${op.counterparty}</div>
          <div class="op-desc">${op.description}</div>
        </div>
      </div>
    </td>
    <td class="amount ${isIn ? 'in' : 'out'}">${maskOrShow((isIn ? '+' : '−') + Fmt.money(op.amount, op.currency))}</td>
    <td><span class="status-chip ${statusClass(op.status)}"><span class="dot"></span>${statusLabel(op.status)}</span></td>
  </tr>`;
}

function currentFilters() {
  return {
    startDate: document.getElementById('startDate').value,
    endDate: document.getElementById('endDate').value,
    search: document.getElementById('searchInput').value,
    direction: document.getElementById('directionSelect').value,
    status: document.getElementById('statusSelect').value,
    sort: document.getElementById('sortSelect').value,
  };
}

async function loadOperations() {
  const f = currentFilters();
  const qs = new URLSearchParams();
  Object.entries(f).forEach(([k, v]) => { if (v) qs.set(k, v); });
  const data = await Api.get(`/accounts/${encodeURIComponent(accountId)}/operations?${qs.toString()}`);
  lastOperations = data.operations || [];

  const body = document.getElementById('opsBody');
  const empty = document.getElementById('emptyState');
  if (lastOperations.length === 0) {
    body.innerHTML = '';
    empty.style.display = 'block';
  } else {
    empty.style.display = 'none';
    body.innerHTML = lastOperations.map(opRowHtml).join('');
  }

  document.getElementById('sumIncome').textContent = maskOrShow('+' + Fmt.money(data.summary.income, account.currency));
  document.getElementById('sumExpense').textContent = maskOrShow('−' + Fmt.money(data.summary.expense, account.currency));
  const net = Number(data.summary.net);
  const netEl = document.getElementById('sumNet');
  netEl.textContent = maskOrShow((net >= 0 ? '+' : '−') + Fmt.money(Math.abs(net), account.currency));
  netEl.className = 'value ' + (net >= 0 ? 'pos' : 'neg');

  renderAnalytics(data.summary);
}

function setDefaultDates() {
  // Demo rejimində (config.js -> CORBANK_DEMO_TODAY) dövr ayın 1-dən demo tarixinə qədərdir.
  // Real tarixdə isə mock məlumat (2025) və real bank məlumatı hər ikisi görünsün deyə 3 il geriyə.
  // Bitmə tarixi: demo tarixdən sonra yaranan real əməliyyatlar (məs. indi göndərilən ödənişlər) görünsün deyə
  // demo tarix ilə bugünkü real tarixdən böyüyü götürülür. Başlanğıc demo ayının 1-i qalır (2025 mock məlumatı görünür).
  const demo = demoNow();
  const realNow = new Date();
  const end = realNow > demo ? realNow : demo;
  const start = DEMO_TODAY
    ? new Date(demo.getFullYear(), demo.getMonth(), 1)
    : new Date(end.getFullYear() - 3, end.getMonth(), end.getDate());
  document.getElementById('startDate').value = Fmt.isoDate(start);
  document.getElementById('endDate').value = Fmt.isoDate(end);
}

// Yuxarı sağdakı hesab seçimi: başqa hesaba keçid
function renderAccountSwitch() {
  const sel = document.getElementById('accountSwitch');
  sel.innerHTML = allAccounts.map(a =>
    `<option value="${escHtml(a.id)}" ${a.id === accountId ? 'selected' : ''}>${escHtml(accountLabel(a))}</option>`).join('');
  sel.onchange = () => { rememberAccount(sel.value); location.href = accountUrl(sel.value); };
}

// URL-dəki ?id= backend-dəki real hesab id-si olmaya bilər (məs. köhnə 'azn-001' default-u, və ya başqa bank).
// Əvvəlcə /accounts siyahısından həqiqi hesabı tapırıq: id / IBAN üzrə dəqiq uyğunluq, sonra 'azn-001' kimi
// valyuta alias-ı, sonra ilk hesab. Beləliklə səhifə heç vaxt "yüklənir" vəziyyətində ilişib qalmır.
async function resolveAccount() {
  const accRes = await Api.get('/accounts');
  allAccounts = accRes.accounts || [];
  if (!allAccounts.length) throw new Error('Backend heç bir hesab qaytarmadı (ms-pasha / abb-bank-service işləyirmi?)');

  const requested = accountId;   // URL-dən gələn ilkin id (aşağıda accountId dəyişdirilə bilər)
  // Boşluq / görünməz simvol / hərf registri fərqi tapılmasına mane olmasın: yalnız hərf-rəqəm müqayisə edilir
  const norm = v => String(v == null ? '' : v).replace(/[^A-Za-z0-9-]/g, '').toLowerCase();
  const want = norm(requested);
  let found = allAccounts.find(a => norm(a.id) === want || norm(a.iban) === want);
  if (!found) {
    const m = want.match(/^([a-z]{3})-\d+$/);
    if (m) found = allAccounts.find(a => String(a.currency).toLowerCase() === m[1]);
  }
  if (!found) found = allAccounts[0];

  // Versiya möhürü: brauzerin köhnə (keşdəki) account.js işlətdiyini dərhal göstərmək üçün
  const stamp = document.getElementById('buildStamp') || document.querySelector('.main').appendChild(Object.assign(document.createElement('div'), { id: 'buildStamp' }));
  stamp.style.cssText = 'margin:18px 0 6px;font-size:11px;color:#8a94ad;text-align:center';
  stamp.textContent = `account.js build r7 · soruşulan id: "${requested}" · tapılan: ${found.currency} (${found.id})`;
  console.info('[account] URL:', location.href, '| soruşulan id:', requested, '| tapılan hesab:', found.id, found.currency);
  if (found.id !== accountId) {
    console.warn('[account] URL-dəki id backend siyahısında tapılmadı, əvəzinə seçildi:', found.id, found.currency);
    // DİQQƏT: accountId aşağıda dəyişir — toast-da ilkin (soruşulan) id göstərilməlidir, ona görə `requested`
    const knownIds = allAccounts.map(a => a.id).join(', ');
    console.warn('[account] soruşulan id:', JSON.stringify(requested), '| backend siyahısı:', knownIds);
    setTimeout(() => showToast(`Sorğulanan hesab tapılmadı: "${requested}" (uzunluq ${String(requested).length}). Göstərilir: ${found.currency}`, 'error'), 300);
    accountId = found.id;
    const p = new URLSearchParams(location.search);
    p.set('id', found.id);
    try { history.replaceState(null, '', location.pathname + '?' + p.toString()); } catch (e) { /* file:// və s. */ }
  }
  return found;
}

async function load() {
  renderServices();
  setDefaultDates();
  renderTemplates();
  renderStanding();

  try {
    const listed = await resolveAccount();
    renderTemplates(); renderStanding();   // accountId dəyişmiş ola bilər — şablonlar/daimi ödənişlər düzgün açarla yenidən çəkilsin
    try {
      const res = await Api.get(`/accounts/${encodeURIComponent(accountId)}`);
      const d = res.account;
      // backend başqa hesabı qaytarıbsa (id/IBAN uyğun gəlmir) — siyahıdakı düzgün hesaba üstünlük veririk
      const same = d && (String(d.id).toLowerCase() === String(accountId).toLowerCase()
                      || String(d.iban || '').toLowerCase() === String(listed.iban || '').toLowerCase());
      account = same ? d : listed;
    } catch (e) {
      console.warn('Hesab detalı alınmadı, siyahıdakı məlumat istifadə olunur', e);
      account = listed;
    }

    document.getElementById('accFlag').innerHTML = flagImg(account.currency, 44);
    document.getElementById('accName').textContent = accountLabel(account);
    document.getElementById('accIban').textContent = account.iban;
    document.getElementById('accStatus').innerHTML = `<span class="dot"></span>${account.status}`;
    document.getElementById('sumBalance').textContent = maskOrShow(Fmt.money(account.balance, account.currency));

    renderAccountSwitch();

    await loadOperations();
  } catch (e) {
    console.error(e);
    document.querySelector('.main').insertAdjacentHTML('beforeend',
      `<p style="color:#d84343">Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı? (mvn spring-boot:run)<br><small>${escHtml(e && e.message)}</small></p>`);
  }
}

document.getElementById('hideBtn').addEventListener('click', () => {
  hidden = !hidden;
  document.getElementById('hideBtn').innerHTML = `${Icons.eyeOff} ${hidden ? 'Məbləğləri göstər' : 'Məbləğləri gizlət'}`;
  document.getElementById('sumBalance').textContent = maskOrShow(Fmt.money(account.balance, account.currency));
  loadOperations();
});

document.getElementById('wholePeriodBtn').addEventListener('click', () => {
  document.getElementById('startDate').value = '2000-01-01';
  // "Bütün dövr": demo tarixdən sonra yaranan (məs. indi göndərilən) əməliyyatlar da görünsün
  const now = new Date();
  document.getElementById('endDate').value = Fmt.isoDate(now > demoNow() ? now : demoNow());
  loadOperations();
});

['startDate', 'endDate', 'directionSelect', 'statusSelect', 'sortSelect'].forEach(id => {
  document.getElementById(id).addEventListener('change', loadOperations);
});
document.getElementById('searchInput').addEventListener('input', () => {
  clearTimeout(window.__searchTimer);
  window.__searchTimer = setTimeout(loadOperations, 250);
});

// ---------------------------------------------------------------
// Tabs
// ---------------------------------------------------------------
function switchTab(tab) {
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
  document.querySelectorAll('.tab-panel').forEach(p => p.classList.toggle('active', p.id === 'panel-' + tab));
}
document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => switchTab(btn.dataset.tab));
});

// ---------------------------------------------------------------
// Export: CSV / Excel
// ---------------------------------------------------------------
document.getElementById('exportCsvBtn').addEventListener('click', () => {
  if (!lastOperations.length) { showToast('İxrac üçün əməliyyat yoxdur', 'error'); return; }
  const header = ['Tarix', 'Qarşı tərəf', 'Təyinat', 'Məbləğ', 'Valyuta', 'İstiqamət', 'Status'];
  const rows = lastOperations.map(op => [
    Fmt.dateAz(op.date),
    op.counterparty,
    op.description,
    (op.direction === 'in' ? '+' : '-') + Number(op.amount).toFixed(2),
    op.currency,
    op.direction === 'in' ? 'Mədaxil' : 'Məxaric',
    statusLabel(op.status),
  ]);
  const csv = [header, ...rows]
    .map(r => r.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(';'))
    .join('\r\n');
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${account ? account.name : 'hesab'}-emeliyyatlar.csv`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
  showToast('CSV fayl endirildi');
});

// ---------------------------------------------------------------
// Export: PDF (browser print — user picks "Save as PDF" in the print dialog)
// ---------------------------------------------------------------
document.getElementById('exportPdfBtn').addEventListener('click', () => {
  switchTab('ops');
  window.print();
});

// ---------------------------------------------------------------
// Analytics tab
// ---------------------------------------------------------------
function renderAnalytics(summary) {
  document.getElementById('analyticsSummary').innerHTML = `
    <div class="box"><div class="lbl">Mədaxil</div><div class="val" style="color:var(--green)">${maskOrShow('+' + Fmt.money(summary.income, account.currency))}</div></div>
    <div class="box"><div class="lbl">Məxaric</div><div class="val" style="color:var(--red)">${maskOrShow('−' + Fmt.money(summary.expense, account.currency))}</div></div>
    <div class="box"><div class="lbl">Əməliyyat sayı</div><div class="val">${lastOperations.length}</div></div>
  `;

  const byParty = {};
  lastOperations.forEach(op => {
    const key = op.counterparty || 'Digər';
    if (!byParty[key]) byParty[key] = { in: 0, out: 0 };
    byParty[key][op.direction === 'in' ? 'in' : 'out'] += Number(op.amount);
  });
  const entries = Object.entries(byParty)
    .map(([name, v]) => ({ name, total: v.in + v.out, dir: v.in >= v.out ? 'in' : 'out' }))
    .sort((a, b) => b.total - a.total);

  const barsEl = document.getElementById('analyticsBars');
  const emptyEl = document.getElementById('analyticsEmpty');
  if (entries.length === 0) {
    barsEl.innerHTML = '';
    emptyEl.style.display = 'block';
    return;
  }
  emptyEl.style.display = 'none';
  const max = Math.max(...entries.map(e => e.total), 1);
  barsEl.innerHTML = entries.map(e => `
    <div class="bar-row">
      <div class="bar-label" title="${e.name}">${e.name}</div>
      <div class="bar-track"><div class="bar-fill ${e.dir === 'out' ? 'out' : ''}" style="width:${(e.total / max * 100).toFixed(1)}%"></div></div>
      <div class="bar-value">${maskOrShow(Fmt.money(e.total, account.currency))}</div>
    </div>`).join('');
}

// ---------------------------------------------------------------
// Templates (localStorage — PASHA Bank's API has no "saved templates" endpoint)
// ---------------------------------------------------------------
function templatesKey() { return `corbank_templates_${accountId}`; }
function getTemplates() { try { return JSON.parse(localStorage.getItem(templatesKey())) || []; } catch { return []; } }
function saveTemplates(list) { localStorage.setItem(templatesKey(), JSON.stringify(list)); }

function renderTemplates() {
  const list = getTemplates();
  const el = document.getElementById('templatesList');
  const empty = document.getElementById('templatesEmpty');
  if (list.length === 0) { el.innerHTML = ''; empty.style.display = 'block'; return; }
  empty.style.display = 'none';
  el.innerHTML = list.map((t, i) => `
    <div class="list-row">
      <div class="info">
        <div class="name">${t.name}</div>
        <div class="meta">${t.iban} · ${Fmt.money(t.amount, account ? account.currency : 'AZN')}</div>
      </div>
      <div class="actions">
        <button class="btn-primary" data-use="${i}">İstifadə et</button>
        <button class="btn-ghost" data-del="${i}">Sil</button>
      </div>
    </div>`).join('');
  el.querySelectorAll('[data-use]').forEach(b => b.addEventListener('click', () => {
    const t = list[Number(b.dataset.use)];
    openFieldWizard(domesticConfig(), { beneficiaryName: t.name, beneficiaryIban: t.iban, amount: t.amount });
  }));
  el.querySelectorAll('[data-del]').forEach(b => b.addEventListener('click', () => {
    list.splice(Number(b.dataset.del), 1);
    saveTemplates(list);
    renderTemplates();
    showToast('Şablon silindi');
  }));
}

document.getElementById('addTemplateBtn').addEventListener('click', () => {
  openModal({
    title: 'Yeni şablon',
    bodyHtml: `
      <div class="modal-field"><label>Şablon adı / Alıcı</label><input id="tplName" placeholder="Məs: Ofis icarəsi"></div>
      <div class="modal-field"><label>IBAN</label><input id="tplIban" placeholder="AZ.."></div>
      <div class="modal-field"><label>Məbləğ</label><input id="tplAmount" type="number" step="0.01" placeholder="0.00"></div>
    `,
    footerHtml: `<button class="btn-ghost" onclick="closeModal()">Ləğv et</button><button class="btn-primary" id="tplSaveBtn">Yadda saxla</button>`
  });
  document.getElementById('tplSaveBtn').addEventListener('click', () => {
    const name = document.getElementById('tplName').value.trim();
    const iban = document.getElementById('tplIban').value.trim();
    const amount = parseFloat(document.getElementById('tplAmount').value);
    if (!name || !iban || !amount) { showToast('Bütün sahələri doldurun', 'error'); return; }
    const list = getTemplates();
    list.push({ name, iban, amount });
    saveTemplates(list);
    renderTemplates();
    closeModal();
    showToast('Şablon yaradıldı');
  });
});

// ---------------------------------------------------------------
// Standing (recurring) payments — localStorage, same reasoning as templates
// ---------------------------------------------------------------
function standingKey() { return `corbank_standing_${accountId}`; }
function getStanding() { try { return JSON.parse(localStorage.getItem(standingKey())) || []; } catch { return []; } }
function saveStanding(list) { localStorage.setItem(standingKey(), JSON.stringify(list)); }

function renderStanding() {
  const list = getStanding();
  const el = document.getElementById('standingList');
  const empty = document.getElementById('standingEmpty');
  if (list.length === 0) { el.innerHTML = ''; empty.style.display = 'block'; return; }
  empty.style.display = 'none';
  el.innerHTML = list.map((s, i) => `
    <div class="list-row">
      <div class="info">
        <div class="name">${s.name}</div>
        <div class="meta">${Fmt.money(s.amount, account ? account.currency : 'AZN')} · ${s.frequency} · növbəti: ${s.nextDate}</div>
      </div>
      <div class="actions"><button class="btn-ghost" data-del="${i}">Sil</button></div>
    </div>`).join('');
  el.querySelectorAll('[data-del]').forEach(b => b.addEventListener('click', () => {
    list.splice(Number(b.dataset.del), 1);
    saveStanding(list);
    renderStanding();
    showToast('Daimi ödəniş silindi');
  }));
}

document.getElementById('addStandingBtn').addEventListener('click', () => {
  openModal({
    title: 'Yeni daimi ödəniş',
    bodyHtml: `
      <div class="modal-field"><label>Alıcı</label><input id="stName" placeholder="Məs: Ofis icarəsi"></div>
      <div class="modal-field"><label>Məbləğ</label><input id="stAmount" type="number" step="0.01" placeholder="0.00"></div>
      <div class="modal-field"><label>Dövrilik</label>
        <select id="stFreq"><option>Aylıq</option><option>Həftəlik</option><option>Rüblük</option></select>
      </div>
      <div class="modal-field"><label>Növbəti tarix</label><input id="stDate" type="date"></div>
    `,
    footerHtml: `<button class="btn-ghost" onclick="closeModal()">Ləğv et</button><button class="btn-primary" id="stSaveBtn">Yadda saxla</button>`
  });
  document.getElementById('stSaveBtn').addEventListener('click', () => {
    const name = document.getElementById('stName').value.trim();
    const amount = parseFloat(document.getElementById('stAmount').value);
    const frequency = document.getElementById('stFreq').value;
    const nextDate = document.getElementById('stDate').value || '—';
    if (!name || !amount) { showToast('Bütün sahələri doldurun', 'error'); return; }
    const list = getStanding();
    list.push({ name, amount, frequency, nextDate });
    saveStanding(list);
    renderStanding();
    closeModal();
    showToast('Daimi ödəniş yaradıldı');
  });
});

// ---------------------------------------------------------------
// Shared FX helpers (used by the transfer/FX wizards)
// ---------------------------------------------------------------
function fxRateFor(cur, rates) {
  if (cur === 'AZN') return { buy: 1, sell: 1 };
  return rates.find(x => x.currency === cur) || { buy: 1, sell: 1 };
}
function fxConvert(amount, fromCur, toCur, rates) {
  if (fromCur === toCur) return amount;
  const inAzn = fromCur === 'AZN' ? amount : amount * fxRateFor(fromCur, rates).sell;
  return toCur === 'AZN' ? inAzn : inAzn / fxRateFor(toCur, rates).buy;
}

const COMPANY_NAME = window.CORBANK_COMPANY_NAME || 'Sizin biznesiniz';

// Re-fetch this account + its operations after a successful payment, so the balance card,
// the transactions table and the analytics tab all reflect the real (mock-ledger) change
// once the modal closes — not just a toast.
async function refreshAfterPayment() {
  try {
    const res = await Api.get(`/accounts/${encodeURIComponent(accountId)}`);
    account = res.account;
    document.getElementById('sumBalance').textContent = maskOrShow(Fmt.money(account.balance, account.currency));
    await loadOperations();
  } catch (e) {
    console.error('refreshAfterPayment failed', e);
  }
}

// Shared "Nəticə" (result) step body/footer for every wizard.
function wizardResultBodyHtml({ ok, amountText, message, source }) {
  return `
  <div class="result-box">
    <div class="result-icon ${ok ? 'success' : 'error'}">${ok ? '✓' : '!'}</div>
    <div class="result-title">${ok ? 'Demo əməliyyat yerinə yetirildi' : 'Əməliyyat uğursuz oldu'}</div>
    ${amountText ? `<div class="result-amount">${amountText}</div>` : ''}
    ${ok ? `<div class="result-note">Hesab qalığı, əməliyyatlar və analitika yeniləndi.</div>` : ''}
    ${ok && source === 'mock' ? `<div class="result-disclaimer">Real pul köçürülməyib. Demo dəyişiklikləri backend işlədiyi müddətcə saxlanılır.</div>` : ''}
    ${!ok ? `<div class="result-sub">${message || ''}</div>` : ''}
  </div>`;
}
function wizardResultFooterHtml(ok) {
  return ok
    ? `<button class="btn-primary" onclick="closeModal()">Hesaba qayıt</button>`
    : `<button class="btn-ghost" onclick="closeModal()">Bağla</button><button class="btn-primary" id="wizRetryBtn">Yenidən cəhd et</button>`;
}

// ---------------------------------------------------------------
// Shared 3-step wizard chrome: step indicator + source-account box
// ---------------------------------------------------------------
function wizardStepsHtml(step) {
  const labels = ['Məlumatlar', 'Yoxlama', 'Nəticə'];
  return `
  <div class="wizard-steps">
    ${labels.map((label, i) => {
      const n = i + 1;
      const cls = n < step ? 'done' : (n === step ? 'active' : '');
      return `
      <div class="wizard-step ${cls}">
        <span class="circle">${n < step ? '✓' : n}</span>
        <span>${label}</span>
      </div>
      ${n < labels.length ? '<div class="wizard-line"></div>' : ''}`;
    }).join('')}
  </div>`;
}
function wizardSubtitleHtml() {
  return `<div style="color:var(--muted); font-size:13px; margin-top:-6px;">${escHtml(Company.current().name)} · ${escHtml(account.name)}</div>`;
}
function wizardSourceBoxHtml() {
  return `
  <div class="source-box">
    <div class="lbl">Mənbə hesabı · ${account.currency}</div>
    <div class="val">${account.iban}</div>
    <div class="sub">Mövcud qalıq: ${Fmt.money(account.balance, account.currency)}</div>
  </div>`;
}

// ---------------------------------------------------------------
// Generic field-driven wizard — used by all "payment-style" services below.
// cfg = {
//   title, successTitle,
//   fields: [{ id, label, type: 'text'|'number'|'select', options?, placeholder?, defaultValue?, optional? }],
//   buildPayload(values) -> { fromAccountId, beneficiaryName, beneficiaryIban, amount, currency, description },
//   reviewRows(values) -> [[label, value], ...]
// }
// ---------------------------------------------------------------
function openFieldWizard(cfg, prefill = {}) {
  const state = { step: 1, values: {}, result: null };
  cfg.fields.forEach(f => {
    state.values[f.id] = prefill[f.id] !== undefined ? prefill[f.id]
      : (f.defaultValue !== undefined ? f.defaultValue : '');
  });

  function fieldHtml(f) {
    const val = state.values[f.id];
    if (f.type === 'select') {
      return `<div class="modal-field"><label>${f.label}</label><select id="wf_${f.id}">
        ${f.options.map(o => `<option value="${o.value}" ${o.value === val ? 'selected' : ''}>${o.label}</option>`).join('')}
      </select></div>`;
    }
    return `<div class="modal-field"><label>${f.label}</label>
      <input id="wf_${f.id}" type="${f.type === 'number' ? 'number' : 'text'}" ${f.type === 'number' ? 'step="0.01"' : ''}
        placeholder="${f.placeholder || ''}" value="${val}"></div>`;
  }

  function fieldsGridHtml() {
    const chunks = [];
    for (let i = 0; i < cfg.fields.length; i += 2) chunks.push(cfg.fields.slice(i, i + 2));
    return chunks.map(pair => pair.length === 2
      ? `<div class="two-col">${fieldHtml(pair[0])}${fieldHtml(pair[1])}</div>`
      : fieldHtml(pair[0])
    ).join('');
  }

  function bindFieldEvents() {
    cfg.fields.forEach(f => {
      const el = document.getElementById('wf_' + f.id);
      el.addEventListener(f.type === 'select' ? 'change' : 'input', () => {
        // f.format: optional live input mask (e.g. card number grouping) — keeps caret at the end.
        const v = f.format ? f.format(el.value) : el.value;
        if (f.format && v !== el.value) el.value = v;
        state.values[f.id] = v;
      });
    });
  }

  function renderStep1() {
    openModal({
      title: cfg.title,
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(1)}
        ${wizardSourceBoxHtml()}
        ${fieldsGridHtml()}
        <div id="wfMsg"></div>
      `,
      footerHtml: `<button class="btn-ghost" onclick="closeModal()">Ləğv et</button>
                   <button class="btn-primary" id="wfNextBtn">Davam et ${Icons.send}</button>`
    });
    bindFieldEvents();

    document.getElementById('wfNextBtn').addEventListener('click', () => {
      for (const f of cfg.fields) {
        if (f.optional || f.type === 'select') continue;
        const v = state.values[f.id];
        if (!v || (f.type === 'number' && parseFloat(v) <= 0)) {
          document.getElementById('wfMsg').innerHTML = `<p style="color:var(--red); font-size:13px;">"${f.label}" sahəsini düzgün doldurun.</p>`;
          return;
        }
        // f.validate: optional custom rule (e.g. card number format) -> returns an error string or null/undefined.
        if (f.validate) {
          const err = f.validate(v);
          if (err) {
            document.getElementById('wfMsg').innerHTML = `<p style="color:var(--red); font-size:13px;">${err}</p>`;
            return;
          }
        }
      }
      if ('amount' in state.values && parseFloat(state.values.amount) > Number(account.balance)) {
        document.getElementById('wfMsg').innerHTML = `<p style="color:var(--red); font-size:13px;">Mövcud qalıqdan artıq məbləğ daxil edilib.</p>`;
        return;
      }
      state.step = 2;
      renderStep2();
    });
  }

  function renderStep2() {
    const rows = cfg.reviewRows(state.values);
    openModal({
      title: cfg.title,
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(2)}
        ${rows.map(([k, v]) => `<div class="review-row"><span class="k">${k}</span><span class="v">${v}</span></div>`).join('')}
      `,
      footerHtml: `<button class="btn-ghost" id="wfBackBtn">Geri</button><button class="btn-primary" id="wfConfirmBtn">Təsdiqlə</button>`
    });
    document.getElementById('wfBackBtn').addEventListener('click', () => { state.step = 1; renderStep1(); });

    document.getElementById('wfConfirmBtn').addEventListener('click', async () => {
      const btn = document.getElementById('wfConfirmBtn');
      btn.disabled = true; btn.textContent = 'Göndərilir...';
      try {
        const res = await Api.post('/payments', cfg.buildPayload(state.values));
        state.result = {
          ok: res.status !== 'rejected',
          message: res.message || '',
          source: res.source
        };
        if (state.result.ok) await refreshAfterPayment();
      } catch (e) {
        console.error(e);
        state.result = { ok: false, message: 'Backend ilə əlaqə mümkün olmadı.', source: 'mock' };
      }
      state.step = 3;
      renderStep3();
    });
  }

  function renderStep3() {
    const ok = state.result && state.result.ok;
    const amountText = 'amount' in state.values ? Fmt.money(state.values.amount, account.currency) : '';
    openModal({
      title: cfg.title,
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(3)}
        ${wizardResultBodyHtml({ ok, amountText, message: state.result ? state.result.message : '', source: state.result ? state.result.source : 'mock' })}
      `,
      footerHtml: wizardResultFooterHtml(ok)
    });
    if (!ok) document.getElementById('wizRetryBtn').addEventListener('click', () => { state.step = 2; renderStep2(); });
    else showToast(cfg.successTitle || 'Əməliyyat tamamlandı');
  }

  renderStep1();
}

function serviceLabel(v) {
  return ({ electric: 'Elektrik enerjisi', gas: 'Qaz', water: 'Su', internet: 'İnternet / TV' })[v] || v;
}

// ---------------------------------------------------------------
// Per-service wizard configs
// ---------------------------------------------------------------
function domesticConfig() {
  return {
    title: 'Ölkədaxili köçürmə',
    fields: [
      { id: 'beneficiaryName', label: 'Alıcının adı', type: 'text', placeholder: 'Şirkətin adı' },
      { id: 'beneficiaryIban', label: 'Alıcının IBAN / hesab nömrəsi', type: 'text', placeholder: 'AZ..' },
      { id: 'amount', label: `Məbləğ (${account.currency})`, type: 'number', placeholder: '0,00' },
      { id: 'description', label: 'Təyinat', type: 'text', optional: true },
    ],
    buildPayload: v => ({
      fromAccountId: accountId, beneficiaryName: v.beneficiaryName, beneficiaryIban: v.beneficiaryIban,
      amount: parseFloat(v.amount), currency: account.currency, description: v.description || 'Ölkədaxili köçürmə'
    }),
    reviewRows: v => [
      ['Alıcı', v.beneficiaryName], ['IBAN', v.beneficiaryIban],
      ['Məbləğ', Fmt.money(v.amount, account.currency)], ['Təyinat', v.description || '—']
    ]
  };
}

function internationalConfig() {
  const countries = [
    { value: 'Türkiyə', label: 'Türkiyə' }, { value: 'Almaniya', label: 'Almaniya' },
    { value: 'Böyük Britaniya', label: 'Böyük Britaniya' }, { value: 'ABŞ', label: 'ABŞ' },
    { value: 'BƏƏ', label: 'BƏƏ' }, { value: 'Gürcüstan', label: 'Gürcüstan' }, { value: 'Digər', label: 'Digər' },
  ];
  return {
    title: 'Ölkəxarici köçürmə',
    fields: [
      { id: 'beneficiaryName', label: 'Benefisiarın adı', type: 'text', placeholder: 'Şirkətin adı' },
      { id: 'country', label: 'Ölkə', type: 'select', options: countries },
      { id: 'beneficiaryBank', label: 'Benefisiarın bankı', type: 'text', optional: true },
      { id: 'swift', label: 'SWIFT / BIC', type: 'text', placeholder: 'CHASUS33' },
      { id: 'beneficiaryIban', label: 'Alıcının IBAN / hesab nömrəsi', type: 'text', placeholder: 'AZ..' },
      { id: 'currency', label: 'Köçürmə valyutası', type: 'select', defaultValue: account.currency,
        options: ['AZN', 'USD', 'EUR'].map(c => ({ value: c, label: c })) },
      { id: 'amount', label: 'Məbləğ', type: 'number', placeholder: '0,00' },
      { id: 'commission', label: 'Komissiya seçimi', type: 'select', options: [
        { value: 'SHA — bölüşdürülmüş', label: 'SHA — bölüşdürülmüş' },
        { value: 'OUR — göndərən ödəyir', label: 'OUR — göndərən ödəyir' },
        { value: 'BEN — alıcı ödəyir', label: 'BEN — alıcı ödəyir' },
      ] },
      { id: 'description', label: 'Ödənişin təyinatı', type: 'text', optional: true },
    ],
    buildPayload: v => ({
      fromAccountId: accountId, beneficiaryName: v.beneficiaryName, beneficiaryIban: v.beneficiaryIban,
      amount: parseFloat(v.amount), currency: v.currency,
      description: `${v.description || 'Ölkəxarici köçürmə'} · SWIFT ${v.swift} · ${v.commission}`
    }),
    reviewRows: v => [
      ['Benefisiar', v.beneficiaryName], ['Ölkə', v.country], ['Bank', v.beneficiaryBank || '—'],
      ['SWIFT/BIC', v.swift], ['IBAN', v.beneficiaryIban], ['Valyuta', v.currency],
      ['Məbləğ', Fmt.money(v.amount, v.currency)], ['Komissiya', v.commission], ['Təyinat', v.description || '—']
    ]
  };
}

function instantConfig() {
  return {
    title: 'Ani ödəniş',
    fields: [
      { id: 'idType', label: 'İdentifikator növü', type: 'select', options: [
        { value: 'Mobil nömrə', label: 'Mobil nömrə' }, { value: 'Kart nömrəsi', label: 'Kart nömrəsi' },
        { value: 'Hesab nömrəsi', label: 'Hesab nömrəsi' },
      ] },
      { id: 'idValue', label: 'İdentifikator', type: 'text', placeholder: '+994501234567' },
      { id: 'amount', label: `Məbləğ (${account.currency})`, type: 'number', placeholder: '0,00' },
      { id: 'description', label: 'Təyinat', type: 'text', optional: true },
    ],
    buildPayload: v => ({
      fromAccountId: accountId, beneficiaryName: v.idValue, beneficiaryIban: v.idValue,
      amount: parseFloat(v.amount), currency: account.currency, description: v.description || 'Ani ödəniş'
    }),
    reviewRows: v => [
      ['İdentifikator', `${v.idType} · ${v.idValue}`],
      ['Məbləğ', Fmt.money(v.amount, account.currency)], ['Təyinat', v.description || '—']
    ]
  };
}

// Kart nömrəsini "0000 0000 0000 0000" formatında canlı formatlayır (max 19 rəqəm — Visa/MC/Mir).
function formatCardNumber(raw) {
  const digits = raw.replace(/\D/g, '').slice(0, 19);
  return digits.replace(/(.{4})/g, '$1 ').trim();
}

function cardToCardConfig() {
  return {
    title: 'Kartdan-karta köçürmə',
    successTitle: 'Kartdan-karta köçürmə tamamlandı',
    fields: [
      { id: 'cardNumber', label: 'Alıcının kart nömrəsi', type: 'text', placeholder: '0000 0000 0000 0000',
        format: formatCardNumber,
        validate: v => (/^\d{13,19}$/.test(v.replace(/\s+/g, '')) ? null : 'Kart nömrəsini düzgün daxil edin (13-19 rəqəm).') },
      { id: 'cardHolderName', label: 'Kart sahibinin adı', type: 'text', placeholder: 'Ad Soyad', optional: true },
      { id: 'amount', label: `Məbləğ (${account.currency})`, type: 'number', placeholder: '0,00' },
      { id: 'description', label: 'Təyinat', type: 'text', optional: true },
    ],
    buildPayload: v => {
      const digits = v.cardNumber.replace(/\s+/g, '');
      const masked = `${digits.slice(0, 4)} •••• •••• ${digits.slice(-4)}`;
      return {
        fromAccountId: accountId,
        beneficiaryName: v.cardHolderName || masked,
        beneficiaryIban: digits,
        amount: parseFloat(v.amount), currency: account.currency,
        description: v.description || `Kartdan-karta köçürmə · ${masked}`
      };
    },
    reviewRows: v => {
      const digits = v.cardNumber.replace(/\s+/g, '');
      const masked = `${digits.slice(0, 4)} •••• •••• ${digits.slice(-4)}`;
      return [
        ['Kart nömrəsi', masked], ['Kart sahibi', v.cardHolderName || '—'],
        ['Məbləğ', Fmt.money(v.amount, account.currency)], ['Təyinat', v.description || '—']
      ];
    }
  };
}

function budgetConfig() {
  return {
    title: 'Büdcə və vergi ödənişləri',
    fields: [
      { id: 'taxType', label: 'Vergi növü', type: 'select', options: [
        { value: 'Mənfəət vergisi', label: 'Mənfəət vergisi' }, { value: 'ƏDV', label: 'ƏDV' },
        { value: 'Gəlir vergisi', label: 'Gəlir vergisi' }, { value: 'Sosial sığorta', label: 'Sosial sığorta' },
      ] },
      { id: 'voen', label: 'VÖEN', type: 'text', placeholder: '' },
      { id: 'budgetCode', label: 'Büdcə təsnifat kodu', type: 'text', defaultValue: '111110' },
      { id: 'amount', label: `Məbləğ (${account.currency})`, type: 'number', placeholder: '0,00' },
      { id: 'period', label: 'Ödəniş dövrü / təyinat', type: 'text', placeholder: 'Aprel 2025' },
    ],
    buildPayload: v => ({
      fromAccountId: accountId, beneficiaryName: 'Dövlət Xəzinədarlığı', beneficiaryIban: 'AZ00STATEBUDGET000000000000',
      amount: parseFloat(v.amount), currency: account.currency,
      description: `${v.taxType} · VÖEN ${v.voen} · ${v.budgetCode} · ${v.period}`
    }),
    reviewRows: v => [
      ['Vergi növü', v.taxType], ['VÖEN', v.voen || '—'], ['Büdcə kodu', v.budgetCode],
      ['Məbləğ', Fmt.money(v.amount, account.currency)], ['Dövr / təyinat', v.period || '—']
    ]
  };
}

function utilityConfig() {
  return {
    title: 'Kommunal və digər xidmət ödənişləri',
    fields: [
      { id: 'serviceType', label: 'Xidmət növü', type: 'select', options: [
        { value: 'electric', label: 'Elektrik enerjisi' }, { value: 'gas', label: 'Qaz' },
        { value: 'water', label: 'Su' }, { value: 'internet', label: 'İnternet / TV' },
      ] },
      { id: 'subscriberId', label: 'Abonent nömrəsi', type: 'text', placeholder: '' },
      { id: 'amount', label: `Məbləğ (${account.currency})`, type: 'number', placeholder: '0,00' },
      { id: 'description', label: 'Təyinat', type: 'text', optional: true },
    ],
    buildPayload: v => ({
      fromAccountId: accountId, beneficiaryName: serviceLabel(v.serviceType), beneficiaryIban: v.subscriberId,
      amount: parseFloat(v.amount), currency: account.currency, description: v.description || serviceLabel(v.serviceType)
    }),
    reviewRows: v => [
      ['Xidmət', serviceLabel(v.serviceType)], ['Abonent', v.subscriberId || '—'],
      ['Məbləğ', Fmt.money(v.amount, account.currency)], ['Təyinat', v.description || '—']
    ]
  };
}

// ---------------------------------------------------------------
// Own-accounts transfer — 3-step wizard: Məlumatlar → Yoxlama → Nəticə
// ---------------------------------------------------------------
async function openOwnTransferModal() {
  const others = allAccounts.filter(a => a.id !== accountId && sameBank(a, account));
  if (others.length === 0) {
    showToast('Eyni bankda başqa hesabınız yoxdur', 'error');
    return;
  }
  let rates = [];
  try {
    const dash = await Api.get('/dashboard');
    rates = dash.exchangeRates || [];
  } catch (e) { /* keep going with empty rates (1:1) */ }

  const state = {
    step: 1,
    targetId: others[0].id,
    amount: '',
    description: '',
    result: null, // { ok, message }
  };

  function targetAccount() { return others.find(a => a.id === state.targetId); }

  function renderStep1() {
    const target = targetAccount();
    const showConv = target.currency !== account.currency;
    const amountNum = parseFloat(state.amount) || 0;
    const rate = fxConvert(1, account.currency, target.currency, rates);
    const converted = fxConvert(amountNum, account.currency, target.currency, rates);

    openModal({
      title: 'Öz hesablarım arasında köçürmə',
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(1)}
        ${wizardSourceBoxHtml()}
        <div class="two-col">
          <div class="modal-field">
            <label>Hədəf hesabı</label>
            <select id="w1Target">
              ${others.map(a => `<option value="${escHtml(a.id)}" ${a.id === state.targetId ? 'selected' : ''}>${a.currency}</option>`).join('')}
            </select>
          </div>
          <div class="modal-field">
            <label>Məbləğ (${account.currency})</label>
            <input id="w1Amount" type="number" step="0.01" placeholder="0,00" value="${state.amount}">
          </div>
        </div>
        ${showConv ? `
        <div class="conv-box">
          <span>Demo məzənnə · 1 ${account.currency} = ${rate.toFixed(4)} ${target.currency}</span>
          <span class="conv-result">${converted.toFixed(2)} ${target.currency}</span>
        </div>` : ''}
        <div class="modal-field">
          <label>Təyinat</label>
          <input id="w1Desc" placeholder="Ödənişin təyinatı" value="${state.description}">
        </div>
        <div id="w1Msg"></div>
      `,
      footerHtml: `<button class="btn-ghost" onclick="closeModal()">Ləğv et</button>
                   <button class="btn-primary" id="w1NextBtn">Davam et ${Icons.send}</button>`
    });

    document.getElementById('w1Target').addEventListener('change', (e) => {
      state.targetId = e.target.value;
      state.amount = document.getElementById('w1Amount').value;
      state.description = document.getElementById('w1Desc').value;
      renderStep1();
    });
    document.getElementById('w1Amount').addEventListener('input', (e) => {
      state.amount = e.target.value;
      renderStep1();
      // keep focus + caret after re-render
      const el = document.getElementById('w1Amount');
      el.focus(); el.setSelectionRange(el.value.length, el.value.length);
    });
    document.getElementById('w1Desc').addEventListener('input', (e) => { state.description = e.target.value; });

    document.getElementById('w1NextBtn').addEventListener('click', () => {
      const amt = parseFloat(state.amount);
      if (!amt || amt <= 0) {
        document.getElementById('w1Msg').innerHTML = `<p style="color:var(--red); font-size:13px;">Məbləği düzgün daxil edin.</p>`;
        return;
      }
      if (amt > Number(account.balance)) {
        document.getElementById('w1Msg').innerHTML = `<p style="color:var(--red); font-size:13px;">Mövcud qalıqdan artıq məbləğ daxil edilib.</p>`;
        return;
      }
      state.step = 2;
      renderStep2();
    });
  }

  function renderStep2() {
    const target = targetAccount();
    const amountNum = parseFloat(state.amount) || 0;
    const showConv = target.currency !== account.currency;
    const converted = fxConvert(amountNum, account.currency, target.currency, rates);

    openModal({
      title: 'Öz hesablarım arasında köçürmə',
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(2)}
        <div class="review-row"><span class="k">Mənbə hesabı</span><span class="v">${account.name} · ${account.iban}</span></div>
        <div class="review-row"><span class="k">Hədəf hesabı</span><span class="v">${target.name} · ${target.iban}</span></div>
        <div class="review-row"><span class="k">Göndərilən məbləğ</span><span class="v">${Fmt.money(amountNum, account.currency)}</span></div>
        ${showConv ? `<div class="review-row"><span class="k">Alınacaq məbləğ (təxmini)</span><span class="v">${Fmt.money(converted, target.currency)}</span></div>` : ''}
        <div class="review-row"><span class="k">Təyinat</span><span class="v">${state.description || '—'}</span></div>
      `,
      footerHtml: `<button class="btn-ghost" id="w2BackBtn">Geri</button>
                   <button class="btn-primary" id="w2ConfirmBtn">Təsdiqlə</button>`
    });

    document.getElementById('w2BackBtn').addEventListener('click', () => { state.step = 1; renderStep1(); });

    document.getElementById('w2ConfirmBtn').addEventListener('click', async () => {
      const btn = document.getElementById('w2ConfirmBtn');
      btn.disabled = true; btn.textContent = 'Göndərilir...';
      try {
        const res = await Api.post('/payments', {
          fromAccountId: accountId,
          beneficiaryName: target.name,
          beneficiaryIban: target.iban,
          amount: amountNum,
          currency: account.currency,
          description: state.description || 'Öz hesablarım arasında köçürmə',
          creditAmount: showConv ? converted : undefined,
          creditCurrency: showConv ? target.currency : undefined,
        });
        state.result = { ok: res.status !== 'rejected', message: res.message || '', source: res.source };
        if (state.result.ok) await refreshAfterPayment();
      } catch (e) {
        console.error(e);
        state.result = { ok: false, message: 'Backend ilə əlaqə mümkün olmadı.', source: 'mock' };
      }
      state.step = 3;
      renderStep3();
    });
  }

  function renderStep3() {
    const amountNum = parseFloat(state.amount) || 0;
    const ok = state.result && state.result.ok;

    openModal({
      title: 'Öz hesablarım arasında köçürmə',
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(3)}
        ${wizardResultBodyHtml({ ok, amountText: Fmt.money(amountNum, account.currency), message: state.result ? state.result.message : '', source: state.result ? state.result.source : 'mock' })}
      `,
      footerHtml: wizardResultFooterHtml(ok)
    });

    if (!ok) {
      document.getElementById('wizRetryBtn').addEventListener('click', () => { state.step = 2; renderStep2(); });
    } else {
      showToast('Köçürmə tamamlandı');
    }
  }

  renderStep1();
}

// ---------------------------------------------------------------
// Currency exchange — 3-step wizard (like the transfer wizard, but converting
// into another own-currency account rather than sending to a beneficiary)
// ---------------------------------------------------------------
async function openFxWizard() {
  let rates = [];
  try {
    const dash = await Api.get('/dashboard');
    rates = dash.exchangeRates || [];
  } catch (e) { /* keep going with empty rates (1:1) */ }

  const currencyOptions = ['AZN', 'USD', 'EUR'].filter(c => c !== account.currency);
  const state = { step: 1, toCurrency: currencyOptions[0] || 'USD', amount: '', result: null };

  function targetOwnAccount() {
    return allAccounts.find(a => a.currency === state.toCurrency && a.id !== accountId && sameBank(a, account));
  }

  function renderStep1() {
    const amountNum = parseFloat(state.amount) || 0;
    const rate = fxConvert(1, account.currency, state.toCurrency, rates);
    const converted = fxConvert(amountNum, account.currency, state.toCurrency, rates);

    openModal({
      title: 'Valyuta mübadiləsi',
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(1)}
        ${wizardSourceBoxHtml()}
        <div class="two-col">
          <div class="modal-field">
            <label>Alınan valyuta / hesab</label>
            <select id="fxTarget">
              ${currencyOptions.map(c => `<option value="${c}" ${c === state.toCurrency ? 'selected' : ''}>${c}</option>`).join('')}
            </select>
          </div>
          <div class="modal-field">
            <label>Məbləğ (${account.currency})</label>
            <input id="fxAmount" type="number" step="0.01" placeholder="0,00" value="${state.amount}">
          </div>
        </div>
        <div class="conv-box">
          <span>Demo məzənnə · 1 ${account.currency} = ${rate.toFixed(4)} ${state.toCurrency}</span>
          <span class="conv-result">${converted.toFixed(2)} ${state.toCurrency}</span>
        </div>
        <div id="fxMsg"></div>
      `,
      footerHtml: `<button class="btn-ghost" onclick="closeModal()">Ləğv et</button>
                   <button class="btn-primary" id="fxNextBtn">Davam et ${Icons.send}</button>`
    });

    document.getElementById('fxTarget').addEventListener('change', (e) => {
      state.toCurrency = e.target.value;
      state.amount = document.getElementById('fxAmount').value;
      renderStep1();
    });
    document.getElementById('fxAmount').addEventListener('input', (e) => {
      state.amount = e.target.value;
      renderStep1();
      const el = document.getElementById('fxAmount');
      el.focus(); el.setSelectionRange(el.value.length, el.value.length);
    });

    document.getElementById('fxNextBtn').addEventListener('click', () => {
      const amt = parseFloat(state.amount);
      if (!amt || amt <= 0) {
        document.getElementById('fxMsg').innerHTML = `<p style="color:var(--red); font-size:13px;">Məbləği düzgün daxil edin.</p>`;
        return;
      }
      if (amt > Number(account.balance)) {
        document.getElementById('fxMsg').innerHTML = `<p style="color:var(--red); font-size:13px;">Mövcud qalıqdan artıq məbləğ daxil edilib.</p>`;
        return;
      }
      state.step = 2;
      renderStep2();
    });
  }

  function renderStep2() {
    const amountNum = parseFloat(state.amount) || 0;
    const rate = fxConvert(1, account.currency, state.toCurrency, rates);
    const converted = fxConvert(amountNum, account.currency, state.toCurrency, rates);
    const target = targetOwnAccount();

    openModal({
      title: 'Valyuta mübadiləsi',
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(2)}
        <div class="review-row"><span class="k">Satılan</span><span class="v">${Fmt.money(amountNum, account.currency)}</span></div>
        <div class="review-row"><span class="k">Alınan</span><span class="v">${Fmt.money(converted, state.toCurrency)}</span></div>
        <div class="review-row"><span class="k">Məzənnə</span><span class="v">1 ${account.currency} = ${rate.toFixed(4)} ${state.toCurrency}</span></div>
        <div class="review-row"><span class="k">Kredit ediləcək hesab</span><span class="v">${target ? target.iban : state.toCurrency + ' hesabı'}</span></div>
      `,
      footerHtml: `<button class="btn-ghost" id="fx2BackBtn">Geri</button>
                   <button class="btn-primary" id="fx2ConfirmBtn">Təsdiqlə</button>`
    });

    document.getElementById('fx2BackBtn').addEventListener('click', () => { state.step = 1; renderStep1(); });

    document.getElementById('fx2ConfirmBtn').addEventListener('click', async () => {
      const btn = document.getElementById('fx2ConfirmBtn');
      btn.disabled = true; btn.textContent = 'Göndərilir...';
      try {
        const res = await Api.post('/payments', {
          fromAccountId: accountId,
          beneficiaryName: target ? target.name : `${state.toCurrency} hesabı`,
          beneficiaryIban: target ? target.iban : '',
          amount: amountNum,
          currency: account.currency,
          description: `Valyuta mübadiləsi: ${account.currency} → ${state.toCurrency}`,
          creditAmount: converted,
          creditCurrency: state.toCurrency,
        });
        state.result = { ok: res.status !== 'rejected', message: res.message || '', source: res.source };
        if (state.result.ok) await refreshAfterPayment();
      } catch (e) {
        console.error(e);
        state.result = { ok: false, message: 'Backend ilə əlaqə mümkün olmadı.', source: 'mock' };
      }
      state.step = 3;
      renderStep3();
    });
  }

  function renderStep3() {
    const amountNum = parseFloat(state.amount) || 0;
    const converted = fxConvert(amountNum, account.currency, state.toCurrency, rates);
    const ok = state.result && state.result.ok;

    openModal({
      title: 'Valyuta mübadiləsi',
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(3)}
        ${wizardResultBodyHtml({ ok, amountText: `${Fmt.money(amountNum, account.currency)} → ${Fmt.money(converted, state.toCurrency)}`, message: state.result ? state.result.message : '', source: state.result ? state.result.source : 'mock' })}
      `,
      footerHtml: wizardResultFooterHtml(ok)
    });

    if (!ok) {
      document.getElementById('wizRetryBtn').addEventListener('click', () => { state.step = 2; renderStep2(); });
    } else {
      showToast('Mübadilə tamamlandı');
    }
  }

  renderStep1();
}

// ---------------------------------------------------------------
// Bulk payments — 3 addımlı wizard: CSV faylı (Məlumatlar) → Yoxlama → Nəticə
// CSV sütunları: Alıcı, IBAN, Məbləğ, Təyinat (Təyinat boş ola bilər). Ayırıcı: vergül və ya nöqtəli vergül.
// ---------------------------------------------------------------
const BULK_MAX_ROWS = 100;
const BULK_MAX_BYTES = 1024 * 1024;   // 1 MB
const BULK_SAMPLE_CSV =
  'Alıcı,IBAN,Məbləğ,Təyinat\n' +
  'Caspian Trade MMC,AZ21NABZ00000000137010001944,150.00,Mal ödənişi\n' +
  'Araz Logistics MMC,AZ96AZEN00000000123456789012,200.50,Daşıma xidməti\n' +
  'Baku Steel MMC,AZ47PAHA00000000567890123456,75.00,\n';

function downloadBulkSample() {
  const blob = new Blob(['\uFEFF' + BULK_SAMPLE_CSV], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'kutlevi-odenisler-numune.csv';
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

// Sadə CSV parser: dırnaq içində vergül/yeni sətir, "" qaçışı, BOM, , və ; ayırıcıları.
function parseCsvText(text) {
  text = text.replace(/^\uFEFF/, '');
  const firstLine = text.split(/\r?\n/, 1)[0] || '';
  const delim = (firstLine.match(/;/g) || []).length > (firstLine.match(/,/g) || []).length ? ';' : ',';
  const rows = [];
  let row = [], cell = '', inQuotes = false;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (inQuotes) {
      if (ch === '"') { if (text[i + 1] === '"') { cell += '"'; i++; } else inQuotes = false; }
      else cell += ch;
    } else if (ch === '"') {
      inQuotes = true;
    } else if (ch === delim) {
      row.push(cell); cell = '';
    } else if (ch === '\n' || ch === '\r') {
      if (ch === '\r' && text[i + 1] === '\n') i++;
      row.push(cell); cell = ''; rows.push(row); row = [];
    } else {
      cell += ch;
    }
  }
  if (cell !== '' || row.length) { row.push(cell); rows.push(row); }
  return rows.filter(r => r.some(c => c.trim() !== ''));
}

function validateBulkRow(cells, line) {
  const name = (cells[0] || '').trim();
  const iban = (cells[1] || '').replace(/\s+/g, '').toUpperCase();
  const amountRaw = (cells[2] || '').trim().replace(/\s/g, '').replace(',', '.');
  const description = (cells[3] || '').trim();
  const errors = [];
  if (!name) errors.push('alıcının adı boşdur');
  if (!(/^[A-Z]{2}\d{2}[A-Z0-9]{11,30}$/.test(iban) && (!iban.startsWith('AZ') || iban.length === 28))) errors.push('IBAN düzgün deyil');
  const amount = Number(amountRaw);
  if (!/^\d+(\.\d{1,2})?$/.test(amountRaw) || !(amount > 0)) errors.push('məbləğ düzgün deyil');
  return { line, name, iban, amount, description, errors };
}

async function parseBulkFile(file) {
  if (file.size > BULK_MAX_BYTES) return { rows: [], fileError: 'Fayl 1 MB-dan böyükdür.' };
  let text;
  try { text = await file.text(); } catch (e) { return { rows: [], fileError: 'Faylı oxumaq mümkün olmadı.' }; }
  let cells = parseCsvText(text);
  // Birinci sətir başlıqdırsa (2-ci xana "IBAN..." ilə başlayır) keç. Başlıqsız faylda səhv sətir səssiz atılmasın,
  // ona görə yalnız bu aydın əlamətə baxırıq.
  if (cells.length && /^\s*iban/i.test(cells[0][1] || '')) cells = cells.slice(1);
  if (!cells.length) return { rows: [], fileError: 'Faylda ödəniş sətri tapılmadı.' };
  if (cells.length > BULK_MAX_ROWS) {
    return { rows: [], fileError: `Fayl ən çox ${BULK_MAX_ROWS} sətir ola bilər (${cells.length} sətir tapıldı).` };
  }
  return { rows: cells.map((c, i) => validateBulkRow(c, i + 1)), fileError: '' };
}

function openBulkModal() {
  const title = 'Kütləvi ödənişlər';
  const state = { fileName: '', rows: [], fileError: '', result: null };
  const total = () => state.rows.reduce((s, r) => s + r.amount, 0);

  function fileStatusHtml() {
    if (!state.fileName) return '';
    if (state.fileError) return `<div class="bulk-file-chip bad">${escHtml(state.fileName)} · ${escHtml(state.fileError)}</div>`;
    const bad = state.rows.filter(r => r.errors.length).length;
    return `<div class="bulk-file-chip ${bad ? 'bad' : ''}">${escHtml(state.fileName)} · ${state.rows.length} sətir${bad ? `, ${bad} səhv` : ''}</div>`;
  }

  function alertHtml() {
    const details = [];
    if (state.fileError) details.push(state.fileError);
    state.rows.filter(r => r.errors.length).slice(0, 5)
      .forEach(r => details.push(`Sətir ${r.line}: ${r.errors.join(', ')}`));
    const more = state.rows.filter(r => r.errors.length).length - 5;
    return `<div class="bulk-alert">Davam etmək üçün bütün fayl sətirləri düzgün olmalıdır.
      ${details.length ? `<ul>${details.map(d => `<li>${escHtml(d)}</li>`).join('')}${more > 0 ? `<li>… və daha ${more} səhv sətir</li>` : ''}</ul>` : ''}</div>`;
  }

  function renderStep1(alert = '') {
    openModal({
      title,
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(1)}
        ${wizardSourceBoxHtml()}
        <button class="btn-ghost btn-sample" id="bulkSampleBtn" type="button">Nümunə CSV faylı</button>
        <div class="modal-field">
          <label for="bulkFile">CSV faylını seçin (ən çox ${BULK_MAX_ROWS} sətir, 1 MB)</label>
          <input id="bulkFile" type="file" accept=".csv,text/csv">
        </div>
        <div id="bulkStatus">${fileStatusHtml()}</div>
        <div id="bulkMsg">${alert}</div>`,
      footerHtml: `<button class="btn-ghost" type="button" onclick="closeModal()">Ləğv et</button>
                   <button class="btn-primary" id="bulkNextBtn" type="button">Davam et ${Icons.send}</button>`
    });

    document.getElementById('bulkSampleBtn').addEventListener('click', downloadBulkSample);

    document.getElementById('bulkFile').addEventListener('change', async (e) => {
      const file = e.target.files && e.target.files[0];
      document.getElementById('bulkMsg').innerHTML = '';
      if (!file) { state.fileName = ''; state.rows = []; state.fileError = ''; }
      else {
        state.fileName = file.name;
        const parsed = await parseBulkFile(file);
        state.rows = parsed.rows;
        state.fileError = parsed.fileError;
      }
      document.getElementById('bulkStatus').innerHTML = fileStatusHtml();
    });

    document.getElementById('bulkNextBtn').addEventListener('click', () => {
      const invalid = !state.fileName || state.fileError || !state.rows.length || state.rows.some(r => r.errors.length);
      if (invalid) { document.getElementById('bulkMsg').innerHTML = alertHtml(); return; }
      if (total() > Number(account.balance)) {
        document.getElementById('bulkMsg').innerHTML = `<div class="bulk-alert">Ümumi məbləğ mövcud qalıqdan artıqdır (${Fmt.money(total(), account.currency)}).</div>`;
        return;
      }
      renderStep2();
    });
  }

  function renderStep2() {
    const sum = total();
    openModal({
      title,
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(2)}
        <div class="review-row"><span class="k">Fayl</span><span class="v">${escHtml(state.fileName)}</span></div>
        <div class="review-row"><span class="k">Ödəniş sayı</span><span class="v">${state.rows.length}</span></div>
        <div class="review-row"><span class="k">Ümumi məbləğ</span><span class="v">${Fmt.money(sum, account.currency)}</span></div>
        <div class="review-row"><span class="k">Əməliyyatdan sonra qalıq</span><span class="v">${Fmt.money(Number(account.balance) - sum, account.currency)}</span></div>
        <div class="bulk-table-wrap">
          <table class="bulk-table">
            <thead><tr><th>Alıcı</th><th>IBAN</th><th>Məbləğ</th></tr></thead>
            <tbody>${state.rows.map(r => `<tr><td>${escHtml(r.name)}</td><td>${escHtml(r.iban)}</td><td>${Fmt.money(r.amount, account.currency)}</td></tr>`).join('')}</tbody>
          </table>
        </div>`,
      footerHtml: `<button class="btn-ghost" id="bulkBackBtn" type="button">Geri</button>
                   <button class="btn-primary" id="bulkConfirmBtn" type="button">Təsdiqlə</button>`
    });
    document.getElementById('bulkBackBtn').addEventListener('click', () => renderStep1());

    document.getElementById('bulkConfirmBtn').addEventListener('click', async () => {
      const btn = document.getElementById('bulkConfirmBtn');
      btn.disabled = true; btn.textContent = 'Göndərilir...';
      let ok = 0, fail = 0;
      for (const r of state.rows) {
        try {
          const res = await Api.post('/payments', {
            fromAccountId: accountId, beneficiaryName: r.name, beneficiaryIban: r.iban,
            amount: r.amount, currency: account.currency, description: r.description || 'Kütləvi ödəniş'
          });
          if (res.status === 'rejected') fail++; else ok++;
        } catch (e) {
          console.error(e);
          fail++;
        }
      }
      state.result = { ok, fail };
      if (ok > 0) await refreshAfterPayment();
      renderStep3();
    });
  }

  function renderStep3() {
    const { ok, fail } = state.result;
    const success = ok > 0;
    const heading = fail === 0 ? 'Demo əməliyyat yerinə yetirildi' : (success ? 'Əməliyyat qismən yerinə yetirildi' : 'Əməliyyat uğursuz oldu');
    openModal({
      title,
      bodyHtml: `
        ${wizardSubtitleHtml()}
        ${wizardStepsHtml(3)}
        <div class="result-box">
          <div class="result-icon ${success ? 'success' : 'error'}">${success ? '✓' : '!'}</div>
          <div class="result-title">${heading}</div>
          <div class="result-amount">${ok} / ${state.rows.length} ödəniş</div>
          <div class="result-note">${ok} ödəniş göndərildi${fail ? `, ${fail} uğursuz oldu` : ''}.</div>
          ${success ? `<div class="result-disclaimer">Real pul köçürülməyib. Demo dəyişiklikləri backend işlədiyi müddətcə saxlanılır.</div>` : ''}
        </div>`,
      footerHtml: wizardResultFooterHtml(success)
    });
    if (!success) document.getElementById('wizRetryBtn').addEventListener('click', renderStep2);
    else showToast(`${ok} ödəniş simulyasiya edildi`);
  }

  renderStep1();
}

// Səhifə açılanda ?action=fx olarsa (məs. "Valyuta mübadiləsinə keç" düyməsindən), valyuta wizard-ı avtomatik açılır
load().then(() => {
  if (params.get('action') === 'fx' && account) openFxWizard();
});
