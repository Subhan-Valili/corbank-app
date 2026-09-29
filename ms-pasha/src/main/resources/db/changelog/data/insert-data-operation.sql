INSERT INTO account_operations (
    created_at,
    updated_at,
    external_id,
    amount_value,
    amount_currency_code,
    counterparty_id,
    counterparty_name,
    operation_date,
    description,
    source,
    type,
    status,
    pasha_account_id
) VALUES
-- 1. ACC-1001 hesabına mədaxil - tamamlanmış
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPR-100001', 18500.00, 'AZN',
            'CUST-334455', 'Caspian Trade MMC', '2025-04-14 10:15:00',
            'Qarşı tərəfdən əldə olunan ödəniş', 'CURRENT', 'CREDIT', 'COMPLETED',
            (SELECT id FROM pasha_accounts WHERE account_id = 'ACC-1001')
),
-- 2. ACC-1001 hesabından məxaric - gözləmədə
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPR-100002', 2400.00, 'AZN',
            'CUST-667788', 'Caspian Business Center MMC', '2025-04-12 14:30:00',
            'Ofis icarəsi', 'CURRENT', 'DEBIT', 'PENDING',
            (SELECT id FROM pasha_accounts WHERE account_id = 'ACC-1001')
),
-- 3. ACC-1002 hesabına mədaxil (USD) - tamamlanmış
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPR-100003', 1500.50, 'USD',
            'CUST-998877', 'Online Marketplace Inc', '2025-04-10 09:00:00',
            'Onlayn satışdan mədaxil', 'CURRENT', 'CREDIT', 'COMPLETED',
            (SELECT id FROM pasha_accounts WHERE account_id = 'ACC-1002')
),
-- 4. ACC-2001 hesabından məxaric (korporativ) - uğursuz
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPR-100004', 28400.00, 'AZN',
            'CUST-112233', 'Caspian HR Services MMC', '2025-04-10 16:45:00',
            'Əmək haqqı layihəsi üzrə ödəniş', 'CURRENT', 'DEBIT', 'FAILED',
            (SELECT id FROM pasha_accounts WHERE account_id = 'ACC-2001')
),
-- 5. ACC-2001 hesabına mədaxil - tamamlanmış
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPR-100005', 50000.00, 'AZN',
            'CUST-445566', 'Araz Logistics MMC', '2025-04-08 11:20:00',
            'Qarşı tərəfdən əldə olunan ödəniş', 'CURRENT', 'CREDIT', 'COMPLETED',
            (SELECT id FROM pasha_accounts WHERE account_id = 'ACC-2001')
),
-- 6. ACC-3001 hesabına mədaxil (maaş kartı) - tamamlanmış
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPR-100006', 1200.00, 'AZN',
            'CUST-554433', 'Ms Pasha Bank MMC', '2025-04-01 08:00:00',
            'Aylıq əmək haqqı', 'CURRENT', 'CREDIT', 'COMPLETED',
            (SELECT id FROM pasha_accounts WHERE account_id = 'ACC-3001')
),
-- 7. ACC-3001 hesabından məxaric - ləğv edilmiş
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPR-100007', 549.75, 'AZN',
            'CUST-778899', 'Market Plus MMC', '2025-04-05 19:10:00',
            'POS terminalda ödəniş', 'CURRENT', 'DEBIT', 'CANCELLED',
            (SELECT id FROM pasha_accounts WHERE account_id = 'ACC-3001')
);