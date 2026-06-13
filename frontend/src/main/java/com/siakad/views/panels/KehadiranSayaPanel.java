package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.AkademikService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class KehadiranSayaPanel extends JPanel {
    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private JLabel lblHadir;
    private JLabel lblIzinSakit;
    private JLabel lblAlpha;
    private JLabel lblPersen;

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

    public KehadiranSayaPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        rootPanel.setBackground(BG);
        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(buildContent(), "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);
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
        JLabel title = new JLabel("Kehadiran Saya");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Akademik / Nilai & Absensi / Kehadiran Saya");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);
        JButton refresh = button("Refresh", BLUE);
        refresh.addActionListener(e -> loadData());
        header.add(titleBlock, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 5, 12, 0));
        stats.setOpaque(false);
        lblTotal = statValue("0");
        lblHadir = statValue("0");
        lblIzinSakit = statValue("0");
        lblAlpha = statValue("0");
        lblPersen = statValue("0%");
        stats.add(statCard("ALL", "Total Catatan", lblTotal));
        stats.add(statCard("H", "Hadir", lblHadir));
        stats.add(statCard("I/S", "Izin + Sakit", lblIzinSakit));
        stats.add(statCard("A", "Alpha", lblAlpha));
        stats.add(statCard("%", "Persentase Hadir", lblPersen));

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

        JLabel title = new JLabel("Riwayat Kehadiran Per Mata Kuliah");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        tableModel = new DefaultTableModel(new String[]{
                "Tahun Ajaran", "Tanggal", "Pertemuan", "Kode", "Mata Kuliah", "SKS", "Semester", "Status", "Keterangan"
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
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
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

    private void loadData() {
        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception { return AkademikService.getKehadiranSaya(); }
            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) throw new Exception(response.get("message").getAsString());
                    fill(response);
                    rootCard.show(rootPanel, "content");
                } catch (Exception e) {
                    statePanel.showState("!", "Gagal memuat kehadiran", e.getMessage(), "Muat ulang", KehadiranSayaPanel.this::loadData);
                    rootCard.show(rootPanel, "state");
                }
            }
        }.execute();
    }

    private void fill(JsonObject response) {
        tableModel.setRowCount(0);
        JsonObject summary = response.getAsJsonObject("summary");
        int izin = number(summary, "izin");
        int sakit = number(summary, "sakit");
        lblTotal.setText(s(summary, "total_pertemuan"));
        lblHadir.setText(s(summary, "hadir"));
        lblIzinSakit.setText(String.valueOf(izin + sakit));
        lblAlpha.setText(s(summary, "alpha"));
        lblPersen.setText(percent(summary, "persentase_hadir"));

        JsonArray data = response.getAsJsonArray("data");
        for (JsonElement el : data) {
            JsonObject o = el.getAsJsonObject();
            tableModel.addRow(new Object[]{
                    s(o, "tahun_ajaran"),
                    dateOnly(s(o, "tanggal")),
                    s(o, "pertemuan").isBlank() ? "-" : s(o, "pertemuan"),
                    s(o, "kode_mk"),
                    s(o, "nama_mk"),
                    s(o, "sks"),
                    s(o, "semester"),
                    capitalize(s(o, "status")),
                    s(o, "keterangan").isBlank() ? "-" : s(o, "keterangan")
            });
        }
    }

    private int number(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
    }

    private String percent(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull()
                ? String.format(java.util.Locale.US, "%.2f%%", o.get(key).getAsDouble())
                : "0%";
    }

    private String s(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private String dateOnly(String value) {
        return value != null && value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) return "-";
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
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

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            String status = String.valueOf(v).toLowerCase();
            if (status.contains("hadir")) label.setForeground(GREEN);
            else if (status.contains("izin") || status.contains("sakit")) label.setForeground(AMBER);
            else label.setForeground(RED);
            return label;
        }
    }
}
