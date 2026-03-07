package com.hrm.bus;

import com.hrm.dao.ChucVuDAO;
import com.hrm.dao.LichSuLuongDAO;
import com.hrm.model.ChucVu;
import com.hrm.model.LichSuHeSoLuong;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service quản lý chức vụ.
 * Áp dụng business logic và delegate persistence xuống ChucVuDAO (JDBC).
 * Tự động ghi lịch sử khi hệ số lương hoặc phụ cấp thay đổi.
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
     * @param heSoLuong    hệ số lương (>0)
     * @param phuCapChucVu phụ cấp chức vụ (>=0)
     * @param moTa         mô tả
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
            throw new IllegalArgumentException("Mã chức vụ '" + maChucVu + "' đã tồn tại.");
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
                "hoatdong"
        );

        positionRepo.save(pos);
    }

    /**
     * Cập nhật thông tin chức vụ.
     * Tự động ghi lịch sử nếu hệ số lương hoặc phụ cấp thay đổi.
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
    }

    /**
     * Ngừng hoạt động chức vụ.
     */
    public void deactivatePosition(String maChucVu) {

        ChucVu pos = positionRepo.findById(maChucVu);

        if (pos == null) {
            throw new IllegalArgumentException("Không tìm thấy chức vụ.");
        }

        pos.setTrangThai("ngung_hoat_dong");
        positionRepo.update(pos);
    }

    /**
     * Kích hoạt lại chức vụ đã ngừng.
     */
    public void activatePosition(String maChucVu) {

        ChucVu pos = positionRepo.findById(maChucVu);

        if (pos == null) {
            throw new IllegalArgumentException("Không tìm thấy chức vụ.");
        }

        pos.setTrangThai("hoatdong");
        positionRepo.update(pos);
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

        return "Admin";
    }
}