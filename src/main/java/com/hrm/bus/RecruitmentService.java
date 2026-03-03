package com.hrm.service;

import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.repo.RecruitmentRepository;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service quản lý tuyển dụng nhân sự.
 * Singleton pattern.
 */
public class RecruitmentService {

    private static RecruitmentService instance;

    private final RecruitmentRepository recruitmentRepo = RecruitmentRepository.getInstance();

    private RecruitmentService() {
    }

    public static synchronized RecruitmentService getInstance() {
        if (instance == null) {
            instance = new RecruitmentService();
        }
        return instance;
    }

    // ============================
    // Yêu cầu tuyển dụng
    // ============================

    public List<YeuCauTuyenDung> getAllYeuCau() {
        return recruitmentRepo.findAllYeuCau();
    }

    /**
     * Tạo yêu cầu tuyển dụng mới.
     */
    public ServiceResult<YeuCauTuyenDung> taoYeuCau(YeuCauTuyenDung yc) {
        // Validate vị trí
        if (yc.getMaChucVu() == null || yc.getMaChucVu().trim().isEmpty()) {
            return ServiceResult.error("Chức vụ/vị trí tuyển dụng không được để trống.");
        }
        // Validate số lượng
        if (yc.getSoLuong() <= 0) {
            return ServiceResult.error("Số lượng tuyển dụng phải lớn hơn 0.");
        }

        // Thiết lập trạng thái và ngày tạo
        yc.setTrangThai("cho_duyet");

        try {
            int maYC = recruitmentRepo.insertYeuCau(yc);
            if (maYC <= 0) {
                return ServiceResult.error("Không thể tạo yêu cầu tuyển dụng. Vui lòng thử lại.");
            }
            yc.setMaYeuCau(maYC);
            return ServiceResult.success(yc, "Tạo yêu cầu tuyển dụng thành công. Đang chờ phê duyệt.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi tạo yêu cầu tuyển dụng: " + e.getMessage());
        }
    }

    /**
     * Phê duyệt yêu cầu tuyển dụng.
     */
    public ServiceResult<Void> duyetYeuCau(int maYC) {
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return ServiceResult.error("Không tìm thấy yêu cầu tuyển dụng.");
        }
        if (!"cho_duyet".equals(yc.getTrangThai())) {
            return ServiceResult.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }

        int nguoiDuyetId = 0;
        if (SessionContext.getInstance().getCurrentUser() != null) {
            nguoiDuyetId = SessionContext.getInstance().getCurrentUser().getId();
        }

