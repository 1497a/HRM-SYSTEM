-- =====================================================
-- HRM SAMPLE DATA - Du lieu mau day du
-- Chay sau hrm_database.sql
-- =====================================================
USE hrm_db;

-- =====================================================
-- 1. NHANVIEN (15 nhan vien mau)
-- =====================================================
INSERT INTO NHANVIEN (maNhanVien, loaiHopDong, ngayVaoLam, trangThai, ghiChu) VALUES
('NV001', 'khong_xac_dinh',   '2020-01-15', 'dang_lam_viec', 'Nhan vien lau nam'),
('NV002', 'khong_xac_dinh',   '2020-03-01', 'dang_lam_viec', NULL),
('NV003', 'xac_dinh_thoi_han','2021-06-01', 'dang_lam_viec', NULL),
('NV004', 'khong_xac_dinh',   '2019-08-15', 'dang_lam_viec', NULL),
('NV005', 'xac_dinh_thoi_han','2022-01-10', 'dang_lam_viec', NULL),
('NV006', 'xac_dinh_thoi_han','2022-04-01', 'dang_lam_viec', NULL),
('NV007', 'khong_xac_dinh',   '2018-11-20', 'dang_lam_viec', 'Truong phong IT'),
('NV008', 'xac_dinh_thoi_han','2023-02-01', 'dang_lam_viec', NULL),
('NV009', 'thu_viec',         '2025-11-01', 'dang_lam_viec', 'Dang thu viec'),
('NV010', 'xac_dinh_thoi_han','2021-09-15', 'dang_lam_viec', NULL),
('NV011', 'khong_xac_dinh',   '2020-07-01', 'dang_lam_viec', NULL),
('NV012', 'xac_dinh_thoi_han','2022-10-01', 'dang_lam_viec', NULL),
('NV013', 'xac_dinh_thoi_han','2023-05-15', 'dang_lam_viec', NULL),
('NV014', 'thu_viec',         '2025-12-01', 'dang_lam_viec', 'Dang thu viec'),
('NV015', 'xac_dinh_thoi_han','2021-03-01', 'tam_nghi',      'Dang nghi thai san');

-- =====================================================
-- 2. THONGTINCANHAN
-- =====================================================
INSERT INTO THONGTINCANHAN (maNV, hoTen, ngaySinh, gioiTinh, cccd, dienThoai, email, diaChi, diaChiThuongTru, queQuan, danToc, tonGiao, tinhTrangHonNhan) VALUES
(1,  'Nguyen Van An',    '1985-04-20', 'nam', '001085004201', '0901234501', 'an.nguyen@abc.com',     '12 Le Loi, Q1, TP.HCM',          '12 Le Loi, Q1, TP.HCM',          'Ha Noi',      'Kinh', 'Khong', 'da_ket_hon'),
(2,  'Tran Thi Binh',    '1988-09-15', 'nu',  '079088009151', '0901234502', 'binh.tran@abc.com',     '45 Hai Ba Trung, Q3, TP.HCM',    '45 Hai Ba Trung, Q3, TP.HCM',    'TP.HCM',     'Kinh', 'Khong', 'da_ket_hon'),
(3,  'Le Minh Cuong',    '1990-12-05', 'nam', '079090012051', '0901234503', 'cuong.le@abc.com',      '78 Dinh Tien Hoang, BT, TP.HCM', '78 Dinh Tien Hoang, BT, TP.HCM', 'Da Nang',    'Kinh', 'Khong', 'da_ket_hon'),
(4,  'Pham Thi Dung',    '1982-07-30', 'nu',  '079082007301', '0901234504', 'dung.pham@abc.com',     '23 Nguyen Hue, Q1, TP.HCM',      '23 Nguyen Hue, Q1, TP.HCM',      'Can Tho',    'Kinh', 'Khong', 'da_ket_hon'),
(5,  'Hoang Van Em',     '1993-03-18', 'nam', '079093003181', '0901234505', 'em.hoang@abc.com',      '56 Phan Dinh Phung, PN, TP.HCM', '56 Phan Dinh Phung, PN, TP.HCM', 'Nghe An',    'Kinh', 'Khong', 'doc_than'),
(6,  'Ngo Thi Phuong',   '1991-11-22', 'nu',  '079091011221', '0901234506', 'phuong.ngo@abc.com',    '34 Ly Tu Trong, Q1, TP.HCM',     '34 Ly Tu Trong, Q1, TP.HCM',     'Binh Dinh',  'Kinh', 'Khong', 'da_ket_hon'),
(7,  'Vu Thanh Giang',   '1980-06-10', 'nam', '079080006101', '0901234507', 'giang.vu@abc.com',      '90 CMT8, Q3, TP.HCM',            '90 CMT8, Q3, TP.HCM',            'Hai Phong',  'Kinh', 'Khong', 'da_ket_hon'),
(8,  'Dang Thi Hoa',     '1995-02-14', 'nu',  '079095002141', '0901234508', 'hoa.dang@abc.com',      '67 Tran Hung Dao, Q5, TP.HCM',   '67 Tran Hung Dao, Q5, TP.HCM',   'TP.HCM',     'Kinh', 'Khong', 'doc_than'),
(9,  'Bui Quoc Hung',    '1998-08-25', 'nam', '079098008251', '0901234509', 'hung.bui@abc.com',      '15 Vo Van Tan, Q3, TP.HCM',      '15 Vo Van Tan, Q3, TP.HCM',      'Ha Noi',     'Kinh', 'Khong', 'doc_than'),
(10, 'Trinh Thi Kim',    '1990-05-07', 'nu',  '079090005071', '0901234510', 'kim.trinh@abc.com',     '88 Nguyen Trai, Q5, TP.HCM',     '88 Nguyen Trai, Q5, TP.HCM',     'Hue',        'Kinh', 'Khong', 'da_ket_hon'),
(11, 'Dinh Van Long',    '1987-01-19', 'nam', '079087001191', '0901234511', 'long.dinh@abc.com',     '22 Hoang Dieu, Q4, TP.HCM',      '22 Hoang Dieu, Q4, TP.HCM',      'Quang Nam',  'Kinh', 'Khong', 'da_ket_hon'),
(12, 'Phan Thi Mai',     '1994-10-03', 'nu',  '079094010031', '0901234512', 'mai.phan@abc.com',      '11 Bui Vien, Q1, TP.HCM',        '11 Bui Vien, Q1, TP.HCM',        'Vinh Long',  'Kinh', 'Khong', 'doc_than'),
(13, 'Ly Van Nhan',      '1996-07-28', 'nam', '079096007281', '0901234513', 'nhan.ly@abc.com',       '33 Pasteur, Q1, TP.HCM',         '33 Pasteur, Q1, TP.HCM',         'Tien Giang', 'Kinh', 'Khong', 'doc_than'),
(14, 'Cao Thi Oanh',     '2000-04-15', 'nu',  '079100004151', '0901234514', 'oanh.cao@abc.com',      '77 Nam Ky Khoi Nghia, Q3',       '77 Nam Ky Khoi Nghia, Q3',       'TP.HCM',     'Kinh', 'Khong', 'doc_than'),
(15, 'Nguyen Thi Phuc',  '1992-12-20', 'nu',  '079092012201', '0901234515', 'phuc.nguyen@abc.com',   '99 Le Van Sy, Q3, TP.HCM',       '99 Le Van Sy, Q3, TP.HCM',       'Ben Tre',    'Kinh', 'Khong', 'da_ket_hon');

