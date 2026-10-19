// Vergi və büdcə ödənişləri (vergi.html) — İcmal · Ödəniş et · Tarixçə
// common.js-dən istifadə edir: Api, Fmt, Icons, Company, bankTitle, bankLogoImg, accountLabel, openModal, closeModal, showToast, escHtml, demoNow

document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('muhasibatliq.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());
document.getElementById('iconBack').innerHTML = Icons.arrowLeft;
document.getElementById('iconCsv').innerHTML = Icons.download;


const STATUS = {
  paid:    { label: 'Ödənilib', cls: 'paid' },
  pending: { label: 'Gözləyir', cls: 'pending' },
  overdue: { label: 'Gecikib',  cls: 'overdue' },
};
const chip = st => `<span class="vg-chip ${STATUS[st].cls}"><span class="dot"></span>${STATUS[st].label}</span>`;

function daysLeftHtml(o) {
  if (o.status === 'paid') return '';
  const d = daysBetween(TODAY_ISO, o.due);
  if (d < 0) return `<span class="vg-days overdue">${-d} gün gecikib</span>`;
  if (d === 0) return `<span class="vg-days soon">Bu gün son gündür</span>`;
  return `<span class="vg-days ${d <= 3 ? 'soon' : d <= 7 ? 'near' : ''}">${d} gün qalıb</span>`;
}

// ---------------------------------------------------------------
// Tablar
// ---------------------------------------------------------------
function switchTab(name) {
  document.querySelectorAll('.vg-tab').forEach(b => b.classList.toggle('active', b.dataset.tab === name));
  document.querySelectorAll('.vg-panel').forEach(p => p.classList.toggle('active', p.id === 'panel-' + name));
  try { history.replaceState(null, '', '#' + name); } catch (e) { /* file:// */ }
}
document.querySelectorAll('.vg-tab').forEach(b => b.addEventListener('click', () => switchTab(b.dataset.tab)));

// ---------------------------------------------------------------
// İCMAL
// ---------------------------------------------------------------
function renderOverview() {
  const unpaid = obligations.filter(o => o.status !== 'paid');
  const overdue = unpaid.filter(o => o.status === 'overdue');
  const ym = TODAY_ISO.slice(0, 7);
  const thisMonth = unpaid.filter(o => o.due.slice(0, 7) === ym);
  const next = unpaid.filter(o => o.status === 'pending').sort((a, b) => a.due.localeCompare(b.due))[0];
  const paidYear = allPayments().filter(p => p.status === 'completed' && p.date.slice(0, 4) === String(YEAR)).reduce((s, p) => s + p.amount, 0);
  const sum = list => list.reduce((s, o) => s + o.remaining, 0);

  // Xəbərdarlıqlar
  const soon = unpaid.filter(o => o.status === 'pending' && daysBetween(TODAY_ISO, o.due) <= 3);
  let alerts = '';
  if (overdue.length) {
    alerts += `<div class="vg-alert danger">
      <div><b>${overdue.length} gecikmiş ödəniş var</b> — ${overdue.map(o => `${esc(TAX[o.taxId].short)} (${esc(o.label)}, son tarix ${Fmt.dateAz(o.due)})`).join(', ')}.
      Gecikmiş hər gün üçün faiz (penya) hesablanır.</div>
      <button class="btn-primary vg-alert-btn" data-pay="${overdue[0].taxId}|${overdue[0].key}">İndi ödə</button></div>`;
  }
  if (soon.length) {
    alerts += `<div class="vg-alert warn"><div><b>Son tarix yaxınlaşır:</b> ${soon.map(o => `${esc(TAX[o.taxId].short)} — ${Fmt.dateAz(o.due)}`).join(', ')}</div></div>`;
  }
  document.getElementById('vgAlerts').innerHTML = alerts;

  document.getElementById('vgKpis').innerHTML = `
    <div class="vg-kpi"><div class="lbl">Bu ay ödənilməli</div><div class="val">${Fmt.money(sum(thisMonth), 'AZN')}</div><div class="sub">${thisMonth.length} öhdəlik · ${MONTHS[TODAY.getMonth()]}</div></div>
    <div class="vg-kpi"><div class="lbl">Ən yaxın son tarix</div><div class="val">${next ? Fmt.dateAz(next.due) : '—'}</div><div class="sub">${next ? `${esc(TAX[next.taxId].short)} · ${daysBetween(TODAY_ISO, next.due)} gün qalıb` : 'Gözləyən ödəniş yoxdur'}</div></div>
    <div class="vg-kpi ${overdue.length ? 'danger' : ''}"><div class="lbl">Gecikmiş ödənişlər</div><div class="val">${Fmt.money(sum(overdue), 'AZN')}</div><div class="sub">${overdue.length ? `${overdue.length} öhdəlik` : 'Gecikmə yoxdur'}</div></div>
    <div class="vg-kpi"><div class="lbl">${YEAR}-ci ildə ödənilib</div><div class="val">${Fmt.money(paidYear, 'AZN')}</div><div class="sub">bütün banklar üzrə</div></div>`;

  // Yaxınlaşan: gecikmişlər + növbəti 30 gün (bu ay ödənilənlər də görünür)
  const horizon = new Date(TODAY); horizon.setDate(horizon.getDate() + 30);
  const monthStart = ym + '-01';
  const list = obligations.filter(o => o.status === 'overdue' || (o.due >= monthStart && o.due <= iso(horizon)))
    .sort((a, b) => (a.status === 'overdue' ? -1 : 0) - (b.status === 'overdue' ? -1 : 0) || a.due.localeCompare(b.due));
  document.getElementById('upcomingNote').textContent = 'növbəti 30 gün';
  document.getElementById('upcomingList').innerHTML = list.length ? list.map(o => `
    <div class="vg-up ${o.status}">
      <div class="vg-up-date"><b>${parseIso(o.due).getDate()}</b><span>${MONTHS[parseIso(o.due).getMonth()].slice(0, 3)}</span></div>
      <div class="vg-up-main">
        <div class="vg-up-name">${esc(TAX[o.taxId].name)}</div>
        <div class="vg-up-meta">${esc(o.label)} · Son tarix ${Fmt.dateAz(o.due)} ${daysLeftHtml(o)}</div>
      </div>
      <div class="vg-up-amount">${Fmt.money(o.status === 'paid' ? o.amount : o.remaining, 'AZN')}</div>
      <div class="vg-up-status">${chip(o.status)}</div>
      <div class="vg-up-act">${o.status === 'paid'
        ? `<button class="vg-btn-outline" data-receipt-for="${o.taxId}|${o.key}">Qəbz</button>`
        : `<button class="btn-primary" data-pay="${o.taxId}|${o.key}">Ödə</button>`}</div>
    </div>`).join('') : '<div class="vg-empty">Yaxın günlərdə ödəniş yoxdur.</div>';

  renderCalendar();
}

// Təqvim
let calMonth = new Date(TODAY.getFullYear(), TODAY.getMonth(), 1);
let calSelected = null;
function renderCalendar() {
  const y = calMonth.getFullYear(), m = calMonth.getMonth();
  document.getElementById('calTitle').textContent = `${MONTHS_CAP[m]} ${y}`;
  const byDay = {};
  obligations.forEach(o => { if (o.due.slice(0, 7) === `${y}-${String(m + 1).padStart(2, '0')}`) (byDay[o.due] = byDay[o.due] || []).push(o); });
  const first = new Date(y, m, 1);
  const lead = (first.getDay() + 6) % 7;                // bazar ertəsi ilə başlayır
  const days = new Date(y, m + 1, 0).getDate();
  let html = ['B.e', 'Ç.a', 'Ç', 'C.a', 'C', 'Ş', 'B'].map(d => `<div class="vg-cal-wd">${d}</div>`).join('');
  for (let i = 0; i < lead; i++) html += '<div></div>';
  for (let d = 1; d <= days; d++) {
    const key = `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    const items = byDay[key] || [];
    const st = items.some(o => o.status === 'overdue') ? 'overdue' : items.some(o => o.status === 'pending') ? 'pending' : items.length ? 'paid' : '';
    html += `<button type="button" class="vg-cal-d${key === TODAY_ISO ? ' today' : ''}${items.length ? ' has ' + st : ''}${key === calSelected ? ' sel' : ''}" data-day="${key}" ${items.length ? '' : 'tabindex="-1"'}>
      <span>${d}</span>${items.length ? `<i>${items.length}</i>` : ''}</button>`;
  }
  document.getElementById('calGrid').innerHTML = html;
  const sel = calSelected && byDay[calSelected];
  document.getElementById('calDay').innerHTML = sel
    ? `<div class="vg-cal-day-title">${Fmt.dateAz(calSelected)}</div>` + sel.map(o => `
        <div class="vg-cal-item"><span>${esc(TAX[o.taxId].short)} <span class="vg-muted">· ${esc(o.label)}</span></span>${chip(o.status)}</div>`).join('')
    : '<div class="vg-muted vg-cal-tip">Son tarixləri görmək üçün rəqəmli günə klikləyin.</div>';
}
document.getElementById('calPrev').addEventListener('click', () => { calMonth.setMonth(calMonth.getMonth() - 1); calSelected = null; renderCalendar(); });
document.getElementById('calNext').addEventListener('click', () => { calMonth.setMonth(calMonth.getMonth() + 1); calSelected = null; renderCalendar(); });
document.getElementById('calGrid').addEventListener('click', e => {
  const b = e.target.closest('.vg-cal-d.has'); if (!b) return;
  calSelected = calSelected === b.dataset.day ? null : b.dataset.day; renderCalendar();
});

document.getElementById('panel-icmal').addEventListener('click', e => {
  const pay = e.target.closest('[data-pay]');
  if (pay) { const [taxId, key] = pay.dataset.pay.split('|'); prefillForm(taxId, key); switchTab('odenis'); window.scrollTo({ top: 0, behavior: 'smooth' }); return; }
  const rc = e.target.closest('[data-receipt-for]');
  if (rc) {
    const [taxId, key] = rc.dataset.receiptFor.split('|');
    const p = allPayments().find(x => x.taxId === taxId && x.periodKey === key && x.kind === 'esas');
    if (p) printReceipt(p);
  }
});

// ---------------------------------------------------------------
// ÖDƏNİŞ ET
// ---------------------------------------------------------------
let aznAccounts = [];
const form = { taxId: 'edv', kind: 'esas', periodKey: '', accountId: '', amount: '', note: '' };

document.getElementById('fTax').innerHTML = ['Vergilər', 'Sosial ödənişlər'].map(g =>
  `<optgroup label="${g}">${TAXES.filter(t => t.group === g).map(t => `<option value="${t.id}">${esc(t.name)}</option>`).join('')}</optgroup>`).join('');

function periodOptions(taxId) {
  // Son tarixi 400 gün əvvəldən 75 gün sonrayadək olan dövrlər, yenidən köhnəyə
  return obligations.filter(o => o.taxId === taxId).sort((a, b) => b.due.localeCompare(a.due));
}
function defaultPeriod(taxId) {
  const opts = periodOptions(taxId);
  const unpaid = opts.filter(o => o.status !== 'paid').sort((a, b) => a.due.localeCompare(b.due));
  return (unpaid[0] || opts.find(o => o.due >= TODAY_ISO) || opts[0] || {}).key || '';
}
const currentObligation = () => obligations.find(o => o.taxId === form.taxId && o.key === form.periodKey);

function renderPeriodSelect() {
  const opts = periodOptions(form.taxId);
  document.getElementById('fPeriod').innerHTML = opts.map(o =>
    `<option value="${o.key}" ${o.key === form.periodKey ? 'selected' : ''}>${esc(o.label)} — son tarix ${Fmt.dateAz(o.due)} · ${STATUS[o.status].label.toLowerCase()}</option>`).join('');
}

function suggestAmount() {
  const o = currentObligation();
  if (form.kind !== 'esas' || !o) return '';
  return o.remaining > 0 ? o.remaining.toFixed(2) : '';
}

function renderAccounts() {
  document.getElementById('fAccounts').innerHTML = aznAccounts.map(a => `
    <label class="vg-acc ${a.id === form.accountId ? 'active' : ''}">
      <input type="radio" name="acc" value="${esc(a.id)}" ${a.id === form.accountId ? 'checked' : ''}>
      ${bankLogoImg(bankTitle(a.bank), 30)}
      <span class="vg-acc-main"><b>${esc(bankTitle(a.bank))}</b><span>${esc(a.iban.slice(0, 4) + ' … ' + a.iban.slice(-4))}</span></span>
      <span class="vg-acc-bal">${Fmt.money(a.balance, 'AZN')}</span>
    </label>`).join('') || '<div class="vg-empty">AZN hesabı tapılmadı.</div>';
}

function hints() {
  const o = currentObligation();
  const ph = document.getElementById('periodHint');
  if (o && form.kind === 'esas') {
    ph.innerHTML = o.status === 'paid'
      ? `Bu dövr üzrə öhdəlik tam ödənilib (${Fmt.money(o.paid, 'AZN')}).`
      : `Hesablanmış öhdəlik: <b>${Fmt.money(o.amount, 'AZN')}</b>${o.paid ? ` · ödənilib ${Fmt.money(o.paid, 'AZN')}` : ''} · qalıq <b>${Fmt.money(o.remaining, 'AZN')}</b> ${daysLeftHtml(o)}`;
  } else if (form.kind !== 'esas') {
    ph.textContent = `${KINDS[form.kind].label} ayrıca büdcə təsnifat kodu ilə ödənilir.`;
  } else ph.textContent = '';
  const acc = aznAccounts.find(a => a.id === form.accountId);
  document.getElementById('amountHint').textContent = acc ? `Hesabda mövcud: ${Fmt.money(acc.balance, 'AZN')}` : '';
}

function codeFor() { return KINDS[form.kind].code || TAX[form.taxId].code; }
function purposeText() {
  const t = TAX[form.taxId];
  const parts = [t.name, KINDS[form.kind].label.toLowerCase(), periodLabel(form.taxId, form.periodKey), `VÖEN ${companyVoen()}`];
  if (form.note.trim()) parts.push(form.note.trim());
  return parts.join(' · ');
}

function renderPreview() {
  const acc = aznAccounts.find(a => a.id === form.accountId);
  const amt = parseFloat(form.amount);
  const rows = [
    ['Ödəyici', `${esc(Company.current().name)}<br><span class="vg-muted">VÖEN ${companyVoen()}</span>`],
    ['Ödəyicinin hesabı', acc ? `${esc(accountLabel(acc))}<br><span class="vg-muted mono">${esc(acc.iban)}</span>` : '—'],
    ['Alan', `${TREASURY.name}<br><span class="vg-muted mono">${TREASURY.iban}</span>`],
    ['Büdcə təsnifat kodu', `<span class="mono">${codeFor()}</span>`],
    ['Dövr', esc(periodLabel(form.taxId, form.periodKey))],
    ['Təyinat', esc(purposeText())],
  ];
  document.getElementById('orderPreview').innerHTML = `
    <div class="vg-order-amount">${amt > 0 ? Fmt.money(amt, 'AZN') : '0,00 AZN'}</div>
    ${rows.map(([k, v]) => `<div class="vg-order-row"><span class="k">${k}</span><span class="v">${v}</span></div>`).join('')}`;
}

function syncForm(full = true) {
  if (full) renderPeriodSelect();
  document.querySelectorAll('#fKind button').forEach(b => b.classList.toggle('active', b.dataset.kind === form.kind));
  document.getElementById('fTax').value = form.taxId;
  document.getElementById('fAmount').value = form.amount;
  document.getElementById('fNote').value = form.note;
  renderAccounts();
  hints();
  renderPreview();
  document.getElementById('formError').textContent = '';
}

function prefillForm(taxId, key) {
  form.taxId = taxId; form.kind = 'esas'; form.periodKey = key || defaultPeriod(taxId);
  form.amount = suggestAmount(); form.note = '';
  syncForm();
}

document.getElementById('fTax').addEventListener('change', e => { form.taxId = e.target.value; form.periodKey = defaultPeriod(form.taxId); form.amount = suggestAmount(); syncForm(); });
document.getElementById('fPeriod').addEventListener('change', e => { form.periodKey = e.target.value; form.amount = suggestAmount(); syncForm(false); });
document.getElementById('fKind').addEventListener('click', e => {
  const b = e.target.closest('button[data-kind]'); if (!b) return;
  form.kind = b.dataset.kind; form.amount = suggestAmount(); syncForm(false);
});
document.getElementById('fAccounts').addEventListener('change', e => { form.accountId = e.target.value; syncForm(false); });
document.getElementById('fAmount').addEventListener('input', e => { form.amount = e.target.value; hints(); renderPreview(); });
document.getElementById('fNote').addEventListener('input', e => { form.note = e.target.value; renderPreview(); });
document.getElementById('resetBtn').addEventListener('click', () => prefillForm(form.taxId));

function validate() {
  const amt = parseFloat(form.amount);
  const acc = aznAccounts.find(a => a.id === form.accountId);
  if (!form.periodKey) return 'Dövr seçin';
  if (!acc) return 'Ödəniş hesabını seçin';
  if (!(amt > 0)) return 'Məbləği daxil edin';
  if (amt > acc.balance) return 'Seçilmiş hesabda kifayət qədər vəsait yoxdur';
  return '';
}

document.getElementById('payForm').addEventListener('submit', e => {
  e.preventDefault();
  const err = validate();
  document.getElementById('formError').textContent = err;
  if (err) return;
  const acc = aznAccounts.find(a => a.id === form.accountId);
  const amt = Math.round(parseFloat(form.amount) * 100) / 100;
  const rows = [
    ['Vergi / ödəniş', esc(TAX[form.taxId].name)], ['Təyinat', KINDS[form.kind].label], ['Dövr', esc(periodLabel(form.taxId, form.periodKey))],
    ['Büdcə təsnifat kodu', codeFor()], ['Ödəniş hesabı', esc(accountLabel(acc))], ['Alan', TREASURY.name], ['Məbləğ', Fmt.money(amt, 'AZN')],
    ['Ödənişdən sonra qalıq', Fmt.money(acc.balance - amt, 'AZN')],
  ];
  openModal({
    title: 'Ödənişi təsdiqləyin',
    subtitle: `${esc(Company.current().name)} · VÖEN ${companyVoen()}`,
    bodyHtml: rows.map(([k, v]) => `<div class="review-row"><span class="k">${k}</span><span class="v">${v}</span></div>`).join(''),
    footerHtml: `<button class="btn-ghost" type="button" onclick="closeModal()">Geri</button>
                 <button class="btn-primary" type="button" id="confirmPay">Təsdiqlə və ödə</button>`,
  });
  document.getElementById('confirmPay').addEventListener('click', async ev => {
    ev.target.disabled = true; ev.target.textContent = 'Göndərilir…';
    let res;
    try {
      res = await Api.post('/payments', { fromAccountId: acc.id, beneficiaryName: TREASURY.name, beneficiaryIban: TREASURY.iban,
        amount: amt, currency: 'AZN', description: purposeText() });
    } catch (x) { res = { status: 'rejected', message: 'Ödəniş göndərilə bilmədi' }; }
    const ok = res && res.status === 'completed';
    let rec = null;
    if (ok) {
      rec = { id: `pay-${Date.now()}`, taxId: form.taxId, kind: form.kind, periodKey: form.periodKey, amount: amt, accountId: acc.id,
        bank: acc.bank, date: TODAY_ISO, ref: refFor(Date.now(), TODAY_ISO), status: 'completed', note: form.note.trim() };
      extraPayments.unshift(rec); saveExtra();
      refreshStatuses();
      await loadAccounts();
      renderOverview(); renderHistory();
      form.amount = suggestAmount(); syncForm();
    }
    openModal({
      title: ok ? 'Ödəniş icra olundu' : 'Ödəniş alınmadı',
      bodyHtml: `<div class="result-box">
          <div class="result-icon ${ok ? 'success' : 'error'}">${ok ? '✓' : '!'}</div>
          <div class="result-title">${ok ? Fmt.money(amt, 'AZN') : 'Ödəniş rədd edildi'}</div>
          <div class="result-note">${ok ? `${esc(TAX[rec.taxId].short)} · ${esc(periodLabel(rec.taxId, rec.periodKey))}<br>İstinad: <b>${rec.ref}</b>` : esc(res.message || '')}</div>
          ${ok ? '<div class="result-disclaimer">Demo ödənişdir — real vəsait köçürülməyib.</div>' : ''}
        </div>`,
      footerHtml: ok
        ? `<button class="btn-ghost" type="button" id="toHistory">Tarixçəyə keç</button><button class="btn-primary" type="button" id="dlReceipt">Qəbzi yüklə</button>`
        : `<button class="btn-primary btn-block" type="button" onclick="closeModal()">Bağla</button>`,
    });
    if (ok) {
      document.getElementById('dlReceipt').addEventListener('click', () => printReceipt(rec));
      document.getElementById('toHistory').addEventListener('click', () => { closeModal(); switchTab('tarixce'); });
      showToast('Vergi ödənişi icra olundu');
    }
  });
});

// ---------------------------------------------------------------
// TARİXÇƏ
// ---------------------------------------------------------------
const H_PAGE = 10;
let hPage = 1;
const years = () => [...new Set(allPayments().map(p => p.date.slice(0, 4)))].sort().reverse();

function fillHistoryFilters() {
  const ys = years();
  document.getElementById('hTax').innerHTML = '<option value="all">Bütün növlər</option>' + TAXES.map(t => `<option value="${t.id}">${esc(t.short)}</option>`).join('');
  document.getElementById('hYear').innerHTML = '<option value="all">Bütün illər</option>' + ys.map(y => `<option value="${y}">${y}</option>`).join('');
  document.getElementById('hBank').innerHTML = '<option value="all">Bütün banklar</option>' + ['PASHA', 'ABB'].map(b => `<option value="${b}">${bankTitle(b)}</option>`).join('');
  document.getElementById('sumYear').innerHTML = ys.map(y => `<option value="${y}">${y}</option>`).join('');
}

function filteredHistory() {
  const t = document.getElementById('hTax').value, y = document.getElementById('hYear').value, b = document.getElementById('hBank').value;
  return allPayments().filter(p => (t === 'all' || p.taxId === t) && (y === 'all' || p.date.startsWith(y)) && (b === 'all' || p.bank === b));
}

function renderHistory() {
  const list = filteredHistory();
  const pages = Math.max(1, Math.ceil(list.length / H_PAGE));
  hPage = Math.min(Math.max(1, hPage), pages);
  const slice = list.slice((hPage - 1) * H_PAGE, hPage * H_PAGE);
  document.getElementById('histBody').innerHTML = slice.map(p => `
    <tr>
      <td class="vg-date">${Fmt.dateAz(p.date)}</td>
      <td><div class="vg-h-name">${esc(TAX[p.taxId].short)}${p.kind !== 'esas' ? ` <span class="vg-tag">${KINDS[p.kind].label}</span>` : ''}</div>
          <div class="vg-muted vg-h-sub">${esc(periodLabel(p.taxId, p.periodKey))} · ${p.ref}</div></td>
      <td class="mono">${KINDS[p.kind].code || TAX[p.taxId].code}</td>
      <td><span class="op-bank">${bankLogoImg(bankTitle(p.bank), 22)}<span>${esc(bankTitle(p.bank))}</span></span></td>
      <td class="vg-amt">−${Fmt.money(p.amount, 'AZN')}</td>
      <td><span class="vg-chip paid"><span class="dot"></span>İcra olundu</span></td>
      <td><button class="vg-btn-outline vg-btn-sm" data-receipt="${esc(p.id)}" title="Ödəniş tapşırığını PDF kimi saxla / çap et">${Icons.fileText} PDF</button></td>
    </tr>`).join('');
  document.getElementById('histEmpty').style.display = list.length ? 'none' : 'block';
  document.querySelector('.vg-table-wrap').style.display = list.length ? '' : 'none';
  document.getElementById('histCount').textContent = `${list.length} ödəniş · cəmi ${Fmt.money(list.reduce((s, p) => s + p.amount, 0), 'AZN')}`;
  document.getElementById('hPage').textContent = `${hPage} / ${pages}`;
  document.getElementById('hPrev').disabled = hPage <= 1;
  document.getElementById('hNext').disabled = hPage >= pages;
  renderYearSummary();
}

function renderYearSummary() {
  const y = document.getElementById('sumYear').value || String(YEAR);
  const pays = allPayments().filter(p => p.date.startsWith(y));
  const total = pays.reduce((s, p) => s + p.amount, 0);
  const byTax = TAXES.map(t => ({ t, sum: pays.filter(p => p.taxId === t.id).reduce((s, p) => s + p.amount, 0) }))
    .filter(x => x.sum > 0).sort((a, b) => b.sum - a.sum);
  const max = Math.max(1, ...byTax.map(x => x.sum));
  const byMonth = Array.from({ length: 12 }, (_, m) => pays.filter(p => Number(p.date.slice(5, 7)) === m + 1).reduce((s, p) => s + p.amount, 0));
  const mMax = Math.max(1, ...byMonth);
  const banks = ['PASHA', 'ABB'].map(b => ({ b, sum: pays.filter(p => p.bank === b).reduce((s, p) => s + p.amount, 0) }));
  document.getElementById('yearSummary').innerHTML = `
    <div class="vg-sum-top">
      <div class="vg-sum-total"><div class="lbl">${y}-ci ildə büdcəyə ödənilib</div><div class="val">${Fmt.money(total, 'AZN')}</div>
        <div class="vg-sum-banks">${banks.map(x => `<span class="op-bank">${bankLogoImg(bankTitle(x.b), 20)}<span>${bankTitle(x.b)}: <b>${Fmt.money(x.sum, 'AZN')}</b></span></span>`).join('')}</div>
        <div class="vg-sum-count">${pays.length} ödəniş</div>
      </div>
      <div class="vg-months" aria-label="Aylar üzrə ödənişlər">
        ${byMonth.map((v, m) => `<div class="vg-mcol" title="${MONTHS_CAP[m]}: ${Fmt.money(v, 'AZN')}"><div class="vg-mbar" style="height:${(v / mMax * 100).toFixed(1)}%"></div><span>${MONTHS_CAP[m].slice(0, 3)}</span></div>`).join('')}
      </div>
    </div>
    <div class="vg-bars">
      ${byTax.map(x => `<div class="vg-bar-row"><span class="vg-bar-lbl">${esc(x.t.short)}</span>
        <span class="vg-bar-track"><span style="width:${(x.sum / max * 100).toFixed(1)}%"></span></span>
        <span class="vg-bar-val">${Fmt.money(x.sum, 'AZN')}</span><span class="vg-bar-pct">${(x.sum / total * 100).toFixed(1).replace('.', ',')}%</span></div>`).join('') || '<div class="vg-empty">Bu il üçün ödəniş yoxdur.</div>'}
    </div>`;
}

['hTax', 'hYear', 'hBank'].forEach(id => document.getElementById(id).addEventListener('change', () => { hPage = 1; renderHistory(); }));
document.getElementById('sumYear').addEventListener('change', renderYearSummary);
document.getElementById('hPrev').addEventListener('click', () => { hPage--; renderHistory(); });
document.getElementById('hNext').addEventListener('click', () => { hPage++; renderHistory(); });
document.getElementById('histBody').addEventListener('click', e => {
  const b = e.target.closest('[data-receipt]'); if (!b) return;
  const p = allPayments().find(x => x.id === b.dataset.receipt); if (p) printReceipt(p);
});

document.getElementById('csvBtn').addEventListener('click', () => {
  const list = filteredHistory();
  if (!list.length) { showToast('İxrac üçün ödəniş yoxdur', 'error'); return; }
  const header = ['Ödəniş tarixi', 'İstinad', 'Vergi növü', 'Təyinat', 'Dövr', 'Büdcə təsnifat kodu', 'Bank', 'Hesab (IBAN)', 'Məbləğ (AZN)', 'Status'];
  const rows = list.map(p => {
    const acc = aznAccounts.find(a => a.id === p.accountId);
    return [Fmt.dateAz(p.date), p.ref, TAX[p.taxId].name, KINDS[p.kind].label, periodLabel(p.taxId, p.periodKey), KINDS[p.kind].code || TAX[p.taxId].code,
      bankTitle(p.bank), acc ? acc.iban : '', p.amount.toFixed(2).replace('.', ','), 'İcra olundu'];
  });
  const csv = [header, ...rows].map(r => r.map(c => `"${String(c).replace(/"/g, '""')}"`).join(';')).join('\r\n');
  const url = URL.createObjectURL(new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' }));
  const a = document.createElement('a'); a.href = url; a.download = 'vergi-odenisleri.csv';
  document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
  showToast('CSV fayl endirildi');
});

