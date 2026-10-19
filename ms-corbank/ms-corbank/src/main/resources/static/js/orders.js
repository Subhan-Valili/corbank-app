// Sifariş (demo müraciət) modalı — index.html və hesablar.html üçün ortaq.
// Uses helpers from common.js (openModal, closeModal, Prefs, Company, Fmt, showToast, escHtml).

const ORDER_TYPES = {
  account: {
    title: 'Cari hesab sifarişi',
    fields: [
      { id: 'currency', label: 'Valyuta', type: 'select', options: ['AZN', 'USD', 'EUR'] },
    ],
  },
  credit: {
    title: 'Kredit sifarişi',
    fields: [
      { id: 'amount', label: 'Məbləğ (AZN)', type: 'number', min: '1', step: '0.01' },
      { id: 'term', label: 'Müddət (ay)', type: 'number', min: '1', step: '1' },
      { id: 'purpose', label: 'Kreditin məqsədi', type: 'text' },
    ],
  },
  deposit: {
    title: 'Depozit sifarişi',
    fields: [
      { id: 'amount', label: 'Məbləğ (AZN)', type: 'number', min: '1', step: '0.01' },
      { id: 'term', label: 'Müddət (ay)', type: 'number', min: '1', step: '1' },
    ],
  },
  card: {
    title: 'Biznes kart sifarişi',
    fields: [
      { id: 'cardType', label: 'Kart növü', type: 'select', options: ['Biznes debet kartı', 'Biznes kredit kartı'] },
      { id: 'count', label: 'Kartların sayı', type: 'number', min: '1', step: '1' },
    ],
  },
  payroll: {
    title: 'Əmək haqqı sifarişi',
    fields: [
      { id: 'employees', label: 'Əməkdaş sayı', type: 'number', min: '1', step: '1' },
      { id: 'fund', label: 'Aylıq əmək haqqı fondu (AZN)', type: 'number', min: '1', step: '0.01' },
    ],
  },
};

function orderFieldHtml(f, value) {
  const id = 'of_' + f.id;
  const label = `<label for="${id}">${f.label}</label>`;
  if (f.type === 'select') {
    return `<div class="modal-field">${label}
      <select id="${id}" name="${f.id}" required>
        ${f.options.map(o => `<option value="${escHtml(o)}">${escHtml(o)}</option>`).join('')}
      </select></div>`;
  }
  const attrs = [
    f.min ? `min="${f.min}"` : '',
    f.step ? `step="${f.step}"` : '',
    f.pattern ? `pattern="${f.pattern}" title="${escHtml(f.hint || '')}"` : '',
    f.placeholder ? `placeholder="${escHtml(f.placeholder)}"` : '',
  ].join(' ');
  return `<div class="modal-field">${label}
    <input id="${id}" name="${f.id}" type="${f.type}" ${attrs} value="${escHtml(value || '')}" autocomplete="off" required></div>`;
}

function openOrderModal(type) {
  const cfg = ORDER_TYPES[type];
  if (!cfg) return;
  const prefs = Prefs.get();
  const companyName = Company.current().name;
  const subtitle = `${escHtml(companyName)} · Demo müraciət`;

  const common = [
    { id: 'company', label: 'Şirkət', type: 'text', value: companyName },
    { id: 'contact', label: 'Əlaqədar şəxs', type: 'text', value: prefs.name },
    { id: 'phone', label: 'Telefon', type: 'tel', placeholder: '+994501234567',
      pattern: '\\+?[0-9\\s\\(\\)\\-]{7,20}', hint: 'Telefon nömrəsini +994501234567 formatında daxil edin' },
    { id: 'email', label: 'E-poçt', type: 'email', value: prefs.email },
  ];

  openModal({
    title: cfg.title,
    subtitle,
    bodyHtml: `
      <form id="orderForm" class="form-grid">
        ${common.map(f => orderFieldHtml(f, f.value)).join('')}
        ${cfg.fields.map(f => orderFieldHtml(f, '')).join('')}
      </form>
      <div class="info-note">Bu forma yalnız demo müraciət yaradır.</div>`,
    footerHtml: `
      <button class="btn-ghost" type="button" onclick="closeModal()">Ləğv et</button>
      <button class="btn-primary" type="submit" form="orderForm">Demo müraciəti göndər</button>`
  });

  const form = document.getElementById('orderForm');
  const first = document.getElementById('of_company');
  if (first) { first.focus(); first.select(); }

  // Boş məcburi sahədə brauzerin öz "Please fill out this field" xəbərdarlığı çıxır (required).
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const ref = `MR-${demoNow().getFullYear()}-${String(Math.floor(1000 + Math.random() * 9000))}`;
    openModal({
      title: cfg.title,
      subtitle,
      bodyHtml: `
        <div class="result-box">
          <div class="result-icon success">✓</div>
          <div class="result-title">Demo müraciət qəbul edildi</div>
          <div class="result-note">Müraciət nömrəsi: <b>${ref}</b></div>
          <div class="result-disclaimer">Real müraciət göndərilməyib. Bu yalnız demo göstəricisidir.</div>
        </div>`,
      footerHtml: `<button class="btn-primary btn-block" type="button" onclick="closeModal()">Bağla</button>`
    });
    showToast('Demo müraciət göndərildi');
  });
}
window.openOrderModal = openOrderModal;
