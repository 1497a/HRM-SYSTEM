package com.hrm.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public final class DaoHelper {
    private DaoHelper() {
    }

    public static Set<String> getDeptSubtree(String currentMaNV, Connection conn) throws SQLException {
        String rootSql = "SELECT b.maPhongBan FROM BONHIEM b "
                + "WHERE b.maNV = ? AND b.trangThai = 'hieu_luc' AND b.loaiBoNhiem = 'chinh' LIMIT 1";
        String rootDept = null;
        try (PreparedStatement ps = conn.prepareStatement(rootSql)) {
            ps.setString(1, currentMaNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rootDept = rs.getString(1);
                }
            }
        }
        Set<String> depts = new LinkedHashSet<>();
        if (rootDept == null) {
            return depts;
        }
        Queue<String> queue = new LinkedList<>();
        queue.add(rootDept);
        String childSql = "SELECT maPhongBan FROM PHONGBAN WHERE phongBanCha = ?";
        while (!queue.isEmpty()) {
            String currentDept = queue.poll();
            if (depts.add(currentDept)) {
                try (PreparedStatement ps = conn.prepareStatement(childSql)) {
                    ps.setString(1, currentDept);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            queue.add(rs.getString(1));
                        }
                    }
                }
            }
        }
        return depts;
    }
}
