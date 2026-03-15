package com.hrm.bus;

import com.hrm.dao.TuyenDungDAO;
import com.hrm.model.BoNhiem;
import com.hrm.model.NhanVien;
import com.hrm.model.RecruitmentStatus;
import com.hrm.model.TaiKhoan;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý tuyển dụng nhân sự.
 * Singleton pattern.
 */
public class TuyenDungBUS {

    private static TuyenDungBUS instance;
    private final TuyenDungDAO recruitmentRepo = TuyenDungDAO.getInstance();

    private TuyenDungBUS() {
    }

    public static synchronized TuyenDungBUS getInstance() {
        if (instance == null) {
            instance = new TuyenDungBUS();
        }
        return instance;
    }

    // ============================
    // Yêu cầu tuyển dụng
    // ============================

    public List<YeuCauTuyenDung> getAllYeuCau() {
        return recruitmentRepo.findAllYeuCau();
    }

    public List<YeuCauTuyenDung> getYeuCauDaDuyet() {
        return getAllYeuCau().stream()
                .filter(yc -> RecruitmentStatus.YeuCau.DA_DUYET.equals(yc.getTrangThai()))
                .collect(Collectors.toList());
    }

    public KetQua<YeuCauTuyenDung> taoYeuCau(YeuCauTuyenDung yc) {
        if (isBlank(yc.getId())) {
            return KetQua.error("Phòng ban không được để trống.");
        }
        if (isBlank(yc.getMaChucVu())) {
            return KetQua.error("Chức vụ/vị trí tuyển dụng không được để trống.");
        }
        if (yc.getSoLuong() <= 0) {
            return KetQua.error("Số lượng tuyển dụng phải lớn hơn 0.");
        }

        yc.setTrangThai(RecruitmentStatus.YeuCau.CHO_DUYET);

        try {
            int maYC = recruitmentRepo.insertYeuCau(yc);
            if (maYC <= 0) {
                return KetQua.error("Không thể tạo yêu cầu tuyển dụng. Vui lòng thử lại.");
            }
            yc.setMaYeuCau(maYC);
            return KetQua.success(yc, "Tạo yêu cầu tuyển dụng thành công. Đang chờ phê duyệt.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo yêu cầu tuyển dụng: " + e.getMessage());
        }
    }

    public KetQua<Void> duyetYeuCau(int maYC) {
        return updateYeuCauTrangThai(
                maYC,
                RecruitmentStatus.YeuCau.DA_DUYET,
                "Đã phê duyệt yêu cầu tuyển dụng #"
        );
    }

    public KetQua<Void> tuChoiYeuCau(int maYC) {
        return updateYeuCauTrangThai(
                maYC,
                RecruitmentStatus.YeuCau.TU_CHOI,
                "Đã từ chối yêu cầu tuyển dụng #"
        );
    }

    // ============================
    // Tin tuyển dụng
    // ============================

    public List<TinTuyenDung> getAllTinTuyenDung() {
        return recruitmentRepo.findAllTin();
    }

    public KetQua<TinTuyenDung> taoTin(TinTuyenDung tin) {
        if (isBlank(tin.getTieuDe())) {
            return KetQua.error("Tiêu đề tin tuyển dụng không được để trống.");
        }
        if (tin.getMaYeuCau() <= 0) {
            return KetQua.error("Vui lòng chọn yêu cầu tuyển dụng liên kết.");
        }

        tin.setTrangThai(RecruitmentStatus.Tin.DANG_TUYEN);
        tin.setNgayTao(LocalDate.now());

        try {
            int maTin = recruitmentRepo.insertTin(tin);
            if (maTin <= 0) {
                return KetQua.error("Không thể tạo tin tuyển dụng.");
            }
            tin.setMaTin(maTin);
            return KetQua.success(tin, "Tạo tin tuyển dụng thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo tin tuyển dụng: " + e.getMessage());
        }
    }

    public KetQua<Void> dongTin(int maTin) {
        TinTuyenDung tin = recruitmentRepo.findTinById(maTin);
        if (tin == null) {
            return KetQua.error("Không tìm thấy tin tuyển dụng #" + maTin);
        }
        if (RecruitmentStatus.Tin.DA_DONG.equals(tin.getTrangThai())) {
            return KetQua.error("Tin tuyển dụng đã được đóng.");
        }

        tin.setTrangThai(RecruitmentStatus.Tin.DA_DONG);
        try {
            recruitmentRepo.updateTin(tin);
            return KetQua.success(null, "Đã đóng tin tuyển dụng #" + maTin);
        } catch (Exception e) {
            return KetQua.error("Lỗi đóng tin tuyển dụng: " + e.getMessage());
        }
    }

