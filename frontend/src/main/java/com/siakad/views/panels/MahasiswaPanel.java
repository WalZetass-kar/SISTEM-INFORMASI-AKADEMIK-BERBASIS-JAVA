package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.MahasiswaService;
import com.siakad.utils.AppTheme;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MahasiswaPanel - CRUD Data Mahasiswa
 */
public class MahasiswaPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblTotal;
    private int currentPage = 1;
    private final int PAGE_SIZE = 15;
    private JButton btnPrev, btnNext;

    // Skeleton
    private CardLayout rootCard;
    private JPanel rootPanel;
    private SkeletonPanel skeleton;
    private StatePanel statePanel;

    private JLabel lblProfileName, lblProfileNim, lblProfileJurusan, lblProfileProdi;
    private JLabel lblProfileEmail, lblProfileSemester, lblProfileAngkatan, lblProfileStatus;

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

    public MahasiswaPanel() {
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
        if (JwtHelper.getInstance().isAdmin()) {
            initUI(content);
        } else {
            initProfileUI(content);
        }
        rootPanel.add(content, "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);

        loadData();
    }

    private void initUI(JPanel target) {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Data Mahasiswa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY());
        JLabel lblSub = new JLabel("Kelola data mahasiswa terdaftar");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED());
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);

        // Search & actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        // Search box with icon
        JPanel searchBox = new JPanel(new BorderLayout()) {
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
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(240, 36));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        searchIcon.setBorder(new EmptyBorder(0, 10, 0, 4));

        txtSearch = new JTextField();
        txtSearch.setOpaque(false);
        txtSearch.setBackground(new Color(0, 0, 0, 0));
        txtSearch.setForeground(TEXT_PRIMARY());
        txtSearch.setCaretColor(TEXT_PRIMARY());
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setToolTipText("Cari NIM, nama, atau email...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { currentPage = 1; loadData(); }
            }
        });

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        JButton btnSearch  = buildBtn("Cari", BLUE());
        JButton btnRefresh = buildBtn("🔄", CARD_BG());
        JButton btnTambah  = buildBtn("＋  Tambah Mahasiswa", GREEN());

        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); currentPage = 1; loadData(); });
        btnTambah.addActionListener(e -> showForm(null));
        btnTambah.setVisible(JwtHelper.getInstance().isAdmin());

        actions.add(searchBox);
        actions.add(btnSearch);
        actions.add(btnRefresh);
        actions.add(btnTambah);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // ── Table ──
        String[] columns = {"NIM", "Nama", "Jurusan", "Program Studi", "Angkatan", "Semester", "Status", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 7; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TABLE_BG() : ROW_ALT());
                }
                return c;
            }
        };
        styleTable(table);

        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(7).setMinWidth(130);
        table.getColumnModel().getColumn(7).setMaxWidth(130);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(TABLE_BG());
        scrollPane.getViewport().setBackground(TABLE_BG());
        scrollPane.setBorder(null);

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
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // ── Footer ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 28, 14, 28));

        lblTotal = new JLabel("Total: 0 mahasiswa");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(TEXT_DIM());

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pagination.setOpaque(false);
        btnPrev = buildPagBtn("◀  Prev");
        btnNext = buildPagBtn("Next  ▶");
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); }});
        btnNext.addActionListener(e -> { currentPage++; loadData(); });
        pagination.add(btnPrev);
        pagination.add(btnNext);

        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(pagination, BorderLayout.EAST);

        target.add(header, BorderLayout.NORTH);
        target.add(tableCard, BorderLayout.CENTER);
        target.add(footer, BorderLayout.SOUTH);
    }

    private void initProfileUI(JPanel target) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 18, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Profil Mahasiswa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY());
        JLabel lblSub = new JLabel("Data akademik yang terhubung dengan akun Anda");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED());
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);

        JButton btnRefresh = buildBtn("Refresh", BLUE());
        btnRefresh.addActionListener(e -> loadData());
        header.add(titleBlock, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(new Color(34, 211, 238, 70));
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 30, 28, 30));

        lblProfileName = profileValue("Memuat...");
        lblProfileName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblProfileNim = profileValue("-");
        lblProfileJurusan = profileValue("-");
        lblProfileProdi = profileValue("-");
        lblProfileEmail = profileValue("-");
        lblProfileSemester = profileValue("-");
        lblProfileAngkatan = profileValue("-");
        lblProfileStatus = profileValue("-");

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 10, 8, 10);
        g.weightx = 1;
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        card.add(lblProfileName, g);
        g.gridy++;
        JLabel nimBadge = new JLabel();
        nimBadge.setForeground(BLUE());
        nimBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblProfileNim = nimBadge;
        card.add(lblProfileNim, g);
        g.gridwidth = 1;

        addProfileRow(card, g, 2, "Jurusan", lblProfileJurusan, "Program Studi", lblProfileProdi);
        addProfileRow(card, g, 3, "Email", lblProfileEmail, "Status", lblProfileStatus);
        addProfileRow(card, g, 4, "Semester", lblProfileSemester, "Angkatan", lblProfileAngkatan);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 28, 28, 28));
        wrap.add(card, BorderLayout.NORTH);

        target.add(header, BorderLayout.NORTH);
        target.add(wrap, BorderLayout.CENTER);
    }

    private void loadData() {
        skeleton.start();
        rootCard.show(rootPanel, "skeleton");

        if (!JwtHelper.getInstance().isAdmin()) {
            loadOwnProfile();
            return;
        }

        String search = txtSearch.getText().trim();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getAll(currentPage, PAGE_SIZE, search);
            }
            @Override protected void done() {
                try {
                    JsonObject resp = get();
                    tableModel.setRowCount(0);
                    if (resp.get("success").getAsBoolean()) {
                        JsonArray data = resp.getAsJsonArray("data");
                        JsonObject pag = resp.getAsJsonObject("pagination");
                        int total = pag.get("total").getAsInt();
                        int totalPages = pag.get("totalPages").getAsInt();
                        if (data.size() == 0) {
                            lblTotal.setText("Tidak ada mahasiswa ditemukan");
                            btnPrev.setEnabled(false);
                            btnNext.setEnabled(false);
                            statePanel.showState("0", "Data mahasiswa kosong",
                                    search.isEmpty()
                                            ? "Belum ada data mahasiswa yang tersimpan."
                                            : "Tidak ada mahasiswa yang cocok dengan pencarian \"" + search + "\".",
                                    search.isEmpty() ? "Muat ulang" : "Reset pencarian",
                                    () -> {
                                        txtSearch.setText("");
                                        currentPage = 1;
                                        loadData();
                                    });
                            rootCard.show(rootPanel, "state");
                            return;
                        }
                        lblTotal.setText("Menampilkan " + data.size() + " dari " + total + " mahasiswa  |  Halaman " + currentPage + " / " + totalPages);
                        btnPrev.setEnabled(currentPage > 1);
                        btnNext.setEnabled(currentPage < totalPages);

                        for (int i = 0; i < data.size(); i++) {
                            JsonObject m = data.get(i).getAsJsonObject();
                            tableModel.addRow(new Object[]{
                                m.get("nim").getAsString(),
                                m.get("nama").getAsString(),
                                safe(m, "jurusan"),
                                safe(m, "program_studi"),
                                safe(m, "angkatan"),
                                m.has("semester") ? m.get("semester").getAsString() : "-",
                                m.has("status") ? m.get("status").getAsString() : "aktif",
                                "aksi"
                            });
                        }
                        rootCard.show(rootPanel, "content");
                    } else {
                        showStateError(resp.has("message") ? resp.get("message").getAsString() : "Gagal memuat data mahasiswa.");
                    }
                } catch (Exception e) {
                    showStateError("Gagal memuat data mahasiswa: " + e.getMessage());
                } finally {
                    skeleton.stop();
                }
            }
        }.execute();
    }

    private void loadOwnProfile() {
        String nim = JwtHelper.getInstance().getNim();
        if (nim == null || nim.isBlank()) {
            skeleton.stop();
            statePanel.showState("!", "Profil belum terhubung",
                    "Akun ini belum memiliki NIM, sehingga profil mahasiswa tidak bisa ditampilkan.",
                    "Coba lagi", this::loadData);
            rootCard.show(rootPanel, "state");
            return;
        }

        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getByNim(nim);
            }
            @Override protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        JsonObject d = resp.getAsJsonObject("data");
                        lblProfileName.setText(safe(d, "nama"));
                        lblProfileNim.setText("NIM " + safe(d, "nim"));
                        lblProfileJurusan.setText(safe(d, "jurusan"));
                        lblProfileProdi.setText(safe(d, "program_studi"));
                        lblProfileEmail.setText(safe(d, "email"));
                        lblProfileSemester.setText(safe(d, "semester"));
                        lblProfileAngkatan.setText(safe(d, "angkatan"));
                        lblProfileStatus.setText(safe(d, "status").toUpperCase());
                        rootCard.show(rootPanel, "content");
                    } else {
                        showStateError(resp.has("message") ? resp.get("message").getAsString() : "Gagal memuat profil mahasiswa.");
                    }
                } catch (Exception e) {
                    showStateError("Gagal memuat profil mahasiswa: " + e.getMessage());
                } finally {
                    skeleton.stop();
                }
            }
        }.execute();
    }

    private void showStateError(String message) {
        statePanel.showState("!", "Data tidak bisa dimuat", message, "Muat ulang", this::loadData);
        rootCard.show(rootPanel, "state");
    }

    private void showForm(String nim) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                nim == null ? "Tambah Mahasiswa" : "Edit Mahasiswa", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG());
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1;

        JTextField fNim      = makeField(); fNim.setEnabled(nim == null);
        JTextField fNama     = makeField();
        JTextField fEmail    = makeField();
        JTextField fTelp     = makeField();
        JTextField fAlamat   = makeField();

        // Jurusan dropdown - synchronized with database seeds
        String[] jurusanList = {
            "Teknik Informatika",
            "Sistem Informasi",
            "Komputerisasi Akuntansi",
            "Hubungan Masyarakat",
            "Administrasi Bisnis",
            "Management Informatika"
        };
        JComboBox<String> cmbJurusan = new JComboBox<>(jurusanList);
        styleCombo(cmbJurusan);

        // Program Studi per jurusan
        String[][] prodiMap = {
            {"S1 Informatika", "D4 Rekayasa Perangkat Lunak"},
            {"S1 Sistem Informasi", "D4 Sistem Informasi Bisnis"},
            {"D3 Komputerisasi Akuntansi", "D4 Akuntansi Digital"},
            {"D3 Hubungan Masyarakat", "D4 Komunikasi Strategis"},
            {"D3 Administrasi Bisnis", "D4 Manajemen Pemasaran"},
            {"D3 Manajemen Informatika", "D4 Rekayasa Perangkat Lunak"}
        };
        JComboBox<String> cmbProdi = new JComboBox<>(prodiMap[0]);
        styleCombo(cmbProdi);
        cmbJurusan.addActionListener(e -> {
            int idx = cmbJurusan.getSelectedIndex();
            if (idx >= 0 && idx < prodiMap.length) {
                cmbProdi.removeAllItems();
                for (String p : prodiMap[idx]) cmbProdi.addItem(p);
            }
        });

        JTextField fAngkatan = makeField();
        JTextField fSemester = makeField();
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"aktif", "cuti", "lulus", "drop_out"});
        styleCombo(cmbStatus);
        JTextField fPassword = nim == null ? makeField() : null;

        if (nim != null) {
            new SwingWorker<JsonObject, Void>() {
                @Override protected JsonObject doInBackground() throws Exception { return MahasiswaService.getByNim(nim); }
                @Override protected void done() {
                    try {
                        JsonObject r = get();
                        if (r.get("success").getAsBoolean()) {
                            JsonObject d = r.getAsJsonObject("data");
                            fNim.setText(d.get("nim").getAsString());
                            fNama.setText(d.get("nama").getAsString());
                            if (!d.get("email").isJsonNull()) fEmail.setText(d.get("email").getAsString());
                            if (!d.get("no_telp").isJsonNull()) fTelp.setText(d.get("no_telp").getAsString());
                            if (!d.get("alamat").isJsonNull()) fAlamat.setText(d.get("alamat").getAsString());
                            
                            // Select Jurusan first, then Prodi
                            if (!d.get("jurusan").isJsonNull()) {
                                String dbJurusan = d.get("jurusan").getAsString();
                                cmbJurusan.setSelectedItem(dbJurusan);
                            }
                            if (!d.get("program_studi").isJsonNull()) {
                                String dbProdi = d.get("program_studi").getAsString();
                                cmbProdi.setSelectedItem(dbProdi);
                            }
                            if (!d.get("angkatan").isJsonNull()) fAngkatan.setText(d.get("angkatan").getAsString());
                            fSemester.setText(d.get("semester").getAsString());
                            cmbStatus.setSelectedItem(d.get("status").getAsString());
                        }
                    } catch (Exception ignored) {}
                }
            }.execute();
        }

        int r = 0;
        addRow(panel, gbc, r++, "NIM *", fNim);
        addRow(panel, gbc, r++, "Nama Lengkap *", fNama);
        addRow(panel, gbc, r++, "Email", fEmail);
        addRow(panel, gbc, r++, "No. Telp", fTelp);
        addRow(panel, gbc, r++, "Alamat", fAlamat);
        addRow(panel, gbc, r++, "Jurusan", cmbJurusan);
        addRow(panel, gbc, r++, "Program Studi", cmbProdi);
        addRow(panel, gbc, r++, "Angkatan", fAngkatan);
        addRow(panel, gbc, r++, "Semester", fSemester);
        gbc.gridx = 0; gbc.gridy = r; panel.add(makeLabel("Status"), gbc);
        gbc.gridx = 1; panel.add(cmbStatus, gbc); r++;
        if (nim == null) addRow(panel, gbc, r++, "Password (default=NIM)", fPassword);

        JButton btnSave = buildBtn(nim == null ? "💾  Simpan" : "💾  Update", BLUE());
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2; gbc.insets = new Insets(18, 4, 4, 4);
        panel.add(btnSave, gbc);

        btnSave.addActionListener(e -> {
            String nimVal = fNim.getText().trim();
            String namaVal = fNama.getText().trim();
            String emailVal = fEmail.getText().trim();
            String telpVal = fTelp.getText().trim();
            String angkatanText = fAngkatan.getText().trim();
            String semesterText = fSemester.getText().trim();

            if (nimVal.isEmpty() || namaVal.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "NIM dan Nama wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validasi format email
            if (!emailVal.isEmpty() && !emailVal.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(dialog, "Format Email tidak valid (harus mengandung '@' dan domain)!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validasi format telp
            if (!telpVal.isEmpty() && !telpVal.matches("^[0-9+ -]+$")) {
                JOptionPane.showMessageDialog(dialog, "Format No. Telp tidak valid (hanya boleh angka, spasi, +, -)!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Integer angkatan = null;
            Integer semester = null;
            try {
                if (!angkatanText.isEmpty()) {
                    // Validasi format tahun angkatan YYYY
                    if (!angkatanText.matches("^(19|20)\\d{2}$")) {
                        JOptionPane.showMessageDialog(dialog, "Format Angkatan harus berupa 4 digit tahun (contoh: 2024).", "Validasi", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    angkatan = Integer.parseInt(angkatanText);
                }
                if (!semesterText.isEmpty()) {
                    semester = Integer.parseInt(semesterText);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Angkatan dan semester harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("nim", nimVal);
            body.addProperty("nama", namaVal);
            body.addProperty("email", emailVal);
            body.addProperty("no_telp", telpVal);
            body.addProperty("alamat", fAlamat.getText().trim());
            body.addProperty("jurusan", (String) cmbJurusan.getSelectedItem());
            body.addProperty("program_studi", (String) cmbProdi.getSelectedItem());
            if (angkatan != null) body.addProperty("angkatan", angkatan);
            if (semester != null) body.addProperty("semester", semester);
            body.addProperty("status", (String) cmbStatus.getSelectedItem());

            if (nim == null) {
                String pwd = (fPassword != null) ? fPassword.getText().trim() : "";
                body.addProperty("password", pwd.isEmpty() ? nimVal : pwd);
            }

            new SwingWorker<JsonObject, Void>() {
                @Override protected JsonObject doInBackground() throws Exception {
                    return nim == null ? MahasiswaService.create(body) : MahasiswaService.update(nim, body);
                }
                @Override protected void done() {
                    try {
                        JsonObject res = get();
                        if (res.get("success").getAsBoolean()) {
                            JOptionPane.showMessageDialog(dialog, res.get("message").getAsString(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose(); loadData();
                        } else {
                            JOptionPane.showMessageDialog(dialog, res.get("message").getAsString(), "Gagal", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        dialog.add(new JScrollPane(panel) {{ setBorder(null); getViewport().setBackground(CARD_BG()); }});
        dialog.setVisible(true);
    }

    private void showDetailDialog(String nim) {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getByNim(nim);
            }
            @Override protected void done() {
                try {
                    JsonObject resp = get();
                    if (!resp.get("success").getAsBoolean()) {
                        JOptionPane.showMessageDialog(MahasiswaPanel.this,
                                resp.has("message") ? resp.get("message").getAsString() : "Data mahasiswa tidak ditemukan.",
                                "Gagal", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JsonObject d = resp.getAsJsonObject("data");
                    String info = "NIM              : " + safe(d, "nim") + "\n"
                            + "Nama             : " + safe(d, "nama") + "\n"
                            + "Email            : " + safe(d, "email") + "\n"
                            + "No. Telp         : " + safe(d, "no_telp") + "\n"
                            + "Alamat           : " + safe(d, "alamat") + "\n"
                            + "Jurusan          : " + safe(d, "jurusan") + "\n"
                            + "Program Studi    : " + safe(d, "program_studi") + "\n"
                            + "Angkatan         : " + safe(d, "angkatan") + "\n"
                            + "Semester         : " + safe(d, "semester") + "\n"
                            + "Status           : " + safe(d, "status").toUpperCase();
                    JTextArea area = new JTextArea(info);
                    area.setEditable(false);
                    area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    area.setBackground(CARD_BG());
                    area.setForeground(TEXT_PRIMARY());
                    area.setBorder(new EmptyBorder(12, 12, 12, 12));
                    JOptionPane.showMessageDialog(MahasiswaPanel.this, new JScrollPane(area),
                            "Detail Mahasiswa", JOptionPane.PLAIN_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MahasiswaPanel.this, "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private String safe(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "-";
    }

    private JLabel profileValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXT_PRIMARY());
        return label;
    }

    private void addProfileRow(JPanel card, GridBagConstraints g, int row,
                               String leftLabel, JLabel leftValue, String rightLabel, JLabel rightValue) {
        g.gridy = row;
        g.gridx = 0;
        card.add(profileBlock(leftLabel, leftValue), g);
        g.gridx = 1;
        card.add(profileBlock(rightLabel, rightValue), g);
    }

    private JPanel profileBlock(String label, JLabel value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED());
        panel.add(l);
        panel.add(Box.createVerticalStrut(4));
        panel.add(value);
        return panel;
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

        JTableHeader th = t.getTableHeader();
        th.setBackground(HEADER_BG());
        th.setForeground(TEXT_MUTED());
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR()));
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 40));
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setBackground(AppTheme.input());
        f.setForeground(TEXT_PRIMARY());
        f.setCaretColor(TEXT_PRIMARY());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1),
                new EmptyBorder(7, 10, 7, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setPreferredSize(new Dimension(240, 34));
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

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(AppTheme.input());
        c.setForeground(TEXT_PRIMARY());
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JButton buildBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
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
                BorderFactory.createLineBorder(BORDER_COLOR(), 1),
                new EmptyBorder(5, 12, 5, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Status Badge Renderer ─────────────────────────────────────────────────
    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = new JLabel(String.valueOf(v), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = getBadgeColor(String.valueOf(v));
                    g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 30));
                    g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 8, 10, 10);
                    g2.setColor(bg);
                    g2.drawRoundRect(2, 4, getWidth() - 5, getHeight() - 9, 10, 10);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(getBadgeColor(String.valueOf(v)));
            lbl.setOpaque(false);
            lbl.setBackground(sel ? new Color(59, 130, 246, 60) : (r % 2 == 0 ? TABLE_BG() : ROW_ALT()));
            return lbl;
        }
        private Color getBadgeColor(String status) {
            return switch (status.toLowerCase()) {
                case "aktif"    -> GREEN();
                case "cuti"     -> YELLOW();
                case "lulus"    -> BLUE();
                case "drop_out" -> RED();
                default         -> TEXT_MUTED();
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
            add(makeActionBtn("Detail", new Color(30, 41, 70)));
            if (JwtHelper.getInstance().isAdmin()) add(makeActionBtn("Edit", BLUE()));
            setBackground(sel ? new Color(59, 130, 246, 40) : (r % 2 == 0 ? TABLE_BG() : ROW_ALT()));
            return this;
        }
        private JButton makeActionBtn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 10));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1),
                    new EmptyBorder(3, 8, 3, 8)));
            b.setFocusPainted(false);
            return b;
        }
    }

    // ── Action Editor ─────────────────────────────────────────────────────────
    class ActionEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private final JButton btnDetail = makeActionBtn("Detail", new Color(30, 41, 70));
        private final JButton btnEdit   = makeActionBtn("Edit", BLUE());
        private String nim;

        ActionEditor() {
            super(new JCheckBox());
            panel.setBackground(TABLE_BG());
            panel.add(btnDetail);
            if (JwtHelper.getInstance().isAdmin()) panel.add(btnEdit);
            btnDetail.addActionListener(e -> { fireEditingStopped(); if (nim != null) showDetailDialog(nim); });
            btnEdit.addActionListener(e -> { fireEditingStopped(); if (nim != null) showForm(nim); });
        }

        private JButton makeActionBtn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 10));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1),
                    new EmptyBorder(3, 8, 3, 8)));
            b.setFocusPainted(false);
            return b;
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            nim = (String) t.getValueAt(r, 0);
            return panel;
        }
        @Override public Object getCellEditorValue() { return "aksi"; }
    }
}
