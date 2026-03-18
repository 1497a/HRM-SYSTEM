package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model cho bang bo_nhiems (Bo nhiem nhan vien).
 * Luu thong tin phan cong chuc vu va phong ban cho nhan vien.
 */
public class BoNhiem {

    private int id;
    private String nhanVienId;
    private String phongBanId;
    private String chucVuId;
    private String loaiBoNhiem;       // "chinh", "kiem_nhiem"
    private double tyLeHuongLuong;    // 0 - 100
    private String quanLyId;          // null = khong co quan ly
    private String nguoiDuyet;        // null = chua duoc duyet
    private LocalDate tuNgay;
    private LocalDate denNgay;        // null = vo thoi han
    private LocalDateTime ngayPheDuyet;
    private String lyDo;
    private String trangThai;         // "cho_duyet", "hieu_luc", "het_hieu_luc", "tu_choi"
    // Transient display fields - khong luu trong DB, load tu JOIN
    private transient String tenNV;
    private transient String tenPhongBan;
    private transient String tenChucVu;
    private transient String tenQuanLy;
    private transient String tenNguoiDuyet;
    public BoNhiem() {
    }

    public BoNhiem(int id, String nhanVienId, String phongBanId, String chucVuId,
                   String loaiBoNhiem, double tyLeHuongLuong, String quanLyId, String nguoiDuyet,
                   LocalDate tuNgay, LocalDate denNgay, LocalDateTime ngayPheDuyet,
                   String lyDo, String trangThai) {
        this.id = id;
        this.nhanVienId = nhanVienId;
        this.phongBanId = phongBanId;
        this.chucVuId = chucVuId;
        this.loaiBoNhiem = loaiBoNhiem;
        this.tyLeHuongLuong = tyLeHuongLuong;
        this.quanLyId = quanLyId;
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
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaNV() { return nhanVienId; }
    public void setMaNV(String nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getMaPhongBan() { return phongBanId; }
    public void setMaPhongBan(String phongBanId) { this.phongBanId = phongBanId; }

    public String getMaChucVu() { return chucVuId; }
    public void setMaChucVu(String chucVuId) { this.chucVuId = chucVuId; }

    public String getLoaiBoNhiem() { return loaiBoNhiem; }
    public void setLoaiBoNhiem(String loaiBoNhiem) { this.loaiBoNhiem = loaiBoNhiem; }

    public double getTyLeHuongLuong() { return tyLeHuongLuong; }
    public void setTyLeHuongLuong(double tyLeHuongLuong) { this.tyLeHuongLuong = tyLeHuongLuong; }

    public String getMaQuanLy() { return quanLyId; }
    public void setMaQuanLy(String quanLyId) { this.quanLyId = quanLyId; }

    public String getNguoiDuyet() { return nguoiDuyet; }
    public void setNguoiDuyet(String nguoiDuyet) { this.nguoiDuyet = nguoiDuyet; }

    public LocalDate getTuNgay() { return tuNgay; }
    public void setTuNgay(LocalDate tuNgay) { this.tuNgay = tuNgay; }

    public LocalDate getDenNgay() { return denNgay; }
    public void setDenNgay(LocalDate denNgay) { this.denNgay = denNgay; }

    public LocalDateTime getNgayPheDuyet() { return ngayPheDuyet; }
    public void setNgayPheDuyet(LocalDateTime ngayPheDuyet) { this.ngayPheDuyet = ngayPheDuyet; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // ============================
    // Getters & Setters - transient fields
    // ============================
    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public String getTenPhongBan() { return tenPhongBan; }
    public void setTenPhongBan(String tenPhongBan) { this.tenPhongBan = tenPhongBan; }

    public String getTenChucVu() { return tenChucVu; }
    public void setTenChucVu(String tenChucVu) { this.tenChucVu = tenChucVu; }

    public String getTenQuanLy() { return tenQuanLy; }
    public void setTenQuanLy(String tenQuanLy) { this.tenQuanLy = tenQuanLy; }

    public String getTenNguoiDuyet() { return tenNguoiDuyet; }
    public void setTenNguoiDuyet(String tenNguoiDuyet) { this.tenNguoiDuyet = tenNguoiDuyet; }

    // ============================
    // Display helpers
    // ============================
    public String getTrangThaiDisplay() {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "cho_duyet":    return "Cho duyet";
            case "hieu_luc":     return "Hieu luc";
            case "het_hieu_luc": return "Het hieu luc";
            case "tu_choi":      return "Tu choi";
            default:             return trangThai;
        }
    }

    public String getLoaiBoNhiemDisplay() {
        if (loaiBoNhiem == null) return "";
        switch (loaiBoNhiem) {
            case "chinh":      return "Chinh";
            case "kiem_nhiem": return "Kiem nhiem";
            default:           return loaiBoNhiem;
        }
    }

    @Override
    public String toString() {
        return "BoNhiem{id=" + id + ", nhanVienId=" + nhanVienId
                + ", phongBanId='" + phongBanId + "', trangThai='" + trangThai + "'}";
    }
}
