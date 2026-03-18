package com.hrm.util;

/**
 * Centralized constants shared across the HRM application.
 */
public final class HRMConstants {

    private HRMConstants() {}

    // Common
    public static final String ALL = "Tất cả";
    // Auth and system identities
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYEE = "NHAN_VIEN";
    public static final String ROLE_TONG_GIAM_DOC = "TONG_GIAM_DOC";
    public static final String USERNAME_ADMIN = "admin";
    // Account status
    public static final String TK_ACTIVE = "Đang hoạt động";
    public static final String TK_LOCKED = "Bị khóa";
    // Shared DB statuses
    public static final String TRANG_THAI_CHO_DUYET = "cho_duyet";
    public static final String TRANG_THAI_HIEU_LUC = "hieu_luc";
    public static final String TRANG_THAI_DA_DUYET = "da_duyet";
    public static final String TRANG_THAI_TU_CHOI = "tu_choi";
    public static final String TRANG_THAI_HUY = "huy";
    public static final String TRANG_THAI_THANH_LY = "thanh_ly";
    public static final String TRANG_THAI_DANG_LAM_VIEC = "dang_lam_viec";
    public static final String TRANG_THAI_HOAT_DONG = "hoatDong";
    public static final String TRANG_THAI_NGUNG_HOAT_DONG = "ngung_hoat_dong";
    // Appointment status
    public static final String BN_CHO_DUYET = "Chờ duyệt";
    public static final String BN_HIEU_LUC = "Hiệu lực";
    public static final String BN_KET_THUC = "Kết thúc";
    public static final String BN_TU_CHOI = "Từ chối";
    // Recruitment request status
    public static final String YC_CHO_DUYET = "Chờ duyệt";
    public static final String YC_DA_DUYET = "Đã duyệt";
    public static final String YC_TU_CHOI = "Từ chối";
    public static final String YC_DA_TUYEN = "Đã tuyển";
    // Recruitment posting status
    public static final String TIN_NHAP = "Nhập";
    public static final String TIN_DANG_TUYEN = "Đang tuyển";
    public static final String TIN_DONG = "Đóng";
    // Candidate status
    public static final String UV_NOP_DON = "Nộp đơn";
    public static final String UV_XEM_XET = "Đang xem xét";
    public static final String UV_PHONG_VAN = "Phỏng vấn";
    public static final String UV_TRUNG_TUYEN = "Trúng tuyển";
    public static final String UV_LOAI = "Loại";
}
