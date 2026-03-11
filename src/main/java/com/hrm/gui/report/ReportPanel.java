package com.hrm.gui.report;

import com.hrm.gui.components.RoundedPanel;
import com.hrm.model.BangLuong;
import com.hrm.model.PhongBan;
import com.hrm.model.NhanVien;
import com.hrm.dao.BangLuongDAO;
import com.hrm.dao.PhongBanDAO;
import com.hrm.dao.NghiPhepDAO;
import com.hrm.dao.NhanVienDAO;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Panel báo cáo nhân sự.
 * Tab 1: Tổng quan nhân sự (dashboard cards + bảng theo phòng ban).
 * Tab 2: Báo cáo nghỉ phép.
 * Tab 3: Báo cáo lương.
 */
public class ReportPanel extends JPanel {

    private final NhanVienDAO nvRepo;
    private final BangLuongDAO blRepo;
    private final PhongBanDAO deptRepo;

    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public ReportPanel() {
        this.nvRepo = NhanVienDAO.getInstance();
        this.blRepo = BangLuongDAO.getInstance();
        this.deptRepo = new PhongBanDAO();

        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        tabbedPane.setBackground(UIColors.WHITE);

        tabbedPane.addTab("Tổng quan nhân sự", buildTongQuanTab());
        tabbedPane.addTab("Báo cáo nghỉ phép", buildLeaveReportTab());
        tabbedPane.addTab("Báo cáo lương", buildSalaryReportTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =======================
    // Tab 1 - Tổng quan
    // =======================

    private JPanel buildTongQuanTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Load data
        List<NhanVien> allNV = Collections.emptyList();
        try {
            allNV = nvRepo.findAll();
        } catch (Exception ex) {
            // ignore, show 0s
        }

        int tongNV = allNV.size();
        long dangLam = allNV.stream().filter(nv -> "dang_lam_viec".equals(nv.getTrangThai())).count();
        long tamNghi = allNV.stream().filter(nv -> "tam_nghi".equals(nv.getTrangThai())).count();
        long nghiViec = allNV.stream().filter(nv -> "nghi_viec".equals(nv.getTrangThai())).count();

        // Stat cards row
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        cardsPanel.add(RoundedPanel.createStatCard("Tổng nhân viên", String.valueOf(tongNV), UIColors.PRIMARY_PURPLE));
        cardsPanel.add(RoundedPanel.createStatCard("Đang làm việc", String.valueOf(dangLam), UIColors.SUCCESS_GREEN));
        cardsPanel.add(RoundedPanel.createStatCard("Tạm nghỉ", String.valueOf(tamNghi), UIColors.WARNING_YELLOW));
        cardsPanel.add(RoundedPanel.createStatCard("Đã nghỉ việc", String.valueOf(nghiViec), UIColors.DANGER_RED));

        // Table - NV by department
        Map<String, Integer> countByDept = new LinkedHashMap<>();
        for (NhanVien nv : allNV) {
            String dept = nv.getTenPhongBanHienTai();
            if (dept == null || dept.isEmpty()) dept = "(Chưa phân công)";
            countByDept.merge(dept, 1, Integer::sum);
        }

        String[] cols = {"Phòng ban", "Số nhân viên"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Map.Entry<String, Integer> entry : countByDept.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }

        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Số nhân viên theo phòng ban"));

        // Refresh button — top-right
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        toolbar.setOpaque(false);
        JButton btnRefresh = UIHelper.createDefaultButton("🔄 Làm mới");
        btnRefresh.addActionListener(e -> {
            JTabbedPane tp = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, panel);
            if (tp != null) {
                int idx = tp.indexOfComponent(panel);
                tp.removeTabAt(idx);
                tp.insertTab("Tổng quan nhân sự", null, buildTongQuanTab(), null, idx);
                tp.setSelectedIndex(idx);
            }
        });
        toolbar.add(btnRefresh);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(cardsPanel, BorderLayout.CENTER);
        north.add(toolbar, BorderLayout.EAST);

        panel.add(north, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Tab 2 - Báo cáo nghỉ phép
    // =======================

    private JPanel buildLeaveReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Tách riêng cột "Nhân viên" và "Loại phép"
        String[] cols = {"Nhân viên", "Loại phép", "Được cấp", "Đã dùng", "Còn lại"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            NghiPhepDAO leaveRepo = NghiPhepDAO.getInstance();
            List<NhanVien> allNV = nvRepo.findAll();
            int currentYear = java.time.LocalDate.now().getYear();

            for (NhanVien nv : allNV) {
                try {
                    List<com.hrm.model.SoDungPhep> balances =
                            leaveRepo.findByMaNVAndNam(nv.getMaNhanVien(), currentYear);
                    if (balances.isEmpty()) {
                        model.addRow(new Object[]{
                                nv.getHoTen() != null ? nv.getHoTen() : nv.getMaNhanVien(),
                                "—", 0, 0, 0
                        });
                    } else {
                        for (com.hrm.model.SoDungPhep bal : balances) {
                            model.addRow(new Object[]{
                                    nv.getHoTen() != null ? nv.getHoTen() : nv.getMaNhanVien(),
                                    mapLeaveTypeCode(bal.getLeaveTypeCode()),
                                    bal.getTotalDays(),
                                    bal.getUsedDays(),
                                    bal.getRemainingDays()
                            });
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Lỗi tải dữ liệu: " + ex.getMessage(), "", "", "", ""});
        }

        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Báo cáo sử dụng phép nghỉ"));

        JPanel toolbar = buildRefreshToolbar(panel, "Báo cáo nghỉ phép", 1);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Tab 3 - Báo cáo lương
    // =======================

    private JPanel buildSalaryReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Bỏ cột "Tên bảng lương" vì trùng nội dung "Tháng/Năm"
        String[] cols = {"Tháng/Năm", "Trạng thái", "Tổng lương (thực nhận)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<BangLuong> bangLuongList = blRepo.findAll();
            for (BangLuong bl : bangLuongList) {
                String thangNam = bl.getThang() + "/" + bl.getNam();
                String trangThai = bl.getTrangThai() != null ? bl.getTrangThai().getDisplayName() : "";

                double tongLuong = 0;
                try {
                    var chiTietList = blRepo.findByBangLuong(bl.getMaBL());
                    tongLuong = chiTietList.stream()
                            .mapToDouble(ct -> ct.getLuongThucNhan())
                            .sum();
                } catch (Exception ignored) {}

                model.addRow(new Object[]{
                        thangNam,
                        trangThai,
                        formatMoney(tongLuong)
                });
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Lỗi tải dữ liệu: " + ex.getMessage(), "", ""});
        }

        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);

        // Right-align tổng lương
        javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Báo cáo tổng hợp bảng lương"));

        JPanel toolbar = buildRefreshToolbar(panel, "Báo cáo lương", 2);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Helpers
    // =======================

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(com.hrm.util.UIFonts.TEXT_NORMAL);
        table.getTableHeader().setFont(com.hrm.util.UIFonts.BOLD_NORMAL);
        table.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        table.getTableHeader().setForeground(java.awt.Color.WHITE); // Fix: trắng trên tím
        table.getTableHeader().setOpaque(true);
        table.setSelectionBackground(UIColors.LIGHT_PURPLE);
        table.setSelectionForeground(UIColors.TEXT_DARK);
        table.setGridColor(UIColors.BORDER_GRAY);
        return table;
    }

    /** Ánh xạ mã loại phép → tên hiển thị tiếng Việt */
    private String mapLeaveTypeCode(String code) {
        if (code == null) return "";
        switch (code) {
            case "PHEP_NAM":         return "Phép năm";
            case "PHEP_OM":          return "Nghỉ ốm";
            case "PHEP_CUOI":        return "Nghỉ cưới";
            case "PHEP_TANG":        return "Nghỉ tang";
            case "PHEP_THAI_SAN":    return "Thai sản";
            case "PHEP_KHONG_LUONG": return "Không lương";
            // Fallback cho code kiểu cũ
            case "AL": return "Phép năm";
            case "SL": return "Nghỉ ốm";
            case "ML": return "Thai sản";
            case "UL": return "Không lương";
            default: return code;
        }
    }

    private JPanel buildRefreshToolbar(JPanel targetPanel, String tabName, int tabIndex) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        toolbar.setOpaque(false);
        JButton btn = UIHelper.createDefaultButton("🔄 Làm mới");
        btn.addActionListener(e -> {
            JTabbedPane tp = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, targetPanel);
            if (tp != null) {
                JPanel newTab;
                switch (tabIndex) {
                    case 0: newTab = buildTongQuanTab(); break;
                    case 1: newTab = buildLeaveReportTab(); break;
                    case 2: newTab = buildSalaryReportTab(); break;
                    default: return;
                }
                tp.removeTabAt(tabIndex);
                tp.insertTab(tabName, null, newTab, null, tabIndex);
                tp.setSelectedIndex(tabIndex);
            }
        });
        toolbar.add(btn);
        return toolbar;
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format((long) amount) + " đ";
    }
}
