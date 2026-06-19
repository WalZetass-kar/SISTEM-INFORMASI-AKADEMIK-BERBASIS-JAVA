package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;
import com.siakad.utils.AppTheme;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PengaturanAkademikPanel extends JPanel {
    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();

    private DefaultTableModel tahunModel;
    private DefaultTableModel mkModel;
    private DefaultTableModel pertemuanJurusanModel;
    private JTable tahunTable;
    private JTable mkTable;
    private JTable pertemuanJurusanTable;
    private JTextField txtTugas;
    private JTextField txtUts;
    private JTextField txtUas;
    private JLabel lblSummary;
    private final List<String> jurusanMasterOptions = new ArrayList<>();

    private static Color BG() { return AppTheme.bg(); }
    private static Color CARD() { return AppTheme.card(); }
    private static Color TABLE() { return AppTheme.table(); }
    private static Color BORDER() { return AppTheme.border(); }
    private static Color TEXT() { return AppTheme.text(); }
    private static Color MUTED() { return AppTheme.muted(); }
    private static Color BLUE() { return AppTheme.blue(); }
    private static Color GREEN() { return AppTheme.green(); }
    private static Color RED() { return AppTheme.red(); }

    public PengaturanAkademikPanel() {
        setBackground(BG());
        setLayout(new BorderLayout());
        rootPanel.setBackground(BG());
        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(buildContent(), "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);
        if (JwtHelper.getInstance().isAdmin()) {
            loadSettings();
        } else {
            showAccessRestricted();
        }
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(BG());
        content.setBorder(new EmptyBorder(26, 28, 14, 28));

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Pengaturan Akademik");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT());
        JLabel subtitle = new JLabel("Kelola tahun ajaran dan bobot nilai, serta lihat master mata kuliah dari modul KRS & Jadwal Kuliah");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED());
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        JButton refresh = button("Refresh", BLUE());
        refresh.addActionListener(e -> loadSettings());
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        headerActions.setOpaque(false);
        headerActions.add(AcademicUi.pill("Admin Akademik", BLUE()));
        headerActions.add(refresh);
        header.add(titleBlock, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.add(featureCard("TA", "Tahun Ajaran", "Periode aktif yang dipakai modul nilai dan absensi."));
        cards.add(featureCard("MK", "Mata Kuliah", "Master kode, SKS, semester, jurusan, dan dosen. Dikelola di KRS & Jadwal Kuliah."));
        cards.add(featureCard("%", "Bobot Nilai", "Komposisi tugas, UTS, dan UAS untuk nilai akhir."));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(cards);
        body.add(Box.createVerticalStrut(12));
        body.add(buildTables());
        body.add(Box.createVerticalStrut(12));
        body.add(buildPertemuanJurusanCard());
        body.add(Box.createVerticalStrut(12));
        body.add(buildBobotCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(BG());
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        content.add(header, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildTables() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);

        tahunModel = new DefaultTableModel(new String[]{"ID", "Tahun", "Semester", "Mulai", "Selesai", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tahunTable = table(tahunModel);
        panel.add(sectionEditable(
                "Tahun Ajaran",
                "Tambah", e -> showTahunDialog(-1),
                "Edit", e -> editTahun(),
                "Hapus", e -> deleteTahun(),
                tahunTable));

        mkModel = new DefaultTableModel(new String[]{"Kode", "Nama", "SKS", "Smt", "Jurusan", "Dosen"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        mkTable = table(mkModel);
        panel.add(sectionReadOnly(
                "Mata Kuliah",
                "Data ini mengikuti master mata kuliah di modul KRS & Jadwal Kuliah.",
                mkTable));
        return panel;
    }

    private JPanel buildBobotCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("%  Bobot Nilai");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT());
        lblSummary = new JLabel("Total bobot harus 100%.");
        lblSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSummary.setForeground(MUTED());
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(lblSummary);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        form.setOpaque(false);
        txtTugas = field("30");
        txtUts = field("30");
        txtUas = field("40");
        form.add(label("Tugas"));
        form.add(txtTugas);
        form.add(label("UTS"));
        form.add(txtUts);
        form.add(label("UAS"));
        form.add(txtUas);
        JButton save = button("Simpan Bobot", GREEN());
        save.addActionListener(e -> saveBobot());
        form.add(save);

        panel.add(left, BorderLayout.WEST);
        panel.add(form, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildPertemuanJurusanCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(12, 14, 10, 14));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Master Pertemuan per Jurusan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT());

        JLabel desc = new JLabel("<html><body style='width:320px'>Total pertemuan ini dipakai oleh menu Input Kehadiran dan otomatis mengikuti jurusan yang dipilih.</body></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(MUTED());

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(desc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(AcademicUi.pill("Sumber Input Kehadiran", BLUE()));
        JButton add = button("Tambah", GREEN());
        JButton edit = button("Edit", BLUE());
        JButton delete = button("Hapus", RED());
        JButton refresh = button("Muat Ulang", BLUE());
        add.addActionListener(e -> showPertemuanJurusanDialog(null, null));
        edit.addActionListener(e -> editPertemuanJurusan());
        delete.addActionListener(e -> deletePertemuanJurusan());
        refresh.addActionListener(e -> loadSettings());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(refresh);

        head.add(left, BorderLayout.WEST);
        head.add(actions, BorderLayout.EAST);

        pertemuanJurusanModel = new DefaultTableModel(new String[]{"Jurusan", "Jumlah Pertemuan"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pertemuanJurusanTable = table(pertemuanJurusanModel);
        pertemuanJurusanTable.setRowHeight(30);
        pertemuanJurusanTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pertemuanJurusanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pertemuanJurusanTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
        pertemuanJurusanTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        pertemuanJurusanTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        pertemuanJurusanTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        pertemuanJurusanTable.getColumnModel().getColumn(1).setMaxWidth(140);

        JScrollPane scroll = new JScrollPane(pertemuanJurusanTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE());
        scroll.setPreferredSize(new Dimension(0, 220));
        scroll.setMinimumSize(new Dimension(0, 200));

        panel.add(head, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel featureCard(String icon, String title, String desc) {
        JPanel panel = cardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(BLUE());
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT());
        JTextArea detail = new JTextArea(desc);
        detail.setOpaque(false);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detail.setForeground(MUTED());
        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(detail);
        return panel;
    }

    private JPanel sectionEditable(String title, String addText, java.awt.event.ActionListener add,
                                   String editText, java.awt.event.ActionListener edit,
                                   String delText, java.awt.event.ActionListener del, JTable table) {
        JPanel panel = AcademicUi.cardPanel(BLUE());
        panel.setLayout(new BorderLayout());
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(12, 14, 10, 14));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton addBtn = button(addText, GREEN());
        JButton editBtn = button(editText, BLUE());
        JButton delBtn = button(delText, RED());
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
        scroll.getViewport().setBackground(TABLE());
        panel.add(head, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel sectionReadOnly(String title, String note, JTable table) {
        JPanel panel = AcademicUi.cardPanel(BLUE());
        panel.setLayout(new BorderLayout());

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(12, 14, 10, 14));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT());

        JLabel desc = new JLabel("<html><body style='width:250px'>" + note + "</body></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(MUTED());

        left.add(lbl);
        left.add(Box.createVerticalStrut(3));
        left.add(desc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(AcademicUi.pill("Read-only", BLUE()));
        actions.add(AcademicUi.pill("Kelola di KRS & Jadwal", GREEN()));

        head.add(left, BorderLayout.WEST);
        head.add(actions, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE());

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

    private void showAccessRestricted() {
        statePanel.showState("!", "Akses terbatas",
                "Pengaturan Akademik hanya tersedia untuk admin.", null, null);
        rootCard.show(rootPanel, "state");
    }

    private void fill(JsonObject data) {
        tahunModel.setRowCount(0);
        for (JsonElement el : data.getAsJsonArray("tahun_ajaran")) {
            JsonObject o = el.getAsJsonObject();
            tahunModel.addRow(new Object[]{s(o, "id"), s(o, "tahun_ajaran"), s(o, "semester"), date(o, "tanggal_mulai"), date(o, "tanggal_selesai"), s(o, "status")});
        }
        mkModel.setRowCount(0);
        for (JsonElement el : data.getAsJsonArray("mata_kuliah")) {
            JsonObject o = el.getAsJsonObject();
            mkModel.addRow(new Object[]{s(o, "kode_mk"), s(o, "nama_mk"), s(o, "sks"), s(o, "semester"), s(o, "jurusan"), s(o, "dosen_pengampu")});
        }
        jurusanMasterOptions.clear();
        JsonArray jurusan = data.has("jurusan") && data.get("jurusan").isJsonArray()
                ? data.getAsJsonArray("jurusan")
                : new JsonArray();
        for (JsonElement el : jurusan) {
            JsonObject o = el.getAsJsonObject();
            if (!isActive(o)) {
                continue;
            }
            String namaJurusan = s(o, "nama_jurusan");
            if (!namaJurusan.isBlank()) {
                jurusanMasterOptions.add(namaJurusan);
            }
        }
        pertemuanJurusanModel.setRowCount(0);
        JsonArray jumlahPertemuanJurusan = data.has("jumlah_pertemuan_jurusan") && data.get("jumlah_pertemuan_jurusan").isJsonArray()
                ? data.getAsJsonArray("jumlah_pertemuan_jurusan")
                : new JsonArray();
        for (JsonElement el : jumlahPertemuanJurusan) {
            JsonObject o = el.getAsJsonObject();
            pertemuanJurusanModel.addRow(new Object[]{s(o, "jurusan"), s(o, "jumlah_pertemuan")});
        }
        JsonObject bobot = data.getAsJsonObject("bobot_nilai");
        if (bobot != null && !bobot.isJsonNull()) {
            txtTugas.setText(s(bobot, "bobot_tugas"));
            txtUts.setText(s(bobot, "bobot_uts"));
            txtUas.setText(s(bobot, "bobot_uas"));
        }
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
        JsonObject body = new JsonObject();
        body.addProperty("bobot_tugas", Double.parseDouble(txtTugas.getText()));
        body.addProperty("bobot_uts", Double.parseDouble(txtUts.getText()));
        body.addProperty("bobot_uas", Double.parseDouble(txtUas.getText()));
        runSave(() -> AkademikService.updateBobotNilai(body));
    }

    private void showPertemuanJurusanDialog(String selectedJurusan, Integer selectedJumlah) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                selectedJurusan == null ? "Tambah Pertemuan Jurusan" : "Edit Pertemuan Jurusan",
                true);
        dialog.setSize(460, 240);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD());
        form.setBorder(new EmptyBorder(22, 24, 22, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        JComboBox<String> cmbJurusan = new JComboBox<>();
        cmbJurusan.setEditable(true);
        cmbJurusan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbJurusan.setBackground(CARD());
        cmbJurusan.setForeground(TEXT());
        cmbJurusan.setPreferredSize(new Dimension(240, 36));
        cmbJurusan.setMinimumSize(new Dimension(180, 36));
        for (String option : jurusanMasterOptions) {
            cmbJurusan.addItem(option);
        }
        if (selectedJurusan != null && !selectedJurusan.isBlank() && !containsComboItem(cmbJurusan, selectedJurusan)) {
            cmbJurusan.addItem(selectedJurusan);
        }
        if (selectedJurusan != null && !selectedJurusan.isBlank()) {
            cmbJurusan.setSelectedItem(selectedJurusan);
        } else if (cmbJurusan.getItemCount() > 0) {
            cmbJurusan.setSelectedIndex(0);
        }

        JSpinner jumlah = new JSpinner(new SpinnerNumberModel(selectedJumlah == null ? 12 : selectedJumlah, 1, 40, 1));
        jumlah.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jumlah.setPreferredSize(new Dimension(120, 36));

        g.gridx = 0; g.gridy = 0;
        form.add(label("Jurusan"), g);
        g.gridx = 1;
        form.add(cmbJurusan, g);

        g.gridx = 0; g.gridy = 1;
        form.add(label("Jumlah Pertemuan"), g);
        g.gridx = 1;
        form.add(jumlah, g);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton cancel = button("Batal", BLUE());
        JButton save = button("Simpan", GREEN());
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            String jurusan = String.valueOf(cmbJurusan.getEditor().getItem()).trim();
            int total = ((Number) jumlah.getValue()).intValue();
            if (jurusan.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Nama jurusan wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!containsComboItem(cmbJurusan, jurusan)) {
                JOptionPane.showMessageDialog(dialog, "Pilih jurusan yang tersedia di master jurusan.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("jurusan", jurusan);
            body.addProperty("jumlah_pertemuan", total);
            savePertemuanJurusan(dialog, body);
        });
        actions.add(cancel);
        actions.add(save);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        g.insets = new Insets(18, 4, 4, 4);
        form.add(actions, g);

        dialog.add(form);
        dialog.setVisible(true);
    }

    private void savePertemuanJurusan(JDialog dialog, JsonObject body) {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.updateJumlahPertemuanJurusan(body);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        throw new IllegalStateException(response.has("message") ? response.get("message").getAsString() : "Gagal menyimpan data pertemuan jurusan.");
                    }
                    dialog.dispose();
                    JOptionPane.showMessageDialog(PengaturanAkademikPanel.this,
                            response.get("message").getAsString(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadSettings();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PengaturanAkademikPanel.this,
                            e.getMessage(),
                            "Gagal",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void editPertemuanJurusan() {
        int row = pertemuanJurusanTable == null ? -1 : pertemuanJurusanTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris jurusan yang ingin diedit.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = pertemuanJurusanTable.convertRowIndexToModel(row);
        String jurusan = String.valueOf(pertemuanJurusanModel.getValueAt(modelRow, 0)).trim();
        Integer jumlah = parseInteger(String.valueOf(pertemuanJurusanModel.getValueAt(modelRow, 1)));
        showPertemuanJurusanDialog(jurusan, jumlah);
    }

    private void deletePertemuanJurusan() {
        int row = pertemuanJurusanTable == null ? -1 : pertemuanJurusanTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris jurusan yang ingin dihapus.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = pertemuanJurusanTable.convertRowIndexToModel(row);
        String jurusan = String.valueOf(pertemuanJurusanModel.getValueAt(modelRow, 0)).trim();
        if (jurusan.isBlank()) {
            return;
        }
        if (!confirm("Hapus pengaturan pertemuan untuk jurusan " + jurusan + "?")) {
            return;
        }
        runSave(() -> AkademikService.deleteJumlahPertemuanJurusan(jurusan));
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
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            panel.add(new JLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 1;
            panel.add(fields[i], g);
        }
        return panel;
    }

    private JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(TABLE());
        table.setForeground(TEXT());
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(37, 99, 235, 80));
        table.setSelectionForeground(TEXT());
        table.setGridColor(BORDER());
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(10, 15, 30));
        header.setForeground(MUTED());
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return table;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER());
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
        btn.setForeground(TEXT());
        btn.setBackground(color);
        btn.setPreferredSize(new Dimension(104, 36));
        btn.setMinimumSize(new Dimension(96, 36));
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    private JTextField field(String value) {
        JTextField field = new JTextField(value);
        field.setPreferredSize(new Dimension(110, 32));
        field.setMinimumSize(new Dimension(90, 32));
        return field;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED());
        return label;
    }

    private boolean containsComboItem(JComboBox<String> combo, String value) {
        if (combo == null || value == null) {
            return false;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (value.equalsIgnoreCase(String.valueOf(item).trim())) {
                return true;
            }
        }
        return false;
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private boolean isActive(JsonObject object) {
        String value = s(object, "is_active");
        return !value.isBlank() && !"0".equals(value) && !"false".equalsIgnoreCase(value);
    }

    private String s(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private String date(JsonObject o, String key) {
        String value = s(o, key);
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }
}
