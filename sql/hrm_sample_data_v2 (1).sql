-- =====================================================
-- HRM SAMPLE DATA V2 - Dữ liệu mẫu hoàn chỉnh & logic
-- Chạy sau hrm_database.sql
-- =====================================================
USE hrm_db;

-- =====================================================
-- 1. PHONG BAN - Cơ cấu tổ chức đầy đủ có phân cấp
-- =====================================================
-- Xóa data mặc định và tạo lại đầy đủ
DELETE FROM PHONGBAN;

INSERT INTO PHONGBAN (maPhongBan, tenPhongBan, phongBanCha, moTa, trangThai) VALUES
-- Cấp công ty
('CONGTY',    'Công ty TNHH ABC Technology',    NULL,       'Công ty công nghệ phần mềm và giải pháp CNTT',              'hoatDong'),
-- Cấp phòng ban chính
('PHONGLD',   'Phòng Lãnh đạo',                  'CONGTY',   'Ban giám đốc và lãnh đạo cấp cao',                          'hoatDong'),
('PHONGNS',   'Phòng Nhân sự',                   'CONGTY',   'Quản lý nhân sự, tuyển dụng, lương thưởng',                 'hoatDong'),
('PHONGKT',   'Phòng Kế toán - Tài chính',       'CONGTY',   'Quản lý tài chính, kế toán, thuế',                         'hoatDong'),
('PHONGKD',   'Phòng Kinh doanh',                'CONGTY',   'Phát triển kinh doanh, bán hàng, chăm sóc khách hàng',     'hoatDong'),
('PHONGIT',   'Phòng Công nghệ thông tin',       'CONGTY',   'Phát triển phần mềm, hệ thống CNTT, bảo mật',              'hoatDong'),
('PHONGMKT',  'Phòng Marketing',                 'CONGTY',   'Truyền thông, quảng cáo, thương hiệu, digital marketing',  'hoatDong'),
('PHONGHC',   'Phòng Hành chính - Tổng hợp',     'CONGTY',   'Quản lý văn phòng, hành chính, tài sản',                   'hoatDong'),
-- Cấp team / nhóm trong phòng IT
('TEAM_BE',   'Team Backend',                    'PHONGIT',  'Phát triển hệ thống backend, API, cơ sở dữ liệu',          'hoatDong'),
('TEAM_FE',   'Team Frontend',                   'PHONGIT',  'Phát triển giao diện, ứng dụng web và mobile',             'hoatDong'),
('TEAM_QA',   'Team QA & Testing',               'PHONGIT',  'Kiểm thử phần mềm, đảm bảo chất lượng sản phẩm',           'hoatDong'),
-- Cấp team trong phòng Kinh doanh
('TEAM_B2B',  'Team Kinh doanh B2B',             'PHONGKD',  'Phát triển khách hàng doanh nghiệp',                       'hoatDong'),
('TEAM_B2C',  'Team Kinh doanh B2C',             'PHONGKD',  'Phát triển khách hàng cá nhân, bán lẻ',                    'hoatDong');

-- =====================================================
-- 2. CHUC VU - Đầy đủ các cấp bậc & chuyên môn
-- =====================================================
DELETE FROM CHUCVU;

INSERT INTO CHUCVU (maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu, moTa, trangThai) VALUES
-- Cấp lãnh đạo
('GD',         'Giám đốc',                    1,  5.50, 15000000, 'Giám đốc điều hành (CEO)',                   'hoatDong'),
('PGD',        'Pho Giám đốc',                2,  4.50, 10000000, 'Pho Giám đốc phụ trách cac mang hoat dong',  'hoatDong'),
('GD_KD',      'Giám đốc Kinh doanh',         2,  4.20,  9000000, 'Giám đốc phụ trách mang Kinh doanh (CSO)',   'hoatDong'),
('GD_CN',      'Giám đốc Công nghệ',          2,  4.20,  9000000, 'Giám đốc phụ trách cong nghe (CTO)',         'hoatDong'),
-- Cấp trưởng / phó phòng
('TP',         'Trưởng phòng',                3,  3.20,  5000000, 'Trưởng đơn vị cấp phòng',                    'hoatDong'),
('PP',         'Phó phòng',                   4,  2.70,  3500000, 'Phó trưởng đơn vị cấp phòng',                'hoatDong'),
-- Cấp team lead / chuyên viên cao cấp
('TT',         'Trưởng nhóm / Team Lead',     5,  2.20,  2000000, 'Trưởng nhóm kỹ thuật hoặc nghiệp vụ',        'hoatDong'),
('CVCC',       'Chuyên viên Cao cấp',         5,  2.10,  1500000, 'Nhân viên cấp cao có kinh nghiệm 5+ năm',    'hoatDong'),
-- Cấp nhân viên chuyên môn IT
('SE_SENIOR',  'Senior Software Engineer',    6,  2.00,  1000000, 'Kỹ sư phần mềm cấp cao, 3+ năm kinh nghiệm', 'hoatDong'),
('SE_MID',     'Software Engineer',           7,  1.50,   500000, 'Kỹ sư phần mềm, 1-3 năm kinh nghiệm',        'hoatDong'),
('SE_JUNIOR',  'Junior Software Engineer',    8,  1.20,   200000, 'Kỹ sư phần mềm mới ra trường',               'hoatDong'),
('QA_SENIOR',  'Senior QA Engineer',          6,  1.80,   800000, 'Kỹ sư kiểm thử cấp cao',                     'hoatDong'),
('QA_MID',     'QA Engineer',                 7,  1.40,   300000, 'Kỹ sư kiểm thử',                             'hoatDong'),
-- Cấp nhân viên chuyên môn khác
('KTV',        'Kế toán viên',                7,  1.40,   300000, 'Kế toán viên chính thức',                    'hoatDong'),
('KTVT',       'Kế toán viên Tổng hợp',       6,  1.70,   600000, 'Kế toán tổng hợp / kế toán trưởng phó',      'hoatDong'),
('NV_KD',      'Nhân viên Kinh doanh',        7,  1.30,   200000, 'Nhân viên bán hàng và phát triển kinh doanh','hoatDong'),
('NVCC_KD',    'Chuyên viên Kinh doanh CC',   6,  1.60,   500000, 'Chuyên viên kinh doanh cao cấp',             'hoatDong'),
('NV_MKT',     'Nhân viên Marketing',         7,  1.30,   200000, 'Nhân viên truyền thông và marketing',        'hoatDong'),
('NV_NS',      'Nhân viên Nhân sự',           7,  1.30,   200000, 'Nhân viên quản lý hồ sơ nhân sự',            'hoatDong'),
('NV_HC',      'Nhân viên Hành chính',        8,  1.10,   100000, 'Nhân viên hành chính văn phòng',             'hoatDong'),
-- Cấp thử việc
('TV',         'Thử việc',                    10, 0.85,         0, 'Nhân sự đang trong thời gian thử việc',      'hoatDong');

-- =====================================================
-- 3. NHAN VIEN - 35 nhan vien, phân bổ hợp lý
-- =====================================================
INSERT INTO NHANVIEN (maNhanVien, loaiHopDong, ngayVaoLam, trangThai, ghiChu) VALUES
-- Lãnh đạo (5)
('NV001', 'khong_xac_dinh',    '2015-01-05', 'dang_lam_viec', 'Giám đốc công ty'),
('NV002', 'khong_xac_dinh',    '2015-06-01', 'dang_lam_viec', 'Pho Giám đốc'),
('NV003', 'khong_xac_dinh',    '2016-03-01', 'dang_lam_viec', 'Giám đốc Kinh doanh'),
('NV004', 'khong_xac_dinh',    '2016-09-01', 'dang_lam_viec', 'Giám đốc Công nghệ - CTO'),
-- Phòng Nhân sự (5)
('NV005', 'khong_xac_dinh',    '2017-02-01', 'dang_lam_viec', 'Trưởng phòng Nhân sự'),
('NV006', 'khong_xac_dinh',    '2018-05-01', 'dang_lam_viec', 'Phó phòng Nhân sự'),
('NV007', 'xac_dinh_thoi_han', '2020-08-01', 'dang_lam_viec', NULL),
('NV008', 'xac_dinh_thoi_han', '2021-11-01', 'dang_lam_viec', NULL),
('NV009', 'thu_viec',          '2025-12-01', 'dang_lam_viec', 'Đang thử việc NS'),
-- Phòng Kế toán (5)
('NV010', 'khong_xac_dinh',    '2017-04-01', 'dang_lam_viec', 'Trưởng phòng Kế toán'),
('NV011', 'khong_xac_dinh',    '2018-09-01', 'dang_lam_viec', 'Phó phòng Kế toán'),
('NV012', 'xac_dinh_thoi_han', '2020-03-01', 'dang_lam_viec', NULL),
('NV013', 'xac_dinh_thoi_han', '2021-07-01', 'dang_lam_viec', NULL),
('NV014', 'xac_dinh_thoi_han', '2023-01-01', 'dang_lam_viec', NULL),
-- Phòng Kinh doanh (7)
('NV015', 'khong_xac_dinh',    '2017-07-01', 'dang_lam_viec', 'Trưởng phòng Kinh doanh'),
('NV016', 'khong_xac_dinh',    '2019-02-01', 'dang_lam_viec', 'Team Lead B2B'),
('NV017', 'xac_dinh_thoi_han', '2020-06-01', 'dang_lam_viec', NULL),
('NV018', 'xac_dinh_thoi_han', '2021-09-01', 'dang_lam_viec', NULL),
('NV019', 'xac_dinh_thoi_han', '2022-04-01', 'dang_lam_viec', NULL),
('NV020', 'xac_dinh_thoi_han', '2023-03-01', 'dang_lam_viec', NULL),
('NV021', 'thu_viec',          '2025-11-01', 'dang_lam_viec', 'Đang thử việc KD'),
-- Phòng IT - Team Backend (5)
('NV022', 'khong_xac_dinh',    '2018-01-15', 'dang_lam_viec', 'Trưởng phòng IT'),
('NV023', 'khong_xac_dinh',    '2019-08-01', 'dang_lam_viec', 'Team Lead Backend'),
('NV024', 'xac_dinh_thoi_han', '2020-10-01', 'dang_lam_viec', NULL),
('NV025', 'xac_dinh_thoi_han', '2022-02-01', 'dang_lam_viec', NULL),
('NV026', 'xac_dinh_thoi_han', '2023-06-01', 'dang_lam_viec', NULL),
-- Phòng IT - Team Frontend (4)
('NV027', 'khong_xac_dinh',    '2019-11-01', 'dang_lam_viec', 'Team Lead Frontend'),
('NV028', 'xac_dinh_thoi_han', '2021-04-01', 'dang_lam_viec', NULL),
('NV029', 'xac_dinh_thoi_han', '2022-09-01', 'dang_lam_viec', NULL),
('NV030', 'thu_viec',          '2025-10-01', 'dang_lam_viec', 'Đang thử việc FE'),
-- Phòng IT - Team QA (3)
('NV031', 'khong_xac_dinh',    '2020-01-01', 'dang_lam_viec', 'Team Lead QA'),
('NV032', 'xac_dinh_thoi_han', '2021-12-01', 'dang_lam_viec', NULL),
-- Phòng Marketing (3)
('NV033', 'khong_xac_dinh',    '2018-07-01', 'dang_lam_viec', 'Trưởng phòng Marketing'),
('NV034', 'xac_dinh_thoi_han', '2021-03-01', 'dang_lam_viec', NULL),
('NV035', 'xac_dinh_thoi_han', '2022-08-01', 'tam_nghi',      'Đang nghỉ thai sản từ 2025-11-01');

