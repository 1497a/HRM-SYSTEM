package com.hrm.model;

import java.util.Date;

public class HopDong {
    private String maHopDong;
    private String maNV;
    private String loaiHopDong;
    private double luongCoSo;
    private Date ngayHieuLuc;
    private String trangThai;

    public HopDong() {}

    public HopDong(String maHopDong, String maNV, String loaiHopDong, double luongCoSo, Date ngayHieuLuc, String trangThai) {
        this.maHopDong = maHopDong;
        this.maNV = maNV;
        this.loaiHopDong = loaiHopDong;
        this.luongCoSo = luongCoSo;
        this.ngayHieuLuc = ngayHieuLuc;
        this.trangThai = trangThai;
    }

    // --- GETTER & SETTER ---
    public String getMaHopDong() { return maHopDong; }
    public void setMaHopDong(String maHopDong) { this.maHopDong = maHopDong; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getLoaiHopDong() { return loaiHopDong; }
    public void setLoaiHopDong(String loaiHopDong) { this.loaiHopDong = loaiHopDong; }

    public double getLuongCoSo() { return luongCoSo; }
    public void setLuongCoSo(double luongCoSo) { this.luongCoSo = luongCoSo; }

    public Date getNgayHieuLuc() { return ngayHieuLuc; }
    public void setNgayHieuLuc(Date ngayHieuLuc) { this.ngayHieuLuc = ngayHieuLuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}