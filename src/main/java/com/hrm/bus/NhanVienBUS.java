package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.dao.TaiKhoanDAO;
import com.hrm.dao.ThongTinCaNhanDAO;
import com.hrm.model.NhanVien;
import com.hrm.model.TaiKhoan;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.util.DatabaseConnection;
import com.hrm.util.SessionContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

/**
 * Service quan ly nhan vien.
 * Singleton pattern.
 */
public class NhanVienBUS {
    private static final String ACTION_EMPLOYEE_VIEW = "EMPLOYEE_VIEW";
    private static final String ACTION_EMPLOYEE_UPDATE = "EMPLOYEE_UPDATE";

    private static final String TRANG_THAI_DANG_LAM_VIEC = "dang_lam_viec";
    private static final String TRANG_THAI_TAM_NGHI = "tam_nghi";
    private static final String TRANG_THAI_NGHI_VIEC = "nghi_viec";

    private static final String MA_LOAI_PHEP_NAM = "PHEP_NAM";
    private static final String REGEX_EMAIL = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final String REGEX_PHONE = "0\\d{9,10}";

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_TONG_GIAM_DOC = "TONG_GIAM_DOC";
    private static final String ROLE_GIAM_DOC = "GIAM_DOC";
    private static final String ROLE_PHO_GIAM_DOC = "PHO_GIAM_DOC";
    private static final String ROLE_TRUONG_PHONG_NS = "TRUONG_PHONG_NS";
    private static final String ROLE_TRUONG_PHONG_KT = "TRUONG_PHONG_KT";
    private static final String ROLE_TRUONG_PHONG = "TRUONG_PHONG";
    private static final String ROLE_QUAN_LY = "QUAN_LY";
    private static final String ROLE_NHAN_SU = "NHAN_SU";
    private static final String ROLE_KE_TOAN = "KE_TOAN";
    private static final String ROLE_NHAN_VIEN = "NHAN_VIEN";

    private static NhanVienBUS instance;

    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();
    private final ThongTinCaNhanDAO ttcnRepo = ThongTinCaNhanDAO.getInstance();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    private final TaiKhoanDAO taiKhoanRepo = new TaiKhoanDAO();

    private NhanVienBUS() {
    }

    public static synchronized NhanVienBUS getInstance() {
        if (instance == null) {
            instance = new NhanVienBUS();
        }
        return instance;
    }

