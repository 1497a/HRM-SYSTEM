package com.hrm.gui.employee;

import com.hrm.bus.EmployeeImportExportService;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.NhanVien;
import com.hrm.model.PhongBan;
import com.hrm.util.HRMConstants;
import com.hrm.util.UIFonts;
import com.hrm.util.PermissionCodes;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.Font;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel danh sách hồ sơ nhân viên.
 */
public class EmployeeListPanel extends JPanel {

    private final NhanVienBUS nvService = NhanVienBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboPhongBan;
    private JButton btnThem;
    private JButton btnChiTiet;
    private JButton btnNhapExcel;
    private JButton btnXuatExcel;
    private JButton btnIn;
    private List<NhanVien> danhSachHienThi = new ArrayList<>();
    private static final String[] COL_NAMES = {
        "STT", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ",
        "Ngày vào làm", "Loại HĐ", "Trạng thái"
    };
    public EmployeeListPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(buildNorthPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);
        setupPermissions();
        setupEvents();
        loadData();
    }

    private JPanel buildNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        JPanel searchWrap = new JPanel(new BorderLayout(0, 4));
        searchWrap.setOpaque(false);
        JLabel lblHint = new JLabel("Tìm theo: Mã NV / Họ tên. Nhấp đúp vào dòng để xem hồ sơ chi tiết.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIColors.TEXT_DARK);
        searchWrap.add(lblHint, BorderLayout.SOUTH);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIFonts.TEXT_NORMAL);
        lblSearch.setForeground(UIColors.TEXT_DARK);
        txtSearch = new JTextField(20);
        txtSearch.setFont(UIFonts.TEXT_NORMAL);
        txtSearch.setPreferredSize(new Dimension(220, 32));
        JLabel lblPhongBan = new JLabel("Phòng ban:");
        lblPhongBan.setFont(UIFonts.TEXT_NORMAL);
        lblPhongBan.setForeground(UIColors.TEXT_DARK);
        cboPhongBan = new JComboBox<>(new String[]{"Tất cả phòng ban"});
        cboPhongBan.setFont(UIFonts.TEXT_NORMAL);
        cboPhongBan.setPreferredSize(new Dimension(180, 32));
        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(UIFonts.TEXT_NORMAL);
        lblTrangThai.setForeground(UIColors.TEXT_DARK);
        cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Đang làm việc", "Tạm nghỉ", "Nghỉ việc"
        });
        cboTrangThai.setFont(UIFonts.TEXT_NORMAL);
        cboTrangThai.setPreferredSize(new Dimension(160, 32));
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(Box.createHorizontalStrut(4));
        searchPanel.add(lblPhongBan);
        searchPanel.add(cboPhongBan);
        searchPanel.add(Box.createHorizontalStrut(4));
        searchPanel.add(lblTrangThai);
        searchPanel.add(cboTrangThai);
        searchPanel.add(Box.createHorizontalStrut(8));
        searchWrap.add(searchPanel, BorderLayout.NORTH);
        panel.add(searchWrap, BorderLayout.NORTH);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = PurpleTable.createNonEditableModel(COL_NAMES);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(7).setCellRenderer(new com.hrm.gui.components.StatusCellRenderer());
        int[] widths = {45, 80, 160, 160, 140, 110, 130, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        for (int i = 0; i < COL_NAMES.length; i++) {
            sorter.setSortable(i, false);
        }
        sorter.setSortable(1, true);
        sorter.setSortable(2, true);
        sorter.setComparator(2, UIHelper.vietnameseNameComparator());
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);
        btnThem = UIHelper.createSuccessButton("+ Tạo hồ sơ");
        btnChiTiet = UIHelper.createPrimaryButton("Xem chi tiết");
        btnChiTiet.setEnabled(false);
        panel.add(btnThem);
        panel.add(btnChiTiet);
        JButton btnLamMoi = UIHelper.createDefaultButton("Làm mới");
        btnLamMoi.addActionListener(e -> loadData());
        panel.add(btnLamMoi);

        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        panel.add(Box.createHorizontalStrut(4));
        panel.add(sep);
        panel.add(Box.createHorizontalStrut(4));

        btnNhapExcel = UIHelper.createWarningButton("Nhập Excel");
        btnXuatExcel = UIHelper.createDefaultButton("Xuất Excel");
        btnIn        = UIHelper.createDefaultButton("In danh sách");
        panel.add(btnNhapExcel);
        panel.add(btnXuatExcel);
        panel.add(btnIn);
        return panel;
    }

    private void setupEvents() {
        btnThem.addActionListener(e -> showAddDialog());
        btnChiTiet.addActionListener(e -> showHoSoDialog());
        btnNhapExcel.addActionListener(e -> doImportExcel());
        btnXuatExcel.addActionListener(e -> doExportExcel());
        btnIn.addActionListener(e -> doPrintList(false));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });
        cboTrangThai.addActionListener(e -> applyFilter());
        cboPhongBan.addActionListener(e -> applyFilter());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showHoSoDialog();
                }
            }
        });
    }

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        boolean isAdmin  = sc.hasRole(HRMConstants.ROLE_ADMIN);
        btnThem.setVisible(isAdmin || sc.hasPermission(PermissionCodes.EMPLOYEE_CREATE));
        btnNhapExcel.setVisible(isAdmin || sc.hasPermission(PermissionCodes.EMPLOYEE_IMPORT));
        btnXuatExcel.setVisible(isAdmin || sc.hasPermission(PermissionCodes.EMPLOYEE_EXPORT));
        btnIn.setVisible(isAdmin || sc.hasPermission(PermissionCodes.EMPLOYEE_PRINT));
        updateActionButtons();
    }

    public void loadData() {
        com.hrm.model.TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getMaNV() : null;
        danhSachHienThi = nvService.getAllByScope(userId);
        tableModel.setRowCount(0);
        int stt = 1;
        for (NhanVien nv : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                stt++,
                nv.getMaNhanVien(),
                nv.getHoTen() != null ? nv.getHoTen() : "",
                nv.getTenPhongBanHienTai() != null ? nv.getTenPhongBanHienTai() : "",
                nv.getTenChucVuHienTai() != null ? nv.getTenChucVuHienTai() : "",
                nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(dtf) : "",
                HRMConstants.display(nv.getLoaiHopDong()),
                HRMConstants.display(nv.getTrangThai())
            });
        }
        rebuildDeptCombo();
        applyFilter();
        updateActionButtons();
    }

    private void rebuildDeptCombo() {
        String selected = (String) cboPhongBan.getSelectedItem();
        cboPhongBan.removeActionListener(e -> applyFilter());
        cboPhongBan.removeAllItems();
        cboPhongBan.addItem("Tất cả phòng ban");
        try {
            for (PhongBan d : new PhongBanBUS().getActiveDepartments()) {
                cboPhongBan.addItem(d.getTenPhongBan());
            }
        } catch (Exception ignored) {
        }
        if (selected != null) {
            for (int i = 0; i < cboPhongBan.getItemCount(); i++) {
                if (selected.equals(cboPhongBan.getItemAt(i))) {
                    cboPhongBan.setSelectedIndex(i);
                    break;
                }
            }
        }
        cboPhongBan.addActionListener(e -> applyFilter());
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        String phongBanFilter = (String) cboPhongBan.getSelectedItem();
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String maNV = entry.getStringValue(1).toLowerCase();
                String hoTen = entry.getStringValue(2).toLowerCase();
                boolean matchSearch = searchText.isEmpty()
                        || maNV.contains(searchText) || hoTen.contains(searchText);
                String tenPhongBan = entry.getStringValue(3);
                boolean matchDept = "Tất cả phòng ban".equals(phongBanFilter)
                        || phongBanFilter == null
                        || phongBanFilter.equals(tenPhongBan);
                String trangThai = entry.getStringValue(7);
                boolean matchStatus = true;
                if ("Đang làm việc".equals(trangThaiFilter)) {
                    matchStatus = "Đang làm việc".equals(trangThai);
                } else if ("Tạm nghỉ".equals(trangThaiFilter)) {
                    matchStatus = "Tạm nghỉ".equals(trangThai);
                } else if ("Nghỉ việc".equals(trangThaiFilter)) {
                    matchStatus = "Nghỉ việc".equals(trangThai);
                }
                return matchSearch && matchDept && matchStatus;
            }
        };
        sorter.setRowFilter(rf);
        updateActionButtons();
    }

    private void showAddDialog() {
        EmployeeFormDialog dialog = new EmployeeFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showHoSoDialog() {
        NhanVien selected = getSelectedNhanVien();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một nhân viên để xem chi tiết.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        EmployeeDetailPanel dialog = new EmployeeDetailPanel(
                (Frame) SwingUtilities.getWindowAncestor(this), selected.getMaNhanVien());
        dialog.setVisible(true);
        if (dialog.isDataChanged()) {
            loadData();
        }
    }

    private void updateActionButtons() {
        if (btnChiTiet != null) {
            btnChiTiet.setEnabled(getSelectedNhanVien() != null);
        }
    }

    private NhanVien getSelectedNhanVien() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
            return null;
        }
        String maNV = (String) tableModel.getValueAt(modelRow, 1);
        for (NhanVien nv : danhSachHienThi) {
            if (maNV.equals(nv.getMaNhanVien())) {
                return nv;
            }
        }
        return null;
    }

    // =========================================================
    // Import / Export / Print actions
    // =========================================================

    private void doImportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chon file Excel de nhap");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();

        // Run on background thread to avoid freezing the UI
        new SwingWorker<EmployeeImportExportService.ImportResult, Void>() {
            @Override
            protected EmployeeImportExportService.ImportResult doInBackground() throws Exception {
                return EmployeeImportExportService.importFromExcel(file);
            }
            @Override
            protected void done() {
                try {
                    EmployeeImportExportService.ImportResult result = get();
                    Frame owner = (Frame) SwingUtilities.getWindowAncestor(EmployeeListPanel.this);
                    new EmployeeImportResultDialog(owner, result).setVisible(true);
                    if (result.added > 0 || result.updated > 0) {
                        loadData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeListPanel.this,
                        "Loi nhap du lieu: " + ex.getMessage(),
                        "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doExportExcel() {
        List<NhanVien> list = getFilteredList();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Khong co du lieu de xuat.",
                "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Luu file Excel");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File("danh_sach_nhan_vien_"
            + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
            + ".xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }
        final File target = file;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                EmployeeImportExportService.exportToExcel(list, target);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(EmployeeListPanel.this,
                        "Xuat Excel thanh cong:\n" + target.getAbsolutePath(),
                        "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeListPanel.this,
                        "Loi xuat Excel: " + ex.getMessage(),
                        "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doPrintList(boolean toPdf) {
        List<NhanVien> list = getFilteredList();
        EmployeeImportExportService.printList(this, list, getFilterInfo(), toPdf);
    }

    /** Trả về danh sách nhân viên đang hiển thị sau filter (theo thứ tự trên bảng). */
    private List<NhanVien> getFilteredList() {
        List<NhanVien> result = new ArrayList<>();
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            String maNV = (String) tableModel.getValueAt(modelRow, 1);
            for (NhanVien nv : danhSachHienThi) {
                if (maNV.equals(nv.getMaNhanVien())) {
                    result.add(nv);
                    break;
                }
            }
        }
        return result;
    }

    /** Tạo chuỗi mô tả điều kiện lọc hiện tại để hiển thị trên PDF/bản in. */
    private String getFilterInfo() {
        List<String> parts = new ArrayList<>();
        String search = txtSearch.getText().trim();
        if (!search.isEmpty()) parts.add("Tim kiem: " + search);
        String dept = (String) cboPhongBan.getSelectedItem();
        if (dept != null && !"Tat ca phong ban".equals(dept)
                         && !dept.contains("Tat ca")) parts.add("Phong ban: " + dept);
        String status = (String) cboTrangThai.getSelectedItem();
        if (status != null && !"Tat ca".equals(status)
                           && !status.contains("Tat ca")) parts.add("Trang thai: " + status);
        return String.join(", ", parts);
    }

}
