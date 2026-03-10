package com.hrm.service;

import com.hrm.model.ChiTietLuong;
import com.hrm.repo.ChiTietLuongRepository;
import java.util.List;

public class ChiTietLuongService {
    private ChiTietLuongRepository repo;

    public ChiTietLuongService() {
        this.repo = new ChiTietLuongRepository();
    }

    public List<String[]> getDanhSachKyLuong() {
        return repo.getDanhSachKyLuong();
    }

    public List<ChiTietLuong> getSalaryDetailsByBangLuong(int maBangLuong) {
        return repo.getSalaryDetailsByBangLuong(maBangLuong);
    }

    public boolean tinhLuong(int maBangLuong) {
        return repo.tinhLuong(maBangLuong);
    }

    public boolean khoaBangLuong(int maBangLuong) {
        return repo.khoaBangLuong(maBangLuong);
    }
}