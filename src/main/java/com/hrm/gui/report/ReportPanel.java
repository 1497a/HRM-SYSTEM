package com.hrm.gui.report;

import com.hrm.bus.ChamCongBUS;
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
import com.hrm.model.DonXinNghiPhep;
import com.hrm.model.NhanVien;
import com.hrm.model.SoDungPhep;
import com.hrm.model.TaiKhoan;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.SimpleXlsx;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final ChamCongBUS chamCongService;
    private final DataScope reportScope;
    private final String currentMaNV;
    private final List<NhanVien> scopedEmployees;
    private final Set<String> scopedEmployeeIds;
    private final boolean canExportReports;

    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int NAM_HIEN_TAI = LocalDate.now().getYear();

    public ReportPanel() {
        this.nvService       = NhanVienBUS.getInstance();
        this.luongService    = LuongBUS.getInstance();
        this.leaveService    = NghiPhepBUS.getInstance();
        this.danhGiaService  = DanhGiaBUS.getInstance();
        this.chamCongService = ChamCongBUS.getInstance();

        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        this.currentMaNV  = currentUser != null ? currentUser.getMaNV() : null;
        this.reportScope  = XacThucBUS.getInstance().getScopeForAction(PermissionCodes.REPORT_VIEW);
        this.scopedEmployees   = loadScopedEmployees();
        this.scopedEmployeeIds = scopedEmployees.stream()
                .map(NhanVien::getMaNhanVien)
                .collect(Collectors.toSet());
        this.canExportReports = SessionContext.getInstance().hasPermission(PermissionCodes.REPORT_EXPORT);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIFonts.TEXT_NORMAL);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Tổng quan nhân sự",  buildTongQuanTab());
        tabs.addTab("Báo cáo chấm công",  buildChamCongTab());
        tabs.addTab("Báo cáo lương",       buildSalaryTab());
        tabs.addTab("Báo cáo nghỉ phép",  buildLeaveTab());
        tabs.addTab("Báo cáo đánh giá",   buildEvalTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ==========================================================================
    // TAB 0: TỔNG QUAN NHÂN SỰ
    // ==========================================================================
    private JPanel buildTongQuanTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        List<NhanVien> allNV = new ArrayList<>(scopedEmployees);
        long dangLam  = allNV.stream().filter(nv -> "dang_lam_viec".equals(nv.getTrangThai())).count();
        long tamNghi  = allNV.stream().filter(nv -> "tam_nghi".equals(nv.getTrangThai())).count();
        long nghiViec = allNV.stream().filter(nv -> "nghi_viec".equals(nv.getTrangThai())).count();

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(0, 0, 16, 0));
        cards.add(RoundedPanel.createStatCard("Tổng nhân viên",  String.valueOf(allNV.size()), UIColors.PRIMARY_PURPLE));
        cards.add(RoundedPanel.createStatCard("Đang làm việc",   String.valueOf(dangLam),      UIColors.SUCCESS_GREEN));
        cards.add(RoundedPanel.createStatCard("Tạm nghỉ",        String.valueOf(tamNghi),      UIColors.ACCENT_YELLOW));
        cards.add(RoundedPanel.createStatCard("Đã nghỉ việc",    String.valueOf(nghiViec),     UIColors.DANGER_RED));

        Map<String, Integer> byDept = new LinkedHashMap<>();
        for (NhanVien nv : allNV) {
            String pb = nv.getTenPhongBanHienTai();
            if (pb == null || pb.isEmpty()) pb = "(Chưa phân công)";
            byDept.merge(pb, 1, Integer::sum);
        }

        DefaultTableModel model = nonEditableModel("Phòng ban", "Số nhân viên");
        byDept.forEach((dept, cnt) -> model.addRow(new Object[]{dept, cnt}));
        if (model.getRowCount() == 0)
            model.addRow(new Object[]{"Không có dữ liệu trong phạm vi báo cáo", 0});

        PurpleTable table = new PurpleTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Số nhân viên theo phòng ban"));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(cards, BorderLayout.CENTER);
        north.add(buildToolbar("Tổng quan nhân sự", 0, table, "bao_cao_tong_quan"), BorderLayout.EAST);

        panel.add(north, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================================
    // TAB 1: CHẤM CÔNG (MỚI)
    // ==========================================================================
    private JPanel buildChamCongTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // --- filter bar ---
        JComboBox<Integer> cbThang = new JComboBox<>();
        for (int t = 1; t <= 12; t++) cbThang.addItem(t);
        cbThang.setSelectedItem(LocalDate.now().getMonthValue());

        JComboBox<Integer> cbNam = new JComboBox<>();
        for (int n = NAM_HIEN_TAI - 2; n <= NAM_HIEN_TAI + 1; n++) cbNam.addItem(n);
        cbNam.setSelectedItem(NAM_HIEN_TAI);

        DefaultTableModel model = nonEditableModel(
                "Nhân viên", "Phòng ban", "Ngày công", "Giờ làm", "Giờ OT", "% Có mặt");
        PurpleTable table = new PurpleTable(model);
        configAttendanceColumns(table);

        JButton btnXem    = UIHelper.createDefaultButton("Xem");
        JButton btnIn     = UIHelper.createDefaultButton("In báo cáo");
        JButton btnExport = canExportReports ? UIHelper.createDefaultButton("Xuất Excel") : null;
        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");

        Runnable loadData = () -> {
            int thang = (Integer) cbThang.getSelectedItem();
            int nam   = (Integer) cbNam.getSelectedItem();
            model.setRowCount(0);
            int ngayLamViec = demNgayLamViec(thang, nam);
            try {
                List<Object[]> rows = chamCongService.getMonthlySummaryForReport(thang, nam, currentMaNV);
                for (Object[] r : rows) {
                    int soNgay = (Integer) r[3];
                    double pct = ngayLamViec > 0 ? soNgay * 100.0 / ngayLamViec : 0;
                    model.addRow(new Object[]{
                            "[" + r[0] + "] " + r[1],
                            r[2],
                            soNgay,
                            String.format("%.1f h", r[4]),
                            String.format("%.1f h", r[5]),
                            String.format("%.0f%%", pct)
                    });
                }
            } catch (Exception ex) {
                model.addRow(new Object[]{"Lỗi tải dữ liệu: " + ex.getMessage(), "", "", "", "", ""});
            }
            if (model.getRowCount() == 0)
                model.addRow(new Object[]{"Không có dữ liệu chấm công trong tháng này", "", "", "", "", ""});
        };

        btnXem.addActionListener(e -> loadData.run());
        btnRefresh.addActionListener(e -> loadData.run());
        btnIn.addActionListener(e -> printTable(table, "Báo cáo chấm công - "
                + cbThang.getSelectedItem() + "/" + cbNam.getSelectedItem()));
        if (btnExport != null)
            btnExport.addActionListener(e -> exportTableToExcel(table, "bao_cao_cham_cong"));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Tháng:"));
        filterBar.add(cbThang);
        filterBar.add(new JLabel("Năm:"));
        filterBar.add(cbNam);
        filterBar.add(btnXem);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionBar.setOpaque(false);
        if (btnExport != null) actionBar.add(btnExport);
        actionBar.add(btnIn);
        actionBar.add(btnRefresh);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(actionBar, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Tổng hợp chấm công theo nhân viên"));

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        loadData.run();
        return panel;
    }

    // ==========================================================================
    // TAB 2: LƯƠNG (CẢI THIỆN)
    // ==========================================================================
    private JPanel buildSalaryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // --- summary model ---
        DefaultTableModel summaryModel = nonEditableModel(
                "Tháng/Năm", "Số NV", "Tổng thu nhập", "Tổng khấu trừ", "Tổng thực nhận", "Trạng thái");

        // --- detail model ---
        DefaultTableModel detailModel = nonEditableModel(
                "Nhân viên", "Phòng ban", "Ngày công", "Thu nhập", "Khấu trừ", "Thực nhận");

        PurpleTable summaryTable = new PurpleTable(summaryModel);
        PurpleTable detailTable  = new PurpleTable(detailModel);
        configMoneyColumns(summaryTable, 2, 3, 4);
        configMoneyColumns(detailTable,  3, 4, 5);

        // --- year filter ---
        JComboBox<String> cbNam = new JComboBox<>();
        cbNam.addItem("Tất cả");
        for (int n = NAM_HIEN_TAI; n >= NAM_HIEN_TAI - 4; n--) cbNam.addItem(String.valueOf(n));

        // store payrolls for detail lookup (wrapper để lambda có thể ghi)
        final java.util.concurrent.atomic.AtomicReference<List<BangLuong>> payrollRef
                = new java.util.concurrent.atomic.AtomicReference<>(new ArrayList<>());

        Runnable loadSummary = () -> {
            summaryModel.setRowCount(0);
            detailModel.setRowCount(0);
            try {
                List<BangLuong> payrolls = luongService.getAllByScope(currentMaNV);
                String selectedYear = (String) cbNam.getSelectedItem();
                payrollRef.set(payrolls);
                for (BangLuong bl : payrolls) {
                    if (!"Tất cả".equals(selectedYear) && bl.getNam() != Integer.parseInt(selectedYear))
                        continue;
                    List<ChiTietLuong> details = luongService.getChiTietByScope(bl.getMaBL(), currentMaNV);
                    if (details.isEmpty()) continue;
                    double tongThuNhap = details.stream().mapToDouble(ChiTietLuong::getTongLuong).sum();
                    double tongKhauTru = details.stream().mapToDouble(ChiTietLuong::getTongKhauTru).sum();
                    double tongNet     = details.stream().mapToDouble(ChiTietLuong::getLuongThucNhan).sum();
                    summaryModel.addRow(new Object[]{
                            bl.getThang() + "/" + bl.getNam(),
                            details.size(),
                            formatMoney(tongThuNhap),
                            formatMoney(tongKhauTru),
                            formatMoney(tongNet),
                            bl.getTrangThai() != null ? bl.getTrangThai().getDisplayName() : ""
                    });
                }
            } catch (Exception ex) {
                summaryModel.addRow(new Object[]{"Lỗi tải dữ liệu: " + ex.getMessage(), "", "", "", "", ""});
            }
            if (summaryModel.getRowCount() == 0)
                summaryModel.addRow(new Object[]{"Không có dữ liệu trong phạm vi báo cáo", "", "", "", "", ""});
        };

        // click summary row → load detail
        summaryTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = summaryTable.getSelectedRow();
            detailModel.setRowCount(0);
            if (row < 0 || row >= payrollRef.get().size()) return;
            // find matching payroll
            String thangNam = (String) summaryModel.getValueAt(row, 0);
            String selectedYear = (String) cbNam.getSelectedItem();
            List<BangLuong> filtered = payrollRef.get().stream()
                    .filter(bl -> {
                        if (!"Tất cả".equals(selectedYear) && bl.getNam() != Integer.parseInt(selectedYear))
                            return false;
                        return (bl.getThang() + "/" + bl.getNam()).equals(thangNam);
                    })
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) return;
            BangLuong bl = filtered.get(0);
            try {
                List<ChiTietLuong> details = luongService.getChiTietByScope(bl.getMaBL(), currentMaNV);
                for (ChiTietLuong ctl : details) {
                    String pb = luongService.getPhongBanCuaNV(ctl.getMaNV());
                    detailModel.addRow(new Object[]{
                            "[" + ctl.getMaNV() + "] " + ctl.getTenNV(),
                            pb != null ? pb : "",
                            ctl.getSoNgayCong(),
                            formatMoney(ctl.getTongLuong()),
                            formatMoney(ctl.getTongKhauTru()),
                            formatMoney(ctl.getLuongThucNhan())
                    });
                }
            } catch (Exception ex) {
                detailModel.addRow(new Object[]{"Lỗi: " + ex.getMessage(), "", "", "", "", ""});
            }
        });

        cbNam.addActionListener(e -> loadSummary.run());

        JButton btnIn     = UIHelper.createDefaultButton("In báo cáo");
        JButton btnExport = canExportReports ? UIHelper.createDefaultButton("Xuất Excel") : null;
        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");

        btnIn.addActionListener(e -> printTable(summaryTable, "Báo cáo lương - " + cbNam.getSelectedItem()));
        if (btnExport != null)
            btnExport.addActionListener(e -> exportTableToExcel(summaryTable, "bao_cao_luong"));
        btnRefresh.addActionListener(e -> loadSummary.run());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Năm:"));
        filterBar.add(cbNam);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionBar.setOpaque(false);
        if (btnExport != null) actionBar.add(btnExport);
        actionBar.add(btnIn);
        actionBar.add(btnRefresh);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(actionBar, BorderLayout.EAST);

        JScrollPane scrollSummary = new JScrollPane(summaryTable);
        scrollSummary.setBorder(new TitledBorder("Tổng hợp bảng lương (click vào hàng để xem chi tiết)"));
        JScrollPane scrollDetail  = new JScrollPane(detailTable);
        scrollDetail.setBorder(new TitledBorder("Chi tiết lương nhân viên"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollSummary, scrollDetail);
        split.setResizeWeight(0.5);
        split.setBorder(null);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        loadSummary.run();
        return panel;
    }

    // ==========================================================================
    // TAB 3: NGHỈ PHÉP (CẢI THIỆN)
    // ==========================================================================
    private JPanel buildLeaveTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        DefaultTableModel balanceModel = nonEditableModel(
                "Nhân viên", "Loại phép", "Được cấp", "Đã dùng", "Còn lại");
        DefaultTableModel requestModel = nonEditableModel(
                "Nhân viên", "Loại phép", "Từ ngày", "Đến ngày", "Số ngày", "Trạng thái");

        PurpleTable balanceTable = new PurpleTable(balanceModel);
        PurpleTable requestTable = new PurpleTable(requestModel);

        balanceTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        balanceTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        requestTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        requestTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        requestTable.getColumnModel().getColumn(5).setPreferredWidth(110);

        // color-code trạng thái đơn từ
        requestTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String s = v != null ? v.toString() : "";
                    if (s.contains("duyệt") || s.contains("Đã duyệt"))
                        setBackground(UIColors.BG_SUCCESS);
                    else if (s.contains("chờ") || s.contains("Chờ"))
                        setBackground(UIColors.BG_WARNING);
                    else if (s.contains("từ chối") || s.contains("Từ chối"))
                        setBackground(UIColors.BG_DANGER);
                    else
                        setBackground(Color.WHITE);
                }
                return this;
            }
        });

        JComboBox<String> cbNam = new JComboBox<>();
        cbNam.addItem("Tất cả");
        for (int n = NAM_HIEN_TAI; n >= NAM_HIEN_TAI - 3; n--) cbNam.addItem(String.valueOf(n));

        Runnable loadData = () -> {
            balanceModel.setRowCount(0);
            requestModel.setRowCount(0);
            String selectedYear = (String) cbNam.getSelectedItem();
            int filterYear = "Tất cả".equals(selectedYear) ? -1 : Integer.parseInt(selectedYear);

            // Số dư phép
            for (NhanVien nv : scopedEmployees) {
                try {
                    List<SoDungPhep> balances = filterYear > 0
                            ? leaveService.getBalances(nv.getMaNhanVien()).stream()
                                    .filter(b -> true) // getBalances already returns current year
                                    .collect(Collectors.toList())
                            : leaveService.getBalances(nv.getMaNhanVien());
                    if (balances.isEmpty()) {
                        balanceModel.addRow(new Object[]{displayEmployee(nv), "-", 0, 0, 0});
                    } else {
                        for (SoDungPhep b : balances) {
                            balanceModel.addRow(new Object[]{
                                    displayEmployee(nv),
                                    mapLeaveTypeCode(b.getMaLoaiPhep()),
                                    b.getSoNgayDuocCap(),
                                    b.getSoNgayDaDung(),
                                    b.getSoNgayConLai()
                            });
                        }
                    }
                } catch (Exception ignored) {}
            }
            if (balanceModel.getRowCount() == 0)
                balanceModel.addRow(new Object[]{"Không có dữ liệu", "", "", "", ""});

            // Lịch sử đơn từ
            try {
                List<DonXinNghiPhep> dons = leaveService.getAllForReport(currentMaNV);
                for (DonXinNghiPhep don : dons) {
                    if (filterYear > 0 && don.getTuNgay() != null && don.getTuNgay().getYear() != filterYear)
                        continue;
                    String trangThai = don.getTrangThai() != null
                            ? don.getTrangThai().getTenHienThi() : "";
                    requestModel.addRow(new Object[]{
                            "[" + don.getMaNV() + "] " + (don.getTenNhanVien() != null ? don.getTenNhanVien() : ""),
                            don.getTenLoaiPhep() != null ? don.getTenLoaiPhep() : mapLeaveTypeCode(don.getMaLoaiPhep()),
                            don.getTuNgay() != null ? don.getTuNgay().toString() : "",
                            don.getDenNgay() != null ? don.getDenNgay().toString() : "",
                            String.format("%.1f", don.getSoNgayNghi()),
                            trangThai
                    });
                }
            } catch (Exception ex) {
                requestModel.addRow(new Object[]{"Lỗi tải đơn từ: " + ex.getMessage(), "", "", "", "", ""});
            }
            if (requestModel.getRowCount() == 0)
                requestModel.addRow(new Object[]{"Không có đơn nghỉ phép", "", "", "", "", ""});
        };

        cbNam.addActionListener(e -> loadData.run());

        JButton btnIn     = UIHelper.createDefaultButton("In báo cáo");
        JButton btnExport = canExportReports ? UIHelper.createDefaultButton("Xuất Excel") : null;
        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");

        btnIn.addActionListener(e -> printTable(balanceTable, "Báo cáo nghỉ phép - " + cbNam.getSelectedItem()));
        if (btnExport != null)
            btnExport.addActionListener(e -> exportTableToExcel(balanceTable, "bao_cao_nghi_phep"));
        btnRefresh.addActionListener(e -> loadData.run());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Năm:"));
        filterBar.add(cbNam);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionBar.setOpaque(false);
        if (btnExport != null) actionBar.add(btnExport);
        actionBar.add(btnIn);
        actionBar.add(btnRefresh);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(actionBar, BorderLayout.EAST);

        JScrollPane scrollBalance = new JScrollPane(balanceTable);
        scrollBalance.setBorder(new TitledBorder("Số dư phép theo nhân viên"));
        JScrollPane scrollRequest = new JScrollPane(requestTable);
        scrollRequest.setBorder(new TitledBorder("Lịch sử đơn xin nghỉ phép"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBalance, scrollRequest);
        split.setResizeWeight(0.45);
        split.setBorder(null);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        loadData.run();
        return panel;
    }

    // ==========================================================================
    // TAB 4: ĐÁNH GIÁ HIỆU SUẤT
    // ==========================================================================
    private JPanel buildEvalTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Nạp toàn bộ submissions một lần
        List<DanhGiaHieuSuat> allSubmissions;
        try {
            allSubmissions = danhGiaService.getAllSubmissionsByScope(currentMaNV);
        } catch (Exception e) {
            allSubmissions = new ArrayList<>();
        }
        final List<DanhGiaHieuSuat> allData = allSubmissions;

        // Map maNV → phongBan (từ danh sách NV đã load)
        Map<String, String> nvToPhongBan = scopedEmployees.stream()
                .collect(Collectors.toMap(
                        NhanVien::getMaNhanVien,
                        nv -> nv.getTenPhongBanHienTai() != null ? nv.getTenPhongBanHienTai() : "(Chưa phân công)",
                        (a, b) -> a));

        // Filter: chọn kỳ đánh giá
        JComboBox<String> cbKy = new JComboBox<>();
        cbKy.addItem("Tất cả kỳ");
        allData.stream()
                .map(DanhGiaHieuSuat::getTenDot)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .forEach(cbKy::addItem);

        // Stat cards (cập nhật động)
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Table 1: Phân bố xếp loại theo phòng ban
        DefaultTableModel deptModel = nonEditableModel(
                "Phòng ban", "Xuất sắc", "Tốt", "Khá", "Trung bình", "Yếu", "Tổng", "Điểm TB");
        PurpleTable deptTable = new PurpleTable(deptModel);
        for (int c = 1; c <= 7; c++) deptTable.getColumnModel().getColumn(c).setPreferredWidth(80);
        deptTable.getColumnModel().getColumn(0).setPreferredWidth(180);

        // Table 2: Bảng xếp hạng nhân viên (sorted điểm giảm dần)
        DefaultTableModel rankModel = nonEditableModel(
                "Hạng", "Nhân viên", "Phòng ban", "Kỳ đánh giá", "Điểm", "Xếp loại");
        PurpleTable rankTable = new PurpleTable(rankModel);
        rankTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        rankTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        rankTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        rankTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        rankTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        rankTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Color-code xếp loại (cột 5)
        DefaultTableCellRenderer xepLoaiRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String s = v != null ? v.toString() : "";
                    if ("Xuất sắc".equals(s) || "Tốt".equals(s)) setBackground(UIColors.BG_SUCCESS);
                    else if ("Khá".equals(s))                      setBackground(UIColors.BG_WARNING);
                    else if ("Trung bình".equals(s) || "Yếu".equals(s)) setBackground(UIColors.BG_DANGER);
                    else                                            setBackground(Color.WHITE);
                }
                return this;
            }
        };
        rankTable.getColumnModel().getColumn(5).setCellRenderer(xepLoaiRenderer);

        // Hàm load dữ liệu theo kỳ được chọn
        Runnable loadEval = () -> {
            String selectedKy = (String) cbKy.getSelectedItem();
            List<DanhGiaHieuSuat> data = "Tất cả kỳ".equals(selectedKy)
                    ? allData
                    : allData.stream()
                            .filter(d -> selectedKy.equals(d.getTenDot()))
                            .collect(Collectors.toList());

            // --- Cập nhật stat cards ---
            long tongDG   = data.size();
            long xsTot    = data.stream().filter(d ->
                    d.getXepLoai() == DanhGiaHieuSuat.XepLoai.XUAT_SAC
                 || d.getXepLoai() == DanhGiaHieuSuat.XepLoai.TOT).count();
            double avg    = data.stream().mapToDouble(DanhGiaHieuSuat::getTongDiem).average().orElse(0);
            long openCy;
            try { openCy = danhGiaService.getOpenCycles().size(); }
            catch (Exception ex) { openCy = 0; }

            cardsPanel.removeAll();
            cardsPanel.add(RoundedPanel.createStatCard("Nhân viên được đánh giá", String.valueOf(tongDG),      UIColors.PRIMARY_PURPLE));
            cardsPanel.add(RoundedPanel.createStatCard("Xuất sắc / Tốt",          String.valueOf(xsTot),       UIColors.SUCCESS_GREEN));
            cardsPanel.add(RoundedPanel.createStatCard("Điểm trung bình",          String.format("%.1f", avg), UIColors.ACCENT_YELLOW));
            cardsPanel.add(RoundedPanel.createStatCard("Đợt đang mở",             String.valueOf(openCy),     UIColors.DANGER_RED));
            cardsPanel.revalidate();
            cardsPanel.repaint();

            // --- Phân bố theo phòng ban ---
            deptModel.setRowCount(0);
            // group by phongBan
            Map<String, int[]> deptCounts = new LinkedHashMap<>();
            Map<String, double[]> deptScores = new LinkedHashMap<>();
            for (DanhGiaHieuSuat dg : data) {
                String pb = nvToPhongBan.getOrDefault(dg.getMaNV(), "(Chưa phân công)");
                int[] counts = deptCounts.computeIfAbsent(pb, k -> new int[5]); // [XS,Tốt,Khá,TB,Yếu]
                double[] scores = deptScores.computeIfAbsent(pb, k -> new double[]{0, 0}); // [sum, count]
                if (dg.getXepLoai() != null) {
                    switch (dg.getXepLoai()) {
                        case XUAT_SAC:  counts[0]++; break;
                        case TOT:       counts[1]++; break;
                        case KHA:       counts[2]++; break;
                        case TRUNG_BINH:counts[3]++; break;
                        case YEU:       counts[4]++; break;
                    }
                }
                scores[0] += dg.getTongDiem();
                scores[1]++;
            }
            // sắp xếp theo điểm TB giảm dần
            deptCounts.entrySet().stream()
                    .sorted((a, b2) -> {
                        double[] sa = deptScores.get(a.getKey());
                        double[] sb = deptScores.get(b2.getKey());
                        double avgA = sa[1] > 0 ? sa[0] / sa[1] : 0;
                        double avgB = sb[1] > 0 ? sb[0] / sb[1] : 0;
                        return Double.compare(avgB, avgA);
                    })
                    .forEach(e -> {
                        int[] c = e.getValue();
                        double[] s = deptScores.get(e.getKey());
                        int total = c[0] + c[1] + c[2] + c[3] + c[4];
                        double avgPB = s[1] > 0 ? s[0] / s[1] : 0;
                        deptModel.addRow(new Object[]{
                                e.getKey(), c[0], c[1], c[2], c[3], c[4], total,
                                String.format("%.1f", avgPB)
                        });
                    });
            if (deptModel.getRowCount() == 0)
                deptModel.addRow(new Object[]{"Không có dữ liệu", 0, 0, 0, 0, 0, 0, "0.0"});

            // --- Bảng xếp hạng ---
            rankModel.setRowCount(0);
            List<DanhGiaHieuSuat> sorted = data.stream()
                    .sorted((a, b2) -> Double.compare(b2.getTongDiem(), a.getTongDiem()))
                    .collect(Collectors.toList());
            int rank = 1;
            for (DanhGiaHieuSuat dg : sorted) {
                String pb = nvToPhongBan.getOrDefault(dg.getMaNV(), "(Chưa phân công)");
                String tenNV = "[" + dg.getMaNV() + "] " + (dg.getTenNhanVien() != null ? dg.getTenNhanVien() : "");
                rankModel.addRow(new Object[]{
                        rank++,
                        tenNV,
                        pb,
                        dg.getTenDot() != null ? dg.getTenDot() : "",
                        String.format("%.2f", dg.getTongDiem()),
                        dg.getXepLoai() != null ? dg.getXepLoai().getTenHienThi() : ""
                });
            }
            if (rankModel.getRowCount() == 0)
                rankModel.addRow(new Object[]{"", "Không có dữ liệu trong phạm vi báo cáo", "", "", "", ""});
        };

        cbKy.addActionListener(e -> loadEval.run());

        JButton btnIn     = UIHelper.createDefaultButton("In báo cáo");
        JButton btnExport = canExportReports ? UIHelper.createDefaultButton("Xuất Excel") : null;
        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");
        btnIn.addActionListener(e -> printTable(rankTable, "Bảng xếp hạng đánh giá - " + cbKy.getSelectedItem()));
        if (btnExport != null)
            btnExport.addActionListener(e -> exportTableToExcel(rankTable, "bao_cao_danh_gia"));
        btnRefresh.addActionListener(e -> loadEval.run());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Kỳ đánh giá:"));
        filterBar.add(cbKy);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionBar.setOpaque(false);
        if (btnExport != null) actionBar.add(btnExport);
        actionBar.add(btnIn);
        actionBar.add(btnRefresh);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(actionBar, BorderLayout.EAST);

        JPanel northSection = new JPanel(new BorderLayout(0, 6));
        northSection.setOpaque(false);
        northSection.add(topBar, BorderLayout.NORTH);
        northSection.add(cardsPanel, BorderLayout.CENTER);

        JScrollPane scrollDept = new JScrollPane(deptTable);
        scrollDept.setBorder(new TitledBorder("Phân bố xếp loại theo phòng ban (sắp xếp theo điểm TB giảm dần)"));
        JScrollPane scrollRank = new JScrollPane(rankTable);
        scrollRank.setBorder(new TitledBorder("Bảng xếp hạng nhân viên (điểm cao → thấp)"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollDept, scrollRank);
        split.setResizeWeight(0.4);
        split.setBorder(null);

        panel.add(northSection, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        loadEval.run();
        return panel;
    }

    // ==========================================================================
    // UTILITIES
    // ==========================================================================

    /** Toolbar chung: [Xuất Excel] [In báo cáo] [Làm mới] cho các tab không có filter riêng. */
    private JPanel buildToolbar(String tabTitle, int tabIndex, JTable table, String exportBaseName) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bar.setOpaque(false);
        if (canExportReports) {
            JButton btnExport = UIHelper.createDefaultButton("Xuất Excel");
            btnExport.addActionListener(e -> exportTableToExcel(table, exportBaseName));
            bar.add(btnExport);
        }
        JButton btnIn = UIHelper.createDefaultButton("In báo cáo");
        btnIn.addActionListener(e -> printTable(table, "Báo cáo HRM - " + tabTitle));
        bar.add(btnIn);
        JButton btnRefresh = UIHelper.createDefaultButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            JTabbedPane tp = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, (Component) table);
            if (tp == null) return;
            JPanel newTab;
            switch (tabIndex) {
                case 0: newTab = buildTongQuanTab(); break;
                case 4: newTab = buildEvalTab();     break;
                default: return;
            }
            tp.removeTabAt(tabIndex);
            String[] tabNames = {"Tổng quan nhân sự", "Báo cáo chấm công", "Báo cáo lương",
                                 "Báo cáo nghỉ phép", "Báo cáo đánh giá"};
            tp.insertTab(tabNames[tabIndex], null, newTab, null, tabIndex);
            tp.setSelectedIndex(tabIndex);
        });
        bar.add(btnRefresh);
        return bar;
    }

    private void printTable(JTable table, String title) {
        try {
            table.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(title),
                    new MessageFormat("Trang {0}"));
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi in báo cáo: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportTableToExcel(JTable table, String baseName) {
        if (table == null || table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel báo cáo");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File(baseName + "_" + LocalDate.now().format(FILE_DATE) + ".xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx"))
            file = new File(file.getAbsolutePath() + ".xlsx");

        final File target = file;
        final String[] headers = getTableHeaders(table);
        final List<String[]> rows = getTableRows(table);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                SimpleXlsx.write(target, headers, rows);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ReportPanel.this,
                            "Xuất Excel thành công:\n" + target.getAbsolutePath(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ReportPanel.this,
                            "Lỗi xuất Excel: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String[] getTableHeaders(JTable table) {
        int n = table.getColumnCount();
        String[] h = new String[n];
        for (int i = 0; i < n; i++) h[i] = table.getColumnName(i);
        return h;
    }

    private List<String[]> getTableRows(JTable table) {
        List<String[]> rows = new ArrayList<>();
        for (int r = 0; r < table.getRowCount(); r++) {
            String[] row = new String[table.getColumnCount()];
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object v = table.getValueAt(r, c);
                row[c] = v != null ? v.toString() : "";
            }
            rows.add(row);
        }
        return rows;
    }

    private List<NhanVien> loadScopedEmployees() {
        if (reportScope == DataScope.NONE || (currentMaNV == null && reportScope != DataScope.ALL))
            return Collections.emptyList();
        return nvService.getAllByActionScope(PermissionCodes.REPORT_VIEW, currentMaNV);
    }

    private static DefaultTableModel nonEditableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private static void configMoneyColumns(JTable table, int... cols) {
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : cols) table.getColumnModel().getColumn(c).setCellRenderer(right);
    }

    private static void configAttendanceColumns(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
    }

    private int demNgayLamViec(int thang, int nam) {
        LocalDate d    = LocalDate.of(nam, thang, 1);
        LocalDate cuoi = d.withDayOfMonth(d.lengthOfMonth());
        int count = 0;
        while (!d.isAfter(cuoi)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) count++;
            d = d.plusDays(1);
        }
        return count;
    }

    private String displayEmployee(NhanVien nv) {
        if (nv == null) return "";
        String ten = nv.getHoTen() != null ? nv.getHoTen() : nv.getMaNhanVien();
        return "[" + nv.getMaNhanVien() + "] " + ten;
    }

    private String formatMoney(double value) {
        return MONEY_FORMAT.format(value) + " đ";
    }

    private String mapLeaveTypeCode(String code) {
        if (code == null) return "";
        switch (code) {
            case "PHEP_NAM": case "AL":          return "Phép năm";
            case "PHEP_OM":  case "SL":          return "Nghỉ ốm";
            case "PHEP_CUOI":                    return "Nghỉ cưới";
            case "PHEP_TANG":                    return "Nghỉ tang";
            case "PHEP_THAI_SAN": case "ML":     return "Thai sản";
            case "PHEP_KHONG_LUONG": case "UL":  return "Không lương";
            default:                             return code;
        }
    }
}