    // ============================
    // Ứng viên
    // ============================

    public List<UngVien> getAllUngVien() {
        return recruitmentRepo.findAllUngVien();
    }

    public KetQua<UngVien> tiepNhanUngVien(UngVien uv) {
        if (isBlank(uv.getHoTen())) {
            return KetQua.error("Họ tên ứng viên không được để trống.");
        }
        if (isBlank(uv.getEmail())) {
            return KetQua.error("Email ứng viên không được để trống.");
        }
        if (uv.getMaTin() <= 0) {
            return KetQua.error("Vui lòng chọn tin tuyển dụng liên kết.");
        }

        TinTuyenDung tin = recruitmentRepo.findTinById(uv.getMaTin());
        if (tin != null) {
            YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
            if (yc != null) {
                if (RecruitmentStatus.YeuCau.DA_TUYEN_DU.equals(yc.getTrangThai())) {
                    return KetQua.error("Yeu cau tuyen dung da tuyen du so luong, khong the tiep nhan them ung vien.");
                }
                if (yc.getHanTuyenDung() != null && LocalDate.now().isAfter(yc.getHanTuyenDung())) {
                    return KetQua.error("Da qua han tuyen dung (" + yc.getHanTuyenDung() + "), khong the tiep nhan them ung vien.");
                }
            }
        }

        uv.setTrangThai(RecruitmentStatus.UngVien.MOI);
        uv.setNgayTao(LocalDate.now());

        try {
            int maUV = recruitmentRepo.insertUngVien(uv);
            if (maUV <= 0) {
                return KetQua.error("Không thể tiếp nhận ứng viên.");
            }
            uv.setMaUngVien(maUV);
            return KetQua.success(uv, "Tiếp nhận ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tiếp nhận ứng viên: " + e.getMessage());
        }
    }

    public KetQua<Void> capNhatTrangThaiUV(int maUV, String trangThai) {
        if (!RecruitmentStatus.isUngVienStatusEditable(trangThai)) {
            return KetQua.error("Trạng thái ứng viên không hợp lệ.");
        }

        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return KetQua.error("Không tìm thấy ứng viên #" + maUV);
        }
        if (daChuyenThanhNhanVien(uv)) {
            return KetQua.error("Ứng viên đã được chuyển thành nhân viên, không thể cập nhật trạng thái.");
        }