-- =====================================================
-- 3. BONHIEM
-- =====================================================
INSERT INTO BONHIEM (maNV, maPhongBan, maChucVu, loaiBoNhiem, tyLeHuongLuong, maQuanLy, tuNgay, trangThai, lyDo) VALUES
(4,  'CONGTY',   'GD',  'chinh', 100.00, NULL, '2019-08-15', 'hieu_luc', 'Bo nhiem Giam doc'),
(1,  'PHONGNS',  'TP',  'chinh', 100.00, 4,    '2020-01-15', 'hieu_luc', 'Bo nhiem Truong phong Nhan su'),
(2,  'PHONGNS',  'PP',  'chinh', 100.00, 1,    '2020-03-01', 'hieu_luc', 'Bo nhiem Pho phong Nhan su'),
(3,  'PHONGNS',  'NV',  'chinh', 100.00, 1,    '2021-06-01', 'hieu_luc', 'Nhan vien Nhan su'),
(5,  'PHONGNS',  'NV',  'chinh', 100.00, 1,    '2022-01-10', 'hieu_luc', 'Nhan vien Nhan su'),
(6,  'PHONGNS',  'NV',  'chinh', 100.00, 1,    '2022-04-01', 'hieu_luc', 'Nhan vien Nhan su'),
(15, 'PHONGNS',  'NV',  'chinh', 100.00, 1,    '2021-03-01', 'hieu_luc', 'Nhan vien Nhan su'),
(7,  'PHONGIT',  'TP',  'chinh', 100.00, 4,    '2018-11-20', 'hieu_luc', 'Bo nhiem Truong phong IT'),
(8,  'PHONGIT',  'NV',  'chinh', 100.00, 7,    '2023-02-01', 'hieu_luc', 'Nhan vien IT'),
(9,  'PHONGIT',  'NV',  'chinh', 100.00, 7,    '2025-11-01', 'hieu_luc', 'Nhan vien IT thu viec'),
(10, 'PHONGIT',  'NV',  'chinh', 100.00, 7,    '2021-09-15', 'hieu_luc', 'Nhan vien IT'),
(11, 'PHONGKT',  'TP',  'chinh', 100.00, 4,    '2020-07-01', 'hieu_luc', 'Truong phong Ke toan'),
(12, 'PHONGKT',  'NV',  'chinh', 100.00, 11,   '2022-10-01', 'hieu_luc', 'Nhan vien Ke toan'),
(13, 'PHONGKD',  'NV',  'chinh', 100.00, 4,    '2023-05-15', 'hieu_luc', 'Nhan vien Kinh doanh'),
(14, 'PHONGKD',  'NV',  'chinh', 100.00, 4,    '2025-12-01', 'hieu_luc', 'Nhan vien Kinh doanh thu viec');

-- =====================================================
-- 4. VAITRO & QUYEN (phai insert truoc TAIKHOAN vi co FK)
-- =====================================================
INSERT IGNORE INTO VAITRO (maVaiTro, tenVaiTro, moTa, laVaiTroHeThong, trangThai) VALUES
('ADMIN',    'Quan tri vien',        'Toan quyen tren he thong',              TRUE,  'hoatDong'),
('HR',       'Nhan su',              'Quan ly nhan vien, tuyen dung, phep',   TRUE,  'hoatDong'),
('MANAGER',  'Quan ly',              'Truong/pho phong, quan ly nhom',        TRUE,  'hoatDong'),
('EMPLOYEE', 'Nhan vien',            'Nhan vien thong thuong',                TRUE,  'hoatDong'),
('DIRECTOR', 'Giam doc',             'Giam doc / Ban lanh dao',               FALSE, 'hoatDong');

INSERT IGNORE INTO QUYEN (maQuyen, tenQuyen, nhomQuyen) VALUES
-- Nhan vien
('EMPLOYEE_VIEW',      'Xem hồ sơ nhân viên',         'Nhân sự'),
('EMPLOYEE_CREATE',    'Tạo nhân viên mới',            'Nhân sự'),
('EMPLOYEE_UPDATE',    'Cập nhật hồ sơ nhân viên',     'Nhân sự'),
('VIEW_SELF',          'Xem thông tin cá nhân',        'Nhân sự'),
-- Phong ban & Chuc vu
('DEPARTMENT_VIEW',    'Xem phòng ban',                'Cơ cấu tổ chức'),
('DEPARTMENT_CREATE',  'Thêm phòng ban',               'Cơ cấu tổ chức'),
('DEPARTMENT_EDIT',    'Sửa phòng ban',                'Cơ cấu tổ chức'),
('DEPARTMENT_HEAD',    'Trưởng phòng',                 'Cơ cấu tổ chức'),
('POSITION_VIEW',      'Xem chức vụ',                  'Cơ cấu tổ chức'),
('POSITION_CREATE',    'Thêm chức vụ',                 'Cơ cấu tổ chức'),
('POSITION_EDIT',      'Sửa chức vụ',                  'Cơ cấu tổ chức'),
-- Bo nhiem
('APPOINTMENT_VIEW',   'Xem bổ nhiệm',                 'Bổ nhiệm'),
('APPOINTMENT_CREATE', 'Tạo bổ nhiệm',                 'Bổ nhiệm'),
('APPOINTMENT_APPROVE','Phê duyệt bổ nhiệm',           'Bổ nhiệm'),
-- Cham cong
('ATTENDANCE_VIEW',    'Xem chấm công',                'Chấm công'),
-- Hop dong
('CONTRACT_VIEW',      'Xem hợp đồng',                 'Hợp đồng'),
('CONTRACT_CREATE',    'Tạo hợp đồng',                 'Hợp đồng'),
('CONTRACT_UPDATE',    'Cập nhật hợp đồng',            'Hợp đồng'),
-- Luong
('PAYROLL_VIEW',       'Xem bảng lương',               'Quản lý lương'),
-- Nghi phep
('LEAVE_VIEW_ALL',     'Xem tất cả đơn nghỉ phép',    'Nghỉ phép'),
('LEAVE_VIEW_SELF',    'Xem đơn nghỉ phép cá nhân',   'Nghỉ phép'),
('LEAVE_CREATE',       'Tạo đơn nghỉ phép',            'Nghỉ phép'),
('LEAVE_APPROVE',      'Duyệt đơn nghỉ phép',          'Nghỉ phép'),
-- Danh gia
('EVAL_VIEW_ALL',      'Xem tất cả đánh giá',         'Đánh giá'),
('EVAL_VIEW_SELF',     'Xem đánh giá cá nhân',        'Đánh giá'),
('EVAL_MANAGE',        'Quản lý đợt đánh giá',         'Đánh giá'),
('EVAL_REVIEW',        'Thực hiện đánh giá',           'Đánh giá'),
-- Tuyen dung
('RECRUITMENT_VIEW',   'Xem tuyển dụng',               'Tuyển dụng'),
-- Bao cao
('REPORT_VIEW',        'Xem báo cáo',                  'Báo cáo'),
-- Thong bao
('NOTIFICATION_SEND',  'Gửi thông báo',                'Thông báo'),
-- Quan tri
('USER_VIEW',          'Xem tài khoản',                'Quản trị hệ thống'),
('USER_CREATE',        'Tạo tài khoản',                'Quản trị hệ thống'),
('USER_UPDATE',        'Cập nhật tài khoản',           'Quản trị hệ thống'),
('USER_DELETE',        'Xóa tài khoản',                'Quản trị hệ thống'),
('ROLE_VIEW',          'Xem vai trò',                  'Quản trị hệ thống'),
('ROLE_CREATE',        'Tạo vai trò',                  'Quản trị hệ thống'),
('ROLE_UPDATE',        'Cập nhật vai trò',             'Quản trị hệ thống'),
('ROLE_DELETE',        'Xóa vai trò',                  'Quản trị hệ thống'),
('SETTINGS_VIEW',      'Xem cài đặt',                  'Quản trị hệ thống');

