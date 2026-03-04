package com.hrm.bus;

import com.hrm.model.*;
import com.hrm.dao.DanhGiaDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance Evaluation Service
 */
public class DanhGiaBUS {
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

    // Cycle Management
    public List<DotDanhGia> getAllCycles() {
        return repository.getAllCycles();
    }

    public List<DotDanhGia> getOpenCycles() {
        return repository.getOpenCycles();
    }

    public DotDanhGia getCycle(int id) {
        return repository.getCycle(id);
    }

    public KetQua<DotDanhGia> openCycle(int cycleId) {
        DotDanhGia cycle = repository.getCycle(cycleId);
        if (cycle == null) {
            return KetQua.error("Khong tim thay ky danh gia");
        }
        if (cycle.getTrangThai() == DotDanhGia.TrangThai.DANG_DIEN_RA) {
            return KetQua.error("Ky danh gia da dang mo");
        }
        if (cycle.getTrangThai() == DotDanhGia.TrangThai.DA_KET_THUC) {
            return KetQua.error("Ky danh gia da dong, khong the mo lai");
        }
        cycle.setTrangThai(DotDanhGia.TrangThai.DANG_DIEN_RA);
        repository.updateCycle(cycle);
        return KetQua.success(cycle, "Da mo ky danh gia thanh cong");
    }

    public KetQua<DotDanhGia> closeCycle(int cycleId) {
        DotDanhGia cycle = repository.getCycle(cycleId);
        if (cycle == null) {
            return KetQua.error("Khong tim thay ky danh gia");
        }
        if (cycle.getTrangThai() != DotDanhGia.TrangThai.DANG_DIEN_RA) {
            return KetQua.error("Chi co the dong ky danh gia dang mo");
        }
        cycle.setTrangThai(DotDanhGia.TrangThai.DA_KET_THUC);
        repository.updateCycle(cycle);
        return KetQua.success(cycle, "Da dong ky danh gia thanh cong");
    }

    public KetQua<DotDanhGia> taoDotDanhGia(String tenDot, int nam, String kyDanhGia,
                                              java.time.LocalDate tuNgay, java.time.LocalDate denNgay,
                                              List<TieuChiDanhGia> selectedCriteria) {
        if (tenDot == null || tenDot.trim().isEmpty()) {
            return KetQua.error("Ten dot danh gia khong duoc de trong");
        }
        if (tuNgay == null || denNgay == null) {
            return KetQua.error("Vui long chon ngay bat dau va ngay ket thuc");
        }
        if (!denNgay.isAfter(tuNgay)) {
            return KetQua.error("Ngay ket thuc phai sau ngay bat dau");
        }
        if (selectedCriteria == null || selectedCriteria.isEmpty()) {
            return KetQua.error("Vui long chon it nhat mot tieu chi danh gia");
        }
        int totalWeight = selectedCriteria.stream().mapToInt(c -> (int) c.getDiemToiDa()).sum();
        if (totalWeight != 100) {
            return KetQua.error("Tong trong so cua cac tieu chi duoc chon phai bang 100% (hien tai: " + totalWeight + "%)");
        }

        DotDanhGia dot = new DotDanhGia();
        dot.setTenDot(tenDot.trim());
        dot.setNam(nam);
        dot.setKyDanhGia(kyDanhGia);
        dot.setTuNgay(tuNgay);
        dot.setDenNgay(denNgay);
        dot.setTrangThai(DotDanhGia.TrangThai.CHUA_BAT_DAU);
        
        repository.insertCycle(dot);
        repository.setCriteriasForDot(dot.getId(), selectedCriteria);
        
        return KetQua.success(dot, "Da tao dot danh gia thanh cong");
    }

    // Criteria Management
    public List<TieuChiDanhGia> getAllCriteria() {
        return repository.getAllCriteria();
    }

    public KetQua<TieuChiDanhGia> saveCriteria(String name, String description, int weight) {
        if (name == null || name.trim().isEmpty()) {
            return KetQua.error("Ten tieu chi khong duoc de trong");
        }

        if (weight <= 0 || weight > 100) {
            return KetQua.error("Trong so phai tu 1 den 100");
        }

        TieuChiDanhGia criteria = new TieuChiDanhGia();
        criteria.setTenTieuChi(name.trim());
        criteria.setMoTa(description);
        criteria.setDiemToiDa(weight);

        repository.saveCriteria(criteria);
        return KetQua.success(criteria, "Da luu tieu chi");
    }

    public KetQua<TieuChiDanhGia> updateCriteria(int id, String name, String description, int weight) {
        TieuChiDanhGia criteria = repository.getCriteria(id);
        if (criteria == null) {
            return KetQua.error("Khong tim thay tieu chi");
        }

        if (name == null || name.trim().isEmpty()) {
            return KetQua.error("Ten tieu chi khong duoc de trong");
        }

        if (weight <= 0 || weight > 100) {
            return KetQua.error("Trong so phai tu 1 den 100");
        }

        criteria.setTenTieuChi(name.trim());
        criteria.setMoTa(description);
        criteria.setDiemToiDa(weight);
        repository.saveCriteria(criteria);
        return KetQua.success(criteria, "Da cap nhat tieu chi");
    }

    public KetQua<Void> deleteCriteria(int id) {
        TieuChiDanhGia criteria = repository.getCriteria(id);
        if (criteria == null) {
            return KetQua.error("Khong tim thay tieu chi");
        }

        repository.deleteCriteria(id);
        return KetQua.success(null, "Da xoa tieu chi");
    }

    public int getTotalWeight() {
        return repository.getAllCriteria().stream()
                .filter(c -> c.isHoatDong())
                .mapToInt(c -> (int) c.getDiemToiDa())
                .sum();
    }

