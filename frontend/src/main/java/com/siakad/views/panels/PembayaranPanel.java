package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.MahasiswaService;
import com.siakad.services.PembayaranService;
import com.siakad.utils.AppTheme;
import com.siakad.utils.JwtHelper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * PembayaranPanel - Input Pembayaran UKT & Status Pembayaran
 */
public class PembayaranPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatus, cmbTahunAjaran, cmbJenis;
    private JLabel lblTotal;
    private JButton btnPrev, btnNext;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 12;
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

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

    // Skeleton
    private CardLayout rootCard;
    private JPanel rootPanel;
    private SkeletonPanel skeleton;
    private StatePanel statePanel;

    public PembayaranPanel() {
        setBackground(BG());
        setLayout(new BorderLayout());

        rootCard = new CardLayout();
        rootPanel = new JPanel(rootCard);
        rootPanel.setBackground(BG());
        skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
        statePanel = new StatePanel();
        rootPanel.add(skeleton, "skeleton");

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG());
        initUI(content);
        rootPanel.add(content, "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);

        loadTahunAjaranOptions();
        loadData();
    }

    private void initUI(JPanel target) {
        boolean admin = JwtHelper.getInstance().isAdmin();

        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(admin ? "Pembayaran UKT" : "Riwayat Pembayaran");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY());
        JLabel lblSub = new JLabel(admin
                ? "Input & verifikasi pembayaran mahasiswa"
                : "Pantau status pembayaran yang terhubung dengan akun Anda");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED());
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);
        header.add(titleBlock, BorderLayout.WEST);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);

        if (admin) {
            JPanel searchBox = buildSearchBox();

            cmbStatus = new JComboBox<>(new String[]{"Semua Status", "pending", "lunas", "gagal", "refund"});
            styleCombo(cmbStatus, 140);
            cmbStatus.addActionListener(e -> { currentPage = 1; loadData(); });

            cmbJenis = new JComboBox<>(new String[]{"Semua Jenis", "ukt", "spp", "praktikum", "wisuda", "lainnya"});
            styleCombo(cmbJenis, 130);
            cmbJenis.addActionListener(e -> { currentPage = 1; loadData(); });

            cmbTahunAjaran = new JComboBox<>(new String[]{"Semua TA"});
            styleCombo(cmbTahunAjaran, 120);
            cmbTahunAjaran.addActionListener(e -> { currentPage = 1; loadData(); });

            JButton btnSearch = buildBtn("Cari", BLUE(), 70);
            btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });

            JButton btnTambah = buildBtn("＋  Input Pembayaran", GREEN(), 180);
            btnTambah.addActionListener(e -> showInputForm());

            filterBar.add(searchBox);
            filterBar.add(cmbStatus);
            filterBar.add(cmbJenis);
            filterBar.add(cmbTahunAjaran);
            filterBar.add(btnSearch);
            filterBar.add(btnTambah);
        } else {
            JButton btnRefresh = buildBtn("Refresh", BLUE(), 100);
            btnRefresh.addActionListener(e -> loadData());
            filterBar.add(btnRefresh);
        }
        header.add(filterBar, BorderLayout.EAST);

        // ── Table ──
        String[] cols = {"ID", "NIM", "Nama", "Jenis", "Jumlah", "Tanggal", "Metode", "Smt", "Status", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 9; }
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
        table.getColumnModel().getColumn(7).setMaxWidth(45);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(9).setMinWidth(330);
        table.getColumnModel().getColumn(9).setMaxWidth(330);
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionEditor());
        if (!admin) {
            table.getColumnModel().getColumn(9).setMinWidth(0);
            table.getColumnModel().getColumn(9).setMaxWidth(0);
            table.getColumnModel().getColumn(9).setPreferredWidth(0);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(TABLE_BG());

        JPanel tableCard = buildCard();
        tableCard.setBorder(new EmptyBorder(0, 28, 0, 28));
        tableCard.add(sp);

        // ── Footer ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 28, 14, 28));

        lblTotal = new JLabel("Total: 0");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(TEXT_DIM());

        JPanel pag = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pag.setOpaque(false);
        btnPrev = buildPagBtn("◀  Prev");
        btnNext = buildPagBtn("Next  ▶");
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); }});
        btnNext.addActionListener(e -> { currentPage++; loadData(); });
        pag.add(btnPrev); pag.add(btnNext);

        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(pag, BorderLayout.EAST);

        target.add(header, BorderLayout.NORTH);
        target.add(tableCard, BorderLayout.CENTER);
        target.add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildSearchBox() {
        JPanel box = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(220, 36));

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        icon.setBorder(new EmptyBorder(0, 10, 0, 4));

        txtSearch = new JTextField();
        txtSearch.setOpaque(false);
        txtSearch.setBackground(new Color(0, 0, 0, 0));
        txtSearch.setForeground(TEXT_PRIMARY());
        txtSearch.setCaretColor(TEXT_PRIMARY());
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setToolTipText("Cari NIM atau nama...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { currentPage = 1; loadData(); }
            }
        });

        box.add(icon, BorderLayout.WEST);
        box.add(txtSearch, BorderLayout.CENTER);
        return box;
    }

    private void loadTahunAjaranOptions() {
        if (cmbTahunAjaran == null || !JwtHelper.getInstance().isAdmin()) return;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return PembayaranService.getTahunAjaran();
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (!resp.get("success").getAsBoolean()) return;
                    String selected = cmbTahunAjaran.getSelectedItem() == null
                            ? "Semua TA"
                            : cmbTahunAjaran.getSelectedItem().toString();
                    cmbTahunAjaran.removeAllItems();
                    cmbTahunAjaran.addItem("Semua TA");
                    JsonArray data = resp.getAsJsonArray("data");
                    for (int i = 0; i < data.size(); i++) {
                        cmbTahunAjaran.addItem(data.get(i).getAsString());
                    }
                    cmbTahunAjaran.setSelectedItem(selected);
                    if (cmbTahunAjaran.getSelectedItem() == null) cmbTahunAjaran.setSelectedIndex(0);
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void loadData() {
        skeleton.start();
        rootCard.show(rootPanel, "skeleton");

        boolean admin = JwtHelper.getInstance().isAdmin();
        String nim = JwtHelper.getInstance().getNim();
        String search = admin ? txtSearch.getText().trim() : "";
        String status = admin && cmbStatus.getSelectedIndex() != 0 ? (String) cmbStatus.getSelectedItem() : null;
        String jenis = admin && cmbJenis.getSelectedIndex() != 0 ? (String) cmbJenis.getSelectedItem() : null;
        String ta = admin && cmbTahunAjaran.getSelectedIndex() != 0 ? (String) cmbTahunAjaran.getSelectedItem() : null;

        if (!admin && (nim == null || nim.isBlank())) {
            skeleton.stop();
            statePanel.showState("!", "Riwayat tidak tersedia",
                    "Akun ini belum memiliki NIM, sehingga riwayat pembayaran tidak bisa ditampilkan.",
                    "Coba lagi", this::loadData);
            rootCard.show(rootPanel, "state");
            return;
        }

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return admin
                        ? PembayaranService.getAll(currentPage, PAGE_SIZE, search, status, ta, jenis)
                        : PembayaranService.getByNim(nim);
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    tableModel.setRowCount(0);
                    if (resp.get("success").getAsBoolean()) {
                        JsonArray data;
                        if (admin) {
                            data = resp.getAsJsonArray("data");
                            JsonObject pg = resp.getAsJsonObject("pagination");
                            int totalPages = pg.get("totalPages").getAsInt();
                            if (data.size() == 0) {
                                showEmptyState(search.isEmpty() ? "Belum ada transaksi pembayaran." :
                                        "Tidak ada transaksi yang cocok dengan filter aktif.", true);
                                return;
                            }
                            lblTotal.setText("Menampilkan " + data.size() + " dari " + pg.get("total").getAsInt()
                                    + " transaksi  |  Hal " + currentPage + "/" + totalPages);
                            btnPrev.setEnabled(currentPage > 1);
                            btnNext.setEnabled(currentPage < totalPages);
                        } else {
                            data = resp.getAsJsonObject("data").getAsJsonArray("pembayaran");
                            if (data.size() == 0) {
                                showEmptyState("Belum ada riwayat pembayaran untuk akun ini.", false);
                                return;
                            }
                            lblTotal.setText("Total: " + data.size() + " riwayat pembayaran");
                            btnPrev.setEnabled(false);
                            btnNext.setEnabled(false);
                        }
                        fillRows(data);
                        rootCard.show(rootPanel, "content");
                    } else {
                        showErrorState(resp.has("message") ? resp.get("message").getAsString() : "Gagal memuat pembayaran.");
                    }
                } catch (Exception e) {
                    showErrorState("Gagal memuat pembayaran: " + e.getMessage());
                } finally {
                    skeleton.stop();
                }
            }
        }.execute();
    }

    private void fillRows(JsonArray data) {
        for (int i = 0; i < data.size(); i++) {
            JsonObject p = data.get(i).getAsJsonObject();
            String tanggal = safe(p, "tanggal_bayar");
            tableModel.addRow(new Object[]{
                    p.get("id").getAsInt(),
                    safe(p, "nim"),
                    safe(p, "nama_mahasiswa"),
                    safe(p, "jenis_pembayaran"),
                    RUPIAH.format(p.get("jumlah").getAsDouble()),
                    tanggal.length() >= 10 ? tanggal.substring(0, 10) : tanggal,
                    safe(p, "metode_pembayaran"),
                    p.get("semester").getAsInt(),
                    safe(p, "status"),
                    "aksi"
            });
        }
    }

    private void showEmptyState(String message, boolean resetFilters) {
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        lblTotal.setText("Tidak ada data");
        statePanel.showState("0", "Data pembayaran kosong", message,
                resetFilters ? "Reset filter" : "Muat ulang",
                () -> {
                    if (resetFilters && txtSearch != null) {
                        txtSearch.setText("");
                        cmbStatus.setSelectedIndex(0);
                        cmbJenis.setSelectedIndex(0);
                        cmbTahunAjaran.setSelectedIndex(0);
                        currentPage = 1;
                    }
                    loadData();
                });
        rootCard.show(rootPanel, "state");
    }

    private void showErrorState(String message) {
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        statePanel.showState("!", "Pembayaran tidak bisa dimuat", message, "Muat ulang", this::loadData);
        rootCard.show(rootPanel, "state");
    }

    private void showInputForm() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Input Pembayaran UKT", true);
        d.setSize(560, 650);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG());
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 4, 5, 4);
        g.weightx = 1;

        JComboBox<Object> cMahasiswa = buildMahasiswaCombo();
        JTextField fNama     = makeReadonlyField();
        JTextField fJurusan  = makeReadonlyField();
        JTextField fJumlah   = makeField();
        JTextField fTanggal  = makeField();
        fTanggal.setText(LocalDate.now().toString());
        fTanggal.setToolTipText("Pilih tanggal dari kalender atau isi format YYYY-MM-DD");
        JTextField fSemester = makeField();
        JTextField fTA       = makeField(); fTA.setText("2024/2025");
        JComboBox<String> cJenis  = new JComboBox<>(new String[]{"ukt", "spp", "praktikum", "wisuda", "lainnya"});
        JComboBox<String> cMetode = new JComboBox<>(new String[]{"transfer_bank", "virtual_account", "tunai", "qris"});
        styleCombo(cJenis, 0); styleCombo(cMetode, 0);
        JTextArea fKet = new JTextArea(2, 20);
        fKet.setBackground(AppTheme.input());
        fKet.setForeground(TEXT_PRIMARY());
        fKet.setCaretColor(TEXT_PRIMARY());
        fKet.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1), new EmptyBorder(6, 8, 6, 8)));
        fKet.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblTarif = new JLabel("Pilih mahasiswa untuk mengambil tarif UKT otomatis.");
        lblTarif.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTarif.setForeground(TEXT_DIM());

        JButton btnAutoTarif = buildPagBtn("Auto Tarif");
        btnAutoTarif.addActionListener(e -> suggestTarifUkt(
                selectedNim(cMahasiswa), fJumlah, fSemester, fTA, cJenis, lblTarif, true));

        JPanel jumlahPanel = new JPanel(new BorderLayout(6, 3));
        jumlahPanel.setOpaque(false);
        JPanel jumlahLine = new JPanel(new BorderLayout(6, 0));
        jumlahLine.setOpaque(false);
        jumlahLine.add(fJumlah, BorderLayout.CENTER);
        jumlahLine.add(btnAutoTarif, BorderLayout.EAST);
        jumlahPanel.add(jumlahLine, BorderLayout.CENTER);
        jumlahPanel.add(lblTarif, BorderLayout.SOUTH);

        final File[] selectedBukti = new File[1];
        JLabel lblBukti = new JLabel("Belum ada file bukti dipilih");
        lblBukti.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBukti.setForeground(TEXT_DIM());
        JButton btnBukti = buildPagBtn("Pilih File");
        btnBukti.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Bukti Pembayaran (JPG, PNG, WEBP, PDF)",
                    "jpg", "jpeg", "png", "webp", "pdf"));
            if (chooser.showOpenDialog(d) != JFileChooser.APPROVE_OPTION) return;
            selectedBukti[0] = chooser.getSelectedFile();
            lblBukti.setText(selectedBukti[0].getName());
        });
        JPanel buktiPanel = new JPanel(new BorderLayout(6, 0));
        buktiPanel.setOpaque(false);
        buktiPanel.add(btnBukti, BorderLayout.WEST);
        buktiPanel.add(lblBukti, BorderLayout.CENTER);

        cMahasiswa.addActionListener(e -> {
            MahasiswaOption option = selectedMahasiswaOption(cMahasiswa);
            if (option == null) {
                fNama.setText("");
                fJurusan.setText("");
                return;
            }
            fNama.setText(option.nama);
            fJurusan.setText(option.jurusan);
            if (fSemester.getText().trim().isEmpty() && option.semester > 0) {
                fSemester.setText(String.valueOf(option.semester));
            }
            suggestTarifUkt(option.nim, fJumlah, fSemester, fTA, cJenis, lblTarif, true);
        });
        cJenis.addActionListener(e -> suggestTarifUkt(
                selectedNim(cMahasiswa), fJumlah, fSemester, fTA, cJenis, lblTarif, true));
        fSemester.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                suggestTarifUkt(selectedNim(cMahasiswa), fJumlah, fSemester, fTA, cJenis, lblTarif, false);
            }
        });
        fTA.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                suggestTarifUkt(selectedNim(cMahasiswa), fJumlah, fSemester, fTA, cJenis, lblTarif, false);
            }
        });
        loadMahasiswaOptions(cMahasiswa);

        int r = 0;
        addRow(p, g, r++, "Mahasiswa *", cMahasiswa);
        addRow(p, g, r++, "Nama", fNama);
        addRow(p, g, r++, "Jurusan", fJurusan);
        addRow(p, g, r++, "Jenis Pembayaran", cJenis);
        addRow(p, g, r++, "Jumlah (Rp) *", jumlahPanel);
        addRow(p, g, r++, "Tanggal Bayar *", datePickerField(fTanggal));
        addRow(p, g, r++, "Metode Pembayaran", cMetode);
        addRow(p, g, r++, "Semester *", fSemester);
        addRow(p, g, r++, "Tahun Ajaran *", fTA);
        addRow(p, g, r++, "Bukti Pembayaran", buktiPanel);
        g.gridx = 0; g.gridy = r; p.add(makeLabel("Keterangan"), g);
        g.gridx = 1; p.add(new JScrollPane(fKet) {{ setBorder(null); }}, g); r++;

        JButton btnSave = buildBtn("💾  Simpan Pembayaran", BLUE(), 220);
        g.gridx = 0; g.gridy = r; g.gridwidth = 2; g.insets = new Insets(18, 4, 4, 4);
        p.add(btnSave, g);

        btnSave.addActionListener(e -> {
            String nim = selectedNim(cMahasiswa);
            if (nim.isEmpty() || fJumlah.getText().isEmpty()
                    || fTanggal.getText().isEmpty() || fSemester.getText().isEmpty() || fTA.getText().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Isi semua field wajib (*).", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double jumlah;
            int semester;
            try {
                jumlah = Double.parseDouble(fJumlah.getText().trim());
                semester = Integer.parseInt(fSemester.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Jumlah dan semester harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("nim", nim);
            body.addProperty("jenis_pembayaran", (String) cJenis.getSelectedItem());
            body.addProperty("jumlah", jumlah);
            body.addProperty("tanggal_bayar", fTanggal.getText().trim());
            body.addProperty("metode_pembayaran", (String) cMetode.getSelectedItem());
            body.addProperty("semester", semester);
            body.addProperty("tahun_ajaran", fTA.getText().trim());
            body.addProperty("keterangan", fKet.getText().trim());

            new SwingWorker<JsonObject, Void>() {
                protected JsonObject doInBackground() throws Exception {
                    JsonObject created = PembayaranService.create(body);
                    if (created.has("success") && created.get("success").getAsBoolean()
                            && selectedBukti[0] != null && created.has("data")
                            && created.getAsJsonObject("data").has("id")) {
                        int paymentId = created.getAsJsonObject("data").get("id").getAsInt();
                        try {
                            JsonObject upload = PembayaranService.uploadBukti(paymentId, selectedBukti[0]);
                            if (upload.has("success") && upload.get("success").getAsBoolean()) {
                                created.addProperty("message", created.get("message").getAsString()
                                        + " Bukti pembayaran juga berhasil diupload.");
                            } else {
                                String uploadMessage = upload.has("message") ? upload.get("message").getAsString() : "Upload bukti gagal.";
                                created.addProperty("message", created.get("message").getAsString()
                                        + " Namun " + uploadMessage);
                            }
                        } catch (Exception uploadError) {
                            created.addProperty("message", created.get("message").getAsString()
                                    + " Namun upload bukti gagal: " + uploadError.getMessage());
                        }
                    }
                    return created;
                }
                protected void done() {
                    try {
                        JsonObject res = get();
                        if (res.get("success").getAsBoolean()) {
                            JOptionPane.showMessageDialog(d, res.get("message").getAsString(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            d.dispose(); loadData();
                        } else {
                            JOptionPane.showMessageDialog(d, res.get("message").getAsString(), "Gagal", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage()); }
                }
            }.execute();
        });

        d.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(CARD_BG()); }});
        d.setVisible(true);
    }

    private void showEditForm(int id) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return PembayaranService.getById(id); }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (!resp.get("success").getAsBoolean()) {
                        JOptionPane.showMessageDialog(PembayaranPanel.this, resp.get("message").getAsString(), "Gagal", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JsonObject pmt = resp.getAsJsonObject("data");
                    JTextField fJumlah = makeField(); fJumlah.setText(safe(pmt, "jumlah"));
                    JTextField fTanggal = makeField();
                    fTanggal.setText(dateOnly(safe(pmt, "tanggal_bayar")));
                    fTanggal.setToolTipText("Pilih tanggal dari kalender atau isi format YYYY-MM-DD");
                    JTextField fSemester = makeField(); fSemester.setText(safe(pmt, "semester"));
                    JTextField fTA = makeField(); fTA.setText(safe(pmt, "tahun_ajaran"));
                    JComboBox<String> cJenis = new JComboBox<>(new String[]{"ukt", "spp", "praktikum", "wisuda", "lainnya"});
                    cJenis.setSelectedItem(safe(pmt, "jenis_pembayaran"));
                    JComboBox<String> cMetode = new JComboBox<>(new String[]{"transfer_bank", "virtual_account", "tunai", "qris"});
                    cMetode.setSelectedItem(safe(pmt, "metode_pembayaran"));
                    JTextArea fKet = new JTextArea("-".equals(safe(pmt, "keterangan")) ? "" : safe(pmt, "keterangan"), 3, 22);
                    fKet.setBackground(AppTheme.input());
                    fKet.setForeground(TEXT_PRIMARY());
                    fKet.setCaretColor(TEXT_PRIMARY());

                    JPanel panel = new JPanel(new GridLayout(7, 2, 8, 8));
                    panel.setBackground(CARD_BG());
                    panel.add(makeLabel("Jenis")); panel.add(cJenis);
                    panel.add(makeLabel("Jumlah")); panel.add(fJumlah);
                    panel.add(makeLabel("Tanggal")); panel.add(datePickerField(fTanggal));
                    panel.add(makeLabel("Metode")); panel.add(cMetode);
                    panel.add(makeLabel("Semester")); panel.add(fSemester);
                    panel.add(makeLabel("Tahun Ajaran")); panel.add(fTA);
                    panel.add(makeLabel("Keterangan")); panel.add(new JScrollPane(fKet));

                    int result = JOptionPane.showConfirmDialog(PembayaranPanel.this, panel,
                            "Edit Pembayaran #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result != JOptionPane.OK_OPTION) return;

                    JsonObject body = new JsonObject();
                    body.addProperty("jenis_pembayaran", (String) cJenis.getSelectedItem());
                    body.addProperty("jumlah", Double.parseDouble(fJumlah.getText().trim()));
                    body.addProperty("tanggal_bayar", fTanggal.getText().trim());
                    body.addProperty("metode_pembayaran", (String) cMetode.getSelectedItem());
                    body.addProperty("semester", Integer.parseInt(fSemester.getText().trim()));
                    body.addProperty("tahun_ajaran", fTA.getText().trim());
                    body.addProperty("keterangan", fKet.getText().trim());
                    saveEdit(id, body);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void saveEdit(int id, JsonObject body) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return PembayaranService.update(id, body); }
            protected void done() {
                try {
                    JsonObject res = get();
                    JOptionPane.showMessageDialog(PembayaranPanel.this, res.get("message").getAsString());
                    if (res.get("success").getAsBoolean()) {
                        loadTahunAjaranOptions();
                        loadData();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void verifyPembayaran(int id, String newStatus) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Ubah status pembayaran #" + id + " menjadi " + newStatus.toUpperCase() + "?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return PembayaranService.updateStatus(id, newStatus); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(PembayaranPanel.this, r.get("message").getAsString());
                    loadData();
                } catch (Exception e) { JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: " + e.getMessage()); }
            }
        }.execute();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void chooseStatus(int id) {
        String status = (String) JOptionPane.showInputDialog(this,
                "Pilih status pembayaran:",
                "Ubah Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"pending", "lunas", "gagal", "refund"},
                "lunas");
        if (status != null) verifyPembayaran(id, status);
    }

    private void manageBukti(int id) {
        String action = (String) JOptionPane.showInputDialog(this,
                "Kelola bukti pembayaran #" + id,
                "Bukti Pembayaran",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"Upload Bukti", "Download Bukti"},
                "Upload Bukti");
        if (action == null) return;
        if (action.startsWith("Upload")) uploadBukti(id);
        else downloadBukti(id);
    }

    private void uploadBukti(int id) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih Bukti Pembayaran");
        chooser.setFileFilter(new FileNameExtensionFilter("Bukti Pembayaran (JPG, PNG, WEBP, PDF)", "jpg", "jpeg", "png", "webp", "pdf"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return PembayaranService.uploadBukti(id, file); }
            protected void done() {
                try {
                    JsonObject res = get();
                    JOptionPane.showMessageDialog(PembayaranPanel.this, res.get("message").getAsString());
                    if (res.get("success").getAsBoolean()) loadData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: " + e.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void downloadBukti(int id) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Bukti Pembayaran");
        chooser.setSelectedFile(new File("bukti-pembayaran-" + id));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                PembayaranService.downloadBukti(id, target);
                return null;
            }
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(PembayaranPanel.this,
                            "Bukti pembayaran tersimpan di:\n" + target.getAbsolutePath());
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(target);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: " + e.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void deletePembayaran(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus pembayaran #" + id + "? Pembayaran lunas tidak dapat dihapus.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return PembayaranService.delete(id); }
            protected void done() {
                try {
                    JsonObject res = get();
                    JOptionPane.showMessageDialog(PembayaranPanel.this, res.get("message").getAsString());
                    if (res.get("success").getAsBoolean()) {
                        loadTahunAjaranOptions();
                        loadData();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: " + e.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void printKwitansi(int id) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return PembayaranService.getById(id); }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (!resp.get("success").getAsBoolean()) {
                        JOptionPane.showMessageDialog(PembayaranPanel.this, resp.get("message").getAsString(), "Gagal", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JsonObject data = resp.getAsJsonObject("data");
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Simpan Kwitansi PDF");
                    chooser.setFileFilter(new FileNameExtensionFilter("PDF Document", "pdf"));
                    chooser.setSelectedFile(new File("kwitansi-" + safe(data, "nomor_referensi") + ".pdf"));
                    if (chooser.showSaveDialog(PembayaranPanel.this) != JFileChooser.APPROVE_OPTION) return;
                    File file = chooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".pdf")) {
                        file = new File(file.getParentFile(), file.getName() + ".pdf");
                    }
                    exportKwitansiPdf(file, data);
                    JOptionPane.showMessageDialog(PembayaranPanel.this,
                            "Kwitansi berhasil dicetak ke:\n" + file.getAbsolutePath());
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Gagal cetak kwitansi: " + e.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void exportKwitansiPdf(File file, JsonObject data) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16);
        com.itextpdf.text.Font labelFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10);
        com.itextpdf.text.Font bodyFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("KWITANSI PEMBAYARAN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Sistem Informasi Akademik"));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(new float[]{2.2f, 4f});
        table.setWidthPercentage(100);
        addReceiptRow(table, "Nomor Referensi", safe(data, "nomor_referensi"), labelFont, bodyFont);
        addReceiptRow(table, "NIM", safe(data, "nim"), labelFont, bodyFont);
        addReceiptRow(table, "Nama", safe(data, "nama_mahasiswa"), labelFont, bodyFont);
        addReceiptRow(table, "Jenis Pembayaran", safe(data, "jenis_pembayaran").toUpperCase(), labelFont, bodyFont);
        addReceiptRow(table, "Jumlah", RUPIAH.format(data.get("jumlah").getAsDouble()), labelFont, bodyFont);
        addReceiptRow(table, "Tanggal Bayar", dateOnly(safe(data, "tanggal_bayar")), labelFont, bodyFont);
        addReceiptRow(table, "Metode", safe(data, "metode_pembayaran"), labelFont, bodyFont);
        addReceiptRow(table, "Semester", safe(data, "semester"), labelFont, bodyFont);
        addReceiptRow(table, "Tahun Ajaran", safe(data, "tahun_ajaran"), labelFont, bodyFont);
        addReceiptRow(table, "Status", safe(data, "status").toUpperCase(), labelFont, bodyFont);
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Dokumen ini dicetak dari aplikasi Sistem Informasi Akademik.", bodyFont));
        document.close();
    }

    private void addReceiptRow(PdfPTable table, String label, String value,
                               com.itextpdf.text.Font labelFont,
                               com.itextpdf.text.Font bodyFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, bodyFont));
        labelCell.setPadding(6);
        valueCell.setPadding(6);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String dateOnly(String value) {
        if (value == null || value.equals("-")) return "";
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private String safe(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "-";
    }

    private String textOf(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private int intOf(JsonObject obj, String key) {
        try {
            return obj != null && obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static class MahasiswaOption {
        final String nim;
        final String nama;
        final String jurusan;
        final String programStudi;
        final int angkatan;
        final int semester;

        MahasiswaOption(String nim, String nama, String jurusan, String programStudi, int angkatan, int semester) {
            this.nim = nim;
            this.nama = nama;
            this.jurusan = jurusan;
            this.programStudi = programStudi;
            this.angkatan = angkatan;
            this.semester = semester;
        }

        String displayName() {
            return nim + " - " + nama + (jurusan.isEmpty() ? "" : " (" + jurusan + ")");
        }

        @Override public String toString() {
            return displayName();
        }
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setOpaque(false);
        return card;
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

    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setBackground(AppTheme.input());
        f.setForeground(TEXT_PRIMARY());
        f.setCaretColor(TEXT_PRIMARY());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1), new EmptyBorder(7, 10, 7, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setPreferredSize(new Dimension(240, 34));
        return f;
    }

    private JTextField makeReadonlyField() {
        JTextField f = makeField();
        f.setEditable(false);
        f.setForeground(TEXT_DIM());
        return f;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED());
        return l;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(makeLabel(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    private JComboBox<Object> buildMahasiswaCombo() {
        JComboBox<Object> combo = new JComboBox<>();
        combo.setEditable(true);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Object display = value instanceof MahasiswaOption ? ((MahasiswaOption) value).displayName() : value;
                return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
            }
        });
        styleCombo(combo, 0);
        combo.setPreferredSize(new Dimension(260, 36));
        combo.addItem("Memuat data mahasiswa...");
        return combo;
    }

    private void loadMahasiswaOptions(JComboBox<Object> combo) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getAll(1, 200, "");
            }

            protected void done() {
                try {
                    JsonObject response = get();
                    DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
                    if (response.has("success") && response.get("success").getAsBoolean()
                            && response.has("data") && response.get("data").isJsonArray()) {
                        JsonArray rows = response.getAsJsonArray("data");
                        for (JsonElement element : rows) {
                            JsonObject m = element.getAsJsonObject();
                            model.addElement(new MahasiswaOption(
                                    textOf(m, "nim"),
                                    textOf(m, "nama"),
                                    textOf(m, "jurusan"),
                                    textOf(m, "program_studi"),
                                    intOf(m, "angkatan"),
                                    intOf(m, "semester")
                            ));
                        }
                    }
                    if (model.getSize() == 0) model.addElement("Ketik NIM mahasiswa");
                    combo.setModel(model);
                    combo.setSelectedIndex(0);
                } catch (Exception ex) {
                    DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
                    model.addElement("Ketik NIM mahasiswa");
                    combo.setModel(model);
                }
            }
        }.execute();
    }

    private MahasiswaOption selectedMahasiswaOption(JComboBox<Object> combo) {
        Object selected = combo.getSelectedItem();
        if (selected instanceof MahasiswaOption) return (MahasiswaOption) selected;
        Object editorItem = combo.isEditable() ? combo.getEditor().getItem() : null;
        return editorItem instanceof MahasiswaOption ? (MahasiswaOption) editorItem : null;
    }

    private String selectedNim(JComboBox<Object> combo) {
        Object item = combo.isEditable() ? combo.getEditor().getItem() : combo.getSelectedItem();
        if (item instanceof MahasiswaOption) return ((MahasiswaOption) item).nim;
        String text = String.valueOf(item == null ? "" : item).trim();
        if (text.equalsIgnoreCase("Memuat data mahasiswa...") || text.equalsIgnoreCase("Ketik NIM mahasiswa")) {
            return "";
        }
        int dash = text.indexOf(" - ");
        return (dash > 0 ? text.substring(0, dash) : text).trim();
    }

    private void suggestTarifUkt(String nim, JTextField jumlahField, JTextField semesterField,
                                 JTextField tahunAjaranField, JComboBox<String> jenisCombo,
                                 JLabel statusLabel, boolean force) {
        if (!"ukt".equals(jenisCombo.getSelectedItem())) {
            statusLabel.setText("Auto tarif hanya untuk jenis pembayaran UKT.");
            return;
        }
        if (nim == null || nim.trim().isEmpty()) {
            statusLabel.setText("Pilih mahasiswa untuk mengambil tarif UKT otomatis.");
            return;
        }

        int semester;
        try {
            semester = Integer.parseInt(semesterField.getText().trim());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Isi semester lebih dulu untuk auto tarif.");
            return;
        }

        String tahunAjaran = tahunAjaranField.getText().trim();
        statusLabel.setText("Mengambil tarif UKT...");
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return PembayaranService.getTarifUkt(nim.trim(), semester, tahunAjaran);
            }

            protected void done() {
                try {
                    JsonObject response = get();
                    if (!response.has("success") || !response.get("success").getAsBoolean()
                            || !response.has("data") || response.get("data").isJsonNull()) {
                        statusLabel.setText(response.has("message") ? response.get("message").getAsString() : "Tarif UKT belum tersedia.");
                        return;
                    }
                    JsonObject data = response.getAsJsonObject("data");
                    if (!data.has("tarif") || data.get("tarif").isJsonNull()) {
                        statusLabel.setText(response.has("message") ? response.get("message").getAsString() : "Tarif UKT belum tersedia.");
                        return;
                    }
                    JsonObject tarif = data.getAsJsonObject("tarif");
                    String nominal = tarif.get("nominal").getAsString();
                    if (force || jumlahField.getText().trim().isEmpty()) {
                        jumlahField.setText(String.valueOf((long) Double.parseDouble(nominal)));
                    }
                    statusLabel.setText("Tarif: " + RUPIAH.format(Double.parseDouble(nominal))
                            + " - " + textOf(tarif, "keterangan"));
                } catch (Exception ex) {
                    statusLabel.setText("Gagal mengambil tarif: " + ex.getMessage());
                }
            }
        }.execute();
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
        for (int year = firstYear; year <= lastYear; year++) {
            yearCombo.addItem(year);
        }
        monthCombo.setSelectedIndex(initial.getMonthValue() - 1);
        yearCombo.setSelectedItem(initial.getYear());
        styleCombo(monthCombo, 150);
        styleCombo(yearCombo, 90);

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
            for (int i = 0; i < firstOffset; i++) {
                days.add(new JLabel(""));
            }

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
                if (date.equals(initialCalendarDate(target))) {
                    dayButton.setBackground(BLUE());
                    dayButton.setForeground(Color.WHITE);
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

        JButton today = buildPagBtn("Hari Ini");
        today.addActionListener(e -> {
            target.setText(LocalDate.now().toString());
            dialog.dispose();
        });
        JButton cancel = buildPagBtn("Batal");
        cancel.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(today);
        footer.add(cancel);

        root.add(header, BorderLayout.NORTH);
        root.add(days, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(370, 340));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private LocalDate initialCalendarDate(JTextField target) {
        try {
            String value = target.getText() == null ? "" : target.getText().trim();
            if (!value.isEmpty()) {
                return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
            }
        } catch (DateTimeParseException ignored) {
            // Fall back to today when the field is empty or not in ISO date format.
        }
        return LocalDate.now();
    }

    private void styleCombo(JComboBox<?> c, int width) {
        c.setBackground(CARD_BG());
        c.setForeground(TEXT_MUTED());
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (width > 0) c.setPreferredSize(new Dimension(width, 36));
    }

    private JButton buildBtn(String text, Color bg, int width) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(width > 0 ? width : 80, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildPagBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(TEXT_MUTED());
        btn.setBackground(CARD_BG());
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1), new EmptyBorder(5, 12, 5, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Status Badge Renderer ─────────────────────────────────────────────────
    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String status = String.valueOf(v);
            Color badgeColor = getBadgeColor(status);
            JLabel lbl = new JLabel(getStatusLabel(status), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 30));
                    g2.fillRoundRect(2, 5, getWidth() - 4, getHeight() - 10, 10, 10);
                    g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 80));
                    g2.drawRoundRect(2, 5, getWidth() - 5, getHeight() - 11, 10, 10);
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

        private Color getBadgeColor(String s) {
            return switch (s.toLowerCase()) {
                case "lunas"  -> GREEN();
                case "pending" -> YELLOW();
                case "gagal"  -> RED();
                case "refund" -> PURPLE();
                default       -> TEXT_MUTED();
            };
        }

        private String getStatusLabel(String s) {
            return switch (s.toLowerCase()) {
                case "lunas"   -> "✅ LUNAS";
                case "pending" -> "⏳ PENDING";
                case "gagal"   -> "❌ GAGAL";
                case "refund"  -> "↩ REFUND";
                default        -> s.toUpperCase();
            };
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
            if (JwtHelper.getInstance().isAdmin()) {
                add(makeSmallBtn("Edit", BLUE()));
                add(makeSmallBtn("Status", GREEN()));
                add(makeSmallBtn("Bukti", PURPLE()));
                add(makeSmallBtn("PDF", YELLOW()));
                add(makeSmallBtn("Hapus", RED()));
                add(makeSmallBtn("✅ Lunas", GREEN()));
                add(makeSmallBtn("❌ Gagal", RED()));
            }
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
        private final JButton bLunas = makeSmallBtn("✅ Lunas", GREEN());
        private final JButton bGagal = makeSmallBtn("❌ Gagal", RED());
        private final JButton bEdit = makeSmallBtn("Edit", BLUE());
        private final JButton bStatus = makeSmallBtn("Status", GREEN());
        private final JButton bBukti = makeSmallBtn("Bukti", PURPLE());
        private final JButton bPdf = makeSmallBtn("PDF", YELLOW());
        private final JButton bDelete = makeSmallBtn("Hapus", RED());
        private int rowId;

        ActionEditor() {
            super(new JCheckBox());
            panel.setBackground(TABLE_BG());
            panel.add(bEdit);
            panel.add(bStatus);
            panel.add(bBukti);
            panel.add(bPdf);
            panel.add(bDelete);
            panel.add(bLunas);
            panel.add(bGagal);
            bEdit.addActionListener(e -> { fireEditingStopped(); showEditForm(rowId); });
            bStatus.addActionListener(e -> { fireEditingStopped(); chooseStatus(rowId); });
            bBukti.addActionListener(e -> { fireEditingStopped(); manageBukti(rowId); });
            bPdf.addActionListener(e -> { fireEditingStopped(); printKwitansi(rowId); });
            bDelete.addActionListener(e -> { fireEditingStopped(); deletePembayaran(rowId); });
            bLunas.addActionListener(e -> { fireEditingStopped(); verifyPembayaran(rowId, "lunas"); });
            bGagal.addActionListener(e -> { fireEditingStopped(); verifyPembayaran(rowId, "gagal"); });
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
