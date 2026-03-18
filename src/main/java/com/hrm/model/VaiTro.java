package com.hrm.model;

import java.util.ArrayList;
import java.util.List;

public class VaiTro {
    private String id;
    private String tenVaiTro;
    private String moTa;
    private boolean laHeThong;
    private List<Quyen> quyens;
    public VaiTro() {
        this.laHeThong = false;
        this.quyens = new ArrayList<>();
    }

    public VaiTro(String id, String tenVaiTro, String moTa) {
        this.id = id;
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
        this.laHeThong = false;
        this.quyens = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public boolean isLaHeThong() { return laHeThong; }
    public void setLaHeThong(boolean laHeThong) { this.laHeThong = laHeThong; }
    public List<Quyen> getQuyens() { return quyens; }
    public void setQuyens(List<Quyen> quyens) { this.quyens = quyens; }

    public void themQuyen(Quyen quyen) {
        if (!quyens.contains(quyen)) quyens.add(quyen);
    }
    public void xoaQuyen(Quyen quyen) { quyens.remove(quyen); }
    public void xoaHetQuyen() { quyens.clear(); }

    public boolean coQuyen(String maQuyen) {
        return quyens.stream().anyMatch(q -> q.getId().equals(maQuyen));
    }

    @Override
    public String toString() { return tenVaiTro + " (" + id + ")"; }
}
