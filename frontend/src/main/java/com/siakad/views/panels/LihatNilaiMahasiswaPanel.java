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

public class LihatNilaiMahasiswaPanel extends JPanel {

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
    private JLabel lblScopeSummary;
    private JLabel lblTotalSummary;
    private JLabel lblInputSummary;
    private JLabel lblAverageSummary;
    private JLabel lblGradeSummary;
    private boolean loadingFilters = false;

    private static final Color BG = new Color(13, 19, 38);
    private static final Color CARD_BG = new Color(18, 26, 48);
    private static final Color TABLE_BG = new Color(15, 22, 42);
    private static final Color HEADER_BG = new Color(10, 15, 30);
    private static final Color BORDER = new Color(25, 36, 65);
    private static final Color ROW_ALT = new Color(20, 29, 52);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color BLUE = new Color(59, 130, 246);

    public LihatNilaiMahasiswaPanel() {
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
            showState("Akses terbatas", "Rekap nilai mahasiswa hanya tersedia untuk admin.");
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

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Lihat Nilai Mahasiswa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Akademik / Nilai & Absensi / Rekap Nilai");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

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
        styleCombo(cmbJurusan, 210);

        cmbMataKuliah = new JComboBox<>();
        styleCombo(cmbMataKuliah, 285);

        txtSearch = new JTextField();
        styleTextField(txtSearch, 210);
        txtSearch.setToolTipText("Opsional: cari data spesifik berdasarkan NIM, nama, kode MK, atau nama mata kuliah");
        txtSearch.getAccessibleContext().setAccessibleName("Cari nilai mahasiswa spesifik");
        txtSearch.getAccessibleContext().setAccessibleDescription("Field opsional. Kosongkan untuk menampilkan semua data sesuai tahun ajaran, jurusan, dan mata kuliah yang dipilih.");
        cmbTahunAjaran.addActionListener(e -> loadRekapFromFilterChange());
        cmbJurusan.addActionListener(e -> loadRekapFromFilterChange());
        cmbMataKuliah.addActionListener(e -> loadRekapFromFilterChange());

        g.gridx = 0; g.weightx = 0;
        fields.add(labeledField("Tahun Ajaran", cmbTahunAjaran), g);
        g.gridx = 1;
        fields.add(labeledField("Jurusan", cmbJurusan), g);
        g.gridx = 2; g.weightx = 1;
        fields.add(labeledField("Mata Kuliah", cmbMataKuliah), g);
        g.gridx = 3; g.weightx = 0;
        fields.add(labeledField("Cari Spesifik", txtSearch), g);

        JButton btnLoad = buildButton("Tampilkan", BLUE);
        JButton btnRefresh = buildButton("Refresh", CARD_BG);
        btnLoad.addActionListener(e -> loadRekap());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadAcademicSettings();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        actions.setOpaque(false);
        actions.add(btnLoad);
        actions.add(btnRefresh);

        filterCard.add(fields, BorderLayout.CENTER);
        filterCard.add(actions, BorderLayout.EAST);

        wrapper.add(titleBlock, BorderLayout.NORTH);
        wrapper.add(filterCard, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildTableCard() {
        String[] columns = {"NIM", "Nama", "Jurusan", "Kode MK", "Mata Kuliah", "SKS", "Smt", "Tugas", "UTS", "UAS", "Akhir", "Grade", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
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
        table.getColumnModel().getColumn(11).setCellRenderer(new GradeRenderer());
        table.getColumnModel().getColumn(12).setCellRenderer(new StatusRenderer());

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
        JLabel tableTitle = new JLabel("Rekap Nilai Berdasarkan KRS");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT);
        lblScopeSummary = new JLabel("Pilih filter untuk menampilkan nilai mahasiswa.");
        lblScopeSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblScopeSummary.setForeground(MUTED);
        headerText.add(tableTitle);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(lblScopeSummary);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stats.setOpaque(false);
        lblTotalSummary = metricLabel("0 Data");
        lblInputSummary = metricLabel("0 Diinput");
        lblAverageSummary = metricLabel("Rata-rata 0");
        stats.add(lblTotalSummary);
        stats.add(lblInputSummary);
        stats.add(lblAverageSummary);

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

        lblInfo = new JLabel("Data nilai ditampilkan dari KRS dan tabel nilai.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(MUTED);

        lblGradeSummary = metricLabel("Grade A:0 B:0 C:0 D:0 E:0");

        footer.add(lblInfo, BorderLayout.WEST);
        footer.add(lblGradeSummary, BorderLayout.EAST);
        return footer;
    }

    private void loadAcademicSettings() {
        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        loadingFilters = true;
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
                        for (JsonElement item : response.getAsJsonArray("data")) {
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
                    cmbMataKuliah.addItem(new MataKuliahItem("", "Semua Mata Kuliah", 0));
                    for (JsonElement item : response.getAsJsonArray("data")) {
                        JsonObject mk = item.getAsJsonObject();
                        cmbMataKuliah.addItem(new MataKuliahItem(
                                getString(mk, "kode_mk"),
                                getString(mk, "nama_mk"),
                                getInt(mk, "semester")
                        ));
                    }

                    rootCard.show(rootPanel, "content");
                    loadingFilters = false;
                    loadRekap();
                } catch (Exception ex) {
                    loadingFilters = false;
                    showState("Gagal memuat mata kuliah", ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadRekapFromFilterChange() {
        if (loadingFilters || cmbTahunAjaran == null || cmbMataKuliah == null) {
            return;
        }
        if (cmbTahunAjaran.getSelectedItem() == null || cmbMataKuliah.getSelectedItem() == null) {
            return;
        }
        loadRekap();
    }

    private void loadRekap() {
        if (cmbTahunAjaran.getSelectedItem() == null) {
            showState("Tahun ajaran kosong", "Aktifkan atau tambahkan tahun ajaran di Pengaturan Akademik.");
            return;
        }

        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        String kodeMk = selected == null ? "" : selected.kodeMk;

        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return NilaiService.getRekap(
                        (String) cmbTahunAjaran.getSelectedItem(),
                        kodeMk,
                        txtSearch.getText().trim(),
                        selectedJurusan()
                );
            }

            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) {
                        showState("Gagal memuat rekap nilai", response.get("message").getAsString());
                        return;
                    }
                    fillRows(response.getAsJsonArray("data"), response.getAsJsonObject("summary"));
                    rootCard.show(rootPanel, "content");
                } catch (Exception ex) {
                    showState("Gagal memuat rekap nilai", ex.getMessage());
                }
            }
        }.execute();
    }

    private void fillRows(JsonArray data, JsonObject summary) {
        tableModel.setRowCount(0);
        for (JsonElement element : data) {
            JsonObject row = element.getAsJsonObject();
            boolean saved = hasValue(row, "id_nilai");
            tableModel.addRow(new Object[]{
                    getString(row, "nim"),
                    getString(row, "nama"),
                    getString(row, "jurusan"),
                    getString(row, "kode_mk"),
                    getString(row, "nama_mk"),
                    getString(row, "sks"),
                    getString(row, "semester"),
                    saved ? formatScore(getDouble(row, "nilai_tugas")) : "-",
                    saved ? formatScore(getDouble(row, "nilai_uts")) : "-",
                    saved ? formatScore(getDouble(row, "nilai_uas")) : "-",
                    saved ? formatScore(getDouble(row, "nilai_akhir")) : "-",
                    saved ? getString(row, "grade") : "-",
                    getString(row, "status_nilai")
            });
        }

        String tahunAjaran = String.valueOf(cmbTahunAjaran.getSelectedItem());
        String jurusan = selectedJurusan().isBlank() ? "Semua Jurusan" : selectedJurusan();
        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        String mk = selected == null || selected.kodeMk.isBlank() ? "Semua Mata Kuliah" : selected.kodeMk + " - " + selected.namaMk;
        lblScopeSummary.setText(tahunAjaran + " | " + jurusan + " | " + mk);

        int total = getInt(summary, "total_records");
        int sudah = getInt(summary, "sudah_diinput");
        int belum = getInt(summary, "belum_diinput");
        double rata = getDouble(summary, "rata_rata");
        lblTotalSummary.setText("  " + total + " Data  ");
        lblInputSummary.setText("  " + sudah + " Diinput | " + belum + " Belum  ");
        lblAverageSummary.setText("  Rata-rata " + formatScore(rata) + "  ");
        lblGradeSummary.setText("  Grade A:" + getInt(summary, "grade_a")
                + " B:" + getInt(summary, "grade_b")
                + " C:" + getInt(summary, "grade_c")
                + " D:" + getInt(summary, "grade_d")
                + " E:" + getInt(summary, "grade_e") + "  ");
        lblInfo.setText(total == 0
                ? "Belum ada data KRS yang cocok dengan filter."
                : "Rekap ini bersifat baca-only. Perubahan nilai dilakukan lewat menu Input Nilai.");
    }

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(MUTED);
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

    private JButton buildButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(bg);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.equals(CARD_BG) ? BORDER : bg.darker()),
                new EmptyBorder(8, 14, 8, 14)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleCombo(JComboBox<?> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 38));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT);
    }

    private void styleTextField(JTextField field, int width) {
        field.setPreferredSize(new Dimension(width, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setBackground(CARD_BG);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
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

        target.getColumnModel().getColumn(0).setMaxWidth(95);
        target.getColumnModel().getColumn(3).setMaxWidth(85);
        target.getColumnModel().getColumn(5).setMaxWidth(55);
        target.getColumnModel().getColumn(6).setMaxWidth(55);
        target.getColumnModel().getColumn(7).setMaxWidth(75);
        target.getColumnModel().getColumn(8).setMaxWidth(75);
        target.getColumnModel().getColumn(9).setMaxWidth(75);
        target.getColumnModel().getColumn(10).setMaxWidth(80);
        target.getColumnModel().getColumn(11).setMaxWidth(70);
        target.getColumnModel().getColumn(12).setMinWidth(115);
    }

    private String selectedJurusan() {
        if (cmbJurusan == null || cmbJurusan.getSelectedItem() == null) {
            return "";
        }
        String value = String.valueOf(cmbJurusan.getSelectedItem()).trim();
        return "Semua Jurusan".equals(value) ? "" : value;
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

    private double getDouble(JsonObject object, String key) {
        try {
            if (object == null || !object.has(key) || object.get(key).isJsonNull()) return 0;
            return object.get(key).getAsDouble();
        } catch (Exception ex) {
            return 0;
        }
    }

    private boolean hasValue(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull();
    }

    private String formatScore(double value) {
        if (Math.abs(value - Math.round(value)) < 0.001) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format("%.2f", value);
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
            if (kodeMk.isBlank()) return namaMk;
            return kodeMk + " - " + namaMk + " (Smt " + semester + ")";
        }
    }

    private static class GradeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground("-".equals(String.valueOf(value)) ? new Color(148, 163, 184) : new Color(191, 219, 254));
            return label;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(value);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground("Sudah Diinput".equals(status) ? new Color(134, 239, 172) : new Color(253, 224, 71));
            return label;
        }
    }
}
