package com.hrm.util;

/**
 * Permission constants — single source of truth, must match maQuyen in DB.
 * Pattern: MODULE_ACTION  (no scope suffix — scope lives in VAITRO_QUYEN.phamVi)
 */
public final class QuyenConst {
    private QuyenConst() {}

    // Nhan vien
    public static final String EMPLOYEE_VIEW    = "EMPLOYEE_VIEW";
    public static final String EMPLOYEE_CREATE  = "EMPLOYEE_CREATE";
    public static final String EMPLOYEE_UPDATE  = "EMPLOYEE_UPDATE";
    public static final String EMPLOYEE_RESIGN  = "EMPLOYEE_RESIGN";  // cho nghi viec, khong xoa DB

    // To chuc
    public static final String DEPARTMENT_VIEW   = "DEPARTMENT_VIEW";
    public static final String DEPARTMENT_MANAGE = "DEPARTMENT_MANAGE";
    public static final String POSITION_VIEW     = "POSITION_VIEW";
    public static final String POSITION_MANAGE   = "POSITION_MANAGE";

    // Bo nhiem
    public static final String APPOINTMENT_VIEW    = "APPOINTMENT_VIEW";
    public static final String APPOINTMENT_CREATE  = "APPOINTMENT_CREATE";
    public static final String APPOINTMENT_APPROVE = "APPOINTMENT_APPROVE";

    // Cham cong
    public static final String ATTENDANCE_VIEW   = "ATTENDANCE_VIEW";
    public static final String ATTENDANCE_MANAGE = "ATTENDANCE_MANAGE";

    // Hop dong
    public static final String CONTRACT_VIEW   = "CONTRACT_VIEW";
    public static final String CONTRACT_CREATE = "CONTRACT_CREATE";
    public static final String CONTRACT_UPDATE = "CONTRACT_UPDATE";
    public static final String CONTRACT_MANAGE = "CONTRACT_MANAGE";

    // Luong
    public static final String PAYROLL_VIEW      = "PAYROLL_VIEW";
    public static final String PAYROLL_CALCULATE = "PAYROLL_CALCULATE";

    // Nghi phep
    public static final String LEAVE_VIEW    = "LEAVE_VIEW";
    public static final String LEAVE_CREATE  = "LEAVE_CREATE";
    public static final String LEAVE_MANAGE  = "LEAVE_MANAGE";
    public static final String LEAVE_APPROVE = "LEAVE_APPROVE";

    // Danh gia
    public static final String EVAL_VIEW   = "EVAL_VIEW";
    public static final String EVAL_MANAGE = "EVAL_MANAGE";
    public static final String EVAL_REVIEW = "EVAL_REVIEW";

    // Tuyen dung
    public static final String RECRUITMENT_VIEW    = "RECRUITMENT_VIEW";
    public static final String RECRUITMENT_REQUEST = "RECRUITMENT_REQUEST"; // truong phong/quan ly gui yeu cau
    public static final String RECRUITMENT_MANAGE  = "RECRUITMENT_MANAGE";  // nhan su xu ly toan bo quy trinh

    // Bao cao & Thong bao
    public static final String REPORT_VIEW       = "REPORT_VIEW";
    public static final String REPORT_EXPORT     = "REPORT_EXPORT";
    public static final String NOTIFICATION_SEND = "NOTIFICATION_SEND";

    // Quan tri he thong
    public static final String USER_VIEW     = "USER_VIEW";
    public static final String USER_CREATE   = "USER_CREATE";
    public static final String USER_UPDATE   = "USER_UPDATE";
    public static final String USER_DELETE   = "USER_DELETE";
    public static final String ROLE_VIEW     = "ROLE_VIEW";
    public static final String ROLE_CREATE   = "ROLE_CREATE";
    public static final String ROLE_UPDATE   = "ROLE_UPDATE";
    public static final String ROLE_DELETE   = "ROLE_DELETE";
    public static final String SETTINGS_VIEW   = "SETTINGS_VIEW";
    public static final String SETTINGS_UPDATE = "SETTINGS_UPDATE";
}
