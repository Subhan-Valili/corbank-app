-- =============================================================================
-- B2B BULK PAYMENTS MOCK DATA SCRIPT (insert-data-b2b.sql)
-- =============================================================================

-- 1. DATA POPULATION (b2b_bulk_payments)
INSERT INTO b2b_bulk_payments (
    id, created_at, updated_at, bulk_id, bulk_description, record_count, status
) VALUES
      (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800101, 'Sentyabr ayı üzrə toplu əməkhaqqı ödənişi', 2, 'COMPLETED'),
      (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800102, 'Təchizatçı və xidmət göstərənlərə toplu ödəniş', 2, 'PENDING'),
      (3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800103, 'Ofis avadanlığı və icarə xərcləri ödənişi', 1, 'FAILED'),
      (4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800104, '3-cü rüb üzrə bonus və mükafat ödənişləri', 3, 'COMPLETED'),
      (5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800105, 'Konsaltinq və İT infrastrukturu xidmət haqqı', 2, 'PROCESSING'),
      (6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800106, 'Ofis təchizatı və kuryer xidmətləri ödənişi', 3, 'COMPLETED'),
      (7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800107, 'Rəqəmsal marketinq və PR kampaniya ödənişləri', 2, 'CANCELLED'),
      (8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 800108, 'İşçilərin korporativ tibbi sığorta primləri', 2, 'COMPLETED');

-- 2. DATA POPULATION (b2b_payment_items)
INSERT INTO b2b_payment_items (
    id, created_at, updated_at, type, amount, commission_account, description,
    file_required, urgent, reference_number, payer_account_number,
    payee_account_number, payee_name, payee_tin, payee_type, payee_email,
    payee_address, payee_additional_info, payee_bank_code, payee_bank_name, bulk_payment_id
) VALUES
      (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SALARY', 2500.00, 'AZ21CORB00000000987654321099', 'Sentyabr ayı əməkhaqqı', false, false, 'REF-B2B-001', 'AZ21CORB00000000987654321099', 'AZ21CORB00000000123456789012', 'Məmmədov Əli', '1234567891', 'INDIVIDUAL', 'ali.mammadov@example.com', 'Bakı şəh., Nəsimi r-nu', 'Əmək haqqı kartına mədaxil', 'PASHAAZ22', 'PAŞA Bank ASC', 1),
-- ... (digər INSERT sətirləri)
      (17, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INSURANCE', 7800.00, 'AZ21CORB00000000987654321099', 'İstehsalatda bədbəxt hadisələrdən icbari sığorta', true, false, 'REF-B2B-017', 'AZ21CORB00000000987654321099', 'AZ21CORB00000000990011223355', 'PAŞA Həyat Sığorta ASC', '9900002222', 'COMPANY', 'life@pashalife.az', 'Bakı şəh., Nizami küç. 11', 'İcbari sığorta polisi № PL-9901', 'PASHAAZ22', 'PAŞA Bank ASC', 8);

-- 3. SEQUENCE ALIGNMENT
SELECT setval(pg_get_serial_sequence('b2b_bulk_payments', 'id'), MAX(id)) FROM b2b_bulk_payments;
SELECT setval(pg_get_serial_sequence('b2b_payment_items', 'id'), MAX(id)) FROM b2b_payment_items;