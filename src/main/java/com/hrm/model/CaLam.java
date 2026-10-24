package com.hrm.model;

import com.hrm.util.HRMConstants;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Model dai dien cho bang ca_lams (Ca lam viec).
 *
 * Moi ca lam viec dinh nghia:
 * - Thoi gian bat dau / ket thuc
 * - So gio chuan (thuong la 8 gio)
 * - Co cho phep lam them gio hay khong
 *
 * Du lieu mau trong DB:
 *   HANH_CHINH  -> 08:00 - 17:00
 *   CA_SANG     -> 06:00 - 14:00
 *   CA_CHIEU    -> 14:00 - 22:00
 *   CA_DEM      -> 22:00 - 06:00
 */
public class CaLam {

    public enum TrangThai {
        HOAT_DONG(HRMConstants.TRANG_THAI_HOAT_DONG, "Hoat dong"),
        NGUNG_HOAT_DONG(HRMConstants.TRANG_THAI_NGUNG_HOAT_DONG, "Ngung hoat dong");
        private final String dbValue;
        private final String displayName;
        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }
        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }
        public static TrangThai fromDbValue(String value) {
            for (TrangThai tt : values()) {
                if (tt.dbValue.equals(value)) {
                    return tt;
                }
            }
            throw new IllegalArgumentException("Trang thai ca lam khong hop le: " + value);
        }
    }

    private String id;               // PRIMARY KEY — VARCHAR(20)
    private String tenCaLam;         // NOT NULL    — NVARCHAR(100)
    private LocalTime gioBatDau;     // NOT NULL    — TIME
    private LocalTime gioKetThuc;    // NOT NULL    — TIME
    private double soGioChuan;       // DEFAULT 8.00 — DECIMAL(4,2)
    private boolean choPhepLamThem;  // DEFAULT TRUE — BOOLEAN
    private String moTa;             // NULL OK     — NVARCHAR(255)
    private TrangThai trangThai;     // DEFAULT 'hoat_dong' — ENUM
    private LocalDateTime ngayTao;   // AUTO        — DATETIME
    public CaLam() {
        this.soGioChuan = 8.00;
        this.choPhepLamThem = true;
        this.trangThai = TrangThai.HOAT_DONG;
        this.ngayTao = LocalDateTime.now();
    }

    public CaLam(String id, String tenCaLam, LocalTime gioBatDau, LocalTime gioKetThuc) {
        this();
        this.id = id;
        this.tenCaLam = tenCaLam;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenCaLam() { return tenCaLam; }
    public void setTenCaLam(String tenCaLam) { this.tenCaLam = tenCaLam; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public double getSoGioChuan() { return soGioChuan; }
    public void setSoGioChuan(double soGioChuan) { this.soGioChuan = soGioChuan; }

    public boolean isChoPhepLamThem() { return choPhepLamThem; }
    public void setChoPhepLamThem(boolean choPhepLamThem) { this.choPhepLamThem = choPhepLamThem; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public boolean laCaDem() {
        return gioKetThuc.isBefore(gioBatDau);
    }

    public boolean conHoatDong() {
        return trangThai == TrangThai.HOAT_DONG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaLam)) return false;
        return java.util.Objects.equals(id, ((CaLam) o).id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return tenCaLam + " (" + gioBatDau + " - " + gioKetThuc + ")";
    }
}
