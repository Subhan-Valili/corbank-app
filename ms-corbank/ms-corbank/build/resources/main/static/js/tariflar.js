// Tariflər page — uses helpers from common.js (Icons, renderSidebar, renderTopbar)

document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('tariflar.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());

function esc(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

const TARIFFS = [
  { service: 'Cari hesabın açılması',        price: '0 AZN', note: 'AZN / USD / EUR' },
  { service: 'Öz hesabları arasında köçürmə', price: '0 AZN', note: 'Demo məzənnə tətbiq olunur' },
  { service: 'Ölkədaxili və ani ödəniş',      price: '0 AZN', note: 'Demo əməliyyat' },
  { service: 'Ölkəxarici köçürmə',            price: '0 AZN', note: 'SHA / OUR / BEN seçimi' },
  { service: 'Çıxarışın yüklənməsi',          price: '0 AZN', note: 'CSV və PDF' },
  { service: 'Biznes kart',                   price: '0 AZN', note: 'Demo müraciət' },
];

document.getElementById('tfBody').innerHTML = TARIFFS.map(t => `
  <tr>
    <td class="tf-service">${esc(t.service)}</td>
    <td class="tf-price">${esc(t.price)}</td>
    <td class="tf-note">${esc(t.note)}</td>
  </tr>`).join('');
