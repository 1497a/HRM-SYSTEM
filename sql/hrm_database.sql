-- =====================================================
-- HRM DATABASE SCHEMA - MySQL (XAMPP Compatible)
-- Refactored: Vietnamese naming, snake_case columns, id PKs
-- =====================================================
-- Encoding: UTF-8 (Vietnamese support)
-- =====================================================

DROP DATABASE IF EXISTS hrm_db;
CREATE DATABASE hrm_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hrm_db;

-- =====================================================
-- NGHIEP VU 2: CO CAU TO CHUC
-- =====================================================

CREATE TABLE PHONGBAN (
    maPhongBan VARCHAR(20) PRIMARY KEY,
    tenPhongBan NVARCHAR(100) NOT NULL,
    phongBanCha VARCHAR(20) NULL,
    moTa NVARCHAR(500),
    trangThai ENUM('hoatDong', 'ngung_hoat_dong') DEFAULT 'hoatDong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (phongBanCha) REFERENCES PHONGBAN(maPhongBan) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE CHUCVU (
    maChucVu VARCHAR(20) PRIMARY KEY,
    tenChucVu NVARCHAR(100) NOT NULL,
    capBac INT NOT NULL DEFAULT 10,
    phuCapChucVu DECIMAL(15,2) DEFAULT 0,
    moTa NVARCHAR(500),
    maVaiTro VARCHAR(20) NULL,
    trangThai ENUM('hoatDong', 'ngung_hoat_dong') DEFAULT 'hoatDong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 1: QUAN LY HO SO NHAN VIEN
-- =====================================================

CREATE TABLE NHANVIEN (
    maNV VARCHAR(20) PRIMARY KEY,
    loaiHopDong ENUM('thu_viec', 'xac_dinh_thoi_han', 'khong_xac_dinh') DEFAULT 'thu_viec',
    ngayVaoLam DATE NOT NULL,
    trangThai ENUM('dang_lam_viec', 'tam_nghi', 'nghi_viec') DEFAULT 'dang_lam_viec',
    ghiChu NVARCHAR(500),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE THONGTINCANHAN (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    ngaySinh DATE,
    gioiTinh ENUM('nam', 'nu', 'khac'),
    cccd VARCHAR(12) UNIQUE,
    dienThoai VARCHAR(15),
    email VARCHAR(100),
    diaChi NVARCHAR(255),
    queQuan NVARCHAR(255),
    danToc NVARCHAR(50),
    tonGiao NVARCHAR(50),
    tinhTrangHonNhan ENUM('doc_than', 'da_ket_hon', 'ly_hon') DEFAULT 'doc_than',
    anhDaiDien VARCHAR(255),
    fileCV VARCHAR(255),
    trinhDoHocVan NVARCHAR(100),
    kinhNghiem NVARCHAR(500),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 3: BO NHIEM VA DIEU CHUYEN
-- =====================================================

CREATE TABLE BONHIEM (
    maBoNhiem INT AUTO_INCREMENT PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maPhongBan VARCHAR(20) NOT NULL,
    maChucVu VARCHAR(20) NOT NULL,
    loaiBoNhiem ENUM('chinh', 'kiem_nhiem') DEFAULT 'chinh',
    tyLeHuongLuong DECIMAL(5,2) DEFAULT 100.00,
    maQuanLy VARCHAR(20) NULL,
    nguoiDuyet VARCHAR(20) NULL,
    tuNgay DATE NOT NULL,
    denNgay DATE NULL,
    ngayPheDuyet DATETIME NULL,
    lyDo NVARCHAR(500),
    trangThai ENUM('cho_duyet', 'hieu_luc', 'het_hieu_luc', 'tu_choi') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maPhongBan) REFERENCES PHONGBAN(maPhongBan),
    FOREIGN KEY (maChucVu) REFERENCES CHUCVU(maChucVu),
    FOREIGN KEY (maQuanLy) REFERENCES NHANVIEN(maNV) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 4: CHAM CONG VA LAM THEM GIO
-- =====================================================

CREATE TABLE CALAM (
    maCaLam VARCHAR(20) PRIMARY KEY,
    tenCaLam NVARCHAR(100) NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL,
    soGioChuan DECIMAL(4,2) DEFAULT 8.00,
    choPhepLamThem BOOLEAN DEFAULT TRUE,
    moTa NVARCHAR(255),
    trangThai ENUM('hoatDong', 'ngung_hoat_dong') DEFAULT 'hoatDong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE CHAMCONG (
    maChamCong INT AUTO_INCREMENT PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    ngay DATE NOT NULL,
    maCaLam VARCHAR(20),
    gioVao DATETIME,
    gioRa DATETIME,
    soGioLam DECIMAL(4,2) DEFAULT 0,
    gioLamThem DECIMAL(4,2) DEFAULT 0,
    trangThai ENUM('dung_gio', 'di_muon', 've_som', 'vang_mat', 'nghi_phep', 'cong_tac') DEFAULT 'dung_gio',
    phuongThucChamCong ENUM('wifi', 'van_tay', 'the_tu', 'gps', 'thu_cong') DEFAULT 'thu_cong',
    ghiChu NVARCHAR(255),
    nguoiChinhSua VARCHAR(20),
    lyDoChinhSua NVARCHAR(500),
    ngayChinhSua DATETIME,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maCaLam) REFERENCES CALAM(maCaLam),
    FOREIGN KEY (nguoiChinhSua) REFERENCES NHANVIEN(maNV),
    UNIQUE KEY uk_nv_ngay (maNV, ngay)
) ENGINE=InnoDB;

CREATE TABLE DANGKY_LAMTHEM (
    maDK INT AUTO_INCREMENT PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    ngay DATE NOT NULL,
    soGio DECIMAL(4,2) NOT NULL,
    heSoOT DECIMAL(4,2) NOT NULL DEFAULT 1.50,
    lyDo NVARCHAR(500),
    nguoiDuyet VARCHAR(20),
    ngayDuyet DATETIME,
    trangThai ENUM('cho_duyet', 'da_duyet', 'tu_choi') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDuyet) REFERENCES NHANVIEN(maNV)
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 5: HOP DONG LAO DONG
-- =====================================================

CREATE TABLE HOPDONGLAODONG (
    maHopDong INT AUTO_INCREMENT PRIMARY KEY,
    soHopDong VARCHAR(50) UNIQUE NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    loaiHopDong ENUM('thu_viec', 'xac_dinh_thoi_han', 'khong_xac_dinh') NOT NULL,
    luongCoSo DECIMAL(15,2) NOT NULL,
    ngayKy DATE NOT NULL,
    ngayHieuLuc DATE NOT NULL,
    ngayHetHieuLuc DATE NULL,
    fileDinhKem VARCHAR(255),
    noiDung TEXT,
    trangThai ENUM('cho_duyet', 'hieu_luc', 'het_han', 'thanh_ly', 'huy') DEFAULT 'cho_duyet',
    nguoiTao VARCHAR(20) NULL,
    nguoiDuyet VARCHAR(20) NULL,
    ngayDuyet DATETIME NULL,
    ngayThanhLy DATE NULL,
    lyDoThanhLy VARCHAR(500) NULL,
    ghiChu VARCHAR(1000) NULL,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 6: TINH LUONG
-- =====================================================

CREATE TABLE BANGLUONG (
    maBangLuong INT AUTO_INCREMENT PRIMARY KEY,
    thang INT NOT NULL,
    nam INT NOT NULL,
    tenBangLuong NVARCHAR(100),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayKhoa DATETIME NULL,
    ngayDuyet DATETIME NULL,
    nguoiTao INT,
    nguoiKhoa INT,
    nguoiDuyet INT,
    trangThai ENUM('dang_xu_ly', 'da_duyet', 'da_khoa') DEFAULT 'dang_xu_ly',
    UNIQUE KEY uk_thang_nam (thang, nam)
) ENGINE=InnoDB;

CREATE TABLE CHITIETLUONG (
    maChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    maBangLuong INT NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    luongCoSo DECIMAL(15,2) NOT NULL,
    tongLuongChucVu DECIMAL(15,2) DEFAULT 0,
    luongLamThem DECIMAL(15,2) DEFAULT 0,
    tongThuNhap DECIMAL(15,2) DEFAULT 0,
    tongKhauTru DECIMAL(15,2) DEFAULT 0,
    luongThucLanh DECIMAL(15,2) DEFAULT 0,
    soNgayCong DECIMAL(4,1) DEFAULT 0,
    soGioLamThem DECIMAL(5,2) DEFAULT 0,
    ghiChu NVARCHAR(500),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maBangLuong) REFERENCES BANGLUONG(maBangLuong) ON DELETE CASCADE,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    UNIQUE KEY uk_bangluong_nv (maBangLuong, maNV)
) ENGINE=InnoDB;

CREATE TABLE THANHPHANLUONG (
    maThanhPhan INT AUTO_INCREMENT PRIMARY KEY,
    maChiTiet INT NOT NULL,
    tenThanhPhan NVARCHAR(100) NOT NULL,
    loai ENUM('thu_nhap', 'khau_tru') NOT NULL,
    soTien DECIMAL(15,2) NOT NULL,
    ghiChu NVARCHAR(255),
    FOREIGN KEY (maChiTiet) REFERENCES CHITIETLUONG(maChiTiet) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 7: QUAN LY NGHI PHEP
-- =====================================================

CREATE TABLE LOAIPHEP (
    maLoaiPhep VARCHAR(20) PRIMARY KEY,
    tenLoaiPhep NVARCHAR(100) NOT NULL,
    coLuong BOOLEAN DEFAULT TRUE,
    canChungTu BOOLEAN DEFAULT FALSE,
    soNgayToiDa INT DEFAULT 0,
    moTa NVARCHAR(255),
    trangThai ENUM('hoatDong', 'ngung_hoat_dong') DEFAULT 'hoatDong'
) ENGINE=InnoDB;

CREATE TABLE SODUNGPHEP (
    maSoDu INT AUTO_INCREMENT PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    nam INT NOT NULL,
    maLoaiPhep VARCHAR(20) NOT NULL,
    soNgayDuocCap DECIMAL(4,1) DEFAULT 12,
    soNgayDaDung DECIMAL(4,1) DEFAULT 0,
    soNgayConLai DECIMAL(4,1) GENERATED ALWAYS AS (soNgayDuocCap - soNgayDaDung) STORED,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maLoaiPhep) REFERENCES LOAIPHEP(maLoaiPhep),
    UNIQUE KEY uk_nv_nam_loai (maNV, nam, maLoaiPhep)
) ENGINE=InnoDB;

CREATE TABLE DONXINNGHIPHEP (
    maDon INT AUTO_INCREMENT PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maLoaiPhep VARCHAR(20) NOT NULL,
    tuNgay DATE NOT NULL,
    denNgay DATE NOT NULL,
    soNgayNghi DECIMAL(4,1) NOT NULL,
    lyDo NVARCHAR(500),
    fileDinhKem VARCHAR(255),
    nguoiDuyet VARCHAR(20),
    ngayDuyet DATETIME,
    lyDoTuChoi NVARCHAR(500),
    trangThai ENUM('cho_duyet', 'da_duyet', 'tu_choi', 'huy') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maLoaiPhep) REFERENCES LOAIPHEP(maLoaiPhep),
    FOREIGN KEY (nguoiDuyet) REFERENCES NHANVIEN(maNV)
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 8: DANH GIA HIEU SUAT
-- =====================================================

CREATE TABLE DOTDANHGIA (
    maDot INT AUTO_INCREMENT PRIMARY KEY,
    tenDot NVARCHAR(100) NOT NULL,
    nam INT NOT NULL,
    kyDanhGia ENUM('quy_1', 'quy_2', 'quy_3', 'quy_4', 'nam') NOT NULL,
    tuNgay DATE NOT NULL,
    denNgay DATE NOT NULL,
    moTa NVARCHAR(500),
    trangThai ENUM('chua_bat_dau', 'dang_dien_ra', 'da_ket_thuc') DEFAULT 'chua_bat_dau',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE TIEUCHIDANHGIA (
    maTieuChi INT AUTO_INCREMENT PRIMARY KEY,
    tenTieuChi NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(500),
    nhomTieuChi NVARCHAR(50),
    trongSo DECIMAL(5,2) DEFAULT 0,
    trangThai ENUM('hoatDong', 'ngung_hoat_dong') DEFAULT 'hoatDong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE DOTDANHGIA_TIEUCHI (
    maDot INT NOT NULL,
    maTieuChi INT NOT NULL,
    trongSo DECIMAL(5,2) DEFAULT 1.00,
    batBuoc BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (maDot, maTieuChi),
    FOREIGN KEY (maDot) REFERENCES DOTDANHGIA(maDot) ON DELETE CASCADE,
    FOREIGN KEY (maTieuChi) REFERENCES TIEUCHIDANHGIA(maTieuChi) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE DANHGIAHIEUSUAT (
    maDanhGia INT AUTO_INCREMENT PRIMARY KEY,
    maDot INT NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    nguoiDanhGia VARCHAR(20) NOT NULL,
    tongDiem DECIMAL(5,2) DEFAULT 0,
    xepLoai ENUM('xuat_sac', 'tot', 'kha', 'trung_binh', 'yeu') DEFAULT 'trung_binh',
    nhanXetChung NVARCHAR(1000),
    ngayDanhGia DATETIME,
    trangThai ENUM('chua_danh_gia', 'da_danh_gia', 'da_xac_nhan') DEFAULT 'chua_danh_gia',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maDot) REFERENCES DOTDANHGIA(maDot) ON DELETE CASCADE,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDanhGia) REFERENCES NHANVIEN(maNV),
    UNIQUE KEY uk_dot_nv (maDot, maNV)
) ENGINE=InnoDB;

CREATE TABLE CHITIETDANHGIA (
    maChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    maDanhGia INT NOT NULL,
    maTieuChi INT NOT NULL,
    diem DECIMAL(5,2) DEFAULT 0,
    nhanXet NVARCHAR(500),
    FOREIGN KEY (maDanhGia) REFERENCES DANHGIAHIEUSUAT(maDanhGia) ON DELETE CASCADE,
    FOREIGN KEY (maTieuChi) REFERENCES TIEUCHIDANHGIA(maTieuChi),
    UNIQUE KEY uk_danhgia_tieuchi (maDanhGia, maTieuChi)
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 9: PHAN QUYEN VA BAO MAT
-- =====================================================

CREATE TABLE VAITRO (
    maVaiTro VARCHAR(20) PRIMARY KEY,
    tenVaiTro NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255),
    laVaiTroHeThong BOOLEAN DEFAULT FALSE,
    trangThai ENUM('hoatDong', 'ngung_hoat_dong') DEFAULT 'hoatDong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE QUYEN (
    maQuyen VARCHAR(50) PRIMARY KEY,
    tenQuyen NVARCHAR(100) NOT NULL,
    nhomQuyen VARCHAR(50),
    moTa NVARCHAR(255),
    coPhamVi BOOLEAN DEFAULT TRUE,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE VAITRO_QUYEN (
    maVaiTro VARCHAR(20) NOT NULL,
    maQuyen  VARCHAR(50) NOT NULL,
    phamVi   ENUM('ALL','DEPT','TEAM','SELF') NOT NULL DEFAULT 'SELF',
    PRIMARY KEY (maVaiTro, maQuyen),
    FOREIGN KEY (maVaiTro) REFERENCES VAITRO(maVaiTro) ON DELETE CASCADE,
    FOREIGN KEY (maQuyen)  REFERENCES QUYEN(maQuyen)   ON DELETE CASCADE
) ENGINE=InnoDB;

-- FK cua CHUCVU -> VAITRO (them sau khi VAITRO da duoc tao)
ALTER TABLE CHUCVU ADD CONSTRAINT FK_CHUCVU_VAITRO
    FOREIGN KEY (maVaiTro) REFERENCES VAITRO(maVaiTro) ON DELETE SET NULL;

CREATE TABLE TAIKHOAN (
    maTaiKhoan INT AUTO_INCREMENT PRIMARY KEY,
    tenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    maNV VARCHAR(20) UNIQUE,
    maVaiTro VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    hoatDong BOOLEAN DEFAULT TRUE,
    biKhoa BOOLEAN DEFAULT FALSE,
    lanDangNhapCuoi DATETIME,
    soLanDangNhapLoi INT DEFAULT 0,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE SET NULL,
    FOREIGN KEY (maVaiTro) REFERENCES VAITRO(maVaiTro)
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 10: THONG BAO NOI BO
-- =====================================================

CREATE TABLE THONGBAO (
    maThongBao INT AUTO_INCREMENT PRIMARY KEY,
    tieuDe NVARCHAR(200) NOT NULL,
    noiDung TEXT,
    loaiThongBao ENUM('he_thong', 'don_tu', 'thong_bao_chung') DEFAULT 'he_thong',
    maTaiKhoanGui INT NULL,
    maTaiKhoanNhan INT NOT NULL,
    daDoc BOOLEAN DEFAULT FALSE,
    ngayDoc DATETIME,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maTaiKhoanGui) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE SET NULL,
    FOREIGN KEY (maTaiKhoanNhan) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIEP VU 11: TUYEN DUNG
-- =====================================================

CREATE TABLE YEUCAUTUYENDUNG (
    maYeuCau INT AUTO_INCREMENT PRIMARY KEY,
    maPhongBan VARCHAR(20) NOT NULL,
    maChucVu VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL DEFAULT 1,
    lyDo NVARCHAR(500),
    mucLuongDuKien NVARCHAR(100),
    yeuCauKinhNghiem NVARCHAR(500),
    yeuCauHocVan NVARCHAR(255),
    yeuCauKhac NVARCHAR(500),
    hanTuyenDung DATE,
    nguoiDuyet INT,
    ngayDuyet DATETIME,
    trangThai ENUM('cho_duyet', 'da_duyet', 'tu_choi', 'da_tuyen_du') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maPhongBan) REFERENCES PHONGBAN(maPhongBan),
    FOREIGN KEY (maChucVu) REFERENCES CHUCVU(maChucVu)
) ENGINE=InnoDB;

CREATE TABLE TINTUYENDUNG (
    maTin INT AUTO_INCREMENT PRIMARY KEY,
    maYeuCau INT NOT NULL,
    tieuDe NVARCHAR(200) NOT NULL,
    noiDung TEXT,
    mucLuong NVARCHAR(100),
    diaDiem NVARCHAR(255),
    hanNopHoSo DATE,
    trangThai ENUM('dang_tuyen', 'tam_dung', 'da_dong') DEFAULT 'dang_tuyen',
    soLuotXem INT DEFAULT 0,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maYeuCau) REFERENCES YEUCAUTUYENDUNG(maYeuCau) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE UNGVIEN (
    maUngVien INT AUTO_INCREMENT PRIMARY KEY,
    maTin INT NOT NULL,
    hoTen NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    dienThoai VARCHAR(15),
    ngaySinh DATE,
    gioiTinh ENUM('nam', 'nu', 'khac'),
    diaChi NVARCHAR(255),
    trinhDoHocVan NVARCHAR(100),
    kinhNghiem NVARCHAR(500),
    fileCV VARCHAR(255),
    nguonUngTuyen NVARCHAR(100),
    trangThai ENUM('moi', 'dang_phong_van', 'trung_tuyen', 'da_chuyen_nhan_vien', 'tu_choi') DEFAULT 'moi',
    nhanXet NVARCHAR(1000),
    maNV VARCHAR(20) NULL,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maTin) REFERENCES TINTUYENDUNG(maTin) ON DELETE CASCADE,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- BANG PHU TRO
-- =====================================================

CREATE TABLE CAUHINH_PHUCAP (
    maCauHinh INT AUTO_INCREMENT PRIMARY KEY,
    loai ENUM('phu_cap', 'khau_tru') NOT NULL DEFAULT 'phu_cap',
    tenKhoan VARCHAR(100) NOT NULL,
    kieuTinh ENUM('co_dinh', 'phan_tram') NOT NULL DEFAULT 'co_dinh',
    giaTri DOUBLE NOT NULL DEFAULT 0,
    nguon VARCHAR(50) DEFAULT 'CongTy',
    hoatDong TINYINT(1) NOT NULL DEFAULT 1,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE LOG_AUDIT (
    maLog INT AUTO_INCREMENT PRIMARY KEY,
    maTaiKhoan INT,
    hanhDong VARCHAR(50) NOT NULL,
    bangDuLieu VARCHAR(50),
    maBanGhi VARCHAR(50),
    duLieuCu TEXT,
    duLieuMoi TEXT,
    diaChiIP VARCHAR(45),
    userAgent VARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maTaiKhoan) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_nv_trangthai ON NHANVIEN(trangThai);
CREATE INDEX idx_nv_ngayvaolam ON NHANVIEN(ngayVaoLam);
CREATE INDEX idx_bonhiem_nv ON BONHIEM(maNV);
CREATE INDEX idx_bonhiem_trangthai ON BONHIEM(trangThai);
CREATE INDEX idx_bonhiem_tungay ON BONHIEM(tuNgay);
CREATE INDEX idx_chamcong_ngay ON CHAMCONG(ngay);
CREATE INDEX idx_chamcong_nv_thang ON CHAMCONG(maNV, ngay);
CREATE INDEX idx_don_nv ON DONXINNGHIPHEP(maNV);
CREATE INDEX idx_don_trangthai ON DONXINNGHIPHEP(trangThai);
CREATE INDEX idx_thongbao_nguoinhan ON THONGBAO(maTaiKhoanNhan);
CREATE INDEX idx_thongbao_dadoc ON THONGBAO(daDoc);
CREATE INDEX idx_audit_taikhoan ON LOG_AUDIT(maTaiKhoan);
CREATE INDEX idx_audit_ngay ON LOG_AUDIT(ngayTao);
CREATE INDEX idx_audit_bang ON LOG_AUDIT(bangDuLieu);

-- =====================================================
-- DU LIEU MAC DINH
-- =====================================================


-- =====================================================
-- TRIGGERS & PROCEDURES
-- =====================================================

DELIMITER //

CREATE TRIGGER trg_cap_nhat_so_phep
AFTER UPDATE ON DONXINNGHIPHEP
FOR EACH ROW
BEGIN
    IF NEW.trangThai = 'da_duyet' AND OLD.trangThai != 'da_duyet' THEN
        UPDATE SODUNGPHEP
        SET soNgayDaDung = soNgayDaDung + NEW.soNgayNghi
        WHERE maNV = NEW.maNV
          AND nam = YEAR(NEW.tuNgay)
          AND maLoaiPhep = NEW.maLoaiPhep;
    END IF;
END //

CREATE PROCEDURE sp_tao_so_du_phep(IN p_nhan_vien_id INT, IN p_nam INT)
BEGIN
    INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap)
    SELECT p_nhan_vien_id, p_nam, maLoaiPhep,
           CASE maLoaiPhep
               WHEN 'PHEP_NAM' THEN 12
               ELSE 0
           END
    FROM LOAIPHEP
    WHERE trangThai = 'hoatDong';
END //

CREATE PROCEDURE sp_tinh_xep_loai(IN p_danh_gia_id INT)
BEGIN
    DECLARE v_tong_diem DECIMAL(5,2);
    DECLARE v_ty_le DECIMAL(5,2);
    DECLARE v_xep_loai VARCHAR(20);

    -- Thang diem co dinh 1-10, trong so tinh theo phan tram (tong = 100%)
    -- tongDiem = SUM(diem * trongSo / 100), gia tri trong khoang 0-10
    SELECT SUM(ct.diem * ddtc.trongSo / 100.0)
    INTO v_tong_diem
    FROM CHITIETDANHGIA ct
    JOIN DANHGIAHIEUSUAT dg ON ct.maDanhGia = dg.maDanhGia
    JOIN DOTDANHGIA_TIEUCHI ddtc ON dg.maDot = ddtc.maDot AND ct.maTieuChi = ddtc.maTieuChi
    WHERE ct.maDanhGia = p_danh_gia_id;

    -- Quy doi ve ty le % (0-100) de xep loai
    SET v_ty_le = v_tong_diem * 10;

    SET v_xep_loai = CASE
        WHEN v_ty_le >= 90 THEN 'xuat_sac'
        WHEN v_ty_le >= 80 THEN 'tot'
        WHEN v_ty_le >= 65 THEN 'kha'
        WHEN v_ty_le >= 50 THEN 'trung_binh'
        ELSE 'yeu'
    END;

    UPDATE DANHGIAHIEUSUAT
    SET tongDiem = v_tong_diem, xepLoai = v_xep_loai
    WHERE maDanhGia = p_danh_gia_id;
END //

DELIMITER ;

SELECT 'Database HRM created successfully!' AS Message;
