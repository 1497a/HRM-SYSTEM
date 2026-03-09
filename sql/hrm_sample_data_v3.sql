-- =====================================================
-- HRM SAMPLE DATA V3 - Dữ liệu mẫu tinh gọn & đại diện
-- Chạy sau hrm_database.sql
-- =====================================================
-- Thiết kế:
--   13 nhân viên — đại diện đầy đủ mọi chức vụ/vai trò
--   7 phòng ban: CONGTY + 5 phòng chính + TEAM_IT (1 sub-team duy nhất)
--   Bỏ: phòng riêng GIAM_DOC / PHO_GIAM_DOC
--
--   NV001: GD    → CONGTY
--   NV002: TP NS → PHONGNS;   NV003: NSV  → PHONGNS
--   NV004: TP KT → PHONGKT;   NV005: KTV  → PHONGKT
--   NV006: TP KD → PHONGKD;   NV007: CV   → PHONGKD
--   NV008: TP IT → PHONGIT;   NV009: TT   → TEAM_IT
--                              NV010: NV   → TEAM_IT
--                              NV011: TV   → TEAM_IT
--   NV012: TP MKT → PHONGMKT; NV013: NV   → PHONGMKT
-- =====================================================
USE hrm_db;

-- =====================================================
-- 1. PHONG BAN
-- =====================================================
DELETE FROM PHONGBAN;

INSERT INTO PHONGBAN (maPhongBan, tenPhongBan, phongBanCha, moTa, trangThai) VALUES
('CONGTY',   'Cong ty TNHH ABC Technology',  NULL,      'Cong ty cong nghe phan mem va giai phap CNTT',             'hoatDong'),
('PHONGNS',  'Phong Nhan su',                'CONGTY',  'Quan ly nhan su, tuyen dung, luong thuong',                'hoatDong'),
('PHONGKT',  'Phong Ke toan - Tai chinh',    'CONGTY',  'Quan ly tai chinh, ke toan, thue',                        'hoatDong'),
('PHONGKD',  'Phong Kinh doanh',             'CONGTY',  'Phat trien kinh doanh, ban hang, cham soc khach hang',    'hoatDong'),
('PHONGIT',  'Phong Cong nghe thong tin',    'CONGTY',  'Phat trien phan mem, he thong CNTT, bao mat',             'hoatDong'),
('PHONGMKT', 'Phong Marketing',              'CONGTY',  'Truyen thong, quang cao, thuong hieu, digital marketing', 'hoatDong'),
('TEAM_IT',  'Team Phat trien IT',           'PHONGIT', 'Phat trien ung dung, API, kiem thu phan mem',             'hoatDong');

-- =====================================================
-- 2. CHUC VU
-- =====================================================
DELETE FROM CHUCVU;

INSERT INTO CHUCVU (maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu, moTa, trangThai) VALUES
('GD',  'Giam doc',       1, 5.00, 15000000, 'Cap lanh dao cao nhat cong ty',                    'hoatDong'),
('TP',  'Truong phong',   2, 3.00,  5000000, 'Quan ly cap phong ban',                             'hoatDong'),
('TT',  'Truong nhom',    3, 2.20,  2000000, 'Quan ly cap team / nhom',                           'hoatDong'),
('CV',  'Chuyen vien',    4, 1.70,   500000, 'Nhan vien co kinh nghiem tu 2 nam tro len',         'hoatDong'),
('KTV', 'Ke toan vien',   5, 1.30,   200000, 'Nhan vien chuyen mon ke toan - tai chinh',          'hoatDong'),
('NSV', 'Nhan su vien',   5, 1.30,   200000, 'Nhan vien chuyen mon nhan su - tuyen dung',         'hoatDong'),
('NV',  'Nhan vien',      5, 1.30,   200000, 'Nhan vien chinh thuc cac phong ban khac',           'hoatDong'),
('TV',  'Thu viec',       6, 0.85,        0, 'Nhan su dang trong thoi gian thu viec',              'hoatDong');

-- =====================================================
-- 3. NHAN VIEN - 13 nhân viên
-- =====================================================
INSERT INTO NHANVIEN (maNV, loaiHopDong, ngayVaoLam, trangThai, ghiChu) VALUES
('NV001', 'khong_xac_dinh',    '2015-01-05', 'dang_lam_viec', 'Giám đốc công ty'),
-- Phòng Nhân sự
('NV002', 'khong_xac_dinh',    '2017-02-01', 'dang_lam_viec', 'Trưởng phòng Nhân sự'),
('NV003', 'xac_dinh_thoi_han', '2021-06-01', 'dang_lam_viec', NULL),
-- Phòng Kế toán
('NV004', 'khong_xac_dinh',    '2017-04-01', 'dang_lam_viec', 'Trưởng phòng Kế toán'),
('NV005', 'xac_dinh_thoi_han', '2021-07-01', 'dang_lam_viec', NULL),
-- Phòng Kinh doanh
('NV006', 'khong_xac_dinh',    '2017-07-01', 'dang_lam_viec', 'Trưởng phòng Kinh doanh'),
('NV007', 'xac_dinh_thoi_han', '2020-06-01', 'dang_lam_viec', NULL),
-- Phòng IT
('NV008', 'khong_xac_dinh',    '2018-01-15', 'dang_lam_viec', 'Trưởng phòng IT'),
('NV009', 'khong_xac_dinh',    '2019-08-01', 'dang_lam_viec', 'Trưởng nhóm TEAM_IT'),
('NV010', 'xac_dinh_thoi_han', '2022-02-01', 'dang_lam_viec', NULL),
('NV011', 'thu_viec',          '2025-10-01', 'dang_lam_viec', 'Đang thử việc IT'),
-- Phòng Marketing
('NV012', 'khong_xac_dinh',    '2018-07-01', 'dang_lam_viec', 'Trưởng phòng Marketing'),
('NV013', 'xac_dinh_thoi_han', '2021-03-01', 'dang_lam_viec', NULL);

-- =====================================================
-- 4. THÔNG TIN CÁ NHÂN
-- =====================================================
INSERT INTO THONGTINCANHAN (maNV, hoTen, ngaySinh, gioiTinh, cccd, dienThoai, email, diaChi, diaChiThuongTru, queQuan, danToc, tonGiao, tinhTrangHonNhan) VALUES
('NV001', 'Nguyen Duc Hung',      '1978-05-12', 'nam', '001078005121', '0901000001', 'hung.nguyen@abctech.vn',    '10 Le Duan, Q1, TP.HCM',           '10 Le Duan, Q1, TP.HCM',           'Ha Noi',     'Kinh', 'Khong',     'da_ket_hon'),
('NV002', 'Nguyen Thi Thu Huong', '1985-07-25', 'nu',  '079085007251', '0901000002', 'huong.nguyen@abctech.vn',   '22 Ly Tu Trong, Q1, TP.HCM',       '22 Ly Tu Trong, Q1, TP.HCM',       'Nghe An',    'Kinh', 'Phat giao', 'da_ket_hon'),
('NV003', 'Dang Thi Lan Anh',     '1992-02-18', 'nu',  '079092002181', '0901000003', 'lananh.dang@abctech.vn',    '67 Nguyen Thi Minh Khai, Q3',      '67 Nguyen Thi Minh Khai, Q3',      'Binh Dinh',  'Kinh', 'Khong',     'doc_than'),
('NV004', 'Hoang Thi Bich Ngoc',  '1984-12-03', 'nu',  '079084012031', '0901000004', 'ngoc.hoang@abctech.vn',     '45 Nam Ky Khoi Nghia, Q3',         '45 Nam Ky Khoi Nghia, Q3',         'Hue',        'Kinh', 'Phat giao', 'da_ket_hon'),
('NV005', 'Ly Thi Thanh Tam',     '1993-01-29', 'nu',  '079093001291', '0901000005', 'thanhTam.ly@abctech.vn',    '56 Bach Dang, BT, TP.HCM',         '56 Bach Dang, BT, TP.HCM',         'Ben Tre',    'Kinh', 'Khong',     'doc_than'),
('NV006', 'Nguyen Anh Tuan',      '1983-06-14', 'nam', '079083006141', '0901000006', 'anh.tuan@abctech.vn',       '22 Pasteur, Q1, TP.HCM',           '22 Pasteur, Q1, TP.HCM',           'Ha Noi',     'Kinh', 'Khong',     'da_ket_hon'),
('NV007', 'Le Minh Hoang',        '1991-12-25', 'nam', '079091012251', '0901000007', 'hoang.le@abctech.vn',       '15 CMT8, Q10, TP.HCM',             '15 CMT8, Q10, TP.HCM',             'Binh Duong', 'Kinh', 'Khong',     'da_ket_hon'),
('NV008', 'Dinh Quang Son',       '1982-08-30', 'nam', '079082008301', '0901000008', 'son.dinh@abctech.vn',       '10 Nguyen Van Cu, Q5, TP.HCM',     '10 Nguyen Van Cu, Q5, TP.HCM',     'Hai Phong',  'Kinh', 'Khong',     'da_ket_hon'),
('NV009', 'Nguyen Van Khoa',      '1990-04-05', 'nam', '079090004051', '0901000009', 'khoa.nguyen@abctech.vn',    '26 Truong Dinh, Q3, TP.HCM',       '26 Truong Dinh, Q3, TP.HCM',       'Quang Ngai', 'Kinh', 'Khong',     'da_ket_hon'),
('NV010', 'Hoang Minh Tri',       '1995-07-01', 'nam', '079095007011', '0901000010', 'tri.hoang@abctech.vn',      '38 Vo Van Tan, Q3, TP.HCM',        '38 Vo Van Tan, Q3, TP.HCM',        'Ha Noi',     'Kinh', 'Khong',     'doc_than'),
('NV011', 'Vo Thi Cam Tu',        '2001-08-07', 'nu',  '079101008071', '0901000011', 'camtu.vo@abctech.vn',       '25 Dinh Tien Hoang, BT, TP.HCM',   '25 Dinh Tien Hoang, BT, TP.HCM',  'TP.HCM',     'Kinh', 'Khong',     'doc_than'),
('NV012', 'Le Thi Phuong Linh',   '1986-10-09', 'nu',  '079086010091', '0901000012', 'phuonglinh.le@abctech.vn',  '30 Nguyen Trong Tuyen, PN, TP.HCM','30 Nguyen Trong Tuyen, PN, TP.HCM','TP.HCM',     'Kinh', 'Khong',     'da_ket_hon'),
('NV013', 'Pham Dinh Khang',      '1993-05-04', 'nam', '079093005041', '0901000013', 'khang.pham@abctech.vn',     '52 Phu Nhuan, PN, TP.HCM',         '52 Phu Nhuan, PN, TP.HCM',         'Binh Phuoc', 'Kinh', 'Khong',     'doc_than');

