package com.hrm.model;

import com.hrm.util.HRMConstants;

import java.util.ArrayList;
import java.util.List;

public class TaiKhoan {
    private int id;
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String email;
    private boolean hoatDong;
    private boolean biKhoa;
    private String nhanVienId;
    private List<VaiTro> vaiTros;
    public TaiKhoan() {
        this.hoatDong = true;
        this.biKhoa = false;
        this.vaiTros = new ArrayList<>();
    }

    public TaiKhoan(int id, String tenDangNhap, String matKhau, String hoTen, String email) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.email = email;
        this.hoatDong = true;
        this.biKhoa = false;
        this.vaiTros = new ArrayList<>();
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
    public List<VaiTro> getVaiTros() { return vaiTros; }
    public void setVaiTros(List<VaiTro> vaiTros) { this.vaiTros = vaiTros; }

    public void themVaiTro(VaiTro vaiTro) {
        if (!vaiTros.contains(vaiTro)) vaiTros.add(vaiTro);
    }
    public void xoaVaiTro(VaiTro vaiTro) { vaiTros.remove(vaiTro); }

    public boolean coQuyen(String maQuyen) {
        if (HRMConstants.USERNAME_ADMIN.equalsIgnoreCase(tenDangNhap) || coVaiTro(HRMConstants.ROLE_ADMIN)) {
            return true;
        }
        return vaiTros.stream().anyMatch(vt -> vt.coQuyen(maQuyen));
    }

    public boolean coVaiTro(String maVaiTro) {
        if (HRMConstants.ROLE_ADMIN.equalsIgnoreCase(maVaiTro) && HRMConstants.USERNAME_ADMIN.equalsIgnoreCase(tenDangNhap)) {
            return true;
        }
        return vaiTros.stream().anyMatch(vt -> vt.getId().equals(maVaiTro));
    }

    public String getTenVaiTros() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vaiTros.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(vaiTros.get(i).getTenVaiTro());
        }
        return sb.toString();
    }

    @Override
    public String toString() { return hoTen + " (" + tenDangNhap + ")"; }
}
