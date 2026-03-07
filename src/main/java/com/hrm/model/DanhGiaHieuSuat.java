package com.hrm.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DanhGiaHieuSuat {
    public enum XepLoai {
        XUAT_SAC("xuat_sac", "Xuất sắc", 9.0, 10.0),
        TOT("tot", "Tốt", 8.0, 8.9),
        KHA("kha", "Khá", 6.5, 7.9),
        TRUNG_BINH("trung_binh", "Trung bình", 5.0, 6.4),
        YEU("yeu", "Yếu", 0.0, 4.9);

        private final String dbValue;
        private final String tenHienThi;
        private final double diemMin;
        private final double diemMax;

        XepLoai(String dbValue, String tenHienThi, double diemMin, double diemMax) {
            this.dbValue = dbValue;
            this.tenHienThi = tenHienThi;
            this.diemMin = diemMin;
            this.diemMax = diemMax;
        }

        public String getDbValue() { return dbValue; }
        public String getTenHienThi() { return tenHienThi; }

        public static XepLoai tuDiem(double diem) {
            for (XepLoai x : values()) {
                if (diem >= x.diemMin && diem <= x.diemMax) return x;
            }
            return YEU;
        }

        public static XepLoai fromDb(String value) {
            for (XepLoai x : values()) {
                if (x.dbValue.equals(value)) return x;
            }
            return TRUNG_BINH;
        }
    }

    private int id;
    private int dotDanhGiaId;
    private String tenDot;
    private String nhanVienId;
    private String tenNhanVien;
    private String nguoiDanhGiaId;
    private String tenNguoiDanhGia;
    private List<ChiTietDanhGia> chiTietDanhGias;
    private double tongDiem;
    private XepLoai xepLoai;
    private String nhanXetChung;
    private LocalDateTime ngayDanhGia;
    private String trangThai;

    public DanhGiaHieuSuat() {
        this.chiTietDanhGias = new ArrayList<>();
        this.ngayDanhGia = LocalDateTime.now();
        this.trangThai = "chua_danh_gia";
    }

    public void tinhTong() {
        if (chiTietDanhGias.isEmpty()) { this.tongDiem = 0; this.xepLoai = XepLoai.YEU; return; }
        double tong = chiTietDanhGias.stream().mapToDouble(ChiTietDanhGia::getDiemCoTrong).sum();
        this.tongDiem = Math.round(tong * 100.0) / 100.0;
        this.xepLoai = XepLoai.tuDiem(tongDiem);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDotDanhGiaId() { return dotDanhGiaId; }
    public void setDotDanhGiaId(int dotDanhGiaId) { this.dotDanhGiaId = dotDanhGiaId; }
    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }
    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }
    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
    public String getNguoiDanhGiaId() { return nguoiDanhGiaId; }
    public void setNguoiDanhGiaId(String nguoiDanhGiaId) { this.nguoiDanhGiaId = nguoiDanhGiaId; }
    public String getTenNguoiDanhGia() { return tenNguoiDanhGia; }
    public void setTenNguoiDanhGia(String tenNguoiDanhGia) { this.tenNguoiDanhGia = tenNguoiDanhGia; }
    public List<ChiTietDanhGia> getChiTietDanhGias() { return chiTietDanhGias; }
    public void setChiTietDanhGias(List<ChiTietDanhGia> chiTietDanhGias) { this.chiTietDanhGias = chiTietDanhGias; }
    public double getTongDiem() { return tongDiem; }
    public void setTongDiem(double tongDiem) { this.tongDiem = tongDiem; }
    public XepLoai getXepLoai() { return xepLoai; }
    public void setXepLoai(XepLoai xepLoai) { this.xepLoai = xepLoai; }
    public String getNhanXetChung() { return nhanXetChung; }
    public void setNhanXetChung(String nhanXetChung) { this.nhanXetChung = nhanXetChung; }
    public LocalDateTime getNgayDanhGia() { return ngayDanhGia; }
    public void setNgayDanhGia(LocalDateTime ngayDanhGia) { this.ngayDanhGia = ngayDanhGia; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // Legacy compat
    public int getCycleId() { return dotDanhGiaId; }
    public String getEmployeeId() { return nhanVienId; }
    public String getEmployeeName() { return tenNhanVien; }
    public String getEvaluatorId() { return nguoiDanhGiaId; }
    public List<ChiTietDanhGia> getScores() { return chiTietDanhGias; }
    public double getOverallScore() { return tongDiem; }
    public String getGeneralComment() { return nhanXetChung; }
    public LocalDateTime getSubmittedAt() { return ngayDanhGia; }
}
