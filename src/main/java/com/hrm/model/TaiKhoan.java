package com.hrm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaiKhoan {
    private int id;
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String email;
    private boolean hoatDong;
    private boolean biKhoa;
    private Integer nhanVienId;
    private List<VaiTro> vaiTros;
    private Map<String, Boolean> ngoaiLeQuyen;

    public TaiKhoan() {
        this.hoatDong = true;
        this.biKhoa = false;
        this.vaiTros = new ArrayList<>();
        this.ngoaiLeQuyen = new HashMap<>();
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
        this.ngoaiLeQuyen = new HashMap<>();
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
    public Integer getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(Integer nhanVienId) { this.nhanVienId = nhanVienId; }
    public List<VaiTro> getVaiTros() { return vaiTros; }
    public void setVaiTros(List<VaiTro> vaiTros) { this.vaiTros = vaiTros; }
    public Map<String, Boolean> getNgoaiLeQuyen() { return ngoaiLeQuyen; }
    public void setNgoaiLeQuyen(Map<String, Boolean> ngoaiLeQuyen) { this.ngoaiLeQuyen = ngoaiLeQuyen; }

    public void themVaiTro(VaiTro vaiTro) {
        if (!vaiTros.contains(vaiTro)) vaiTros.add(vaiTro);
    }
    public void xoaVaiTro(VaiTro vaiTro) { vaiTros.remove(vaiTro); }
    public void capQuyenNgoaiLe(String maQuyen) { ngoaiLeQuyen.put(maQuyen, true); }
    public void thuHoiQuyenNgoaiLe(String maQuyen) { ngoaiLeQuyen.put(maQuyen, false); }
    public void xoaNgoaiLeQuyen(String maQuyen) { ngoaiLeQuyen.remove(maQuyen); }

    public boolean coQuyen(String maQuyen) {
        if (ngoaiLeQuyen.containsKey(maQuyen)) return ngoaiLeQuyen.get(maQuyen);
        return vaiTros.stream().anyMatch(vt -> vt.coQuyen(maQuyen));
    }

    public Boolean trangThaiQuyen(String maQuyen) { return ngoaiLeQuyen.get(maQuyen); }

    public boolean coVaiTro(String maVaiTro) {
        return vaiTros.stream().anyMatch(vt -> vt.getId().equals(maVaiTro));
    }

    // Legacy compatibility methods used by GUI/SessionContext
    public String getUsername() { return tenDangNhap; }
    public String getPassword() { return matKhau; }
    public String getFullName() { return hoTen; }
    public boolean isActive() { return hoatDong; }
    public boolean isLocked() { return biKhoa; }
    public Integer getMaNV() { return nhanVienId; }
    public void setMaNV(Integer maNV) { this.nhanVienId = maNV; }
    public List<VaiTro> getRoles() { return vaiTros; }
    public boolean hasPermission(String permissionCode) { return coQuyen(permissionCode); }
    public boolean hasRole(String roleCode) { return coVaiTro(roleCode); }

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
