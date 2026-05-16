package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.LaporanService;
import com.siakad.utils.ReportExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;

/**
 * LaporanPanel - Generate & Cetak Laporan (Admin only)
 */
public class LaporanPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private int currentPage = 1;

    public LaporanPanel() {
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Laporan & Cetak");
        t.setFont(new Font("Segoe UI", Font.BOLD, 22));
        t.setForeground(new Color(248, 250, 252));
        JLabel s = new JLabel("Generate dan unduh laporan akademik & keuangan");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(new Color(100, 116, 139));
        titleBlock.add(t); titleBlock.add(s);

        // Generate buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnGenPembayaran = makeBtn("📊 Laporan Pembayaran", new Color(59, 130, 246));
        btnGenPembayaran.addActionListener(e -> showGeneratePembayaranDialog());

        JButton btnGenMahasiswa = makeBtn("👨‍🎓 Laporan Mahasiswa", new Color(34, 197, 94));
        btnGenMahasiswa.addActionListener(e -> generateMahasiswa());

        JButton btnGenKeuangan = makeBtn("💰 Laporan Keuangan", new Color(234, 179, 8));
        btnGenKeuangan.addActionListener(e -> showGenerateKeuanganDialog());

        JButton btnRefresh = makeBtn("🔄", new Color(30, 41, 59));
        btnRefresh.addActionListener(e -> loadData());

        actions.add(btnGenPembayaran);
        actions.add(btnGenMahasiswa);
        actions.add(btnGenKeuangan);
        actions.add(btnRefresh);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Judul", "Jenis", "Periode", "Tahun Ajaran", "Records", "Status", "Dibuat", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(8).setMinWidth(150);
        table.getColumnModel().getColumn(8).setMaxWidth(150);
        table.getColumnModel().getColumn(8).setCellRenderer(new LaporanActionRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new LaporanActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(new Color(22, 33, 54));

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(new Color(22, 33, 54));
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 28, 0, 28),
                BorderFactory.createLineBorder(new Color(30, 41, 59), 1)));
        tableCard.add(sp);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 28, 12, 28));
        lblTotal = new JLabel("Total: 0 laporan");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(new Color(100, 116, 139));
        footer.add(lblTotal);

        add(header, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadData() {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return LaporanService.getAll(currentPage, 20, null);
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    tableModel.setRowCount(0);
                    if (resp.get("success").getAsBoolean()) {
                        JsonArray data = resp.getAsJsonArray("data");
                        JsonObject pg = resp.getAsJsonObject("pagination");
                        lblTotal.setText("Total: " + pg.get("total").getAsInt() + " laporan");

                        for (int i = 0; i < data.size(); i++) {
                            JsonObject l = data.get(i).getAsJsonObject();
                            String periode = "";
                            if (l.has("periode_mulai") && !l.get("periode_mulai").isJsonNull())
                                periode = l.get("periode_mulai").getAsString().substring(0, 10);
                            if (l.has("periode_selesai") && !l.get("periode_selesai").isJsonNull())
                                periode += " ~ " + l.get("periode_selesai").getAsString().substring(0, 10);

                            tableModel.addRow(new Object[]{
                                l.get("id").getAsInt(),
                                l.get("judul").getAsString(),
                                l.get("jenis_laporan").getAsString(),
                                periode.isEmpty() ? "-" : periode,
                                l.has("tahun_ajaran") && !l.get("tahun_ajaran").isJsonNull() ? l.get("tahun_ajaran").getAsString() : "-",
                                l.get("total_records").getAsInt(),
                                l.get("status").getAsString(),
                                l.get("created_at").getAsString().substring(0, 10),
                                "aksi"
                            });
                        }
                    }
                } catch (Exception ex) {
                    // silent
                }
            }
        }.execute();
    }

    private void showGeneratePembayaranDialog() {
        JTextField fMulai = makeField("YYYY-MM-DD");
        JTextField fSelesai = makeField("YYYY-MM-DD");
        JTextField fTA = makeField("2024/2025");

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBackground(new Color(22, 33, 54));
        panel.add(lbl("Periode Mulai")); panel.add(fMulai);
        panel.add(lbl("Periode Selesai")); panel.add(fSelesai);
        panel.add(lbl("Tahun Ajaran")); panel.add(fTA);

        int result = JOptionPane.showConfirmDialog(this, panel, "Generate Laporan Pembayaran",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return LaporanService.generatePembayaran(
                        fMulai.getText().trim().isEmpty() ? null : fMulai.getText().trim(),
                        fSelesai.getText().trim().isEmpty() ? null : fSelesai.getText().trim(),
                        fTA.getText().trim().isEmpty() ? null : fTA.getText().trim());
            }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString(),
                            r.get("success").getAsBoolean() ? "Sukses" : "Gagal",
                            r.get("success").getAsBoolean() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void generateMahasiswa() {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.generateMahasiswa(); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString(), "Info", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showGenerateKeuanganDialog() {
        String ta = JOptionPane.showInputDialog(this, "Masukkan Tahun Ajaran (contoh: 2024/2025):", "2024/2025");
        if (ta == null || ta.trim().isEmpty()) return;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.generateKeuangan(ta.trim()); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString());
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void viewDetail(int id) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.getById(id); }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        showDetailDialog(resp.getAsJsonObject("data"));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showDetailDialog(JsonObject d) {
        int id = d.get("id").getAsInt();
        String info = "Judul: " + d.get("judul").getAsString() + "\n"
                + "Jenis: " + d.get("jenis_laporan").getAsString() + "\n"
                + "Total Records: " + d.get("total_records").getAsInt() + "\n"
                + "Status: " + d.get("status").getAsString() + "\n"
                + "Dibuat: " + d.get("created_at").getAsString() + "\n";
        if (d.has("deskripsi") && !d.get("deskripsi").isJsonNull())
            info += "Deskripsi: " + d.get("deskripsi").getAsString();

        JTextArea area = new JTextArea(info);
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setBackground(new Color(22, 33, 54));
        area.setForeground(Color.WHITE);

        JButton btnCetak = makeBtn("🖨 Cetak PDF", new Color(59, 130, 246));
        btnCetak.addActionListener(e -> cetakLaporan(d));

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(22, 33, 54));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        panel.add(btnCetak, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(480, 220));

        JOptionPane.showMessageDialog(this, panel, "Detail Laporan #" + id, JOptionPane.PLAIN_MESSAGE);
    }

    private void cetakLaporan(JsonObject laporan) {
        new SwingWorker<File, Void>() {
            protected File doInBackground() throws Exception {
                return ReportExporter.exportToPdf(laporan, LaporanPanel.this);
            }
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this,
                            "Gagal cetak laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void cetakLaporanById(int id) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.getById(id); }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        cetakLaporan(resp.getAsJsonObject("data"));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void deleteLaporan(int id) {
        int c = JOptionPane.showConfirmDialog(this, "Hapus laporan #" + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.delete(id); }
            protected void done() {
                try { get(); loadData(); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // === Helpers ===
    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(Color.WHITE); b.setBackground(bg);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bg.darker()), new EmptyBorder(7, 14, 7, 14)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setFocusPainted(false);
        return b;
    }
    private JTextField makeField(String hint) {
        JTextField f = new JTextField(14);
        f.setBackground(new Color(30, 41, 59)); f.setForeground(Color.WHITE); f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)), new EmptyBorder(6, 10, 6, 10)));
        f.setToolTipText(hint);
        return f;
    }
    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setForeground(new Color(148, 163, 184)); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); return l; }
    private void styleTable(JTable t) {
        t.setBackground(new Color(22, 33, 54)); t.setForeground(new Color(203, 213, 225));
        t.setSelectionBackground(new Color(37, 99, 235, 80)); t.setGridColor(new Color(30, 41, 59));
        t.setRowHeight(38); t.setFont(new Font("Segoe UI", Font.PLAIN, 12)); t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader(); h.setBackground(new Color(15, 23, 42));
        h.setForeground(new Color(148, 163, 184)); h.setFont(new Font("Segoe UI", Font.BOLD, 11)); h.setReorderingAllowed(false);
    }

    // Action renderer & editor
    class LaporanActionRenderer extends JPanel implements TableCellRenderer {
        JButton b1 = new JButton("👁"), b2 = new JButton("🖨"), b3 = new JButton("🗑");
        LaporanActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 4)); setOpaque(true);
            for (JButton b : new JButton[]{b1, b2, b3}) {
                b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                b.setForeground(Color.WHITE); b.setBorderPainted(false); b.setFocusPainted(false);
            }
            b1.setBackground(new Color(59, 130, 246));
            b2.setBackground(new Color(34, 197, 94));
            b3.setBackground(new Color(239, 68, 68));
            add(b1); add(b2); add(b3);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? new Color(37, 99, 235, 40) : new Color(22, 33, 54)); return this;
        }
    }
    class LaporanActionEditor extends DefaultCellEditor {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 4));
        JButton bView = new JButton("👁"), bPrint = new JButton("🖨"), bDel = new JButton("🗑");
        int rowId;
        LaporanActionEditor() {
            super(new JCheckBox()); panel.setBackground(new Color(22, 33, 54));
            for (JButton b : new JButton[]{bView, bPrint, bDel}) {
                b.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                b.setForeground(Color.WHITE); b.setBorderPainted(false); b.setFocusPainted(false);
            }
            bView.setBackground(new Color(59, 130, 246));
            bPrint.setBackground(new Color(34, 197, 94));
            bDel.setBackground(new Color(239, 68, 68));
            panel.add(bView); panel.add(bPrint); panel.add(bDel);
            bView.addActionListener(e -> { fireEditingStopped(); viewDetail(rowId); });
            bPrint.addActionListener(e -> { fireEditingStopped(); cetakLaporanById(rowId); });
            bDel.addActionListener(e -> { fireEditingStopped(); deleteLaporan(rowId); });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            rowId = (int) t.getValueAt(r, 0); return panel;
        }
        public Object getCellEditorValue() { return "aksi"; }
    }
}
