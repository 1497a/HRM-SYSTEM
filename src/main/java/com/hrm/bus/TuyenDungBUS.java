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

    /**
     * Tạo yêu cầu tuyển dụng mới.
     */
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

    /**
     * Phê duyệt yêu cầu tuyển dụng.
     */
    public KetQua<Void> duyetYeuCau(int maYC) {
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return KetQua.error("Không tìm thấy yêu cầu tuyển dụng.");
        }
        if (!"cho_duyet".equals(yc.getTrangThai())) {
            return KetQua.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }

        int nguoiDuyetId = 0;
        if (SessionContext.getInstance().getCurrentUser() != null) {
            nguoiDuyetId = SessionContext.getInstance().getCurrentUser().getId();
        }

        recruitmentRepo.updateYeuCauTrangThai(maYC, "da_duyet", nguoiDuyetId, LocalDateTime.now());
        return KetQua.success(null, "Đã phê duyệt yêu cầu tuyển dụng.");
    }

    /**
     * Từ chối yêu cầu tuyển dụng.
     */
    public KetQua<Void> tuChoiYeuCau(int maYC) {
        YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(maYC);
        if (yc == null) {
            return KetQua.error("Không tìm thấy yêu cầu tuyển dụng.");
        }
        if (!"cho_duyet".equals(yc.getTrangThai())) {
            return KetQua.error("Yêu cầu này không ở trạng thái chờ duyệt.");
        }

        int nguoiDuyetId = 0;
        if (SessionContext.getInstance().getCurrentUser() != null) {
            nguoiDuyetId = SessionContext.getInstance().getCurrentUser().getId();
        }

        recruitmentRepo.updateYeuCauTrangThai(maYC, "tu_choi", nguoiDuyetId, LocalDateTime.now());
        return KetQua.success(null, "Đã từ chối yêu cầu tuyển dụng.");
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
    public KetQua<TinTuyenDung> taoTin(TinTuyenDung tin) {
        // Validate tiêu đề
        if (tin.getTieuDe() == null || tin.getTieuDe().trim().isEmpty()) {
            return KetQua.error("Tiêu đề tin tuyển dụng không được để trống.");
        }
        // Validate mã yêu cầu
        if (tin.getMaYeuCau() <= 0) {
            return KetQua.error("Vui lòng chọn yêu cầu tuyển dụng liên kết.");
        }

        // Thiết lập trạng thái và ngày đăng
        tin.setTrangThai("dang_tuyen");
        tin.setNgayTao(LocalDate.now());

        try {
            int maTin = recruitmentRepo.insertTin(tin);
            if (maTin <= 0) {
                return KetQua.error("Không thể tạo tin tuyển dụng. Vui lòng thử lại.");
            }
            tin.setMaTin(maTin);
            return KetQua.success(tin, "Tạo tin tuyển dụng thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tạo tin tuyển dụng: " + e.getMessage());
        }
    }

    /**
     * Đóng tin tuyển dụng.
     */
    public KetQua<Void> dongTin(int maTin) {
        TinTuyenDung tin = recruitmentRepo.findTinById(maTin);
        if (tin == null) {
            return KetQua.error("Không tìm thấy tin tuyển dụng.");
        }
        if ("da_dong".equals(tin.getTrangThai())) {
            return KetQua.error("Tin tuyển dụng đã được đóng.");
        }

        tin.setTrangThai("da_dong");
        try {
            recruitmentRepo.updateTin(tin);
            return KetQua.success(null, "Đã đóng tin tuyển dụng.");
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

    /**
     * Tiếp nhận ứng viên nộp hồ sơ.
     */
    public KetQua<UngVien> tiepNhanUngVien(UngVien uv) {
        // Validate họ tên
        if (uv.getHoTen() == null || uv.getHoTen().trim().isEmpty()) {
            return KetQua.error("Họ tên ứng viên không được để trống.");
        }
        // Validate email
        if (uv.getEmail() == null || uv.getEmail().trim().isEmpty()) {
            return KetQua.error("Email ứng viên không được để trống.");
        }
        // Validate mã tin
        if (uv.getMaTin() <= 0) {
            return KetQua.error("Vui lòng chọn tin tuyển dụng liên kết.");
        }

        // Thiết lập trạng thái và ngày nộp
        uv.setTrangThai("moi");
        uv.setNgayTao(LocalDate.now());

        try {
            int maUV = recruitmentRepo.insertUngVien(uv);
            if (maUV <= 0) {
                return KetQua.error("Không thể tiếp nhận ứng viên. Vui lòng thử lại.");
            }
            uv.setMaUngVien(maUV);
            return KetQua.success(uv, "Tiếp nhận ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi tiếp nhận ứng viên: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái ứng viên.
     */
    public KetQua<Void> capNhatTrangThaiUV(int maUV, String trangThai) {
        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return KetQua.error("Không tìm thấy ứng viên.");
        }

        uv.setTrangThai(trangThai);
        try {
            recruitmentRepo.updateUngVien(uv);
            return KetQua.success(null, "Cập nhật trạng thái ứng viên thành công.");
        } catch (Exception e) {
            return KetQua.error("Lỗi cập nhật trạng thái ứng viên: " + e.getMessage());
        }
    }

    /**
     * Chuyển ứng viên trúng tuyển thành nhân viên chính thức.
     */
        public KetQua<Void> chuyenUVThanhNV(int maUV) {
        UngVien uv = recruitmentRepo.findById(maUV);
        if (uv == null) {
            return KetQua.error("Khong tim thay ung vien.");
        }
        if (uv.getMaNV() > 0) {
            return KetQua.error("Ung vien nay da duoc chuyen thanh nhan vien (maNV=" + uv.getMaNV() + ").");
        }
        if (!"trung_tuyen".equals(uv.getTrangThai())) {
            return KetQua.error("Chi co the chuyen ung vien co trang thai 'Trung tuyen' thanh nhan vien.");
        }

        try {
            // Tao nhan vien moi tu thong tin ung vien
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

            KetQua<NhanVien> ketQua = NhanVienBUS.getInstance().taoHoSo(nv, ttcn);
            if (!ketQua.isSuccess()) {
                return KetQua.error("Loi tao ho so nhan vien: " + ketQua.getMessage());
            }

            TinTuyenDung tin = recruitmentRepo.findTinById(uv.getMaTin());
            if (tin == null) {
                return KetQua.error("Khong tim thay tin tuyen dung lien ket cua ung vien.");
            }
            YeuCauTuyenDung yc = recruitmentRepo.findYeuCauById(tin.getMaYeuCau());
            if (yc == null) {
                return KetQua.error("Khong tim thay yeu cau tuyen dung lien ket cua ung vien.");
            }
            if (yc.getId() == null || yc.getId().trim().isEmpty() || yc.getMaChucVu() == null || yc.getMaChucVu().trim().isEmpty()) {
                return KetQua.error("Yeu cau tuyen dung thieu phong ban/chuc vu, khong the tao bo nhiem tu dong.");
            }

            BoNhiem boNhiem = new BoNhiem();
            boNhiem.setMaNV(ketQua.getData().getId());
            boNhiem.setPhongBanId(yc.getId());
            boNhiem.setChucVuId(yc.getMaChucVu());
            boNhiem.setLoaiBoNhiem("chinh");
            boNhiem.setTyLeHuongLuong(100);
            boNhiem.setMaQuanLy(0);
            boNhiem.setTuNgay(LocalDate.now());
            boNhiem.setLyDo("Tu dong bo nhiem khi chuyen ung vien #" + uv.getMaUngVien() + " thanh nhan vien");

            KetQua<BoNhiem> kqBoNhiem = BoNhiemBUS.getInstance().taoBoNhiem(boNhiem);
            if (!kqBoNhiem.isSuccess()) {
                return KetQua.error("Da tao nhan vien nhung tao bo nhiem that bai: " + kqBoNhiem.getMessage());
            }

            int nguoiDuyetId = 0;
            if (SessionContext.getInstance().getCurrentUser() != null) {
                nguoiDuyetId = SessionContext.getInstance().getCurrentUser().getId();
            }
            KetQua<BoNhiem> kqPheDuyet = BoNhiemBUS.getInstance()
                    .pheDuyetBoNhiem(kqBoNhiem.getData().getMaBoNhiem(), nguoiDuyetId);
            if (!kqPheDuyet.isSuccess()) {
                return KetQua.error("Da tao nhan vien va bo nhiem nhung phe duyet bo nhiem that bai: " + kqPheDuyet.getMessage());
            }

            // Lien ket ung vien voi nhan vien va cap nhat trang thai
            uv.setMaNV(ketQua.getData().getId());
            uv.setTrangThai("trung_tuyen");
            recruitmentRepo.updateUngVien(uv);

            com.hrm.model.TaiKhoan tk = XacThucBUS.getInstance().findByMaNV(ketQua.getData().getId());
            String thongTinTK = (tk != null)
                    ? (" | Tai khoan: " + tk.getTenDangNhap())
                    : " | Tai khoan chua duoc tao";

            return KetQua.success(null,
                    "Chuyen ung vien thanh nhan vien thanh cong. Ma NV: " + nv.getMaNhanVien()
                            + " | Bo nhiem da duoc tao va phe duyet" + thongTinTK);
        } catch (Exception e) {
            return KetQua.error("Loi chuyen ung vien thanh nhan vien: " + e.getMessage());
        }
    }
}

