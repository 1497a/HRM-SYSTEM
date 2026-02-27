package com.hrm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lớp BoNhiem đại diện cho bảng BONHIEM .
 * Lưu thông tin bổ nhiệm / điều chuyển nhân viên: chức vụ, phòng ban,
 * loại bổ nhiệm, thời gian hiệu lực, trạng thái duyệt và các thông tin liên quan.
 */
public class BoNhiem implements Serializable {

    private static final long serialVersionUID = 1L;

    
    public enum LoaiBoNhiem {
        CHINH("chinh"),
        KIEM_NHIEM("kiem_nhiem");

        private final String dbValue;

        LoaiBoNhiem(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static LoaiBoNhiem fromDbValue(String dbValue) {
            if (dbValue == null) return CHINH;
            for (LoaiBoNhiem l : values()) {
                if (l.dbValue.equalsIgnoreCase(dbValue)) {
                    return l;
                }
            }
            return CHINH; // mặc định nếu không khớp
        }
    }

   
    public enum TrangThai {
        CHO_DUYET("cho_duyet", "Chờ duyệt"),
        HIEU_LUC("hieu_luc", "Hiệu lực"),
        HET_HIEU_LUC("het_hieu_luc", "Hết hiệu lực"),
        TU_CHOI("tu_choi", "Từ chối");

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

        public static TrangThai fromDbValue(String dbValue) {
            if (dbValue == null) return CHO_DUYET;
            for (TrangThai tt : values()) {
                if (tt.dbValue.equalsIgnoreCase(dbValue)) {
                    return tt;
                }
            }
            return CHO_DUYET; // mặc định nếu không khớp
        }
    }

    private int maBoNhiem;
    private int maNV;
    private String maPhongBan;
    private String maChucVu;
    private LoaiBoNhiem loaiBoNhiem;
    private double tyLeHuongLuong;
    private Integer maQuanLy;
    private Integer nguoiDuyet;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private LocalDateTime ngayPheDuyet;
    private String lyDo;
    private TrangThai trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Constructor mặc định
    public BoNhiem() {
        this.trangThai = TrangThai.CHO_DUYET;
        this.tyLeHuongLuong = 100.0;
        this.loaiBoNhiem = LoaiBoNhiem.CHINH;
    }

    
    public BoNhiem(int maNV, String maPhongBan, String maChucVu, LoaiBoNhiem loaiBoNhiem,
                   double tyLeHuongLuong, Integer maQuanLy, LocalDate tuNgay, LocalDate denNgay,
                   String lyDo) {
        this();
        this.maNV = maNV;
        this.maPhongBan = maPhongBan;
        this.maChucVu = maChucVu;
        this.loaiBoNhiem = loaiBoNhiem != null ? loaiBoNhiem : LoaiBoNhiem.CHINH;
        setTyLeHuongLuong(tyLeHuongLuong); 
        this.maQuanLy = maQuanLy;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.lyDo = lyDo;
    }

    // Constructor 
    public BoNhiem(int maBoNhiem, int maNV, String maPhongBan, String maChucVu,
                   LoaiBoNhiem loaiBoNhiem, double tyLeHuongLuong, Integer maQuanLy,
                   Integer nguoiDuyet, LocalDate tuNgay, LocalDate denNgay,
                   LocalDateTime ngayPheDuyet, String lyDo, TrangThai trangThai,
                   LocalDateTime ngayTao, LocalDateTime ngayCapNhat) {
        this.maBoNhiem = maBoNhiem;
        this.maNV = maNV;
        this.maPhongBan = maPhongBan;
        this.maChucVu = maChucVu;
        this.loaiBoNhiem = loaiBoNhiem;
        this.tyLeHuongLuong = tyLeHuongLuong;
        this.maQuanLy = maQuanLy;
        this.nguoiDuyet = nguoiDuyet;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.ngayPheDuyet = ngayPheDuyet;
        this.lyDo = lyDo;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    //  Getters & Setters 

    public int getMaBoNhiem() { return maBoNhiem; }
    public void setMaBoNhiem(int maBoNhiem) { this.maBoNhiem = maBoNhiem; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getMaPhongBan() { return maPhongBan; }
    public void setMaPhongBan(String maPhongBan) { this.maPhongBan = maPhongBan; }

    public String getMaChucVu() { return maChucVu; }
    public void setMaChucVu(String maChucVu) { this.maChucVu = maChucVu; }

    public LoaiBoNhiem getLoaiBoNhiem() { return loaiBoNhiem; }
    public void setLoaiBoNhiem(LoaiBoNhiem loaiBoNhiem) { this.loaiBoNhiem = loaiBoNhiem; }

    public double getTyLeHuongLuong() { return tyLeHuongLuong; }
    public void setTyLeHuongLuong(double tyLeHuongLuong) {
        if (tyLeHuongLuong < 0 || tyLeHuongLuong > 100) {
            throw new IllegalArgumentException("Tỷ lệ hưởng lương phải nằm trong khoảng 0 - 100%");
        }
        this.tyLeHuongLuong = tyLeHuongLuong;
    }

    public Integer getMaQuanLy() { return maQuanLy; }
    public void setMaQuanLy(Integer maQuanLy) { this.maQuanLy = maQuanLy; }

    public Integer getNguoiDuyet() { return nguoiDuyet; }
    public void setNguoiDuyet(Integer nguoiDuyet) { this.nguoiDuyet = nguoiDuyet; }

    public LocalDate getTuNgay() { return tuNgay; }
    public void setTuNgay(LocalDate tuNgay) {
        if (tuNgay == null) {
            throw new IllegalArgumentException("Ngày bắt đầu bổ nhiệm không được để trống");
        }
        this.tuNgay = tuNgay;
    }

    public LocalDate getDenNgay() { return denNgay; }
    public void setDenNgay(LocalDate denNgay) { this.denNgay = denNgay; }

    public LocalDateTime getNgayPheDuyet() { return ngayPheDuyet; }
    public void setNgayPheDuyet(LocalDateTime ngayPheDuyet) { this.ngayPheDuyet = ngayPheDuyet; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    // Equals & HashCode 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoNhiem boNhiem = (BoNhiem) o;
        return maBoNhiem == boNhiem.maBoNhiem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBoNhiem);
    }

    // toString 
    @Override
    public String toString() {
        return "BoNhiem{" +
                "maBoNhiem=" + maBoNhiem +
                ", maNV=" + maNV +
                ", phongBan='" + maPhongBan + '\'' +
                ", chucVu='" + maChucVu + '\'' +
                ", loaiBoNhiem=" + (loaiBoNhiem != null ? loaiBoNhiem.getDbValue() : "null") +
                ", tyLeHuongLuong=" + tyLeHuongLuong +
                ", tuNgay=" + tuNgay +
                ", denNgay=" + denNgay +
                ", trangThai=" + (trangThai != null ? trangThai.getDisplayName() : "null") +
                ", ngayPheDuyet=" + ngayPheDuyet +
                '}';
    }
}
