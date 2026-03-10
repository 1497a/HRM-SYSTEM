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
}   