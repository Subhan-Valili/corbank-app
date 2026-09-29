// Mühasibatlıq page — uses helpers from common.js (Api, Fmt, Icons, showToast, renderSidebar)

document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('muhasibatliq.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());

// ---------------------------------------------------------------
// Page-specific icons (kept here so common.js stays untouched)
// ---------------------------------------------------------------
const mhSvg = (paths, size = 24, sw = 1.8) =>
  `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="${sw}" stroke-linecap="round" stroke-linejoin="round">${paths}</svg>`;

const MhIcons = {
  receipt: mhSvg('<path d="M5 3h14v18l-2.5-1.5L14 21l-2-1.5L10 21l-2.5-1.5L5 21z"/><path d="M9 8h6M9 12h6M9 16h4"/>'),
  calculator: mhSvg('<rect x="5" y="3" width="14" height="18" rx="2"/><path d="M8 7h8"/><path d="M8.5 11.5h.01M12 11.5h.01M15.5 11.5h.01M8.5 15h.01M12 15h.01M15.5 15h.01M8.5 18h.01M12 18h.01M15.5 18h.01" stroke-width="2.4"/>'),
  transfer: mhSvg('<path d="M4 8h15"/><path d="M15 4l4 4-4 4"/><path d="M20 16H5"/><path d="M9 12l-4 4 4 4"/>'),
  tax: mhSvg('<path d="M3 10l9-6 9 6"/><path d="M3 10h18"/><path d="M5 10v8M9.5 10v8M14.5 10v8M19 10v8"/><path d="M3 21h18"/>'),
  robot: mhSvg('<rect x="4" y="8" width="16" height="12" rx="3"/><path d="M12 8V5"/><circle cx="12" cy="4" r="1"/><circle cx="9" cy="13.5" r="1" fill="currentColor"/><circle cx="15" cy="13.5" r="1" fill="currentColor"/><path d="M9.5 17h5"/>'),
  sparkles: mhSvg('<path d="M11 4l1.6 4.4L17 10l-4.4 1.6L11 16l-1.6-4.4L5 10l4.4-1.6z"/><path d="M18 3v4M16 5h4"/><path d="M6 17v4M4 19h4"/>'),
};

document.getElementById('iconCsv').innerHTML = Icons.download;

// ---------------------------------------------------------------
// Tool cards
// ---------------------------------------------------------------
const TOOLS = [
  { icon: MhIcons.receipt,    label: 'Elektron Qaimə Faktura' },
  { icon: MhIcons.calculator, label: 'Mühasibatlıq proqramı' },
  { icon: MhIcons.transfer,   label: 'Debitor/ kreditor borcları' },
  { icon: MhIcons.tax,        label: 'Vergi və büdcə ödənişləri' },
  { icon: MhIcons.robot,      label: 'Aİ Vergi Agenti' },
  { icon: MhIcons.sparkles,   label: 'Aİ Mühasib' },
];

document.getElementById('toolsGrid').innerHTML = TOOLS.map((t, i) => `
  <button class="card mh-tool" type="button" data-idx="${i}">
    <span class="mh-tool-icon">${t.icon}</span>
    <span>${t.label}</span>
  </button>`).join('');

document.querySelectorAll('.mh-tool').forEach(btn => {
  btn.addEventListener('click', () => {
    showToast(`“${TOOLS[Number(btn.dataset.idx)].label}” bölməsi tezliklə əlavə olunacaq`, 'info');
  });
});

// ---------------------------------------------------------------
// Operations (all accounts, client-side filter + pagination)
// ---------------------------------------------------------------
const PAGE_SIZE = 10;

let accounts = [];
let allOps = [];
let filteredOps = [];
let currentPage = 1;

function esc(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// lower-case that also treats "İ" as "i" (default toLowerCase gives "i" + combining dot)
function norm(s) {
  return String(s == null ? '' : s).toLowerCase().replace(/\u0307/g, '');
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
    <td class="mh-date">${Fmt.dateAz(op.date)}</td>
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
    <td><span class="status-chip ${statusClass(op.status)}"><span class="dot"></span>${statusLabel(op.status)}</span></td>
  </tr>`;
}

function applyFilters() {
  const q = norm(document.getElementById('searchInput').value.trim());
  const accId = document.getElementById('accountSelect').value;

  filteredOps = allOps.filter(op => {
    if (accId !== 'all' && op.accountId !== accId) return false;
    if (!q) return true;
    return norm(op.counterparty).includes(q) || norm(op.description).includes(q);
  });
  currentPage = 1;
  render();
}

function render() {
  const total = filteredOps.length;
  const pages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  currentPage = Math.min(Math.max(1, currentPage), pages);

  const slice = filteredOps.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);
  document.getElementById('opsBody').innerHTML = slice.map(opRowHtml).join('');
  document.getElementById('opsEmpty').style.display = total === 0 ? 'block' : 'none';
  document.querySelector('.mh-table-wrap').style.display = total === 0 ? 'none' : '';

  document.getElementById('opsCount').textContent = `${total} əməliyyat`;
  document.getElementById('pageInfo').textContent = `${currentPage} / ${pages}`;
  document.getElementById('prevBtn').disabled = currentPage <= 1;
  document.getElementById('nextBtn').disabled = currentPage >= pages;
}

async function load() {
  try {
    const accRes = await Api.get('/accounts');
    accounts = accRes.accounts || [];

    document.getElementById('accountSelect').innerHTML =
      '<option value="all">Bütün hesablar</option>' +
      accounts.map(a => `<option value="${esc(a.id)}">${esc(accountLabel(a))}</option>`).join('');

    // Wide window so every operation (mock data is dated 2025) is included.
    const qs = new URLSearchParams({
      startDate: '2000-01-01',
      endDate: new Date().toISOString().slice(0, 10),
      sort: 'newest',
    });
    const results = await Promise.all(
      accounts.map(a => Api.get(`/accounts/${encodeURIComponent(a.id)}/operations?${qs.toString()}`))
    );

    allOps = results.flatMap((r, i) =>
      (r.operations || []).map(op => ({ ...op, accountId: accounts[i].id }))
    );
    // newest first; Array.sort is stable so same-day rows keep account order (AZN, USD, EUR)
    allOps.sort((a, b) => String(b.date).localeCompare(String(a.date)));

    applyFilters();
  } catch (e) {
    console.error(e);
    const err = document.getElementById('opsError');
    err.textContent = 'Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı? (mvn spring-boot:run)';
    err.style.display = 'block';
    document.querySelector('.mh-table-wrap').style.display = 'none';
  }
}

// ---------------------------------------------------------------
// Events
// ---------------------------------------------------------------
document.getElementById('accountSelect').addEventListener('change', applyFilters);
document.getElementById('searchInput').addEventListener('input', () => {
  clearTimeout(window.__mhSearchTimer);
  window.__mhSearchTimer = setTimeout(applyFilters, 200);
});
document.getElementById('prevBtn').addEventListener('click', () => { currentPage--; render(); });
document.getElementById('nextBtn').addEventListener('click', () => { currentPage++; render(); });

// CSV export — exports everything matching the current filters (all pages)
document.getElementById('csvBtn').addEventListener('click', () => {
  if (!filteredOps.length) { showToast('İxrac üçün əməliyyat yoxdur', 'error'); return; }
  const accName = id => { const a = accounts.find(x => x.id === id); return a ? accountLabel(a) : ''; };
  const header = ['Tarix', 'Hesab', 'Qarşı tərəf', 'Təyinat', 'Məbləğ', 'Valyuta', 'İstiqamət', 'Status'];
  const rows = filteredOps.map(op => [
    Fmt.dateAz(op.date),
    accName(op.accountId),
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
  a.download = 'muhasibatliq-emeliyyatlar.csv';
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
  showToast('CSV fayl endirildi');
});

load();
