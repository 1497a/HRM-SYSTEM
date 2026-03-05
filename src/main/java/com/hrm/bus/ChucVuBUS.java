package com.hrm.bus;

import com.hrm.model.ChucVu;
import com.hrm.model.LichSuHeSoLuong;
import com.hrm.dao.ChucVuDAO;
import com.hrm.dao.LichSuLuongDAO;
import com.hrm.util.SessionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service quÃ¡ÂºÂ£n lÃƒÂ½ chÃ¡Â»Â©c vÃ¡Â»Â¥.
 * ÃƒÂp dÃ¡Â»Â¥ng business logic, delegate persistence xuÃ¡Â»â€˜ng ChucVuDAO (JDBC).
 * Ghi lÃ¡Â»â€¹ch sÃ¡Â»Â­ khi hÃ¡Â»â€¡ sÃ¡Â»â€˜ lÃ†Â°Ã†Â¡ng hoÃ¡ÂºÂ·c phÃ¡Â»Â¥ cÃ¡ÂºÂ¥p thay Ã„â€˜Ã¡Â»â€¢i.
 */
public class ChucVuBUS {

    private final ChucVuDAO positionRepo = new ChucVuDAO();
    private final LichSuLuongDAO historyRepo = LichSuLuongDAO.getInstance();

    /**
     * LÃ¡ÂºÂ¥y tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ chÃ¡Â»Â©c vÃ¡Â»Â¥.
     */
    public List<ChucVu> getAllPositions() {
        return positionRepo.findAll();
    }

    /**
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch chÃ¡Â»Â©c vÃ¡Â»Â¥ Ã„â€˜ang hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng.
     */
    public List<ChucVu> getActivePositions() {
        return positionRepo.findActive();
    }

    /**
     * TÃƒÂ¬m chÃ¡Â»Â©c vÃ¡Â»Â¥ theo mÃƒÂ£.
     */
    public ChucVu getById(String maChucVu) {
        return positionRepo.findById(maChucVu);
    }

    /**
     * LÃ¡ÂºÂ¥y lÃ¡Â»â€¹ch sÃ¡Â»Â­ thay Ã„â€˜Ã¡Â»â€¢i hÃ¡Â»â€¡ sÃ¡Â»â€˜ lÃ†Â°Ã†Â¡ng cÃ¡Â»Â§a mÃ¡Â»â„¢t chÃ¡Â»Â©c vÃ¡Â»Â¥.
     */
    public List<LichSuHeSoLuong> getHistoryByMaChucVu(String maChucVu) {
        return historyRepo.findByMaChucVu(maChucVu);
    }

