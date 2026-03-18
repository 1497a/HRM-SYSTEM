package com.hrm.model;

import java.time.LocalDate;

/**
 * Model cho bang nhan_viens trong co so du lieu.
 * Luu thong tin hop dong va trang thai lam viec cua nhan vien.
 */
public class NhanVien {

    private String maNhanVien;
    private String loaiHopDong; // "thu_viec", "xac_dinh_thoi_han", "khong_xac_dinh"
    private LocalDate ngayVaoLam;
    private String trangThai;   // "dang_lam_viec", "tam_nghi", "nghi_viec"
    private String ghiChu;
    // Transient - dung de hien thi, khong luu truc tiep trong bang nhan_viens
    private transient String hoTen;
    private transient String phongBanHienTaiId;
    private transient String tenPhongBanHienTai;
    private transient String tenChucVuHienTai;
    public NhanVien() {
    }

    public NhanVien(String maNhanVien, String loaiHopDong, LocalDate ngayVaoLam, String trangThai, String ghiChu) {
        this.maNhanVien = maNhanVien;
        this.loaiHopDong = loaiHopDong;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    // ============================
    // Getters & Setters - core fields
    // ============================
    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getLoaiHopDong() {
        return loaiHopDong;
    }

    public void setLoaiHopDong(String loaiHopDong) {
        this.loaiHopDong = loaiHopDong;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    // ============================
    // Getters & Setters - transient display fields
    // ============================
    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getMaPhongBanHienTai() {
        return phongBanHienTaiId;
    }

    public void setMaPhongBanHienTai(String maPhongBanHienTai) {
        this.phongBanHienTaiId = maPhongBanHienTai;
    }
    public String getTenPhongBanHienTai() {
        return tenPhongBanHienTai;
    }

    public void setTenPhongBanHienTai(String tenPhongBanHienTai) {
        this.tenPhongBanHienTai = tenPhongBanHienTai;
    }

    public String getTenChucVuHienTai() {
        return tenChucVuHienTai;
    }

    public void setTenChucVuHienTai(String tenChucVuHienTai) {
        this.tenChucVuHienTai = tenChucVuHienTai;
    }

    // ============================
    // Display helpers
    // ============================
    public String getTrangThaiDisplay() {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "dang_lam_viec": return "Dang lam viec";
            case "tam_nghi":      return "Tam nghi";
            case "nghi_viec":     return "Nghi viec";
            default:              return trangThai;
        }
    }

    public String getLoaiHopDongDisplay() {
        if (loaiHopDong == null) return "";
        switch (loaiHopDong) {
            case "thu_viec":          return "Thu viec";
            case "xac_dinh_thoi_han": return "Xac dinh thoi han";
            case "khong_xac_dinh":    return "Khong xac dinh";
            default:                  return loaiHopDong;
        }
    }

    @Override
    public String toString() {
        if (hoTen != null && !hoTen.isEmpty()) {
            return hoTen + " (" + maNhanVien + ")";
        }
        return maNhanVien;
    }
}
