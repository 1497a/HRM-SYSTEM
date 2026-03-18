package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DonXinNghiPhep {
    public enum TrangThai {
        CHO_DUYET("cho_duyet", "Cho duyet"),
        DA_DUYET("da_duyet", "Da duyet"),
        TU_CHOI("tu_choi", "Tu choi"),
        HUY("huy", "Da huy");
        private final String dbValue;
        private final String tenHienThi;
        TrangThai(String dbValue, String tenHienThi) {
            this.dbValue = dbValue;
            this.tenHienThi = tenHienThi;
        }
        public String getDbValue() { return dbValue; }
        public String getTenHienThi() { return tenHienThi; }
        public static TrangThai fromDb(String value) {
            for (TrangThai t : values()) {
                if (t.dbValue.equals(value)) return t;
            }
            return CHO_DUYET;
        }
    }

    private int id;
    private String nhanVienId;
    private String tenNhanVien;
    private String loaiPhepId;
    private String tenLoaiPhep;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private double soNgayNghi;
    private String lyDo;
    private TrangThai trangThai;
    private String nguoiDuyetId;
    private String tenNguoiDuyet;
    private String lyDoTuChoi;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    public DonXinNghiPhep() {
        this.trangThai = TrangThai.CHO_DUYET;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMaNV() { return nhanVienId; }
    public void setMaNV(String maNV) { this.nhanVienId = maNV; }
    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
    public String getMaLoaiPhep() { return loaiPhepId; }
    public void setMaLoaiPhep(String maLoaiPhep) { this.loaiPhepId = maLoaiPhep; }
    public String getTenLoaiPhep() { return tenLoaiPhep; }
    public void setTenLoaiPhep(String tenLoaiPhep) { this.tenLoaiPhep = tenLoaiPhep; }
    public LocalDate getTuNgay() { return tuNgay; }
    public void setTuNgay(LocalDate tuNgay) { this.tuNgay = tuNgay; }
    public LocalDate getDenNgay() { return denNgay; }
    public void setDenNgay(LocalDate denNgay) { this.denNgay = denNgay; }
    public double getSoNgayNghi() { return soNgayNghi; }
    public void setSoNgayNghi(double soNgayNghi) { this.soNgayNghi = soNgayNghi; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) {
        this.trangThai = trangThai;
        this.ngayCapNhat = LocalDateTime.now();
    }
    public String getMaNguoiDuyet() { return nguoiDuyetId; }
    public void setMaNguoiDuyet(String maNguoiDuyet) { this.nguoiDuyetId = maNguoiDuyet; }
    public String getTenNguoiDuyet() { return tenNguoiDuyet; }
    public void setTenNguoiDuyet(String tenNguoiDuyet) { this.tenNguoiDuyet = tenNguoiDuyet; }
    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
