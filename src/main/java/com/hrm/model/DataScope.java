package com.hrm.model;

/**
 * Enum đại diện cho các mức độ phạm vi dữ liệu (Data Scope) trong hệ thống
 */
public enum DataScope {
    ALL,    // Toàn bộ dữ liệu
    DEPT,   // Dữ liệu trong phòng ban
    TEAM,   // Dữ liệu của cấp dưới trực tiếp (theo maQuanLy)
    SELF,   // Chỉ dữ liệu của bản thân
    NONE    // Không có quyền
}
