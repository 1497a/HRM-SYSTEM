package com.hrm.bus;

import com.hrm.dao.ThongBaoDAO;
import com.hrm.model.ThongBao;
import com.hrm.util.HRMConstants;
import com.hrm.util.ValidationUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Service cho module thông báo.
 * Singleton pattern.
 */
public class ThongBaoBUS {
    private static ThongBaoBUS instance;
    private final ThongBaoDAO thongBaoRepo;
    private ThongBaoBUS() {
        this.thongBaoRepo = ThongBaoDAO.getInstance();
    }

    public static synchronized ThongBaoBUS getInstance() {
        if (instance == null) {
            instance = new ThongBaoBUS();
        }
        return instance;
    }

    public KetQua<Void> guiThongBaoHeThong(int maTaiKhoanNhan, String tieuDe, String noiDung) {
        return guiThongBao(0, maTaiKhoanNhan, tieuDe, noiDung, HRMConstants.LOAI_TB_HE_THONG);
    }

    public KetQua<Void> guiThongBaoChoMaNV(String maNV, String tieuDe, String noiDung) {
        Integer maTaiKhoanNhan = thongBaoRepo.findMaTaiKhoanByMaNV(maNV);
        if (maTaiKhoanNhan == null) {
            return KetQua.error("Không tìm thấy tài khoản cho nhân viên " + maNV + ".");
        }
        return guiThongBaoHeThong(maTaiKhoanNhan, tieuDe, noiDung);
    }

    public KetQua<Void> guiThongBao(int maTaiKhoanGui, int maTaiKhoanNhan, String tieuDe, String noiDung) {
        return guiThongBao(maTaiKhoanGui, maTaiKhoanNhan, tieuDe, noiDung, HRMConstants.LOAI_TB_CHUNG);
    }

    public KetQua<Void> guiThongBao(int maTaiKhoanGui, int maTaiKhoanNhan, String tieuDe, String noiDung, String loai) {
        KetQua<Void> validation = validateNotificationPayload(tieuDe, noiDung);
        if (!validation.isSuccess()) return validation;
        if (maTaiKhoanNhan <= 0) {
            return KetQua.error("Người nhận thông báo không hợp lệ.");
        }
        thongBaoRepo.insert(buildThongBao(maTaiKhoanGui, maTaiKhoanNhan, tieuDe, noiDung, loai));
        return KetQua.success(null, "Đã gửi thông báo thành công.");
    }

    public KetQua<Void> guiThongBaoCaNhan(int nguoiGui, String maNVNhan, String tieuDe, String noiDung) {
        return guiThongBaoCaNhan(nguoiGui, maNVNhan, tieuDe, noiDung, HRMConstants.LOAI_TB_CHUNG);
    }

    public KetQua<Void> guiThongBaoCaNhan(int nguoiGui, String maNVNhan, String tieuDe, String noiDung, String loai) {
        KetQua<Void> validation = validateNotificationPayload(tieuDe, noiDung);
        if (!validation.isSuccess()) return validation;
        Integer maTaiKhoanNhan = findTaiKhoanByMaNV(maNVNhan);
        if (maTaiKhoanNhan == null) {
            return KetQua.error("Không tìm thấy tài khoản cho nhân viên " + maNVNhan + ".");
        }
        thongBaoRepo.insert(buildThongBao(nguoiGui, maTaiKhoanNhan, tieuDe, noiDung, loai));
        return KetQua.success(null, "Đã gửi thông báo thành công.");
    }

    public KetQua<Void> guiThongBaoPhongBan(int nguoiGui, String maPhongBan, String tieuDe, String noiDung) {
        return guiThongBaoPhongBan(nguoiGui, maPhongBan, tieuDe, noiDung, HRMConstants.LOAI_TB_CHUNG);
    }

    public KetQua<Void> guiThongBaoPhongBan(int nguoiGui, String maPhongBan, String tieuDe, String noiDung, String loai) {
        List<Integer> maTaiKhoanList = thongBaoRepo.findTaiKhoanByPhongBan(maPhongBan);
        return sendBulk(nguoiGui, maTaiKhoanList, tieuDe, noiDung, loai);
    }

    public KetQua<Void> guiThongBaoChucVu(int nguoiGui, String maChucVu, String tieuDe, String noiDung) {
        return guiThongBaoChucVu(nguoiGui, maChucVu, tieuDe, noiDung, HRMConstants.LOAI_TB_CHUNG);
    }

    public KetQua<Void> guiThongBaoChucVu(int nguoiGui, String maChucVu, String tieuDe, String noiDung, String loai) {
        List<Integer> maTaiKhoanList = thongBaoRepo.findTaiKhoanByChucVu(maChucVu);
        return sendBulk(nguoiGui, maTaiKhoanList, tieuDe, noiDung, loai);
    }