-- =====================================================
-- 4. THÔNG TIN CÁ NHÂN
-- =====================================================
INSERT INTO THONGTINCANHAN (maNV, hoTen, ngaySinh, gioiTinh, cccd, dienThoai, email, diaChi, diaChiThuongTru, queQuan, danToc, tonGiao, tinhTrangHonNhan) VALUES
-- Lãnh đạo
(1,  'Nguyen Duc Hung',      '1978-05-12', 'nam', '001078005121', '0901000001', 'hung.nguyen@abctech.vn',    '10 Le Duan, Q1, TP.HCM',           '10 Le Duan, Q1, TP.HCM',        'Ha Noi',      'Kinh', 'Khong', 'da_ket_hon'),
(2,  'Tran Thi Minh Chau',   '1980-09-20', 'nu',  '001080009201', '0901000002', 'chau.tran@abctech.vn',      '35 Nguyen Du, Q1, TP.HCM',          '35 Nguyen Du, Q1, TP.HCM',       'TP.HCM',     'Kinh', 'Khong', 'da_ket_hon'),
(3,  'Le Quang Khai',        '1979-03-15', 'nam', '079079003151', '0901000003', 'khai.le@abctech.vn',        '55 Hai Ba Trung, Q3, TP.HCM',       '55 Hai Ba Trung, Q3, TP.HCM',    'Da Nang',    'Kinh', 'Khong', 'da_ket_hon'),
(4,  'Pham Van Thanh',       '1981-11-08', 'nam', '079081011081', '0901000004', 'thanh.pham@abctech.vn',     '88 Dien Bien Phu, BT, TP.HCM',      '88 Dien Bien Phu, BT, TP.HCM',   'Hai Phong',  'Kinh', 'Khong', 'da_ket_hon'),
-- Phong NS
(5,  'Nguyen Thi Thu Huong', '1985-07-25', 'nu',  '079085007251', '0901000005', 'huong.nguyen@abctech.vn',   '22 Ly Tu Trong, Q1, TP.HCM',        '22 Ly Tu Trong, Q1, TP.HCM',     'Nghe An',    'Kinh', 'Phat giao', 'da_ket_hon'),
(6,  'Vo Minh Duc',          '1988-04-10', 'nam', '079088004101', '0901000006', 'duc.vo@abctech.vn',         '14 Tran Phu, Q5, TP.HCM',           '14 Tran Phu, Q5, TP.HCM',        'TP.HCM',     'Kinh', 'Khong', 'da_ket_hon'),
(7,  'Dang Thi Lan Anh',     '1992-02-18', 'nu',  '079092002181', '0901000007', 'lananh.dang@abctech.vn',    '67 Nguyen Thi Minh Khai, Q3',       '67 Nguyen Thi Minh Khai, Q3',    'Binh Dinh',  'Kinh', 'Khong', 'doc_than'),
(8,  'Bui Quang Huy',        '1994-10-30', 'nam', '079094010301', '0901000008', 'huy.bui@abctech.vn',        '90 Phan Van Tri, BT, TP.HCM',       '90 Phan Van Tri, BT, TP.HCM',    'TP.HCM',     'Kinh', 'Khong', 'doc_than'),
(9,  'Ngo Thi Kieu Oanh',    '2000-06-15', 'nu',  '079100006151', '0901000009', 'oanh.ngo@abctech.vn',       '11 Tran Hung Dao, Q5, TP.HCM',      '11 Tran Hung Dao, Q5, TP.HCM',   'Tien Giang', 'Kinh', 'Khong', 'doc_than'),
-- Phong KT
(10, 'Hoang Thi Bich Ngoc',  '1984-12-03', 'nu',  '079084012031', '0901000010', 'ngoc.hoang@abctech.vn',     '45 Nam Ky Khoi Nghia, Q3',          '45 Nam Ky Khoi Nghia, Q3',       'Hue',        'Kinh', 'Phat giao', 'da_ket_hon'),
(11, 'Trinh Van Liem',        '1987-08-22', 'nam', '079087008221', '0901000011', 'liem.trinh@abctech.vn',     '78 Le Van Sy, Q3, TP.HCM',          '78 Le Van Sy, Q3, TP.HCM',       'Quang Nam',  'Kinh', 'Khong', 'da_ket_hon'),
(12, 'Phan Thi My Linh',     '1991-05-17', 'nu',  '079091005171', '0901000012', 'mylinh.phan@abctech.vn',    '33 Hoang Dieu, Q4, TP.HCM',         '33 Hoang Dieu, Q4, TP.HCM',      'Vinh Long',  'Kinh', 'Khong', 'da_ket_hon'),
(13, 'Ly Thi Thanh Tam',     '1993-01-29', 'nu',  '079093001291', '0901000013', 'thanhTam.ly@abctech.vn',    '56 Bach Dang, BT, TP.HCM',          '56 Bach Dang, BT, TP.HCM',       'Ben Tre',    'Kinh', 'Khong', 'doc_than'),
(14, 'Do Quoc Toan',          '1996-09-11', 'nam', '079096009111', '0901000014', 'toan.do@abctech.vn',        '12 Vo Thi Sau, Q3, TP.HCM',         '12 Vo Thi Sau, Q3, TP.HCM',      'Can Tho',    'Kinh', 'Khong', 'doc_than'),
-- Phong KD
(15, 'Nguyen Anh Tuan',      '1983-06-14', 'nam', '079083006141', '0901000015', 'anh.tuan@abctech.vn',       '22 Pasteur, Q1, TP.HCM',            '22 Pasteur, Q1, TP.HCM',         'Ha Noi',     'Kinh', 'Khong', 'da_ket_hon'),
(16, 'Tran Thi Ngoc Bich',   '1989-03-07', 'nu',  '079089003071', '0901000016', 'ngocbich.tran@abctech.vn',  '99 Nguyen Hue, Q1, TP.HCM',         '99 Nguyen Hue, Q1, TP.HCM',      'TP.HCM',     'Kinh', 'Khong', 'da_ket_hon'),
(17, 'Le Minh Hoang',        '1991-12-25', 'nam', '079091012251', '0901000017', 'hoang.le@abctech.vn',       '15 CMT8, Q10, TP.HCM',              '15 CMT8, Q10, TP.HCM',           'Binh Duong', 'Kinh', 'Khong', 'da_ket_hon'),
(18, 'Pham Thi Thanh Thuy',  '1993-07-19', 'nu',  '079093007191', '0901000018', 'thanhthuy.pham@abctech.vn', '88 Tran Quoc Toan, Q3, TP.HCM',     '88 Tran Quoc Toan, Q3, TP.HCM',  'Da Nang',    'Kinh', 'Khong', 'doc_than'),
(19, 'Vu Duc Manh',          '1995-04-08', 'nam', '079095004081', '0901000019', 'manh.vu@abctech.vn',        '44 Bui Thi Xuan, Q1, TP.HCM',       '44 Bui Thi Xuan, Q1, TP.HCM',    'Thanh Hoa',  'Kinh', 'Khong', 'doc_than'),
(20, 'Doan Thi Phuong Thao', '1997-10-22', 'nu',  '079097010221', '0901000020', 'phuongthao.doan@abctech.vn','33 Ly Chinh Thang, Q3, TP.HCM',     '33 Ly Chinh Thang, Q3, TP.HCM',  'Long An',    'Kinh', 'Khong', 'doc_than'),
(21, 'Cao Van Phuc',          '2000-02-28', 'nam', '079100002281', '0901000021', 'phuc.cao@abctech.vn',       '77 Dien Bien Phu, BT, TP.HCM',      '77 Dien Bien Phu, BT, TP.HCM',   'TP.HCM',     'Kinh', 'Khong', 'doc_than'),
-- Phòng IT Backend
(22, 'Dinh Quang Son',       '1982-08-30', 'nam', '079082008301', '0901000022', 'son.dinh@abctech.vn',       '10 Nguyen Van Cu, Q5, TP.HCM',      '10 Nguyen Van Cu, Q5, TP.HCM',   'Hai Phong',  'Kinh', 'Khong', 'da_ket_hon'),
(23, 'Nguyen Van Khoa',      '1990-04-05', 'nam', '079090004051', '0901000023', 'khoa.nguyen@abctech.vn',    '26 Truong Dinh, Q3, TP.HCM',        '26 Truong Dinh, Q3, TP.HCM',     'Quang Ngai', 'Kinh', 'Khong', 'da_ket_hon'),
(24, 'Tran Thi Mai Trang',   '1992-11-14', 'nu',  '079092011141', '0901000024', 'maitrang.tran@abctech.vn',  '55 Nguyen Thi Minh Khai, Q3',       '55 Nguyen Thi Minh Khai, Q3',    'Khanh Hoa',  'Kinh', 'Khong', 'da_ket_hon'),
(25, 'Hoang Minh Tri',       '1995-07-01', 'nam', '079095007011', '0901000025', 'tri.hoang@abctech.vn',      '38 Vo Van Tan, Q3, TP.HCM',         '38 Vo Van Tan, Q3, TP.HCM',      'Ha Noi',     'Kinh', 'Khong', 'doc_than'),
(26, 'Le Thi Quynh Nhu',     '1998-03-20', 'nu',  '079098003201', '0901000026', 'quynhnhu.le@abctech.vn',    '19 Doan Thi Diem, PN, TP.HCM',      '19 Doan Thi Diem, PN, TP.HCM',   'Tay Ninh',   'Kinh', 'Khong', 'doc_than'),
-- Phòng IT Frontend
(27, 'Ngo Thanh Long',       '1991-09-18', 'nam', '079091009181', '0901000027', 'long.ngo@abctech.vn',       '5 Tran Quy Cap, BT, TP.HCM',        '5 Tran Quy Cap, BT, TP.HCM',     'TP.HCM',     'Kinh', 'Khong', 'da_ket_hon'),
(28, 'Bui Thi Hong Van',     '1994-06-11', 'nu',  '079094006111', '0901000028', 'hongvan.bui@abctech.vn',    '63 Nguyen Gia Tri, BT, TP.HCM',     '63 Nguyen Gia Tri, BT, TP.HCM',  'Dong Nai',   'Kinh', 'Khong', 'doc_than'),
(29, 'Pham Ngoc Duy',        '1997-01-25', 'nam', '079097001251', '0901000029', 'duy.pham@abctech.vn',       '101 Cach Mang Thang 8, Q3',         '101 Cach Mang Thang 8, Q3',      'Binh Thuan', 'Kinh', 'Khong', 'doc_than'),
(30, 'Vo Thi Cam Tu',        '2001-08-07', 'nu',  '079101008071', '0901000030', 'camtu.vo@abctech.vn',       '25 Dinh Tien Hoang, BT, TP.HCM',    '25 Dinh Tien Hoang, BT, TP.HCM', 'TP.HCM',     'Kinh', 'Khong', 'doc_than'),
-- Phòng IT QA
(31, 'Nguyen Xuan Bach',     '1990-12-22', 'nam', '079090012221', '0901000031', 'bach.nguyen@abctech.vn',    '7 Phan Dinh Phung, PN, TP.HCM',     '7 Phan Dinh Phung, PN, TP.HCM',  'Ha Tinh',    'Kinh', 'Khong', 'da_ket_hon'),
(32, 'Tran Ngoc Bao',        '1994-04-16', 'nam', '079094004161', '0901000032', 'bao.tran@abctech.vn',       '48 Hoang Viet, Tan Binh, TP.HCM',   '48 Hoang Viet, Tan Binh, TP.HCM','TP.HCM',     'Kinh', 'Khong', 'doc_than'),
-- Phòng Marketing
(33, 'Le Thi Phuong Linh',   '1986-10-09', 'nu',  '079086010091', '0901000033', 'phuonglinh.le@abctech.vn',  '30 Nguyen Trong Tuyển, PN, TP.HCM', '30 Nguyen Trong Tuyển, PN, TP.HCM','TP.HCM',    'Kinh', 'Khong', 'da_ket_hon'),
(34, 'Pham Dinh Khang',      '1993-05-04', 'nam', '079093005041', '0901000034', 'khang.pham@abctech.vn',     '52 Phu Nhuan, PN, TP.HCM',          '52 Phu Nhuan, PN, TP.HCM',       'Binh Phuoc', 'Kinh', 'Khong', 'doc_than'),
(35, 'Cao Thi Hanh',         '1996-02-14', 'nu',  '079096002141', '0901000035', 'hanh.cao@abctech.vn',       '18 Nguyen Kim, Q10, TP.HCM',        '18 Nguyen Kim, Q10, TP.HCM',     'Dong Thap',  'Kinh', 'Khong', 'da_ket_hon');

-- =====================================================
-- 5. BỔ NHIỆM - Lịch sử bổ nhiệm đầy đủ, có người duyệt
-- =====================================================
INSERT INTO BONHIEM (maNV, maPhongBan, maChucVu, loaiBoNhiem, tyLeHuongLuong, maQuanLy, nguoiDuyet, tuNgay, trangThai, lyDo) VALUES
-- Lãnh đạo công ty
(1,  'PHONGLD',  'GD',        'chinh', 100.00, NULL, NULL, '2015-01-05', 'hieu_luc', 'Bổ nhiệm Giám đốc điều hành'),
(2,  'PHONGLD',  'PGD',       'chinh', 100.00, 1,    1,    '2015-06-01', 'hieu_luc', 'Bổ nhiệm Pho Giám đốc'),
(3,  'PHONGKD',  'GD_KD',     'chinh', 100.00, 1,    1,    '2016-03-01', 'hieu_luc', 'Bổ nhiệm Giám đốc Kinh doanh'),
(4,  'PHONGIT',  'GD_CN',     'chinh', 100.00, 1,    1,    '2016-09-01', 'hieu_luc', 'Bổ nhiệm Giám đốc Công nghệ / CTO'),
-- Phong NS
(5,  'PHONGNS',  'TP',        'chinh', 100.00, 2,    1,    '2017-02-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Nhân sự'),
(6,  'PHONGNS',  'PP',        'chinh', 100.00, 5,    1,    '2018-05-01', 'hieu_luc', 'Bổ nhiệm Phó phòng Nhân sự'),
(7,  'PHONGNS',  'NV_NS',     'chinh', 100.00, 5,    1,    '2020-08-01', 'hieu_luc', 'Nhân viên Nhân sự'),
(8,  'PHONGNS',  'NV_NS',     'chinh', 100.00, 5,    1,    '2021-11-01', 'hieu_luc', 'Nhân viên Nhân sự'),
(9,  'PHONGNS',  'TV',        'chinh', 100.00, 5,    1,    '2025-12-01', 'hieu_luc', 'Thử việc nhân sự'),
-- Phong KT
(10, 'PHONGKT',  'TP',        'chinh', 100.00, 2,    1,    '2017-04-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Kế toán'),
(11, 'PHONGKT',  'PP',        'chinh', 100.00, 10,   1,    '2018-09-01', 'hieu_luc', 'Bổ nhiệm Phó phòng Kế toán'),
(12, 'PHONGKT',  'KTVT',      'chinh', 100.00, 10,   1,    '2020-03-01', 'hieu_luc', 'Kế toán Tổng hợp'),
(13, 'PHONGKT',  'KTV',       'chinh', 100.00, 10,   1,    '2021-07-01', 'hieu_luc', 'Kế toán viên'),
(14, 'PHONGKT',  'KTV',       'chinh', 100.00, 10,   1,    '2023-01-01', 'hieu_luc', 'Kế toán viên'),
-- Phong KD
(15, 'PHONGKD',  'TP',        'chinh', 100.00, 3,    1,    '2017-07-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Kinh doanh'),
(16, 'TEAM_B2B', 'TT',        'chinh', 100.00, 15,   1,    '2019-02-01', 'hieu_luc', 'Team Lead B2B'),
(17, 'TEAM_B2B', 'NVCC_KD',   'chinh', 100.00, 16,   1,    '2020-06-01', 'hieu_luc', 'Chuyen vien Kinh doanh cao cap'),
(18, 'TEAM_B2B', 'NV_KD',     'chinh', 100.00, 16,   1,    '2021-09-01', 'hieu_luc', 'Nhân viên Kinh doanh B2B'),
(19, 'TEAM_B2C', 'NVCC_KD',   'chinh', 100.00, 15,   1,    '2022-04-01', 'hieu_luc', 'Chuyen vien Kinh doanh B2C cao cap'),
(20, 'TEAM_B2C', 'NV_KD',     'chinh', 100.00, 15,   1,    '2023-03-01', 'hieu_luc', 'Nhân viên Kinh doanh B2C'),
(21, 'TEAM_B2C', 'TV',        'chinh', 100.00, 15,   1,    '2025-11-01', 'hieu_luc', 'Thử việc Kinh doanh'),
-- Phòng IT
(22, 'PHONGIT',  'TP',        'chinh', 100.00, 4,    1,    '2018-01-15', 'hieu_luc', 'Bổ nhiệm Trưởng phòng IT'),
(23, 'TEAM_BE',  'TT',        'chinh', 100.00, 22,   1,    '2019-08-01', 'hieu_luc', 'Team Lead Backend'),
(24, 'TEAM_BE',  'SE_SENIOR', 'chinh', 100.00, 23,   1,    '2020-10-01', 'hieu_luc', 'Senior Software Engineer Backend'),
(25, 'TEAM_BE',  'SE_MID',    'chinh', 100.00, 23,   1,    '2022-02-01', 'hieu_luc', 'Software Engineer Backend'),
(26, 'TEAM_BE',  'SE_JUNIOR', 'chinh', 100.00, 23,   1,    '2023-06-01', 'hieu_luc', 'Junior Software Engineer Backend'),
(27, 'TEAM_FE',  'TT',        'chinh', 100.00, 22,   1,    '2019-11-01', 'hieu_luc', 'Team Lead Frontend'),
(28, 'TEAM_FE',  'SE_SENIOR', 'chinh', 100.00, 27,   1,    '2021-04-01', 'hieu_luc', 'Senior Frontend Engineer'),
(29, 'TEAM_FE',  'SE_MID',    'chinh', 100.00, 27,   1,    '2022-09-01', 'hieu_luc', 'Frontend Engineer'),
(30, 'TEAM_FE',  'TV',        'chinh', 100.00, 27,   1,    '2025-10-01', 'hieu_luc', 'Thử việc Frontend'),
(31, 'TEAM_QA',  'TT',        'chinh', 100.00, 22,   1,    '2020-01-01', 'hieu_luc', 'Team Lead QA'),
(32, 'TEAM_QA',  'QA_MID',    'chinh', 100.00, 31,   1,    '2021-12-01', 'hieu_luc', 'QA Engineer'),
-- Phòng Marketing
(33, 'PHONGMKT', 'TP',        'chinh', 100.00, 2,    1,    '2018-07-01', 'hieu_luc', 'Bổ nhiệm Trưởng phòng Marketing'),
(34, 'PHONGMKT', 'NV_MKT',    'chinh', 100.00, 33,   1,    '2021-03-01', 'hieu_luc', 'Nhân viên Marketing'),
(35, 'PHONGMKT', 'NV_MKT',    'chinh', 100.00, 33,   1,    '2022-08-01', 'hieu_luc', 'Nhân viên Marketing - đang tạm nghỉ thai sản'),
-- Kiêm nhiệm: NV022 (TP IT) kiem nhiem CVCC trong dự án dac biet
(22, 'PHONGIT',  'CVCC',      'kiem_nhiem', 20.00, 4, 1,   '2023-01-01', 'hieu_luc', 'Kiêm nhiệm Chuyên viên tư vấn dự án chuyển đổi số');

-- =====================================================
-- 5.5. VAI TRÒ & QUYỀN
-- =====================================================
INSERT INTO VAITRO (maVaiTro, tenVaiTro, moTa, laVaiTroHeThong, trangThai) VALUES
('ADMIN',         'Quản trị viên',   'Toàn quyền quản trị hệ thống',            TRUE,  'hoatDong'),
('TONG_GIAM_DOC', 'Tổng giám đốc',   'Điều hành cấp cao toàn công ty',          FALSE, 'hoatDong'),
('GIAM_DOC',      'Giám đốc',        'Quản lý nghiep vu cap công ty',           FALSE, 'hoatDong'),
('PHO_GIAM_DOC',  'Phó giám đốc',    'Hỗ trợ giám đốc, điều hành bộ phận',      FALSE, 'hoatDong'),
('TRUONG_PHONG',  'Trưởng phòng',    'Quản lý phòng ban và phê duyệt cấp dept', FALSE, 'hoatDong'),
('QUAN_LY',       'Quản lý',         'Quản lý nhóm và phê duyệt cấp team',      FALSE, 'hoatDong'),
('NHAN_VIEN',     'Nhân viên',       'Nhân viên thông thường',                   FALSE, 'hoatDong');

