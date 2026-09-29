-- =============================================================================
-- GPP MOCK DATA SCRIPT (insert-data-gpp.sql)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. gpp_payments
-- -----------------------------------------------------------------------------
INSERT INTO gpp_payments (
    id, created_at, updated_at, external_payment_id, description, status, type, bank_status_code,
    reject_reason, transfer_amount, commission_amount, create_date, payer_account_number,
    payer_tin, payer_type, invoice_request_code, invoice_request_prefix,
    invoice_request_additional_code, invoice_request_identification_subtype,
    invoice_request_sc_code, invoice_request_sp_code
) VALUES
-- Payment 1: Fərdi Müştəri (ACC-1001) - Vergi Ödənişi (COMPLETED)
(
    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9007199254740101, 'Mənfəət və Gəlir Vergisi Ödənişi',
    'COMPLETED', 'TAX', '200', NULL, 350.00, 1.50, TIMESTAMP '2026-09-20 10:30:00',
    'AZ21CORB00000000123456789012', '1234567891', 'PHYSICAL', 'TAX-2026-01', 'AZ',
    '101', 'TIN', 10, 1001
),
-- Payment 2: Korporativ Müştəri (ACC-2001) - Dövlət Rüsumu və Kommunal (COMPLETED)
(
    2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9007199254740102, 'Dövlət Rüsumu və Kommunal Xidmət Ödənişləri',
    'COMPLETED', 'UTILITY', '200', NULL, 1280.50, 5.00, TIMESTAMP '2026-09-21 14:15:00',
    'AZ21CORB00000000987654321099', '9900123451', 'LEGAL', 'UTIL-2026-09', 'AZ',
    '102', 'PIN', 12, 1002
),
-- Payment 3: Fərdi Müştəri (ACC-3001) - YPX Yol Hərəkəti Cəriməsi (PENDING)
(
    3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9007199254740103, 'YPX Yol Hərəkəti Qaydaları Cəriməsi',
    'PENDING', 'FINE', '102', NULL, 50.00, 0.50, TIMESTAMP '2026-09-22 09:00:00',
    'AZ21CORB00000000554433221100', '5544332211', 'PHYSICAL', 'FINE-2026-88', 'AZ',
    '103', 'SERIAL', 15, 1003
),
-- Payment 4: Korporativ Müştəri (ACC-2001) - Gömrük Rüsumu (REJECTED)
(
    4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9007199254740104, 'Gömrük Bəyannaməsi üzrə Ödəniş',
    'REJECTED', 'CUSTOMS', '400', 'Hesabda kikifayət qədər vəsait yoxdur', 15000.00, 25.00, TIMESTAMP '2026-09-23 16:45:00',
    'AZ21CORB00000000987654321099', '9900123451', 'LEGAL', 'CUST-2026-55', 'AZ',
    '104', 'TIN', 20, 1004
);

-- -----------------------------------------------------------------------------
-- 2. gpp_payment_service_codes
-- -----------------------------------------------------------------------------
INSERT INTO gpp_payment_service_codes (payment_id, service_code) VALUES
                                                                     (1, 1001), (1, 1002),
                                                                     (2, 2001), (2, 2002), (2, 2003),
                                                                     (3, 3001),
                                                                     (4, 4001);

