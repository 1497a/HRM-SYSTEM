package com.hrm.model;

import java.time.LocalDate;

/**
 * Model cho bảng TINTUYENDUNG.
 */
public class TinTuyenDung {

    private int maTin;
    private int maYeuCau;
    private String tieuDe;
    private String noiDung;
    private String mucLuong;
    private String diaDiem;
    private LocalDate hanNopHoSo;
    private String trangThai; // "dang_tuyen","tam_dung","da_dong"
    private LocalDate ngayTao;

    // Transient - dùng để hiển thị
    private transient String tenPhongBan;
    private transient String tenChucVu;
    private transient int soUngVien;

    public TinTuyenDung() {
    }

    // ============================
    // Getters & Setters
    // ============================

    public int getMaTin() {
        return maTin;
    }

    public void setMaTin(int maTin) {
        this.maTin = maTin;
    }

    public int getMaYeuCau() {
        return maYeuCau;
    }

    public void setMaYeuCau(int maYeuCau) {
        this.maYeuCau = maYeuCau;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getMucLuong() {
        return mucLuong;
    }

    public void setMucLuong(String mucLuong) {
        this.mucLuong = mucLuong;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public LocalDate getHanNopHoSo() {
        return hanNopHoSo;
    }

    public void setHanNopHoSo(LocalDate hanNopHoSo) {
        this.hanNopHoSo = hanNopHoSo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
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

    public int getSoUngVien() {
        return soUngVien;
    }

    public void setSoUngVien(int soUngVien) {
        this.soUngVien = soUngVien;
    }

    /**
     * Trả về tên trạng thái bằng tiếng Việt.
     */
    public String getTrangThaiDisplay() {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "dang_tuyen": return "Đang tuyển";
            case "tam_dung":   return "Tạm dừng";
            case "da_dong":    return "Đã đóng";
            default:           return trangThai;
        }
    }

    @Override
    public String toString() {
        return tieuDe != null ? tieuDe : "Tin#" + maTin;
    }
}
