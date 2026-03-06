package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Model dai dien cho bang cham_congs (Cham cong hang ngay).
 *
 * Moi ban ghi luu lai:
 * - Nhan vien nao cham cong
 * - Ngay nao
 * - Gio vao / gio ra
 * - So gio lam thuc te / gio lam them
 * - Trang thai (dung gio, di muon, ve som...)
 * - Phuong thuc cham cong (wifi, van tay, thu cong...)
 *
 * RANG BUOC QUAN TRONG:
 * Moi nhan vien chi co DUY NHAT 1 ban ghi cham cong moi ngay.
 */
public class ChamCong {

    public enum TrangThai {
        DUNG_GIO("dung_gio", "Dung gio"),
        DI_MUON("di_muon", "Di muon"),
        VE_SOM("ve_som", "Ve som"),
        VANG_MAT("vang_mat", "Vang mat"),
        NGHI_PHEP("nghi_phep", "Nghi phep"),
        CONG_TAC("cong_tac", "Cong tac");

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
            throw new IllegalArgumentException("Trang thai cham cong khong hop le: " + value);
        }
    }

    public enum PhuongThuc {
        WIFI("wifi", "WiFi"),
        VAN_TAY("van_tay", "Van tay"),
        THE_TU("the_tu", "The tu"),
        GPS("gps", "GPS"),
        THU_CONG("thu_cong", "Thu cong");

        private final String dbValue;
        private final String displayName;

        PhuongThuc(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        public static PhuongThuc fromDbValue(String value) {
            for (PhuongThuc pt : values()) {
                if (pt.dbValue.equals(value)) {
                    return pt;
                }
            }
            throw new IllegalArgumentException("Phuong thuc cham cong khong hop le: " + value);
        }
    }

    private int id;                         // PRIMARY KEY — AUTO_INCREMENT
    private int nhanVienId;                 // FK -> nhan_viens — NOT NULL
    private String employeeName;            // TRANSIENT — Khong luu DB, chi de hien thi
    private LocalDate ngay;                 // NOT NULL — Ngay cham cong
    private String caLamId;                 // FK -> ca_lams — co the NULL
    private String tenCaLam;               // TRANSIENT — Ten ca de hien thi
    private LocalDateTime gioVao;           // Thoi diem check-in
    private LocalDateTime gioRa;            // Thoi diem check-out
    private double soGioLam;               // So gio lam thuc te — DECIMAL(4,2)
    private double gioLamThem;             // Gio OT — DECIMAL(4,2)
    private TrangThai trangThai;           // Trang thai cham cong
    private PhuongThuc phuongThucChamCong; // Cach cham cong
    private String ghiChu;                 // Ghi chu — co the NULL
    private LocalDateTime ngayTao;         // Thoi diem tao ban ghi

    public ChamCong() {
        this.soGioLam = 0;
        this.gioLamThem = 0;
        this.trangThai = TrangThai.DUNG_GIO;
        this.phuongThucChamCong = PhuongThuc.THU_CONG;
        this.ngayTao = LocalDateTime.now();
    }

    public ChamCong(int nhanVienId, LocalDate ngay, String caLamId) {
        this();
        this.nhanVienId = nhanVienId;
        this.ngay = ngay;
        this.caLamId = caLamId;
        this.gioVao = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Legacy compatibility
    public int getMaChamCong() { return id; }
    public void setMaChamCong(int maChamCong) { this.id = maChamCong; }

    public int getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(int nhanVienId) { this.nhanVienId = nhanVienId; }

    // Legacy compatibility
    public int getMaNV() { return nhanVienId; }
    public void setMaNV(int maNV) { this.nhanVienId = maNV; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public String getCaLamId() { return caLamId; }
    public void setCaLamId(String caLamId) { this.caLamId = caLamId; }

    // Legacy compatibility
    public String getMaCaLam() { return caLamId; }
    public void setMaCaLam(String maCaLam) { this.caLamId = maCaLam; }

    public String getTenCaLam() { return tenCaLam; }
    public void setTenCaLam(String tenCaLam) { this.tenCaLam = tenCaLam; }

    public LocalDateTime getGioVao() { return gioVao; }
    public void setGioVao(LocalDateTime gioVao) { this.gioVao = gioVao; }

    public LocalDateTime getGioRa() { return gioRa; }
    public void setGioRa(LocalDateTime gioRa) { this.gioRa = gioRa; }

    public double getSoGioLam() { return soGioLam; }
    public void setSoGioLam(double soGioLam) { this.soGioLam = soGioLam; }

    public double getGioLamThem() { return gioLamThem; }
    public void setGioLamThem(double gioLamThem) { this.gioLamThem = gioLamThem; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public PhuongThuc getPhuongThucChamCong() { return phuongThucChamCong; }
    public void setPhuongThucChamCong(PhuongThuc phuongThucChamCong) {
        this.phuongThucChamCong = phuongThucChamCong;
    }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    private static final String OT_FLAG = "OT";
    public boolean isLaOT() {
        return ghiChu != null && OT_FLAG.equals(ghiChu.trim());
    }
    public void setLaOT(boolean laOT) {
        if (laOT) {
            this.ghiChu = OT_FLAG;
        } else if (OT_FLAG.equals(this.ghiChu)) {
            this.ghiChu = null;
        }
    }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public double tinhSoGioLam() {
        if (gioVao == null || gioRa == null) {
            return 0;
        }
        long phut = Duration.between(gioVao, gioRa).toMinutes();
        return Math.round(phut / 60.0 * 100.0) / 100.0;
    }

    public boolean daCheckIn() { return gioVao != null; }
    public boolean daCheckOut() { return gioRa != null; }
    public boolean hoanTat() { return daCheckIn() && daCheckOut(); }

    @Override
    public String toString() {
        return "ChamCong{" +
                "nhanVienId=" + nhanVienId +
                ", ngay=" + ngay +
                ", trangThai=" + (trangThai != null ? trangThai.toString() : "N/A") +
                ", soGioLam=" + soGioLam +
                '}';
    }
}
