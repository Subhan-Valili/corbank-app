INSERT INTO pasha_accounts (
    created_at,
    updated_at,
    account_id,
    customer_no,
    account_category,
    account_no,
    account_open_date,
    account_status,
    account_type,
    available_balance,
    current_balance,
    blocked_amount,
    currency,
    iban,
    bank_code,
    branch_code,
    branch_name,
    tin,
    credit_is_allowed,
    debit_is_allowed,
    has_card,
    has_credit,
    has_pos,
    today_income,
    today_opening_balance,
    today_outcome
) VALUES
-- 1. Fərdi Müştəri (AZN Cari Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-1001', 'CUST-998877', 'individual', '1002345678',
            '2021-05-15', 'ACTIVE', 'CURRENT', 64320.00, 64320.00,
            0.00, 'AZN', 'AZ21CORB00000000123456789012', 'PASHAAZ22', '001', 'Baş Ofis', '1234567891',
            true, true, true, true, true,
            18500.00, 48220.00, 2400.00
),
-- 2. Fərdi Müştəri (USD Kart Hesabı)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-1002', 'CUST-998877', 'individual', '1002345679',
            '2022-01-10', 'ACTIVE', 'CARD', 1500.50, 1500.50,
            0.00, 'USD', 'AZ21CORB00000000123456789013', 'PASHAAZ22', '001', 'Baş Ofis', '1234567891',
            true, true, true, false, false,
            0.00, 1500.50, 0.00
),
-- 3. Fərdi Müştəri (EUR Cari Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-1003', 'CUST-998877', 'individual', '1002345680',
            '2023-03-22', 'ACTIVE', 'CURRENT', 3200.00, 3200.00,
            0.00, 'EUR', 'AZ21CORB00000000123456789014', 'PASHAAZ22', '001', 'Baş Ofis', '1234567891',
            true, true, false, false, false,
            500.00, 2700.00, 0.00
),
-- 4. Korporativ Müştəri (AZN Əsas Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-2001', 'CUST-112233', 'corporate', '2005678901',
            '2020-11-20', 'ACTIVE', 'CURRENT', 125000.00, 130000.00,
            5000.00, 'AZN', 'AZ21CORB00000000987654321099', 'PASHAAZ22', '002', 'Nəsimi Filialı', '9900123451',
            true, true, true, true, true,
            50000.00, 80000.00, 5000.00
),
-- 5. Korporativ Müştəri (USD Bloklanmış Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-2002', 'CUST-112233', 'corporate', '2005678902',
            '2021-08-05', 'BLOCKED', 'CURRENT', 0.00, 12000.00,
            12000.00, 'USD', 'AZ21CORB00000000987654321100', 'PASHAAZ22', '002', 'Nəsimi Filialı', '9900123451',
            false, false, false, true, false,
            0.00, 12000.00, 0.00
),
-- 6. Yeni Fərdi Müştəri (AZN Maaş Kartı)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-3001', 'CUST-554433', 'individual', '3009876543',
            '2024-02-01', 'ACTIVE', 'CARD', 850.25, 850.25,
            0.00, 'AZN', 'AZ21CORB00000000554433221100', 'PASHAAZ22', '003', 'Gənclik Filialı', '5544332211',
            true, true, true, false, false,
            1200.00, 200.00, 549.75
);