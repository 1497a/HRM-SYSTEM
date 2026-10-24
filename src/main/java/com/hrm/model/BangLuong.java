package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model đại diện cho bảng bang_luongs trong cơ sở dữ liệu.
 * Mỗi bản ghi đại diện cho 1 kỳ lương (thường là 1 tháng).
 * Liên kết: bang_luongs → N chi_tiet_luongs (mỗi nhân viên 1 chi tiết).
 */
public class BangLuong {

    public enum TrangThai {
        NHAP("nhap", "Nhập liệu"),
        DA_TINH("da_tinh", "Đã tính"),
        DA_DUYET("da_duyet", "Đã duyệt"),
        DA_KHOA("da_khoa", "Đã khóa");
        private final String dbValue;
        private final String displayName;
        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }
        public String getDbValue() {
            return dbValue;
        }
        public String getDisplayName() {
            return displayName;
        }
        public static TrangThai fromDbValue(String value) {
            if (value == null) return NHAP;
            for (TrangThai t : values()) {
                if (t.dbValue.equalsIgnoreCase(value.trim())) {
                    return t;
                }
            }
            return NHAP; // fallback
        }
        @Override
        public String toString() {
            return displayName;
        }
    }

    private int maBL;                     // Khóa chính (auto-increment)
    private int thang;
    private int nam;
    private String tenBangLuong;          // Ví dụ: "Bảng lương tháng 03/2025"
    private LocalDate ngayBatDau;         // Ngày đầu kỳ lương
    private LocalDate ngayKetThuc;        // Ngày cuối kỳ lương
    private LocalDateTime ngayTao;
    private LocalDateTime ngayDuyet;
    private LocalDateTime ngayKhoa;
    private String nguoiTao;              // maNV hoặc username người tạo
    private String nguoiDuyet;            // maNV hoặc username người duyệt
    private String nguoiKhoa;             // maNV hoặc username người khóa
    private TrangThai trangThai;
    private String ghiChu;                // Ghi chú chung cho bảng lương
    private Double tongThuNhap;           // Tổng thu nhập tất cả nhân viên (tùy chọn, có thể tính động)
    private Double tongThucNhan;          // Tổng thực nhận tất cả nhân viên (tùy chọn)
    // Constructor mặc định
    public BangLuong() {
        this.trangThai = TrangThai.NHAP;
        this.ngayTao = LocalDateTime.now();
    }

    // Constructor phổ biến khi tạo mới bảng lương
    public BangLuong(int thang, int nam) {
        this();
        this.thang = thang;
        this.nam = nam;
        this.tenBangLuong = String.format("Bảng lương tháng %02d/%d", thang, nam);
        this.ngayBatDau = LocalDate.of(nam, thang, 1);
        this.ngayKetThuc = this.ngayBatDau.withDayOfMonth(this.ngayBatDau.lengthOfMonth());
    }

    // Getters & Setters
    public int getMaBL() {
        return maBL;
    }

    public void setMaBL(int maBL) {
        this.maBL = maBL;
    }

    public int getThang() {
        return thang;
    }

    public void setThang(int thang) {
        this.thang = thang;
    }

    public int getNam() {
        return nam;
    }

    public void setNam(int nam) {
        this.nam = nam;
    }

    public String getTenBangLuong() {
        return tenBangLuong;
    }

    public void setTenBangLuong(String tenBangLuong) {
        this.tenBangLuong = tenBangLuong;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayDuyet() {
        return ngayDuyet;
    }

    public void setNgayDuyet(LocalDateTime ngayDuyet) {
        this.ngayDuyet = ngayDuyet;
    }

    public LocalDateTime getNgayKhoa() {
        return ngayKhoa;
    }

    public void setNgayKhoa(LocalDateTime ngayKhoa) {
        this.ngayKhoa = ngayKhoa;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public String getNguoiDuyet() {
        return nguoiDuyet;
    }

    public void setNguoiDuyet(String nguoiDuyet) {
        this.nguoiDuyet = nguoiDuyet;
    }

    public String getNguoiKhoa() {
        return nguoiKhoa;
    }

    public void setNguoiKhoa(String nguoiKhoa) {
        this.nguoiKhoa = nguoiKhoa;
    }

    public TrangThai getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThai trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Double getTongThuNhap() {
        return tongThuNhap;
    }

    public void setTongThuNhap(Double tongThuNhap) {
        this.tongThuNhap = tongThuNhap;
    }

    public Double getTongThucNhan() {
        return tongThucNhan;
    }

    public void setTongThucNhan(Double tongThucNhan) {
        this.tongThucNhan = tongThucNhan;
    }

    @Override
    public String toString() {
        return "BangLuong{" +
                "maBL=" + maBL +
                ", thang=" + thang +
                ", nam=" + nam +
                ", tenBangLuong='" + tenBangLuong + '\'' +
                ", trangThai=" + (trangThai != null ? trangThai.getDisplayName() : "null") +
                ", ngayBatDau=" + ngayBatDau +
                ", ngayKetThuc=" + ngayKetThuc +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BangLuong bangLuong = (BangLuong) o;
        return maBL == bangLuong.maBL;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBL);
    }
}