INSERT INTO QUYEN (maQuyen, tenQuyen, nhomQuyen) VALUES
-- Nhân viên
('EMPLOYEE_VIEW_ALL',      'Xem toàn bộ nhân viên',           'Nhân viên'),
('EMPLOYEE_VIEW_DEPT',     'Xem nhân viên trong phòng',       'Nhân viên'),
('EMPLOYEE_VIEW_TEAM',     'Xem nhân viên dưới quyền',        'Nhân viên'),
('EMPLOYEE_VIEW_SELF',     'Xem thông tin cá nhân',           'Nhân viên'),
('EMPLOYEE_CREATE',        'Tạo nhân viên',                   'Nhân viên'),
('EMPLOYEE_UPDATE',        'Cập nhật nhân viên',              'Nhân viên'),
('EMPLOYEE_DELETE',        'Xóa nhân viên',                   'Nhân viên'),
-- To chuc
('DEPARTMENT_VIEW',        'Xem phòng ban',                   'Tổ chức'),
('DEPARTMENT_MANAGE',      'Quản lý phòng ban',               'Tổ chức'),
('POSITION_VIEW',          'Xem chức vụ',                     'Tổ chức'),
('POSITION_MANAGE',        'Quản lý chức vụ',                 'Tổ chức'),
-- Bổ nhiệm
('APPOINTMENT_VIEW_ALL',   'Xem tất cả bổ nhiệm',             'Bổ nhiệm'),
('APPOINTMENT_VIEW_DEPT',  'Xem bổ nhiệm trong phòng',        'Bổ nhiệm'),
('APPOINTMENT_VIEW_TEAM',  'Xem bổ nhiệm dưới quyền',         'Bổ nhiệm'),
('APPOINTMENT_VIEW_SELF',  'Xem bổ nhiệm cá nhân',            'Bổ nhiệm'),
('APPOINTMENT_CREATE',     'Tạo bổ nhiệm',                    'Bổ nhiệm'),
('APPOINTMENT_APPROVE',    'Duyệt bổ nhiệm',                  'Bổ nhiệm'),
-- Cham cong
('ATTENDANCE_VIEW_ALL',    'Xem tất cả chấm công',            'Chấm công'),
('ATTENDANCE_VIEW_DEPT',   'Xem chấm công trong phòng',       'Chấm công'),
('ATTENDANCE_VIEW_TEAM',   'Xem chấm công dưới quyền',        'Chấm công'),
('ATTENDANCE_VIEW_SELF',   'Xem chấm công cá nhân',           'Chấm công'),
('ATTENDANCE_MANAGE',      'Quản lý chấm công',               'Chấm công'),
-- Hop dong
('CONTRACT_VIEW_ALL',      'Xem tất cả hợp đồng',             'Hợp đồng'),
('CONTRACT_VIEW_DEPT',     'Xem hợp đồng trong phòng',        'Hợp đồng'),
('CONTRACT_VIEW_TEAM',     'Xem hợp đồng dưới quyền',         'Hợp đồng'),
('CONTRACT_VIEW_SELF',     'Xem hợp đồng cá nhân',            'Hợp đồng'),
('CONTRACT_CREATE',        'Tạo hợp đồng',                    'Hợp đồng'),
('CONTRACT_UPDATE',        'Cập nhật hợp đồng',               'Hợp đồng'),
('CONTRACT_MANAGE',        'Quản lý hợp đồng',                'Hợp đồng'),
-- Luong
('PAYROLL_VIEW_ALL',       'Xem tất cả lương',                'Lương'),
('PAYROLL_VIEW_DEPT',      'Xem lương trong phòng',           'Lương'),
('PAYROLL_VIEW_TEAM',      'Xem lương dưới quyền',            'Lương'),
('PAYROLL_VIEW_SELF',      'Xem lương cá nhân',               'Lương'),
('PAYROLL_CALCULATE',      'Tính lương',                      'Lương'),
-- Nghi phep
('LEAVE_VIEW_ALL',         'Xem tất cả nghỉ phép',            'Nghỉ phép'),
('LEAVE_VIEW_DEPT',        'Xem nghỉ phép trong phòng',       'Nghỉ phép'),
('LEAVE_VIEW_TEAM',        'Xem nghỉ phép dưới quyền',        'Nghỉ phép'),
('LEAVE_VIEW_SELF',        'Xem nghỉ phép cá nhân',           'Nghỉ phép'),
('LEAVE_CREATE',           'Tạo đơn nghỉ phép',               'Nghỉ phép'),
('LEAVE_MANAGE',           'Quản lý nghỉ phép',               'Nghỉ phép'),
('LEAVE_APPROVE_ALL',      'Duyệt tất cả nghỉ phép',          'Nghỉ phép'),
('LEAVE_APPROVE_DEPT',     'Duyệt nghỉ phép trong phòng',     'Nghỉ phép'),
('LEAVE_APPROVE_TEAM',     'Duyệt nghỉ phép dưới quyền',      'Nghỉ phép'),
-- Danh gia
('EVAL_VIEW_ALL',          'Xem tất cả đánh giá',             'Đánh giá'),
('EVAL_VIEW_DEPT',         'Xem đánh giá trong phòng',        'Đánh giá'),
('EVAL_VIEW_TEAM',         'Xem đánh giá dưới quyền',         'Đánh giá'),
('EVAL_VIEW_SELF',         'Xem đánh giá cá nhân',            'Đánh giá'),
('EVAL_MANAGE',            'Quản lý đợt đánh giá',            'Đánh giá'),
('EVAL_REVIEW_ALL',        'Đánh giá toàn bộ nhân viên',      'Đánh giá'),
('EVAL_REVIEW_DEPT',       'Đánh giá nhân viên trong phòng',  'Đánh giá'),
('EVAL_REVIEW_TEAM',       'Đánh giá nhân viên dưới quyền',   'Đánh giá'),
-- Tuyển dung
('RECRUITMENT_VIEW_ALL',   'Xem tất cả tuyển dụng',           'Tuyển dụng'),
('RECRUITMENT_VIEW_DEPT',  'Xem tuyển dụng trong phòng',      'Tuyển dụng'),
('RECRUITMENT_VIEW_TEAM',  'Xem tuyển dụng dưới quyền',       'Tuyển dụng'),
('RECRUITMENT_VIEW_SELF',  'Xem tuyển dụng cá nhân',          'Tuyển dụng'),
('RECRUITMENT_MANAGE',     'Quản lý tuyển dụng',              'Tuyển dụng'),
-- Bao cao & Thông báo
('REPORT_VIEW',            'Xem báo cáo',                     'Báo cáo'),
('REPORT_EXPORT',          'Xuất báo cáo',                    'Báo cáo'),
('NOTIFICATION_SEND',      'Gửi thông báo',                   'Thông báo'),
-- Quản trị he thong
('USER_VIEW',              'Xem danh sách tài khoản',         'Tài khoản'),
('USER_CREATE',            'Tạo tài khoản',                   'Tài khoản'),
('USER_UPDATE',            'Cập nhật tài khoản',              'Tài khoản'),
('USER_DELETE',            'Xóa tài khoản',                   'Tài khoản'),
('ROLE_VIEW',              'Xem vai trò',                     'Vai trò'),
('ROLE_CREATE',            'Tạo vai trò',                     'Vai trò'),
('ROLE_UPDATE',            'Cập nhật vai trò',                'Vai trò'),
('ROLE_DELETE',            'Xóa vai trò',                     'Vai trò'),
('SETTINGS_VIEW',          'Xem cài đặt',                     'Cài đặt'),
('SETTINGS_UPDATE',        'Cập nhật cài đặt',                'Cài đặt');

-- ADMIN: tat ca quyen
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'ADMIN', maQuyen FROM QUYEN;

-- NHAN_VIEN
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('NHAN_VIEN','EMPLOYEE_VIEW_SELF'),
('NHAN_VIEN','APPOINTMENT_VIEW_SELF'),
('NHAN_VIEN','ATTENDANCE_VIEW_SELF'),
('NHAN_VIEN','CONTRACT_VIEW_SELF'),
('NHAN_VIEN','PAYROLL_VIEW_SELF'),
('NHAN_VIEN','LEAVE_VIEW_SELF'),('NHAN_VIEN','LEAVE_CREATE'),
('NHAN_VIEN','EVAL_VIEW_SELF'),
('NHAN_VIEN','RECRUITMENT_VIEW_SELF'),
('NHAN_VIEN','REPORT_VIEW');

-- QUAN_LY (ke thua NHAN_VIEN)
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'QUAN_LY', maQuyen FROM VAITRO_QUYEN WHERE maVaiTro = 'NHAN_VIEN';
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('QUAN_LY','EMPLOYEE_VIEW_TEAM'),
('QUAN_LY','APPOINTMENT_VIEW_TEAM'),
('QUAN_LY','ATTENDANCE_VIEW_TEAM'),
('QUAN_LY','CONTRACT_VIEW_TEAM'),
('QUAN_LY','PAYROLL_VIEW_TEAM'),
('QUAN_LY','LEAVE_VIEW_TEAM'),
('QUAN_LY','LEAVE_APPROVE_TEAM'),
('QUAN_LY','EVAL_VIEW_TEAM'),
('QUAN_LY','EVAL_REVIEW_TEAM'),
('QUAN_LY','RECRUITMENT_VIEW_TEAM'),
('QUAN_LY','NOTIFICATION_SEND');

-- TRUONG_PHONG (ke thua QUAN_LY)
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'TRUONG_PHONG', maQuyen FROM VAITRO_QUYEN WHERE maVaiTro = 'QUAN_LY';
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('TRUONG_PHONG','EMPLOYEE_VIEW_DEPT'),
('TRUONG_PHONG','APPOINTMENT_VIEW_DEPT'),
('TRUONG_PHONG','ATTENDANCE_VIEW_DEPT'),
('TRUONG_PHONG','CONTRACT_VIEW_DEPT'),
('TRUONG_PHONG','PAYROLL_VIEW_DEPT'),
('TRUONG_PHONG','LEAVE_VIEW_DEPT'),('TRUONG_PHONG','LEAVE_APPROVE_DEPT'),
('TRUONG_PHONG','EVAL_VIEW_DEPT'),('TRUONG_PHONG','EVAL_REVIEW_DEPT'),
('TRUONG_PHONG','RECRUITMENT_VIEW_DEPT');

-- PHO_GIAM_DOC (ke thua TRUONG_PHONG)
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'PHO_GIAM_DOC', maQuyen FROM VAITRO_QUYEN WHERE maVaiTro = 'TRUONG_PHONG';
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('PHO_GIAM_DOC','EMPLOYEE_VIEW_ALL'),
('PHO_GIAM_DOC','APPOINTMENT_VIEW_ALL'),
('PHO_GIAM_DOC','ATTENDANCE_VIEW_ALL'),
('PHO_GIAM_DOC','CONTRACT_VIEW_ALL'),
('PHO_GIAM_DOC','PAYROLL_VIEW_ALL'),
('PHO_GIAM_DOC','LEAVE_VIEW_ALL'),
('PHO_GIAM_DOC','EVAL_VIEW_ALL'),
('PHO_GIAM_DOC','RECRUITMENT_VIEW_ALL'),
('PHO_GIAM_DOC','REPORT_EXPORT');

-- GIAM_DOC (ke thua PHO_GIAM_DOC)
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'GIAM_DOC', maQuyen FROM VAITRO_QUYEN WHERE maVaiTro = 'PHO_GIAM_DOC';
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('GIAM_DOC','APPOINTMENT_APPROVE'),
('GIAM_DOC','CONTRACT_MANAGE'),
('GIAM_DOC','LEAVE_MANAGE'),
('GIAM_DOC','RECRUITMENT_MANAGE'),
('GIAM_DOC','EVAL_MANAGE'),
('GIAM_DOC','PAYROLL_CALCULATE');

-- TONG_GIAM_DOC (ke thua GIAM_DOC)
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'TONG_GIAM_DOC', maQuyen FROM VAITRO_QUYEN WHERE maVaiTro = 'GIAM_DOC';
INSERT IGNORE INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('TONG_GIAM_DOC','DEPARTMENT_VIEW'),
('TONG_GIAM_DOC','POSITION_VIEW'),
('TONG_GIAM_DOC','USER_VIEW'),
('TONG_GIAM_DOC','ROLE_VIEW'),
('TONG_GIAM_DOC','SETTINGS_VIEW');

-- =====================================================
-- 6. TAI KHOAN - Cho toan bo nhan vien
-- =====================================================
INSERT INTO TAIKHOAN (tenDangNhap, matKhau, maNV, maVaiTro, email, hoatDong) VALUES
('admin',           '123',   NULL, 'ADMIN',    'admin@abctech.vn',              TRUE),
('hung.nguyen',     '123',             1,    'TONG_GIAM_DOC',    'hung.nguyen@abctech.vn',        TRUE),
('chau.tran',       '123',             2,    'GIAM_DOC',    'chau.tran@abctech.vn',          TRUE),
('khai.le',         '123',             3,    'PHO_GIAM_DOC',  'khai.le@abctech.vn',            TRUE),
('thanh.pham',      '123',             4,    'TRUONG_PHONG',  'thanh.pham@abctech.vn',         TRUE),
('huong.nguyen',    '123',             5,    'TRUONG_PHONG',       'huong.nguyen@abctech.vn',       TRUE),
('duc.vo',          '123',             6,    'TRUONG_PHONG',       'duc.vo@abctech.vn',             TRUE),
('lananh.dang',     '123',             7,    'NHAN_VIEN', 'lananh.dang@abctech.vn',        TRUE),
('huy.bui',         '123',             8,    'NHAN_VIEN', 'huy.bui@abctech.vn',            TRUE),
('oanh.ngo',        '123',             9,    'NHAN_VIEN', 'oanh.ngo@abctech.vn',           TRUE),
('ngoc.hoang',      '123',             10,   'TRUONG_PHONG',       'ngoc.hoang@abctech.vn',         TRUE),
('liem.trinh',      '123',             11,   'QUAN_LY',  'liem.trinh@abctech.vn',         TRUE),
('mylinh.phan',     '123',             12,   'NHAN_VIEN', 'mylinh.phan@abctech.vn',        TRUE),
('thanhTam.ly',     '123',             13,   'NHAN_VIEN', 'thanhTam.ly@abctech.vn',        TRUE),
('toan.do',         '123',             14,   'NHAN_VIEN', 'toan.do@abctech.vn',            TRUE),
('anh.tuan',        '123',             15,   'QUAN_LY',  'anh.tuan@abctech.vn',           TRUE),
('ngocbich.tran',   '123',             16,   'QUAN_LY',  'ngocbich.tran@abctech.vn',      TRUE),
('hoang.le',        '123',             17,   'NHAN_VIEN', 'hoang.le@abctech.vn',           TRUE),
('thanhthuy.pham',  '123',             18,   'NHAN_VIEN', 'thanhthuy.pham@abctech.vn',     TRUE),
('manh.vu',         '123',             19,   'NHAN_VIEN', 'manh.vu@abctech.vn',            TRUE),
('phuongthao.doan', '123',             20,   'NHAN_VIEN', 'phuongthao.doan@abctech.vn',    TRUE),
('phuc.cao',        '123',             21,   'NHAN_VIEN', 'phuc.cao@abctech.vn',           TRUE),
('son.dinh',        '123',             22,   'QUAN_LY',  'son.dinh@abctech.vn',           TRUE),
('khoa.nguyen',     '123',             23,   'QUAN_LY',  'khoa.nguyen@abctech.vn',        TRUE),
('maitrang.tran',   '123',             24,   'NHAN_VIEN', 'maitrang.tran@abctech.vn',      TRUE),
('tri.hoang',       '123',             25,   'NHAN_VIEN', 'tri.hoang@abctech.vn',          TRUE),
('quynhnhu.le',     '123',             26,   'NHAN_VIEN', 'quynhnhu.le@abctech.vn',        TRUE),
('long.ngo',        '123',             27,   'QUAN_LY',  'long.ngo@abctech.vn',           TRUE),
('hongvan.bui',     '123',             28,   'NHAN_VIEN', 'hongvan.bui@abctech.vn',        TRUE),
('duy.pham',        '123',             29,   'NHAN_VIEN', 'duy.pham@abctech.vn',           TRUE),
('camtu.vo',        '123',             30,   'NHAN_VIEN', 'camtu.vo@abctech.vn',           TRUE),
('bach.nguyen',     '123',             31,   'QUAN_LY',  'bach.nguyen@abctech.vn',        TRUE),
('bao.tran',        '123',             32,   'NHAN_VIEN', 'bao.tran@abctech.vn',           TRUE),
('phuonglinh.le',   '123',             33,   'QUAN_LY',  'phuonglinh.le@abctech.vn',      TRUE),
('khang.pham',      '123',             34,   'NHAN_VIEN', 'khang.pham@abctech.vn',         TRUE),
('hanh.cao',        '123',             35,   'NHAN_VIEN', 'hanh.cao@abctech.vn',           FALSE);

