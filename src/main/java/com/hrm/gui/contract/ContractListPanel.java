package com.hrm.gui.contract;

import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.PurpleTable;
import com.hrm.model.DataScope;
import com.hrm.model.HopDongLaoDong;
import com.hrm.model.TaiKhoan;
import com.hrm.bus.HopDongBUS;
import com.hrm.bus.KetQua;
import com.hrm.bus.XacThucBUS;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Panel danh sách hợp đồng lao động.
 */
public class ContractListPanel extends JPanel {

    private final HopDongBUS hopDongService = HopDongBUS.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PurpleTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JComboBox<String> cboTrangThai;
    private JComboBox<Object> cboNhanVien;
    private PurpleButton btnTao;
    private PurpleButton btnThanhLy;


    private List<HopDongLaoDong> danhSachHienThi = new ArrayList<>();

    private static final String[] COL_NAMES = {
        "Mã HĐ", "Số HĐ", "Mã NV", "Loại HĐ", "Lương cơ sở",
        "Ngày ký", "Ngày hiệu lực", "Ngày hết HLực", "Trạng thái"
    };

    public ContractListPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UIColors.LIGHT_GRAY_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildNorthPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);

        setupPermissions();
        setupEvents();
        loadData();
    }

    // ============================
    // Build sections
    // ============================

    private JPanel buildNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        // Tiêu đề
        JLabel lblTitle = new JLabel("QUẢN LÝ HỢP ĐỒNG LAO ĐỘNG");
        lblTitle.setFont(com.hrm.util.UIFonts.HEADER_H2);
        lblTitle.setForeground(UIColors.PRIMARY_PURPLE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);

        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblTrangThai.setForeground(UIColors.TEXT_DARK);

        cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Hiệu lực", "Hết hiệu lực", "Thanh lý"
        });
        cboTrangThai.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboTrangThai.setPreferredSize(new Dimension(160, 32));

        JLabel lblNhanVien = new JLabel("Nhân viên:");
        lblNhanVien.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        lblNhanVien.setForeground(UIColors.TEXT_DARK);

        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(com.hrm.util.UIFonts.TEXT_MEDIUM);
        cboNhanVien.addItem("Tất cả");

        DataScope contractScope = XacThucBUS.getInstance().getScopeForAction("CONTRACT_VIEW");
        boolean isManager = contractScope == DataScope.ALL
                         || contractScope == DataScope.DEPT
                         || contractScope == DataScope.TEAM;

        if (isManager) {
            com.hrm.model.TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
            List<com.hrm.model.NhanVien> dsNV = com.hrm.bus.NhanVienBUS.getInstance().getAllByActionScope("EMPLOYEE_VIEW", currentUser != null ? currentUser.getNhanVienId() : null);
            for (com.hrm.model.NhanVien nv : dsNV) {
                cboNhanVien.addItem(nv);
            }
        }

        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof com.hrm.model.NhanVien) {
                    com.hrm.model.NhanVien nv = (com.hrm.model.NhanVien) value;
                    setText("[" + nv.getMaNhanVien() + "] " + nv.getHoTen());
                } else if (value != null) {
                    setText(value.toString());
                }
                return this;
            }
        });

        if (isManager) {
            filterPanel.add(lblNhanVien);
            filterPanel.add(cboNhanVien);
        }
        filterPanel.add(lblTrangThai);
        filterPanel.add(cboTrangThai);

        panel.add(filterPanel, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        tableModel = PurpleTable.createNonEditableModel(COL_NAMES);
        table = new PurpleTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());

        // Chiều rộng cột
        int[] widths = {60, 120, 70, 130, 120, 100, 100, 110, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(0, Comparator.comparingInt(a -> (Integer) a));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIColors.BORDER_GRAY));
        return scroll;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setOpaque(false);

        btnTao = new PurpleButton("+ Tạo hợp đồng");
        btnThanhLy = PurpleButton.warning("Thanh lý");
        btnThanhLy.setToolTipText("Thanh lý: Hai bên thỏa thuận chấm dứt hợp đồng trước thời hạn. Hợp đồng vẫn được lưu với trạng thái 'Thanh lý'.");
        JButton btnLamMoi = new JButton("Lam moi");
        btnLamMoi.addActionListener(e -> loadData());
        panel.add(btnTao);
        panel.add(btnThanhLy);
        panel.add(btnLamMoi);

        // Ghi chú giải thích các trạng thái
        JLabel lblNote = new JLabel(
                "<html><i>💡 <b>Thanh lý hợp đồng:</b> Hai bên thỏa thuận chấm dứt hợp đồng lao động trước thời hạn theo quy định pháp luật.</i></html>");
        lblNote.setForeground(com.hrm.util.UIColors.TEXT_GRAY);
        lblNote.setFont(com.hrm.util.UIFonts.TEXT_SMALL);
        panel.add(lblNote);

        return panel;
    }

    // ============================
    // Events
    // ============================

    private void setupEvents() {
        btnTao.addActionListener(e -> showCreateDialog());
        btnThanhLy.addActionListener(e -> thanhLyHopDong());

        cboTrangThai.addActionListener(e -> applyFilter());
        cboNhanVien.addActionListener(e -> applyFilter());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });
    }

    // ============================
    // Permissions
    // ============================

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();
        boolean canCreate = sc.coQuyen("CONTRACT_CREATE");
        boolean canUpdate = sc.coQuyen("CONTRACT_MANAGE");

        btnTao.setVisible(canCreate);
        btnThanhLy.setVisible(canUpdate);
    }

    // ============================
    // Data loading
    // ============================

    public void loadData() {
        DataScope scope = XacThucBUS.getInstance().getScopeForAction("CONTRACT_VIEW");
        TaiKhoan user = SessionContext.getInstance().getCurrentUser();
        String myMaNV = user != null ? user.getNhanVienId() : null;
        if (scope == DataScope.SELF) {
            danhSachHienThi = (myMaNV != null && !myMaNV.isEmpty())
                    ? hopDongService.getByMaNV(myMaNV)
                    : new java.util.ArrayList<>();
        } else if (scope == DataScope.ALL) {
            danhSachHienThi = hopDongService.getAll();
        } else {
            // DEPT hoac TEAM: chi load hop dong cua NV trong pham vi
            java.util.Set<String> maNVSet = com.hrm.bus.NhanVienBUS.getInstance()
                    .getAllByActionScope("CONTRACT_VIEW", myMaNV).stream()
                    .map(com.hrm.model.NhanVien::getMaNhanVien)
                    .collect(java.util.stream.Collectors.toSet());
            danhSachHienThi = hopDongService.getAll().stream()
                    .filter(hd -> maNVSet.contains(hd.getMaNV()))
                    .collect(java.util.stream.Collectors.toList());
        }
        tableModel.setRowCount(0);

        for (HopDongLaoDong hd : danhSachHienThi) {
            tableModel.addRow(new Object[]{
                hd.getMaHopDong(),
                hd.getSoHopDong(),
                hd.getMaNV(),
                hd.getLoaiHopDongDisplay(),
                String.format("%,d đ", hd.getLuongCoSo()),
                hd.getNgayKy() != null ? hd.getNgayKy().format(dtf) : "",
                hd.getNgayHieuLuc() != null ? hd.getNgayHieuLuc().format(dtf) : "",
                hd.getNgayHetHieuLuc() != null ? hd.getNgayHetHieuLuc().format(dtf) : "Không xác định",
                hd.getTrangThaiDisplay()
            });
        }

        applyFilter();
    }

    private void applyFilter() {
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        
        String tempMaNV = null;
        if (cboNhanVien != null && cboNhanVien.getSelectedItem() instanceof com.hrm.model.NhanVien) {
            tempMaNV = ((com.hrm.model.NhanVien) cboNhanVien.getSelectedItem()).getMaNhanVien();
        }
        final String filterMaNV = tempMaNV;

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Check Employee filter
                if (filterMaNV != null) {
                    Object nvIdObj = entry.getValue(2);
                    if (nvIdObj != null && !nvIdObj.toString().equals(filterMaNV)) return false;
                }

                // Check Status filter
                if (!"Tất cả".equals(trangThaiFilter)) {
                    String trangThai = entry.getStringValue(8);
                    if ("Hiệu lực".equals(trangThaiFilter) && !"Hiệu lực".equals(trangThai)) return false;
                    if ("Hết hiệu lực".equals(trangThaiFilter) && !"Hết hạn".equals(trangThai)) return false;
                    if ("Thanh lý".equals(trangThaiFilter) && !"Thanh lý".equals(trangThai)) return false;
                }
                
                return true;
            }
        };

        sorter.setRowFilter(rf);
    }

    // ============================
    // Actions
    // ============================

    private void showCreateDialog() {
        ContractFormDialog dialog = new ContractFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showDetailDialog() {
        HopDongLaoDong selected = getSelectedHopDong();
        if (selected == null) return;
        ContractFormDialog dialog = new ContractFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void thanhLyHopDong() {
        HopDongLaoDong selected = getSelectedHopDong();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon mot hop dong de thanh ly.",
                    "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String confirmMsg = "Xac nhan thanh ly hop dong so '" + selected.getSoHopDong() + "'?";
        if (hopDongService.isHopDongHetHan(selected.getMaHopDong())) {
            confirmMsg = "Hop dong nay DA HET HAN. Ban co muon thanh ly khong?\n"
                    + "So HD: " + selected.getSoHopDong() + "\n"
                    + "Luu y: Cac bo nhiem hieu luc se bi ket thuc.";
        }
        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg,
                "Xac nhan thanh ly", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        KetQua<Void> result = hopDongService.thanhLyHopDong(selected.getMaHopDong());

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }


    // ============================
    // Helper: get selected HopDong
    // ============================

    private HopDongLaoDong getSelectedHopDong() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= danhSachHienThi.size()) return null;
        int maHopDong = (int) tableModel.getValueAt(modelRow, 0);
        for (HopDongLaoDong hd : danhSachHienThi) {
            if (hd.getMaHopDong() == maHopDong) return hd;
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
            setToolTipText(null);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? com.hrm.util.UIColors.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);

                // Tô màu + tooltip cột trạng thái (cột 8)
                if (col == 8 && value != null) {
                    String val = value.toString();
                    if ("Hiệu lực".equals(val)) {
                        c.setForeground(UIColors.SUCCESS_GREEN);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                        setToolTipText("Hop dong dang co hieu luc");
                    } else if ("Hết hạn".equals(val)) {
                        c.setForeground(com.hrm.util.UIColors.WARNING_TEXT_AMBER);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                        setToolTipText("Hop dong da het thoi han");
                    } else if ("Thanh lý".equals(val)) {
                        c.setForeground(UIColors.DANGER_RED);
                        ((JLabel) c).setFont(com.hrm.util.UIFonts.BOLD_SMALL);
                        setToolTipText("Hop dong da duoc thanh ly");
                    }
                }
            }
            return c;
        }
    }
}
