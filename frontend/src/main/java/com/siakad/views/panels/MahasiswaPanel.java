package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;
import com.siakad.services.MahasiswaService;
import com.siakad.utils.JwtHelper;
import com.siakad.utils.SwingUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

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

    public MahasiswaPanel() {
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
        lblTitle.setForeground(TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Kelola data mahasiswa terdaftar");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
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
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
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
        txtSearch.setForeground(TEXT_PRIMARY);
        txtSearch.setCaretColor(TEXT_PRIMARY);
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

        JButton btnSearch  = buildBtn("Cari", BLUE);
        JButton btnRefresh = buildBtn("🔄", CARD_BG);
        JButton btnTambah  = buildBtn("＋  Tambah Mahasiswa", GREEN);

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
                    c.setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                }
                return c;
            }
        };
        styleTable(table);

        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(7).setMinWidth(210);
        table.getColumnModel().getColumn(7).setMaxWidth(210);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(TABLE_BG);
        scrollPane.getViewport().setBackground(TABLE_BG);
        scrollPane.setBorder(null);

        JPanel tableCard = new JPanel(new BorderLayout()) {
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
        tableCard.setOpaque(false);
        tableCard.setBorder(new EmptyBorder(0, 28, 0, 28));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // ── Footer ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 28, 14, 28));

        lblTotal = new JLabel("Total: 0 mahasiswa");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(TEXT_DIM);

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
        lblTitle.setForeground(TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Data akademik yang terhubung dengan akun Anda");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);

        JButton btnRefresh = buildBtn("Refresh", BLUE);
        btnRefresh.addActionListener(e -> loadData());
        header.add(titleBlock, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
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
        nimBadge.setForeground(BLUE);
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
        dialog.setSize(500, 560);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1;

        JTextField fNim      = makeField(); fNim.setEnabled(nim == null);
        JTextField fNama     = makeField();
        JTextField fEmail    = makeField();
        JTextField fTelp     = makeField();

        JComboBox<String> cmbJurusan = new JComboBox<>();
        cmbJurusan.addItem("Memuat jurusan...");
        styleCombo(cmbJurusan);

        JTextField fProdi = makeField();
        loadJurusanCombo(cmbJurusan, null);

        JComboBox<Integer> cmbAngkatan = buildAngkatanCombo();
        JSpinner spSemester = makeSemesterSpinner();
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
                            if (!d.get("jurusan").isJsonNull()) loadJurusanCombo(cmbJurusan, d.get("jurusan").getAsString());
                            if (!d.get("program_studi").isJsonNull()) fProdi.setText(d.get("program_studi").getAsString());
                            if (!d.get("angkatan").isJsonNull()) selectAngkatan(cmbAngkatan, d.get("angkatan").getAsInt());
                            if (!d.get("semester").isJsonNull()) {
                                int semester = d.get("semester").getAsInt();
                                spSemester.setValue(Math.max(1, Math.min(14, semester)));
                            }
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
        addRow(panel, gbc, r++, "Jurusan", cmbJurusan);
        addRow(panel, gbc, r++, "Program Studi", fProdi);
        addRow(panel, gbc, r++, "Angkatan", cmbAngkatan);
        addRow(panel, gbc, r++, "Semester", spSemester);
        gbc.gridx = 0; gbc.gridy = r; panel.add(makeLabel("Status"), gbc);
        gbc.gridx = 1; panel.add(cmbStatus, gbc); r++;
        if (nim == null) addRow(panel, gbc, r++, "Password (default=mhs123)", fPassword);

        JButton btnSave = buildBtn(nim == null ? "💾  Simpan" : "💾  Update", BLUE);
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2; gbc.insets = new Insets(18, 4, 4, 4);
        panel.add(btnSave, gbc);

        btnSave.addActionListener(e -> {
            if (fNim.getText().trim().isEmpty() || fNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "NIM dan Nama wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String selectedJurusan = String.valueOf(cmbJurusan.getSelectedItem());
            if (selectedJurusan.isBlank() || selectedJurusan.startsWith("Belum ada") || selectedJurusan.startsWith("Memuat")) {
                JOptionPane.showMessageDialog(dialog, "Pilih jurusan dari master jurusan terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Integer angkatan = (Integer) cmbAngkatan.getSelectedItem();
            int selectedSemester = ((Number) spSemester.getValue()).intValue();
            if (selectedSemester <= 0) {
                JOptionPane.showMessageDialog(dialog, "Semester harus lebih dari 0.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("nim", fNim.getText().trim());
            body.addProperty("nama", fNama.getText().trim());
            body.addProperty("email", fEmail.getText().trim());
            body.addProperty("no_telp", fTelp.getText().trim());
            body.addProperty("jurusan", selectedJurusan);
            body.addProperty("program_studi", fProdi.getText().trim());
            body.addProperty("angkatan", angkatan);
            body.addProperty("semester", selectedSemester);
            body.addProperty("status", (String) cmbStatus.getSelectedItem());
            if (nim == null && fPassword != null) body.addProperty("password", fPassword.getText().trim());

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

        dialog.add(new JScrollPane(panel) {{ setBorder(null); getViewport().setBackground(CARD_BG); }});
        SwingUi.configurePopups(dialog);
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
                            + "Jurusan          : " + safe(d, "jurusan") + "\n"
                            + "Program Studi    : " + safe(d, "program_studi") + "\n"
                            + "Angkatan         : " + safe(d, "angkatan") + "\n"
                            + "Semester         : " + safe(d, "semester") + "\n"
                            + "Status           : " + safe(d, "status").toUpperCase();
                    JTextArea area = new JTextArea(info);
                    area.setEditable(false);
                    area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    area.setBackground(CARD_BG);
                    area.setForeground(TEXT_PRIMARY);
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

    private void deleteMahasiswa(String nim) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Hapus mahasiswa dengan NIM " + nim + "?\n\nData KRS, nilai, kehadiran, pembayaran, dan akun login mahasiswa ini juga akan ikut dihapus.",
                "Konfirmasi Hapus Mahasiswa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.delete(nim);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    boolean success = response.has("success") && response.get("success").getAsBoolean();
                    JOptionPane.showMessageDialog(
                            MahasiswaPanel.this,
                            response.has("message") ? response.get("message").getAsString() : (success ? "Mahasiswa berhasil dihapus." : "Mahasiswa gagal dihapus."),
                            success ? "Berhasil" : "Gagal",
                            success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
                    );
                    if (success) {
                        loadData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MahasiswaPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        label.setForeground(TEXT_PRIMARY);
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
        l.setForeground(TEXT_MUTED);
        panel.add(l);
        panel.add(Box.createVerticalStrut(4));
        panel.add(value);
        return panel;
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

        JTableHeader th = t.getTableHeader();
        th.setBackground(HEADER_BG);
        th.setForeground(TEXT_MUTED);
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 40));
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(13, 19, 38));
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(7, 10, 7, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setPreferredSize(new Dimension(240, 34));
        return f;
    }

    private JSpinner makeSemesterSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 14, 1));
        spinner.setPreferredSize(new Dimension(240, 34));
        spinner.setMinimumSize(new Dimension(180, 34));
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(3, 8, 3, 8)));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JTextField field = defaultEditor.getTextField();
            field.setBackground(new Color(13, 19, 38));
            field.setForeground(TEXT_PRIMARY);
            field.setCaretColor(TEXT_PRIMARY);
            field.setBorder(BorderFactory.createEmptyBorder());
            field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        return spinner;
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

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(new Color(13, 19, 38));
        c.setForeground(TEXT_PRIMARY);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setPreferredSize(new Dimension(240, 34));
        c.setMinimumSize(new Dimension(180, 34));
        c.setMaximumRowCount(8);
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(3, 8, 3, 8)));
        SwingUi.configurePopups(c);
    }

    private void loadSemesterCombo(JComboBox<SemesterItem> combo, Integer selectedNomor) {
        Object pendingNomor = combo.getClientProperty("selectedSemesterNomor");
        Integer initialNomor = selectedNomor != null
                ? selectedNomor
                : pendingNomor instanceof Integer ? (Integer) pendingNomor : null;
        populateFallbackSemesterCombo(combo, initialNomor);

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
                            int nomor = semester.get("nomor").getAsInt();
                            combo.addItem(new SemesterItem(nomor, safe(semester, "nama_semester")));
                        }
                    }
                    if (combo.getItemCount() == 0) {
                        combo.addItem(new SemesterItem(0, "Belum ada semester aktif"));
                    }
                    Integer targetNomor = selectedNomor;
                    Object pendingNomor = combo.getClientProperty("selectedSemesterNomor");
                    if (targetNomor == null && pendingNomor instanceof Integer) {
                        targetNomor = (Integer) pendingNomor;
                    }
                    if (targetNomor != null) {
                        selectSemester(combo, targetNomor);
                    }
                } catch (Exception ex) {
                    Integer targetNomor = selectedNomor;
                    Object pendingNomor = combo.getClientProperty("selectedSemesterNomor");
                    if (targetNomor == null && pendingNomor instanceof Integer) {
                        targetNomor = (Integer) pendingNomor;
                    }
                    populateFallbackSemesterCombo(combo, targetNomor);
                }
            }
        }.execute();
    }

    private void populateFallbackSemesterCombo(JComboBox<SemesterItem> combo, Integer selectedNomor) {
        combo.removeAllItems();
        for (int nomor = 1; nomor <= 14; nomor++) {
            combo.addItem(new SemesterItem(nomor, "Semester " + nomor));
        }
        if (selectedNomor != null) {
            selectSemester(combo, selectedNomor);
        } else {
            combo.setSelectedIndex(0);
        }
    }

    private void selectSemester(JComboBox<SemesterItem> combo, int nomor) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            SemesterItem item = combo.getItemAt(i);
            if (item.nomor == nomor) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.addItem(new SemesterItem(nomor, "Semester " + nomor));
        combo.setSelectedIndex(combo.getItemCount() - 1);
    }

    private void loadSemesterPicker(SemesterPicker picker, Integer selectedNomor) {
        picker.setOptions(defaultSemesterItems());
        if (selectedNomor != null) {
            picker.setSelectedNomor(selectedNomor);
        }

        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getSettings();
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        return;
                    }

                    List<SemesterItem> options = new ArrayList<>();
                    JsonArray data = response.getAsJsonObject("data").getAsJsonArray("semester");
                    for (JsonElement item : data) {
                        JsonObject semester = item.getAsJsonObject();
                        if (semester.has("is_active") && !semester.get("is_active").isJsonNull()
                                && semester.get("is_active").getAsInt() == 0) {
                            continue;
                        }
                        int nomor = semester.get("nomor").getAsInt();
                        options.add(new SemesterItem(nomor, safe(semester, "nama_semester")));
                    }
                    if (!options.isEmpty()) {
                        int current = picker.getSelectedNomor();
                        picker.setOptions(options);
                        if (selectedNomor != null) {
                            picker.setSelectedNomor(selectedNomor);
                        } else if (current > 0) {
                            picker.setSelectedNomor(current);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private List<SemesterItem> defaultSemesterItems() {
        List<SemesterItem> items = new ArrayList<>();
        for (int nomor = 1; nomor <= 14; nomor++) {
            items.add(new SemesterItem(nomor, "Semester " + nomor));
        }
        return items;
    }

    private JComboBox<Integer> buildAngkatanCombo() {
        JComboBox<Integer> combo = new JComboBox<>();
        populateAngkatanCombo(combo, Year.now().getValue(), null);
        styleCombo(combo);
        loadAngkatanFromActiveTahunAjaran(combo);
        return combo;
    }

    private void loadAngkatanFromActiveTahunAjaran(JComboBox<Integer> combo) {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getSettings();
            }

            @Override protected void done() {
                try {
                    Object selected = combo.getSelectedItem();
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        return;
                    }
                    JsonArray data = response.getAsJsonObject("data").getAsJsonArray("tahun_ajaran");
                    int activeYear = 0;
                    for (JsonElement item : data) {
                        JsonObject tahun = item.getAsJsonObject();
                        if ("aktif".equalsIgnoreCase(safe(tahun, "status"))) {
                            activeYear = parseFirstYear(safe(tahun, "tahun_ajaran"));
                            break;
                        }
                    }
                    if (activeYear > 0) {
                        populateAngkatanCombo(combo, activeYear, selected instanceof Integer ? (Integer) selected : activeYear);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void populateAngkatanCombo(JComboBox<Integer> combo, int startYear, Integer selectedYear) {
        combo.removeAllItems();
        for (int year = startYear; year >= startYear - 20; year--) {
            combo.addItem(year);
        }
        combo.setSelectedItem(selectedYear != null ? selectedYear : startYear);
    }

    private int parseFirstYear(String tahunAjaran) {
        try {
            if (tahunAjaran == null || tahunAjaran.length() < 4) {
                return 0;
            }
            return Integer.parseInt(tahunAjaran.substring(0, 4));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void selectAngkatan(JComboBox<Integer> combo, int angkatan) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i) == angkatan) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.addItem(angkatan);
        combo.setSelectedItem(angkatan);
    }

    private void loadJurusanCombo(JComboBox<String> combo, String selected) {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getJurusanList();
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    combo.removeAllItems();
                    if (response.get("success").getAsBoolean()) {
                        for (com.google.gson.JsonElement item : response.getAsJsonArray("data")) {
                            String jurusan = item.getAsString();
                            if (jurusan != null && !jurusan.isBlank()) {
                                combo.addItem(jurusan);
                            }
                        }
                    }
                    if (selected != null && !selected.isBlank()) {
                        combo.setSelectedItem(selected);
                    }
                    if (combo.getItemCount() == 0) {
                        combo.addItem("Belum ada jurusan");
                    }
                } catch (Exception ex) {
                    combo.removeAllItems();
                    combo.addItem(selected != null && !selected.isBlank() ? selected : "Belum ada jurusan");
                }
            }
        }.execute();
    }

    private class SemesterPicker extends JButton {
        private final List<SemesterItem> options = new ArrayList<>();
        private SemesterItem selected;

        SemesterPicker() {
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(TEXT_PRIMARY);
            setBackground(new Color(13, 19, 38));
            setHorizontalAlignment(SwingConstants.LEFT);
            setPreferredSize(new Dimension(240, 34));
            setMinimumSize(new Dimension(180, 34));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(7, 10, 7, 10)));
            addActionListener(e -> showPickerDialog());
        }

        void setOptions(List<SemesterItem> items) {
            int current = getSelectedNomor();
            options.clear();
            options.addAll(items);
            if (current > 0) {
                setSelectedNomor(current);
            } else if (!options.isEmpty()) {
                setSelected(options.get(0));
            } else {
                setText("Pilih semester");
            }
        }

        void setSelectedNomor(int nomor) {
            for (SemesterItem item : options) {
                if (item.nomor == nomor) {
                    setSelected(item);
                    return;
                }
            }
            SemesterItem fallback = new SemesterItem(nomor, "Semester " + nomor);
            options.add(fallback);
            setSelected(fallback);
        }

        int getSelectedNomor() {
            return selected == null ? 0 : selected.nomor;
        }

        SemesterItem getSelectedSemester() {
            return selected;
        }

        private void setSelected(SemesterItem item) {
            selected = item;
            setText(item + "  ▾");
        }

        private void showPickerDialog() {
            if (options.isEmpty()) {
                JOptionPane.showMessageDialog(MahasiswaPanel.this,
                        "Data semester belum tersedia.",
                        "Semester kosong",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            SemesterItem choice = (SemesterItem) JOptionPane.showInputDialog(
                    MahasiswaPanel.this,
                    "Pilih semester mahasiswa:",
                    "Pilih Semester",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options.toArray(new SemesterItem[0]),
                    selected
            );
            if (choice != null) {
                setSelected(choice);
            }
        }
    }

    private static class SemesterItem {
        private final int nomor;
        private final String label;

        private SemesterItem(int nomor, String label) {
            this.nomor = nomor;
            this.label = label;
        }

        @Override public String toString() {
            return nomor > 0 ? nomor + " - " + label : label;
        }
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
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(CARD_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
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
            lbl.setBackground(sel ? new Color(59, 130, 246, 60) : (r % 2 == 0 ? TABLE_BG : ROW_ALT));
            return lbl;
        }
        private Color getBadgeColor(String status) {
            return switch (status.toLowerCase()) {
                case "aktif"    -> GREEN;
                case "cuti"     -> YELLOW;
                case "lulus"    -> BLUE;
                case "drop_out" -> RED;
                default         -> TEXT_MUTED;
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
            if (JwtHelper.getInstance().isAdmin()) add(makeActionBtn("Edit", BLUE));
            if (JwtHelper.getInstance().isAdmin()) add(makeActionBtn("Hapus", new Color(185, 28, 28)));
            setBackground(sel ? new Color(59, 130, 246, 40) : (r % 2 == 0 ? TABLE_BG : ROW_ALT));
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
        private final JButton btnEdit   = makeActionBtn("Edit", BLUE);
        private final JButton btnDelete = makeActionBtn("Hapus", new Color(185, 28, 28));
        private String nim;

        ActionEditor() {
            super(new JCheckBox());
            panel.setBackground(TABLE_BG);
            panel.add(btnDetail);
            if (JwtHelper.getInstance().isAdmin()) panel.add(btnEdit);
            if (JwtHelper.getInstance().isAdmin()) panel.add(btnDelete);
            btnDetail.addActionListener(e -> { fireEditingStopped(); if (nim != null) showDetailDialog(nim); });
            btnEdit.addActionListener(e -> { fireEditingStopped(); if (nim != null) showForm(nim); });
            btnDelete.addActionListener(e -> { fireEditingStopped(); if (nim != null) deleteMahasiswa(nim); });
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