-- =====================================================
-- 7. HOP DONG LAO DONG
-- =====================================================
INSERT INTO HOPDONGLAODONG (soHopDong, maNV, loaiHopDong, luongCoSo, ngayKy, ngayHieuLuc, ngayHetHieuLuc, trangThai, noiDung) VALUES
-- Lãnh đạo (khong xac dinh)
('HD2015-GD-001',   1,  'khong_xac_dinh',    80000000, '2015-01-03', '2015-01-05', NULL,         'hieu_luc', 'Hop dong lao dong khong xac dinh thoi han - Giám đốc'),
('HD2015-PGD-001',  2,  'khong_xac_dinh',    60000000, '2015-05-28', '2015-06-01', NULL,         'hieu_luc', 'Hop dong lao dong khong xac dinh thoi han - Pho GD'),
('HD2016-GDK-001',  3,  'khong_xac_dinh',    55000000, '2016-02-25', '2016-03-01', NULL,         'hieu_luc', 'Hop dong lao dong khong xac dinh thoi han - GD KD'),
('HD2016-CTO-001',  4,  'khong_xac_dinh',    55000000, '2016-08-26', '2016-09-01', NULL,         'hieu_luc', 'Hop dong lao dong khong xac dinh thoi han - CTO'),
-- Phong NS
('HD2017-NS-005',   5,  'khong_xac_dinh',    22000000, '2017-01-25', '2017-02-01', NULL,         'hieu_luc', 'Hop dong TP Nhân sự'),
('HD2018-NS-006',   6,  'khong_xac_dinh',    18000000, '2018-04-25', '2018-05-01', NULL,         'hieu_luc', 'Hop dong PP Nhân sự'),
('HD2020-NS-007',   7,  'xac_dinh_thoi_han', 10000000, '2020-07-25', '2020-08-01', '2022-08-01', 'het_han',  'Hop dong lan 1 NV NS'),
('HD2022-NS-007',   7,  'xac_dinh_thoi_han', 11500000, '2022-07-28', '2022-08-01', '2024-08-01', 'het_han',  'Hop dong lan 2 NV NS'),
('HD2024-NS-007',   7,  'xac_dinh_thoi_han', 13000000, '2024-07-28', '2024-08-01', '2026-08-01', 'hieu_luc', 'Hop dong lan 3 NV NS'),
('HD2021-NS-008',   8,  'xac_dinh_thoi_han',  9500000, '2021-10-25', '2021-11-01', '2023-11-01', 'het_han',  'Hop dong lan 1 NV NS'),
('HD2023-NS-008',   8,  'xac_dinh_thoi_han', 11000000, '2023-10-25', '2023-11-01', '2025-11-01', 'het_han',  'Hop dong lan 2 NV NS'),
('HD2025-NS-008',   8,  'xac_dinh_thoi_han', 12500000, '2025-10-25', '2025-11-01', '2027-11-01', 'hieu_luc', 'Hop dong lan 3 NV NS'),
('HD2025-NS-009',   9,  'thu_viec',            8000000, '2025-11-28', '2025-12-01', '2026-03-01', 'hieu_luc', 'Hop dong thu viec NS'),
-- Phong KT
('HD2017-KT-010',   10, 'khong_xac_dinh',    25000000, '2017-03-28', '2017-04-01', NULL,         'hieu_luc', 'Hop dong TP Kế toán'),
('HD2018-KT-011',   11, 'khong_xac_dinh',    20000000, '2018-08-25', '2018-09-01', NULL,         'hieu_luc', 'Hop dong PP Kế toán'),
('HD2020-KT-012',   12, 'xac_dinh_thoi_han', 12000000, '2020-02-25', '2020-03-01', '2022-03-01', 'het_han',  'Hop dong lan 1 KTVT'),
('HD2022-KT-012',   12, 'xac_dinh_thoi_han', 14000000, '2022-02-25', '2022-03-01', '2024-03-01', 'het_han',  'Hop dong lan 2 KTVT'),
('HD2024-KT-012',   12, 'xac_dinh_thoi_han', 16000000, '2024-02-25', '2024-03-01', '2026-03-01', 'hieu_luc', 'Hop dong lan 3 KTVT'),
('HD2021-KT-013',   13, 'xac_dinh_thoi_han', 10000000, '2021-06-25', '2021-07-01', '2023-07-01', 'het_han',  'Hop dong lan 1 KTV'),
('HD2023-KT-013',   13, 'xac_dinh_thoi_han', 12000000, '2023-06-25', '2023-07-01', '2025-07-01', 'het_han',  'Hop dong lan 2 KTV'),
('HD2025-KT-013',   13, 'xac_dinh_thoi_han', 13500000, '2025-06-25', '2025-07-01', '2027-07-01', 'hieu_luc', 'Hop dong lan 3 KTV'),
('HD2023-KT-014',   14, 'xac_dinh_thoi_han',  9500000, '2022-12-28', '2023-01-01', '2025-01-01', 'het_han',  'Hop dong lan 1 KTV'),
('HD2025-KT-014',   14, 'xac_dinh_thoi_han', 11000000, '2024-12-28', '2025-01-01', '2027-01-01', 'hieu_luc', 'Hop dong lan 2 KTV'),
-- Phong KD
('HD2017-KD-015',   15, 'khong_xac_dinh',    28000000, '2017-06-25', '2017-07-01', NULL,         'hieu_luc', 'Hop dong TP Kinh doanh'),
('HD2019-KD-016',   16, 'khong_xac_dinh',    22000000, '2019-01-28', '2019-02-01', NULL,         'hieu_luc', 'Hop dong TL B2B'),
('HD2020-KD-017',   17, 'xac_dinh_thoi_han', 13000000, '2020-05-28', '2020-06-01', '2022-06-01', 'het_han',  'Hop dong lan 1 NV KD'),
('HD2022-KD-017',   17, 'xac_dinh_thoi_han', 16000000, '2022-05-28', '2022-06-01', '2024-06-01', 'het_han',  'Hop dong lan 2 NVCC KD'),
('HD2024-KD-017',   17, 'xac_dinh_thoi_han', 18000000, '2024-05-28', '2024-06-01', '2026-06-01', 'hieu_luc', 'Hop dong lan 3 NVCC KD'),
('HD2021-KD-018',   18, 'xac_dinh_thoi_han', 11000000, '2021-08-25', '2021-09-01', '2023-09-01', 'het_han',  'Hop dong lan 1 NV KD'),
('HD2023-KD-018',   18, 'xac_dinh_thoi_han', 13000000, '2023-08-25', '2023-09-01', '2025-09-01', 'het_han',  'Hop dong lan 2 NV KD'),
('HD2025-KD-018',   18, 'xac_dinh_thoi_han', 14500000, '2025-08-25', '2025-09-01', '2027-09-01', 'hieu_luc', 'Hop dong lan 3 NV KD'),
('HD2022-KD-019',   19, 'xac_dinh_thoi_han', 14000000, '2022-03-28', '2022-04-01', '2024-04-01', 'het_han',  'Hop dong lan 1 NVCC KD B2C'),
('HD2024-KD-019',   19, 'xac_dinh_thoi_han', 17000000, '2024-03-28', '2024-04-01', '2026-04-01', 'hieu_luc', 'Hop dong lan 2 NVCC KD B2C'),
('HD2023-KD-020',   20, 'xac_dinh_thoi_han', 10000000, '2023-02-25', '2023-03-01', '2025-03-01', 'het_han',  'Hop dong lan 1 NV KD B2C'),
('HD2025-KD-020',   20, 'xac_dinh_thoi_han', 12000000, '2025-02-25', '2025-03-01', '2027-03-01', 'hieu_luc', 'Hop dong lan 2 NV KD B2C'),
('HD2025-KD-021',   21, 'thu_viec',            8000000, '2025-10-28', '2025-11-01', '2026-02-01', 'hieu_luc', 'Hop dong thu viec KD'),
-- Phòng IT
('HD2018-IT-022',   22, 'khong_xac_dinh',    35000000, '2018-01-10', '2018-01-15', NULL,         'hieu_luc', 'Hop dong TP IT'),
('HD2019-IT-023',   23, 'khong_xac_dinh',    28000000, '2019-07-28', '2019-08-01', NULL,         'hieu_luc', 'Hop dong TL Backend'),
('HD2020-IT-024',   24, 'xac_dinh_thoi_han', 20000000, '2020-09-28', '2020-10-01', '2022-10-01', 'het_han',  'Hop dong lan 1 SE Senior BE'),
('HD2022-IT-024',   24, 'xac_dinh_thoi_han', 24000000, '2022-09-28', '2022-10-01', '2024-10-01', 'het_han',  'Hop dong lan 2 SE Senior BE'),
('HD2024-IT-024',   24, 'xac_dinh_thoi_han', 28000000, '2024-09-28', '2024-10-01', '2026-10-01', 'hieu_luc', 'Hop dong lan 3 SE Senior BE'),
('HD2022-IT-025',   25, 'xac_dinh_thoi_han', 15000000, '2022-01-28', '2022-02-01', '2024-02-01', 'het_han',  'Hop dong lan 1 SE Mid BE'),
('HD2024-IT-025',   25, 'xac_dinh_thoi_han', 18000000, '2024-01-28', '2024-02-01', '2026-02-01', 'hieu_luc', 'Hop dong lan 2 SE Mid BE'),
('HD2023-IT-026',   26, 'xac_dinh_thoi_han', 11000000, '2023-05-28', '2023-06-01', '2025-06-01', 'het_han',  'Hop dong lan 1 SE Junior BE'),
('HD2025-IT-026',   26, 'xac_dinh_thoi_han', 13000000, '2025-05-28', '2025-06-01', '2027-06-01', 'hieu_luc', 'Hop dong lan 2 SE Junior BE'),
('HD2019-IT-027',   27, 'khong_xac_dinh',    26000000, '2019-10-28', '2019-11-01', NULL,         'hieu_luc', 'Hop dong TL Frontend'),
('HD2021-IT-028',   28, 'xac_dinh_thoi_han', 20000000, '2021-03-28', '2021-04-01', '2023-04-01', 'het_han',  'Hop dong lan 1 SE Senior FE'),
('HD2023-IT-028',   28, 'xac_dinh_thoi_han', 24000000, '2023-03-28', '2023-04-01', '2025-04-01', 'het_han',  'Hop dong lan 2 SE Senior FE'),
('HD2025-IT-028',   28, 'xac_dinh_thoi_han', 27000000, '2025-03-28', '2025-04-01', '2027-04-01', 'hieu_luc', 'Hop dong lan 3 SE Senior FE'),
('HD2022-IT-029',   29, 'xac_dinh_thoi_han', 13000000, '2022-08-28', '2022-09-01', '2024-09-01', 'het_han',  'Hop dong lan 1 SE Mid FE'),
('HD2024-IT-029',   29, 'xac_dinh_thoi_han', 15500000, '2024-08-28', '2024-09-01', '2026-09-01', 'hieu_luc', 'Hop dong lan 2 SE Mid FE'),
('HD2025-IT-030',   30, 'thu_viec',            8500000, '2025-09-28', '2025-10-01', '2026-01-01', 'hieu_luc', 'Hop dong thu viec FE'),
('HD2020-IT-031',   31, 'khong_xac_dinh',    22000000, '2019-12-28', '2020-01-01', NULL,         'hieu_luc', 'Hop dong TL QA'),
('HD2021-IT-032',   32, 'xac_dinh_thoi_han', 13000000, '2021-11-28', '2021-12-01', '2023-12-01', 'het_han',  'Hop dong lan 1 QA Mid'),
('HD2023-IT-032',   32, 'xac_dinh_thoi_han', 15000000, '2023-11-28', '2023-12-01', '2025-12-01', 'het_han',  'Hop dong lan 2 QA Mid'),
('HD2025-IT-032',   32, 'xac_dinh_thoi_han', 17000000, '2025-11-28', '2025-12-01', '2027-12-01', 'hieu_luc', 'Hop dong lan 3 QA Mid'),
-- Phòng Marketing
('HD2018-MKT-033',  33, 'khong_xac_dinh',    22000000, '2018-06-25', '2018-07-01', NULL,         'hieu_luc', 'Hop dong TP Marketing'),
('HD2021-MKT-034',  34, 'xac_dinh_thoi_han', 11000000, '2021-02-25', '2021-03-01', '2023-03-01', 'het_han',  'Hop dong lan 1 NV MKT'),
('HD2023-MKT-034',  34, 'xac_dinh_thoi_han', 13000000, '2023-02-25', '2023-03-01', '2025-03-01', 'het_han',  'Hop dong lan 2 NV MKT'),
('HD2025-MKT-034',  34, 'xac_dinh_thoi_han', 14500000, '2025-02-25', '2025-03-01', '2027-03-01', 'hieu_luc', 'Hop dong lan 3 NV MKT'),
('HD2022-MKT-035',  35, 'xac_dinh_thoi_han', 11000000, '2022-07-28', '2022-08-01', '2024-08-01', 'het_han',  'Hop dong lan 1 NV MKT - nghi thai san'),
('HD2024-MKT-035',  35, 'xac_dinh_thoi_han', 13000000, '2024-07-28', '2024-08-01', '2026-08-01', 'hieu_luc', 'Hop dong lan 2 NV MKT - dang nghi thai san');

-- =====================================================
-- 8. CA LAM
-- =====================================================
INSERT INTO CALAM (maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, choPhepLamThem, moTa, trangThai) VALUES
('HANH_CHINH', 'Ca hanh chinh', '08:00:00', '17:00:00', 8.00, TRUE,  'Ca lam viec hanh chinh van phong', 'hoatDong'),
('CA_SANG',    'Ca sáng',       '06:00:00', '14:00:00', 8.00, TRUE,  'Ca sáng cho bo phan san xuat',     'hoatDong'),
('CA_CHIEU',   'Ca chieu',      '14:00:00', '22:00:00', 8.00, TRUE,  'Ca chieu cho bo phan san xuat',    'hoatDong'),
('CA_DEM',     'Ca dem',        '22:00:00', '06:00:00', 8.00, FALSE, 'Ca dem cho bo phan ky thuat',      'hoatDong');

-- =====================================================
-- 9. CHAM CONG - Thang 1 va 2/2026 cho toan bo NV
-- Cac ngày lam viec tháng 1: Mon-Fri (khong tinh cuoi tuan, le)
-- Cac ngày lam viec tháng 2: Mon-Fri
-- =====================================================

-- Helper: Thang 1/2026 - Ngay lam viec (bo qua 1/1 la nghi le)
-- Thang 1: 2,5,6,7,8,9,12,13,14,15,16,19,20,21,22,23,26,27,28,29,30 = 21 ngày
-- Thang 2: 2,3,4,5,6,9,10,11,12,13,16,17,18,19,20,23,24,25,26,27 = 19 ngày

-- *** CHAM CONG THANG 1/2026 ***
-- Tao du lieu cham cong cho cac nhan vien chinh (bo qua nhan vien tam nghi NV035)
-- Format: maNV, ngày, maCaLam, giờVao, giờRa, soGioLam, giờLamThem, trangThai, phuongThucChamCong

