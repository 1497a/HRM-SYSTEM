package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.NghiPhepDAO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Leave Management Service
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
     * Calculate business days between two dates (excluding weekends)
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
     * Create a new leave request
     */
    public KetQua<DonXinNghiPhep> createRequest(int employeeId, String employeeName,
                                                     String leaveTypeCode, LocalDate startDate,
                                                     LocalDate endDate, String reason) {
        // Validation
        if (startDate == null || endDate == null) {
            return KetQua.error("Vui long chon ngay bat dau va ket thuc");
        }

        if (startDate.isBefore(LocalDate.now())) {
            return KetQua.error("Ngay bat dau phai tu hom nay tro di");
        }

        if (endDate.isBefore(startDate)) {
            return KetQua.error("Ngay ket thuc phai sau ngay bat dau");
        }

        LoaiPhep leaveType = repository.findLoaiPhepById(leaveTypeCode);
        if (leaveType == null) {
            return KetQua.error("Loai phep khong hop le");
        }

        int totalDays = calculateBusinessDays(startDate, endDate);
        if (totalDays == 0) {
            return KetQua.error("Khong co ngay lam viec trong khoang thoi gian nay");
        }

        // Check balance for annual leave
        if ("AL".equals(leaveTypeCode)) {
            SoDungPhep balance = repository.findByMaNVAndNamAndLoai(employeeId,
                    LocalDate.now().getYear(), leaveTypeCode);
            if (balance == null || balance.getRemainingDays() < totalDays) {
                int remaining = balance != null ? (int)(balance.getSoNgayDuocCap() - balance.getSoNgayDaDung()) : 0;
                return KetQua.error("So ngay phep con lai khong du. Con lai: " + remaining + " ngay");
            }
        }

        // Create request
        DonXinNghiPhep request = new DonXinNghiPhep();
        request.setNhanVienId(employeeId);
        request.setTenNhanVien(employeeName);
        request.setLoaiPhepId(leaveTypeCode);
        request.setTenLoaiPhep(leaveType.getName());
        request.setTuNgay(startDate);
        request.setDenNgay(endDate);
        request.setSoNgayNghi(totalDays);
        request.setLyDo(reason);
        request.setTrangThai(DonXinNghiPhep.TrangThai.CHO_DUYET);

        repository.insert(request);
        return KetQua.success(request, "Tao don nghi phep thanh cong");
    }

    /**
     * Approve or reject a leave request
     */
    public KetQua<DonXinNghiPhep> processRequest(int requestId, boolean approve,
                                                       int approverId, String approverName, String note) {
        DonXinNghiPhep request = repository.findById(requestId);
        if (request == null) {
            return KetQua.error("Khong tim thay don nghi phep");
        }

        if (request.getTrangThai() != DonXinNghiPhep.TrangThai.CHO_DUYET) {
            return KetQua.error("Don nghi phep da duoc xu ly truoc do");
        }

        request.setNguoiDuyetId(approverId);
        request.setTenNguoiDuyet(approverName);
        request.setLyDoTuChoi(note);

        LocalDateTime now = LocalDateTime.now();

        if (approve) {
            // Deduct balance for annual leave
            if ("AL".equals(request.getLeaveTypeCode())) {
                SoDungPhep balance = repository.findByMaNVAndNamAndLoai(request.getEmployeeId(),
                        LocalDate.now().getYear(), request.getLeaveTypeCode());
                if (balance != null) {
                    if (balance.getRemainingDays() < request.getTotalDays()) {
                        return KetQua.error("Nhan vien khong du so ngay phep");
                    }
                    repository.capNhatSoDaDung(request.getEmployeeId(),
                            LocalDate.now().getYear(), request.getLeaveTypeCode(), request.getTotalDays());
                }
            }
            request.setTrangThai(DonXinNghiPhep.TrangThai.DA_DUYET);
            repository.updateTrangThai(requestId, "da_duyet", approverId, now, null);
            return KetQua.success(request, "Da duyet don nghi phep");
        } else {
            request.setTrangThai(DonXinNghiPhep.TrangThai.TU_CHOI);
            repository.updateTrangThai(requestId, "tu_choi", approverId, now, note);
            return KetQua.success(request, "Da tu choi don nghi phep");
        }
    }

    /**
     * Cancel a leave request
     */
    public KetQua<DonXinNghiPhep> cancelRequest(int requestId, int userId) {
        DonXinNghiPhep request = repository.findById(requestId);
        if (request == null) {
            return KetQua.error("Khong tim thay don nghi phep");
        }

        if (request.getEmployeeId() != userId) {
            return KetQua.error("Ban khong co quyen huy don nay");
        }

        if (request.getTrangThai() == DonXinNghiPhep.TrangThai.DA_DUYET) {
            // Restore balance for annual leave
            if ("AL".equals(request.getLeaveTypeCode())) {
                repository.capNhatSoDaDung(request.getEmployeeId(),
                        LocalDate.now().getYear(), request.getLeaveTypeCode(), -request.getTotalDays());
            }
        }

        request.setTrangThai(DonXinNghiPhep.TrangThai.HUY);
        repository.updateTrangThai(requestId, "huy", 0, null, null);
        return KetQua.success(request, "Da huy don nghi phep");
    }

    // Query methods
    public List<DonXinNghiPhep> getMyRequests(int employeeId) {
        return repository.findByMaNV(employeeId);
    }

    public List<DonXinNghiPhep> getPendingRequests() {
        return repository.findChoDuyet();
    }

    public List<DonXinNghiPhep> getAllRequests() {
        return repository.findAll();
    }

    public List<LoaiPhep> getAllLeaveTypes() {
        return repository.findAllLoaiPhep();
    }

    public List<SoDungPhep> getBalances(int employeeId) {
        return repository.findByMaNVAndNam(employeeId, LocalDate.now().getYear());
    }

    public DonXinNghiPhep getRequest(int id) {
        return repository.findById(id);
    }

    /**
     * Generic service result wrapper
     */
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
