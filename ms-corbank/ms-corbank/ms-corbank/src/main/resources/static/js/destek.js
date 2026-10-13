// Əlaqə və dəstək page — uses helpers from common.js (Icons, Prefs, renderSidebar, renderTopbar, showToast)

document.getElementById('app').insertAdjacentHTML('afterbegin', renderSidebar('destek.html'));
document.querySelector('.shell').insertAdjacentHTML('afterbegin', renderTopbar());

document.getElementById('dsSendIcon').innerHTML = Icons.send;
document.getElementById('dsEmail').value = Prefs.get().email;

function esc(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// ---------------------------------------------------------------
// FAQ akkordeon
// ---------------------------------------------------------------
const FAQ = [
  {
    q: 'Bu sistem real bankdır?',
    a: 'Xeyr. COR Bank təqdimat prototipidir. Bütün hesablar və əməliyyatlar demo məlumatlardır.',
    open: true,
  },
  {
    q: 'Çıxarışı necə yükləyim?',
    a: 'Mühasibatlıq bölməsindəki “CSV yüklə” düyməsi ilə əməliyyatların demo çıxarışını endirə bilərsiniz.',
    open: false,
  },
  {
    q: 'Demo dəyişikliklər saxlanılır?',
    a: 'Parametrlərdəki seçimlər brauzerinizdə (localStorage) saxlanılır; hesab qalıqları isə hər dəfə demo məlumatlardan yenidən yüklənir.',
    open: false,
  },
];

document.getElementById('dsFaqList').innerHTML = FAQ.map((item, i) => `
  <div class="ds-faq-item ${item.open ? 'open' : ''}" data-idx="${i}">
    <button class="ds-faq-q" type="button">
      <span class="ds-faq-chev">${Icons.chevDown}</span>
      <span>${esc(item.q)}</span>
    </button>
    <div class="ds-faq-a">${esc(item.a)}</div>
  </div>`).join('');

document.querySelectorAll('.ds-faq-q').forEach(btn => {
  btn.addEventListener('click', () => {
    btn.closest('.ds-faq-item').classList.toggle('open');
  });
});

// ---------------------------------------------------------------
// Demo müraciət forması
// ---------------------------------------------------------------
document.getElementById('dsSubmit').addEventListener('click', () => {
  const subject = document.getElementById('dsSubject').value.trim();
  const message = document.getElementById('dsMessage').value.trim();

  if (!subject || !message) {
    showToast('Zəhmət olmasa mövzu və mesaj daxil edin', 'error');
    return;
  }

  showToast('Demo müraciətiniz yaradıldı', 'success');
  document.getElementById('dsSubject').value = '';
  document.getElementById('dsMessage').value = '';
});
