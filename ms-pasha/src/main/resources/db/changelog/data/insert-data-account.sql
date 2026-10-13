-- ms-pasha: hesablar (customers insert-indən SONRA işlət).
-- Subhan  : Pasha AZN cari + USD kart   (ABB-də də hesabı var)
-- Rasul   : Pasha AZN cari + EUR cari   (ABB-də YOXDUR)
-- İmran   : Pasha AZN maaş kartı + USD kart + EUR cari (ABB-də AZN + USD var, EUR YOXDUR)
-- Fərid   : Pasha 2 AZN (cari + kart) + EUR cari       (ABB-də AZN + USD var, EUR YOXDUR)
INSERT INTO pasha_accounts (
    created_at,
    updated_at,
    account_id,
    customer_id,
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
-- 1. Subhan Vəlili (AZN Cari Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-4001',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410001'),
            'individual', '4001000001',
            '2022-03-14', 'ACTIVE', 'CURRENT', 12450.80, 12450.80,
            0.00, 'AZN', 'AZ21PASH00000000400100000001', 'PASHAAZ22', '001', 'Baş Ofis', '1400110011',
            true, true, true, false, false,
            1500.00, 11450.80, 500.00
),
-- 2. Subhan Vəlili (USD Kart Hesabı)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-4002',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410001'),
            'individual', '4001000002',
            '2023-06-02', 'ACTIVE', 'CARD', 2300.00, 2300.00,
            0.00, 'USD', 'AZ21PASH00000000400100000002', 'PASHAAZ22', '001', 'Baş Ofis', '1400110011',
            true, true, true, false, false,
            0.00, 2300.00, 0.00
),
-- 3. Rasul Kərimov (AZN Cari Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-5001',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410002'),
            'individual', '5001000001',
            '2021-09-20', 'ACTIVE', 'CURRENT', 8760.40, 8760.40,
            0.00, 'AZN', 'AZ21PASH00000000500100000001', 'PASHAAZ22', '002', 'Nəsimi Filialı', '1400220022',
            true, true, true, true, false,
            2200.00, 7060.40, 500.00
),
-- 4. Rasul Kərimov (EUR Cari Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-5002',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410002'),
            'individual', '5001000002',
            '2023-01-11', 'ACTIVE', 'CURRENT', 1520.00, 1520.00,
            0.00, 'EUR', 'AZ21PASH00000000500100000002', 'PASHAAZ22', '002', 'Nəsimi Filialı', '1400220022',
            true, true, false, false, false,
            0.00, 1520.00, 0.00
),
-- 5. İmran Yusubov (AZN Maaş Kartı)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-6001',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410004'),
            'individual', '6001000001',
            '2024-02-05', 'ACTIVE', 'CARD', 3150.25, 3150.25,
            0.00, 'AZN', 'AZ21PASH00000000600100000001', 'PASHAAZ22', '003', 'Gənclik Filialı', '1400440044',
            true, true, true, false, false,
            1800.00, 1500.25, 150.00
),
-- 6. İmran Yusubov (USD Kart Hesabı)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-6002',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410004'),
            'individual', '6001000002',
            '2024-02-05', 'ACTIVE', 'CARD', 2750.00, 2750.00,
            0.00, 'USD', 'AZ21PASH00000000600100000002', 'PASHAAZ22', '003', 'Gənclik Filialı', '1400440044',
            true, true, true, false, false,
            300.00, 2450.00, 0.00
),
-- 7. İmran Yusubov (EUR Cari Hesab) — EUR yalnız Pasha-dadır, ABB-də yoxdur
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-6003',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410004'),
            'individual', '6001000003',
            '2024-05-18', 'ACTIVE', 'CURRENT', 4180.60, 4180.60,
            0.00, 'EUR', 'AZ21PASH00000000600100000003', 'PASHAAZ22', '003', 'Gənclik Filialı', '1400440044',
            true, true, false, false, false,
            0.00, 4180.60, 0.00
),
-- 8. Fərid Abbasov (AZN Cari Hesab)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-7001',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410003'),
            'individual', '7001000001',
            '2022-07-12', 'ACTIVE', 'CURRENT', 5640.30, 5640.30,
            0.00, 'AZN', 'AZ21PASH00000000700100000001', 'PASHAAZ22', '004', 'Yasamal Filialı', '1400330033',
            true, true, true, false, false,
            900.00, 4940.30, 200.00
),
-- 9. Fərid Abbasov (AZN Maaş Kartı)
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-7002',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410003'),
            'individual', '7001000002',
            '2023-10-03', 'ACTIVE', 'CARD', 1275.90, 1275.90,
            0.00, 'AZN', 'AZ21PASH00000000700100000002', 'PASHAAZ22', '004', 'Yasamal Filialı', '1400330033',
            true, true, true, false, false,
            1500.00, 275.90, 500.00
),
-- 10. Fərid Abbasov (EUR Cari Hesab) — EUR yalnız Pasha-dadır, ABB-də yoxdur
(
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACC-7003',
            (SELECT id FROM customers WHERE customer_no = 'CUST-410003'),
            'individual', '7001000003',
            '2024-04-22', 'ACTIVE', 'CURRENT', 2140.00, 2140.00,
            0.00, 'EUR', 'AZ21PASH00000000700100000003', 'PASHAAZ22', '004', 'Yasamal Filialı', '1400330033',
            true, true, false, false, false,
            0.00, 2140.00, 0.00
);