    /**
     * ThÃƒÂªm chÃ¡Â»Â©c vÃ¡Â»Â¥ mÃ¡Â»â€ºi.
     *
     * @param maChucVu     mÃƒÂ£ chÃ¡Â»Â©c vÃ¡Â»Â¥ (duy nhÃ¡ÂºÂ¥t)
     * @param tenChucVu    tÃƒÂªn chÃ¡Â»Â©c vÃ¡Â»Â¥
     * @param capBac       cÃ¡ÂºÂ¥p bÃ¡ÂºÂ­c (1 lÃƒÂ  cao nhÃ¡ÂºÂ¥t)
     * @param heSoLuong    hÃ¡Â»â€¡ sÃ¡Â»â€˜ lÃ†Â°Ã†Â¡ng (phÃ¡ÂºÂ£i > 0)
     * @param phuCapChucVu phÃ¡Â»Â¥ cÃ¡ÂºÂ¥p chÃ¡Â»Â©c vÃ¡Â»Â¥ (>= 0)
     * @param moTa         mÃƒÂ´ tÃ¡ÂºÂ£
     * @throws IllegalArgumentException nÃ¡ÂºÂ¿u dÃ¡Â»Â¯ liÃ¡Â»â€¡u khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡
     */
    public void addPosition(String maChucVu, String tenChucVu, int capBac,
                            double heSoLuong, double phuCapChucVu, String moTa) {
        if (maChucVu == null || maChucVu.trim().isEmpty()) {
            throw new IllegalArgumentException("MÃƒÂ£ chÃ¡Â»Â©c vÃ¡Â»Â¥ khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (tenChucVu == null || tenChucVu.trim().isEmpty()) {
            throw new IllegalArgumentException("TÃƒÂªn chÃ¡Â»Â©c vÃ¡Â»Â¥ khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (positionRepo.existsById(maChucVu.trim())) {
            throw new IllegalArgumentException("MÃƒÂ£ chÃ¡Â»Â©c vÃ¡Â»Â¥ '" + maChucVu.trim() + "' Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i.");
        }
        if (heSoLuong <= 0) {
            throw new IllegalArgumentException("HÃ¡Â»â€¡ sÃ¡Â»â€˜ lÃ†Â°Ã†Â¡ng phÃ¡ÂºÂ£i lÃ¡Â»â€ºn hÃ†Â¡n 0.");
        }
        if (phuCapChucVu < 0) {
            throw new IllegalArgumentException("PhÃ¡Â»Â¥ cÃ¡ÂºÂ¥p khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c ÃƒÂ¢m.");
        }

        ChucVu pos = new ChucVu(
                maChucVu.trim(),
                tenChucVu.trim(),
                capBac,
                heSoLuong,
                phuCapChucVu,
                moTa,
                "hoat_dong"
        );
        positionRepo.save(pos);
    }

    /**
     * CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ´ng tin chÃ¡Â»Â©c vÃ¡Â»Â¥.
     * TÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng ghi lÃ¡Â»â€¹ch sÃ¡Â»Â­ nÃ¡ÂºÂ¿u hÃ¡Â»â€¡ sÃ¡Â»â€˜ lÃ†Â°Ã†Â¡ng hoÃ¡ÂºÂ·c phÃ¡Â»Â¥ cÃ¡ÂºÂ¥p thay Ã„â€˜Ã¡Â»â€¢i.
     *
     * @param maChucVu  mÃƒÂ£ chÃ¡Â»Â©c vÃ¡Â»Â¥ cÃ¡ÂºÂ§n cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t
     * @param tenMoi    tÃƒÂªn chÃ¡Â»Â©c vÃ¡Â»Â¥ mÃ¡Â»â€ºi
     * @param capBacMoi cÃ¡ÂºÂ¥p bÃ¡ÂºÂ­c mÃ¡Â»â€ºi
     * @param heSoMoi   hÃ¡Â»â€¡ sÃ¡Â»â€˜ lÃ†Â°Ã†Â¡ng mÃ¡Â»â€ºi
     * @param phuCapMoi phÃ¡Â»Â¥ cÃ¡ÂºÂ¥p mÃ¡Â»â€ºi
     * @param moTaMoi   mÃƒÂ´ tÃ¡ÂºÂ£ mÃ¡Â»â€ºi
     * @throws IllegalArgumentException nÃ¡ÂºÂ¿u dÃ¡Â»Â¯ liÃ¡Â»â€¡u khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡
     */
    public void updatePosition(String maChucVu, String tenMoi, int capBacMoi,
                               double heSoMoi, double phuCapMoi, String moTaMoi) {
        ChucVu pos = positionRepo.findById(maChucVu);
        if (pos == null) {
            throw new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y chÃ¡Â»Â©c vÃ¡Â»Â¥.");
        }
        if (tenMoi == null || tenMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("TÃƒÂªn chÃ¡Â»Â©c vÃ¡Â»Â¥ khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (heSoMoi <= 0) {
            throw new IllegalArgumentException("HÃ¡Â»â€¡ sÃ¡Â»â€˜ lÃ†Â°Ã†Â¡ng phÃ¡ÂºÂ£i lÃ¡Â»â€ºn hÃ†Â¡n 0.");
        }
        if (phuCapMoi < 0) {
            throw new IllegalArgumentException("PhÃ¡Â»Â¥ cÃ¡ÂºÂ¥p khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c ÃƒÂ¢m.");
        }

        boolean heSoThayDoi = Double.compare(pos.getHeSoLuong(), heSoMoi) != 0;
        boolean phuCapThayDoi = Double.compare(pos.getPhuCapChucVu(), phuCapMoi) != 0;

        if (heSoThayDoi || phuCapThayDoi) {
            String ngayHom = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // LÃ¡ÂºÂ¥y tÃƒÂªn ngÃ†Â°Ã¡Â»Âi thÃ¡Â»Â±c hiÃ¡Â»â€¡n tÃ¡Â»Â« session nÃ¡ÂºÂ¿u cÃƒÂ³
            String nguoiThayDoi = "Admin";
            if (SessionContext.getInstance().isLoggedIn()
                    && SessionContext.getInstance().getCurrentUser() != null) {
                String fullName = SessionContext.getInstance().getCurrentUser().getHoTen();
                if (fullName != null && !fullName.isEmpty()) {
                    nguoiThayDoi = fullName;
                } else {
                    nguoiThayDoi = SessionContext.getInstance().getCurrentUser().getTenDangNhap();
                }
            }

            LichSuHeSoLuong history = new LichSuHeSoLuong(
                    historyRepo.generateId(),
                    maChucVu,
                    pos.getHeSoLuong(),
                    heSoMoi,
                    pos.getPhuCapChucVu(),
                    phuCapMoi,
                    ngayHom,
                    nguoiThayDoi
            );
            historyRepo.save(history);
        }

        pos.setTenChucVu(tenMoi.trim());
        pos.setCapBac(capBacMoi);
        pos.setHeSoLuong(heSoMoi);
        pos.setPhuCapChucVu(phuCapMoi);
        pos.setMoTa(moTaMoi);
        positionRepo.update(pos);
    }

    /**
     * NgÃ†Â°ng hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng chÃ¡Â»Â©c vÃ¡Â»Â¥.
     *
     * @param maChucVu mÃƒÂ£ chÃ¡Â»Â©c vÃ¡Â»Â¥ cÃ¡ÂºÂ§n ngÃ†Â°ng
     * @throws IllegalArgumentException nÃ¡ÂºÂ¿u khÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y chÃ¡Â»Â©c vÃ¡Â»Â¥
     */
    public void deactivatePosition(String maChucVu) {
        ChucVu pos = positionRepo.findById(maChucVu);
        if (pos == null) {
            throw new IllegalArgumentException("Khong tim thay chuc vu.");
        }
        pos.setTrangThai("ngung");
        positionRepo.update(pos);
    }

    /**
     * Kich hoat lai chuc vu da ngung.
     */
    public void activatePosition(String maChucVu) {
        ChucVu pos = positionRepo.findById(maChucVu);
        if (pos == null) {
            throw new IllegalArgumentException("Khong tim thay chuc vu.");
        }
        pos.setTrangThai("hoat_dong");
        positionRepo.update(pos);
    }
}