package com.hrm.util;

/**
 * Constants for all permission codes used in the system.
 * These must match the maQuyen values in the QUYEN table.
 */
public final class PermissionCodes {
    private PermissionCodes() {}

    // Nhan vien
    public static final String EMPLOYEE_VIEW          = "EMPLOYEE_VIEW";
    public static final String EMPLOYEE_CREATE        = "EMPLOYEE_CREATE";
    public static final String EMPLOYEE_UPDATE        = "EMPLOYEE_UPDATE";
    public static final String EMPLOYEE_STATUS_UPDATE = "EMPLOYEE_STATUS_UPDATE";

    // To chuc
    public static final String DEPARTMENT_VIEW        = "DEPARTMENT_VIEW";
    public static final String DEPARTMENT_MANAGE      = "DEPARTMENT_MANAGE";
    public static final String POSITION_VIEW          = "POSITION_VIEW";
    public static final String POSITION_MANAGE        = "POSITION_MANAGE";

    // Bo nhiem
    public static final String APPOINTMENT_VIEW       = "APPOINTMENT_VIEW";
    public static final String APPOINTMENT_CREATE     = "APPOINTMENT_CREATE";
    public static final String APPOINTMENT_APPROVE    = "APPOINTMENT_APPROVE";

    // Cham cong
    public static final String ATTENDANCE_VIEW        = "ATTENDANCE_VIEW";
    public static final String ATTENDANCE_CHECKIN     = "ATTENDANCE_CHECKIN";
    public static final String ATTENDANCE_MANAGE      = "ATTENDANCE_MANAGE";
    public static final String ALLOWANCE_MANAGE       = "ALLOWANCE_MANAGE";
    public static final String OVERTIME_REQUEST       = "OVERTIME_REQUEST";
    public static final String OVERTIME_APPROVE       = "OVERTIME_APPROVE";

    // Hop dong
    public static final String CONTRACT_VIEW          = "CONTRACT_VIEW";
    public static final String CONTRACT_CREATE        = "CONTRACT_CREATE";
    public static final String CONTRACT_APPROVE       = "CONTRACT_APPROVE";
    public static final String CONTRACT_MANAGE        = "CONTRACT_MANAGE";

    // Luong
    public static final String PAYROLL_VIEW           = "PAYROLL_VIEW";
    public static final String PAYROLL_CALCULATE      = "PAYROLL_CALCULATE";
    public static final String PAYROLL_LOCK           = "PAYROLL_LOCK";

    // Nghi phep
    public static final String LEAVE_VIEW             = "LEAVE_VIEW";
    public static final String LEAVE_CREATE           = "LEAVE_CREATE";
    public static final String LEAVE_APPROVE          = "LEAVE_APPROVE";
    public static final String LEAVE_MANAGE           = "LEAVE_MANAGE";

    // Danh gia
    public static final String EVAL_VIEW              = "EVAL_VIEW";
    public static final String EVAL_MANAGE            = "EVAL_MANAGE";
    public static final String EVAL_REVIEW            = "EVAL_REVIEW";

    // Tuyen dung
    public static final String RECRUITMENT_VIEW       = "RECRUITMENT_VIEW";
    public static final String RECRUITMENT_REQUEST    = "RECRUITMENT_REQUEST";
    public static final String RECRUITMENT_MANAGE     = "RECRUITMENT_MANAGE";

    // Bao cao & thong bao
    public static final String REPORT_VIEW            = "REPORT_VIEW";
    public static final String NOTIFICATION_SEND      = "NOTIFICATION_SEND";

    // Tai khoan & vai tro
    public static final String USER_VIEW              = "USER_VIEW";
    public static final String USER_CREATE            = "USER_CREATE";
    public static final String USER_UPDATE            = "USER_UPDATE";
    public static final String USER_DELETE            = "USER_DELETE";
    public static final String ROLE_VIEW              = "ROLE_VIEW";
    public static final String ROLE_CREATE            = "ROLE_CREATE";
    public static final String ROLE_UPDATE            = "ROLE_UPDATE";
    public static final String ROLE_DELETE            = "ROLE_DELETE";
    public static final String CHANGE_PASSWORD        = "CHANGE_PASSWORD";
}
