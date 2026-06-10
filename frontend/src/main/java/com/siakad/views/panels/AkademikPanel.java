package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;
import com.siakad.services.MahasiswaService;
import com.siakad.services.NilaiService;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class AkademikPanel extends JPanel {

    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();

    private JComboBox<MataKuliahItem> cmbMataKuliah;
    private JComboBox<String> cmbTahunAjaran;
    private JComboBox<String> cmbJurusan;
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblInfo;
    private JLabel lblCourseSummary;
    private JLabel lblCountSummary;
    private JLabel lblFormulaSummary;
    private JLabel lblBobotBadge;
    private JButton btnSave;
    private boolean recalculating = false;
    private double bobotTugas = 30.0;
    private double bobotUts = 30.0;
    private double bobotUas = 40.0;

    private static final Color BG = new Color(13, 19, 38);
    private static final Color CARD_BG = new Color(18, 26, 48);
    private static final Color TABLE_BG = new Color(15, 22, 42);
    private static final Color HEADER_BG = new Color(10, 15, 30);
    private static final Color BORDER = new Color(25, 36, 65);
    private static final Color ROW_ALT = new Color(20, 29, 52);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color DIM = new Color(71, 85, 105);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color AMBER = new Color(234, 179, 8);

    public AkademikPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());

        rootPanel.setBackground(BG);
        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(buildContent(), "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);

        if (JwtHelper.getInstance().isAdmin()) {
            loadAcademicSettings();
        } else {
            showState("Akses terbatas", "Input nilai hanya tersedia untuk admin.");
        }
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(buildTableCard(), BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(26, 28, 18, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Input Nilai Mahasiswa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Akademik / Nilai & Absensi / Input Nilai");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        lblBobotBadge = new JLabel(bobotText());
        lblBobotBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBobotBadge.setForeground(new Color(191, 219, 254));
        lblBobotBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246, 90)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        top.add(titleBlock, BorderLayout.WEST);
        top.add(lblBobotBadge, BorderLayout.EAST);

        JPanel filterCard = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        filterCard.setOpaque(false);
        filterCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 0, 12);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;

        cmbTahunAjaran = new JComboBox<>();
        styleCombo(cmbTahunAjaran, 145);

        cmbJurusan = new JComboBox<>();
        cmbJurusan.addItem("Semua Jurusan");
        styleCombo(cmbJurusan, 220);

        cmbMataKuliah = new JComboBox<>();
        styleCombo(cmbMataKuliah, 280);

        txtSearch = new JTextField();
        styleTextField(txtSearch, 190);

        JButton btnLoad = buildButton("Tampilkan", BLUE);
        JButton btnRefresh = buildButton("Refresh", CARD_BG);
        btnLoad.addActionListener(e -> loadInputList());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadAcademicSettings();
        });

        g.gridx = 0; g.weightx = 0;
        fields.add(labeledField("Tahun Ajaran", cmbTahunAjaran), g);
        g.gridx = 1; g.weightx = 0;
        fields.add(labeledField("Jurusan", cmbJurusan), g);
        g.gridx = 2; g.weightx = 1;
        fields.add(labeledField("Mata Kuliah", cmbMataKuliah), g);
        g.gridx = 3; g.weightx = 0;
        fields.add(labeledField("Cari Mahasiswa", txtSearch), g);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        actions.setOpaque(false);
        actions.add(btnLoad);
        actions.add(btnRefresh);

        filterCard.add(fields, BorderLayout.CENTER);
        filterCard.add(actions, BorderLayout.EAST);

        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(filterCard, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadAcademicSettings() {
        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getSettings();
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        showState("Gagal memuat pengaturan akademik", response.get("message").getAsString());
                        return;
                    }
                    populateTahunAjaran(response.getAsJsonObject("data").getAsJsonArray("tahun_ajaran"));
                    loadJurusanList();
                    loadMataKuliah();
                } catch (Exception ex) {
                    showState("Gagal memuat pengaturan akademik", ex.getMessage());
                } finally {
                    skeleton.stop();
                }
            }
        }.execute();
    }

    private void populateTahunAjaran(JsonArray data) {
        cmbTahunAjaran.removeAllItems();
        String active = null;
        for (JsonElement item : data) {
            JsonObject tahun = item.getAsJsonObject();
            String label = getString(tahun, "tahun_ajaran");
            if (!label.isBlank()) {
                cmbTahunAjaran.addItem(label);
                if ("aktif".equals(getString(tahun, "status"))) {
                    active = label;
                }
            }
        }
        if (active != null) {
            cmbTahunAjaran.setSelectedItem(active);
        }
    }

    private void loadJurusanList() {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getJurusanList();
            }

            @Override protected void done() {
                try {
                    Object previous = cmbJurusan.getSelectedItem();
                    cmbJurusan.removeAllItems();
                    cmbJurusan.addItem("Semua Jurusan");

                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        JsonArray data = response.getAsJsonArray("data");
                        for (JsonElement item : data) {
                            String jurusan = item.getAsString();
                            if (jurusan != null && !jurusan.isBlank()) {
                                cmbJurusan.addItem(jurusan);
                            }
                        }
                    }

                    if (previous != null) {
                        cmbJurusan.setSelectedItem(previous);
                    }
                } catch (Exception ex) {
                    cmbJurusan.removeAllItems();
                    cmbJurusan.addItem("Semua Jurusan");
                    if (lblInfo != null) {
                        lblInfo.setText("Daftar jurusan gagal dimuat, filter jurusan memakai semua data.");
                    }
                }
            }
        }.execute();
    }

    private JComponent buildTableCard() {
        String[] columns = {"NIM", "Nama", "Jurusan", "Tugas", "UTS", "UAS", "Nilai Akhir", "Grade", "Status", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return (col >= 3 && col <= 5) || col == 9;
            }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                }
                return c;
            }
        };
        styleTable(table);
        table.getColumnModel().getColumn(7).setCellRenderer(new GradeRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(9).setMinWidth(126);
        table.getColumnModel().getColumn(9).setMaxWidth(126);
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && !recalculating && e.getColumn() >= 3 && e.getColumn() <= 5) {
                recalculateRow(e.getFirstRow());
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE_BG);
        scroll.setBackground(TABLE_BG);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(0, 28, 0, 28));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel tableTitle = new JLabel("Daftar Nilai");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT);
        lblCourseSummary = new JLabel("Pilih mata kuliah untuk mulai mengisi nilai.");
        lblCourseSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCourseSummary.setForeground(MUTED);
        headerText.add(tableTitle);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(lblCourseSummary);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stats.setOpaque(false);
        lblCountSummary = metricLabel("0 Mahasiswa");
        lblFormulaSummary = metricLabel("Otomatis");
        stats.add(lblCountSummary);
        stats.add(lblFormulaSummary);

        tableHeader.add(headerText, BorderLayout.WEST);
        tableHeader.add(stats, BorderLayout.EAST);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 28, 14, 28));

        lblInfo = new JLabel("Pilih mata kuliah untuk memuat mahasiswa.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(MUTED);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnReset = buildButton("Reset", CARD_BG);
        btnSave = buildButton("Simpan Semua", GREEN);
        btnSave.setEnabled(false);
        btnReset.addActionListener(e -> loadInputList());
        btnSave.addActionListener(e -> saveAll());
        actions.add(btnReset);
        actions.add(btnSave);

        footer.add(lblInfo, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = smallLabel(labelText);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private JLabel metricLabel(String text) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(203, 213, 225));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 8, 7, 8)
        ));
        return label;
    }

    private void loadMataKuliah() {
        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return NilaiService.getMataKuliah("", "");
            }

            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        showState("Gagal memuat mata kuliah", response.get("message").getAsString());
                        return;
                    }

                    cmbMataKuliah.removeAllItems();
                    JsonArray data = response.getAsJsonArray("data");
                    for (JsonElement item : data) {
                        JsonObject mk = item.getAsJsonObject();
                        cmbMataKuliah.addItem(new MataKuliahItem(
                                getString(mk, "kode_mk"),
                                getString(mk, "nama_mk"),
                                getInt(mk, "semester")
                        ));
                    }

                    rootCard.show(rootPanel, "content");
                    if (cmbMataKuliah.getItemCount() > 0) {
                        loadInputList();
                    } else {
                        tableModel.setRowCount(0);
                        btnSave.setEnabled(false);
                        lblInfo.setText("Belum ada data mata kuliah.");
                    }
                } catch (Exception ex) {
                    showState("Gagal memuat mata kuliah", ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadInputList() {
        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        if (selected == null) {
            showState("Mata kuliah kosong", "Tambahkan data mata kuliah terlebih dahulu.");
            return;
        }
        if (cmbTahunAjaran.getSelectedItem() == null) {
            showState("Tahun ajaran kosong", "Aktifkan atau tambahkan tahun ajaran di Pengaturan Akademik.");
            return;
        }

        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return NilaiService.getInputList(
                        selected.kodeMk,
                        (String) cmbTahunAjaran.getSelectedItem(),
                        txtSearch.getText().trim(),
                        selectedJurusan()
                );
            }

            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        showState("Gagal memuat input nilai", response.get("message").getAsString());
                        return;
                    }
                    JsonObject mataKuliah = response.getAsJsonObject("mata_kuliah");
                    applyBobot(response.has("bobot_nilai") ? response.getAsJsonObject("bobot_nilai") : null);
                    fillRows(response.getAsJsonArray("data"), mataKuliah);
                    rootCard.show(rootPanel, "content");
                } catch (Exception ex) {
                    showState("Gagal memuat input nilai", ex.getMessage());
                }
            }
        }.execute();
    }

    private void fillRows(JsonArray data, JsonObject mataKuliah) {
        recalculating = true;
        tableModel.setRowCount(0);
        for (JsonElement element : data) {
            JsonObject row = element.getAsJsonObject();
            double tugas = getDouble(row, "nilai_tugas");
            double uts = getDouble(row, "nilai_uts");
            double uas = getDouble(row, "nilai_uas");
            double akhir = calculateFinal(tugas, uts, uas);
            String grade = calculateGrade(akhir);
            boolean saved = hasValue(row, "id_nilai");
            tableModel.addRow(new Object[]{
                    getString(row, "nim"),
                    getString(row, "nama"),
                    getString(row, "jurusan"),
                    formatScore(tugas),
                    formatScore(uts),
                    formatScore(uas),
                    formatScore(akhir),
                    grade,
                    saved ? "Tersimpan" : "Baru",
                    "aksi"
            });
        }
        recalculating = false;
        btnSave.setEnabled(data.size() > 0);
        String kode = getString(mataKuliah, "kode_mk");
        String nama = getString(mataKuliah, "nama_mk");
        String semester = getString(mataKuliah, "semester");
        String jurusan = selectedJurusan();
        String jurusanInfo = jurusan.isBlank() ? "Semua Jurusan" : jurusan;
        lblCourseSummary.setText(kode + " - " + nama + " • Semester " + semester + " • " + cmbTahunAjaran.getSelectedItem() + " • " + jurusanInfo);
        lblCountSummary.setText("  " + data.size() + " Mahasiswa  ");
        lblFormulaSummary.setText("  " + formulaText() + "  ");
        lblInfo.setText(data.size() == 0
                ? "Belum ada mahasiswa yang mengambil mata kuliah ini di KRS."
                : "Edit nilai langsung di kolom Tugas, UTS, dan UAS. Nilai akhir dihitung otomatis sebelum disimpan.");
    }

    private void saveAll() {
        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        if (selected == null || tableModel.getRowCount() == 0) return;

        JsonArray nilai = new JsonArray();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            JsonObject item = new JsonObject();
            item.addProperty("nim", String.valueOf(tableModel.getValueAt(i, 0)));
            item.addProperty("nilai_tugas", parseScore(tableModel.getValueAt(i, 3)));
            item.addProperty("nilai_uts", parseScore(tableModel.getValueAt(i, 4)));
            item.addProperty("nilai_uas", parseScore(tableModel.getValueAt(i, 5)));
            nilai.add(item);
        }

        JsonObject body = new JsonObject();
        body.addProperty("kode_mk", selected.kodeMk);
        body.addProperty("tahun_ajaran", (String) cmbTahunAjaran.getSelectedItem());
        body.add("nilai", nilai);

        btnSave.setEnabled(false);
        lblInfo.setText("Menyimpan nilai...");
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return NilaiService.bulkSave(body);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    JOptionPane.showMessageDialog(AkademikPanel.this,
                            response.get("message").getAsString(),
                            response.get("success").getAsBoolean() ? "Berhasil" : "Gagal",
                            response.get("success").getAsBoolean() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    loadInputList();
                } catch (Exception ex) {
                    btnSave.setEnabled(true);
                    lblInfo.setText("Gagal menyimpan nilai.");
                    JOptionPane.showMessageDialog(AkademikPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String selectedJurusan() {
        if (cmbJurusan == null || cmbJurusan.getSelectedItem() == null) {
            return "";
        }
        String value = String.valueOf(cmbJurusan.getSelectedItem()).trim();
        return "Semua Jurusan".equals(value) ? "" : value;
    }

    private void showNilaiDialog(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;

        String nim = String.valueOf(tableModel.getValueAt(row, 0));
        String nama = String.valueOf(tableModel.getValueAt(row, 1));

        JTextField txtTugas = scoreField(tableModel.getValueAt(row, 3));
        JTextField txtUts = scoreField(tableModel.getValueAt(row, 4));
        JTextField txtUas = scoreField(tableModel.getValueAt(row, 5));
        JLabel lblPreview = new JLabel();
        lblPreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPreview.setForeground(TEXT);

        Runnable updatePreview = () -> {
            double akhir = calculateFinal(parseScore(txtTugas.getText()), parseScore(txtUts.getText()), parseScore(txtUas.getText()));
            lblPreview.setText("Nilai akhir: " + formatScore(akhir) + " • Grade " + calculateGrade(akhir));
        };

        javax.swing.event.DocumentListener listener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview.run(); }
        };
        txtTugas.getDocument().addDocumentListener(listener);
        txtUts.getDocument().addDocumentListener(listener);
        txtUas.getDocument().addDocumentListener(listener);
        updatePreview.run();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 8, 4, 8));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1;
        JLabel student = new JLabel(nim + " - " + nama);
        student.setFont(new Font("Segoe UI", Font.BOLD, 14));
        form.add(student, g);

        g.gridwidth = 1; g.weightx = 0;
        addDialogRow(form, g, 1, "Tugas", txtTugas);
        addDialogRow(form, g, 2, "UTS", txtUts);
        addDialogRow(form, g, 3, "UAS", txtUas);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        form.add(lblPreview, g);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Input/Edit Nilai",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            tableModel.setValueAt(formatScore(parseScore(txtTugas.getText())), row, 3);
            tableModel.setValueAt(formatScore(parseScore(txtUts.getText())), row, 4);
            tableModel.setValueAt(formatScore(parseScore(txtUas.getText())), row, 5);
            recalculateRow(row);
        }
    }

    private JTextField scoreField(Object value) {
        JTextField field = new JTextField(String.valueOf(value));
        field.setPreferredSize(new Dimension(120, 32));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return field;
    }

    private void addDialogRow(JPanel form, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.weightx = 0;
        form.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1;
        form.add(field, g);
    }

    private void deleteNilai(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;

        String status = String.valueOf(tableModel.getValueAt(row, 8));
        String nim = String.valueOf(tableModel.getValueAt(row, 0));
        String nama = String.valueOf(tableModel.getValueAt(row, 1));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Hapus nilai " + nama + " (" + nim + ")?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        if (!"Tersimpan".equals(status)) {
            clearRow(row);
            return;
        }

        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        if (selected == null) return;

        JsonObject body = new JsonObject();
        body.addProperty("nim", nim);
        body.addProperty("kode_mk", selected.kodeMk);
        body.addProperty("tahun_ajaran", (String) cmbTahunAjaran.getSelectedItem());

        lblInfo.setText("Menghapus nilai " + nim + "...");
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return NilaiService.delete(body);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    JOptionPane.showMessageDialog(AkademikPanel.this,
                            response.get("message").getAsString(),
                            response.get("success").getAsBoolean() ? "Berhasil" : "Gagal",
                            response.get("success").getAsBoolean() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    loadInputList();
                } catch (Exception ex) {
                    lblInfo.setText("Gagal menghapus nilai.");
                    JOptionPane.showMessageDialog(AkademikPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void clearRow(int row) {
        recalculating = true;
        tableModel.setValueAt("0.00", row, 3);
        tableModel.setValueAt("0.00", row, 4);
        tableModel.setValueAt("0.00", row, 5);
        tableModel.setValueAt("0.00", row, 6);
        tableModel.setValueAt("E", row, 7);
        tableModel.setValueAt("Baru", row, 8);
        recalculating = false;
    }

    private void recalculateRow(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        double tugas = parseScore(tableModel.getValueAt(row, 3));
        double uts = parseScore(tableModel.getValueAt(row, 4));
        double uas = parseScore(tableModel.getValueAt(row, 5));
        double akhir = calculateFinal(tugas, uts, uas);
        recalculating = true;
        tableModel.setValueAt(formatScore(akhir), row, 6);
        tableModel.setValueAt(calculateGrade(akhir), row, 7);
        tableModel.setValueAt("Diubah", row, 8);
        recalculating = false;
    }

    private double calculateFinal(double tugas, double uts, double uas) {
        return Math.round(((tugas * (bobotTugas / 100.0)) + (uts * (bobotUts / 100.0)) + (uas * (bobotUas / 100.0))) * 100.0) / 100.0;
    }

    private void applyBobot(JsonObject bobot) {
        if (bobot != null) {
            bobotTugas = getDouble(bobot, "bobot_tugas", 30.0);
            bobotUts = getDouble(bobot, "bobot_uts", 30.0);
            bobotUas = getDouble(bobot, "bobot_uas", 40.0);
        }
        if (lblBobotBadge != null) {
            lblBobotBadge.setText(bobotText());
        }
    }

    private String bobotText() {
        return "  " + formulaText() + "  ";
    }

    private String formulaText() {
        return "Tugas " + formatPercent(bobotTugas) + "%  •  UTS " + formatPercent(bobotUts) + "%  •  UAS " + formatPercent(bobotUas) + "%";
    }

    private String formatPercent(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String calculateGrade(double value) {
        if (value >= 85) return "A";
        if (value >= 70) return "B";
        if (value >= 60) return "C";
        if (value >= 50) return "D";
        return "E";
    }

    private double parseScore(Object value) {
        try {
            double score = Double.parseDouble(String.valueOf(value).replace(",", "."));
            if (score < 0) return 0;
            if (score > 100) return 100;
            return score;
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatScore(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private void showState(String title, String message) {
        skeleton.stop();
        statePanel.showState("!", title, message, "Muat ulang", this::loadMataKuliah);
        rootCard.show(rootPanel, "state");
    }

    private JButton buildButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT);
        btn.setBackground(bg);
        btn.setBorder(new EmptyBorder(9, 14, 9, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel smallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(MUTED);
        return label;
    }

    private void styleCombo(JComboBox<?> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 36));
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    private void styleTextField(JTextField field, int width) {
        field.setPreferredSize(new Dimension(width, 36));
        field.setBackground(CARD_BG);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 10, 0, 10)
        ));
        field.setToolTipText("Cari NIM atau nama");
    }

    private void styleTable(JTable t) {
        t.setBackground(TABLE_BG);
        t.setForeground(TEXT);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(42);
        t.setGridColor(BORDER);
        t.setSelectionBackground(new Color(37, 99, 235, 80));
        t.setSelectionForeground(TEXT);
        t.setShowVerticalLines(false);

        JTableHeader header = t.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    private static boolean hasValue(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull();
    }

    private static String getString(JsonObject object, String key) {
        return hasValue(object, key) ? object.get(key).getAsString() : "";
    }

    private static int getInt(JsonObject object, String key) {
        return hasValue(object, key) ? object.get(key).getAsInt() : 0;
    }

    private static double getDouble(JsonObject object, String key) {
        return hasValue(object, key) ? object.get(key).getAsDouble() : 0;
    }

    private static double getDouble(JsonObject object, String key, double defaultValue) {
        return hasValue(object, key) ? object.get(key).getAsDouble() : defaultValue;
    }

    private static class MataKuliahItem {
        final String kodeMk;
        final String namaMk;
        final int semester;

        MataKuliahItem(String kodeMk, String namaMk, int semester) {
            this.kodeMk = kodeMk;
            this.namaMk = namaMk;
            this.semester = semester;
        }

        @Override public String toString() {
            return kodeMk + " - " + namaMk + " (Smt " + semester + ")";
        }
    }

    private class GradeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String grade = String.valueOf(v);
            label.setForeground("A".equals(grade) || "B".equals(grade) ? GREEN : ("C".equals(grade) ? AMBER : new Color(239, 68, 68)));
            return label;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground("Diubah".equals(String.valueOf(v)) ? AMBER : MUTED);
            return label;
        }
    }

    private class ActionRenderer extends JPanel implements TableCellRenderer {
        ActionRenderer() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 6));
            add(actionChip("Edit", BLUE));
            add(actionChip("Hapus", new Color(185, 28, 28)));
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setBackground(sel ? t.getSelectionBackground() : (r % 2 == 0 ? TABLE_BG : ROW_ALT));
            return this;
        }
    }

    private class ActionEditor extends DefaultCellEditor {
        private int row;

        ActionEditor() {
            super(new JCheckBox());
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            row = r;
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            panel.setOpaque(true);
            panel.setBackground(t.getSelectionBackground());
            JButton edit = actionChip("Edit", BLUE);
            JButton delete = actionChip("Hapus", new Color(185, 28, 28));
            edit.addActionListener(e -> {
                fireEditingStopped();
                showNilaiDialog(row);
            });
            delete.addActionListener(e -> {
                fireEditingStopped();
                deleteNilai(row);
            });
            panel.add(edit);
            panel.add(delete);
            return panel;
        }

        @Override public Object getCellEditorValue() { return "aksi"; }
    }

    private JButton actionChip(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setForeground(TEXT);
        btn.setBackground(color);
        btn.setBorder(new EmptyBorder(5, 8, 5, 8));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