    public KetQua<NhanVien> taoHoSo(NhanVien nv, ThongTinCaNhan ttcn) {
        if (isBlank(nv.getMaNhanVien())) {
            return KetQua.error("Ma nhan vien khong duoc de trong.");
        }
        if (nvRepo.existsByMaNhanVien(nv.getMaNhanVien().trim())) {
            return KetQua.error("Ma nhan vien '" + nv.getMaNhanVien() + "' da ton tai.");
        }
        if (isBlank(ttcn.getHoTen())) {
            return KetQua.error("Ho ten khong duoc de trong.");
        }

        KetQua<Void> cccdValidation = validateCCCD(ttcn.getCccd(), nv.getMaNhanVien());
        if (!cccdValidation.isSuccess()) {
            return KetQua.error(cccdValidation.getMessage());
        }

        if (nv.getNgayVaoLam() == null) {
            return KetQua.error("Ngay vao lam khong duoc de trong.");
        }

        if (isBlank(nv.getTrangThai())) {
            nv.setTrangThai(TRANG_THAI_DANG_LAM_VIEC);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String maNV = nvRepo.insert(conn, nv);
                ttcn.setMaNV(maNV);
                ttcnRepo.insert(conn, ttcn);
                insertSoDungPhep(conn, maNV, LocalDate.now().getYear(), MA_LOAI_PHEP_NAM, 12.0);
                conn.commit();

                nv.setHoTen(ttcn.getHoTen());
                return KetQua.success(nv, "Tao ho so nhan vien thanh cong. Ma NV: " + maNV);
            } catch (Exception ex) {
                conn.rollback();
                return KetQua.error("Loi khi tao ho so: " + ex.getMessage());
            }
        } catch (SQLException e) {
            return KetQua.error("Loi ket noi co so du lieu: " + e.getMessage());
        }
    }

    public KetQua<ThongTinCaNhan> capNhatThongTinCaNhan(ThongTinCaNhan ttcn) {
        if (isBlank(ttcn.getMaNV())) {
            return KetQua.error("Ma nhan vien khong hop le.");
        }
        if (!canEditEmployeeProfile(ttcn.getMaNV())) {
            return KetQua.error("Ban khong co quyen cap nhat thong tin nhan vien nay.");
        }

        KetQua<Void> cccdValidation = validateCCCD(ttcn.getCccd(), ttcn.getMaNV());
        if (!cccdValidation.isSuccess()) {
            return KetQua.error(cccdValidation.getMessage());
        }

        if (ttcn.getEmail() != null && !ttcn.getEmail().trim().isEmpty()
                && !ttcn.getEmail().trim().matches(REGEX_EMAIL)) {
            return KetQua.error("Dia chi email khong hop le.");
        }

        if (ttcn.getDienThoai() != null && !ttcn.getDienThoai().trim().isEmpty()
                && !ttcn.getDienThoai().trim().matches(REGEX_PHONE)) {
            return KetQua.error("So dien thoai khong hop le (bat dau bang 0 va co 10-11 chu so).");
        }

        try {
            ttcnRepo.update(ttcn);
            return KetQua.success(ttcn, "Cap nhat thong tin ca nhan thanh cong.");
        } catch (Exception e) {
            return KetQua.error("Loi cap nhat thong tin ca nhan: " + e.getMessage());
        }
    }

    public KetQua<NhanVien> capNhatTrangThai(String maNV, String trangThaiMoi, String lyDo) {
        if (isBlank(maNV)) {
            return KetQua.error("Ma nhan vien khong hop le.");
        }

        NhanVien nv = nvRepo.findByMaNhanVien(maNV);
        if (nv == null) {
            return KetQua.error("Khong tim thay nhan vien voi ma: " + maNV);
        }

        KetQua<Void> permissionValidation = validateStatusChangePermission(nv, trangThaiMoi);
        if (!permissionValidation.isSuccess()) {
            return KetQua.error(permissionValidation.getMessage());
        }

        KetQua<Void> transitionValidation = validateTrangThaiTransition(nv.getTrangThai(), trangThaiMoi);
        if (!transitionValidation.isSuccess()) {
            return KetQua.error(transitionValidation.getMessage());
        }

        nv.setTrangThai(trangThaiMoi);
        nvRepo.update(nv);

        if (TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi)) {
            boNhiemRepo.endAllActiveBoNhiemForNV(maNV, LocalDate.now());
            taiKhoanRepo.deactivateByMaNV(maNV);
        }

        return KetQua.success(nv, "Cap nhat trang thai nhan vien thanh cong.");
    }

    public boolean canChangeEmployeeStatus(String maNV) {
        if (isBlank(maNV)) {
            return false;
        }
        NhanVien target = nvRepo.findByMaNhanVien(maNV);
        return canChangeEmployeeStatus(target);
    }

    public boolean canEditEmployeeProfile(String maNV) {
        if (isBlank(maNV)) {
            return false;
        }
        NhanVien target = nvRepo.findByMaNhanVien(maNV);
        return canEditEmployeeProfile(target);
    }

    public boolean canUpdateEmployeeStatus(String maNV) {
        if (isBlank(maNV)) {
            return false;
        }
        NhanVien target = nvRepo.findByMaNhanVien(maNV);
        return canChangeEmployeeStatus(target);
    }

    public List<NhanVien> getAll() {
        return nvRepo.findAll();
    }

    public List<NhanVien> getAllByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(ACTION_EMPLOYEE_VIEW);
        return nvRepo.findAllByScope(scope, currentMaNV);
    }

    public List<NhanVien> getAllByActionScope(String action, String currentMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(action);
        return nvRepo.findAllByScope(scope, currentMaNV);
    }

    public List<NhanVien> getDangLamViec() {
        return nvRepo.findDangLamViec();
    }

    public NhanVien getByMaNhanVien(String maNhanVien) {
        return nvRepo.findByMaNhanVien(maNhanVien);
    }

    public NhanVien getById(String maNV) {
        return nvRepo.findByMaNhanVien(maNV);
    }

    public ThongTinCaNhan getThongTinCaNhan(String maNV) {
        return ttcnRepo.findByMaNV(maNV);
    }

    public String generateMaNhanVien() {
        return nvRepo.generateMaNhanVien();
    }

    public List<NhanVien> getNhanVienByMaQuanLy(String maQuanLy) {
        return nvRepo.findNhanVienByMaQuanLy(maQuanLy);
    }

    private void insertSoDungPhep(Connection conn, String maNV, int nam, String maLoaiPhep,
                                  double soNgayDuocCap) throws SQLException {
        String sql = "INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap, soNgayDaDung) "
                + "VALUES (?, ?, ?, ?, 0) "
                + "ON DUPLICATE KEY UPDATE soNgayDuocCap=VALUES(soNgayDuocCap)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setInt(2, nam);
            ps.setString(3, maLoaiPhep);
            ps.setDouble(4, soNgayDuocCap);
            ps.executeUpdate();
        }
    }

    private KetQua<Void> validateCCCD(String cccdRaw, String maNV) {
        if (isBlank(cccdRaw)) {
            return KetQua.success(null, "");
        }
        String cccd = cccdRaw.trim();
        if (!cccd.matches("\\d{12}")) {
            return KetQua.error("CCCD phai la 12 chu so.");
        }
        if (ttcnRepo.existsByCCCD(cccd, maNV)) {
            return KetQua.error("CCCD '" + cccdRaw + "' da duoc dang ky cho nhan vien khac.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateTrangThaiTransition(String trangThaiHienTai, String trangThaiMoi) {
        boolean valid = false;
        if (TRANG_THAI_DANG_LAM_VIEC.equals(trangThaiHienTai)
                && (TRANG_THAI_TAM_NGHI.equals(trangThaiMoi) || TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi))) {
            valid = true;
        }
        if (TRANG_THAI_TAM_NGHI.equals(trangThaiHienTai)
                && (TRANG_THAI_DANG_LAM_VIEC.equals(trangThaiMoi) || TRANG_THAI_NGHI_VIEC.equals(trangThaiMoi))) {
            valid = true;
        }
        if (valid) {
            return KetQua.success(null, "");
        }
        if (TRANG_THAI_NGHI_VIEC.equals(trangThaiHienTai)) {
            return KetQua.error("Nhan vien da nghi viec, khong the thay doi trang thai.");
        }
        return KetQua.error("Chuyen trang thai khong hop le tu '" + trangThaiHienTai + "' sang '" + trangThaiMoi + "'.");
    }

    private boolean canEditEmployeeProfile(NhanVien target) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || target == null || isBlank(target.getMaNhanVien())) {
            return false;
        }
        String currentMaNV = currentUser.getNhanVienId();
        if (SelfApprovalGuard.isSelfAction(currentMaNV, target.getMaNhanVien())) {
            return true;
        }
        return isCurrentUserAllowedForAction(currentUser, ACTION_EMPLOYEE_UPDATE)
                && isTargetWithinActionScope(ACTION_EMPLOYEE_UPDATE, currentMaNV, target.getMaNhanVien());
    }

    private boolean canChangeEmployeeStatus(NhanVien target) {
        return validateStatusPermission(target).isSuccess();
    }

    private KetQua<Void> validateStatusChangePermission(NhanVien target, String trangThaiMoi) {
        return validateStatusPermission(target);
    }

    private KetQua<Void> validateStatusPermission(NhanVien target) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return KetQua.error("Phien dang nhap khong hop le.");
        }
        if (target == null || isBlank(target.getMaNhanVien())) {
            return KetQua.error("Nhan vien muc tieu khong hop le.");
        }

        String currentMaNV = currentUser.getNhanVienId();
        if (SelfApprovalGuard.isSelfAction(currentMaNV, target.getMaNhanVien())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Khong the tu doi trang thai cua chinh minh.");
        }

        if (!isCurrentUserAllowedForAction(currentUser, ACTION_EMPLOYEE_UPDATE)) {
            return KetQua.error("Ban khong co quyen doi trang thai nhan vien nay.");
        }
        if (!isTargetWithinActionScope(ACTION_EMPLOYEE_UPDATE, currentMaNV, target.getMaNhanVien())) {
            return KetQua.error("Ban khong co pham vi thao tac tren nhan vien nay.");
        }
        if (!canManageTargetByHierarchy(currentUser, target)) {
            return KetQua.error("Khong duoc phep doi trang thai cua nhan su co cap bac bang hoac cao hon.");
        }
        return KetQua.success(null, "");
    }

    private boolean isCurrentUserAllowedForAction(TaiKhoan currentUser, String action) {
        return currentUser != null
                && ("admin".equalsIgnoreCase(currentUser.getTenDangNhap())
                || currentUser.coVaiTro(ROLE_ADMIN)
                || currentUser.coQuyen(action));
    }

    private boolean isTargetWithinActionScope(String action, String currentMaNV, String targetMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(action);
        if (scope == com.hrm.model.DataScope.ALL) {
            return true;
        }
        if (scope == com.hrm.model.DataScope.NONE || isBlank(currentMaNV) || isBlank(targetMaNV)) {
            return false;
        }
        if (scope == com.hrm.model.DataScope.SELF) {
            return targetMaNV.equalsIgnoreCase(currentMaNV);
        }
        for (NhanVien nv : nvRepo.findAllByScope(scope, currentMaNV)) {
            if (targetMaNV.equalsIgnoreCase(nv.getMaNhanVien())) {
                return true;
            }
        }
        return false;
    }

    private boolean canManageTargetByHierarchy(TaiKhoan actor, NhanVien target) {
        if (actor == null) {
            return false;
        }
        if ("admin".equalsIgnoreCase(actor.getTenDangNhap()) || actor.coVaiTro(ROLE_ADMIN)) {
            return true;
        }

        TaiKhoan targetUser = taiKhoanRepo.findByMaNV(target.getMaNhanVien());
        int actorRank = getAuthorityRank(actor, null);
        int targetRank = getAuthorityRank(targetUser, target.getTenChucVuHienTai());
        return actorRank > targetRank;
    }

    private int getAuthorityRank(TaiKhoan user, String fallbackTitle) {
        int rank = getTitleRank(fallbackTitle);
        if (user == null) {
            return rank;
        }
        if (user.getVaiTros() != null) {
            for (com.hrm.model.VaiTro role : user.getVaiTros()) {
                rank = Math.max(rank, getRoleRank(role != null ? role.getId() : null));
            }
        }
        return rank;
    }

    private int getRoleRank(String roleId) {
        if (roleId == null) {
            return 0;
        }
        switch (roleId.trim().toUpperCase()) {
            case ROLE_ADMIN:
                return 1000;
            case ROLE_TONG_GIAM_DOC:
                return 900;
            case ROLE_GIAM_DOC:
                return 850;
            case ROLE_PHO_GIAM_DOC:
                return 800;
            case ROLE_TRUONG_PHONG_NS:
                return 750;
            case ROLE_TRUONG_PHONG_KT:
            case ROLE_TRUONG_PHONG:
                return 700;
            case ROLE_QUAN_LY:
                return 600;
            case ROLE_NHAN_SU:
                return 500;
            case ROLE_KE_TOAN:
                return 450;
            case ROLE_NHAN_VIEN:
                return 100;
            default:
                return 0;
        }
    }

    private int getTitleRank(String title) {
        String normalized = normalizeText(title);
        if (normalized.isEmpty()) {
            return 0;
        }
        if (normalized.contains("tong giam doc")) {
            return 900;
        }
        if (normalized.contains("pho giam doc")) {
            return 800;
        }
        if (normalized.contains("giam doc")) {
            return 850;
        }
        if (normalized.contains("truong phong nhan su")) {
            return 750;
        }
        if (normalized.contains("truong phong ke toan") || normalized.contains("truong phong")) {
            return 700;
        }
        if (normalized.contains("quan ly")) {
            return 600;
        }
        if (normalized.contains("nhan su")) {
            return 500;
        }
        if (normalized.contains("ke toan")) {
            return 450;
        }
        return 100;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase()
                .trim();
        return normalized.replace('_', ' ');
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
