package vn.ptit.report;

import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sinh bao cao Bai tap 2 (.docx) tu tep noi dung van ban va thu muc anh.
 * <p>
 * Dinh dang tep noi dung (moi dong mot lenh):
 * <pre>
 * # Tieu de chuong          ## Muc cap 2           ### Muc cap 3
 * P: doan van               B: gach dau dong       (phan truoc "::" in dam)
 * IMG: tep.png | chu thich | rong toi da (cm) | cao toi da (cm)
 * TABLE: chu thich | do rong cot (cm) ... ; cac dong "o1|o2|..." ; END   (dong dau la tieu de)
 * CODE: ... END             CODEFILE: tep.sql
 * </pre>
 * Tham so: duongDanNoiDung thuMucAnh tepDocxRa [--update-fields]
 */
public class ReportGenerator {

    private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    /** Do rong vung chu: A4 21 cm - le trai 3 cm - le phai 2 cm. */
    private static final int TEXT_WIDTH_TWIPS = cmToTwips(16);
    private static final Pattern CHAPTER_NO = Pattern.compile("CHƯƠNG (\\d+)");

    private final XWPFDocument doc = new XWPFDocument();
    private final Path imgDir;
    private final Path contentDir;
    private BigInteger bulletNumId;
    private int chapter;
    private int figure;
    private int table;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: ReportGenerator <noi-dung.txt> <img dir> <out.docx> [--update-fields]");
            System.exit(2);
        }
        Path content = Path.of(args[0]);
        ReportGenerator g = new ReportGenerator(content.toAbsolutePath().getParent(), Path.of(args[1]));
        g.build(Files.readAllLines(content, StandardCharsets.UTF_8));
        boolean updateFields = args.length > 3 && args[3].equals("--update-fields");
        if (updateFields) {
            g.doc.enforceUpdateFields();
        }
        try (OutputStream out = Files.newOutputStream(Path.of(args[2]))) {
            g.doc.write(out);
        }
        System.out.printf("Wrote %s: %d tables, %d pictures, updateFields=%s%n",
                args[2], g.doc.getTables().size(), g.doc.getAllPictures().size(), updateFields);
    }

    private ReportGenerator(Path contentDir, Path imgDir) {
        this.contentDir = contentDir;
        this.imgDir = imgDir;
    }

    // ===================================================================== Cau truc tai lieu

    private void build(List<String> lines) throws Exception {
        setUpStyles();
        setUpNumbering();
        setUpPageAndFooter();
        cover();
        frontMatter();

        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            if (l.isBlank()) {
                continue;
            }
            if (l.startsWith("### ")) {
                heading(l.substring(4), "Heading3");
            } else if (l.startsWith("## ")) {
                heading(l.substring(3), "Heading2");
            } else if (l.startsWith("# ")) {
                String title = l.substring(2);
                Matcher m = CHAPTER_NO.matcher(title);
                chapter = m.find() ? Integer.parseInt(m.group(1)) : 0;
                figure = 0;
                table = 0;
                heading(title, "Heading1");
            } else if (l.startsWith("P: ")) {
                richParagraph(l.substring(3), "Normal", false);
            } else if (l.startsWith("B: ")) {
                richParagraph(l.substring(3), "ListBullet", true);
            } else if (l.startsWith("IMG: ")) {
                String[] f = l.substring(5).split("\\s*\\|\\s*");
                image(f[0], f[1], Double.parseDouble(f[2]), Double.parseDouble(f[3]));
            } else if (l.startsWith("TABLE: ")) {
                String[] f = l.substring(7).split("\\s*\\|\\s*");
                List<String> rows = new ArrayList<>();
                while (!lines.get(++i).equals("END")) {
                    rows.add(lines.get(i));
                }
                table(f[0], f[1].trim().split("\\s+"), rows);
            } else if (l.equals("CODE:")) {
                List<String> code = new ArrayList<>();
                while (!lines.get(++i).equals("END")) {
                    code.add(lines.get(i));
                }
                code(code);
            } else if (l.startsWith("CODEFILE: ")) {
                code(Files.readAllLines(contentDir.resolve(l.substring(10).trim()), StandardCharsets.UTF_8)
                        .stream().filter(s -> !s.isBlank()).toList());
            } else {
                throw new IllegalArgumentException("Dong khong hop le " + (i + 1) + ": " + l);
            }
        }
    }

    private void cover() {
        coverLine("HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG", 28, true, 0);
        coverLine("---------- oOo ----------", 24, false, 80);
        coverLine("BÁO CÁO BÀI TẬP 2", 40, true, 2200);
        coverLine("MÔN: KỸ THUẬT LẬP TRÌNH HƯỚNG ĐỐI TƯỢNG", 28, true, 280);
        coverLine("THIẾT KẾ BIỂU ĐỒ LỚP VÀ LƯỢC ĐỒ CƠ SỞ DỮ LIỆU", 32, true, 720);
        coverLine("Đề tài:", 26, false, 600);
        coverLine("XÂY DỰNG HỆ THỐNG QUẢN LÝ GIAO HÀNG NỘI THÀNH HÀ NỘI", 28, true, 80);
        coverLine("VÀ ĐỐI SOÁT TIỀN THU HỘ (COD)", 28, true, 40);

        String[][] info = {
                {"Giảng viên hướng dẫn:", "...................................."},
                {"Sinh viên thực hiện:", "Lưu Cung Tuấn"},
                {"Mã sinh viên:", "B22DTCN040"},
                {"Lớp:", "D22TXCN01-B"}};
        for (int k = 0; k < info.length; k++) {
            XWPFParagraph p = doc.createParagraph();
            p.setStyle("Bia");
            p.setAlignment(ParagraphAlignment.LEFT);
            p.setIndentationLeft(cmToTwips(3));
            p.setSpacingBefore(k == 0 ? 1800 : 120);
            CTTabStop tab = pPr(p).addNewTabs().addNewTab();
            tab.setVal(STTabJc.LEFT);
            tab.setPos(BigInteger.valueOf(cmToTwips(8.5)));
            XWPFRun r = p.createRun();
            r.setText(info[k][0]);
            r.addTab();
            XWPFRun v = p.createRun();
            v.setBold(true);
            v.setText(info[k][1]);
        }
        coverLine("Hà Nội, tháng 11 năm 2026", 26, false, 2400);
    }

    private void coverLine(String text, int halfPoints, boolean bold, int spaceBefore) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("Bia");
        p.setSpacingBefore(spaceBefore);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(bold);
        r.setFontSize(halfPoints / 2.0);
    }

    private void frontMatter() {
        titleOnNewPage("MỤC LỤC");
        field(doc.createParagraph(), "TOC \\o \"1-3\" \\h \\z \\u",
                "Nhấp chuột phải, chọn Update Field để cập nhật mục lục.");
        titleOnNewPage("DANH MỤC HÌNH");
        // Danh muc hinh / bang dung cap 9 de lay kieu TOC9 (chu thuong), khong bi in dam nhu cap 1 cua muc luc
        field(doc.createParagraph(), "TOC \\h \\z \\t \"CaptionHinh,9\"", "Cập nhật để hiện danh mục hình.");
        XWPFParagraph t = doc.createParagraph();
        t.setStyle("TieuDe");
        t.setSpacingBefore(480);
        t.createRun().setText("DANH MỤC BẢNG");
        field(doc.createParagraph(), "TOC \\h \\z \\t \"CaptionBang,9\"", "Cập nhật để hiện danh mục bảng.");
    }

    private void titleOnNewPage(String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("TieuDe");
        p.setPageBreak(true);
        p.createRun().setText(text);
    }

    // ===================================================================== Thanh phan noi dung

    private void heading(String text, String style) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle(style);
        p.createRun().setText(text);
    }

    /** Doan van; phan truoc "::" (neu co) in dam. */
    private void richParagraph(String text, String style, boolean bullet) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle(style);
        if (bullet) {
            p.setNumID(bulletNumId);
            p.setNumILvl(BigInteger.ZERO);
        }
        int i = text.indexOf("::");
        if (i >= 0) {
            XWPFRun b = p.createRun();
            b.setBold(true);
            b.setText(text.substring(0, i) + ":");
            p.createRun().setText(text.substring(i + 2));
        } else {
            p.createRun().setText(text);
        }
    }

    private void image(String file, String caption, double maxWidthCm, double maxHeightCm) throws IOException {
        Path path = imgDir.resolve(file);
        BufferedImage img = ImageIO.read(path.toFile());
        double ratio = (double) img.getHeight() / img.getWidth();
        double widthCm = Math.min(maxWidthCm, img.getWidth() / 170.0 * 2.54);   // anh ve o 170 dpi
        double heightCm = widthCm * ratio;
        if (heightCm > maxHeightCm) {
            heightCm = maxHeightCm;
            widthCm = heightCm / ratio;
        }
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("HinhAnh");
        try (InputStream in = Files.newInputStream(path)) {
            p.createRun().addPicture(in, Document.PICTURE_TYPE_PNG, file,
                    Units.toEMU(widthCm / 2.54 * 72), Units.toEMU(heightCm / 2.54 * 72));
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            throw new IOException(e);
        }
        figure++;
        XWPFParagraph c = doc.createParagraph();
        c.setStyle("CaptionHinh");
        c.createRun().setText("Hình " + chapter + "." + figure + ". " + caption);
    }

    private void table(String caption, String[] widthsCm, List<String> rows) {
        table++;
        XWPFParagraph c = doc.createParagraph();
        c.setStyle("CaptionBang");
        c.createRun().setText("Bảng " + chapter + "." + table + ". " + caption);

        int cols = widthsCm.length;
        int[] widths = new int[cols];
        int total = 0;
        for (int k = 0; k < cols; k++) {
            widths[k] = cmToTwips(Double.parseDouble(widthsCm[k]));
            total += widths[k];
        }
        // Co lai cho vua vung chu neu tong do rong vuot qua
        if (total > TEXT_WIDTH_TWIPS) {
            for (int k = 0; k < cols; k++) {
                widths[k] = widths[k] * TEXT_WIDTH_TWIPS / total;
            }
        }

        XWPFTable t = doc.createTable(rows.size(), cols);
        CTTbl ct = t.getCTTbl();
        CTTblPr pr = ct.getTblPr() != null ? ct.getTblPr() : ct.addNewTblPr();
        pr.addNewTblStyle().setVal("BangLuoi");
        CTTblWidth tw = pr.isSetTblW() ? pr.getTblW() : pr.addNewTblW();
        tw.setType(STTblWidth.DXA);
        tw.setW(BigInteger.valueOf(sum(widths)));
        pr.addNewTblLayout().setType(STTblLayoutType.FIXED);
        CTTblCellMar mar = pr.addNewTblCellMar();
        mar.addNewLeft().setW(BigInteger.valueOf(85));
        mar.getLeft().setType(STTblWidth.DXA);
        mar.addNewRight().setW(BigInteger.valueOf(85));
        mar.getRight().setType(STTblWidth.DXA);
        CTTblGrid grid = ct.getTblGrid() != null ? ct.getTblGrid() : ct.addNewTblGrid();
        while (grid.sizeOfGridColArray() > 0) {
            grid.removeGridCol(0);
        }
        for (int w : widths) {
            grid.addNewGridCol().setW(BigInteger.valueOf(w));
        }

        for (int r = 0; r < rows.size(); r++) {
            String[] cells = rows.get(r).split("\\|", -1);
            XWPFTableRow row = t.getRow(r);
            CTTrPr trPr = row.getCtRow().isSetTrPr() ? row.getCtRow().getTrPr() : row.getCtRow().addNewTrPr();
            trPr.addNewCantSplit();
            if (r == 0) {
                trPr.addNewTblHeader();
            }
            for (int k = 0; k < cols; k++) {
                XWPFTableCell cell = row.getCell(k);
                CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
                CTTblWidth cw = tcPr.addNewTcW();
                cw.setType(STTblWidth.DXA);
                cw.setW(BigInteger.valueOf(widths[k]));
                XWPFParagraph p = cell.getParagraphs().get(0);
                p.setStyle(r == 0 ? "BangTieuDe" : "BangChu");
                p.createRun().setText(k < cells.length ? cells[k] : "");
                if (r == 0) {
                    cell.setColor("DDE8F7");
                    cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                }
            }
        }
        // Khoang trong nho sau bang
        doc.createParagraph().setStyle("GianCach");
    }

    private void code(List<String> lines) {
        for (String line : lines) {
            XWPFParagraph p = doc.createParagraph();
            p.setStyle("CodeBlock");
            p.createRun().setText(line);
        }
        doc.createParagraph().setStyle("GianCach");
    }

    // ===================================================================== Field, trang, kieu chu

    /** Chen field (muc luc, so trang) dang begin / instr / separate / ket qua tam / end. */
    private static void field(XWPFParagraph p, String instr, String placeholder) {
        p.createRun().getCTR().addNewFldChar().setFldCharType(STFldCharType.BEGIN);
        CTText it = p.createRun().getCTR().addNewInstrText();
        it.setStringValue(" " + instr + " ");
        it.setSpace(org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute.Space.PRESERVE);
        p.createRun().getCTR().addNewFldChar().setFldCharType(STFldCharType.SEPARATE);
        p.createRun().setText(placeholder);
        p.createRun().getCTR().addNewFldChar().setFldCharType(STFldCharType.END);
    }

    private void setUpPageAndFooter() {
        CTSectPr sect = doc.getDocument().getBody().isSetSectPr()
                ? doc.getDocument().getBody().getSectPr() : doc.getDocument().getBody().addNewSectPr();
        CTPageSz size = sect.addNewPgSz();
        size.setW(BigInteger.valueOf(11906));
        size.setH(BigInteger.valueOf(16838));
        CTPageMar mar = sect.addNewPgMar();
        mar.setTop(BigInteger.valueOf(cmToTwips(2)));
        mar.setBottom(BigInteger.valueOf(cmToTwips(2)));
        mar.setLeft(BigInteger.valueOf(cmToTwips(3)));
        mar.setRight(BigInteger.valueOf(cmToTwips(2)));
        mar.setHeader(BigInteger.valueOf(cmToTwips(1)));
        mar.setFooter(BigInteger.valueOf(cmToTwips(1)));
        mar.setGutter(BigInteger.ZERO);
        sect.addNewTitlePg();   // trang bia khong danh so

        doc.createFooter(HeaderFooterType.FIRST).createParagraph();
        XWPFParagraph p = doc.createFooter(HeaderFooterType.DEFAULT).createParagraph();
        p.setStyle("ChanTrang");
        field(p, "PAGE", "1");
    }

    private void setUpNumbering() throws Exception {
        String xml = "<w:numbering xmlns:w=\"" + W_NS + "\"><w:abstractNum w:abstractNumId=\"0\">"
                + "<w:multiLevelType w:val=\"singleLevel\"/><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
                + "<w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"–\"/><w:lvlJc w:val=\"left\"/>"
                + "<w:pPr><w:ind w:left=\"567\" w:hanging=\"284\"/></w:pPr>"
                + "<w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/></w:rPr>"
                + "</w:lvl></w:abstractNum></w:numbering>";
        CTNumbering ct = NumberingDocument.Factory.parse(xml).getNumbering();
        XWPFNumbering numbering = doc.createNumbering();
        BigInteger abstractId = numbering.addAbstractNum(new XWPFAbstractNum(ct.getAbstractNumArray(0)));
        bulletNumId = numbering.addNum(abstractId);
    }

    private void setUpStyles() throws Exception {
        String font = "<w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\" "
                + "w:eastAsia=\"Times New Roman\" w:cs=\"Times New Roman\"/>";
        String tocTab = "<w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"" + TEXT_WIDTH_TWIPS + "\"/></w:tabs>";
        String single = "w:line=\"240\" w:lineRule=\"auto\"";
        StringBuilder s = new StringBuilder("<w:styles xmlns:w=\"" + W_NS + "\">");
        s.append("<w:docDefaults><w:rPrDefault><w:rPr>").append(font)
                .append("<w:sz w:val=\"26\"/><w:szCs w:val=\"26\"/><w:lang w:val=\"vi-VN\" w:eastAsia=\"en-US\"/>")
                .append("</w:rPr></w:rPrDefault><w:pPrDefault><w:pPr>")
                .append("<w:spacing w:after=\"120\" w:line=\"312\" w:lineRule=\"auto\"/><w:jc w:val=\"both\"/>")
                .append("</w:pPr></w:pPrDefault></w:docDefaults>");
        s.append(pStyle("Normal", "Normal", null, true, "", ""));
        s.append(pStyle("Heading1", "heading 1", "Normal", true,
                "<w:keepNext/><w:pageBreakBefore/><w:spacing w:before=\"0\" w:after=\"360\" " + single + "/>"
                        + "<w:jc w:val=\"center\"/><w:outlineLvl w:val=\"0\"/>", "<w:b/><w:sz w:val=\"30\"/><w:szCs w:val=\"30\"/>"));
        s.append(pStyle("Heading2", "heading 2", "Normal", true,
                "<w:keepNext/><w:spacing w:before=\"240\" w:after=\"120\" " + single + "/><w:jc w:val=\"left\"/>"
                        + "<w:outlineLvl w:val=\"1\"/>", "<w:b/><w:sz w:val=\"28\"/><w:szCs w:val=\"28\"/>"));
        s.append(pStyle("Heading3", "heading 3", "Normal", true,
                "<w:keepNext/><w:spacing w:before=\"200\" w:after=\"80\" " + single + "/><w:jc w:val=\"left\"/>"
                        + "<w:outlineLvl w:val=\"2\"/>", "<w:b/><w:i/>"));
        s.append(pStyle("TieuDe", "Tieu de muc", "Normal", true,
                "<w:spacing w:before=\"0\" w:after=\"360\" " + single + "/><w:jc w:val=\"center\"/>",
                "<w:b/><w:sz w:val=\"30\"/><w:szCs w:val=\"30\"/>"));
        s.append(pStyle("Bia", "Bia", "Normal", false,
                "<w:spacing w:after=\"0\" " + single + "/><w:jc w:val=\"center\"/>", ""));
        s.append(pStyle("HinhAnh", "Hinh anh", "Normal", false,
                "<w:keepNext/><w:spacing w:before=\"120\" w:after=\"0\" " + single + "/><w:jc w:val=\"center\"/>", ""));
        s.append(pStyle("CaptionHinh", "CaptionHinh", "Normal", true,
                "<w:spacing w:before=\"60\" w:after=\"240\" " + single + "/><w:jc w:val=\"center\"/>",
                "<w:i/><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/>"));
        s.append(pStyle("CaptionBang", "CaptionBang", "Normal", true,
                "<w:keepNext/><w:spacing w:before=\"160\" w:after=\"80\" " + single + "/><w:jc w:val=\"center\"/>",
                "<w:i/><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/>"));
        s.append(pStyle("BangChu", "Bang chu", "Normal", false,
                "<w:spacing w:before=\"20\" w:after=\"20\" " + single + "/><w:jc w:val=\"left\"/>",
                "<w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/>"));
        s.append(pStyle("BangTieuDe", "Bang tieu de", "BangChu", false,
                "<w:keepNext/><w:jc w:val=\"center\"/>", "<w:b/>"));
        s.append(pStyle("GianCach", "Gian cach", "Normal", false,
                "<w:spacing w:after=\"0\" " + single + "/>", "<w:sz w:val=\"8\"/><w:szCs w:val=\"8\"/>"));
        s.append(pStyle("CodeBlock", "Code", "Normal", false,
                "<w:spacing w:after=\"0\" " + single + "/><w:jc w:val=\"left\"/>",
                "<w:rFonts w:ascii=\"Consolas\" w:hAnsi=\"Consolas\" w:cs=\"Consolas\"/><w:sz w:val=\"17\"/><w:szCs w:val=\"17\"/>"));
        s.append(pStyle("ListBullet", "List Bullet", "Normal", false,
                "<w:spacing w:after=\"60\"/>", ""));
        s.append(pStyle("ChanTrang", "footer", "Normal", false,
                "<w:spacing w:after=\"0\" " + single + "/><w:jc w:val=\"center\"/>", "<w:sz w:val=\"24\"/>"));
        for (int k = 1; k <= 3; k++) {
            s.append(pStyle("TOC" + k, "toc " + k, "Normal", false,
                    tocTab + "<w:spacing w:before=\"" + (k == 1 ? 60 : 0) + "\" w:after=\"0\" " + single + "/>"
                            + "<w:ind w:left=\"" + (k - 1) * 280 + "\"/><w:jc w:val=\"left\"/>",
                    k == 1 ? "<w:b/>" : ""));
        }
        s.append(pStyle("TOC9", "toc 9", "Normal", false,
                tocTab + "<w:spacing w:before=\"0\" w:after=\"40\" " + single + "/><w:ind w:left=\"0\"/><w:jc w:val=\"left\"/>",
                ""));
        s.append(pStyle("TableofFigures", "table of figures", "Normal", false,
                tocTab + "<w:spacing w:before=\"0\" w:after=\"60\" " + single + "/><w:jc w:val=\"left\"/>", ""));
        s.append("<w:style w:type=\"character\" w:styleId=\"Hyperlink\"><w:name w:val=\"Hyperlink\"/>"
                + "<w:rPr><w:color w:val=\"000000\"/></w:rPr></w:style>");
        s.append("<w:style w:type=\"table\" w:styleId=\"BangLuoi\"><w:name w:val=\"Bang luoi\"/><w:tblPr><w:tblBorders>");
        for (String side : new String[]{"top", "left", "bottom", "right", "insideH", "insideV"}) {
            s.append("<w:").append(side).append(" w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"808080\"/>");
        }
        s.append("</w:tblBorders></w:tblPr></w:style>");
        s.append("</w:styles>");
        doc.createStyles().setStyles(StylesDocument.Factory.parse(s.toString()).getStyles());
    }

    private static String pStyle(String id, String name, String basedOn, boolean qFormat, String pPr, String rPr) {
        StringBuilder s = new StringBuilder("<w:style w:type=\"paragraph\" w:styleId=\"" + id + "\"");
        if (id.equals("Normal")) {
            s.append(" w:default=\"1\"");
        }
        s.append("><w:name w:val=\"").append(name).append("\"/>");
        if (basedOn != null) {
            s.append("<w:basedOn w:val=\"").append(basedOn).append("\"/><w:next w:val=\"Normal\"/>");
        }
        if (qFormat) {
            s.append("<w:qFormat/>");
        }
        if (!pPr.isEmpty()) {
            s.append("<w:pPr>").append(pPr).append("</w:pPr>");
        }
        if (!rPr.isEmpty()) {
            s.append("<w:rPr>").append(rPr).append("</w:rPr>");
        }
        return s.append("</w:style>").toString();
    }

    private static CTPPr pPr(XWPFParagraph p) {
        return p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
    }

    private static int cmToTwips(double cm) {
        return (int) Math.round(cm / 2.54 * 1440);
    }

    private static int sum(int[] a) {
        int s = 0;
        for (int v : a) {
            s += v;
        }
        return s;
    }
}
