-- ms-pasha: API key-lər (customers insert-indən SONRA işlət).
-- Yalnız DEV/TEST üçün. Açıq key-lər aşağıda şərhdədir, DB-yə yalnız SHA-256 hash yazılır.
-- Subhan : aktiv key + ləğv olunmuş key + vaxtı bitmiş key (401 ssenariləri üçün)
-- Rasul, Fərid, İmran : hər birinə 1 aktiv key
INSERT INTO api_keys (
    created_at,
    updated_at,
    key_hash,
    key_prefix,
    active,
    expires_at,
    customer_id
) VALUES
-- 1. Subhan Vəlili (aktiv)   açıq key: pasha_test_subhan_410001
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
            encode(sha256('pasha_test_subhan_410001'::bytea), 'hex'),
            'pasha_test',
            true,
            NULL,
            (SELECT id FROM customers WHERE customer_no = 'CUST-410001')
),
-- 2. Rasul Kərimov (aktiv)   açıq key: pasha_test_rasul_410002
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
            encode(sha256('pasha_test_rasul_410002'::bytea), 'hex'),
            'pasha_test',
            true,
            NULL,
            (SELECT id FROM customers WHERE customer_no = 'CUST-410002')
),
-- 3. Fərid Abbasov (aktiv)   açıq key: pasha_test_ferid_410003
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
            encode(sha256('pasha_test_ferid_410003'::bytea), 'hex'),
            'pasha_test',
            true,
            NULL,
            (SELECT id FROM customers WHERE customer_no = 'CUST-410003')
),
-- 4. İmran Yusubov (aktiv)   açıq key: pasha_test_imran_410004
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
            encode(sha256('pasha_test_imran_410004'::bytea), 'hex'),
            'pasha_test',
            true,
            NULL,
            (SELECT id FROM customers WHERE customer_no = 'CUST-410004')
),
-- 5. Subhan Vəlili (LƏĞV OLUNUB, active=false)   açıq key: pasha_test_revoked_410001
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
            encode(sha256('pasha_test_revoked_410001'::bytea), 'hex'),
            'pasha_test',
            false,
            NULL,
            (SELECT id FROM customers WHERE customer_no = 'CUST-410001')
),
-- 6. Subhan Vəlili (VAXTI BİTİB)   açıq key: pasha_test_expired_410001
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
            encode(sha256('pasha_test_expired_410001'::bytea), 'hex'),
            'pasha_test',
            true,
            CURRENT_TIMESTAMP - INTERVAL '1 day',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410001')
);