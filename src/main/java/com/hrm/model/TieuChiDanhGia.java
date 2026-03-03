package com.hrm.model;

public class TieuChiDanhGia {
    private int id;
    private String tenTieuChi;
    private String moTa;
    private String nhomTieuChi;
    private double diemToiDa;
    private boolean hoatDong;

    public TieuChiDanhGia() {
        this.diemToiDa = 10;
        this.hoatDong = true;
    }

    public TieuChiDanhGia(int id, String tenTieuChi, String moTa, String nhomTieuChi) {
        this.id = id;
        this.tenTieuChi = tenTieuChi;
        this.moTa = moTa;
        this.nhomTieuChi = nhomTieuChi;
        this.diemToiDa = 10;
        this.hoatDong = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTenTieuChi() { return tenTieuChi; }
    public void setTenTieuChi(String tenTieuChi) { this.tenTieuChi = tenTieuChi; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getNhomTieuChi() { return nhomTieuChi; }
    public void setNhomTieuChi(String nhomTieuChi) { this.nhomTieuChi = nhomTieuChi; }
    public double getDiemToiDa() { return diemToiDa; }
    public void setDiemToiDa(double diemToiDa) { this.diemToiDa = diemToiDa; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }

    // Legacy compatibility
    public String getName() { return tenTieuChi; }
    public String getDescription() { return moTa; }
    public double getMaxScore() { return diemToiDa; }
    public boolean isActive() { return hoatDong; }

    @Override
    public String toString() { return tenTieuChi; }
}
