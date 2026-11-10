-- Phase 0 additive DDL (no destructive change, no data migration)
-- DB: travel / MySQL 8.0.34
-- Executed: 2026-11-10
-- Rollback: see rollback_phase0_additive_ddl.sql

-- 1) admin: username must be unique (login key)
ALTER TABLE admin
    ADD UNIQUE KEY uk_admin_username (username),
    ALGORITHM = INPLACE, LOCK = NONE;

-- 2) member: prevent concurrent duplicate registration
ALTER TABLE member
    ADD UNIQUE KEY uk_member_phoneNum (phoneNum),
    ADD UNIQUE KEY uk_member_email (email),
    ALGORITHM = INPLACE, LOCK = NONE;

-- 3) product: frontdesk list query WHERE status=1 [AND cid=?] ORDER BY pid DESC
--    split into two indexes so both list variants can use index ordering
ALTER TABLE product
    ADD KEY idx_product_status_pid (status, pid),
    ADD KEY idx_product_cid_status_pid (cid, status, pid),
    ALGORITHM = INPLACE, LOCK = NONE;
