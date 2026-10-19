// Backend ünvanı.
// Frontend Spring Boot-un özündən verilirsə (http://localhost:8082/) '/api' kifayətdir.
// Ayrıca server ilə açılırsa (Live Server, file:// və s.) avtomatik http://localhost:8082/api istifadə olunur.
// Fərqli ünvan lazımdırsa aşağıda CORBANK_BACKEND_URL yazın, məs: 'https://bank.example.az'
window.CORBANK_BACKEND_URL = '';

// false = real backend (ms-corbank, port 8082). true = backend-siz demo rejimi (js/mock-api.js):
// qeydiyyat PIN-i 1234567 / 7654321, təsdiq kodu 123456, hazır hesab demo@corbank.az / Demo12345
window.CORBANK_USE_MOCK = false;

(function () {
  const devPorts = ['3000', '5173', '5500', '5501', '8000'];
  const standalone = location.protocol === 'file:' || devPorts.includes(location.port);
  const base = window.CORBANK_BACKEND_URL || (standalone ? 'http://localhost:8082' : '');
  window.CORBANK_API_BASE_URL = base + '/api';
})();

window.CORBANK_COMPANY_NAME = 'Sizin biznesiniz';

// Boş = real bugünkü tarix
window.CORBANK_DEMO_TODAY = '';
