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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public class RekapAbsensiPanel extends JPanel {

    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();

    private JComboBox<MataKuliahItem> cmbMataKuliah;
    private JComboBox<String> cmbTahunAjaran;
    private JComboBox<String> cmbJurusan;
    private JTextField txtTanggalMulai;
    private JTextField txtTanggalSelesai;
    private JTextField txtSearch;
    private JTable table;
    private JScrollPane tableScroll;
    private DefaultTableModel tableModel;
    private JLabel lblInfo;
    private JLabel lblScopeSummary;
    private JLabel lblTotalSummary;
    private JLabel lblAverageSummary;
    private JLabel lblRiskSummary;
    private JLabel lblCountSummary;

    private static final Color BG = new Color(13, 19, 38);
    private static final Color CARD_BG = new Color(18, 26, 48);
    private static final Color TABLE_BG = new Color(15, 22, 42);
    private static final Color HEADER_BG = new Color(10, 15, 30);
    private static final Color BORDER = new Color(25, 36, 65);
    private static final Color ROW_ALT = new Color(20, 29, 52);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color BLUE = new Color(59, 130, 246);

    public RekapAbsensiPanel() {
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
            showState("Akses terbatas", "Rekap absensi hanya tersedia untuk admin.");
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
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(26, 28, 18, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Rekap Absensi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Akademik / Nilai & Absensi / Rekap Absensi");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        JPanel filterCard = new JPanel(new BorderLayout(0, 14)) {
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
        filterCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel filterHeader = new JPanel(new BorderLayout());
        filterHeader.setOpaque(false);
        JPanel filterText = new JPanel();
        filterText.setOpaque(false);
        filterText.setLayout(new BoxLayout(filterText, BoxLayout.Y_AXIS));
        JLabel filterTitle = new JLabel("Filter Rekap");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        filterTitle.setForeground(TEXT);
        JLabel filterNote = new JLabel("Tentukan tahun ajaran, jurusan, mata kuliah, dan rentang tanggal absensi.");
        filterNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        filterNote.setForeground(MUTED);
        filterText.add(filterTitle);
        filterText.add(Box.createVerticalStrut(3));
        filterText.add(filterNote);
        filterHeader.add(filterText, BorderLayout.WEST);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 12, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        cmbTahunAjaran = new JComboBox<>();
        styleCombo(cmbTahunAjaran, 170);
        cmbJurusan = new JComboBox<>();
        cmbJurusan.addItem("Semua Jurusan");
        styleCombo(cmbJurusan, 220);
        cmbMataKuliah = new JComboBox<>();
        styleCombo(cmbMataKuliah, 320);
        txtTanggalMulai = new JTextField();
        styleTextField(txtTanggalMulai, 150);
        txtTanggalMulai.setEditable(false);
        txtTanggalMulai.setToolTipText("Pilih tanggal mulai dari kalender");
        txtTanggalSelesai = new JTextField();
        styleTextField(txtTanggalSelesai, 150);
        txtTanggalSelesai.setEditable(false);
        txtTanggalSelesai.setToolTipText("Pilih tanggal selesai dari kalender");
        txtSearch = new JTextField();
        styleTextField(txtSearch, 260);

        g.gridy = 0;
        g.gridx = 0; g.weightx = 0.18;
        fields.add(labeledField("Tahun Ajaran", cmbTahunAjaran), g);
        g.gridx = 1; g.weightx = 0.24;
        fields.add(labeledField("Jurusan", cmbJurusan), g);
        g.gridx = 2; g.weightx = 0.58; g.gridwidth = 2;
        fields.add(labeledField("Mata Kuliah", cmbMataKuliah), g);
        g.gridwidth = 1;
        g.gridy = 1;
        g.gridx = 0; g.weightx = 0.18;
        fields.add(labeledField("Tanggal Mulai", datePickerField(txtTanggalMulai)), g);
        g.gridx = 1; g.weightx = 0.18;
        fields.add(labeledField("Tanggal Selesai", datePickerField(txtTanggalSelesai)), g);
        g.gridx = 2; g.weightx = 0.34;
        fields.add(labeledField("Cari", txtSearch), g);

        JButton btnLoad = buildButton("Tampilkan", BLUE);
        JButton btnRefresh = buildButton("Refresh", CARD_BG);
        btnLoad.addActionListener(e -> loadRekap());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            txtTanggalMulai.setText("");
            txtTanggalSelesai.setText("");
            loadAcademicSettings();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 20));
        actions.setOpaque(false);
        actions.add(btnLoad);
        actions.add(btnRefresh);
        g.gridx = 3; g.weightx = 0.30;
        fields.add(actions, g);

        filterCard.add(filterHeader, BorderLayout.NORTH);
        filterCard.add(fields, BorderLayout.CENTER);

        wrapper.add(titleBlock, BorderLayout.NORTH);
        wrapper.add(filterCard, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel datePickerField(JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(field.getPreferredSize().width + 74, 38));
        panel.add(field, BorderLayout.CENTER);

        JButton pick = smallIconButton("...");
        pick.setToolTipText("Pilih tanggal dari kalender");
        pick.addActionListener(e -> showDatePicker(field));

        JButton clear = smallIconButton("x");
        clear.setToolTipText("Kosongkan tanggal");
        clear.addActionListener(e -> field.setText(""));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 3, 0));
        buttons.setOpaque(false);
        buttons.add(pick);
        buttons.add(clear);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private JButton smallIconButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(32, 38));
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(TEXT);
        button.setBackground(CARD_BG);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 6, 6, 6)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showDatePicker(JTextField target) {
        int[] years = selectedAcademicYears();
        LocalDate initial = initialCalendarDate(target, years);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Pilih Tanggal", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(CARD_BG);

        JComboBox<String> monthCombo = new JComboBox<>(new String[]{
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        });
        JComboBox<Integer> yearCombo = new JComboBox<>();
        for (int year = years[0]; year <= years[1]; year++) {
            yearCombo.addItem(year);
        }
        monthCombo.setSelectedIndex(initial.getMonthValue() - 1);
        yearCombo.setSelectedItem(initial.getYear());
        styleCombo(monthCombo, 150);
        styleCombo(yearCombo, 90);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        header.setOpaque(false);
        header.add(monthCombo);
        header.add(yearCombo);

        JPanel days = new JPanel(new GridLayout(0, 7, 4, 4));
        days.setOpaque(false);

        Runnable[] renderCalendar = new Runnable[1];
        renderCalendar[0] = () -> {
            days.removeAll();
            String[] labels = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
            for (String label : labels) {
                JLabel dayLabel = new JLabel(label, SwingConstants.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                dayLabel.setForeground(MUTED);
                days.add(dayLabel);
            }

            int year = (Integer) yearCombo.getSelectedItem();
            int month = monthCombo.getSelectedIndex() + 1;
            YearMonth yearMonth = YearMonth.of(year, month);
            int firstOffset = yearMonth.atDay(1).getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            for (int i = 0; i < firstOffset; i++) {
                days.add(new JLabel(""));
            }

            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                JButton dayButton = smallIconButton(String.valueOf(day));
                dayButton.setPreferredSize(new Dimension(42, 34));
                if (date.equals(LocalDate.now())) {
                    dayButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BLUE),
                            new EmptyBorder(6, 6, 6, 6)
                    ));
                }
                dayButton.addActionListener(e -> {
                    target.setText(date.toString());
                    dialog.dispose();
                });
                days.add(dayButton);
            }
            days.revalidate();
            days.repaint();
        };

        monthCombo.addActionListener(e -> renderCalendar[0].run());
        yearCombo.addActionListener(e -> renderCalendar[0].run());
        renderCalendar[0].run();

        root.add(header, BorderLayout.NORTH);
        root.add(days, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(370, 320));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JComponent buildTableCard() {
        String[] columns = {"NIM", "Nama", "Jurusan", "Kode MK", "Mata Kuliah", "Pertemuan", "Hadir", "Izin", "Sakit", "Alpha", "% Hadir", "Status"};
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
        table.getColumnModel().getColumn(10).setCellRenderer(new PercentRenderer());
        table.getColumnModel().getColumn(11).setCellRenderer(new StatusRenderer());

        JScrollPane scroll = new JScrollPane(table);
        tableScroll = scroll;
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE_BG);
        scroll.setBackground(TABLE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        scroll.setPreferredSize(new Dimension(0, 380));
        scroll.setMinimumSize(new Dimension(0, 330));

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
        JLabel tableTitle = new JLabel("Rekap Kehadiran Berdasarkan KRS");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT);
        lblScopeSummary = new JLabel("Pilih filter untuk menampilkan rekap absensi.");
        lblScopeSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblScopeSummary.setForeground(MUTED);
        headerText.add(tableTitle);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(lblScopeSummary);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stats.setOpaque(false);
        lblTotalSummary = metricLabel("0 Data");
        lblAverageSummary = metricLabel("Rata-rata 0%");
        lblRiskSummary = metricLabel("0 Bermasalah");
        stats.add(lblTotalSummary);
        stats.add(lblAverageSummary);
        stats.add(lblRiskSummary);

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
        lblInfo = new JLabel("Rekap absensi dihitung dari data Input Kehadiran.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(MUTED);
        lblCountSummary = metricLabel("H:0 I:0 S:0 A:0");
        footer.add(lblInfo, BorderLayout.WEST);
        footer.add(lblCountSummary, BorderLayout.EAST);
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
            if ("draft".equalsIgnoreCase(getString(tahun, "status"))) {
                continue;
            }
            String label = getString(tahun, "tahun_ajaran");
            if (!label.isBlank()) {
                cmbTahunAjaran.addItem(label);
                if ("aktif".equals(getString(tahun, "status"))) active = label;
            }
        }
        if (active != null) cmbTahunAjaran.setSelectedItem(active);
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
                            if (jurusan != null && !jurusan.isBlank()) cmbJurusan.addItem(jurusan);
                        }
                    }
                    if (previous != null) cmbJurusan.setSelectedItem(previous);
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
                        cmbMataKuliah.addItem(new MataKuliahItem(getString(mk, "kode_mk"), getString(mk, "nama_mk"), getInt(mk, "semester")));
                    }
                    rootCard.show(rootPanel, "content");
                    loadRekap();
                } catch (Exception ex) {
                    showState("Gagal memuat mata kuliah", ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadRekap() {
        loadRekap(true);
    }

    private void loadRekap(boolean allowFallback) {
        if (cmbTahunAjaran.getSelectedItem() == null) {
            showState("Tahun ajaran kosong", "Aktifkan atau tambahkan tahun ajaran di Pengaturan Akademik.");
            return;
        }
        if (!validDateOrBlank(txtTanggalMulai.getText()) || !validDateOrBlank(txtTanggalSelesai.getText())) {
            JOptionPane.showMessageDialog(this, "Format tanggal harus YYYY-MM-DD atau kosong.", "Tanggal tidak valid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        String kodeMk = selected == null ? "" : selected.kodeMk;
        String tahunAjaran = String.valueOf(cmbTahunAjaran.getSelectedItem());
        String tanggalMulai = txtTanggalMulai.getText().trim();
        String tanggalSelesai = txtTanggalSelesai.getText().trim();
        String search = txtSearch.getText().trim();
        String jurusan = selectedJurusan();

        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            private String resolvedKodeMk = "";
            private String resolvedJurusan = "";

            @Override protected JsonObject doInBackground() throws Exception {
                JsonObject response = AkademikService.getKehadiranRekap(
                        tahunAjaran,
                        kodeMk,
                        tanggalMulai,
                        tanggalSelesai,
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
                            response = AkademikService.getKehadiranRekap(
                                    tahunAjaran,
                                    resolution.kodeMk(),
                                    tanggalMulai,
                                    tanggalSelesai,
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
                        showState("Gagal memuat rekap absensi", response.get("message").getAsString());
                        return;
                    }
                    if (!resolvedKodeMk.isBlank()) {
                        selectMataKuliahByKode(resolvedKodeMk);
                    }
                    if (!resolvedJurusan.isBlank()) {
                        selectJurusanByName(resolvedJurusan);
                    }
                    fillRows(response.getAsJsonArray("data"), response.getAsJsonObject("summary"));
                    rootCard.show(rootPanel, "content");
                } catch (Exception ex) {
                    showState("Gagal memuat rekap absensi", ex.getMessage());
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

    private void fillRows(JsonArray data, JsonObject summary) {
        tableModel.setRowCount(0);
        for (JsonElement element : data) {
            JsonObject row = element.getAsJsonObject();
            tableModel.addRow(new Object[]{
                    getString(row, "nim"),
                    getString(row, "nama"),
                    getString(row, "jurusan"),
                    getString(row, "kode_mk"),
                    getString(row, "nama_mk"),
                    getInt(row, "total_pertemuan"),
                    getInt(row, "hadir"),
                    getInt(row, "izin"),
                    getInt(row, "sakit"),
                    getInt(row, "alpha"),
                    formatPercent(getDouble(row, "persentase_hadir")),
                    getString(row, "status_absensi")
            });
        }
        String tahunAjaran = String.valueOf(cmbTahunAjaran.getSelectedItem());
        String jurusan = selectedJurusan().isBlank() ? "Semua Jurusan" : selectedJurusan();
        MataKuliahItem selected = (MataKuliahItem) cmbMataKuliah.getSelectedItem();
        String mk = selected == null || selected.kodeMk.isBlank() ? "Semua Mata Kuliah" : selected.kodeMk + " - " + selected.namaMk;
        String periode = (txtTanggalMulai.getText().isBlank() && txtTanggalSelesai.getText().isBlank())
                ? "Semua Tanggal"
                : (txtTanggalMulai.getText().isBlank() ? "..." : txtTanggalMulai.getText().trim()) + " s/d " + (txtTanggalSelesai.getText().isBlank() ? "..." : txtTanggalSelesai.getText().trim());
        lblScopeSummary.setText(tahunAjaran + " | " + jurusan + " | " + mk + " | " + periode);

        lblTotalSummary.setText("  " + getInt(summary, "total_records") + " Data  ");
        lblAverageSummary.setText("  Rata-rata " + formatPercent(getDouble(summary, "rata_rata_persentase")) + "  ");
        lblRiskSummary.setText("  " + getInt(summary, "bermasalah") + " Bermasalah  ");
        lblCountSummary.setText("  H:" + getInt(summary, "total_hadir")
                + " I:" + getInt(summary, "total_izin")
                + " S:" + getInt(summary, "total_sakit")
                + " A:" + getInt(summary, "total_alpha") + "  ");
        lblInfo.setText(data.size() == 0
                ? "Belum ada KRS yang cocok dengan filter."
                : "Kolom Status menampilkan status absensi pada tanggal/periode filter.");
    }

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 8, 7, 8)));
        return label;
    }

    private JButton buildButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(118, 38));
        button.setMinimumSize(new Dimension(108, 38));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bg.equals(CARD_BG) ? BORDER : bg.darker()), new EmptyBorder(8, 14, 8, 14)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleCombo(JComboBox<?> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 38));
        combo.setMinimumSize(new Dimension(Math.min(width, 160), 38));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT);
    }

    private void styleTextField(JTextField field, int width) {
        field.setPreferredSize(new Dimension(width, 38));
        field.setMinimumSize(new Dimension(Math.min(width, 160), 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setBackground(CARD_BG);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
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
        for (int i = 5; i <= 10; i++) target.getColumnModel().getColumn(i).setMaxWidth(i == 10 ? 85 : 72);
        target.getColumnModel().getColumn(11).setMinWidth(115);
    }

    private String selectedJurusan() {
        if (cmbJurusan == null || cmbJurusan.getSelectedItem() == null) return "";
        String value = String.valueOf(cmbJurusan.getSelectedItem()).trim();
        return "Semua Jurusan".equals(value) ? "" : value;
    }

    private boolean validDateOrBlank(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isBlank()) return true;
        try {
            LocalDate.parse(text);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private int[] selectedAcademicYears() {
        String tahunAjaran = cmbTahunAjaran == null || cmbTahunAjaran.getSelectedItem() == null
                ? ""
                : String.valueOf(cmbTahunAjaran.getSelectedItem());
        String[] parts = tahunAjaran.split("/");
        int start = parseYear(parts.length > 0 ? parts[0] : "", LocalDate.now().getYear());
        int end = parseYear(parts.length > 1 ? parts[1] : "", start);
        if (end < start) end = start;
        return new int[]{start, end};
    }

    private int parseYear(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private LocalDate initialCalendarDate(JTextField target, int[] years) {
        String currentValue = target.getText() == null ? "" : target.getText().trim();
        if (!currentValue.isBlank()) {
            try {
                LocalDate selected = LocalDate.parse(currentValue);
                if (selected.getYear() >= years[0] && selected.getYear() <= years[1]) {
                    return selected;
                }
            } catch (DateTimeParseException ignored) {
            }
        }

        LocalDate today = LocalDate.now();
        if (today.getYear() >= years[0] && today.getYear() <= years[1]) {
            return today;
        }
        return LocalDate.of(years[0], 8, 1);
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

    private String formatPercent(double value) {
        if (Math.abs(value - Math.round(value)) < 0.001) return ((int) Math.round(value)) + "%";
        return String.format("%.2f%%", value);
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

    private static class PercentRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(new Color(191, 219, 254));
            return label;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(value);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if ("hadir".equals(status)) label.setForeground(new Color(134, 239, 172));
            else if ("izin".equals(status)) label.setForeground(new Color(147, 197, 253));
            else if ("sakit".equals(status)) label.setForeground(new Color(253, 224, 71));
            else if ("alpha".equals(status)) label.setForeground(new Color(252, 165, 165));
            else if ("Campuran".equals(status)) label.setForeground(new Color(216, 180, 254));
            else label.setForeground(new Color(148, 163, 184));
            return label;
        }
    }
}
