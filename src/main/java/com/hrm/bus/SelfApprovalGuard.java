package com.hrm.bus;

import com.hrm.model.TaiKhoan;
import com.hrm.util.HRMConstants;
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
        return HRMConstants.USERNAME_ADMIN.equalsIgnoreCase(currentUser.getTenDangNhap())
                || currentUser.coVaiTro(HRMConstants.ROLE_ADMIN)
                || currentUser.coVaiTro("TONG_GIAM_DOC");
    }
}
