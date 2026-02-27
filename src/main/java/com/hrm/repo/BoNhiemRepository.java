package com.hrm.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.hrm.model.BoNhiem;


public class BoNhiemRepository {

    private static volatile BoNhiemRepository instance;

    private final Map<Integer, BoNhiem> danhSachBoNhiem = new LinkedHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private BoNhiemRepository() {
        initializeMockData();
    }

    public static BoNhiemRepository getInstance() {
        if (instance == null) {
            synchronized (BoNhiemRepository.class) {
                if (instance == null) {
                    instance = new BoNhiemRepository();
                }
            }
        }
        return instance;
    }

    //  MOCK DATA  
    private void initializeMockData() {
        LocalDate now = LocalDate.now();

        // Bổ nhiệm chính hiện tại
        save(createMock(1, "PBNS", "CVTP", BoNhiem.LoaiBoNhiem.CHINH, 100.0,
                now.minusYears(1), null, BoNhiem.TrangThai.HIEU_LUC));

        // Bổ nhiệm cũ đã hết hiệu lực
        save(createMock(1, "PBKD", "CVNV", BoNhiem.LoaiBoNhiem.CHINH, 100.0,
                now.minusYears(2), now.minusMonths(13), BoNhiem.TrangThai.HET_HIEU_LUC));

        // Bổ nhiệm chờ duyệt
        save(createMock(2, "PBKT", "CVPP", BoNhiem.LoaiBoNhiem.KIEM_NHIEM, 50.0,
                now.plusDays(10), null, BoNhiem.TrangThai.CHO_DUYET));

        // Một bổ nhiệm bị từ chối
        save(createMock(3, "PBNS", "CVTT", BoNhiem.LoaiBoNhiem.CHINH, 100.0,
                now.minusMonths(1), now.minusDays(5), BoNhiem.TrangThai.TU_CHOI));
    }

    private BoNhiem createMock(int maNV, String phongBan, String chucVu,
                           BoNhiem.LoaiBoNhiem loai, double tyLe,
                           LocalDate tuNgay, LocalDate denNgay,
                           BoNhiem.TrangThai trangThai) {
    BoNhiem bn = new BoNhiem();
    bn.setMaBoNhiem(idGenerator.getAndIncrement());
    bn.setMaNV(maNV);
    bn.setMaPhongBan(phongBan);
    bn.setMaChucVu(chucVu);
    bn.setLoaiBoNhiem(loai);
    bn.setTyLeHuongLuong(tyLe);
    bn.setTuNgay(tuNgay);
    bn.setDenNgay(denNgay);
    bn.setTrangThai(trangThai);
    bn.setNgayTao(LocalDateTime.now());
    bn.setNgayCapNhat(LocalDateTime.now());

    
    bn.setNguoiDuyet(1);                       // Mã người duyệt (giả sử admin hoặc HR mã 1)
    bn.setNgayPheDuyet(LocalDateTime.now().minusDays(5));  // Ngày duyệt cách đây 5 ngày
    bn.setLyDo("Bổ nhiệm do thăng chức");      // Lý do mẫu


    //  nếu là CHO_DUYET thì không set nguoiDuyet và ngayPheDuyet
    if (trangThai == BoNhiem.TrangThai.CHO_DUYET) {
        bn.setNguoiDuyet(null);
        bn.setNgayPheDuyet(null);
        bn.setLyDo("Yêu cầu bổ nhiệm vị trí mới");
    }

    return bn;
}

    //  CRUD 

    /**
     * Lưu hoặc cập nhật BoNhiem. Tạo mới nếu maBoNhiem = 0.
     */
    public BoNhiem save(BoNhiem boNhiem) {
        if (boNhiem == null) throw new IllegalArgumentException("BoNhiem không được null");

        BoNhiem copy = copyBoNhiem(boNhiem); // deep copy

        if (copy.getMaBoNhiem() == 0) {
            copy.setMaBoNhiem(idGenerator.getAndIncrement());
        }

        danhSachBoNhiem.put(copy.getMaBoNhiem(), copy);
        return copy;
    }

    public BoNhiem findById(int id) {
        BoNhiem found = danhSachBoNhiem.get(id);
        return found != null ? copyBoNhiem(found) : null;
    }

