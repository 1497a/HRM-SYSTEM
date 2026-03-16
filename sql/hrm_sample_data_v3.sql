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
('CONGTY',   'Công ty TNHH ABC Technology',  NULL,      'Công ty công nghệ phần mềm và giải pháp CNTT',             'hoatDong'),
('PHONGNS',  'Phòng Nhân sự',                'CONGTY',  'Quản lý nhân sự, tuyển dụng, lương thưởng',                'hoatDong'),
('PHONGKT',  'Phòng Kế toán - Tài chính',    'CONGTY',  'Quản lý tài chính, kế toán, thuế',                        'hoatDong'),
('PHONGKD',  'Phòng Kinh doanh',             'CONGTY',  'Phát triển kinh doanh, bán hàng, chăm sóc khách hàng',    'hoatDong'),
('PHONGIT',  'Phòng Công nghệ thông tin',    'CONGTY',  'Phát triển phần mềm, hệ thống CNTT, bảo mật',             'hoatDong'),
('PHONGMKT', 'Phòng Marketing',              'CONGTY',  'Truyền thông, quảng cáo, thương hiệu, digital marketing', 'hoatDong'),
('TEAM_IT',  'Team Phát triển IT',           'PHONGIT', 'Phát triển ứng dụng, API, kiểm thử phần mềm',             'hoatDong');

-- =====================================================
-- 2. CHUC VU
-- =====================================================
DELETE FROM CHUCVU;

INSERT INTO CHUCVU (maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu, moTa, trangThai) VALUES
('GD',  'Giám đốc',       1, 5.00, 15000000, 'Cấp lãnh đạo cao nhất công ty',                     'hoatDong'),
('TP',  'Trưởng phòng',   2, 3.00,  5000000, 'Quản lý cấp phòng ban',                              'hoatDong'),
('TT',  'Trưởng nhóm',    3, 2.20,  2000000, 'Quản lý cấp team / nhóm',                            'hoatDong'),
('CV',  'Chuyên viên',    4, 1.70,   500000, 'Nhân viên có kinh nghiệm từ 2 năm trở lên',         'hoatDong'),
('KTV', 'Kế toán viên',   5, 1.30,   200000, 'Nhân viên chuyên môn kế toán - tài chính',          'hoatDong'),
('NSV', 'Nhân sự viên',   5, 1.30,   200000, 'Nhân viên chuyên môn nhân sự - tuyển dụng',         'hoatDong'),
('NV',  'Nhân viên',      5, 1.30,   200000, 'Nhân viên chính thức các phòng ban khác',           'hoatDong'),
('TV',  'Thử việc',       6, 0.85,        0, 'Nhân sự đang trong thời gian thử việc',              'hoatDong');

