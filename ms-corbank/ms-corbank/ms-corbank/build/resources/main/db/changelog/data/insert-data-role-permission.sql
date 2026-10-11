-- 1. PERMISSIONS
INSERT INTO permissions (name, created_at)
VALUES ('USER_READ', CURRENT_TIMESTAMP),
       ('USER_WRITE', CURRENT_TIMESTAMP),
       ('USER_DELETE', CURRENT_TIMESTAMP),
       ('ACCOUNT_READ', CURRENT_TIMESTAMP),
       ('ACCOUNT_WRITE', CURRENT_TIMESTAMP),
       ('ACCOUNT_CLOSE', CURRENT_TIMESTAMP),
       ('TRANSFER_READ', CURRENT_TIMESTAMP),
       ('TRANSFER_CREATE', CURRENT_TIMESTAMP),
       ('TRANSFER_APPROVE', CURRENT_TIMESTAMP),
       ('CARD_READ', CURRENT_TIMESTAMP),
       ('CARD_WRITE', CURRENT_TIMESTAMP),
       ('REPORT_READ', CURRENT_TIMESTAMP);

-- 2. ROLES
INSERT INTO roles (name, created_at)
VALUES ('ROLE_ADMIN', CURRENT_TIMESTAMP),
       ('ROLE_OPERATOR', CURRENT_TIMESTAMP),
       ('ROLE_USER', CURRENT_TIMESTAMP),
       ('ROLE_AUDITOR', CURRENT_TIMESTAMP);

-- 3. ROLES_PERMISSIONS MAPPING
-- ROLE_ADMIN (id: 1) -> Bütün icazələr (1-12)
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12);

-- ROLE_OPERATOR (id: 2) -> Bank əməliyyatçıları (Hesab, Köçürmə təsdiqi, Kart və Hesabatlar)
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (2, 4), -- ACCOUNT_READ
       (2, 5), -- ACCOUNT_WRITE
       (2, 7), -- TRANSFER_READ
       (2, 8), -- TRANSFER_CREATE
       (2, 9), -- TRANSFER_APPROVE
       (2, 10),-- CARD_READ
       (2, 11),-- CARD_WRITE
       (2, 12);
-- REPORT_READ

-- ROLE_USER (id: 3) -> Korporativ müştəri (Baxış, Köçürmə yaratma, Kart baxışı)
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (3, 4), -- ACCOUNT_READ
       (3, 7), -- TRANSFER_READ
       (3, 8), -- TRANSFER_CREATE
       (3, 10);
-- CARD_READ

-- ROLE_AUDITOR (id: 4) -> Monitorinq və Audit (Yalnız oxuma icazələri)
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (4, 1), -- USER_READ
       (4, 4), -- ACCOUNT_READ
       (4, 7), -- TRANSFER_READ
       (4, 10),-- CARD_READ
       (4, 12);-- REPORT_READ