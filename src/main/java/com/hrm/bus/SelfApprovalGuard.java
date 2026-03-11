package com.hrm.bus;

import com.hrm.model.TaiKhoan;
import com.hrm.util.SessionContext;

final class SelfApprovalGuard {
    private SelfApprovalGuard() {
    }

    static boolean isSelfAction(String actorEmployeeId, String targetEmployeeId) {
        return actorEmployeeId != null
                && targetEmployeeId != null
                && actorEmployeeId.trim().equalsIgnoreCase(targetEmployeeId.trim());
    }

    static boolean currentUserCanBypassSelfRestriction() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return "admin".equalsIgnoreCase(currentUser.getTenDangNhap())
                || currentUser.coVaiTro("ADMIN")
                || currentUser.coVaiTro("TONG_GIAM_DOC")
                || currentUser.coVaiTro("GIAM_DOC");
    }
}