    public KetQua<Void> guiThongBaoTatCa(int nguoiGui, String tieuDe, String noiDung) {
        return guiThongBaoTatCa(nguoiGui, tieuDe, noiDung, HRMConstants.LOAI_TB_CHUNG);
    }

    public KetQua<Void> guiThongBaoTatCa(int nguoiGui, String tieuDe, String noiDung, String loai) {
        List<Integer> maTaiKhoanList = thongBaoRepo.findAllActiveTaiKhoan();
        return sendBulk(nguoiGui, maTaiKhoanList, tieuDe, noiDung, loai);
    }

    public KetQua<Void> guiThongBaoTheoDanhSachMaNV(int nguoiGui, List<String> dsMaNV, String tieuDe, String noiDung, String loai) {
        KetQua<Void> validation = validateNotificationPayload(tieuDe, noiDung);
        if (!validation.isSuccess()) return validation;
        if (dsMaNV == null || dsMaNV.isEmpty()) {
            return KetQua.error("Danh sách người nhận không được để trống.");
        }
        Set<Integer> maTaiKhoanSet = new LinkedHashSet<>();
        for (String maNV : dsMaNV) {
            if (maNV == null || maNV.trim().isEmpty()) {
                continue;
            }
            Integer maTaiKhoanNhan = findTaiKhoanByMaNV(maNV);
            if (maTaiKhoanNhan != null) {
                maTaiKhoanSet.add(maTaiKhoanNhan);
            }
        }
        return sendBulk(nguoiGui, new ArrayList<>(maTaiKhoanSet), tieuDe, noiDung, loai);
    }

    public KetQua<Void> danhDauDaDoc(int maThongBao) {
        try {
            thongBaoRepo.markAsRead(maThongBao);
            return KetQua.success(null, "Đã đánh dấu là đã đọc.");
        } catch (Exception e) {
            System.err.println("Lỗi danhDauDaDoc: " + e.getMessage());
            return KetQua.error("Không thể đánh dấu thông báo: " + e.getMessage());
        }
    }

    public KetQua<Void> danhDauTatCaDaDoc(int maTaiKhoanNhan) {
        try {
            thongBaoRepo.markAllAsRead(maTaiKhoanNhan);
            return KetQua.success(null, "Đã đánh dấu tất cả là đã đọc.");
        } catch (Exception e) {
            System.err.println("Lỗi danhDauTatCaDaDoc: " + e.getMessage());
            return KetQua.error("Không thể cập nhật thông báo: " + e.getMessage());
        }
    }

    public List<ThongBao> getThongBaoCuaToi(int maTaiKhoanNhan) {
        return thongBaoRepo.findByNguoiNhan(maTaiKhoanNhan);
    }

    public int demChuaDoc(int maTaiKhoanNhan) {
        return thongBaoRepo.countUnread(maTaiKhoanNhan);
    }

    private ThongBao buildThongBao(int nguoiGui, int nguoiNhan, String tieuDe, String noiDung, String loai) {
        ThongBao tb = new ThongBao();
        tb.setMaTaiKhoanGui(nguoiGui);
        tb.setMaTaiKhoanNhan(nguoiNhan);
        tb.setTieuDe(tieuDe);
        tb.setNoiDung(noiDung);
        tb.setLoaiThongBao(normalizeLoai(loai));
        return tb;
    }

    private KetQua<Void> sendBulk(int nguoiGui, List<Integer> maTaiKhoanList, String tieuDe, String noiDung, String loai) {
        KetQua<Void> validation = validateNotificationPayload(tieuDe, noiDung);
        if (!validation.isSuccess()) return validation;
        if (maTaiKhoanList == null || maTaiKhoanList.isEmpty()) {
            return KetQua.error("Không có người nhận hợp lệ để gửi thông báo.");
        }
        List<ThongBao> batch = new ArrayList<>();
        for (int maTK : maTaiKhoanList) {
            batch.add(buildThongBao(nguoiGui, maTK, tieuDe, noiDung, loai));
        }
        thongBaoRepo.insertBulk(batch);
        return KetQua.success(null, "Đã gửi thông báo thành công.");
    }

    private String normalizeLoai(String loai) {
        if (HRMConstants.LOAI_TB_HE_THONG.equals(loai) || HRMConstants.LOAI_TB_DON_TU.equals(loai) || HRMConstants.LOAI_TB_CHUNG.equals(loai)) {
            return loai;
        }
        return HRMConstants.LOAI_TB_CHUNG;
    }

    private Integer findTaiKhoanByMaNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            return null;
        }
        return thongBaoRepo.findMaTaiKhoanByMaNV(maNV.trim());
    }

    private KetQua<Void> validateNotificationPayload(String tieuDe, String noiDung) {
        String titleErr = ValidationUtils.validateNotificationTitle(tieuDe);
        if (titleErr != null) return KetQua.error(titleErr);
        String contentErr = ValidationUtils.validateNotificationContent(noiDung);
        if (contentErr != null) return KetQua.error(contentErr);
        return KetQua.success(null, "");
    }
}