    // Evaluation Submission
    public KetQua<DanhGiaHieuSuat> submitEvaluation(int cycleId, int employeeId, String employeeName,
                                                          int evaluatorId, String evaluatorName,
                                                          List<ChiTietDanhGia> scores, String generalComment) {
        DotDanhGia cycle = repository.getCycle(cycleId);
        if (cycle == null) {
            return KetQua.error("Khong tim thay ky danh gia");
        }

        if (!cycle.dangDienRa()) {
            return KetQua.error("Ky danh gia chua mo hoac da dong");
        }

        // Check if already submitted
        DanhGiaHieuSuat existing = repository.findSubmission(cycleId, employeeId);
        if (existing != null) {
            return KetQua.error("Nhan vien nay da duoc danh gia trong ky nay");
        }

        // Validate scores
        if (scores == null || scores.isEmpty()) {
            return KetQua.error("Vui long nhap diem cho cac tieu chi");
        }

        for (ChiTietDanhGia score : scores) {
            if (score.getScore() < 1 || score.getScore() > 10) {
                return KetQua.error("Diem phai tu 1 den 10");
            }
        }

        // Create submission
        DanhGiaHieuSuat submission = new DanhGiaHieuSuat();
        submission.setDotDanhGiaId(cycleId);
        submission.setTenDot(cycle.getName());
        submission.setNhanVienId(employeeId);
        submission.setTenNhanVien(employeeName);
        submission.setNguoiDanhGiaId(evaluatorId);
        submission.setTenNguoiDanhGia(evaluatorName);
        submission.setChiTietDanhGias(new ArrayList<>(scores));
        submission.setNhanXetChung(generalComment);
        submission.tinhTong();

        repository.saveSubmission(submission);

        // CRITICAL: save per-criterion scores to CHITIETDANHGIA
        if (submission.getId() > 0) {
            repository.saveScores(submission.getId(), scores);
        }

        return KetQua.success(submission, "Da luu danh gia. Diem tong: " +
                String.format("%.2f", submission.getOverallScore()) + " - " +
                submission.getXepLoai().toString());
    }


    // Query methods
    public List<DanhGiaHieuSuat> getSubmissionsByCycle(int cycleId) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsByCycle(cycleId);
        if (list != null) {
            for (DanhGiaHieuSuat s : list) {
                s.setChiTietDanhGias(repository.findScoresByDanhGia(s.getId()));
            }
        }
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsByEmployee(int employeeId) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsByEmployee(employeeId);
        if (list != null) {
            for (DanhGiaHieuSuat s : list) {
                s.setChiTietDanhGias(repository.findScoresByDanhGia(s.getId()));
            }
        }
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsRelatedToUser(int userId) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsRelatedToUser(userId);
        if (list != null) {
            for (DanhGiaHieuSuat s : list) {
                s.setChiTietDanhGias(repository.findScoresByDanhGia(s.getId()));
            }
        }
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsForManagedEmployees(int managerMaNV) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsForManagedEmployees(managerMaNV);
        if (list != null) {
            for (DanhGiaHieuSuat s : list) {
                s.setChiTietDanhGias(repository.findScoresByDanhGia(s.getId()));
            }
        }
        return list;
    }

    public List<DanhGiaHieuSuat> getSubmissionsForDepartment(String maPhongBan) {
        List<DanhGiaHieuSuat> list = repository.getSubmissionsForDepartment(maPhongBan);
        if (list != null) {
            for (DanhGiaHieuSuat s : list) {
                s.setChiTietDanhGias(repository.findScoresByDanhGia(s.getId()));
            }
        }
        return list;
    }

    public List<Integer> getEvaluatedMaNVInCycle(int cycleId) {
        return repository.getEvaluatedMaNVInCycle(cycleId);
    }

    /** Load detail scores for a specific evaluation (lazy load). */
    public java.util.List<ChiTietDanhGia> loadScores(int maDanhGia) {
        return repository.findScoresByDanhGia(maDanhGia);
    }

    public DanhGiaHieuSuat getSubmission(int cycleId, int employeeId) {
        DanhGiaHieuSuat sub = repository.findSubmission(cycleId, employeeId);
        if (sub != null) {
            sub.setChiTietDanhGias(repository.findScoresByDanhGia(sub.getId()));
        }
        return sub;
    }

    public List<DanhGiaHieuSuat> getAllSubmissions() {
        List<DanhGiaHieuSuat> list = repository.getAllSubmissions();
        if (list != null) {
            for (DanhGiaHieuSuat s : list) {
                s.setChiTietDanhGias(repository.findScoresByDanhGia(s.getId()));
            }
        }
        return list;
    }

    public List<DanhGiaHieuSuat> getAllSubmissionsByScope(int currentUserId) {
        com.hrm.model.DataScope scope = com.hrm.bus.XacThucBUS.getInstance().getScopeForAction("EVAL_VIEW");
        List<DanhGiaHieuSuat> list = repository.findAllByScope(scope, currentUserId);
        if (list != null) {
            for (DanhGiaHieuSuat s : list) {
                s.setChiTietDanhGias(repository.findScoresByDanhGia(s.getId()));
            }
        }
        return list;
    }

    /**
     * Calculate overall score from weighted scores
     */
    public double calculateOverallScore(List<ChiTietDanhGia> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (ChiTietDanhGia score : scores) {
            total += score.getWeightedScore();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Get rating from score
     */
    public DanhGiaHieuSuat.XepLoai getRatingFromScore(double score) {
        return DanhGiaHieuSuat.XepLoai.tuDiem(score);
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
