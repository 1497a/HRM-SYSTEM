package com.hrm.bus;

import com.hrm.model.NhanVien;
import com.hrm.model.BoNhiem;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.model.TinTuyenDung;
import com.hrm.model.UngVien;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.dao.TuyenDungDAO;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    public KetQua<YeuCauTuyenDung> taoYeuCau(YeuCauTuyenDung yc) {
        // Validate vị trí
        if (yc.getMaChucVu() == null || yc.getMaChucVu().trim().isEmpty()) {
            return KetQua.error("Chức vụ/vị trí tuyển dụng không được để trống.");
        }
        // Validate số lượng
        if (yc.getSoLuong() <= 0) {
            return KetQua.error("Số lượng tuyển dụng phải lớn hơn 0.");
        }

        // Thiết lập trạng thái và ngày tạo
        yc.setTrangThai("cho_duyet");

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
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return KetQua.error("Không tìm thấy yêu cầu tuyển dụng #" + maYC);
        }
        if (!"cho_duyet".equals(yc.getTrangThai())) {
            return KetQua.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }

        int nguoiDuyetId = SessionContext.getInstance().getCurrentUser() != null
                ? SessionContext.getInstance().getCurrentUser().getId() : 0;

        recruitmentRepo.updateYeuCauTrangThai(maYC, "da_duyet", nguoiDuyetId, LocalDateTime.now());
        return KetQua.success(null, "Đã phê duyệt yêu cầu tuyển dụng #" + maYC);
    }

    public KetQua<Void> tuChoiYeuCau(int maYC) {
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return KetQua.error("Không tìm thấy yêu cầu tuyển dụng #" + maYC);
        }
        if (!"cho_duyet".equals(yc.getTrangThai())) {
            return KetQua.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }

        int nguoiDuyetId = SessionContext.getInstance().getCurrentUser() != null
                ? SessionContext.getInstance().getCurrentUser().getId() : 0;

        recruitmentRepo.updateYeuCauTrangThai(maYC, "tu_choi", nguoiDuyetId, LocalDateTime.now());
        return KetQua.success(null, "Đã từ chối yêu cầu tuyển dụng #" + maYC);
    }

    // ============================
    // Tin tuyển dụng
    // ============================

    public List<TinTuyenDung> getAllTinTuyenDung() {
        return recruitmentRepo.findAllTin();
    }

    public KetQua<TinTuyenDung> taoTin(TinTuyenDung tin) {
        if (tin.getTieuDe() == null || tin.getTieuDe().trim().isEmpty()) {
            return KetQua.error("Tiêu đề tin tuyển dụng không được để trống.");
        }
        if (tin.getMaYeuCau() <= 0) {
            return KetQua.error("Vui lòng chọn yêu cầu tuyển dụng liên kết.");
        }

        tin.setTrangThai("dang_tuyen");
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
        if ("da_dong".equals(tin.getTrangThai())) {
            return KetQua.error("Tin tuyển dụng đã được đóng.");
        }

        tin.setTrangThai("da_dong");
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
        if (uv.getHoTen() == null || uv.getHoTen().trim().isEmpty()) {
            return KetQua.error("Họ tên ứng viên không được để trống.");
        }
        if (uv.getEmail() == null || uv.getEmail().trim().isEmpty()) {
            return KetQua.error("Email ứng viên không được để trống.");
        }
        if (uv.getMaTin() <= 0) {
            return KetQua.error("Vui lòng chọn tin tuyển dụng liên kết.");
        }

        uv.setTrangThai("moi");
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
        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return KetQua.error("Không tìm thấy ứng viên #" + maUV);
        }

        // Kiểm tra đã chuyển thành nhân viên chưa (maNV là String)
        if (uv.getMaNV() != null && !uv.getMaNV().trim().isEmpty()) {
            return KetQua.error("Ứng viên đã được chuyển thành nhân viên (mã NV: " + uv.getMaNV() + "), không thể cập nhật trạng thái tuyển dụng.");
        }

        uv.setTrangThai(trangThai);
        try {
            recruitmentRepo.updateUngVien(uv);
            return KetQua.success(null, "Cập nhật trạng thái ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật trạng thái ứng viên: " + e.getMessage());
        }
    }

    public KetQua<Void> chuyenUVThanhNV(int maUV) {
        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return KetQua.error("Không tìm thấy ứng viên #" + maUV);
        }

        // Kiểm tra đã chuyển thành nhân viên chưa
        if (uv.getMaNV() != null && !uv.getMaNV().trim().isEmpty()) {
            return KetQua.error("Ứng viên này đã được chuyển thành nhân viên (mã NV: " + uv.getMaNV() + ").");
        }

        if (!"trung_tuyen".equals(uv.getTrangThai())) {
            return KetQua.error("Chỉ có thể chuyển ứng viên trạng thái 'Trúng tuyển' thành nhân viên.");
        }

        try {
            // Tạo nhân viên mới từ thông tin ứng viên
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

            // Tạo hồ sơ nhân viên
            KetQua<NhanVien> ketQuaNV = NhanVienBUS.getInstance().taoHoSo(nv, ttcn);
            if (!ketQuaNV.isSuccess()) {
                return KetQua.error("Lỗi tạo hồ sơ nhân viên: " + ketQuaNV.getMessage());
            }

            String newMaNV = ketQuaNV.getData().getMaNhanVien();  // Lấy mã String

            // Tự động bổ nhiệm (không block nếu thất bại)
            String boNhiemNote = "";
            TinTuyenDung tin = recruitmentRepo.findTinById(uv.getMaTin());
            if (tin != null) {
                YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
                if (yc != null && yc.getId() != null && !yc.getId().trim().isEmpty()
                        && yc.getMaChucVu() != null && !yc.getMaChucVu().trim().isEmpty()) {

                    BoNhiem boNhiem = new BoNhiem();
                    boNhiem.setNhanVienId(newMaNV);  // String
                    boNhiem.setPhongBanId(yc.getId());
                    boNhiem.setChucVuId(yc.getMaChucVu());
                    boNhiem.setLoaiBoNhiem("chinh");
                    boNhiem.setTyLeHuongLuong(100);
                    boNhiem.setQuanLyId(null);  // hoặc mã quản lý nếu có
                    boNhiem.setTuNgay(LocalDate.now());
                    boNhiem.setLyDo("Tự động bổ nhiệm khi chuyển ứng viên #" + uv.getMaUngVien());

                    KetQua<BoNhiem> kqBoNhiem = BoNhiemBUS.getInstance().taoBoNhiem(boNhiem);
                    if (kqBoNhiem.isSuccess()) {
                        int nguoiDuyetId = SessionContext.getInstance().getCurrentUser() != null
                                ? SessionContext.getInstance().getCurrentUser().getId() : 0;
                        KetQua<BoNhiem> kqPheDuyet = BoNhiemBUS.getInstance()
                                .pheDuyetBoNhiem(kqBoNhiem.getData().getMaBoNhiem(), nguoiDuyetId);
                        boNhiemNote = kqPheDuyet.isSuccess()
                                ? " | Bổ nhiệm đã được tạo và phê duyệt"
                                : " | Bổ nhiệm tạo nhưng chưa phê duyệt: " + kqPheDuyet.getMessage();
                    } else {
                        boNhiemNote = " | Bổ nhiệm thất bại (cần tạo thủ công): " + kqBoNhiem.getMessage();
                    }
                } else {
                    boNhiemNote = " | Thiếu thông tin phòng ban/chức vụ, bổ nhiệm cần tạo thủ công";
                }
            } else {
                boNhiemNote = " | Không tìm thấy tin tuyển dụng để tạo bổ nhiệm";
            }

            // LUÔN cập nhật trạng thái ứng viên
            uv.setMaNV(newMaNV);
            uv.setTrangThai("da_chuyen_nhan_vien");
            recruitmentRepo.updateUngVien(uv);

            // Kiểm tra đủ số lượng → tự cập nhật trạng thái YeuCau
            if (tin != null) {
                YeuCauTuyenDung ycCheck = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
                if (ycCheck != null && !"da_tuyen_du".equals(ycCheck.getTrangThai())) {
                    int daChuyen = recruitmentRepo.countConvertedByYeuCau(tin.getMaYeuCau());
                    if (daChuyen >= ycCheck.getSoLuong()) {
                        recruitmentRepo.updateYeuCauTrangThai(
                                tin.getMaYeuCau(), "da_tuyen_du", 0, null);
                    }
                }
            }

            // Kiểm tra tài khoản
            com.hrm.model.TaiKhoan tk = XacThucBUS.getInstance().findByMaNV(newMaNV);
            String thongTinTK = tk != null
                    ? " | Tài khoản: " + tk.getTenDangNhap()
                    : " | Tài khoản chưa được tạo";

            return KetQua.success(null,
                    "Chuyển ứng viên thành nhân viên thành công.\n"
                            + "Mã NV: " + newMaNV + boNhiemNote + thongTinTK);
        } catch (Exception e) {
            return KetQua.error("Lỗi chuyển ứng viên thành nhân viên: " + e.getMessage());
        }
    }
}