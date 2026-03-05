package com.hrm.bus;

import com.hrm.dao.BoNhiemDAO;
import com.hrm.dao.PhongBanDAO;
import com.hrm.model.PhongBan;

import java.util.List;

/**
 * Service quan ly phong ban.
 */
public class PhongBanBUS {

    private final PhongBanDAO repository = new PhongBanDAO();
    private final BoNhiemDAO boNhiemRepo = BoNhiemDAO.getInstance();

    public List<PhongBan> getAllDepartments() {
        return repository.findAll();
    }

    public List<PhongBan> getActiveDepartments() {
        return repository.findActive();
    }

    public PhongBan getById(String maPhongBan) {
        return repository.findById(maPhongBan);
    }

    public void addDepartment(String maPhongBan, String tenPhongBan, String phongBanCha) {
        if (maPhongBan == null || maPhongBan.trim().isEmpty()) {
            throw new IllegalArgumentException("\u004d\u00e3\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u006b\u0068\u00f4\u006e\u0067\u0020\u0111\u01b0\u1ee3\u0063\u0020\u0111\u1ec3\u0020\u0074\u0072\u1ed1\u006e\u0067\u002e");
        }
        if (tenPhongBan == null || tenPhongBan.trim().isEmpty()) {
            throw new IllegalArgumentException("\u0054\u00ea\u006e\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u006b\u0068\u00f4\u006e\u0067\u0020\u0111\u01b0\u1ee3\u0063\u0020\u0111\u1ec3\u0020\u0074\u0072\u1ed1\u006e\u0067\u002e");
        }
        if (repository.existsById(maPhongBan.trim())) {
            throw new IllegalArgumentException("\u004d\u00e3\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0027" + maPhongBan.trim() + "\u0027\u0020\u0111\u00e3\u0020\u0074\u1ed3\u006e\u0020\u0074\u1ea1\u0069\u0020\u0074\u0072\u006f\u006e\u0067\u0020\u0068\u1ec7\u0020\u0074\u0068\u1ed1\u006e\u0067\u002e");
        }

        if (phongBanCha != null && !phongBanCha.trim().isEmpty()) {
            PhongBan cha = repository.findById(phongBanCha.trim());
            if (cha == null) {
                throw new IllegalArgumentException("\u0050\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u0068\u0061\u0020\u006b\u0068\u00f4\u006e\u0067\u0020\u0074\u1ed3\u006e\u0020\u0074\u1ea1\u0069\u002e");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                throw new IllegalArgumentException("\u0050\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u0068\u0061\u0020\u0027" + cha.getTenPhongBan() + "\u0027\u0020\u0111\u00e3\u0020\u006e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067\u002e");
            }
        }

        String maCha = (phongBanCha != null && !phongBanCha.trim().isEmpty()) ? phongBanCha.trim() : null;
        PhongBan dept = new PhongBan(maPhongBan.trim(), tenPhongBan.trim(), maCha, "hoatDong");
        repository.save(dept);
    }

