package com.hrm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.hrm.model.BoNhiem;
import com.hrm.repo.BoNhiemRepository;


public class BoNhiemService {

    private final BoNhiemRepository repository;

    public BoNhiemService() {
        this.repository = BoNhiemRepository.getInstance();
    }

    
    public static class ServiceResult<T> {
        private final boolean success;
        private final String message;
        private final T data;

        public ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }

        public static <T> ServiceResult<T> success(String msg, T data) {
            return new ServiceResult<>(true, msg, data);
        }

        public static <T> ServiceResult<T> error(String msg) {
            return new ServiceResult<>(false, msg, null);
        }
    }

   
    public ServiceResult<BoNhiem> taoYeuCau(BoNhiem bn) {
        try {
            validate(bn);

            bn.setTrangThai(BoNhiem.TrangThai.CHO_DUYET);
            bn.setNgayTao(LocalDateTime.now());
            bn.setNgayCapNhat(LocalDateTime.now());

            BoNhiem saved = repository.save(bn);

            return ServiceResult.success("Tạo yêu cầu bổ nhiệm thành công", copy(bn));

        } catch (Exception e) {
            return ServiceResult.error("Lỗi khi tạo yêu cầu: " + e.getMessage());
        }
    }

   
    public ServiceResult<BoNhiem> pheDuyet(int maBoNhiem, int nguoiDuyet) {
        try {
            BoNhiem bn = repository.findById(maBoNhiem);
            if (bn == null) {
                return ServiceResult.error("Không tìm thấy quyết định bổ nhiệm");
            }

            if (bn.getTrangThai() != BoNhiem.TrangThai.CHO_DUYET) {
                return ServiceResult.error("Chỉ được duyệt khi đang CHỜ DUYỆT");
            }

            
            if (bn.getLoaiBoNhiem() == BoNhiem.LoaiBoNhiem.CHINH) {
                BoNhiem current = repository.findCurrentByNhanVien(bn.getMaNV());
                if (current != null && current.getMaBoNhiem() != maBoNhiem) {
                    current.setDenNgay(bn.getTuNgay().minusDays(1));
                    current.setTrangThai(BoNhiem.TrangThai.HET_HIEU_LUC);
                    current.setNgayCapNhat(LocalDateTime.now());
                    repository.save(current);
                }
            }

           
            bn.setTrangThai(BoNhiem.TrangThai.HIEU_LUC);
            bn.setNguoiDuyet(nguoiDuyet);
            bn.setNgayPheDuyet(LocalDateTime.now());
            bn.setNgayCapNhat(LocalDateTime.now());

            repository.save(bn);

            return ServiceResult.success("Duyệt bổ nhiệm thành công", copy(bn));

        } catch (Exception e) {
            return ServiceResult.error("Lỗi khi duyệt: " + e.getMessage());
        }
    }

    
    public ServiceResult<BoNhiem> tuChoi(int maBoNhiem, int nguoiDuyet, String lyDoTuChoi) {
        try {
            BoNhiem bn = repository.findById(maBoNhiem);
            if (bn == null) {
                return ServiceResult.error("Không tìm thấy quyết định bổ nhiệm");
            }

            if (bn.getTrangThai() != BoNhiem.TrangThai.CHO_DUYET) {
                return ServiceResult.error("Chỉ được từ chối khi đang CHỜ DUYỆT");
            }

            String finalLyDo = (bn.getLyDo() != null && !bn.getLyDo().isBlank())
                    ? bn.getLyDo() + " | Từ chối: " + lyDoTuChoi
                    : "Từ chối: " + lyDoTuChoi;

            bn.setTrangThai(BoNhiem.TrangThai.TU_CHOI);
            bn.setNguoiDuyet(nguoiDuyet);
            bn.setLyDo(finalLyDo);
            bn.setNgayCapNhat(LocalDateTime.now());

            repository.save(bn);

            return ServiceResult.success("Từ chối bổ nhiệm thành công", copy(bn));

        } catch (Exception e) {
            return ServiceResult.error("Lỗi khi từ chối: " + e.getMessage());
        }
    }

    
    public ServiceResult<BoNhiem> ketThuc(int maBoNhiem, LocalDate denNgay) {
        try {
            BoNhiem bn = repository.findById(maBoNhiem);
            if (bn == null) {
                return ServiceResult.error("Không tìm thấy bổ nhiệm");
            }

            if (bn.getTrangThai() != BoNhiem.TrangThai.HIEU_LUC) {
                return ServiceResult.error("Chỉ được kết thúc khi đang HIỆU LỰC");
            }

            if (denNgay == null || denNgay.isBefore(bn.getTuNgay())) {
                return ServiceResult.error("Ngày kết thúc không hợp lệ");
            }

            bn.setDenNgay(denNgay);
            bn.setTrangThai(BoNhiem.TrangThai.HET_HIEU_LUC);
            bn.setNgayCapNhat(LocalDateTime.now());

            repository.save(bn);

            return ServiceResult.success("Kết thúc bổ nhiệm thành công", copy(bn));

        } catch (Exception e) {
            return ServiceResult.error("Lỗi khi kết thúc: " + e.getMessage());
        }
    }

   
    public List<BoNhiem> findAll() {
        return repository.findAllBoNhiem();
    }

    public List<BoNhiem> findByNhanVien(int maNV) {
        return repository.findByNhanVien(maNV);
    }

    public List<BoNhiem> findDangHieuLuc() {
        return repository.findDangHieuLuc();
    }

    public List<BoNhiem> findChoDuyet() {
        return repository.findByTrangThai(BoNhiem.TrangThai.CHO_DUYET);
    }

   
    private void validate(BoNhiem bn) {
        if (bn.getMaNV() <= 0) {
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        }
        if (bn.getMaPhongBan() == null || bn.getMaPhongBan().isBlank()) {
            throw new IllegalArgumentException("Phòng ban không được để trống");
        }
        if (bn.getMaChucVu() == null || bn.getMaChucVu().isBlank()) {
            throw new IllegalArgumentException("Chức vụ không được để trống");
        }
        if (bn.getTuNgay() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống");
        }
        if (bn.getDenNgay() != null && bn.getDenNgay().isBefore(bn.getTuNgay())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
        if (bn.getTyLeHuongLuong() < 0 || bn.getTyLeHuongLuong() > 100) {
            throw new IllegalArgumentException("Tỷ lệ hưởng lương phải từ 0 - 100%");
        }
        if (bn.getLoaiBoNhiem() == null) {
            throw new IllegalArgumentException("Loại bổ nhiệm không hợp lệ");
        }
    }

    
    private BoNhiem copy(BoNhiem original) {
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