    public List<BoNhiem> findAllBoNhiem() {
        return danhSachBoNhiem.values().stream()
                .map(this::copyBoNhiem)
                .sorted(Comparator.comparing(BoNhiem::getTuNgay).reversed())
                .collect(Collectors.toList());
    }

    public boolean delete(int id) {
        return danhSachBoNhiem.remove(id) != null;
    }

    //  FILTER 

    public List<BoNhiem> findByNhanVien(int maNV) {
        return danhSachBoNhiem.values().stream()
                .filter(b -> b.getMaNV() == maNV)
                .map(this::copyBoNhiem)
                .sorted(Comparator.comparing(BoNhiem::getTuNgay).reversed())
                .collect(Collectors.toList());
    }

    public List<BoNhiem> findByTrangThai(BoNhiem.TrangThai trangThai) {
    if (trangThai == null) return List.of();

    System.out.println("Filter by trạng thái: " + trangThai.getDisplayName() + " (dbValue: " + trangThai.getDbValue() + ")");

    return danhSachBoNhiem.values().stream()
            .filter(b -> {
                boolean match = b.getTrangThai() == trangThai;
                System.out.println("  - Record: " + b.getMaBoNhiem() + " | Trạng thái: " + b.getTrangThai().getDisplayName() + " → match? " + match);
                return match;
            })
            .map(this::copyBoNhiem)
            .sorted(Comparator.comparing(BoNhiem::getTuNgay).reversed())
            .collect(Collectors.toList());
}

    public List<BoNhiem> findDangHieuLuc() {
        LocalDate today = LocalDate.now();
        return danhSachBoNhiem.values().stream()
                .filter(b -> b.getTrangThai() == BoNhiem.TrangThai.HIEU_LUC)
                .filter(b -> b.getDenNgay() == null || !b.getDenNgay().isBefore(today))
                .map(this::copyBoNhiem)
                .sorted(Comparator.comparing(BoNhiem::getTuNgay).reversed())
                .collect(Collectors.toList());
    }

    public BoNhiem findCurrentByNhanVien(int maNV) {
        LocalDate today = LocalDate.now();
        return danhSachBoNhiem.values().stream()
                .filter(b -> b.getMaNV() == maNV)
                .filter(b -> b.getTrangThai() == BoNhiem.TrangThai.HIEU_LUC)
                .filter(b -> b.getDenNgay() == null || !b.getDenNgay().isBefore(today))
                .map(this::copyBoNhiem)
                .max(Comparator.comparing(BoNhiem::getTuNgay)) // lấy cái mới nhất
                .orElse(null);
    }

    public List<BoNhiem> findOverlapping(int maNV, LocalDate start, LocalDate end) {
        if (start == null) return List.of();
        LocalDate finalEnd = (end != null) ? end : LocalDate.MAX;

        return danhSachBoNhiem.values().stream()
                .filter(b -> b.getMaNV() == maNV)
                .filter(b -> b.getTrangThai() == BoNhiem.TrangThai.HIEU_LUC)
                .filter(b -> !b.getTuNgay().isAfter(finalEnd))
                .filter(b -> b.getDenNgay() == null || !b.getDenNgay().isBefore(start))
                .map(this::copyBoNhiem)
                .collect(Collectors.toList());
    }

    //  HELPER 

    private BoNhiem copyBoNhiem(BoNhiem original) {
        if (original == null) return null;
        BoNhiem copy = new BoNhiem();
        copy.setMaBoNhiem(original.getMaBoNhiem());
        copy.setMaNV(original.getMaNV());
        copy.setMaPhongBan(original.getMaPhongBan());
        copy.setMaChucVu(original.getMaChucVu());
        copy.setLoaiBoNhiem(original.getLoaiBoNhiem());
        copy.setTyLeHuongLuong(original.getTyLeHuongLuong());
        copy.setMaQuanLy(original.getMaQuanLy());
        copy.setNguoiDuyet(original.getNguoiDuyet());
        copy.setTuNgay(original.getTuNgay());
        copy.setDenNgay(original.getDenNgay());
        copy.setNgayPheDuyet(original.getNgayPheDuyet());
        copy.setLyDo(original.getLyDo());
        copy.setTrangThai(original.getTrangThai());
        copy.setNgayTao(original.getNgayTao());
        copy.setNgayCapNhat(original.getNgayCapNhat());
        return copy;
    }
}