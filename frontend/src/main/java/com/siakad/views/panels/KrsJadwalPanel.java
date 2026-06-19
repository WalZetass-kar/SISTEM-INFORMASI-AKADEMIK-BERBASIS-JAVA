package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;
import com.siakad.services.MahasiswaService;
import com.siakad.utils.AppTheme;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * KrsJadwalPanel - Modul KRS & Jadwal Kuliah
 */
public class KrsJadwalPanel extends JPanel {
    public enum PageMode {
        INPUT_KRS,
        MATA_KULIAH,
        JADWAL_KULIAH,
        CETAK_KRS
    }

    private JTable tableKrs;
    private JTable tableMatakuliah;
    private JTable tableJadwal;
    private DefaultTableModel krsTableModel;
    private DefaultTableModel matakuliahTableModel;
    private DefaultTableModel jadwalTableModel;

    private JTextField txtNimFilter;
    private JTextField txtTahunAjaranFilter;
    private JTextField txtInputNimFilter;
    private JTextField txtInputTahunAjaranFilter;
    private JTextField txtCetakNimFilter;
    private JTextField txtCetakTahunAjaranFilter;
    private JComboBox<MatakuliahOption> cmbKrsMatakuliah;
    private JPanel printPreviewHost;
    private JScrollPane printPreviewScroll;

    private JLabel lblKrsStat;
    private JLabel lblSksStat;
    private JLabel lblJadwalStat;
    private JLabel lblKrsInfo;
    private JLabel lblInputKrsInfo;
    private JLabel lblMatakuliahInfo;
    private JLabel lblJadwalInfo;

    private JsonArray matakuliahCache = new JsonArray();
    private JsonArray jadwalCache = new JsonArray();
    private JsonArray mahasiswaCache = new JsonArray();
    private JsonArray currentKrsCache = new JsonArray();
    private JsonObject currentKrsSummary = null;

    private int activeLoads = 0;
    private final PageMode mode;

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

    public KrsJadwalPanel() {
        this(PageMode.INPUT_KRS);
    }

    public KrsJadwalPanel(PageMode mode) {
        this.mode = mode;
        setBackground(BG());
        setLayout(new BorderLayout());
        initUI();
        refreshAllData();
    }

