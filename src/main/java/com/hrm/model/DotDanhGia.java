package com.hrm.model;

import java.time.LocalDate;

public class DotDanhGia {
    public enum TrangThai {
        CHUA_BAT_DAU("chua_bat_dau", "Chưa bắt đầu"),
        DANG_DIEN_RA("dang_dien_ra", "Đang diễn ra"),
        DA_KET_THUC("da_ket_thuc",   "Đã kết thúc");
        private final String dbValue;
        private final String tenHienThi;
        TrangThai(String dbValue, String tenHienThi) {
            this.dbValue = dbValue;
            this.tenHienThi = tenHienThi;
        }
        public String getDbValue() { return dbValue; }
        public String getTenHienThi() { return tenHienThi; }
        public static TrangThai fromDb(String value) {
            for (TrangThai t : values()) {
                if (t.dbValue.equals(value)) return t;
            }
            return CHUA_BAT_DAU;
        }
    }

    private int id;
    private String tenDot;
    private int nam;
    private String kyDanhGia;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private TrangThai trangThai;
    public DotDanhGia() { this.trangThai = TrangThai.CHUA_BAT_DAU; }

    public DotDanhGia(int id, String tenDot, int nam, String kyDanhGia, LocalDate tuNgay, LocalDate denNgay) {
        this.id = id;
        this.tenDot = tenDot;
        this.nam = nam;
        this.kyDanhGia = kyDanhGia;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.trangThai = TrangThai.CHUA_BAT_DAU;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }
    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }
    public String getKyDanhGia() { return kyDanhGia; }
    public void setKyDanhGia(String kyDanhGia) { this.kyDanhGia = kyDanhGia; }
    public LocalDate getTuNgay() { return tuNgay; }
    public void setTuNgay(LocalDate tuNgay) { this.tuNgay = tuNgay; }
    public LocalDate getDenNgay() { return denNgay; }
    public void setDenNgay(LocalDate denNgay) { this.denNgay = denNgay; }
    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public boolean dangDienRa() { return trangThai == TrangThai.DANG_DIEN_RA; }
    public boolean daKetThuc() { return trangThai == TrangThai.DA_KET_THUC; }

    @Override
    public String toString() { return tenDot + " (" + kyDanhGia + "/" + nam + ")"; }
}
