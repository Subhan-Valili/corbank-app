// Demo backend: brauzerin daxilində ms-corbank /api cavablarını təqlid edir.
// Real backend olmadan bütün səhifələr və ödəniş funksiyaları işləyir.
// Edilən ödənişlər sessiya boyu (brauzer tabı bağlanana qədər) yadda qalır.
(function () {
  // config.js -> CORBANK_USE_MOCK = true olanda real backend əvəzinə brauzerdaxili demo data işləyir.
  if (window.CORBANK_USE_MOCK !== true) return;
  const STORE_KEY = 'corbank_demo_state_v3';
  const pad = n => String(n).padStart(2, '0');
  const iso = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

  function seededRandom(seed) {
    let h = 2166136261;
    for (const ch of seed) { h ^= ch.charCodeAt(0); h = Math.imul(h, 16777619); }
    return () => { h += 0x6D2B79F5; let t = h; t = Math.imul(t ^ (t >>> 15), t | 1); t ^= t + Math.imul(t ^ (t >>> 7), t | 61); return ((t ^ (t >>> 14)) >>> 0) / 4294967296; };
  }

  // Hər valyuta hesabının öz qarşı tərəfləri var: yerli kommunal/vergi/icarə ödənişləri yalnız AZN-dən,
  // xarici ticarət ödənişləri isə USD/EUR hesablarından keçir.
  const PARTIES = {
    AZN: [
      ['Azərenerji ASC', 'Elektrik enerjisi', 'out'], ['Kontinent MMC', 'Müqavilə üzrə ödəniş', 'in'],
      ['Vergilər Nazirliyi', 'ƏDV ödənişi', 'out'], ['Delta Trade MMC', 'Mal göndərişi', 'in'],
      ['Əməkdaşlar', 'Əmək haqqı', 'out'], ['Bakcell MMC', 'Rabitə xidməti', 'out'],
      ['Ofis icarəsi', 'Aylıq icarə', 'out'], ['Azərsu ASC', 'Su təchizatı', 'out'],
      ['Nord Invest MMC', 'Xidmət haqqı', 'in'], ['Azəriqaz İB', 'Qaz təchizatı', 'out'],
    ],
    USD: [
      ['Global Logistics LLC', 'İnvoys #2291', 'in'], ['Pacific Supply Inc.', 'Avadanlıq idxalı', 'out'],
      ['Atlas Trading FZE', 'İxrac üzrə ödəniş', 'in'], ['Maersk Line', 'Konteyner daşınması', 'out'],
      ['Amazon Web Services', 'Bulud xidmətləri', 'out'], ['Caspian Oil Services', 'Xidmət müqaviləsi', 'in'],
    ],
    EUR: [
      ['Siemens AG', 'Avadanlıq tədarükü', 'out'], ['EuroTech GmbH', 'Proqram lisenziyası', 'out'],
      ['Milano Design S.r.l.', 'Dizayn xidməti', 'out'], ['Baltic Export OÜ', 'Mal ixracı', 'in'],
      ['DHL Express', 'Kuryer xidməti', 'out'], ['Alpen Partners AG', 'Konsaltinq haqqı', 'in'],
    ],
  };

  function seed() {
    const accounts = [
      { id: 'azn-001', name: 'AZN hesabı', balance: 184520.75, currency: 'AZN', iban: 'AZ21PAHA38090AZNHA1234567890', status: 'active', bank: 'PASHA' },
      { id: 'usd-001', name: 'USD hesabı', balance: 32410.00, currency: 'USD', iban: 'AZ35PAHA38090USDHA1234567891', status: 'active', bank: 'PASHA' },
      { id: 'eur-001', name: 'EUR hesabı', balance: 8915.40, currency: 'EUR', iban: 'AZ62PAHA38090EURHA1234567892', status: 'active', bank: 'PASHA' },
      { id: 'abb-azn-01', name: 'AZN hesabı', balance: 56300.00, currency: 'AZN', iban: 'AZ77IBAZ38010019441234567893', status: 'active', bank: 'ABB' },
    ];
    const ops = {};
    const today = new Date();
    accounts.forEach(a => {
      const rnd = seededRandom(a.id);
      const scale = a.currency === 'AZN' ? 1 : 0.35;
      ops[a.id] = Array.from({ length: 30 }, (_, i) => {
        const list = PARTIES[a.currency] || PARTIES.AZN;
        const [cp, desc, dir] = list[Math.floor(rnd() * list.length)];
        const d = new Date(today); d.setDate(d.getDate() - (i * 3 + Math.floor(rnd() * 3)));
        return {
          id: `${a.id}-op${i}`, date: iso(d), counterparty: cp, description: desc,
          amount: Math.round((150 + rnd() * 8800) * scale * 100) / 100, currency: a.currency, direction: dir,
          accountId: a.id, bank: a.bank,
          status: i === 1 ? 'pending' : (i === 7 ? 'rejected' : 'completed'),
        };
      });
    });
    // ABB hesabının son günlərə aid əməliyyatları — "Son əməliyyatlar"da hər iki bank görünsün
    const daysAgo = n => { const d = new Date(today); d.setDate(d.getDate() - n); return iso(d); };
    ops['abb-azn-01'].unshift(
      { id: 'abb-azn-01-r1', date: daysAgo(0), counterparty: 'Kontinent MMC', description: 'Müqavilə üzrə ödəniş',
        amount: 4250.00, currency: 'AZN', direction: 'in', accountId: 'abb-azn-01', bank: 'ABB', status: 'completed' },
      { id: 'abb-azn-01-r2', date: daysAgo(1), counterparty: 'Bakcell MMC', description: 'Rabitə xidməti',
        amount: 186.40, currency: 'AZN', direction: 'out', accountId: 'abb-azn-01', bank: 'ABB', status: 'completed' },
    );
    return { accounts, ops, seq: 0 };
  }

  let state;
  try { state = JSON.parse(sessionStorage.getItem(STORE_KEY)); } catch (e) { /* yaddaş əlçatan deyil */ }
  if (!state || !state.accounts) state = seed();
  const save = () => { try { sessionStorage.setItem(STORE_KEY, JSON.stringify(state)); } catch (e) { /* yaddaş əlçatan deyil */ } };

  const PROJECTS = [
    { id: 'credit-01', type: 'credit', name: 'Biznes krediti', amount: 50000, currency: 'AZN', meta: null,
      details: [{ label: 'Qalıq məbləğ', value: '50 000 AZN' }, { label: 'Faiz dərəcəsi', value: '14%' }, { label: 'Növbəti ödəniş', value: '01.11.2026' }] },
    { id: 'deposit-01', type: 'deposit', name: 'Müddətli depozit', amount: 120000, currency: 'AZN', meta: null,
      details: [{ label: 'Əsas məbləğ', value: '120 000 AZN' }, { label: 'Faiz dərəcəsi', value: '9,5%' }, { label: 'Bitmə tarixi', value: '14.10.2027' }] },
    { id: 'payroll-01', type: 'payroll', name: 'Əmək haqqı layihəsi', amount: null, currency: null, meta: '24 əməkdaş',
      details: [{ label: 'Əməkdaş sayı', value: '24 əməkdaş' }, { label: 'Son köçürmə', value: '01.10.2026' }, { label: 'Növbəti köçürmə', value: '01.11.2026' }] },
  ];
  const RATES = [{ currency: 'USD', buy: 1.6985, sell: 1.7015 }, { currency: 'EUR', buy: 1.9620, sell: 1.9890 }];
  const BANK_NAMES = { PASHA: 'PAŞA Bank', ABB: 'ABB' };

  const r2 = n => Math.round(n * 100) / 100;
  const accountsFor = bank => state.accounts.filter(a => !bank || a.bank === bank);

  function operations(id, q) {
    let list = (state.ops[id] || []).slice();
    const start = q.get('startDate'), end = q.get('endDate'), search = (q.get('search') || '').toLowerCase();
    const dir = q.get('direction'), status = q.get('status');
    if (start) list = list.filter(o => o.date >= start);
    if (end) list = list.filter(o => o.date <= end);
    if (search) list = list.filter(o => (o.counterparty + ' ' + o.description + ' ' + o.id).toLowerCase().includes(search));
    if (dir && dir !== 'all') list = list.filter(o => o.direction === dir);
    if (status && status !== 'all') list = list.filter(o => o.status === status);
    list.sort((a, b) => q.get('sort') === 'oldest' ? a.date.localeCompare(b.date) : b.date.localeCompare(a.date));
    const done = list.filter(o => o.status === 'completed');
    const income = r2(done.filter(o => o.direction === 'in').reduce((s, o) => s + o.amount, 0));
    const expense = r2(done.filter(o => o.direction === 'out').reduce((s, o) => s + o.amount, 0));
    return { operations: list, summary: { income, expense, net: r2(income - expense) } };
  }

  function dashboard(bank) {
    const accs = accountsFor(bank);
    const recent = accs.flatMap(a => state.ops[a.id].map(o => ({ ...o, accountId: a.id, bank: a.bank, currency: a.currency }))).sort((a, b) => b.date.localeCompare(a.date)).slice(0, 6);
    const connected = ['PASHA', 'ABB'].map(code => ({ code, name: BANK_NAMES[code], accountCount: state.accounts.filter(a => a.bank === code).length }));
    return {
      accounts: accs, otherProjects: PROJECTS, recentOperations: recent,
      otherBanks: [{ name: 'Ziraat Bank', balance: 4200, currency: 'USD' }],
      exchangeRates: RATES, connectedBanks: connected, selectedBank: bank || null,
    };
  }

  function pay(body) {
    const from = state.accounts.find(a => a.id === body.fromAccountId);
    const amount = Number(body.amount);
    if (!from) return { status: 'rejected', message: 'Hesab tapılmadı', source: 'mock', bulkId: null };
    // Valyuta qaydası: hər hesabdan yalnız öz valyutasında məxaric edilə bilər (AZN→AZN, USD→USD, EUR→EUR)
    if (body.currency && body.currency !== from.currency)
      return { status: 'rejected', message: `${from.currency} hesabından yalnız ${from.currency} valyutasında ödəniş edilə bilər. Başqa valyuta üçün əvvəlcə valyuta mübadiləsi edin.`, source: 'mock', bulkId: null };
    if (!(amount > 0)) return { status: 'rejected', message: 'Məbləğ düzgün deyil', source: 'mock', bulkId: null };
    if (amount > from.balance) return { status: 'rejected', message: 'Hesabda kifayət qədər vəsait yoxdur', source: 'mock', bulkId: null };
    // Öz hesablar arası köçürmə / valyuta mübadiləsi: alıcı hesabı da yenilənəcək
    const to = state.accounts.find(a => a.id !== from.id && body.beneficiaryIban && a.iban === String(body.beneficiaryIban).replace(/\s/g, ''));
    // Fərqli valyutalı hesaba vəsait yalnız valyuta mübadiləsi (creditAmount ilə) yolu ilə keçə bilər
    if (to && to.currency !== from.currency && body.creditAmount == null)
      return { status: 'rejected', message: 'Fərqli valyutalı hesaba köçürmə yalnız valyuta mübadiləsi ilə mümkündür', source: 'mock', bulkId: null };
    const date = iso(new Date());
    from.balance = r2(from.balance - amount);
    state.seq += 1;
    state.ops[from.id].unshift({ id: `pay-${state.seq}`, date, counterparty: body.beneficiaryName || 'Alıcı',
      description: body.description || 'Ödəniş', amount: r2(amount), currency: from.currency, direction: 'out', status: 'completed',
      accountId: from.id, bank: from.bank });
    if (to) {
      const credit = body.creditAmount != null ? Number(body.creditAmount) : amount;
      to.balance = r2(to.balance + credit);
      state.ops[to.id].unshift({ id: `pay-${state.seq}-in`, date, counterparty: from.name + ' · ' + BANK_NAMES[from.bank],
        description: body.description || 'Öz hesablar arası köçürmə', amount: r2(credit), currency: to.currency, direction: 'in', status: 'completed',
        accountId: to.id, bank: to.bank });
    }
    save();
    return { status: 'completed', message: 'Ödəniş uğurla icra olundu (demo)', source: 'mock', bulkId: null };
  }

  // ---------- Mock giriş / qeydiyyat ----------
  // Demo qaydaları: təsdiq (OTP) kodu həmişə 123456. Bankda "tapılan" PIN-lər: 1234567, 7654321.
  // Hazır hesab: demo@corbank.az / Demo12345. Qeydiyyatdan keçənlər brauzerin localStorage-ində qalır.
  const USERS_KEY = 'corbank_mock_users';
  const PENDING_KEY = 'corbank_mock_pending';
  const KNOWN_PINS = ['1234567', '7654321'];
  const OTP = '123456';
  const readJson = (k, d) => { try { return JSON.parse(localStorage.getItem(k)) || d; } catch (e) { return d; } };
  const writeJson = (k, v) => { try { localStorage.setItem(k, JSON.stringify(v)); } catch (e) { /* yaddaş bağlıdır */ } };
  const users = () => {
    const list = readJson(USERS_KEY, []);
    if (!list.some(u => u.email === 'demo@corbank.az')) {
      list.unshift({ name: 'Elvin', surname: 'Məmmədov', email: 'demo@corbank.az', mobileNumber: '+994501112233', password: 'Demo12345', pin: '1234567' });
    }
    return list;
  };
  const tokensFor = email => ({ accessToken: 'mock.' + btoa(unescape(encodeURIComponent(email))), refreshToken: 'mockrefresh.' + btoa(unescape(encodeURIComponent(email))) });
  const emailFrom = token => {
    const m = /^mock(?:refresh)?\.(.+)$/.exec(token || '');
    try { return m ? decodeURIComponent(escape(atob(m[1]))) : null; } catch (e) { return null; }
  };
  const err = (status, message) => [status, { message }];

  function authRoute(method, path, body, headers) {
    if (method === 'GET' && path === '/public/fx-rates') return [200, RATES];
    if (method === 'POST' && path.startsWith('/v1/auth/')) {
      body = body || {};
      if (path === '/v1/auth/login') {
        const u = users().find(x => x.email === String(body.email || '').trim().toLowerCase() && x.password === body.password);
        return u ? [200, tokensFor(u.email)] : err(401, 'Email və ya şifrə yanlışdır');
      }
      if (path === '/v1/auth/refresh') {
        const email = emailFrom(body.refreshToken);
        return email ? [200, tokensFor(email)] : err(401, 'Refresh token etibarsızdır və ya vaxtı bitib');
      }
      if (path === '/v1/auth/register') {
        const email = String(body.email || '').trim().toLowerCase();
        if (users().some(u => u.email === email)) return err(400, 'Bu email artıq qeydiyyatdadır');
        if (users().some(u => u.mobileNumber === body.mobileNumber)) return err(400, 'Bu mobil nömrə artıq qeydiyyatdadır');
        if (users().some(u => u.pin === String(body.pinCode).trim())) return err(400, 'Bu PIN artıq qeydiyyatdadır');
        if (!KNOWN_PINS.includes(String(body.pinCode || '').trim())) return err(422, 'Bu PIN ilə bankda müştəri tapılmadı');
        const id = 'mock-' + Date.now();
        writeJson(PENDING_KEY, { id, user: { name: body.name, surname: body.surname, email, mobileNumber: body.mobileNumber, password: body.password, pin: String(body.pinCode).trim() } });
        setTimeout(() => { if (window.showToast) showToast('Demo rejimi: təsdiq kodu ' + OTP, 'info'); }, 400);
        return [200, { signUpSessionId: id, message: 'OTP kodu emailinizə göndərildi' }];
      }
      if (path === '/v1/auth/resend-otp') {
        setTimeout(() => { if (window.showToast) showToast('Demo rejimi: təsdiq kodu ' + OTP, 'info'); }, 300);
        return [204, undefined];
      }
      if (path === '/v1/auth/verify') {
        const pending = readJson(PENDING_KEY, null);
        if (!pending || pending.id !== body.signUpSessionId) return err(400, 'Qeydiyyat sessiyası tapılmadı və ya vaxtı bitib');
        if (body.otp !== OTP) return err(400, 'OTP yanlışdır');
        const list = users(); list.push(pending.user); writeJson(USERS_KEY, list);
        try { localStorage.removeItem(PENDING_KEY); } catch (e) { /* yaddaş bağlıdır */ }
        return [201, undefined];
      }
    }
    return null;
  }

  function route(method, url, body, headers) {
    const u = new URL(url, location.href);
    const path = u.pathname.replace(/^.*\/api/, '');
    const q = u.searchParams;
    const authRes = authRoute(method, path, body, headers);
    if (authRes) return authRes;
    // qalan hər şey login tələb edir
    const auth = (headers && (headers.Authorization || headers.authorization)) || '';
    const email = emailFrom(auth.replace(/^Bearer\s+/i, ''));
    if (!email || !users().some(x => x.email === email)) return err(401, 'Giriş tələb olunur');
    if (path === '/me') {
      const me = users().find(x => x.email === email);
      return [200, { name: me.name, surname: me.surname, email: me.email, mobileNumber: me.mobileNumber }];
    }
    if (method === 'POST' && path === '/payments') return [200, pay(body || {})];
    if (path === '/dashboard') return [200, dashboard(q.get('bank'))];
    if (path === '/accounts') return [200, { accounts: accountsFor(q.get('bank')) }];
    let m = path.match(/^\/projects\/([^/]+)$/);
    if (m) { const p = PROJECTS.find(x => x.id === decodeURIComponent(m[1])); return p ? [200, p] : [404, {}]; }
    m = path.match(/^\/accounts\/([^/]+)(\/operations)?$/);
    if (m) {
      const id = decodeURIComponent(m[1]);
      const acc = state.accounts.find(a => a.id === id);
      if (!acc) return [404, {}];
      return [200, m[2] ? operations(id, q) : { account: acc }];
    }
    return [404, {}];
  }

  const realFetch = window.fetch.bind(window);
  window.fetch = async function (input, init) {
    const url = typeof input === 'string' ? input : input.url;
    if (!/\/api(\/|$)/.test(url)) return realFetch(input, init);
    const method = ((init && init.method) || 'GET').toUpperCase();
    const headers = (init && init.headers) || {};
    let body = null;
    try { body = init && init.body ? JSON.parse(init.body) : null; } catch (e) { body = null; }
    await new Promise(r => setTimeout(r, 120)); // şəbəkə gecikməsi
    const [status, json] = route(method, url, body, headers);
    return new Response(status === 204 ? null : JSON.stringify(json), { status, headers: { 'Content-Type': 'application/json' } });
  };
})();
