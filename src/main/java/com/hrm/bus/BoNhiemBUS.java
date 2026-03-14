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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service quản lý bổ nhiệm nhân viên.
 * Singleton pattern.
 */
public class BoNhiemBUS {

    private static BoNhiemBUS instance;

    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();
    private final ThongTinCaNhanDAO ttcnRepo = ThongTinCaNhanDAO.getInstance();
    private final ChucVuDAO chucVuRepo = new ChucVuDAO();

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
        if (SelfApprovalGuard.isSelfAction(getCurrentUserNhanVienId(), bn.getNhanVienId())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự tạo bổ nhiệm cho chính mình.");
        }

        // Validate nhân viên tồn tại và đang làm việc
        NhanVien nv = nvRepo.findByMaNhanVien(bn.getNhanVienId());
        if (nv == null) {
            return KetQua.error("Không tìm thấy nhân viên với mã: " + bn.getNhanVienId());
        }
        if (!"dang_lam_viec".equals(nv.getTrangThai())) {
            return KetQua.error("Nhân viên không ở trạng thái đang làm việc, không thể bổ nhiệm.");
        }

        // Validate phòng ban
        if (bn.getPhongBanId() == null || bn.getPhongBanId().trim().isEmpty()) {
            return KetQua.error("Phòng ban không được để trống.");
        }

        // Validate chức vụ
        if (bn.getChucVuId() == null || bn.getChucVuId().trim().isEmpty()) {
            return KetQua.error("Chức vụ không được để trống.");
        }

        // Validate ngày bắt đầu
        if (bn.getTuNgay() == null) {
            return KetQua.error("Ngày bắt đầu bổ nhiệm không được để trống.");
        }

        // Validate ngày kết thúc (nếu có)
        if (bn.getDenNgay() != null && !bn.getDenNgay().isAfter(bn.getTuNgay())) {
            return KetQua.error("Ngày kết thúc phải sau ngày bắt đầu (" + bn.getTuNgay() + ").");
        }

        // Validate tỷ lệ hưởng lương
        if (bn.getTyLeHuongLuong() <= 0 || bn.getTyLeHuongLuong() > 100) {
            return KetQua.error("Tỷ lệ hưởng lương phải từ 1% đến 100%.");
        }

        // Kiểm tra xung đột nếu là bổ nhiệm chính
        if ("chinh".equals(bn.getLoaiBoNhiem())) {
            boolean conflict = boNhiemRepo.hasConflictingChinhBoNhiem(
                    bn.getNhanVienId(), bn.getTuNgay(), bn.getDenNgay(), 0);
            if (conflict) {
                return KetQua.error(
                        "Nhân viên đã có bổ nhiệm chính hiệu lực trong khoảng thời gian này. "
                        + "Hãy kết thúc bổ nhiệm cũ trước.");
            }
            // Không chặn tạo bổ nhiệm khi chức vụ đã có người giữ —
            // phê duyệt sẽ tự động kết thúc bổ nhiệm cũ (nếu là chức vụ độc nhất như TPhòng).
            // Đây cho phép tuyển nhiều nhân viên cùng chức vụ trong một phòng ban bình thường.
        }

        // Thiết lập trạng thái chờ duyệt
        bn.setTrangThai("cho_duyet");

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

        if (!"cho_duyet".equals(bn.getTrangThai())) {
            return KetQua.error("Bổ nhiệm này không ở trạng thái chờ duyệt.");
        }

