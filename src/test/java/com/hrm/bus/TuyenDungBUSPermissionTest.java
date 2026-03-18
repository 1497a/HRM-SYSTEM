package com.hrm.bus;

import com.hrm.model.Quyen;
import com.hrm.model.TaiKhoan;
import com.hrm.model.UngVien;
import com.hrm.model.VaiTro;
import com.hrm.model.YeuCauTuyenDung;
import com.hrm.util.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TuyenDungBUSPermissionTest {

    @AfterEach
    void tearDown() {
        SessionContext.getInstance().clearSession();
    }

    @Test
    @DisplayName("hasAnyPermission: nhan su tuyen dung co quyen manage")
    void hasAnyPermission_manageRole_returnTrue() {
        TaiKhoan user = createUserWithPermissions(TuyenDungBUS.ACTION_RECRUITMENT_MANAGE);

        assertTrue(TuyenDungBUS.hasAnyPermission(user,
                TuyenDungBUS.ACTION_RECRUITMENT_REQUEST,
                TuyenDungBUS.ACTION_RECRUITMENT_MANAGE));
    }

    @Test
    @DisplayName("hasAnyPermission: nhan vien thuong khong co quyen tuyen dung")
    void hasAnyPermission_noPermission_returnFalse() {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("tri.hoang");

        assertFalse(TuyenDungBUS.hasAnyPermission(user,
                TuyenDungBUS.ACTION_RECRUITMENT_REQUEST,
                TuyenDungBUS.ACTION_RECRUITMENT_MANAGE));
    }

    @Test
    @DisplayName("taoYeuCau: chan khi nguoi dung khong co quyen request hoac manage")
    void taoYeuCau_withoutPermission_blockedBeforeDao() {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("tri.hoang");
        SessionContext.getInstance().setCurrentUser(user);

        YeuCauTuyenDung yc = new YeuCauTuyenDung();
        yc.setId("PHONGIT");
        yc.setMaChucVu("NV");
        yc.setSoLuong(1);
        yc.setHanTuyenDung(LocalDate.now().plusDays(7));

        KetQua<YeuCauTuyenDung> result = TuyenDungBUS.getInstance().taoYeuCau(yc);

        assertFalse(result.isSuccess());
        assertEquals("Ban khong co quyen tao yeu cau tuyen dung.", result.getMessage());
    }

    @Test
    @DisplayName("tiepNhanUngVien: chan khi nguoi dung khong co quyen manage")
    void tiepNhanUngVien_withoutPermission_blockedBeforeDao() {
        TaiKhoan user = createUserWithPermissions(TuyenDungBUS.ACTION_RECRUITMENT_REQUEST);
        SessionContext.getInstance().setCurrentUser(user);

        UngVien uv = new UngVien();
        uv.setHoTen("Ung vien test");
        uv.setEmail("uv@example.com");
        uv.setMaTin(1);

        KetQua<UngVien> result = TuyenDungBUS.getInstance().tiepNhanUngVien(uv);

        assertFalse(result.isSuccess());
        assertEquals("Ban khong co quyen tiep nhan ung vien.", result.getMessage());
    }

    private TaiKhoan createUserWithPermissions(String... permissions) {
        TaiKhoan user = new TaiKhoan();
        user.setTenDangNhap("tester");
        VaiTro role = new VaiTro("TEST_ROLE", "Test role", null);
        for (String permissionId : permissions) {
            role.themQuyen(new Quyen(permissionId, permissionId, "TEST"));
        }
        user.themVaiTro(role);
        return user;
    }
}
