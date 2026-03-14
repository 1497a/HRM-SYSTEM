package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.DanhGiaDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service quản lý đánh giá hiệu suất nhân viên (Performance Evaluation).
 * Singleton pattern.
 * Đã cập nhật để maNV là String (ví dụ: "NV001", "NV002", ...).
 */
public class DanhGiaBUS {
    private static final String ACTION_EVAL_VIEW = "EVAL_VIEW";

    private static DanhGiaBUS instance;
    private final DanhGiaDAO repository;

    private DanhGiaBUS() {
        this.repository = DanhGiaDAO.getInstance();
    }

    public static synchronized DanhGiaBUS getInstance() {
        if (instance == null) {
            instance = new DanhGiaBUS();
        }
        return instance;
    }

    // ============================
    // Quản lý đợt đánh giá (Cycle)
    // ============================

    public List<DotDanhGia> getAllCycles() {
        return repository.findAllCycles();
    }

    public List<DotDanhGia> getOpenCycles() {
        return repository.getOpenCycles();
    }

    public DotDanhGia getCycle(int maDot) {
        return repository.findCycleById(maDot);
    }

    public KetQua<DotDanhGia> openCycle(int maDot) {
        DotDanhGia dot = repository.findCycleById(maDot);
        if (dot == null) {
            return KetQua.error("Không tìm thấy đợt đánh giá #" + maDot);
        }
        if (dot.getTrangThai() == DotDanhGia.TrangThai.DANG_DIEN_RA) {
            return KetQua.error("Đợt đánh giá đã được mở rồi.");
        }
        if (dot.getTrangThai() == DotDanhGia.TrangThai.DA_KET_THUC) {
            return KetQua.error("Đợt đánh giá đã kết thúc, không thể mở lại.");
        }

        dot.setTrangThai(DotDanhGia.TrangThai.DANG_DIEN_RA);
        repository.updateCycle(dot);
        return KetQua.success(dot, "Đã mở đợt đánh giá thành công.");
    }

    public KetQua<DotDanhGia> closeCycle(int maDot) {
        DotDanhGia dot = repository.findCycleById(maDot);
        if (dot == null) {
            return KetQua.error("Không tìm thấy đợt đánh giá #" + maDot);
        }
        if (dot.getTrangThai() != DotDanhGia.TrangThai.DANG_DIEN_RA) {
            return KetQua.error("Chỉ có thể đóng đợt đang diễn ra.");
        }

        dot.setTrangThai(DotDanhGia.TrangThai.DA_KET_THUC);
        repository.updateCycle(dot);
        return KetQua.success(dot, "Đã đóng đợt đánh giá thành công.");
    }

    public KetQua<DotDanhGia> taoDotDanhGia(String tenDot, int nam, String kyDanhGia,
                                            LocalDate tuNgay, LocalDate denNgay,
                                            List<TieuChiDanhGia> selectedCriteria) {
        KetQua<Void> cycleValidation = validateCycleInput(tenDot, tuNgay, denNgay, selectedCriteria);
        if (!cycleValidation.isSuccess()) return KetQua.error(cycleValidation.getMessage());

        // Kiểm tra tổng trọng số = 100% (cho phép sai số nhỏ)
        double tongTrongSo = selectedCriteria.stream()
                .mapToDouble(TieuChiDanhGia::getTrongSo)
                .sum();
        if (Math.abs(tongTrongSo - 100.0) > 0.01) {
            return KetQua.error(String.format("Tổng trọng số phải bằng 100%% (hiện tại: %.2f%%)", tongTrongSo));
        }

        DotDanhGia dot = new DotDanhGia();
        dot.setTenDot(tenDot.trim());
        dot.setNam(nam);
        dot.setKyDanhGia(kyDanhGia);
        dot.setTuNgay(tuNgay);
        dot.setDenNgay(denNgay);
        dot.setTrangThai(DotDanhGia.TrangThai.CHUA_BAT_DAU);

        int maDot = repository.insertCycle(dot);
        if (maDot <= 0) {
            return KetQua.error("Không thể tạo đợt đánh giá.");
        }

        // Gán tiêu chí cho đợt
        repository.setCriteriasForDot(maDot, selectedCriteria);

        dot.setId(maDot);
        return KetQua.success(dot, "Đã tạo đợt đánh giá thành công.");
    }

    // ============================
    // Quản lý tiêu chí đánh giá (Criteria)
    // ============================

    public List<TieuChiDanhGia> getAllCriteria() {
        return repository.findAllCriteria();
    }

    public List<TieuChiDanhGia> getCriteriaByCycle(int maDot) {
        return repository.findCriteriaByDot(maDot);
    }

