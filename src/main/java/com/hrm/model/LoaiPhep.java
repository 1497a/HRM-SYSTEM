package com.hrm.model;

public class LoaiPhep {
    private String id;
    private String tenLoaiPhep;
    private boolean coLuong;
    private boolean canChungTu;
    private int soNgayToiDa;
    private String moTa;
    private String trangThai;

    public LoaiPhep() {}

    public LoaiPhep(String id, String tenLoaiPhep, int soNgayToiDa, boolean coLuong) {
        this.id = id;
        this.tenLoaiPhep = tenLoaiPhep;
        this.soNgayToiDa = soNgayToiDa;
        this.coLuong = coLuong;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenLoaiPhep() { return tenLoaiPhep; }
    public void setTenLoaiPhep(String tenLoaiPhep) { this.tenLoaiPhep = tenLoaiPhep; }
    public boolean isCoLuong() { return coLuong; }
    public void setCoLuong(boolean coLuong) { this.coLuong = coLuong; }
    public boolean isCanChungTu() { return canChungTu; }
    public void setCanChungTu(boolean canChungTu) { this.canChungTu = canChungTu; }
    public int getSoNgayToiDa() { return soNgayToiDa; }
    public void setSoNgayToiDa(int soNgayToiDa) { this.soNgayToiDa = soNgayToiDa; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // Legacy compat
    public String getCode() { return id; }
    public String getName() { return tenLoaiPhep; }
    public int getDefaultDays() { return soNgayToiDa; }
    public boolean isPaid() { return coLuong; }

    @Override
    public String toString() { return tenLoaiPhep + " (" + id + ")"; }
}
