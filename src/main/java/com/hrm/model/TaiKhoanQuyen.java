package com.hrm.model;

public class TaiKhoanQuyen {
    private int taiKhoanId;
    private String quyenId;
    private boolean choPhep;
    private String ghiChu;

    public TaiKhoanQuyen(int taiKhoanId, String quyenId, boolean choPhep) {
        this.taiKhoanId = taiKhoanId;
        this.quyenId = quyenId;
        this.choPhep = choPhep;
    }

    public TaiKhoanQuyen(int taiKhoanId, String quyenId, boolean choPhep, String ghiChu) {
        this(taiKhoanId, quyenId, choPhep);
        this.ghiChu = ghiChu;
    }

    public int getTaiKhoanId() { return taiKhoanId; }
    public void setTaiKhoanId(int taiKhoanId) { this.taiKhoanId = taiKhoanId; }
    public String getQuyenId() { return quyenId; }
    public void setQuyenId(String quyenId) { this.quyenId = quyenId; }
    public boolean isChoPhep() { return choPhep; }
    public void setChoPhep(boolean choPhep) { this.choPhep = choPhep; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    // Legacy compatibility
    public int getUserId() { return taiKhoanId; }
    public String getPermissionCode() { return quyenId; }
    public boolean isGranted() { return choPhep; }

    @Override
    public String toString() { return quyenId + (choPhep ? " [CAP]" : " [THU HOI]"); }
}
