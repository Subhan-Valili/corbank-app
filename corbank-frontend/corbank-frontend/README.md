# COR Bank Business — frontend (static HTML/CSS/JS)

No build step, no framework. Pages, matching the mockups:
- `index.html` — overview ("Biznesinizə ümumi baxış")
- `account.html?id=azn-001` — account detail ("AZN hesabı")
- `muhasibatliq.html` — accounting ("Mühasibatlıq"): all-accounts operations + requests
- `hesablar.html` — all accounts ("Bütün hesablar"), opened from "Bütün hesabları gör"

## Run

Point it at the backend first — edit `js/config.js`:

```js
window.CORBANK_API_BASE_URL = 'http://localhost:8082/api';
```

Then serve this folder with any static file server, e.g.:

```bash
npx serve .
# or
python3 -m http.server 5500
```

Open the served `index.html` in your browser. Make sure `corbank-backend` is running (see the
separate backend package) — the frontend calls it for all data, it has no data of its own.

## Files

```
index.html          overview page
account.html         account detail page (?id=azn-001 | usd-001 | eur-001)
muhasibatliq.html    accounting page
css/style.css        base styling (shared)
css/shell.css        sidebar logo, topbar, flag/bank image styles (shared)
css/dashboard.css    styling for index.html only
css/muhasibatliq.css styling for muhasibatliq.html only (kept separate from style.css)
js/config.js         <- set your backend URL here
js/common.js         API client, formatting helpers, sidebar/icon templates
js/dashboard.js       logic for index.html
js/account.js         logic for account.html
js/muhasibatliq.js    logic for muhasibatliq.html
js/hesablar.js        logic for hesablar.html
js/orders.js          order (demo application) modal, shared by index.html and hesablar.html
css/hesablar.css      styling for hesablar.html only
img/                 SVG logo, flags (AZN/USD/EUR), bank logos (PASHA/ABB/Ziraat) — replace with official files if you have them
```

## Demo behaviour (added from the screen recording)

- **Dashboard**: project cards open a detail modal (data from `GET /projects/{id}` → `details`) with a
  "Yeni müraciət" button; the 5 "Sifarişlər" open demo application forms (native `required` validation,
  nothing is sent to the backend); "Bütün bankları gör" / "Bütün məzənnələri gör" open modals;
  "Valyuta mübadiləsinə keç" opens `account.html?id=azn-001&action=fx` (FX wizard opens automatically).
- **Topbar (all pages)**: company dropdown (ALFA MMC / BETA MMC) and profile modal. The selection is kept
  in `localStorage` (`corbank_company`).
  The backend has no notion of companies, so BETA MMC is a *demo view*: `Company` in `js/common.js`
  multiplies the backend's money fields by 0.4 on GET (dashboard/accounts/projects) and divides the
  amounts by 0.4 on `POST /payments`, so BETA payments still change the shared mock ledger. If the backend
  learns about companies, remove `Company.scale` / `Company.toBackend` and pass the company id instead.
- **Account page**: topbar, "← Əsas səhifəyə qayıt", account switcher, and "Kütləvi ödənişlər" as a 3-step
  CSV wizard (columns `Alıcı,IBAN,Məbləğ,Təyinat`; max 100 rows / 1 MB; sample CSV download; every row is
  sent to `POST /payments`).
- **Kartdan-karta köçürmə**: another field-driven wizard (same `openFieldWizard` used by domestic/international/
  instant/budget/utility) — no separate backend endpoint, the card number is just formatted/validated
  client-side (`js/account.js` → `cardToCardConfig`) and sent as `beneficiaryIban` on the same generic
  `POST /payments` (backend already treats every wizard's output through one `PaymentRequestDto`).
- **Demo date**: `window.CORBANK_DEMO_TODAY` in `js/config.js` (default `'2025-04-14'`, as in the recording)
  drives the "Bugün, …" label and the account page's default period (1st of that month → that date).
  Set it to `''` to use the real date again. Payments made in the demo are dated by the backend, so press
  "Bütün dövr" on the account page if a fresh payment falls outside the default period.
