package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AcademicSearchResolver;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class InputKehadiranPanel extends JPanel {

    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();

    private JComboBox<MataKuliahItem> cmbMataKuliah;
    private JComboBox<String> cmbTahunAjaran;
    private JComboBox<String> cmbJurusan;
    private JComboBox<Integer> cmbPertemuan;
    private JTextField txtSearch;
    private JTable table;
    private JScrollPane tableScroll;
    private DefaultTableModel tableModel;
    private JLabel lblInfo;
    private JLabel lblCourseSummary;
    private JLabel lblCountSummary;
    private JLabel lblSavedSummary;
    private JLabel lblDistributionSummary;
    private JButton btnSave;
    private JButton btnEdit;
    private boolean loadingRows = false;
    private boolean editingSavedRows = false;
    private boolean initialLoadDone = false;
    private int jumlahPertemuan = 12;
    private final Map<String, String> tanggalMulaiByTahunAjaran = new HashMap<>();
    private final Map<String, Integer> jumlahPertemuanByJurusan = new HashMap<>();

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
        JPanel content = buildContent();
        JScrollPane pageScroll = AcademicUi.pageScroll(content);
        AcademicUi.relayWheelToParentScroll(tableScroll, pageScroll);
        rootPanel.add(pageScroll, "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);

        if (JwtHelper.getInstance().isAdmin()) {
            loadAcademicSettings();
        } else {
            showState("Akses terbatas", "Input kehadiran hanya tersedia untuk admin.");
        }
    }

    public void onPanelShown() {
        if (JwtHelper.getInstance().isAdmin() && initialLoadDone) {
            loadAcademicSettings();
        }
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.add(AcademicUi.centeredWidth(buildHeader(), 980));
        content.add(Box.createVerticalStrut(14));
        content.add(AcademicUi.centeredWidth(buildTableCard(), 980));
        content.add(Box.createVerticalStrut(14));
        content.add(AcademicUi.centeredWidth(buildFooter(), 980));
        return content;
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 18));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(22, 28, 4, 28));

        JPanel titleBlock = AcademicUi.pageHeader(
                "Input Kehadiran Mahasiswa",
                "Akademik / Nilai & Absensi / Input Kehadiran",
                "Pertemuan otomatis",
                GREEN
        );

        JPanel filterCard = AcademicUi.cardPanel(GREEN);
        filterCard.setLayout(new BorderLayout(0, 16));
        filterCard.setBorder(new EmptyBorder(16, 18, 16, 18));

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
        cmbMataKuliah.addActionListener(e -> updatePertemuanOptionsForSelectedJurusan());

        cmbPertemuan = new JComboBox<>();
        styleCombo(cmbPertemuan, 150);
        populatePertemuanOptions(12);
        cmbJurusan.addActionListener(e -> updatePertemuanOptionsForSelectedJurusan());

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
        fields.add(labeledField("Pertemuan", cmbPertemuan), g);
        g.gridx = 1; g.weightx = 0.60;
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

        filterCard.add(AcademicUi.sectionIntro(
                "Filter Kehadiran",
                "Pilih periode, jurusan, mata kuliah, dan pertemuan untuk membuat daftar absensi."
        ), BorderLayout.NORTH);
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
                if (col != 3 && col != 4) return false;
                String saveStatus = String.valueOf(getValueAt(row, 5));
                return editingSavedRows || !"Tersimpan".equals(saveStatus);
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

        StatusCellEditor statusCellEditor = new StatusCellEditor();
        table.getColumnModel().getColumn(3).setCellEditor(statusCellEditor);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(3).setHeaderValue("Status ▾");
        table.getColumnModel().getColumn(5).setCellRenderer(new SavedRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col != 3 || !table.isCellEditable(row, col)) {
                    return;
                }
                table.editCellAt(row, col, e);
                Component editor = table.getEditorComponent();
                if (editor instanceof JComboBox<?> combo) {
                    SwingUtilities.invokeLater(combo::showPopup);
                }
            }
        });
        tableModel.addTableModelListener(e -> {
            if (loadingRows || e.getFirstRow() < 0) return;
            if (e.getColumn() == 3 || e.getColumn() == 4) {
                tableModel.setValueAt("Diubah", e.getFirstRow(), 5);
                updateSummaries();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        tableScroll = scroll;
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE_BG);
        scroll.setBackground(TABLE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        scroll.setPreferredSize(new Dimension(0, 360));
        scroll.setMinimumSize(new Dimension(0, 320));

        JPanel card = AcademicUi.cardPanel(GREEN);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 18, 0, 18));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(12, 14, 10, 14));

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel tableTitle = new JLabel("Daftar Kehadiran");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(TEXT);
        lblCourseSummary = new JLabel("Pilih mata kuliah dan pertemuan untuk mulai mengisi kehadiran.");
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
        btnEdit = buildButton("Edit", BLUE);
        btnSave = buildButton("Simpan", GREEN);
        btnEdit.setVisible(false);
        btnSave.setVisible(false);
        btnReset.addActionListener(e -> loadInputList());
        btnEdit.addActionListener(e -> {
            editingSavedRows = true;
            btnEdit.setVisible(false);
            lblInfo.setText("Silakan ubah status atau keterangan mahasiswa, lalu tekan Simpan.");
            if (table.getRowCount() > 0) {
                table.requestFocusInWindow();
                table.setRowSelectionInterval(0, 0);
            }
        });
        btnSave.addActionListener(e -> saveAll());
        actions.add(btnReset);
        actions.add(btnEdit);
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
                    populatePertemuanJurusan(response.getAsJsonObject("data").getAsJsonArray("jumlah_pertemuan_jurusan"));
                    populatePertemuanOptions(jumlahPertemuan);
                    populateTahunAjaran(response.getAsJsonObject("data").getAsJsonArray("tahun_ajaran"));
                    loadJurusanList();
                    loadMataKuliah();
                    initialLoadDone = true;
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
        tanggalMulaiByTahunAjaran.clear();
        String active = null;
        for (JsonElement item : data) {
            JsonObject tahun = item.getAsJsonObject();
            if ("draft".equalsIgnoreCase(getString(tahun, "status"))) {
                continue;
            }
            String label = getString(tahun, "tahun_ajaran");
            if (!label.isBlank()) {
                cmbTahunAjaran.addItem(label);
                tanggalMulaiByTahunAjaran.put(label, getString(tahun, "tanggal_mulai"));
                if ("aktif".equals(getString(tahun, "status"))) {
                    active = label;
                }
            }
        }
        if (active != null) {
            cmbTahunAjaran.setSelectedItem(active);
        }
    }

    private void populatePertemuanJurusan(JsonArray data) {
        jumlahPertemuanByJurusan.clear();
        if (data == null) return;
        for (JsonElement item : data) {
            JsonObject row = item.getAsJsonObject();
            String jurusan = getString(row, "jurusan");
            int total = getInt(row, "jumlah_pertemuan");
            if (!jurusan.isBlank() && total > 0) {
                jumlahPertemuanByJurusan.put(jurusan, total);
            }
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
                    updatePertemuanOptionsForSelectedJurusan();
                } catch (Exception ex) {
                    cmbJurusan.removeAllItems();
                    cmbJurusan.addItem("Semua Jurusan");
                    updatePertemuanOptionsForSelectedJurusan();
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
                                getInt(mk, "semester"),
                                getString(mk, "jurusan")
                        ));
                    }

                    rootCard.show(rootPanel, "content");
                    if (cmbMataKuliah.getItemCount() > 0) {
                        loadInputList();
                    } else {
                        tableModel.setRowCount(0);
                        updateSummaries();
                        lblInfo.setText("Belum ada data mata kuliah.");
                    }
                } catch (Exception ex) {
                    showState("Gagal memuat mata kuliah", ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadInputList() {
        loadInputList(true);
    }

    private void loadInputList(boolean allowFallback) {
        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        if (selected == null) {
            showState("Mata kuliah kosong", "Tambahkan data mata kuliah terlebih dahulu.");
            return;
        }
        if (cmbTahunAjaran.getSelectedItem() == null) {
            showState("Tahun ajaran kosong", "Aktifkan atau tambahkan tahun ajaran di Pengaturan Akademik.");
            return;
        }
        final String tanggal = computedAttendanceDate();
        if (tanggal.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Tanggal mulai tahun ajaran belum diatur. Lengkapi di Pengaturan Akademik.",
                    "Tanggal otomatis belum tersedia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String kodeMk = selected.kodeMk;
        final String tahunAjaran = String.valueOf(cmbTahunAjaran.getSelectedItem());
        final String search = txtSearch.getText().trim();
        final String jurusan = selectedJurusan();

        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            private String resolvedKodeMk = "";
            private String resolvedJurusan = "";

            @Override protected JsonObject doInBackground() throws Exception {
                JsonObject response = AkademikService.getKehadiranInputList(
                        kodeMk,
                        tahunAjaran,
                        tanggal,
                        search,
                        jurusan
                );

                if (allowFallback && search != null && !search.isBlank()) {
                    JsonArray data = response.has("data") && response.get("data").isJsonArray()
                            ? response.getAsJsonArray("data")
                            : null;
                    if (data != null && data.size() == 0) {
                        AcademicSearchResolver.Resolution resolution = AcademicSearchResolver.resolveSingleStudentCourse(search, tahunAjaran);
                        if (resolution != null && !resolution.kodeMk().isBlank() && !resolution.kodeMk().equals(kodeMk)) {
                            resolvedKodeMk = resolution.kodeMk();
                            resolvedJurusan = resolution.jurusan();
                            response = AkademikService.getKehadiranInputList(
                                    resolution.kodeMk(),
                                    tahunAjaran,
                                    tanggal,
                                    search,
                                    resolution.jurusan()
                            );
                        }
                    }
                }

                return response;
            }

            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        showState("Gagal memuat input kehadiran", response.get("message").getAsString());
                        return;
                    }
                    if (!resolvedKodeMk.isBlank()) {
                        selectMataKuliahByKode(resolvedKodeMk);
                    }
                    if (!resolvedJurusan.isBlank()) {
                        selectJurusanByName(resolvedJurusan);
                    }
                    fillRows(response.getAsJsonArray("data"), response.getAsJsonObject("mata_kuliah"));
                    rootCard.show(rootPanel, "content");
                } catch (Exception ex) {
                    showState("Gagal memuat input kehadiran", ex.getMessage());
                }
            }
        }.execute();
    }

    private void selectMataKuliahByKode(String kodeMk) {
        if (cmbMataKuliah == null || kodeMk == null || kodeMk.isBlank()) {
            return;
        }
        for (int i = 0; i < cmbMataKuliah.getItemCount(); i++) {
            MataKuliahItem item = cmbMataKuliah.getItemAt(i);
            if (item != null && kodeMk.equals(item.kodeMk)) {
                cmbMataKuliah.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectJurusanByName(String jurusan) {
        if (cmbJurusan == null || jurusan == null || jurusan.isBlank()) {
            return;
        }
        for (int i = 0; i < cmbJurusan.getItemCount(); i++) {
            String item = cmbJurusan.getItemAt(i);
            if (jurusan.equalsIgnoreCase(String.valueOf(item).trim())) {
                cmbJurusan.setSelectedIndex(i);
                return;
            }
        }
        cmbJurusan.addItem(jurusan);
        cmbJurusan.setSelectedItem(jurusan);
    }

    private void fillRows(JsonArray data, JsonObject mataKuliah) {
        loadingRows = true;
        editingSavedRows = false;
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

        String kode = getString(mataKuliah, "kode_mk");
        String nama = getString(mataKuliah, "nama_mk");
        String semester = getString(mataKuliah, "semester");
        String jurusan = selectedJurusan();
        String jurusanInfo = jurusan.isBlank() ? "Semua Jurusan" : jurusan;
        lblCourseSummary.setText(kode + " - " + nama + " | Semester " + semester + " | " + cmbTahunAjaran.getSelectedItem() + " | Pertemuan " + cmbPertemuan.getSelectedItem() + " | " + jurusanInfo);
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
        String tanggal = computedAttendanceDate();
        if (tanggal.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Tanggal mulai tahun ajaran belum diatur. Lengkapi di Pengaturan Akademik.",
                    "Tanggal otomatis belum tersedia",
                    JOptionPane.WARNING_MESSAGE);
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

        btnSave.setVisible(false);
        if (btnEdit != null) btnEdit.setVisible(false);
        editingSavedRows = false;
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
                        btnSave.setVisible(true);
                        lblInfo.setText("Simpan kehadiran gagal.");
                    }
                } catch (Exception ex) {
                    btnSave.setVisible(true);
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
        return AcademicUi.metric(text);
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
        target.setRowHeight(46);
        target.setShowVerticalLines(false);
        target.setShowHorizontalLines(true);
        target.setGridColor(BORDER);
        target.setBackground(TABLE_BG);
        target.setForeground(TEXT);
        target.setSelectionBackground(new Color(59, 130, 246, 70));
        target.setSelectionForeground(TEXT);
        target.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        target.setFillsViewportHeight(true);

        JTableHeader header = target.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setForeground(TEXT);
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        target.setDefaultRenderer(Object.class, renderer);

        target.getColumnModel().getColumn(0).setPreferredWidth(110);
        target.getColumnModel().getColumn(1).setPreferredWidth(210);
        target.getColumnModel().getColumn(2).setPreferredWidth(160);
        target.getColumnModel().getColumn(3).setPreferredWidth(130);
        target.getColumnModel().getColumn(4).setPreferredWidth(270);
        target.getColumnModel().getColumn(5).setPreferredWidth(110);
        target.getColumnModel().getColumn(0).setMaxWidth(125);
        target.getColumnModel().getColumn(3).setMaxWidth(145);
        target.getColumnModel().getColumn(5).setMaxWidth(120);
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
        updateActionButtons(saved, changed);
    }

    private void updateActionButtons(int saved, int changed) {
        if (btnSave != null) {
            btnSave.setVisible(changed > 0);
        }
        if (btnEdit != null) {
            btnEdit.setVisible(changed == 0 && saved > 0);
        }
        revalidate();
        repaint();
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

    private void updatePertemuanOptionsForSelectedJurusan() {
        String jurusan = selectedJurusanForPertemuan();
        int total = jurusan.isBlank()
                ? jumlahPertemuan
                : jumlahPertemuanByJurusan.getOrDefault(jurusan, jumlahPertemuan);
        populatePertemuanOptions(total);
    }

    private String selectedJurusanForPertemuan() {
        String jurusan = selectedJurusan();
        if (!jurusan.isBlank()) {
            return jurusan;
        }
        Object selected = cmbMataKuliah == null ? null : cmbMataKuliah.getSelectedItem();
        if (selected instanceof MataKuliahItem item && item.jurusan != null) {
            return item.jurusan.trim();
        }
        return "";
    }

    private String computedAttendanceDate() {
        Object tahun = cmbTahunAjaran.getSelectedItem();
        Object pertemuan = cmbPertemuan.getSelectedItem();
        if (tahun == null || !(pertemuan instanceof Integer)) {
            return "";
        }
        String startDate = tanggalMulaiByTahunAjaran.getOrDefault(String.valueOf(tahun), "");
        if (startDate == null || startDate.length() < 10) {
            return "";
        }
        try {
            LocalDate start = LocalDate.parse(startDate.substring(0, 10));
            return start.plusWeeks(((Integer) pertemuan) - 1L).toString();
        } catch (Exception ex) {
            return "";
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
        final String jurusan;

        MataKuliahItem(String kodeMk, String namaMk, int semester, String jurusan) {
            this.kodeMk = kodeMk;
            this.namaMk = namaMk;
            this.semester = semester;
            this.jurusan = jurusan;
        }

        @Override public String toString() {
            return kodeMk + " - " + namaMk + " (Smt " + semester + ")";
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(value);
            label.setText(status + "  ▾");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(Color.WHITE);
            label.setToolTipText("Klik untuk memilih status kehadiran");
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1),
                    new EmptyBorder(0, 8, 0, 8)
            ));
            if (!isSelected) {
                if ("hadir".equals(status)) label.setBackground(new Color(22, 101, 52));
                else if ("izin".equals(status)) label.setBackground(new Color(37, 99, 235));
                else if ("sakit".equals(status)) label.setBackground(new Color(202, 138, 4));
                else label.setBackground(new Color(185, 28, 28));
            }
            return label;
        }
    }

    private class StatusCellEditor extends DefaultCellEditor {
        private final JComboBox<String> combo;

        StatusCellEditor() {
            super(new JComboBox<>(new String[]{"hadir", "izin", "sakit", "alpha"}));
            combo = (JComboBox<String>) getComponent();
            combo.setBackground(CARD_BG);
            combo.setForeground(TEXT);
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            combo.setBorder(BorderFactory.createLineBorder(BORDER));
            setClickCountToStart(1);
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            combo.setSelectedItem(value == null ? "hadir" : String.valueOf(value));
            SwingUtilities.invokeLater(combo::showPopup);
            return component;
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
