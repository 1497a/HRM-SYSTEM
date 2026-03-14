package com.hrm.bus;

import com.hrm.dao.ChucVuDAO;
import com.hrm.dao.LichSuLuongDAO;
import com.hrm.dao.BoNhiemDAO;
import com.hrm.model.ChucVu;
import com.hrm.model.LichSuHeSoLuong;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý chức vụ.
 * Áp dụng business logic và delegate persistence xuống ChucVuDAO (JDBC).
 * Tự động ghi lịch sử khi hệ số lương hoặc phụ cấp thay đổi.
 */
public class ChucVuBUS {
    private static final String TRANG_THAI_HOAT_DONG = "hoatdong";
    private static final String TRANG_THAI_NGUNG_HOAT_DONG = "ngung_hoat_dong";
    private static final String DEFAULT_USER_NAME = "Admin";

    private final ChucVuDAO positionRepo = new ChucVuDAO();
    private final LichSuLuongDAO historyRepo = LichSuLuongDAO.getInstance();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();

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
     * Lấy danh sách chức vụ được phép tuyển dụng thông thường (capBac >= 3).
     * Các vị trí cấp cao (Giám đốc, Trưởng phòng) chỉ bổ nhiệm trực tiếp.
     */
    public List<ChucVu> getRecruitablePositions() {
        return getActivePositions().stream()
                .filter(cv -> cv.getCapBac() >= 3)
                .collect(Collectors.toList());
    }

    /**
     * Tìm chức vụ theo mã.
     */
    public ChucVu getById(String maChucVu) {
        return positionRepo.findById(maChucVu);
    }

    public boolean existsActiveByCode(String maChucVu) {
        return positionRepo.existsActiveByCode(maChucVu);
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
     * @param heSoLuong    hệ số lương (>0)
     * @param phuCapChucVu phụ cấp chức vụ (>=0)
     * @param moTa         mô tả
     */
    public KetQua<Void> addPosition(String maChucVu, String tenChucVu, int capBac,
                                    double heSoLuong, double phuCapChucVu, String moTa) {

        if (isBlank(maChucVu)) {
            return KetQua.error("Mã chức vụ không được để trống.");
        }

        if (isBlank(tenChucVu)) {
            return KetQua.error("Tên chức vụ không được để trống.");
        }

        if (positionRepo.existsById(maChucVu.trim())) {
            return KetQua.error("Mã chức vụ '" + maChucVu + "' đã tồn tại.");
        }

        if (heSoLuong <= 0) {
            return KetQua.error("Hệ số lương phải lớn hơn 0.");
        }

        if (phuCapChucVu < 0) {
            return KetQua.error("Phụ cấp không được âm.");
        }

        ChucVu pos = new ChucVu(
                maChucVu.trim(),
                tenChucVu.trim(),
                capBac,
                heSoLuong,
                phuCapChucVu,
                moTa,
                TRANG_THAI_HOAT_DONG
        );

        positionRepo.save(pos);
        return KetQua.success(null, "Thêm chức vụ thành công.");
    }

    /**
     * Cập nhật thông tin chức vụ.
     * Tự động ghi lịch sử nếu hệ số lương hoặc phụ cấp thay đổi.
     */
    public KetQua<Void> updatePosition(String maChucVu, String tenMoi, int capBacMoi,
                                       double heSoMoi, double phuCapMoi, String moTaMoi) {

        ChucVu pos = positionRepo.findById(maChucVu);

        if (pos == null) {
            return KetQua.error("Không tìm thấy chức vụ.");
        }

        if (isBlank(tenMoi)) {
            return KetQua.error("Tên chức vụ không được để trống.");
        }

        if (heSoMoi <= 0) {
            return KetQua.error("Hệ số lương phải lớn hơn 0.");
        }

        if (phuCapMoi < 0) {
            return KetQua.error("Phụ cấp không được âm.");
        }

        boolean heSoThayDoi = Double.compare(pos.getHeSoLuong(), heSoMoi) != 0;
        boolean phuCapThayDoi = Double.compare(pos.getPhuCapChucVu(), phuCapMoi) != 0;

        if (heSoThayDoi || phuCapThayDoi) {

            String ngayHom = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String nguoiThayDoi = getCurrentUserName();

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
        return KetQua.success(null, "Cập nhật chức vụ thành công.");
    }

    /**
     * Ngừng hoạt động chức vụ.
     */
    public KetQua<Void> deactivatePosition(String maChucVu) {

        ChucVu pos = positionRepo.findById(maChucVu);

        if (pos == null) {
            return KetQua.error("Không tìm thấy chức vụ.");
        }

        if (boNhiemRepo.hasActiveBoNhiemByChucVu(maChucVu)) {
            return KetQua.error("Không thể ngừng hoạt động chức vụ vì còn nhân viên đang giữ chức vụ này.");
        }

        pos.setTrangThai(TRANG_THAI_NGUNG_HOAT_DONG);
        positionRepo.update(pos);
        return KetQua.success(null, "Đã ngừng hoạt động chức vụ.");
    }

    /**
     * Kích hoạt lại chức vụ đã ngừng.
     */
    public KetQua<Void> activatePosition(String maChucVu) {

        ChucVu pos = positionRepo.findById(maChucVu);

        if (pos == null) {
            return KetQua.error("Không tìm thấy chức vụ.");
        }

        pos.setTrangThai(TRANG_THAI_HOAT_DONG);
        positionRepo.update(pos);
        return KetQua.success(null, "Đã kích hoạt lại chức vụ.");
    }

    /**
     * Lấy tên người đang đăng nhập để ghi lịch sử.
     */
    private String getCurrentUserName() {

        SessionContext session = SessionContext.getInstance();

        if (session.isLoggedIn() && session.getCurrentUser() != null) {

            String fullName = session.getCurrentUser().getHoTen();

            if (fullName != null && !fullName.isEmpty()) {
                return fullName;
            }

            return session.getCurrentUser().getTenDangNhap();
        }

        return DEFAULT_USER_NAME;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