-- Quyen cua ADMIN: tat ca
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'ADMIN', maQuyen FROM QUYEN;

-- Quyen cua HR
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('HR', 'EMPLOYEE_VIEW'), ('HR', 'EMPLOYEE_CREATE'), ('HR', 'EMPLOYEE_UPDATE'),
('HR', 'DEPARTMENT_VIEW'), ('HR', 'POSITION_VIEW'),
('HR', 'APPOINTMENT_VIEW'), ('HR', 'APPOINTMENT_CREATE'), ('HR', 'APPOINTMENT_APPROVE'),
('HR', 'ATTENDANCE_VIEW'),
('HR', 'CONTRACT_VIEW'), ('HR', 'CONTRACT_CREATE'), ('HR', 'CONTRACT_UPDATE'),
('HR', 'PAYROLL_VIEW'),
('HR', 'LEAVE_VIEW_ALL'), ('HR', 'LEAVE_APPROVE'),
('HR', 'RECRUITMENT_VIEW'),
('HR', 'REPORT_VIEW'),
('HR', 'NOTIFICATION_SEND'),
('HR', 'USER_VIEW'), ('HR', 'USER_CREATE'), ('HR', 'USER_UPDATE'),
('HR', 'ROLE_VIEW'),
('HR', 'VIEW_SELF'), ('HR', 'LEAVE_CREATE'), ('HR', 'LEAVE_VIEW_SELF'),
('HR', 'EVAL_VIEW_ALL'), ('HR', 'EVAL_MANAGE'), ('HR', 'EVAL_REVIEW');

-- Quyen cua MANAGER
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('MANAGER', 'EMPLOYEE_VIEW'),
('MANAGER', 'DEPARTMENT_VIEW'), ('MANAGER', 'DEPARTMENT_HEAD'),
('MANAGER', 'APPOINTMENT_VIEW'),
('MANAGER', 'ATTENDANCE_VIEW'),
('MANAGER', 'CONTRACT_VIEW'),
('MANAGER', 'PAYROLL_VIEW'),
('MANAGER', 'LEAVE_VIEW_ALL'), ('MANAGER', 'LEAVE_APPROVE'),
('MANAGER', 'EVAL_VIEW_ALL'), ('MANAGER', 'EVAL_REVIEW'),
('MANAGER', 'REPORT_VIEW'),
('MANAGER', 'RECRUITMENT_VIEW'),
('MANAGER', 'VIEW_SELF'), ('MANAGER', 'LEAVE_CREATE'), ('MANAGER', 'LEAVE_VIEW_SELF');

-- Quyen cua EMPLOYEE
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('EMPLOYEE', 'VIEW_SELF'),
('EMPLOYEE', 'ATTENDANCE_VIEW'),
('EMPLOYEE', 'LEAVE_VIEW_SELF'), ('EMPLOYEE', 'LEAVE_CREATE'),
('EMPLOYEE', 'EVAL_VIEW_SELF');

-- Quyen cua DIRECTOR
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('DIRECTOR', 'EMPLOYEE_VIEW'),
('DIRECTOR', 'DEPARTMENT_VIEW'), ('DIRECTOR', 'POSITION_VIEW'),
('DIRECTOR', 'APPOINTMENT_VIEW'), ('DIRECTOR', 'APPOINTMENT_APPROVE'),
('DIRECTOR', 'CONTRACT_VIEW'), ('DIRECTOR', 'PAYROLL_VIEW'),
('DIRECTOR', 'LEAVE_VIEW_ALL'), ('DIRECTOR', 'LEAVE_APPROVE'),
('DIRECTOR', 'EVAL_VIEW_ALL'), ('DIRECTOR', 'EVAL_MANAGE'),
('DIRECTOR', 'REPORT_VIEW'), ('DIRECTOR', 'RECRUITMENT_VIEW'),
('DIRECTOR', 'USER_VIEW'), ('DIRECTOR', 'ROLE_VIEW'),
('DIRECTOR', 'VIEW_SELF'), ('DIRECTOR', 'LEAVE_CREATE'), ('DIRECTOR', 'LEAVE_VIEW_SELF');

-- =====================================================
-- 5. TAIKHOAN
-- =====================================================
INSERT INTO TAIKHOAN (tenDangNhap, matKhau, maNV, maVaiTro, email, hoatDong) VALUES
('an.nguyen',   'Password@123', 1,  'HR',       'an.nguyen@abc.com',   TRUE),
('binh.tran',   'Password@123', 2,  'HR',       'binh.tran@abc.com',   TRUE),
('cuong.le',    'Password@123', 3,  'EMPLOYEE', 'cuong.le@abc.com',    TRUE),
('dung.pham',   'Password@123', 4,  'ADMIN',    'dung.pham@abc.com',   TRUE),
('em.hoang',    'Password@123', 5,  'EMPLOYEE', 'em.hoang@abc.com',    TRUE),
('phuong.ngo',  'Password@123', 6,  'EMPLOYEE', 'phuong.ngo@abc.com',  TRUE),
('giang.vu',    'Password@123', 7,  'MANAGER',  'giang.vu@abc.com',    TRUE),
('hoa.dang',    'Password@123', 8,  'EMPLOYEE', 'hoa.dang@abc.com',    TRUE),
('hung.bui',    'Password@123', 9,  'EMPLOYEE', 'hung.bui@abc.com',    TRUE),
('kim.trinh',   'Password@123', 10, 'EMPLOYEE', 'kim.trinh@abc.com',   TRUE),
('long.dinh',   'Password@123', 11, 'MANAGER',  'long.dinh@abc.com',   TRUE),
('mai.phan',    'Password@123', 12, 'EMPLOYEE', 'mai.phan@abc.com',    TRUE),
('nhan.ly',     'Password@123', 13, 'EMPLOYEE', 'nhan.ly@abc.com',     TRUE),
('oanh.cao',    'Password@123', 14, 'EMPLOYEE', 'oanh.cao@abc.com',    TRUE),
('phuc.nguyen', 'Password@123', 15, 'EMPLOYEE', 'phuc.nguyen@abc.com', FALSE);

