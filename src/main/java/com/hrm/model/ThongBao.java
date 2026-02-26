package com.hrm.model;

import java.time.LocalDateTime;

/**
 * Model cho bảng THONGBAO.
 */
public class ThongBao {

    private int maThongBao;
    private String tieuDe;
    private String noiDung;
    private String loaiThongBao; // "he_thong", "don_tu", "thong_bao_chung"
    private int maTaiKhoanGui;   // 0 = hệ thống
    private int maTaiKhoanNhan;
    private boolean daDoc;
    private LocalDateTime ngayDoc;
    private String linkLienQuan;
    private LocalDateTime ngayTao;

    public ThongBao() {
    }

    // ============================
    // Getters & Setters
    // ============================

    public int getMaThongBao() {
        return maThongBao;
    }

    public void setMaThongBao(int maThongBao) {
        this.maThongBao = maThongBao;
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

    public String getLoaiThongBao() {
        return loaiThongBao;
    }

    public void setLoaiThongBao(String loaiThongBao) {
        this.loaiThongBao = loaiThongBao;
    }

    public int getMaTaiKhoanGui() {
        return maTaiKhoanGui;
    }

    public void setMaTaiKhoanGui(int maTaiKhoanGui) {
        this.maTaiKhoanGui = maTaiKhoanGui;
    }

    public int getMaTaiKhoanNhan() {
        return maTaiKhoanNhan;
    }

    public void setMaTaiKhoanNhan(int maTaiKhoanNhan) {
        this.maTaiKhoanNhan = maTaiKhoanNhan;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public void setDaDoc(boolean daDoc) {
        this.daDoc = daDoc;
    }

    public LocalDateTime getNgayDoc() {
        return ngayDoc;
    }

    public void setNgayDoc(LocalDateTime ngayDoc) {
        this.ngayDoc = ngayDoc;
    }

    public String getLinkLienQuan() {
        return linkLienQuan;
    }

    public void setLinkLienQuan(String linkLienQuan) {
        this.linkLienQuan = linkLienQuan;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    /**
     * Trả về tên loại thông báo bằng tiếng Việt.
     */
    public String getLoaiDisplay() {
        if (loaiThongBao == null) return "";
        switch (loaiThongBao) {
            case "he_thong":       return "Hệ thống";
            case "don_tu":         return "Đơn từ";
            case "thong_bao_chung": return "Thông báo chung";
            default:               return loaiThongBao;
        }
    }

    @Override
    public String toString() {
        return tieuDe;
    }
}
