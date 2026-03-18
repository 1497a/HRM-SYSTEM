package com.hrm.model;

import java.time.LocalDate;

/**
 * Model cho thông tin cá nhân của nhân viên.
 * Bổ sung cho bảng NHANVIEN, lưu các thông tin nhân thân.
 */
public class ThongTinCaNhan {

    private String maNV;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;        // "nam", "nu", "khac"
    private String cccd;
    private String dienThoai;
    private String email;
    private String diaChi;
    private String diaChiThuongTru;
    private String queQuan;
    private String tinhTrangHonNhan; // "doc_than", "da_ket_hon", "ly_hon"
    private String fileCV;
    private String trinhDoHocVan;
    private String kinhNghiem;
    public ThongTinCaNhan() {
    }

    public ThongTinCaNhan(String maNV, String hoTen, LocalDate ngaySinh, String gioiTinh,
                          String cccd, String dienThoai, String email,
                          String diaChi, String diaChiThuongTru, String queQuan,
                          String tinhTrangHonNhan) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.cccd = cccd;
        this.dienThoai = dienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.diaChiThuongTru = diaChiThuongTru;
        this.queQuan = queQuan;
        this.tinhTrangHonNhan = tinhTrangHonNhan;
    }

    // ============================
    // Getters & Setters
    // ============================
    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getDiaChiThuongTru() {
        return diaChiThuongTru;
    }

    public void setDiaChiThuongTru(String diaChiThuongTru) {
        this.diaChiThuongTru = diaChiThuongTru;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }

    public String getTinhTrangHonNhan() {
        return tinhTrangHonNhan;
    }

    public void setTinhTrangHonNhan(String tinhTrangHonNhan) {
        this.tinhTrangHonNhan = tinhTrangHonNhan;
    }

    public String getFileCv() {
        return fileCV;
    }

    public void setFileCv(String fileCv) {
        this.fileCV = fileCv;
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

    @Override
    public String toString() {
        return hoTen + " (maNV=" + maNV + ")";
    }
}
