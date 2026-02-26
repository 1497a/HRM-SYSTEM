package com.hrm.gui.employee;

import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.service.NhanVienService;
import com.hrm.service.ServiceResult;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel danh sách hồ sơ nhân viên.
 */
public class EmployeeListPanel extends JPanel {

    private final NhanVienService nvService = NhanVienService.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtSearch;
    private JComboBox<String> cboTrangThai;

    private PurpleButton btnThem;
    private PurpleButton btnXem;
    private PurpleButton btnXemHoSo;
    private PurpleButton btnDoiTrangThai;

    // Danh sách đang hiển thị (để lấy đối tượng khi chọn dòng)
    private List<NhanVien> danhSachHienThi = new ArrayList<>();

    private static final String[] COL_NAMES = {
        "STT", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ",
        "Ngày vào làm", "Loại HĐ", "Trạng thái"
    };

    public EmployeeListPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UIColors.LIGHT_GRAY_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildNorthPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);

        setupPermissions();
        setupEvents();
        refreshTable();
    }

    // ============================
    // Build sections
    // ============================

    private JPanel buildNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        // Tiêu đề
        JLabel lblTitle = new JLabel("HO SO NHAN VIEN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Search + filter
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tim kiem:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSearch.setForeground(UIColors.TEXT_DARK);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(220, 32));

        JLabel lblTrangThai = new JLabel("Trang thai:");
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTrangThai.setForeground(UIColors.TEXT_DARK);

        cboTrangThai = new JComboBox<>(new String[]{
            "Tat ca", "Dang lam viec", "Tam nghi", "Nghi viec"
        });
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboTrangThai.setPreferredSize(new Dimension(160, 32));

        PurpleButton btnTimKiem = new PurpleButton("Tim kiem");
        btnTimKiem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTimKiem.addActionListener(e -> applyFilter());

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(Box.createHorizontalStrut(8));
        searchPanel.add(lblTrangThai);
        searchPanel.add(cboTrangThai);
        searchPanel.add(Box.createHorizontalStrut(8));
        searchPanel.add(btnTimKiem);

        panel.add(searchPanel, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = PurpleTable.createNonEditableModel(COL_NAMES);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());

        // Chiều rộng cột
        int[] widths = {45, 80, 160, 160, 140, 110, 130, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);

        btnThem = new PurpleButton("+ Them moi");
        btnXem = PurpleButton.info("Xem / Sua");
        btnXemHoSo = PurpleButton.info("Xem ho so");
        btnDoiTrangThai = PurpleButton.warning("Doi trang thai");

        panel.add(btnThem);
        panel.add(btnXem);
        panel.add(btnXemHoSo);
        panel.add(btnDoiTrangThai);

        return panel;
    }

    // ============================
    // Events
    // ============================

    private void setupEvents() {
        btnThem.addActionListener(e -> showAddDialog());
        btnXem.addActionListener(e -> showEditDialog());
        btnXemHoSo.addActionListener(e -> showHoSoDialog());
        btnDoiTrangThai.addActionListener(e -> showDoiTrangThaiDialog());

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });

        cboTrangThai.addActionListener(e -> applyFilter());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditDialog();
                }
            }
        });
    }

    // ============================
    // Permissions
    // ============================

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        boolean canCreate = sc.hasRole("ADMIN") || sc.hasPermission("EMPLOYEE_CREATE");
        boolean canUpdate = sc.hasRole("ADMIN") || sc.hasPermission("EMPLOYEE_UPDATE");

        btnThem.setVisible(canCreate);
        btnDoiTrangThai.setVisible(canUpdate);
    }

    // ============================
    // Data loading
    // ============================

    public void refreshTable() {
        danhSachHienThi = nvService.getAll();
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
                nv.getLoaiHopDongDisplay(),
                nv.getTrangThaiDisplay()
            });
        }
        applyFilter();
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Lọc theo tên hoặc mã (cột 1, 2)
                String maNV = entry.getStringValue(1).toLowerCase();
                String hoTen = entry.getStringValue(2).toLowerCase();
                boolean matchSearch = searchText.isEmpty()
                        || maNV.contains(searchText) || hoTen.contains(searchText);

                // Lọc theo trạng thái (cột 7)
                String trangThai = entry.getStringValue(7);
                boolean matchStatus = true;
                if ("Dang lam viec".equals(trangThaiFilter)) {
                    matchStatus = "Dang lam viec".equals(trangThai);
                } else if ("Tam nghi".equals(trangThaiFilter)) {
                    matchStatus = "Tam nghi".equals(trangThai);
                } else if ("Nghi viec".equals(trangThaiFilter)) {
                    matchStatus = "Nghi viec".equals(trangThai);
                }
                return matchSearch && matchStatus;
            }
        };

        sorter.setRowFilter(rf);
    }

    // ============================
    // Actions
    // ============================

    private void showAddDialog() {
        EmployeeFormDialog dialog = new EmployeeFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTable();
        }
    }

    private void showEditDialog() {
        NhanVien selected = getSelectedNhanVien();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon mot nhan vien de xem/sua.",
                    "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ThongTinCaNhan ttcn = nvService.getThongTinCaNhan(selected.getId());
        EmployeeFormDialog dialog = new EmployeeFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selected, ttcn);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTable();
        }
    }

    private void showHoSoDialog() {
        NhanVien selected = getSelectedNhanVien();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon mot nhan vien de xem ho so.",
                    "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        EmployeeDetailPanel dialog = new EmployeeDetailPanel(
                (Frame) SwingUtilities.getWindowAncestor(this), selected.getId());
        dialog.setVisible(true);
    }

    private void showDoiTrangThaiDialog() {
        NhanVien selected = getSelectedNhanVien();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon mot nhan vien.",
                    "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Xây dựng danh sách trạng thái hợp lệ
        String hienTai = selected.getTrangThai();
        List<String> optionList = new ArrayList<>();
        if ("dang_lam_viec".equals(hienTai)) {
            optionList.add("Tam nghi");
            optionList.add("Nghi viec");
        } else if ("tam_nghi".equals(hienTai)) {
            optionList.add("Dang lam viec");
            optionList.add("Nghi viec");
        }

        if (optionList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nhan vien da nghi viec, khong the doi trang thai.",
                    "Khong the thay doi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> cboMoi = new JComboBox<>(optionList.toArray(new String[0]));
        cboMoi.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextField txtLyDo = new JTextField(30);
        txtLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        Object[] message = {
            "Trang thai moi:", cboMoi,
            "Ly do:", txtLyDo
        };

        int choice = JOptionPane.showConfirmDialog(this, message,
                "Doi trang thai nhan vien: " + selected.toString(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) return;

        String trangThaiMoiVN = (String) cboMoi.getSelectedItem();
        String trangThaiMoi;
        if ("Tam nghi".equals(trangThaiMoiVN)) {
            trangThaiMoi = "tam_nghi";
        } else if ("Nghi viec".equals(trangThaiMoiVN)) {
            trangThaiMoi = "nghi_viec";
        } else {
            trangThaiMoi = "dang_lam_viec";
        }

        ServiceResult<NhanVien> result = nvService.capNhatTrangThai(
                selected.getId(), trangThaiMoi, txtLyDo.getText().trim());

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================
    // Helper: get selected NhanVien
    // ============================

    private NhanVien getSelectedNhanVien() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= danhSachHienThi.size()) return null;
        // Tìm đúng đối tượng dựa vào mã NV trong model
        String maNV = (String) tableModel.getValueAt(modelRow, 1);
        for (NhanVien nv : danhSachHienThi) {
            if (maNV.equals(nv.getMaNhanVien())) return nv;
        }
        return null;
    }

    // ============================
    // Custom renderer for status coloring
    // ============================

    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);

                // Tô màu cột trạng thái (cột 7)
                if (col == 7 && value != null) {
                    String val = value.toString();
                    if (val.contains("lam viec") || val.contains("Dang")) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                    } else if (val.contains("Tam nghi")) {
                        c.setForeground(new Color(230, 120, 0));
                    } else if (val.contains("Nghi viec")) {
                        c.setForeground(UIColors.DANGER_RED);
                    }
                    ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
            }
            return c;
        }
    }
}