INSERT INTO CHAMCONG (maNV, ngay, maCaLam, gioVao, gioRa, soGioLam, gioLamThem, trangThai, phuongThucChamCong, ghiChu) VALUES
-- NV001 (GD) - tháng 1
(1,'2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:30',8.00,0.50,'dung_giờ','the_tu',NULL),
(1,'2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 18:00',8.00,1.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
-- NV005 (TP NS) - tháng 1: 1 ngày di muon, 1 ngày nghi phep
(5,'2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-05','HANH_CHINH','2026-01-05 08:25','2026-01-05 17:00',7.58,0.00,'di_muon','van_tay','Di muon 25 phut'),
(5,'2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-14',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Phep nam'),
(5,'2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV022 (TP IT) - tháng 1
(22,'2026-01-02','HANH_CHINH','2026-01-02 07:55','2026-01-02 18:00',8.00,1.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 19:00',8.00,2.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV023 (TL Backend) - tháng 1 co 1 ngày nghi om
(23,'2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-08',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Nghi om'),
(23,'2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(23,'2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV024 (SE Senior BE) - co lam them nhieu
(24,'2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 19:30',8.00,2.50,'dung_giờ','van_tay',NULL),
(24,'2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-06','HANH_CHINH','2026-01-06 08:00','2026-01-06 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 20:00',8.00,3.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 18:00',8.00,1.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV025 (SE Mid BE) - 1 ngày vang mat ko phep
(25,'2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-06',NULL,NULL,NULL,0.00,0.00,'vang_mat','thu_cong','Vang mat khong ly do'),
(25,'2026-01-07','HANH_CHINH','2026-01-07 08:00','2026-01-07 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(25,'2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV015 (TP KD) - tháng 1, cong tac 2 ngày
(15,'2026-01-02','HANH_CHINH','2026-01-02 08:00','2026-01-02 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-05','HANH_CHINH','2026-01-05 08:00','2026-01-05 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-06',NULL,NULL,NULL,8.00,0.00,'cong_tac','thu_cong','Cong tac Ha Noi'),
(15,'2026-01-07',NULL,NULL,NULL,8.00,0.00,'cong_tac','thu_cong','Cong tac Ha Noi'),
(15,'2026-01-08','HANH_CHINH','2026-01-08 08:00','2026-01-08 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-09','HANH_CHINH','2026-01-09 08:00','2026-01-09 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-12','HANH_CHINH','2026-01-12 08:00','2026-01-12 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-13','HANH_CHINH','2026-01-13 08:00','2026-01-13 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-14','HANH_CHINH','2026-01-14 08:00','2026-01-14 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-15','HANH_CHINH','2026-01-15 08:00','2026-01-15 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-16','HANH_CHINH','2026-01-16 08:00','2026-01-16 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-19','HANH_CHINH','2026-01-19 08:00','2026-01-19 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-20','HANH_CHINH','2026-01-20 08:00','2026-01-20 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-21','HANH_CHINH','2026-01-21 08:00','2026-01-21 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-22','HANH_CHINH','2026-01-22 08:00','2026-01-22 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-23','HANH_CHINH','2026-01-23 08:00','2026-01-23 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-26','HANH_CHINH','2026-01-26 08:00','2026-01-26 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-27','HANH_CHINH','2026-01-27 08:00','2026-01-27 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-28','HANH_CHINH','2026-01-28 08:00','2026-01-28 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-29','HANH_CHINH','2026-01-29 08:00','2026-01-29 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-01-30','HANH_CHINH','2026-01-30 08:00','2026-01-30 17:00',8.00,0.00,'dung_giờ','gps',NULL);

-- *** CHAM CONG THANG 2/2026 (Thang 2 ngày lam viec: 2,3,4,5,6,9,10,11,12,13,16,17,18,19,20,23,24,25,26,27) ***
INSERT INTO CHAMCONG (maNV, ngay, maCaLam, gioVao, gioRa, soGioLam, gioLamThem, trangThai, phuongThucChamCong, ghiChu) VALUES
-- NV001 tháng 2
(1,'2026-02-02','HANH_CHINH','2026-02-02 08:00','2026-02-02 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-03','HANH_CHINH','2026-02-03 07:55','2026-02-03 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-04','HANH_CHINH','2026-02-04 08:25','2026-02-04 17:00',7.58,0.00,'di_muon','the_tu','Di muon 25 phut'),
(1,'2026-02-05','HANH_CHINH','2026-02-05 08:00','2026-02-05 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-06','HANH_CHINH','2026-02-06 08:00','2026-02-06 19:00',8.00,2.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-09','HANH_CHINH','2026-02-09 08:00','2026-02-09 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-10','HANH_CHINH','2026-02-10 08:00','2026-02-10 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-11','HANH_CHINH','2026-02-11 08:00','2026-02-11 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-12','HANH_CHINH','2026-02-12 08:00','2026-02-12 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-13','HANH_CHINH','2026-02-13 08:00','2026-02-13 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-16','HANH_CHINH','2026-02-16 08:00','2026-02-16 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-17','HANH_CHINH','2026-02-17 08:00','2026-02-17 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-18','HANH_CHINH','2026-02-18 08:00','2026-02-18 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-19','HANH_CHINH','2026-02-19 08:35','2026-02-19 17:00',7.42,0.00,'di_muon','the_tu','Di muon 35 phut'),
(1,'2026-02-20','HANH_CHINH','2026-02-20 08:00','2026-02-20 19:30',8.00,2.50,'dung_giờ','the_tu',NULL),
(1,'2026-02-23','HANH_CHINH','2026-02-23 08:00','2026-02-23 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-24','HANH_CHINH','2026-02-24 08:00','2026-02-24 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-25','HANH_CHINH','2026-02-25 08:00','2026-02-25 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-26','HANH_CHINH','2026-02-26 08:00','2026-02-26 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
(1,'2026-02-27','HANH_CHINH','2026-02-27 08:00','2026-02-27 17:00',8.00,0.00,'dung_giờ','the_tu',NULL),
-- NV005 tháng 2 (nghi phep 1 ngày)
(5,'2026-02-02','HANH_CHINH','2026-02-02 08:00','2026-02-02 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-03','HANH_CHINH','2026-02-03 08:00','2026-02-03 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-04','HANH_CHINH','2026-02-04 08:00','2026-02-04 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-05','HANH_CHINH','2026-02-05 08:00','2026-02-05 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-06','HANH_CHINH','2026-02-06 08:00','2026-02-06 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-09','HANH_CHINH','2026-02-09 08:00','2026-02-09 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-10','HANH_CHINH','2026-02-10 08:00','2026-02-10 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-11','HANH_CHINH','2026-02-11 08:00','2026-02-11 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-12','HANH_CHINH','2026-02-12 08:00','2026-02-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-13','HANH_CHINH','2026-02-13 08:00','2026-02-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-16',NULL,NULL,NULL,0.00,0.00,'nghi_phep','thu_cong','Nghi phep nam'),
(5,'2026-02-17','HANH_CHINH','2026-02-17 08:00','2026-02-17 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-18','HANH_CHINH','2026-02-18 08:00','2026-02-18 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-19','HANH_CHINH','2026-02-19 08:00','2026-02-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-20','HANH_CHINH','2026-02-20 08:00','2026-02-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-23','HANH_CHINH','2026-02-23 08:00','2026-02-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-24','HANH_CHINH','2026-02-24 08:00','2026-02-24 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-25','HANH_CHINH','2026-02-25 08:00','2026-02-25 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-26','HANH_CHINH','2026-02-26 08:00','2026-02-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(5,'2026-02-27','HANH_CHINH','2026-02-27 08:00','2026-02-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV022 tháng 2 (lam them nhieu)
(22,'2026-02-02','HANH_CHINH','2026-02-02 08:00','2026-02-02 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-03','HANH_CHINH','2026-02-03 08:00','2026-02-03 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-04','HANH_CHINH','2026-02-04 08:00','2026-02-04 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-05','HANH_CHINH','2026-02-05 08:00','2026-02-05 19:00',8.00,2.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-06','HANH_CHINH','2026-02-06 08:00','2026-02-06 19:30',8.00,2.50,'dung_giờ','van_tay',NULL),
(22,'2026-02-09','HANH_CHINH','2026-02-09 08:00','2026-02-09 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-10','HANH_CHINH','2026-02-10 08:00','2026-02-10 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-11','HANH_CHINH','2026-02-11 08:00','2026-02-11 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-12','HANH_CHINH','2026-02-12 08:00','2026-02-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-13','HANH_CHINH','2026-02-13 08:00','2026-02-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-16','HANH_CHINH','2026-02-16 08:00','2026-02-16 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-17','HANH_CHINH','2026-02-17 08:00','2026-02-17 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-18','HANH_CHINH','2026-02-18 08:00','2026-02-18 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-19','HANH_CHINH','2026-02-19 08:00','2026-02-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-20','HANH_CHINH','2026-02-20 08:00','2026-02-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-23','HANH_CHINH','2026-02-23 08:00','2026-02-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-24','HANH_CHINH','2026-02-24 08:00','2026-02-24 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-25','HANH_CHINH','2026-02-25 08:00','2026-02-25 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-26','HANH_CHINH','2026-02-26 08:00','2026-02-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(22,'2026-02-27','HANH_CHINH','2026-02-27 08:00','2026-02-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV024 tháng 2 (lam them cao)
(24,'2026-02-02','HANH_CHINH','2026-02-02 08:00','2026-02-02 20:00',8.00,3.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-03','HANH_CHINH','2026-02-03 08:00','2026-02-03 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-04','HANH_CHINH','2026-02-04 08:00','2026-02-04 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-05','HANH_CHINH','2026-02-05 08:00','2026-02-05 19:30',8.00,2.50,'dung_giờ','van_tay',NULL),
(24,'2026-02-06','HANH_CHINH','2026-02-06 08:00','2026-02-06 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-09','HANH_CHINH','2026-02-09 08:00','2026-02-09 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-10','HANH_CHINH','2026-02-10 08:00','2026-02-10 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-11','HANH_CHINH','2026-02-11 08:00','2026-02-11 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-12','HANH_CHINH','2026-02-12 08:00','2026-02-12 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-13','HANH_CHINH','2026-02-13 08:00','2026-02-13 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-16','HANH_CHINH','2026-02-16 08:00','2026-02-16 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-17','HANH_CHINH','2026-02-17 08:00','2026-02-17 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-18','HANH_CHINH','2026-02-18 08:00','2026-02-18 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-19','HANH_CHINH','2026-02-19 08:00','2026-02-19 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-20','HANH_CHINH','2026-02-20 08:00','2026-02-20 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-23','HANH_CHINH','2026-02-23 08:00','2026-02-23 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-24','HANH_CHINH','2026-02-24 08:00','2026-02-24 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-25','HANH_CHINH','2026-02-25 08:00','2026-02-25 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-26','HANH_CHINH','2026-02-26 08:00','2026-02-26 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
(24,'2026-02-27','HANH_CHINH','2026-02-27 08:00','2026-02-27 17:00',8.00,0.00,'dung_giờ','van_tay',NULL),
-- NV015 tháng 2 (truong phong KD - di muon 1 lan)
(15,'2026-02-02','HANH_CHINH','2026-02-02 08:00','2026-02-02 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-03','HANH_CHINH','2026-02-03 08:40','2026-02-03 17:00',7.33,0.00,'di_muon','gps','Di muon 40 phut - ket xe'),
(15,'2026-02-04','HANH_CHINH','2026-02-04 08:00','2026-02-04 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-05','HANH_CHINH','2026-02-05 08:00','2026-02-05 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-06','HANH_CHINH','2026-02-06 08:00','2026-02-06 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-09','HANH_CHINH','2026-02-09 08:00','2026-02-09 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-10','HANH_CHINH','2026-02-10 08:00','2026-02-10 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-11','HANH_CHINH','2026-02-11 08:00','2026-02-11 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-12',NULL,NULL,NULL,8.00,0.00,'cong_tac','thu_cong','Cong tac gap khach Da Nang'),
(15,'2026-02-13',NULL,NULL,NULL,8.00,0.00,'cong_tac','thu_cong','Cong tac gap khach Da Nang'),
(15,'2026-02-16','HANH_CHINH','2026-02-16 08:00','2026-02-16 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-17','HANH_CHINH','2026-02-17 08:00','2026-02-17 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-18','HANH_CHINH','2026-02-18 08:00','2026-02-18 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-19','HANH_CHINH','2026-02-19 08:00','2026-02-19 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-20','HANH_CHINH','2026-02-20 08:00','2026-02-20 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-23','HANH_CHINH','2026-02-23 08:00','2026-02-23 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-24','HANH_CHINH','2026-02-24 08:00','2026-02-24 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-25','HANH_CHINH','2026-02-25 08:00','2026-02-25 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-26','HANH_CHINH','2026-02-26 08:00','2026-02-26 17:00',8.00,0.00,'dung_giờ','gps',NULL),
(15,'2026-02-27','HANH_CHINH','2026-02-27 08:00','2026-02-27 17:00',8.00,0.00,'dung_giờ','gps',NULL);

-- =====================================================
-- 10. DANG KY LAM THEM
-- =====================================================
INSERT INTO DANGKY_LAMTHEM (maNV, ngay, soGio, heSoOT, lyDo, nguoiDuyet, ngayDuyet, trangThai) VALUES
-- Thang 1
(1,  '2026-01-02', 0.50, 1.50, 'Chuan bi bao cao nhân sự dau nam',               2,  '2026-01-02 17:30:00', 'da_duyet'),
(1,  '2026-01-07', 1.00, 1.50, 'Hop tong ket nam 2025 ngoai giờ',                2,  '2026-01-07 16:00:00', 'da_duyet'),
(22, '2026-01-02', 1.00, 1.50, 'Deploy hotfix he thong production',               4,  '2026-01-02 16:00:00', 'da_duyet'),
(22, '2026-01-07', 2.00, 1.50, 'Sprint planning va kien truc he thong mới',       4,  '2026-01-06 17:00:00', 'da_duyet'),
(24, '2026-01-02', 2.50, 1.50, 'Xu ly bug nghiem trong quan ly don hang',         23, '2026-01-02 15:00:00', 'da_duyet'),
(24, '2026-01-07', 3.00, 1.50, 'Hoan thanh module API payment trước deadline',    23, '2026-01-06 17:00:00', 'da_duyet'),
(24, '2026-01-09', 1.00, 1.50, 'Code review va merge nhieu PR',                   23, '2026-01-08 17:00:00', 'da_duyet'),
-- Thang 2
(22, '2026-02-05', 2.00, 1.50, 'Setup mới truong staging cho sprint mới',         4,  '2026-02-04 17:00:00', 'da_duyet'),
(22, '2026-02-06', 2.50, 1.50, 'Demo sản phẩm cho khach hang',                    4,  '2026-02-05 17:00:00', 'da_duyet'),
(24, '2026-02-02', 3.00, 1.50, 'Refactor module thanh toan, viết unit test',      23, '2026-02-01 17:00:00', 'da_duyet'),
(24, '2026-02-05', 2.50, 1.50, 'Hoan thanh API tich hop cong thanh toan mới',    23, '2026-02-04 16:00:00', 'da_duyet'),
(1,  '2026-02-06', 2.00, 1.50, 'Xu ly ho so nhan vien mới tháng 2',               2,  '2026-02-05 16:00:00', 'da_duyet'),
(1,  '2026-02-20', 2.50, 1.50, 'Bao cao tong hop nhân sự Quy 1',                  2,  '2026-02-19 17:00:00', 'da_duyet'),
(17, '2026-02-28', 2.00, 1.50, 'Gap khach hang ngoai giờ ky hop dong mới',        15, NULL,                   'cho_duyet'),
(25, '2026-02-28', 3.00, 1.50, 'Xu ly bug release 2.1.0 gap',                     23, NULL,                   'cho_duyet'),
(33, '2026-02-27', 2.00, 1.50, 'Chay chien dich quang cao cuoi tháng',            2,  NULL,                   'cho_duyet'),
(19, '2026-02-14', 1.50, 2.00, 'Ho tro event ra mat sản phẩm cuoi tuan',           15, '2026-02-13 17:00:00', 'da_duyet');

-- =====================================================
-- 10.5. LOAI PHEP
-- =====================================================
INSERT INTO LOAIPHEP (maLoaiPhep, tenLoaiPhep, coLuong, canChungTu, soNgayToiDa, moTa, trangThai) VALUES
('PHEP_NAM',        'Nghi phep nam',    TRUE,  FALSE,  12,  'Phep nam theo luat lao dong',       'hoatDong'),
('PHEP_OM',         'Nghi om',          TRUE,  TRUE,   30,  'Nghi om co giay benh vien',         'hoatDong'),
('PHEP_CUOI',       'Nghi cuoi',        TRUE,  TRUE,   3,   'Nghi cuoi ban than hoac con',       'hoatDong'),
('PHEP_TANG',       'Nghi tang',        TRUE,  TRUE,   3,   'Nghi tang cha me vo chong con',     'hoatDong'),
('PHEP_THAI_SAN',   'Nghi thai san',    TRUE,  TRUE,   180, 'Nghi thai san theo luat BHXH',      'hoatDong'),
('PHEP_KHONG_LUONG','Nghi khong luong', FALSE, FALSE,  0,   'Nghi phep khong huong luong',       'hoatDong');

-- =====================================================
-- 11. SO DUNG PHEP
-- =====================================================
INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) VALUES
-- Nam 2025
(1,  2025, 'PHEP_NAM',        14, 10), (1,  2025, 'PHEP_OM',          0, 1),
(2,  2025, 'PHEP_NAM',        14,  8),
(3,  2025, 'PHEP_NAM',        12,  5),
(4,  2025, 'PHEP_NAM',        14, 12),
(5,  2025, 'PHEP_NAM',        14,  9), (5,  2025, 'PHEP_CUOI',        3,  3),
(6,  2025, 'PHEP_NAM',        12,  6),
(7,  2025, 'PHEP_NAM',        12,  4),
(8,  2025, 'PHEP_NAM',        12,  3),
(9,  2025, 'PHEP_NAM',         3,  0),
(10, 2025, 'PHEP_NAM',        14, 11),
(11, 2025, 'PHEP_NAM',        14,  7),
(12, 2025, 'PHEP_NAM',        12,  8),
(13, 2025, 'PHEP_NAM',        12,  5),
(14, 2025, 'PHEP_NAM',        12,  2),
(15, 2025, 'PHEP_NAM',        14, 10),
(16, 2025, 'PHEP_NAM',        14,  9),
(17, 2025, 'PHEP_NAM',        12,  6),
(18, 2025, 'PHEP_NAM',        12,  4), (18, 2025, 'PHEP_OM',          0, 3),
(19, 2025, 'PHEP_NAM',        12,  7),
(20, 2025, 'PHEP_NAM',        12,  5),
(21, 2025, 'PHEP_NAM',         3,  0),
(22, 2025, 'PHEP_NAM',        14, 12),
(23, 2025, 'PHEP_NAM',        14,  8),
(24, 2025, 'PHEP_NAM',        12,  5),
(25, 2025, 'PHEP_NAM',        12,  6),
(26, 2025, 'PHEP_NAM',        12,  3),
(27, 2025, 'PHEP_NAM',        14,  9),
(28, 2025, 'PHEP_NAM',        12,  7),
(29, 2025, 'PHEP_NAM',        12,  4),
(30, 2025, 'PHEP_NAM',         3,  0),
(31, 2025, 'PHEP_NAM',        14,  6),
(32, 2025, 'PHEP_NAM',        12,  5),
(33, 2025, 'PHEP_NAM',        14,  8),
(34, 2025, 'PHEP_NAM',        12,  6),
(35, 2025, 'PHEP_NAM',        12, 12), (35, 2025, 'PHEP_THAI_SAN',  180, 90),
-- Nam 2026
(1,  2026, 'PHEP_NAM',        14,  2),
(2,  2026, 'PHEP_NAM',        14,  1),
(3,  2026, 'PHEP_NAM',        12,  0),
(4,  2026, 'PHEP_NAM',        14,  0),
(5,  2026, 'PHEP_NAM',        14,  2),
(6,  2026, 'PHEP_NAM',        12,  1),
(7,  2026, 'PHEP_NAM',        12,  0),
(8,  2026, 'PHEP_NAM',        12,  0),
(9,  2026, 'PHEP_NAM',         3,  0),
(10, 2026, 'PHEP_NAM',        14,  0),
(11, 2026, 'PHEP_NAM',        14,  1),
(12, 2026, 'PHEP_NAM',        12,  0),
(13, 2026, 'PHEP_NAM',        12,  0),
(14, 2026, 'PHEP_NAM',        12,  0),
(15, 2026, 'PHEP_NAM',        14,  0),
(16, 2026, 'PHEP_NAM',        14,  1),
(17, 2026, 'PHEP_NAM',        12,  0),
(18, 2026, 'PHEP_NAM',        12,  0),
(19, 2026, 'PHEP_NAM',        12,  0),
(20, 2026, 'PHEP_NAM',        12,  0),
(21, 2026, 'PHEP_NAM',         3,  0),
(22, 2026, 'PHEP_NAM',        14,  0),
(23, 2026, 'PHEP_NAM',        14,  1),
(24, 2026, 'PHEP_NAM',        12,  0),
(25, 2026, 'PHEP_NAM',        12,  0),
(26, 2026, 'PHEP_NAM',        12,  0),
(27, 2026, 'PHEP_NAM',        14,  0),
(28, 2026, 'PHEP_NAM',        12,  1),
(29, 2026, 'PHEP_NAM',        12,  0),
(30, 2026, 'PHEP_NAM',         3,  0),
(31, 2026, 'PHEP_NAM',        14,  0),
(32, 2026, 'PHEP_NAM',        12,  0),
(33, 2026, 'PHEP_NAM',        14,  1),
(34, 2026, 'PHEP_NAM',        12,  0),
(35, 2026, 'PHEP_THAI_SAN',  180, 90);

-- =====================================================
-- 12. DON XIN NGHI PHEP
-- =====================================================
INSERT INTO DONXINNGHIPHEP (maNV, maLoaiPhep, tuNgay, denNgay, soNgayNghi, lyDo, fileDinhKem, nguoiDuyet, ngayDuyet, lyDoTuChoi, trangThai) VALUES
-- Da duyet 2025
(5,  'PHEP_CUOI',      '2025-05-15', '2025-05-17', 3.0, 'Dam cuoi cua em gai',             NULL, 5,  '2025-05-10 09:00:00', NULL,  'da_duyet'),
(22, 'PHEP_NAM',       '2025-07-14', '2025-07-18', 5.0, 'Du lich nuoc ngoai gia dinh',     NULL, 4,  '2025-07-10 10:00:00', NULL,  'da_duyet'),
(4,  'PHEP_NAM',       '2025-12-22', '2025-12-26', 5.0, 'Nghi Tet som cung gia dinh',      NULL, 1,  '2025-12-15 09:00:00', NULL,  'da_duyet'),
(35, 'PHEP_THAI_SAN',  '2025-11-01', '2026-04-29', 90.0,'Nghi thai san lan 1',             NULL, 5,  '2025-10-20 08:00:00', NULL,  'da_duyet'),
(16, 'PHEP_NAM',       '2025-08-04', '2025-08-08', 5.0, 'Tham than Ha Noi',                NULL, 15, '2025-07-30 10:00:00', NULL,  'da_duyet'),
(24, 'PHEP_NAM',       '2025-09-15', '2025-09-19', 5.0, 'Du lich Nha Trang',               NULL, 23, '2025-09-10 09:00:00', NULL,  'da_duyet'),
-- Da duyet 2026
(1,  'PHEP_NAM',       '2026-01-07', '2026-01-08', 2.0, 'Viec gia dinh',                   NULL, 5,  '2026-01-05 08:00:00', NULL,  'da_duyet'),
(23, 'PHEP_OM',        '2026-01-08', '2026-01-08', 1.0, 'Bi benh - cam cum',               NULL, 22, '2026-01-08 07:30:00', NULL,  'da_duyet'),
(5,  'PHEP_NAM',       '2026-01-14', '2026-01-14', 1.0, 'Kham suc khoe dinh ky',           NULL, 5,  '2026-01-13 16:00:00', NULL,  'da_duyet'),
(5,  'PHEP_NAM',       '2026-02-16', '2026-02-16', 1.0, 'Viec ca nhan',                    NULL, 5,  '2026-02-14 10:00:00', NULL,  'da_duyet'),
(33, 'PHEP_NAM',       '2026-01-26', '2026-01-27', 2.0, 'Du lich cuoi tuan dai',           NULL, 5,  '2026-01-22 09:00:00', NULL,  'da_duyet'),
(28, 'PHEP_NAM',       '2026-01-22', '2026-01-22', 1.0, 'Kham benh ca nhan',               NULL, 27, '2026-01-21 15:00:00', NULL,  'da_duyet'),
-- Dang cho duyet
(25, 'PHEP_NAM',       '2026-03-10', '2026-03-13', 4.0, 'Du lich Hoi An gia dinh',         NULL, null, NULL,                  NULL,  'cho_duyet'),
(18, 'PHEP_NAM',       '2026-03-05', '2026-03-06', 2.0, 'Viec gia dinh quan trong',        NULL, null, NULL,                  NULL,  'cho_duyet'),
(29, 'PHEP_NAM',       '2026-04-02', '2026-04-03', 2.0, 'Gio to Hung Vuong + viec nha',    NULL, null, NULL,                  NULL,  'cho_duyet'),
(13, 'PHEP_NAM',       '2026-03-20', '2026-03-20', 1.0, 'Hop dong khoa so tieu hoc chau', NULL, null, NULL,                  NULL,  'cho_duyet'),
-- Tu choi
(26, 'PHEP_NAM',       '2026-02-10', '2026-02-13', 4.0, 'Du lich Phu Quoc',                NULL, 23, '2026-02-08 11:00:00', 'Thoi diem gay khi nhieu viec, hoan lai sau', 'tu_choi'),
-- Huy  
(20, 'PHEP_NAM',       '2026-01-20', '2026-01-21', 2.0, 'Ke hoach nghi',                   NULL, 15, NULL,                  NULL,  'huy');

-- =====================================================
-- 13. BANG LUONG + CHI TIET LUONG + THANH PHAN LUONG
-- =====================================================
INSERT INTO BANGLUONG (thang, nam, tenBangLuong, nguoiTao, nguoiDuyet, ngayDuyet, trangThai) VALUES
(11, 2025, 'Bảng lương tháng 11-2025', 5, 2, '2025-12-03 10:00:00', 'da_khoa'),
(12, 2025, 'Bảng lương tháng 12-2025', 5, 2, '2026-01-05 10:00:00', 'da_khoa'),
( 1, 2026, 'Bảng lương tháng 01-2026', 5, 2, '2026-02-05 09:00:00', 'da_khoa'),
( 2, 2026, 'Bảng lương tháng 02-2026', 5, NULL, NULL,               'dang_xu_ly');

-- Bảng lương tháng 1/2026 (maBangLuong=3) - day du 34 NV (bo NV035 dang nghi thai san huong BHXH)
INSERT INTO CHITIETLUONG (maBangLuong, maNV, luongCoSo, tongLuongChucVu, luongLamThem, tongThuNhap, tongKhauTru, luongThucLanh, soNgayCong, soGioLamThem) VALUES
-- Lãnh đạo
(3, 1,  80000000, 15000000,  1125000, 96125000, 9612500, 86512500, 21.0,  1.5),
(3, 2,  60000000, 10000000,     0,    70000000, 7000000, 63000000, 21.0,  0.0),
(3, 3,  55000000,  9000000,     0,    64000000, 6400000, 57600000, 21.0,  0.0),
(3, 4,  55000000,  9000000,     0,    64000000, 6400000, 57600000, 21.0,  0.0),
-- Phong NS
(3, 5,  22000000,  5000000,     0,    27000000, 2700000, 24300000, 20.0,  0.0), -- 1 ngày phep
(3, 6,  18000000,  3500000,     0,    21500000, 2150000, 19350000, 21.0,  0.0),
(3, 7,  13000000,   200000,     0,    13200000, 1320000, 11880000, 21.0,  0.0),
(3, 8,  12500000,   200000,     0,    12700000, 1270000, 11430000, 21.0,  0.0),
(3, 9,   8000000,      0,      0,     8000000,  800000,  7200000, 21.0,  0.0),
-- Phong KT
(3,10,  25000000,  5000000,     0,    30000000, 3000000, 27000000, 21.0,  0.0),
(3,11,  20000000,  3500000,     0,    23500000, 2350000, 21150000, 21.0,  0.0),
(3,12,  16000000,   600000,     0,    16600000, 1660000, 14940000, 21.0,  0.0),
(3,13,  13500000,   300000,     0,    13800000, 1380000, 12420000, 21.0,  0.0),
(3,14,  11000000,   300000,     0,    11300000, 1130000, 10170000, 21.0,  0.0),
-- Phong KD
(3,15,  28000000,  5000000,     0,    33000000, 3300000, 29700000, 21.0,  0.0),
(3,16,  22000000,  2000000,     0,    24000000, 2400000, 21600000, 21.0,  0.0),
(3,17,  18000000,   500000,     0,    18500000, 1850000, 16650000, 21.0,  0.0),
(3,18,  14500000,   200000,     0,    14700000, 1470000, 13230000, 21.0,  0.0),
(3,19,  17000000,   500000,  337500,  17837500, 1783750, 16053750, 21.0,  1.5),
(3,20,  12000000,   200000,     0,    12200000, 1220000, 10980000, 21.0,  0.0),
(3,21,   8000000,      0,      0,     8000000,  800000,  7200000, 21.0,  0.0),
-- Phòng IT
(3,22,  35000000,  5000000,  2250000, 42250000, 4225000, 38025000, 21.0,  3.0),
(3,23,  28000000,  2000000,     0,    30000000, 3000000, 27000000, 20.0,  0.0), -- 1 ngày om
(3,24,  28000000,  1000000,  7875000, 36875000, 3687500, 33187500, 21.0, 6.50),
(3,25,  18000000,   500000,     0,    18500000, 1850000, 16650000, 20.0,  0.0), -- 1 ngày vang
(3,26,  13000000,   200000,     0,    13200000, 1320000, 11880000, 21.0,  0.0),
(3,27,  26000000,  2000000,     0,    28000000, 2800000, 25200000, 21.0,  0.0),
(3,28,  27000000,  1000000,     0,    28000000, 2800000, 25200000, 21.0,  0.0),
(3,29,  15500000,   500000,     0,    16000000, 1600000, 14400000, 21.0,  0.0),
(3,30,   8500000,      0,      0,     8500000,  850000,  7650000, 21.0,  0.0),
(3,31,  22000000,  2000000,     0,    24000000, 2400000, 21600000, 21.0,  0.0),
(3,32,  17000000,   300000,     0,    17300000, 1730000, 15570000, 21.0,  0.0),
-- Phong MKT
(3,33,  22000000,  5000000,     0,    27000000, 2700000, 24300000, 21.0,  0.0),
(3,34,  14500000,   200000,     0,    14700000, 1470000, 13230000, 21.0,  0.0);

-- Thanh phan luong chi tiet cho mot so nhan vien tieu bieu (NV001, NV022, NV024)
INSERT INTO THANHPHANLUONG (maChiTiet, tenThanhPhan, loai, soTien, ghiChu) VALUES
-- NV001 (maChiTiet=1)
(1,'Luong co so',              'thu_nhap', 80000000, NULL),
(1,'Phu cap chuc vu GD',       'thu_nhap', 15000000, NULL),
(1,'Luong lam them (1.5h)',    'thu_nhap',  1125000, '1.5h x 180,000 OT/h'),
(1,'BHXH 8%',                  'khau_tru',  6400000, 'Tren luong co so 80tr'),
(1,'BHYT 1.5%',                'khau_tru',  1200000, NULL),
(1,'BHTN 1%',                  'khau_tru',   800000, NULL),
(1,'Thuế TNCN',                'khau_tru',  1212500, 'Tinh theo bieu thue luy tien'),
-- NV022 (maChiTiet=22 trong bang = row thu 22, maChiTiet thực tế phu thuoc AUTO_INCREMENT)
-- Dung subquery de tranh hard-code maChiTiet
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=22),'Luong co so',       'thu_nhap', 35000000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=22),'Phu cap chuc vu TP','thu_nhap',  5000000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=22),'Lam them 3h OT',   'thu_nhap',  2250000, '3h x 150,000'),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=22),'BHXH 8%',           'khau_tru',  2800000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=22),'BHYT 1.5%',         'khau_tru',   525000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=22),'BHTN 1%',           'khau_tru',   350000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=22),'Thuế TNCN',         'khau_tru',   550000, NULL),
-- NV024 (SE Senior, lam them nhieu)
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=24),'Luong co so',       'thu_nhap', 28000000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=24),'Phu cap chuyen mon','thu_nhap',  1000000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=24),'Lam them 6.5h OT', 'thu_nhap',  7875000, '6.5h x 150,000 x 1.5'),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=24),'BHXH 8%',           'khau_tru',  2240000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=24),'BHYT 1.5%',         'khau_tru',   420000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=24),'BHTN 1%',           'khau_tru',   280000, NULL),
((SELECT maChiTiet FROM CHITIETLUONG WHERE maBangLuong=3 AND maNV=24),'Thuế TNCN',         'khau_tru',   747500, NULL);

