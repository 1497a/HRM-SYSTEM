package com.hrm.model;

/**
 * Model cau hinh phu cap / khau tru mac dinh.
 *
 * Admin quan ly danh sach nay. Khi tinh luong,
 * he thong tu dong ap dung cac khoan nay cho moi NV.
 *
 * Co 2 kieu tinh:
 *   CO_DINH  -> so tien co dinh (VD: phu cap an trua = 500,000d)
 *   PHAN_TRAM -> % tren luong co ban (VD: BHXH = 8% luong CB)
 */
public class CauHinhPhuCap {

    public enum KieuTinh {
        CO_DINH("co_dinh", "Co dinh"),
        PHAN_TRAM("phan_tram", "% Luong CB");
        private final String dbValue;
        private final String displayName;
        KieuTinh(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }
        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }
    }

    private int id;
    private ThanhPhanLuong.Loai loai;   // PHU_CAP hoac KHAU_TRU
    private String tenKhoan;
    private KieuTinh kieuTinh;
    private double giaTri;              // So tien (neu co dinh) hoac % (neu phan tram)
    private String nguon;               // CongTy, LuatDinh, DanhGia...
    private boolean hoatDong;
    private String xepLoaiApDung;       // null = ap dung cho moi NV; non-null = chi ap cho NV co xepLoai nay
    public CauHinhPhuCap() {
        this.hoatDong = true;
        this.kieuTinh = KieuTinh.CO_DINH;
    }

    public CauHinhPhuCap(ThanhPhanLuong.Loai loai, String tenKhoan,
                          KieuTinh kieuTinh, double giaTri, String nguon) {
        this();
        this.loai = loai;
        this.tenKhoan = tenKhoan;
        this.kieuTinh = kieuTinh;
        this.giaTri = giaTri;
        this.nguon = nguon;
    }

    /**
     * Tinh so tien thuc te dua tren luong co ban.
     * - Co dinh: tra ve giaTri
     * - Phan tram: tra ve luongCoBan x giaTri / 100
     */
    public double tinhSoTien(double luongCoBan) {
        if (kieuTinh == KieuTinh.PHAN_TRAM) {
            return Math.round(luongCoBan * giaTri / 100.0);
        }
        return giaTri;
    }

    /** Hien thi gia tri (500,000d hoac 8%) */
    public String hienThiGiaTri() {
        if (kieuTinh == KieuTinh.PHAN_TRAM) {
            return giaTri % 1 == 0 ? (int) giaTri + "%" : giaTri + "%";
        }
        return String.valueOf((long) giaTri);
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ThanhPhanLuong.Loai getLoai() { return loai; }
    public void setLoai(ThanhPhanLuong.Loai loai) { this.loai = loai; }

    public String getTenKhoan() { return tenKhoan; }
    public void setTenKhoan(String tenKhoan) { this.tenKhoan = tenKhoan; }

    public KieuTinh getKieuTinh() { return kieuTinh; }
    public void setKieuTinh(KieuTinh kieuTinh) { this.kieuTinh = kieuTinh; }

    public double getGiaTri() { return giaTri; }
    public void setGiaTri(double giaTri) { this.giaTri = giaTri; }

    public String getNguon() { return nguon; }
    public void setNguon(String nguon) { this.nguon = nguon; }

    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }

    public String getXepLoaiApDung() { return xepLoaiApDung; }
    public void setXepLoaiApDung(String xepLoaiApDung) { this.xepLoaiApDung = xepLoaiApDung; }

    @Override
    public String toString() {
        return loai.toString() + ": " + tenKhoan + " = " + hienThiGiaTri();
    }
}
