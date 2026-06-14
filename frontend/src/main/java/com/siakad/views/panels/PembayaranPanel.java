package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;
import com.siakad.services.PembayaranService;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * PembayaranPanel - Input Pembayaran UKT & Status Pembayaran
 */
public class PembayaranPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatus, cmbTahunAjaran;
    private JLabel lblTotal;
    private JButton btnPrev, btnNext;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 12;
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // Warna tema
    private static final Color BG           = new Color(13, 19, 38);
    private static final Color CARD_BG      = new Color(18, 26, 48);
    private static final Color TABLE_BG     = new Color(15, 22, 42);
    private static final Color HEADER_BG    = new Color(10, 15, 30);
    private static final Color BORDER_COLOR = new Color(25, 36, 65);
    private static final Color ROW_ALT      = new Color(20, 29, 52);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_MUTED   = new Color(148, 163, 184);
    private static final Color TEXT_DIM     = new Color(71, 85, 105);
    private static final Color BLUE         = new Color(59, 130, 246);
    private static final Color GREEN        = new Color(34, 197, 94);
    private static final Color YELLOW       = new Color(234, 179, 8);
    private static final Color RED          = new Color(239, 68, 68);
    private static final Color PURPLE       = new Color(168, 85, 247);

    // Skeleton
    private CardLayout rootCard;
    private JPanel rootPanel;
    private SkeletonPanel skeleton;
    private StatePanel statePanel;

    public PembayaranPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());

        rootCard = new CardLayout();
        rootPanel = new JPanel(rootCard);
        rootPanel.setBackground(BG);
        skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
        statePanel = new StatePanel();
        rootPanel.add(skeleton, "skeleton");

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        initUI(content);
        rootPanel.add(content, "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);

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
        lblTitle.setForeground(TEXT_PRIMARY);
        JLabel lblSub = new JLabel(admin
                ? "Input & verifikasi pembayaran mahasiswa"
                : "Pantau status pembayaran yang terhubung dengan akun Anda");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
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

            cmbTahunAjaran = new JComboBox<>(new String[]{"Semua TA", "2024/2025", "2023/2024"});
            styleCombo(cmbTahunAjaran, 120);
            cmbTahunAjaran.addActionListener(e -> { currentPage = 1; loadData(); });

            JButton btnSearch = buildBtn("Cari", BLUE, 70);
            btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });

            JButton btnTambah = buildBtn("＋  Input Pembayaran", GREEN, 180);
            btnTambah.addActionListener(e -> showInputForm());

            filterBar.add(searchBox);
            filterBar.add(cmbStatus);
            filterBar.add(cmbTahunAjaran);
            filterBar.add(btnSearch);
            filterBar.add(btnTambah);
        } else {
            JButton btnRefresh = buildBtn("Refresh", BLUE, 100);
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
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                return c;
            }
        };
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(7).setMaxWidth(45);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(9).setMinWidth(150);
        table.getColumnModel().getColumn(9).setMaxWidth(150);
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionEditor());
        if (!admin) {
            table.getColumnModel().getColumn(9).setMinWidth(0);
            table.getColumnModel().getColumn(9).setMaxWidth(0);
            table.getColumnModel().getColumn(9).setPreferredWidth(0);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(TABLE_BG);

        JPanel tableCard = buildCard();
        tableCard.setBorder(new EmptyBorder(0, 28, 0, 28));
        tableCard.add(sp);

        // ── Footer ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 28, 14, 28));

        lblTotal = new JLabel("Total: 0");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(TEXT_DIM);

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
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
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
        txtSearch.setForeground(TEXT_PRIMARY);
        txtSearch.setCaretColor(TEXT_PRIMARY);
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

    private void loadData() {
        skeleton.start();
        rootCard.show(rootPanel, "skeleton");

        boolean admin = JwtHelper.getInstance().isAdmin();
        String nim = JwtHelper.getInstance().getNim();
        String search = admin ? txtSearch.getText().trim() : "";
        String status = admin && cmbStatus.getSelectedIndex() != 0 ? (String) cmbStatus.getSelectedItem() : null;
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
                        ? PembayaranService.getAll(currentPage, PAGE_SIZE, search, status, ta)
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
        d.setSize(460, 500);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 4, 5, 4);
        g.weightx = 1;

        JTextField fNim      = makeField();
        JTextField fJumlah   = makeField();
        JTextField fTanggal  = makeField(); fTanggal.setToolTipText("Format: YYYY-MM-DD");
        JComboBox<SemesterOption> cmbSemester = buildSemesterCombo();
        JTextField fTA       = makeField(); fTA.setText("2024/2025");
        JComboBox<String> cJenis  = new JComboBox<>(new String[]{"ukt", "spp", "praktikum", "wisuda", "lainnya"});
        JComboBox<String> cMetode = new JComboBox<>(new String[]{"transfer_bank", "virtual_account", "tunai", "qris"});
        styleCombo(cJenis, 0); styleCombo(cMetode, 0);
        JTextArea fKet = new JTextArea(2, 20);
        fKet.setBackground(new Color(13, 19, 38));
        fKet.setForeground(TEXT_PRIMARY);
        fKet.setCaretColor(TEXT_PRIMARY);
        fKet.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(6, 8, 6, 8)));
        fKet.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        int r = 0;
        addRow(p, g, r++, "NIM Mahasiswa *", fNim);
        addRow(p, g, r++, "Jenis Pembayaran", cJenis);
        addRow(p, g, r++, "Jumlah (Rp) *", fJumlah);
        addRow(p, g, r++, "Tanggal Bayar *", fTanggal);
        addRow(p, g, r++, "Metode Pembayaran", cMetode);
        addRow(p, g, r++, "Semester *", cmbSemester);
        addRow(p, g, r++, "Tahun Ajaran *", fTA);
        g.gridx = 0; g.gridy = r; p.add(makeLabel("Keterangan"), g);
        g.gridx = 1; p.add(new JScrollPane(fKet) {{ setBorder(null); }}, g); r++;

        JButton btnSave = buildBtn("💾  Simpan Pembayaran", BLUE, 220);
        g.gridx = 0; g.gridy = r; g.gridwidth = 2; g.insets = new Insets(18, 4, 4, 4);
        p.add(btnSave, g);

        btnSave.addActionListener(e -> {
            if (fNim.getText().isEmpty() || fJumlah.getText().isEmpty()
                    || fTanggal.getText().isEmpty() || fTA.getText().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Isi semua field wajib (*).", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double jumlah;
            try {
                jumlah = Double.parseDouble(fJumlah.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Jumlah harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SemesterOption selectedSemester = (SemesterOption) cmbSemester.getSelectedItem();
            if (selectedSemester == null || selectedSemester.nomor() <= 0) {
                JOptionPane.showMessageDialog(d, "Pilih semester dari master semester terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("nim", fNim.getText().trim());
            body.addProperty("jenis_pembayaran", (String) cJenis.getSelectedItem());
            body.addProperty("jumlah", jumlah);
            body.addProperty("tanggal_bayar", fTanggal.getText().trim());
            body.addProperty("metode_pembayaran", (String) cMetode.getSelectedItem());
            body.addProperty("semester", selectedSemester.nomor());
            body.addProperty("tahun_ajaran", fTA.getText().trim());
            body.addProperty("keterangan", fKet.getText().trim());

            new SwingWorker<JsonObject, Void>() {
                protected JsonObject doInBackground() throws Exception { return PembayaranService.create(body); }
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

        d.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(CARD_BG); }});
        d.setVisible(true);
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
    private String safe(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "-";
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private void styleTable(JTable t) {
        t.setBackground(TABLE_BG);
        t.setForeground(TEXT_PRIMARY);
        t.setSelectionBackground(new Color(59, 130, 246, 60));
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(new Color(20, 30, 55));
        t.setRowHeight(42);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(HEADER_BG);
        h.setForeground(TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 40));
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(13, 19, 38));
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(7, 10, 7, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setPreferredSize(new Dimension(240, 34));
        return f;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(makeLabel(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    private void styleCombo(JComboBox<?> c, int width) {
        c.setBackground(CARD_BG);
        c.setForeground(TEXT_MUTED);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (width > 0) c.setPreferredSize(new Dimension(width, 36));
    }

    private JComboBox<SemesterOption> buildSemesterCombo() {
        JComboBox<SemesterOption> combo = new JComboBox<>();
        combo.addItem(new SemesterOption(0, "Memuat semester..."));
        styleCombo(combo, 0);
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getSettings();
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    combo.removeAllItems();
                    if (response.get("success").getAsBoolean()) {
                        JsonArray data = response.getAsJsonObject("data").getAsJsonArray("semester");
                        for (JsonElement item : data) {
                            JsonObject semester = item.getAsJsonObject();
                            if (semester.has("is_active") && !semester.get("is_active").isJsonNull()
                                    && semester.get("is_active").getAsInt() == 0) {
                                continue;
                            }
                            combo.addItem(new SemesterOption(
                                    parseInt(safe(semester, "nomor")),
                                    safe(semester, "nama_semester")
                            ));
                        }
                    }
                    if (combo.getItemCount() == 0) {
                        combo.addItem(new SemesterOption(0, "Belum ada semester aktif"));
                    }
                } catch (Exception ex) {
                    combo.removeAllItems();
                    combo.addItem(new SemesterOption(0, "Gagal memuat semester"));
                }
            }
        }.execute();
        return combo;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private record SemesterOption(int nomor, String nama) {
        @Override public String toString() {
            return nomor > 0 ? nomor + " - " + nama : nama;
        }
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
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(CARD_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(5, 12, 5, 12)));
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
            lbl.setBackground(sel ? new Color(59, 130, 246, 60) : (r % 2 == 0 ? TABLE_BG : ROW_ALT));
            return lbl;
        }

        private Color getBadgeColor(String s) {
            return switch (s.toLowerCase()) {
                case "lunas"  -> GREEN;
                case "pending" -> YELLOW;
                case "gagal"  -> RED;
                case "refund" -> PURPLE;
                default       -> TEXT_MUTED;
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
                add(makeSmallBtn("✅ Lunas", GREEN));
                add(makeSmallBtn("❌ Gagal", RED));
            }
            setBackground(sel ? new Color(59, 130, 246, 40) : (r % 2 == 0 ? TABLE_BG : ROW_ALT));
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
        private final JButton bLunas = makeSmallBtn("✅ Lunas", GREEN);
        private final JButton bGagal = makeSmallBtn("❌ Gagal", RED);
        private int rowId;

        ActionEditor() {
            super(new JCheckBox());
            panel.setBackground(TABLE_BG);
            panel.add(bLunas);
            panel.add(bGagal);
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