-- -----------------------------------------------------------------------------
-- 3. gpp_invoices
-- -----------------------------------------------------------------------------
INSERT INTO gpp_invoices (
    id, created_at, updated_at, external_invoice_id, invoice_code, invoice_type, state_code,
    receipt_number, fee_calculation_method, amount_due, commission_amount, current_debt,
    max_allowed_amount, min_allowed_amount, partial_payment_allowed,
    manual_payment_receiver_selection_required, manual_sp_branch_selection_required,
    service_code, service_description, sp_branch_code, sp_branch_description,
    payment_receiver_code, payment_receiver_description, payment_id
) VALUES
-- Invoice 1 (Payment 1 - Vergi)
(
    101, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INV-2026-001', 'INC-TAX-01', 'SINGLE', 'PAID',
    'REC-880011', 'PERCENTAGE', 350.00, 1.50, 0.00, 10000.00, 1.00, TRUE,
    FALSE, FALSE, 1001, 'Gəlir Vergisi Ödənişi', 10, 'Baku Main Branch',
    5001, 'Dövlət Vergi Xidməti', 1
),
-- Invoice 2 (Payment 2 - Azərişıq)
(
    102, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INV-2026-002', 'INC-UTIL-01', 'GROUP', 'PAID',
    'REC-880012', 'FIXED', 780.00, 3.00, 0.00, 5000.00, 5.00, TRUE,
    FALSE, FALSE, 2001, 'Elektrik Enerjisi Təchizatı', 20, 'Nəsimi Branch',
    5002, 'Azərişıq ASC', 2
),
-- Invoice 3 (Payment 2 - Azərsu)
(
    103, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INV-2026-003', 'INC-UTIL-02', 'GROUP', 'PAID',
    'REC-880013', 'FIXED', 500.50, 2.00, 0.00, 3000.00, 2.00, FALSE,
    FALSE, FALSE, 2002, 'Su Təchizatı və Tullantı suları', 20, 'Nəsimi Branch',
    5003, 'Azərsu ASC', 2
),
-- Invoice 4 (Payment 3 - YPX Cəriməsi)
(
    104, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INV-2026-004', 'INC-FINE-01', 'SINGLE', 'UNPAID',
    'REC-880014', 'FIXED', 50.00, 0.50, 50.00, 500.00, 10.00, FALSE,
    FALSE, FALSE, 3001, 'Sürət Həddinin Aşılması Cəriməsi', 30, 'Gənclik Branch',
    5004, 'Baş Dövlət Yol Polisi İdarəsi', 3
),
-- Invoice 5 (Payment 4 - Gömrük)
(
    105, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INV-2026-005', 'INC-CUST-01', 'SINGLE', 'CANCELLED',
    'REC-880015', 'PERCENTAGE', 15000.00, 25.00, 15000.00, 100000.00, 100.00, FALSE,
    TRUE, FALSE, 4001, 'İdxal Gömrük Rüsumu', 10, 'Baku Main Branch',
    5005, 'Dövlət Gömrük Komitəsi', 4
);

-- -----------------------------------------------------------------------------
-- 4. gpp_invoice_payment_receivers
-- -----------------------------------------------------------------------------
INSERT INTO gpp_invoice_payment_receivers (invoice_id, code, description) VALUES
                                                                              (101, 5001, 'Dövlət Vergi Xidməti'),
                                                                              (102, 5002, 'Azərişıq ASC'),
                                                                              (103, 5003, 'Azərsu ASC'),
                                                                              (104, 5004, 'Baş Dövlət Yol Polisi İdarəsi'),
                                                                              (105, 5005, 'Dövlət Gömrük Komitəsi');

-- -----------------------------------------------------------------------------
-- 5. gpp_child_invoices
-- -----------------------------------------------------------------------------
INSERT INTO gpp_child_invoices (
    id, created_at, updated_at, external_child_invoice_id, invoice_code, receipt_number,
    fee_calculation_method, amount, commission_amount, service_code, service_description,
    payment_receiver_code, payment_receiver_description, invoice_id
) VALUES
-- Child Invoice 1 (Invoice 101 için detay)
(
    1001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CHILD-INV-101-A', 'INC-TAX-01-1', 'REC-880011-1',
    'PERCENTAGE', 350.00, 1.50, 1001, 'Fiki Şəxslərin Gəlir Vergisi',
    5001, 'Dövlət Vergi Xidməti', 101
),
-- Child Invoices for Group Invoice 102 (Azərişıq alt faturası)
(
    1002, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CHILD-INV-102-A', 'INC-UTIL-01-1', 'REC-880012-1',
    'FIXED', 500.00, 2.00, 2001, 'Aktiv Elektrik Enerjisi',
    5002, 'Azərişıq ASC - Mərkəz', 102
),
(
    1003, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CHILD-INV-102-B', 'INC-UTIL-01-2', 'REC-880012-2',
    'FIXED', 280.00, 1.00, 2001, 'Reaktiv Güc Dəbbə Pulu',
    5002, 'Azərişıq ASC - Mərkəz', 102
),
-- Child Invoice 3 (Invoice 103 - Azərsu)
(
    1004, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CHILD-INV-103-A', 'INC-UTIL-02-1', 'REC-880013-1',
    'FIXED', 500.50, 2.00, 2002, 'İçməli Su İstehlakı',
    5003, 'Azərsu ASC', 103
),
-- Child Invoice 4 (Invoice 104 - YPX Cəriməsi)
(
    1005, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CHILD-INV-104-A', 'INC-FINE-01-1', 'REC-880014-1',
    'FIXED', 50.00, 0.50, 3001, 'Protokol № YPX-99228',
    5004, 'Baş Dövlət Yol Polisi İdarəsi', 104
);

-- insert-data-gpp.sql faylının sonuna əlavə edin:

SELECT setval(pg_get_serial_sequence('gpp_payments', 'id'), MAX(id)) FROM gpp_payments;
SELECT setval(pg_get_serial_sequence('gpp_invoices', 'id'), MAX(id)) FROM gpp_invoices;
SELECT setval(pg_get_serial_sequence('gpp_child_invoices', 'id'), MAX(id)) FROM gpp_child_invoices;