        if (SelfApprovalGuard.isSelfAction(nguoiDuyetId, bn.getNhanVienId())
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự duyệt bổ nhiệm cho chính mình.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Nếu là bổ nhiệm chính → kết thúc bổ nhiệm cũ (nếu có)
        if ("chinh".equals(bn.getLoaiBoNhiem())) {
            // Kết thúc bổ nhiệm chính cũ của nhân viên
            BoNhiem cuHieuLucNV = boNhiemRepo.findBoNhiemChinhHieuLuc(bn.getNhanVienId());
            if (cuHieuLucNV != null && cuHieuLucNV.getMaBoNhiem() != maBoNhiem) {
                boNhiemRepo.endBoNhiem(cuHieuLucNV.getMaBoNhiem(), bn.getTuNgay().minusDays(1));
            }

        }

        // Cập nhật trạng thái và người duyệt
        boNhiemRepo.updateTrangThai(maBoNhiem, "hieu_luc", now);
        boNhiemRepo.updateNguoiDuyet(maBoNhiem, nguoiDuyetId);

        bn.setTrangThai("hieu_luc");
        bn.setNgayPheDuyet(now);
        bn.setNguoiDuyet(nguoiDuyetId); // giả định nguoiDuyet là String trong model

        // Tự động tạo tài khoản + vai trò nếu là bổ nhiệm chính
        if ("chinh".equals(bn.getLoaiBoNhiem())) {
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
        if (lyDo == null || lyDo.trim().isEmpty()) {
            return KetQua.error("Lý do từ chối không được để trống.");
        }

        BoNhiem bn = boNhiemRepo.findById(maBoNhiem);
        if (bn == null) {
            return KetQua.error("Không tìm thấy bổ nhiệm #" + maBoNhiem);
        }

        boNhiemRepo.updateTrangThai(maBoNhiem, "tu_choi", null);
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
        com.hrm.model.DataScope scope = XacThucBUS.getInstance().getScopeForAction("APPOINTMENT_VIEW");
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

        if (!"hieu_luc".equals(bn.getTrangThai())) {
            return KetQua.error("Chỉ có thể kết thúc bổ nhiệm đang hiệu lực.");
        }

        if (bn.getTuNgay() != null && !denNgay.isAfter(bn.getTuNgay())) {
            return KetQua.error("Ngày kết thúc phải sau ngày bắt đầu (" + bn.getTuNgay() + ").");
        }

        try {
            boNhiemRepo.endBoNhiem(maBoNhiem, denNgay);
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
        NhanVien nv = nvRepo.findByMaNhanVien(bn.getNhanVienId());
        if (nv == null) return;

        // Xác định vai trò mặc định
        String roleCode = resolveDefaultRoleCode(authService);
        if (roleCode == null || roleCode.trim().isEmpty()) {
            System.err.println("Cảnh báo: Không tìm thấy vai trò mặc định để tạo tài khoản.");
            return;
        }

        // Kiểm tra tài khoản đã tồn tại chưa
        TaiKhoan existing = authService.findByMaNV(bn.getNhanVienId());

        if (existing == null) {
            // Tạo tài khoản mới
            String username = nv.getMaNhanVien();  // hoặc tạo username theo quy tắc khác
            ThongTinCaNhan ttcn = ttcnRepo.findByMaNV(bn.getNhanVienId());

            String password;
            if (ttcn != null && ttcn.getNgaySinh() != null) {
                password = ttcn.getNgaySinh().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                password = bn.getNhanVienId(); // fallback: dùng mã NV
            }

            String email = (ttcn != null) ? ttcn.getEmail() : null;

            KetQua<Integer> result = authService.createUser(
                    username, password, bn.getNhanVienId(), roleCode, email);

            if (!result.isSuccess()) {
                System.err.println("Tạo tài khoản tự động thất bại: " + result.getMessage());
            }
        }
        // Nếu tài khoản đã tồn tại → có thể cập nhật vai trò nếu cần (hiện tại giữ nguyên)
    }

    private String resolveDefaultRoleCode(XacThucBUS authService) {
        List<VaiTro> roles = authService.getAllRoles();
        if (roles == null || roles.isEmpty()) return null;

        // Ưu tiên tìm "NHAN_VIEN" hoặc "EMPLOYEE"
        for (VaiTro role : roles) {
            if ("NHAN_VIEN".equalsIgnoreCase(role.getId()) ||
                "EMPLOYEE".equalsIgnoreCase(role.getId())) {
                return role.getId();
            }
        }

        // Nếu không có → lấy vai trò đầu tiên không phải ADMIN
        for (VaiTro role : roles) {
            if (role.getId() != null && !"ADMIN".equalsIgnoreCase(role.getId())) {
                return role.getId();
            }
        }

        // Cuối cùng lấy vai trò đầu tiên
        return roles.get(0).getId();
    }
    private String getCurrentUserNhanVienId() {
        TaiKhoan currentUser = com.hrm.util.SessionContext.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getNhanVienId() : null;
    }
}
