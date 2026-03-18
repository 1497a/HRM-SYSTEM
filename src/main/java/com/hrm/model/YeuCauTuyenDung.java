package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model cho bảng YEUCAUTUYENDUNG.
 */
public class YeuCauTuyenDung {

    private int maYeuCau;
    private String maPhongBan;
    private String maChucVu;
    private int soLuong;
    private String lyDo;
    private String mucLuongDuKien;
    private String yeuCauKinhNghiem;
    private String yeuCauHocVan;
    private String yeuCauKhac;
    private LocalDate hanTuyenDung;
    private int nguoiDuyet;    // 0 = chưa có
    private LocalDateTime ngayDuyet;
    private String trangThai; // "cho_duyet","da_duyet","tu_choi","da_tuyen_du"
    // Transient - dùng để hiển thị
    private transient String tenPhongBan;
    private transient String tenChucVu;
    public YeuCauTuyenDung() {
    }

    // ============================
    // Getters & Setters
    // ============================
    public int getMaYeuCau() {
        return maYeuCau;
    }

    public void setMaYeuCau(int maYeuCau) {
        this.maYeuCau = maYeuCau;
    }

    public String getId() {
        return maPhongBan;
    }

    public void setId(String maPhongBan) {
        this.maPhongBan = maPhongBan;
    }

    public String getMaChucVu() {
        return maChucVu;
    }

    public void setMaChucVu(String maChucVu) {
        this.maChucVu = maChucVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getMucLuongDuKien() {
        return mucLuongDuKien;
    }

    public void setMucLuongDuKien(String mucLuongDuKien) {
        this.mucLuongDuKien = mucLuongDuKien;
    }

    public String getYeuCauKinhNghiem() {
        return yeuCauKinhNghiem;
    }

    public void setYeuCauKinhNghiem(String yeuCauKinhNghiem) {
        this.yeuCauKinhNghiem = yeuCauKinhNghiem;
    }

    public String getYeuCauHocVan() {
        return yeuCauHocVan;
    }

    public void setYeuCauHocVan(String yeuCauHocVan) {
        this.yeuCauHocVan = yeuCauHocVan;
    }

    public String getYeuCauKhac() {
        return yeuCauKhac;
    }

    public void setYeuCauKhac(String yeuCauKhac) {
        this.yeuCauKhac = yeuCauKhac;
    }

    public LocalDate getHanTuyenDung() {
        return hanTuyenDung;
    }

    public void setHanTuyenDung(LocalDate hanTuyenDung) {
        this.hanTuyenDung = hanTuyenDung;
    }

    public int getNguoiDuyet() {
        return nguoiDuyet;
    }

    public void setNguoiDuyet(int nguoiDuyet) {
        this.nguoiDuyet = nguoiDuyet;
    }

    public LocalDateTime getNgayDuyet() {
        return ngayDuyet;
    }

    public void setNgayDuyet(LocalDateTime ngayDuyet) {
        this.ngayDuyet = ngayDuyet;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
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

    /**
     * Trả về tên trạng thái bằng tiếng Việt.
     */
    public String getTrangThaiDisplay() {
        return RecruitmentStatus.displayYeuCau(trangThai);
    }

    @Override
    public String toString() {
        return "YeuCauTuyenDung#" + maYeuCau + " [" + (tenPhongBan != null ? tenPhongBan : maPhongBan) + "]";
    }
}