-- =====================================================
-- 14. DOT DANH GIA & TIEU CHI
-- =====================================================
-- Tieu chi danh gia (them 1 tieu chi mới: Tuan thu noi quy)
INSERT INTO TIEUCHIDANHGIA (tenTieuChi, moTa, nhomTieuChi, diemToiDa, trangThai) VALUES
('Chat luong cong viec',    'Chat luong dau ra, sản phẩm, dich vu cung cap',       'Ket qua',  10, 'hoatDong'),
('Tien do hoan thanh',      'Hoan thanh dung han, khong tre deadline',              'Ket qua',  10, 'hoatDong'),
('Kha nang sáng tao',       'De xuat giai phap, cai tien quy trinh',               'Nang luc', 10, 'hoatDong'),
('Kỹ năng chuyen mon',      'Trinh do chuyen mon, kỹ năng ky thuat',               'Nang luc', 10, 'hoatDong'),
('Lam viec nhom',           'Phoi hop, ho tro dong nghiep, tinh than team',        'Thai do',  10, 'hoatDong'),
('Tuan thu noi quy',        'Chap hanh quy che, di lam dung giờ, tac phong',       'Thai do',  10, 'hoatDong'),
('Phát triển ban than',     'Hoc hoi kỹ năng mới, nang cao trinh do',              'Nang luc', 10, 'hoatDong');

-- Dot danh gia
INSERT INTO DOTDANHGIA (tenDot, nam, kyDanhGia, tuNgay, denNgay, moTa, trangThai) VALUES
('Danh gia Quy 4 nam 2024',  2024, 'quy_4', '2025-01-06', '2025-01-17', 'Danh gia hieu suat Quy 4/2024',     'da_ket_thuc'),
('Danh gia Quy 1 nam 2025',  2025, 'quy_1', '2025-04-07', '2025-04-18', 'Danh gia hieu suat Quy 1/2025',     'da_ket_thuc'),
('Danh gia Quy 2 nam 2025',  2025, 'quy_2', '2025-07-07', '2025-07-18', 'Danh gia hieu suat Quy 2/2025',     'da_ket_thuc'),
('Danh gia Quy 3 nam 2025',  2025, 'quy_3', '2025-10-06', '2025-10-17', 'Danh gia hieu suat Quy 3/2025',     'da_ket_thuc'),
('Danh gia Nam 2025',        2025, 'nam',   '2026-01-12', '2026-01-23', 'Danh gia tong ket nam 2025',         'da_ket_thuc'),
('Danh gia Quy 1 nam 2026',  2026, 'quy_1', '2026-04-06', '2026-04-17', 'Danh gia hieu suat Quy 1/2026',     'chua_bat_dau');

-- Trong so tieu chi cho mới dot (tong trong so cua mới dot = 100%)
-- Dot 1 (Q4/2024): tap trung Ket qua va Nang luc
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(1,1,20.0,TRUE),(1,2,20.0,TRUE),(1,3,10.0,FALSE),(1,4,20.0,TRUE),(1,5,10.0,TRUE),(1,6,10.0,TRUE),(1,7,10.0,FALSE);
-- Dot 2 (Q1/2025)
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(2,1,20.0,TRUE),(2,2,20.0,TRUE),(2,3,10.0,FALSE),(2,4,20.0,TRUE),(2,5,15.0,TRUE),(2,6,10.0,TRUE),(2,7,5.0,FALSE);
-- Dot 3 (Q2/2025)
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(3,1,20.0,TRUE),(3,2,15.0,TRUE),(3,3,15.0,FALSE),(3,4,20.0,TRUE),(3,5,15.0,TRUE),(3,6,10.0,TRUE),(3,7,5.0,FALSE);
-- Dot 4 (Q3/2025)
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(4,1,20.0,TRUE),(4,2,20.0,TRUE),(4,3,10.0,FALSE),(4,4,20.0,TRUE),(4,5,15.0,TRUE),(4,6,10.0,TRUE),(4,7,5.0,FALSE);
-- Dot 5 (Nam 2025): nang cao trong so Phát triển ban than
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(5,1,17.0,TRUE),(5,2,17.0,TRUE),(5,3,13.0,FALSE),(5,4,17.0,TRUE),(5,5,13.0,TRUE),(5,6,8.0,TRUE),(5,7,15.0,FALSE);
-- Dot 6 (Q1/2026): chua bat dau
INSERT INTO DOTDANHGIA_TIEUCHI (maDot, maTieuChi, trongSo, batBuoc) VALUES
(6,1,20.0,TRUE),(6,2,20.0,TRUE),(6,3,10.0,FALSE),(6,4,20.0,TRUE),(6,5,15.0,TRUE),(6,6,10.0,TRUE),(6,7,5.0,FALSE);