// ---------------------------------------------------------------
// Qəbz — çap pəncərəsi (brauzerin "PDF kimi saxla" funksiyası ilə)
// ---------------------------------------------------------------
function printReceipt(p) {
  const acc = aznAccounts.find(a => a.id === p.accountId);
  const t = TAX[p.taxId];
  const purpose = [t.name, KINDS[p.kind].label.toLowerCase(), periodLabel(p.taxId, p.periodKey), `VÖEN ${companyVoen()}`, p.note].filter(Boolean).join(' · ');
  const rows = [
    ['Tarix', Fmt.dateAz(p.date)], ['İstinad nömrəsi', p.ref],
    ['Ödəyici', `${Company.current().name} (VÖEN ${companyVoen()})`],
    ['Ödəyicinin bankı', bankTitle(p.bank)], ['Ödəyicinin hesabı', acc ? acc.iban : '—'],
    ['Alan', TREASURY.name], ['Alanın hesabı', TREASURY.iban], ['SWIFT', TREASURY.swift],
    ['Büdcə təsnifat kodu', KINDS[p.kind].code || t.code], ['Ödənişin təyinatı', purpose],
    ['Məbləğ', Fmt.money(p.amount, 'AZN')], ['Status', 'İcra olundu'],
  ];
  const html = `<!doctype html><html lang="az"><head><meta charset="utf-8"><title>Ödəniş tapşırığı ${p.ref}</title>
  <style>
    body{font-family:Inter,Segoe UI,Arial,sans-serif;color:#16213e;margin:40px;}
    .top{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:2px solid #2f5fe0;padding-bottom:14px;margin-bottom:22px}
    .brand{font-size:20px;font-weight:700}.brand small{display:block;font-size:12px;color:#6b7896;font-weight:500}
    h1{font-size:18px;margin:0 0 4px;text-align:right}.ref{color:#6b7896;font-size:12.5px;text-align:right}
    table{width:100%;border-collapse:collapse;font-size:13.5px}td{padding:9px 6px;border-bottom:1px solid #e3e9f3;vertical-align:top}
    td:first-child{color:#6b7896;width:34%}.amt td:last-child{font-size:17px;font-weight:700}
    .stamp{display:inline-block;margin-top:26px;border:2px solid #1a9c5b;color:#1a9c5b;font-weight:700;padding:8px 16px;border-radius:8px;transform:rotate(-4deg)}
    .note{margin-top:26px;font-size:11.5px;color:#6b7896}
    @media print{body{margin:18mm}}
  </style></head><body>
  <div class="top"><div class="brand">COR Bank<small>Business</small></div>
    <div><h1>ÖDƏNİŞ TAPŞIRIĞI</h1><div class="ref">№ ${p.ref}</div></div></div>
  <table>${rows.map(([k, v]) => `<tr class="${k === 'Məbləğ' ? 'amt' : ''}"><td>${esc(k)}</td><td>${esc(v)}</td></tr>`).join('')}</table>
  <div class="stamp">İCRA OLUNDU · ${Fmt.dateAz(p.date)}</div>
  <div class="note">Demo sənəddir — real bank əməliyyatı deyil. Sənəd COR Bank Business platformasında avtomatik yaradılıb.</div>
  <script>window.onload=function(){setTimeout(function(){window.print()},250)}<\/script>
  </body></html>`;
  const w = window.open('', '_blank');
  if (!w) { showToast('Brauzer yeni pəncərəni blokladı — pop-up icazəsi verin', 'error'); return; }
  w.document.open(); w.document.write(html); w.document.close();
}

// ---------------------------------------------------------------
// Yükləmə
// ---------------------------------------------------------------
async function loadAccounts() {
  try {
    const res = await Api.get('/accounts');
    aznAccounts = (res.accounts || []).filter(a => a.currency === 'AZN');
  } catch (e) { aznAccounts = []; }
  if (!aznAccounts.find(a => a.id === form.accountId)) form.accountId = (aznAccounts[0] || {}).id || '';
}

(async function init() {
  buildData();
  await loadAccounts();
  renderOverview();
  prefillForm('edv');
  fillHistoryFilters();
  renderHistory();
  const h = (location.hash || '').slice(1);
  if (['icmal', 'odenis', 'tarixce'].includes(h)) switchTab(h);
  // ?pay=tv|2026-H1 — başqa səhifədən (məs. Aİ Vergi Agenti) hazır ödənişlə açılış
  const pay = new URLSearchParams(location.search).get('pay');
  if (pay) {
    const [taxId, key] = pay.split('|');
    if (TAX[taxId]) { prefillForm(taxId, obligations.some(o => o.taxId === taxId && o.key === key) ? key : undefined); switchTab('odenis'); }
  }
})();
