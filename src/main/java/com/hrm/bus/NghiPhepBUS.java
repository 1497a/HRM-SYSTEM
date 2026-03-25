package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.ChamCongDAO;
import com.hrm.dao.NghiPhepDAO;
import com.hrm.util.HRMConstants;
import com.hrm.util.PermissionCodes;
import com.hrm.util.ValidationUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service quản lý nghỉ phép.
 * Đã cập nhật để maNV là String (ví dụ: "NV001", "NV002", ...).
 */
public class NghiPhepBUS {

    private static final String ACTION_LEAVE_APPROVE = PermissionCodes.LEAVE_APPROVE;
    private static final String ACTION_LEAVE_VIEW = PermissionCodes.LEAVE_VIEW;
    private static NghiPhepBUS instance;
    private final NghiPhepDAO repository;
    private final ChamCongDAO chamCongRepo;
    private NghiPhepBUS() {
        this.repository = NghiPhepDAO.getInstance();
        this.chamCongRepo = ChamCongDAO.getInstance();
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
        if (ValidationUtils.isBlank(maNV)) {
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
        if (ValidationUtils.isBlank(lyDo)) {
            return KetQua.error("L� do ngh? ph�p kh�ng du?c d? tr?ng.");
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
        if (HRMConstants.LOAI_PHEP_NAM.equals(maLoaiPhep)) {
            int namHienTai = LocalDate.now().getYear();
            SoDungPhep soDu = repository.findByMaNVAndNamAndLoai(maNV, namHienTai, maLoaiPhep);
            if (soDu == null || soDu.getSoNgayConLai() < soNgayNghi) {
                double conLai = soDu != null ? soDu.getSoNgayConLai() : 0;
                return KetQua.error("Số ngày phép còn lại không đủ. Còn lại: " + conLai + " ngày.");
            }
        }
        // Tạo đơn
        DonXinNghiPhep don = new DonXinNghiPhep();
        don.setMaNV(maNV);
        don.setTenNhanVien(tenNhanVien);
        don.setMaLoaiPhep(maLoaiPhep);
        don.setTenLoaiPhep(loaiPhep.getTenLoaiPhep());
        don.setTuNgay(tuNgay);
        don.setDenNgay(denNgay);
        don.setSoNgayNghi(soNgayNghi);
        don.setLyDo(lyDo != null ? lyDo.trim() : "");
        don.setTrangThai(DonXinNghiPhep.TrangThai.CHO_DUYET);
        try {
            int id = repository.insert(don);
            if (id <= 0) {
                return KetQua.error("Không thể tạo đơn xin nghỉ phép. Vui lòng thử lại.");
            }
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
        if (SelfApprovalGuard.isSelfAction(maNguoiDuyet, don.getMaNV())) {
            return KetQua.error("Bạn không thể tự duyệt đơn xin nghỉ phép của chính mình.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (duyet) {
            // Trừ số ngày phép nếu là phép năm
            if (HRMConstants.LOAI_PHEP_NAM.equals(don.getMaLoaiPhep())) {
                int namHienTai = LocalDate.now().getYear();
                SoDungPhep soDu = repository.findByMaNVAndNamAndLoai(don.getMaNV(), namHienTai, HRMConstants.LOAI_PHEP_NAM);
                if (soDu == null || soDu.getSoNgayConLai() < don.getSoNgayNghi()) {
                    return KetQua.error("Số ngày phép còn lại không đủ để duyệt đơn này.");
                }
                int capNhatRows = repository.capNhatSoDaDung(don.getMaNV(), namHienTai, HRMConstants.LOAI_PHEP_NAM, don.getSoNgayNghi());
                if (capNhatRows <= 0) {
                    return KetQua.error("Không thể cập nhật số ngày phép đã dùng. Vui lòng thử lại.");
                }
            }
            createAttendanceForApprovedLeave(don);
            don.setTrangThai(DonXinNghiPhep.TrangThai.DA_DUYET);
            int duyetRows = repository.updateTrangThai(maDon, HRMConstants.TRANG_THAI_DA_DUYET, maNguoiDuyet, now, null);
            if (duyetRows <= 0) {
                return KetQua.error("Không thể duyệt đơn xin nghỉ phép. Vui lòng thử lại.");
            }
            return KetQua.success(don, "Đã duyệt đơn xin nghỉ phép #" + maDon);
        } else {
            don.setTrangThai(DonXinNghiPhep.TrangThai.TU_CHOI);
            int tuChoiRows = repository.updateTrangThai(maDon, HRMConstants.TRANG_THAI_TU_CHOI, maNguoiDuyet, now, ghiChu);
            if (tuChoiRows <= 0) {
                return KetQua.error("Không thể từ chối đơn xin nghỉ phép. Vui lòng thử lại.");
            }
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
        if (ValidationUtils.isBlank(maNVHuy) || !maNVHuy.equals(don.getMaNV())) {
            return KetQua.error("Bạn không có quyền hủy đơn này.");
        }
        don.setTrangThai(DonXinNghiPhep.TrangThai.HUY);
        int huyRows = repository.updateTrangThai(maDon, HRMConstants.TRANG_THAI_HUY, maNVHuy, null, null);
        if (huyRows <= 0) {
            return KetQua.error("Không thể hủy đơn xin nghỉ phép. Vui lòng thử lại.");
        }
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
                .getScopeForAction(ACTION_LEAVE_APPROVE);
        return repository.findChoDuyetByScope(scope, currentMaNV);
    }

    public List<DonXinNghiPhep> getAllRequestsByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance()
                .getScopeForAction(ACTION_LEAVE_VIEW);
        return repository.findAllByScope(scope, currentMaNV);
    }

    /** Dùng cho ReportPanel — dùng scope REPORT_VIEW thay vì LEAVE_VIEW. */
    public List<DonXinNghiPhep> getAllForReport(String currentMaNV) {
        com.hrm.model.DataScope scope = XacThucBUS.getInstance()
                .getScopeForAction(PermissionCodes.REPORT_VIEW);
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

    private void createAttendanceForApprovedLeave(DonXinNghiPhep don) {
        LoaiPhep loaiPhep = repository.findLoaiPhepById(don.getMaLoaiPhep());
        boolean coLuong = loaiPhep != null && loaiPhep.isCoLuong();
        ChamCong.TrangThai trangThaiChamCong = coLuong
                ? ChamCong.TrangThai.NGHI_PHEP
                : ChamCong.TrangThai.VANG_MAT;
        String ghiChu = "Nghi phep: " + don.getTenLoaiPhep() + " - " + don.getLyDo();
        LocalDate date = don.getTuNgay();
        while (!date.isAfter(don.getDenNgay())) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
                    && chamCongRepo.findChamCongByNVAndNgay(don.getMaNV(), date) == null) {
                ChamCong chamCong = new ChamCong();
                chamCong.setMaNV(don.getMaNV());
                chamCong.setNgay(date);
                chamCong.setTrangThai(trangThaiChamCong);
                chamCong.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);
                chamCong.setGhiChu(ghiChu);
                int saved = chamCongRepo.saveChamCong(chamCong);
                if (saved <= 0) {
                    System.err.println("Canh bao: Khong the tao ban ghi cham cong nghi phep cho NV " + don.getMaNV() + " ngay " + date);
                }
            }
            date = date.plusDays(1);
        }
    }

}