-- =====================================================
-- 5. BỔ NHIỆM
-- =====================================================
-- Quy tắc phân cấp:
--   NV001 (GD) → tự duyệt, không có quản lý
--   TP các phòng → báo cáo GD (NV001), NV001 duyệt
--   NV009 (TT TEAM_IT) → báo cáo TP IT (NV008), NV008 duyệt
--   Nhân viên thường → báo cáo TP/TT trực tiếp
INSERT INTO BONHIEM (maNV, maPhongBan, maChucVu, loaiBoNhiem, tyLeHuongLuong, maQuanLy, nguoiDuyet, tuNgay, trangThai, lyDo) VALUES
-- Cấp công ty
('NV001', 'CONGTY',   'GD',  'chinh', 100.00, NULL,    NULL,    '2015-01-05', 'hieu_luc', 'Bo nhiem Giam doc dieu hanh'),
-- Phòng Nhân sự
('NV002', 'PHONGNS',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2017-02-01', 'hieu_luc', 'Bo nhiem Truong phong Nhan su'),
('NV003', 'PHONGNS',  'NSV', 'chinh', 100.00, 'NV002', 'NV001', '2021-06-01', 'hieu_luc', 'Nhan su vien - tuyen dung'),
-- Phòng Kế toán
('NV004', 'PHONGKT',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2017-04-01', 'hieu_luc', 'Bo nhiem Truong phong Ke toan'),
('NV005', 'PHONGKT',  'KTV', 'chinh', 100.00, 'NV004', 'NV001', '2021-07-01', 'hieu_luc', 'Ke toan vien'),
-- Phòng Kinh doanh
('NV006', 'PHONGKD',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2017-07-01', 'hieu_luc', 'Bo nhiem Truong phong Kinh doanh'),
('NV007', 'PHONGKD',  'CV',  'chinh', 100.00, 'NV006', 'NV001', '2020-06-01', 'hieu_luc', 'Chuyen vien Kinh doanh'),
-- Phòng IT
('NV008', 'PHONGIT',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2018-01-15', 'hieu_luc', 'Bo nhiem Truong phong Cong nghe thong tin'),
('NV009', 'TEAM_IT',  'TT',  'chinh', 100.00, 'NV008', 'NV008', '2019-08-01', 'hieu_luc', 'Truong nhom phat trien IT'),
('NV010', 'TEAM_IT',  'NV',  'chinh', 100.00, 'NV009', 'NV008', '2022-02-01', 'hieu_luc', 'Nhan vien phat trien IT'),
('NV011', 'TEAM_IT',  'TV',  'chinh', 100.00, 'NV009', 'NV008', '2025-10-01', 'hieu_luc', 'Thu viec phat trien IT'),
-- Phòng Marketing
('NV012', 'PHONGMKT', 'TP',  'chinh', 100.00, 'NV001', 'NV001', '2018-07-01', 'hieu_luc', 'Bo nhiem Truong phong Marketing'),
('NV013', 'PHONGMKT', 'NV',  'chinh', 100.00, 'NV012', 'NV001', '2021-03-01', 'hieu_luc', 'Nhan vien Marketing');

-- =====================================================
-- 5.5. VAI TRÒ & QUYỀN
-- =====================================================
INSERT INTO VAITRO (maVaiTro, tenVaiTro, moTa, laVaiTroHeThong, trangThai) VALUES
('ADMIN',         'Quản trị viên',   'Toàn quyền quản trị hệ thống',            TRUE,  'hoatDong'),
('TONG_GIAM_DOC', 'Tổng giám đốc',   'Điều hành cấp cao toàn công ty',          FALSE, 'hoatDong'),
('TRUONG_PHONG',  'Trưởng phòng',    'Quản lý phòng ban và phê duyệt cấp dept', FALSE, 'hoatDong'),
('QUAN_LY',       'Quản lý',         'Quản lý nhóm và phê duyệt cấp team',      FALSE, 'hoatDong'),
('NHAN_SU',       'Nhân sự',         'Nhân viên phòng Nhân sự - toàn quyền NS', FALSE, 'hoatDong'),
('KE_TOAN',       'Kế toán',         'Nhân viên phòng Kế toán - toàn quyền KT', FALSE, 'hoatDong'),
('NHAN_VIEN',     'Nhân viên',       'Nhân viên thông thường',                   FALSE, 'hoatDong');

-- QUYEN
INSERT INTO QUYEN (maQuyen, tenQuyen, nhomQuyen) VALUES
('EMPLOYEE_VIEW',       'Xem nhân viên',              'Nhân viên'),
('EMPLOYEE_CREATE',     'Tạo nhân viên',              'Nhân viên'),
('EMPLOYEE_UPDATE',     'Cập nhật nhân viên',         'Nhân viên'),
('EMPLOYEE_RESIGN',     'Cho nghỉ việc',              'Nhân viên'),
('DEPARTMENT_VIEW',     'Xem phòng ban',              'Tổ chức'),
('DEPARTMENT_MANAGE',   'Quản lý phòng ban',          'Tổ chức'),
('POSITION_VIEW',       'Xem chức vụ',                'Tổ chức'),
('POSITION_MANAGE',     'Quản lý chức vụ',            'Tổ chức'),
('APPOINTMENT_VIEW',    'Xem bổ nhiệm',               'Bổ nhiệm'),
('APPOINTMENT_CREATE',  'Tạo bổ nhiệm',               'Bổ nhiệm'),
('APPOINTMENT_APPROVE', 'Duyệt bổ nhiệm',             'Bổ nhiệm'),
('ATTENDANCE_VIEW',     'Xem chấm công',              'Chấm công'),
('ATTENDANCE_MANAGE',   'Quản lý chấm công',          'Chấm công'),
('CONTRACT_VIEW',       'Xem hợp đồng',               'Hợp đồng'),
('CONTRACT_CREATE',     'Tạo hợp đồng',               'Hợp đồng'),
('CONTRACT_UPDATE',     'Cập nhật hợp đồng',          'Hợp đồng'),
('CONTRACT_MANAGE',     'Quản lý hợp đồng',           'Hợp đồng'),
('PAYROLL_VIEW',        'Xem lương',                  'Lương'),
('PAYROLL_CALCULATE',   'Tính lương',                 'Lương'),
('LEAVE_VIEW',          'Xem nghỉ phép',              'Nghỉ phép'),
('LEAVE_CREATE',        'Tạo đơn nghỉ phép',          'Nghỉ phép'),
('LEAVE_MANAGE',        'Quản lý nghỉ phép',          'Nghỉ phép'),
('LEAVE_APPROVE',       'Duyệt nghỉ phép',            'Nghỉ phép'),
('EVAL_VIEW',           'Xem đánh giá',               'Đánh giá'),
('EVAL_MANAGE',         'Quản lý đợt đánh giá',       'Đánh giá'),
('EVAL_REVIEW',         'Đánh giá nhân viên',         'Đánh giá'),
('RECRUITMENT_VIEW',    'Xem tuyển dụng',             'Tuyển dụng'),
('RECRUITMENT_REQUEST', 'Yêu cầu tuyển dụng',         'Tuyển dụng'),
('RECRUITMENT_MANAGE',  'Quản lý tuyển dụng',         'Tuyển dụng'),
('REPORT_VIEW',         'Xem báo cáo',                'Báo cáo'),
('REPORT_EXPORT',       'Xuất báo cáo',               'Báo cáo'),
('NOTIFICATION_SEND',   'Gửi thông báo',              'Thông báo'),
('USER_VIEW',           'Xem danh sách tài khoản',    'Tài khoản'),
('USER_CREATE',         'Tạo tài khoản',              'Tài khoản'),
('USER_UPDATE',         'Cập nhật tài khoản',         'Tài khoản'),
('USER_DELETE',         'Xóa tài khoản',              'Tài khoản'),
('ROLE_VIEW',           'Xem vai trò',                'Vai trò'),
('ROLE_CREATE',         'Tạo vai trò',                'Vai trò'),
('ROLE_UPDATE',         'Cập nhật vai trò',           'Vai trò'),
('ROLE_DELETE',         'Xóa vai trò',                'Vai trò'),
('SETTINGS_VIEW',       'Xem cài đặt',                'Cài đặt'),
('SETTINGS_UPDATE',     'Cập nhật cài đặt',           'Cài đặt');

-- VAITRO_QUYEN
-- ADMIN: toàn quyền
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi)
SELECT 'ADMIN', maQuyen, 'ALL' FROM QUYEN;

-- NHAN_VIEN: chỉ xem dữ liệu bản thân
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('NHAN_VIEN', 'EMPLOYEE_VIEW',    'SELF'),
('NHAN_VIEN', 'APPOINTMENT_VIEW', 'SELF'),
('NHAN_VIEN', 'ATTENDANCE_VIEW',  'SELF'),
('NHAN_VIEN', 'CONTRACT_VIEW',    'SELF'),
('NHAN_VIEN', 'PAYROLL_VIEW',     'SELF'),
('NHAN_VIEN', 'LEAVE_VIEW',       'SELF'),
('NHAN_VIEN', 'LEAVE_CREATE',     'SELF'),
('NHAN_VIEN', 'EVAL_VIEW',        'SELF'),
('NHAN_VIEN', 'REPORT_VIEW',      'SELF');

-- QUAN_LY (Team Lead): xem & duyệt cấp team
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('QUAN_LY', 'EMPLOYEE_VIEW',         'TEAM'),
('QUAN_LY', 'APPOINTMENT_VIEW',      'TEAM'),
('QUAN_LY', 'ATTENDANCE_VIEW',       'TEAM'),
('QUAN_LY', 'CONTRACT_VIEW',         'TEAM'),
('QUAN_LY', 'PAYROLL_VIEW',          'TEAM'),
('QUAN_LY', 'LEAVE_VIEW',            'TEAM'),
('QUAN_LY', 'LEAVE_CREATE',          'SELF'),
('QUAN_LY', 'LEAVE_APPROVE',         'TEAM'),
('QUAN_LY', 'EVAL_VIEW',             'TEAM'),
('QUAN_LY', 'EVAL_REVIEW',           'TEAM'),
('QUAN_LY', 'RECRUITMENT_VIEW',      'TEAM'),
('QUAN_LY', 'RECRUITMENT_REQUEST',   'SELF'),
('QUAN_LY', 'REPORT_VIEW',           'SELF'),
('QUAN_LY', 'NOTIFICATION_SEND',     'SELF');

-- TRUONG_PHONG: xem & duyệt cấp phòng ban
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('TRUONG_PHONG', 'EMPLOYEE_VIEW',         'DEPT'),
('TRUONG_PHONG', 'APPOINTMENT_VIEW',      'DEPT'),
('TRUONG_PHONG', 'ATTENDANCE_VIEW',       'DEPT'),
('TRUONG_PHONG', 'CONTRACT_VIEW',         'DEPT'),
('TRUONG_PHONG', 'PAYROLL_VIEW',          'DEPT'),
('TRUONG_PHONG', 'LEAVE_VIEW',            'DEPT'),
('TRUONG_PHONG', 'LEAVE_CREATE',          'SELF'),
('TRUONG_PHONG', 'LEAVE_APPROVE',         'DEPT'),
('TRUONG_PHONG', 'EVAL_VIEW',             'DEPT'),
('TRUONG_PHONG', 'EVAL_REVIEW',           'DEPT'),
('TRUONG_PHONG', 'RECRUITMENT_VIEW',      'DEPT'),
('TRUONG_PHONG', 'RECRUITMENT_REQUEST',   'SELF'),
('TRUONG_PHONG', 'REPORT_VIEW',           'SELF'),
('TRUONG_PHONG', 'NOTIFICATION_SEND',     'SELF'),
('TRUONG_PHONG', 'ATTENDANCE_MANAGE',     'SELF'),
('TRUONG_PHONG', 'CONTRACT_CREATE',       'SELF'),
('TRUONG_PHONG', 'CONTRACT_UPDATE',       'SELF');

-- TONG_GIAM_DOC: toàn quyền nghiệp vụ + xem hệ thống, không quản lý tài khoản
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('TONG_GIAM_DOC', 'EMPLOYEE_VIEW',         'ALL'),
('TONG_GIAM_DOC', 'EMPLOYEE_CREATE',       'SELF'),
('TONG_GIAM_DOC', 'EMPLOYEE_UPDATE',       'ALL'),
('TONG_GIAM_DOC', 'EMPLOYEE_RESIGN',       'SELF'),
('TONG_GIAM_DOC', 'DEPARTMENT_VIEW',       'ALL'),
('TONG_GIAM_DOC', 'DEPARTMENT_MANAGE',     'SELF'),
('TONG_GIAM_DOC', 'POSITION_VIEW',         'ALL'),
('TONG_GIAM_DOC', 'POSITION_MANAGE',       'SELF'),
('TONG_GIAM_DOC', 'APPOINTMENT_VIEW',      'ALL'),
('TONG_GIAM_DOC', 'APPOINTMENT_CREATE',    'SELF'),
('TONG_GIAM_DOC', 'APPOINTMENT_APPROVE',   'ALL'),
('TONG_GIAM_DOC', 'ATTENDANCE_VIEW',       'ALL'),
('TONG_GIAM_DOC', 'ATTENDANCE_MANAGE',     'ALL'),
('TONG_GIAM_DOC', 'CONTRACT_VIEW',         'ALL'),
('TONG_GIAM_DOC', 'CONTRACT_CREATE',       'SELF'),
('TONG_GIAM_DOC', 'CONTRACT_UPDATE',       'ALL'),
('TONG_GIAM_DOC', 'CONTRACT_MANAGE',       'ALL'),
('TONG_GIAM_DOC', 'PAYROLL_VIEW',          'ALL'),
('TONG_GIAM_DOC', 'PAYROLL_CALCULATE',     'SELF'),
('TONG_GIAM_DOC', 'LEAVE_VIEW',            'ALL'),
('TONG_GIAM_DOC', 'LEAVE_CREATE',          'SELF'),
('TONG_GIAM_DOC', 'LEAVE_APPROVE',         'ALL'),
('TONG_GIAM_DOC', 'LEAVE_MANAGE',          'SELF'),
('TONG_GIAM_DOC', 'EVAL_VIEW',             'ALL'),
('TONG_GIAM_DOC', 'EVAL_MANAGE',           'SELF'),
('TONG_GIAM_DOC', 'EVAL_REVIEW',           'ALL'),
('TONG_GIAM_DOC', 'RECRUITMENT_VIEW',      'ALL'),
('TONG_GIAM_DOC', 'RECRUITMENT_REQUEST',   'SELF'),
('TONG_GIAM_DOC', 'RECRUITMENT_MANAGE',    'SELF'),
('TONG_GIAM_DOC', 'REPORT_VIEW',           'ALL'),
('TONG_GIAM_DOC', 'REPORT_EXPORT',         'SELF'),
('TONG_GIAM_DOC', 'NOTIFICATION_SEND',     'SELF'),
('TONG_GIAM_DOC', 'SETTINGS_VIEW',         'SELF'),
('TONG_GIAM_DOC', 'SETTINGS_UPDATE',       'SELF'),
('TONG_GIAM_DOC', 'USER_VIEW',             'SELF'),
('TONG_GIAM_DOC', 'ROLE_VIEW',             'SELF');

-- NHAN_SU: quản lý toàn bộ nghiệp vụ nhân sự
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('NHAN_SU', 'EMPLOYEE_VIEW',         'ALL'),
('NHAN_SU', 'EMPLOYEE_CREATE',       'SELF'),
('NHAN_SU', 'EMPLOYEE_UPDATE',       'ALL'),
('NHAN_SU', 'EMPLOYEE_RESIGN',       'SELF'),
('NHAN_SU', 'APPOINTMENT_VIEW',      'ALL'),
('NHAN_SU', 'APPOINTMENT_CREATE',    'SELF'),
('NHAN_SU', 'ATTENDANCE_VIEW',       'ALL'),
('NHAN_SU', 'ATTENDANCE_MANAGE',     'SELF'),
('NHAN_SU', 'CONTRACT_VIEW',         'ALL'),
('NHAN_SU', 'CONTRACT_CREATE',       'SELF'),
('NHAN_SU', 'CONTRACT_UPDATE',       'ALL'),
('NHAN_SU', 'CONTRACT_MANAGE',       'SELF'),
('NHAN_SU', 'LEAVE_VIEW',            'ALL'),
('NHAN_SU', 'LEAVE_CREATE',          'SELF'),
('NHAN_SU', 'LEAVE_MANAGE',          'SELF'),
('NHAN_SU', 'EVAL_VIEW',             'ALL'),
('NHAN_SU', 'EVAL_MANAGE',           'SELF'),
('NHAN_SU', 'RECRUITMENT_VIEW',      'ALL'),
('NHAN_SU', 'RECRUITMENT_REQUEST',   'SELF'),
('NHAN_SU', 'RECRUITMENT_MANAGE',    'SELF'),
('NHAN_SU', 'REPORT_VIEW',           'ALL'),
('NHAN_SU', 'REPORT_EXPORT',         'SELF'),
('NHAN_SU', 'PAYROLL_VIEW',          'ALL'),
('NHAN_SU', 'NOTIFICATION_SEND',     'SELF');

-- KE_TOAN: xem/tính lương, xem chấm công và hợp đồng toàn công ty
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('KE_TOAN', 'EMPLOYEE_VIEW',    'ALL'),
('KE_TOAN', 'ATTENDANCE_VIEW',  'ALL'),
('KE_TOAN', 'CONTRACT_VIEW',    'ALL'),
('KE_TOAN', 'PAYROLL_VIEW',     'ALL'),
('KE_TOAN', 'PAYROLL_CALCULATE','SELF'),
('KE_TOAN', 'LEAVE_VIEW',       'ALL'),
('KE_TOAN', 'LEAVE_CREATE',     'SELF'),
('KE_TOAN', 'REPORT_VIEW',      'SELF'),
('KE_TOAN', 'REPORT_EXPORT',    'SELF'),
('KE_TOAN', 'EVAL_VIEW',        'SELF');

-- =====================================================
-- 6. TÀI KHOẢN
-- =====================================================
-- Mapping vai trò:
--   TONG_GIAM_DOC : NV001 (GD)
--   TRUONG_PHONG  : NV002 (TP NS), NV004 (TP KT), NV006 (TP KD), NV008 (TP IT), NV012 (TP MKT)
--   QUAN_LY       : NV009 (TT TEAM_IT)
--   NHAN_SU       : NV003 (NSV)
--   KE_TOAN       : NV005 (KTV)
--   NHAN_VIEN     : NV007, NV010, NV011, NV013
INSERT INTO TAIKHOAN (tenDangNhap, matKhau, maNV, maVaiTro, email, hoatDong) VALUES
('admin',           '123', NULL,    'ADMIN',         'admin@abctech.vn',             TRUE),
('hung.nguyen',     '123', 'NV001', 'TONG_GIAM_DOC', 'hung.nguyen@abctech.vn',       TRUE),
('huong.nguyen',    '123', 'NV002', 'TRUONG_PHONG',  'huong.nguyen@abctech.vn',      TRUE),
('lananh.dang',     '123', 'NV003', 'NHAN_SU',       'lananh.dang@abctech.vn',       TRUE),
('ngoc.hoang',      '123', 'NV004', 'TRUONG_PHONG',  'ngoc.hoang@abctech.vn',        TRUE),
('thanhTam.ly',     '123', 'NV005', 'KE_TOAN',       'thanhTam.ly@abctech.vn',       TRUE),
('anh.tuan',        '123', 'NV006', 'TRUONG_PHONG',  'anh.tuan@abctech.vn',          TRUE),
('hoang.le',        '123', 'NV007', 'NHAN_VIEN',     'hoang.le@abctech.vn',          TRUE),
('son.dinh',        '123', 'NV008', 'TRUONG_PHONG',  'son.dinh@abctech.vn',          TRUE),
('khoa.nguyen',     '123', 'NV009', 'QUAN_LY',       'khoa.nguyen@abctech.vn',       TRUE),
('tri.hoang',       '123', 'NV010', 'NHAN_VIEN',     'tri.hoang@abctech.vn',         TRUE),
('camtu.vo',        '123', 'NV011', 'NHAN_VIEN',     'camtu.vo@abctech.vn',          TRUE),
('phuonglinh.le',   '123', 'NV012', 'TRUONG_PHONG',  'phuonglinh.le@abctech.vn',     TRUE),
('khang.pham',      '123', 'NV013', 'NHAN_VIEN',     'khang.pham@abctech.vn',        TRUE);

-- =====================================================
-- 7. HỢP ĐỒNG LAO ĐỘNG
-- =====================================================
INSERT INTO HOPDONGLAODONG (soHopDong, maNV, loaiHopDong, luongCoSo, ngayKy, ngayHieuLuc, ngayHetHieuLuc, trangThai, noiDung) VALUES
-- NV001 GD
('HD2015-GD-001',  'NV001', 'khong_xac_dinh', 80000000, '2015-01-03', '2015-01-05', NULL,         'hieu_luc', 'Hop dong Giam doc dieu hanh'),
-- Phòng NS
('HD2017-NS-002',  'NV002', 'khong_xac_dinh', 22000000, '2017-01-25', '2017-02-01', NULL,         'hieu_luc', 'Hop dong TP Nhan su'),
('HD2021-NS-003',  'NV003', 'xac_dinh_thoi_han', 10000000, '2021-05-28', '2021-06-01', '2023-06-01', 'het_han', 'Hop dong lan 1 NSV'),
('HD2023-NS-003',  'NV003', 'xac_dinh_thoi_han', 12000000, '2023-05-28', '2023-06-01', '2025-06-01', 'het_han', 'Hop dong lan 2 NSV'),
('HD2025-NS-003',  'NV003', 'xac_dinh_thoi_han', 13500000, '2025-05-28', '2025-06-01', '2027-06-01', 'hieu_luc', 'Hop dong lan 3 NSV'),
-- Phòng KT
('HD2017-KT-004',  'NV004', 'khong_xac_dinh', 25000000, '2017-03-28', '2017-04-01', NULL,         'hieu_luc', 'Hop dong TP Ke toan'),
('HD2021-KT-005',  'NV005', 'xac_dinh_thoi_han', 10000000, '2021-06-25', '2021-07-01', '2023-07-01', 'het_han', 'Hop dong lan 1 KTV'),
('HD2023-KT-005',  'NV005', 'xac_dinh_thoi_han', 12000000, '2023-06-25', '2023-07-01', '2025-07-01', 'het_han', 'Hop dong lan 2 KTV'),
('HD2025-KT-005',  'NV005', 'xac_dinh_thoi_han', 13500000, '2025-06-25', '2025-07-01', '2027-07-01', 'hieu_luc', 'Hop dong lan 3 KTV'),
-- Phòng KD
('HD2017-KD-006',  'NV006', 'khong_xac_dinh', 28000000, '2017-06-25', '2017-07-01', NULL,         'hieu_luc', 'Hop dong TP Kinh doanh'),
('HD2020-KD-007',  'NV007', 'xac_dinh_thoi_han', 13000000, '2020-05-28', '2020-06-01', '2022-06-01', 'het_han', 'Hop dong lan 1 CV KD'),
('HD2022-KD-007',  'NV007', 'xac_dinh_thoi_han', 16000000, '2022-05-28', '2022-06-01', '2024-06-01', 'het_han', 'Hop dong lan 2 CV KD'),
('HD2024-KD-007',  'NV007', 'xac_dinh_thoi_han', 18000000, '2024-05-28', '2024-06-01', '2026-06-01', 'hieu_luc', 'Hop dong lan 3 CV KD'),
-- Phòng IT
('HD2018-IT-008',  'NV008', 'khong_xac_dinh', 35000000, '2018-01-10', '2018-01-15', NULL,         'hieu_luc', 'Hop dong TP IT'),
('HD2019-IT-009',  'NV009', 'khong_xac_dinh', 28000000, '2019-07-28', '2019-08-01', NULL,         'hieu_luc', 'Hop dong TT TEAM_IT'),
('HD2022-IT-010',  'NV010', 'xac_dinh_thoi_han', 15000000, '2022-01-28', '2022-02-01', '2024-02-01', 'het_han', 'Hop dong lan 1 NV IT'),
('HD2024-IT-010',  'NV010', 'xac_dinh_thoi_han', 18000000, '2024-01-28', '2024-02-01', '2026-02-01', 'hieu_luc', 'Hop dong lan 2 NV IT'),
('HD2025-IT-011',  'NV011', 'thu_viec',           8500000, '2025-09-28', '2025-10-01', '2026-01-01', 'hieu_luc', 'Hop dong thu viec IT'),
-- Phòng Marketing
('HD2018-MKT-012', 'NV012', 'khong_xac_dinh', 22000000, '2018-06-25', '2018-07-01', NULL,         'hieu_luc', 'Hop dong TP Marketing'),
('HD2021-MKT-013', 'NV013', 'xac_dinh_thoi_han', 11000000, '2021-02-25', '2021-03-01', '2023-03-01', 'het_han', 'Hop dong lan 1 NV MKT'),
('HD2023-MKT-013', 'NV013', 'xac_dinh_thoi_han', 13000000, '2023-02-25', '2023-03-01', '2025-03-01', 'het_han', 'Hop dong lan 2 NV MKT'),
('HD2025-MKT-013', 'NV013', 'xac_dinh_thoi_han', 14500000, '2025-02-25', '2025-03-01', '2027-03-01', 'hieu_luc', 'Hop dong lan 3 NV MKT');

-- =====================================================
-- 8. CA LÀM
-- =====================================================
INSERT INTO CALAM (maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, choPhepLamThem, moTa, trangThai) VALUES
('HANH_CHINH', 'Ca hanh chinh', '08:00:00', '17:00:00', 8.00, TRUE,  'Ca lam viec hanh chinh van phong', 'hoatDong'),
('CA_SANG',    'Ca sáng',       '06:00:00', '14:00:00', 8.00, TRUE,  'Ca sang',                          'hoatDong'),
('CA_CHIEU',   'Ca chieu',      '14:00:00', '22:00:00', 8.00, TRUE,  'Ca chieu',                         'hoatDong'),
('CA_DEM',     'Ca dem',        '22:00:00', '06:00:00', 8.00, FALSE, 'Ca dem ky thuat',                  'hoatDong');

-- =====================================================
-- 9. CHẤM CÔNG - Tháng 1 & 2 / 2026
-- Ngày làm việc T1: 2,5,6,7,8,9,12,13,14,15,16,19,20,21,22,23,26,27,28,29,30 (21 ngày)
-- Ngày làm việc T2: 2,3,4,5,6,9,10,11,12,13,16,17,18,19,20,23,24,25,26,27   (19 ngày)
-- =====================================================

INSERT INTO CHAMCONG (maNV, ngay, maCaLam, gioVao, gioRa, soGioLam, gioLamThem, trangThai, phuongThucChamCong, ghiChu) VALUES
-- *** NV001 (GD) - T1: đủ công, có 2 ngày làm thêm ***
('NV001','2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 18:00',8.00,1.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 18:30',8.00,1.50,'dung_gio','the_tu',NULL),
('NV001','2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
-- *** NV002 (TP NS) - T1: đủ công, 1 ngày đi muộn, 1 ngày nghỉ phép ***
('NV002','2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-05','HANH_CHINH','2026-01-05 08:25','2026-01-05 17:00',7.58,0.00,'di_muon','van_tay','Di muon 25 phut'),
('NV002','2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-14',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Phep nam'),
('NV002','2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV002','2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
-- *** NV008 (TP IT) - T1: đủ công, làm thêm nhiều ***
('NV008','2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 19:00',8.00,2.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 19:00',8.00,2.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV008','2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
-- *** NV009 (TT TEAM_IT) - T1: 1 ngày nghỉ ốm ***
('NV009','2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-08',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Nghi om'),
('NV009','2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV009','2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
-- *** NV010 (NV TEAM_IT) - T1: làm thêm nhiều, 1 ngày vắng mặt ***
('NV010','2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 19:30',8.00,2.50,'dung_gio','van_tay',NULL),
('NV010','2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-06',NULL,NULL,NULL,0.00,0.00,'vang_mat','thu_cong','Vang mat khong ly do'),
('NV010','2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 20:00',8.00,3.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 18:00',8.00,1.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
-- *** Các NV còn lại T1: đủ công bình thường (NV003,004,005,006,007,011,012,013) ***
-- NV003 (NSV)
('NV003','2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV003','2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
-- NV011 (TV - thử việc): chỉ từ 01/10/2025, T1/2026 đủ công
('NV011','2026-01-02','HANH_CHINH','2026-01-02 08:05','2026-01-02 17:00',7.92,0.00,'dung_gio','van_tay','Vao muon 5 phut'),
('NV011','2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV011','2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_gio','van_tay',NULL);

-- *** THÁNG 2/2026 - chọn đại diện NV001, NV008, NV009, NV010 ***
INSERT INTO CHAMCONG (maNV, ngay, maCaLam, gioVao, gioRa, soGioLam, gioLamThem, trangThai, phuongThucChamCong, ghiChu) VALUES
-- NV001 T2 (đủ công)
('NV001','2026-02-02','HANH_CHINH','2026-02-02 08:00','2026-02-02 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-03','HANH_CHINH','2026-02-03 08:00','2026-02-03 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-04','HANH_CHINH','2026-02-04 08:00','2026-02-04 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-05','HANH_CHINH','2026-02-05 08:00','2026-02-05 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-06','HANH_CHINH','2026-02-06 08:00','2026-02-06 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-09','HANH_CHINH','2026-02-09 08:00','2026-02-09 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-10','HANH_CHINH','2026-02-10 08:00','2026-02-10 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-11','HANH_CHINH','2026-02-11 08:00','2026-02-11 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-12','HANH_CHINH','2026-02-12 08:00','2026-02-12 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-13','HANH_CHINH','2026-02-13 08:00','2026-02-13 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-16','HANH_CHINH','2026-02-16 08:00','2026-02-16 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-17','HANH_CHINH','2026-02-17 08:00','2026-02-17 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-18','HANH_CHINH','2026-02-18 08:00','2026-02-18 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-19','HANH_CHINH','2026-02-19 08:00','2026-02-19 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-20','HANH_CHINH','2026-02-20 08:00','2026-02-20 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-23','HANH_CHINH','2026-02-23 08:00','2026-02-23 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-24','HANH_CHINH','2026-02-24 08:00','2026-02-24 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-25','HANH_CHINH','2026-02-25 08:00','2026-02-25 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-26','HANH_CHINH','2026-02-26 08:00','2026-02-26 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
('NV001','2026-02-27','HANH_CHINH','2026-02-27 08:00','2026-02-27 17:00',8.00,0.00,'dung_gio','the_tu',NULL),
-- NV010 T2: có đơn nghỉ phép 10-13/02
('NV010','2026-02-02','HANH_CHINH','2026-02-02 08:00','2026-02-02 19:00',8.00,2.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-03','HANH_CHINH','2026-02-03 08:00','2026-02-03 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-04','HANH_CHINH','2026-02-04 08:00','2026-02-04 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-05','HANH_CHINH','2026-02-05 08:00','2026-02-05 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-06','HANH_CHINH','2026-02-06 08:00','2026-02-06 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-09','HANH_CHINH','2026-02-09 08:00','2026-02-09 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-10',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Nghi phep co phep 10-13/02'),
('NV010','2026-02-11',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Nghi phep co phep 10-13/02'),
('NV010','2026-02-12',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Nghi phep co phep 10-13/02'),
('NV010','2026-02-13',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Nghi phep co phep 10-13/02'),
('NV010','2026-02-16','HANH_CHINH','2026-02-16 08:00','2026-02-16 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-17','HANH_CHINH','2026-02-17 08:00','2026-02-17 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-18','HANH_CHINH','2026-02-18 08:00','2026-02-18 18:30',8.00,1.50,'dung_gio','van_tay',NULL),
('NV010','2026-02-19','HANH_CHINH','2026-02-19 08:00','2026-02-19 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-20','HANH_CHINH','2026-02-20 08:00','2026-02-20 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-23','HANH_CHINH','2026-02-23 08:00','2026-02-23 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-24','HANH_CHINH','2026-02-24 08:00','2026-02-24 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-25','HANH_CHINH','2026-02-25 08:00','2026-02-25 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-26','HANH_CHINH','2026-02-26 08:00','2026-02-26 17:00',8.00,0.00,'dung_gio','van_tay',NULL),
('NV010','2026-02-27','HANH_CHINH','2026-02-27 08:00','2026-02-27 17:00',8.00,0.00,'dung_gio','van_tay',NULL);

-- =====================================================
-- 10. ĐĂNG KÝ LÀM THÊM
-- =====================================================
INSERT INTO DANGKY_LAMTHEM (maNV, ngay, soGio, heSoOT, lyDo, nguoiDuyet, ngayDuyet, trangThai) VALUES
('NV010', '2026-01-02', 2.50, 1.50, 'Sprint deadline du an A',      'NV009', '2026-01-01', 'da_duyet'),
('NV010', '2026-01-07', 3.00, 1.50, 'Fix bug urgent production',    'NV009', '2026-01-06', 'da_duyet'),
('NV010', '2026-01-09', 1.00, 1.50, 'Review code truoc release',    'NV009', '2026-01-08', 'da_duyet'),
('NV008', '2026-01-02', 2.00, 1.50, 'Hop review Q4 bao cao nam',    'NV001', '2025-12-31', 'da_duyet'),
('NV008', '2026-01-07', 2.00, 1.50, 'Chuan bi kick-off du an moi',  'NV001', '2026-01-06', 'da_duyet'),
('NV010', '2026-02-18', 1.50, 1.50, 'Bu lai sau nghi phep',         'NV009', '2026-02-17', 'da_duyet');

-- =====================================================
-- 11. LOAI PHEP
-- =====================================================
INSERT INTO LOAIPHEP (maLoaiPhep, tenLoaiPhep, coLuong, canChungTu, soNgayToiDa, moTa, trangThai) VALUES
('PHEP_NAM',         'Nghi phep nam',     TRUE,  FALSE, 12,  'Phep nam theo luat lao dong',       'hoatDong'),
('PHEP_OM',          'Nghi om',           TRUE,  TRUE,  30,  'Nghi om co giay benh vien',         'hoatDong'),
('PHEP_CUOI',        'Nghi cuoi',         TRUE,  TRUE,   3,  'Nghi cuoi ban than hoac con',       'hoatDong'),
('PHEP_TANG',        'Nghi tang',         TRUE,  TRUE,   3,  'Nghi tang cha me vo chong con',     'hoatDong'),
('PHEP_THAI_SAN',    'Nghi thai san',     TRUE,  TRUE, 180,  'Nghi thai san theo luat BHXH',      'hoatDong'),
('PHEP_KHONG_LUONG', 'Nghi khong luong',  FALSE, FALSE,  0,  'Nghi phep khong huong luong',       'hoatDong');

-- =====================================================
-- 12. Sổ Dư Nghỉ PHÉP
-- =====================================================
-- Mỗi NV có thể có nhiều loại phép trong 1 năm (PHEP_NAM là chính, PHEP_OM khi cần)
INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) VALUES
-- Phép năm 2026 (gốc 12 ngày, một số TP được thêm 1-2 ngày)
('NV001', 2026, 'PHEP_NAM', 15, 1),
('NV002', 2026, 'PHEP_NAM', 13, 1),
('NV003', 2026, 'PHEP_NAM', 12, 0),
('NV004', 2026, 'PHEP_NAM', 13, 0),
('NV005', 2026, 'PHEP_NAM', 12, 0),
('NV006', 2026, 'PHEP_NAM', 13, 0),
('NV007', 2026, 'PHEP_NAM', 12, 1),
('NV008', 2026, 'PHEP_NAM', 14, 1),
('NV009', 2026, 'PHEP_NAM', 12, 0),
('NV009', 2026, 'PHEP_OM',  30, 1),
('NV010', 2026, 'PHEP_NAM', 12, 4),
('NV011', 2026, 'PHEP_NAM',  4, 0),
('NV012', 2026, 'PHEP_NAM', 13, 0),
('NV013', 2026, 'PHEP_NAM', 12, 0);

-- =====================================================
-- 13. ĐƠN XIN NGHỈ PHÉP
-- =====================================================
-- Cot: maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi, lyDo, fileDinhKem, nguoiDuyet, ngayDuyet, lyDoTuChoi, trangThai
INSERT INTO DONXINNGHIPHEP (maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi, lyDo, fileDinhKem, nguoiDuyet, ngayDuyet, lyDoTuChoi, trangThai) VALUES
('NV002', 'PHEP_NAM', '2026-01-14', '2026-01-14', 1, 'Viec ca nhan',                    NULL, 'NV001', '2026-01-13', NULL, 'da_duyet'),
('NV009', 'PHEP_OM',  '2026-01-08', '2026-01-08', 1, 'Om cap toc',                      NULL, 'NV008', '2026-01-08', NULL, 'da_duyet'),
('NV010', 'PHEP_NAM', '2026-02-10', '2026-02-13', 4, 'Du lich nghi Tet Duong lich muon', NULL, 'NV009', '2026-02-07', NULL, 'da_duyet'),
('NV007', 'PHEP_NAM', '2026-02-16', '2026-02-16', 1, 'Viec gia dinh',                   NULL, NULL,    NULL,         NULL, 'cho_duyet');

-- =====================================================
-- 14. BẢNG LƯƠNG
-- =====================================================
-- trangThai ENUM: 'dang_xu_ly' | 'da_duyet' | 'da_khoa'
-- nguoiTao/nguoiDuyet là INT (id auto-increment của NHANVIEN), để NULL khi không xác định
INSERT INTO BANGLUONG (thang, nam, tenBangLuong, nguoiTao, nguoiDuyet, ngayDuyet, trangThai) VALUES
(11, 2025, 'Bang luong thang 11/2025', NULL, NULL, '2025-12-03', 'da_khoa'),
(12, 2025, 'Bang luong thang 12/2025', NULL, NULL, '2026-01-03', 'da_khoa'),
( 1, 2026, 'Bang luong thang 01/2026', NULL, NULL, '2026-02-03', 'da_duyet'),
( 2, 2026, 'Bang luong thang 02/2026', NULL, NULL, NULL,         'dang_xu_ly');

-- Chi tiet luong T1/2026 (maBangLuong = 3, mau 6 NV dai dien)
-- Cot: maBangLuong, maNV, luongCoSo, tongLuongChucVu, luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem
INSERT INTO CHITIETLUONG (maBangLuong, maNV, luongCoSo, tongLuongChucVu, luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem) VALUES
(3, 'NV001', 80000000, 15000000,  3030000, 98030000, 37060000,  60970000, 20,  2.5),
(3, 'NV002', 22000000,  5000000,        0, 27000000,  8993000,  18007000, 19,  0.0),
(3, 'NV008', 35000000,  5000000,  2188000, 42188000, 13895000,  28293000, 21,  4.0),
(3, 'NV009', 28000000,  2000000,        0, 30000000, 10006000,  19994000, 20,  0.0),
(3, 'NV010', 18000000,        0,  1463000, 19463000,  4753000,  14710000, 20,  6.5),
(3, 'NV011',  8500000,        0,        0,  8500000,  1838000,   6662000, 21,  0.0);

-- =====================================================
-- 15. ĐÁNH GIÁ HIỆU SUẤT
-- =====================================================
INSERT INTO DOTDANHGIA (tenDot, nam, kyDanhGia, tuNgay, denNgay, moTa, trangThai) VALUES
('Danh gia hieu suat Q3/2025', 2025, 'quy_3', '2025-09-01', '2025-09-30', 'Danh gia Q3/2025',             'da_ket_thuc'),
('Danh gia cuoi nam 2025',     2025, 'nam',   '2025-12-01', '2025-12-31', 'Tong ket KPI nam 2025',        'da_ket_thuc'),
('Danh gia hieu suat Q1/2026', 2026, 'quy_1', '2026-04-01', '2026-04-30', 'Chuan bi dot danh gia Q1/2026','chua_bat_dau');

INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, nhanXetChung, ngayDanhGia, trangThai) VALUES
-- Q3/2025
(1, 'NV008', 'NV001', 8.7, 'tot',      'TP IT chu dong xu ly su co, dam bao tien do du an',   '2025-09-30', 'da_xac_nhan'),
(1, 'NV009', 'NV008', 8.2, 'tot',      'TT nhom phat trien on dinh, giao tiep tot',            '2025-09-30', 'da_xac_nhan'),
(1, 'NV010', 'NV009', 7.3, 'kha',      'NV co gang nhung can cai thien dung gio',              '2025-09-30', 'da_xac_nhan'),
(1, 'NV011', 'NV009', 7.3, 'kha',      'Thu viec tich cuc, hoc nhanh',                         '2025-09-30', 'da_xac_nhan'),
-- Cuoi nam 2025
(2, 'NV008', 'NV001', 8.8, 'xuat_sac', 'Du an lon hoan thanh dung han, chat luong cao',        '2025-12-31', 'da_xac_nhan'),
(2, 'NV009', 'NV008', 8.3, 'tot',      'Hoan thanh tot nhiem vu TT, team tinh than cao',       '2025-12-31', 'da_xac_nhan'),
(2, 'NV010', 'NV009', 7.6, 'kha',      'Tien bo ro ret, can han che vang mat',                 '2025-12-31', 'da_xac_nhan');



INSERT INTO TIEUCHIDANHGIA (tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai) VALUES
('Chat luong cong viec',    'Chat luong dau ra, sản phẩm, dich vu cung cap',       'Ket qua',  10, 'hoatDong'),
('Tien do hoan thanh',      'Hoan thanh dung han, khong tre deadline',              'Ket qua',  10, 'hoatDong'),
('Kha nang sáng tao',       'De xuat giai phap, cai tien quy trinh',               'Nang luc', 10, 'hoatDong'),
('Kỹ năng chuyen mon',      'Trinh do chuyen mon, kỹ năng ky thuat',               'Nang luc', 10, 'hoatDong'),
('Lam viec nhom',           'Phoi hop, ho tro dong nghiep, tinh than team',        'Thai do',  10, 'hoatDong'),
('Tuan thu noi quy',        'Chap hanh quy che, di lam dung giờ, tac phong',       'Thai do',  10, 'hoatDong'),
('Phát triển ban than',     'Hoc hoi kỹ năng mới, nang cao trinh do',              'Nang luc', 10, 'hoatDong');

-- =====================================================
-- 15. TUYỂN DỤNG
-- =====================================================
-- Bước 1: Tạo yêu cầu tuyển dụng (YEUCAUTUYENDUNG)
INSERT INTO YEUCAUTUYENDUNG (maPhongBan, maChucVu, soLuong, lyDo, mucLuongDuKien, yeuCauKinhNghiem, yeuCauHocVan, hanTuyenDung, nguoiDuyet, ngayDuyet, trangThai) VALUES
('PHONGIT',  'NV',  1, 'Mo rong team, tang tai nang IT',    '20-30 trieu',  'Java/Spring Boot, 3+ nam KN', 'Dai hoc CNTT',      '2026-02-28', NULL, '2026-01-12', 'da_duyet'),
('PHONGKT',  'KTV', 1, 'Bo sung nhan luc phong KT',          '10-15 trieu',  'Ke toan tong hop 2+ nam',     'Dai hoc Ke toan',   '2026-03-15', NULL, '2026-01-18', 'da_duyet'),
('PHONGNS',  'NSV', 1, 'Tang cuong tuyen dung cho Q2',       '9-13 trieu',   'Tuyen dung, C&B, Excel',      'Dai hoc QTKD/NS',   '2026-02-15', NULL, '2026-01-22', 'da_duyet'),
('PHONGMKT', 'NV',  1, 'Tuyen them NV cho chien dich H1',    '10-15 trieu',  'Digital marketing, SEO/SEM',  'Dai hoc Marketing', '2026-03-31', NULL, NULL,         'cho_duyet');

-- Bước 2: Đăng tin tuyển dụng (TINTUYENDUNG) - tương ứng với maYeuCau 1,2,3,4
INSERT INTO TINTUYENDUNG (maYeuCau, tieuDe, noiDung, mucLuong, diaDiem, hanNopHoSo, trangThai) VALUES
(1, 'Tuyen Senior IT Developer',   'Phat trien he thong, Java/Spring Boot, 3+ nam KN',        '20-30 trieu',  'TP.HCM', '2026-02-28', 'dang_tuyen'),
(2, 'Tuyen Ke toan vien',          'Ke toan tong hop, thue, co kinh nghiem 2+ nam',           '10-15 trieu',  'TP.HCM', '2026-03-15', 'dang_tuyen'),
(3, 'Tuyen Nhan su vien',          'Tuyen dung, C&B, thong thao Excel',                        '9-13 trieu',   'TP.HCM', '2026-02-15', 'tam_dung'),
(4, 'Tuyen Marketing Executive',   'Digital marketing, SEO/SEM, content',                     '10-15 trieu',  'TP.HCM', '2026-03-31', 'dang_tuyen');

-- Bước 3: Ứng viên (UNGVIEN) - maTin tham chiếu TINTUYENDUNG
INSERT INTO UNGVIEN (maTin, hoTen, email, dienThoai, ngaySinh, gioiTinh, trinhDoHocVan, kinhNghiem, nguonUngTuyen, trangThai, nhanXet) VALUES
(1, 'Tran Van An',    'an.tran@gmail.com',   '0912345001', '1992-05-10', 'nam', 'Dai hoc CNTT', '5 nam Java/Spring Boot',     'LinkedIn',   'dang_phong_van', 'Ung vien tiem nang Senior Java'),
(1, 'Nguyen Thi Be',  'be.nguyen@gmail.com', '0912345002', '1990-08-15', 'nu',  'Dai hoc CNTT', 'Fullstack, kinh nghiem 5 nam','TopCV',      'dang_phong_van', 'Fullstack, kinh nghiem 5 nam'),
(2, 'Le Minh Cuong',  'cuong.le@gmail.com',  '0912345003', '1995-03-22', 'nam', 'Dai hoc KT',   'Ke toan tong hop 3 nam',     'VietnamWorks','moi',            NULL),
(3, 'Pham Thi Dieu',  'dieu.pham@gmail.com', '0912345004', '1993-11-01', 'nu',  'Dai hoc QTKD', 'NS tuyen dung cong ty lon',  'Referral',   'moi',            'NSV truoc day o cong ty lon'),
(4, 'Hoang Van Em',   'em.hoang@gmail.com',  '0912345005', '1997-07-18', 'nam', 'Dai hoc MKT',  'Digital marketing 2 nam',    'TopCV',      'moi',            NULL);

-- =====================================================
-- 16. THÔNG BÁO
-- =====================================================
-- maTaiKhoan: 1=admin, 2=NV001(GD), 3=NV002(TP NS), 4=NV003(NSV),
--             5=NV004(TP KT), 6=NV005(KTV), 7=NV006(TP KD), 8=NV007(CV KD)
--             9=NV008(TP IT), 10=NV009(TT), 11=NV010(NV IT), 12=NV011(TV IT)
--             13=NV012(TP MKT), 14=NV013(NV MKT)
INSERT INTO THONGBAO (tieuDe, noiDung, loaiThongBao, maTaiKhoanGui, maTaiKhoanNhan, daDoc, ngayDoc) VALUES
('Chúc mừng năm mới 2026!',
 'BGD Cong ty TNHH ABC Technology chan thanh chuc toan the CBNV mot nam moi 2026 an khang thinh vuong!',
 'thong_bao_chung', 2, 3,  TRUE,  '2026-01-02 09:00:00'),
('Chúc mừng năm mới 2026!',
 'BGD Cong ty TNHH ABC Technology chan thanh chuc toan the CBNV mot nam moi 2026 an khang thinh vuong!',
 'thong_bao_chung', 2, 9,  TRUE,  '2026-01-02 09:15:00'),
('Chúc mừng năm mới 2026!',
 'BGD Cong ty TNHH ABC Technology chan thanh chuc toan the CBNV mot nam moi 2026 an khang thinh vuong!',
 'thong_bao_chung', 2, 13, FALSE, NULL),
('Chinh sach WFH Q1/2026',
 'Thu 4 hang tuan la ngay WFH. Vui long dang ky truoc 8h sang.',
 'thong_bao_chung', 1, 3,  TRUE,  '2026-01-05 10:00:00'),
('Chinh sach WFH Q1/2026',
 'Thu 4 hang tuan la ngay WFH. Vui long dang ky truoc 8h sang.',
 'thong_bao_chung', 1, 9,  TRUE,  '2026-01-05 09:45:00'),
('Bảng lương T12/2025 đã sẵn sàng',
 'BL thang 12/2025 da duoc phe duyet. Vui long dang nhap de kiem tra.',
 'he_thong', 1, 2,  TRUE,  '2026-01-06 09:00:00'),
('Bảng lương T12/2025 đã sẵn sàng',
 'BL thang 12/2025 da duoc phe duyet. Vui long dang nhap de kiem tra.',
 'he_thong', 1, 9,  TRUE,  '2026-01-06 10:00:00'),
('Bảng lương T01/2026 đã sẵn sàng',
 'BL thang 01/2026 da duoc phe duyet. Vui long dang nhap de kiem tra.',
 'he_thong', 1, 2,  FALSE, NULL),
('Bảng lương T01/2026 đã sẵn sàng',
 'BL thang 01/2026 da duoc phe duyet. Vui long dang nhap de kiem tra.',
 'he_thong', 1, 9,  FALSE, NULL),
('Đơn nghỉ phép đã được phê duyệt',
 'Don nghi phep ngay 14/01/2026 cua ban da duoc phe duyet. Chuc ban nghi vui!',
 'don_tu', 1, 3,    TRUE,  '2026-01-13 17:00:00'),
('Đơn nghỉ phép đã được phê duyệt',
 'Don nghi phep 10-13/02/2026 cua ban da duoc phe duyet.',
 'don_tu', 10, 11,  FALSE, NULL),
('Yêu cầu tuyển dụng mới cần phê duyệt',
 'NV008 (TP IT) gui yeu cau tuyen 1 Senior Developer. Vui long xem xet.',
 'don_tu', 3, 2,    TRUE,  '2026-01-12 09:00:00'),
('Lịch đánh giá Q1/2026 sắp diễn ra',
 'Dot danh gia hieu suat Q1/2026 bat dau 06/04/2026. Moi CBQL chuan bi tieu chi.',
 'thong_bao_chung', 1, 9,   FALSE, NULL),
('Lịch đánh giá Q1/2026 sắp diễn ra',
 'Dot danh gia hieu suat Q1/2026 bat dau 06/04/2026. Moi CBQL chuan bi tieu chi.',
 'thong_bao_chung', 1, 7,   FALSE, NULL);

-- =====================================================
-- 17. CẤU HÌNH PHỤ CẤP
-- =====================================================
DELETE FROM CAUHINH_PHUCAP;
INSERT INTO CAUHINH_PHUCAP (loai, tenKhoan, kieuTinh, giaTri, nguon, hoatDong) VALUES
('phu_cap', 'Phu cap an trua',              'co_dinh',   750000, 'CongTy',   1),
('phu_cap', 'Phu cap dien thoai',           'co_dinh',   500000, 'CongTy',   1),
('phu_cap', 'Phu cap di lai',               'co_dinh',   600000, 'CongTy',   1),
('phu_cap', 'Phu cap tham nien (3-5 nam)',  'co_dinh',   500000, 'CongTy',   1),
('phu_cap', 'Phu cap tham nien (5+ nam)',   'co_dinh',  1000000, 'CongTy',   1),
('phu_cap', 'Thuong hieu qua hang thang',   'phan_tram',   5.00, 'CongTy',   1),
('khau_tru','BHXH NLD (8%)',               'phan_tram',   8.00, 'LuatDinh', 1),
('khau_tru','BHYT NLD (1.5%)',             'phan_tram',   1.50, 'LuatDinh', 1),
('khau_tru','BHTN NLD (1%)',               'phan_tram',   1.00, 'LuatDinh', 1),
('khau_tru','Thue TNCN',                   'phan_tram',   0.00, 'LuatDinh', 1);

-- =====================================================
-- 18. LOG AUDIT
-- =====================================================
INSERT INTO LOG_AUDIT (maTaiKhoan, hanhDong, bangDuLieu, maBanGhi, diaChiIP, userAgent) VALUES
(2,  'LOGIN',   NULL,              NULL, '192.168.1.10', 'Mozilla/5.0 Chrome/121'),
(2,  'APPROVE', 'YEUCAUTUYENDUNG', '1',  '192.168.1.10', 'Mozilla/5.0 Chrome/121'),
(2,  'APPROVE', 'YEUCAUTUYENDUNG', '2',  '192.168.1.10', 'Mozilla/5.0 Chrome/121'),
(2,  'APPROVE', 'YEUCAUTUYENDUNG', '3',  '192.168.1.10', 'Mozilla/5.0 Chrome/121'),
(1,  'LOGIN',   NULL,              NULL, '192.168.1.1',  'Mozilla/5.0 Chrome/121'),
(1,  'CREATE',  'BANGLUONG',       '3',  '192.168.1.1',  'Mozilla/5.0 Chrome/121'),
(1,  'UPDATE',  'BANGLUONG',       '3',  '192.168.1.1',  'Mozilla/5.0 Chrome/121'),
(1,  'CREATE',  'CHITIETLUONG',    '1',  '192.168.1.1',  'Mozilla/5.0 Chrome/121'),
(1,  'UPDATE',  'DONXINNGHIPHEP',  '1',  '192.168.1.1',  'Mozilla/5.0 Chrome/121'),
(9,  'LOGIN',   NULL,              NULL, '192.168.1.30', 'Mozilla/5.0 Firefox/122'),
(9,  'APPROVE', 'DONXINNGHIPHEP',  '2',  '192.168.1.30', 'Mozilla/5.0 Firefox/122'),
(9,  'UPDATE',  'DANHGIAHIEUSUAT', '4',  '192.168.1.30', 'Mozilla/5.0 Firefox/122'),
(9,  'APPROVE', 'DANGKY_LAMTHEM',  '1',  '192.168.1.30', 'Mozilla/5.0 Firefox/122'),
(9,  'APPROVE', 'DANGKY_LAMTHEM',  '2',  '192.168.1.30', 'Mozilla/5.0 Firefox/122'),
(9,  'APPROVE', 'DANGKY_LAMTHEM',  '3',  '192.168.1.30', 'Mozilla/5.0 Firefox/122'),
(3,  'LOGIN',   NULL,              NULL, '192.168.1.20', 'Mozilla/5.0 Chrome/121'),
(3,  'UPDATE',  'DONXINNGHIPHEP',  '1',  '192.168.1.20', 'Mozilla/5.0 Chrome/121'),
(7,  'LOGIN',   NULL,              NULL, '192.168.1.40', 'Mozilla/5.0 Safari/17'),
(7,  'APPROVE', 'DONXINNGHIPHEP',  '3',  '192.168.1.40', 'Mozilla/5.0 Safari/17'),
(2,  'APPROVE', 'DONXINNGHIPHEP',  '3',  '192.168.1.10', 'Mozilla/5.0 Chrome/121'),
(4,  'LOGIN',   NULL,              NULL, '10.0.0.5',     'Mobile Safari iOS/17'),
(4,  'CREATE',  'NHANVIEN',        '13', '10.0.0.5',     'Mobile Safari iOS/17');

-- =====================================================
-- KIỂM TRA KẾT QUẢ
-- =====================================================
SELECT '=== HRM Sample Data V3 - Inserted Successfully! ===' AS Message;
SELECT CONCAT('Tong so nhan vien: ',   COUNT(*)) AS Info FROM NHANVIEN;
SELECT CONCAT('Tong so phong ban: ',   COUNT(*)) AS Info FROM PHONGBAN;
SELECT CONCAT('Tong so hop dong: ',    COUNT(*)) AS Info FROM HOPDONGLAODONG;
SELECT CONCAT('Tong so cham cong: ',   COUNT(*)) AS Info FROM CHAMCONG;
SELECT CONCAT('Tong so don nghi phep:',COUNT(*)) AS Info FROM DONXINNGHIPHEP;
SELECT CONCAT('Tong so ung vien: ',    COUNT(*)) AS Info FROM UNGVIEN;