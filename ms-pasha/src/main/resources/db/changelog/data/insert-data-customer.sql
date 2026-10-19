INSERT INTO customers (
    created_at, updated_at, customer_no, customer_type,
    pin, name, surname, tin, company_name, mobile_number
) VALUES
      (
                  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                  'CUST-410001',
                  'INDIVIDUAL',
                  '5SV1A23',
                  'Subhan',
                  'Vəlili',
                  '1400110011',
                  NULL,
                  '+994501110001'
      ),
      (
                  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                  'CUST-410002',
                  'INDIVIDUAL',
                  '6RK2B34',
                  'Rasul',
                  'Kərimov',
                  '1400220022',
                  NULL,
                  '+994501110002'
      ),
      (
                  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                  'CUST-410003',
                  'INDIVIDUAL',
                  '7FA3C45',
                  'Fərid',
                  'Abbasov',
                  '1400330033',
                  NULL,
                  '+994501110003'
      ),
      (
                  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                  'CUST-410004',
                  'INDIVIDUAL',
                  '8IY4D56',
                  'İmran',
                  'Yusubov',
                  '1400440044',
                  NULL,
                  '+994501110004'
      );