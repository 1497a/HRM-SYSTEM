package com.hrm.bus;

import com.hrm.model.BoNhiem;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.dao.ThongTinCaNhanDAO;
import com.hrm.dao.ChucVuDAO;
import com.hrm.model.ChucVu;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service quản lý bổ nhiệm nhân viên.
 * Singleton pattern.
 */
public class BoNhiemBUS {

    private static final String LOAI_BO_NHIEM_CHINH = "chinh";
    private static final String ACTION_APPOINTMENT_VIEW = PermissionCodes.APPOINTMENT_VIEW;
    private static final String ROLE_ADMIN = HRMConstants.ROLE_ADMIN;
    private static final String ROLE_EMPLOYEE = HRMConstants.ROLE_EMPLOYEE;
    private static BoNhiemBUS instance;
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();
    private final ThongTinCaNhanDAO ttcnRepo = ThongTinCaNhanDAO.getInstance();
    private final ChucVuDAO chucVuRepo = ChucVuDAO.getInstance();
    private BoNhiemBUS() {
    }

    public static synchronized BoNhiemBUS getInstance() {
        if (instance == null) {
            instance = new BoNhiemBUS();
        }
        return instance;
    }

    // ============================
    // Tạo bổ nhiệm mới
    // ============================
    public KetQua<BoNhiem> taoBoNhiem(BoNhiem bn) {
        if (SelfApprovalGuard.isSelfAction(getCurrentUserNhanVienId(), bn.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự tạo bổ nhiệm cho chính mình.");
        }
        // Validate nhân viên tồn tại và đang làm việc
        NhanVien nv = nvRepo.findByMaNhanVien(bn.getMaNV());
        if (nv == null) {
            return KetQua.error("Không tìm thấy nhân viên với mã: " + bn.getMaNV());
        }
        if (!HRMConstants.TRANG_THAI_DANG_LAM_VIEC.equals(nv.getTrangThai())) {
            return KetQua.error("Nhân viên không ở trạng thái đang làm việc, không thể bổ nhiệm.");
        }
        KetQua<Void> validation = validateBoNhiemInput(bn);
        if (!validation.isSuccess()) return KetQua.error(validation.getMessage());
        // Kiểm tra xung đột nếu là bổ nhiệm chính
        if (isLoaiBoNhiemChinh(bn)) {
            boolean conflict = boNhiemRepo.hasConflictingChinhBoNhiem(
                    bn.getMaNV(), bn.getTuNgay(), bn.getDenNgay(), 0);
            if (conflict) {
                return KetQua.error(
                        "Nhân viên đã có bổ nhiệm chính hiệu lực trong khoảng thời gian này. "
                        + "Hãy kết thúc bổ nhiệm cũ trước.");
            }
            // Chan tao bo nhiem moi neu chuc vu lanh dao (capBac <= 3: GD/TP/TT) da co nguoi giu
            ChucVu cv = chucVuRepo.findById(bn.getMaChucVu());
            if (cv != null && cv.getCapBac() <= 3) {
                // Giam doc (capBac=1): kiem tra toan cong ty, khong gioi han phong ban
                String deptToCheck = (cv.getCapBac() == 1) ? null : bn.getMaPhongBan();
                if (boNhiemRepo.hasActiveChinhForChucVuInDept(deptToCheck, bn.getMaChucVu(), 0)) {
                    String tenCV = cv.getTenChucVu() != null ? cv.getTenChucVu() : bn.getMaChucVu();
                    String scope = (cv.getCapBac() == 1) ? "trong cong ty" : "trong phong ban nay";
                    return KetQua.error("Chuc vu " + tenCV + " da co nguoi dang giu " + scope + ". Ket thuc bo nhiem cu truoc khi tao moi.");
                }
            }
        }
        // Thiết lập trạng thái chờ duyệt
        bn.setTrangThai(HRMConstants.TRANG_THAI_CHO_DUYET);
        try {
            boNhiemRepo.insert(bn);
            return KetQua.success(bn, "Tạo bổ nhiệm thành công. Đang chờ phê duyệt.");
        } catch (Exception e) {
            return KetQua.error("Lỗi khi tạo bổ nhiệm: " + e.getMessage());
        }
    }

    // ============================
    // Phê duyệt bổ nhiệm
    // ============================
    public KetQua<BoNhiem> pheDuyetBoNhiem(int maBoNhiem, String nguoiDuyetId) {
        BoNhiem bn = boNhiemRepo.findById(maBoNhiem);
        if (bn == null) {
            return KetQua.error("Không tìm thấy bổ nhiệm #" + maBoNhiem);
        }
        if (!HRMConstants.TRANG_THAI_CHO_DUYET.equals(bn.getTrangThai())) {
            return KetQua.error("Bổ nhiệm này không ở trạng thái chờ duyệt.");
        }
        if (SelfApprovalGuard.isSelfAction(nguoiDuyetId, bn.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự duyệt bổ nhiệm cho chính mình.");
        }
        LocalDateTime now = LocalDateTime.now();
        // Nếu là bổ nhiệm chính → kết thúc bổ nhiệm cũ (nếu có)
        if (isLoaiBoNhiemChinh(bn)) {
            // Kết thúc bổ nhiệm chính cũ của nhân viên
            BoNhiem cuHieuLucNV = boNhiemRepo.findBoNhiemChinhHieuLuc(bn.getMaNV());
            if (cuHieuLucNV != null && cuHieuLucNV.getId() != maBoNhiem) {
                boNhiemRepo.endBoNhiem(cuHieuLucNV.getId(), bn.getTuNgay().minusDays(1));
            }
        }
        // Cập nhật trạng thái và người duyệt
        boNhiemRepo.updateTrangThai(maBoNhiem, HRMConstants.TRANG_THAI_HIEU_LUC, now);
        boNhiemRepo.updateNguoiDuyet(maBoNhiem, nguoiDuyetId);
        bn.setTrangThai(HRMConstants.TRANG_THAI_HIEU_LUC);
        bn.setNgayPheDuyet(now);
        bn.setNguoiDuyet(nguoiDuyetId); // giả định nguoiDuyet là String trong model
        // Tự động tạo tài khoản + vai trò nếu là bổ nhiệm chính
        if (isLoaiBoNhiemChinh(bn)) {
            try {
                autoTaoTaiKhoanVaVaiTro(bn);
            } catch (Exception ex) {
                System.err.println("Cảnh báo: Tự động tạo tài khoản thất bại: " + ex.getMessage());
            }
        }
        return KetQua.success(bn, "Phê duyệt bổ nhiệm thành công.");
    }

    // ============================
    // Từ chối bổ nhiệm
    // ============================
    public KetQua<BoNhiem> tuChoiBoNhiem(int maBoNhiem, String lyDo) {
        if (ValidationUtils.isBlank(lyDo)) {
            return KetQua.error("Lý do từ chối không được để trống.");
        }
        BoNhiem bn = boNhiemRepo.findById(maBoNhiem);
        if (bn == null) {
            return KetQua.error("Không tìm thấy bổ nhiệm #" + maBoNhiem);
        }
        boNhiemRepo.updateTrangThai(maBoNhiem, HRMConstants.TRANG_THAI_TU_CHOI, null);
        boNhiemRepo.updateLyDoTuChoi(maBoNhiem, lyDo.trim());
        return KetQua.success(null, "Đã từ chối bổ nhiệm #" + maBoNhiem + ".");
    }

    // ============================
    // Lấy danh sách
    // ============================
    public List<BoNhiem> getAll() {
        return boNhiemRepo.findAll();
    }

    public List<BoNhiem> getAllByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(ACTION_APPOINTMENT_VIEW);
        return boNhiemRepo.findAllByScope(scope, currentMaNV);
    }

    public List<BoNhiem> getChoDuyet() {
        return boNhiemRepo.findChoDuyet();
    }

    public List<BoNhiem> getByMaNV(String maNV) {  // Đổi tham số thành String
        return boNhiemRepo.findByMaNV(maNV);
    }

    public BoNhiem getBoNhiemChinhHieuLuc(String maNV) {  // Đổi tham số thành String
        return boNhiemRepo.findBoNhiemChinhHieuLuc(maNV);
    }

    // ============================
    // Kết thúc bổ nhiệm
    // ============================
    public KetQua<BoNhiem> ketThucBoNhiem(int maBoNhiem, LocalDate denNgay) {
        if (denNgay == null) {
            return KetQua.error("Vui lòng chọn ngày kết thúc.");
        }
        BoNhiem bn = boNhiemRepo.findById(maBoNhiem);
        if (bn == null) {
            return KetQua.error("Không tìm thấy bổ nhiệm #" + maBoNhiem);
        }
        if (!HRMConstants.TRANG_THAI_HIEU_LUC.equals(bn.getTrangThai())) {
            return KetQua.error("Chỉ có thể kết thúc bổ nhiệm đang hiệu lực.");
        }
        // Không cho tự kết thúc bổ nhiệm của chính mình (nhất quán với tao/phe duyet)
        if (SelfApprovalGuard.isSelfAction(getCurrentUserNhanVienId(), bn.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự kết thúc bổ nhiệm của chính mình.");
        }
        // Bảo vệ bổ nhiệm CEO (capBac=1): chỉ ADMIN mới được kết thúc
        ChucVu cv = chucVuRepo.findById(bn.getMaChucVu());
        if (cv != null && cv.getCapBac() == 1) {
            SessionContext ctx = SessionContext.getInstance();
            if (!ctx.isAdmin()) {
                return KetQua.error("Chỉ ADMIN mới có thể kết thúc bổ nhiệm Tổng Giám Đốc.");
            }
        }
        if (bn.getTuNgay() != null && !denNgay.isAfter(bn.getTuNgay())) {
            return KetQua.error("Ngày kết thúc phải sau ngày bắt đầu (" + bn.getTuNgay() + ").");
        }
        try {
            boNhiemRepo.endBoNhiem(maBoNhiem, denNgay);
            // Cascade: chuyển cấp dưới lên cấp trên kế tiếp (grandparent)
            java.util.List<String> subordinates = boNhiemRepo.findActiveSubordinateNVIds(bn.getMaNV());
            if (!subordinates.isEmpty()) {
                boNhiemRepo.updateManagerForNVList(subordinates, bn.getMaQuanLy());
            }
            return KetQua.success(null, "Đã kết thúc bổ nhiệm #" + maBoNhiem + " từ ngày " + denNgay);
        } catch (Exception e) {
            return KetQua.error("Lỗi khi kết thúc bổ nhiệm: " + e.getMessage());
        }
    }

    // ============================
    // Tự động tạo tài khoản + vai trò (private helper)
    // ============================
    private void autoTaoTaiKhoanVaVaiTro(BoNhiem bn) {
        XacThucBUS authService = XacThucBUS.getInstance();
        NhanVien nv = nvRepo.findByMaNhanVien(bn.getMaNV());
        if (nv == null) return;
        // Xác định vai trò mặc định
        String roleCode = resolveDefaultRoleCode(authService);
        if (roleCode == null || roleCode.trim().isEmpty()) {
            System.err.println("Cảnh báo: Không tìm thấy vai trò mặc định để tạo tài khoản.");
            return;
        }
        // Kiểm tra tài khoản đã tồn tại chưa
        TaiKhoan existing = authService.findByMaNV(bn.getMaNV());
        if (existing == null) {
            // Tạo tài khoản mới
            String username = nv.getMaNhanVien();  // hoặc tạo username theo quy tắc khác
            ThongTinCaNhan ttcn = ttcnRepo.findByMaNV(bn.getMaNV());
            String password;
            if (ttcn != null && ttcn.getNgaySinh() != null) {
                password = ttcn.getNgaySinh().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                password = bn.getMaNV(); // fallback: dùng mã NV
            }
            String email = (ttcn != null) ? ttcn.getEmail() : null;
            KetQua<Integer> result = authService.createUser(
                    username, password, bn.getMaNV(), roleCode, email);
            if (!result.isSuccess()) {
                System.err.println("Tạo tài khoản tự động thất bại: " + result.getMessage());
            }
        }
        // Nếu tài khoản đã tồn tại → có thể cập nhật vai trò nếu cần (hiện tại giữ nguyên)
    }

    private String resolveDefaultRoleCode(XacThucBUS authService) {
        List<VaiTro> roles = authService.getAllRoles();
        if (roles == null || roles.isEmpty()) return null;
        // Ưu tiên tìm vai trò nhân viên chuẩn của hệ thống.
        for (VaiTro role : roles) {
            if (ROLE_EMPLOYEE.equalsIgnoreCase(role.getId())) {
                return role.getId();
            }
        }
        // Nếu không có → lấy vai trò đầu tiên không phải ADMIN
        for (VaiTro role : roles) {
            if (role.getId() != null && !ROLE_ADMIN.equalsIgnoreCase(role.getId())) {
                return role.getId();
            }
        }
        // Cuối cùng lấy vai trò đầu tiên
        return roles.get(0).getId();
    }

    private KetQua<Void> validateBoNhiemInput(BoNhiem bn) {
        if (ValidationUtils.isBlank(bn.getMaPhongBan())) {
            return KetQua.error("Phòng ban không được để trống.");
        }
        if (ValidationUtils.isBlank(bn.getMaChucVu())) {
            return KetQua.error("Chức vụ không được để trống.");
        }
        if (bn.getTuNgay() == null) {
            return KetQua.error("Ngày bắt đầu bổ nhiệm không được để trống.");
        }
        if (bn.getDenNgay() != null && !bn.getDenNgay().isAfter(bn.getTuNgay())) {
            return KetQua.error("Ngày kết thúc phải sau ngày bắt đầu (" + bn.getTuNgay() + ").");
        }
        if (bn.getTyLeHuongLuong() <= 0 || bn.getTyLeHuongLuong() > 100) {
            return KetQua.error("Tỷ lệ hưởng lương phải từ 1% đến 100%.");
        }
        return KetQua.success(null, "");
    }

    private boolean isLoaiBoNhiemChinh(BoNhiem bn) {
        return bn != null && LOAI_BO_NHIEM_CHINH.equals(bn.getLoaiBoNhiem());
    }

    private String getCurrentUserNhanVienId() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getMaNV() : null;
    }

}
