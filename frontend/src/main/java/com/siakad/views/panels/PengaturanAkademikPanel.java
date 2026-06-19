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
    private DefaultTableModel semesterModel;
    private DefaultTableModel jurusanModel;
    private DefaultTableModel mkModel;
    private DefaultTableModel pertemuanJurusanModel;
    private JTable tahunTable;
    private JTable semesterTable;
    private JTable jurusanTable;
    private JTable mkTable;
    private JTable pertemuanJurusanTable;
    private JTextField txtTugas;
    private JTextField txtUts;
    private JTextField txtUas;
    private JLabel lblSummary;
    private JLabel lblActiveTahun;
    private JLabel lblTahunCount;
    private JLabel lblSemesterCount;
    private JLabel lblJurusanCount;
    private JLabel lblMkCount;
    private JButton btnSaveBobot;

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

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Pengaturan Akademik");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Kelola tahun ajaran, semester, bobot nilai, jurusan, dan jumlah pertemuan per jurusan");
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
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        headerActions.setOpaque(false);
        headerActions.add(AcademicUi.pill("Admin Akademik", BLUE));
        headerActions.add(refresh);
        header.add(titleBlock, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));
        lblTahunCount = statValue("0");
        lblSemesterCount = statValue("0");
        lblJurusanCount = statValue("0");
        lblMkCount = statValue("0");
        cards.add(metricCard("TA", "Tahun Ajaran", "Periode akademik", lblTahunCount));
        cards.add(metricCard("S", "Semester Aktif", "Sumber dropdown", lblSemesterCount));
        cards.add(metricCard("J", "Jurusan Aktif", "Program studi tersedia", lblJurusanCount));
        cards.add(metricCard("MK", "Mata Kuliah", "Referensi KRS", lblMkCount));

        JPanel tables = buildTables();
        tables.setAlignmentX(Component.LEFT_ALIGNMENT);
        tables.setPreferredSize(new Dimension(0, 760));
        tables.setMaximumSize(new Dimension(Integer.MAX_VALUE, 760));

        JPanel bobot = buildBobotCard();
        bobot.setAlignmentX(Component.LEFT_ALIGNMENT);
        bobot.setPreferredSize(new Dimension(0, 590));
        bobot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 590));

        content.add(header);
        content.add(Box.createVerticalStrut(18));
        content.add(cards);
        content.add(Box.createVerticalStrut(18));
        content.add(tables);
        content.add(Box.createVerticalStrut(18));
        content.add(bobot);
        content.add(Box.createVerticalStrut(18));
        return content;
    }

    private JPanel buildTables() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        tahunModel = new DefaultTableModel(new String[]{"ID", "No", "Tahun", "Semester", "Mulai", "Selesai", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tahunTable = table(tahunModel);
        hideModelIdColumn(tahunTable);
        tahunTable.getColumnModel().getColumn(0).setMaxWidth(52);

        semesterModel = new DefaultTableModel(new String[]{"ID", "No", "Semester", "Nama Semester", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        semesterTable = table(semesterModel);
        hideModelIdColumn(semesterTable);
        semesterTable.getColumnModel().getColumn(0).setMaxWidth(52);
        semesterTable.getColumnModel().getColumn(1).setMaxWidth(74);
        semesterTable.getColumnModel().getColumn(3).setMaxWidth(88);

        jurusanModel = new DefaultTableModel(new String[]{"ID", "No", "Nama Jurusan", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jurusanTable = table(jurusanModel);
        hideModelIdColumn(jurusanTable);
        jurusanTable.getColumnModel().getColumn(0).setMaxWidth(52);
        jurusanTable.getColumnModel().getColumn(2).setMaxWidth(88);

        mkModel = new DefaultTableModel(new String[]{"Kode", "Nama", "SKS", "Smt", "Jurusan", "Dosen"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        mkTable = table(mkModel);

        JPanel topGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        topGrid.setOpaque(false);
        topGrid.add(section("Tahun Ajaran", "Tambah", e -> showTahunDialog(-1), "Edit", e -> editTahun(), "Hapus", e -> deleteTahun(), tahunTable));

        JPanel rightStack = new JPanel(new GridLayout(2, 1, 0, 14));
        rightStack.setOpaque(false);
        rightStack.add(section("Kelola Semester", "Tambah", e -> showSemesterDialog(-1), "Edit", e -> editSemester(), "Nonaktifkan", e -> deleteSemester(), semesterTable));
        rightStack.add(section("Kelola Jurusan", "Tambah", e -> showJurusanDialog(-1), "Edit", e -> editJurusan(), "Nonaktifkan", e -> deleteJurusan(), jurusanTable));
        topGrid.add(rightStack);
        topGrid.setPreferredSize(new Dimension(0, 410));

        JPanel mataKuliah = readOnlySection("Mata Kuliah", "Referensi mata kuliah aktif dari menu KRS & Jadwal Kuliah.", mkTable);
        mataKuliah.setPreferredSize(new Dimension(0, 330));

        panel.add(sectionTitle("Data Master Akademik", "Kelola periode, semester aktif/nonaktif, jurusan, dan referensi mata kuliah."), BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(topGrid, BorderLayout.NORTH);
        content.add(mataKuliah, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBobotCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Konfigurasi Akademik");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT);
        lblSummary = new JLabel("Total bobot harus 100%.");
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSummary.setForeground(MUTED);
        header.add(title, BorderLayout.WEST);
        header.add(lblSummary, BorderLayout.EAST);

        JPanel configContent = new JPanel();
        configContent.setOpaque(false);
        configContent.setLayout(new BoxLayout(configContent, BoxLayout.Y_AXIS));

        JPanel bobotBlock = configBlock("Bobot Nilai", "Komposisi nilai akhir mahasiswa untuk perhitungan otomatis.");
        bobotBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 106));
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

        JPanel pertemuanBlock = buildPertemuanJurusanBlock();
        pertemuanBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        configContent.add(bobotBlock);
        configContent.add(Box.createVerticalStrut(16));
        configContent.add(pertemuanBlock);
        updateBobotSummary();

        panel.add(header, BorderLayout.NORTH);
        panel.add(configContent, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPertemuanJurusanBlock() {
        JPanel jurusanBlock = configBlock("Pertemuan per Jurusan", "Input Kehadiran mengikuti angka pertemuan jurusan aktif.");
        pertemuanJurusanModel = new DefaultTableModel(new String[]{"Jurusan", "Pertemuan"}, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pertemuanJurusanTable = table(pertemuanJurusanModel);
        pertemuanJurusanTable.getColumnModel().getColumn(1).setMaxWidth(95);
        pertemuanJurusanTable.setRowHeight(38);

        JScrollPane scroll = new JScrollPane(pertemuanJurusanTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(TABLE);
        scroll.setPreferredSize(new Dimension(0, 340));

        JButton save = button("Update Pertemuan", BLUE);
        save.addActionListener(e -> showPertemuanJurusanDialog());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(save);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(actions, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        jurusanBlock.add(body, BorderLayout.CENTER);
        return jurusanBlock;
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

    private JPanel metricCard(String icon, String title, String desc, JLabel value) {
        JPanel panel = AcademicUi.cardPanel(BLUE);
        panel.setLayout(new BorderLayout(14, 0));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLabel.setForeground(BLUE);
        iconLabel.setPreferredSize(new Dimension(44, 44));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT);
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(MUTED);
        text.add(value);
        text.add(Box.createVerticalStrut(3));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(descLabel);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JLabel statValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(TEXT);
        return label;
    }

    private JPanel sectionTitle(String title, String desc) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 2, 0, 2));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT);
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(MUTED);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));
        text.add(descLabel);

        panel.add(text, BorderLayout.WEST);
        return panel;
    }

    private JPanel section(String title, String addText, java.awt.event.ActionListener add,
                           String editText, java.awt.event.ActionListener edit,
                           String delText, java.awt.event.ActionListener del, JTable table) {
        JPanel panel = AcademicUi.cardPanel(BLUE);
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
        JPanel panel = AcademicUi.cardPanel(new Color(99, 102, 241));
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
        int tahunCount = 0;
        int tahunNo = 1;
        for (JsonElement el : data.getAsJsonArray("tahun_ajaran")) {
            JsonObject o = el.getAsJsonObject();
            tahunModel.addRow(new Object[]{s(o, "id"), tahunNo++, s(o, "tahun_ajaran"), s(o, "semester"), date(o, "tanggal_mulai"), date(o, "tanggal_selesai"), s(o, "status")});
            tahunCount++;
            if ("aktif".equals(s(o, "status"))) {
                activeTahun = s(o, "tahun_ajaran") + " (" + s(o, "semester") + ")";
            }
        }
        lblActiveTahun.setText("Tahun ajaran aktif: " + activeTahun);
        semesterModel.setRowCount(0);
        int semesterAktif = 0;
        int semesterNo = 1;
        if (data.has("semester") && data.get("semester").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("semester")) {
                JsonObject o = el.getAsJsonObject();
                semesterModel.addRow(new Object[]{s(o, "id"), semesterNo++, s(o, "nomor"), s(o, "nama_semester"), statusAktif(o)});
                if ("Aktif".equals(statusAktif(o))) semesterAktif++;
            }
        }
        jurusanModel.setRowCount(0);
        int jurusanAktif = 0;
        int jurusanNo = 1;
        if (data.has("jurusan") && data.get("jurusan").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("jurusan")) {
                JsonObject o = el.getAsJsonObject();
                jurusanModel.addRow(new Object[]{s(o, "id"), jurusanNo++, s(o, "nama_jurusan"), statusJurusan(o)});
                if ("Aktif".equals(statusJurusan(o))) jurusanAktif++;
            }
        }
        mkModel.setRowCount(0);
        int mkCount = 0;
        for (JsonElement el : data.getAsJsonArray("mata_kuliah")) {
            JsonObject o = el.getAsJsonObject();
            mkModel.addRow(new Object[]{s(o, "kode_mk"), s(o, "nama_mk"), s(o, "sks"), s(o, "semester"), s(o, "jurusan"), s(o, "dosen_pengampu")});
            mkCount++;
        }
        lblTahunCount.setText(String.valueOf(tahunCount));
        lblSemesterCount.setText(String.valueOf(semesterAktif));
        lblJurusanCount.setText(String.valueOf(jurusanAktif));
        lblMkCount.setText(String.valueOf(mkCount));
        pertemuanJurusanModel.setRowCount(0);
        if (data.has("jumlah_pertemuan_jurusan") && data.get("jumlah_pertemuan_jurusan").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("jumlah_pertemuan_jurusan")) {
                JsonObject o = el.getAsJsonObject();
                pertemuanJurusanModel.addRow(new Object[]{s(o, "jurusan"), s(o, "jumlah_pertemuan")});
            }
        }
        JsonObject bobot = data.getAsJsonObject("bobot_nilai");
        if (bobot != null && !bobot.isJsonNull()) {
            txtTugas.setText(s(bobot, "bobot_tugas"));
            txtUts.setText(s(bobot, "bobot_uts"));
            txtUas.setText(s(bobot, "bobot_uas"));
        }
        updateBobotSummary();
    }

    private void showTahunDialog(int row) {
        JTextField tahun = field(row >= 0 ? String.valueOf(tahunModel.getValueAt(row, 2)) : "2025/2026");
        JComboBox<String> semester = new JComboBox<>(new String[]{"ganjil", "genap"});
        JComboBox<String> status = new JComboBox<>(new String[]{"draft", "aktif", "arsip"});
        JTextField mulai = field(row >= 0 ? String.valueOf(tahunModel.getValueAt(row, 4)) : "");
        JTextField selesai = field(row >= 0 ? String.valueOf(tahunModel.getValueAt(row, 5)) : "");
        if (row >= 0) {
            semester.setSelectedItem(String.valueOf(tahunModel.getValueAt(row, 3)));
            status.setSelectedItem(String.valueOf(tahunModel.getValueAt(row, 6)));
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

    private void showSemesterDialog(int row) {
        JTextField nomor = field(row >= 0 ? String.valueOf(semesterModel.getValueAt(row, 2)) : "");
        JTextField nama = field(row >= 0 ? String.valueOf(semesterModel.getValueAt(row, 3)) : "");
        JComboBox<String> status = new JComboBox<>(new String[]{"Aktif", "Nonaktif"});
        if (row >= 0) {
            status.setSelectedItem(String.valueOf(semesterModel.getValueAt(row, 4)));
        }
        JPanel form = formPanel(new String[]{"Nomor Semester", "Nama Semester", "Status"}, new JComponent[]{nomor, nama, status});
        if (JOptionPane.showConfirmDialog(this, form, "Kelola Semester", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            int nomorValue = parsePositiveInt(nomor.getText());
            if (nomorValue < 1 || nomorValue > 20) {
                JOptionPane.showMessageDialog(this, "Nomor semester harus berupa angka 1-20.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String namaValue = nama.getText().trim();
            if (namaValue.isBlank()) {
                namaValue = "Semester " + nomorValue;
            }

            JsonObject body = new JsonObject();
            body.addProperty("nomor", nomorValue);
            body.addProperty("nama_semester", namaValue);
            body.addProperty("is_active", "Aktif".equals(String.valueOf(status.getSelectedItem())) ? 1 : 0);
            runSave(() -> row >= 0
                    ? AkademikService.updateSemester(Integer.parseInt(String.valueOf(semesterModel.getValueAt(row, 0))), body)
                    : AkademikService.createSemester(body));
        }
    }

    private void editSemester() {
        int row = semesterTable.getSelectedRow();
        if (row >= 0) showSemesterDialog(semesterTable.convertRowIndexToModel(row));
    }

    private void deleteSemester() {
        int row = semesterTable.getSelectedRow();
        if (row < 0) return;
        int modelRow = semesterTable.convertRowIndexToModel(row);
        String nama = String.valueOf(semesterModel.getValueAt(modelRow, 3));
        if ("Nonaktif".equals(String.valueOf(semesterModel.getValueAt(modelRow, 4)))) {
            JOptionPane.showMessageDialog(this, "Semester ini sudah nonaktif.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (confirm("Nonaktifkan " + nama + "? Data lama tetap tersimpan, tetapi semester ini tidak muncul di dropdown data baru.")) {
            runSave(() -> AkademikService.deleteSemester(Integer.parseInt(String.valueOf(semesterModel.getValueAt(modelRow, 0)))));
        }
    }

    private void showJurusanDialog(int row) {
        JTextField nama = field(row >= 0 ? String.valueOf(jurusanModel.getValueAt(row, 2)) : "");
        JComboBox<String> status = new JComboBox<>(new String[]{"Aktif", "Nonaktif"});
        if (row >= 0) {
            status.setSelectedItem(String.valueOf(jurusanModel.getValueAt(row, 3)));
        }
        JPanel form = formPanel(new String[]{"Nama Jurusan", "Status"}, new JComponent[]{nama, status});
        if (JOptionPane.showConfirmDialog(this, form, "Kelola Jurusan", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (nama.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(this, "Nama jurusan wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("nama_jurusan", nama.getText().trim());
            body.addProperty("is_active", "Aktif".equals(String.valueOf(status.getSelectedItem())) ? 1 : 0);
            runSave(() -> row >= 0
                    ? AkademikService.updateJurusan(Integer.parseInt(String.valueOf(jurusanModel.getValueAt(row, 0))), body)
                    : AkademikService.createJurusan(body));
        }
    }

    private void editJurusan() {
        int row = jurusanTable.getSelectedRow();
        if (row >= 0) showJurusanDialog(jurusanTable.convertRowIndexToModel(row));
    }

    private void deleteJurusan() {
        int row = jurusanTable.getSelectedRow();
        if (row < 0) return;
        int modelRow = jurusanTable.convertRowIndexToModel(row);
        String nama = String.valueOf(jurusanModel.getValueAt(modelRow, 2));
        if ("Nonaktif".equals(String.valueOf(jurusanModel.getValueAt(modelRow, 3)))) {
            JOptionPane.showMessageDialog(this, "Jurusan ini sudah nonaktif.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (confirm("Nonaktifkan jurusan " + nama + "? Data mahasiswa lama tetap tersimpan, tetapi jurusan ini tidak muncul di dropdown data baru.")) {
            runSave(() -> AkademikService.deleteJurusan(Integer.parseInt(String.valueOf(jurusanModel.getValueAt(modelRow, 0)))));
        }
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

    private void showPertemuanJurusanDialog() {
        if (pertemuanJurusanModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada jurusan aktif yang bisa diatur.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> jurusanCombo = new JComboBox<>();
        for (int i = 0; i < pertemuanJurusanModel.getRowCount(); i++) {
            String jurusan = String.valueOf(pertemuanJurusanModel.getValueAt(i, 0)).trim();
            if (!jurusan.isBlank()) {
                jurusanCombo.addItem(jurusan);
            }
        }
        styleDialogComponent(jurusanCombo);
        JTextField jumlahField = field(String.valueOf(findJumlahPertemuan(String.valueOf(jurusanCombo.getSelectedItem()))));
        jurusanCombo.addActionListener(e -> jumlahField.setText(String.valueOf(findJumlahPertemuan(String.valueOf(jurusanCombo.getSelectedItem())))));
        JPanel form = formPanel(
                new String[]{"Jurusan", "Jumlah Pertemuan"},
                new JComponent[]{jurusanCombo, jumlahField}
        );
        int result = JOptionPane.showConfirmDialog(this, form, "Update Pertemuan Jurusan", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String jurusan = String.valueOf(jurusanCombo.getSelectedItem()).trim();
        int jumlah = parsePositiveInt(jumlahField.getText());
        if (jurusan.isBlank()) {
            JOptionPane.showMessageDialog(this, "Jurusan wajib dipilih.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (jumlah < 1 || jumlah > 40) {
            JOptionPane.showMessageDialog(this, "Jumlah pertemuan jurusan harus berupa angka 1-40.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("jurusan", jurusan);
        body.addProperty("jumlah_pertemuan", jumlah);
        runSave(() -> AkademikService.updateJumlahPertemuanJurusan(body));
    }

    private int findJumlahPertemuan(String jurusan) {
        for (int i = 0; i < pertemuanJurusanModel.getRowCount(); i++) {
            if (jurusan.equals(String.valueOf(pertemuanJurusanModel.getValueAt(i, 0)))) {
                return parsePositiveInt(pertemuanJurusanModel.getValueAt(i, 1));
            }
        }
        return 12;
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

    private int parsePositiveInt(Object value) {
        try {
            return Integer.parseInt(String.valueOf(value).trim());
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

    private void hideModelIdColumn(JTable table) {
        if (table.getColumnModel().getColumnCount() > 0) {
            table.removeColumn(table.getColumnModel().getColumn(0));
        }
    }

    private JPanel cardPanel() {
        return AcademicUi.cardPanel(BLUE);
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

    private String statusJurusan(JsonObject o) {
        return statusAktif(o);
    }

    private String statusAktif(JsonObject o) {
        String value = s(o, "is_active").trim();
        return "0".equals(value) || "false".equalsIgnoreCase(value) ? "Nonaktif" : "Aktif";
    }

    private String date(JsonObject o, String key) {
        String value = s(o, key);
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }
}