-- =====================================================
-- 5. HOPDONGLAODONG
-- =====================================================
INSERT INTO HOPDONGLAODONG (soHopDong, maNV, loaiHopDong, luongCoSo, ngayKy, ngayHieuLuc, ngayHetHieuLuc, trangThai) VALUES
('HD2020-001', 1,  'khong_xac_dinh',    15000000, '2020-01-10', '2020-01-15', NULL,         'hieu_luc'),
('HD2020-002', 2,  'khong_xac_dinh',    12000000, '2020-02-25', '2020-03-01', NULL,         'hieu_luc'),
('HD2021-001', 3,  'xac_dinh_thoi_han',  9000000, '2021-05-25', '2021-06-01', '2023-06-01', 'het_han'),
('HD2023-001', 3,  'xac_dinh_thoi_han', 11000000, '2023-06-01', '2023-06-01', '2025-06-01', 'het_han'),
('HD2025-001', 3,  'xac_dinh_thoi_han', 12000000, '2025-06-01', '2025-06-01', '2027-06-01', 'hieu_luc'),
('HD2019-001', 4,  'khong_xac_dinh',    50000000, '2019-08-10', '2019-08-15', NULL,         'hieu_luc'),
('HD2022-001', 5,  'xac_dinh_thoi_han',  8500000, '2022-01-05', '2022-01-10', '2024-01-10', 'het_han'),
('HD2024-001', 5,  'xac_dinh_thoi_han',  9500000, '2024-01-10', '2024-01-10', '2026-01-10', 'hieu_luc'),
('HD2022-002', 6,  'xac_dinh_thoi_han',  8000000, '2022-03-25', '2022-04-01', '2024-04-01', 'het_han'),
('HD2024-002', 6,  'xac_dinh_thoi_han',  9000000, '2024-04-01', '2024-04-01', '2026-04-01', 'hieu_luc'),
('HD2018-001', 7,  'khong_xac_dinh',    20000000, '2018-11-15', '2018-11-20', NULL,         'hieu_luc'),
('HD2023-002', 8,  'xac_dinh_thoi_han', 10000000, '2023-01-25', '2023-02-01', '2025-02-01', 'het_han'),
('HD2025-002', 8,  'xac_dinh_thoi_han', 11000000, '2025-02-01', '2025-02-01', '2027-02-01', 'hieu_luc'),
('HD2025-003', 9,  'thu_viec',           7000000, '2025-11-01', '2025-11-01', '2026-02-01', 'hieu_luc'),
('HD2021-002', 10, 'xac_dinh_thoi_han',  9000000, '2021-09-10', '2021-09-15', '2023-09-15', 'het_han'),
('HD2023-003', 10, 'xac_dinh_thoi_han', 10000000, '2023-09-15', '2023-09-15', '2025-09-15', 'het_han'),
('HD2025-004', 10, 'xac_dinh_thoi_han', 11500000, '2025-09-15', '2025-09-15', '2027-09-15', 'hieu_luc'),
('HD2020-003', 11, 'khong_xac_dinh',    18000000, '2020-06-25', '2020-07-01', NULL,         'hieu_luc'),
('HD2022-003', 12, 'xac_dinh_thoi_han',  8500000, '2022-09-25', '2022-10-01', '2024-10-01', 'het_han'),
('HD2024-003', 12, 'xac_dinh_thoi_han',  9500000, '2024-10-01', '2024-10-01', '2026-10-01', 'hieu_luc'),
('HD2023-004', 13, 'xac_dinh_thoi_han',  9000000, '2023-05-10', '2023-05-15', '2025-05-15', 'het_han'),
('HD2025-005', 13, 'xac_dinh_thoi_han', 10000000, '2025-05-15', '2025-05-15', '2027-05-15', 'hieu_luc'),
('HD2025-006', 14, 'thu_viec',           7000000, '2025-12-01', '2025-12-01', '2026-03-01', 'hieu_luc'),
('HD2021-003', 15, 'xac_dinh_thoi_han',  9000000, '2021-02-25', '2021-03-01', '2023-03-01', 'het_han'),
('HD2023-005', 15, 'xac_dinh_thoi_han', 10000000, '2023-03-01', '2023-03-01', '2025-03-01', 'het_han'),
('HD2025-007', 15, 'xac_dinh_thoi_han', 11000000, '2025-03-01', '2025-03-01', '2027-03-01', 'hieu_luc');

