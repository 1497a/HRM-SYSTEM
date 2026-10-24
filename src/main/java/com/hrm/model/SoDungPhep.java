package com.hrm.model;

public class SoDungPhep {
    private int id;
    private String nhanVienId;
    private String loaiPhepId;
    private int nam;
    private double soNgayDuocCap;
    private double soNgayDaDung;
    public SoDungPhep() {}

    public SoDungPhep(String nhanVienId, String loaiPhepId, int nam, double soNgayDuocCap) {
        this.nhanVienId = nhanVienId;
        this.loaiPhepId = loaiPhepId;
        this.nam = nam;
        this.soNgayDuocCap = soNgayDuocCap;
        this.soNgayDaDung = 0;
    }

    public double getSoNgayConLai() { return soNgayDuocCap - soNgayDaDung; }
    public void tru(double soNgay) { this.soNgayDaDung += soNgay; }
    public void cong(double soNgay) { this.soNgayDaDung = Math.max(0, this.soNgayDaDung - soNgay); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMaNV() { return nhanVienId; }
    public void setMaNV(String maNV) { this.nhanVienId = maNV; }
    public String getMaLoaiPhep() { return loaiPhepId; }
    public void setMaLoaiPhep(String maLoaiPhep) { this.loaiPhepId = maLoaiPhep; }
    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }
    public double getSoNgayDuocCap() { return soNgayDuocCap; }
    public void setSoNgayDuocCap(double soNgayDuocCap) { this.soNgayDuocCap = soNgayDuocCap; }
    public double getSoNgayDaDung() { return soNgayDaDung; }
    public void setSoNgayDaDung(double soNgayDaDung) { this.soNgayDaDung = soNgayDaDung; }
}
