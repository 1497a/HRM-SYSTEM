package com.hrm.util;

import com.hrm.model.TaiKhoan;

import static com.hrm.util.HRMConstants.ROLE_ADMIN;
import static com.hrm.util.HRMConstants.USERNAME_ADMIN;

/**
 * Session Context - Manages current user session
 * Singleton pattern
 */
public class SessionContext {
    private static SessionContext instance;
    private TaiKhoan currentUser;
    private SessionContext() {
    }

    public static synchronized SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public TaiKhoan getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(TaiKhoan user) {
        this.currentUser = user;
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasPermission(String permissionCode) {
        return currentUser != null && currentUser.coQuyen(permissionCode);
    }

    public boolean hasRole(String roleCode) {
        return currentUser != null && currentUser.coVaiTro(roleCode);
    }

    public boolean isAdmin() {
        return isLoggedIn()
                && (USERNAME_ADMIN.equalsIgnoreCase(currentUser.getTenDangNhap()) || hasRole(ROLE_ADMIN));
    }
}
