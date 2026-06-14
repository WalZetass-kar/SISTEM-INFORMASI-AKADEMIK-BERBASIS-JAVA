package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;
import com.siakad.services.NilaiService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class RekapAkademikSayaPanel extends JPanel {
    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();

    private DefaultTableModel tableModel;
    private JComboBox<String> cmbSemester;
    private JLabel lblMk;
    private JLabel lblSks;
    private JLabel lblRataNilai;
    private JLabel lblHadir;
    private JLabel lblPersenHadir;

    private JsonArray nilaiData = new JsonArray();
    private JsonArray kehadiranData = new JsonArray();
    private boolean loadingSemesterOptions = false;

    private static final Color BG = new Color(13, 19, 38);
    private static final Color CARD = new Color(18, 26, 48);
    private static final Color TABLE = new Color(15, 22, 42);
    private static final Color ROW_ALT = new Color(20, 29, 52);
    private static final Color BORDER = new Color(25, 36, 65);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color AMBER = new Color(234, 179, 8);
    private static final Color RED = new Color(239, 68, 68);

    public RekapAkademikSayaPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        rootPanel.setBackground(BG);
        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(buildContent(), "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);
        loadData();
    }

    public void onPanelShown() {
        loadData();
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(26, 28, 14, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Rekap Akademik Saya");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Akademik / Nilai & Absensi / Rekap Akademik Saya");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        cmbSemester = new JComboBox<>();
        styleCombo(cmbSemester, 180);
        cmbSemester.addItem("Semua Semester");

        JButton show = button("Tampilkan", GREEN);
        show.addActionListener(e -> applySemesterFilter());

        JButton refresh = button("Refresh", BLUE);
        refresh.addActionListener(e -> loadData());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        controls.add(labeledField("Semester", cmbSemester));
        controls.add(show);
        controls.add(refresh);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 5, 12, 0));
        stats.setOpaque(false);
        lblMk = statValue("0");
        lblSks = statValue("0");
        lblRataNilai = statValue("0.00");
        lblHadir = statValue("0");
        lblPersenHadir = statValue("0%");
        stats.add(statCard("MK", "Mata Kuliah", lblMk));
        stats.add(statCard("SKS", "Total SKS", lblSks));
        stats.add(statCard("AVG", "Rata Nilai", lblRataNilai));
        stats.add(statCard("H", "Total Hadir", lblHadir));
        stats.add(statCard("%", "Persen Hadir", lblPersenHadir));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);

        content.add(top, BorderLayout.NORTH);
        content.add(buildTableCard(), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildTableCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 18, 18));

        JLabel title = new JLabel("Rekap Nilai dan Kehadiran");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        tableModel = new DefaultTableModel(new String[]{
                "Tahun Ajaran", "Kode", "Mata Kuliah", "SKS", "Semester",
                "Tugas", "UTS", "UAS", "Akhir", "Grade",
                "Hadir", "Izin", "Sakit", "Alpha", "% Hadir"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? TABLE : ROW_ALT);
                return c;
            }
        };
        styleTable(table);
        table.getColumnModel().getColumn(9).setCellRenderer(new GradeRenderer());
        table.getColumnModel().getColumn(14).setCellRenderer(new PercentRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void loadData() {
        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<RekapResponse, Void>() {
            @Override protected RekapResponse doInBackground() throws Exception {
                JsonObject nilaiResponse = NilaiService.getMyNilai();
                JsonObject kehadiranResponse = AkademikService.getKehadiranSaya();
                JsonObject semesterResponse = AkademikService.getSemester();
                return new RekapResponse(nilaiResponse, kehadiranResponse, semesterResponse);
            }

            @Override protected void done() {
                skeleton.stop();
                try {
                    RekapResponse response = get();
                    if (!isSuccess(response.nilaiResponse)) throw new Exception(message(response.nilaiResponse));
                    if (!isSuccess(response.kehadiranResponse)) throw new Exception(message(response.kehadiranResponse));
                    nilaiData = array(response.nilaiResponse, "data");
                    kehadiranData = array(response.kehadiranResponse, "data");
                    populateSemesterOptions(response.semesterResponse);
                    applySemesterFilter();
                    rootCard.show(rootPanel, "content");
                } catch (Exception e) {
                    statePanel.showState("!", "Gagal memuat rekap akademik", e.getMessage(), "Muat ulang", RekapAkademikSayaPanel.this::loadData);
                    rootCard.show(rootPanel, "state");
                }
            }
        }.execute();
    }

    private void populateSemesterOptions(JsonObject semesterResponse) {
        loadingSemesterOptions = true;
        Object previous = cmbSemester.getSelectedItem();
        cmbSemester.removeAllItems();
        cmbSemester.addItem("Semua Semester");

        if (semesterResponse != null && semesterResponse.has("success") && semesterResponse.get("success").getAsBoolean()) {
            JsonArray data = semesterResponse.getAsJsonArray("data");
            for (JsonElement el : data) {
                JsonObject o = el.getAsJsonObject();
                int nomor = number(o, "nomor");
                String nama = s(o, "nama_semester");
                if (nomor > 0) {
                    cmbSemester.addItem(nomor + " - " + (nama.isBlank() ? "Semester " + nomor : nama));
                }
            }
        }

        if (previous != null) cmbSemester.setSelectedItem(previous);
        if (cmbSemester.getSelectedItem() == null) cmbSemester.setSelectedIndex(0);
        loadingSemesterOptions = false;
    }

    private void applySemesterFilter() {
        if (loadingSemesterOptions) return;
        tableModel.setRowCount(0);
        int selectedSemester = selectedSemester();

        Map<String, RekapRow> rows = buildRows(selectedSemester);
        int totalMk = 0;
        int totalSks = 0;
        double totalNilai = 0;
        int nilaiCount = 0;
        int totalHadir = 0;
        int totalPertemuan = 0;

        for (RekapRow row : rows.values()) {
            totalMk++;
            totalSks += row.sks;
            if (row.hasNilai) {
                totalNilai += row.nilaiAkhir;
                nilaiCount++;
            }
            totalHadir += row.hadir;
            totalPertemuan += row.totalKehadiran();

            tableModel.addRow(new Object[]{
                    row.tahunAjaran,
                    row.kodeMk,
                    row.namaMk,
                    row.sks == 0 ? "-" : row.sks,
                    row.semester == 0 ? "-" : row.semester,
                    row.hasNilai ? score(row.nilaiTugas) : "-",
                    row.hasNilai ? score(row.nilaiUts) : "-",
                    row.hasNilai ? score(row.nilaiUas) : "-",
                    row.hasNilai ? score(row.nilaiAkhir) : "-",
                    row.grade.isBlank() ? "-" : row.grade,
                    row.hadir,
                    row.izin,
                    row.sakit,
                    row.alpha,
                    row.totalKehadiran() > 0 ? String.format(Locale.US, "%.2f%%", row.persenHadir()) : "-"
            });
        }

        lblMk.setText(String.valueOf(totalMk));
        lblSks.setText(String.valueOf(totalSks));
        lblRataNilai.setText(nilaiCount > 0 ? String.format(Locale.US, "%.2f", totalNilai / nilaiCount) : "0.00");
        lblHadir.setText(String.valueOf(totalHadir));
        lblPersenHadir.setText(totalPertemuan > 0 ? String.format(Locale.US, "%.2f%%", totalHadir * 100.0 / totalPertemuan) : "0%");
    }

    private Map<String, RekapRow> buildRows(int selectedSemester) {
        Map<String, RekapRow> rows = new LinkedHashMap<>();
        for (JsonElement el : nilaiData) {
            JsonObject o = el.getAsJsonObject();
            int semester = number(o, "semester");
            if (selectedSemester > 0 && semester != selectedSemester) continue;

            RekapRow row = rows.computeIfAbsent(key(o), ignored -> new RekapRow());
            row.tahunAjaran = s(o, "tahun_ajaran");
            row.kodeMk = s(o, "kode_mk");
            row.namaMk = s(o, "nama_mk");
            row.sks = number(o, "sks");
            row.semester = semester;
            row.nilaiTugas = doubleNumber(o, "nilai_tugas");
            row.nilaiUts = doubleNumber(o, "nilai_uts");
            row.nilaiUas = doubleNumber(o, "nilai_uas");
            row.nilaiAkhir = doubleNumber(o, "nilai_akhir");
            row.grade = s(o, "grade");
            row.hasNilai = true;
        }

        for (JsonElement el : kehadiranData) {
            JsonObject o = el.getAsJsonObject();
            int semester = number(o, "semester");
            if (selectedSemester > 0 && semester != selectedSemester) continue;

            RekapRow row = rows.computeIfAbsent(key(o), ignored -> new RekapRow());
            if (row.tahunAjaran.isBlank()) row.tahunAjaran = s(o, "tahun_ajaran");
            if (row.kodeMk.isBlank()) row.kodeMk = s(o, "kode_mk");
            if (row.namaMk.isBlank()) row.namaMk = s(o, "nama_mk");
            if (row.sks == 0) row.sks = number(o, "sks");
            if (row.semester == 0) row.semester = semester;

            String status = s(o, "status").toLowerCase(Locale.ROOT);
            if ("hadir".equals(status)) row.hadir++;
            else if ("izin".equals(status)) row.izin++;
            else if ("sakit".equals(status)) row.sakit++;
            else if ("alpha".equals(status) || "alpa".equals(status)) row.alpha++;
        }
        return rows;
    }

    private String key(JsonObject o) {
        return s(o, "tahun_ajaran") + "|" + s(o, "kode_mk") + "|" + number(o, "semester");
    }

    private boolean isSuccess(JsonObject response) {
        return response != null && response.has("success") && response.get("success").getAsBoolean();
    }

    private String message(JsonObject response) {
        return response != null && response.has("message") && !response.get("message").isJsonNull()
                ? response.get("message").getAsString()
                : "Response server tidak valid";
    }

    private JsonArray array(JsonObject response, String key) {
        return response != null && response.has(key) && response.get(key).isJsonArray()
                ? response.getAsJsonArray(key)
                : new JsonArray();
    }

    private int selectedSemester() {
        Object selected = cmbSemester.getSelectedItem();
        if (selected == null) return 0;
        String text = String.valueOf(selected).trim();
        if ("Semua Semester".equalsIgnoreCase(text)) return 0;
        try {
            return Integer.parseInt(text.split("\\D+")[0]);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String score(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private int number(JsonObject o, String key) {
        try {
            return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
        } catch (Exception ex) {
            return 0;
        }
    }

    private double doubleNumber(JsonObject o, String key) {
        try {
            return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : 0;
        } catch (Exception ex) {
            return 0;
        }
    }

    private String s(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
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

    private JPanel statCard(String icon, String label, JLabel value) {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ic.setForeground(BLUE);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED);
        text.add(value);
        text.add(Box.createVerticalStrut(3));
        text.add(l);
        panel.add(ic, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JLabel statValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 21));
        label.setForeground(TEXT);
        return label;
    }

    private void styleCombo(JComboBox<?> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 38));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(CARD);
        combo.setForeground(TEXT);
    }

    private void styleTable(JTable table) {
        table.setBackground(TABLE);
        table.setForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(38);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(37, 99, 235, 80));
        table.setSelectionForeground(TEXT);
        table.setShowVerticalLines(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(10, 15, 30));
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT);
        btn.setBackground(color);
        btn.setBorder(new EmptyBorder(9, 14, 9, 14));
        btn.setFocusPainted(false);
        return btn;
    }

    private static class RekapResponse {
        private final JsonObject nilaiResponse;
        private final JsonObject kehadiranResponse;
        private final JsonObject semesterResponse;

        private RekapResponse(JsonObject nilaiResponse, JsonObject kehadiranResponse, JsonObject semesterResponse) {
            this.nilaiResponse = nilaiResponse;
            this.kehadiranResponse = kehadiranResponse;
            this.semesterResponse = semesterResponse;
        }
    }

    private static class RekapRow {
        private String tahunAjaran = "";
        private String kodeMk = "";
        private String namaMk = "";
        private int sks;
        private int semester;
        private double nilaiTugas;
        private double nilaiUts;
        private double nilaiUas;
        private double nilaiAkhir;
        private String grade = "";
        private boolean hasNilai;
        private int hadir;
        private int izin;
        private int sakit;
        private int alpha;

        private int totalKehadiran() {
            return hadir + izin + sakit + alpha;
        }

        private double persenHadir() {
            return totalKehadiran() > 0 ? hadir * 100.0 / totalKehadiran() : 0;
        }
    }

    private class GradeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String grade = String.valueOf(v);
            if ("A".equals(grade) || "B".equals(grade)) label.setForeground(GREEN);
            else if ("C".equals(grade)) label.setForeground(AMBER);
            else if ("-".equals(grade)) label.setForeground(MUTED);
            else label.setForeground(RED);
            return label;
        }
    }

    private class PercentRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            String value = String.valueOf(v).replace("%", "");
            try {
                double percent = Double.parseDouble(value);
                label.setForeground(percent >= 75 ? GREEN : (percent >= 50 ? AMBER : RED));
            } catch (Exception ex) {
                label.setForeground(MUTED);
            }
            return label;
        }
    }
}
