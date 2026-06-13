package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class PengaturanAkademikPanel extends JPanel {
    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();

    private DefaultTableModel tahunModel;
    private DefaultTableModel mkModel;
    private JTable tahunTable;
    private JTable mkTable;
    private JTextField txtTugas;
    private JTextField txtUts;
    private JTextField txtUas;
    private JTextField txtJumlahPertemuan;
    private JLabel lblSummary;
    private JLabel lblActiveTahun;
    private JButton btnSaveBobot;
    private JButton btnSavePertemuan;

    private static final Color BG = new Color(13, 19, 38);
    private static final Color CARD = new Color(18, 26, 48);
    private static final Color TABLE = new Color(15, 22, 42);
    private static final Color BORDER = new Color(25, 36, 65);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color ROW_ALT = new Color(20, 29, 52);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color RED = new Color(185, 28, 28);

    public PengaturanAkademikPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        rootPanel.setBackground(BG);
        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(buildScrollableContent(), "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);
        loadSettings();
    }

    private JScrollPane buildScrollableContent() {
        JPanel content = buildContent();
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(26, 28, 14, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Pengaturan Akademik");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Kelola tahun ajaran, bobot nilai, dan jumlah pertemuan akademik");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        lblActiveTahun = new JLabel("Tahun ajaran aktif: -");
        lblActiveTahun.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblActiveTahun.setForeground(new Color(191, 219, 254));
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(lblActiveTahun);

        JButton refresh = button("Refresh", BLUE);
        refresh.addActionListener(e -> loadSettings());
        header.add(titleBlock, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        cards.add(featureCard("TA", "Tahun Ajaran", "Periode aktif untuk nilai, absensi, KRS, dan jadwal."));
        cards.add(featureCard("MK", "Mata Kuliah", "Referensi mata kuliah dari modul KRS & Jadwal Kuliah."));
        cards.add(featureCard("%", "Konfigurasi", "Bobot nilai dan jumlah pertemuan input kehadiran."));

        JPanel tables = buildTables();
        tables.setAlignmentX(Component.LEFT_ALIGNMENT);
        tables.setPreferredSize(new Dimension(0, 430));
        tables.setMaximumSize(new Dimension(Integer.MAX_VALUE, 430));

        JPanel bobot = buildBobotCard();
        bobot.setAlignmentX(Component.LEFT_ALIGNMENT);
        bobot.setPreferredSize(new Dimension(0, 190));
        bobot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        content.add(header);
        content.add(Box.createVerticalStrut(14));
        content.add(cards);
        content.add(Box.createVerticalStrut(14));
        content.add(tables);
        content.add(Box.createVerticalStrut(14));
        content.add(bobot);
        content.add(Box.createVerticalStrut(18));
        return content;
    }

    private JPanel buildTables() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);

        tahunModel = new DefaultTableModel(new String[]{"ID", "Tahun", "Semester", "Mulai", "Selesai", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tahunTable = table(tahunModel);
        panel.add(section("Tahun Ajaran", "Tambah", e -> showTahunDialog(-1), "Edit", e -> editTahun(), "Hapus", e -> deleteTahun(), tahunTable));

        mkModel = new DefaultTableModel(new String[]{"Kode", "Nama", "SKS", "Smt", "Jurusan", "Dosen"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        mkTable = table(mkModel);
        panel.add(readOnlySection("Mata Kuliah", "Input mata kuliah dikelola dari menu KRS & Jadwal Kuliah.", mkTable));
        return panel;
    }

    private JPanel buildBobotCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Konfigurasi Penilaian & Absensi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        lblSummary = new JLabel("Total bobot harus 100%.");
        lblSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSummary.setForeground(MUTED);
        header.add(title, BorderLayout.WEST);
        header.add(lblSummary, BorderLayout.EAST);

        JPanel configGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        configGrid.setOpaque(false);

        JPanel bobotBlock = configBlock("Bobot Nilai", "Komposisi nilai akhir mahasiswa.");
        JPanel bobotForm = new JPanel(new GridBagLayout());
        bobotForm.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;
        txtTugas = field("30");
        txtUts = field("30");
        txtUas = field("40");
        installBobotListener(txtTugas);
        installBobotListener(txtUts);
        installBobotListener(txtUas);
        g.gridx = 0; g.weightx = 0; bobotForm.add(label("Tugas"), g);
        g.gridx = 1; g.weightx = 1; bobotForm.add(txtTugas, g);
        g.gridx = 2; g.weightx = 0; bobotForm.add(label("UTS"), g);
        g.gridx = 3; g.weightx = 1; bobotForm.add(txtUts, g);
        g.gridx = 4; g.weightx = 0; bobotForm.add(label("UAS"), g);
        g.gridx = 5; g.weightx = 1; bobotForm.add(txtUas, g);
        btnSaveBobot = button("Simpan Bobot", GREEN);
        btnSaveBobot.addActionListener(e -> saveBobot());
        g.gridx = 6; g.weightx = 0; bobotForm.add(btnSaveBobot, g);
        bobotBlock.add(bobotForm, BorderLayout.CENTER);

        JPanel pertemuanBlock = configBlock("Jumlah Pertemuan", "Dipakai dropdown pertemuan pada Input Kehadiran.");
        JPanel pertemuanForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pertemuanForm.setOpaque(false);
        txtJumlahPertemuan = field("12");
        txtJumlahPertemuan.setPreferredSize(new Dimension(90, 34));
        pertemuanForm.add(label("Total"));
        pertemuanForm.add(txtJumlahPertemuan);
        btnSavePertemuan = button("Simpan Pertemuan", BLUE);
        btnSavePertemuan.addActionListener(e -> saveJumlahPertemuan());
        pertemuanForm.add(btnSavePertemuan);
        pertemuanBlock.add(pertemuanForm, BorderLayout.CENTER);

        configGrid.add(bobotBlock);
        configGrid.add(pertemuanBlock);
        updateBobotSummary();

        panel.add(header, BorderLayout.NORTH);
        panel.add(configGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel configBlock(String title, String desc) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT);
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(MUTED);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));
        text.add(descLabel);
        panel.add(text, BorderLayout.NORTH);
        return panel;
    }

    private JPanel featureCard(String icon, String title, String desc) {
        JPanel panel = cardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(BLUE);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT);
        JTextArea detail = new JTextArea(desc);
        detail.setOpaque(false);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detail.setForeground(MUTED);
        detail.setFocusable(false);
        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(detail);
        return panel;
    }

    private JPanel section(String title, String addText, java.awt.event.ActionListener add,
                           String editText, java.awt.event.ActionListener edit,
                           String delText, java.awt.event.ActionListener del, JTable table) {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout());
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(12, 14, 10, 14));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton addBtn = button(addText, GREEN);
        JButton editBtn = button(editText, BLUE);
        JButton delBtn = button(delText, RED);
        addBtn.addActionListener(add);
        editBtn.addActionListener(edit);
        delBtn.addActionListener(del);
        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(delBtn);
        head.add(lbl, BorderLayout.WEST);
        head.add(actions, BorderLayout.EAST);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE);
        scroll.setBackground(TABLE);
        panel.add(head, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel readOnlySection(String title, String note, JTable table) {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout());
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(12, 14, 10, 14));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT);
        JLabel info = new JLabel(note);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.setForeground(MUTED);
        titleBlock.add(lbl);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(info);

        head.add(titleBlock, BorderLayout.WEST);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE);
        scroll.setBackground(TABLE);
        panel.add(head, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadSettings() {
        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception { return AkademikService.getSettings(); }
            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) throw new Exception(response.get("message").getAsString());
                    fill(response.getAsJsonObject("data"));
                    rootCard.show(rootPanel, "content");
                } catch (Exception e) {
                    statePanel.showState("!", "Gagal memuat pengaturan", e.getMessage(), "Muat ulang", PengaturanAkademikPanel.this::loadSettings);
                    rootCard.show(rootPanel, "state");
                }
            }
        }.execute();
    }

    private void fill(JsonObject data) {
        tahunModel.setRowCount(0);
        String activeTahun = "-";
        for (JsonElement el : data.getAsJsonArray("tahun_ajaran")) {
            JsonObject o = el.getAsJsonObject();
            tahunModel.addRow(new Object[]{s(o, "id"), s(o, "tahun_ajaran"), s(o, "semester"), date(o, "tanggal_mulai"), date(o, "tanggal_selesai"), s(o, "status")});
            if ("aktif".equals(s(o, "status"))) {
                activeTahun = s(o, "tahun_ajaran") + " (" + s(o, "semester") + ")";
            }
        }
        lblActiveTahun.setText("Tahun ajaran aktif: " + activeTahun);
        mkModel.setRowCount(0);
        for (JsonElement el : data.getAsJsonArray("mata_kuliah")) {
            JsonObject o = el.getAsJsonObject();
            mkModel.addRow(new Object[]{s(o, "kode_mk"), s(o, "nama_mk"), s(o, "sks"), s(o, "semester"), s(o, "jurusan"), s(o, "dosen_pengampu")});
        }
        JsonObject bobot = data.getAsJsonObject("bobot_nilai");
        if (bobot != null && !bobot.isJsonNull()) {
            txtTugas.setText(s(bobot, "bobot_tugas"));
            txtUts.setText(s(bobot, "bobot_uts"));
            txtUas.setText(s(bobot, "bobot_uas"));
        }
        if (data.has("jumlah_pertemuan") && !data.get("jumlah_pertemuan").isJsonNull()) {
            txtJumlahPertemuan.setText(data.get("jumlah_pertemuan").getAsString());
        }
        updateBobotSummary();
    }

    private void showTahunDialog(int row) {
        JTextField tahun = field(row >= 0 ? String.valueOf(tahunModel.getValueAt(row, 1)) : "2025/2026");
        JComboBox<String> semester = new JComboBox<>(new String[]{"ganjil", "genap"});
        JComboBox<String> status = new JComboBox<>(new String[]{"draft", "aktif", "arsip"});
        JTextField mulai = field(row >= 0 ? String.valueOf(tahunModel.getValueAt(row, 3)) : "");
        JTextField selesai = field(row >= 0 ? String.valueOf(tahunModel.getValueAt(row, 4)) : "");
        if (row >= 0) {
            semester.setSelectedItem(String.valueOf(tahunModel.getValueAt(row, 2)));
            status.setSelectedItem(String.valueOf(tahunModel.getValueAt(row, 5)));
        }
        JPanel form = formPanel(new String[]{"Tahun", "Semester", "Tanggal Mulai", "Tanggal Selesai", "Status"}, new JComponent[]{tahun, semester, mulai, selesai, status});
        if (JOptionPane.showConfirmDialog(this, form, "Tahun Ajaran", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            JsonObject body = new JsonObject();
            body.addProperty("tahun_ajaran", tahun.getText());
            body.addProperty("semester", String.valueOf(semester.getSelectedItem()));
            body.addProperty("tanggal_mulai", mulai.getText().isBlank() ? null : mulai.getText());
            body.addProperty("tanggal_selesai", selesai.getText().isBlank() ? null : selesai.getText());
            body.addProperty("status", String.valueOf(status.getSelectedItem()));
            runSave(() -> row >= 0 ? AkademikService.updateTahunAjaran(Integer.parseInt(String.valueOf(tahunModel.getValueAt(row, 0))), body) : AkademikService.createTahunAjaran(body));
        }
    }

    private void editTahun() { int r = tahunTable.getSelectedRow(); if (r >= 0) showTahunDialog(r); }

    private void deleteTahun() {
        int r = tahunTable.getSelectedRow();
        if (r >= 0 && confirm("Hapus tahun ajaran ini?")) runSave(() -> AkademikService.deleteTahunAjaran(Integer.parseInt(String.valueOf(tahunModel.getValueAt(r, 0)))));
    }

    private void saveBobot() {
        if (!isBobotValid()) {
            JOptionPane.showMessageDialog(this, "Total bobot harus tepat 100% dan setiap bobot harus 0-100.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("bobot_tugas", parseBobot(txtTugas));
        body.addProperty("bobot_uts", parseBobot(txtUts));
        body.addProperty("bobot_uas", parseBobot(txtUas));
        runSave(() -> AkademikService.updateBobotNilai(body));
    }

    private void saveJumlahPertemuan() {
        int jumlah = parseJumlahPertemuan();
        if (jumlah < 1 || jumlah > 40) {
            JOptionPane.showMessageDialog(this, "Jumlah pertemuan harus berupa angka 1-40.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("jumlah_pertemuan", jumlah);
        runSave(() -> AkademikService.updateJumlahPertemuan(body));
    }

    private void installBobotListener(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateBobotSummary(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateBobotSummary(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateBobotSummary(); }
        });
    }

    private void updateBobotSummary() {
        if (lblSummary == null) return;
        double tugas = parseBobot(txtTugas);
        double uts = parseBobot(txtUts);
        double uas = parseBobot(txtUas);
        double total = tugas + uts + uas;
        boolean valid = isBobotValid();
        lblSummary.setText("Total: " + formatNumber(total) + "%  |  Tugas " + formatNumber(tugas) + "%, UTS " + formatNumber(uts) + "%, UAS " + formatNumber(uas) + "%");
        lblSummary.setForeground(valid ? new Color(134, 239, 172) : new Color(252, 165, 165));
        if (btnSaveBobot != null) {
            btnSaveBobot.setEnabled(valid);
        }
    }

    private boolean isBobotValid() {
        double tugas = parseBobot(txtTugas);
        double uts = parseBobot(txtUts);
        double uas = parseBobot(txtUas);
        double total = tugas + uts + uas;
        return tugas >= 0 && tugas <= 100
                && uts >= 0 && uts <= 100
                && uas >= 0 && uas <= 100
                && Math.round(total * 100.0) / 100.0 == 100.0;
    }

    private double parseBobot(JTextField field) {
        try {
            return Double.parseDouble(field.getText().trim().replace(",", "."));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int parseJumlahPertemuan() {
        try {
            return Integer.parseInt(txtJumlahPertemuan.getText().trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private interface ApiCall { JsonObject run() throws Exception; }
    private void runSave(ApiCall call) {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception { return call.run(); }
            @Override protected void done() {
                try {
                    JsonObject response = get();
                    JOptionPane.showMessageDialog(PengaturanAkademikPanel.this, response.get("message").getAsString());
                    loadSettings();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PengaturanAkademikPanel.this, e.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private JPanel formPanel(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(10, 8, 10, 8));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            panel.add(label(labels[i]), g);
            g.gridx = 1; g.weightx = 1;
            styleDialogComponent(fields[i]);
            panel.add(fields[i], g);
        }
        return panel;
    }

    private JTable table(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component component = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    component.setBackground(row % 2 == 0 ? TABLE : ROW_ALT);
                }
                return component;
            }
        };
        table.setBackground(TABLE);
        table.setForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(38);
        table.setSelectionBackground(new Color(37, 99, 235, 80));
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setForeground(TEXT);
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        table.setDefaultRenderer(Object.class, renderer);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(10, 15, 30));
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        return table;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JButton button(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(TEXT);
        btn.setBackground(color);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                new EmptyBorder(8, 12, 8, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField field(String value) {
        JTextField field = new JTextField(value);
        field.setPreferredSize(new Dimension(110, 32));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setBackground(TABLE);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 10, 0, 10)
        ));
        return field;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(MUTED);
        return label;
    }

    private void styleDialogComponent(JComponent component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (component instanceof JTextField field) {
            field.setForeground(TEXT);
            field.setBackground(TABLE);
            field.setCaretColor(TEXT);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(0, 10, 0, 10)
            ));
        } else if (component instanceof JComboBox<?> combo) {
            combo.setForeground(TEXT);
            combo.setBackground(TABLE);
            combo.setBorder(BorderFactory.createLineBorder(BORDER));
        }
    }

    private String s(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private String date(JsonObject o, String key) {
        String value = s(o, key);
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }
}