    public KetQua<TieuChiDanhGia> saveCriteria(String tenTieuChi, String moTa, String nhomTieuChi,
                                               double diemToiDa, double trongSo) {
        KetQua<Void> criteriaValidation = validateCriteriaInput(tenTieuChi, diemToiDa, trongSo);
        if (!criteriaValidation.isSuccess()) return KetQua.error(criteriaValidation.getMessage());

        TieuChiDanhGia tc = new TieuChiDanhGia();
        tc.setTenTieuChi(tenTieuChi.trim());
        tc.setMoTa(moTa != null ? moTa.trim() : "");
        tc.setNhomTieuChi(nhomTieuChi != null ? nhomTieuChi.trim() : "");
        tc.setDiemToiDa(diemToiDa);
        tc.setTrongSo(trongSo);
        tc.setHoatDong(true);

        repository.saveCriteria(tc);
        return KetQua.success(tc, "Đã lưu tiêu chí đánh giá thành công.");
    }

    public KetQua<TieuChiDanhGia> updateCriteria(int maTieuChi, String tenTieuChi, String moTa,
                                                 String nhomTieuChi, double diemToiDa, double trongSo) {
        TieuChiDanhGia tc = repository.getCriteria(maTieuChi);
        if (tc == null) {
            return KetQua.error("Không tìm thấy tiêu chí #" + maTieuChi);
        }

        KetQua<Void> criteriaValidation = validateCriteriaInput(tenTieuChi, diemToiDa, trongSo);
        if (!criteriaValidation.isSuccess()) return KetQua.error(criteriaValidation.getMessage());

        tc.setTenTieuChi(tenTieuChi.trim());
        tc.setMoTa(moTa != null ? moTa.trim() : tc.getMoTa());
        tc.setNhomTieuChi(nhomTieuChi != null ? nhomTieuChi.trim() : tc.getNhomTieuChi());
        tc.setDiemToiDa(diemToiDa);
        tc.setTrongSo(trongSo);

        repository.saveCriteria(tc);
        return KetQua.success(tc, "Đã cập nhật tiêu chí đánh giá thành công.");
    }

    public KetQua<Void> deleteCriteria(int maTieuChi) {
        TieuChiDanhGia tc = repository.getCriteria(maTieuChi);
        if (tc == null) {
            return KetQua.error("Không tìm thấy tiêu chí #" + maTieuChi);
        }
        repository.deleteCriteria(maTieuChi);
        return KetQua.success(null, "Đã vô hiệu hóa tiêu chí đánh giá thành công.");
    }

    public int getTotalWeight() {
        return repository.getAllCriteria().stream()
                .filter(TieuChiDanhGia::isHoatDong)
                .mapToInt(c -> (int) c.getTrongSo())
                .sum();
    }

    // ============================
    // Nộp đánh giá (Submission)
    // ============================

    public KetQua<DanhGiaHieuSuat> submitEvaluation(int maDot, String maNV, String tenNV,
                                                    String maNguoiDanhGia, String tenNguoiDanhGia,
                                                    List<ChiTietDanhGia> chiTiets, String nhanXetChung) {
        DotDanhGia dot = repository.findCycleById(maDot);
        if (dot == null) {
            return KetQua.error("Không tìm thấy đợt đánh giá #" + maDot);
        }

        if (!dot.dangDienRa()) {
            return KetQua.error("Đợt đánh giá chưa mở hoặc đã kết thúc.");
        }

        // Kiểm tra đã đánh giá chưa
        if (SelfApprovalGuard.isSelfAction(maNguoiDanhGia, maNV)
                && !SelfApprovalGuard.currentUserCanBypassSelfRestriction()) {
            return KetQua.error("Bạn không thể tự đánh giá hiệu suất của chính mình.");
        }

        DanhGiaHieuSuat existing = repository.findSubmissionByDotAndNV(maDot, maNV);
        if (existing != null) {
            return KetQua.error("Nhân viên này đã được đánh giá trong đợt này.");
        }

        if (chiTiets == null || chiTiets.isEmpty()) {
            return KetQua.error("Vui lòng nhập điểm cho các tiêu chí.");
        }

        List<TieuChiDanhGia> tieuChis = repository.findCriteriaByDot(maDot);
        if (tieuChis == null || tieuChis.isEmpty()) {
            return KetQua.error("Đợt đánh giá chưa được cấu hình tiêu chí.");
        }

        Set<Integer> validTieuChiIds = taoTapTieuChiHopLe(tieuChis);

        KetQua<Double> tongDiemResult = tinhTongDiem(chiTiets, validTieuChiIds);
        if (!tongDiemResult.isSuccess()) return KetQua.error(tongDiemResult.getMessage());
        double tongDiem = tongDiemResult.getData();

        // Tạo đánh giá
        DanhGiaHieuSuat dg = new DanhGiaHieuSuat();
        dg.setDotDanhGiaId(maDot);
        dg.setTenDot(dot.getTenDot());
        dg.setNhanVienId(maNV);
        dg.setTenNhanVien(tenNV);
        dg.setNguoiDanhGiaId(maNguoiDanhGia);
        dg.setTenNguoiDanhGia(tenNguoiDanhGia);
        dg.setChiTietDanhGias(new ArrayList<>(chiTiets));
        dg.setNhanXetChung(nhanXetChung != null ? nhanXetChung.trim() : "");
        dg.setTongDiem(tongDiem);
        dg.setXepLoai(DanhGiaHieuSuat.XepLoai.tuDiem(tongDiem));
        dg.setNgayDanhGia(LocalDateTime.now());

        int maDanhGia = repository.insertSubmission(dg);
        if (maDanhGia <= 0) {
            return KetQua.error("Không thể lưu đánh giá.");
        }

        // Lưu chi tiết điểm
        repository.saveScores(maDanhGia, chiTiets);

        dg.setId(maDanhGia);
        return KetQua.success(dg, String.format("Đã lưu đánh giá thành công. Tổng điểm: %.2f - Xếp loại: %s",
                tongDiem, dg.getXepLoai().name()));
    }

