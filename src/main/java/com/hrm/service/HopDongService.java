package com.hrm.service;

import com.hrm.model.HopDong;
import com.hrm.repo.HopDongRepository;
import java.util.List;

public class HopDongService {
    private HopDongRepository repo;

    public HopDongService() {
        this.repo = new HopDongRepository();
    }

    public List<HopDong> getAllHopDong() {
        return repo.getAllHopDong();
    }

    // Hàm kiểm tra và ép kiểu trước khi lưu
    public boolean addHopDong(String soHopDong, String maNhanVien, String loaiHienThi, String luongText, String ngayText) throws Exception {
        double luong = Double.parseDouble(luongText);
        java.sql.Date ngayHieuLuc = java.sql.Date.valueOf(ngayText);
        
        String loaiEnum = "thu_viec";
        if (loaiHienThi.equals("Xác định thời hạn")) loaiEnum = "xac_dinh_thoi_han";
        else if (loaiHienThi.equals("Không xác định")) loaiEnum = "khong_xac_dinh";
        
        return repo.insertHopDong(soHopDong, maNhanVien, loaiEnum, luong, ngayHieuLuc);
    }
}