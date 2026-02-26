package com.hrm.service;

import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.repo.HopDongRepository;
import com.hrm.repo.NhanVienRepository;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service quản lý hợp đồng lao động.
 * Singleton pattern.
 */
public class HopDongService {

    private static HopDongService instance;

    private final HopDongRepository hopDongRepo = HopDongRepository.getInstance();
    private final NhanVienRepository nvRepo = NhanVienRepository.getInstance();

    private HopDongService() {
    }

    public static synchronized HopDongService getInstance() {
        if (instance == null) {
            instance = new HopDongService();
        }
        return instance;
    }

    // ============================
    // taoHopDong
    // ============================

    public ServiceResult<HopDongLaoDong> taoHopDong(HopDongLaoDong hd) {
        // Validate nhân viên
        NhanVien nv = nvRepo.findById(hd.getMaNV());
        if (nv == null) {
            return ServiceResult.error("Không tìm thấy nhân viên.");
        }

        // Validate số hợp đồng
        if (hd.getSoHopDong() == null || hd.getSoHopDong().trim().isEmpty()) {
            return ServiceResult.error("Số hợp đồng không được để trống.");
        }
        if (hopDongRepo.existsBySoHopDong(hd.getSoHopDong().trim(), 0)) {
            return ServiceResult.error("Số hợp đồng '" + hd.getSoHopDong() + "' đã tồn tại.");
        }

        // Validate ngày ký và ngày hiệu lực
        if (hd.getNgayKy() == null) {
            return ServiceResult.error("Ngày ký không được để trống.");
        }
        if (hd.getNgayHieuLuc() == null) {
            return ServiceResult.error("Ngày hiệu lực không được để trống.");
        }
        if (hd.getNgayHieuLuc().isBefore(hd.getNgayKy())) {
            return ServiceResult.error("Ngày hiệu lực phải bằng hoặc sau ngày ký.");
        }

        // Validate theo loại hợp đồng
        String loai = hd.getLoaiHopDong();
        if ("thu_viec".equals(loai)) {
            if (hd.getNgayHetHieuLuc() == null) {
                return ServiceResult.error("Hợp đồng thử việc phải có ngày hết hiệu lực.");
            }
            long soNgay = ChronoUnit.DAYS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
            if (soNgay > 60) {
                return ServiceResult.error("Hợp đồng thử việc không được vượt quá 60 ngày.");
            }
            if (soNgay <= 0) {
                return ServiceResult.error("Ngày hết hiệu lực phải sau ngày hiệu lực.");
            }
        } else if ("xac_dinh_thoi_han".equals(loai)) {
            if (hd.getNgayHetHieuLuc() == null) {
                return ServiceResult.error("Hợp đồng xác định thời hạn phải có ngày hết hiệu lực.");
            }
            long soThang = ChronoUnit.MONTHS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
            if (soThang > 36) {
                return ServiceResult.error("Hợp đồng xác định thời hạn không được vượt quá 36 tháng.");
            }
            if (hd.getNgayHetHieuLuc().isBefore(hd.getNgayHieuLuc())) {
                return ServiceResult.error("Ngày hết hiệu lực phải sau ngày hiệu lực.");
            }
        }
        // khong_xac_dinh: ngayHetHieuLuc có thể null

        // Kiểm tra nhân viên đã có hợp đồng hiệu lực chưa
        HopDongLaoDong existing = hopDongRepo.findHieuLuc(hd.getMaNV());
        if (existing != null) {
            return ServiceResult.error("Nhân viên đã có hợp đồng đang hiệu lực (số HĐ: "
                    + existing.getSoHopDong() + "). Hãy thanh lý hoặc hủy hợp đồng cũ trước.");
        }

        // Thiết lập trạng thái
        hd.setTrangThai("hieu_luc");

        try {
            hopDongRepo.insert(hd);

            // Đồng bộ loại hợp đồng lên NHANVIEN
            nv.setLoaiHopDong(hd.getLoaiHopDong());
            nvRepo.update(nv);

            return ServiceResult.success(hd, "Tạo hợp đồng lao động thành công.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi tạo hợp đồng: " + e.getMessage());
        }
    }

    // ============================
    // thanhLyHopDong
    // ============================

    public ServiceResult<Void> thanhLyHopDong(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) {
            return ServiceResult.error("Không tìm thấy hợp đồng.");
        }
        if ("thanh_ly".equals(hd.getTrangThai()) || "huy".equals(hd.getTrangThai())) {
            return ServiceResult.error("Hợp đồng đã được thanh lý hoặc hủy.");
        }
        hopDongRepo.updateTrangThai(maHopDong, "thanh_ly");
        return ServiceResult.success(null, "Thanh lý hợp đồng thành công.");
    }

    // ============================
    // huyHopDong
    // ============================

    public ServiceResult<Void> huyHopDong(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) {
            return ServiceResult.error("Không tìm thấy hợp đồng.");
        }
        if ("huy".equals(hd.getTrangThai())) {
            return ServiceResult.error("Hợp đồng đã được hủy.");
        }
        hopDongRepo.updateTrangThai(maHopDong, "huy");
        return ServiceResult.success(null, "Hủy hợp đồng thành công.");
    }

    // ============================
    // Getters
    // ============================

    public List<HopDongLaoDong> getSapHetHan(int soNgay) {
        return hopDongRepo.findSapHetHan(soNgay);
    }

    public List<HopDongLaoDong> getByMaNV(int maNV) {
        return hopDongRepo.findByMaNV(maNV);
    }

    public List<HopDongLaoDong> getAll() {
        return hopDongRepo.findAll();
    }

    public HopDongLaoDong getHieuLuc(int maNV) {
        return hopDongRepo.findHieuLuc(maNV);
    }

    // ============================
    // Private helper
    // ============================

    private HopDongLaoDong findById(int maHopDong) {
        for (HopDongLaoDong hd : hopDongRepo.findAll()) {
            if (hd.getMaHopDong() == maHopDong) {
                return hd;
            }
        }
        return null;
    }
}
