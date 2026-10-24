package com.hrm.util;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

/**
 * Minimal pure-Java XLSX reader/writer. No external dependencies.
 * Supports string cells (all values treated as strings).
 */
public final class SimpleXlsx {

    private SimpleXlsx() {}

    // ============================================================
    // WRITE
    // ============================================================

    /**
     * Write rows to an .xlsx file.
     *
     * @param file    target file
     * @param headers column header names (rendered bold)
     * @param rows    data rows
     */
    public static void write(File file, String[] headers, List<String[]> rows) throws IOException {
        // Build shared strings table (deduplicated)
        List<String> ssList = new ArrayList<>();
        Map<String, Integer> ssMap = new LinkedHashMap<>();

        for (String h : headers) {
            addShared(h != null ? h : "", ssList, ssMap);
        }
        for (String[] row : rows) {
            for (String cell : row) {
                addShared(cell != null ? cell : "", ssList, ssMap);
            }
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            zos.setMethod(ZipOutputStream.DEFLATED);
            putEntry(zos, "[Content_Types].xml", contentTypes());
            putEntry(zos, "_rels/.rels",           rootRels());
            putEntry(zos, "xl/workbook.xml",       workbook());
            putEntry(zos, "xl/_rels/workbook.xml.rels", workbookRels());
            putEntry(zos, "xl/styles.xml",         styles());
            putEntry(zos, "xl/sharedStrings.xml",  sharedStringsXml(ssList));
            putEntry(zos, "xl/worksheets/sheet1.xml", sheetXml(headers, rows, ssMap));
        }
    }

    private static void addShared(String val, List<String> list, Map<String, Integer> map) {
        map.computeIfAbsent(val, k -> { list.add(k); return list.size() - 1; });
    }

