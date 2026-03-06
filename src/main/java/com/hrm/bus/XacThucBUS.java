package com.hrm.bus;

import com.hrm.model.Quyen;
import com.hrm.model.VaiTro;
import com.hrm.model.TaiKhoan;
import com.hrm.dao.TaiKhoanDAO;
import com.hrm.util.PasswordUtil;
import com.hrm.util.SessionContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Authentication Service - xÃ¡Â»Â­ lÃƒÂ½ Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p, Ã„â€˜Ã„Æ’ng xuÃ¡ÂºÂ¥t vÃƒÂ  quÃ¡ÂºÂ£n lÃƒÂ½ tÃƒÂ i khoÃ¡ÂºÂ£n.
 * SÃ¡Â»Â­ dÃ¡Â»Â¥ng TaiKhoanDAO Ã„â€˜Ã¡Â»Æ’ truy xuÃ¡ÂºÂ¥t cÃ†Â¡ sÃ¡Â»Å¸ dÃ¡Â»Â¯ liÃ¡Â»â€¡u thÃ¡Â»Â±c.
 */
public class XacThucBUS {

    private static XacThucBUS instance;
    private final TaiKhoanDAO taiKhoanRepo;

    private XacThucBUS() {
        this.taiKhoanRepo = new TaiKhoanDAO();
    }

    public static synchronized XacThucBUS getInstance() {
        if (instance == null) {
            instance = new XacThucBUS();
        }
        return instance;
    }

    // =====================================================================
    // ==================== Authentication =================================
    // =====================================================================

    /**
     * XÃƒÂ¡c thÃ¡Â»Â±c ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng bÃ¡ÂºÂ±ng tÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p vÃƒÂ  mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u.
     * KiÃ¡Â»Æ’m tra: tÃƒÂ i khoÃ¡ÂºÂ£n tÃ¡Â»â€œn tÃ¡ÂºÂ¡i, Ã„â€˜ang hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng, khÃƒÂ´ng bÃ¡Â»â€¹ khÃƒÂ³a, mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u Ã„â€˜ÃƒÂºng.
     *
     * @param username tÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p
     * @param password mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u dÃ¡ÂºÂ¡ng plaintext
     * @return TaiKhoan nÃ¡ÂºÂ¿u thÃƒÂ nh cÃƒÂ´ng, null nÃ¡ÂºÂ¿u thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i
     */
    public TaiKhoan authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (password == null || password.isEmpty()) {
            return null;
        }

        TaiKhoan user = taiKhoanRepo.findByUsername(username.trim());

        if (user == null) {
            return null;
        }

        if (!user.isHoatDong()) {
            return null;
        }

        if (user.isBiKhoa()) {
            return null;
        }

        if (!PasswordUtil.verifyPassword(password, user.getMatKhau())) {
            return null;
        }

