// Analitika page — uses helpers from common.js (Api, Fmt, Icons, showToast, renderSidebar)
// No dedicated backend analytics endpoint exists yet, so everything here is computed
// client-side from /accounts + /accounts/{id}/operations, the same data muhasibatliq.html
// and account.html already use.

document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('analitika.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());

function esc(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
function norm(s) {
  return String(s == null ? '' : s).toLowerCase().replace(/\u0307/g, '');
}

// ---------------------------------------------------------------
// FX helpers (same convention as account.js)
// ---------------------------------------------------------------
function fxRateFor(cur, rates) {
  if (cur === 'AZN') return { buy: 1, sell: 1 };
  return rates.find(x => x.currency === cur) || { buy: 1, sell: 1 };
}
function toAzn(amount, cur, rates) {
  if (cur === 'AZN') return Number(amount) || 0;
  return (Number(amount) || 0) * fxRateFor(cur, rates).sell;
}

// ---------------------------------------------------------------
// Internal-transfer detection — excluded from income/expense/cash-flow
// ---------------------------------------------------------------
function isInternalTransfer(op, accountNames) {
  const text = norm(`${op.counterparty || ''} ${op.description || ''}`);
  if (/(öz hesab|daxili köçürmə|internal transfer)/.test(text)) return true;
  return accountNames.some(n => n && text.includes(norm(n)));
}

// ---------------------------------------------------------------
// Category classification (keyword-based, since operations have no explicit category field)
// ---------------------------------------------------------------
function classifyIncome(op) {
  const text = norm(`${op.counterparty || ''} ${op.description || ''}`);
  if (/(xidmət|servis|service|consulting|konsalting|dizayn|audit|delta)/.test(text)) return 'Xidmət gəlirləri';
  return 'Satış gəlirləri';
}
function classifyExpense(op) {
  const text = norm(`${op.counterparty || ''} ${op.description || ''}`);
  if (/(əmək haqqı|maaş|salary|payroll|hr\b|kadr)/.test(text)) return 'Əmək haqqı';
  if (/(icar|rent|business center|ofis)/.test(text)) return 'İcarə';
  if (/(vergi|tax|dövlət)/.test(text)) return 'Vergilər';
  if (/(kommunal|utility|işıq|elektrik|\bqaz\b|su tə|azərişıq|azəríqaz|energy)/.test(text)) return 'Kommunal';
  return 'Digər xərclər';
}

const INCOME_COLORS = ['#1a9c5b', '#5fbf8c', '#98d6b7', '#c7ead7'];
const EXPENSE_COLORS = ['#d84343', '#e8797c', '#f0a7ab', '#f6cdd0'];

// ---------------------------------------------------------------
// Formatting helpers
// ---------------------------------------------------------------
function fmtAzn(v) { return Fmt.money(v, 'AZN'); }
function fmtPct(v) { return v.toFixed(1).replace('.', ',') + '%'; }
function dateShort(iso) { return Fmt.dateAz(iso).slice(0, 5); } // "dd.mm"

function niceTicks(max, count = 4) {
  if (max <= 0) return [0];
  const rough = max / count;
  const mag = Math.pow(10, Math.floor(Math.log10(rough)));
  const norm2 = rough / mag;
  let step;
  if (norm2 < 1.5) step = 1 * mag;
  else if (norm2 < 3) step = 2 * mag;
  else if (norm2 < 7) step = 5 * mag;
  else step = 10 * mag;
  const top = Math.ceil(max / step) * step;
  const ticks = [];
  for (let v = 0; v <= top + step / 2; v += step) ticks.push(Math.round(v));
  return ticks;
}
function fmtAxisVal(v) {
  if (v === 0) return '0';
  if (v >= 1000) return (v / 1000).toFixed(v % 1000 === 0 ? 0 : 1).replace('.', ',') + 'k';
  return String(v);
}

// ---------------------------------------------------------------
// Dynamics chart (cumulative income vs expense, smooth SVG area chart)
// ---------------------------------------------------------------
function catmullRomPath(points) {
  if (points.length < 2) return '';
  if (points.length === 2) return `M${points[0][0]},${points[0][1]} L${points[1][0]},${points[1][1]}`;
  let d = `M${points[0][0]},${points[0][1]}`;
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[i - 1] || points[i];
    const p1 = points[i];
    const p2 = points[i + 1];
    const p3 = points[i + 2] || p2;
    const c1x = p1[0] + (p2[0] - p0[0]) / 6;
    const c1y = p1[1] + (p2[1] - p0[1]) / 6;
    const c2x = p2[0] - (p3[0] - p1[0]) / 6;
    const c2y = p2[1] - (p3[1] - p1[1]) / 6;
    d += ` C${c1x},${c1y} ${c2x},${c2y} ${p2[0]},${p2[1]}`;
  }
  return d;
}

function renderDynamicsChart(containerId, startIso, endIso, incomeOps, expenseOps) {
  const W = 1000, H = 320, padL = 44, padR = 12, padT = 14, padB = 30;
  const startMs = new Date(startIso).getTime();
  const endMs = new Date(endIso).getTime();
  const span = Math.max(1, endMs - startMs);

  const eventTimes = new Set([startMs, endMs]);
  incomeOps.forEach(o => eventTimes.add(new Date(o.date).getTime()));
  expenseOps.forEach(o => eventTimes.add(new Date(o.date).getTime()));
  const times = Array.from(eventTimes).filter(t => t >= startMs && t <= endMs).sort((a, b) => a - b);

  function cumulativeAt(ops, t) {
    return ops.reduce((sum, o) => (new Date(o.date).getTime() <= t ? sum + o.aznAmount : sum), 0);
  }

  const incomeSeries = times.map(t => [t, cumulativeAt(incomeOps, t)]);
  const expenseSeries = times.map(t => [t, cumulativeAt(expenseOps, t)]);
  const maxVal = Math.max(1, ...incomeSeries.map(p => p[1]), ...expenseSeries.map(p => p[1]));
  const ticks = niceTicks(maxVal, 4);
  const yMax = ticks[ticks.length - 1] || maxVal;

  const x = t => padL + ((t - startMs) / span) * (W - padL - padR);
  const y = v => (H - padB) - (v / yMax) * (H - padT - padB);

  const incomePts = incomeSeries.map(([t, v]) => [x(t), y(v)]);
  const expensePts = expenseSeries.map(([t, v]) => [x(t), y(v)]);

  const incomePath = catmullRomPath(incomePts);
  const expensePath = catmullRomPath(expensePts);
  const incomeArea = incomePts.length
    ? `${incomePath} L${incomePts[incomePts.length - 1][0]},${y(0)} L${incomePts[0][0]},${y(0)} Z` : '';
  const expenseArea = expensePts.length
    ? `${expensePath} L${expensePts[expensePts.length - 1][0]},${y(0)} L${expensePts[0][0]},${y(0)} Z` : '';

  // x-axis labels: up to 7 evenly spaced points across the time span
  const tickCount = 7;
  const xLabels = [];
  for (let i = 0; i < tickCount; i++) {
    const t = startMs + (span * i) / (tickCount - 1);
    xLabels.push({ t, x: x(t), label: dateShort(new Date(t).toISOString().slice(0, 10)) });
  }

  const gridHtml = ticks.map(v => `
    <line class="an-grid-line" x1="${padL}" x2="${W - padR}" y1="${y(v)}" y2="${y(v)}"></line>
    <text x="${padL - 10}" y="${y(v) + 4}" text-anchor="end">${fmtAxisVal(v)}</text>`).join('');

  const xAxisHtml = xLabels.map(l => `<text x="${l.x}" y="${H - 8}" text-anchor="middle">${l.label}</text>`).join('');

  document.getElementById(containerId).innerHTML = `
  <svg viewBox="0 0 ${W} ${H}" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="anIncomeGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="var(--green)" stop-opacity="0.22"/>
        <stop offset="100%" stop-color="var(--green)" stop-opacity="0"/>
      </linearGradient>
      <linearGradient id="anExpenseGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="var(--red)" stop-opacity="0.16"/>
        <stop offset="100%" stop-color="var(--red)" stop-opacity="0"/>
      </linearGradient>
    </defs>
    <g class="an-axis-line">${gridHtml}${xAxisHtml}</g>
    <path d="${incomeArea}" fill="url(#anIncomeGrad)" stroke="none"></path>
    <path d="${expenseArea}" fill="url(#anExpenseGrad)" stroke="none"></path>
    <path d="${expensePath}" fill="none" stroke="var(--red)" stroke-width="2.5"></path>
    <path d="${incomePath}" fill="none" stroke="var(--green)" stroke-width="2.5"></path>
  </svg>`;
}

// ---------------------------------------------------------------
// Donut chart
// ---------------------------------------------------------------
function renderDonut(containerId, entries, colors, centerLabel) {
  const el = document.getElementById(containerId);
  const total = entries.reduce((s, e) => s + e.value, 0);
  if (total <= 0 || entries.length === 0) {
    el.innerHTML = '<div class="an-empty">Bu dövr üçün məlumat yoxdur</div>';
    return;
  }
  const r = 54, cx = 70, cy = 70, sw = 16;
  const circ = 2 * Math.PI * r;
  let acc = 0;
  const segments = entries.map((e, i) => {
    const frac = e.value / total;
    const dash = frac * circ;
    const offset = -acc * circ;
    acc += frac;
    return `<circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="${colors[i % colors.length]}"
      stroke-width="${sw}" stroke-dasharray="${dash} ${circ - dash}" stroke-dashoffset="${offset}"></circle>`;
  }).join('');

  const legend = entries.map((e, i) => `
    <div class="an-donut-legend-row">
      <span class="dot" style="background:${colors[i % colors.length]}"></span>
      <span class="name">${esc(e.name)}</span>
      <span class="val">${fmtAzn(e.value)}</span>
      <span class="pct">${fmtPct(e.value / total * 100)}</span>
    </div>`).join('');

  const [labelLine1, labelLine2] = String(centerLabel).split('\n');
  el.innerHTML = `
    <svg width="140" height="140" viewBox="0 0 140 140">
      <g transform="rotate(-90 70 70)">${segments}</g>
      <text x="70" y="65" text-anchor="middle" class="an-donut-center-lbl">${esc(labelLine1 || '')}</text>
      <text x="70" y="80" text-anchor="middle" class="an-donut-center-val">${fmtAzn(total)}</text>
      <text x="70" y="94" text-anchor="middle" class="an-donut-center-lbl">${esc(labelLine2 || '')}</text>
    </svg>
    <div class="an-donut-legend">${legend}</div>`;
}

// ---------------------------------------------------------------
// Top contragents lists
// ---------------------------------------------------------------
function renderContragents(containerId, ops, kind) {
  const el = document.getElementById(containerId);
  const byParty = {};
  ops.forEach(op => {
    const key = op.counterparty || 'Digər';
    byParty[key] = (byParty[key] || 0) + op.aznAmount;
  });
  const entries = Object.entries(byParty)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 5);

  if (entries.length === 0) {
    el.innerHTML = '<div class="an-empty">Bu dövr üçün əməliyyat yoxdur</div>';
    return;
  }
  const max = entries[0].value || 1;
  el.innerHTML = entries.map((e, i) => `
    <div class="an-cp-row">
      <div class="an-cp-top">
        <span class="an-cp-name">${i + 1}. ${esc(e.name)}</span>
        <span class="an-cp-val">${fmtAzn(e.value)}</span>
      </div>
      <div class="an-cp-track"><div class="an-cp-fill ${kind === 'out' ? 'out' : ''}" style="width:${(e.value / max * 100).toFixed(1)}%"></div></div>
    </div>`).join('');
}

