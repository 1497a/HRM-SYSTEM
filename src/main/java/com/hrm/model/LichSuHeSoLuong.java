package com.hrm.model;

public class LichSuHeSoLuong {
    private int id;
    private String chucVuId;
    private double heSoLuongCu;
    private double heSoLuongMoi;
    private double phuCapCu;
    private double phuCapMoi;
    private String ngayThayDoi;
    private String nguoiThayDoi;

    public LichSuHeSoLuong() {}

    public LichSuHeSoLuong(int id, String chucVuId, double heSoLuongCu, double heSoLuongMoi,
            double phuCapCu, double phuCapMoi, String ngayThayDoi, String nguoiThayDoi) {
        this.id = id;
        this.chucVuId = chucVuId;
        this.heSoLuongCu = heSoLuongCu;
        this.heSoLuongMoi = heSoLuongMoi;
        this.phuCapCu = phuCapCu;
        this.phuCapMoi = phuCapMoi;
        this.ngayThayDoi = ngayThayDoi;
        this.nguoiThayDoi = nguoiThayDoi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getChucVuId() { return chucVuId; }
    public void setChucVuId(String chucVuId) { this.chucVuId = chucVuId; }
    public double getHeSoLuongCu() { return heSoLuongCu; }
    public void setHeSoLuongCu(double heSoLuongCu) { this.heSoLuongCu = heSoLuongCu; }
    public double getHeSoLuongMoi() { return heSoLuongMoi; }
    public void setHeSoLuongMoi(double heSoLuongMoi) { this.heSoLuongMoi = heSoLuongMoi; }
    public double getPhuCapCu() { return phuCapCu; }
    public void setPhuCapCu(double phuCapCu) { this.phuCapCu = phuCapCu; }
    public double getPhuCapMoi() { return phuCapMoi; }
    public void setPhuCapMoi(double phuCapMoi) { this.phuCapMoi = phuCapMoi; }
    public String getNgayThayDoi() { return ngayThayDoi; }
    public void setNgayThayDoi(String ngayThayDoi) { this.ngayThayDoi = ngayThayDoi; }
    public String getNguoiThayDoi() { return nguoiThayDoi; }
    public void setNguoiThayDoi(String nguoiThayDoi) { this.nguoiThayDoi = nguoiThayDoi; }

    // Legacy compat
    public String getMaChucVu() { return chucVuId; }
}