    // ============================
    // Truy vấn đánh giá
    // ============================

    public List<DanhGiaHieuSuat> getSubmissionsByCycle(int maDot) {
        List<DanhGiaHieuSuat> list = repository.findByDot(maDot);
        loadChiTietForList(list);
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsByEmployee(String maNV) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsByEmployee(maNV);
        loadChiTietForList(list);
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsRelatedToUser(String maNV) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsRelatedToUser(maNV);
        loadChiTietForList(list);
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsForManagedEmployees(String maQuanLy) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsForManagedEmployees(maQuanLy);
        loadChiTietForList(list);
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsForDepartment(String maPhongBan) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsForDepartment(maPhongBan);
        loadChiTietForList(list);
        return list;
    }

    public List<String> getEvaluatedMaNVInCycle(int maDot) {
        return repository.getEvaluatedMaNVInCycle(maDot);
    }

    public DanhGiaHieuSuat getSubmission(int maDot, String maNV) {
        DanhGiaHieuSuat dg = repository.findSubmissionByDotAndNV(maDot, maNV);
        if (dg != null) {
            dg.setChiTietDanhGias(repository.findScoresByDanhGia(dg.getId()));
        }
        return dg;
    }

    public List<DanhGiaHieuSuat> getAllSubmissions() {
        List<DanhGiaHieuSuat> list = repository.findAll();
        loadChiTietForList(list);
        return list;
    }

    public List<DanhGiaHieuSuat> getAllSubmissionsByScope(String currentMaNV) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance()
                .getScopeForAction(ACTION_EVAL_VIEW);
        List<DanhGiaHieuSuat> list = repository.findAllByScope(scope, currentMaNV);
        loadChiTietForList(list);
        return list;
    }

    // Helper: tải chi tiết điểm cho danh sách đánh giá
    private void loadChiTietForList(List<DanhGiaHieuSuat> list) {
        if (list == null || list.isEmpty()) return;
        for (DanhGiaHieuSuat dg : list) {
            dg.setChiTietDanhGias(repository.findScoresByDanhGia(dg.getId()));
        }
    }

    public List<ChiTietDanhGia> loadScores(int maDanhGia) {
        return repository.findScoresByDanhGia(maDanhGia);
    }

    private KetQua<Void> validateCycleInput(String tenDot, LocalDate tuNgay, LocalDate denNgay,
                                            List<TieuChiDanhGia> selectedCriteria) {
        if (tenDot == null || tenDot.trim().isEmpty()) {
            return KetQua.error("Tên đợt đánh giá không được để trống.");
        }
        if (tuNgay == null || denNgay == null) {
            return KetQua.error("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
        }
        if (!denNgay.isAfter(tuNgay)) {
            return KetQua.error("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        if (selectedCriteria == null || selectedCriteria.isEmpty()) {
            return KetQua.error("Vui lòng chọn ít nhất một tiêu chí đánh giá.");
        }
        return KetQua.success(null, "");
    }

    private KetQua<Void> validateCriteriaInput(String tenTieuChi, double diemToiDa, double trongSo) {
        if (tenTieuChi == null || tenTieuChi.trim().isEmpty()) {
            return KetQua.error("Tên tiêu chí không được để trống.");
        }
        if (diemToiDa <= 0) {
            return KetQua.error("Điểm tối đa phải lớn hơn 0.");
        }
        if (trongSo < 0 || trongSo > 100) {
            return KetQua.error("Trọng số phải từ 0 đến 100.");
        }
        return KetQua.success(null, "");
    }

    private Set<Integer> taoTapTieuChiHopLe(List<TieuChiDanhGia> tieuChis) {
        Set<Integer> validTieuChiIds = new HashSet<>();
        for (TieuChiDanhGia tc : tieuChis) {
            validTieuChiIds.add(tc.getMaTieuChi());
        }
        return validTieuChiIds;
    }

    private KetQua<Double> tinhTongDiem(List<ChiTietDanhGia> chiTiets, Set<Integer> validTieuChiIds) {
        double tongDiem = 0.0;
        for (ChiTietDanhGia ct : chiTiets) {
            if (!validTieuChiIds.contains(ct.getTieuChiId())) {
                return KetQua.error("Có tiêu chí không thuộc đợt đánh giá này.");
            }
            if (ct.getDiem() < 0 || ct.getDiem() > 10) {
                return KetQua.error("Điểm phải từ 0 đến 10.");
            }
            tongDiem += ct.getDiem() * ct.getTrongSo() / 100.0;
        }
        return KetQua.success(tongDiem, "");
    }

}
