package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.LaporanService;
import com.siakad.utils.AppTheme;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * LaporanPanel - Generate & Cetak Laporan (Admin only)
 */
public class LaporanPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private JTextField txtSearch, txtTahunAjaran;
    private JComboBox<String> cmbJenis, cmbStatus;
    private JButton btnPrev, btnNext;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private CardLayout centerCard;
    private JPanel centerPanel;
    private StatePanel statePanel;

    private static Color BG() { return AppTheme.bg(); }
    private static Color CARD_BG() { return AppTheme.card(); }
    private static Color TABLE_BG() { return AppTheme.table(); }
    private static Color HEADER_BG() { return AppTheme.header(); }
    private static Color BORDER_COLOR() { return AppTheme.border(); }
    private static Color ROW_ALT() { return AppTheme.rowAlt(); }
    private static Color TEXT_PRIMARY() { return AppTheme.text(); }
    private static Color TEXT_MUTED() { return AppTheme.muted(); }
    private static Color TEXT_DIM() { return AppTheme.dim(); }
    private static Color BLUE() { return AppTheme.blue(); }
    private static Color GREEN() { return AppTheme.green(); }
    private static Color YELLOW() { return AppTheme.yellow(); }
    private static Color RED() { return AppTheme.red(); }
    private static Color PURPLE() { return AppTheme.purple(); }

    public LaporanPanel() {
        setBackground(BG());
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    private void initUI() {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Laporan & Cetak");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY());
        JLabel lblSub = new JLabel("Generate dan unduh laporan akademik & keuangan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED());
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        txtSearch = makeField("Cari judul");
        txtSearch.setPreferredSize(new Dimension(150, 34));
        txtSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        cmbJenis = new JComboBox<>(new String[]{"Semua Jenis", "pembayaran", "mahasiswa", "keuangan"});
        cmbJenis.setPreferredSize(new Dimension(130, 34));
        cmbJenis.addActionListener(e -> { currentPage = 1; loadData(); });
        cmbStatus = new JComboBox<>(new String[]{"Semua Status", "generated", "draft", "archived"});
        cmbStatus.setPreferredSize(new Dimension(130, 34));
        cmbStatus.addActionListener(e -> { currentPage = 1; loadData(); });
        txtTahunAjaran = makeField("TA");
        txtTahunAjaran.setPreferredSize(new Dimension(100, 34));
        txtTahunAjaran.addActionListener(e -> { currentPage = 1; loadData(); });
        JButton btnRefresh = buildBtn("🔄  Refresh", CARD_BG(), 110);
        btnRefresh.addActionListener(e -> loadData());
        actions.add(txtSearch);
        actions.add(cmbJenis);
        actions.add(cmbStatus);
        actions.add(txtTahunAjaran);
        actions.add(btnRefresh);
        header.add(actions, BorderLayout.EAST);

        // ── Generate Cards ──
        JPanel genCards = new JPanel(new GridLayout(1, 3, 14, 0));
        genCards.setOpaque(false);
        genCards.setBorder(new EmptyBorder(0, 28, 20, 28));

        genCards.add(buildGenerateCard(
            "📊", "Laporan Pembayaran",
            "Generate laporan transaksi pembayaran UKT berdasarkan periode dan tahun ajaran.",
            BLUE(), e -> showGeneratePembayaranDialog()
        ));
        genCards.add(buildGenerateCard(
            "👨‍🎓", "Laporan Mahasiswa",
            "Generate laporan data seluruh mahasiswa terdaftar beserta status akademik.",
            GREEN(), e -> generateMahasiswa()
        ));
        genCards.add(buildGenerateCard(
            "💰", "Laporan Keuangan",
            "Generate ringkasan keuangan dan rekapitulasi pendapatan per tahun ajaran.",
            YELLOW(), e -> showGenerateKeuanganDialog()
        ));

        // ── Table ──
        String[] cols = {"ID", "Judul", "Jenis", "Periode", "Tahun Ajaran", "Records", "Status", "Dibuat", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? TABLE_BG() : ROW_ALT());
                return c;
            }
        };
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(8).setMinWidth(260);
        table.getColumnModel().getColumn(8).setMaxWidth(260);
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new ActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(TABLE_BG());

        JPanel tableCard = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBorder(new EmptyBorder(0, 28, 0, 28));
        tableCard.add(sp);

        // ── Footer ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 28, 14, 28));
        lblTotal = new JLabel("Total: 0 laporan");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(TEXT_DIM());
        btnPrev = buildBtn("Prev", CARD_BG(), 74);
        btnNext = buildBtn("Next", CARD_BG(), 74);
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { currentPage++; loadData(); });
        footer.add(lblTotal);
        footer.add(btnPrev);
        footer.add(btnNext);

        // ── Body ──
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(genCards);
        body.add(tableCard);
        body.add(footer);

        centerCard = new CardLayout();
        centerPanel = new JPanel(centerCard);
        centerPanel.setOpaque(false);
        statePanel = new StatePanel();

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(scroll, "content");
        centerPanel.add(statePanel, "state");

        add(header, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildGenerateCard(String icon, String title, String desc, Color accent,
                                      java.awt.event.ActionListener action) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                // Top accent
                GradientPaint gp = new GradientPaint(0, 0, accent,
                        getWidth(), 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(0, 160));

        // Icon circle
        JLabel lblIcon = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
                g2.fillOval(0, 0, 44, 44);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
            @Override public Dimension getMaximumSize() { return new Dimension(44, 44); }
        };
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);
        lblIcon.setOpaque(false);
        lblIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_PRIMARY());
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><body style='width:180px'>" + desc + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(TEXT_MUTED());
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnGen = new JButton("Generate  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? accent.brighter() : accent;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnGen.setPreferredSize(new Dimension(140, 34));
        btnGen.setMaximumSize(new Dimension(140, 34));
        btnGen.setBorderPainted(false);
        btnGen.setContentAreaFilled(false);
        btnGen.setFocusPainted(false);
        btnGen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGen.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGen.addActionListener(action);

        card.add(lblIcon);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblDesc);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(12));
        card.add(btnGen);
        return card;
    }

    private void loadData() {
        String jenis = cmbJenis != null && cmbJenis.getSelectedIndex() > 0 ? cmbJenis.getSelectedItem().toString() : null;
        String status = cmbStatus != null && cmbStatus.getSelectedIndex() > 0 ? cmbStatus.getSelectedItem().toString() : null;
        String tahunAjaran = txtTahunAjaran != null && !txtTahunAjaran.getText().trim().isEmpty()
                ? txtTahunAjaran.getText().trim()
                : null;
        String search = txtSearch != null ? txtSearch.getText().trim() : null;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return LaporanService.getAll(currentPage, PAGE_SIZE, jenis, status, tahunAjaran, search);
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    tableModel.setRowCount(0);
                    if (resp.get("success").getAsBoolean()) {
                        JsonArray data = resp.getAsJsonArray("data");
                        JsonObject pg = resp.getAsJsonObject("pagination");
                        int totalPages = pg.get("totalPages").getAsInt();
                        lblTotal.setText("Total: " + pg.get("total").getAsInt() + " laporan tersimpan | Hal " + currentPage + "/" + Math.max(totalPages, 1));
                        btnPrev.setEnabled(currentPage > 1);
                        btnNext.setEnabled(currentPage < totalPages);
                        if (data.size() == 0) {
                            lblTotal.setText("Belum ada laporan tersimpan. Gunakan kartu generate di atas untuk membuat laporan.");
                            btnPrev.setEnabled(false);
                            btnNext.setEnabled(false);
                            centerCard.show(centerPanel, "content");
                            return;
                        }

                        for (int i = 0; i < data.size(); i++) {
                            JsonObject l = data.get(i).getAsJsonObject();
                            String periode = "";
                            if (l.has("periode_mulai") && !l.get("periode_mulai").isJsonNull())
                                periode = l.get("periode_mulai").getAsString().substring(0, 10);
                            if (l.has("periode_selesai") && !l.get("periode_selesai").isJsonNull())
                                periode += " ~ " + l.get("periode_selesai").getAsString().substring(0, 10);

                            tableModel.addRow(new Object[]{
                                l.get("id").getAsInt(),
                                l.get("judul").getAsString(),
                                l.get("jenis_laporan").getAsString(),
                                periode.isEmpty() ? "-" : periode,
                                l.has("tahun_ajaran") && !l.get("tahun_ajaran").isJsonNull() ? l.get("tahun_ajaran").getAsString() : "-",
                                l.get("total_records").getAsInt(),
                                l.get("status").getAsString(),
                                l.get("created_at").getAsString().substring(0, 10),
                                "aksi"
                            });
                        }
                        centerCard.show(centerPanel, "content");
                    } else {
                        showStateError(resp.has("message") ? resp.get("message").getAsString() : "Gagal memuat laporan.");
                    }
                } catch (Exception e) {
                    showStateError("Gagal memuat laporan: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void showStateError(String message) {
        statePanel.showState("!", "Laporan tidak bisa dimuat", message, "Muat ulang", this::loadData);
        centerCard.show(centerPanel, "state");
    }

    private void showGeneratePembayaranDialog() {
        JTextField fMulai   = makeField("YYYY-MM-DD");
        JTextField fSelesai = makeField("YYYY-MM-DD");
        JTextField fTA      = makeField("2024/2025");

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 10));
        panel.setBackground(CARD_BG());
        panel.setBorder(new EmptyBorder(8, 4, 8, 4));
        panel.add(makeLabel("Periode Mulai")); panel.add(datePickerField(fMulai));
        panel.add(makeLabel("Periode Selesai")); panel.add(datePickerField(fSelesai));
        panel.add(makeLabel("Tahun Ajaran")); panel.add(fTA);

        int result = JOptionPane.showConfirmDialog(this, panel, "Generate Laporan Pembayaran",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return LaporanService.generatePembayaran(
                        fMulai.getText().trim().isEmpty() ? null : fMulai.getText().trim(),
                        fSelesai.getText().trim().isEmpty() ? null : fSelesai.getText().trim(),
                        fTA.getText().trim().isEmpty() ? null : fTA.getText().trim());
            }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString(),
                            r.get("success").getAsBoolean() ? "Sukses" : "Gagal",
                            r.get("success").getAsBoolean() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void generateMahasiswa() {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.generateMahasiswa(); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString(), "Info", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showGenerateKeuanganDialog() {
        String ta = JOptionPane.showInputDialog(this, "Masukkan Tahun Ajaran (contoh: 2024/2025):", "2024/2025");
        if (ta == null || ta.trim().isEmpty()) return;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.generateKeuangan(ta.trim()); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString());
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void viewDetail(int id) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.getById(id); }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        JsonObject d = resp.getAsJsonObject("data");
                        String info = "Judul      : " + d.get("judul").getAsString() + "\n"
                                + "Jenis      : " + d.get("jenis_laporan").getAsString() + "\n"
                                + "Records    : " + d.get("total_records").getAsInt() + "\n"
                                + "Status     : " + d.get("status").getAsString() + "\n"
                                + "Dibuat     : " + d.get("created_at").getAsString() + "\n";
                        if (d.has("deskripsi") && !d.get("deskripsi").isJsonNull())
                            info += "Deskripsi  : " + d.get("deskripsi").getAsString();
                        info += "\n\n" + buildLaporanPreview(d);
                        JTextArea area = new JTextArea(info);
                        area.setEditable(false);
                        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        area.setBackground(CARD_BG());
                        area.setForeground(TEXT_PRIMARY());
                        area.setBorder(new EmptyBorder(8, 8, 8, 8));
                        JOptionPane.showMessageDialog(LaporanPanel.this, new JScrollPane(area),
                                "Detail Laporan #" + id, JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void exportLaporan(int id, String format) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.getById(id); }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (!resp.get("success").getAsBoolean()) {
                        JOptionPane.showMessageDialog(LaporanPanel.this, resp.get("message").getAsString(), "Gagal", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JsonObject laporan = resp.getAsJsonObject("data");
                    String ext = format.equals("excel") ? "xlsx" : format;
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Simpan Laporan " + format.toUpperCase());
                    chooser.setFileFilter(new FileNameExtensionFilter(format.toUpperCase(), ext));
                    chooser.setSelectedFile(new File(slug(safe(laporan, "judul")) + "." + ext));
                    if (chooser.showSaveDialog(LaporanPanel.this) != JFileChooser.APPROVE_OPTION) return;
                    File file = chooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith("." + ext)) {
                        file = new File(file.getParentFile(), file.getName() + "." + ext);
                    }
                    if (format.equals("pdf")) exportPdf(file, laporan);
                    else if (format.equals("excel")) exportExcel(file, laporan);
                    else exportCsv(file, laporan);
                    JOptionPane.showMessageDialog(LaporanPanel.this,
                            "Laporan berhasil diexport ke:\n" + file.getAbsolutePath());
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Export gagal: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void exportPdf(File file, JsonObject laporan) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 15);
        com.itextpdf.text.Font bodyFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 9);
        Paragraph title = new Paragraph(safe(laporan, "judul"), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Jenis: " + safe(laporan, "jenis_laporan") + " | Records: " + safe(laporan, "total_records"), bodyFont));
        document.add(new Paragraph("Dibuat: " + safe(laporan, "created_at"), bodyFont));
        document.add(new Paragraph(" "));

        JsonObject data = dataLaporan(laporan);
        String jenis = safe(laporan, "jenis_laporan");
        if ("pembayaran".equals(jenis)) {
            JsonArray records = array(data, "records");
            PdfPTable table = new PdfPTable(new float[]{1.7f, 1.3f, 2.4f, 1.4f, 1.8f, 1.5f, 1.2f});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Referensi", "NIM", "Nama", "Jenis", "Jumlah", "Tanggal", "Status"}) addPdfHeader(table, h);
            for (JsonElement el : records) {
                JsonObject r = el.getAsJsonObject();
                addPdfCell(table, safe(r, "nomor_referensi"), bodyFont);
                addPdfCell(table, safe(r, "nim"), bodyFont);
                addPdfCell(table, safe(r, "nama_mahasiswa"), bodyFont);
                addPdfCell(table, safe(r, "jenis_pembayaran"), bodyFont);
                addPdfCell(table, money(r, "jumlah"), bodyFont);
                addPdfCell(table, dateOnly(safe(r, "tanggal_bayar")), bodyFont);
                addPdfCell(table, safe(r, "status"), bodyFont);
            }
            document.add(table);
        } else if ("mahasiswa".equals(jenis)) {
            PdfPTable table = new PdfPTable(new float[]{3f, 2f, 1f});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Jurusan", "Status", "Total"}) addPdfHeader(table, h);
            for (JsonElement el : array(data, "per_jurusan")) {
                JsonObject r = el.getAsJsonObject();
                addPdfCell(table, safe(r, "jurusan"), bodyFont);
                addPdfCell(table, safe(r, "status"), bodyFont);
                addPdfCell(table, safe(r, "total"), bodyFont);
            }
            document.add(table);
        } else {
            PdfPTable table = new PdfPTable(new float[]{1.6f, 1.4f, 1.4f, 2f});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Bulan", "Status", "Transaksi", "Jumlah"}) addPdfHeader(table, h);
            for (JsonElement el : array(data, "pendapatan_bulanan")) {
                JsonObject r = el.getAsJsonObject();
                addPdfCell(table, safe(r, "bulan"), bodyFont);
                addPdfCell(table, safe(r, "status"), bodyFont);
                addPdfCell(table, safe(r, "total"), bodyFont);
                addPdfCell(table, money(r, "jumlah"), bodyFont);
            }
            document.add(table);
            document.add(new Paragraph("Total pendapatan lunas: " + money(data, "total_pendapatan"), bodyFont));
        }
        document.close();
    }

    private void exportExcel(File file, JsonObject laporan) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {
            JsonObject data = dataLaporan(laporan);
            String jenis = safe(laporan, "jenis_laporan");
            if ("pembayaran".equals(jenis)) {
                Sheet sheet = workbook.createSheet("Pembayaran");
                writeRow(sheet.createRow(0), "Referensi", "NIM", "Nama", "Jenis", "Jumlah", "Tanggal", "Metode", "Status");
                int row = 1;
                for (JsonElement el : array(data, "records")) {
                    JsonObject r = el.getAsJsonObject();
                    writeRow(sheet.createRow(row++), safe(r, "nomor_referensi"), safe(r, "nim"), safe(r, "nama_mahasiswa"),
                            safe(r, "jenis_pembayaran"), safe(r, "jumlah"), dateOnly(safe(r, "tanggal_bayar")),
                            safe(r, "metode_pembayaran"), safe(r, "status"));
                }
            } else if ("mahasiswa".equals(jenis)) {
                Sheet jurusan = workbook.createSheet("Per Jurusan");
                writeRow(jurusan.createRow(0), "Jurusan", "Status", "Total");
                int row = 1;
                for (JsonElement el : array(data, "per_jurusan")) {
                    JsonObject r = el.getAsJsonObject();
                    writeRow(jurusan.createRow(row++), safe(r, "jurusan"), safe(r, "status"), safe(r, "total"));
                }
                Sheet angkatan = workbook.createSheet("Per Angkatan");
                writeRow(angkatan.createRow(0), "Angkatan", "Total");
                row = 1;
                for (JsonElement el : array(data, "per_angkatan")) {
                    JsonObject r = el.getAsJsonObject();
                    writeRow(angkatan.createRow(row++), safe(r, "angkatan"), safe(r, "total"));
                }
            } else {
                Sheet sheet = workbook.createSheet("Keuangan");
                writeRow(sheet.createRow(0), "Bulan", "Status", "Transaksi", "Jumlah");
                int row = 1;
                for (JsonElement el : array(data, "pendapatan_bulanan")) {
                    JsonObject r = el.getAsJsonObject();
                    writeRow(sheet.createRow(row++), safe(r, "bulan"), safe(r, "status"), safe(r, "total"), safe(r, "jumlah"));
                }
            }
            workbook.write(out);
        }
    }

    private void exportCsv(File file, JsonObject laporan) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject data = dataLaporan(laporan);
            String jenis = safe(laporan, "jenis_laporan");
            if ("pembayaran".equals(jenis)) {
                writer.write("Referensi,NIM,Nama,Jenis,Jumlah,Tanggal,Metode,Status\n");
                for (JsonElement el : array(data, "records")) {
                    JsonObject r = el.getAsJsonObject();
                    writer.write(csv(safe(r, "nomor_referensi"), safe(r, "nim"), safe(r, "nama_mahasiswa"),
                            safe(r, "jenis_pembayaran"), safe(r, "jumlah"), dateOnly(safe(r, "tanggal_bayar")),
                            safe(r, "metode_pembayaran"), safe(r, "status")) + "\n");
                }
            } else if ("mahasiswa".equals(jenis)) {
                writer.write("Jurusan,Status,Total\n");
                for (JsonElement el : array(data, "per_jurusan")) {
                    JsonObject r = el.getAsJsonObject();
                    writer.write(csv(safe(r, "jurusan"), safe(r, "status"), safe(r, "total")) + "\n");
                }
            } else {
                writer.write("Bulan,Status,Transaksi,Jumlah\n");
                for (JsonElement el : array(data, "pendapatan_bulanan")) {
                    JsonObject r = el.getAsJsonObject();
                    writer.write(csv(safe(r, "bulan"), safe(r, "status"), safe(r, "total"), safe(r, "jumlah")) + "\n");
                }
            }
        }
    }

    private String buildLaporanPreview(JsonObject laporan) {
        JsonObject data = dataLaporan(laporan);
        String jenis = safe(laporan, "jenis_laporan");
        StringBuilder sb = new StringBuilder("Isi laporan:\n");
        if ("pembayaran".equals(jenis)) {
            JsonObject summary = object(data, "summary");
            sb.append("Total transaksi: ").append(safe(summary, "total_transaksi")).append("\n");
            sb.append("Total lunas: ").append(money(summary, "total_lunas")).append("\n");
            sb.append("Total pending: ").append(money(summary, "total_pending")).append("\n\nPreview transaksi:\n");
            int count = 0;
            for (JsonElement el : array(data, "records")) {
                JsonObject r = el.getAsJsonObject();
                sb.append("- ").append(safe(r, "nim")).append(" | ").append(safe(r, "nama_mahasiswa"))
                        .append(" | ").append(money(r, "jumlah")).append(" | ").append(safe(r, "status")).append("\n");
                if (++count >= 10) break;
            }
        } else if ("mahasiswa".equals(jenis)) {
            sb.append("Total mahasiswa: ").append(safe(data, "total")).append("\n\nPer jurusan:\n");
            for (JsonElement el : array(data, "per_jurusan")) {
                JsonObject r = el.getAsJsonObject();
                sb.append("- ").append(safe(r, "jurusan")).append(" / ").append(safe(r, "status"))
                        .append(": ").append(safe(r, "total")).append("\n");
            }
        } else {
            sb.append("Total pendapatan lunas: ").append(money(data, "total_pendapatan")).append("\n\nPendapatan bulanan:\n");
            for (JsonElement el : array(data, "pendapatan_bulanan")) {
                JsonObject r = el.getAsJsonObject();
                sb.append("- ").append(safe(r, "bulan")).append(" | ").append(safe(r, "status"))
                        .append(" | ").append(money(r, "jumlah")).append("\n");
            }
        }
        return sb.toString();
    }

    private void deleteLaporan(int id) {
        int c = JOptionPane.showConfirmDialog(this, "Hapus laporan #" + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.delete(id); }
            protected void done() {
                try { get(); loadData(); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private JsonObject dataLaporan(JsonObject laporan) {
        if (!laporan.has("data_laporan") || laporan.get("data_laporan").isJsonNull()) return new JsonObject();
        JsonElement el = laporan.get("data_laporan");
        if (el.isJsonObject()) return el.getAsJsonObject();
        try {
            return com.google.gson.JsonParser.parseString(el.getAsString()).getAsJsonObject();
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    private JsonArray array(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonArray() ? object.getAsJsonArray(key) : new JsonArray();
    }

    private JsonObject object(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonObject() ? object.getAsJsonObject(key) : new JsonObject();
    }

    private String safe(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "-";
    }

    private String dateOnly(String value) {
        if (value == null || value.equals("-")) return "-";
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private String money(JsonObject object, String key) {
        try {
            return object.has(key) && !object.get(key).isJsonNull()
                    ? RUPIAH.format(object.get(key).getAsDouble())
                    : RUPIAH.format(0);
        } catch (Exception ignored) {
            return RUPIAH.format(0);
        }
    }

    private String slug(String value) {
        String normalized = value == null ? "laporan" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-|-$", "");
        return normalized.isBlank() ? "laporan" : normalized;
    }

    private void addPdfHeader(PdfPTable table, String text) {
        com.itextpdf.text.Font font = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 8);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void writeRow(Row row, String... values) {
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private String csv(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            String value = values[i] == null ? "" : values[i].replace("\"", "\"\"");
            sb.append('"').append(value).append('"');
        }
        return sb.toString();
    }

    private JButton buildBtn(String text, Color bg, int width) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(TEXT_MUTED());
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(width, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField makeField(String hint) {
        JTextField f = new JTextField(14);
        f.setBackground(AppTheme.input());
        f.setForeground(TEXT_PRIMARY());
        f.setCaretColor(TEXT_PRIMARY());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1), new EmptyBorder(6, 10, 6, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setToolTipText(hint);
        return f;
    }

    private JComponent datePickerField(JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);

        JButton pick = smallDateButton("...");
        pick.setToolTipText("Pilih tanggal dari kalender");
        pick.addActionListener(e -> showDatePicker(field));
        panel.add(pick, BorderLayout.EAST);
        return panel;
    }

    private JButton smallDateButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(42, 34));
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(TEXT_PRIMARY());
        button.setBackground(AppTheme.input());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showDatePicker(JTextField target) {
        LocalDate initial = initialCalendarDate(target);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Pilih Tanggal", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(8, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(CARD_BG());

        JComboBox<String> monthCombo = new JComboBox<>(new String[]{
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        });
        JComboBox<Integer> yearCombo = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        int firstYear = Math.min(initial.getYear(), currentYear) - 5;
        int lastYear = Math.max(initial.getYear(), currentYear) + 5;
        for (int year = firstYear; year <= lastYear; year++) yearCombo.addItem(year);
        monthCombo.setSelectedIndex(initial.getMonthValue() - 1);
        yearCombo.setSelectedItem(initial.getYear());
        styleDateCombo(monthCombo, 150);
        styleDateCombo(yearCombo, 90);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        header.setOpaque(false);
        header.add(monthCombo);
        header.add(yearCombo);

        JPanel days = new JPanel(new GridLayout(0, 7, 4, 4));
        days.setOpaque(false);

        Runnable[] renderCalendar = new Runnable[1];
        renderCalendar[0] = () -> {
            days.removeAll();
            String[] labels = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
            for (String label : labels) {
                JLabel dayLabel = new JLabel(label, SwingConstants.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                dayLabel.setForeground(TEXT_MUTED());
                days.add(dayLabel);
            }

            int year = (Integer) yearCombo.getSelectedItem();
            int month = monthCombo.getSelectedIndex() + 1;
            YearMonth yearMonth = YearMonth.of(year, month);
            int firstOffset = yearMonth.atDay(1).getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            for (int i = 0; i < firstOffset; i++) days.add(new JLabel(""));

            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                JButton dayButton = smallDateButton(String.valueOf(day));
                dayButton.setPreferredSize(new Dimension(42, 34));
                if (date.equals(LocalDate.now())) {
                    dayButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BLUE(), 2),
                            new EmptyBorder(4, 7, 4, 7)
                    ));
                }
                dayButton.addActionListener(e -> {
                    target.setText(date.toString());
                    dialog.dispose();
                });
                days.add(dayButton);
            }
            days.revalidate();
            days.repaint();
        };

        monthCombo.addActionListener(e -> renderCalendar[0].run());
        yearCombo.addActionListener(e -> renderCalendar[0].run());
        renderCalendar[0].run();

        JButton today = smallDateButton("Hari Ini");
        today.setPreferredSize(new Dimension(84, 34));
        today.addActionListener(e -> {
            target.setText(LocalDate.now().toString());
            dialog.dispose();
        });
        JButton clear = smallDateButton("Kosong");
        clear.setPreferredSize(new Dimension(78, 34));
        clear.addActionListener(e -> {
            target.setText("");
            dialog.dispose();
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(today);
        footer.add(clear);

        root.add(header, BorderLayout.NORTH);
        root.add(days, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(370, 340));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void styleDateCombo(JComboBox<?> combo, int width) {
        combo.setBackground(AppTheme.input());
        combo.setForeground(TEXT_PRIMARY());
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setPreferredSize(new Dimension(width, 34));
    }

    private LocalDate initialCalendarDate(JTextField target) {
        try {
            String value = target.getText() == null ? "" : target.getText().trim();
            if (!value.isEmpty()) return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
        } catch (DateTimeParseException ignored) {
            // Use today when the field is empty or not in ISO format.
        }
        return LocalDate.now();
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED());
        return l;
    }

    private void styleTable(JTable t) {
        t.setBackground(TABLE_BG());
        t.setForeground(TEXT_PRIMARY());
        t.setSelectionBackground(new Color(59, 130, 246, 60));
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(new Color(20, 30, 55));
        t.setRowHeight(42);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(HEADER_BG());
        h.setForeground(TEXT_MUTED());
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR()));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 40));
    }

    // ── Status Badge Renderer ─────────────────────────────────────────────────
    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String status = String.valueOf(v);
            Color badgeColor = switch (status.toLowerCase()) {
                case "selesai"    -> GREEN();
                case "diproses"   -> YELLOW();
                case "gagal"      -> RED();
                default           -> BLUE();
            };
            JLabel lbl = new JLabel(status.toUpperCase(), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 30));
                    g2.fillRoundRect(2, 5, getWidth() - 4, getHeight() - 10, 10, 10);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(badgeColor);
            lbl.setOpaque(false);
            lbl.setBackground(sel ? new Color(59, 130, 246, 60) : (r % 2 == 0 ? TABLE_BG() : ROW_ALT()));
            return lbl;
        }
    }

    // ── Action Renderer ───────────────────────────────────────────────────────
    class ActionRenderer extends JPanel implements TableCellRenderer {
        ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 6));
            setOpaque(false);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            removeAll();
            add(makeSmallBtn("PDF", BLUE()));
            add(makeSmallBtn("Excel", GREEN()));
            add(makeSmallBtn("CSV", YELLOW()));
            add(makeSmallBtn("👁 Detail", BLUE()));
            add(makeSmallBtn("🗑", RED()));
            setBackground(sel ? new Color(59, 130, 246, 40) : (r % 2 == 0 ? TABLE_BG() : ROW_ALT()));
            return this;
        }
        private JButton makeSmallBtn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 10));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1), new EmptyBorder(3, 6, 3, 6)));
            b.setFocusPainted(false);
            return b;
        }
    }

    // ── Action Editor ─────────────────────────────────────────────────────────
    class ActionEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private final JButton bView = makeSmallBtn("👁 Detail", BLUE());
        private final JButton bDel  = makeSmallBtn("🗑", RED());
        private final JButton bPdf = makeSmallBtn("PDF", BLUE());
        private final JButton bExcel = makeSmallBtn("Excel", GREEN());
        private final JButton bCsv = makeSmallBtn("CSV", YELLOW());
        private int rowId;

        ActionEditor() {
            super(new JCheckBox());
            panel.setBackground(TABLE_BG());
            panel.add(bPdf);
            panel.add(bExcel);
            panel.add(bCsv);
            panel.add(bView);
            panel.add(bDel);
            bPdf.addActionListener(e -> { fireEditingStopped(); exportLaporan(rowId, "pdf"); });
            bExcel.addActionListener(e -> { fireEditingStopped(); exportLaporan(rowId, "excel"); });
            bCsv.addActionListener(e -> { fireEditingStopped(); exportLaporan(rowId, "csv"); });
            bView.addActionListener(e -> { fireEditingStopped(); viewDetail(rowId); });
            bDel.addActionListener(e -> { fireEditingStopped(); deleteLaporan(rowId); });
        }

        private JButton makeSmallBtn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 10));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1), new EmptyBorder(3, 6, 3, 6)));
            b.setFocusPainted(false);
            return b;
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            rowId = (int) t.getValueAt(r, 0);
            return panel;
        }
        @Override public Object getCellEditorValue() { return "aksi"; }
    }
}
