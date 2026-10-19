document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar(''));

const params = new URLSearchParams(location.search);
const projectId = params.get('id');

const ICON_MAP = { credit: Icons.swap, deposit: Icons.bank, payroll: Icons.bookmark };
const AMOUNT_LABEL_MAP = { credit: 'Kredit məbləği', deposit: 'Depozit məbləği', payroll: 'Əmək haqqı layihəsi' };

function detailRowHtml(row) {
  return `
  <tr>
    <td style="color:var(--muted); width:40%;">${row.label}</td>
    <td style="font-weight:600;">${row.value}</td>
  </tr>`;
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
    <td class="amount ${isIn ? 'in' : 'out'}">${isIn ? '+' : '−'}${Fmt.money(op.amount, op.currency)}</td>
    <td><span class="status-chip"><span class="dot"></span>Yerinə yetirildi</span></td>
  </tr>`;
}

async function load() {
  if (!projectId) {
    document.querySelector('.main').innerHTML = '<p>Layihə ID-si göstərilməyib.</p>';
    return;
  }
  try {
    const p = await Api.get(`/projects/${projectId}`);

    document.title = `COR Bank Business — ${p.name}`;
    document.getElementById('projIcon').innerHTML = ICON_MAP[p.type] || '💳';
    document.getElementById('projName').textContent = p.name;
    document.getElementById('projSubtitle').textContent =
      p.type === 'payroll' ? (p.meta || '') : 'Aktiv';

    document.getElementById('mainAmountLabel').textContent = AMOUNT_LABEL_MAP[p.type] || 'Məbləğ';
    document.getElementById('mainAmount').textContent = p.amount != null
      ? Fmt.money(p.amount, p.currency)
      : (p.meta || '—');

    document.getElementById('detailsBody').innerHTML = (p.details || []).map(detailRowHtml).join('');

    const opsBody = document.getElementById('opsBody');
    const empty = document.getElementById('emptyState');
    const ops = p.relatedOperations || [];
    if (ops.length === 0) {
      opsBody.innerHTML = '';
      empty.style.display = 'block';
    } else {
      empty.style.display = 'none';
      opsBody.innerHTML = ops.map(opRowHtml).join('');
    }
  } catch (e) {
    console.error(e);
    document.querySelector('.main').insertAdjacentHTML('beforeend',
      '<p style="color:#d84343">Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı? (mvn spring-boot:run)</p>');
  }
}

load();
