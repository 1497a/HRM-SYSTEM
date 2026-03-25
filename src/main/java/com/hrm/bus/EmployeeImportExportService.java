package com.hrm.bus;

import com.hrm.dao.ThongTinCaNhanDAO;
import com.hrm.model.NhanVien;
import com.hrm.model.ThongTinCaNhan;
import com.hrm.util.HRMConstants;
import com.hrm.util.SimpleXlsx;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý import/export Excel và in ấn danh sách nhân viên.
 * Không phụ thuộc thư viện bên ngoài: dùng SimpleXlsx (pure Java) cho Excel,
 * và Java PrinterJob cho PDF/in.
 */
public final class EmployeeImportExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Tiêu đề cột khi xuất / mẫu nhập. */
    public static final String[] EXPORT_HEADERS = {
        "Mã NV", "Họ tên", "Ngày sinh", "Giới tính", "CCCD",
        "Điện thoại", "Email", "Địa chỉ", "Quê quán",
        "Trình độ học vấn", "Kinh nghiệm",
        "Ngày vào làm", "Trạng thái", "Ghi chú"
    };

    private EmployeeImportExportService() {}

    // =========================================================
    // EXPORT EXCEL
    // =========================================================

    /**
     * Xuất danh sách nhân viên đang hiển thị ra file .xlsx.
     *
     * @param list danh sách sau filter
     * @param file file đích (phải có đuôi .xlsx)
     */
    public static void exportToExcel(List<NhanVien> list, File file) throws IOException {
        ThongTinCaNhanDAO ttcnDao = ThongTinCaNhanDAO.getInstance();
        List<String[]> rows = new ArrayList<>(list.size());
        for (NhanVien nv : list) {
            ThongTinCaNhan ttcn = ttcnDao.findByMaNV(nv.getMaNhanVien());
            rows.add(toExportRow(nv, ttcn));
        }
        SimpleXlsx.write(file, EXPORT_HEADERS, rows);
    }

    private static String[] toExportRow(NhanVien nv, ThongTinCaNhan ttcn) {
        return new String[]{
            safe(nv.getMaNhanVien()),
            ttcn != null ? safe(ttcn.getHoTen())       : safe(nv.getHoTen()),
            ttcn != null && ttcn.getNgaySinh() != null ? ttcn.getNgaySinh().format(DATE_FMT) : "",
            ttcn != null ? HRMConstants.display(ttcn.getGioiTinh()) : "",
            ttcn != null ? safe(ttcn.getCccd()) : "",
            ttcn != null ? safe(ttcn.getDienThoai()) : "",
            ttcn != null ? safe(ttcn.getEmail()) : "",
            ttcn != null ? safe(ttcn.getDiaChi()) : "",
            ttcn != null ? safe(ttcn.getQueQuan()) : "",
            ttcn != null ? safe(ttcn.getTrinhDoHocVan()) : "",
            ttcn != null ? safe(ttcn.getKinhNghiem()) : "",
            nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(DATE_FMT) : "",
            HRMConstants.display(nv.getTrangThai()),
            safe(nv.getGhiChu())
        };
    }

    // =========================================================
    // IMPORT EXCEL
    // =========================================================

    /**
     * Nhập nhân viên từ file .xlsx.
     * <ul>
     *   <li>Nếu mã NV chưa tồn tại: thêm mới (gọi NhanVienBUS.taoHoSo).</li>
     *   <li>Nếu mã NV đã tồn tại: cập nhật ThongTinCaNhan (gọi NhanVienBUS.capNhatThongTinCaNhan).</li>
     * </ul>
     */
    public static ImportResult importFromExcel(File file) throws IOException {
        List<String[]> rows = SimpleXlsx.read(file);
        if (rows.isEmpty()) {
            return new ImportResult(0, 0, 0, 0, List.of());
        }
        // Skip header row (index 0)
        NhanVienBUS nvBUS = NhanVienBUS.getInstance();
        int added = 0, updated = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int lineNum = i + 1; // 1-based for display (row 1 = header)

            // Skip fully blank rows
            if (isBlankRow(row)) continue;

            String maNV = col(row, 0).trim();
            if (maNV.isEmpty()) {
                errors.add("Dòng " + lineNum + ": Mã NV không được để trống.");
                failed++;
                continue;
            }

            // Build ThongTinCaNhan
            ThongTinCaNhan ttcn = new ThongTinCaNhan();
            ttcn.setMaNV(maNV);
            ttcn.setHoTen(col(row, 1).trim());
            ttcn.setNgaySinh(parseDate(col(row, 2)));
            ttcn.setGioiTinh(parseGioiTinh(col(row, 3)));
            ttcn.setCccd(col(row, 4).trim());
            ttcn.setDienThoai(col(row, 5).trim());
            ttcn.setEmail(col(row, 6).trim());
            ttcn.setDiaChi(col(row, 7).trim());
            ttcn.setQueQuan(col(row, 8).trim());
            ttcn.setTrinhDoHocVan(col(row, 9).trim());
            ttcn.setKinhNghiem(col(row, 10).trim());

            // Build NhanVien
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(maNV);
            nv.setNgayVaoLam(parseDate(col(row, 11)));
            nv.setLoaiHopDong(null);
            nv.setTrangThai(parseTrangThai(col(row, 12)));
            nv.setGhiChu(col(row, 13).trim());

            // Check existence
            NhanVien existing = nvBUS.getByMaNhanVien(maNV);

            if (existing == null) {
                // Thêm mới
                KetQua<NhanVien> kq = nvBUS.taoHoSo(nv, ttcn);
                if (kq.isSuccess()) {
                    added++;
                } else {
                    errors.add("Dòng " + lineNum + " (" + maNV + "): " + kq.getMessage());
                    failed++;
                }
            } else {
                // Chỉ cập nhật thông tin cá nhân cho nhân viên đã tồn tại.
                ThongTinCaNhan current = nvBUS.getThongTinCaNhan(maNV);
                if (sameThongTinCaNhan(current, ttcn)) {
                    skipped++;
                    errors.add("Dòng " + lineNum + " (" + maNV + "): Bỏ qua vì không có thay đổi.");
                    continue;
                }

                // Cập nhật ThongTinCaNhan
                KetQua<ThongTinCaNhan> kq = nvBUS.capNhatThongTinCaNhan(ttcn);
                if (kq.isSuccess()) {
                    updated++;
                } else {
                    errors.add("Dòng " + lineNum + " (" + maNV + "): " + kq.getMessage());
                    failed++;
                }
            }
        }
        return new ImportResult(added, updated, skipped, failed, errors);
    }

    // =========================================================
    // PRINT / PDF
    // =========================================================

    /**
     * In danh sách nhân viên.
     *
     * @param parent parent component (để căn giữa dialog)
     * @param list danh sách sau filter
     * @param filterInfo chuỗi mô tả điều kiện lọc (có thể rỗng)
     * @param toPdf true -> cố gắng chọn máy in PDF; false -> máy in mặc định
     */
    public static void printList(Component parent, List<NhanVien> list,
                                 String filterInfo, boolean toPdf) {
        if (list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "Không có dữ liệu để in.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setJobName("Danh sách nhân viên");

        if (toPdf) {
            trySelectPdfPrinter(pj);
        }

        PageFormat pf = pj.defaultPage();
        pf.setOrientation(PageFormat.LANDSCAPE);

        EmployeePrintable printable = new EmployeePrintable(list, filterInfo);
        pj.setPrintable(printable, pf);

        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(parent,
                    "Lỗi in: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void trySelectPdfPrinter(PrinterJob pj) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService ps : services) {
            String name = ps.getName().toLowerCase();
            if (name.contains("pdf") || name.contains("xps")) {
                try {
                    pj.setPrintService(ps);
                    return;
                } catch (PrinterException ignored) {}
            }
        }
    }

    // =========================================================
    // PARSE HELPERS
    // =========================================================

    private static String col(String[] row, int idx) {
        return (idx < row.length && row[idx] != null) ? row[idx] : "";
    }

    private static boolean isBlankRow(String[] row) {
        for (String v : row) if (v != null && !v.trim().isEmpty()) return false;
        return true;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String parseGioiTinh(String s) {
        if (s == null) return HRMConstants.GIOI_TINH_NAM;
        switch (s.trim().toLowerCase()) {
            case "nu": case "nữ": return HRMConstants.GIOI_TINH_NU;
            case "khac": case "khác": return HRMConstants.GIOI_TINH_KHAC;
            default: return HRMConstants.GIOI_TINH_NAM;
        }
    }

    private static String parseTrangThai(String s) {
        if (s == null || s.trim().isEmpty()) return HRMConstants.TRANG_THAI_DANG_LAM_VIEC;
        String v = s.trim();
        if (v.equalsIgnoreCase("tam_nghi")
                || v.contains("Tạm nghỉ") || v.equalsIgnoreCase("tam nghi"))
            return HRMConstants.TRANG_THAI_TAM_NGHI;
        if (v.equalsIgnoreCase("nghi_viec")
                || v.contains("Nghỉ việc") || v.equalsIgnoreCase("nghi viec"))
            return HRMConstants.TRANG_THAI_NGHI_VIEC;
        return HRMConstants.TRANG_THAI_DANG_LAM_VIEC;
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static boolean sameThongTinCaNhan(ThongTinCaNhan a, ThongTinCaNhan b) {
        if (a == null || b == null) return false;
        return same(a.getHoTen(), b.getHoTen())
            && same(a.getNgaySinh(), b.getNgaySinh())
            && same(a.getGioiTinh(), b.getGioiTinh())
            && same(a.getCccd(), b.getCccd())
            && same(a.getDienThoai(), b.getDienThoai())
            && same(a.getEmail(), b.getEmail())
            && same(a.getDiaChi(), b.getDiaChi())
            && same(a.getQueQuan(), b.getQueQuan())
            && same(a.getTrinhDoHocVan(), b.getTrinhDoHocVan())
            && same(a.getKinhNghiem(), b.getKinhNghiem());
    }

    private static boolean same(String a, String b) {
        return safe(a).trim().equals(safe(b).trim());
    }

    private static boolean same(LocalDate a, LocalDate b) {
        return a == null ? b == null : a.equals(b);
    }

    // =========================================================
    // RESULT MODEL
    // =========================================================

    public static final class ImportResult {
        public final int added;
        public final int updated;
        public final int skipped;
        public final int failed;
        public final List<String> errors;

        public ImportResult(int added, int updated, int skipped, int failed, List<String> errors) {
            this.added = added;
            this.updated = updated;
            this.skipped = skipped;
            this.failed = failed;
            this.errors = errors;
        }

        public int total() { return added + updated + skipped + failed; }
    }

    // =========================================================
    // PRINTABLE
    // =========================================================

    private static final class EmployeePrintable implements Printable {
        private static final String[] COLS = {"STT", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Ngày vào làm", "Loại HĐ", "Trạng thái"};
        private static final int[] WEIGHTS = {25, 55, 120, 110, 100, 75, 95, 80};
        private static final int COL_H = 18, ROW_H = 15, FOOTER_H = 14;
        private static final Font F_TITLE = new Font("SansSerif", Font.BOLD, 14);
        private static final Font F_COL = new Font("SansSerif", Font.BOLD, 9);
        private static final Font F_DATA = new Font("SansSerif", Font.PLAIN, 9);
        private static final Font F_FOOTER = new Font("SansSerif", Font.ITALIC, 8);

        private final List<NhanVien> list;
        private final String filterInfo;
        private int cachedPageCount = -1;

        EmployeePrintable(List<NhanVien> list, String filterInfo) {
            this.list = list;
            this.filterInfo = filterInfo != null ? filterInfo : "";
        }

        private int headerHeight() {
            // Title (20) + date (14) + filter line if any (14) + count (14) + gap (4)
            return 20 + 14 + (!filterInfo.isEmpty() ? 14 : 0) + 14 + 4;
        }

        private int rowsPerPage(PageFormat pf) {
            double usable = pf.getImageableHeight() - headerHeight() - COL_H - FOOTER_H - 4;
            return Math.max(1, (int) (usable / ROW_H));
        }

        private int pageCount(PageFormat pf) {
            if (cachedPageCount < 0) {
                int rpp = rowsPerPage(pf);
                cachedPageCount = Math.max(1, (int) Math.ceil((double) list.size() / rpp));
            }
            return cachedPageCount;
        }

        @Override
        public int print(Graphics graphics, PageFormat pf, int pageIndex) throws PrinterException {
            if (pageIndex >= pageCount(pf)) return NO_SUCH_PAGE;

            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.translate(pf.getImageableX(), pf.getImageableY());

            int pw = (int) pf.getImageableWidth();
            int ph = (int) pf.getImageableHeight();

            // Compute column widths proportional to page width.
            int totalW = 0;
            for (int w : WEIGHTS) totalW += w;
            int[] cw = new int[WEIGHTS.length];
            for (int i = 0; i < WEIGHTS.length; i++) cw[i] = WEIGHTS[i] * pw / totalW;

            int y = 0;

            // Header block (first page only).
            if (pageIndex == 0) {
                g.setFont(F_TITLE);
                String title = "DANH SÁCH NHÂN VIÊN";
                int tw = g.getFontMetrics().stringWidth(title);
                g.drawString(title, (pw - tw) / 2, y + 16);
                y += 20;

                g.setFont(F_DATA);
                String dt = "Ngày xuất: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                g.drawString(dt, 0, y + 12);
                y += 14;

                if (!filterInfo.isEmpty()) {
                    g.drawString("Lọc: " + filterInfo, 0, y + 12);
                    y += 14;
                }
                g.drawString("Tổng số: " + list.size() + " nhân viên", 0, y + 12);
                y += 14 + 4;
            } else {
                y = 4;
            }

            // Column header.
            g.setColor(new Color(70, 40, 120));
            g.fillRect(0, y, pw, COL_H);
            g.setColor(Color.WHITE);
            g.setFont(F_COL);
            int cx = 0;
            FontMetrics cfm = g.getFontMetrics();
            for (int c = 0; c < COLS.length; c++) {
                g.drawString(clip(COLS[c], cw[c] - 4, cfm), cx + 2, y + COL_H - 5);
                cx += cw[c];
            }
            y += COL_H;

            // Data rows.
            g.setFont(F_DATA);
            FontMetrics dfm = g.getFontMetrics();
            int rpp = rowsPerPage(pf);
            int startRow = pageIndex * rpp;
            int endRow = Math.min(startRow + rpp, list.size());

            for (int i = startRow; i < endRow; i++) {
                NhanVien nv = list.get(i);
                Color rowBg = (i % 2 == 0) ? Color.WHITE : new Color(248, 246, 255);
                g.setColor(rowBg);
                g.fillRect(0, y, pw, ROW_H);
                g.setColor(new Color(220, 220, 220));
                g.drawLine(0, y + ROW_H - 1, pw, y + ROW_H - 1);

                g.setColor(Color.BLACK);
                String[] cells = {
                    String.valueOf(i + 1),
                    safe(nv.getMaNhanVien()),
                    safe(nv.getHoTen()),
                    safe(nv.getTenPhongBanHienTai()),
                    safe(nv.getTenChucVuHienTai()),
                    nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(DATE_FMT) : "",
                    HRMConstants.display(nv.getLoaiHopDong()),
                    HRMConstants.display(nv.getTrangThai())
                };
                cx = 0;
                for (int c = 0; c < cells.length; c++) {
                    g.drawString(clip(cells[c], cw[c] - 4, dfm), cx + 2, y + ROW_H - 3);
                    cx += cw[c];
                }
                y += ROW_H;
            }

            // Footer.
            g.setFont(F_FOOTER);
            g.setColor(Color.GRAY);
            String footer = "Trang " + (pageIndex + 1) + "/" + pageCount(pf);
            FontMetrics ffm = g.getFontMetrics();
            g.drawString(footer, pw - ffm.stringWidth(footer), ph - 4);

            return PAGE_EXISTS;
        }

        private String clip(String text, int maxWidth, FontMetrics fm) {
            if (text == null || text.isEmpty()) return "";
            if (fm.stringWidth(text) <= maxWidth) return text;
            while (text.length() > 1 && fm.stringWidth(text + "..") > maxWidth) {
                text = text.substring(0, text.length() - 1);
            }
            return text.isEmpty() ? "" : text + "..";
        }
    }
}