-- Danh gia hieu suat dot 5 (Nam 2025) - day du nhieu nhan vien
INSERT INTO DANHGIAHIEUSUAT (maDot, maNV, nguoiDanhGia, tongDiem, xepLoai, nhanXetChung, ngayDanhGia, trangThai) VALUES
-- Dot 1 (Q4/2024)
(1,23, 22, 8.60,'tot',      'Khoa Nguyen hoan thanh tot muc tieu ky, team van hanh on dinh.',        '2025-01-10 10:00:00','da_xac_nhan'),
(1,24, 23, 8.90,'tot',      'Mai Trang duy tri chat luong ky thuat cao, dong gop lon cho dự án.',     '2025-01-10 11:00:00','da_xac_nhan'),
(1,16, 15, 8.20,'tot',      'Ngoc Bich dat KPI kinh doanh va ho tro team B2B hieu qua.',              '2025-01-13 09:00:00','da_xac_nhan'),
-- Dot 2 (Q1/2025)
(2,23, 22, 8.70,'tot',      'Khoa Nguyen tiep tuc dan dat team backend tot, giao hang dung han.',      '2025-04-11 09:30:00','da_xac_nhan'),
(2,29, 27, 7.70,'kha',      'Ngoc Duy tien bo ro net, can toi uu them hieu nang giao diện.',           '2025-04-11 10:30:00','da_xac_nhan'),
(2,34, 33, 7.90,'kha',      'Dinh Khang hoan thanh chien dich marketing dung tien do.',                '2025-04-15 14:00:00','da_xac_nhan'),
-- Dot 3 (Q2/2025)
(3,17, 16, 8.30,'tot',      'Minh Hoang dat ket qua kinh doanh on dinh, kỹ năng dam phan tot.',       '2025-07-11 10:00:00','da_xac_nhan'),
(3,31, 22, 8.10,'tot',      'Xuan Bach cai tien quy trinh QA, giam bug tai mới truong test.',         '2025-07-14 15:00:00','da_xac_nhan'),
(3,12, 10, 8.00,'tot',      'My Linh xu ly nghiep vu ke toan chinh xac, dung han.',                    '2025-07-16 09:00:00','da_xac_nhan'),
-- Dot 4 (Q3/2025)
(4,18, 16, 7.50,'kha',      'Thanh Thuy giu hieu suat on dinh, can mo rong tap khach hang mới.',       '2025-10-10 16:00:00','da_xac_nhan'),
(4,25, 23, 8.20,'tot',      'Minh Tri hoan thanh tot cac task backend, chu dong xu ly su co.',         '2025-10-14 10:00:00','da_xac_nhan'),
(4,32, 31, 7.80,'kha',      'Ngoc Bao bao cao bug ro rang, can bo sung test automation.',              '2025-10-16 14:30:00','da_xac_nhan'),
-- NV5 danh gia toan Phong NS (truong phong danh gia)
(5, 6,  5, 8.20,'tot',      'Duc Vo hoan thanh tot nhiem vu, kỹ năng quan ly ho so tot.',        '2026-01-15 09:00:00','da_xac_nhan'),
(5, 7,  5, 7.80,'kha',      'Lan Anh co gang, can phat trien them kỹ năng tuyen dung.',          '2026-01-15 10:00:00','da_xac_nhan'),
(5, 8,  5, 7.50,'kha',      'Huy Bui co tien bo, can chu dong hon trong cong viec.',             '2026-01-15 11:00:00','da_xac_nhan'),
-- NV22 danh gia Phòng IT (TP IT danh gia)
(5,23, 22, 8.80,'tot',      'Khoa Nguyen dan dat team tot, code quality cao.',                    '2026-01-16 09:00:00','da_xac_nhan'),
(5,24, 23, 9.10,'xuat_sac', 'Mai Trang - Senior xuất sắc, dong gop lon cho dự án X.',            '2026-01-16 10:00:00','da_xac_nhan'),
(5,25, 23, 8.30,'tot',      'Minh Tri phat trien nhanh, can kinh nghiem them.',                  '2026-01-16 11:00:00','da_xac_nhan'),
(5,26, 23, 7.20,'kha',      'Quynh Nhu nhan vien mới co tiem nang, can mentoring.',              '2026-01-16 14:00:00','da_xac_nhan'),
(5,27, 22, 8.60,'tot',      'Thanh Long TL Frontend tot, team hoat dong on dinh.',               '2026-01-17 09:00:00','da_xac_nhan'),
(5,28, 27, 8.90,'tot',      'Hong Van - Senior FE, chat luong UI/UX rat tot.',                   '2026-01-17 10:00:00','da_xac_nhan'),
(5,29, 27, 7.80,'kha',      'Ngoc Duy tich cuc, can cai thien frontend performance.',            '2026-01-17 11:00:00','da_xac_nhan'),
(5,31, 22, 8.50,'tot',      'Xuan Bach TL QA xay dung quy trinh kiem thu hieu qua.',            '2026-01-17 14:00:00','da_xac_nhan'),
(5,32, 31, 8.00,'tot',      'Ngoc Bao QA on dinh, doc bug nhieu, bao cao ro rang.',             '2026-01-17 15:00:00','da_xac_nhan'),
-- NV15 danh gia Phong KD (TP KD danh gia)
(5,16, 15, 8.70,'tot',      'Ngoc Bich TL B2B hoan thanh 115% chi tieu doanh so.',              '2026-01-15 14:00:00','da_xac_nhan'),
(5,17, 16, 8.40,'tot',      'Minh Hoang co hieu suat tot, kỹ năng dam phan kha.',               '2026-01-15 15:00:00','da_xac_nhan'),
(5,18, 16, 7.60,'kha',      'Thanh Thuy can tang cau KH mới, doanh so on dinh.',                '2026-01-15 16:00:00','da_xac_nhan'),
(5,19, 15, 9.00,'xuat_sac', 'Duc Manh B2C xuất sắc, doanh so vuot 130% ke hoach.',             '2026-01-16 14:00:00','da_xac_nhan'),
(5,20, 15, 7.40,'kha',      'Phuong Thao can co gang them, doanh so chua dat chi tieu.',        '2026-01-16 15:00:00','da_xac_nhan'),
-- Phong KT (TP KT danh gia)
(5,11, 10, 8.80,'tot',      'Van Liem pho phong lam viec hieu qua, ke toan chinh xac.',         '2026-01-14 09:00:00','da_xac_nhan'),
(5,12, 10, 8.20,'tot',      'My Linh KTVT on dinh, xu ly nghiep vu phuc tap tot.',              '2026-01-14 10:00:00','da_xac_nhan'),
(5,13, 10, 7.80,'kha',      'Thanh Tam can nang cao kỹ năng xu ly ho so thue.',                 '2026-01-14 11:00:00','da_xac_nhan'),
(5,14, 10, 7.30,'kha',      'Quoc Toan nhan vien mới, co gang hoc hoi nhieu.',                  '2026-01-14 14:00:00','da_xac_nhan'),
-- Phong MKT
(5,34, 33, 8.00,'tot',      'Dinh Khang sáng tao trong content, cac chien dich hieu qua.',      '2026-01-15 09:00:00','da_xac_nhan');

-- Chi tiet danh gia dot 5 cho NV024 (xuất sắc)
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=24),1, 9.5,'API quality cao, zero bug trong production'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=24),2, 9.0,'Luon hoan thanh sprint trước deadline'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=24),3, 9.0,'De xuat nhieu giai phap toi uu hoa DB'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=24),4, 9.5,'Trinh do Java/Spring Boot xuất sắc'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=24),5, 9.0,'Mentor tot cho junior, code review chat'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=24),6, 8.5,'Chap hanh tot noi quy'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=24),7, 9.0,'Tu hoc Docker, K8s, nang cao kỹ năng DevOps');

-- Chi tiet danh gia dot 5 cho NV019 (xuất sắc KD)
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet) VALUES
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=19),1, 9.0,'Doanh so 130% ke hoach, chat luong KH cao'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=19),2, 9.0,'Hoan thanh muc tieu hang tháng lien tuc'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=19),3, 9.5,'Sang tao trong chien luoc tiep can KH mới'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=19),4, 9.0,'Kỹ năng dam phan va chot hop dong tot'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=19),5, 8.5,'Ho tro dong nghiep trong team B2C hieu qua'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=19),6, 9.0,'Cham chi, tuong xuyen di cong tac dung giờ'),
((SELECT maDanhGia FROM DANHGIAHIEUSUAT WHERE maDot=5 AND maNV=19),7, 9.0,'Tu hoc phan tich du lieu kinh doanh, dung Excel/CRM');

-- Bo sung chi tiet danh gia cho tat ca ban ghi chua co du tieu chi trong dot
INSERT INTO CHITIETDANHGIA (maDanhGia, maTieuChi, diem, nhanXet)
SELECT
    dg.maDanhGia,
    dt.maTieuChi,
    ROUND(LEAST(10.0, GREATEST(6.0, dg.tongDiem + ((dt.trongSo - 15) / 25))), 1) AS diem,
    CONCAT('Bo sung du lieu chi tiet tu dong cho dot ', dg.maDot, ' - tieu chi ', dt.maTieuChi) AS nhanXet
FROM DANHGIAHIEUSUAT dg
JOIN DOTDANHGIA_TIEUCHI dt
    ON dt.maDot = dg.maDot
LEFT JOIN CHITIETDANHGIA cd
    ON cd.maDanhGia = dg.maDanhGia
   AND cd.maTieuChi = dt.maTieuChi
WHERE cd.maChiTiet IS NULL;

-- =====================================================
-- 15. TUYỂN DỤNG
-- =====================================================
INSERT INTO YEUCAUTUYENDUNG (maPhongBan, maChucVu, soLuong, lyDo, mucLuongDuKien, yeuCauKinhNghiem, yeuCauHocVan, yeuCauKhac, hanTuyenDung, nguoiDuyet, ngayDuyet, trangThai) VALUES
('TEAM_BE',  'SE_SENIOR',  1, 'Phát triển tính năng mới cho Platform V2',     '25-35 triệu',   '3+ nam Java/Spring Boot, Microservices', 'Đại học CNTT hoặc tương đương',  'Có kinh nghiệm Kubernetes, CI/CD là lợi thế', '2026-03-31', 4, '2026-01-20 10:00:00', 'da_duyet'),
('TEAM_BE',  'SE_MID',     2, 'Bổ sung team Backend cho dự án mới',           '18-24 triệu',   '2+ nam Java/Spring Boot, REST API',      'Đại học CNTT',                   'Biết Docker, PostgreSQL',                     '2026-04-15', 4, '2026-01-20 10:00:00', 'da_duyet'),
('TEAM_FE',  'SE_MID',     1, 'Tăng cường team Frontend, project scale up',   '16-22 triệu',   '2+ nam ReactJS, TypeScript',            'Đại học CNTT',                   'Có kinh nghiệm NextJS, Tailwind la plus',     '2026-04-30', 4, '2026-01-25 14:00:00', 'da_duyet'),
('TEAM_QA',  'QA_MID',     1, 'Mở rộng QA team cho sản phẩm mới',             '14-19 triệu',   '1.5+ nam Automation Testing',           'Đại học CNTT',                   'Biết Selenium, Playwright, JMeter',           '2026-05-15', 4, '2026-02-01 09:00:00', 'da_duyet'),
('TEAM_B2B', 'NV_KD',      2, 'Tăng trưởng kinh doanh B2B mang Enterprise',   '12-18 triệu + Hoa hồng', '1+ nam sales B2B, IT solution', 'Đại học Kinh tế, Quản trị',       'Co mạng lưới quan hệ doanh nghiệp là lợi thế','2026-05-31', 3, '2026-02-05 10:00:00', 'da_duyet'),
('PHONGMKT', 'NV_MKT',     1, 'Phát triển digital marketing cho sản phẩm SaaS','11-15 triệu',   '2+ nam Digital Marketing',             'Đại học Marketing, Truyền thông', 'Biết chay quang cao Google, Meta Ads',        '2026-04-30', 2, '2026-02-10 14:00:00', 'da_duyet'),
('PHONGNS',  'NV_NS',      1, 'Bổ sung nhan luc tuyen dung cho công ty tang truong nhanh', '10-13 triệu', '1+ nam tuyển dụng IT', 'Đại học QTKD hoac QTNL',        'Có kinh nghiệm tuyển dụng IT là ưu tiên',     '2026-06-30', 2, NULL,                 'cho_duyet');

INSERT INTO TINTUYENDUNG (maYeuCau, tieuDe, noiDung, mucLuong, diaDiem, hanNopHoSo, trangThai, soLuotXem) VALUES
(1,'Tuyển Senior Java Backend Engineer (Spring Boot / Microservices)','Tham gia phat trien Platform V2 phục vụ 100K+ user. Kỹ năng: Java 11+, Spring Boot, Microservices, Docker, Kafka, Redis.','25-35 triệu','Q1, TP.HCM (Hybrid)','2026-03-25','dang_tuyen',1250),
(2,'Tuyển 02 Java Backend Engineer (Mid Level)','Xây dựng REST API, tích hợp hệ thống 3rd party, viết unit test. 2+ nam Java Spring Boot.','18-24 triệu','Q1, TP.HCM','2026-04-10','dang_tuyen',870),
(3,'Tuyển Frontend Engineer (ReactJS / TypeScript)','Phát triển giao diện web cho sản phẩm SaaS, tương tác API. Kỹ năng: React, TypeScript, NextJS.','16-22 triệu','Q1, TP.HCM (Hybrid)','2026-04-25','dang_tuyen',620),
(4,'Tuyển QA Automation Engineer','Thiết kế và triển khai automation test suite. Kỹ năng: Selenium/Playwright, API testing, CI/CD pipeline.','14-19 triệu','Q1, TP.HCM','2026-05-10','dang_tuyen',310),
(5,'Tuyển 02 Nhân viên Kinh doanh B2B (IT Solution)','Phát triển va quan ly khach hang doanh nghiep, giời thieu giai phap phan mem quan ly.','12-18 triệu + Hoa hồng hấp dẫn','TP.HCM & cả nước','2026-05-25','dang_tuyen',480),
(6,'Tuyển Chuyen vien Digital Marketing','Làm các chiến dịch quảng cáo Google, Meta, SEO/SEM cho sản phẩm SaaS B2B.','11-15 triệu','Q1, TP.HCM','2026-04-25','dang_tuyen',390);

