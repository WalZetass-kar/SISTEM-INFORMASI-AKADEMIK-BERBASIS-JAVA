package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.siakad.services.AkademikService;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Year;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * KrsJadwalPanel - Modul KRS & Jadwal Kuliah
 */
public class KrsJadwalPanel extends JPanel {

    private JTable tableKrs;
    private JTable tableMatakuliah;
    private JTable tableJadwal;
    private DefaultTableModel krsTableModel;
    private DefaultTableModel matakuliahTableModel;
    private DefaultTableModel jadwalTableModel;

    private JTextField txtNimFilter;
    private JTextField txtTahunAjaranFilter;

    private JLabel lblKrsStat;
    private JLabel lblSksStat;
    private JLabel lblJadwalStat;
    private JLabel lblKrsInfo;
    private JLabel lblMatakuliahInfo;
    private JLabel lblJadwalInfo;

    private JsonArray matakuliahCache = new JsonArray();
    private JsonArray jadwalCache = new JsonArray();
    private JsonArray currentKrsCache = new JsonArray();
    private JsonObject currentKrsSummary = null;

    private int activeLoads = 0;

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

    public KrsJadwalPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        initUI();
        refreshAllData();
    }

    private void initUI() {
        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildSummarySection());
        body.add(Box.createVerticalStrut(16));
        body.add(buildTabbedSection());

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("KRS & Jadwal Kuliah");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Kelola input KRS, mata kuliah, jadwal kuliah, dan cetak KRS");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnRefresh = buildBtn("Refresh", BLUE, 110);
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

        wrap.add(buildStatCard("KRS Aktif", "Jumlah baris KRS yang sedang ditampilkan", lblKrsStat, BLUE));
        wrap.add(buildStatCard("Total SKS", "Akumulasi SKS dari hasil filter KRS aktif", lblSksStat, GREEN));
        wrap.add(buildStatCard("Jadwal Kuliah", "Jumlah jadwal kuliah yang tersedia", lblJadwalStat, YELLOW));
        return wrap;
    }

    private JPanel buildStatCard(String title, String desc, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
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
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><body style='width:220px'>" + desc + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(lblDesc);
        return card;
    }

    private JComponent buildTabbedSection() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBackground(CARD_BG);
        tabs.setForeground(TEXT_MUTED);
        tabs.setBorder(new EmptyBorder(0, 28, 18, 28));

        tabs.addTab("Input KRS", buildKrsTab());
        tabs.addTab("Mata Kuliah", buildMatakuliahTab());
        tabs.addTab("Jadwal Kuliah", buildJadwalTab());

        return tabs;
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

        JButton btnLoad = buildBtn("Muat KRS", BLUE, 120);
        btnLoad.addActionListener(e -> loadKrs());
        JButton btnInput = buildBtn("Input KRS", GREEN, 120);
        btnInput.addActionListener(e -> showKrsDialog());
        JButton btnPrint = buildBtn("Cetak KRS", new Color(30, 41, 70), 120);
        btnPrint.addActionListener(e -> printKrs());

        actions.add(btnLoad);
        actions.add(btnInput);
        actions.add(btnPrint);

        topCard.add(filters, BorderLayout.CENTER);
        topCard.add(actions, BorderLayout.EAST);

        String[] columns = {"ID", "NIM", "Nama", "Kode MK", "Mata Kuliah", "SKS", "Semester", "Tahun Ajaran", "Jadwal", "Ruangan", "Dosen"};
        krsTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        };
        tableKrs = buildTable(krsTableModel);
        tableKrs.getColumnModel().getColumn(0).setMaxWidth(55);
        tableKrs.getColumnModel().getColumn(5).setMaxWidth(55);
        tableKrs.getColumnModel().getColumn(6).setMaxWidth(70);

        JPanel tableCard = buildCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableCard.add(new JScrollPane(tableKrs) {{
            setBorder(null);
            getViewport().setBackground(TABLE_BG);
        }}, BorderLayout.CENTER);

        lblKrsInfo = makeInfoLabel("Belum ada data KRS dimuat.");

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 0, 4));
        footer.add(lblKrsInfo, BorderLayout.WEST);

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

    private JComponent buildMatakuliahTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel topCard = buildCard();
        topCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        topCard.setLayout(new BorderLayout());

        JLabel info = new JLabel("Daftar mata kuliah yang dapat dipilih pada proses input KRS.");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(TEXT_MUTED);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnRefresh = buildBtn("Refresh", BLUE, 110);
        btnRefresh.addActionListener(e -> loadMatakuliah());
        actions.add(btnRefresh);
        if (JwtHelper.getInstance().isAdmin()) {
            JButton btnTambah = buildBtn("Tambah Mata Kuliah", GREEN, 190);
            btnTambah.addActionListener(e -> showMatakuliahDialog());
            actions.add(btnTambah);
        }

        topCard.add(info, BorderLayout.WEST);
        topCard.add(actions, BorderLayout.EAST);

        String[] columns = {"Kode MK", "Nama Mata Kuliah", "SKS", "Semester"};
        matakuliahTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        };
        tableMatakuliah = buildTable(matakuliahTableModel);
        tableMatakuliah.getColumnModel().getColumn(2).setMaxWidth(70);
        tableMatakuliah.getColumnModel().getColumn(3).setMaxWidth(80);

        JPanel tableCard = buildCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tableMatakuliah) {{
            setBorder(null);
            getViewport().setBackground(TABLE_BG);
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
        info.setForeground(TEXT_MUTED);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnRefresh = buildBtn("Refresh", BLUE, 110);
        btnRefresh.addActionListener(e -> loadJadwal());
        actions.add(btnRefresh);
        if (JwtHelper.getInstance().isAdmin()) {
            JButton btnTambah = buildBtn("Tambah Jadwal", GREEN, 150);
            btnTambah.addActionListener(e -> showJadwalDialog());
            actions.add(btnTambah);
        }

        topCard.add(info, BorderLayout.WEST);
        topCard.add(actions, BorderLayout.EAST);

        String[] columns = {"ID", "Kode MK", "Mata Kuliah", "Hari", "Jam", "Ruangan", "Dosen"};
        jadwalTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        };
        tableJadwal = buildTable(jadwalTableModel);
        tableJadwal.getColumnModel().getColumn(0).setMaxWidth(50);

        JPanel tableCard = buildCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tableJadwal) {{
            setBorder(null);
            getViewport().setBackground(TABLE_BG);
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

    private void refreshAllData() {
        loadMatakuliah();
        loadJadwal();
        loadKrs();
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
                        fillMatakuliahTable(matakuliahCache);
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
                        fillJadwalTable(jadwalCache);
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
        startBusy();

        String nim = txtNimFilter.getText().trim();
        if (JwtHelper.getInstance().isMahasiswa()) {
            nim = JwtHelper.getInstance().getNim();
        }
        String tahunAjaran = txtTahunAjaranFilter.getText().trim();

        final String nimFilter = nim.isBlank() ? null : nim;
        final String tahunAjaranFilter = tahunAjaran.isBlank() ? null : tahunAjaran;

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
                        fillKrsTable(currentKrsCache);
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
            matakuliahTableModel.addRow(new Object[]{
                    safe(mk, "kode_mk"),
                    safe(mk, "nama_mk"),
                    safe(mk, "sks"),
                    safe(mk, "semester")
            });
        }
        lblMatakuliahInfo.setText(data.size() + " mata kuliah tersedia");
    }

    private void fillJadwalTable(JsonArray data) {
        jadwalTableModel.setRowCount(0);
        for (JsonElement element : data) {
            JsonObject jadwal = element.getAsJsonObject();
            jadwalTableModel.addRow(new Object[]{
                    safe(jadwal, "id_jadwal"),
                    safe(jadwal, "kode_mk"),
                    safe(jadwal, "nama_mk"),
                    capitalize(safe(jadwal, "hari")),
                    safe(jadwal, "jam"),
                    safe(jadwal, "ruangan"),
                    safe(jadwal, "dosen")
            });
        }
        lblJadwalInfo.setText(data.size() + " jadwal kuliah tersedia");
        lblJadwalStat.setText(String.valueOf(data.size()));
    }

    private void fillKrsTable(JsonArray data) {
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

        lblKrsInfo.setText(data.size() == 0
                ? "Belum ada data KRS untuk filter aktif."
                : data.size() + " baris KRS ditemukan");
        lblKrsStat.setText(String.valueOf(data.size()));
        lblSksStat.setText(totalSks + " SKS");
    }

    private void showMatakuliahDialog() {
        JDialog dialog = createDialog("Tambah Mata Kuliah", 480, 360);

        JPanel form = buildDialogForm();
        GridBagConstraints g = createFormConstraints();

        JTextField fKode = makeField();
        JTextField fNama = makeField();
        JTextField fSks = makeField();
        JTextField fSemester = makeField();

        int row = 0;
        addFormRow(form, g, row++, "Kode Mata Kuliah *", fKode);
        addFormRow(form, g, row++, "Nama Mata Kuliah *", fNama);
        addFormRow(form, g, row++, "SKS *", fSks);
        addFormRow(form, g, row++, "Semester *", fSemester);

        JButton btnSave = buildBtn("Simpan Mata Kuliah", BLUE, 200);
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.insets = new Insets(18, 4, 4, 4);
        form.add(btnSave, g);

        btnSave.addActionListener(e -> {
            if (isBlank(fKode) || isBlank(fNama) || isBlank(fSks) || isBlank(fSemester)) {
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

            submitDialog(dialog, () -> AkademikService.createMatakuliah(body), this::loadMatakuliah);
        });

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(CARD_BG);
        }});
        dialog.setVisible(true);
    }

    private void showJadwalDialog() {
        if (matakuliahCache.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Data mata kuliah belum tersedia. Muat data atau tambahkan mata kuliah terlebih dahulu.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = createDialog("Tambah Jadwal Kuliah", 520, 420);

        JPanel form = buildDialogForm();
        GridBagConstraints g = createFormConstraints();

        JComboBox<MatakuliahOption> cmbMatakuliah = buildMatakuliahCombo();
        JComboBox<String> cmbHari = new JComboBox<>(new String[]{"senin", "selasa", "rabu", "kamis", "jumat", "sabtu"});
        styleCombo(cmbHari);
        JTextField fJam = makeField();
        fJam.setToolTipText("Contoh: 08:00-10:00");
        JTextField fRuangan = makeField();
        JTextField fDosen = makeField();

        MatakuliahOption selected = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
        if (selected != null && !selected.dosen().isBlank()) {
            fDosen.setText(selected.dosen());
        }
        cmbMatakuliah.addActionListener(e -> {
            MatakuliahOption option = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
            if (option != null && !option.dosen().isBlank()) {
                fDosen.setText(option.dosen());
            }
        });

        int row = 0;
        addFormRow(form, g, row++, "Mata Kuliah *", cmbMatakuliah);
        addFormRow(form, g, row++, "Hari *", cmbHari);
        addFormRow(form, g, row++, "Jam *", fJam);
        addFormRow(form, g, row++, "Ruangan *", fRuangan);
        addFormRow(form, g, row++, "Dosen *", fDosen);

        JButton btnSave = buildBtn("Simpan Jadwal", BLUE, 180);
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

            submitDialog(dialog, () -> AkademikService.createJadwal(body), () -> {
                loadJadwal();
                loadKrs();
            });
        });

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(CARD_BG);
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

        JDialog dialog = createDialog("Input KRS", 520, 390);

        JPanel form = buildDialogForm();
        GridBagConstraints g = createFormConstraints();

        JTextField fNim = makeField();
        if (JwtHelper.getInstance().isMahasiswa()) {
            fNim.setText(JwtHelper.getInstance().getNim());
            fNim.setEnabled(false);
        } else if (!txtNimFilter.getText().trim().isEmpty()) {
            fNim.setText(txtNimFilter.getText().trim());
        }

        JComboBox<MatakuliahOption> cmbMatakuliah = buildMatakuliahCombo();
        JTextField fTahunAjaran = makeField();
        fTahunAjaran.setText(txtTahunAjaranFilter.getText().trim().isEmpty()
                ? defaultAcademicYear()
                : txtTahunAjaranFilter.getText().trim());

        JLabel lblPreview = new JLabel("Pilih mata kuliah untuk melihat ringkasan.");
        lblPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPreview.setForeground(TEXT_MUTED);

        cmbMatakuliah.addActionListener(e -> {
            MatakuliahOption option = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
            if (option != null) {
                lblPreview.setText("SKS " + option.sks() + " | Semester " + option.semester());
            }
        });
        if (cmbMatakuliah.getSelectedItem() != null) {
            MatakuliahOption option = (MatakuliahOption) cmbMatakuliah.getSelectedItem();
            lblPreview.setText("SKS " + option.sks() + " | Semester " + option.semester());
        }

        int row = 0;
        addFormRow(form, g, row++, "NIM *", fNim);
        addFormRow(form, g, row++, "Mata Kuliah *", cmbMatakuliah);
        addFormRow(form, g, row++, "Tahun Ajaran *", fTahunAjaran);
        g.gridx = 1;
        g.gridy = row++;
        form.add(lblPreview, g);

        JButton btnSave = buildBtn("Simpan KRS", BLUE, 160);
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
                    txtNimFilter.setText(fNim.getText().trim());
                }
                txtTahunAjaranFilter.setText(fTahunAjaran.getText().trim());
                loadKrs();
            });
        });

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(CARD_BG);
        }});
        dialog.setVisible(true);
    }

    private void printKrs() {
        if (currentKrsCache.size() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data KRS yang bisa dicetak.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String nim = JwtHelper.getInstance().isMahasiswa()
                ? JwtHelper.getInstance().getNim()
                : txtNimFilter.getText().trim();
        String tahunAjaran = txtTahunAjaranFilter.getText().trim();

        Set<String> uniqueNim = new LinkedHashSet<>();
        Set<String> uniqueTa = new LinkedHashSet<>();
        for (JsonElement element : currentKrsCache) {
            JsonObject item = element.getAsJsonObject();
            uniqueNim.add(safe(item, "nim"));
            uniqueTa.add(safe(item, "tahun_ajaran"));
        }

        if ((nim == null || nim.isBlank()) && uniqueNim.size() == 1) {
            nim = uniqueNim.iterator().next();
        }
        if (tahunAjaran.isBlank() && uniqueTa.size() == 1) {
            tahunAjaran = uniqueTa.iterator().next();
        }

        if (nim == null || nim.isBlank() || tahunAjaran.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Isi filter NIM dan tahun ajaran terlebih dahulu sebelum mencetak KRS.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (uniqueNim.size() > 1 || uniqueTa.size() > 1) {
            JOptionPane.showMessageDialog(this,
                    "Filter KRS harus mengarah ke satu mahasiswa dan satu tahun ajaran sebelum dicetak.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nama = currentKrsSummary != null ? safe(currentKrsSummary, "nama_mahasiswa") : "-";
        int totalSks = currentKrsSummary != null
                ? parseInteger(safe(currentKrsSummary, "total_sks"))
                : calculateTotalSks(currentKrsCache);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan PDF KRS");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Document", "pdf"));
        chooser.setSelectedFile(new File("KRS_" + nim + "_" + tahunAjaran.replace('/', '-') + ".pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            exportKrsPdf(file, nim, nama, tahunAjaran, totalSks);
            JOptionPane.showMessageDialog(this,
                    "KRS berhasil dicetak ke:\n" + file.getAbsolutePath(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mencetak KRS: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportKrsPdf(File file, String nim, String nama, String tahunAjaran, int totalSks) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
        com.itextpdf.text.Font subtitleFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
        com.itextpdf.text.Font headerFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        com.itextpdf.text.Font bodyFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 9, BaseColor.BLACK);

        Paragraph title = new Paragraph("Kartu Rencana Studi (KRS)", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph meta = new Paragraph(
                "NIM: " + nim + "    Nama: " + nama + "    Tahun Ajaran: " + tahunAjaran + "    Total SKS: " + totalSks,
                subtitleFont);
        meta.setSpacingBefore(8f);
        meta.setSpacingAfter(14f);
        document.add(meta);

        PdfPTable table = new PdfPTable(new float[]{1.5f, 3.4f, 0.8f, 2.8f, 1.6f, 2.1f});
        table.setWidthPercentage(100);

        addPdfHeader(table, "Kode MK", headerFont);
        addPdfHeader(table, "Mata Kuliah", headerFont);
        addPdfHeader(table, "SKS", headerFont);
        addPdfHeader(table, "Jadwal", headerFont);
        addPdfHeader(table, "Ruangan", headerFont);
        addPdfHeader(table, "Dosen", headerFont);

        for (JsonElement element : currentKrsCache) {
            JsonObject item = element.getAsJsonObject();
            addPdfCell(table, safe(item, "kode_mk"), bodyFont);
            addPdfCell(table, safe(item, "nama_mk"), bodyFont);
            addPdfCell(table, safe(item, "sks"), bodyFont);
            addPdfCell(table, safe(item, "jadwal"), bodyFont);
            addPdfCell(table, safe(item, "ruangan"), bodyFont);
            addPdfCell(table, safe(item, "dosen"), bodyFont);
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Dokumen dicetak dari modul KRS & Jadwal Kuliah.", subtitleFont));
        document.close();
    }

    private void addPdfHeader(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new BaseColor(30, 41, 70));
        cell.setPadding(8f);
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        table.addCell(cell);
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
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
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
                    component.setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                }
                component.setForeground(TEXT_PRIMARY);
                return component;
            }
        };
        table.setBackground(TABLE_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(59, 130, 246, 60));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(20, 30, 55));
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        left.setForeground(TEXT_PRIMARY);
        table.setDefaultRenderer(Object.class, left);

        return table;
    }

    private JTextField makeField() {
        JTextField field = new JTextField();
        field.setBackground(new Color(13, 19, 38));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(7, 10, 7, 10)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(220, 34));
        return field;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(TEXT_DIM);
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
        combo.setBackground(new Color(13, 19, 38));
        combo.setForeground(TEXT_PRIMARY);
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
        panel.setBackground(CARD_BG);
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

    @FunctionalInterface
    private interface RequestAction {
        JsonObject execute() throws Exception;
    }

    private record MatakuliahOption(String kodeMk, String namaMk, int sks, int semester, String dosen) {
        @Override public String toString() {
            return kodeMk + " - " + namaMk;
        }
    }
}