    private static void putEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
             + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
             + "<Default Extension=\"xml\"  ContentType=\"application/xml\"/>"
             + "<Override PartName=\"/xl/workbook.xml\""
             +  " ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
             + "<Override PartName=\"/xl/worksheets/sheet1.xml\""
             +  " ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
             + "<Override PartName=\"/xl/sharedStrings.xml\""
             +  " ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>"
             + "<Override PartName=\"/xl/styles.xml\""
             +  " ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
             + "</Types>";
    }

    private static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
             + "<Relationship Id=\"rId1\""
             +  " Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\""
             +  " Target=\"xl/workbook.xml\"/>"
             + "</Relationships>";
    }

    private static String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\""
             +  " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
             + "<sheets><sheet name=\"Sheet1\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
             + "</workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
             + "<Relationship Id=\"rId1\""
             +  " Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\""
             +  " Target=\"worksheets/sheet1.xml\"/>"
             + "<Relationship Id=\"rId2\""
             +  " Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\""
             +  " Target=\"sharedStrings.xml\"/>"
             + "<Relationship Id=\"rId3\""
             +  " Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\""
             +  " Target=\"styles.xml\"/>"
             + "</Relationships>";
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
             + "<fonts count=\"2\">"
             +   "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
             +   "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
             + "</fonts>"
             + "<fills count=\"2\">"
             +   "<fill><patternFill patternType=\"none\"/></fill>"
             +   "<fill><patternFill patternType=\"gray125\"/></fill>"
             + "</fills>"
             + "<borders count=\"1\">"
             +   "<border><left/><right/><top/><bottom/><diagonal/></border>"
             + "</borders>"
             + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
             + "<cellXfs count=\"2\">"
             +   "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
             +   "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
             + "</cellXfs>"
             + "</styleSheet>";
    }

    private static String sharedStringsXml(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"")
          .append(list.size()).append("\" uniqueCount=\"").append(list.size()).append("\">");
        for (String s : list) {
            sb.append("<si><t xml:space=\"preserve\">").append(xmlEsc(s)).append("</t></si>");
        }
        sb.append("</sst>");
        return sb.toString();
    }

    private static String sheetXml(String[] headers, List<String[]> rows, Map<String, Integer> ssMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sb.append("<sheetData>");

        // Header row – bold (style index 1)
        sb.append("<row r=\"1\">");
        for (int c = 0; c < headers.length; c++) {
            String ref = colLetter(c) + "1";
            int idx = ssMap.getOrDefault(headers[c] != null ? headers[c] : "", 0);
            sb.append("<c r=\"").append(ref).append("\" t=\"s\" s=\"1\"><v>").append(idx).append("</v></c>");
        }
        sb.append("</row>");

        // Data rows
        int r = 2;
        for (String[] row : rows) {
            sb.append("<row r=\"").append(r).append("\">");
            for (int c = 0; c < row.length; c++) {
                String val = row[c] != null ? row[c] : "";
                int idx = ssMap.getOrDefault(val, 0);
                String ref = colLetter(c) + r;
                sb.append("<c r=\"").append(ref).append("\" t=\"s\"><v>").append(idx).append("</v></c>");
            }
            sb.append("</row>");
            r++;
        }

        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    /** 0 → "A", 1 → "B", 25 → "Z", 26 → "AA", … */
    private static String colLetter(int col) {
        StringBuilder sb = new StringBuilder();
        int n = col + 1;
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    private static String xmlEsc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    // ============================================================
    // READ
    // ============================================================

    /**
     * Read the first sheet of an .xlsx file.
     *
     * @return list of rows; row[0] is the header row. Each inner array has the
     *         same length (max column index + 1). Empty cells become "".
     */
    public static List<String[]> read(File file) throws IOException {
        Map<Integer, String> sharedStrings = Collections.emptyMap();
        List<Map<Integer, String>> rawRows = Collections.emptyList();
        int maxCol = 0;

        try (ZipFile zip = new ZipFile(file)) {
            ZipEntry ssEntry = zip.getEntry("xl/sharedStrings.xml");
            if (ssEntry != null) {
                try (InputStream is = zip.getInputStream(ssEntry)) {
                    sharedStrings = parseSharedStrings(is);
                }
            }

            ZipEntry sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml");
            if (sheetEntry != null) {
                try (InputStream is = zip.getInputStream(sheetEntry)) {
                    int[] maxColHolder = {0};
                    rawRows = parseSheet(is, sharedStrings, maxColHolder);
                    maxCol = maxColHolder[0];
                }
            }
        }

        List<String[]> result = new ArrayList<>(rawRows.size());
        for (Map<Integer, String> rowMap : rawRows) {
            String[] row = new String[maxCol + 1];
            for (int c = 0; c <= maxCol; c++) {
                row[c] = rowMap.getOrDefault(c, "");
            }
            result.add(row);
        }
        return result;
    }

    private static Map<Integer, String> parseSharedStrings(InputStream is) throws IOException {
        Map<Integer, String> result = new LinkedHashMap<>();
        try {
            Document doc = newBuilder().parse(is);
            NodeList siList = doc.getElementsByTagName("si");
            for (int i = 0; i < siList.getLength(); i++) {
                Element si = (Element) siList.item(i);
                NodeList tList = si.getElementsByTagName("t");
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < tList.getLength(); j++) {
                    sb.append(tList.item(j).getTextContent());
                }
                result.put(i, sb.toString());
            }
        } catch (Exception e) {
            throw new IOException("Cannot parse sharedStrings.xml: " + e.getMessage(), e);
        }
        return result;
    }

    private static List<Map<Integer, String>> parseSheet(
            InputStream is, Map<Integer, String> ss, int[] maxColHolder) throws IOException {
        List<Map<Integer, String>> rows = new ArrayList<>();
        try {
            Document doc = newBuilder().parse(is);
            NodeList rowList = doc.getElementsByTagName("row");
            for (int r = 0; r < rowList.getLength(); r++) {
                Element rowEl = (Element) rowList.item(r);
                Map<Integer, String> rowMap = new LinkedHashMap<>();
                NodeList cList = rowEl.getElementsByTagName("c");
                for (int c = 0; c < cList.getLength(); c++) {
                    Element cell = (Element) cList.item(c);
                    String ref  = cell.getAttribute("r");   // e.g. "B3"
                    String type = cell.getAttribute("t");   // "s", "inlineStr", or empty

                    int colIdx = colRefToIndex(ref);
                    if (colIdx > maxColHolder[0]) maxColHolder[0] = colIdx;

                    String value = "";
                    if ("s".equals(type)) {
                        NodeList vl = cell.getElementsByTagName("v");
                        if (vl.getLength() > 0) {
                            int idx = Integer.parseInt(vl.item(0).getTextContent().trim());
                            value = ss.getOrDefault(idx, "");
                        }
                    } else if ("inlineStr".equals(type)) {
                        NodeList tl = cell.getElementsByTagName("t");
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < tl.getLength(); j++) sb.append(tl.item(j).getTextContent());
                        value = sb.toString();
                    } else {
                        NodeList vl = cell.getElementsByTagName("v");
                        if (vl.getLength() > 0) value = vl.item(0).getTextContent().trim();
                    }
                    rowMap.put(colIdx, value);
                }
                rows.add(rowMap);
            }
        } catch (Exception e) {
            throw new IOException("Cannot parse sheet1.xml: " + e.getMessage(), e);
        }
        return rows;
    }

    /** "B3" → 1,  "AA5" → 26 */
    private static int colRefToIndex(String cellRef) {
        int col = 0;
        for (char ch : cellRef.toCharArray()) {
            if (!Character.isLetter(ch)) break;
            col = col * 26 + (Character.toUpperCase(ch) - 'A' + 1);
        }
        return col - 1;
    }

    private static DocumentBuilder newBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        // Disable external entity resolution for safety
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return dbf.newDocumentBuilder();
    }
}
