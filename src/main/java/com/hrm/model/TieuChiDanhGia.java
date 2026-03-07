package com.hrm.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model đại diện cho bảng TIEUCHIDANHGIA (Tiêu chí đánh giá hiệu suất).
 * Mỗi tiêu chí là một hạng mục dùng để chấm điểm nhân viên trong các đợt đánh giá.
 */
public class TieuChiDanhGia {

    private int maTieuChi;              // Khóa chính (auto-increment)
    private String tenTieuChi;          // Tên tiêu chí (ví dụ: "Kỹ năng chuyên môn", "Thái độ làm việc")
    private String moTa;                // Mô tả chi tiết tiêu chí
    private String nhomTieuChi;         // Nhóm tiêu chí (ví dụ: "KPI", "Năng lực", "Thái độ")
    private double diemToiDa;           // Điểm tối đa của tiêu chí (thường 5, 10, 100)
    private double trongSo;             // Trọng số (%) – dùng khi gán vào đợt đánh giá (tổng = 100)
    private boolean hoatDong;           // Trạng thái hoạt động (true = đang dùng, false = ngừng sử dụng)
    private LocalDateTime ngayTao;      // Thời điểm tạo tiêu chí
    private String nguoiTao;            // Mã NV hoặc username người tạo

    // Transient (không lưu DB) - dùng để hiển thị
    private transient String tenNhomHienThi;

    public TieuChiDanhGia() {
        this.diemToiDa = 10.0;
        this.trongSo = 0.0;
        this.hoatDong = true;
        this.ngayTao = LocalDateTime.now();
    }

    public TieuChiDanhGia(int maTieuChi, String tenTieuChi, String moTa, String nhomTieuChi,
                          double diemToiDa, double trongSo) {
        this();
        this.maTieuChi = maTieuChi;
        this.tenTieuChi = tenTieuChi;
        this.moTa = moTa;
        this.nhomTieuChi = nhomTieuChi;
        this.diemToiDa = diemToiDa;
        this.trongSo = trongSo;
    }

    // ============================
    // Getters & Setters
    // ============================

    public int getMaTieuChi() {
        return maTieuChi;
    }

    public void setMaTieuChi(int maTieuChi) {
        this.maTieuChi = maTieuChi;
    }

    public String getTenTieuChi() {
        return tenTieuChi;
    }

    public void setTenTieuChi(String tenTieuChi) {
        this.tenTieuChi = tenTieuChi;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getNhomTieuChi() {
        return nhomTieuChi;
    }

    public void setNhomTieuChi(String nhomTieuChi) {
        this.nhomTieuChi = nhomTieuChi;
    }

    public double getDiemToiDa() {
        return diemToiDa;
    }

    public void setDiemToiDa(double diemToiDa) {
        this.diemToiDa = diemToiDa;
    }

    public double getTrongSo() {
        return trongSo;
    }

    public void setTrongSo(double trongSo) {
        this.trongSo = trongSo;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public String getTenNhomHienThi() {
        return tenNhomHienThi;
    }

    public void setTenNhomHienThi(String tenNhomHienThi) {
        this.tenNhomHienThi = tenNhomHienThi;
    }

    // ============================
    // Legacy compatibility (cho code cũ/GUI cũ)
    // ============================

    public int getId() {
        return maTieuChi;
    }

    public void setId(int id) {
        this.maTieuChi = id;
    }

    public String getName() {
        return tenTieuChi;
    }

    public String getDescription() {
        return moTa;
    }

    public double getMaxScore() {
        return diemToiDa;
    }

    public boolean isActive() {
        return hoatDong;
    }

    // ============================
    // Display & Business Helpers
    // ============================

    public String getTrangThaiDisplay() {
        return hoatDong ? "Đang hoạt động" : "Ngừng hoạt động";
    }

    public String getTrongSoDisplay() {
        return String.format("%.1f%%", trongSo);
    }

    @Override
    public String toString() {
        return "TieuChiDanhGia{" +
                "maTieuChi=" + maTieuChi +
                ", tenTieuChi='" + tenTieuChi + '\'' +
                ", nhomTieuChi='" + nhomTieuChi + '\'' +
                ", diemToiDa=" + diemToiDa +
                ", trongSo=" + trongSo +
                ", hoatDong=" + hoatDong +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TieuChiDanhGia that = (TieuChiDanhGia) o;
        return maTieuChi == that.maTieuChi;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maTieuChi);
    }
}