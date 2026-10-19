# Dəyişikliklər (guest vs login + PIN)

## Məntiq
- **Guest**: yalnız `GET /api/public/fx-rates` (şəxsi data yoxdur).
- **Login olmuş user**: `/api/dashboard`, `/api/accounts`, `/api/accounts/{id}/operations`, `/api/payments`
  yalnız onun PIN-inə aid hesabları görür/işlədir (ms-pasha `by-pin`). Başqasının hesabı -> 404.
- **Register**: OTP göndərilməzdən əvvəl ms-pasha-da PIN yoxlanılır (yoxdursa 422, ms-pasha düşübsə 503),
  PIN artıq qeydiyyatdadırsa rədd edilir.
- PIN artıq BCrypt yox: `pin_hash` (HMAC, unikallıq üçün) + `pin_encrypted` (AES-GCM, bank sorğusu üçün).

## Yeni env dəyişənləri (production-da mütləq)
PIN_ENCRYPTION_KEY, PIN_HASH_KEY  (uzun təsadüfi dəyərlər; dəyişsə köhnə PIN-lər açılmaz)

## DB
Liquibase: `alterations/11102026_users_pin_protect.yaml` — `pin_code` silinir, `pin_hash`/`pin_encrypted` əlavə olunur.
Köhnə istifadəçilərin PIN-i bərpa oluna bilmir (yalnız bcrypt hash var) -> yenidən register olmalıdırlar.

## Diqqət
- ms-abb-bank `X-Pin` header-ini nəzərə almalıdır, yoxsa ABB hesabları hamıya eyni görünər.
- `/api/v1/accounts/**` və `/api/v1/abb/**` passthrough-lar yalnız ROLE_ADMIN-dir.
- Bulk/GPP ödəniş yaratma: ödəyən hesab user-ə aid olmalıdır. Bulk `/approve` hələ yoxlanmır.

## Frontend (ayrıca: corbank-front)
Frontend bu layihədən ayrıdır. Backend onunla CORS (app.cors.allowed-origins) və bu endpoint-lərlə danışır:
- açıq: POST /api/v1/auth/{register,verify,resend-otp,login,refresh}, GET /api/public/fx-rates
- login tələb edir: GET /api/me, /api/dashboard, /api/accounts, /api/accounts/{id}, /api/accounts/{id}/operations, POST /api/payments
- xəta cavabları: {"message": "..."} — 400 (qeydiyyat), 401 (şifrə/refresh), 422 (PIN tapılmadı), 503 (bank/Redis əlçatmaz)

## Redis
OTP və qeydiyyat sessiyası Redis-də saxlanılır (localhost:6379). Redis işləməsə register 503 qaytarır.
Başlatmaq: docker run -d -p 6379:6379 redis   (Windows-da Memurai/Redis və ya WSL)