INSERT INTO UNGVIEN (maTin, hoTen, email, dienThoai, ngaySinh, gioiTinh, diaChi, trinhDoHocVan, kinhNghiem, nguonUngTuyen, trangThai, nhanXet) VALUES
-- Tin 1 (Senior Java) - 6 ứng viên
(1,'Nguyen Van Kien',    'kien.nv@gmail.com',     '0912001001','1995-03-12','nam','Ha Noi',      'DH Bach Khoa HN - CNTT',       '5 nam Java Spring Boot, giao thuc Kafka, AWS',     'LinkedIn',   'dang_phong_van','Kỹ năng xuất sắc, phù hợp vị trí. Chau mới tu Ha Noi.'),
(1,'Tran Minh Quan',     'quan.tm@gmail.com',     '0912001002','1996-07-25','nam','TP.HCM',     'DH CNTT TP.HCM',               '4 nam Java, Microservices, Kubernetes',             'Website cty','dang_phong_van','Kinh nghiệm thực tế tot, salary yêu cầu hợp lý.'),
(1,'Le Phuong Uyen',     'uyen.lp@gmail.com',     '0912001003','1997-01-10','nu', 'Binh Duong', 'DH FPT - Software Engineering', '3 nam Backend Java, đã làm startup, scale up',     'Topdev',     'mới',          'Hồ sơ ấn tượng, kỹ năng multitasking tot.'),
(1,'Do Thi Lan',         'lan.dt@gmail.com',       '0912001004','1993-11-08','nu', 'TP.HCM',    'DH Khoa hoc TN - CNTT',        '6 nam Java Enterprise, banking domain',             'Giới thiệu', 'trung_tuyen',  'Ung vien manh nhat. De nghi offer 32 triệu.'),
(1,'Cao Quang Hieu',     'hieu.cq@gmail.com',     '0912001005','1998-05-20','nam','Da Nang',   'DH Da Nang - CNTT',             '2.5 nam Java, can nang cao them',                   'VietnamWorks','tu_choi',     'Kinh nghiệm chua du yêu cầu Senior.'),
(1,'Vu Thi Hue',         'hue.vt@gmail.com',      '0912001006','1994-09-14','nu', 'TP.HCM',    'DH Bach Khoa HCM',             '5 nam Java, co lead dự án nho',                     'ITviec',     'mới',          'Ứng viên tốt, chờ lịch phỏng vấn vòng 2.'),
-- Tin 2 (Mid Java) - 5 ứng viên
(2,'Phan Van Hoa',       'hoa.pv@gmail.com',      '0912002001','1998-04-18','nam','TP.HCM',    'DH Công nghệ - DHQG HCM',      '2.5 nam Java Spring, REST API, MySQL',              'LinkedIn',   'dang_phong_van','Kỹ năng on, phù hợp voi yêu cầu.'),
(2,'Nguyen Thi Thao',    'thao.nt@gmail.com',     '0912002002','1999-12-03','nu', 'TP.HCM',    'DH Su pham Ky thuat',          '2 nam Java, muon chuyen sáng Backend chuyen sau',   'Facebook',   'mới',          'Sáng tạo, câu hỏi phỏng vấn tốt.'),
(2,'Hoang Duc Anh',      'ducAnh.h@gmail.com',    '0912002003','1997-06-30','nam','Ha Noi',    'DH Công nghệ thong tin - DHQG','3 nam Java Backend, co kinh nghiem Docker',          'Topdev',     'trung_tuyen',  'On dinh, kỹ năng kha, co kinh nghiem Docker.'),
(2,'Trinh Quoc Thanh',   'thanh.tq@gmail.com',    '0912002004','2000-02-22','nam','Dong Nai',  'DH Lac Hong - CNTT',           '2 nam Java, muon phat trien lau dai',               'ITviec',     'mới',          'Tiềm năng, cần training thêm.'),
(2,'Bui Thi Ngoc Hien',  'ngochiEn.bt@gmail.com', '0912002005','1998-08-15','nu', 'TP.HCM',   'DH RMIT Vietnam',               '2 nam Java Spring Boot, Agile/Scrum',               'LinkedIn',   'mới',          'Giao tiep Tieng Anh tot, kỹ năng tot.'),
-- Tin 3 (Frontend React) - 4 ứng viên
(3,'Le Van Tan',         'tan.lv@gmail.com',      '0912003001','1998-09-05','nam','TP.HCM',    'DH CNTT TP.HCM',               '2.5 nam ReactJS, TypeScript, NextJS',               'LinkedIn',   'dang_phong_van','Kỹ năng frontend tot, portfolio dep.'),
(3,'Nguyen Thi Bao Tran','baotran.nt@gmail.com',  '0912003002','2000-03-18','nu', 'Tay Ninh',  'DH Công nghệ TP.HCM',          '2 nam ReactJS, co kinh nghiem NextJS',              'Website cty','mới',          'Ho so tot, trao doi luong hợp lý.'),
(3,'Pham Ngoc An',       'ngocan.pm@gmail.com',   '0912003003','1997-11-25','nam','Binh Thuan','DH FPT TPHCM',                  '3 nam Frontend, Vue va React, thich React hon',     'Topdev',     'trung_tuyen',  'Co nen ky thuat tot. De nghi offer 20 triệu.'),
(3,'Vo Thi Kim Thi',     'kimthi.vt@gmail.com',   '0912003004','1999-07-10','nu', 'TP.HCM',    'DH Nguyen Tat Thanh',          '2 nam ReactJS, chua co kinh nghiem TypeScript',     'Facebook',   'tu_choi',      'Chua co TypeScript, khong du yêu cầu hien tai.'),
-- Tin 5 (KD B2B) - 4 ứng viên
(5,'Ngo Tuan Vu',        'tuanvu.ngo@gmail.com',  '0912005001','1995-02-14','nam','TP.HCM',    'DH Kinh tế TP.HCM',             '3 nam sales B2B phan mem ERP',                      'LinkedIn',   'dang_phong_van','Mạng KH tốt, đã chốt nhiều hợp đồng lớn.'),
(5,'Dang Thi Minh Phuong','minhphuong.dt@gmail.com','0912005002','1997-08-30','nu','TP.HCM',   'DH Ngoai thuong',               '2 nam kinh doanh phan mem, tiếng Anh tốt',          'Facebook',   'mới',          'Giao tiep tot, can test kỹ năng chot deal.'),
(5,'Ha Quoc Khanh',      'khanh.hq@gmail.com',    '0912005003','1993-12-22','nam','Binh Duong','DH Kinh tế Quoc dan',           '5 nam sales Enterprise, co sach KH rieng',          'Giới thiệu', 'trung_tuyen',  'Ung vien xuất sắc. Salary yêu cầu 18tr + 5% hoa hong.'),
(5,'Ly Thi Thanh Huong', 'thanhHuong.lt@gmail.com','0912005004','1999-05-08','nu','Long An',   'DH Thuong mai TP.HCM',          '1 nam sales, mới vao nghe nhung nhiet tinh',        'VietnamWorks','mới',          'Cam kết tốt, cần thời gian đào tạo ban đầu.');

-- =====================================================
-- 16. THÔNG BÁO NỘI BỘ
-- =====================================================
INSERT INTO THONGBAO (tieuDe, noiDung, loaiThongBao, maTaiKhoanGui, maTaiKhoanNhan, daDoc, ngayDoc) VALUES
-- Thông báo chung tu Admin/BGD
('Chúc mừng năm mới 2026!',                'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng, hạnh phúc và nhiều thành công mới!', 'thong_bao_chung', 2, 1, TRUE, '2026-01-02 09:00:00'),
('Chúc mừng năm mới 2026!',                'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng, hạnh phúc và nhiều thành công mới!', 'thong_bao_chung', 2, 5, TRUE, '2026-01-02 09:30:00'),
('Chúc mừng năm mới 2026!',                'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng, hạnh phúc và nhiều thành công mới!', 'thong_bao_chung', 2,22, TRUE, '2026-01-02 09:15:00'),
('Chúc mừng năm mới 2026!',                'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng, hạnh phúc và nhiều thành công mới!', 'thong_bao_chung', 2,15, TRUE, '2026-01-02 08:55:00'),
('Chúc mừng năm mới 2026!',                'BGD Công ty TNHH ABC Technology chân thành chúc toàn thể CBNV một năm mới 2026 an khang thịnh vượng, hạnh phúc và nhiều thành công mới!', 'thong_bao_chung', 2,33, FALSE, NULL),
-- Thông báo noi quy, quy dinh
('Nhắc nhở: Chính sách làm việc từ xa Q1/2026','Lịch Work-From-Home tháng 1-3/2026: Thu 4 hàng tuần la ngày WFH. Vui lòng đăng ký WFH qua hệ thống trước 8h sáng.','thong_bao_chung', 1, 5, TRUE, '2026-01-05 10:00:00'),
('Nhắc nhở: Chính sách làm việc từ xa Q1/2026','Lịch Work-From-Home tháng 1-3/2026: Thu 4 hàng tuần la ngày WFH. Vui lòng đăng ký WFH qua hệ thống trước 8h sáng.','thong_bao_chung', 1,22, TRUE, '2026-01-05 09:45:00'),
('Nhắc nhở: Chính sách làm việc từ xa Q1/2026','Lịch Work-From-Home tháng 1-3/2026: Thu 4 hàng tuần la ngày WFH. Vui lòng đăng ký WFH qua hệ thống trước 8h sáng.','thong_bao_chung', 1,15, FALSE, NULL),
-- Thông báo luong
('Bảng lương tháng 11/2025 da san sáng',   'BL tháng 11/2025 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6, 1, TRUE, '2025-12-04 09:00:00'),
('Bảng lương tháng 11/2025 da san sáng',   'BL tháng 11/2025 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6,22, TRUE, '2025-12-04 10:00:00'),
('Bảng lương tháng 12/2025 da san sáng',   'BL tháng 12/2025 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6, 1, TRUE, '2026-01-06 09:00:00'),
('Bảng lương tháng 12/2025 da san sáng',   'BL tháng 12/2025 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6, 5, TRUE, '2026-01-06 09:30:00'),
('Bảng lương tháng 12/2025 da san sáng',   'BL tháng 12/2025 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6,22, TRUE, '2026-01-06 10:00:00'),
('Bảng lương tháng 01/2026 da san sáng',   'BL tháng 01/2026 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6, 1, FALSE, NULL),
('Bảng lương tháng 01/2026 da san sáng',   'BL tháng 01/2026 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6,22, FALSE, NULL),
('Bảng lương tháng 01/2026 da san sáng',   'BL tháng 01/2026 đã được phê duyệt. Vui lòng đăng nhập he thong để kiểm tra chi tiết.', 'he_thong', 6,15, FALSE, NULL),
-- Thông báo don tu
('Đơn nghỉ phép đã được phê duyệt',        'Đơn nghỉ phép tu 14/01/2026 của bạn đã được phê duyệt. Chúc bạn nghỉ vui!',               'don_tu',   6,  8, TRUE, '2026-01-13 17:00:00'),
('Đơn nghỉ phép đã được phê duyệt',        'Đơn nghỉ phép tu 22/01/2026 của bạn đã được phê duyệt.',                                    'don_tu',   6, 29, TRUE, '2026-01-21 16:00:00'),
('Có đơn nghỉ phép mới cần duyệt',         'NV Le Thi Quynh Nhu (NV026) gửi đơn nghỉ phép 10-13/02/2026. Vui lòng xem xét phê duyệt.', 'don_tu',  27, 23, FALSE, NULL),
('Yeu cau lam them đã được phê duyệt',     'Yeu cau lam them ngày 02/02/2026 (3 giờ) đã được phê duyệt. Cảm ơn bạn đã đóng góp!',      'don_tu',  23, 25, FALSE, NULL),
-- Thông báo danh gia
('Lịch đánh giá Quý 1/2026 sắp diễn ra',   'Đợt đánh giá hiệu suất Q1/2026 sẽ bắt đầu từ 06/04/2026. Mời CBQL chuẩn bị hồ sơ và tiêu chí đánh giá cho NV.', 'thong_bao_chung', 6, 5, FALSE, NULL),
('Lịch đánh giá Quý 1/2026 sắp diễn ra',   'Đợt đánh giá hiệu suất Q1/2026 sẽ bắt đầu từ 06/04/2026. Mời CBQL chuẩn bị hồ sơ và tiêu chí đánh giá cho NV.', 'thong_bao_chung', 6,22, FALSE, NULL),
('Lịch đánh giá Quý 1/2026 sắp diễn ra',   'Đợt đánh giá hiệu suất Q1/2026 sẽ bắt đầu từ 06/04/2026. Mời CBQL chuẩn bị hồ sơ và tiêu chí đánh giá cho NV.', 'thong_bao_chung', 6,15, FALSE, NULL),
-- Thông báo tuyen dung
('Yêu cầu tuyển dụng mới cần phê duyệt',   'Có yêu cầu tuyển 1 NV NS mới tu Phong NS. Vui lòng xem xet va phe duyet.', 'don_tu', 6, 2, FALSE, NULL),
('Họp kết quả phỏng vấn Senior Java',      'Tuần tới (09-13/02/2026) sẽ phỏng vấn vòng cuối 2 ứng viên Senior Java Backend. Mời anh/chị TP IT và HR sắp xếp tham dự.', 'thong_bao_chung', 6, 1, FALSE, NULL),
('Họp kết quả phỏng vấn Senior Java',      'Tuần tới (09-13/02/2026) sẽ phỏng vấn vòng cuối 2 ứng viên Senior Java Backend. Mời anh/chị TP IT và HR sắp xếp tham dự.', 'thong_bao_chung', 6,23, FALSE, NULL);

-- =====================================================
-- 17. CẤU HÌNH PHỤ CẤP (giu & bo sung)
-- =====================================================
DELETE FROM CAUHINH_PHUCAP;
INSERT INTO CAUHINH_PHUCAP (loai, tenKhoan, kieuTinh, giaTri, nguon, hoatDong) VALUES
('phu_cap', 'Phụ cấp ăn trưa',              'co_dinh',   750000, 'CongTy',   1),
('phu_cap', 'Phụ cấp điện thoại',           'co_dinh',   500000, 'CongTy',   1),
('phu_cap', 'Phụ cấp đi lại',               'co_dinh',   600000, 'CongTy',   1),
('phu_cap', 'Phụ cấp thâm niên (3-5 nam)',  'co_dinh',   500000, 'CongTy',   1),
('phu_cap', 'Phụ cấp thâm niên (5+ nam)',   'co_dinh',  1000000, 'CongTy',   1),
('phu_cap', 'Thuong hieu qua hang tháng',   'phan_tram',   5.00, 'CongTy',   1),
('khau_tru','BHXH NLD (8%)',                'phan_tram',   8.00, 'LuatDinh', 1),
('khau_tru','BHYT NLD (1.5%)',              'phan_tram',   1.50, 'LuatDinh', 1),
('khau_tru','BHTN NLD (1%)',                'phan_tram',   1.00, 'LuatDinh', 1),
('khau_tru','Thuế TNCN',                    'phan_tram',   0.00, 'LuatDinh', 1); -- Tính theo biểu lũy tiến

-- =====================================================
-- 18. LOG AUDIT - Lịch sử thao tac he thong
-- =====================================================
INSERT INTO LOG_AUDIT (maTaiKhoan, hanhDong, bangDuLieu, maBanGhi, diaChiIP, userAgent) VALUES
(2,'LOGIN',  NULL,               NULL,  '192.168.1.10', 'Mozilla/5.0 Chrome/120'),
(2,'CREATE', 'YEUCAUTUYENDUNG',  '1',   '192.168.1.10', 'Mozilla/5.0 Chrome/120'),
(2,'APPROVE','YEUCAUTUYENDUNG',  '1',   '192.168.1.10', 'Mozilla/5.0 Chrome/120'),
(6,'LOGIN',  NULL,               NULL,  '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(6,'CREATE', 'BANGLUONG',        '3',   '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(6,'UPDATE', 'BANGLUONG',        '3',   '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(6,'CREATE', 'CHITIETLUONG',     '1',   '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(6,'UPDATE', 'DONXINNGHIPHEP',   '7',   '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(6,'UPDATE', 'DONXINNGHIPHEP',   '8',   '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(23,'LOGIN', NULL,               NULL,  '192.168.1.30', 'Mozilla/5.0 Firefox/121'),
(23,'UPDATE','DANHGIAHIEUSUAT',  '5',   '192.168.1.30', 'Mozilla/5.0 Firefox/121'),
(23,'UPDATE','DANHGIAHIEUSUAT',  '6',   '192.168.1.30', 'Mozilla/5.0 Firefox/121'),
(23,'APPROVE','DANGKY_LAMTHEM',  '10',  '192.168.1.30', 'Mozilla/5.0 Firefox/121'),
(15,'LOGIN', NULL,               NULL,  '192.168.1.40', 'Mozilla/5.0 Safari/17'),
(15,'APPROVE','DONXINNGHIPHEP',  '16',  '192.168.1.40', 'Mozilla/5.0 Safari/17'),
(22,'LOGIN', NULL,               NULL,  '192.168.1.31', 'Mozilla/5.0 Chrome/120'),
(22,'CREATE','DOTDANHGIA',       '6',   '192.168.1.31', 'Mozilla/5.0 Chrome/120'),
(22,'APPROVE','DANGKY_LAMTHEM',  '8',   '192.168.1.31', 'Mozilla/5.0 Chrome/120'),
(4,'LOGIN',  NULL,               NULL,  '10.0.0.2',     'Mobile Safari iOS/17'),
(4,'APPROVE','DANGKY_LAMTHEM',   '1',   '10.0.0.2',     'Mobile Safari iOS/17'),
(4,'APPROVE','DANGKY_LAMTHEM',   '2',   '10.0.0.2',     'Mobile Safari iOS/17'),
(4,'APPROVE','DANGKY_LAMTHEM',   '3',   '10.0.0.2',     'Mobile Safari iOS/17'),
(1,'LOGIN',  NULL,               NULL,  '192.168.1.10', 'Mozilla/5.0 Chrome/120'),
(1,'UPDATE', 'NHANVIEN',         '9',   '192.168.1.10', 'Mozilla/5.0 Chrome/120'),
(5,'LOGIN',  NULL,               NULL,  '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(5,'CREATE', 'NHANVIEN',         '35',  '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(5,'UPDATE', 'DONXINNGHIPHEP',   '9',   '192.168.1.20', 'Mozilla/5.0 Chrome/120'),
(5,'UPDATE', 'DONXINNGHIPHEP',   '10',  '192.168.1.20', 'Mozilla/5.0 Chrome/120');

SELECT '=== HRM Sample Data V2 - Inserted Successfully! ===' AS Message;
SELECT CONCAT('Tổng số nhân viên: ', COUNT(*)) AS Info FROM NHANVIEN;
SELECT CONCAT('Tổng số hợp đồng: ', COUNT(*)) AS Info FROM HOPDONGLAODONG;
SELECT CONCAT('Tổng số chấm công: ', COUNT(*)) AS Info FROM CHAMCONG;
SELECT CONCAT('Tổng số đơn nghỉ phép: ', COUNT(*)) AS Info FROM DONXINNGHIPHEP;
SELECT CONCAT('Tong so ứng viên: ', COUNT(*)) AS Info FROM UNGVIEN;