-- =====================================================
-- 3. NHAN VIEN - 13 nhân viên
-- =====================================================
INSERT INTO NHANVIEN (maNV, loaiHopDong, ngayVaoLam, trangThai, ghiChu) VALUES
('admin', 'khong_xac_dinh',    '2015-01-01', 'dang_lam_viec', 'Tài khoản hệ thống'),
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
('admin', 'Quản trị viên',          NULL,         'khac',NULL,            NULL,         'admin@abctech.vn',         NULL,                                  NULL,                                  NULL,          NULL,   NULL,         'doc_than'),
('NV001', 'Nguyễn Đức Hùng',      '1978-05-12', 'nam', '001078005121', '0901000001', 'hung.nguyen@abctech.vn',    '10 Lê Duẩn, Q1, TP.HCM',             '10 Lê Duẩn, Q1, TP.HCM',             'Hà Nội',      'Kinh', 'Không',      'da_ket_hon'),
('NV002', 'Nguyễn Thị Thu Hương', '1985-07-25', 'nu',  '079085007251', '0901000002', 'huong.nguyen@abctech.vn',   '22 Lý Tự Trọng, Q1, TP.HCM',         '22 Lý Tự Trọng, Q1, TP.HCM',         'Nghệ An',     'Kinh', 'Phật giáo',  'da_ket_hon'),
('NV003', 'Đặng Thị Lan Anh',     '1992-02-18', 'nu',  '079092002181', '0901000003', 'lananh.dang@abctech.vn',    '67 Nguyễn Thị Minh Khai, Q3',        '67 Nguyễn Thị Minh Khai, Q3',        'Bình Định',   'Kinh', 'Không',      'doc_than'),
('NV004', 'Hoàng Thị Bích Ngọc',  '1984-12-03', 'nu',  '079084012031', '0901000004', 'ngoc.hoang@abctech.vn',     '45 Nam Kỳ Khởi Nghĩa, Q3',           '45 Nam Kỳ Khởi Nghĩa, Q3',           'Huế',         'Kinh', 'Phật giáo',  'da_ket_hon'),
('NV005', 'Lý Thị Thanh Tâm',     '1993-01-29', 'nu',  '079093001291', '0901000005', 'thanhTam.ly@abctech.vn',    '56 Bạch Đằng, BT, TP.HCM',           '56 Bạch Đằng, BT, TP.HCM',           'Bến Tre',     'Kinh', 'Không',      'doc_than'),
('NV006', 'Nguyễn Anh Tuấn',      '1983-06-14', 'nam', '079083006141', '0901000006', 'anh.tuan@abctech.vn',       '22 Pasteur, Q1, TP.HCM',             '22 Pasteur, Q1, TP.HCM',             'Hà Nội',      'Kinh', 'Không',      'da_ket_hon'),
('NV007', 'Lê Minh Hoàng',        '1991-12-25', 'nam', '079091012251', '0901000007', 'hoang.le@abctech.vn',       '15 CMT8, Q10, TP.HCM',               '15 CMT8, Q10, TP.HCM',               'Bình Dương',  'Kinh', 'Không',      'da_ket_hon'),
('NV008', 'Đinh Quang Sơn',       '1982-08-30', 'nam', '079082008301', '0901000008', 'son.dinh@abctech.vn',       '10 Nguyễn Văn Cừ, Q5, TP.HCM',      '10 Nguyễn Văn Cừ, Q5, TP.HCM',      'Hải Phòng',   'Kinh', 'Không',      'da_ket_hon'),
('NV009', 'Nguyễn Văn Khoa',      '1990-04-05', 'nam', '079090004051', '0901000009', 'khoa.nguyen@abctech.vn',    '26 Trương Định, Q3, TP.HCM',         '26 Trương Định, Q3, TP.HCM',         'Quảng Ngãi',  'Kinh', 'Không',      'da_ket_hon'),
('NV010', 'Hoàng Minh Trí',       '1995-07-01', 'nam', '079095007011', '0901000010', 'tri.hoang@abctech.vn',      '38 Võ Văn Tần, Q3, TP.HCM',          '38 Võ Văn Tần, Q3, TP.HCM',          'Hà Nội',      'Kinh', 'Không',      'doc_than'),
('NV011', 'Võ Thị Cẩm Tú',        '2001-08-07', 'nu',  '079101008071', '0901000011', 'camtu.vo@abctech.vn',       '25 Đinh Tiên Hoàng, BT, TP.HCM',    '25 Đinh Tiên Hoàng, BT, TP.HCM',    'TP.HCM',      'Kinh', 'Không',      'doc_than'),
('NV012', 'Lê Thị Phương Linh',   '1986-10-09', 'nu',  '079086010091', '0901000012', 'phuonglinh.le@abctech.vn',  '30 Nguyễn Trọng Tuyển, PN, TP.HCM', '30 Nguyễn Trọng Tuyển, PN, TP.HCM', 'TP.HCM',      'Kinh', 'Không',      'da_ket_hon'),
('NV013', 'Phạm Đình Khang',      '1993-05-04', 'nam', '079093005041', '0901000013', 'khang.pham@abctech.vn',     '52 Phú Nhuận, PN, TP.HCM',           '52 Phú Nhuận, PN, TP.HCM',           'Bình Phước',  'Kinh', 'Không',      'doc_than');

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
('NV001', 'CONGTY',   'GD',  'chinh', 100.00, NULL,    NULL,    '2015-01-05', 'hieu_luc', 'Bổ nhiệm Giám đốc điều hành'),
-- Phòng Nhân sự
('NV002', 'PHONGNS',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2017-02-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Nhân sự'),
('NV003', 'PHONGNS',  'NSV', 'chinh', 100.00, 'NV002', 'NV001', '2021-06-01', 'hieu_luc', 'Nhân sự viên - tuyển dụng'),
-- Phòng Kế toán
('NV004', 'PHONGKT',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2017-04-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Kế toán'),
('NV005', 'PHONGKT',  'KTV', 'chinh', 100.00, 'NV004', 'NV001', '2021-07-01', 'hieu_luc', 'Kế toán viên'),
-- Phòng Kinh doanh
('NV006', 'PHONGKD',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2017-07-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Kinh doanh'),
('NV007', 'PHONGKD',  'CV',  'chinh', 100.00, 'NV006', 'NV001', '2020-06-01', 'hieu_luc', 'Chuyên viên Kinh doanh'),
-- Phòng IT
('NV008', 'PHONGIT',  'TP',  'chinh', 100.00, 'NV001', 'NV001', '2018-01-15', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Công nghệ thông tin'),
('NV009', 'TEAM_IT',  'TT',  'chinh', 100.00, 'NV008', 'NV008', '2019-08-01', 'hieu_luc', 'Trưởng nhóm phát triển IT'),
('NV010', 'TEAM_IT',  'NV',  'chinh', 100.00, 'NV009', 'NV008', '2022-02-01', 'hieu_luc', 'Nhân viên phát triển IT'),
('NV011', 'TEAM_IT',  'TV',  'chinh', 100.00, 'NV009', 'NV008', '2025-10-01', 'hieu_luc', 'Thử việc phát triển IT'),
-- Phòng Marketing
('NV012', 'PHONGMKT', 'TP',  'chinh', 100.00, 'NV001', 'NV001', '2018-07-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Marketing'),
('NV013', 'PHONGMKT', 'NV',  'chinh', 100.00, 'NV012', 'NV001', '2021-03-01', 'hieu_luc', 'Nhân viên Marketing');

-- =====================================================
-- 5.5. VAI TRÒ & QUYỀN
-- =====================================================
-- [CHANGE] Tách TRUONG_PHONG thành 3 role chuyên biệt:
--   TRUONG_PHONG_NS  : TP Nhân sự — HR đầy đủ + bổ nhiệm + báo cáo
--   TRUONG_PHONG_KT  : TP Kế toán — lương + tài chính + báo cáo
--   TRUONG_PHONG     : TP chung (IT, KD, MKT) — base quản lý phòng thuần túy
INSERT INTO VAITRO (maVaiTro, tenVaiTro, moTa, laVaiTroHeThong, trangThai) VALUES
('ADMIN',           'Quản trị viên',          'Toàn quyền quản trị hệ thống',                  TRUE,  'hoatDong'),
('TONG_GIAM_DOC',   'Tổng giám đốc',          'Điều hành cấp cao toàn công ty',                FALSE, 'hoatDong'),
('TRUONG_PHONG_NS', 'Trưởng phòng Nhân sự',   'Quản lý phòng NS + toàn quyền HR + bổ nhiệm',  FALSE, 'hoatDong'),
('TRUONG_PHONG_KT', 'Trưởng phòng Kế toán',   'Quản lý phòng KT + lương + báo cáo tài chính', FALSE, 'hoatDong'),
('TRUONG_PHONG',    'Trưởng phòng',            'Quản lý phòng thông thường (IT, KD, MKT...)',   FALSE, 'hoatDong'),
('QUAN_LY',         'Quản lý',                 'Quản lý nhóm và phê duyệt cấp team',            FALSE, 'hoatDong'),
('NHAN_SU',         'Nhân sự',                 'Nhân viên phòng Nhân sự — nghiệp vụ HR',        FALSE, 'hoatDong'),
('KE_TOAN',         'Kế toán',                 'Nhân viên phòng Kế toán — nghiệp vụ tài chính', FALSE, 'hoatDong'),
('NHAN_VIEN',       'Nhân viên',               'Nhân viên thông thường',                         FALSE, 'hoatDong');

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
('SETTINGS_UPDATE',     'Cập nhật cài đặt',           'Cài đặt'),
('PAYROLL_LOCK',        'Khóa bảng lương',             'Lương');

-- =============================================
-- VAITRO_QUYEN
-- =============================================

-- ADMIN: toàn quyền — không thay đổi
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi)
SELECT 'ADMIN', maQuyen, 'ALL' FROM QUYEN;

-- -----------------------------------------------
-- NHAN_VIEN: chỉ xem/thao tác dữ liệu bản thân
-- SELF hợp lệ vì mọi action đều chỉ liên quan đến chính họ
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('NHAN_VIEN', 'EMPLOYEE_VIEW',    'SELF'),
('NHAN_VIEN', 'APPOINTMENT_VIEW', 'SELF'),
('NHAN_VIEN', 'ATTENDANCE_VIEW',  'SELF'),
('NHAN_VIEN', 'CONTRACT_VIEW',    'SELF'),
('NHAN_VIEN', 'PAYROLL_VIEW',     'SELF'),
('NHAN_VIEN', 'LEAVE_VIEW',       'SELF'),
('NHAN_VIEN', 'LEAVE_CREATE',     'SELF'),
('NHAN_VIEN', 'EVAL_VIEW',        'SELF');

-- -----------------------------------------------
-- QUAN_LY (Team Lead): quản lý nhóm (phòng ban con)
-- [FIX R2] TEAM→DEPT vì TEAM_IT là PHONG_BAN thực sự trong DB (phongBanCha='PHONGIT').
--          Scope TEAM là redundant — hệ thống filter theo maPhongBan của user.
--          Trưởng nhóm Backend vẫn chỉ thấy TEAM_IT vì maPhongBan của họ là TEAM_IT.
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('QUAN_LY', 'EMPLOYEE_VIEW',         'DEPT'),
('QUAN_LY', 'APPOINTMENT_VIEW',      'SELF'),   -- xem bo nhiem ban than
('QUAN_LY', 'ATTENDANCE_VIEW',       'DEPT'),
('QUAN_LY', 'CONTRACT_VIEW',         'SELF'),   -- xem hop dong ban than
('QUAN_LY', 'PAYROLL_VIEW',          'SELF'),   -- xem luong ban than
('QUAN_LY', 'LEAVE_VIEW',            'DEPT'),
('QUAN_LY', 'LEAVE_CREATE',          'SELF'),
('QUAN_LY', 'LEAVE_APPROVE',         'DEPT'),
('QUAN_LY', 'EVAL_VIEW',             'DEPT'),
('QUAN_LY', 'EVAL_REVIEW',           'DEPT'),
('QUAN_LY', 'RECRUITMENT_REQUEST',   'DEPT');

-- -----------------------------------------------
-- TRUONG_PHONG (IT, KD, MKT — base thuần): quản lý cấp phòng
-- [FIX R3] CONTRACT_CREATE: XÓA — TP chỉ đề xuất, không tự tạo hợp đồng
-- [FIX R1] RECRUITMENT_REQUEST: SELF→DEPT
-- [FIX R1] NOTIFICATION_SEND: SELF→DEPT
-- [FIX R1] ATTENDANCE_MANAGE: SELF→DEPT
-- [FIX R1] CONTRACT_UPDATE: SELF→DEPT
-- Không có REPORT_VIEW — dữ liệu phòng đã có trong module quản lý
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('TRUONG_PHONG', 'EMPLOYEE_VIEW',         'DEPT'),
('TRUONG_PHONG', 'APPOINTMENT_VIEW',      'DEPT'),
('TRUONG_PHONG', 'ATTENDANCE_VIEW',       'DEPT'),
('TRUONG_PHONG', 'ATTENDANCE_MANAGE',     'DEPT'),   -- [FIX R1] SELF→DEPT
('TRUONG_PHONG', 'CONTRACT_VIEW',         'DEPT'),
('TRUONG_PHONG', 'CONTRACT_UPDATE',       'DEPT'),   -- [FIX R1] SELF→DEPT
('TRUONG_PHONG', 'PAYROLL_VIEW',          'DEPT'),
('TRUONG_PHONG', 'LEAVE_VIEW',            'DEPT'),
('TRUONG_PHONG', 'LEAVE_CREATE',          'SELF'),   -- tạo đơn cho bản thân: SELF hợp lệ
('TRUONG_PHONG', 'LEAVE_APPROVE',         'DEPT'),
('TRUONG_PHONG', 'EVAL_VIEW',             'DEPT'),
('TRUONG_PHONG', 'EVAL_REVIEW',           'DEPT'),
('TRUONG_PHONG', 'RECRUITMENT_VIEW',      'DEPT'),
('TRUONG_PHONG', 'RECRUITMENT_REQUEST',   'DEPT'),   -- [FIX R1] SELF→DEPT
('TRUONG_PHONG', 'NOTIFICATION_SEND',     'DEPT'),   -- [FIX R1] SELF→DEPT
('TRUONG_PHONG', 'REPORT_VIEW',           'DEPT'),   -- [NEW] trưởng phòng xem báo cáo phòng mình
('TRUONG_PHONG', 'REPORT_EXPORT',         'DEPT'),   -- [NEW] trưởng phòng xuất báo cáo phòng mình
('TRUONG_PHONG', 'DEPARTMENT_VIEW',       'DEPT'),   -- [FIX R2] trưởng phòng cần xem sơ đồ tổ chức
('TRUONG_PHONG', 'POSITION_VIEW',         'DEPT');   -- [FIX R2] trưởng phòng cần xem danh mục chức vụ

-- -----------------------------------------------
-- TRUONG_PHONG_NS: base TRUONG_PHONG + toàn quyền HR
-- Có thêm: EMPLOYEE_CREATE/RESIGN, APPOINTMENT_CREATE/APPROVE,
--          CONTRACT_CREATE/MANAGE, LEAVE_MANAGE, EVAL_MANAGE,
--          RECRUITMENT_MANAGE, REPORT_VIEW/EXPORT, NOTIFICATION_SEND=ALL
-- phamVi cho mọi CREATE/MANAGE=ALL vì NS xử lý dữ liệu toàn công ty
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('TRUONG_PHONG_NS', 'EMPLOYEE_VIEW',         'ALL'),
('TRUONG_PHONG_NS', 'EMPLOYEE_CREATE',       'ALL'),
('TRUONG_PHONG_NS', 'EMPLOYEE_UPDATE',       'ALL'),
('TRUONG_PHONG_NS', 'EMPLOYEE_RESIGN',       'ALL'),
('TRUONG_PHONG_NS', 'APPOINTMENT_VIEW',      'ALL'),
('TRUONG_PHONG_NS', 'APPOINTMENT_CREATE',    'ALL'),
('TRUONG_PHONG_NS', 'APPOINTMENT_APPROVE',   'ALL'),
('TRUONG_PHONG_NS', 'ATTENDANCE_VIEW',       'ALL'),
('TRUONG_PHONG_NS', 'ATTENDANCE_MANAGE',     'ALL'),
('TRUONG_PHONG_NS', 'CONTRACT_VIEW',         'ALL'),
('TRUONG_PHONG_NS', 'CONTRACT_CREATE',       'ALL'),
('TRUONG_PHONG_NS', 'CONTRACT_UPDATE',       'ALL'),
('TRUONG_PHONG_NS', 'CONTRACT_MANAGE',       'ALL'),
('TRUONG_PHONG_NS', 'PAYROLL_VIEW',          'ALL'),
('TRUONG_PHONG_NS', 'LEAVE_VIEW',            'ALL'),
('TRUONG_PHONG_NS', 'LEAVE_CREATE',          'SELF'),
('TRUONG_PHONG_NS', 'LEAVE_APPROVE',         'ALL'),
('TRUONG_PHONG_NS', 'LEAVE_MANAGE',          'ALL'),
('TRUONG_PHONG_NS', 'EVAL_VIEW',             'ALL'),
('TRUONG_PHONG_NS', 'EVAL_MANAGE',           'ALL'),
('TRUONG_PHONG_NS', 'EVAL_REVIEW',           'ALL'),
('TRUONG_PHONG_NS', 'RECRUITMENT_VIEW',      'ALL'),
('TRUONG_PHONG_NS', 'RECRUITMENT_REQUEST',   'DEPT'),
('TRUONG_PHONG_NS', 'RECRUITMENT_MANAGE',    'ALL'),
('TRUONG_PHONG_NS', 'REPORT_VIEW',           'ALL'),
('TRUONG_PHONG_NS', 'REPORT_EXPORT',         'ALL'),
('TRUONG_PHONG_NS', 'NOTIFICATION_SEND',     'ALL'),
('TRUONG_PHONG_NS', 'DEPARTMENT_VIEW',       'ALL'),   -- [FIX R2] TP_NS phải xem sơ đồ tổ chức toàn cty
('TRUONG_PHONG_NS', 'POSITION_VIEW',         'ALL');   -- [FIX R2] TP_NS phải xem danh mục chức vụ toàn cty

-- -----------------------------------------------
-- TRUONG_PHONG_KT: base TRUONG_PHONG + quyền tài chính
-- Có thêm: PAYROLL_CALCULATE=ALL, CONTRACT_MANAGE=ALL, REPORT_VIEW/EXPORT=ALL
-- KT xem toàn bộ để phục vụ hạch toán — không có quyền HR/bổ nhiệm
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('TRUONG_PHONG_KT', 'EMPLOYEE_VIEW',         'ALL'),
('TRUONG_PHONG_KT', 'APPOINTMENT_VIEW',      'DEPT'),
('TRUONG_PHONG_KT', 'ATTENDANCE_VIEW',       'ALL'),
('TRUONG_PHONG_KT', 'ATTENDANCE_MANAGE',     'DEPT'),
('TRUONG_PHONG_KT', 'CONTRACT_VIEW',         'ALL'),
('TRUONG_PHONG_KT', 'CONTRACT_UPDATE',       'DEPT'),
('TRUONG_PHONG_KT', 'CONTRACT_MANAGE',       'ALL'),
('TRUONG_PHONG_KT', 'PAYROLL_VIEW',          'ALL'),
('TRUONG_PHONG_KT', 'PAYROLL_CALCULATE',     'ALL'),
('TRUONG_PHONG_KT', 'LEAVE_VIEW',            'DEPT'),
('TRUONG_PHONG_KT', 'LEAVE_CREATE',          'SELF'),
('TRUONG_PHONG_KT', 'LEAVE_APPROVE',         'DEPT'),
('TRUONG_PHONG_KT', 'EVAL_VIEW',             'ALL'),
('TRUONG_PHONG_KT', 'EVAL_REVIEW',           'DEPT'),
('TRUONG_PHONG_KT', 'RECRUITMENT_VIEW',      'DEPT'),
('TRUONG_PHONG_KT', 'RECRUITMENT_REQUEST',   'DEPT'),
('TRUONG_PHONG_KT', 'REPORT_VIEW',           'ALL'),
('TRUONG_PHONG_KT', 'REPORT_EXPORT',         'ALL'),
('TRUONG_PHONG_KT', 'NOTIFICATION_SEND',     'DEPT'),
('TRUONG_PHONG_KT', 'PAYROLL_LOCK',          'ALL');  -- [NEW] chỉ TP_KT duyệt và khóa bảng lương

-- -----------------------------------------------
-- TONG_GIAM_DOC: toàn quyền nghiệp vụ
-- [FIX R1] Tất cả CREATE/MANAGE/CALCULATE fix SELF→ALL
-- LEAVE_CREATE giữ SELF (tạo đơn cho bản thân)
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('TONG_GIAM_DOC', 'EMPLOYEE_VIEW',         'ALL'),
('TONG_GIAM_DOC', 'EMPLOYEE_CREATE',       'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'EMPLOYEE_UPDATE',       'ALL'),
('TONG_GIAM_DOC', 'EMPLOYEE_RESIGN',       'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'DEPARTMENT_VIEW',       'ALL'),
('TONG_GIAM_DOC', 'DEPARTMENT_MANAGE',     'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'POSITION_VIEW',         'ALL'),
('TONG_GIAM_DOC', 'POSITION_MANAGE',       'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'APPOINTMENT_VIEW',      'ALL'),
('TONG_GIAM_DOC', 'APPOINTMENT_CREATE',    'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'APPOINTMENT_APPROVE',   'ALL'),
('TONG_GIAM_DOC', 'ATTENDANCE_VIEW',       'ALL'),
('TONG_GIAM_DOC', 'ATTENDANCE_MANAGE',     'ALL'),
('TONG_GIAM_DOC', 'CONTRACT_VIEW',         'ALL'),
('TONG_GIAM_DOC', 'CONTRACT_CREATE',       'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'CONTRACT_UPDATE',       'ALL'),
('TONG_GIAM_DOC', 'CONTRACT_MANAGE',       'ALL'),
('TONG_GIAM_DOC', 'PAYROLL_VIEW',          'ALL'),
('TONG_GIAM_DOC', 'PAYROLL_CALCULATE',     'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'LEAVE_VIEW',            'ALL'),
('TONG_GIAM_DOC', 'LEAVE_CREATE',          'SELF'),  -- tạo đơn cho bản thân: SELF hợp lệ
('TONG_GIAM_DOC', 'LEAVE_APPROVE',         'ALL'),
('TONG_GIAM_DOC', 'LEAVE_MANAGE',          'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'EVAL_VIEW',             'ALL'),
('TONG_GIAM_DOC', 'EVAL_MANAGE',           'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'EVAL_REVIEW',           'ALL'),
('TONG_GIAM_DOC', 'RECRUITMENT_VIEW',      'ALL'),
('TONG_GIAM_DOC', 'RECRUITMENT_REQUEST',   'DEPT'),  -- [FIX R1] SELF→DEPT
('TONG_GIAM_DOC', 'RECRUITMENT_MANAGE',    'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'REPORT_VIEW',           'ALL'),
('TONG_GIAM_DOC', 'REPORT_EXPORT',         'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'NOTIFICATION_SEND',     'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'SETTINGS_VIEW',         'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'SETTINGS_UPDATE',       'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'USER_VIEW',             'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'ROLE_VIEW',             'ALL'),   -- [FIX R1] SELF→ALL
('TONG_GIAM_DOC', 'PAYROLL_LOCK',          'ALL');   -- [NEW] TGĐ cũng có thể duyệt/khóa bảng lương

-- -----------------------------------------------
-- NHAN_SU: nghiệp vụ HR chuyên sâu
-- [FIX R1] Tất cả CREATE/MANAGE fix SELF→ALL
-- [FIX R2] Xóa EMPLOYEE_RESIGN, EVAL_MANAGE — thẩm quyền của TRUONG_PHONG_NS
-- [FIX R2] Thêm DEPARTMENT_VIEW, POSITION_VIEW — cần xem org chart để làm việc
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('NHAN_SU', 'EMPLOYEE_VIEW',         'ALL'),
('NHAN_SU', 'EMPLOYEE_CREATE',       'ALL'),
('NHAN_SU', 'EMPLOYEE_UPDATE',       'ALL'),
-- EMPLOYEE_RESIGN: KHÔNG cấp cho NHAN_SU — [FIX R2] cho nghỉ việc là thẩm quyền TRUONG_PHONG_NS trở lên
('NHAN_SU', 'APPOINTMENT_VIEW',      'ALL'),
('NHAN_SU', 'APPOINTMENT_CREATE',    'ALL'),
-- APPOINTMENT_APPROVE: KHÔNG cấp cho NHAN_SU — chỉ TRUONG_PHONG_NS/TONG_GIAM_DOC mới duyệt bổ nhiệm
('NHAN_SU', 'ATTENDANCE_VIEW',       'ALL'),
('NHAN_SU', 'ATTENDANCE_MANAGE',     'ALL'),
('NHAN_SU', 'CONTRACT_VIEW',         'ALL'),
('NHAN_SU', 'CONTRACT_CREATE',       'ALL'),
('NHAN_SU', 'CONTRACT_UPDATE',       'ALL'),
('NHAN_SU', 'CONTRACT_MANAGE',       'ALL'),
('NHAN_SU', 'PAYROLL_VIEW',          'ALL'),
('NHAN_SU', 'LEAVE_VIEW',            'ALL'),
('NHAN_SU', 'LEAVE_CREATE',          'SELF'),  -- tạo đơn cho bản thân: SELF hợp lệ
('NHAN_SU', 'LEAVE_MANAGE',          'ALL'),
('NHAN_SU', 'EVAL_VIEW',             'ALL'),
-- EVAL_MANAGE: KHÔNG cấp cho NHAN_SU — [FIX R2] tạo đợt đánh giá là quyết định chiến lược của TRUONG_PHONG_NS
('NHAN_SU', 'RECRUITMENT_VIEW',      'ALL'),
('NHAN_SU', 'RECRUITMENT_REQUEST',   'DEPT'),
('NHAN_SU', 'RECRUITMENT_MANAGE',    'ALL'),
('NHAN_SU', 'REPORT_VIEW',           'ALL'),
('NHAN_SU', 'REPORT_EXPORT',         'ALL'),
('NHAN_SU', 'NOTIFICATION_SEND',     'ALL'),
('NHAN_SU', 'DEPARTMENT_VIEW',       'ALL'),   -- [FIX R2] nhân viên NS cần xem sơ đồ tổ chức
('NHAN_SU', 'POSITION_VIEW',         'ALL');   -- [FIX R2] nhân viên NS cần xem danh mục chức vụ

-- -----------------------------------------------
-- KE_TOAN: tài chính, lương, hạch toán
-- [FIX R1] PAYROLL_CALCULATE: SELF→ALL (tính lương cho toàn bộ NV)
-- [FIX R4] EVAL_VIEW: SELF→ALL (xem KPI toàn công ty để tính lương)
-- [FIX R1] REPORT_VIEW/EXPORT: SELF→ALL (báo cáo tài chính toàn công ty)
-- -----------------------------------------------
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen, phamVi) VALUES
('KE_TOAN', 'EMPLOYEE_VIEW',     'ALL'),
('KE_TOAN', 'ATTENDANCE_VIEW',   'ALL'),
('KE_TOAN', 'CONTRACT_VIEW',     'ALL'),
('KE_TOAN', 'PAYROLL_VIEW',      'ALL'),
('KE_TOAN', 'PAYROLL_CALCULATE', 'ALL'),   -- [FIX R1] SELF→ALL
('KE_TOAN', 'LEAVE_VIEW',        'ALL'),
('KE_TOAN', 'LEAVE_CREATE',      'SELF'),  -- tạo đơn cho bản thân: SELF hợp lệ
('KE_TOAN', 'REPORT_VIEW',       'ALL'),   -- [FIX R1] SELF→ALL
('KE_TOAN', 'REPORT_EXPORT',     'ALL'),   -- [FIX R1] SELF→ALL
('KE_TOAN', 'EVAL_VIEW',         'ALL');   -- [FIX R4] SELF→ALL

-- =====================================================
-- 6. TÀI KHOẢN
-- =====================================================
-- [CHANGE] NV002 → TRUONG_PHONG_NS (Trưởng phòng Nhân sự chuyên biệt)
-- [CHANGE] NV004 → TRUONG_PHONG_KT (Trưởng phòng Kế toán chuyên biệt)
-- NV006, NV008, NV012 giữ TRUONG_PHONG (base — IT, KD, MKT)
INSERT INTO TAIKHOAN (tenDangNhap, matKhau, maNV, maVaiTro, email, hoatDong) VALUES
('admin',           '123', 'admin', 'ADMIN',           'admin@abctech.vn',          TRUE),
('hung.nguyen',     '123', 'NV001', 'TONG_GIAM_DOC',   'hung.nguyen@abctech.vn',    TRUE),
('huong.nguyen',    '123', 'NV002', 'TRUONG_PHONG_NS', 'huong.nguyen@abctech.vn',   TRUE),
('lananh.dang',     '123', 'NV003', 'NHAN_SU',         'lananh.dang@abctech.vn',    TRUE),
('ngoc.hoang',      '123', 'NV004', 'TRUONG_PHONG_KT', 'ngoc.hoang@abctech.vn',     TRUE),
('thanhTam.ly',     '123', 'NV005', 'KE_TOAN',         'thanhTam.ly@abctech.vn',    TRUE),
('anh.tuan',        '123', 'NV006', 'TRUONG_PHONG',    'anh.tuan@abctech.vn',       TRUE),
('hoang.le',        '123', 'NV007', 'NHAN_VIEN',       'hoang.le@abctech.vn',       TRUE),
('son.dinh',        '123', 'NV008', 'TRUONG_PHONG',    'son.dinh@abctech.vn',       TRUE),
('khoa.nguyen',     '123', 'NV009', 'QUAN_LY',         'khoa.nguyen@abctech.vn',    TRUE),
('tri.hoang',       '123', 'NV010', 'NHAN_VIEN',       'tri.hoang@abctech.vn',      TRUE),
('camtu.vo',        '123', 'NV011', 'NHAN_VIEN',       'camtu.vo@abctech.vn',       TRUE),
('phuonglinh.le',   '123', 'NV012', 'TRUONG_PHONG',    'phuonglinh.le@abctech.vn',  TRUE),
('khang.pham',      '123', 'NV013', 'NHAN_VIEN',       'khang.pham@abctech.vn',     TRUE);

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
('HANH_CHINH', 'Ca hanh chinh', '08:00:00', '17:00:00', 8.00, TRUE,  'Ca làm việc hành chính văn phòng', 'hoatDong'),
('CA_SANG',    'Ca sáng',       '06:00:00', '14:00:00', 8.00, TRUE,  'Ca sáng',                          'hoatDong'),
('CA_CHIEU',   'Ca chiều',      '14:00:00', '22:00:00', 8.00, TRUE,  'Ca chiều',                         'hoatDong'),
('CA_DEM',     'Ca đêm',        '22:00:00', '06:00:00', 8.00, FALSE, 'Ca đêm kỹ thuật',                  'hoatDong');

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
('NV002', 'PHEP_NAM', '2026-01-14', '2026-01-14', 1, 'Việc cá nhân',                     NULL, 'NV001', '2026-01-13', NULL, 'da_duyet'),
('NV009', 'PHEP_OM',  '2026-01-08', '2026-01-08', 1, 'Ốm cấp tốc',                       NULL, 'NV008', '2026-01-08', NULL, 'da_duyet'),
('NV010', 'PHEP_NAM', '2026-02-10', '2026-02-13', 4, 'Du lịch nghỉ Tết Dương lịch muộn', NULL, 'NV009', '2026-02-07', NULL, 'da_duyet'),
('NV007', 'PHEP_NAM', '2026-02-16', '2026-02-16', 1, 'Việc gia đình',                    NULL, NULL,    NULL,         NULL, 'cho_duyet');

-- =====================================================
-- 14. BẢNG LƯƠNG
-- =====================================================
-- trangThai ENUM: 'dang_xu_ly' | 'da_duyet' | 'da_khoa'
-- nguoiTao/nguoiDuyet là INT (id auto-increment của NHANVIEN), để NULL khi không xác định
INSERT INTO BANGLUONG (thang, nam, tenBangLuong, nguoiTao, nguoiDuyet, ngayDuyet, trangThai) VALUES
( 1, 2026, 'Bảng lương tháng 01/2026', 5, 4, '2026-02-03', 'da_duyet'),
( 2, 2026, 'Bảng lương tháng 02/2026', 5, NULL, NULL,      'dang_xu_ly');

-- Chi tiet luong T1/2026 (maBangLuong = 1, mau 6 NV dai dien)
-- Cot: maBangLuong, maNV, luongCoSo, tongLuongChucVu, luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem
INSERT INTO CHITIETLUONG (maBangLuong, maNV, luongCoSo, tongLuongChucVu, luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem) VALUES
(1, 'NV001', 80000000, 15000000,  3030000, 98030000, 37060000,  60970000, 20,  2.5),
(1, 'NV002', 22000000,  5000000,        0, 27000000,  8993000,  18007000, 19,  0.0),
(1, 'NV008', 35000000,  5000000,  2188000, 42188000, 13895000,  28293000, 21,  4.0),
(1, 'NV009', 28000000,  2000000,        0, 30000000, 10006000,  19994000, 20,  0.0),
(1, 'NV010', 18000000,        0,  1463000, 19463000,  4753000,  14710000, 20,  6.5),
(1, 'NV011',  8500000,        0,        0,  8500000,  1838000,   6662000, 21,  0.0);

-- =====================================================
-- 15. ĐÁNH GIÁ HIỆU SUẤT
-- =====================================================
INSERT INTO DOTDANHGIA (tenDot, nam, kyDanhGia, tuNgay, denNgay, moTa, trangThai) VALUES
('Đánh giá hiệu suất Q3/2025', 2025, 'quy_3', '2025-09-01', '2025-09-30', 'Đánh giá Q3/2025',              'da_ket_thuc'),
('Đánh giá cuối năm 2025',     2025, 'nam',   '2025-12-01', '2025-12-31', 'Tổng kết KPI năm 2025',         'da_ket_thuc'),
('Đánh giá hiệu suất Q1/2026', 2026, 'quy_1', '2026-04-01', '2026-04-30', 'Chuẩn bị đợt đánh giá Q1/2026', 'chua_bat_dau');

INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, nhanXetChung, ngayDanhGia, trangThai) VALUES
-- Q3/2025
(1, 'NV008', 'NV001', 8.7, 'tot',      'TP IT chủ động xử lý sự cố, đảm bảo tiến độ dự án',    '2025-09-30', 'da_xac_nhan'),
(1, 'NV009', 'NV008', 8.2, 'tot',      'TT nhóm phát triển ổn định, giao tiếp tốt',            '2025-09-30', 'da_xac_nhan'),
(1, 'NV010', 'NV009', 7.3, 'kha',      'NV cố gắng nhưng cần cải thiện đúng giờ',              '2025-09-30', 'da_xac_nhan'),
(1, 'NV011', 'NV009', 7.3, 'kha',      'Thử việc tích cực, học nhanh',                          '2025-09-30', 'da_xac_nhan'),
-- Cuoi nam 2025
(2, 'NV008', 'NV001', 8.8, 'xuat_sac', 'Dự án lớn hoàn thành đúng hạn, chất lượng cao',         '2025-12-31', 'da_xac_nhan'),
(2, 'NV009', 'NV008', 8.3, 'tot',      'Hoàn thành tốt nhiệm vụ TT, team tinh thần cao',        '2025-12-31', 'da_xac_nhan'),
(2, 'NV010', 'NV009', 7.6, 'kha',      'Tiến bộ rõ rệt, cần hạn chế vắng mặt',                   '2025-12-31', 'da_xac_nhan');



INSERT INTO TIEUCHIDANHGIA (tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai) VALUES
('Chất lượng công việc',    'Chất lượng đầu ra, sản phẩm, dịch vụ cung cấp',        'Kết quả',  30, 'hoatDong'),
('Tiến độ hoàn thành',      'Hoàn thành đúng hạn, không trễ deadline',               'Kết quả',  20, 'hoatDong'),
('Khả năng sáng tạo',       'Đề xuất giải pháp, cải tiến quy trình',                'Năng lực', 10, 'hoatDong'),
('Kỹ năng chuyên môn',      'Trình độ chuyên môn, kỹ năng kỹ thuật',                'Năng lực', 10, 'hoatDong'),
('Làm việc nhóm',           'Phối hợp, hỗ trợ đồng nghiệp, tinh thần team',         'Thái độ',  10, 'hoatDong'),
('Tuân thủ nội quy',        'Chấp hành quy chế, đi làm đúng giờ, tác phong',        'Thái độ',  10, 'hoatDong'),
('Phát triển bản thân',     'Học hỏi kỹ năng mới, nâng cao trình độ',               'Năng lực', 10, 'hoatDong');

-- =====================================================
-- 15.5. ĐÁNH GIÁ HIỆU SUẤT - CHI TIẾT
-- =====================================================
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo) VALUES
(1, 1, 1.00), (1, 2, 1.00), (1, 3, 1.00), (1, 4, 1.00), (1, 5, 1.00), (1, 6, 1.00), (1, 7, 1.00),
(2, 1, 1.00), (2, 2, 1.00), (2, 3, 1.00), (2, 4, 1.00), (2, 5, 1.00), (2, 6, 1.00), (2, 7, 1.00),
(3, 1, 1.00), (3, 2, 1.00), (3, 3, 1.00), (3, 4, 1.00), (3, 5, 1.00), (3, 6, 1.00), (3, 7, 1.00);

-- Chi tiết đánh giá
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
-- Q3/2025: NV008 (Tổng 8.7/10 ~ 87/100)
(1, 1, 28.00, 'Chất lượng rất tốt'), 
(1, 2, 18.00, 'Đúng tiến độ'),       
(1, 3,  8.00, 'Khá sáng tạo'),       
(1, 4,  8.00, 'Chuyên môn vững'),    
(1, 5,  8.00, 'Làm việc nhóm tốt'),  
(1, 6,  9.00, 'Tuân thủ tốt'),       
(1, 7,  8.00, 'Cố gắng nâng cao năng lực'), 
-- Q3/2025: NV009 (Tổng 8.2 ~ 82/100)
(2, 1, 25.00, 'Chất lượng tốt'),
(2, 2, 17.00, 'Đa phần đúng hạn'),
(2, 3,  8.00, 'Đề xuất nhiều cải tiến'),
(2, 4,  8.00, 'Chuyên môn tốt'),
(2, 5,  8.00, 'Hỗ trợ team nhiệt tình'),
(2, 6,  8.00, 'Chấp hành quy chế'),
(2, 7,  8.00, 'Học thêm kỹ năng mới'),
-- Q3/2025: NV010 (Tổng 7.3 ~ 73/100)
(3, 1, 23.00, 'Chất lượng khá'),
(3, 2, 15.00, 'Đôi khi trễ hạn'),
(3, 3,  7.00, 'Chưa có nhiều ý tưởng'),
(3, 4,  7.00, 'Chuyên môn cần cải thiện thêm'),
(3, 5,  7.00, 'Phối hợp chưa tốt lắm'),
(3, 6,  7.00, 'Cần đi làm đúng giờ hơn'),
(3, 7,  7.00, 'Có tinh thần học hỏi'),
-- Q3/2025: NV011 (Tổng 7.3 ~ 73/100)
(4, 1, 22.00, 'Làm việc được giao tốt'),
(4, 2, 16.00, 'Theo kịp tiến độ'),
(4, 3,  7.00, 'Mới làm việc, chưa thể hiện nhiều'),
(4, 4,  7.00, 'Kiến thức cơ bản vững'),
(4, 5,  7.00, 'Tích cực'),
(4, 6,  7.00, 'Tác phong tốt'),
(4, 7,  7.00, 'Ham học hỏi'),
-- Cuoi nam 2025: NV008 (Tổng 8.8 ~ 88/100)
(5, 1, 29.00, 'Hoàn thành xuất sắc'),
(5, 2, 19.00, 'Luôn bám deadline'),
(5, 3,  8.00, 'Có các giải pháp hữu hiệu cho hệ thống'),
(5, 4,  8.00, 'Am hiểu sâu về kiến trúc'),
(5, 5,  8.00, 'Teamwork hoàn hảo'),
(5, 6,  8.00, 'Chấp hành thể lệ tốt'),
(5, 7,  8.00, 'Học thêm chứng chỉ PMI'),
-- Cuoi nam 2025: NV009 (Tổng 8.3 ~ 83/100)
(6, 1, 25.00, 'Tốt'),
(6, 2, 18.00, 'Đúng hạn'),
(6, 3,  8.00, 'Sáng tạo trong UX'),
(6, 4,  8.00, 'Kiến thức tốt'),
(6, 5,  8.00, 'Tốt'),
(6, 6,  8.00, 'Nghiêm túc'),
(6, 7,  8.00, 'Luôn tích cực nâng cao tay nghề'),
-- Cuoi nam 2025: NV010 (Tổng 7.6 ~ 76/100)
(7, 1, 24.00, 'Hoàn thành công việc'),
(7, 2, 16.00, 'Khắc phục tình trạng trễ deadline'),
(7, 3,  7.00, 'Bình thường'),
(7, 4,  7.00, 'Không thay đổi nhiều'),
(7, 5,  7.00, 'Đã cải thiện giao tiếp với đồng nghiệp'),
(7, 6,  8.00, 'Đã hạn chế việc đi muộn'),
(7, 7,  7.00, 'Bình thường');

-- =====================================================
-- 15. TUYỂN DỤNG
-- =====================================================
-- Bước 1: Tạo yêu cầu tuyển dụng (YEUCAUTUYENDUNG)
INSERT INTO YEUCAUTUYENDUNG (maPhongBan, maChucVu, soLuong, lyDo, mucLuongDuKien, yeuCauKinhNghiem, yeuCauHocVan, hanTuyenDung, nguoiDuyet, ngayDuyet, trangThai) VALUES
('PHONGIT',  'NV',  1, 'Mở rộng team, tăng tài năng IT',     '20-30 triệu',  'Java/Spring Boot, 3+ năm KN', 'Đại học CNTT',      '2026-02-28', NULL, '2026-01-12', 'da_duyet'),
('PHONGKT',  'KTV', 1, 'Bổ sung nhân lực phòng KT',          '10-15 triệu',  'Kế toán tổng hợp 2+ năm',     'Đại học Kế toán',   '2026-03-15', NULL, '2026-01-18', 'da_duyet'),
('PHONGNS',  'NSV', 1, 'Tăng cường tuyển dụng cho Q2',       '9-13 triệu',   'Tuyển dụng, C&B, Excel',      'Đại học QTKD/NS',   '2026-02-15', NULL, '2026-01-22', 'da_duyet'),
('PHONGMKT', 'NV',  1, 'Tuyển thêm NV cho chiến dịch H1',    '10-15 triệu',  'Digital marketing, SEO/SEM',  'Đại học Marketing', '2026-03-31', NULL, NULL,         'cho_duyet');

-- Bước 2: Đăng tin tuyển dụng (TINTUYENDUNG) - tương ứng với maYeuCau 1,2,3,4
INSERT INTO TINTUYENDUNG (maYeuCau, tieuDe, noiDung, mucLuong, diaDiem, hanNopHoSo, trangThai) VALUES
(1, 'Tuyển Senior IT Developer',   'Phát triển hệ thống, Java/Spring Boot, 3+ năm KN',         '20-30 triệu',  'TP.HCM', '2026-02-28', 'dang_tuyen'),
(2, 'Tuyển Kế toán viên',          'Kế toán tổng hợp, thuế, có kinh nghiệm 2+ năm',            '10-15 triệu',  'TP.HCM', '2026-03-15', 'dang_tuyen'),
(3, 'Tuyển Nhân sự viên',          'Tuyển dụng, C&B, thông thạo Excel',                         '9-13 triệu',   'TP.HCM', '2026-02-15', 'tam_dung'),
(4, 'Tuyển Marketing Executive',   'Digital marketing, SEO/SEM, content',                      '10-15 triệu',  'TP.HCM', '2026-03-31', 'dang_tuyen');

-- Bước 3: Ứng viên (UNGVIEN) - maTin tham chiếu TINTUYENDUNG
INSERT INTO UNGVIEN (maTin, hoTen, email, dienThoai, ngaySinh, gioiTinh, trinhDoHocVan, kinhNghiem, nguonUngTuyen, trangThai, nhanXet) VALUES
(1, 'Trần Văn An',    'an.tran@gmail.com',   '0912345001', '1992-05-10', 'nam', 'Đại học CNTT', '5 năm Java/Spring Boot',      'LinkedIn',   'dang_phong_van', 'Ứng viên tiềm năng Senior Java'),
(1, 'Nguyễn Thị Bé',  'be.nguyen@gmail.com', '0912345002', '1990-08-15', 'nu',  'Đại học CNTT', 'Fullstack, kinh nghiệm 5 năm', 'TopCV',      'dang_phong_van', 'Fullstack, kinh nghiệm 5 năm'),
(2, 'Lê Minh Cường',  'cuong.le@gmail.com',  '0912345003', '1995-03-22', 'nam', 'Đại học KT',   'Kế toán tổng hợp 3 năm',      'VietnamWorks','moi',            NULL),
(3, 'Phạm Thị Diệu',  'dieu.pham@gmail.com', '0912345004', '1993-11-01', 'nu',  'Đại học QTKD', 'NS tuyển dụng công ty lớn',   'Referral',   'moi',            'NSV trước đây ở công ty lớn'),
(4, 'Hoàng Văn Em',   'em.hoang@gmail.com',  '0912345005', '1997-07-18', 'nam', 'Đại học MKT',  'Digital marketing 2 năm',     'TopCV',      'moi',            NULL);

-- =====================================================
-- 16. THÔNG BÁO
-- =====================================================
-- maTaiKhoan: 1=admin, 2=NV001(GD), 3=NV002(TP NS), 4=NV003(NSV),
--             5=NV004(TP KT), 6=NV005(KTV), 7=NV006(TP KD), 8=NV007(CV KD)
--             9=NV008(TP IT), 10=NV009(TT), 11=NV010(NV IT), 12=NV011(TV IT)
--             13=NV012(TP MKT), 14=NV013(NV MKT)
INSERT INTO THONGBAO (tieuDe, noiDung, loaiThongBao, maTaiKhoanGui, maTaiKhoanNhan, daDoc, ngayDoc) VALUES
('Chúc mừng năm mới 2026!',
 'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng!',
 'thong_bao_chung', 2, 3,  TRUE,  '2026-01-02 09:00:00'),
('Chúc mừng năm mới 2026!',
 'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng!',
 'thong_bao_chung', 2, 9,  TRUE,  '2026-01-02 09:15:00'),
('Chúc mừng năm mới 2026!',
 'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng!',
 'thong_bao_chung', 2, 13, FALSE, NULL),
('Bảng lương T01/2026 đã sẵn sàng',
 'BL tháng 01/2026 đã được phê duyệt. Vui lòng đăng nhập để kiểm tra.',
 'he_thong', 1, 2,  FALSE, NULL),
('Bảng lương T01/2026 đã sẵn sàng',
 'BL tháng 01/2026 đã được phê duyệt. Vui lòng đăng nhập để kiểm tra.',
 'he_thong', 1, 9,  FALSE, NULL),
('Đơn nghỉ phép đã được phê duyệt',
 'Đơn nghỉ phép ngày 14/01/2026 của bạn đã được phê duyệt. Chúc bạn nghỉ vui!',
 'don_tu', 1, 3,    TRUE,  '2026-01-13 17:00:00'),
('Đơn nghỉ phép đã được phê duyệt',
 'Đơn nghỉ phép 10-13/02/2026 của bạn đã được phê duyệt.',
 'don_tu', 10, 11,  FALSE, NULL),
('Yêu cầu tuyển dụng mới cần phê duyệt',
 'NV008 (TP IT) gửi yêu cầu tuyển 1 Senior Developer. Vui lòng xem xét.',
 'don_tu', 3, 2,    TRUE,  '2026-01-12 09:00:00'),
('Lịch đánh giá Q1/2026 sắp diễn ra',
 'Đợt đánh giá hiệu suất Q1/2026 bắt đầu 06/04/2026. Mời CBQL chuẩn bị tiêu chí.',
 'thong_bao_chung', 1, 9,   FALSE, NULL),
('Lịch đánh giá Q1/2026 sắp diễn ra',
 'Đợt đánh giá hiệu suất Q1/2026 bắt đầu 06/04/2026. Mời CBQL chuẩn bị tiêu chí.',
 'thong_bao_chung', 1, 7,   FALSE, NULL);

-- =====================================================
-- 17. CẤU HÌNH PHỤ CẤP
-- =====================================================
DELETE FROM CAUHINH_PHUCAP;
INSERT INTO CAUHINH_PHUCAP (loai, tenKhoan, kieuTinh, giaTri, nguon, hoatDong) VALUES
('phu_cap', 'Phụ cấp ăn trưa',               'co_dinh',   750000, 'CongTy',   1),
('phu_cap', 'Phụ cấp điện thoại',            'co_dinh',   500000, 'CongTy',   1),
('phu_cap', 'Phụ cấp đi lại',                'co_dinh',   600000, 'CongTy',   1),
('phu_cap', 'Phụ cấp thâm niên (3-5 năm)',   'co_dinh',   500000, 'CongTy',   1),
('phu_cap', 'Phụ cấp thâm niên (5+ năm)',    'co_dinh',  1000000, 'CongTy',   1),
('phu_cap', 'Thưởng hiệu quả hàng tháng',    'phan_tram',   5.00, 'CongTy',   1),
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
(1,  'CREATE',  'BANGLUONG',       '1',  '192.168.1.1',  'Mozilla/5.0 Chrome/121'),
(1,  'UPDATE',  'BANGLUONG',       '1',  '192.168.1.1',  'Mozilla/5.0 Chrome/121'),
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
