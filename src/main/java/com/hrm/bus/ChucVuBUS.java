package com.hrm.bus;

import com.hrm.model.ChucVu;
import com.hrm.model.LichSuHeSoLuong;
import com.hrm.dao.ChucVuDAO;
import com.hrm.dao.LichSuLuongDAO;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service quản lý chức vụ.
 * Áp dụng business logic, delegate persistence xuống ChucVuDAO (JDBC).
 * Ghi lịch sử khi hệ số lương hoặc phụ cấp thay đổi.
 */
public class ChucVuBUS {

    private final ChucVuDAO positionRepo = new ChucVuDAO();
    private final LichSuLuongDAO historyRepo = LichSuLuongDAO.getInstance();

    /**
     * Lấy tất cả chức vụ.
     */
    public List<ChucVu> getAllPositions() {
        return positionRepo.findAll();
    }

    /**
     * Lấy danh sách chức vụ đang hoạt động.
     */
    public List<ChucVu> getActivePositions() {
        return positionRepo.findActive();
    }

    /**
     * Tìm chức vụ theo mã.
     */
    public ChucVu getById(String maChucVu) {
        return positionRepo.findById(maChucVu);
    }

    /**
     * Lấy lịch sử thay đổi hệ số lương của một chức vụ.
     */
    public List<LichSuHeSoLuong> getHistoryByMaChucVu(String maChucVu) {
        return historyRepo.findByMaChucVu(maChucVu);
    }

    /**
     * Thêm chức vụ mới.
     *
     * @param maChucVu     mã chức vụ (duy nhất)
     * @param tenChucVu    tên chức vụ
     * @param capBac       cấp bậc (1 là cao nhất)
     * @param heSoLuong    hệ số lương (phải > 0)
     * @param phuCapChucVu phụ cấp chức vụ (>= 0)
     * @param moTa         mô tả
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public void addPosition(String maChucVu, String tenChucVu, int capBac,
                            double heSoLuong, double phuCapChucVu, String moTa) {
        if (maChucVu == null || maChucVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chức vụ không được để trống.");
        }
        if (tenChucVu == null || tenChucVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chức vụ không được để trống.");
        }
        if (positionRepo.existsById(maChucVu.trim())) {
            throw new IllegalArgumentException("Mã chức vụ '" + maChucVu.trim() + "' đã tồn tại.");
        }
        if (heSoLuong <= 0) {
            throw new IllegalArgumentException("Hệ số lương phải lớn hơn 0.");
        }
        if (phuCapChucVu < 0) {
            throw new IllegalArgumentException("Phụ cấp không được âm.");
        }

        ChucVu pos = new ChucVu(
                maChucVu.trim(),
                tenChucVu.trim(),
                capBac,
                heSoLuong,
                phuCapChucVu,
                moTa,
                "hoat_dong"
        );
        positionRepo.save(pos);
    }

    /**
     * Cập nhật thông tin chức vụ.
     * Tự động ghi lịch sử nếu hệ số lương hoặc phụ cấp thay đổi.
     *
     * @param maChucVu  mã chức vụ cần cập nhật
     * @param tenMoi    tên chức vụ mới
     * @param capBacMoi cấp bậc mới
     * @param heSoMoi   hệ số lương mới
     * @param phuCapMoi phụ cấp mới
     * @param moTaMoi   mô tả mới
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public void updatePosition(String maChucVu, String tenMoi, int capBacMoi,
                               double heSoMoi, double phuCapMoi, String moTaMoi) {
        ChucVu pos = positionRepo.findById(maChucVu);
        if (pos == null) {
            throw new IllegalArgumentException("Không tìm thấy chức vụ.");
        }
        if (tenMoi == null || tenMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chức vụ không được để trống.");
        }
        if (heSoMoi <= 0) {
            throw new IllegalArgumentException("Hệ số lương phải lớn hơn 0.");
        }
        if (phuCapMoi < 0) {
            throw new IllegalArgumentException("Phụ cấp không được âm.");
        }

        boolean heSoThayDoi = Double.compare(pos.getHeSoLuong(), heSoMoi) != 0;
        boolean phuCapThayDoi = Double.compare(pos.getPhuCapChucVu(), phuCapMoi) != 0;

        if (heSoThayDoi || phuCapThayDoi) {
            String ngayHom = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Lấy tên người thực hiện từ session nếu có
            String nguoiThayDoi = "Admin";
            if (SessionContext.getInstance().isLoggedIn()
                    && SessionContext.getInstance().getCurrentUser() != null) {
                String fullName = SessionContext.getInstance().getCurrentUser().getFullName();
                if (fullName != null && !fullName.isEmpty()) {
                    nguoiThayDoi = fullName;
                } else {
                    nguoiThayDoi = SessionContext.getInstance().getCurrentUser().getUsername();
                }
            }

            LichSuHeSoLuong history = new LichSuHeSoLuong(
                    historyRepo.generateId(),
                    maChucVu,
                    pos.getHeSoLuong(),
                    heSoMoi,
                    pos.getPhuCapChucVu(),
                    phuCapMoi,
                    ngayHom,
                    nguoiThayDoi
            );
            historyRepo.save(history);
        }

        pos.setTenChucVu(tenMoi.trim());
        pos.setCapBac(capBacMoi);
        pos.setHeSoLuong(heSoMoi);
        pos.setPhuCapChucVu(phuCapMoi);
        pos.setMoTa(moTaMoi);
        positionRepo.update(pos);
    }

    /**
     * Ngưng hoạt động chức vụ.
     *
     * @param maChucVu mã chức vụ cần ngưng
     * @throws IllegalArgumentException nếu không tìm thấy chức vụ
     */
    public void deactivatePosition(String maChucVu) {
        ChucVu pos = positionRepo.findById(maChucVu);
        if (pos == null) {
            throw new IllegalArgumentException("Không tìm thấy chức vụ.");
        }
        pos.setTrangThai("ngung");
        positionRepo.update(pos);
    }
}