        recruitmentRepo.updateYeuCauTrangThai(maYC, "da_duyet", nguoiDuyetId, LocalDateTime.now());
        return ServiceResult.success(null, "Đã phê duyệt yêu cầu tuyển dụng.");
    }

    /**
     * Từ chối yêu cầu tuyển dụng.
     */
    public ServiceResult<Void> tuChoiYeuCau(int maYC) {
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return ServiceResult.error("Không tìm thấy yêu cầu tuyển dụng.");
        }
        if (!"cho_duyet".equals(yc.getTrangThai())) {
            return ServiceResult.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }

        int nguoiDuyetId = 0;
        if (SessionContext.getInstance().getCurrentUser() != null) {
            nguoiDuyetId = SessionContext.getInstance().getCurrentUser().getId();
        }

        recruitmentRepo.updateYeuCauTrangThai(maYC, "tu_choi", nguoiDuyetId, LocalDateTime.now());
        return ServiceResult.success(null, "Đã từ chối yêu cầu tuyển dụng.");
    }

    // ============================
    // Tin tuyển dụng
    // ============================

    public List<TinTuyenDung> getAllTinTuyenDung() {
        return recruitmentRepo.findAllTin();
    }

    /**
     * Tạo tin tuyển dụng mới.
     */
    public ServiceResult<TinTuyenDung> taoTin(TinTuyenDung tin) {
        // Validate tiêu đề
        if (tin.getTieuDe() == null || tin.getTieuDe().trim().isEmpty()) {
            return ServiceResult.error("Tiêu đề tin tuyển dụng không được để trống.");
        }
        // Validate mã yêu cầu
        if (tin.getMaYeuCau() <= 0) {
            return ServiceResult.error("Vui lòng chọn yêu cầu tuyển dụng liên kết.");
        }

        // Thiết lập trạng thái và ngày đăng
        tin.setTrangThai("dang_tuyen");
        tin.setNgayTao(LocalDate.now());

        try {
            int maTin = recruitmentRepo.insertTin(tin);
            if (maTin <= 0) {
                return ServiceResult.error("Không thể tạo tin tuyển dụng. Vui lòng thử lại.");
            }
            tin.setMaTin(maTin);
            return ServiceResult.success(tin, "Tạo tin tuyển dụng thành công.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi tạo tin tuyển dụng: " + e.getMessage());
        }
    }

    /**
     * Đóng tin tuyển dụng.
     */
    public ServiceResult<Void> dongTin(int maTin) {
        TinTuyenDung tin = recruitmentRepo.findTinById(maTin);
        if (tin == null) {
            return ServiceResult.error("Không tìm thấy tin tuyển dụng.");
        }
        if ("da_dong".equals(tin.getTrangThai())) {
            return ServiceResult.error("Tin tuyển dụng đã được đóng.");
        }

        tin.setTrangThai("da_dong");
        try {
            recruitmentRepo.updateTin(tin);
            return ServiceResult.success(null, "Đã đóng tin tuyển dụng.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi đóng tin tuyển dụng: " + e.getMessage());
        }
    }

    // ============================
    // Ứng viên
    // ============================

    public List<UngVien> getAllUngVien() {
        return recruitmentRepo.findAllUngVien();
    }

    /**
     * Tiếp nhận ứng viên nộp hồ sơ.
     */
    public ServiceResult<UngVien> tiepNhanUngVien(UngVien uv) {
        // Validate họ tên
        if (uv.getHoTen() == null || uv.getHoTen().trim().isEmpty()) {
            return ServiceResult.error("Họ tên ứng viên không được để trống.");
        }
        // Validate email
        if (uv.getEmail() == null || uv.getEmail().trim().isEmpty()) {
            return ServiceResult.error("Email ứng viên không được để trống.");
        }
        // Validate mã tin
        if (uv.getMaTin() <= 0) {
            return ServiceResult.error("Vui lòng chọn tin tuyển dụng liên kết.");
        }

        // Thiết lập trạng thái và ngày nộp
        uv.setTrangThai("nop_ho_so");
        uv.setNgayTao(LocalDate.now());

        try {
            int maUV = recruitmentRepo.insertUngVien(uv);
            if (maUV <= 0) {
                return ServiceResult.error("Không thể tiếp nhận ứng viên. Vui lòng thử lại.");
            }
            uv.setMaUngVien(maUV);
            return ServiceResult.success(uv, "Tiếp nhận ứng viên thành công.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi tiếp nhận ứng viên: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái ứng viên.
     */
    public ServiceResult<Void> capNhatTrangThaiUV(int maUV, String trangThai) {
        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return ServiceResult.error("Không tìm thấy ứng viên.");
        }

        uv.setTrangThai(trangThai);
        try {
            recruitmentRepo.updateUngVien(uv);
            return ServiceResult.success(null, "Cập nhật trạng thái ứng viên thành công.");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi cập nhật trạng thái ứng viên: " + e.getMessage());
        }
    }

    /**
     * Chuyển ứng viên trúng tuyển thành nhân viên chính thức.
     */
    public ServiceResult<Void> chuyenUVThanhNV(int maUV) {
        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return ServiceResult.error("Không tìm thấy ứng viên.");
        }
        if (!"trung_tuyen".equals(uv.getTrangThai())) {
            return ServiceResult.error("Chỉ có thể chuyển ứng viên có trạng thái 'Trúng tuyển' thành nhân viên.");
        }

        try {
            // Tạo nhân viên mới từ thông tin ứng viên
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(NhanVienService.getInstance().generateMaNhanVien());
            nv.setTrangThai("dang_lam_viec");
            nv.setNgayVaoLam(LocalDate.now());

            ThongTinCaNhan ttcn = new ThongTinCaNhan();
            ttcn.setHoTen(uv.getHoTen());
            ttcn.setEmail(uv.getEmail());
            ttcn.setDienThoai(uv.getDienThoai());
            ttcn.setNgaySinh(uv.getNgaySinh());
            ttcn.setGioiTinh(uv.getGioiTinh());
            ttcn.setDiaChi(uv.getDiaChi());

            ServiceResult<NhanVien> ketQua = NhanVienService.getInstance().taoHoSo(nv, ttcn);
            if (!ketQua.isSuccess()) {
                return ServiceResult.error("Lỗi tạo hồ sơ nhân viên: " + ketQua.getMessage());
            }

            // Liên kết ứng viên với nhân viên và cập nhật trạng thái
            uv.setMaNV(ketQua.getData().getId());
            uv.setTrangThai("da_tuyen");
            recruitmentRepo.updateUngVien(uv);

            return ServiceResult.success(null,
                    "Chuyển ứng viên thành nhân viên thành công. Mã NV: " + nv.getMaNhanVien());
        } catch (Exception e) {
            return ServiceResult.error("Lỗi chuyển ứng viên thành nhân viên: " + e.getMessage());
        }
    }
}
