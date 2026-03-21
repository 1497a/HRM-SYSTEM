USE hrm_db;

CREATE TABLE IF NOT EXISTS LICHSU_HESOLUONG (
    maLichSu INT AUTO_INCREMENT PRIMARY KEY,
    maChucVu VARCHAR(20) NOT NULL,
    heSoLuongCu DECIMAL(10,2) DEFAULT 0,
    heSoLuongMoi DECIMAL(10,2) DEFAULT 0,
    phuCapCu DECIMAL(15,2) DEFAULT 0,
    phuCapMoi DECIMAL(15,2) DEFAULT 0,
    ngayThayDoi DATETIME DEFAULT CURRENT_TIMESTAMP,
    nguoiThayDoi VARCHAR(50),
    FOREIGN KEY (maChucVu) REFERENCES CHUCVU(maChucVu) ON DELETE CASCADE,
    INDEX idx_lshs_chucvu_ngay (maChucVu, ngayThayDoi)
) ENGINE=InnoDB;

-- Nếu bảng đã tồn tại từ bản cũ (chưa có cột nguoiThayDoi) thì tự bổ sung.
SET @hasNguoiCol := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'LICHSU_HESOLUONG'
      AND COLUMN_NAME = 'nguoiThayDoi'
);

SET @sqlAddCol := IF(
    @hasNguoiCol = 0,
    'ALTER TABLE LICHSU_HESOLUONG ADD COLUMN nguoiThayDoi VARCHAR(50) NULL AFTER ngayThayDoi',
    'SELECT 1'
);
PREPARE stmt FROM @sqlAddCol;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Seed dữ liệu mẫu cho lịch sử thay đổi chức vụ.
-- Tạo nhiều bản ghi cho nhiều chức vụ, ngày ngẫu nhiên trong khoảng 01/03/2026 -> 19/03/2026.
-- Chỉ seed 1 lần để tránh chèn trùng khi chạy lại migration.
WITH pos AS (
    SELECT maChucVu, capBac, phuCapChucVu
    FROM CHUCVU
    ORDER BY maChucVu
    LIMIT 6
),
step AS (
    SELECT 1 AS lan
    UNION ALL SELECT 2
    UNION ALL SELECT 3
)
INSERT INTO LICHSU_HESOLUONG
    (maChucVu, heSoLuongCu, heSoLuongMoi, phuCapCu, phuCapMoi, ngayThayDoi, nguoiThayDoi)
SELECT
    p.maChucVu,
    GREATEST(p.capBac + s.lan, 1) AS heSoLuongCu,
    p.capBac AS heSoLuongMoi,
    GREATEST(p.phuCapChucVu - (s.lan * 250000), 0) AS phuCapCu,
    p.phuCapChucVu + (s.lan * 150000) AS phuCapMoi,
    TIMESTAMP(
        DATE_ADD('2026-03-01', INTERVAL FLOOR(RAND() * 19) DAY),
        SEC_TO_TIME(28800 + FLOOR(RAND() * 32400))
    ) AS ngayThayDoi,
    CONCAT('seed_migration_', s.lan) AS nguoiThayDoi
FROM pos p
CROSS JOIN step s
WHERE NOT EXISTS (
    SELECT 1
    FROM LICHSU_HESOLUONG h
    WHERE h.nguoiThayDoi LIKE 'seed_migration_%'
);

-- Kiểm tra nhanh kết quả seed.
SELECT maLichSu, maChucVu, heSoLuongCu, heSoLuongMoi, phuCapCu, phuCapMoi, ngayThayDoi, nguoiThayDoi
FROM LICHSU_HESOLUONG
ORDER BY ngayThayDoi DESC, maLichSu DESC;
