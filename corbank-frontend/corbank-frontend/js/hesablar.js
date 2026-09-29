// Bütün hesablar page — uses helpers from common.js and orders.js
document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar(''));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());

function accountTileHtml(acc) {
  return `
  <div class="hs-card">
    <div class="hs-flag">${flagImg(acc.currency, 44)}</div>
    <div class="hs-name">${escHtml(accountLabel(acc))}</div>
    <div class="hs-balance">${Fmt.money(acc.balance, acc.currency)}</div>
    <div class="hs-iban">${escHtml(acc.iban)}</div>
    <a class="btn-primary hs-go" href="${accountUrl(acc.id)}">Hesaba keç <span class="hs-go-chev">${Icons.chevron}</span></a>
  </div>`;
}

async function load() {
  try {
    const res = await Api.get('/accounts' + BankSel.query());
    document.getElementById('accountsGrid').innerHTML = (res.accounts || []).map(accountTileHtml).join('');
  } catch (e) {
    console.error(e);
    document.querySelector('.db-page').insertAdjacentHTML('afterbegin',
      '<p style="color:#d84343">Məlumatları yükləmək mümkün olmadı. Backend işə salınıbmı? (mvn spring-boot:run)</p>');
  }
}

document.getElementById('newAccountBtn').addEventListener('click', () => openOrderModal('account'));
document.addEventListener('corbank:company-changed', (e) => { e.preventDefault(); load(); });

load();
