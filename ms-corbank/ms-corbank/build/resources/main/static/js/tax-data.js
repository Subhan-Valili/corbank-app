// Ortaq vergi datası — vergi.html və ai-vergi.html istifadə edir.
// Vergi növləri, son tarix qaydaları, demo öhdəliklər və ödənişlər (sessionStorage: corbank_tax_v1).
// common.js-dən SONRA yüklənir.
const esc = escHtml;
const MONTHS = ['yanvar', 'fevral', 'mart', 'aprel', 'may', 'iyun', 'iyul', 'avqust', 'sentyabr', 'oktyabr', 'noyabr', 'dekabr'];
const MONTHS_CAP = MONTHS.map(m => m[0].toUpperCase() + m.slice(1));
const ROMAN = ['I', 'II', 'III', 'IV'];
const iso = d => Fmt.isoDate(d);
const parseIso = s => { const [y, m, d] = s.split('-').map(Number); return new Date(y, m - 1, d); };
const TODAY = (() => { const d = demoNow(); return new Date(d.getFullYear(), d.getMonth(), d.getDate()); })();
const TODAY_ISO = iso(TODAY);
const daysBetween = (a, b) => Math.round((parseIso(b) - parseIso(a)) / 86400000);

// ---------------------------------------------------------------
// Vergi növləri. Son tarixlər — Vergi Məcəlləsi / taxes.gov.az vergi təqvimi:
//   ƏDV, ödəmə mənbəyindən gəlir vergisi — növbəti ayın 20-dək
//   DSMF, işsizlikdən sığorta, icbari tibbi sığorta — növbəti ayın 15-dək
//   Mənfəət vergisi (cari) — rüb bitdikdən sonra 15 gün ərzində
//   Əmlak vergisi (cari) — hər rübün 2-ci ayının 15-dək
//   Torpaq vergisi — 15 avqust və 15 noyabr (bərabər hissələrlə)
// Büdcə təsnifat kodları — rəsmi təsnifatdan nümunə (demo).
// ---------------------------------------------------------------
const TAXES = [
  { id: 'edv',  group: 'Vergilər', name: 'Əlavə dəyər vergisi (ƏDV)', short: 'ƏDV', code: '114100', freq: 'month', dueDay: 20, base: 7840 },
  { id: 'gv',   group: 'Vergilər', name: 'Gəlir vergisi (muzdlu işdən, ödəmə mənbəyində)', short: 'Gəlir vergisi', code: '111111', freq: 'month', dueDay: 20, base: 3950 },
  { id: 'mv',   group: 'Vergilər', name: 'Mənfəət vergisi (cari ödəniş)', short: 'Mənfəət vergisi', code: '111200', freq: 'quarter', base: 12600 },
  { id: 'ev',   group: 'Vergilər', name: 'Əmlak vergisi (cari ödəniş)', short: 'Əmlak vergisi', code: '113210', freq: 'quarterMid', base: 1450 },
  { id: 'tv',   group: 'Vergilər', name: 'Torpaq vergisi', short: 'Torpaq vergisi', code: '113100', freq: 'half', base: 980 },
  { id: 'dsmf', group: 'Sosial ödənişlər', name: 'Məcburi dövlət sosial sığorta haqları', short: 'Sosial sığorta (DSMF)', code: '121000', freq: 'month', dueDay: 15, base: 8120 },
  { id: 'iss',  group: 'Sosial ödənişlər', name: 'İşsizlikdən sığorta haqları', short: 'İşsizlikdən sığorta', code: '123000', freq: 'month', dueDay: 15, base: 365 },
  { id: 'its',  group: 'Sosial ödənişlər', name: 'İcbari tibbi sığorta haqları', short: 'Tibbi sığorta', code: '—', freq: 'month', dueDay: 15, base: 720 },
];
const TAX = Object.fromEntries(TAXES.map(t => [t.id, t]));
const KINDS = {
  esas:     { label: 'Əsas məbləğ', code: null },
  faiz:     { label: 'Faiz (penya)', code: '143400' },
  sanksiya: { label: 'Maliyyə sanksiyası', code: '143000' },
};
const TREASURY = { name: 'Dövlət Xəzinədarlıq Agentliyi', iban: 'AZ41CTRE00000000000000010001', swift: 'CTREAZ24' };
const COMPANY_VOEN = { alfa: '1700123451', beta: '1700456782' };
const companyVoen = () => COMPANY_VOEN[Company.current().id] || '1700000000';

function seeded(str) {
  let h = 2166136261;
  for (const ch of str) { h ^= ch.charCodeAt(0); h = Math.imul(h, 16777619); }
  return ((h >>> 0) % 10000) / 10000;
}
// Son tarix şənbə/bazar gününə düşürsə — növbəti iş gününə keçir
function workday(d) {
  const x = new Date(d);
  while (x.getDay() === 0 || x.getDay() === 6) x.setDate(x.getDate() + 1);
  return x;
}

