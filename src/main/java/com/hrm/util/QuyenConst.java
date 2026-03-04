package com.hrm.util;

/**
 * Permission constants — single source of truth, must match maQuyen in DB.
 * Pattern: MODULE_ACTION_SCOPE
 * Scope: ALL | DEPT | TEAM | SELF (omitted for non-scoped actions)
 */
public final class QuyenConst {
    private QuyenConst() {}

    // Nhan vien
    public static final String EMPLOYEE_VIEW_ALL   = "EMPLOYEE_VIEW_ALL";
    public static final String EMPLOYEE_VIEW_DEPT  = "EMPLOYEE_VIEW_DEPT";
    public static final String EMPLOYEE_VIEW_TEAM  = "EMPLOYEE_VIEW_TEAM";
    public static final String EMPLOYEE_VIEW_SELF  = "EMPLOYEE_VIEW_SELF";
    public static final String EMPLOYEE_CREATE     = "EMPLOYEE_CREATE";
    public static final String EMPLOYEE_UPDATE     = "EMPLOYEE_UPDATE";
    public static final String EMPLOYEE_DELETE     = "EMPLOYEE_DELETE";

    // To chuc
    public static final String DEPARTMENT_VIEW     = "DEPARTMENT_VIEW";
    public static final String DEPARTMENT_MANAGE   = "DEPARTMENT_MANAGE";
    public static final String POSITION_VIEW       = "POSITION_VIEW";
    public static final String POSITION_MANAGE     = "POSITION_MANAGE";

    // Bo nhiem
    public static final String APPOINTMENT_VIEW_ALL  = "APPOINTMENT_VIEW_ALL";
    public static final String APPOINTMENT_VIEW_DEPT = "APPOINTMENT_VIEW_DEPT";
    public static final String APPOINTMENT_VIEW_TEAM = "APPOINTMENT_VIEW_TEAM";
    public static final String APPOINTMENT_VIEW_SELF = "APPOINTMENT_VIEW_SELF";
    public static final String APPOINTMENT_CREATE    = "APPOINTMENT_CREATE";
    public static final String APPOINTMENT_APPROVE   = "APPOINTMENT_APPROVE";

    // Cham cong
    public static final String ATTENDANCE_VIEW_ALL   = "ATTENDANCE_VIEW_ALL";
    public static final String ATTENDANCE_VIEW_DEPT  = "ATTENDANCE_VIEW_DEPT";
    public static final String ATTENDANCE_VIEW_TEAM  = "ATTENDANCE_VIEW_TEAM";
    public static final String ATTENDANCE_VIEW_SELF  = "ATTENDANCE_VIEW_SELF";
    public static final String ATTENDANCE_MANAGE     = "ATTENDANCE_MANAGE";

    // Hop dong
    public static final String CONTRACT_VIEW_ALL   = "CONTRACT_VIEW_ALL";
    public static final String CONTRACT_VIEW_DEPT  = "CONTRACT_VIEW_DEPT";
    public static final String CONTRACT_VIEW_TEAM  = "CONTRACT_VIEW_TEAM";
    public static final String CONTRACT_VIEW_SELF  = "CONTRACT_VIEW_SELF";
    public static final String CONTRACT_CREATE     = "CONTRACT_CREATE";
    public static final String CONTRACT_UPDATE     = "CONTRACT_UPDATE";
    public static final String CONTRACT_MANAGE     = "CONTRACT_MANAGE";

    // Luong
    public static final String PAYROLL_VIEW_ALL    = "PAYROLL_VIEW_ALL";
    public static final String PAYROLL_VIEW_DEPT   = "PAYROLL_VIEW_DEPT";
    public static final String PAYROLL_VIEW_TEAM   = "PAYROLL_VIEW_TEAM";
    public static final String PAYROLL_VIEW_SELF   = "PAYROLL_VIEW_SELF";
    public static final String PAYROLL_CALCULATE   = "PAYROLL_CALCULATE";

    // Nghi phep
    public static final String LEAVE_VIEW_ALL      = "LEAVE_VIEW_ALL";
    public static final String LEAVE_VIEW_DEPT     = "LEAVE_VIEW_DEPT";
    public static final String LEAVE_VIEW_TEAM     = "LEAVE_VIEW_TEAM";
    public static final String LEAVE_VIEW_SELF     = "LEAVE_VIEW_SELF";
    public static final String LEAVE_CREATE        = "LEAVE_CREATE";
    public static final String LEAVE_MANAGE        = "LEAVE_MANAGE";
    public static final String LEAVE_APPROVE_ALL   = "LEAVE_APPROVE_ALL";
    public static final String LEAVE_APPROVE_DEPT  = "LEAVE_APPROVE_DEPT";
    public static final String LEAVE_APPROVE_TEAM  = "LEAVE_APPROVE_TEAM";

    // Danh gia
    public static final String EVAL_VIEW_ALL       = "EVAL_VIEW_ALL";
    public static final String EVAL_VIEW_DEPT      = "EVAL_VIEW_DEPT";
    public static final String EVAL_VIEW_TEAM      = "EVAL_VIEW_TEAM";
    public static final String EVAL_VIEW_SELF      = "EVAL_VIEW_SELF";
    public static final String EVAL_MANAGE         = "EVAL_MANAGE";
    public static final String EVAL_REVIEW_ALL     = "EVAL_REVIEW_ALL";
    public static final String EVAL_REVIEW_DEPT    = "EVAL_REVIEW_DEPT";
    public static final String EVAL_REVIEW_TEAM    = "EVAL_REVIEW_TEAM";

    // Tuyen dung
    public static final String RECRUITMENT_VIEW_ALL   = "RECRUITMENT_VIEW_ALL";
    public static final String RECRUITMENT_VIEW_DEPT  = "RECRUITMENT_VIEW_DEPT";
    public static final String RECRUITMENT_VIEW_TEAM  = "RECRUITMENT_VIEW_TEAM";
    public static final String RECRUITMENT_VIEW_SELF  = "RECRUITMENT_VIEW_SELF";
    public static final String RECRUITMENT_MANAGE     = "RECRUITMENT_MANAGE";

    // Bao cao & Thong bao
    public static final String REPORT_VIEW         = "REPORT_VIEW";
    public static final String REPORT_EXPORT       = "REPORT_EXPORT";
    public static final String NOTIFICATION_SEND   = "NOTIFICATION_SEND";

    // Quan tri he thong
    public static final String USER_VIEW           = "USER_VIEW";
    public static final String USER_CREATE         = "USER_CREATE";
    public static final String USER_UPDATE         = "USER_UPDATE";
    public static final String USER_DELETE         = "USER_DELETE";
    public static final String ROLE_VIEW           = "ROLE_VIEW";
    public static final String ROLE_CREATE         = "ROLE_CREATE";
    public static final String ROLE_UPDATE         = "ROLE_UPDATE";
    public static final String ROLE_DELETE         = "ROLE_DELETE";
    public static final String SETTINGS_VIEW       = "SETTINGS_VIEW";
    public static final String SETTINGS_UPDATE     = "SETTINGS_UPDATE";
}
