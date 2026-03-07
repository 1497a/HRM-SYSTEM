package com.hrm.bus;

import com.hrm.model.HopDongLaoDong;
import com.hrm.model.NhanVien;
import com.hrm.dao.HopDongDAO;
import com.hrm.dao.NhanVienDAO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service quản lý hợp đồng lao động.
 * Singleton pattern.
 */
public class HopDongBUS {

    private static HopDongBUS instance;

    private final HopDongDAO hopDongRepo = HopDongDAO.getInstance();
    private final NhanVienDAO nvRepo = NhanVienDAO.getInstance();

    private HopDongBUS() {
    }

    public static synchronized HopDongBUS getInstance() {
        if (instance == null) {
            instance = new HopDongBUS();
        }
        return instance;
    }

    // ============================
    // taoHopDong
    // ============================

    public KetQua<HopDongLaoDong> taoHopDong(HopDongLaoDong hd) {
        // Validate nhân viên
        NhanVien nv = nvRepo.findByMaNhanVien(hd.getMaNV());
        if (nv == null) {
            return KetQua.error("Không tìm thấy nhân viên.");
        }

        // Tự động tạo số hợp đồng
        LocalDate today = LocalDate.now();
        int seq = hopDongRepo.countByYearMonth(today.getYear(), today.getMonthValue()) + 1;
        String soHopDong = String.format("HD-%04d%02d-%04d", today.getYear(), today.getMonthValue(), seq);
        hd.setSoHopDong(soHopDong);

        // Validate ngày ký và ngày hiệu lực
        if (hd.getNgayKy() == null) {
            return KetQua.error("Ngày ký không được để trống.");
        }
        if (hd.getNgayHieuLuc() == null) {
            return KetQua.error("Ngày hiệu lực không được để trống.");
        }
        if (hd.getNgayHieuLuc().isBefore(hd.getNgayKy())) {
            return KetQua.error("Ngày hiệu lực phải bằng hoặc sau ngày ký.");
        }

        // Validate theo loại hợp đồng
        String loai = hd.getLoaiHopDong();
        if ("thu_viec".equals(loai)) {
            if (hd.getNgayHetHieuLuc() == null) {
                return KetQua.error("Hợp đồng thử việc phải có ngày hết hiệu lực.");
            }
            long soNgay = ChronoUnit.DAYS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
            if (soNgay > 60) {
                return KetQua.error("Hợp đồng thử việc không được vượt quá 60 ngày.");
            }
            if (soNgay <= 0) {
                return KetQua.error("Ngày hết hiệu lực phải sau ngày hiệu lực.");
            }
        } else if ("xac_dinh_thoi_han".equals(loai)) {
            if (hd.getNgayHetHieuLuc() == null) {
                return KetQua.error("Hợp đồng xác định thời hạn phải có ngày hết hiệu lực.");
            }
            long soThang = ChronoUnit.MONTHS.between(hd.getNgayHieuLuc(), hd.getNgayHetHieuLuc());
            if (soThang > 36) {
                return KetQua.error("Hợp đồng xác định thời hạn không được vượt quá 36 tháng.");
            }
            if (hd.getNgayHetHieuLuc().isBefore(hd.getNgayHieuLuc())) {
                return KetQua.error("Ngày hết hiệu lực phải sau ngày hiệu lực.");
            }
        }
        // khong_xac_dinh: ngayHetHieuLuc có thể null

        // Kiểm tra nhân viên đã có hợp đồng hiệu lực chưa
        HopDongLaoDong existing = hopDongRepo.findHieuLuc(hd.getMaNV());
        if (existing != null) {
            return KetQua.error("Nhân viên đã có hợp đồng đang hiệu lực (số HĐ: "
                    + existing.getSoHopDong() + "). Hãy thanh lý hoặc hủy hợp đồng cũ trước.");
        }

        // Thiết lập trạng thái
        hd.setTrangThai("hieu_luc");

        try {
            hopDongRepo.insert(hd);

            // Đồng bộ loại hợp đồng lên NHANVIEN
            nv.setLoaiHopDong(hd.getLoaiHopDong());
            nvRepo.update(nv);

            return KetQua.success(hd, "Tạo hợp đồng lao động thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo hợp đồng: " + e.getMessage());
        }
    }

    // ============================
    // thanhLyHopDong
    // ============================

    public KetQua<Void> thanhLyHopDong(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) {
            return KetQua.error("Không tìm thấy hợp đồng.");
        }
        if ("thanh_ly".equals(hd.getTrangThai()) || "huy".equals(hd.getTrangThai())) {
            return KetQua.error("Hợp đồng đã được thanh lý hoặc hủy.");
        }
        hopDongRepo.updateTrangThai(maHopDong, "thanh_ly");
        return KetQua.success(null, "Thanh lý hợp đồng thành công.");
    }

    // ============================
    // huyHopDong
    // ============================

    public KetQua<Void> huyHopDong(int maHopDong) {
        HopDongLaoDong hd = findById(maHopDong);
        if (hd == null) {
            return KetQua.error("Không tìm thấy hợp đồng.");
        }
        if ("huy".equals(hd.getTrangThai())) {
            return KetQua.error("Hợp đồng đã được hủy.");
        }
        hopDongRepo.updateTrangThai(maHopDong, "huy");
        return KetQua.success(null, "Hủy hợp đồng thành công.");
    }

    // ============================
    // Getters
    // ============================

    public List<HopDongLaoDong> getSapHetHan(int soNgay) {
        return hopDongRepo.findSapHetHan(soNgay);
    }

    public List<HopDongLaoDong> getByMaNV(String maNV) {
        return hopDongRepo.findByMaNV(maNV);
    }

    public List<HopDongLaoDong> getAll() {
        return hopDongRepo.findAll();
    }

    public HopDongLaoDong getHieuLuc(String maNV) {
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