// Bir vergi növü üçün dövrlər: { key, label, due }
function periodsFor(tax, fromYear, toYear) {
  const out = [];
  for (let y = fromYear; y <= toYear; y++) {
    if (tax.freq === 'month') {
      for (let m = 0; m < 12; m++) out.push({ key: `${y}-${String(m + 1).padStart(2, '0')}`, label: `${MONTHS_CAP[m]} ${y}`, due: iso(workday(new Date(y, m + 1, tax.dueDay))) });
    } else if (tax.freq === 'quarter') {
      for (let q = 0; q < 4; q++) out.push({ key: `${y}-Q${q + 1}`, label: `${ROMAN[q]} rüb ${y}`, due: iso(workday(new Date(y, q * 3 + 3, 15))) });
    } else if (tax.freq === 'quarterMid') {
      for (let q = 0; q < 4; q++) out.push({ key: `${y}-Q${q + 1}`, label: `${ROMAN[q]} rüb ${y}`, due: iso(workday(new Date(y, q * 3 + 1, 15))) });
    } else if (tax.freq === 'half') {
      out.push({ key: `${y}-H1`, label: `${y} · 1-ci hissə`, due: iso(workday(new Date(y, 7, 15))) });
      out.push({ key: `${y}-H2`, label: `${y} · 2-ci hissə`, due: iso(workday(new Date(y, 10, 15))) });
    }
  }
  return out;
}
const periodLabel = (taxId, key) => {
  const p = periodsFor(TAX[taxId], Number(key.slice(0, 4)), Number(key.slice(0, 4))).find(x => x.key === key);
  return p ? p.label : key;
};
const amountFor = (tax, key) => Math.round(tax.base * (0.88 + seeded(tax.id + key) * 0.24) * 100) / 100;

// ---------------------------------------------------------------
// Vəziyyət: demo tarixçə (keçmiş ödənişlər) + bu sessiyada edilən ödənişlər
// ---------------------------------------------------------------
const STORE_KEY = 'corbank_tax_v1';
let extraPayments = [];
try { extraPayments = JSON.parse(sessionStorage.getItem(STORE_KEY)) || []; } catch (e) { extraPayments = []; }
const saveExtra = () => { try { sessionStorage.setItem(STORE_KEY, JSON.stringify(extraPayments)); } catch (e) { /* yaddaş bağlıdır */ } };

const DEMO_ACCOUNTS = [{ id: 'azn-001', bank: 'PASHA' }, { id: 'abb-azn-01', bank: 'ABB' }];
const YEAR = TODAY.getFullYear();

function refFor(seed, date) {
  return `BP-${date.replace(/-/g, '').slice(2)}-${String(Math.floor(seeded('ref' + seed) * 900000) + 100000)}`;
}

// Hər öhdəlik: { taxId, key, label, due, amount, status: paid|pending|overdue, payment? }
let obligations = [];
let seededPayments = [];

function buildData() {
  seededPayments = [];
  obligations = [];
  TAXES.forEach(tax => {
    periodsFor(tax, YEAR - 1, YEAR + 1).forEach(p => {
      const amount = amountFor(tax, p.key);
      const dueDiff = daysBetween(TODAY_ISO, p.due);       // müsbət — qarşıdadır
      if (dueDiff < -400 || dueDiff > 75) return;
      let paidSeed = dueDiff < 0;
      // Demo: torpaq vergisinin bu ilin 1-ci hissəsi ödənilməyib (gecikmiş nümunə)
      if (tax.id === 'tv' && p.key === `${YEAR}-H1` && dueDiff < 0) paidSeed = false;
      // Demo: DSMF yaxınlaşan dövr üçün vaxtından əvvəl ödənilib
      if (tax.id === 'dsmf' && dueDiff >= 0 && dueDiff <= 12) paidSeed = true;
      if (paidSeed) {
        const pd = parseIso(p.due); pd.setDate(pd.getDate() - (1 + Math.floor(seeded('d' + tax.id + p.key) * 5)));
        const paidDate = iso(pd) > TODAY_ISO ? TODAY_ISO : iso(pd);
        const acc = DEMO_ACCOUNTS[seeded('a' + tax.id + p.key) < 0.72 ? 0 : 1];
        seededPayments.push({ id: `seed-${tax.id}-${p.key}`, taxId: tax.id, kind: 'esas', periodKey: p.key, amount,
          accountId: acc.id, bank: acc.bank, date: paidDate, ref: refFor(tax.id + p.key, paidDate), status: 'completed' });
      }
      obligations.push({ taxId: tax.id, key: p.key, label: p.label, due: p.due, amount });
    });
  });
  refreshStatuses();
}

const allPayments = () => [...extraPayments, ...seededPayments].sort((a, b) => b.date.localeCompare(a.date) || b.id.localeCompare(a.id));

function refreshStatuses() {
  const pays = allPayments();
  obligations.forEach(o => {
    const paid = pays.filter(p => p.taxId === o.taxId && p.periodKey === o.key && p.kind === 'esas' && p.status === 'completed')
      .reduce((s, p) => s + p.amount, 0);
    o.paid = Math.round(paid * 100) / 100;
    o.remaining = Math.max(0, Math.round((o.amount - paid) * 100) / 100);
    o.status = o.remaining <= 0.009 ? 'paid' : (o.due < TODAY_ISO ? 'overdue' : 'pending');
  });
}
