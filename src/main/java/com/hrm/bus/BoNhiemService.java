package com.hrm.service;

import com.hrm.model.BoNhiem;
import com.hrm.model.NhanVien;
import com.hrm.model.Position;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.User;
import com.hrm.repo.BoNhiemRepository;
import com.hrm.repo.NhanVienRepository;
import com.hrm.repo.PositionRepository;
import com.hrm.repo.ThongTinCaNhanRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service quản lý bổ nhiệm nhân viên.
 * Singleton pattern.
 */
public class BoNhiemService {

    private static BoNhiemService instance;

    private final BoNhiemRepository boNhiemRepo = BoNhiemRepository.getInstance();
    private final NhanVienRepository nvRepo = NhanVienRepository.getInstance();
    private final PositionRepository positionRepo = new PositionRepository();
    private final ThongTinCaNhanRepository ttcnRepo = ThongTinCaNhanRepository.getInstance();

    private BoNhiemService() {
    }

    public static synchronized BoNhiemService getInstance() {
        if (instance == null) {
            instance = new BoNhiemService();
        }
        return instance;
    }

    // ============================
    // taoBoNhiem
    // ============================

    public ServiceResult<BoNhiem> taoBoNhiem(BoNhiem bn) {
        // Validate nhân viên tồn tại và đang làm việc
        NhanVien nv = nvRepo.findById(bn.getMaNV());
        if (nv == null) {
            return ServiceResult.error("Không tìm thấy nhân viên.");
        }
        if (!"dang_lam_viec".equals(nv.getTrangThai())) {
            return ServiceResult.error("Nhân viên không ở trạng thái đang làm việc, không thể bổ nhiệm.");
        }

        // Validate phòng ban (sẽ lấy từ DB trong query - không cần gọi thêm service)
        if (bn.getMaPhongBan() == null || bn.getMaPhongBan().trim().isEmpty()) {
            return ServiceResult.error("Phòng ban không được để trống.");
        }

        // Validate chức vụ
        if (bn.getMaChucVu() == null || bn.getMaChucVu().trim().isEmpty()) {
            return ServiceResult.error("Chức vụ không được để trống.");
        }

        // Validate ngày
        if (bn.getTuNgay() == null) {
            return ServiceResult.error("Ngày bắt đầu không được để trống.");
        }
        if (bn.getDenNgay() != null && !bn.getDenNgay().isAfter(bn.getTuNgay())) {
            return ServiceResult.error("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        // Validate tỷ lệ hưởng lương
        if (bn.getTyLeHuongLuong() <= 0 || bn.getTyLeHuongLuong() > 100) {
            return ServiceResult.error("Tỷ lệ hưởng lương phải từ 1% đến 100%.");
        }

        // Nếu là bổ nhiệm chính, kiểm tra conflict
        if ("chinh".equals(bn.getLoaiBoNhiem())) {
            boolean conflict = boNhiemRepo.hasConflictingChinhBoNhiem(
                    bn.getMaNV(), bn.getTuNgay(), bn.getDenNgay(), 0);
            if (conflict) {
                return ServiceResult.error(
                        "Nhân viên đã có bổ nhiệm chính hiệu lực trong khoảng thời gian này. "
                        + "Hãy kết thúc bổ nhiệm cũ trước.");
            }
        }

        // Thiết lập trạng thái chờ duyệt
        bn.setTrangThai("cho_duyet");

        try {
            boNhiemRepo.insert(bn);
            return ServiceResult.success(bn, "Tạo bổ nhiệm thành công. Đang chờ phê duyệt.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi tạo bổ nhiệm: " + e.getMessage());
        }
    }

    // ============================
    // pheDuyetBoNhiem
    // ============================

    public ServiceResult<BoNhiem> pheDuyetBoNhiem(int maBoNhiem, int nguoiDuyetId) {
        // Tải bổ nhiệm từ DB
        List<BoNhiem> all = boNhiemRepo.findAll();
        BoNhiem bn = null;
        for (BoNhiem b : all) {
            if (b.getMaBoNhiem() == maBoNhiem) {
                bn = b;
                break;
            }
        }

        if (bn == null) {
            return ServiceResult.error("Không tìm thấy bổ nhiệm.");
        }
        if (!"cho_duyet".equals(bn.getTrangThai())) {
            return ServiceResult.error("Bổ nhiệm này không ở trạng thái chờ duyệt.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Nếu là bổ nhiệm chính: kết thúc bổ nhiệm chính cũ đang hiệu lực
        if ("chinh".equals(bn.getLoaiBoNhiem())) {
            BoNhiem cuHieuLuc = boNhiemRepo.findBoNhiemChinhHieuLuc(bn.getMaNV());
            if (cuHieuLuc != null && cuHieuLuc.getMaBoNhiem() != maBoNhiem) {
                boNhiemRepo.endBoNhiem(cuHieuLuc.getMaBoNhiem(), bn.getTuNgay().minusDays(1));
            }
        }

        // Cập nhật trạng thái phê duyệt
        boNhiemRepo.updateTrangThai(maBoNhiem, "hieu_luc", now);
        boNhiemRepo.updateNguoiDuyet(maBoNhiem, nguoiDuyetId);

        bn.setTrangThai("hieu_luc");
        bn.setNgayPheDuyet(now);
        bn.setNguoiDuyet(nguoiDuyetId);

        // Tự động tạo tài khoản và vai trò khi phê duyệt bổ nhiệm chính
        if ("chinh".equals(bn.getLoaiBoNhiem())) {
            try {
                autoTaoTaiKhoanVaVaiTro(bn);
            } catch (Exception ex) {
                System.err.println("Canh bao tu tao tai khoan: " + ex.getMessage());
            }
        }

        return ServiceResult.success(bn, "Phê duyệt bổ nhiệm thành công.");
    }

    // ============================
    // tuChoiBoNhiem
    // ============================

    public ServiceResult<BoNhiem> tuChoiBoNhiem(int maBoNhiem, String lyDo) {
        if (lyDo == null || lyDo.trim().isEmpty()) {
            return ServiceResult.error("Lý do từ chối không được để trống.");
        }
        boNhiemRepo.updateTrangThai(maBoNhiem, "tu_choi", null);
        boNhiemRepo.updateLyDoTuChoi(maBoNhiem, lyDo.trim());
        return ServiceResult.success(null, "Đã từ chối bổ nhiệm.");
    }

    // ============================
    // Getters
    // ============================

    public List<BoNhiem> getAll() {
        return boNhiemRepo.findAll();
    }

    public List<BoNhiem> getChoDuyet() {
        return boNhiemRepo.findChoDuyet();
    }

    public List<BoNhiem> getByMaNV(int maNV) {
        return boNhiemRepo.findByMaNV(maNV);
    }

    public BoNhiem getBoNhiemChinhHieuLuc(int maNV) {
        return boNhiemRepo.findBoNhiemChinhHieuLuc(maNV);
    }

    // ============================
    // autoTaoTaiKhoanVaVaiTro (private helper)
    // ============================

    private void autoTaoTaiKhoanVaVaiTro(BoNhiem bn) {
        AuthService authService = AuthService.getInstance();
        NhanVien nv = nvRepo.findById(bn.getMaNV());
        if (nv == null) return;

        // Bước 1: Xác định vai trò từ chức vụ
        String maChucVu = bn.getMaChucVu();
        Position pos = positionRepo.findById(maChucVu);
        String roleCode = "ROLE_" + maChucVu.toUpperCase();
        String roleName = (pos != null) ? pos.getTenChucVu() : maChucVu;

        // Tạo vai trò nếu chưa có
        if (authService.getRoleByCode(roleCode) == null) {
            authService.createRole(roleCode, roleName, "Tu dong tao khi phe duyet bo nhiem");
        }

        // Bước 2: Kiểm tra tài khoản nhân viên
        User existingUser = authService.findByMaNV(bn.getMaNV());

        if (existingUser == null) {
            // Tạo tài khoản mới
            String username = nv.getMaNhanVien();
            ThongTinCaNhan ttcn = ttcnRepo.findByMaNV(bn.getMaNV());
            String password;
            if (ttcn != null && ttcn.getNgaySinh() != null) {
                password = ttcn.getNgaySinh().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                password = nv.getMaNhanVien(); // fallback: dùng mã NV
            }
            String email = (ttcn != null) ? ttcn.getEmail() : null;
            authService.createUser(username, password, bn.getMaNV(), roleCode, email);
        } else {
            // Tài khoản đã tồn tại: cập nhật vai trò
            authService.assignRoleToUser(existingUser.getId(), roleCode);
        }
    }
}
