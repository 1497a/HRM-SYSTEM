import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class FixAdmin {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/hrm_db?useUnicode=true&characterEncoding=UTF-8";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                // Ignore if exists
                stmt.executeUpdate("INSERT IGNORE INTO NHANVIEN (maNV, ngayVaoLam, loaiHopDong, trangThai, ghiChu) VALUES ('admin', '2020-01-01', 'khong_xac_dinh', 'dang_lam_viec', 'Tai khoan he thong')");
                stmt.executeUpdate("INSERT IGNORE INTO THONGTINCANHAN (maNV, hoTen, gioiTinh) VALUES ('admin', 'Quản trị viên', 'khac')");
                stmt.executeUpdate("UPDATE TAIKHOAN SET maNV = 'admin' WHERE tenDangNhap = 'admin'");
                conn.commit();
                System.out.println("FIX_ADMIN_SUCCESS");
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