        SessionContext.getInstance().setCurrentUser(user);
        return user;
    }

    /**
     * Ã„ÂÃ„Æ’ng xuÃ¡ÂºÂ¥t ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i.
     */
    public void logout() {
        SessionContext.getInstance().clearSession();
    }

    /**
     * KiÃ¡Â»Æ’m tra cÃƒÂ³ ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng Ã„â€˜ang Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p khÃƒÂ´ng.
     */
    public boolean isLoggedIn() {
        return SessionContext.getInstance().isLoggedIn();
    }

    /**
     * LÃ¡ÂºÂ¥y ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng Ã„â€˜ang Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p.
     */
    public TaiKhoan getCurrentUser() {
        return SessionContext.getInstance().getCurrentUser();
    }

    /**
     * KiÃ¡Â»Æ’m tra ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i cÃƒÂ³ quyÃ¡Â»Ân chÃ¡Â»â€° Ã„â€˜Ã¡Â»â€¹nh khÃƒÂ´ng.
     */
    public boolean hasPermission(String permissionCode) {
        TaiKhoan user = getCurrentUser();
        return user != null && user.coQuyen(permissionCode);
    }

    private boolean isBuiltInAdminAccount(TaiKhoan user) {
        if (user == null) return false;
        return "admin".equalsIgnoreCase(user.getTenDangNhap());
    }

    private boolean isCurrentAdminSelfUpdate(TaiKhoan target) {
        if (target == null) return false;
        TaiKhoan current = SessionContext.getInstance().getCurrentUser();
        if (current == null || current.getId() != target.getId()) return false;
        return current.coVaiTro("ADMIN");
    }

    /**
     * LÃ¡ÂºÂ¥y phÃ¡ÂºÂ¡m vi dÃ¡Â»Â¯ liÃ¡Â»â€¡u cho phÃƒÂ©p dÃ¡Â»Â±a trÃƒÂªn cÃ¡Â»Â¥m tÃ¡Â»Â« khoÃƒÂ¡ quyÃ¡Â»Ân (actionPrefix).
     * VÃƒÂ­ dÃ¡Â»Â¥ actionPrefix = "LEAVE_VIEW", hÃƒÂ m sÃ¡ÂºÂ½ kiÃ¡Â»Æ’m tra lÃ¡ÂºÂ§n lÃ†Â°Ã¡Â»Â£t:
     * LEAVE_VIEW_ALL, LEAVE_VIEW_DEPT, LEAVE_VIEW_TEAM, LEAVE_VIEW_SELF
     * @param actionPrefix TiÃ¡Â»Ân tÃ¡Â»â€˜ cÃ¡Â»Â§a quyÃ¡Â»Ân cÃ¡ÂºÂ§n kiÃ¡Â»Æ’m tra.
     * @return PhÃ¡ÂºÂ¡m vi dÃ¡Â»Â¯ liÃ¡Â»â€¡u (DataScope).
     */
    public com.hrm.model.DataScope getScopeForAction(String actionPrefix) {
        if (hasPermission(actionPrefix + "_ALL")) return com.hrm.model.DataScope.ALL;
        if (hasPermission(actionPrefix + "_DEPT")) return com.hrm.model.DataScope.DEPT;
        if (hasPermission(actionPrefix + "_TEAM")) return com.hrm.model.DataScope.TEAM;
        if (hasPermission(actionPrefix + "_SELF")) return com.hrm.model.DataScope.SELF;
        return com.hrm.model.DataScope.NONE;
    }

    /** Alias for hasPermission() */
    public boolean coQuyen(String maQuyen) {
        return hasPermission(maQuyen);
    }

    /**
     * KiÃ¡Â»Æ’m tra ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i cÃƒÂ³ vai trÃƒÂ² chÃ¡Â»â€° Ã„â€˜Ã¡Â»â€¹nh khÃƒÂ´ng.
     */
    public boolean hasRole(String roleCode) {
        TaiKhoan user = getCurrentUser();
        return user != null && user.coVaiTro(roleCode);
    }

    /**
     * Ã„ÂÃ¡Â»â€¢i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cho ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng.
     * XÃƒÂ¡c minh mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cÃ…Â© trÃ†Â°Ã¡Â»â€ºc khi cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t.
     *
     * @param userId   ID tÃƒÂ i khoÃ¡ÂºÂ£n
     * @param oldPass  mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cÃ…Â© dÃ¡ÂºÂ¡ng plaintext
     * @param newPass  mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi dÃ¡ÂºÂ¡ng plaintext
     * @return KetQua thÃƒÂ nh cÃƒÂ´ng hoÃ¡ÂºÂ·c thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i kÃƒÂ¨m thÃƒÂ´ng bÃƒÂ¡o
     */
    public KetQua<Void> changePassword(int userId, String oldPass, String newPass) {
        if (oldPass == null || oldPass.isEmpty()) {
            return KetQua.error("MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cÃ…Â© khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (newPass == null || newPass.length() < 6) {
            return KetQua.error("MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi phÃ¡ÂºÂ£i cÃƒÂ³ ÃƒÂ­t nhÃ¡ÂºÂ¥t 6 kÃƒÂ½ tÃ¡Â»Â±.");
        }

        TaiKhoan user = taiKhoanRepo.findById(userId);
        if (user == null) {
            return KetQua.error("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y tÃƒÂ i khoÃ¡ÂºÂ£n.");
        }
        if (!PasswordUtil.verifyPassword(oldPass, user.getMatKhau())) {
            return KetQua.error("MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cÃ…Â© khÃƒÂ´ng Ã„â€˜ÃƒÂºng.");
        }

        String hashedNew = PasswordUtil.hashPassword(newPass);
        boolean updated = taiKhoanRepo.updatePassword(userId, hashedNew);
        if (updated) {
            return KetQua.success(null, "Ã„ÂÃ¡Â»â€¢i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u thÃƒÂ nh cÃƒÂ´ng.");
        }
        return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u. Vui lÃƒÂ²ng thÃ¡Â»Â­ lÃ¡ÂºÂ¡i.");
    }

    /**
     * Admin reset mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng khÃƒÂ¡c (khÃƒÂ´ng cÃ¡ÂºÂ§n mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cÃ…Â©).
     */
    public KetQua<Void> resetPassword(int userId, String newPass) {
        if (newPass == null || newPass.isEmpty()) {
            return KetQua.error("Mat khau moi khong duoc de trong.");
        }
        String hashedNew = PasswordUtil.hashPassword(newPass);
        boolean updated = taiKhoanRepo.updatePassword(userId, hashedNew);
        if (updated) {
            return KetQua.success(null, "Da cap nhat mat khau thanh cong.");
        }
        return KetQua.error("Khong the cap nhat mat khau.");
    }

    // =====================================================================
    // ==================== TaiKhoan Management ================================
    // =====================================================================

    /**
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ tÃƒÂ i khoÃ¡ÂºÂ£n.
     */
    public List<TaiKhoan> getAllUsers() {
        return taiKhoanRepo.findAll();
    }

    /**
     * TÃ¡ÂºÂ¡o tÃƒÂ i khoÃ¡ÂºÂ£n mÃ¡Â»â€ºi.
     *
     * @param tenDangNhap tÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p
     * @param matKhau     mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u dÃ¡ÂºÂ¡ng plaintext (sÃ¡ÂºÂ½ Ã„â€˜Ã†Â°Ã¡Â»Â£c hash)
     * @param maNV        mÃƒÂ£ nhÃƒÂ¢n viÃƒÂªn liÃƒÂªn kÃ¡ÂºÂ¿t (cÃƒÂ³ thÃ¡Â»Æ’ null)
     * @param maVaiTro    mÃƒÂ£ vai trÃƒÂ²
     * @param email       Ã„â€˜Ã¡Â»â€¹a chÃ¡Â»â€° email
     * @return KetQua chÃ¡Â»Â©a ID tÃƒÂ i khoÃ¡ÂºÂ£n mÃ¡Â»â€ºi hoÃ¡ÂºÂ·c lÃ¡Â»â€”i
     */
    public KetQua<Integer> createUser(String tenDangNhap, String matKhau,
                                              Integer maNV, String maVaiTro, String email) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return KetQua.error("TÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (matKhau == null || matKhau.isEmpty()) {
            return KetQua.error("MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return KetQua.error("Vai trÃƒÂ² khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }

        String trimmedUsername = tenDangNhap.trim();
        if (taiKhoanRepo.existsByUsername(trimmedUsername)) {
            return KetQua.error("TÃƒÂªn Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p '" + trimmedUsername + "' Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i.");
        }

        // KiÃ¡Â»Æ’m tra mÃ¡Â»â€”i nhÃƒÂ¢n viÃƒÂªn chÃ¡Â»â€° Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃƒÂ³ 1 tÃƒÂ i khoÃ¡ÂºÂ£n
        if (maNV != null && maNV > 0) {
            TaiKhoan existing = taiKhoanRepo.findByMaNV(maNV);
            if (existing != null) {
                return KetQua.error("NhÃƒÂ¢n viÃƒÂªn nÃƒÂ y Ã„â€˜ÃƒÂ£ cÃƒÂ³ tÃƒÂ i khoÃ¡ÂºÂ£n ('" + existing.getTenDangNhap()
                        + "'). MÃ¡Â»â€”i nhÃƒÂ¢n viÃƒÂªn chÃ¡Â»â€° Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃƒÂ³ mÃ¡Â»â„¢t tÃƒÂ i khoÃ¡ÂºÂ£n.");
            }
        }

        String hashedPassword = PasswordUtil.hashPassword(matKhau);
        int newId = taiKhoanRepo.insert(trimmedUsername, hashedPassword, maNV, maVaiTro.trim(), email);

        if (newId < 0) {
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ¡o tÃƒÂ i khoÃ¡ÂºÂ£n. Vui lÃƒÂ²ng thÃ¡Â»Â­ lÃ¡ÂºÂ¡i.");
        }
        return KetQua.success(newId, "TÃ¡ÂºÂ¡o tÃƒÂ i khoÃ¡ÂºÂ£n thÃƒÂ nh cÃƒÂ´ng.");
    }

    /**
     * CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ´ng tin tÃƒÂ i khoÃ¡ÂºÂ£n.
     *
     * @param user Ã„â€˜Ã¡Â»â€˜i tÃ†Â°Ã¡Â»Â£ng TaiKhoan vÃ¡Â»â€ºi thÃƒÂ´ng tin Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c chÃ¡Â»â€°nh sÃ¡Â»Â­a
     * @return KetQua thÃƒÂ nh cÃƒÂ´ng hoÃ¡ÂºÂ·c lÃ¡Â»â€”i
     */
    public KetQua<Void> updateUser(TaiKhoan user) {
        if (user == null) {
            return KetQua.error("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y tÃƒÂ i khoÃ¡ÂºÂ£n.");
        }
        TaiKhoan existing = taiKhoanRepo.findById(user.getId());
        if (existing == null) {
            return KetQua.error("Khong tim thay tai khoan.");
        }
        if (isBuiltInAdminAccount(existing) || isCurrentAdminSelfUpdate(existing)) {
            user.setHoatDong(true);
            user.setBiKhoa(false);
            VaiTro adminRole = getRoleByCode("ADMIN");
            if (adminRole != null) {
                java.util.List<VaiTro> fixedRoles = new java.util.ArrayList<>();
                fixedRoles.add(adminRole);
                user.setVaiTros(fixedRoles);
            }
        }
        try {
            taiKhoanRepo.update(user);
            return KetQua.success(null, "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t tÃƒÂ i khoÃ¡ÂºÂ£n thÃƒÂ nh cÃƒÂ´ng.");
        } catch (Exception e) {
            System.err.println("LÃ¡Â»â€”i updateUser: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t tÃƒÂ i khoÃ¡ÂºÂ£n: " + e.getMessage());
        }
    }

    /**
     * XÃƒÂ³a tÃƒÂ i khoÃ¡ÂºÂ£n theo ID.
     *
     * @param id ID tÃƒÂ i khoÃ¡ÂºÂ£n cÃ¡ÂºÂ§n xÃƒÂ³a
     * @return KetQua thÃƒÂ nh cÃƒÂ´ng hoÃ¡ÂºÂ·c lÃ¡Â»â€”i
     */
    public KetQua<Void> deleteUser(int id) {
        TaiKhoan user = taiKhoanRepo.findById(id);
        if (user == null) {
            return KetQua.error("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y tÃƒÂ i khoÃ¡ÂºÂ£n.");
        }
        TaiKhoan current = SessionContext.getInstance().getCurrentUser();
        if (current != null && current.getId() == id && current.coVaiTro("ADMIN")) {
            return KetQua.error("Khong the xoa tai khoan admin dang dang nhap.");
        }
        if ("admin".equalsIgnoreCase(user.getTenDangNhap())) {
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ xÃƒÂ³a tÃƒÂ i khoÃ¡ÂºÂ£n admin hÃ¡Â»â€¡ thÃ¡Â»â€˜ng.");
        }
        try {
            taiKhoanRepo.delete(id);
            return KetQua.success(null, "XÃƒÂ³a tÃƒÂ i khoÃ¡ÂºÂ£n thÃƒÂ nh cÃƒÂ´ng.");
        } catch (Exception e) {
            System.err.println("LÃ¡Â»â€”i deleteUser: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ xÃƒÂ³a tÃƒÂ i khoÃ¡ÂºÂ£n: " + e.getMessage());
        }
    }

    // =====================================================================
    // ==================== VaiTro Management ================================
    // =====================================================================

    /**
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ vai trÃƒÂ².
     */
    public List<VaiTro> getAllRoles() {
        return taiKhoanRepo.findAllRoles();
    }

    /**
     * TÃ¡ÂºÂ¡o vai trÃƒÂ² mÃ¡Â»â€ºi.
     *
     * @param maVaiTro  mÃƒÂ£ vai trÃƒÂ²
     * @param tenVaiTro tÃƒÂªn vai trÃƒÂ²
     * @param moTa      mÃƒÂ´ tÃ¡ÂºÂ£
     * @return KetQua thÃƒÂ nh cÃƒÂ´ng hoÃ¡ÂºÂ·c lÃ¡Â»â€”i
     */
    public KetQua<Void> createRole(String maVaiTro, String tenVaiTro, String moTa) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return KetQua.error("MÃƒÂ£ vai trÃƒÂ² khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (tenVaiTro == null || tenVaiTro.trim().isEmpty()) {
            return KetQua.error("TÃƒÂªn vai trÃƒÂ² khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        try {
            int rows = taiKhoanRepo.insertRole(maVaiTro.trim(), tenVaiTro.trim(), moTa);
            if (rows > 0) {
                return KetQua.success(null, "TÃ¡ÂºÂ¡o vai trÃƒÂ² thÃƒÂ nh cÃƒÂ´ng.");
            }
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ¡o vai trÃƒÂ². MÃƒÂ£ vai trÃƒÂ² cÃƒÂ³ thÃ¡Â»Æ’ Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i.");
        } catch (Exception e) {
            System.err.println("LÃ¡Â»â€”i createRole: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ¡o vai trÃƒÂ²: " + e.getMessage());
        }
    }

    /**
     * CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ´ng tin vai trÃƒÂ².
     *
     * @param role Ã„â€˜Ã¡Â»â€˜i tÃ†Â°Ã¡Â»Â£ng VaiTro vÃ¡Â»â€ºi thÃƒÂ´ng tin Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c chÃ¡Â»â€°nh sÃ¡Â»Â­a
     * @return KetQua thÃƒÂ nh cÃƒÂ´ng hoÃ¡ÂºÂ·c lÃ¡Â»â€”i
     */
    public KetQua<Void> updateRole(VaiTro role) {
        if (role == null) {
            return KetQua.error("ThÃƒÂ´ng tin vai trÃƒÂ² khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.");
        }
        try {
            taiKhoanRepo.updateRole(role);
            return KetQua.success(null, "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t vai trÃƒÂ² thÃƒÂ nh cÃƒÂ´ng.");
        } catch (Exception e) {
            System.err.println("LÃ¡Â»â€”i updateRole: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t vai trÃƒÂ²: " + e.getMessage());
        }
    }

    /**
     * XÃƒÂ³a vai trÃƒÂ² theo mÃƒÂ£.
     * KhÃƒÂ´ng thÃ¡Â»Æ’ xÃƒÂ³a vai trÃƒÂ² hÃ¡Â»â€¡ thÃ¡Â»â€˜ng hoÃ¡ÂºÂ·c vai trÃƒÂ² Ã„â€˜ang Ã„â€˜Ã†Â°Ã¡Â»Â£c sÃ¡Â»Â­ dÃ¡Â»Â¥ng.
     *
     * @param code mÃƒÂ£ vai trÃƒÂ² cÃ¡ÂºÂ§n xÃƒÂ³a
     * @return KetQua thÃƒÂ nh cÃƒÂ´ng hoÃ¡ÂºÂ·c lÃ¡Â»â€”i
     */
    public KetQua<Void> deleteRole(String code) {
        if (code == null || code.trim().isEmpty()) {
            return KetQua.error("MÃƒÂ£ vai trÃƒÂ² khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.");
        }
        boolean deleted = taiKhoanRepo.deleteRole(code.trim());
        if (deleted) {
            return KetQua.success(null, "XÃƒÂ³a vai trÃƒÂ² thÃƒÂ nh cÃƒÂ´ng.");
        }
        return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ xÃƒÂ³a vai trÃƒÂ². Vai trÃƒÂ² cÃƒÂ³ thÃ¡Â»Æ’ lÃƒÂ  vai trÃƒÂ² hÃ¡Â»â€¡ thÃ¡Â»â€˜ng hoÃ¡ÂºÂ·c Ã„â€˜ang Ã„â€˜Ã†Â°Ã¡Â»Â£c sÃ¡Â»Â­ dÃ¡Â»Â¥ng.");
    }

    // =====================================================================
    // ==================== Quyen Management ==========================
    // =====================================================================

    /**
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ quyÃ¡Â»Ân.
     */
    public List<Quyen> getAllPermissions() {
        return taiKhoanRepo.findAllPermissions();
    }

    /**
     * GÃƒÂ¡n danh sÃƒÂ¡ch quyÃ¡Â»Ân mÃ¡Â»â€ºi cho vai trÃƒÂ² (xÃƒÂ³a cÃ…Â©, thÃƒÂªm mÃ¡Â»â€ºi).
     *
     * @param maVaiTro        mÃƒÂ£ vai trÃƒÂ²
     * @param permissionCodes danh sÃƒÂ¡ch mÃƒÂ£ quyÃ¡Â»Ân mÃ¡Â»â€ºi
     * @return KetQua thÃƒÂ nh cÃƒÂ´ng hoÃ¡ÂºÂ·c lÃ¡Â»â€”i
     */
    public KetQua<Void> setRolePermissions(String maVaiTro, List<String> permissionCodes) {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return KetQua.error("MÃƒÂ£ vai trÃƒÂ² khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.");
        }
        if ("ADMIN".equalsIgnoreCase(maVaiTro.trim())) {
            return KetQua.error("Khong the chinh quyen vai tro ADMIN. ADMIN luon toan quyen.");
        }
        if (permissionCodes == null) {
            return KetQua.error("Danh sÃƒÂ¡ch quyÃ¡Â»Ân khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡.");
        }
        try {
            taiKhoanRepo.setRolePermissions(maVaiTro.trim(), permissionCodes);
            return KetQua.success(null, "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t quyÃ¡Â»Ân cho vai trÃƒÂ² thÃƒÂ nh cÃƒÂ´ng.");
        } catch (Exception e) {
            System.err.println("LÃ¡Â»â€”i setRolePermissions: " + e.getMessage());
            e.printStackTrace();
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t quyÃ¡Â»Ân vai trÃƒÂ²: " + e.getMessage());
        }
    }

    /**
     * TÃƒÂ¬m tÃƒÂ i khoÃ¡ÂºÂ£n theo maNV nhÃƒÂ¢n viÃƒÂªn.
     */
    public com.hrm.model.TaiKhoan findByMaNV(int maNV) {
        return taiKhoanRepo.findByMaNV(maNV);
    }

    /**
     * TÃƒÂ¬m vai trÃƒÂ² theo mÃƒÂ£.
     */
    public com.hrm.model.VaiTro getRoleByCode(String code) {
        return taiKhoanRepo.findRoleByCode(code);
    }

    /**
     * TÃ¡ÂºÂ¡o vai trÃƒÂ² vÃƒÂ  gÃƒÂ¡n danh sÃƒÂ¡ch quyÃ¡Â»Ân.
     */
    public KetQua<Void> createRoleWithPermissions(String maVaiTro, String tenVaiTro, java.util.List<String> permissions) {
        KetQua<Void> createResult = createRole(maVaiTro, tenVaiTro, null);
        if (!createResult.isSuccess()) return createResult;
        if (permissions != null && !permissions.isEmpty()) {
            return setRolePermissions(maVaiTro, permissions);
        }
        return createResult;
    }

    /**
     * GÃƒÂ¡n vai trÃƒÂ² cho tÃƒÂ i khoÃ¡ÂºÂ£n (ghi Ã„â€˜ÃƒÂ¨ vai trÃƒÂ² cÃ…Â© Ã¢â‚¬â€ single-role).
     */
    public KetQua<Void> assignRoleToUser(int maTaiKhoan, String maVaiTro) {
        TaiKhoan target = taiKhoanRepo.findById(maTaiKhoan);
        if (target == null) {
            return KetQua.error("Khong tim thay tai khoan.");
        }
        if (isBuiltInAdminAccount(target) && !"ADMIN".equalsIgnoreCase(maVaiTro)) {
            return KetQua.error("Khong the thay doi vai tro cua tai khoan admin.");
        }
        if (isCurrentAdminSelfUpdate(target) && !"ADMIN".equalsIgnoreCase(maVaiTro)) {
            return KetQua.error("Khong the thay doi vai tro cua tai khoan admin.");
        }
        try {
            taiKhoanRepo.updateRole(maTaiKhoan, maVaiTro);
            return KetQua.success(null, "GÃƒÂ¡n vai trÃƒÂ² thÃƒÂ nh cÃƒÂ´ng.");
        } catch (Exception e) {
            return KetQua.error("KhÃƒÂ´ng thÃ¡Â»Æ’ gÃƒÂ¡n vai trÃƒÂ²: " + e.getMessage());
        }
    }

    /**
     * TÃƒÂ­nh quyÃ¡Â»Ân hiÃ¡Â»â€¡u dÃ¡Â»Â¥ng theo cÃƒÂ´ng thÃ¡Â»Â©c RBAC Ã„â€˜Ã¡Â»â„¢ng:
     * QuyÃ¡Â»Ân hiÃ¡Â»â€¡u dÃ¡Â»Â¥ng = (QuyÃ¡Â»Ân tÃ¡Â»Â« vai trÃƒÂ²) Ã¢Ë†Âª (TaiKhoanQuyen choPhep=true) Ã¢Ë†â€™ (TaiKhoanQuyen choPhep=false)
     *
     * @param maTaiKhoan ID tÃƒÂ i khoÃ¡ÂºÂ£n
     * @return tÃ¡ÂºÂ­p mÃƒÂ£ quyÃ¡Â»Ân hiÃ¡Â»â€¡u dÃ¡Â»Â¥ng
     */
    public Set<String> getEffectivePermissions(int maTaiKhoan) {
        TaiKhoan user = taiKhoanRepo.findById(maTaiKhoan);
        if (user == null) return new HashSet<>();

        // TÃ¡ÂºÂ­p quyÃ¡Â»Ân tÃ¡Â»Â« vai trÃƒÂ²
        Set<String> fromRoles = new HashSet<>();
        user.getVaiTros().forEach(role ->
            role.getQuyens().forEach(p -> fromRoles.add(p.getId()))
        );

        return fromRoles;
    }

}

