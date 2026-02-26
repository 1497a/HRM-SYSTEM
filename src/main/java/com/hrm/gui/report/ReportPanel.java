package com.hrm.gui.report;

import com.hrm.gui.components.RoundedPanel;
import com.hrm.model.BangLuong;
import com.hrm.model.Department;
import com.hrm.model.NhanVien;
import com.hrm.repo.BangLuongRepository;
import com.hrm.repo.DepartmentRepository;
import com.hrm.repo.LeaveRepository;
import com.hrm.repo.NhanVienRepository;
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

    private final NhanVienRepository nvRepo;
    private final BangLuongRepository blRepo;
    private final DepartmentRepository deptRepo;

    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public ReportPanel() {
        this.nvRepo = NhanVienRepository.getInstance();
        this.blRepo = BangLuongRepository.getInstance();
        this.deptRepo = new DepartmentRepository();

        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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

        // Refresh button
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            // Rebuild tab by switching away and back
            JTabbedPane tp = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, panel);
            if (tp != null) {
                int idx = tp.indexOfComponent(panel);
                tp.removeTabAt(idx);
                tp.insertTab("Tổng quan nhân sự", null, buildTongQuanTab(), null, idx);
                tp.setSelectedIndex(idx);
            }
        });
        toolbar.add(btnRefresh);

        panel.add(cardsPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    // =======================
    // Tab 2 - Báo cáo nghỉ phép
    // =======================

    private JPanel buildLeaveReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Nhân viên", "Phép năm được cấp", "Đã dùng", "Còn lại"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            LeaveRepository leaveRepo = LeaveRepository.getInstance();
            List<NhanVien> allNV = nvRepo.findAll();
            int currentYear = java.time.LocalDate.now().getYear();

            for (NhanVien nv : allNV) {
                // Lấy số dư phép nghỉ năm hiện tại
                try {
                    List<com.hrm.model.LeaveBalance> balances =
                            leaveRepo.findByMaNVAndNam(nv.getId(), currentYear);
                    if (balances.isEmpty()) {
                        // Show row with zeros if no balance records
                        model.addRow(new Object[]{
                                nv.getHoTen() != null ? nv.getHoTen() : nv.getMaNhanVien(),
                                0, 0, 0
                        });
                    } else {
                        for (com.hrm.model.LeaveBalance bal : balances) {
                            model.addRow(new Object[]{
                                    (nv.getHoTen() != null ? nv.getHoTen() : nv.getMaNhanVien())
                                            + " (" + bal.getLeaveTypeCode() + ")",
                                    bal.getTotalDays(),
                                    bal.getUsedDays(),
                                    bal.getRemainingDays()
                            });
                        }
                    }
                } catch (Exception ignored) {
                    // Skip if no balance data for this employee
                }
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Lỗi tải dữ liệu: " + ex.getMessage(), "", "", ""});
        }

        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Báo cáo sử dụng phép nghỉ"));

        JPanel toolbar = buildRefreshToolbar(panel, "Báo cáo nghỉ phép");

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    // =======================
    // Tab 3 - Báo cáo lương
    // =======================

    private JPanel buildSalaryReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Tháng/Năm", "Tên bảng lương", "Trạng thái", "Tổng lương"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<BangLuong> bangLuongList = blRepo.findAll();
            for (BangLuong bl : bangLuongList) {
                int thang = bl.getNgayBD() != null ? bl.getNgayBD().getMonthValue() : 0;
                int nam = bl.getNgayBD() != null ? bl.getNgayBD().getYear() : 0;
                String thangNam = thang + "/" + nam;
                String tenBL = "Bảng lương tháng " + thangNam;
                String trangThai = bl.getTrangThai() != null ? bl.getTrangThai().getDisplayName() : "";

                // Tính tổng lương thực lãnh từ ChiTietLuong
                double tongLuong = 0;
                try {
                    var chiTietList = blRepo.findByBangLuong(bl.getMaBL());
                    tongLuong = chiTietList.stream()
                            .mapToDouble(ct -> ct.getLuongThucNhan())
                            .sum();
                } catch (Exception ignored) {}

                model.addRow(new Object[]{
                        thangNam,
                        tenBL,
                        trangThai,
                        formatMoney(tongLuong)
                });
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Lỗi tải dữ liệu: " + ex.getMessage(), "", "", ""});
        }

        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);

        // Right-align tổng lương
        javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(right);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Báo cáo tổng hợp bảng lương"));

        JPanel toolbar = buildRefreshToolbar(panel, "Báo cáo lương");

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    // =======================
    // Helpers
    // =======================

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        table.getTableHeader().setForeground(UIColors.TEXT_DARK);
        table.setSelectionBackground(UIColors.LIGHT_PURPLE);
        table.setSelectionForeground(UIColors.TEXT_DARK);
        table.setGridColor(UIColors.BORDER_GRAY);
        return table;
    }

    private JPanel buildRefreshToolbar(JPanel targetPanel, String tabName) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        JButton btn = UIHelper.createDefaultButton("Làm mới");
        btn.addActionListener(e -> {
            JTabbedPane tp = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, targetPanel);
            if (tp != null) {
                int idx = tp.indexOfComponent(targetPanel);
                if (idx >= 0) {
                    JPanel newTab;
                    switch (idx) {
                        case 0: newTab = buildTongQuanTab(); break;
                        case 1: newTab = buildLeaveReportTab(); break;
                        case 2: newTab = buildSalaryReportTab(); break;
                        default: return;
                    }
                    tp.removeTabAt(idx);
                    tp.insertTab(tabName, null, newTab, null, idx);
                    tp.setSelectedIndex(idx);
                }
            }
        });
        toolbar.add(btn);
        return toolbar;
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format((long) amount) + " đ";
    }
}
