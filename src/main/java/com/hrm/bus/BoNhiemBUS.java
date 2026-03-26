package com.hrm.bus;

import com.hrm.model.BoNhiem;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.TaiKhoan;
import com.hrm.model.VaiTro;
import com.hrm.model.YeuCauTuyenDung;
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
import java.util.Collections;
import java.util.List;

/**
 * Service quản lý bổ nhiệm nhân viên.
 * Singleton pattern.
 */
public class BoNhiemBUS {

    private static final String ACTION_APPOINTMENT_VIEW = PermissionCodes.APPOINTMENT_VIEW;
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
        // Chặn bổ nhiệm (mọi loại) nếu chức vụ lãnh đạo (capBac <= 3) đã có người giữ
        ChucVu cvCheck = chucVuRepo.findById(bn.getMaChucVu());
        if (cvCheck != null && cvCheck.getCapBac() <= 3) {
            String deptToCheck = (cvCheck.getCapBac() == 1) ? null : bn.getMaPhongBan();
            if (boNhiemRepo.hasActiveChinhForChucVuInDept(deptToCheck, bn.getMaChucVu(), 0)) {
                String tenCV = cvCheck.getTenChucVu() != null ? cvCheck.getTenChucVu() : bn.getMaChucVu();
                String scope = (cvCheck.getCapBac() == 1) ? "trong công ty" : "trong phòng ban này";
                return KetQua.error("Chức vụ " + tenCV + " đã có người đang giữ " + scope + ". Kết thúc bổ nhiệm cũ trước khi tạo mới.");
            }
        }
        // Kiểm tra xung đột nếu là bổ nhiệm chính
        if (isLoaiBoNhiemChinh(bn)) {
            boolean conflict = boNhiemRepo.hasConflictingChinhBoNhiem(
                    bn.getMaNV(), bn.getTuNgay(), bn.getDenNgay(), 0);
            if (conflict) {
                return KetQua.error(
                        "Nhân viên đã có bổ nhiệm chính hiệu lực trong khoảng thời gian này. "
                        + "Hãy kết thúc bổ nhiệm cũ trước khi tạo mới.");
            }
        }
        // thiết lập trạng thái ban đầu là chờ duyệt
        bn.setTrangThai(HRMConstants.TRANG_THAI_CHO_DUYET);
        try {
            int id = boNhiemRepo.insert(bn);
            if (id <= 0) {
                return KetQua.error("Không thể tạo bổ nhiệm. Vui lòng thử lại.");
            }
            return KetQua.success(bn, "Tạo bổ nhiệm thành công. Đang chờ phê duyệt.");
        } catch (Exception e) {
            return KetQua.error("Lỗi khi tạo bổ nhiệm: " + e.getMessage());
        }
    }

    // ============================
    // Tạo bổ nhiệm từ hợp đồng thử việc (gọi bởi HopDongBUS khi phê duyệt HĐ)
    // ============================
    public KetQua<Void> taoBoNhiemTuHopDong(String maNV, YeuCauTuyenDung yc, LocalDate tuNgay, int maHopDong) {
        BoNhiem bn = new BoNhiem();
        bn.setMaNV(maNV);
        bn.setMaPhongBan(yc.getId());
        bn.setMaChucVu(yc.getMaChucVu());
        bn.setLoaiBoNhiem(HRMConstants.LOAI_BO_NHIEM_CHINH);
        bn.setTyLeHuongLuong(100);
        bn.setTuNgay(tuNgay != null ? tuNgay : LocalDate.now());
        bn.setTrangThai(HRMConstants.TRANG_THAI_CHO_DUYET);
        bn.setLyDo("Tu dong bo nhiem sau khi phe duyet hop dong thu viec #" + maHopDong);
        ChucVu cvHD = chucVuRepo.findById(bn.getMaChucVu());
        if (cvHD != null && cvHD.getCapBac() <= 3) {
            String deptToCheck = (cvHD.getCapBac() == 1) ? null : bn.getMaPhongBan();
            if (boNhiemRepo.hasActiveChinhForChucVuInDept(deptToCheck, bn.getMaChucVu(), 0)) {
                String tenCV = cvHD.getTenChucVu() != null ? cvHD.getTenChucVu() : bn.getMaChucVu();
                String scope = (cvHD.getCapBac() == 1) ? "trong cong ty" : "trong phong ban nay";
                return KetQua.error("Chuc vu " + tenCV + " da co nguoi dang giu " + scope + ". Ket thuc bo nhiem cu truoc khi tao moi.");
            }
        }
        try {
            int id = boNhiemRepo.insert(bn);
            if (id <= 0) {
                return KetQua.error("Không thể tạo bổ nhiệm tự động. Vui lòng thử lại.");
            }
            return KetQua.success(null, "Bổ nhiệm đã được tạo tự động.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo bổ nhiệm tự động: " + e.getMessage());
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
        // Chặn duyệt nếu chức vụ lãnh đạo (capBac <= 3) đã có người giữ
        ChucVu cvDuyet = chucVuRepo.findById(bn.getMaChucVu());
        if (cvDuyet != null && cvDuyet.getCapBac() <= 3) {
            String deptToCheck = (cvDuyet.getCapBac() == 1) ? null : bn.getMaPhongBan();
            if (boNhiemRepo.hasActiveChinhForChucVuInDept(deptToCheck, bn.getMaChucVu(), maBoNhiem)) {
                String tenCV = cvDuyet.getTenChucVu() != null ? cvDuyet.getTenChucVu() : bn.getMaChucVu();
                String scope = (cvDuyet.getCapBac() == 1) ? "trong cong ty" : "trong phong ban nay";
                return KetQua.error("Chuc vu " + tenCV + " da co nguoi dang giu " + scope + ". Ket thuc bo nhiem cu truoc khi duyet.");
            }
        }
        // Nếu là bổ nhiệm chính → kết thúc bổ nhiệm cũ (nếu có)
        if (isLoaiBoNhiemChinh(bn)) {
            // Kết thúc bổ nhiệm chính cũ của nhân viên
            BoNhiem cuHieuLucNV = boNhiemRepo.findBoNhiemChinhHieuLuc(bn.getMaNV());
            if (cuHieuLucNV != null && cuHieuLucNV.getId() != maBoNhiem) {
                boNhiemRepo.endBoNhiem(cuHieuLucNV.getId(), bn.getTuNgay().minusDays(1));
            }
        }
        // Cập nhật trạng thái và người duyệt
        try {
            int rows = boNhiemRepo.updateTrangThai(maBoNhiem, HRMConstants.TRANG_THAI_HIEU_LUC, now);
            if (rows <= 0) {
                return KetQua.error("Không thể cập nhật trạng thái bổ nhiệm. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            return KetQua.error("Lỗi phê duyệt bổ nhiệm: " + e.getMessage());
        }
        try {
            int updRows = boNhiemRepo.updateNguoiDuyet(maBoNhiem, nguoiDuyetId);
            if (updRows <= 0) {
                System.err.println("Cảnh báo: Không thể cập nhật người duyệt cho bổ nhiệm " + maBoNhiem);
            }
        } catch (Exception e) {
            System.err.println("Cảnh báo: Không thể cập nhật người duyệt cho bổ nhiệm " + maBoNhiem + ": " + e.getMessage());
        }
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
        try {
            int rows = boNhiemRepo.updateTrangThai(maBoNhiem, HRMConstants.TRANG_THAI_TU_CHOI, null);
            if (rows <= 0) {
                return KetQua.error("Không thể từ chối bổ nhiệm. Vui lòng thử lại.");
            }
            int lyDoRows = boNhiemRepo.updateLyDoTuChoi(maBoNhiem, lyDo.trim());
            if (lyDoRows <= 0) {
                System.err.println("Cảnh báo: Không thể lưu lý do từ chối cho bổ nhiệm " + maBoNhiem);
            }
        } catch (Exception e) {
            return KetQua.error("Lỗi từ chối bổ nhiệm: " + e.getMessage());
        }
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

    public List<BoNhiem> getByMaNVInScope(String targetMaNV) {
        return canViewEmployeeAppointments(targetMaNV)
                ? boNhiemRepo.findByMaNV(targetMaNV)
                : Collections.emptyList();
    }

    public BoNhiem getBoNhiemChinhHieuLuc(String maNV) {  // Đổi tham số thành String
        return boNhiemRepo.findBoNhiemChinhHieuLuc(maNV);
    }

    public BoNhiem getBoNhiemChinhHieuLucInScope(String targetMaNV) {
        return canViewEmployeeAppointments(targetMaNV)
                ? boNhiemRepo.findBoNhiemChinhHieuLuc(targetMaNV)
                : null;
    }

    public boolean canViewEmployeeAppointments(String targetMaNV) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || ValidationUtils.isBlank(targetMaNV)) {
            return false;
        }
        if (SessionContext.getInstance().isAdmin()) {
            return true;
        }
        if (!currentUser.coQuyen(ACTION_APPOINTMENT_VIEW)) {
            return false;
        }
        String currentMaNV = currentUser.getMaNV();
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction(ACTION_APPOINTMENT_VIEW);
        if (scope == com.hrm.model.DataScope.ALL) {
            return true;
        }
        if (scope == com.hrm.model.DataScope.NONE || ValidationUtils.isBlank(currentMaNV)) {
            return false;
        }
        if (scope == com.hrm.model.DataScope.SELF) {
            return targetMaNV.equalsIgnoreCase(currentMaNV);
        }
        for (NhanVien nv : NhanVienBUS.getInstance().getAllByActionScope(ACTION_APPOINTMENT_VIEW, currentMaNV)) {
            if (targetMaNV.equalsIgnoreCase(nv.getMaNhanVien())) {
                return true;
            }
        }
        return false;
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
        // Không cho tự  kết thúc bổ nhiệm của chnh mnh (nhất qun với tạo/phê duyệt)
        if (SelfApprovalGuard.isSelfAction(getCurrentUserNhanVienId(), bn.getMaNV())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể kết thúc bổ nhiệm của chính mình.");
        }
        // Bảo vệ bổ nhiệm CEO (capBac=1): chỉ ADMIN mới được kết thúc, dù có phải bổ nhiệm của mình hay không
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
            int rows = boNhiemRepo.endBoNhiem(maBoNhiem, denNgay);
            if (rows <= 0) {
                return KetQua.error("Không thể kết thúc bổ nhiệm. Vui lòng thử lại.");
            }
            // Cascade: chuyển cấp dưới lên cấp trên kế tiếp (grandparent)
            java.util.List<String> subordinates = boNhiemRepo.findActiveSubordinateNVIds(bn.getMaNV());
            if (!subordinates.isEmpty()) {
                int updRows = boNhiemRepo.updateManagerForNVList(subordinates, bn.getMaQuanLy());
                if (updRows <= 0) {
                    System.err.println("Canh bao: Khong the cap nhat quan ly cho cap duoi cua bo nhiem " + maBoNhiem);
                }
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
        // Resolve role từ ChucVu.maVaiTro; fallback về NHAN_VIEN nếu chưa có mapping
        String roleCode = resolveRoleFromChucVu(bn.getMaChucVu(), authService);
        if (roleCode == null) {
            roleCode = ROLE_EMPLOYEE;
            System.err.println("Cảnh báo: Chức vụ '" + bn.getMaChucVu()
                    + "' chưa có mapping vai trò, sử dụng mặc định: " + ROLE_EMPLOYEE);
        }
        // Kiểm tra tài khoản đã tồn tại chưa
        TaiKhoan existing = authService.findByMaNV(bn.getMaNV());
        if (existing == null) {
            // Tạo tài khoản mới
            String username = nv.getMaNhanVien();
            ThongTinCaNhan ttcn = ttcnRepo.findByMaNV(bn.getMaNV());
            String password;
            if (ttcn != null && ttcn.getNgaySinh() != null) {
                password = ttcn.getNgaySinh().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                password = bn.getMaNV();
            }
            String email = (ttcn != null) ? ttcn.getEmail() : null;
            KetQua<Integer> result = authService.createUser(
                    username, password, bn.getMaNV(), roleCode, email);
            if (!result.isSuccess()) {
                System.err.println("Tạo tài khoản tự động thất bại: " + result.getMessage());
            }
        } else {
            // Tài khoản đã tồn tại → cập nhật role theo chức vụ mới (thăng chức)
            String mappedRole = resolveRoleFromChucVu(bn.getMaChucVu(), authService);
            if (mappedRole != null) {
                KetQua<Void> upd = authService.updateUserRole(bn.getMaNV(), mappedRole);
                if (!upd.isSuccess()) {
                    System.err.println("Cập nhật vai trò tự động thất bại: " + upd.getMessage());
                }
            }
        }
    }

    private String resolveRoleFromChucVu(String maChucVu, XacThucBUS authService) {
        if (maChucVu == null || maChucVu.trim().isEmpty()) return null;
        ChucVu cv = ChucVuDAO.getInstance().findById(maChucVu);
        if (cv == null || cv.getMaVaiTro() == null || cv.getMaVaiTro().trim().isEmpty()) return null;
        // Verify role actually exists in system
        VaiTro vt = authService.getRoleByCode(cv.getMaVaiTro().trim());
        return (vt != null) ? vt.getId() : null;
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
        return bn != null && HRMConstants.LOAI_BO_NHIEM_CHINH.equals(bn.getLoaiBoNhiem());
    }

    private String getCurrentUserNhanVienId() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getMaNV() : null;
    }

}
