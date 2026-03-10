package com.hrm.service;

import com.hrm.model.ChiTietLuong;
import com.hrm.repo.ChiTietLuongRepository;
import java.util.List;

public class ChiTietLuongService {
    private ChiTietLuongRepository repo;

    public ChiTietLuongService() {
        this.repo = new ChiTietLuongRepository();
    }

    public List<ChiTietLuong> getAllSalaryDetails() {
        return repo.getAllSalaryDetails();
    }
}