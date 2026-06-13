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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class InputKehadiranPanel extends JPanel {

    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();

    private JComboBox<MataKuliahItem> cmbMataKuliah;
    private JComboBox<String> cmbTahunAjaran;
    private JComboBox<String> cmbJurusan;
    private JTextField txtTanggal;
    private JComboBox<Integer> cmbPertemuan;
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblInfo;
    private JLabel lblCourseSummary;
    private JLabel lblCountSummary;
    private JLabel lblSavedSummary;
    private JLabel lblDistributionSummary;
    private JButton btnSave;
    private boolean loadingRows = false;
    private int jumlahPertemuan = 12;

    private static final Color BG = new Color(13, 19, 38);
    private static final Color CARD_BG = new Color(18, 26, 48);
    private static final Color TABLE_BG = new Color(15, 22, 42);
    private static final Color HEADER_BG = new Color(10, 15, 30);
    private static final Color BORDER = new Color(25, 36, 65);
    private static final Color ROW_ALT = new Color(20, 29, 52);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color GREEN = new Color(34, 197, 94);

    public InputKehadiranPanel() {
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
            showState("Akses terbatas", "Input kehadiran hanya tersedia untuk admin.");
        }
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(BG);
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(buildTableCard(), BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 18));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(26, 28, 6, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Input Kehadiran Mahasiswa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Akademik / Nilai & Absensi / Input Kehadiran");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        JPanel filterCard = new JPanel(new BorderLayout(0, 16)) {
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
        filterCard.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 14, 18);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        cmbTahunAjaran = new JComboBox<>();
        styleCombo(cmbTahunAjaran, 180);

        cmbJurusan = new JComboBox<>();
        cmbJurusan.addItem("Semua Jurusan");
        styleCombo(cmbJurusan, 220);

        cmbMataKuliah = new JComboBox<>();
        styleCombo(cmbMataKuliah, 380);

        txtTanggal = new JTextField(LocalDate.now().toString());
        styleTextField(txtTanggal, 170);

        cmbPertemuan = new JComboBox<>();
        styleCombo(cmbPertemuan, 150);
        populatePertemuanOptions(12);

        txtSearch = new JTextField();
        styleTextField(txtSearch, 270);
        txtSearch.setToolTipText("Opsional: cari mahasiswa tertentu berdasarkan NIM atau nama");
        txtSearch.getAccessibleContext().setAccessibleName("Cari nama atau NIM mahasiswa");
        txtSearch.getAccessibleContext().setAccessibleDescription("Field opsional untuk menyaring mahasiswa tertentu. Kosongkan untuk menampilkan semua mahasiswa sesuai filter.");

        g.gridy = 0;
        g.gridx = 0; g.weightx = 0.18;
        fields.add(labeledField("Tahun Ajaran", cmbTahunAjaran), g);
        g.gridx = 1; g.weightx = 0.22;
        fields.add(labeledField("Jurusan", cmbJurusan), g);
        g.gridx = 2; g.weightx = 0.60;
        g.insets = new Insets(0, 0, 14, 0);
        fields.add(labeledField("Mata Kuliah", cmbMataKuliah), g);
        g.gridy = 1;
        g.gridx = 0; g.weightx = 0.22;
        g.insets = new Insets(0, 0, 0, 18);
        fields.add(labeledField("Tanggal", txtTanggal), g);
        g.gridx = 1; g.weightx = 0.18;
        fields.add(labeledField("Pertemuan", cmbPertemuan), g);
        g.gridx = 2; g.weightx = 0.60;
        g.insets = new Insets(0, 0, 0, 0);
        fields.add(labeledField("Cari Nama/NIM", txtSearch), g);

        JButton btnLoad = buildButton("Tampilkan", BLUE);
        JButton btnRefresh = buildButton("Refresh", CARD_BG);
        btnLoad.addActionListener(e -> loadInputList());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadAcademicSettings();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(btnLoad);
        actions.add(btnRefresh);

        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        JLabel hint = new JLabel("Kosongkan pencarian untuk menampilkan semua mahasiswa sesuai filter.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        actionRow.add(hint, BorderLayout.WEST);
        actionRow.add(actions, BorderLayout.EAST);

        filterCard.add(fields, BorderLayout.CENTER);
        filterCard.add(actionRow, BorderLayout.SOUTH);

        wrapper.add(titleBlock, BorderLayout.NORTH);
        wrapper.add(filterCard, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildTableCard() {
        String[] columns = {"NIM", "Nama", "Jurusan", "Status", "Keterangan", "Tersimpan"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 3 || col == 4;
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

        JComboBox<String> statusEditor = new JComboBox<>(new String[]{"hadir", "izin", "sakit", "alpha"});
        statusEditor.setBackground(CARD_BG);
        statusEditor.setForeground(TEXT);
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusEditor));
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new SavedRenderer());
        tableModel.addTableModelListener(e -> {
            if (loadingRows || e.getFirstRow() < 0) return;
            if (e.getColumn() == 3 || e.getColumn() == 4) {
                tableModel.setValueAt("Diubah", e.getFirstRow(), 5);
                updateSummaries();
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
        JLabel tableTitle = new JLabel("Daftar Kehadiran");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT);
        lblCourseSummary = new JLabel("Pilih mata kuliah dan tanggal untuk mulai mengisi kehadiran.");
        lblCourseSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCourseSummary.setForeground(MUTED);
        headerText.add(tableTitle);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(lblCourseSummary);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stats.setOpaque(false);
        lblCountSummary = metricLabel("0 Mahasiswa");
        lblSavedSummary = metricLabel("0 Tersimpan");
        lblDistributionSummary = metricLabel("H:0 I:0 S:0 A:0");
        stats.add(lblCountSummary);
        stats.add(lblSavedSummary);
        stats.add(lblDistributionSummary);

        tableHeader.add(headerText, BorderLayout.WEST);
        tableHeader.add(stats, BorderLayout.EAST);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(2, 28, 18, 28));

        lblInfo = new JLabel("Pilih mata kuliah untuk memuat mahasiswa dari KRS.");
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
                    jumlahPertemuan = getInt(response.getAsJsonObject("data"), "jumlah_pertemuan");
                    if (jumlahPertemuan <= 0) {
                        jumlahPertemuan = 12;
                    }
                    populatePertemuanOptions(jumlahPertemuan);
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
                }
            }
        }.execute();
    }

    private void loadMataKuliah() {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return NilaiService.getMataKuliah("", "");
            }

            @Override protected void done() {
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
        String tanggal = txtTanggal.getText().trim();
        if (!isValidDate(tanggal)) {
            JOptionPane.showMessageDialog(this, "Format tanggal harus YYYY-MM-DD.", "Tanggal tidak valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getKehadiranInputList(
                        selected.kodeMk,
                        (String) cmbTahunAjaran.getSelectedItem(),
                        tanggal,
                        txtSearch.getText().trim(),
                        selectedJurusan()
                );
            }

            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        showState("Gagal memuat input kehadiran", response.get("message").getAsString());
                        return;
                    }
                    fillRows(response.getAsJsonArray("data"), response.getAsJsonObject("mata_kuliah"));
                    rootCard.show(rootPanel, "content");
                } catch (Exception ex) {
                    showState("Gagal memuat input kehadiran", ex.getMessage());
                }
            }
        }.execute();
    }

    private void fillRows(JsonArray data, JsonObject mataKuliah) {
        loadingRows = true;
        tableModel.setRowCount(0);
        for (JsonElement element : data) {
            JsonObject row = element.getAsJsonObject();
            boolean alreadySaved = hasValue(row, "id_kehadiran");
            tableModel.addRow(new Object[]{
                    getString(row, "nim"),
                    getString(row, "nama"),
                    getString(row, "jurusan"),
                    getString(row, "status_kehadiran").isBlank() ? "hadir" : getString(row, "status_kehadiran"),
                    getString(row, "keterangan"),
                    alreadySaved ? "Tersimpan" : "Baru"
            });
        }
        loadingRows = false;

        btnSave.setEnabled(data.size() > 0);
        String kode = getString(mataKuliah, "kode_mk");
        String nama = getString(mataKuliah, "nama_mk");
        String semester = getString(mataKuliah, "semester");
        String jurusan = selectedJurusan();
        String jurusanInfo = jurusan.isBlank() ? "Semua Jurusan" : jurusan;
        lblCourseSummary.setText(kode + " - " + nama + " | Semester " + semester + " | " + cmbTahunAjaran.getSelectedItem() + " | " + txtTanggal.getText().trim() + " | " + jurusanInfo);
        lblCountSummary.setText("  " + data.size() + " Mahasiswa  ");
        updateSummaries();
        lblInfo.setText(data.size() == 0
                ? "Belum ada mahasiswa yang mengambil mata kuliah ini di KRS."
                : "Ubah status kehadiran langsung di tabel, lalu simpan semua.");
    }

    private void saveAll() {
        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        if (selected == null || tableModel.getRowCount() == 0) return;
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        String tanggal = txtTanggal.getText().trim();
        if (!isValidDate(tanggal)) {
            JOptionPane.showMessageDialog(this, "Format tanggal harus YYYY-MM-DD.", "Tanggal tidak valid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer pertemuan = (Integer) cmbPertemuan.getSelectedItem();
        if (pertemuan == null || pertemuan <= 0) {
            JOptionPane.showMessageDialog(this, "Pertemuan wajib dipilih.", "Pertemuan tidak valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JsonArray items = new JsonArray();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            JsonObject item = new JsonObject();
            item.addProperty("nim", String.valueOf(tableModel.getValueAt(i, 0)));
            item.addProperty("status", String.valueOf(tableModel.getValueAt(i, 3)));
            item.addProperty("keterangan", cleanCellText(tableModel.getValueAt(i, 4)));
            items.add(item);
        }

        JsonObject body = new JsonObject();
        body.addProperty("kode_mk", selected.kodeMk);
        body.addProperty("tahun_ajaran", (String) cmbTahunAjaran.getSelectedItem());
        body.addProperty("tanggal", tanggal);
        body.addProperty("pertemuan", pertemuan);
        body.add("items", items);

        btnSave.setEnabled(false);
        lblInfo.setText("Menyimpan kehadiran...");
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.bulkSaveKehadiran(body);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    boolean success = response.get("success").getAsBoolean();
                    JOptionPane.showMessageDialog(InputKehadiranPanel.this,
                            getString(response, "message"),
                            success ? "Berhasil" : "Gagal",
                            success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    if (success) {
                        loadInputList();
                    } else {
                        btnSave.setEnabled(true);
                        lblInfo.setText("Simpan kehadiran gagal.");
                    }
                } catch (Exception ex) {
                    btnSave.setEnabled(true);
                    lblInfo.setText("Gagal menyimpan kehadiran.");
                    JOptionPane.showMessageDialog(InputKehadiranPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(MUTED);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
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

    private JButton buildButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(bg);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.equals(CARD_BG) ? BORDER : bg.darker()),
                new EmptyBorder(10, 18, 10, 18)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleCombo(JComboBox<?> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 42));
        combo.setMinimumSize(new Dimension(Math.min(width, 160), 42));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT);
    }

    private void styleTextField(JTextField field, int width) {
        field.setPreferredSize(new Dimension(width, 42));
        field.setMinimumSize(new Dimension(Math.min(width, 160), 42));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setBackground(CARD_BG);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));
    }

    private void styleTable(JTable target) {
        target.setRowHeight(42);
        target.setShowVerticalLines(false);
        target.setShowHorizontalLines(true);
        target.setGridColor(BORDER);
        target.setBackground(TABLE_BG);
        target.setForeground(TEXT);
        target.setSelectionBackground(new Color(59, 130, 246, 70));
        target.setSelectionForeground(TEXT);
        target.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTableHeader header = target.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setForeground(TEXT);
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        target.setDefaultRenderer(Object.class, renderer);

        target.getColumnModel().getColumn(0).setMaxWidth(105);
        target.getColumnModel().getColumn(3).setMaxWidth(105);
        target.getColumnModel().getColumn(5).setMaxWidth(110);
    }

    private String selectedJurusan() {
        if (cmbJurusan == null || cmbJurusan.getSelectedItem() == null) {
            return "";
        }
        String value = String.valueOf(cmbJurusan.getSelectedItem()).trim();
        return "Semua Jurusan".equals(value) ? "" : value;
    }

    private void updateSummaries() {
        if (tableModel == null) return;
        int saved = 0;
        int changed = 0;
        int fresh = 0;
        int hadir = 0;
        int izin = 0;
        int sakit = 0;
        int alpha = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String statusSave = String.valueOf(tableModel.getValueAt(i, 5));
            if ("Tersimpan".equals(statusSave)) saved++;
            else if ("Diubah".equals(statusSave)) changed++;
            else fresh++;

            String status = String.valueOf(tableModel.getValueAt(i, 3));
            if ("hadir".equals(status)) hadir++;
            else if ("izin".equals(status)) izin++;
            else if ("sakit".equals(status)) sakit++;
            else if ("alpha".equals(status)) alpha++;
        }
        lblSavedSummary.setText("  " + saved + " Tersimpan | " + changed + " Diubah | " + fresh + " Baru  ");
        lblDistributionSummary.setText("  H:" + hadir + " I:" + izin + " S:" + sakit + " A:" + alpha + "  ");
    }

    private void populatePertemuanOptions(int total) {
        if (cmbPertemuan == null) return;
        int previous = cmbPertemuan.getSelectedItem() instanceof Integer
                ? (Integer) cmbPertemuan.getSelectedItem()
                : 1;
        cmbPertemuan.removeAllItems();
        int safeTotal = Math.max(1, total);
        for (int i = 1; i <= safeTotal; i++) {
            cmbPertemuan.addItem(i);
        }
        cmbPertemuan.setSelectedItem(Math.min(previous, safeTotal));
    }

    private boolean isValidDate(String value) {
        if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private String cleanCellText(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).trim();
        return "null".equalsIgnoreCase(text) ? "" : text;
    }

    private void showState(String title, String message) {
        statePanel.showState("!", title, message, "Muat Ulang", this::loadAcademicSettings);
        rootCard.show(rootPanel, "state");
    }

    private String getString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) return "";
        return object.get(key).getAsString();
    }

    private int getInt(JsonObject object, String key) {
        try {
            if (object == null || !object.has(key) || object.get(key).isJsonNull()) return 0;
            return object.get(key).getAsInt();
        } catch (Exception ex) {
            return 0;
        }
    }

    private boolean hasValue(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull();
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

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(value);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(Color.WHITE);
            if (!isSelected) {
                if ("hadir".equals(status)) label.setBackground(new Color(22, 101, 52));
                else if ("izin".equals(status)) label.setBackground(new Color(37, 99, 235));
                else if ("sakit".equals(status)) label.setBackground(new Color(202, 138, 4));
                else label.setBackground(new Color(185, 28, 28));
            }
            return label;
        }
    }

    private static class SavedRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground("Tersimpan".equals(String.valueOf(value)) ? new Color(134, 239, 172) : new Color(253, 224, 71));
            return label;
        }
    }
}