// ---------------------------------------------------------------
// Load + compute
// ---------------------------------------------------------------
let rates = [];
let accounts = [];

function setDefaultDates() {
  const end = new Date();
  const start = new Date();
  start.setFullYear(end.getFullYear() - 3);
  const toInput = d => d.toISOString().slice(0, 10);
  document.getElementById('startDate').value = toInput(start);
  document.getElementById('endDate').value = toInput(end);
}

async function loadAndRender() {
  const startDate = document.getElementById('startDate').value;
  const endDate = document.getElementById('endDate').value;
  const errEl = document.getElementById('anError');
  errEl.style.display = 'none';

  try {
    const qs = new URLSearchParams({ startDate, endDate, sort: 'newest' });
    const results = await Promise.all(
      accounts.map(a => Api.get(`/accounts/${encodeURIComponent(a.id)}/operations?${qs.toString()}`))
    );
    const allOps = results.flatMap((r, i) =>
      (r.operations || []).map(op => ({ ...op, accountId: accounts[i].id, currency: accounts[i].currency }))
    );

    const accountNames = accounts.map(a => a.name);
    const effectiveOps = allOps
      .filter(op => !isInternalTransfer(op, accountNames))
      .map(op => ({ ...op, aznAmount: toAzn(op.amount, op.currency, rates) }));

    const incomeOps = effectiveOps.filter(op => op.direction === 'in');
    const expenseOps = effectiveOps.filter(op => op.direction === 'out');

    const totalIncome = incomeOps.reduce((s, o) => s + o.aznAmount, 0);
    const totalExpense = expenseOps.reduce((s, o) => s + o.aznAmount, 0);
    const net = totalIncome - totalExpense;

    // Total current balance across all accounts, AZN equivalent
    const totalBalance = accounts.reduce((s, a) => s + toAzn(a.balance, a.currency, rates), 0);

    document.getElementById('sumBalance').textContent = fmtAzn(totalBalance);
    document.getElementById('sumIncome').textContent = fmtAzn(totalIncome);
    document.getElementById('sumExpense').textContent = fmtAzn(totalExpense);

    document.getElementById('chartLegend').innerHTML = `
      <div class="an-legend-item"><span class="dot" style="background:var(--green)"></span><span class="name">Mədaxil</span><span class="val">${fmtAzn(totalIncome)}</span></div>
      <div class="an-legend-item"><span class="dot" style="background:var(--red)"></span><span class="name">Məxaric</span><span class="val">${fmtAzn(totalExpense)}</span></div>`;

    renderDynamicsChart('chartWrap', startDate, endDate, incomeOps, expenseOps);

    const netEl = document.getElementById('netFlow');
    netEl.textContent = (net >= 0 ? '' : '−') + fmtAzn(Math.abs(net));
    netEl.className = net >= 0 ? 'pos' : 'neg';

    const incomeByCat = {};
    incomeOps.forEach(op => {
      const cat = classifyIncome(op);
      incomeByCat[cat] = (incomeByCat[cat] || 0) + op.aznAmount;
    });
    const incomeEntries = Object.entries(incomeByCat).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value);
    renderDonut('incomeDonut', incomeEntries, INCOME_COLORS, 'Mədaxil\nAZN');

    const expenseByCat = {};
    expenseOps.forEach(op => {
      const cat = classifyExpense(op);
      expenseByCat[cat] = (expenseByCat[cat] || 0) + op.aznAmount;
    });
    const expenseEntries = Object.entries(expenseByCat).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value);
    renderDonut('expenseDonut', expenseEntries, EXPENSE_COLORS, 'Məxaric\nAZN');

    renderContragents('cpIn', incomeOps, 'in');
    renderContragents('cpOut', expenseOps, 'out');
  } catch (e) {
    console.error(e);
    errEl.textContent = 'Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı? (mvn spring-boot:run)';
    errEl.style.display = 'block';
  }
}

async function init() {
  setDefaultDates();
  try {
    const [dash, accRes] = await Promise.all([Api.get('/dashboard'), Api.get('/accounts')]);
    rates = dash.exchangeRates || [];
    accounts = accRes.accounts || [];

    const usd = fxRateFor('USD', rates).sell;
    const eur = fxRateFor('EUR', rates).sell;
    document.getElementById('fxNote').innerHTML =
      `Demo çevirmə: 1 USD = ${usd.toFixed(4).replace('.', ',')} AZN · 1 EUR = ${eur.toFixed(4).replace('.', ',')} AZN.<br>` +
      `Öz hesabları arasındakı köçürmələr pul axınından çıxarılıb.`;

    await loadAndRender();
  } catch (e) {
    console.error(e);
    const errEl = document.getElementById('anError');
    errEl.textContent = 'Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı? (mvn spring-boot:run)';
    errEl.style.display = 'block';
  }
}

document.getElementById('startDate').addEventListener('change', loadAndRender);
document.getElementById('endDate').addEventListener('change', loadAndRender);

init();
