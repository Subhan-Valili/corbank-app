// Aİ Vergi Agenti (ai-vergi.html) — SSENARİLİ DEMO.
// Real Aİ modeli yoxdur: tapıntılar, etiketlər və söhbət cavabları əvvəlcədən yazılmış qaydalarla,
// şirkətin demo məlumatları (mock-api.js + tax-data.js) əsasında yaradılır.
// Vergi qaydaları: Vergi Məcəlləsi; 2026 əmək haqqı dərəcələri — taxes.gov.az/az/post/4461.

document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('muhasibatliq.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());
document.getElementById('iconBack').innerHTML = Icons.arrowLeft;
document.getElementById('iconRefresh').innerHTML = Icons.refresh;

const AI_ICON = `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="8" width="16" height="12" rx="3"/><path d="M12 8V5"/><circle cx="12" cy="4" r="1"/><circle cx="9" cy="13.5" r="1" fill="currentColor"/><circle cx="15" cy="13.5" r="1" fill="currentColor"/><path d="M9.5 17h5"/></svg>`;
const FILE_ICON = `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M12 18v-6"/><path d="M9 15l3-3 3 3"/></svg>`;
document.getElementById('aiAvatar').innerHTML = AI_ICON;
document.getElementById('iconFile').innerHTML = FILE_ICON;

const money = (v, c = 'AZN') => Fmt.money(v, c);
const r2 = n => Math.round(n * 100) / 100;
const norm = s => String(s || '').toLowerCase().replace(/̇/g, '').replace(/i̇/g, 'i');
const PENYA_RATE = 0.001;              // gündə 0,1% (VM 59)
const penyaFor = (amount, days) => r2(amount * PENYA_RATE * Math.min(Math.max(days, 0), 365));
const wait = ms => new Promise(r => setTimeout(r, ms));

// ---------------------------------------------------------------
// Data
// ---------------------------------------------------------------
let accounts = [], ops = [], rates = [];
const toAzn = (amount, cur) => {
  if (cur === 'AZN') return amount;
  const r = rates.find(x => x.currency === cur);
  return r2(amount * (r ? r.buy : 1));
};

async function loadData() {
  buildData();                                   // tax-data.js: öhdəliklər və vergi ödənişləri
  const accRes = await Api.get('/accounts');
  accounts = accRes.accounts || [];
  try { rates = (await Api.get('/dashboard')).exchangeRates || []; } catch (e) { rates = []; }
  const from = new Date(TODAY); from.setDate(from.getDate() - 90);
  const qs = new URLSearchParams({ startDate: iso(from), endDate: TODAY_ISO, sort: 'newest' });
  const res = await Promise.all(accounts.map(a => Api.get(`/accounts/${encodeURIComponent(a.id)}/operations?${qs}`)));
  ops = res.flatMap((r, i) => (r.operations || []).map(o => ({ ...o, accountId: accounts[i].id, bank: accounts[i].bank, currency: accounts[i].currency })))
    .filter(o => o.status !== 'rejected')
    .sort((a, b) => b.date.localeCompare(a.date));
  ops.forEach(o => { o.review = classify(o); });
}

// ---------------------------------------------------------------
// Əməliyyatların təsnifatı (qayda əsaslı)
// level: risk — yoxlanmalı · info — məlumat · ok — qaydasındadır · none — vergi təsiri yoxdur
// ---------------------------------------------------------------
const NONRES = {
  'Amazon Web Services':  { kind: 'service', rate: 0.10, what: 'bulud (IT) xidməti', extra: 'Əgər şirkət Azərbaycanda e-ticarət üzrə ƏDV qeydiyyatındadırsa, ƏDV-ni o özü ödəyir — hesab-fakturaya baxın.' },
  'Milano Design S.r.l.': { kind: 'service', rate: 0.10, what: 'dizayn xidməti' },
  'EuroTech GmbH':        { kind: 'royalty', rate: 0.14, what: 'proqram lisenziyası (royalti)' },
  'Maersk Line':          { kind: 'transport', rate: 0.06, what: 'beynəlxalq yükdaşıma' },
  'DHL Express':          { kind: 'transport', rate: 0.06, what: 'beynəlxalq kuryer/daşıma' },
  'Siemens AG':           { kind: 'goods', what: 'avadanlıq (mal) tədarükü' },
  'Pacific Supply Inc.':  { kind: 'goods', what: 'avadanlıq idxalı' },
};
const UTILITY = ['Azərenerji ASC', 'Azərsu ASC', 'Bakcell MMC', 'Azəriqaz İB'];
const BUDGET = ['Vergilər Nazirliyi', 'Dövlət Xəzinədarlıq Agentliyi'];

function classify(o) {
  const cp = o.counterparty || '';
  const isOut = o.direction === 'out';
  const nr = NONRES[cp];
  if (BUDGET.includes(cp)) return { level: 'none', tag: 'Büdcə ödənişi', text: 'Vergi və ya büdcə ödənişidir — əlavə vergi öhdəliyi yaratmır.' };
  if (/hesabı ·|Öz hesablar|Valyuta mübadiləsi/.test(cp + ' ' + (o.description || ''))) return { level: 'none', tag: 'Öz hesablar arası', text: 'Şirkətin öz hesabları arasında köçürmədir — vergi təsiri yoxdur.' };
  if (nr && isOut) {
    if (nr.kind === 'goods') return { level: 'info', nonres: true, tag: 'Mal idxalı', what: nr.what,
      text: `Xarici təchizatçıdan ${nr.what}. İdxal ƏDV-si (18%) və gömrük rüsumları gömrük orqanında ödənilir — ödənişi gömrük bəyannaməsi ilə uzlaşdırın.`, law: 'Vergi Məcəlləsi, 159-cu maddə' };
    const amtAzn = toAzn(o.amount, o.currency);
    const wht = r2(amtAzn * nr.rate);
    const vat = nr.kind === 'transport' ? 0 : r2(amtAzn * 0.18);
    const lines = [`Qeyri-rezidentə ${nr.what} üçün ödəniş. Daimi nümayəndəlik yoxdursa, ödəmə mənbəyində ${(nr.rate * 100).toFixed(0)}% vergi tutulmalıdır (≈ ${money(wht)}).`];
    if (nr.kind === 'transport') lines.push('Beynəlxalq daşıma xidmətlərinə ƏDV-nin 0 dərəcəsi tətbiq oluna bilər — sənədləri yoxlayın.');
    else lines.push(`Xidmət Azərbaycanda göstərilmiş sayılırsa, ƏDV (18%) alıcı — yəni şirkətiniz tərəfindən ödənilməlidir (≈ ${money(vat)}).`);
    if (nr.extra) lines.push(nr.extra);
    return { level: 'risk', nonres: true, tag: 'Qeyri-rezident · yoxlanmalı', text: lines.join(' '), wht, vat, amtAzn,
      law: nr.kind === 'transport' ? 'Vergi Məcəlləsi, 125-ci maddə' : 'Vergi Məcəlləsi, 125 və 169-cu maddələr' };
  }
  if (cp === 'Ofis icarəsi') return { level: 'risk', tag: 'İcarə · yoxlanmalı', est: r2(o.amount * 0.14),
    text: `İcarəyə verən fiziki şəxsdirsə (VÖEN-siz), icarə haqqından 14% gəlir vergisi ödəmə mənbəyində tutulmalıdır (≈ ${money(r2(o.amount * 0.14))}). Hüquqi şəxsdirsə — elektron qaimə-faktura tələb edin.`, law: 'Vergi Məcəlləsi, 124-cü maddə' };
  if (cp === 'Əməkdaşlar') return { level: 'info', tag: 'Əmək haqqı', calc: 'salary',
    text: 'Əmək haqqından gəlir vergisi, DSMF, işsizlikdən və icbari tibbi sığorta haqları hesablanmalı, növbəti ayın 15–20-dək büdcəyə köçürülməlidir.', law: 'Vergi Məcəlləsi, 150-ci maddə' };
  if (UTILITY.includes(cp) && isOut) return { level: 'ok', tag: 'ƏDV-li alış',
    text: 'Yerli təchizatçıdan alış. ƏDV-ni əvəzləşdirmək üçün elektron qaimə-faktura alınmalı və ƏDV depozit hesabı vasitəsilə ödənilməlidir.', law: 'Vergi Məcəlləsi, 175-ci maddə' };
  if (!isOut && o.currency === 'AZN') return { level: 'ok', tag: 'Satış daxilolması', vatOut: r2(o.amount * 18 / 118),
    text: `Satışdan daxilolma. Elektron qaimə-faktura təqdim edilməli, ƏDV öhdəliyi bəyannamədə əks olunmalıdır (məbləğə ƏDV daxildirsə ≈ ${money(r2(o.amount * 18 / 118))}).`, law: 'Vergi Məcəlləsi, 173-cü maddə' };
  if (!isOut) return { level: 'info', tag: 'İxrac / xarici daxilolma',
    text: 'Xaricdən daxilolma. Mal və ya xidmət ixracıdırsa, ƏDV-nin 0 dərəcəsi tətbiq olunur — ixracı təsdiqləyən sənədləri saxlayın.', law: 'Vergi Məcəlləsi, 165-ci maddə' };
  return { level: 'ok', tag: 'Adi xərc', text: 'Xüsusi vergi riski aşkarlanmadı.' };
}

const LEVEL = {
  risk: { label: 'Yoxlanmalı', cls: 'risk' },
  info: { label: 'Məlumat', cls: 'info' },
  ok:   { label: 'Qaydasındadır', cls: 'ok' },
  none: { label: 'Təsiri yoxdur', cls: 'none' },
};

// ---------------------------------------------------------------
// Tapıntılar
// ---------------------------------------------------------------
const DISMISS_KEY = 'corbank_ai_dismissed';
let dismissed = [];
try { dismissed = JSON.parse(sessionStorage.getItem(DISMISS_KEY)) || []; } catch (e) { dismissed = []; }
const saveDismissed = () => { try { sessionStorage.setItem(DISMISS_KEY, JSON.stringify(dismissed)); } catch (e) { /* */ } };

const aznAccounts = () => accounts.filter(a => a.currency === 'AZN').sort((a, b) => b.balance - a.balance);
const overdueList = () => obligations.filter(o => o.status === 'overdue');
const upcoming = days => obligations.filter(o => o.status === 'pending' && daysBetween(TODAY_ISO, o.due) <= days).sort((a, b) => a.due.localeCompare(b.due));

function buildFindings() {
  const out = [];
  // 1) Gecikmiş ödənişlər + penya
  overdueList().forEach(o => {
    const days = daysBetween(o.due, TODAY_ISO);
    const p = penyaFor(o.remaining, days);
    out.push({ id: `od-${o.taxId}-${o.key}`, level: 'critical', icon: '!', title: `${TAX[o.taxId].short} ${days} gündür ödənilməyib`,
      text: `${o.label} üzrə ${money(o.remaining)} son tarix ${Fmt.dateAz(o.due)} idi. Bu günədək hesablanmış faiz (penya) təxminən <b>${money(p)}</b>-dir və hər gün ${money(r2(o.remaining * PENYA_RATE))} artır.`,
      law: 'Vergi Məcəlləsi, 59-cu maddə',
      actions: [{ label: 'Ödənişi hazırla', href: `vergi.html?pay=${o.taxId}|${o.key}#odenis`, primary: true },
                { label: 'Penyanı hesabla', calc: { amount: o.remaining, due: o.due } }] });
  });
  // 2) Yaxın 14 gün + likvidlik
  const soon = upcoming(14);
  if (soon.length) {
    const total = r2(soon.reduce((s, o) => s + o.remaining, 0));
    const azn = aznAccounts();
    const enough = azn.find(a => a.balance >= total);
    const weak = azn.filter(a => a.balance < total);
    const byDate = {};
    soon.forEach(o => { (byDate[o.due] = byDate[o.due] || []).push(o); });
    out.push({ id: `soon-${soon.map(o => o.taxId + o.key).join('')}`, level: 'warning', icon: '⏱', title: `14 gün ərzində ${soon.length} vergi ödənişi — cəmi ${money(total)}`,
      text: Object.entries(byDate).map(([d, list]) => `<b>${Fmt.dateAz(d)}</b>: ${list.map(o => `${esc(TAX[o.taxId].short)} ${money(o.remaining)}`).join(', ')}`).join('<br>') +
        (enough ? `<div class="ai-reco">💡 Tövsiyə: <b>${esc(accountLabel(enough))}</b> hesabından ödəyin — qalıq ${money(enough.balance)}, ödənişlərdən sonra ${money(r2(enough.balance - total))} qalacaq.${weak.length ? ` ${weak.map(a => esc(bankTitle(a.bank))).join(', ')} hesabındakı vəsait (${weak.map(a => money(a.balance)).join(', ')}) bütün ödənişlər üçün kifayət etmir.` : ''}</div>`
          : `<div class="ai-reco warn">⚠ Heç bir AZN hesabında ${money(total)} yoxdur — vəsaiti əvvəlcədən bir hesabda toplayın və ya valyuta mübadiləsi edin.</div>`),
      actions: [{ label: 'Vergi təqviminə bax', href: 'vergi.html#icmal', primary: true }] });
  }
  // 3) ƏDV dinamikası
  const edv = obligations.filter(o => o.taxId === 'edv').sort((a, b) => b.due.localeCompare(a.due));
  const cur = edv.slice().reverse().find(o => o.status !== 'paid' && o.due >= TODAY_ISO) || edv.find(o => o.due <= TODAY_ISO);
  const prev = cur && edv.find(o => o.due < cur.due);
  if (cur && prev) {
    const ch = (cur.amount - prev.amount) / prev.amount * 100;
    const [py, pm] = cur.key.split('-').map(Number);
    const sales = ops.filter(o => o.direction === 'in' && o.currency === 'AZN' && o.date.startsWith(`${py}-${String(pm).padStart(2, '0')}`))
      .sort((a, b) => b.amount - a.amount);
    out.push({ id: `edv-${cur.key}`, level: Math.abs(ch) >= 8 ? 'warning' : 'info', icon: '%', title: `${cur.label} üzrə ƏDV ${Math.abs(ch).toFixed(1).replace('.', ',')}% ${ch >= 0 ? 'artıb' : 'azalıb'}`,
      text: `Hesablanmış ƏDV: <b>${money(cur.amount)}</b> (əvvəlki ay: ${money(prev.amount)}).` +
        (sales.length ? ` Bu ay AZN hesablarına satış daxilolmaları: ${sales.length} əməliyyat, ən böyüyü — ${esc(sales[0].counterparty)} ${money(sales[0].amount)} (${Fmt.dateAz(sales[0].date)}).` : '') +
        ' Bəyannaməni təqdim etməzdən əvvəl bütün e-qaimə-fakturaların uçota alındığını yoxlayın.',
      law: 'Vergi Məcəlləsi, 173–175-ci maddələr',
      actions: [{ label: 'ƏDV-ni hesabla', calcVat: true }] });
  }
  // 4) Qeyri-rezident ödənişləri
  const nr = ops.filter(o => o.review.nonres && o.review.level === 'risk');
  if (nr.length) {
    const wht = r2(nr.reduce((s, o) => s + o.review.wht, 0));
    const vat = r2(nr.reduce((s, o) => s + o.review.vat, 0));
    out.push({ id: `nr-${nr.length}`, level: 'warning', icon: '🌐', title: `${nr.length} qeyri-rezident ödənişində vergi öhdəliyi yarana bilər`,
      text: `Son 90 gündə xarici şirkətlərə xidmət ödənişləri: ${[...new Set(nr.map(o => o.counterparty))].map(esc).join(', ')}. Ehtimal olunan ödəmə mənbəyində vergi ≈ <b>${money(wht)}</b>, alıcı tərəfindən ödənilən ƏDV ≈ <b>${money(vat)}</b>. Bu vergilər ödənilməyibsə, maliyyə sanksiyası və faiz riski var.`,
      law: 'Vergi Məcəlləsi, 125 və 169-cu maddələr',
      actions: [{ label: 'Əməliyyatlara bax', tab: 'ops', filter: 'nonres', primary: true }, { label: 'Agentdən soruş', ask: 'Qeyri-rezidentə ödənişdə hansı vergilər tutulur?' }] });
  }
  // 5) İcarə
  const rent = ops.filter(o => o.counterparty === 'Ofis icarəsi');
  if (rent.length) {
    out.push({ id: `rent-${rent.length}`, level: 'info', icon: '🏢', title: 'İcarə ödənişlərində ödəmə mənbəyində vergini yoxlayın',
      text: `Son 90 gündə ${rent.length} icarə ödənişi (${money(r2(rent.reduce((s, o) => s + o.amount, 0)))}). İcarəyə verən fiziki şəxsdirsə, 14% gəlir vergisi şirkət tərəfindən tutulmalıdır.`,
      law: 'Vergi Məcəlləsi, 124-cü maddə',
      actions: [{ label: 'Agentdən soruş', ask: 'Ofis icarəsinə görə vergi tutmalıyam?', primary: true }] });
  }
  // 6) Qanun yeniliyi
  out.push({ id: 'law-2026-gv', level: 'info', icon: '§', title: '2026: özəl sektorda gəlir vergisinin yeni mexanizmi',
    text: 'Aylıq 2 500 AZN-dək gəlirə 3% (ilk 200 AZN azad), 2 500–8 000 AZN — 75 AZN + 10%, 8 000 AZN-dən çox — 625 AZN + 14%. 2027-də 5%, 2028-də 7%-ə qalxır.',
    actions: [{ label: 'Şirkətə təsirinə bax', tab: 'docs', primary: true }, { label: 'Maaşı hesabla', tab: 'calc' }] });
  // 7) Müsbət
  const paidEarly = obligations.filter(o => o.status === 'paid' && o.due >= TODAY_ISO);
  if (paidEarly.length) {
    out.push({ id: `ok-${paidEarly.map(o => o.taxId + o.key).join('')}`, level: 'success', icon: '✓', title: 'Vaxtından əvvəl ödənilib',
      text: paidEarly.map(o => `${esc(TAX[o.taxId].name)} — ${esc(o.label)} (${money(o.amount)})`).join('<br>') });
  }
  return out;
}

const SEV = { critical: 'Kritik', warning: 'Diqqət', info: 'Məlumat', success: 'Qaydasındadır' };
let findings = [];

function renderFindings() {
  findings = buildFindings();
  const visible = findings.filter(f => !dismissed.includes(f.id));
  const crit = visible.filter(f => f.level === 'critical').length, warn = visible.filter(f => f.level === 'warning').length;
  document.getElementById('cntFindings').textContent = crit + warn || '';
  const name = (Prefs.get().name || '').split(' ')[0];
  document.getElementById('heroSummary').innerHTML =
    `${name ? `Salam, ${esc(name)}! ` : ''}${accounts.length} hesab və ${ops.length} əməliyyat analiz olundu. ` +
    (crit + warn ? `<b>${crit + warn} məsələ</b> diqqətinizi tələb edir${crit ? ` (${crit} kritik)` : ''}.` : 'Diqqət tələb edən məsələ yoxdur.');
  document.getElementById('findingsList').innerHTML = visible.map(f => `
    <article class="ai-f ${f.level}" data-id="${esc(f.id)}">
      <div class="ai-f-icon">${f.icon}</div>
      <div class="ai-f-body">
        <div class="ai-f-top"><span class="ai-sev ${f.level}">${SEV[f.level]}</span>${f.law ? `<span class="ai-law">${esc(f.law)}</span>` : ''}</div>
        <h3>${f.title}</h3>
        <div class="ai-f-text">${f.text}</div>
        <div class="ai-f-actions">
          ${(f.actions || []).map((a, i) => `<button type="button" class="${a.primary ? 'btn-primary' : 'vg-btn-outline'}" data-act="${i}">${a.label}</button>`).join('')}
          <button type="button" class="ai-dismiss" data-dismiss="1">Bildim, gizlət</button>
        </div>
      </div>
    </article>`).join('') +
    (dismissed.length ? `<button type="button" class="ai-restore" id="restoreFindings">Gizlədilən ${dismissed.length} tapıntını göstər</button>` : '');
}

document.getElementById('findingsList').addEventListener('click', e => {
  if (e.target.id === 'restoreFindings') { dismissed = []; saveDismissed(); renderFindings(); return; }
  const card = e.target.closest('.ai-f'); if (!card) return;
  const f = findings.find(x => x.id === card.dataset.id); if (!f) return;
  if (e.target.closest('[data-dismiss]')) {
    card.classList.add('ai-f-out');
    setTimeout(() => { dismissed.push(f.id); saveDismissed(); renderFindings(); }, 220);
    return;
  }
  const btn = e.target.closest('[data-act]'); if (!btn) return;
  runAction(f.actions[Number(btn.dataset.act)]);
});

function runAction(a) {
  if (!a) return;
  if (a.href) { location.href = a.href; return; }
  if (a.calc) { setPenya(a.calc.amount, a.calc.due); switchTab('calc'); flash('calcPenya'); return; }
  if (a.calcVat) { switchTab('calc'); flash('calcVat'); return; }
  if (a.ask) { switchTab('chat'); ask(a.ask); return; }
  if (a.tab) { if (a.filter) setOpFilter(a.filter); switchTab(a.tab); }
}
function flash(id) {
  const el = document.getElementById(id);
  el.scrollIntoView({ behavior: 'smooth', block: 'center' });
  el.classList.remove('ai-flash'); void el.offsetWidth; el.classList.add('ai-flash');
}

// Skan animasiyası
async function scan(animated) {
  const box = document.getElementById('scanBox');
  const list = document.getElementById('findingsList');
  const steps = ['Hesablar və qalıqlar oxunur', 'Son 90 günün əməliyyatları təsnif edilir', 'Vergi öhdəlikləri və son tarixlər yoxlanır', 'Qeyri-rezident ödənişləri və risklər qiymətləndirilir'];
  if (!animated) { box.style.display = 'none'; return; }
  box.style.display = ''; list.style.opacity = '.25';
  const st = document.getElementById('scanSteps');
  for (let i = 0; i < steps.length; i++) {
    st.innerHTML = steps.map((s, j) => `<div class="${j < i ? 'done' : j === i ? 'now' : ''}">${j < i ? '✓' : j === i ? '…' : '•'} ${s}</div>`).join('');
    box.style.setProperty('--p', `${(i + 1) / steps.length * 100}%`);
    await wait(420);
  }
  box.style.display = 'none'; list.style.opacity = '';
}
document.getElementById('rescanBtn').addEventListener('click', async () => {
  switchTab('findings'); await scan(true); renderFindings(); showToast('Analiz yeniləndi');
});

// ---------------------------------------------------------------
// Əməliyyat yoxlanışı
// ---------------------------------------------------------------
const OP_PAGE = 10;
let opFilter = 'all', opPage = 1;
const reviewed = new Set();
function setOpFilter(f) {
  opFilter = f; opPage = 1;
  document.querySelectorAll('#opFilter button').forEach(b => b.classList.toggle('active', b.dataset.f === f));
  renderOps();
}
function filteredOps() {
  const bank = document.getElementById('opBank').value;
  return ops.filter(o => (bank === 'all' || o.bank === bank) && (
    opFilter === 'all' || (opFilter === 'risk' && o.review.level === 'risk') || (opFilter === 'nonres' && o.review.nonres) ||
    (opFilter === 'ok' && (o.review.level === 'ok' || o.review.level === 'none'))));
}
function renderOps() {
  const cnt = l => ops.filter(o => o.review.level === l).length;
  document.getElementById('cntOps').textContent = cnt('risk') || '';
  document.getElementById('opStats').innerHTML = `
    <div class="ai-stat"><b>${ops.length}</b><span>əməliyyat yoxlanıldı</span></div>
    <div class="ai-stat risk"><b>${cnt('risk')}</b><span>yoxlanmalı</span></div>
    <div class="ai-stat info"><b>${cnt('info')}</b><span>məlumat üçün</span></div>
    <div class="ai-stat ok"><b>${cnt('ok') + cnt('none')}</b><span>qaydasındadır</span></div>`;
  const list = filteredOps();
  const pages = Math.max(1, Math.ceil(list.length / OP_PAGE));
  opPage = Math.min(Math.max(1, opPage), pages);
  document.getElementById('opBody').innerHTML = list.slice((opPage - 1) * OP_PAGE, opPage * OP_PAGE).map(o => {
    const lv = LEVEL[o.review.level], isIn = o.direction === 'in';
    return `<tr class="ai-op-row" data-id="${esc(o.id)}" tabindex="0">
      <td class="vg-date">${Fmt.dateAz(o.date)}</td>
      <td><div class="vg-h-name">${esc(o.counterparty)}</div><div class="vg-muted vg-h-sub">${esc(o.description)}</div></td>
      <td><span class="op-bank">${bankLogoImg(bankTitle(o.bank), 22)}<span>${esc(bankTitle(o.bank))}</span></span></td>
      <td class="ai-amt ${isIn ? 'in' : 'out'}">${isIn ? '+' : '−'}${money(o.amount, o.currency)}</td>
      <td><span class="ai-tag ${lv.cls}">${esc(o.review.tag)}</span>${reviewed.has(o.id) ? '<span class="ai-checked">✓ yoxlanıldı</span>' : ''}</td>
      <td class="ai-more">›</td>
    </tr>`;
  }).join('');
  document.getElementById('opEmpty').style.display = list.length ? 'none' : 'block';
  document.querySelector('#panel-ops .vg-table-wrap').style.display = list.length ? '' : 'none';
  document.getElementById('opCount').textContent = `${list.length} əməliyyat`;
  document.getElementById('opPage').textContent = `${opPage} / ${pages}`;
  document.getElementById('opPrev').disabled = opPage <= 1;
  document.getElementById('opNext').disabled = opPage >= pages;
}
document.getElementById('opFilter').addEventListener('click', e => { const b = e.target.closest('button[data-f]'); if (b) setOpFilter(b.dataset.f); });
document.getElementById('opBank').addEventListener('change', () => { opPage = 1; renderOps(); });
document.getElementById('opPrev').addEventListener('click', () => { opPage--; renderOps(); });
document.getElementById('opNext').addEventListener('click', () => { opPage++; renderOps(); });
function openOpFromRow(e) {
  const row = e.target.closest('.ai-op-row'); if (!row) return;
  const o = ops.find(x => x.id === row.dataset.id); if (o) openOpReview(o);
}
document.getElementById('opBody').addEventListener('click', openOpFromRow);
document.getElementById('opBody').addEventListener('keydown', e => { if (e.key === 'Enter') openOpFromRow(e); });

function openOpReview(o) {
  const r = o.review, lv = LEVEL[r.level], isIn = o.direction === 'in';
  const est = [];
  if (r.wht) est.push(['Ödəmə mənbəyində vergi (təxmini)', money(r.wht)]);
  if (r.vat) est.push(['ƏDV — alıcı tərəfindən (təxmini)', money(r.vat)]);
  if (r.est) est.push(['14% gəlir vergisi (fiziki şəxsdirsə)', money(r.est)]);
  if (r.vatOut) est.push(['Hesablanmış ƏDV (ƏDV daxildirsə)', money(r.vatOut)]);
  if (o.currency !== 'AZN') est.unshift(['Məbləğ AZN ekvivalentində', money(toAzn(o.amount, o.currency))]);
  openModal({
    title: 'Agentin rəyi',
    subtitle: `${esc(o.counterparty)} · ${Fmt.dateAz(o.date)} · ${esc(bankTitle(o.bank))}`,
    bodyHtml: `
      <div class="op-detail-head ai-op-head">
        <div><div class="op-detail-name">${esc(o.counterparty)}</div><div class="op-detail-desc">${esc(o.description)}</div></div>
        <div class="ai-amt ${isIn ? 'in' : 'out'} ai-op-big">${isIn ? '+' : '−'}${money(o.amount, o.currency)}</div>
      </div>
      <div class="ai-review"><span class="ai-tag ${lv.cls}">${esc(r.tag)}</span>
        <div class="ai-review-text"><span class="ai-avatar-sm">${AI_ICON}</span><p>${r.text}</p></div>
        ${r.law ? `<div class="ai-law">Əsas: ${esc(r.law)}</div>` : ''}
      </div>
      ${est.length ? est.map(([k, v]) => `<div class="review-row"><span class="k">${k}</span><span class="v">${v}</span></div>`).join('') : ''}
      <div class="info-note">Ssenarili demo rəyidir — qərar verməzdən əvvəl müqaviləni və sənədləri mühasiblə yoxlayın.</div>`,
    footerHtml: `
      ${r.calc ? '<button class="vg-btn-outline" type="button" id="rvCalc">Maaş vergilərini hesabla</button>' : ''}
      ${r.level === 'risk' ? '<button class="vg-btn-outline" type="button" id="rvAsk">Agentdən soruş</button>' : ''}
      <button class="btn-primary" type="button" id="rvDone">${reviewed.has(o.id) ? 'Bağla' : 'Yoxlanıldı kimi qeyd et'}</button>`,
  });
  document.getElementById('rvDone').addEventListener('click', () => { reviewed.add(o.id); closeModal(); renderOps(); });
  const c = document.getElementById('rvCalc'); if (c) c.addEventListener('click', () => { closeModal(); switchTab('calc'); flash('calcSalary'); });
  const a = document.getElementById('rvAsk'); if (a) a.addEventListener('click', () => {
    closeModal(); switchTab('chat');
    ask(r.nonres ? `${o.counterparty} ödənişi üçün nə etməliyəm?` : 'Ofis icarəsinə görə vergi tutmalıyam?');
  });
}

// ---------------------------------------------------------------
// Söhbət (ssenarili)
// ---------------------------------------------------------------
const CHIPS = [
  'Bu ay nə qədər vergi ödəməliyəm?', 'Gecikmiş ödənişim var?', 'DSMF ödənilib?', 'Vergiləri hansı hesabdan ödəyim?',
  'Ofis icarəsinə görə vergi tutmalıyam?', 'Amazon Web Services ödənişi üçün nə etməliyəm?', '1500 AZN maaşdan əlinə nə çatır?', 'ƏDV nə vaxt ödənilir?',
];
const chatLog = document.getElementById('chatLog');
let busy = false;

function bubble(role, html, actions) {
  const div = document.createElement('div');
  div.className = `ai-msg ${role}`;
  div.innerHTML = role === 'bot'
    ? `<span class="ai-avatar-sm">${AI_ICON}</span><div class="ai-bubble">${html}${actions && actions.length ? `<div class="ai-msg-actions">${actions.map((a, i) => `<button type="button" class="vg-btn-outline vg-btn-sm" data-i="${i}">${a.label}</button>`).join('')}</div>` : ''}</div>`
    : `<div class="ai-bubble">${esc(html)}</div>`;
  if (actions) div.querySelectorAll('[data-i]').forEach(b => b.addEventListener('click', () => runAction(actions[Number(b.dataset.i)])));
  chatLog.appendChild(div);
  chatLog.scrollTop = chatLog.scrollHeight;
  return div;
}

async function ask(q) {
  if (busy || !q.trim()) return;
  busy = true;
  bubble('user', q.trim());
  const typing = bubble('bot', '<span class="ai-typing"><i></i><i></i><i></i></span>');
  await wait(650 + Math.random() * 500);
  typing.remove();
  const a = answer(q);
  bubble('bot', a.html + (a.law ? `<div class="ai-src">Mənbə: ${esc(a.law)}</div>` : ''), a.actions);
  busy = false;
}

const list = items => `<ul>${items.map(i => `<li>${i}</li>`).join('')}</ul>`;

function answer(raw) {
  const q = norm(raw);
  const has = (...w) => w.some(x => q.includes(norm(x)));
  const num = (q.match(/(\d[\d\s]*[.,]?\d*)/) || [])[1];
  const amount = num ? parseFloat(num.replace(/\s/g, '').replace(',', '.')) : NaN;

  if (has('maaş', 'maas', 'əmək haqq', 'emek haqq', 'əlinə', 'elinə', 'net', 'gross')) {
    const g = amount > 0 ? amount : 1500;
    const s = salaryCalc(g);
    return { html: `<b>${money(g)}</b> hesablanmış əmək haqqı üçün (2026, özəl qeyri-neft sektoru):` + list([
      `Gəlir vergisi: ${money(s.it)}`, `DSMF (işçi): ${money(s.dsmfE)}`, `İşsizlikdən sığorta (işçi): ${money(s.unE)}`, `İcbari tibbi sığorta (işçi): ${money(s.medE)}`,
      `<b>Əlinə çatan: ${money(s.net)}</b>`, `İşəgötürənin əlavə xərci: ${money(s.employer)} → ümumi xərc ${money(s.cost)}`]),
      law: 'Vergi Məcəlləsi, 101-ci maddə; taxes.gov.az — 2026 gəlir vergisi mexanizmi',
      actions: [{ label: 'Kalkulyatorda aç', tab: 'calc' }] };
  }
  if (has('gecik', 'penya', 'faiz', 'borc')) {
    const od = overdueList();
    if (!od.length) return { html: 'Gecikmiş vergi ödənişiniz yoxdur 👍 Hamısı vaxtında ödənilib və ya son tarixi hələ gəlməyib.' };
    return { html: `Bəli, <b>${od.length} gecikmiş ödəniş</b> var:` + list(od.map(o => {
      const d = daysBetween(o.due, TODAY_ISO);
      return `${esc(TAX[o.taxId].name)} — ${esc(o.label)}: <b>${money(o.remaining)}</b>, ${d} gün gecikib, penya ≈ <b>${money(penyaFor(o.remaining, d))}</b>`;
    })) + 'Faiz hər gün 0,1% artır — mümkün qədər tez ödəməyi tövsiyə edirəm.',
      law: 'Vergi Məcəlləsi, 59-cu maddə',
      actions: [{ label: 'İndi ödə', href: `vergi.html?pay=${od[0].taxId}|${od[0].key}#odenis` }, { label: 'Penyanı hesabla', calc: { amount: od[0].remaining, due: od[0].due } }] };
  }
  if (has('dsmf', 'sosial sığorta', 'sosial sigorta')) {
    const d = obligations.filter(o => o.taxId === 'dsmf' && o.due <= iso(new Date(TODAY.getFullYear(), TODAY.getMonth() + 1, 28))).sort((a, b) => b.due.localeCompare(a.due))
      .find(o => o.due.slice(0, 7) === TODAY_ISO.slice(0, 7)) || obligations.filter(o => o.taxId === 'dsmf' && o.due <= TODAY_ISO).sort((a, b) => b.due.localeCompare(a.due))[0];
    if (!d) return { html: 'DSMF üzrə məlumat tapmadım.' };
    const p = allPayments().find(x => x.taxId === 'dsmf' && x.periodKey === d.key && x.kind === 'esas');
    return { html: d.status === 'paid'
      ? `Bəli ✓ <b>${esc(d.label)}</b> üzrə sosial sığorta haqları ödənilib: ${money(d.amount)}${p ? `, ${Fmt.dateAz(p.date)} tarixində ${esc(bankTitle(p.bank))} hesabından` : ''}. Son tarix ${Fmt.dateAz(d.due)} idi.`
      : `Xeyr, <b>${esc(d.label)}</b> üzrə DSMF (${money(d.remaining)}) hələ ödənilməyib. Son tarix: ${Fmt.dateAz(d.due)}.`,
      law: 'Vergi təqvimi: növbəti ayın 15-dək',
      actions: d.status === 'paid' ? [] : [{ label: 'Ödənişi hazırla', href: `vergi.html?pay=dsmf|${d.key}#odenis` }] };
  }
  if (has('hansı hesab', 'hansi hesab', 'hesabdan', 'likvid', 'vəsait çatır', 'kifayət')) {
    const soon = upcoming(14); const total = r2(soon.reduce((s, o) => s + o.remaining, 0));
    const azn = aznAccounts();
    return { html: `Növbəti 14 gündə ${soon.length} vergi ödənişi üçün <b>${money(total)}</b> lazımdır. AZN hesablarınız:` +
      list(azn.map(a => `${esc(accountLabel(a))}: ${money(a.balance)} ${a.balance >= total ? '✓ kifayət edir' : '✗ kifayət etmir'}`)) +
      (azn[0] && azn[0].balance >= total ? `Tövsiyə: <b>${esc(accountLabel(azn[0]))}</b> — ödənişlərdən sonra ${money(r2(azn[0].balance - total))} qalacaq. Vergiləri yalnız AZN hesabından ödəmək olar.` : 'Vəsaiti əvvəlcədən bir AZN hesabında toplamağı tövsiyə edirəm.'),
      actions: [{ label: 'Ödəniş et', href: 'vergi.html#odenis' }] };
  }
  if (has('icarə', 'icare', 'kirayə', 'kiraye')) {
    const rent = ops.filter(o => o.counterparty === 'Ofis icarəsi');
    return { html: 'Bu, icarəyə verənin statusundan asılıdır:' + list([
      '<b>Fiziki şəxsdir (VÖEN-siz)</b> — icarə haqqından <b>14% gəlir vergisi</b> ödəmə mənbəyində şirkət tərəfindən tutulub büdcəyə köçürülməlidir.',
      '<b>Hüquqi şəxs və ya VÖEN-li sahibkardır</b> — ödəmə mənbəyində vergi tutulmur; ƏDV ödəyicisidirsə, elektron qaimə-faktura tələb edin (ƏDV əvəzləşdirilməsi üçün).']) +
      (rent.length ? `Son 90 gündə ${rent.length} icarə ödənişiniz var (${money(r2(rent.reduce((s, o) => s + o.amount, 0)))}). Fiziki şəxsə ödənilibsə, ≈ ${money(r2(rent.reduce((s, o) => s + o.amount, 0) * 0.14))} vergi tutulmalı idi.` : ''),
      law: 'Vergi Məcəlləsi, 124-cü maddə', actions: [{ label: 'İcarə əməliyyatlarına bax', tab: 'ops', filter: 'risk' }] };
  }
  const nrName = Object.keys(NONRES).find(n => q.includes(norm(n.split(' ')[0])));
  if (nrName || has('qeyri-rezident', 'qeyri rezident', 'xarici şirkət', 'xarici sirket', 'xaricə')) {
    if (nrName && NONRES[nrName].kind === 'goods') return { html: `${esc(nrName)} — ${NONRES[nrName].what}. Mal idxalında ödəmə mənbəyində vergi tutulmur; idxal ƏDV-si (18%) və gömrük rüsumları gömrük bəyannaməsi ilə ödənilir. Bank ödənişini gömrük sənədləri ilə uzlaşdırın.`, law: 'Vergi Məcəlləsi, 159-cu maddə' };
    const nr = nrName ? NONRES[nrName] : null;
    const pays = ops.filter(o => nrName ? o.counterparty === nrName : (o.review.nonres && o.review.level === 'risk'));
    return { html: (nr ? `<b>${esc(nrName)}</b> — ${nr.what}. ` : 'Qeyri-rezidentə (Azərbaycanda daimi nümayəndəliyi olmayan xarici şirkətə) xidmət haqqı ödəyəndə adətən iki öhdəlik yaranır: ') + list([
      `<b>Ödəmə mənbəyində vergi</b> — ${nr ? `${(nr.rate * 100).toFixed(0)}%` : 'xidmət növündən asılı olaraq 6%, 10% və ya 14% (royalti)'}; şirkətiniz tutub büdcəyə köçürür.`,
      nr && nr.kind === 'transport' ? '<b>ƏDV</b> — beynəlxalq daşımaya 0 dərəcə tətbiq oluna bilər.' : '<b>ƏDV (18%)</b> — xidmət Azərbaycanda göstərilmiş sayılırsa, alıcı (siz) tərəfindən ödənilir və sonra əvəzləşdirilə bilər.',
      ...(nr && nr.extra ? [nr.extra] : []), 'İkiqat vergitutmanın aradan qaldırılması sazişi varsa, dərəcə azala bilər — rezidentlik sertifikatı tələb edin.']) +
      (pays.length ? `Son 90 gündə ${pays.length} belə ödəniş: ≈ ${money(r2(pays.reduce((s, o) => s + (o.review.wht || 0), 0)))} ödəmə mənbəyində vergi${pays.some(o => o.review.vat) ? ` və ≈ ${money(r2(pays.reduce((s, o) => s + (o.review.vat || 0), 0)))} ƏDV` : ''}.` : ''),
      law: 'Vergi Məcəlləsi, 125 və 169-cu maddələr', actions: [{ label: 'Bu əməliyyatlara bax', tab: 'ops', filter: 'nonres' }] };
  }
  if (has('ədv', 'edv', 'vat')) {
    const e = obligations.filter(o => o.taxId === 'edv' && o.status !== 'paid').sort((a, b) => a.due.localeCompare(b.due))[0];
    return { html: 'ƏDV dərəcəsi <b>18%</b>-dir. Bəyannamə və ödəniş hesabat ayından sonrakı ayın <b>20-dək</b> edilir (bazar/şənbə düşərsə — növbəti iş günü).' +
      (e ? ` Növbəti ödənişiniz: <b>${esc(e.label)}</b> — ${money(e.remaining)}, son tarix ${Fmt.dateAz(e.due)} (${daysBetween(TODAY_ISO, e.due)} gün qalıb).` : '') +
      ' Əvəzləşdirmə üçün təchizatçılardan elektron qaimə-faktura alınmalı və ƏDV depozit hesabı vasitəsilə ödənilməlidir.',
      law: 'Vergi Məcəlləsi, 173 və 175-ci maddələr', actions: e ? [{ label: 'ƏDV-ni ödə', href: `vergi.html?pay=edv|${e.key}#odenis` }, { label: 'ƏDV kalkulyatoru', calcVat: true }] : [] };
  }
  if (has('mənfəət', 'menfeet')) {
    const m = obligations.filter(o => o.taxId === 'mv' && o.status !== 'paid').sort((a, b) => a.due.localeCompare(b.due))[0];
    return { html: 'Mənfəət vergisinin dərəcəsi <b>20%</b>-dir. Cari ödənişlər hər rüb bitdikdən sonra 15 gün ərzində, illik bəyannamə və yekun ödəniş 31 marta qədər edilir.' +
      (m ? ` Növbəti cari ödəniş: <b>${esc(m.label)}</b> — ${money(m.remaining)}, son tarix ${Fmt.dateAz(m.due)}.` : ''),
      law: 'Vergi Məcəlləsi, 105-ci maddə; vergi təqvimi', actions: m ? [{ label: 'Ödənişi hazırla', href: `vergi.html?pay=mv|${m.key}#odenis` }] : [] };
  }
  if (has('bu ay', 'nə qədər', 'ne qeder', 'ödəməliyəm', 'odemeliyem', 'son tarix', 'nə vaxt', 'ne vaxt', 'vergi')) {
    const ym = TODAY_ISO.slice(0, 7);
    const month = obligations.filter(o => o.due.slice(0, 7) === ym).sort((a, b) => a.due.localeCompare(b.due));
    const unpaid = month.filter(o => o.status !== 'paid'); const od = overdueList();
    const total = r2(unpaid.reduce((s, o) => s + o.remaining, 0) + od.reduce((s, o) => s + o.remaining, 0));
    return { html: `${MONTHS_CAP[TODAY.getMonth()]} ayında cəmi <b>${money(total)}</b> ödəməlisiniz:` + list([
      ...od.map(o => `⚠ ${esc(TAX[o.taxId].short)} (${esc(o.label)}) — ${money(o.remaining)}, <b>gecikib</b>`),
      ...month.map(o => `${Fmt.dateAz(o.due)} — ${esc(TAX[o.taxId].short)}: ${money(o.status === 'paid' ? o.amount : o.remaining)} ${o.status === 'paid' ? '✓ ödənilib' : `(${daysBetween(TODAY_ISO, o.due)} gün qalıb)`}`)]),
      law: 'Vergi təqvimi — taxes.gov.az', actions: [{ label: 'Vergi təqvimi', href: 'vergi.html#icmal' }] };
  }
  if (has('salam', 'hello', 'necəsən', 'sağ ol', 'sag ol', 'təşəkkür', 'tesekkur')) {
    return { html: has('sağ ol', 'sag ol', 'təşəkkür', 'tesekkur') ? 'Dəyməz! Başqa sualınız olsa, buradayam.' : 'Salam! Mən şirkətinizin vergi köməkçisiyəm. Öhdəliklər, son tarixlər, əmək haqqı vergiləri, ƏDV və xarici ödənişlər haqqında soruşa bilərsiniz.' };
  }
  return { html: 'Bu sualı demo rejimində hələ tanımıram 🙂 Bunları soruşa bilərsiniz:' + list(CHIPS.slice(0, 5).map(esc)) };
}

document.getElementById('chatChips').innerHTML = CHIPS.map(c => `<button type="button" class="ai-chip">${esc(c)}</button>`).join('');
document.getElementById('chatChips').addEventListener('click', e => { const b = e.target.closest('.ai-chip'); if (b) ask(b.textContent); });
document.getElementById('chatForm').addEventListener('submit', e => {
  e.preventDefault();
  const inp = document.getElementById('chatInput');
  const v = inp.value; inp.value = ''; ask(v);
});

// ---------------------------------------------------------------
// Kalkulyatorlar
// ---------------------------------------------------------------
// 2026 · qeyri-neft-qaz özəl sektoru · əsas iş yeri (taxes.gov.az/az/post/4461)
function salaryCalc(g) {
  const it = g <= 2500 ? Math.max(0, g - 200) * 0.03 : g <= 8000 ? 75 + (g - 2500) * 0.10 : 625 + (g - 8000) * 0.14;
  const dsmfE = g <= 200 ? g * 0.03 : g <= 8000 ? 6 + (g - 200) * 0.10 : 786 + (g - 8000) * 0.10;
  const dsmfR = g <= 200 ? g * 0.22 : g <= 8000 ? 44 + (g - 200) * 0.15 : 1214 + (g - 8000) * 0.11;
  const un = g * 0.005;
  const med = g <= 2500 ? g * 0.02 : 50 + (g - 2500) * 0.005;
  const net = g - it - dsmfE - un - med;
  const employer = dsmfR + un + med;
  return { it: r2(it), dsmfE: r2(dsmfE), dsmfR: r2(dsmfR), unE: r2(un), unR: r2(un), medE: r2(med), medR: r2(med), net: r2(net), employer: r2(employer), cost: r2(g + employer) };
}

function setPenya(amount, due) {
  document.getElementById('pAmount').value = amount.toFixed(2);
  document.getElementById('pDue').value = due;
  document.getElementById('pPay').value = TODAY_ISO;
  renderPenya();
}
function renderPenya() {
  const a = parseFloat(document.getElementById('pAmount').value) || 0;
  const due = document.getElementById('pDue').value, pay = document.getElementById('pPay').value;
  if (!due || !pay) { document.getElementById('pResult').innerHTML = ''; return; }
  const days = Math.max(0, daysBetween(due, pay));
  const p = penyaFor(a, days);
  document.getElementById('pResult').innerHTML = `
    <div class="ai-res-row"><span>Gecikmə</span><b>${days} gün${days > 365 ? ' (365 gün hesablanır)' : ''}</b></div>
    <div class="ai-res-row"><span>Gündəlik faiz</span><b>${money(r2(a * PENYA_RATE))}</b></div>
    <div class="ai-res-row"><span>Hesablanmış penya</span><b class="neg">${money(p)}</b></div>
    <div class="ai-res-row total"><span>Cəmi ödəniləcək</span><b>${money(r2(a + p))}</b></div>
    ${days === 0 ? '<div class="ai-res-note">Ödəniş son tarixdə və ya ondan əvvəl edilsə, faiz hesablanmır.</div>' : ''}`;
}
['pAmount', 'pDue', 'pPay'].forEach(id => document.getElementById(id).addEventListener('input', renderPenya));

function renderSalary() {
  const g = parseFloat(document.getElementById('sGross').value) || 0;
  const n = Math.max(1, parseInt(document.getElementById('sCount').value, 10) || 1);
  const s = salaryCalc(g);
  const row = (k, v, cls = '') => `<div class="ai-res-row ${cls}"><span>${k}</span><b>${money(v)}${n > 1 ? `<small> · ${n} nəfər: ${money(r2(v * n))}</small>` : ''}</b></div>`;
  document.getElementById('sResult').innerHTML = `
    <div class="ai-res-group">İşçidən tutulur</div>
    ${row('Gəlir vergisi', s.it)}${row('DSMF (işçi)', s.dsmfE)}${row('İşsizlikdən sığorta 0,5%', s.unE)}${row('İcbari tibbi sığorta', s.medE)}
    ${row('Əlinə çatan', s.net, 'total')}
    <div class="ai-res-group">İşəgötürən ödəyir</div>
    ${row('DSMF (işəgötürən)', s.dsmfR)}${row('İşsizlikdən sığorta 0,5%', s.unR)}${row('İcbari tibbi sığorta', s.medR)}
    ${row('İşəgötürənin ümumi xərci', s.cost, 'total')}
    <div class="ai-res-note">Gəlir vergisi: 2 500 AZN-dək 3% (ilk 200 AZN azad), 2 500–8 000 AZN: 75 + 10%, 8 000-dən çox: 625 + 14%.</div>`;
}
['sGross', 'sCount'].forEach(id => document.getElementById(id).addEventListener('input', renderSalary));

let vatMode = 'extract';
function renderVat() {
  const a = parseFloat(document.getElementById('vAmount').value) || 0;
  const vat = vatMode === 'extract' ? r2(a * 18 / 118) : r2(a * 0.18);
  const net = vatMode === 'extract' ? r2(a - vat) : a;
  const gross = vatMode === 'extract' ? a : r2(a + vat);
  document.getElementById('vResult').innerHTML = `
    <div class="ai-res-row"><span>ƏDV-siz məbləğ</span><b>${money(net)}</b></div>
    <div class="ai-res-row"><span>ƏDV (18%)</span><b>${money(vat)}</b></div>
    <div class="ai-res-row total"><span>ƏDV daxil məbləğ</span><b>${money(gross)}</b></div>`;
}
document.getElementById('vAmount').addEventListener('input', renderVat);
document.getElementById('vMode').addEventListener('click', e => {
  const b = e.target.closest('button[data-m]'); if (!b) return;
  vatMode = b.dataset.m;
  document.querySelectorAll('#vMode button').forEach(x => x.classList.toggle('active', x === b));
  renderVat();
});

// ---------------------------------------------------------------
// Sənəd analizi (ssenarili) və qanun yenilikləri
// ---------------------------------------------------------------
async function analyzeDoc(fileName) {
  const box = document.getElementById('docResult');
  const steps = ['Sənəd yüklənir', 'Mətn tanınır (OCR)', 'Sənədin növü müəyyən edilir', 'Şirkətin məlumatları ilə müqayisə olunur'];
  for (let i = 0; i < steps.length; i++) {
    box.innerHTML = `<div class="ai-doc-progress">${steps.map((s, j) => `<div class="${j < i ? 'done' : j === i ? 'now' : ''}">${j < i ? '✓' : j === i ? '…' : '•'} ${s}</div>`).join('')}</div>`;
    await wait(500);
  }
  const od = overdueList()[0] || obligations.find(o => o.taxId === 'tv') || obligations[0];
  const days = Math.max(0, daysBetween(od.due, TODAY_ISO));
  const p = penyaFor(od.remaining || od.amount, days);
  const debt = od.remaining || od.amount;
  const company = Company.current().name;
  const letter = `${company} (VÖEN ${companyVoen()})
Dövlət Vergi Xidmətinə

Bildirişinizə cavab

Hörmətli vergi orqanı,

${TAX[od.taxId].name} üzrə ${od.label} dövrü üçün ${money(debt)} məbləğində vergi borcu və ${money(p)} məbləğində faiz barədə bildirişinizi aldıq.

Borcun əsas məbləği və hesablanmış faiz ${Fmt.dateAz(TODAY_ISO)} tarixində büdcəyə ödəniləcək. Ödəniş tapşırığının surəti əlavə olunur.

Gecikmə daxili ödəniş prosesindəki texniki səbəbdən baş vermişdir; təkrarlanmaması üçün avtomatik xatırlatma sistemi tətbiq edilmişdir.

Hörmətlə,
Direktor ______________          Baş mühasib ______________`;
  box.innerHTML = `
    ${fileName ? `<div class="ai-demo-warn">“${esc(fileName)}” yükləndi. Demo rejimində faylın məzmunu oxunmur — aşağıda nümunə analiz göstərilir.</div>` : ''}
    <div class="ai-doc">
      <div class="ai-doc-type"><span class="ai-sev critical">Bildiriş</span> Vergi borcu barədə bildiriş — ${esc(TAX[od.taxId].name)}</div>
      <div class="ai-review-text"><span class="ai-avatar-sm">${AI_ICON}</span>
        <p>Vergi orqanı ${esc(od.label)} üzrə <b>${money(debt)}</b> vergi borcunun və ${days} günlük gecikməyə görə hesablanmış <b>${money(p)}</b> faizin ödənilməsini tələb edir. Məlumatlar şirkətin uçotu ilə üst-üstə düşür — borc həqiqətən ödənilməyib.</p></div>
      <div class="ai-res-group">Əsas məqamlar</div>
      <div class="ai-res-row"><span>Vergi növü</span><b>${esc(TAX[od.taxId].name)}</b></div>
      <div class="ai-res-row"><span>Dövr / son tarix</span><b>${esc(od.label)} · ${Fmt.dateAz(od.due)}</b></div>
      <div class="ai-res-row"><span>Borc</span><b>${money(debt)}</b></div>
      <div class="ai-res-row"><span>Faiz (0,1% × ${days} gün)</span><b class="neg">${money(p)}</b></div>
      <div class="ai-res-row total"><span>Cəmi</span><b>${money(r2(debt + p))}</b></div>
      <div class="ai-res-group">Tövsiyə olunan addımlar</div>
      <ol class="ai-steps"><li>Borcu və faizi bu gün ödəyin — faiz hər gün ${money(r2(debt * PENYA_RATE))} artır.</li>
        <li>Faizi ayrıca büdcə təsnifat kodu ilə ödəyin (“Faiz (penya)” təyinatı).</li>
        <li>Ödəniş tapşırığının surətini cavab məktubuna əlavə edin.</li></ol>
      <div class="ai-res-group">Cavab məktubunun qaralaması</div>
      <textarea class="ai-letter" id="letterText" rows="12">${esc(letter)}</textarea>
      <div class="ai-f-actions">
        <a class="btn-primary" href="vergi.html?pay=${od.taxId}|${od.key}#odenis">Borcu ödə</a>
        <button class="vg-btn-outline" type="button" id="copyLetter">Mətni kopyala</button>
        <button class="vg-btn-outline" type="button" id="dlLetter">.txt yüklə</button>
      </div>
    </div>`;
  document.getElementById('copyLetter').addEventListener('click', async () => {
    try { await navigator.clipboard.writeText(document.getElementById('letterText').value); showToast('Mətn kopyalandı'); }
    catch (e) { document.getElementById('letterText').select(); showToast('Mətni seçdim — Ctrl+C ilə kopyalayın', 'info'); }
  });
  document.getElementById('dlLetter').addEventListener('click', () => {
    const url = URL.createObjectURL(new Blob([document.getElementById('letterText').value], { type: 'text/plain;charset=utf-8' }));
    const a = document.createElement('a'); a.href = url; a.download = 'cavab-mektubu.txt'; document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
  });
}
document.getElementById('sampleDoc').addEventListener('click', () => analyzeDoc(''));
document.getElementById('docFile').addEventListener('change', e => { const f = e.target.files[0]; if (f) analyzeDoc(f.name); e.target.value = ''; });
const drop = document.getElementById('dropZone');
['dragenter', 'dragover'].forEach(ev => drop.addEventListener(ev, e => { e.preventDefault(); drop.classList.add('over'); }));
['dragleave', 'drop'].forEach(ev => drop.addEventListener(ev, e => { e.preventDefault(); drop.classList.remove('over'); }));
drop.addEventListener('drop', e => { const f = e.dataTransfer.files[0]; if (f) analyzeDoc(f.name); });

function renderNews() {
  // Şirkətə təsir: maaş fondu — 24 əməkdaş × 1 500 AZN (demo) — əvvəlki güzəştlə gəlir vergisi 0 idi
  const staff = 24, avg = 1500;
  const now = salaryCalc(avg).it;
  const y27 = Math.max(0, avg - 200) * 0.05, y28 = Math.max(0, avg - 200) * 0.07;
  document.getElementById('newsList').innerHTML = `
    <article class="ai-news">
      <div class="ai-news-date">1 yanvar 2026 · <a href="https://www.taxes.gov.az/az/post/4461" target="_blank" rel="noopener">taxes.gov.az</a></div>
      <h3>Özəl qeyri-neft-qaz sektorunda gəlir vergisinin yeni mexanizmi</h3>
      <p>7 illik güzəşt (8 000 AZN-dək 0%) başa çatdı. 2026-dan aylıq gəlir 2 500 AZN-dək olanda 3% (ilk 200 AZN vergidən azad), 2 500–8 000 AZN — 75 AZN + 10%, 8 000 AZN-dən çox — 625 AZN + 14%.</p>
      <div class="ai-impact"><span class="ai-avatar-sm">${AI_ICON}</span><div><b>Şirkətinizə təsiri:</b> ${staff} əməkdaş, orta əmək haqqı ${money(avg)} (demo) — hər işçidən ayda ${money(now)}, şirkət üzrə ayda <b>${money(r2(now * staff))}</b> gəlir vergisi tutulmalıdır. İşçilərin əlinə çatan məbləğ bu qədər azalır.</div></div>
    </article>
    <article class="ai-news">
      <div class="ai-news-date">2027–2028 · <a href="https://www.taxes.gov.az/az/post/4461" target="_blank" rel="noopener">taxes.gov.az</a></div>
      <h3>Dərəcə mərhələli artacaq: 2027-də 5%, 2028-dən 7%</h3>
      <p>2 500 AZN-dək gəlirə dərəcə 2027-də 5%-ə, 2028-dən 7%-ə qalxır; yuxarı pillələrdə sabit məbləğ müvafiq olaraq 125 və 175 AZN olur.</p>
      <div class="ai-impact"><span class="ai-avatar-sm">${AI_ICON}</span><div><b>Planlaşdırma üçün:</b> eyni əmək haqqı fondunda aylıq gəlir vergisi 2027-də ≈ ${money(r2(y27 * staff))}, 2028-də ≈ ${money(r2(y28 * staff))} olacaq. Əmək haqqı siyasətini (net razılaşmalar) əvvəlcədən nəzərdən keçirin.</div></div>
    </article>
    <article class="ai-news">
      <div class="ai-news-date">Daimi qayda · Vergi Məcəlləsi, 59-cu maddə</div>
      <h3>Gecikmiş vergiyə görə faiz: gündə 0,1%</h3>
      <p>Vergi vaxtında ödənilmədikdə hər gecikdirilmiş gün üçün ödənilməmiş məbləğin 0,1%-i həcmində faiz hesablanır, lakin bir ildən çox olmayaraq.</p>
      <div class="ai-impact"><span class="ai-avatar-sm">${AI_ICON}</span><div>${overdueList().length ? `<b>Sizə aiddir:</b> hazırda ${overdueList().length} gecikmiş ödəniş var — <a href="#" data-goto="findings">tapıntılara bax</a>.` : 'Hazırda gecikmiş ödənişiniz yoxdur ✓'}</div></div>
    </article>`;
}
document.getElementById('newsList').addEventListener('click', e => { const a = e.target.closest('[data-goto]'); if (a) { e.preventDefault(); switchTab(a.dataset.goto); } });

// ---------------------------------------------------------------
// Tablar və başlanğıc
// ---------------------------------------------------------------
function switchTab(name) {
  document.querySelectorAll('.vg-tab').forEach(b => b.classList.toggle('active', b.dataset.tab === name));
  document.querySelectorAll('.vg-panel').forEach(p => p.classList.toggle('active', p.id === 'panel-' + name));
  try { history.replaceState(null, '', '#' + name); } catch (e) { /* file:// */ }
  if (name === 'chat') setTimeout(() => document.getElementById('chatInput').focus(), 50);
}
document.querySelectorAll('.vg-tab').forEach(b => b.addEventListener('click', () => switchTab(b.dataset.tab)));

(async function init() {
  const h = (location.hash || '').slice(1);
  const first = !sessionStorage.getItem('corbank_ai_scanned');
  try { sessionStorage.setItem('corbank_ai_scanned', '1'); } catch (e) { /* */ }
  const loading = loadData();
  await Promise.all([loading, scan(first && (!h || h === 'findings'))]);
  document.getElementById('opBank').innerHTML = '<option value="all">Bütün banklar</option>' +
    [...new Set(accounts.map(a => a.bank))].map(b => `<option value="${b}">${esc(bankTitle(b))}</option>`).join('');
  renderFindings(); renderOps();
  const od = overdueList()[0];
  if (od) setPenya(od.remaining, od.due); else { document.getElementById('pDue').value = TODAY_ISO; document.getElementById('pPay').value = TODAY_ISO; renderPenya(); }
  renderSalary(); renderVat(); renderNews();
  const name = (Prefs.get().name || '').split(' ')[0];
  bubble('bot', `Salam${name ? ', ' + esc(name) : ''}! Mən <b>Aİ Vergi Agentiyəm</b>. Şirkətinizin hesablarını, əməliyyatlarını və vergi öhdəliklərini görürəm. Aşağıdakı suallardan birini seçin və ya öz sualınızı yazın.`);
  if (['findings', 'ops', 'chat', 'calc', 'docs'].includes(h)) switchTab(h);
})();
