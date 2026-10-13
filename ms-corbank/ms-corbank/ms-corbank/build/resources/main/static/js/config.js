// Backend ünvanı.
// Frontend Spring Boot-un özündən verilirsə (http://localhost:8082/) '/api' kifayətdir.
// Ayrıca server ilə açılırsa (Live Server, file:// və s.) avtomatik http://localhost:8082/api istifadə olunur.
// Fərqli ünvan lazımdırsa aşağıda CORBANK_BACKEND_URL yazın, məs: 'https://bank.example.az'
window.CORBANK_BACKEND_URL = '';

// false = real backend (ms-corbank, port 8082). true = backend-siz demo rejimi (js/mock-api.js):
// qeydiyyat PIN-i 1234567 / 7654321, təsdiq kodu 123456, hazır hesab demo@corbank.az / Demo12345
window.CORBANK_USE_MOCK = false;

(function () {
  // Backend-in öz portu (8082) istisna olmaqla, localhost-dakı istənilən port / file:// ayrıca front sayılır
  const local = ['localhost', '127.0.0.1'].includes(location.hostname);
  const standalone = location.protocol === 'file:' || (local && location.port !== '8082');
  const base = window.CORBANK_BACKEND_URL || (standalone ? 'http://localhost:8082' : '');
  window.CORBANK_API_BASE_URL = base + '/api';
})();

window.CORBANK_COMPANY_NAME = 'Sizin biznesiniz';

// Boş = real bugünkü tarix
window.CORBANK_DEMO_TODAY = '';
