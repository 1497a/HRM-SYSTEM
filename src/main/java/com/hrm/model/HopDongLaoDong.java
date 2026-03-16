package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model đại diện cho bảng HOPDONGLAODONG (Hợp đồng lao động).
 * Lưu thông tin hợp đồng giữa doanh nghiệp và nhân viên.
 */
public class HopDongLaoDong {

    private int maHopDong;                // Khóa chính (auto-increment)
    private String soHopDong;             // Số hợp đồng (ví dụ: HD001/2025)
    private String maNV;                  // Mã nhân viên (String: "NV001", "NV002", ...)
    private String loaiHopDong;           // "thu_viec", "xac_dinh_thoi_han", "khong_xac_dinh"
    private long luongCoSo;               // Lương cơ bản theo hợp đồng
    private LocalDate ngayKy;             // Ngày ký hợp đồng
    private LocalDate ngayHieuLuc;        // Ngày hợp đồng có hiệu lực
    private LocalDate ngayHetHieuLuc;     // Ngày hết hiệu lực (null nếu không xác định thời hạn)
    private String fileDinhKem;           // Đường dẫn file hợp đồng (PDF, ảnh, ...)
    private String noiDung;               // Nội dung hợp đồng (tóm tắt hoặc full text)
    private String trangThai;             // "cho_duyet", "hieu_luc", "het_han", "thanh_ly", "huy"
    private LocalDate ngayThanhLy;        // Ngày thanh lý (nếu có)
    private String lyDoThanhLy;           // Lý do thanh lý/hủy
    private String ghiChu;                // Ghi chú bổ sung

    // Transient (không lưu DB) - dùng để hiển thị
    private transient String tenNV;

    public HopDongLaoDong() {
        this.trangThai = "cho_duyet";
    }

    // Constructor đầy đủ
    public HopDongLaoDong(int maHopDong, String soHopDong, String maNV, String loaiHopDong,
                          long luongCoSo, LocalDate ngayKy, LocalDate ngayHieuLuc,
                          LocalDate ngayHetHieuLuc, String fileDinhKem, String noiDung,
                          String trangThai, LocalDate ngayThanhLy, String lyDoThanhLy,
                          String ghiChu) {
        this.maHopDong = maHopDong;
        this.soHopDong = soHopDong;
        this.maNV = maNV;
        this.loaiHopDong = loaiHopDong;
        this.luongCoSo = luongCoSo;
        this.ngayKy = ngayKy;
        this.ngayHieuLuc = ngayHieuLuc;
        this.ngayHetHieuLuc = ngayHetHieuLuc;
        this.fileDinhKem = fileDinhKem;
        this.noiDung = noiDung;
        this.trangThai = trangThai;
        this.ngayThanhLy = ngayThanhLy;
        this.lyDoThanhLy = lyDoThanhLy;
        this.ghiChu = ghiChu;
    }

    // ============================
    // Getters & Setters
    // ============================

    public int getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(int maHopDong) {
        this.maHopDong = maHopDong;
    }

    public String getSoHopDong() {
        return soHopDong;
    }

    public void setSoHopDong(String soHopDong) {
        this.soHopDong = soHopDong;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getLoaiHopDong() {
        return loaiHopDong;
    }

    public void setLoaiHopDong(String loaiHopDong) {
        this.loaiHopDong = loaiHopDong;
    }

    public long getLuongCoSo() {
        return luongCoSo;
    }

    public void setLuongCoSo(long luongCoSo) {
        this.luongCoSo = luongCoSo;
    }

    public LocalDate getNgayKy() {
        return ngayKy;
    }

    public void setNgayKy(LocalDate ngayKy) {
        this.ngayKy = ngayKy;
    }

    public LocalDate getNgayHieuLuc() {
        return ngayHieuLuc;
    }

    public void setNgayHieuLuc(LocalDate ngayHieuLuc) {
        this.ngayHieuLuc = ngayHieuLuc;
    }

    public LocalDate getNgayHetHieuLuc() {
        return ngayHetHieuLuc;
    }

    public void setNgayHetHieuLuc(LocalDate ngayHetHieuLuc) {
        this.ngayHetHieuLuc = ngayHetHieuLuc;
    }

    public String getFileDinhKem() {
        return fileDinhKem;
    }

    public void setFileDinhKem(String fileDinhKem) {
        this.fileDinhKem = fileDinhKem;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayThanhLy() {
        return ngayThanhLy;
    }

    public void setNgayThanhLy(LocalDate ngayThanhLy) {
        this.ngayThanhLy = ngayThanhLy;
    }

    public String getLyDoThanhLy() {
        return lyDoThanhLy;
    }

    public void setLyDoThanhLy(String lyDoThanhLy) {
        this.lyDoThanhLy = lyDoThanhLy;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    // ============================
    // Display & Business Helpers
    // ============================

    public String getLoaiHopDongDisplay() {
        if (loaiHopDong == null) return "Không xác định";
        return switch (loaiHopDong) {
            case "thu_viec"             -> "Thử việc";
            case "xac_dinh_thoi_han"    -> "Xác định thời hạn";
            case "khong_xac_dinh"       -> "Không xác định thời hạn";
            default                     -> loaiHopDong;
        };
    }

    public String getTrangThaiDisplay() {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case "cho_duyet"    -> "Chờ phê duyệt";
            case "hieu_luc"     -> "Hiệu lực";
            case "sap_het_han"  -> "Sắp hết hạn";
            case "het_han"      -> "Hết hạn";
            case "thanh_ly"     -> "Thanh lý";
            case "huy"          -> "Hủy";
            default             -> trangThai;
        };
    }

    /**
     * Kiểm tra hợp đồng đang hiệu lực (ngày hiện tại nằm trong khoảng hiệu lực).
     */
    public boolean isHieuLuc() {
        LocalDate today = LocalDate.now();
        return "hieu_luc".equals(trangThai)
                && ngayHieuLuc != null && !today.isBefore(ngayHieuLuc)
                && (ngayHetHieuLuc == null || !today.isAfter(ngayHetHieuLuc));
    }

    /**
     * Kiểm tra hợp đồng sắp hết hạn (còn dưới 30 ngày).
     */
    public boolean isSapHetHan() {
        return ngayHetHieuLuc != null
                && LocalDate.now().plusDays(30).isAfter(ngayHetHieuLuc)
                && !LocalDate.now().isAfter(ngayHetHieuLuc);
    }

    /**
     * Kiểm tra hợp đồng đã hết hạn.
     */
    public boolean isHetHan() {
        return ngayHetHieuLuc != null && LocalDate.now().isAfter(ngayHetHieuLuc);
    }

    /**
     * Kiểm tra hợp đồng có thể thanh lý/hủy (đang hiệu lực và chưa thanh lý).
     */
    public boolean coTheThanhLy() {
        return "hieu_luc".equals(trangThai) && !isHetHan();
    }

    @Override
    public String toString() {
        return "HopDongLaoDong{" +
                "maHopDong=" + maHopDong +
                ", soHopDong='" + soHopDong + '\'' +
                ", maNV='" + maNV + '\'' +
                ", loaiHopDong='" + getLoaiHopDongDisplay() + '\'' +
                ", trangThai='" + getTrangThaiDisplay() + '\'' +
                ", ngayHieuLuc=" + ngayHieuLuc +
                ", ngayHetHieuLuc=" + ngayHetHieuLuc +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HopDongLaoDong that = (HopDongLaoDong) o;
        return maHopDong == that.maHopDong &&
               Objects.equals(soHopDong, that.soHopDong) &&
               Objects.equals(maNV, that.maNV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHopDong, soHopDong, maNV);
    }
}