-- Phase 0 rollback (only if the additive indexes cause a problem)
ALTER TABLE product DROP KEY idx_product_status_pid, DROP KEY idx_product_cid_status_pid;
ALTER TABLE member DROP KEY uk_member_phoneNum, DROP KEY uk_member_email;
ALTER TABLE admin DROP KEY uk_admin_username;
