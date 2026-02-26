package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model cho bảng BONHIEM (Bổ nhiệm nhân viên).
 * Lưu thông tin phân công chức vụ và phòng ban cho nhân viên.
 */
public class BoNhiem {

    private int maBoNhiem;
    private int maNV;
    private String maPhongBan;
    private String maChucVu;
    private String loaiBoNhiem;       // "chinh", "kiem_nhiem"
    private double tyLeHuongLuong;    // 0 - 100
    private int maQuanLy;             // 0 = không có quản lý
    private int nguoiDuyet;           // 0 = chưa được duyệt
    private LocalDate tuNgay;
    private LocalDate denNgay;        // null = vô thời hạn
    private LocalDateTime ngayPheDuyet;
    private String lyDo;
    private String trangThai;         // "cho_duyet", "hieu_luc", "het_hieu_luc", "tu_choi"

    // Transient display fields - không lưu trong DB, load từ JOIN
    private transient String tenNV;
    private transient String tenPhongBan;
    private transient String tenChucVu;
    private transient String tenQuanLy;
    private transient String tenNguoiDuyet;

    public BoNhiem() {
    }

    public BoNhiem(int maBoNhiem, int maNV, String maPhongBan, String maChucVu,
                   String loaiBoNhiem, double tyLeHuongLuong, int maQuanLy, int nguoiDuyet,
                   LocalDate tuNgay, LocalDate denNgay, LocalDateTime ngayPheDuyet,
                   String lyDo, String trangThai) {
        this.maBoNhiem = maBoNhiem;
        this.maNV = maNV;
        this.maPhongBan = maPhongBan;
        this.maChucVu = maChucVu;
        this.loaiBoNhiem = loaiBoNhiem;
        this.tyLeHuongLuong = tyLeHuongLuong;
        this.maQuanLy = maQuanLy;
        this.nguoiDuyet = nguoiDuyet;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.ngayPheDuyet = ngayPheDuyet;
        this.lyDo = lyDo;
        this.trangThai = trangThai;
    }

    // ============================
    // Getters & Setters - core fields
    // ============================

    public int getMaBoNhiem() {
        return maBoNhiem;
    }

    public void setMaBoNhiem(int maBoNhiem) {
        this.maBoNhiem = maBoNhiem;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getMaPhongBan() {
        return maPhongBan;
    }

    public void setMaPhongBan(String maPhongBan) {
        this.maPhongBan = maPhongBan;
    }

    public String getMaChucVu() {
        return maChucVu;
    }

    public void setMaChucVu(String maChucVu) {
        this.maChucVu = maChucVu;
    }

    public String getLoaiBoNhiem() {
        return loaiBoNhiem;
    }

    public void setLoaiBoNhiem(String loaiBoNhiem) {
        this.loaiBoNhiem = loaiBoNhiem;
    }

    public double getTyLeHuongLuong() {
        return tyLeHuongLuong;
    }

    public void setTyLeHuongLuong(double tyLeHuongLuong) {
        this.tyLeHuongLuong = tyLeHuongLuong;
    }

    public int getMaQuanLy() {
        return maQuanLy;
    }

    public void setMaQuanLy(int maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    public int getNguoiDuyet() {
        return nguoiDuyet;
    }

    public void setNguoiDuyet(int nguoiDuyet) {
        this.nguoiDuyet = nguoiDuyet;
    }

    public LocalDate getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(LocalDate tuNgay) {
        this.tuNgay = tuNgay;
    }

    public LocalDate getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(LocalDate denNgay) {
        this.denNgay = denNgay;
    }

    public LocalDateTime getNgayPheDuyet() {
        return ngayPheDuyet;
    }

    public void setNgayPheDuyet(LocalDateTime ngayPheDuyet) {
        this.ngayPheDuyet = ngayPheDuyet;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    // ============================
    // Getters & Setters - transient fields
    // ============================

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public String getTenPhongBan() {
        return tenPhongBan;
    }

    public void setTenPhongBan(String tenPhongBan) {
        this.tenPhongBan = tenPhongBan;
    }

    public String getTenChucVu() {
        return tenChucVu;
    }

    public void setTenChucVu(String tenChucVu) {
        this.tenChucVu = tenChucVu;
    }

    public String getTenQuanLy() {
        return tenQuanLy;
    }

    public void setTenQuanLy(String tenQuanLy) {
        this.tenQuanLy = tenQuanLy;
    }

    public String getTenNguoiDuyet() {
        return tenNguoiDuyet;
    }

    public void setTenNguoiDuyet(String tenNguoiDuyet) {
        this.tenNguoiDuyet = tenNguoiDuyet;
    }

    // ============================
    // Display helpers
    // ============================

    public String getTrangThaiDisplay() {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "cho_duyet":    return "Chờ duyệt";
            case "hieu_luc":     return "Hiệu lực";
            case "het_hieu_luc": return "Hết hiệu lực";
            case "tu_choi":      return "Từ chối";
            default:             return trangThai;
        }
    }

    public String getLoaiBoNhiemDisplay() {
        if (loaiBoNhiem == null) return "";
        switch (loaiBoNhiem) {
            case "chinh":      return "Chính";
            case "kiem_nhiem": return "Kiêm nhiệm";
            default:           return loaiBoNhiem;
        }
    }

    @Override
    public String toString() {
        return "BoNhiem{maBoNhiem=" + maBoNhiem + ", maNV=" + maNV
                + ", maPhongBan='" + maPhongBan + "', trangThai='" + trangThai + "'}";
    }
}
