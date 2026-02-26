package com.hrm.service;

import com.hrm.model.BoNhiem;
import com.hrm.model.NhanVien;
import com.hrm.repo.BoNhiemRepository;
import com.hrm.repo.NhanVienRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service quản lý bổ nhiệm nhân viên.
 * Singleton pattern.
 */
public class BoNhiemService {

    private static BoNhiemService instance;

    private final BoNhiemRepository boNhiemRepo = BoNhiemRepository.getInstance();
    private final NhanVienRepository nvRepo = NhanVienRepository.getInstance();

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

        return ServiceResult.success(bn, "Phê duyệt bổ nhiệm thành công.");
    }

    // ============================
    // tuChoiBoNhiem
    // ============================

    public ServiceResult<BoNhiem> tuChoiBoNhiem(int maBoNhiem, String lyDo) {
        boNhiemRepo.updateTrangThai(maBoNhiem, "tu_choi", null);
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
}