        uv.setTrangThai(trangThai);
        try {
            recruitmentRepo.updateUngVien(uv);
            return KetQua.success(null, "Cập nhật trạng thái ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật trạng thái ứng viên: " + e.getMessage());
        }
    }

    public KetQua<String> taoThongDiepXacNhanChuyenUVThanhNV(int maUV) {
        KetQua<UngVien> checkResult = validateUngVienForConversion(maUV);
        if (!checkResult.isSuccess()) {
            return KetQua.error(checkResult.getMessage());
        }

        UngVien uv = checkResult.getData();
        TransferContext context = loadTransferContext(uv);
        String pbInfo = context.phongBanInfo;
        String cvInfo = context.chucVuInfo;
        boolean thieuThongTin = context.missingAppointmentInfo;

        String msg = "Chuyển \"" + uv.getHoTen() + "\" thành nhân viên chính thức?\n\n"
                + "Sẽ tự động bổ nhiệm:\n  Phòng ban : " + pbInfo + "\n  Chức vụ   : " + cvInfo
                + (thieuThongTin ? "\n\nCHÚ Ý: Thiếu thông tin - bổ nhiệm cần tạo thủ công sau." : "");
        return KetQua.success(msg, "OK");
    }

    public KetQua<Void> chuyenUVThanhNV(int maUV) {
        KetQua<UngVien> checkResult = validateUngVienForConversion(maUV);
        if (!checkResult.isSuccess()) {
            return KetQua.error(checkResult.getMessage());
        }
        UngVien uv = checkResult.getData();

        try {
            KetQua<NhanVien> ketQuaNV = taoNhanVienTuUngVien(uv);
            if (!ketQuaNV.isSuccess()) {
                return KetQua.error("Lỗi tạo hồ sơ nhân viên: " + ketQuaNV.getMessage());
            }

            String maNV = ketQuaNV.getData().getMaNhanVien();
            TransferContext context = loadTransferContext(uv);
            String boNhiemNote = tuDongBoNhiemNeuCoThe(uv, context, maNV);

            capNhatUngVienDaChuyen(uv, maNV);
            capNhatTrangThaiYeuCauNeuDuSoLuong(context.tin);

            String thongTinTK = taoThongTinTaiKhoan(maNV);
            return KetQua.success(null,
                    "Chuyển ứng viên thành nhân viên thành công.\nMã NV: " + maNV + boNhiemNote + thongTinTK);
        } catch (Exception e) {
            return KetQua.error("Lỗi chuyển ứng viên thành nhân viên: " + e.getMessage());
        }
    }

    private KetQua<UngVien> validateUngVienForConversion(int maUV) {
        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return KetQua.error("Không tìm thấy ứng viên #" + maUV);
        }
        if (daChuyenThanhNhanVien(uv)) {
            return KetQua.error("Ứng viên này đã được chuyển thành nhân viên (mã NV: " + uv.getMaNV() + ").");
        }
        if (!RecruitmentStatus.UngVien.TRUNG_TUYEN.equals(uv.getTrangThai())) {
            return KetQua.error("Chỉ có thể chuyển ứng viên trạng thái 'Trúng tuyển' thành nhân viên.");
        }
        TinTuyenDung tinConv = recruitmentRepo.findTinById(uv.getMaTin());
        if (tinConv != null) {
            YeuCauTuyenDung ycConv = recruitmentRepo.findYeuCauById(tinConv.getMaYeuCau());
            if (ycConv != null && RecruitmentStatus.YeuCau.DA_TUYEN_DU.equals(ycConv.getTrangThai())) {
                return KetQua.error("Yeu cau tuyen dung da tuyen du so luong, khong the chuyen them nhan vien.");
            }
        }
        return KetQua.success(uv, "OK");
    }

    private KetQua<NhanVien> taoNhanVienTuUngVien(UngVien uv) {
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(NhanVienBUS.getInstance().generateMaNhanVien());
        nv.setTrangThai("dang_lam_viec");
        nv.setNgayVaoLam(LocalDate.now());

        ThongTinCaNhan ttcn = new ThongTinCaNhan();
        ttcn.setHoTen(uv.getHoTen());
        ttcn.setEmail(uv.getEmail());
        ttcn.setDienThoai(uv.getDienThoai());
        ttcn.setNgaySinh(uv.getNgaySinh());
        ttcn.setGioiTinh(uv.getGioiTinh());
        ttcn.setDiaChi(uv.getDiaChi());
        ttcn.setTrinhDoHocVan(uv.getTrinhDoHocVan());
        ttcn.setKinhNghiem(uv.getKinhNghiem());
        ttcn.setFileCV(uv.getFileCV());

        return NhanVienBUS.getInstance().taoHoSo(nv, ttcn);
    }

    private String tuDongBoNhiemNeuCoThe(UngVien uv, TransferContext context, String maNV) {
        if (context.tin == null) {
            return " | Không tìm thấy tin tuyển dụng để tạo bổ nhiệm";
        }
        if (context.yeuCau == null || isBlank(context.yeuCau.getId()) || isBlank(context.yeuCau.getMaChucVu())) {
            return " | Thiếu thông tin phòng ban/chức vụ, bổ nhiệm cần tạo thủ công";
        }

        BoNhiem boNhiem = new BoNhiem();
        boNhiem.setNhanVienId(maNV);
        boNhiem.setPhongBanId(context.yeuCau.getId());
        boNhiem.setChucVuId(context.yeuCau.getMaChucVu());
        boNhiem.setLoaiBoNhiem("chinh");
        boNhiem.setTyLeHuongLuong(100);
        boNhiem.setQuanLyId(null);
        boNhiem.setTuNgay(LocalDate.now());
        boNhiem.setLyDo("Tự động bổ nhiệm khi chuyển ứng viên #" + uv.getMaUngVien());

        KetQua<BoNhiem> kqBoNhiem = BoNhiemBUS.getInstance().taoBoNhiem(boNhiem);
        if (!kqBoNhiem.isSuccess()) {
            return " | Bổ nhiệm thất bại (cần tạo thủ công): " + kqBoNhiem.getMessage();
        }

        KetQua<BoNhiem> kqPheDuyet = BoNhiemBUS.getInstance()
                .pheDuyetBoNhiem(kqBoNhiem.getData().getMaBoNhiem(), getCurrentUserNhanVienId());
        if (kqPheDuyet.isSuccess()) {
            return " | Bổ nhiệm đã được tạo và phê duyệt";
        }
        return " | Bổ nhiệm tạo nhưng chưa phê duyệt: " + kqPheDuyet.getMessage();
    }

    private void capNhatUngVienDaChuyen(UngVien uv, String maNV) {
        uv.setMaNV(maNV);
        uv.setTrangThai(RecruitmentStatus.UngVien.DA_CHUYEN_NHAN_VIEN);
        recruitmentRepo.updateUngVien(uv);
    }

    private void capNhatTrangThaiYeuCauNeuDuSoLuong(TinTuyenDung tin) {
        if (tin == null) {
            return;
        }
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
        if (yc == null || RecruitmentStatus.YeuCau.DA_TUYEN_DU.equals(yc.getTrangThai())) {
            return;
        }

        int daChuyen = recruitmentRepo.countConvertedByYeuCau(tin.getMaYeuCau());
        if (daChuyen >= yc.getSoLuong()) {
            recruitmentRepo.updateYeuCauTrangThai(tin.getMaYeuCau(), RecruitmentStatus.YeuCau.DA_TUYEN_DU, 0, null);
        }
    }

    private String taoThongTinTaiKhoan(String maNV) {
        TaiKhoan tk = XacThucBUS.getInstance().findByMaNV(maNV);
        if (tk != null) {
            return " | Tài khoản: " + tk.getTenDangNhap();
        }
        return " | Tài khoản chưa được tạo";
    }

    private TransferContext loadTransferContext(UngVien uv) {
        TinTuyenDung tin = recruitmentRepo.findTinById(uv.getMaTin());
        YeuCauTuyenDung yc = tin != null ? recruitmentRepo.findYeuCauById(tin.getMaYeuCau()) : null;

        String pb = "(chưa xác định)";
        String cv = "(chưa xác định)";
        if (tin != null) {
            pb = isBlank(tin.getTenPhongBan()) ? pb : tin.getTenPhongBan();
            cv = isBlank(tin.getTenChucVu()) ? cv : tin.getTenChucVu();
        }
        if (yc != null) {
            pb = isBlank(pb) || "(chưa xác định)".equals(pb)
                    ? (isBlank(yc.getTenPhongBan()) ? pb : yc.getTenPhongBan())
                    : pb;
            cv = isBlank(cv) || "(chưa xác định)".equals(cv)
                    ? (isBlank(yc.getTenChucVu()) ? cv : yc.getTenChucVu())
                    : cv;
        }

        boolean missingInfo = "(chưa xác định)".equals(pb) || "(chưa xác định)".equals(cv);
        return new TransferContext(tin, yc, pb, cv, missingInfo);
    }

    private KetQua<Void> updateYeuCauTrangThai(int maYC, String trangThaiMoi, String successPrefix) {
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return KetQua.error("Không tìm thấy yêu cầu tuyển dụng #" + maYC);
        }
        if (!RecruitmentStatus.YeuCau.CHO_DUYET.equals(yc.getTrangThai())) {
            return KetQua.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }
        recruitmentRepo.updateYeuCauTrangThai(maYC, trangThaiMoi, getCurrentUserId(), LocalDateTime.now());
        return KetQua.success(null, successPrefix + maYC);
    }

    private int getCurrentUserId() {
        return SessionContext.getInstance().getCurrentUser() != null
                ? SessionContext.getInstance().getCurrentUser().getId() : 0;
    }

    private String getCurrentUserNhanVienId() {
        if (SessionContext.getInstance().getCurrentUser() == null) {
            return "admin";
        }
        String nvId = SessionContext.getInstance().getCurrentUser().getNhanVienId();
        return isBlank(nvId) ? "admin" : nvId;
    }

    private boolean daChuyenThanhNhanVien(UngVien uv) {
        return uv.getMaNV() != null && !uv.getMaNV().trim().isEmpty();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class TransferContext {
        private final TinTuyenDung tin;
        private final YeuCauTuyenDung yeuCau;
        private final String phongBanInfo;
        private final String chucVuInfo;
        private final boolean missingAppointmentInfo;

        private TransferContext(TinTuyenDung tin, YeuCauTuyenDung yeuCau,
                String phongBanInfo, String chucVuInfo, boolean missingAppointmentInfo) {
            this.tin = tin;
            this.yeuCau = yeuCau;
            this.phongBanInfo = phongBanInfo;
            this.chucVuInfo = chucVuInfo;
            this.missingAppointmentInfo = missingAppointmentInfo;
        }
    }
}
