package com.hrm.model;

public class ChiTietDanhGia {
    private int id;
    private int danhGiaId;
    private int tieuChiId;
    private String tenTieuChi;
    private double trongSo;
    private double diem;
    private String nhanXet;

    public ChiTietDanhGia() {}

    public ChiTietDanhGia(int tieuChiId, String tenTieuChi, double trongSo, double diem) {
        this.tieuChiId = tieuChiId;
        this.tenTieuChi = tenTieuChi;
        this.trongSo = trongSo;
        this.diem = diem;
    }

    public double getDiemCoTrong() { return trongSo > 0 ? diem * trongSo / 100.0 : diem; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDanhGiaId() { return danhGiaId; }
    public void setDanhGiaId(int danhGiaId) { this.danhGiaId = danhGiaId; }
    public int getTieuChiId() { return tieuChiId; }
    public void setTieuChiId(int tieuChiId) { this.tieuChiId = tieuChiId; }
    public String getTenTieuChi() { return tenTieuChi; }
    public void setTenTieuChi(String tenTieuChi) { this.tenTieuChi = tenTieuChi; }
    public double getTrongSo() { return trongSo; }
    public void setTrongSo(double trongSo) { this.trongSo = trongSo; }
    public double getDiem() { return diem; }
    public void setDiem(double diem) { this.diem = diem; }
    public String getNhanXet() { return nhanXet; }
    public void setNhanXet(String nhanXet) { this.nhanXet = nhanXet; }

    // Legacy compat
    public int getCriteriaId() { return tieuChiId; }
    public String getCriteriaName() { return tenTieuChi; }
    public double getScore() { return diem; }
    public String getComment() { return nhanXet; }
    public double getWeightedScore() { return getDiemCoTrong(); }
}
