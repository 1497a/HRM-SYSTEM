package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.NghiPhepDAO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service quản lý nghỉ phép.
 * Đã cập nhật để maNV là String (ví dụ: "NV001", "NV002", ...).
 */
public class NghiPhepBUS {

    private static NghiPhepBUS instance;
    private final NghiPhepDAO repository;

    private NghiPhepBUS() {
        this.repository = NghiPhepDAO.getInstance();
    }

    public static synchronized NghiPhepBUS getInstance() {
        if (instance == null) {
            instance = new NghiPhepBUS();
        }
        return instance;
    }

    /**
     * Tính số ngày làm việc thực tế (loại trừ thứ 7, chủ nhật)
     */
    public int calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return 0;
        }

        int businessDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                businessDays++;
            }
            date = date.plusDays(1);
        }
        return businessDays;
    }

    /**
     * Tạo đơn xin nghỉ phép mới
     */
    public KetQua<DonXinNghiPhep> createRequest(String maNV, String tenNhanVien,
                                                String maLoaiPhep, LocalDate tuNgay,
                                                LocalDate denNgay, String lyDo) {
        // Validation
        if (maNV == null || maNV.trim().isEmpty()) {
            return KetQua.error("Mã nhân viên không hợp lệ.");
        }
        if (tuNgay == null || denNgay == null) {
            return KetQua.error("Vui lòng chọn ngày bắt đầu và kết thúc.");
        }
        if (tuNgay.isBefore(LocalDate.now())) {
            return KetQua.error("Ngày bắt đầu phải từ hôm nay trở đi.");
        }
        if (denNgay.isBefore(tuNgay)) {
            return KetQua.error("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        LoaiPhep loaiPhep = repository.findLoaiPhepById(maLoaiPhep);
        if (loaiPhep == null) {
            return KetQua.error("Loại phép không hợp lệ.");
        }

        int soNgayNghi = calculateBusinessDays(tuNgay, denNgay);
        if (soNgayNghi == 0) {
            return KetQua.error("Không có ngày làm việc nào trong khoảng thời gian này.");
        }

        // Kiểm tra số ngày phép còn lại (chỉ áp dụng cho phép năm - AL)
        if ("AL".equals(maLoaiPhep)) {
            int namHienTai = LocalDate.now().getYear();
            SoDungPhep soDu = repository.findByMaNVAndNamAndLoai(maNV, namHienTai, maLoaiPhep);
            if (soDu == null || soDu.getRemainingDays() < soNgayNghi) {
                double conLai = soDu != null ? soDu.getRemainingDays() : 0;
                return KetQua.error("Số ngày phép còn lại không đủ. Còn lại: " + conLai + " ngày.");
            }
        }

        // Tạo đơn
        DonXinNghiPhep don = new DonXinNghiPhep();
        don.setNhanVienId(maNV);
        don.setTenNhanVien(tenNhanVien);
        don.setLoaiPhepId(maLoaiPhep);
        don.setTenLoaiPhep(loaiPhep.getTenLoaiPhep());
        don.setTuNgay(tuNgay);
        don.setDenNgay(denNgay);
        don.setSoNgayNghi(soNgayNghi);
        don.setLyDo(lyDo != null ? lyDo.trim() : "");
        don.setTrangThai(DonXinNghiPhep.TrangThai.CHO_DUYET);

        try {
            repository.insert(don);
            return KetQua.success(don, "Tạo đơn xin nghỉ phép thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi khi tạo đơn xin nghỉ phép: " + e.getMessage());
        }
    }

    /**
     * Duyệt hoặc từ chối đơn xin nghỉ phép
     */
    public KetQua<DonXinNghiPhep> processRequest(int maDon, boolean duyet,
                                                 String maNguoiDuyet, String tenNguoiDuyet, String ghiChu) {
        DonXinNghiPhep don = repository.findById(maDon);
        if (don == null) {
            return KetQua.error("Không tìm thấy đơn xin nghỉ phép #" + maDon);
        }

        if (don.getTrangThai() != DonXinNghiPhep.TrangThai.CHO_DUYET) {
            return KetQua.error("Đơn xin nghỉ phép đã được xử lý trước đó.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (duyet) {
            // Trừ số ngày phép nếu là phép năm
            if ("AL".equals(don.getLoaiPhepId())) {
                int namHienTai = LocalDate.now().getYear();
                SoDungPhep soDu = repository.findByMaNVAndNamAndLoai(don.getNhanVienId(), namHienTai, "AL");
                if (soDu == null || soDu.getRemainingDays() < don.getSoNgayNghi()) {
                    return KetQua.error("Số ngày phép còn lại không đủ để duyệt đơn này.");
                }
                repository.capNhatSoDaDung(don.getNhanVienId(), namHienTai, "AL", don.getSoNgayNghi());
            }

            don.setTrangThai(DonXinNghiPhep.TrangThai.DA_DUYET);
            repository.updateTrangThai(maDon, "da_duyet", maNguoiDuyet, now, null);
            return KetQua.success(don, "Đã duyệt đơn xin nghỉ phép #" + maDon);
        } else {
            don.setTrangThai(DonXinNghiPhep.TrangThai.TU_CHOI);
            repository.updateTrangThai(maDon, "tu_choi", maNguoiDuyet, now, ghiChu);
            return KetQua.success(don, "Đã từ chối đơn xin nghỉ phép #" + maDon);
        }
    }

    /**
     * Hủy đơn xin nghỉ phép (chỉ người tạo được hủy)
     */
    public KetQua<DonXinNghiPhep> cancelRequest(int maDon, String maNVHuy) {
        DonXinNghiPhep don = repository.findById(maDon);
        if (don == null) {
            return KetQua.error("Không tìm thấy đơn xin nghỉ phép #" + maDon);
        }

        if (!maNVHuy.equals(don.getNhanVienId())) {
            return KetQua.error("Bạn không có quyền hủy đơn này.");
        }

        if (don.getTrangThai() == DonXinNghiPhep.TrangThai.DA_DUYET) {
            // Hoàn lại số ngày phép nếu là phép năm
            if ("AL".equals(don.getLoaiPhepId())) {
                int namHienTai = LocalDate.now().getYear();
                repository.capNhatSoDaDung(don.getNhanVienId(), namHienTai, "AL", -don.getSoNgayNghi());
            }
        }

        don.setTrangThai(DonXinNghiPhep.TrangThai.HUY);
        repository.updateTrangThai(maDon, "huy", maNVHuy, null, null);
        return KetQua.success(don, "Đã hủy đơn xin nghỉ phép #" + maDon);
    }

    // ============================
    // Query methods
    // ============================

    public List<DonXinNghiPhep> getMyRequests(String maNV) {
        return repository.findByMaNV(maNV);
    }

    public List<DonXinNghiPhep> getPendingRequests() {
        return repository.findChoDuyet();
    }

    public List<DonXinNghiPhep> getAllRequests() {
        return repository.findAll();
    }

    public List<DonXinNghiPhep> getPendingRequestsByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance()
                .getScopeForAction("LEAVE_APPROVE");
        return repository.findChoDuyetByScope(scope, currentMaNV);
    }

    public List<DonXinNghiPhep> getAllRequestsByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance()
                .getScopeForAction("LEAVE_VIEW");
        return repository.findAllByScope(scope, currentMaNV);
    }

    public List<LoaiPhep> getAllLeaveTypes() {
        return repository.findAllLoaiPhep();
    }

    public List<SoDungPhep> getBalances(String maNV) {
        return repository.findByMaNVAndNam(maNV, LocalDate.now().getYear());
    }

    public DonXinNghiPhep getRequest(int maDon) {
        return repository.findById(maDon);
    }

    // ============================
    // Generic result wrapper (giữ nguyên)
    // ============================

    public static class KetQua<T> {
        private boolean success;
        private String message;
        private T data;

        private KetQua(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> KetQua<T> success(T data, String message) {
            return new KetQua<>(true, message, data);
        }

        public static <T> KetQua<T> error(String message) {
            return new KetQua<>(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}