-- =====================================================
-- 6. CHAMCONG (Thang 2/2026)
-- =====================================================
INSERT INTO CHAMCONG (maNV, ngay, maCaLam, gioVao, gioRa, soGioLam, gioLamThem, trangThai, phuongThucChamCong) VALUES
(1,'2026-02-02','HANH_CHINH','2026-02-02 08:05:00','2026-02-02 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-03','HANH_CHINH','2026-02-03 07:58:00','2026-02-03 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-04','HANH_CHINH','2026-02-04 08:20:00','2026-02-04 17:00:00',7.67,0.00,'di_muon','the_tu'),
(1,'2026-02-05','HANH_CHINH','2026-02-05 08:00:00','2026-02-05 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-06','HANH_CHINH','2026-02-06 08:00:00','2026-02-06 19:00:00',8.00,2.00,'dung_gio','the_tu'),
(1,'2026-02-09','HANH_CHINH','2026-02-09 08:00:00','2026-02-09 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-10','HANH_CHINH','2026-02-10 08:00:00','2026-02-10 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-11',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong'),
(1,'2026-02-12','HANH_CHINH','2026-02-12 08:00:00','2026-02-12 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-13','HANH_CHINH','2026-02-13 08:00:00','2026-02-13 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-16','HANH_CHINH','2026-02-16 08:00:00','2026-02-16 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-17','HANH_CHINH','2026-02-17 08:00:00','2026-02-17 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-18','HANH_CHINH','2026-02-18 08:00:00','2026-02-18 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-19','HANH_CHINH','2026-02-19 08:30:00','2026-02-19 17:00:00',7.50,0.00,'di_muon','the_tu'),
(1,'2026-02-20','HANH_CHINH','2026-02-20 08:00:00','2026-02-20 19:30:00',8.00,2.50,'dung_gio','the_tu'),
(1,'2026-02-23','HANH_CHINH','2026-02-23 08:00:00','2026-02-23 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-24','HANH_CHINH','2026-02-24 08:00:00','2026-02-24 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-25','HANH_CHINH','2026-02-25 08:00:00','2026-02-25 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(1,'2026-02-26','HANH_CHINH','2026-02-26 08:00:00','2026-02-26 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(7,'2026-02-02','HANH_CHINH','2026-02-02 08:00:00','2026-02-02 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-03','HANH_CHINH','2026-02-03 08:00:00','2026-02-03 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-04','HANH_CHINH','2026-02-04 08:00:00','2026-02-04 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-05','HANH_CHINH','2026-02-05 08:00:00','2026-02-05 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-06','HANH_CHINH','2026-02-06 08:00:00','2026-02-06 20:00:00',8.00,3.00,'dung_gio','van_tay'),
(7,'2026-02-09','HANH_CHINH','2026-02-09 08:00:00','2026-02-09 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-10','HANH_CHINH','2026-02-10 08:00:00','2026-02-10 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-11','HANH_CHINH','2026-02-11 08:00:00','2026-02-11 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-12','HANH_CHINH','2026-02-12 08:00:00','2026-02-12 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-13','HANH_CHINH','2026-02-13 08:00:00','2026-02-13 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-16','HANH_CHINH','2026-02-16 08:00:00','2026-02-16 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-17','HANH_CHINH','2026-02-17 08:00:00','2026-02-17 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-18','HANH_CHINH','2026-02-18 08:00:00','2026-02-18 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-19','HANH_CHINH','2026-02-19 08:00:00','2026-02-19 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(7,'2026-02-20','HANH_CHINH','2026-02-20 08:00:00','2026-02-20 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-02','HANH_CHINH','2026-02-02 08:10:00','2026-02-02 17:00:00',7.83,0.00,'dung_gio','van_tay'),
(8,'2026-02-03','HANH_CHINH','2026-02-03 08:00:00','2026-02-03 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-04',NULL,NULL,NULL,0.00,0.00,'vang_mat','thu_cong'),
(8,'2026-02-05','HANH_CHINH','2026-02-05 08:00:00','2026-02-05 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-06','HANH_CHINH','2026-02-06 08:00:00','2026-02-06 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-09','HANH_CHINH','2026-02-09 08:00:00','2026-02-09 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-10','HANH_CHINH','2026-02-10 08:00:00','2026-02-10 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-11','HANH_CHINH','2026-02-11 08:00:00','2026-02-11 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-12','HANH_CHINH','2026-02-12 08:00:00','2026-02-12 17:00:00',8.00,0.00,'dung_gio','van_tay'),
(8,'2026-02-13','HANH_CHINH','2026-02-13 08:00:00','2026-02-13 17:30:00',8.00,1.50,'dung_gio','van_tay'),
(11,'2026-02-02','HANH_CHINH','2026-02-02 08:00:00','2026-02-02 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(11,'2026-02-03','HANH_CHINH','2026-02-03 08:00:00','2026-02-03 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(11,'2026-02-04','HANH_CHINH','2026-02-04 08:00:00','2026-02-04 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(11,'2026-02-05','HANH_CHINH','2026-02-05 08:00:00','2026-02-05 17:00:00',8.00,0.00,'dung_gio','the_tu'),
(11,'2026-02-06','HANH_CHINH','2026-02-06 08:00:00','2026-02-06 17:00:00',8.00,0.00,'dung_gio','the_tu');

-- =====================================================
-- 7. DANGKY_LAMTHEM
-- =====================================================
INSERT INTO DANGKY_LAMTHEM (maNV, ngay, soGio, lyDo, nguoiDuyet, ngayDuyet, trangThai) VALUES
(1, '2026-02-06', 2.0, 'Hoan thien bao cao nhan su thang 2', 4, '2026-02-05 16:00:00', 'da_duyet'),
(1, '2026-02-20', 2.5, 'Xu ly ho so nhan vien moi',          4, '2026-02-19 17:00:00', 'da_duyet'),
(7, '2026-02-06', 3.0, 'Deploy he thong len production',     4, '2026-02-05 17:00:00', 'da_duyet'),
(8, '2026-02-13', 1.5, 'Fix bug he thong HRM',               7, '2026-02-12 16:30:00', 'da_duyet'),
(5, '2026-02-27', 2.0, 'Ho tro phong van ung vien',          1, NULL,                  'cho_duyet'),
(13,'2026-02-28', 3.0, 'Gap khach hang ngoai gio',           4, NULL,                  'cho_duyet');

-- =====================================================
-- 8. SODUNGPHEP
-- =====================================================
INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) VALUES
(1, 2025, 'PHEP_NAM', 12, 8), (1, 2025, 'PHEP_OM', 0, 2), (1, 2026, 'PHEP_NAM', 12, 1),
(2, 2025, 'PHEP_NAM', 12, 5), (2, 2026, 'PHEP_NAM', 12, 0),
(3, 2025, 'PHEP_NAM', 12, 3), (3, 2026, 'PHEP_NAM', 12, 0),
(4, 2025, 'PHEP_NAM', 12, 10),(4, 2026, 'PHEP_NAM', 12, 0),
(5, 2025, 'PHEP_NAM', 12, 4), (5, 2026, 'PHEP_NAM', 12, 0),
(6, 2025, 'PHEP_NAM', 12, 6), (6, 2026, 'PHEP_NAM', 12, 0),
(7, 2025, 'PHEP_NAM', 12, 9), (7, 2026, 'PHEP_NAM', 12, 0),
(8, 2025, 'PHEP_NAM', 12, 2), (8, 2026, 'PHEP_NAM', 12, 0),
(9, 2025, 'PHEP_NAM', 3,  0), (9, 2026, 'PHEP_NAM', 3,  0),
(10,2025, 'PHEP_NAM', 12, 7), (10,2026, 'PHEP_NAM', 12, 0),
(11,2025, 'PHEP_NAM', 12, 11),(11,2026, 'PHEP_NAM', 12, 0),
(12,2025, 'PHEP_NAM', 12, 3), (12,2026, 'PHEP_NAM', 12, 0),
(13,2025, 'PHEP_NAM', 12, 5), (13,2026, 'PHEP_NAM', 12, 0),
(14,2025, 'PHEP_NAM', 3,  0), (14,2026, 'PHEP_NAM', 3,  0),
(15,2025, 'PHEP_NAM', 12, 12),(15,2025, 'PHEP_THAI_SAN', 180, 90);

-- =====================================================
-- 9. DONXINNGHIPHEP
-- =====================================================
INSERT INTO DONXINNGHIPHEP (maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi, lyDo, nguoiDuyet, ngayDuyet, trangThai) VALUES
(1,  'PHEP_NAM',       '2026-02-11', '2026-02-11', 1.0,   'Viec ca nhan',          1, '2026-02-09 10:00:00', 'da_duyet'),
(3,  'PHEP_NAM',       '2026-01-20', '2026-01-22', 3.0,   'Du lich gia dinh',      1, '2026-01-18 09:00:00', 'da_duyet'),
(5,  'PHEP_OM',        '2026-01-15', '2026-01-16', 2.0,   'Bi cam cum',            1, '2026-01-14 17:00:00', 'da_duyet'),
(8,  'PHEP_NAM',       '2026-03-10', '2026-03-12', 3.0,   'Viec gia dinh',         7, NULL,                  'cho_duyet'),
(10, 'PHEP_CUOI',      '2026-04-01', '2026-04-03', 3.0,   'Dam cuoi ban than',     7, NULL,                  'cho_duyet'),
(15, 'PHEP_THAI_SAN',  '2025-09-01', '2026-03-01', 180.0, 'Nghi thai san',         1, '2025-08-25 08:00:00', 'da_duyet'),
(7,  'PHEP_NAM',       '2025-12-25', '2026-01-02', 5.0,   'Nghi Tet som',          4, '2025-12-20 16:00:00', 'da_duyet'),
(2,  'PHEP_NAM',       '2026-02-27', '2026-02-27', 1.0,   'Kham benh dinh ky',     1, NULL,                  'cho_duyet');

-- =====================================================
-- 10. BANGLUONG + CHITIETLUONG + THANHPHANLUONG
-- =====================================================
DELETE FROM BANGLUONG WHERE (thang = 1 AND nam = 2026) OR (thang = 2 AND nam = 2026);

INSERT INTO BANGLUONG (thang, nam, tenBangLuong, nguoiTao, trangThai) VALUES
(1, 2026, 'Bang luong thang 1-2026', 1, 'da_khoa'),
(2, 2026, 'Bang luong thang 2-2026', 1, 'dang_xu_ly');

INSERT INTO CHITIETLUONG (maBangLuong, maNV, luongCoSo, tongLuongChucVu, luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem) VALUES
(1,1,  15000000,5000000,1800000,21800000,2180000,19620000,22.0,10.0),
(1,2,  12000000,3000000,0,      15000000,1500000,13500000,22.0,0.0),
(1,3,  12000000,0,      0,      12000000,1200000,10800000,22.0,0.0),
(1,4,  50000000,10000000,0,     60000000,6000000,54000000,22.0,0.0),
(1,5,  9500000, 0,      0,      9500000, 950000, 8550000, 22.0,0.0),
(1,7,  20000000,5000000,2250000,27250000,2725000,24525000,22.0,15.0),
(1,8,  11000000,0,      900000, 11900000,1190000,10710000,22.0,5.0),
(1,11, 18000000,5000000,0,      23000000,2300000,20700000,22.0,0.0);

INSERT INTO THANHPHANLUONG (maChiTiet, tenThanhPhan, loai, soTien) VALUES
(1,'Luong co so',         'thu_nhap',15000000),
(1,'Phu cap chuc vu',     'thu_nhap',5000000),
(1,'Luong lam them',      'thu_nhap',1800000),
(1,'Bao hiem xa hoi 8%',  'khau_tru',1500000),
(1,'Bao hiem y te 1.5%',  'khau_tru',450000),
(1,'Bao hiem TN 1%',      'khau_tru',230000),
(6,'Luong co so',         'thu_nhap',20000000),
(6,'Phu cap chuc vu',     'thu_nhap',5000000),
(6,'Luong lam them',      'thu_nhap',2250000),
(6,'Bao hiem xa hoi 8%',  'khau_tru',2000000),
(6,'Bao hiem y te 1.5%',  'khau_tru',450000),
(6,'Bao hiem TN 1%',      'khau_tru',275000);

-- =====================================================
-- 11. DOTDANHGIA + DANHGIAHIEUSUAT + CHITIETDANHGIA
-- =====================================================
-- 3 ky da va dang ton tai (trong so phai tong = 100%)
INSERT INTO DOTDANHGIA (tenDot, nam, kyDanhGia, tuNgay, denNgay, trangThai) VALUES
('Danh gia Quy 4 nam 2024', 2024, 'quy_4', '2025-01-02', '2025-01-15', 'da_ket_thuc'),
('Danh gia Quy 4 nam 2025', 2025, 'quy_4', '2026-01-02', '2026-01-15', 'da_ket_thuc'),
('Danh gia nam 2025',        2025, 'nam',   '2026-01-16', '2026-01-31', 'da_ket_thuc'),
('Danh gia Quy 1 nam 2026',  2026, 'quy_1', '2026-04-01', '2026-04-15', 'chua_bat_dau');

-- Trong so tieu chi cho moi ky (tong = 100%)
-- Ky 1 (maDot=1): Quy4/2024
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(1,1,30,TRUE),(1,2,25,TRUE),(1,3,15,FALSE),(1,4,20,TRUE),(1,5,10,TRUE);

-- Ky 2 (maDot=2): Quy4/2025
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(2,1,30,TRUE),(2,2,25,TRUE),(2,3,15,FALSE),(2,4,20,TRUE),(2,5,10,TRUE);

-- Ky 3 (maDot=3): Nam 2025
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(3,1,25,TRUE),(3,2,25,TRUE),(3,3,10,FALSE),(3,4,25,TRUE),(3,5,15,TRUE);

-- Ky 4 (maDot=4): Quy1/2026 - chua bat dau, co the tao tieu chi truoc
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(4,1,30,TRUE),(4,2,20,TRUE),(4,3,15,FALSE),(4,4,20,TRUE),(4,5,15,TRUE);

-- ==========================================
-- DANHGIAHIEUSUAT - Ky 1 (maDot=1: Quy4/2024)
-- ==========================================
INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, nhanXetChung, ngayDanhGia, trangThai) VALUES
(1,3, 1, 7.80,'kha',      'Le Minh Cuong co gang, can phat trien them ky nang',  '2025-01-08 09:00:00','da_xac_nhan'),
(1,5, 1, 8.40,'tot',      'Hoang Van Em hoan thanh tot cac nhiem vu duoc giao',  '2025-01-08 10:00:00','da_xac_nhan'),
(1,6, 1, 9.10,'xuat_sac', 'Ngo Thi Phuong xuat sac, vuot ca chi tieu',           '2025-01-08 11:00:00','da_xac_nhan'),
(1,8, 7, 7.60,'kha',      'Dang Thi Hoa lam viec kha on, can co gang hon',       '2025-01-09 09:00:00','da_xac_nhan'),
(1,10,7, 8.80,'tot',      'Trinh Thi Kim tich cuc, ky nang tot',                 '2025-01-09 10:00:00','da_xac_nhan'),
(1,12,11,7.00,'kha',      'Phan Thi Mai can nang cao hieu suat lam viec',        '2025-01-10 09:00:00','da_xac_nhan'),
(1,13,4, 8.00,'tot',      'Ly Van Nhan nhan vien moi co nhieu tiem nang',        '2025-01-10 10:00:00','da_xac_nhan');

-- Chi tiet danh gia ky 1 (maDanhGia 1-7)
-- Danh gia #1: Le Minh Cuong (maDanhGia=1)
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(1,1,8.0,'Chat luong on'),(1,2,7.5,'Dung tien do'),(1,3,8.5,'Co sang kien'),
(1,4,7.0,'Can cai thien ky nang'),(1,5,8.0,'Hop tac tot');
-- Danh gia #2: Hoang Van Em
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(2,1,8.5,'Chat luong cao'),(2,2,8.0,'Hoan thanh dung han'),(2,3,9.0,'Sang tao tot'),
(2,4,8.5,'Ky nang kha'),(2,5,8.0,'Lam viec nhom tot');
-- Danh gia #3: Ngo Thi Phuong
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(3,1,9.5,'Rat xuat sac'),(3,2,9.0,'Luon hoan thanh som'),(3,3,9.0,'Sang tao xuat sac'),
(3,4,9.0,'Chuyen mon gioi'),(3,5,9.0,'Tinh than nhom cao');
-- Danh gia #4: Dang Thi Hoa
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(4,1,7.5,'Chat luong on'),(4,2,7.5,'Tuong doi dung han'),(4,3,8.0,'Co sang kien'),
(4,4,7.0,'Can phat trien ky nang'),(4,5,8.0,'Lam viec nhom kha');
-- Danh gia #5: Trinh Thi Kim
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(5,1,9.0,'Chat luong tot'),(5,2,8.5,'Dung tien do'),(5,3,9.0,'Co sang kien hay'),
(5,4,8.5,'Ky nang tot'),(5,5,9.0,'Tinh than doi nhom cao');
-- Danh gia #6: Phan Thi Mai
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(6,1,7.0,'Can co gang them'),(6,2,7.0,'Doi khi cham han'),(6,3,7.5,'Co sang kien'),
(6,4,6.5,'Ky nang can phat trien'),(6,5,7.0,'Hop tac kha');
-- Danh gia #7: Ly Van Nhan
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(7,1,8.0,'Tiem nang tot'),(7,2,8.0,'Hoan thanh tot'),(7,3,8.5,'Ham hoc hoi'),
(7,4,7.5,'Dang phat trien ky nang'),(7,5,8.5,'Hoa dong voi doi nhom');

-- ==========================================
-- DANHGIAHIEUSUAT - Ky 2 (maDot=2: Quy4/2025)
-- ==========================================
INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, nhanXetChung, ngayDanhGia, trangThai) VALUES
(2,3, 1, 8.20,'tot',      'Le Minh Cuong da co nhieu tien bo trong nam',          '2026-01-10 09:00:00','da_xac_nhan'),
(2,5, 1, 7.50,'kha',      'Hoang Van Em can cai thien ky nang chuyen mon',        '2026-01-10 10:00:00','da_xac_nhan'),
(2,6, 1, 9.00,'xuat_sac', 'Ngo Thi Phuong tiep tuc xuat sac trong nhieu quy',     '2026-01-10 11:00:00','da_xac_nhan'),
(2,8, 7, 7.80,'kha',      'Dang Thi Hoa hoan thanh tot cac nhiem vu duoc giao',   '2026-01-11 09:00:00','da_xac_nhan'),
(2,10,7, 8.50,'tot',      'Trinh Thi Kim nhan vien tich cuc, ky nang tot',        '2026-01-11 10:00:00','da_xac_nhan'),
(2,12,11,7.20,'kha',      'Phan Thi Mai can nang cao hieu suat',                  '2026-01-12 09:00:00','da_xac_nhan'),
(2,9, 7, 7.00,'kha',      'Bui Quoc Hung thu viec, tiem nang tot',               '2026-01-12 10:00:00','da_xac_nhan');

-- Chi tiet danh gia ky 2 (maDanhGia 8-14)
-- Danh gia #8: Le Minh Cuong
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(8,1,8.5,'Chat luong tot'),(8,2,8.0,'Dung tien do'),(8,3,7.5,'Co sang tao'),
(8,4,8.0,'Ky nang on'),(8,5,8.5,'Hop tac tot');
-- Danh gia #9: Hoang Van Em
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(9,1,7.0,'Can co gang'),(9,2,7.5,'Tuong doi dung han'),(9,3,8.0,'Co sang kien'),
(9,4,7.5,'Ky nang kha'),(9,5,8.0,'Lam viec nhom tot');
-- Danh gia #10: Ngo Thi Phuong
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(10,1,9.5,'Rat tot'),(10,2,9.0,'Luon hoan thanh som'),(10,3,8.5,'Sang tao xuat sac'),
(10,4,9.0,'Chuyen mon gioi'),(10,5,9.0,'Tinh than nhom cao');
-- Danh gia #11: Dang Thi Hoa
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(11,1,8.0,'Cai thien ro ret'),(11,2,7.5,'Dung tien do'),(11,3,8.0,'Co sang kien'),
(11,4,7.5,'Dang phat trien'),(11,5,8.0,'Hop tac tot hon');
-- Danh gia #12: Trinh Thi Kim
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(12,1,9.0,'Chat luong cao'),(12,2,8.5,'Hoan thanh dung han'),(12,3,8.0,'Sang tao'),
(12,4,8.5,'Ky nang tot'),(12,5,8.5,'Tinh than doi nhom cao');
-- Danh gia #13: Phan Thi Mai
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(13,1,7.5,'On dinh hon'),(13,2,6.5,'Can co gang dung han'),(13,3,7.0,'It sang kien'),
(13,4,7.5,'Dang hoc hoi'),(13,5,7.5,'Hop tac tot hon');
-- Danh gia #14: Bui Quoc Hung (NV thu viec)
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(14,1,7.0,'Tiem nang tot'),(14,2,7.0,'Hoan thanh co ban'),(14,3,7.5,'Ham hoc hoi'),
(14,4,6.5,'Dang phat trien ky nang'),(14,5,7.5,'Hoa dong voi doi nhom');

-- ==========================================
-- DANHGIAHIEUSUAT - Ky 3 (maDot=3: Nam 2025)
-- ==========================================
INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, nhanXetChung, ngayDanhGia, trangThai) VALUES
(3,3, 1, 8.00,'tot',      'Nam 2025: Le Minh Cuong on dinh, co tien bo ro ret',   '2026-01-20 09:00:00','da_xac_nhan'),
(3,5, 1, 7.70,'kha',      'Nam 2025: Hoang Van Em can co gang chuyen mon hon',    '2026-01-20 10:00:00','da_xac_nhan'),
(3,6, 1, 9.20,'xuat_sac', 'Nam 2025: Ngo Thi Phuong - nhan vien xuat sac nhat',  '2026-01-20 11:00:00','da_xac_nhan'),
(3,8, 7, 8.00,'tot',      'Nam 2025: Dang Thi Hoa co tien bo tot',                '2026-01-21 09:00:00','da_xac_nhan'),
(3,10,7, 8.70,'tot',      'Nam 2025: Trinh Thi Kim lien tuc hoan thanh tot',      '2026-01-21 10:00:00','da_xac_nhan'),
(3,12,11,7.50,'kha',      'Nam 2025: Phan Thi Mai co tien bo trong nua sau nam',  '2026-01-22 09:00:00','da_xac_nhan'),
(3,13,4, 8.20,'tot',      'Nam 2025: Ly Van Nhan nhan vien tiem nang cao',        '2026-01-22 10:00:00','da_xac_nhan'),
(3,7, 4, 9.10,'xuat_sac', 'Nam 2025: Vu Thanh Giang quan ly xuat sac, giang day tot nhiệm vu', '2026-01-23 09:00:00','da_xac_nhan'),
(3,11,4, 8.90,'tot',      'Nam 2025: Dinh Van Long quan ly ke toan hieu qua',     '2026-01-23 10:00:00','da_xac_nhan');

-- Chi tiet danh gia ky 3 (maDanhGia 15-23)
-- #15: Le Minh Cuong
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(15,1,8.0,'Kha tot'),(15,2,8.0,'Dung tien do'),(15,3,8.0,'Co sang kien'),
(15,4,8.0,'Ky nang on'),(15,5,8.5,'Hop tac tot');
-- #16: Hoang Van Em
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(16,1,7.5,'On dinh'),(16,2,7.5,'Dung han'),(16,3,8.5,'Ham sang tao'),
(16,4,8.0,'Dang phat trien'),(16,5,7.5,'Nhom hoa dong');
-- #17: Ngo Thi Phuong
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(17,1,9.5,'Rat xuat sac'),(17,2,9.0,'Luon som han'),(17,3,9.0,'Sang tao xuat sac'),
(17,4,9.0,'Chuyen mon gioi'),(17,5,9.5,'Nguyen tau nhom');
-- #18: Dang Thi Hoa
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(18,1,8.0,'Cai thien tot'),(18,2,8.5,'Dung han tot hon'),(18,3,7.5,'Co sang kien'),
(18,4,7.5,'Dang hoc them'),(18,5,8.5,'Cong tac nhom tot');
-- #19: Trinh Thi Kim
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(19,1,9.0,'Chat luong cao'),(19,2,9.0,'Hoan thanh dung han'),(19,3,8.0,'Co sang kien hay'),
(19,4,8.5,'Ky nang cao cap'),(19,5,9.0,'Dau tau nhom');
-- #20: Phan Thi Mai
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(20,1,7.5,'On hon'),(20,2,7.5,'Co gang dung han'),(20,3,7.0,'It sang kien'),
(20,4,8.0,'Hoc them ky nang'),(20,5,7.5,'Cong tac on');
-- #21: Ly Van Nhan
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(21,1,8.5,'Tiem nang tot'),(21,2,8.0,'Hoan thanh tot'),(21,3,8.5,'Ham hoc hoi'),
(21,4,8.0,'Phat trien ky nang nhanh'),(21,5,8.5,'Hoa dong nhom tot');
-- #22: Vu Thanh Giang (Truong phong IT)
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(22,1,9.5,'Chat luong quan ly cao'),(22,2,9.0,'Luan hoan thanh muc tieu'),(22,3,9.0,'Sang tao trong giai phap'),
(22,4,9.0,'Chuyen mon xuat sac'),(22,5,9.0,'Lanh dao doi nhom tot');
-- #23: Dinh Van Long (Truong phong KT)
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
(23,1,9.0,'Chat luong quan ly tot'),(23,2,8.5,'Hoan thanh muc tieu'),(23,3,9.0,'Sang tao trong ke toan'),
(23,4,9.0,'Chuyen mon ke toan cao'),(23,5,8.5,'Lanh dao hieu qua');

-- =====================================================
-- 12. YEUCAUTUYENDUNG + TINTUYENDUNG + UNGVIEN
-- =====================================================
INSERT INTO YEUCAUTUYENDUNG (maPhongBan, maChucVu, soLuong, lyDo, mucLuongDuKien, yeuCauKinhNghiem, yeuCauHocVan, hanTuyenDung, nguoiDuyet, ngayDuyet, trangThai) VALUES
('PHONGIT','NV',2,'Mo rong he thong',         '12-18 trieu','2 nam IT, Java Spring Boot','Dai hoc CNTT',    '2026-03-31',4,'2026-01-15 10:00:00','da_duyet'),
('PHONGKD','NV',1,'Tang truong kinh doanh',   '10-15 trieu','1 nam kinh doanh',          'Dai hoc Kinh te', '2026-04-30',4,'2026-01-20 14:00:00','da_duyet'),
('PHONGNS','NV',1,'Bo sung nhan luc NS',      '9-12 trieu', '1 nam HR',                  'Dai hoc QTKD',    '2026-05-31',4,NULL,                 'cho_duyet');

INSERT INTO TINTUYENDUNG (maYeuCau, tieuDe, noiDung, mucLuong, diaDiem, hanNopHoSo, trangThai, soLuotXem) VALUES
(1,'Tuyen Lap trinh vien Java Backend','Thiet ke REST API, Spring Boot, MySQL, deploy Cloud','12-18 trieu','TP.HCM','2026-03-25','dang_tuyen',245),
(2,'Tuyen Nhan vien Kinh doanh B2B',  'Tim kiem va phat trien khach hang doanh nghiep',    '10-15 trieu + Hoa hong','TP.HCM','2026-04-25','dang_tuyen',180);

INSERT INTO UNGVIEN (maTin, hoTen, email, dienThoai, ngaySinh, gioiTinh, diaChi, trinhDoHocVan, kinhNghiem, nguonUngTuyen, trangThai, nhanXet) VALUES
(1,'Nguyen Minh Tuan','tuan.it@gmail.com','0912345601','1999-03-15','nam','Ha Noi',     'DH BKHN - CNTT',    '3 nam Java Spring, MySQL',  'Website',  'dang_phong_van','Ung vien tiem nang'),
(1,'Tran Thi Lan',   'lan.dev@gmail.com', '0912345602','2000-07-22','nu', 'TP.HCM',   'DH CNTT TP.HCM',    '2 nam Java, React, Docker', 'LinkedIn', 'moi',          'Ho so an tuong'),
(1,'Le Van Duc',     'duc.java@gmail.com','0912345603','1998-11-10','nam','Binh Duong','DH FPT - CNTT',     '4 nam Java, Microservices', 'Gioi thieu','moi',         'Kinh nghiem phong phu'),
(2,'Pham Hoang Nam', 'nam.biz@gmail.com', '0912345604','1997-05-18','nam','TP.HCM',   'DH Kinh te TP.HCM', '2 nam sales B2B',           'Facebook', 'dang_phong_van','Thanh tich doanh so tot'),
(2,'Vo Thi Thu',     'thu.sales@gmail.com','0912345605','1999-09-30','nu','TP.HCM',   'DH Thuong mai',     '1 nam sales',               'Website',  'moi',          'Ho so tot');

-- =====================================================
-- 13. THONGBAO
-- =====================================================
INSERT INTO THONGBAO (tieuDe, noiDung, loaiThongBao, maTaiKhoanGui, maTaiKhoanNhan, daDoc, ngayDoc) VALUES
('Chuc mung nam moi 2026!',      'Cong ty chuc toan the nhan vien nam moi hanh phuc, suc khoe, thanh cong!','thong_bao_chung',4,1, TRUE, '2026-01-02 09:15:00'),
('Chuc mung nam moi 2026!',      'Cong ty chuc toan the nhan vien nam moi hanh phuc, suc khoe, thanh cong!','thong_bao_chung',4,2, TRUE, '2026-01-02 08:45:00'),
('Chuc mung nam moi 2026!',      'Cong ty chuc toan the nhan vien nam moi hanh phuc, suc khoe, thanh cong!','thong_bao_chung',4,3, TRUE, '2026-01-02 10:00:00'),
('Chuc mung nam moi 2026!',      'Cong ty chuc toan the nhan vien nam moi hanh phuc, suc khoe, thanh cong!','thong_bao_chung',4,5, FALSE,NULL),
('Don nghi phep da duoc duyet',  'Don nghi phep ngay 11/02/2026 cua ban da duoc duyet. Chuc ban nghi vui!', 'don_tu',         2,1, TRUE, '2026-02-09 11:00:00'),
('Don nghi phep can duyet',      'NV Dang Thi Hoa gui don nghi phep 10-12/03/2026. Vui long phe duyet.',    'don_tu',         8,7, FALSE,NULL),
('Bang luong thang 1/2026 co roi','BL thang 1/2026 da xac nhan. Vui long kiem tra chi tiet luong.',          'he_thong',       1,1, TRUE, '2026-02-05 09:30:00'),
('Bang luong thang 1/2026 co roi','BL thang 1/2026 da xac nhan. Vui long kiem tra chi tiet luong.',          'he_thong',       1,7, TRUE, '2026-02-05 10:00:00'),
('Lich danh gia Quy 1/2026',     'Dot DG hieu suat Q1/2026 bat dau 01/04/2026. Vui long chuan bi ho so.',   'thong_bao_chung',1,7, FALSE,NULL),
('Lich danh gia Quy 1/2026',     'Dot DG hieu suat Q1/2026 bat dau 01/04/2026. Vui long chuan bi ho so.',   'thong_bao_chung',1,11,FALSE,NULL),
('Lich danh gia Quy 1/2026',     'Dot DG hieu suat Q1/2026 bat dau 01/04/2026. Vui long chuan bi ho so.',   'thong_bao_chung',1,2, FALSE,NULL),
('Hop tong ket Q1/2026',         'Hop tong ket Q1 ngay 31/03/2026 luc 14:00 tai phong hop lon.',             'thong_bao_chung',4,1, FALSE,NULL),
('Hop tong ket Q1/2026',         'Hop tong ket Q1 ngay 31/03/2026 luc 14:00 tai phong hop lon.',             'thong_bao_chung',4,7, FALSE,NULL);

-- =====================================================
-- 14. LOG_AUDIT
-- =====================================================
INSERT INTO LOG_AUDIT (maTaiKhoan, hanhDong, bangDuLieu, maBanGhi, diaChiIP) VALUES
(4,'LOGIN',  NULL,              NULL, '192.168.1.10'),
(4,'CREATE', 'NHANVIEN',      '14', '192.168.1.10'),
(4,'CREATE', 'TINTUYENDUNG', '1',  '192.168.1.10'),
(4,'CREATE', 'TINTUYENDUNG', '2',  '192.168.1.10'),
(1,'LOGIN',  NULL,              NULL, '192.168.1.11'),
(1,'CREATE', 'BANGLUONG',     '1',  '192.168.1.11'),
(1,'UPDATE', 'BANGLUONG',     '1',  '192.168.1.11'),
(1,'UPDATE', 'DONXINNGHIPHEP','1', '192.168.1.11'),
(7,'LOGIN',  NULL,              NULL, '192.168.1.12'),
(7,'UPDATE', 'DONXINNGHIPHEP','4', '192.168.1.12'),
(3,'LOGIN',  NULL,              NULL, '192.168.1.13'),
(3,'CREATE', 'DONXINNGHIPHEP','3', '192.168.1.13'),
(8,'LOGIN',  NULL,              NULL, '192.168.1.14'),
(8,'CREATE', 'DONXINNGHIPHEP','4', '192.168.1.14');

-- =====================================================
-- CAUHINH_PHUCAP
-- =====================================================
INSERT INTO CAUHINH_PHUCAP (loai, tenKhoan, kieuTinh, giaTri, nguon, hoatDong) VALUES
('phu_cap', 'Phu cap an trua',    'co_dinh',  500000, 'CongTy',  1),
('phu_cap', 'Phu cap dien thoai','co_dinh',  300000, 'CongTy',  1),
('phu_cap', 'Phu cap di lai',    'co_dinh',  400000, 'CongTy',  1),
('khau_tru','BHXH (8%)',         'phan_tram', 8,     'LuatDinh',1),
('khau_tru','BHYT (1.5%)',       'phan_tram', 1.5,   'LuatDinh',1),
('khau_tru','BHTN (1%)',         'phan_tram', 1,     'LuatDinh',1);

SELECT '=== HRM Sample Data Inserted Successfully! ===' AS Message;
