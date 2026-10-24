package com.hrm.model;

import com.hrm.util.HRMConstants;

public class TaiKhoan {
    private int id;
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String email;
    private boolean hoatDong;
    private boolean biKhoa;
    private String nhanVienId;
    private VaiTro vaiTro;
    public TaiKhoan() {
        this.hoatDong = true;
        this.biKhoa = false;
    }

    public TaiKhoan(int id, String tenDangNhap, String matKhau, String hoTen, String email) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.email = email;
        this.hoatDong = true;
        this.biKhoa = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }
    public boolean isBiKhoa() { return biKhoa; }
    public void setBiKhoa(boolean biKhoa) { this.biKhoa = biKhoa; }
    public String getMaNV() { return nhanVienId; }
    public void setMaNV(String maNV) { this.nhanVienId = maNV; }
    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }

    public boolean coQuyen(String maQuyen) {
        if (HRMConstants.USERNAME_ADMIN.equalsIgnoreCase(tenDangNhap) || coVaiTro(HRMConstants.ROLE_ADMIN)) {
            return true;
        }
        return vaiTro != null && vaiTro.coQuyen(maQuyen);
    }

    public boolean coVaiTro(String maVaiTro) {
        if (HRMConstants.ROLE_ADMIN.equalsIgnoreCase(maVaiTro) && HRMConstants.USERNAME_ADMIN.equalsIgnoreCase(tenDangNhap)) {
            return true;
        }
        return vaiTro != null && vaiTro.getId().equals(maVaiTro);
    }

    public String getTenVaiTro() {
        return vaiTro != null ? vaiTro.getTenVaiTro() : "";
    }

    @Override
    public String toString() { return hoTen + " (" + tenDangNhap + ")"; }
}
