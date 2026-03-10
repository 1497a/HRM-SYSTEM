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

    public boolean addHopDong(String soHopDong, String maNhanVien, String loaiHienThi, String luongText, String ngayText) throws Exception {
        double luong = Double.parseDouble(luongText);
        java.sql.Date ngayHieuLuc = java.sql.Date.valueOf(ngayText);
        
        String loaiEnum = "thu_viec";
        if (loaiHienThi.equals("Xác định thời hạn") || loaiHienThi.equals("xac_dinh_thoi_han")) loaiEnum = "xac_dinh_thoi_han";
        else if (loaiHienThi.equals("Không xác định") || loaiHienThi.equals("khong_xac_dinh")) loaiEnum = "khong_xac_dinh";
        
        return repo.insertHopDong(soHopDong, maNhanVien, loaiEnum, luong, ngayHieuLuc);
    }

    // HÀM XỬ LÝ SỬA
    public boolean updateHopDong(String maHopDong, String loaiHienThi, String luongText, String ngayText, String trangThaiHienThi) throws Exception {
        // Lọc bỏ chữ VNĐ và dấu phẩy để lấy số tiền gốc
        String cleanLuong = luongText.replaceAll("[^\\d.]", "");
        double luong = Double.parseDouble(cleanLuong);
        java.sql.Date ngayHieuLuc = java.sql.Date.valueOf(ngayText);
        
        String loaiEnum = "thu_viec";
        if (loaiHienThi.equals("Xác định thời hạn") || loaiHienThi.equals("xac_dinh_thoi_han")) loaiEnum = "xac_dinh_thoi_han";
        else if (loaiHienThi.equals("Không xác định") || loaiHienThi.equals("khong_xac_dinh")) loaiEnum = "khong_xac_dinh";

        String trangThaiEnum = "hieu_luc";
        if (trangThaiHienThi.equals("Hết hạn") || trangThaiHienThi.equals("het_han")) trangThaiEnum = "het_han";
        else if (trangThaiHienThi.equals("Thanh lý") || trangThaiHienThi.equals("thanh_ly")) trangThaiEnum = "thanh_ly";
        else if (trangThaiHienThi.equals("Hủy") || trangThaiHienThi.equals("huy")) trangThaiEnum = "huy";

        return repo.updateHopDong(maHopDong, loaiEnum, luong, ngayHieuLuc, trangThaiEnum);
    }

    // HÀM XỬ LÝ XÓA
    public boolean deleteHopDong(String maHopDong) {
        return repo.deleteHopDong(maHopDong);
    }
}