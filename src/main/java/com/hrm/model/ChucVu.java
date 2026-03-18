package com.hrm.model;

public class ChucVu {
    private String id;
    private String tenChucVu;
    private int capBac;
    private double phuCapChucVu;
    private String moTa;
    private String trangThai;
    public ChucVu() {}

    public ChucVu(String id, String tenChucVu, int capBac, double phuCapChucVu, String moTa, String trangThai) {
        this.id = id;
        this.tenChucVu = tenChucVu;
        this.capBac = capBac;
        this.phuCapChucVu = phuCapChucVu;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenChucVu() { return tenChucVu; }
    public void setTenChucVu(String tenChucVu) { this.tenChucVu = tenChucVu; }
    public int getCapBac() { return capBac; }
    public void setCapBac(int capBac) { this.capBac = capBac; }
    public double getPhuCapChucVu() { return phuCapChucVu; }
    public void setPhuCapChucVu(double phuCapChucVu) { this.phuCapChucVu = phuCapChucVu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() { return tenChucVu + " (" + id + ")"; }
}
