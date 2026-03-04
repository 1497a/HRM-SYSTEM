-- Migration: add candidate status after onboarding to employee
-- Apply on existing databases (MySQL 8+).
ALTER TABLE UNGVIEN
MODIFY COLUMN trangThai ENUM('moi', 'dang_phong_van', 'trung_tuyen', 'da_chuyen_nhan_vien', 'tu_choi')
DEFAULT 'moi';

