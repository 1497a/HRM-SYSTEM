package com.hrm.util;

/**
 * Centralized constants for status strings used across filters, tables, and combo boxes.
 */
public final class HRMConstants {

    private HRMConstants() {}

    // ── Common ────────────────────────────────────────────────────────────────
    public static final String ALL = "Tất cả";

    // ── Account (TaiKhoan) status ─────────────────────────────────────────────
    public static final String TK_ACTIVE = "Đang hoạt động";
    public static final String TK_LOCKED = "Bị khóa";

    // ── Appointment (BoNhiem) status ──────────────────────────────────────────
    public static final String BN_CHO_DUYET  = "Chờ duyệt";
    public static final String BN_HIEU_LUC   = "Hiệu lực";
    public static final String BN_KET_THUC   = "Kết thúc";
    public static final String BN_TU_CHOI    = "Từ chối";

    // ── Recruitment – YeuCauTuyenDung status ──────────────────────────────────
    public static final String YC_CHO_DUYET  = "Chờ duyệt";
    public static final String YC_DA_DUYET   = "Đã duyệt";
    public static final String YC_TU_CHOI    = "Từ chối";
    public static final String YC_DA_TUYEN   = "Đã tuyển";

    // ── Recruitment – TinTuyenDung status ────────────────────────────────────
    public static final String TIN_NHAP       = "Nháp";
    public static final String TIN_DANG_TUYEN = "Đang tuyển";
    public static final String TIN_DONG       = "Đóng";

    // ── Recruitment – UngVien status ──────────────────────────────────────────
    public static final String UV_NOP_DON     = "Nộp đơn";
    public static final String UV_XEM_XET     = "Đang xem xét";
    public static final String UV_PHONG_VAN   = "Phỏng vấn";
    public static final String UV_TRUNG_TUYEN = "Trúng tuyển";
    public static final String UV_LOAI        = "Loại";
}
