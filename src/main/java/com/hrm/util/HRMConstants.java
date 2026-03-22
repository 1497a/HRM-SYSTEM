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
    // Trạng thái nhân viên
    public static final String TRANG_THAI_DANG_LAM_VIEC  = "dang_lam_viec";
    public static final String TRANG_THAI_TAM_NGHI        = "tam_nghi";
    public static final String TRANG_THAI_NGHI_VIEC       = "nghi_viec";
    // Trạng thái phòng ban / chức vụ
    public static final String TRANG_THAI_HOAT_DONG       = "hoatDong";
    public static final String TRANG_THAI_NGUNG_HOAT_DONG = "ngung_hoat_dong";
    // Trạng thái chung (bổ nhiệm, đơn nghỉ phép, yêu cầu tuyển dụng...)
    public static final String TRANG_THAI_CHO_DUYET       = "cho_duyet";
    public static final String TRANG_THAI_DA_DUYET        = "da_duyet";
    public static final String TRANG_THAI_TU_CHOI         = "tu_choi";
    public static final String TRANG_THAI_HUY             = "huy";
    // Trạng thái bổ nhiệm / hợp đồng
    public static final String TRANG_THAI_HIEU_LUC        = "hieu_luc";
    public static final String TRANG_THAI_HET_HIEU_LUC    = "het_hieu_luc";
    public static final String TRANG_THAI_SAP_HET_HAN     = "sap_het_han";
    public static final String TRANG_THAI_HET_HAN         = "het_han";
    public static final String TRANG_THAI_THANH_LY        = "thanh_ly";
    // Loại bổ nhiệm
    public static final String LOAI_BO_NHIEM_CHINH        = "chinh";
    public static final String LOAI_BO_NHIEM_KIEM_NHIEM   = "kiem_nhiem";
    // Loại hợp đồng
    public static final String LOAI_HOP_DONG_THU_VIEC         = "thu_viec";
    public static final String LOAI_HOP_DONG_XAC_DINH         = "xac_dinh_thoi_han";
    public static final String LOAI_HOP_DONG_KHONG_XAC_DINH   = "khong_xac_dinh";
    public static final String LOAI_PHEP_NAM                  = "PHEP_NAM";
    // Loại thông báo
    public static final String LOAI_TB_HE_THONG          = "he_thong";
    public static final String LOAI_TB_DON_TU            = "don_tu";
    public static final String LOAI_TB_CHUNG             = "thong_bao_chung";
    public static final String PHUONG_THUC_CHAM_CONG_THU_CONG = "thu_cong";
    // Giới tính
    public static final String GIOI_TINH_NAM  = "nam";
    public static final String GIOI_TINH_NU   = "nu";
    public static final String GIOI_TINH_KHAC = "khac";
    // Tình trạng hôn nhân
    public static final String HON_NHAN_DOC_THAN   = "doc_than";
    public static final String HON_NHAN_DA_KET_HON = "da_ket_hon";
    public static final String HON_NHAN_LY_HON     = "ly_hon";
    // Trạng thái bảng lương
    public static final String TRANG_THAI_DANG_XU_LY = "dang_xu_ly";
    public static final String TRANG_THAI_DA_KHOA     = "da_khoa";
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

    /**
     * Chuyển raw DB status value sang chuỗi tiếng Việt hiển thị cho người dùng.
     * Áp dụng cho mọi trạng thái dạng snake_case được khai báo là hằng trong class này.
     */
    public static String display(String raw) {
        if (raw == null) return "";
        switch (raw) {
            // Nhân viên
            case TRANG_THAI_DANG_LAM_VIEC:  return "Đang làm việc";
            case TRANG_THAI_TAM_NGHI:       return "Tạm nghỉ";
            case TRANG_THAI_NGHI_VIEC:      return "Nghỉ việc";
            // Phòng ban / Chức vụ
            case TRANG_THAI_HOAT_DONG:      return "Hoạt động";
            case TRANG_THAI_NGUNG_HOAT_DONG:return "Ngừng hoạt động";
            // Chung
            case TRANG_THAI_CHO_DUYET:      return "Chờ duyệt";
            case TRANG_THAI_DA_DUYET:       return "Đã duyệt";
            case TRANG_THAI_TU_CHOI:        return "Từ chối";
            case TRANG_THAI_HUY:            return "Đã hủy";
            // Bổ nhiệm / Hợp đồng
            case TRANG_THAI_HIEU_LUC:       return "Hiệu lực";
            case TRANG_THAI_HET_HIEU_LUC:   return "Hết hiệu lực";
            case TRANG_THAI_SAP_HET_HAN:    return "Sắp hết hạn";
            case TRANG_THAI_HET_HAN:        return "Hết hạn";
            case TRANG_THAI_THANH_LY:       return "Thanh lý";
            // Loại bổ nhiệm
            case LOAI_BO_NHIEM_CHINH:       return "Chính";
            case LOAI_BO_NHIEM_KIEM_NHIEM:  return "Kiêm nhiệm";
            // Loại hợp đồng
            case LOAI_HOP_DONG_THU_VIEC:        return "Thử việc";
            case LOAI_HOP_DONG_XAC_DINH:        return "Xác định thời hạn";
            case LOAI_HOP_DONG_KHONG_XAC_DINH:  return "Không xác định";
            // Loại thông báo
            case LOAI_TB_HE_THONG:  return "Hệ thống";
            case LOAI_TB_DON_TU:    return "Đơn từ";
            case LOAI_TB_CHUNG:     return "Thông báo chung";
            // Giới tính
            case GIOI_TINH_NAM:     return "Nam";
            case GIOI_TINH_NU:      return "Nữ";
            case GIOI_TINH_KHAC:    return "Khác";
            // Tình trạng hôn nhân
            case HON_NHAN_DOC_THAN:     return "Độc thân";
            case HON_NHAN_DA_KET_HON:   return "Đã kết hôn";
            case HON_NHAN_LY_HON:       return "Ly hôn";
            // Bảng lương
            case TRANG_THAI_DANG_XU_LY: return "Đang xử lý";
            case TRANG_THAI_DA_KHOA:    return "Đã khóa";
            default:                return raw;
        }
    }
}