    private void initUI() {
        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        if (mode == PageMode.INPUT_KRS) {
            body.add(buildSummarySection());
            body.add(Box.createVerticalStrut(16));
        }
        body.add(buildPageContent());

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(pageTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY());

        JLabel lblSub = new JLabel(pageSubtitle());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED());

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnRefresh = buildBtn("Refresh", BLUE(), 110);
        btnRefresh.addActionListener(e -> refreshAllData());
        actions.add(btnRefresh);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSummarySection() {
        JPanel wrap = new JPanel(new GridLayout(1, 3, 14, 0));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 28, 0, 28));

        lblKrsStat = new JLabel("0");
        lblSksStat = new JLabel("0 SKS");
        lblJadwalStat = new JLabel("0");

        wrap.add(buildStatCard("KRS Aktif", "Jumlah baris KRS yang sedang ditampilkan", lblKrsStat, BLUE()));
        wrap.add(buildStatCard("Total SKS", "Akumulasi SKS dari hasil filter KRS aktif", lblSksStat, GREEN()));
        wrap.add(buildStatCard("Jadwal Kuliah", "Jumlah jadwal kuliah yang tersedia", lblJadwalStat, YELLOW()));
        return wrap;
    }

    private JPanel buildStatCard(String title, String desc, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                GradientPaint gradient = new GradientPaint(0, 0, accent, getWidth(), 0,
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(0, 120));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_PRIMARY());
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><body style='width:220px'>" + desc + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(TEXT_MUTED());
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY());
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(lblDesc);
        return card;
    }

    private JComponent buildPageContent() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 28, 18, 28));
        switch (mode) {
            case MATA_KULIAH -> wrap.add(buildMatakuliahTab(), BorderLayout.CENTER);
            case JADWAL_KULIAH -> wrap.add(buildJadwalTab(), BorderLayout.CENTER);
            case CETAK_KRS -> wrap.add(buildCetakKrsPage(), BorderLayout.CENTER);
            case INPUT_KRS -> wrap.add(buildKrsWorkspace(), BorderLayout.CENTER);
        }
        return wrap;
    }

    private String pageTitle() {
        return switch (mode) {
            case INPUT_KRS -> "KRS Mahasiswa";
            case MATA_KULIAH -> "Mata Kuliah";
            case JADWAL_KULIAH -> "Jadwal Kuliah";
            case CETAK_KRS -> "Cetak KRS";
        };
    }

    private String pageSubtitle() {
        return switch (mode) {
            case INPUT_KRS -> "Input, lihat data, dan cetak KRS mahasiswa dari satu menu";
            case MATA_KULIAH -> "Kelola daftar mata kuliah yang dapat dipilih pada KRS";
            case JADWAL_KULIAH -> "Kelola jadwal kuliah berdasarkan mata kuliah";
            case CETAK_KRS -> "Cari KRS mahasiswa dan cetak dokumen resmi";
        };
    }

    private JComponent buildKrsWorkspace() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBackground(BG());
        tabs.setForeground(TEXT_PRIMARY());
        tabs.addTab("Input KRS", buildInputKrsTab());
        tabs.addTab("Data KRS", buildKrsTab());
        tabs.addTab("Cetak KRS", buildCetakKrsPage());
        return tabs;
    }

    private JComponent buildInputKrsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel topCard = buildCard();
        topCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        topCard.setLayout(new BorderLayout(12, 12));

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        txtInputNimFilter = makeField();
        txtInputTahunAjaranFilter = makeField();
        cmbKrsMatakuliah = new JComboBox<>();
        styleCombo(cmbKrsMatakuliah);

        if (JwtHelper.getInstance().isMahasiswa()) {
            txtInputNimFilter.setText(JwtHelper.getInstance().getNim());
            txtInputNimFilter.setEnabled(false);
        }

        g.gridx = 0;
        g.gridy = 0;
        filters.add(makeLabel("NIM"), g);
        g.gridx = 1;
        filters.add(txtInputNimFilter, g);
        g.gridx = 2;
        filters.add(makeLabel("Tahun Ajaran"), g);
        g.gridx = 3;
        filters.add(txtInputTahunAjaranFilter, g);
        g.gridx = 0;
        g.gridy = 1;
        filters.add(makeLabel("Pilih Mata Kuliah"), g);
        g.gridx = 1;
        g.gridwidth = 3;
        filters.add(cmbKrsMatakuliah, g);
        g.gridwidth = 1;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnInput = buildBtn("Input KRS", GREEN(), 120);
        btnInput.addActionListener(e -> submitInlineKrs());
        actions.add(btnInput);

        topCard.add(filters, BorderLayout.CENTER);
        topCard.add(actions, BorderLayout.EAST);

        JLabel info = makeInfoLabel("Pilih mahasiswa, tahun ajaran, dan mata kuliah untuk menambahkan KRS baru.");
        info.setBorder(new EmptyBorder(12, 4, 0, 4));

        JPanel inputContent = new JPanel();
        inputContent.setOpaque(false);
        inputContent.setLayout(new BoxLayout(inputContent, BoxLayout.Y_AXIS));
        inputContent.add(topCard);
        inputContent.add(info);

        lblInputKrsInfo = makeInfoLabel("Belum ada data KRS dimuat.");

        panel.add(inputContent, BorderLayout.NORTH);
        panel.add(buildKrsTableCard(buildKrsDataTable()), BorderLayout.CENTER);
        panel.add(buildKrsTableFooter(lblInputKrsInfo), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildKrsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel topCard = buildCard();
        topCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        topCard.setLayout(new BorderLayout(12, 12));

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        txtNimFilter = makeField();
        txtTahunAjaranFilter = makeField();

        if (JwtHelper.getInstance().isMahasiswa()) {
            txtNimFilter.setText(JwtHelper.getInstance().getNim());
            txtNimFilter.setEnabled(false);
        }

        int row = 0;
        g.gridx = 0;
        g.gridy = row;
        filters.add(makeLabel("NIM"), g);
        g.gridx = 1;
        filters.add(txtNimFilter, g);
        g.gridx = 2;
        filters.add(makeLabel("Tahun Ajaran"), g);
        g.gridx = 3;
        filters.add(txtTahunAjaranFilter, g);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnLoad = buildBtn("Muat KRS", BLUE(), 120);
        btnLoad.addActionListener(e -> loadKrs());

        actions.add(btnLoad);

        topCard.add(filters, BorderLayout.CENTER);
        topCard.add(actions, BorderLayout.EAST);

        JTable dataKrsTable = buildKrsDataTable();
        JPanel tableCard = buildKrsTableCard(dataKrsTable);
        lblKrsInfo = makeInfoLabel("Belum ada data KRS dimuat.");

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(topCard);
        content.add(Box.createVerticalStrut(14));
        content.add(tableCard);
        content.add(buildKrsTableFooter(lblKrsInfo));

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableModel getKrsTableModel() {
        if (krsTableModel == null) {
            String[] columns = {"ID", "NIM", "Nama", "Kode MK", "Mata Kuliah", "SKS", "Semester", "Tahun Ajaran", "Jadwal", "Ruangan", "Dosen"};
            krsTableModel = new DefaultTableModel(columns, 0) {
                @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
            };
        }
        return krsTableModel;
    }

    private JTable buildKrsDataTable() {
        JTable table = buildTable(getKrsTableModel());
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(5).setMaxWidth(55);
        table.getColumnModel().getColumn(6).setMaxWidth(70);
        if (tableKrs == null) {
            tableKrs = table;
        }
        return table;
    }

    private JPanel buildKrsTableCard(JTable table) {
        JPanel tableCard = buildCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableCard.setPreferredSize(new Dimension(0, 280));
        tableCard.add(new JScrollPane(table) {{
            setBorder(null);
            getViewport().setBackground(TABLE_BG());
        }}, BorderLayout.CENTER);
        return tableCard;
    }

    private JPanel buildKrsTableFooter(JLabel infoLabel) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 0, 4));
        footer.add(infoLabel, BorderLayout.WEST);
        return footer;
    }

    private JComponent buildMatakuliahTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel topCard = buildCard();
        topCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        topCard.setLayout(new BorderLayout());

        JLabel info = new JLabel("Daftar mata kuliah yang dapat dipilih pada proses input KRS.");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(TEXT_MUTED());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnRefresh = buildBtn("Refresh", BLUE(), 110);
        btnRefresh.addActionListener(e -> loadMatakuliah());
        actions.add(btnRefresh);
        if (JwtHelper.getInstance().isAdmin()) {
            JButton btnTambah = buildBtn("Tambah Mata Kuliah", GREEN(), 190);
            btnTambah.addActionListener(e -> showMatakuliahDialog());
            actions.add(btnTambah);
        }

        topCard.add(info, BorderLayout.WEST);
        topCard.add(actions, BorderLayout.EAST);

        String[] columns = JwtHelper.getInstance().isAdmin()
                ? new String[]{"Kode MK", "Nama Mata Kuliah", "SKS", "Semester", "Jurusan", "Edit", "Hapus"}
                : new String[]{"Kode MK", "Nama Mata Kuliah", "SKS", "Semester", "Jurusan"};
        matakuliahTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        };
        tableMatakuliah = buildTable(matakuliahTableModel);
        tableMatakuliah.getColumnModel().getColumn(2).setMaxWidth(70);
        tableMatakuliah.getColumnModel().getColumn(3).setMaxWidth(80);
        if (JwtHelper.getInstance().isAdmin()) {
            setupActionColumns(tableMatakuliah, 5, 6, this::editSelectedMatakuliah, this::deleteSelectedMatakuliah);
        }

        JPanel tableCard = buildCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tableMatakuliah) {{
            setBorder(null);
            getViewport().setBackground(TABLE_BG());
        }}, BorderLayout.CENTER);

        lblMatakuliahInfo = makeInfoLabel("Belum ada data mata kuliah.");

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 0, 4));
        footer.add(lblMatakuliahInfo, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(topCard);
        content.add(Box.createVerticalStrut(14));
        content.add(tableCard);
        content.add(footer);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildJadwalTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel topCard = buildCard();
        topCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        topCard.setLayout(new BorderLayout());

        JLabel info = new JLabel("Jadwal kuliah berdasarkan mata kuliah yang sudah terdaftar.");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(TEXT_MUTED());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnRefresh = buildBtn("Refresh", BLUE(), 110);
        btnRefresh.addActionListener(e -> loadJadwal());
        actions.add(btnRefresh);
        if (JwtHelper.getInstance().isAdmin()) {
            JButton btnTambah = buildBtn("Tambah Jadwal", GREEN(), 150);
            btnTambah.addActionListener(e -> showJadwalDialog());
            actions.add(btnTambah);
        }

        topCard.add(info, BorderLayout.WEST);
        topCard.add(actions, BorderLayout.EAST);

        String[] columns = JwtHelper.getInstance().isAdmin()
                ? new String[]{"ID", "Kode MK", "Mata Kuliah", "Hari", "Jam", "Ruangan", "Dosen", "Edit", "Hapus"}
                : new String[]{"ID", "Kode MK", "Mata Kuliah", "Hari", "Jam", "Ruangan", "Dosen"};
        jadwalTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        };
        tableJadwal = buildTable(jadwalTableModel);
        tableJadwal.getColumnModel().getColumn(0).setMaxWidth(50);
        if (JwtHelper.getInstance().isAdmin()) {
            setupActionColumns(tableJadwal, 7, 8, this::editSelectedJadwal, this::deleteSelectedJadwal);
        }

        JPanel tableCard = buildCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tableJadwal) {{
            setBorder(null);
            getViewport().setBackground(TABLE_BG());
        }}, BorderLayout.CENTER);

        lblJadwalInfo = makeInfoLabel("Belum ada data jadwal kuliah.");

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 0, 4));
        footer.add(lblJadwalInfo, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(topCard);
        content.add(Box.createVerticalStrut(14));
        content.add(tableCard);
        content.add(footer);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildCetakKrsPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel topCard = buildCard();
        topCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        topCard.setLayout(new BorderLayout(12, 12));

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        txtCetakNimFilter = makeField();
        txtCetakTahunAjaranFilter = makeField();
        if (JwtHelper.getInstance().isMahasiswa()) {
            txtCetakNimFilter.setText(JwtHelper.getInstance().getNim());
            txtCetakNimFilter.setEnabled(false);
        }

        g.gridx = 0;
        g.gridy = 0;
        filters.add(makeLabel("NIM"), g);
        g.gridx = 1;
        filters.add(txtCetakNimFilter, g);
        g.gridx = 2;
        filters.add(makeLabel("Tahun Ajaran"), g);
        g.gridx = 3;
        filters.add(txtCetakTahunAjaranFilter, g);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnLoad = buildBtn("Muat KRS", BLUE(), 120);
        btnLoad.addActionListener(e -> loadPrintKrs());
        JButton btnPdf = buildBtn("Export PDF", BLUE(), 120);
        btnPdf.addActionListener(e -> exportKrsPdf());
        JButton btnPrint = buildBtn("Cetak", GREEN(), 110);
        btnPrint.addActionListener(e -> printKrs());
        actions.add(btnLoad);
        actions.add(btnPdf);
        actions.add(btnPrint);

        topCard.add(filters, BorderLayout.CENTER);
        topCard.add(actions, BorderLayout.EAST);

        printPreviewHost = buildCard();
        printPreviewHost.setLayout(new BorderLayout());
        printPreviewHost.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel empty = makeInfoLabel("Masukkan NIM dan tahun ajaran, lalu klik Muat KRS untuk menampilkan preview.");
        empty.setHorizontalAlignment(SwingConstants.CENTER);
        printPreviewHost.add(empty, BorderLayout.CENTER);

        printPreviewScroll = new JScrollPane(printPreviewHost);
        printPreviewScroll.setBorder(null);
        printPreviewScroll.getViewport().setBackground(BG());
        printPreviewScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        printPreviewScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        printPreviewScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(topCard, BorderLayout.NORTH);
        content.add(printPreviewScroll, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void refreshAllData() {
        switch (mode) {
            case INPUT_KRS -> {
                loadMahasiswa();
                loadMatakuliah();
                loadJadwal();
                loadKrs();
            }
            case MATA_KULIAH -> loadMatakuliah();
            case JADWAL_KULIAH -> {
                loadMatakuliah();
                loadJadwal();
            }
            case CETAK_KRS -> loadMahasiswa();
        }
    }

    private void loadMahasiswa() {
        if (!JwtHelper.getInstance().isAdmin()) {
            return;
        }

        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getAll(1, 1000, "");
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean() && response.has("data")) {
                        mahasiswaCache = response.getAsJsonArray("data");
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception e) {
                    showErrorMessage("Gagal memuat mahasiswa: " + e.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private void loadMatakuliah() {
        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getMatakuliah(null, null);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        matakuliahCache = response.getAsJsonArray("data");
                        if (matakuliahTableModel != null) {
                            fillMatakuliahTable(matakuliahCache);
                        }
                        refreshInlineMatakuliahCombo();
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception e) {
                    showErrorMessage("Gagal memuat mata kuliah: " + e.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private void loadJadwal() {
        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getJadwal(null, null, null);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        jadwalCache = response.getAsJsonArray("data");
                        if (jadwalTableModel != null) {
                            fillJadwalTable(jadwalCache);
                        }
                        if (lblJadwalStat != null) {
                            lblJadwalStat.setText(String.valueOf(jadwalCache.size()));
                        }
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception e) {
                    showErrorMessage("Gagal memuat jadwal kuliah: " + e.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private void loadKrs() {
        if (txtNimFilter == null || txtTahunAjaranFilter == null) {
            return;
        }
        String nim = txtNimFilter.getText().trim();
        if (JwtHelper.getInstance().isMahasiswa()) {
            nim = JwtHelper.getInstance().getNim();
        }
        String tahunAjaran = txtTahunAjaranFilter.getText().trim();

        loadKrsData(nim, tahunAjaran, false);
    }

    private void loadPrintKrs() {
        if (txtCetakNimFilter == null || txtCetakTahunAjaranFilter == null) {
            return;
        }
        String nim = JwtHelper.getInstance().isMahasiswa()
                ? JwtHelper.getInstance().getNim()
                : txtCetakNimFilter.getText().trim();
        String tahunAjaran = txtCetakTahunAjaranFilter.getText().trim();

        loadKrsData(nim, tahunAjaran, true);
    }

    private void loadKrsData(String nim, String tahunAjaran, boolean updatePreview) {
        startBusy();

        final String nimFilter = nim == null || nim.isBlank() ? null : nim;
        final String tahunAjaranFilter = tahunAjaran == null || tahunAjaran.isBlank() ? null : tahunAjaran;

        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getKrs(nimFilter, tahunAjaranFilter, null);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        currentKrsCache = response.getAsJsonArray("data");
                        currentKrsSummary = response.has("summary") && response.get("summary").isJsonObject()
                                ? response.getAsJsonObject("summary")
                                : null;
                        if (krsTableModel != null) {
                            fillKrsTable(currentKrsCache);
                        }
                        if (updatePreview || mode == PageMode.CETAK_KRS) {
                            updatePrintPreview();
                        }
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception e) {
                    showErrorMessage("Gagal memuat data KRS: " + e.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private void fillMatakuliahTable(JsonArray data) {
        matakuliahTableModel.setRowCount(0);
        for (JsonElement element : data) {
            JsonObject mk = element.getAsJsonObject();
            List<Object> row = new ArrayList<>();
            row.add(safe(mk, "kode_mk"));
            row.add(safe(mk, "nama_mk"));
            row.add(safe(mk, "sks"));
            row.add(safe(mk, "semester"));
            row.add(safe(mk, "jurusan"));
            if (JwtHelper.getInstance().isAdmin()) {
                row.add("Edit");
                row.add("Hapus");
            }
            matakuliahTableModel.addRow(row.toArray(new Object[0]));
        }
        lblMatakuliahInfo.setText(data.size() + " mata kuliah tersedia");
    }

    private void fillJadwalTable(JsonArray data) {
        jadwalTableModel.setRowCount(0);
        for (JsonElement element : data) {
            JsonObject jadwal = element.getAsJsonObject();
            List<Object> row = new ArrayList<>();
            row.add(safe(jadwal, "id_jadwal"));
            row.add(safe(jadwal, "kode_mk"));
            row.add(safe(jadwal, "nama_mk"));
            row.add(capitalize(safe(jadwal, "hari")));
            row.add(safe(jadwal, "jam"));
            row.add(safe(jadwal, "ruangan"));
            row.add(safe(jadwal, "dosen"));
            if (JwtHelper.getInstance().isAdmin()) {
                row.add("Edit");
                row.add("Hapus");
            }
            jadwalTableModel.addRow(row.toArray(new Object[0]));
        }
        lblJadwalInfo.setText(data.size() + " jadwal kuliah tersedia");
        if (lblJadwalStat != null) {
            lblJadwalStat.setText(String.valueOf(data.size()));
        }
    }

    private void fillKrsTable(JsonArray data) {
        if (krsTableModel == null) {
            return;
        }

        krsTableModel.setRowCount(0);
        int totalSks = 0;
        for (JsonElement element : data) {
            JsonObject krs = element.getAsJsonObject();
            int sks = parseInteger(safe(krs, "sks"));
            totalSks += sks;
            krsTableModel.addRow(new Object[]{
                    safe(krs, "id_krs"),
                    safe(krs, "nim"),
                    safe(krs, "nama_mahasiswa"),
                    safe(krs, "kode_mk"),
                    safe(krs, "nama_mk"),
                    safe(krs, "sks"),
                    safe(krs, "semester"),
                    safe(krs, "tahun_ajaran"),
                    safe(krs, "jadwal"),
                    safe(krs, "ruangan"),
                    safe(krs, "dosen")
            });
        }

        String infoText = data.size() == 0
                ? "Belum ada data KRS untuk filter aktif."
                : data.size() + " baris KRS ditemukan";
        if (lblKrsInfo != null) {
            lblKrsInfo.setText(infoText);
        }
        if (lblInputKrsInfo != null) {
            lblInputKrsInfo.setText(infoText);
        }
        if (lblKrsStat != null) {
            lblKrsStat.setText(String.valueOf(data.size()));
        }
        if (lblSksStat != null) {
            lblSksStat.setText(totalSks + " SKS");
        }
    }

    private void showMatakuliahDialog() {
        showMatakuliahDialog(null);
    }

    private void showMatakuliahDialog(JsonObject existing) {
        boolean editMode = existing != null;
        JDialog dialog = createDialog(editMode ? "Edit Mata Kuliah" : "Tambah Mata Kuliah", 500, 430);

        JPanel form = buildDialogForm();
        GridBagConstraints g = createFormConstraints();

        JTextField fKode = makeField();
        JTextField fNama = makeField();
        JTextField fSks = makeField();
        JTextField fSemester = makeField();
        JTextField fJurusan = makeField();

        if (editMode) {
            fKode.setText(safe(existing, "kode_mk"));
            fKode.setEnabled(false);
            fNama.setText(safe(existing, "nama_mk"));
            fSks.setText(safe(existing, "sks"));
            fSemester.setText(safe(existing, "semester"));
            fJurusan.setText(emptyDash(safe(existing, "jurusan")));
        }

        int row = 0;
        addFormRow(form, g, row++, "Kode MK *", fKode);
        addFormRow(form, g, row++, "Nama MK *", fNama);
        addFormRow(form, g, row++, "SKS *", fSks);
        addFormRow(form, g, row++, "Semester *", fSemester);
        addFormRow(form, g, row++, "Jurusan *", fJurusan);

        JButton btnSave = buildBtn(editMode ? "Update Mata Kuliah" : "Simpan Mata Kuliah", BLUE(), 210);
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.insets = new Insets(18, 4, 4, 4);
        form.add(btnSave, g);

        btnSave.addActionListener(e -> {
            if (isBlank(fKode) || isBlank(fNama) || isBlank(fSks) || isBlank(fSemester) || isBlank(fJurusan)) {
                JOptionPane.showMessageDialog(dialog, "Semua field wajib harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int sks;
            int semester;
            try {
                sks = Integer.parseInt(fSks.getText().trim());
                semester = Integer.parseInt(fSemester.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "SKS dan semester harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("kode_mk", fKode.getText().trim());
            body.addProperty("nama_mk", fNama.getText().trim());
            body.addProperty("sks", sks);
            body.addProperty("semester", semester);
            body.addProperty("jurusan", fJurusan.getText().trim());

            submitDialog(dialog,
                    () -> editMode
                            ? AkademikService.updateMatakuliah(safe(existing, "kode_mk"), body)
                            : AkademikService.createMatakuliah(body),
                    this::loadMatakuliah);
        });

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(CARD_BG());
        }});
        dialog.setVisible(true);
    }

    private void showJadwalDialog() {
        showJadwalDialog(null);
    }

    private void showJadwalDialog(JsonObject existing) {
        if (matakuliahCache.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Data mata kuliah belum tersedia. Muat data atau tambahkan mata kuliah terlebih dahulu.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean editMode = existing != null;
        JDialog dialog = createDialog(editMode ? "Edit Jadwal Kuliah" : "Tambah Jadwal Kuliah", 520, 450);

        JPanel form = buildDialogForm();
        GridBagConstraints g = createFormConstraints();

        JComboBox<MatakuliahOption> cmbMatakuliah = buildMatakuliahCombo();
        JComboBox<String> cmbHari = new JComboBox<>(new String[]{"senin", "selasa", "rabu", "kamis", "jumat", "sabtu"});
        styleCombo(cmbHari);
        JTextField fJam = makeField();
        fJam.setToolTipText("Contoh: 08:00-10:00");
        JTextField fRuangan = makeField();
        JTextField fDosen = makeField();

        if (editMode) {
            selectMatakuliah(cmbMatakuliah, safe(existing, "kode_mk"));
            cmbHari.setSelectedItem(safe(existing, "hari").toLowerCase());
            fJam.setText(safe(existing, "jam"));
            fRuangan.setText(emptyDash(safe(existing, "ruangan")));
            fDosen.setText(emptyDash(safe(existing, "dosen")));
        }

        MatakuliahOption selected = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
        if (!editMode && selected != null && !selected.dosen().isBlank()) {
            fDosen.setText(selected.dosen());
        }
        cmbMatakuliah.addActionListener(e -> {
            MatakuliahOption option = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
            if (!editMode && option != null && !option.dosen().isBlank()) {
                fDosen.setText(option.dosen());
            }
        });

        int row = 0;
        addFormRow(form, g, row++, "Mata Kuliah *", cmbMatakuliah);
        addFormRow(form, g, row++, "Hari *", cmbHari);
        addFormRow(form, g, row++, "Jam *", fJam);
        addFormRow(form, g, row++, "Ruangan *", fRuangan);
        addFormRow(form, g, row++, "Dosen *", fDosen);

        JButton btnSave = buildBtn(editMode ? "Update Jadwal" : "Simpan Jadwal", BLUE(), 180);
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.insets = new Insets(18, 4, 4, 4);
        form.add(btnSave, g);

        btnSave.addActionListener(e -> {
            MatakuliahOption option = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
            if (option == null || isBlank(fJam) || isBlank(fRuangan) || isBlank(fDosen)) {
                JOptionPane.showMessageDialog(dialog, "Semua field wajib harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("kode_mk", option.kodeMk());
            body.addProperty("hari", String.valueOf(cmbHari.getSelectedItem()));
            body.addProperty("jam", fJam.getText().trim());
            body.addProperty("ruangan", fRuangan.getText().trim());
            body.addProperty("dosen", fDosen.getText().trim());

            submitDialog(dialog,
                    () -> editMode
                            ? AkademikService.updateJadwal(safe(existing, "id_jadwal"), body)
                            : AkademikService.createJadwal(body),
                    this::loadJadwal);
        });

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(CARD_BG());
        }});
        dialog.setVisible(true);
    }

    private void showKrsDialog() {
        if (matakuliahCache.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Data mata kuliah belum tersedia. Muat data atau tambahkan mata kuliah terlebih dahulu.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (JwtHelper.getInstance().isAdmin() && mahasiswaCache.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Data mahasiswa belum tersedia. Klik Refresh terlebih dahulu.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = createDialog("Input KRS", 620, 560);

        JPanel form = buildDialogForm();
        GridBagConstraints g = createFormConstraints();

        JComboBox<MahasiswaOption> cmbMahasiswa = buildMahasiswaCombo();
        JTextField fNim = makeField();
        JTextField fNama = makeField();
        fNim.setEnabled(false);
        fNama.setEnabled(false);
        if (JwtHelper.getInstance().isMahasiswa()) {
            fNim.setText(JwtHelper.getInstance().getNim());
        } else if (txtNimFilter != null && !txtNimFilter.getText().trim().isEmpty()) {
            selectMahasiswa(cmbMahasiswa, txtNimFilter.getText().trim());
        }

        JComboBox<MatakuliahOption> cmbMatakuliah = buildMatakuliahCombo();
        JTextField fKodeMk = makeField();
        JTextField fSks = makeField();
        JTextField fSemester = makeField();
        JTextField fJadwal = makeField();
        JTextField fRuangan = makeField();
        JTextField fDosen = makeField();
        for (JTextField field : new JTextField[]{fKodeMk, fSks, fSemester, fJadwal, fRuangan, fDosen}) {
            field.setEnabled(false);
        }
        JTextField fTahunAjaran = makeField();
        String activeTahunAjaran = txtTahunAjaranFilter == null ? "" : txtTahunAjaranFilter.getText().trim();
        fTahunAjaran.setText(activeTahunAjaran.isEmpty()
                ? defaultAcademicYear()
                : activeTahunAjaran);

        JLabel lblPreview = new JLabel("Total SKS maksimal 24.");
        lblPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPreview.setForeground(TEXT_MUTED());

        Runnable updateMahasiswa = () -> {
            MahasiswaOption option = (MahasiswaOption) cmbMahasiswa.getSelectedItem();
            if (option != null) {
                fNim.setText(option.nim());
                fNama.setText(option.nama());
            }
        };
        cmbMahasiswa.addActionListener(e -> updateMahasiswa.run());
        updateMahasiswa.run();

        Runnable updateMatakuliah = () -> {
            MatakuliahOption option = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
            if (option != null) {
                JsonObject jadwal = findFirstJadwal(option.kodeMk());
                fKodeMk.setText(option.kodeMk());
                fSks.setText(String.valueOf(option.sks()));
                fSemester.setText(String.valueOf(option.semester()));
                fJadwal.setText(jadwal == null ? "-" : safe(jadwal, "hari") + " " + safe(jadwal, "jam"));
                fRuangan.setText(jadwal == null ? "-" : safe(jadwal, "ruangan"));
                fDosen.setText(jadwal == null ? emptyDash(option.dosen()) : safe(jadwal, "dosen"));
                lblPreview.setText("Detail otomatis terisi dari mata kuliah dan jadwal aktif.");
            }
        };
        cmbMatakuliah.addActionListener(e -> {
            updateMatakuliah.run();
        });
        updateMatakuliah.run();

        int row = 0;
        if (JwtHelper.getInstance().isAdmin()) {
            addFormRow(form, g, row++, "NIM Mahasiswa *", cmbMahasiswa);
        }
        addFormRow(form, g, row++, "NIM", fNim);
        addFormRow(form, g, row++, "Nama", fNama);
        addFormRow(form, g, row++, "Mata Kuliah *", cmbMatakuliah);
        addFormRow(form, g, row++, "Kode MK", fKodeMk);
        addFormRow(form, g, row++, "SKS", fSks);
        addFormRow(form, g, row++, "Semester", fSemester);
        addFormRow(form, g, row++, "Jadwal", fJadwal);
        addFormRow(form, g, row++, "Ruangan", fRuangan);
        addFormRow(form, g, row++, "Dosen", fDosen);
        addFormRow(form, g, row++, "Tahun Ajaran *", fTahunAjaran);
        g.gridx = 1;
        g.gridy = row++;
        form.add(lblPreview, g);

        JButton btnSave = buildBtn("Simpan KRS", BLUE(), 160);
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.insets = new Insets(18, 4, 4, 4);
        form.add(btnSave, g);

        btnSave.addActionListener(e -> {
            MatakuliahOption option = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
            if (option == null || isBlank(fNim) || isBlank(fTahunAjaran)) {
                JOptionPane.showMessageDialog(dialog, "Semua field wajib harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("nim", fNim.getText().trim());
            body.addProperty("kode_mk", option.kodeMk());
            body.addProperty("tahun_ajaran", fTahunAjaran.getText().trim());

            submitDialog(dialog, () -> AkademikService.createKrs(body), () -> {
                if (JwtHelper.getInstance().isAdmin()) {
                    if (txtNimFilter != null) {
                        txtNimFilter.setText(fNim.getText().trim());
                    }
                }
                if (txtTahunAjaranFilter != null) {
                    txtTahunAjaranFilter.setText(fTahunAjaran.getText().trim());
                }
                loadKrs();
            });
        });

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(CARD_BG());
        }});
        dialog.setVisible(true);
    }

    private String getActivePrintNim() {
        if (JwtHelper.getInstance().isMahasiswa()) {
            return JwtHelper.getInstance().getNim();
        }
        if (txtCetakNimFilter != null) {
            return txtCetakNimFilter.getText().trim();
        }
        return txtNimFilter == null ? "" : txtNimFilter.getText().trim();
    }

    private String getActivePrintTahunAjaran() {
        if (txtCetakTahunAjaranFilter != null) {
            return txtCetakTahunAjaranFilter.getText().trim();
        }
        return txtTahunAjaranFilter == null ? "" : txtTahunAjaranFilter.getText().trim();
    }

    private void showInputKrsPrintPreview() {
        String nim = JwtHelper.getInstance().isMahasiswa()
                ? JwtHelper.getInstance().getNim()
                : txtInputNimFilter.getText().trim();
        String tahunAjaran = txtInputTahunAjaranFilter.getText().trim();

        if (nim.isBlank() || tahunAjaran.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Isi NIM dan Tahun Ajaran terlebih dahulu sebelum mencetak KRS.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loadKrsForPrintPreview(nim, tahunAjaran);
    }

    private void loadKrsForPrintPreview(String nim, String tahunAjaran) {
        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getKrs(nim, tahunAjaran, null);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        currentKrsCache = response.getAsJsonArray("data");
                        currentKrsSummary = response.has("summary") && response.get("summary").isJsonObject()
                                ? response.getAsJsonObject("summary")
                                : null;
                        fillKrsTable(currentKrsCache);
                        syncKrsPrintFilters(nim, tahunAjaran);
                        if (currentKrsCache.size() == 0) {
                            JOptionPane.showMessageDialog(KrsJadwalPanel.this,
                                    "Data KRS tidak ditemukan untuk NIM dan Tahun Ajaran tersebut.",
                                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            showKrsPreviewDialog(nim, tahunAjaran);
                        }
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception e) {
                    showErrorMessage("Gagal memuat preview KRS: " + e.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private void syncKrsPrintFilters(String nim, String tahunAjaran) {
        if (txtNimFilter != null && !JwtHelper.getInstance().isMahasiswa()) {
            txtNimFilter.setText(nim);
        }
        if (txtTahunAjaranFilter != null) {
            txtTahunAjaranFilter.setText(tahunAjaran);
        }
        if (txtCetakNimFilter != null && !JwtHelper.getInstance().isMahasiswa()) {
            txtCetakNimFilter.setText(nim);
        }
        if (txtCetakTahunAjaranFilter != null) {
            txtCetakTahunAjaranFilter.setText(tahunAjaran);
        }
    }

    private void showKrsPreviewDialog(String nim, String tahunAjaran) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Preview Cetak KRS", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnPdf = buildBtn("Export PDF", BLUE(), 120);
        JButton btnPrint = buildBtn("Print", GREEN(), 110);
        JButton btnClose = buildBtn("Tutup", BLUE(), 100);
        btnPdf.addActionListener(e -> exportKrsPdfDocument(nim, tahunAjaran));
        btnPrint.addActionListener(e -> printKrsDocument(nim, tahunAjaran));
        btnClose.addActionListener(e -> dialog.dispose());
        actions.add(btnPdf);
        actions.add(btnPrint);
        actions.add(btnClose);

        JScrollPane scroll = new JScrollPane(buildKrsPreviewPages(nim, tahunAjaran));
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(new Color(226, 232, 240));

        dialog.add(actions, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setSize(980, 760);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void exportKrsPdf() {
        String nim = getActivePrintNim();
        String tahunAjaran = getActivePrintTahunAjaran();

        if (nim == null || nim.isBlank() || tahunAjaran == null || tahunAjaran.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Isi NIM dan Tahun Ajaran terlebih dahulu sebelum export PDF KRS.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loadKrsForDirectPdfExport(nim, tahunAjaran);
    }

    private void loadKrsForDirectPdfExport(String nim, String tahunAjaran) {
        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getKrs(nim, tahunAjaran, null);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        currentKrsCache = response.getAsJsonArray("data");
                        currentKrsSummary = response.has("summary") && response.get("summary").isJsonObject()
                                ? response.getAsJsonObject("summary")
                                : null;
                        if (krsTableModel != null) {
                            fillKrsTable(currentKrsCache);
                        }
                        syncKrsPrintFilters(nim, tahunAjaran);
                        updatePrintPreview();
                        if (currentKrsCache.size() == 0) {
                            JOptionPane.showMessageDialog(KrsJadwalPanel.this,
                                    "Data KRS tidak ditemukan untuk NIM dan Tahun Ajaran tersebut.",
                                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            exportKrsPdfDocument(nim, tahunAjaran);
                        }
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception e) {
                    showErrorMessage("Gagal memuat data Export PDF KRS: " + e.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }
    private void printKrs() {
        String nim = getActivePrintNim();
        String tahunAjaran = getActivePrintTahunAjaran();

        if (nim == null || nim.isBlank() || tahunAjaran == null || tahunAjaran.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Isi NIM dan Tahun Ajaran terlebih dahulu sebelum mencetak KRS.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loadKrsForDirectPrint(nim, tahunAjaran);
    }

    private void loadKrsForDirectPrint(String nim, String tahunAjaran) {
        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return AkademikService.getKrs(nim, tahunAjaran, null);
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        currentKrsCache = response.getAsJsonArray("data");
                        currentKrsSummary = response.has("summary") && response.get("summary").isJsonObject()
                                ? response.getAsJsonObject("summary")
                                : null;
                        if (krsTableModel != null) {
                            fillKrsTable(currentKrsCache);
                        }
                        syncKrsPrintFilters(nim, tahunAjaran);
                        updatePrintPreview();
                        if (currentKrsCache.size() == 0) {
                            JOptionPane.showMessageDialog(KrsJadwalPanel.this,
                                    "Data KRS tidak ditemukan untuk NIM dan Tahun Ajaran tersebut.",
                                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            printKrsDocument(nim, tahunAjaran);
                        }
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception e) {
                    showErrorMessage("Gagal memuat data Cetak KRS: " + e.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private void printKrsDocument(String nim, String tahunAjaran) {
        startBusy();
        try {
            List<JPanel> pages = buildKrsPrintPages(nim, tahunAjaran);
            for (JPanel page : pages) {
                Dimension pageSize = page.getPreferredSize();
                page.setSize(pageSize);
                page.doLayout();
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("KRS_" + nim + "_" + tahunAjaran.replace('/', '-'));
            PageFormat pageFormat = buildA4PortraitPageFormat(job);
            job.setPrintable((graphics, format, pageIndex) -> {
                if (pageIndex < 0 || pageIndex >= pages.size()) return java.awt.print.Printable.NO_SUCH_PAGE;
                JPanel page = pages.get(pageIndex);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.translate(format.getImageableX(), format.getImageableY());
                double scaleX = format.getImageableWidth() / page.getWidth();
                double scaleY = format.getImageableHeight() / page.getHeight();
                double scale = Math.min(scaleX, scaleY);
                g2.scale(scale, scale);
                page.printAll(g2);
                g2.dispose();
                return java.awt.print.Printable.PAGE_EXISTS;
            }, pageFormat);
            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this, "KRS berhasil dikirim ke printer.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            offerPdfFallback(nim, tahunAjaran, e);
        } finally {
            stopBusy();
        }
    }

    private void offerPdfFallback(String nim, String tahunAjaran, Exception cause) {
        if (GraphicsEnvironment.isHeadless()) {
            try {
                File outputFile = defaultPdfFile(nim, tahunAjaran);
                writeKrsPdf(buildKrsPrintPages(nim, tahunAjaran), outputFile);
                System.out.println("KRS PDF berhasil dibuat: " + outputFile.getAbsolutePath());
            } catch (Exception pdfError) {
                System.err.println("Gagal mencetak dan gagal membuat PDF KRS: " + pdfError.getMessage());
            }
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Gagal membuka printer: " + cause.getMessage() + "\n\nSimpan KRS sebagai PDF sebagai alternatif?",
                "Cetak KRS", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            exportKrsPdfDocument(nim, tahunAjaran);
        }
    }

    private void exportKrsPdfDocument(String nim, String tahunAjaran) {
        startBusy();
        try {
            List<JPanel> pages = buildKrsPrintPages(nim, tahunAjaran);
            File outputFile = choosePdfOutputFile(nim, tahunAjaran);
            if (outputFile == null) {
                return;
            }
            writeKrsPdf(pages, outputFile);
            JOptionPane.showMessageDialog(this,
                    "PDF KRS berhasil dibuat:\n" + outputFile.getAbsolutePath(),
                    "Export PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal export PDF KRS: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            stopBusy();
        }
    }

    private File choosePdfOutputFile(String nim, String tahunAjaran) {
        if (GraphicsEnvironment.isHeadless()) {
            return defaultPdfFile(nim, tahunAjaran);
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan PDF KRS");
        chooser.setSelectedFile(defaultPdfFile(nim, tahunAjaran));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selected = chooser.getSelectedFile();
        if (!selected.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            selected = new File(selected.getParentFile(), selected.getName() + ".pdf");
        }
        if (selected.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this,
                    "File sudah ada. Timpa file ini?",
                    "Konfirmasi Export PDF", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        return selected;
    }

    private File defaultPdfFile(String nim, String tahunAjaran) {
        String fileName = "KRS_" + sanitizeFileName(nim) + "_" + sanitizeFileName(tahunAjaran) + ".pdf";
        return new File(System.getProperty("user.home"), fileName);
    }

    private String sanitizeFileName(String value) {
        String clean = value == null ? "" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        return clean.isBlank() ? "KRS" : clean;
    }

    private void writeKrsPdf(List<JPanel> pages, File outputFile) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 0, 0, 0, 0);
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();
        try {
            for (int i = 0; i < pages.size(); i++) {
                JPanel page = pages.get(i);
                Dimension pageSize = page.getPreferredSize();
                page.setSize(pageSize);
                page.doLayout();

                BufferedImage image = new BufferedImage(page.getWidth(), page.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, image.getWidth(), image.getHeight());
                page.printAll(g2);
                g2.dispose();

                com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(image, null);
                pdfImage.scaleToFit(com.itextpdf.text.PageSize.A4.getWidth(), com.itextpdf.text.PageSize.A4.getHeight());
                pdfImage.setAbsolutePosition(
                        (com.itextpdf.text.PageSize.A4.getWidth() - pdfImage.getScaledWidth()) / 2,
                        (com.itextpdf.text.PageSize.A4.getHeight() - pdfImage.getScaledHeight()) / 2);
                document.add(pdfImage);
                if (i < pages.size() - 1) {
                    document.newPage();
                }
            }
        } finally {
            document.close();
        }
    }

    private PageFormat buildA4PortraitPageFormat(PrinterJob job) {
        PageFormat pageFormat = job.defaultPage();
        Paper paper = new Paper();
        double width = 595.2755906;
        double height = 841.8897638;
        double margin = 56.7;
        paper.setSize(width, height);
        paper.setImageableArea(margin, margin, width - (margin * 2), height - (margin * 2));
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        pageFormat.setPaper(paper);
        return pageFormat;
    }

    private JPanel buildKrsPreviewPages(String nim, String tahunAjaran) {
        JPanel preview = new JPanel();
        preview.setBackground(new Color(226, 232, 240));
        preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));
        preview.setBorder(new EmptyBorder(18, 0, 18, 0));
        for (JPanel page : buildKrsPrintPages(nim, tahunAjaran)) {
            page.setAlignmentX(Component.CENTER_ALIGNMENT);
            preview.add(page);
            preview.add(Box.createVerticalStrut(18));
        }
        return preview;
    }

    private List<JPanel> buildKrsPrintPages(String nim, String tahunAjaran) {
        PrintIdentity identity = buildPrintIdentity(nim, tahunAjaran);
        List<String[]> krsRows = buildPrintKrsRows();
        List<String[]> scheduleRows = buildPrintScheduleRows();
        List<List<String[]>> scheduleChunks = splitScheduleRowsForPrint(scheduleRows);
        List<JPanel> pages = new ArrayList<>();
        int totalPages = Math.max(1, scheduleChunks.size());
        for (int i = 0; i < totalPages; i++) {
            boolean firstPage = i == 0;
            boolean lastPage = i == totalPages - 1;
            List<String[]> scheduleChunk = scheduleChunks.isEmpty() ? new ArrayList<>() : scheduleChunks.get(i);
            pages.add(buildKrsPrintPage(identity, krsRows, scheduleChunk, i + 1, totalPages, firstPage, lastPage));
        }
        return pages;
    }

    private PrintIdentity buildPrintIdentity(String nim, String tahunAjaran) {
        JsonObject first = currentKrsCache.get(0).getAsJsonObject();
        String nama = firstNonEmpty(first, "nama_mahasiswa", "nama");
        String programStudi = firstNonEmpty(first, "program_studi", "jurusan");
        String semesterMahasiswa = firstNonEmpty(first, "semester_mahasiswa", "semester");
        String dosenWali = firstNonEmpty(first, "dosen_wali", "nama_dosen_wali");
        MahasiswaOption mahasiswa = findMahasiswa(nim);
        if (mahasiswa != null) {
            if (programStudi.equals("-")) programStudi = emptyDash(mahasiswa.kelas());
            if (semesterMahasiswa.equals("-")) semesterMahasiswa = emptyDash(mahasiswa.semester());
        }
        return new PrintIdentity(nama, nim, programStudi, semesterMahasiswa, tahunAjaran, dosenWali, buildKrsDocumentNumber(nim, tahunAjaran));
    }

    private List<String[]> buildPrintKrsRows() {
        List<String[]> rows = new ArrayList<>();
        int no = 1;
        for (JsonElement element : currentKrsCache) {
            JsonObject item = element.getAsJsonObject();
            rows.add(new String[]{String.valueOf(no++), safe(item, "kode_mk"), safe(item, "nama_mk"), safe(item, "sks"), firstNonEmpty(item, "semester_mata_kuliah", "semester")});
        }
        return rows;
    }

    private List<String[]> buildPrintScheduleRows() {
        List<String[]> rows = new ArrayList<>();
        int no = 1;
        for (JsonElement element : currentKrsCache) {
            JsonObject item = element.getAsJsonObject();
            String kodeMk = safe(item, "kode_mk");
            String namaMk = safe(item, "nama_mk");
            String[] schedules = splitPipeValues(safe(item, "jadwal"));
            String[] rooms = splitCommaValues(safe(item, "ruangan"));
            String lecturer = emptyDash(safe(item, "dosen"));
            if (schedules.length == 0) {
                rows.add(new String[]{String.valueOf(no++), kodeMk, namaMk, "-", "-", "-", lecturer});
                continue;
            }
            for (int i = 0; i < schedules.length; i++) {
                String[] schedule = splitSchedule(schedules[i]);
                rows.add(new String[]{String.valueOf(no++), kodeMk, namaMk, schedule[0], schedule[1], valueAtOrLast(rooms, i), lecturer});
            }
        }
        if (rows.isEmpty()) rows.add(new String[]{"1", "-", "Belum ada jadwal perkuliahan terdaftar", "-", "-", "-", "-"});
        return rows;
    }

    private List<List<String[]>> splitScheduleRowsForPrint(List<String[]> scheduleRows) {
        List<List<String[]>> chunks = new ArrayList<>();
        if (scheduleRows.isEmpty()) {
            chunks.add(new ArrayList<>());
            return chunks;
        }
        int index = 0;
        while (index < scheduleRows.size()) {
            int maxRows = chunks.isEmpty() ? 10 : 12;
            int end = Math.min(scheduleRows.size(), index + maxRows);
            chunks.add(new ArrayList<>(scheduleRows.subList(index, end)));
            index = end;
        }
        return chunks;
    }

    private JPanel buildKrsPrintPage(PrintIdentity identity, List<String[]> krsRows, List<String[]> scheduleRows,
                                     int pageNumber, int totalPages, boolean firstPage, boolean lastPage) {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(36, 46, 28, 46));
        page.setSize(794, 1123);
        page.setPreferredSize(new Dimension(794, 1123));
        page.add(buildOfficialPrintHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        if (firstPage) {
            body.add(buildIdentitySection(identity));
            body.add(Box.createVerticalStrut(12));
            body.add(buildSectionTitle("DAFTAR MATA KULIAH KRS"));
            body.add(buildPrintTable(new String[]{"No", "Kode MK", "Nama Mata Kuliah", "SKS", "Semester"},
                    new int[]{38, 100, 370, 56, 82}, krsRows,
                    new boolean[]{true, true, false, true, true}, new Color(235, 237, 240)));
            body.add(Box.createVerticalStrut(14));
        }
        body.add(buildSectionTitle(firstPage ? "JADWAL PERKULIAHAN" : "JADWAL PERKULIAHAN (LANJUTAN)"));
        body.add(buildPrintTable(new String[]{"No", "Kode MK", "Nama Mata Kuliah", "Hari", "Jam", "Ruangan", "Dosen Pengampu"},
                new int[]{34, 70, 190, 66, 86, 74, 166}, scheduleRows,
                new boolean[]{true, true, false, true, true, true, false}, new Color(228, 232, 238)));
        if (lastPage) {
            body.add(Box.createVerticalStrut(16));
            body.add(buildKrsSummaryBox(krsRows.size(), calculateTotalSks(currentKrsCache)));
            body.add(Box.createVerticalStrut(18));
            body.add(buildApprovalSection(identity));
        }
        page.add(body, BorderLayout.CENTER);
        page.add(buildOfficialPrintFooter(identity.documentNumber(), pageNumber, totalPages), BorderLayout.SOUTH);
        page.doLayout();
        return page;
    }

    private JPanel buildOfficialPrintHeader() {
        JPanel wrap = new JPanel(new BorderLayout(16, 0));
        wrap.setOpaque(false);
        wrap.add(buildCampusLogo(72), BorderLayout.WEST);
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(centerPrintLabel("SIAKAD", 22, Font.BOLD));
        center.add(centerPrintLabel("Sistem Informasi Akademik", 12, Font.PLAIN));
        center.add(Box.createVerticalStrut(4));
        center.add(centerPrintLabel("KARTU RENCANA STUDI (KRS)", 18, Font.BOLD));
        wrap.add(center, BorderLayout.CENTER);
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(72, 72));
        wrap.add(spacer, BorderLayout.EAST);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(wrap);
        header.add(Box.createVerticalStrut(10));
        header.add(buildDoubleRule());
        return header;
    }

    private void updatePrintPreview() {
        if (printPreviewHost == null) {
            return;
        }
        printPreviewHost.removeAll();
        if (currentKrsCache.size() == 0) {
            JLabel empty = makeInfoLabel("Data KRS tidak ditemukan untuk filter tersebut.");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            printPreviewHost.add(empty, BorderLayout.CENTER);
        } else {
            String nim = getActivePrintNim();
            String tahunAjaran = getActivePrintTahunAjaran();
            Set<String> uniqueNim = new LinkedHashSet<>();
            Set<String> uniqueTa = new LinkedHashSet<>();
            for (JsonElement element : currentKrsCache) {
                JsonObject item = element.getAsJsonObject();
                uniqueNim.add(safe(item, "nim"));
                uniqueTa.add(safe(item, "tahun_ajaran"));
            }
            if ((nim == null || nim.isBlank()) && uniqueNim.size() == 1) nim = uniqueNim.iterator().next();
            if ((tahunAjaran == null || tahunAjaran.isBlank()) && uniqueTa.size() == 1) tahunAjaran = uniqueTa.iterator().next();
            printPreviewHost.add(buildKrsPreviewPages(nim, tahunAjaran), BorderLayout.CENTER);
        }
        printPreviewHost.revalidate();
        printPreviewHost.repaint();
    }

    private JComponent buildCampusLogo(int size) {
        java.net.URL logoUrl = KrsJadwalPanel.class.getResource("/images/Logo_LP3I.png");
        final Image logo = logoUrl == null ? null : new ImageIcon(logoUrl).getImage();
        return new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                if (logo != null) {
                    int pad = 1;
                    int boxW = getWidth() - pad * 2;
                    int boxH = getHeight() - pad * 2;
                    int iw = logo.getWidth(this);
                    int ih = logo.getHeight(this);
                    if (iw > 0 && ih > 0) {
                        double scale = Math.min((double) boxW / iw, (double) boxH / ih);
                        int drawW = Math.max(1, (int) Math.round(iw * scale));
                        int drawH = Math.max(1, (int) Math.round(ih * scale));
                        int x = (getWidth() - drawW) / 2;
                        int y = (getHeight() - drawH) / 2;
                        g2.drawImage(logo, x, y, drawW, drawH, this);
                        g2.dispose();
                        return;
                    }
                }

                int pad = 3;
                int d = Math.min(getWidth(), getHeight()) - pad * 2;
                int x = (getWidth() - d) / 2;
                int y = (getHeight() - d) / 2;
                g2.setColor(new Color(245, 247, 250));
                g2.fillOval(x, y, d, d);
                g2.setColor(new Color(31, 41, 55));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawOval(x, y, d, d);
                g2.setColor(new Color(79, 70, 229));
                g2.fillOval(x + 10, y + 10, d - 20, d - 20);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int bx = x + d / 2 - 15;
                int by = y + d / 2 - 10;
                g2.drawRoundRect(bx, by, 30, 24, 5, 5);
                g2.drawLine(bx + 15, by, bx + 15, by + 24);
                g2.drawLine(bx + 5, by + 8, bx + 12, by + 8);
                g2.drawLine(bx + 18, by + 8, bx + 25, by + 8);
                g2.dispose();
            }
        };
    }

    private JComponent buildDoubleRule() {
        return new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(1, 8); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, 1, getWidth(), 1);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, 6, getWidth(), 6);
                g2.dispose();
            }
        };
    }

    private JPanel buildIdentitySection(PrintIdentity identity) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(buildSectionTitle("IDENTITAS MAHASISWA"));
        JPanel grid = new JPanel(new GridLayout(6, 2, 0, 0));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addIdentityRow(grid, "Nama Mahasiswa", identity.nama());
        addIdentityRow(grid, "NIM", identity.nim());
        addIdentityRow(grid, "Program Studi", identity.programStudi());
        addIdentityRow(grid, "Semester", identity.semester());
        addIdentityRow(grid, "Tahun Akademik", identity.tahunAjaran());
        addIdentityRow(grid, "Dosen Wali", identity.dosenWali());
        section.add(grid);
        return section;
    }

    private void addIdentityRow(JPanel grid, String label, String value) {
        grid.add(buildPrintInfoCell(label + " :", true, true));
        grid.add(buildPrintInfoCell(emptyDash(value), false, false));
    }

    private JLabel buildSectionTitle(String title) {
        JLabel label = printText(title, 11, Font.BOLD);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        return label;
    }

    private JPanel buildPrintTable(String[] columns, int[] widths, List<String[]> rows, boolean[] centerColumns, Color headerColor) {
        JPanel table = new JPanel(new GridBagLayout());
        table.setOpaque(false);
        table.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridy = 0;
        for (int i = 0; i < columns.length; i++) {
            g.gridx = i;
            g.weightx = widths[i];
            table.add(buildPrintTableCell(columns[i], widths[i], true, true, headerColor), g);
        }
        for (int row = 0; row < rows.size(); row++) {
            String[] data = rows.get(row);
            g.gridy = row + 1;
            for (int col = 0; col < columns.length; col++) {
                g.gridx = col;
                g.weightx = widths[col];
                String value = col < data.length ? data[col] : "-";
                table.add(buildPrintTableCell(value, widths[col], false, centerColumns[col], Color.WHITE), g);
            }
        }
        return table;
    }

    private JPanel buildPrintTableCell(String text, int width, boolean header, boolean center, Color background) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(background);
        cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cell.setPreferredSize(new Dimension(width, header ? 28 : 30));
        JLabel label = new JLabel(toHtml(text, Math.max(30, width - 10), center));
        label.setFont(new Font("Times New Roman", header ? Font.BOLD : Font.PLAIN, 10));
        label.setForeground(Color.BLACK);
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        label.setVerticalAlignment(SwingConstants.CENTER);
        cell.add(label, BorderLayout.CENTER);
        return cell;
    }

    private JPanel buildPrintInfoCell(String text, boolean header, boolean labelCell) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(labelCell ? new Color(248, 249, 250) : Color.WHITE);
        cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JLabel label = new JLabel(toHtml(text, labelCell ? 180 : 420, false));
        label.setFont(new Font("Times New Roman", header ? Font.BOLD : Font.PLAIN, 10));
        label.setForeground(Color.BLACK);
        label.setBorder(new EmptyBorder(5, 8, 5, 8));
        cell.add(label, BorderLayout.CENTER);
        return cell;
    }

    private JPanel buildKrsSummaryBox(int jumlahMk, int totalSks) {
        JPanel box = new JPanel(new GridLayout(2, 2, 12, 4));
        box.setOpaque(true);
        box.setBackground(new Color(248, 249, 250));
        box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), new EmptyBorder(10, 12, 10, 12)));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPrintMeta(box, "Jumlah Mata Kuliah", String.valueOf(jumlahMk));
        addPrintMeta(box, "Total SKS", totalSks + " SKS");
        return box;
    }

    private JPanel buildApprovalSection(PrintIdentity identity) {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel date = printText("Pekanbaru, " + formatPrintDate(), 11, Font.PLAIN);
        date.setHorizontalAlignment(SwingConstants.RIGHT);
        wrap.add(date, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);

        JLabel knowing = printText("Mengetahui,", 11, Font.PLAIN);
        knowing.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(knowing, BorderLayout.NORTH);

        JPanel signatures = new JPanel(new GridLayout(1, 2, 120, 0));
        signatures.setOpaque(false);
        signatures.add(signatureBlock("Mahasiswa,", identity.nama(), "", true, false));
        signatures.add(signatureBlock("Ketua Akademik,", "Dr. H. Ahmad Syafii, M.Kom", "", false, true));
        center.add(signatures, BorderLayout.CENTER);
        wrap.add(center, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildOfficialPrintFooter(String documentNumber, int pageNumber, int totalPages) {
        JPanel footer = new JPanel(new BorderLayout(8, 3));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        JLabel note = printText("Dokumen ini dicetak secara otomatis oleh Sistem Informasi Akademik (SIAKAD)", 9, Font.ITALIC);
        note.setHorizontalAlignment(SwingConstants.CENTER);
        footer.add(note, BorderLayout.NORTH);
        JPanel meta = new JPanel(new GridLayout(1, 3, 8, 0));
        meta.setOpaque(false);
        meta.add(printText("No. Dokumen: " + documentNumber, 8, Font.PLAIN));
        JLabel date = printText("Tanggal Cetak: " + formatPrintDate(), 8, Font.PLAIN);
        date.setHorizontalAlignment(SwingConstants.CENTER);
        meta.add(date);
        JLabel page = printText("Page " + pageNumber + " of " + totalPages, 8, Font.PLAIN);
        page.setHorizontalAlignment(SwingConstants.RIGHT);
        meta.add(page);
        footer.add(meta, BorderLayout.SOUTH);
        return footer;
    }

    private String[] splitPipeValues(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return new String[0];
        String[] raw = value.split("\\|");
        List<String> values = new ArrayList<>();
        for (String item : raw) {
            String clean = item.trim();
            if (!clean.isBlank() && !"-".equals(clean)) values.add(clean);
        }
        return values.toArray(new String[0]);
    }

    private String[] splitCommaValues(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return new String[0];
        String[] raw = value.split(",");
        List<String> values = new ArrayList<>();
        for (String item : raw) {
            String clean = item.trim();
            if (!clean.isBlank() && !"-".equals(clean)) values.add(clean);
        }
        return values.toArray(new String[0]);
    }

    private String valueAtOrLast(String[] values, int index) {
        if (values.length == 0) return "-";
        return values[Math.min(index, values.length - 1)];
    }

    private String buildKrsDocumentNumber(String nim, String tahunAjaran) {
        String cleanTa = tahunAjaran == null ? "TA" : tahunAjaran.replaceAll("[^0-9]", "");
        String cleanNim = nim == null ? "NIM" : nim.replaceAll("[^A-Za-z0-9]", "");
        return "KRS/" + cleanTa + "/" + cleanNim + "/" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private String formatPrintDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID")));
    }

    private String toHtml(String value, int width, boolean center) {
        String align = center ? "center" : "left";
        return "<html><body style='width:" + width + "px;text-align:" + align + ";'>" + escapeHtml(emptyDash(value)) + "</body></html>";
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void submitDialog(JDialog dialog, RequestAction requestAction, Runnable afterSuccess) {
        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return requestAction.execute();
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        String message = response.has("message")
                                ? response.get("message").getAsString()
                                : "Permintaan berhasil diproses.";
                        JOptionPane.showMessageDialog(dialog, message, "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        afterSuccess.run();
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                response.has("message") ? response.get("message").getAsString() : "Permintaan gagal diproses.",
                                "Gagal", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    component.setBackground(row % 2 == 0 ? TABLE_BG() : ROW_ALT());
                }
                component.setForeground(TEXT_PRIMARY());
                return component;
            }
        };
        table.setBackground(TABLE_BG());
        table.setForeground(TEXT_PRIMARY());
        table.setSelectionBackground(new Color(59, 130, 246, 60));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(20, 30, 55));
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG());
        header.setForeground(TEXT_MUTED());
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR()));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        left.setForeground(TEXT_PRIMARY());
        table.setDefaultRenderer(Object.class, left);

        return table;
    }

    private JTextField makeField() {
        JTextField field = new JTextField();
        field.setBackground(AppTheme.input());
        field.setForeground(TEXT_PRIMARY());
        field.setCaretColor(TEXT_PRIMARY());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1),
                new EmptyBorder(7, 10, 7, 10)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(220, 34));
        return field;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_MUTED());
        return label;
    }

    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(TEXT_DIM());
        return label;
    }

    private JButton buildBtn(String text, Color bg, int width) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics metrics = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - metrics.stringWidth(getText())) / 2,
                        (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(width, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(AppTheme.input());
        combo.setForeground(TEXT_PRIMARY());
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JDialog createDialog(String title, int width, int height) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private JPanel buildDialogForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG());
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        return panel;
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 4, 5, 4);
        g.weightx = 1;
        return g;
    }

    private void addFormRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        panel.add(makeLabel(label), g);

        g.gridx = 1;
        panel.add(field, g);
    }

    private JComboBox<MatakuliahOption> buildMatakuliahCombo() {
        JComboBox<MatakuliahOption> combo = new JComboBox<>();
        for (JsonElement element : matakuliahCache) {
            JsonObject mk = element.getAsJsonObject();
            String dosen = safe(mk, "dosen_pengampu");
            combo.addItem(new MatakuliahOption(
                    safe(mk, "kode_mk"),
                    safe(mk, "nama_mk"),
                    parseInteger(safe(mk, "sks")),
                    parseInteger(safe(mk, "semester")),
                    "-".equals(dosen) ? "" : dosen
            ));
        }
        styleCombo(combo);
        return combo;
    }

    private void refreshInlineMatakuliahCombo() {
        if (cmbKrsMatakuliah == null) {
            return;
        }
        Object selected = cmbKrsMatakuliah.getSelectedItem();
        String selectedKode = selected instanceof MatakuliahOption option ? option.kodeMk() : "";
        cmbKrsMatakuliah.removeAllItems();
        for (JsonElement element : matakuliahCache) {
            JsonObject mk = element.getAsJsonObject();
            String dosen = safe(mk, "dosen_pengampu");
            MatakuliahOption option = new MatakuliahOption(
                    safe(mk, "kode_mk"),
                    safe(mk, "nama_mk"),
                    parseInteger(safe(mk, "sks")),
                    parseInteger(safe(mk, "semester")),
                    "-".equals(dosen) ? "" : dosen
            );
            cmbKrsMatakuliah.addItem(option);
            if (!selectedKode.isBlank() && option.kodeMk().equals(selectedKode)) {
                cmbKrsMatakuliah.setSelectedItem(option);
            }
        }
    }

    private void submitInlineKrs() {
        MatakuliahOption option = cmbKrsMatakuliah == null ? null : (MatakuliahOption) cmbKrsMatakuliah.getSelectedItem();
        String nim = JwtHelper.getInstance().isMahasiswa()
                ? JwtHelper.getInstance().getNim()
                : txtInputNimFilter.getText().trim();
        String tahunAjaran = txtInputTahunAjaranFilter.getText().trim();
        if (option == null || nim.isBlank() || tahunAjaran.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "NIM, tahun ajaran, dan mata kuliah wajib diisi.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("nim", nim);
        body.addProperty("kode_mk", option.kodeMk());
        body.addProperty("tahun_ajaran", tahunAjaran);

        runRequest(() -> AkademikService.createKrs(body), () -> {
            if (txtNimFilter != null && !JwtHelper.getInstance().isMahasiswa()) {
                txtNimFilter.setText(nim);
            }
            if (txtTahunAjaranFilter != null) {
                txtTahunAjaranFilter.setText(tahunAjaran);
            }
            loadKrsData(nim, tahunAjaran, false);
        });
    }

    private JComboBox<MahasiswaOption> buildMahasiswaCombo() {
        JComboBox<MahasiswaOption> combo = new JComboBox<>();
        if (JwtHelper.getInstance().isMahasiswa()) {
            combo.addItem(new MahasiswaOption(JwtHelper.getInstance().getNim(), "-", "-", "-", "-"));
        } else {
            for (JsonElement element : mahasiswaCache) {
                JsonObject mhs = element.getAsJsonObject();
                combo.addItem(new MahasiswaOption(
                        safe(mhs, "nim"),
                        safe(mhs, "nama"),
                        safe(mhs, "jurusan"),
                        safe(mhs, "program_studi"),
                        safe(mhs, "semester")
                ));
            }
        }
        styleCombo(combo);
        return combo;
    }

    private void setupActionColumns(JTable table, int editColumn, int deleteColumn, Runnable editAction, Runnable deleteAction) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(editColumn).setMaxWidth(72);
        table.getColumnModel().getColumn(deleteColumn).setMaxWidth(72);
        table.getColumnModel().getColumn(editColumn).setCellRenderer(center);
        table.getColumnModel().getColumn(deleteColumn).setCellRenderer(center);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0) return;
                table.setRowSelectionInterval(row, row);
                if (col == editColumn) {
                    editAction.run();
                } else if (col == deleteColumn) {
                    deleteAction.run();
                }
            }
        });
    }

    private void editSelectedMatakuliah() {
        int row = tableMatakuliah.getSelectedRow();
        if (row < 0) return;
        String kodeMk = String.valueOf(matakuliahTableModel.getValueAt(tableMatakuliah.convertRowIndexToModel(row), 0));
        JsonObject mk = findMatakuliah(kodeMk);
        if (mk != null) showMatakuliahDialog(mk);
    }

    private void deleteSelectedMatakuliah() {
        int row = tableMatakuliah.getSelectedRow();
        if (row < 0) return;
        String kodeMk = String.valueOf(matakuliahTableModel.getValueAt(tableMatakuliah.convertRowIndexToModel(row), 0));
        if (JOptionPane.showConfirmDialog(this, "Hapus mata kuliah " + kodeMk + "?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        runRequest(() -> AkademikService.deleteMatakuliah(kodeMk), () -> {
            loadMatakuliah();
        });
    }

    private void editSelectedJadwal() {
        int row = tableJadwal.getSelectedRow();
        if (row < 0) return;
        String id = String.valueOf(jadwalTableModel.getValueAt(tableJadwal.convertRowIndexToModel(row), 0));
        JsonObject jadwal = findJadwal(id);
        if (jadwal != null) showJadwalDialog(jadwal);
    }

    private void deleteSelectedJadwal() {
        int row = tableJadwal.getSelectedRow();
        if (row < 0) return;
        String id = String.valueOf(jadwalTableModel.getValueAt(tableJadwal.convertRowIndexToModel(row), 0));
        if (JOptionPane.showConfirmDialog(this, "Hapus jadwal ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        runRequest(() -> AkademikService.deleteJadwal(id), () -> {
            loadJadwal();
        });
    }

    private void runRequest(RequestAction requestAction, Runnable afterSuccess) {
        startBusy();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return requestAction.execute();
            }

            @Override protected void done() {
                try {
                    JsonObject response = get();
                    if (response.get("success").getAsBoolean()) {
                        JOptionPane.showMessageDialog(KrsJadwalPanel.this,
                                response.has("message") ? response.get("message").getAsString() : "Permintaan berhasil diproses.",
                                "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        afterSuccess.run();
                    } else {
                        showErrorMessage(response);
                    }
                } catch (Exception ex) {
                    showErrorMessage("Error: " + ex.getMessage());
                } finally {
                    stopBusy();
                }
            }
        }.execute();
    }

    private JsonObject findMatakuliah(String kodeMk) {
        for (JsonElement element : matakuliahCache) {
            JsonObject mk = element.getAsJsonObject();
            if (safe(mk, "kode_mk").equals(kodeMk)) return mk;
        }
        return null;
    }

    private JsonObject findJadwal(String id) {
        for (JsonElement element : jadwalCache) {
            JsonObject jadwal = element.getAsJsonObject();
            if (safe(jadwal, "id_jadwal").equals(id)) return jadwal;
        }
        return null;
    }

    private JsonObject findFirstJadwal(String kodeMk) {
        for (JsonElement element : jadwalCache) {
            JsonObject jadwal = element.getAsJsonObject();
            if (safe(jadwal, "kode_mk").equals(kodeMk)) return jadwal;
        }
        return null;
    }

    private MahasiswaOption findMahasiswa(String nim) {
        for (JsonElement element : mahasiswaCache) {
            JsonObject mhs = element.getAsJsonObject();
            if (safe(mhs, "nim").equals(nim)) {
                return new MahasiswaOption(
                        safe(mhs, "nim"),
                        safe(mhs, "nama"),
                        safe(mhs, "jurusan"),
                        safe(mhs, "program_studi"),
                        safe(mhs, "semester")
                );
            }
        }
        return null;
    }

    private void selectMatakuliah(JComboBox<MatakuliahOption> combo, String kodeMk) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).kodeMk().equals(kodeMk)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectMahasiswa(JComboBox<MahasiswaOption> combo, String nim) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).nim().equals(nim)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String[] splitSchedule(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return new String[]{"-", "-"};
        String first = value.split("\\|")[0].trim();
        int space = first.indexOf(' ');
        if (space < 0) return new String[]{first, "-"};
        return new String[]{first.substring(0, space), first.substring(space + 1)};
    }

    private JLabel centerPrintLabel(String text, int size, int style) {
        JLabel label = printText(text, size, style);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel printText(String text, int size, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Serif", style, size));
        return label;
    }

    private void addPrintMeta(JPanel panel, String label, String value) {
        panel.add(printText(label + ":", 11, Font.BOLD));
        panel.add(printText(emptyDash(value), 11, Font.PLAIN));
    }

    private JPanel signatureBlock(String top, String name, String role) {
        return signatureBlock(top, name, role, true, false);
    }

    private JPanel signatureBlock(String top, String name, String role, boolean wrapNameInParentheses) {
        return signatureBlock(top, name, role, wrapNameInParentheses, false);
    }

    private JPanel signatureBlock(String top, String name, String role, boolean wrapNameInParentheses, boolean digitalSignature) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(286, 132));
        panel.setMinimumSize(new Dimension(286, 132));

        JLabel title = centerPrintLabel(top, 11, Font.PLAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);

        JPanel signatureArea = new JPanel(new GridBagLayout());
        signatureArea.setOpaque(false);
        signatureArea.setPreferredSize(new Dimension(286, 74));
        signatureArea.setMaximumSize(new Dimension(286, 74));
        if (digitalSignature) {
            signatureArea.add(buildDigitalSignature());
        }
        panel.add(signatureArea);

        String displayName = wrapNameInParentheses ? "(" + emptyDash(name) + ")" : emptyDash(name);
        JLabel nameLabel = centerPrintLabel(displayName, 11, Font.BOLD);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);
        if (role != null && !role.isBlank()) {
            JLabel roleLabel = centerPrintLabel(role, 11, Font.PLAIN);
            roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(roleLabel);
        }
        return panel;
    }

    private JComponent buildDigitalSignature() {
        return new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(178, 50); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(29, 78, 216));
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int y = getHeight() / 2 + 8;
                g2.drawArc(18, y - 24, 38, 26, 180, -230);
                g2.drawLine(47, y - 13, 64, y + 6);
                g2.drawArc(57, y - 22, 46, 28, 200, 240);
                g2.drawArc(93, y - 20, 40, 26, 190, -230);
                g2.drawLine(125, y - 8, 153, y - 20);
                g2.drawLine(153, y - 20, 169, y + 4);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(24, y + 14, 171, y + 14);
                g2.dispose();
            }
        };
    }

    private void startBusy() {
        activeLoads++;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void stopBusy() {
        activeLoads = Math.max(0, activeLoads - 1);
        if (activeLoads == 0) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void showErrorMessage(JsonObject response) {
        showErrorMessage(response.has("message") ? response.get("message").getAsString() : "Terjadi kesalahan.");
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Informasi", JOptionPane.WARNING_MESSAGE);
    }

    private String safe(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsString()
                : "-";
    }

    private String firstNonEmpty(JsonObject object, String... keys) {
        for (String key : keys) {
            String value = safe(object, key);
            if (value != null && !value.isBlank() && !"-".equals(value)) {
                return value;
            }
        }
        return "-";
    }

    private boolean isBlank(JTextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int calculateTotalSks(JsonArray data) {
        int total = 0;
        for (JsonElement element : data) {
            total += parseInteger(safe(element.getAsJsonObject(), "sks"));
        }
        return total;
    }

    private String defaultAcademicYear() {
        int year = Year.now().getValue();
        return year + "/" + (year + 1);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return "-";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String emptyDash(String value) {
        return value == null || value.isBlank() || "-".equals(value) ? "-" : value;
    }

    @FunctionalInterface
    private interface RequestAction {
        JsonObject execute() throws Exception;
    }

    private record PrintIdentity(String nama, String nim, String programStudi, String semester,
                                 String tahunAjaran, String dosenWali, String documentNumber) {}

    private record MatakuliahOption(String kodeMk, String namaMk, int sks, int semester, String dosen) {
        @Override public String toString() {
            return kodeMk + " - " + namaMk;
        }
    }

    private record MahasiswaOption(String nim, String nama, String jurusan, String kelas, String semester) {
        @Override public String toString() {
            return nim + " - " + nama;
        }
    }
}
