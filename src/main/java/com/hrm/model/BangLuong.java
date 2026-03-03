package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model dai dien cho bang bang_luongs trong co so du lieu.
 *
 * Moi ban ghi dai dien cho 1 ky luong (thuong la 1 thang).
 * Lien ket: bang_luongs → N chi_tiet_luongs (moi NV 1 chi tiet)
 */
public class BangLuong {

    public enum TrangThai {
        NHAP("nhap", "Nhap"),
        DA_TINH("da_tinh", "Da tinh"),
        DA_DUYET("da_duyet", "Da duyet"),
        DA_KHOA("da_khoa", "Da khoa");

        private final String dbValue;
        private final String displayName;

        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        public static TrangThai fromDbValue(String value) {
            for (TrangThai t : values()) {
                if (t.dbValue.equals(value)) return t;
            }
            return NHAP;
        }
    }

    private int id;
    private int thang;
    private int nam;
    private String tenBangLuong;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayKhoa;
    private LocalDateTime ngayDuyet;
    private String nguoiTao;
    private String nguoiKhoa;
    private String nguoiDuyet;
    private TrangThai trangThai;

    public BangLuong() {
        this.trangThai = TrangThai.NHAP;
        this.ngayTao = LocalDateTime.now();
    }

    public BangLuong(int thang, int nam, String tenBangLuong) {
        this();
        this.thang = thang;
        this.nam = nam;
        this.tenBangLuong = tenBangLuong;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public String getTenBangLuong() { return tenBangLuong; }
    public void setTenBangLuong(String tenBangLuong) { this.tenBangLuong = tenBangLuong; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgayKhoa() { return ngayKhoa; }
    public void setNgayKhoa(LocalDateTime ngayKhoa) { this.ngayKhoa = ngayKhoa; }

    public LocalDateTime getNgayDuyet() { return ngayDuyet; }
    public void setNgayDuyet(LocalDateTime ngayDuyet) { this.ngayDuyet = ngayDuyet; }

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public String getNguoiKhoa() { return nguoiKhoa; }
    public void setNguoiKhoa(String nguoiKhoa) { this.nguoiKhoa = nguoiKhoa; }

    public String getNguoiDuyet() { return nguoiDuyet; }
    public void setNguoiDuyet(String nguoiDuyet) { this.nguoiDuyet = nguoiDuyet; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    // Legacy compatibility - old code may reference maBL
    public int getMaBL() { return id; }
    public void setMaBL(int maBL) { this.id = maBL; }

    @Override
    public String toString() {
        return "BangLuong{id=" + id + ", thang=" + thang + "/" + nam
                + ", " + (trangThai != null ? trangThai.toString() : "") + "}";
    }
}
