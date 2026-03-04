package com.hrm.model;

import java.time.LocalDate;

/**
 * Model cho bảng UNGVIEN.
 */
public class UngVien {

    private int maUngVien;
    private int maTin;
    private String hoTen;
    private String email;
    private String dienThoai;
    private LocalDate ngaySinh;
    private String gioiTinh; // "nam","nu","khac"
    private String diaChi;
    private String trinhDoHocVan;
    private String kinhNghiem;
    private String fileCv;
    private String nguonUngTuyen;
    private String trangThai; // "moi","dang_phong_van","trung_tuyen","tu_choi"
    private String nhanXet;
    private int maNV;          // 0 = chưa liên kết nhân viên
    private LocalDate ngayTao;

    // Transient - dùng để hiển thị
    private transient String tenTin;

    public UngVien() {
    }

    // ============================
    // Getters & Setters
    // ============================

    public int getMaUngVien() {
        return maUngVien;
    }

    public void setMaUngVien(int maUngVien) {
        this.maUngVien = maUngVien;
    }

    public int getMaTin() {
        return maTin;
    }

    public void setMaTin(int maTin) {
        this.maTin = maTin;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getTrinhDoHocVan() {
        return trinhDoHocVan;
    }

    public void setTrinhDoHocVan(String trinhDoHocVan) {
        this.trinhDoHocVan = trinhDoHocVan;
    }

    public String getKinhNghiem() {
        return kinhNghiem;
    }

    public void setKinhNghiem(String kinhNghiem) {
        this.kinhNghiem = kinhNghiem;
    }

    public String getFileCv() {
        return fileCv;
    }

    public String getFileCV() {
        return fileCv;
    }

    public void setFileCv(String fileCv) {
        this.fileCv = fileCv;
    }

    public String getNguonUngTuyen() {
        return nguonUngTuyen;
    }

    public void setNguonUngTuyen(String nguonUngTuyen) {
        this.nguonUngTuyen = nguonUngTuyen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTenTin() {
        return tenTin;
    }

    public void setTenTin(String tenTin) {
        this.tenTin = tenTin;
    }

    /**
     * Trả về tên trạng thái bằng tiếng Việt.
     */
    public String getTrangThaiDisplay() {
        if (maNV > 0) return "Da chuyen thanh nhan vien";
        if (trangThai == null) return "";
        switch (trangThai) {
            case "moi":             return "Mới";
            case "dang_phong_van":  return "Đang phỏng vấn";
            case "trung_tuyen":     return "Trung tuyen";
            case "da_chuyen_nhan_vien": return "Da chuyen thanh nhan vien";
            case "tu_choi":         return "Từ chối";
            default:                return trangThai;
        }
    }

    @Override
    public String toString() {
        return hoTen != null ? hoTen : "UngVien#" + maUngVien;
    }
}



