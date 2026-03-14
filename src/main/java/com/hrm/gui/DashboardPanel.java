package com.hrm.gui;

import com.hrm.bus.XacThucBUS;
import com.hrm.bus.LuongBUS;
import com.hrm.bus.NghiPhepBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.TuyenDungBUS;
import com.hrm.gui.components.RoundedPanel;
import com.hrm.model.DataScope;
import com.hrm.model.DonXinNghiPhep;
import com.hrm.model.SoDungPhep;
import com.hrm.model.TaiKhoan;
import com.hrm.model.BangLuong;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard panel - hien thi tong quan he thong hoac thong tin ca nhan
 * tuy theo pham vi quyen han cua nguoi dung hien tai.
 */
class DashboardPanel extends JPanel {

    private final XacThucBUS authService;

    DashboardPanel(XacThucBUS authService) {
        this.authService = authService;
        setLayout(new BorderLayout());
        setBackground(UIColors.LIGHT_GRAY_BG);
        DataScope scope = authService.getScopeForAction("EMPLOYEE_VIEW");
        JPanel inner = (scope == DataScope.ALL || scope == DataScope.DEPT || scope == DataScope.TEAM)
                ? buildManagerDashboard(scope)
                : buildPersonalDashboard();
        add(inner, BorderLayout.CENTER);
    }

    private JPanel buildManagerDashboard(DataScope scope) {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String maNV = currentUser != null ? currentUser.getNhanVienId() : null;

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UIColors.LIGHT_GRAY_BG);
        root.setBorder(new EmptyBorder(25, 25, 25, 25));
        root.add(createTitle("Tổng quan hệ thống"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        List<JPanel> cards = new ArrayList<>();
        addManagerStatCards(cards, scope, maNV);

        if (!cards.isEmpty()) {
            int cols = Math.min(cards.size(), 3);
            int rows = (int) Math.ceil((double) cards.size() / cols);
            JPanel grid = new JPanel(new GridLayout(rows, cols, 18, 18));
            grid.setOpaque(false);
            for (JPanel card : cards) grid.add(card);
            body.add(grid);
            body.add(Box.createVerticalStrut(28));
        }

        root.add(scroll(body), BorderLayout.CENTER);
        return root;
    }

    private void addManagerStatCards(List<JPanel> cards, DataScope scope, String maNV) {
        if (authService.getScopeForAction("EMPLOYEE_VIEW") != DataScope.NONE) {
            try {
                long count = NhanVienBUS.getInstance().getDangLamViec().size();
                cards.add(RoundedPanel.createStatCard("NV đang làm việc", String.valueOf(count), UIColors.PRIMARY_PURPLE));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("NV đang làm việc", "—", UIColors.PRIMARY_PURPLE));
            }
        }
        if (authService.getScopeForAction("LEAVE_VIEW") != DataScope.NONE) {
            try {
                long pending = NghiPhepBUS.getInstance().getPendingRequestsByScope(maNV).size();
                cards.add(RoundedPanel.createStatCard("Đơn nghỉ chờ duyệt", String.valueOf(pending), UIColors.DANGER_RED));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Đơn nghỉ chờ duyệt", "—", UIColors.DANGER_RED));
            }
        }
        if (authService.getScopeForAction("PAYROLL_VIEW") != DataScope.NONE) {
            try {
                LocalDate today = LocalDate.now();
                BangLuong payroll = LuongBUS.getInstance().getBangLuongTheoKy(today.getMonthValue(), today.getYear());
                String status = payroll != null ? payroll.getTrangThai().getDisplayName() : "Chưa tạo";
                cards.add(RoundedPanel.createStatCard("Lương tháng " + today.getMonthValue(), status, UIColors.SUCCESS_GREEN));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Lương tháng này", "—", UIColors.SUCCESS_GREEN));
            }
        }
        if (authService.getScopeForAction("RECRUITMENT_VIEW") != DataScope.NONE) {
            try {
                long opening = TuyenDungBUS.getInstance().getAllTinTuyenDung().stream()
                        .filter(t -> "dang_tuyen".equals(t.getTrangThai())).count();
                cards.add(RoundedPanel.createStatCard("Tuyển dụng đang mở", String.valueOf(opening), UIColors.WARNING_YELLOW));
            } catch (Exception ignored) {
                cards.add(RoundedPanel.createStatCard("Tuyển dụng đang mở", "—", UIColors.WARNING_YELLOW));
            }
        }
    }

    private JPanel buildPersonalDashboard() {
        TaiKhoan currentUser = SessionContext.getInstance().getCurrentUser();
        String maNV = currentUser != null ? currentUser.getNhanVienId() : null;

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UIColors.LIGHT_GRAY_BG);
        root.setBorder(new EmptyBorder(25, 25, 25, 25));
        root.add(createTitle("Thông tin của tôi"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 18, 18));
        cardsGrid.setOpaque(false);
        addPersonalStatCards(cardsGrid, maNV);
        body.add(cardsGrid);
        body.add(Box.createVerticalStrut(28));

        root.add(scroll(body), BorderLayout.CENTER);
        return root;
    }

    private void addPersonalStatCards(JPanel cardsGrid, String maNV) {
        try {
            SoDungPhep phepNam = null;
            for (SoDungPhep p : NghiPhepBUS.getInstance().getBalances(maNV)) {
                if ("PHEP_NAM".equals(p.getLeaveTypeCode()) || "AL".equals(p.getLeaveTypeCode())) {
                    phepNam = p;
                    break;
                }
            }
            double remaining = phepNam != null ? phepNam.getRemainingDays() : 0;
            cardsGrid.add(RoundedPanel.createStatCard("Phép năm còn lại", (int) remaining + " ngày", UIColors.PRIMARY_PURPLE));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Phép năm còn lại", "—", UIColors.PRIMARY_PURPLE));
        }
        try {
            long pending = maNV == null ? 0 : NghiPhepBUS.getInstance().getMyRequests(maNV).stream()
                    .filter(d -> DonXinNghiPhep.TrangThai.CHO_DUYET.equals(d.getTrangThai())).count();
            cardsGrid.add(RoundedPanel.createStatCard("Đơn đang chờ duyệt", String.valueOf(pending), UIColors.WARNING_YELLOW));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Đơn đang chờ duyệt", "—", UIColors.WARNING_YELLOW));
        }
        try {
            String luongText = "Chưa có";
            if (maNV != null) {
                List<BangLuong> payrolls = LuongBUS.getInstance().getAll();
                for (int i = payrolls.size() - 1; i >= 0; i--) {
                    com.hrm.model.ChiTietLuong detail = LuongBUS.getInstance()
                            .getChiTietCaNhan(payrolls.get(i).getMaBL(), maNV);
                    if (detail != null) {
                        luongText = NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                                .format((long) detail.getLuongThucNhan()) + " đ";
                        break;
                    }
                }
            }
            cardsGrid.add(RoundedPanel.createStatCard("Lương gần nhất", luongText, UIColors.SUCCESS_GREEN));
        } catch (Exception ignored) {
            cardsGrid.add(RoundedPanel.createStatCard("Lương gần nhất", "—", UIColors.SUCCESS_GREEN));
        }
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIFonts.HEADER_H1);
        label.setForeground(UIColors.TEXT_DARK);
        return label;
    }

    private JScrollPane scroll(JComponent component) {
        JScrollPane sp = new JScrollPane(component);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        return sp;
    }
}