    public void updateDepartment(String maPhongBan, String tenMoi, String phongBanChaMoi) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            throw new IllegalArgumentException("\u004b\u0068\u00f4\u006e\u0067\u0020\u0074\u00ec\u006d\u0020\u0074\u0068\u1ea5\u0079\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u002e");
        }
        if (tenMoi == null || tenMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("\u0054\u00ea\u006e\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u006b\u0068\u00f4\u006e\u0067\u0020\u0111\u01b0\u1ee3\u0063\u0020\u0111\u1ec3\u0020\u0074\u0072\u1ed1\u006e\u0067\u002e");
        }

        if (phongBanChaMoi != null && !phongBanChaMoi.trim().isEmpty()) {
            PhongBan cha = repository.findById(phongBanChaMoi.trim());
            if (cha == null) {
                throw new IllegalArgumentException("\u0050\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u0068\u0061\u0020\u006b\u0068\u00f4\u006e\u0067\u0020\u0074\u1ed3\u006e\u0020\u0074\u1ea1\u0069\u002e");
            }
            if (!isActiveStatus(cha.getTrangThai())) {
                throw new IllegalArgumentException("\u0050\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u0068\u0061\u0020\u0027" + cha.getTenPhongBan() + "\u0027\u0020\u0111\u00e3\u0020\u006e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067\u002e");
            }
            if (isDescendant(maPhongBan, phongBanChaMoi.trim())) {
                throw new IllegalArgumentException("\u004b\u0068\u00f4\u006e\u0067\u0020\u0074\u0068\u1ec3\u0020\u0063\u0068\u1ecdn\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u006f\u006e\u002f\u0063\u0068\u00e1\u0075\u0020\u006c\u00e0\u006d\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u0068\u0061\u002e\u0020\u0053\u1ebd\u0020\u0074\u1ea1\u006f\u0020\u0076\u00f2\u006e\u0067\u0020\u006c\u1eb7\u0070\u0020\u0074\u0072\u006f\u006e\u0067\u0020\u0063\u00e2\u0079\u0020\u0074\u1ed5\u0020\u0063\u0068\u1ee9\u0063\u002e");
            }
        }

        String maCha = (phongBanChaMoi != null && !phongBanChaMoi.trim().isEmpty()) ? phongBanChaMoi.trim() : null;
        dept.setTenPhongBan(tenMoi.trim());
        dept.setPhongBanChaId(maCha);
        repository.update(dept);
    }

    public void deactivateDepartment(String maPhongBan) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            throw new IllegalArgumentException("\u004b\u0068\u00f4\u006e\u0067\u0020\u0074\u00ec\u006d\u0020\u0074\u0068\u1ea5\u0079\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u002e");
        }

        List<PhongBan> danhSachCon = repository.findChildren(maPhongBan);
        for (PhongBan con : danhSachCon) {
            if (isActiveStatus(con.getTrangThai())) {
                throw new IllegalArgumentException("\u004b\u0068\u00f4\u006e\u0067\u0020\u0074\u0068\u1ec3\u0020\u006e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067\u002e\u0020\u0050\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0027" + con.getTenPhongBan() + "\u0027\u0020\u0076\u1eab\u006e\u0020\u0111\u0061\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067\u002e\u0020\u0056\u0075\u0069\u0020\u006c\u00f2\u006e\u0067\u0020\u006e\u0067\u1eeb\u006e\u0067\u0020\u0063\u00e1\u0063\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u006f\u006e\u0020\u0074\u0072\u01b0\u1edb\u0063\u002e");
            }
        }

        if (boNhiemRepo.hasActiveBoNhiemInDepartment(maPhongBan)) {
            throw new IllegalArgumentException("\u004b\u0068\u00f4\u006e\u0067\u0020\u0074\u0068\u1ec3\u0020\u006e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067\u002e\u0020\u0050\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0076\u1eab\u006e\u0020\u0063\u00f2\u006e\u0020\u0062\u1ed5\u0020\u006e\u0068\u0069\u1ec7\u006d\u0020\u0111\u0061\u006e\u0067\u0020\u0068\u0069\u1ec7\u0075\u0020\u006c\u1ef1\u0063\u002e\u0020\u0056\u0075\u0069\u0020\u006c\u00f2\u006e\u0067\u0020\u006b\u1ebf\u0074\u0020\u0074\u0068\u00fa\u0063\u0020\u0063\u00e1\u0063\u0020\u0062\u1ed5\u0020\u006e\u0068\u0069\u1ec7\u006d\u0020\u0074\u0072\u01b0\u1edb\u0063\u002e");
        }

        dept.setTrangThai("ngung_hoat_dong");
        repository.update(dept);
    }

    /**
     * Kich hoat lai phong ban da ngung hoat dong.
     */
    public void activateDepartment(String maPhongBan) {
        PhongBan dept = repository.findById(maPhongBan);
        if (dept == null) {
            throw new IllegalArgumentException("\u004b\u0068\u00f4\u006e\u0067\u0020\u0074\u00ec\u006d\u0020\u0074\u0068\u1ea5\u0079\u0020\u0070\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u002e");
        }

        String maCha = dept.getPhongBanChaId();
        if (maCha != null && !maCha.trim().isEmpty()) {
            PhongBan cha = repository.findById(maCha.trim());
            if (cha != null && !isActiveStatus(cha.getTrangThai())) {
                throw new IllegalArgumentException("\u004b\u0068\u00f4\u006e\u0067\u0020\u0074\u0068\u1ec3\u0020\u006b\u00ed\u0063\u0068\u0020\u0068\u006f\u1ea1\u0074\u002e\u0020\u0050\u0068\u00f2\u006e\u0067\u0020\u0062\u0061\u006e\u0020\u0063\u0068\u0061\u0020\u0027" + cha.getTenPhongBan() + "\u0027\u0020\u0111\u0061\u006e\u0067\u0020\u006e\u0067\u1eeb\u006e\u0067\u0020\u0068\u006f\u1ea1\u0074\u0020\u0111\u1ed9\u006e\u0067\u002e");
            }
        }

        dept.setTrangThai("hoatDong");
        repository.update(dept);
    }

    private boolean isActiveStatus(String status) {
        return "hoatdong".equals(normalizeStatus(status));
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        return status.toLowerCase().replace("_", "").replace(" ", "").replace("-", "");
    }

    private boolean isDescendant(String maCha, String maCon) {
        if (maCon == null) {
            return false;
        }
        if (maCon.equals(maCha)) {
            return true;
        }
        PhongBan con = repository.findById(maCon);
        if (con == null) {
            return false;
        }
        return isDescendant(maCha, con.getPhongBanChaId());
    }
}
