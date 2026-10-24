package com.hrm.model;

public final class RecruitmentStatus {

    private RecruitmentStatus() {
    }

    public static final class YeuCau {
        public static final String CHO_DUYET = "cho_duyet";
        public static final String DA_DUYET = "da_duyet";
        public static final String TU_CHOI = "tu_choi";
        public static final String DA_TUYEN_DU = "da_tuyen_du";
        private YeuCau() {
        }
    }

    public static final class Tin {
        public static final String DANG_TUYEN = "dang_tuyen";
        public static final String TAM_DUNG = "tam_dung";
        public static final String DA_DONG = "da_dong";
        private Tin() {
        }
    }

    public static final class UngVien {
        public static final String MOI = "moi";
        public static final String DANG_PHONG_VAN = "dang_phong_van";
        public static final String TRUNG_TUYEN = "trung_tuyen";
        public static final String TU_CHOI = "tu_choi";
        public static final String DA_CHUYEN_NHAN_VIEN = "da_chuyen_nhan_vien";
        private UngVien() {
        }
    }

    public static boolean isUngVienStatusEditable(String status) {
        return UngVien.MOI.equals(status)
                || UngVien.DANG_PHONG_VAN.equals(status)
                || UngVien.TRUNG_TUYEN.equals(status)
                || UngVien.TU_CHOI.equals(status);
    }

    public static String displayYeuCau(String status) {
        if (status == null) return "";
        switch (status) {
            case YeuCau.CHO_DUYET:
                return "Chờ duyệt";
            case YeuCau.DA_DUYET:
                return "Đã duyệt";
            case YeuCau.TU_CHOI:
                return "Từ chối";
            case YeuCau.DA_TUYEN_DU:
                return "Đã tuyển đủ";
            default:
                return status;
        }
    }

    public static String displayTin(String status) {
        if (status == null) return "";
        switch (status) {
            case Tin.DANG_TUYEN:
                return "Đang tuyển";
            case Tin.TAM_DUNG:
                return "Tạm dừng";
            case Tin.DA_DONG:
                return "Đã đóng";
            default:
                return status;
        }
    }

    public static String displayUngVien(String status, String maNV) {
        if (maNV != null && !maNV.isEmpty()) {
            return "Đã chuyển thành nhân viên";
        }
        if (status == null) return "";
        switch (status) {
            case UngVien.MOI:
                return "Mới";
            case UngVien.DANG_PHONG_VAN:
                return "Đang phỏng vấn";
            case UngVien.TRUNG_TUYEN:
                return "Trúng Tuyển";
            case UngVien.DA_CHUYEN_NHAN_VIEN:
                return "Đã chuyển thành nhân viên";
            case UngVien.TU_CHOI:
                return "Từ chối";
            default:
                return status;
        }
    }
}
