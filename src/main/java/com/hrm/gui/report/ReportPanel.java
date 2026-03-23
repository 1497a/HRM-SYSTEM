package com.hrm.gui.report;

import com.hrm.bus.DanhGiaBUS;
import com.hrm.bus.LuongBUS;
import com.hrm.bus.NghiPhepBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.XacThucBUS;
import com.hrm.gui.components.PurpleTable;
import com.hrm.gui.components.RoundedPanel;
import com.hrm.model.BangLuong;
import com.hrm.model.ChiTietLuong;
import com.hrm.model.DanhGiaHieuSuat;
import com.hrm.model.DataScope;
import com.hrm.model.NhanVien;
import com.hrm.model.SoDungPhep;
import com.hrm.model.TaiKhoan;
import com.hrm.util.PermissionCodes;
import com.hrm.util.UIFonts;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportPanel extends JPanel {

    private final NhanVienBUS nvService;
    private final LuongBUS luongService;
    private final NghiPhepBUS leaveService;
    private final DanhGiaBUS danhGiaService;
    private final DataScope reportScope;
    private final String currentMaNV;
    private final List<NhanVien> scopedEmployees;
    private final Set<String> scopedEmployeeIds;
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    public ReportPanel() {
        this.nvService = NhanVienBUS.getInstance();
        this.luongService = LuongBUS.getInstance();
        this.leaveService = NghiPhepBUS.getInstance();
        this.danhGiaService = DanhGiaBUS.getInstance();
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        this.currentMaNV = currentUser != null ? currentUser.getMaNV() : null;
        this.reportScope = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.REPORT_VIEW);
        this.scopedEmployees = loadScopedEmployees();
        this.scopedEmployeeIds = scopedEmployees.stream()
                .map(NhanVien::getMaNhanVien)
                .collect(Collectors.toSet());
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIFonts.TEXT_NORMAL);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Tổng quan nhân sự", buildTongQuanTab());
        tabbedPane.addTab("Báo cáo nghỉ phép", buildLeaveReportTab());
        tabbedPane.addTab("Báo cáo lương", buildSalaryReportTab());
        tabbedPane.addTab("Báo cáo đánh giá", buildEvalReportTab());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildTongQuanTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        List<NhanVien> allNV = new ArrayList<>(scopedEmployees);
        int tongNV = allNV.size();
        long dangLam = allNV.stream().filter(nv -> "dang_lam_viec".equals(nv.getTrangThai())).count();
        long tamNghi = allNV.stream().filter(nv -> "tam_nghi".equals(nv.getTrangThai())).count();
        long nghiViec = allNV.stream().filter(nv -> "nghi_viec".equals(nv.getTrangThai())).count();
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(0, 0, 16, 0));
        cardsPanel.add(RoundedPanel.createStatCard("Tổng nhân viên", String.valueOf(tongNV), UIColors.PRIMARY_PURPLE));
        cardsPanel.add(RoundedPanel.createStatCard("Đang làm việc", String.valueOf(dangLam), UIColors.SUCCESS_GREEN));
        cardsPanel.add(RoundedPanel.createStatCard("Tạm nghỉ", String.valueOf(tamNghi), UIColors.ACCENT_YELLOW));
        cardsPanel.add(RoundedPanel.createStatCard("Đã nghỉ việc", String.valueOf(nghiViec), UIColors.DANGER_RED));
        Map<String, Integer> countByDept = new LinkedHashMap<>();
        for (NhanVien nv : allNV) {
            String department = nv.getTenPhongBanHienTai();
            if (department == null || department.isEmpty()) {
                department = "(Chưa phân công)";
            }
            countByDept.merge(department, 1, Integer::sum);
        }
        DefaultTableModel model = new DefaultTableModel(new String[]{"Phòng ban", "Số nhân viên"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Map.Entry<String, Integer> entry : countByDept.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"Không có dữ liệu trong phạm vi báo cáo", 0});
        }
        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Số nhân viên theo phòng ban"));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        toolbar.setOpaque(false);
        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");
        btnRefresh.addActionListener(e -> refreshTab(panel, "Tổng quan nhân sự", 0));
        toolbar.add(btnRefresh);
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(cardsPanel, BorderLayout.CENTER);
        north.add(toolbar, BorderLayout.EAST);
        panel.add(north, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLeaveReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Nhân viên", "Loại phép", "Được cấp", "Đã dùng", "Còn lại"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (NhanVien nv : scopedEmployees) {
            try {
                List<SoDungPhep> balances = leaveService.getBalances(nv.getMaNhanVien());
                if (balances.isEmpty()) {
                    model.addRow(new Object[]{displayEmployee(nv), "-", 0, 0, 0});
                } else {
                    for (SoDungPhep balance : balances) {
                        model.addRow(new Object[]{
                                displayEmployee(nv),
                                mapLeaveTypeCode(balance.getMaLoaiPhep()),
                                balance.getSoNgayDuocCap(),
                                balance.getSoNgayDaDung(),
                                balance.getSoNgayConLai()
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"Không có dữ liệu trong phạm vi báo cáo", "", "", "", ""});
        }
        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Báo cáo sử dụng phép nghỉ"));
        panel.add(buildRefreshToolbar(panel, "Báo cáo nghỉ phép", 1), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSalaryReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Tháng/Năm", "Trạng thái", "Tổng lương (thực nhận)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        try {
            List<BangLuong> payrolls = luongService.getAll();
            for (BangLuong bangLuong : payrolls) {
                List<ChiTietLuong> details = applyPayrollScopeFilter(luongService.getChiTiet(bangLuong.getMaBL()));
                if (details.isEmpty() && reportScope != DataScope.ALL) {
                    continue;
                }
                double tongLuong = details.stream()
                        .mapToDouble(ChiTietLuong::getLuongThucNhan)
                        .sum();
                model.addRow(new Object[]{
                        bangLuong.getThang() + "/" + bangLuong.getNam(),
                        bangLuong.getTrangThai() != null ? bangLuong.getTrangThai().getDisplayName() : "",
                        formatMoney(tongLuong)
                });
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Lỗi tải dữ liệu: " + ex.getMessage(), "", ""});
        }
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"Không có dữ liệu trong phạm vi báo cáo", "", ""});
        }
        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Báo cáo tổng hợp bảng lương"));
        panel.add(buildRefreshToolbar(panel, "Báo cáo lương", 2), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private List<NhanVien> loadScopedEmployees() {
        if (reportScope == DataScope.NONE || currentMaNV == null && reportScope != DataScope.ALL) {
            return Collections.emptyList();
        }
        return nvService.getAllByActionScope(PermissionCodes.REPORT_VIEW, currentMaNV);
    }

    private List<ChiTietLuong> applyPayrollScopeFilter(List<ChiTietLuong> details) {
        if (details == null) {
            return new ArrayList<>();
        }
        if (reportScope == DataScope.ALL) {
            return new ArrayList<>(details);
        }
        return details.stream()
                .filter(detail -> scopedEmployeeIds.contains(detail.getMaNV()))
                .collect(Collectors.toList());
    }

    private PurpleTable buildStyledTable(DefaultTableModel model) {
        return new PurpleTable(model);
    }

    private String mapLeaveTypeCode(String code) {
        if (code == null) {
            return "";
        }
        switch (code) {
            case "PHEP_NAM":
            case "AL":
                return "Phép năm";
            case "PHEP_OM":
            case "SL":
                return "Nghỉ ốm";
            case "PHEP_CUOI":
                return "Nghỉ cưới";
            case "PHEP_TANG":
                return "Nghỉ tang";
            case "PHEP_THAI_SAN":
            case "ML":
                return "Thai sản";
            case "PHEP_KHONG_LUONG":
            case "UL":
                return "Không lương";
            default:
                return code;
        }
    }

    private JPanel buildRefreshToolbar(JPanel targetPanel, String tabName, int tabIndex) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        toolbar.setOpaque(false);
        JButton btn = UIHelper.createDefaultButton("Làm mới");
        btn.addActionListener(e -> refreshTab(targetPanel, tabName, tabIndex));
        toolbar.add(btn);
        return toolbar;
    }

    private void refreshTab(JPanel targetPanel, String tabName, int tabIndex) {
        JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, targetPanel);
        if (tabbedPane == null) {
            return;
        }
        JPanel newTab;
        switch (tabIndex) {
            case 0:
                newTab = buildTongQuanTab();
                break;
            case 1:
                newTab = buildLeaveReportTab();
                break;
            case 2:
                newTab = buildSalaryReportTab();
                break;
            case 3:
                newTab = buildEvalReportTab();
                break;
            default:
                return;
        }
        tabbedPane.removeTabAt(tabIndex);
        tabbedPane.insertTab(tabName, null, newTab, null, tabIndex);
        tabbedPane.setSelectedIndex(tabIndex);
    }

    private JPanel buildEvalReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        List<DanhGiaHieuSuat> submissions;
        try {
            submissions = danhGiaService.getAllSubmissionsByScope(currentMaNV);
        } catch (Exception e) {
            submissions = new ArrayList<>();
        }

        long tongDG = submissions.size();
        long xsTot = submissions.stream()
                .filter(d -> d.getXepLoai() == DanhGiaHieuSuat.XepLoai.XUAT_SAC
                        || d.getXepLoai() == DanhGiaHieuSuat.XepLoai.TOT)
                .count();
        double avgScore = submissions.stream()
                .mapToDouble(DanhGiaHieuSuat::getTongDiem).average().orElse(0);
        long openCycles;
        try {
            openCycles = danhGiaService.getOpenCycles().size();
        } catch (Exception e) {
            openCycles = 0;
        }

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(0, 0, 12, 0));
        cardsPanel.add(RoundedPanel.createStatCard("Tổng lượt đánh giá", String.valueOf(tongDG), UIColors.PRIMARY_PURPLE));
        cardsPanel.add(RoundedPanel.createStatCard("Xuất sắc / Tốt", String.valueOf(xsTot), UIColors.SUCCESS_GREEN));
        cardsPanel.add(RoundedPanel.createStatCard("Điểm trung bình", String.format("%.1f", avgScore), UIColors.ACCENT_YELLOW));
        cardsPanel.add(RoundedPanel.createStatCard("Đợt đang mở", String.valueOf(openCycles), UIColors.DANGER_RED));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Nhân viên", "Kỳ đánh giá", "Điểm", "Xếp loại", "Người đánh giá", "Ngày"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (DanhGiaHieuSuat dg : submissions) {
            String tenNV = dg.getMaNV() != null
                    ? "[" + dg.getMaNV() + "] " + (dg.getTenNhanVien() != null ? dg.getTenNhanVien() : "")
                    : "";
            String xepLoaiText = dg.getXepLoai() != null ? dg.getXepLoai().getTenHienThi() : "";
            String ngay = dg.getNgayDanhGia() != null ? dg.getNgayDanhGia().toLocalDate().toString() : "";
            model.addRow(new Object[]{
                    tenNV,
                    dg.getTenDot() != null ? dg.getTenDot() : "",
                    String.format("%.2f", dg.getTongDiem()),
                    xepLoaiText,
                    dg.getTenNguoiDanhGia() != null ? dg.getTenNguoiDanhGia() : "",
                    ngay
            });
        }
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"Không có dữ liệu trong phạm vi báo cáo", "", "", "", "", ""});
        }

        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Color-code cột Xếp loại (index 3)
        table.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String v = value != null ? value.toString() : "";
                    if ("Xuất sắc".equals(v) || "Tốt".equals(v)) {
                        setBackground(UIColors.BG_SUCCESS);
                    } else if ("Khá".equals(v)) {
                        setBackground(UIColors.BG_WARNING);
                    } else if ("Trung bình".equals(v) || "Yếu".equals(v)) {
                        setBackground(UIColors.BG_DANGER);
                    } else {
                        setBackground(Color.WHITE);
                    }
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Danh sách đánh giá hiệu suất"));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(cardsPanel, BorderLayout.CENTER);
        north.add(buildRefreshToolbar(panel, "Báo cáo đánh giá", 3), BorderLayout.EAST);

        panel.add(north, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private String displayEmployee(NhanVien nhanVien) {
        if (nhanVien == null) {
            return "";
        }
        String hoTen = nhanVien.getHoTen() != null ? nhanVien.getHoTen() : nhanVien.getMaNhanVien();
        return "[" + nhanVien.getMaNhanVien() + "] " + hoTen;
    }

    private String formatMoney(double value) {
        return MONEY_FORMAT.format(value) + " đ";
    }

}
