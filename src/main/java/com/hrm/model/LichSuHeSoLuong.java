package com.hrm.model;

public class LichSuHeSoLuong {
    private int maLichSu;
    private String chucVuId;
    private double heSoLuongCu;
    private double heSoLuongMoi;
    private double phuCapCu;
    private double phuCapMoi;
    private String ngayThayDoi;
    private String nguoiThayDoi;

    public LichSuHeSoLuong() {}

    public LichSuHeSoLuong(int maLichSu, String chucVuId, double heSoLuongCu, double heSoLuongMoi,
                            double phuCapCu, double phuCapMoi, String ngayThayDoi, String nguoiThayDoi) {
        this.maLichSu = maLichSu;
        this.chucVuId = chucVuId;
        this.heSoLuongCu = heSoLuongCu;
        this.heSoLuongMoi = heSoLuongMoi;
        this.phuCapCu = phuCapCu;
        this.phuCapMoi = phuCapMoi;
        this.ngayThayDoi = ngayThayDoi;
        this.nguoiThayDoi = nguoiThayDoi;
    }

    public int getMaLichSu() { return maLichSu; }
    public String getChucVuId() { return chucVuId; }
    public double getHeSoLuongCu() { return heSoLuongCu; }
    public double getHeSoLuongMoi() { return heSoLuongMoi; }
    public double getPhuCapCu() { return phuCapCu; }
    public double getPhuCapMoi() { return phuCapMoi; }
    public String getNgayThayDoi() { return ngayThayDoi; }
    public String getNguoiThayDoi() { return nguoiThayDoi; }
}
