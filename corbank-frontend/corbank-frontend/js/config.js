// Edit this if your backend runs somewhere other than http://localhost:8082
// (ms-corbank server.port=8082 in application.yaml — 8081 is ms-pasha, the upstream mock bank, not this backend)
window.CORBANK_API_BASE_URL = 'http://localhost:8082/api';

// Shown as a subtitle in some modals (e.g. the transfer wizard). Purely cosmetic.
window.CORBANK_COMPANY_NAME = 'Sizin biznesiniz';

// Demo tarixi (YYYY-MM-DD). "Bugün, ..." yazısı və hesab səhifəsinin default dövrü buna əsaslanır.
// Real tarixdən istifadə etmək üçün boş buraxın: window.CORBANK_DEMO_TODAY = '';
window.CORBANK_DEMO_TODAY = '2025-04-14';
