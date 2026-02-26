package com.hrm.model;

import java.time.LocalDate;

/**
 * Model cho bảng HOPDONGLAODONG (Hợp đồng lao động).
 * Lưu thông tin hợp đồng giữa doanh nghiệp và nhân viên.
 */
public class HopDongLaoDong {

    private int maHopDong;
    private String soHopDong;
    private int maNV;
    private String loaiHopDong;         // "thu_viec", "xac_dinh_thoi_han", "khong_xac_dinh"
    private long luongCoSo;
    private LocalDate ngayKy;
    private LocalDate ngayHieuLuc;
    private LocalDate ngayHetHieuLuc;   // null nếu không xác định thời hạn
    private String fileDinhKem;
    private String noiDung;
    private String trangThai;           // "hieu_luc", "het_han", "thanh_ly", "huy"

    // Transient display field
    private transient String tenNV;

    public HopDongLaoDong() {
    }

    public HopDongLaoDong(int maHopDong, String soHopDong, int maNV, String loaiHopDong,
                          long luongCoSo, LocalDate ngayKy, LocalDate ngayHieuLuc,
                          LocalDate ngayHetHieuLuc, String fileDinhKem, String noiDung,
                          String trangThai) {
        this.maHopDong = maHopDong;
        this.soHopDong = soHopDong;
        this.maNV = maNV;
        this.loaiHopDong = loaiHopDong;
        this.luongCoSo = luongCoSo;
        this.ngayKy = ngayKy;
        this.ngayHieuLuc = ngayHieuLuc;
        this.ngayHetHieuLuc = ngayHetHieuLuc;
        this.fileDinhKem = fileDinhKem;
        this.noiDung = noiDung;
        this.trangThai = trangThai;
    }

    // ============================
    // Getters & Setters
    // ============================

    public int getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(int maHopDong) {
        this.maHopDong = maHopDong;
    }

    public String getSoHopDong() {
        return soHopDong;
    }

    public void setSoHopDong(String soHopDong) {
        this.soHopDong = soHopDong;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getLoaiHopDong() {
        return loaiHopDong;
    }

    public void setLoaiHopDong(String loaiHopDong) {
        this.loaiHopDong = loaiHopDong;
    }

    public long getLuongCoSo() {
        return luongCoSo;
    }

    public void setLuongCoSo(long luongCoSo) {
        this.luongCoSo = luongCoSo;
    }

    public LocalDate getNgayKy() {
        return ngayKy;
    }

    public void setNgayKy(LocalDate ngayKy) {
        this.ngayKy = ngayKy;
    }

    public LocalDate getNgayHieuLuc() {
        return ngayHieuLuc;
    }

    public void setNgayHieuLuc(LocalDate ngayHieuLuc) {
        this.ngayHieuLuc = ngayHieuLuc;
    }

    public LocalDate getNgayHetHieuLuc() {
        return ngayHetHieuLuc;
    }

    public void setNgayHetHieuLuc(LocalDate ngayHetHieuLuc) {
        this.ngayHetHieuLuc = ngayHetHieuLuc;
    }

    public String getFileDinhKem() {
        return fileDinhKem;
    }

    public void setFileDinhKem(String fileDinhKem) {
        this.fileDinhKem = fileDinhKem;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    // ============================
    // Display helpers
    // ============================

    public String getTrangThaiDisplay() {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "hieu_luc":  return "Hiệu lực";
            case "het_han":   return "Hết hạn";
            case "thanh_ly":  return "Thanh lý";
            case "huy":       return "Hủy";
            default:          return trangThai;
        }
    }

    public String getLoaiHopDongDisplay() {
        if (loaiHopDong == null) return "";
        switch (loaiHopDong) {
            case "thu_viec":              return "Thử việc";
            case "xac_dinh_thoi_han":     return "Xác định thời hạn";
            case "khong_xac_dinh":        return "Không xác định";
            default:                      return loaiHopDong;
        }
    }

    /**
     * Kiểm tra hợp đồng sắp hết hạn (trong vòng 30 ngày).
     */
    public boolean isSapHetHan() {
        return ngayHetHieuLuc != null
                && ngayHetHieuLuc.minusDays(30).isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "HopDongLaoDong{maHopDong=" + maHopDong + ", soHopDong='" + soHopDong
                + "', maNV=" + maNV + ", trangThai='" + trangThai + "'}";
    }
}
