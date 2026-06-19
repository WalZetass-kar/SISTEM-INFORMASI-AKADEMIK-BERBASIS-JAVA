package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.siakad.services.NilaiService;
import com.siakad.utils.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class NilaiSayaPanel extends JPanel {
    private final CardLayout rootCard = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCard);
    private final SkeletonPanel skeleton = new SkeletonPanel(SkeletonPanel.Type.TABLE);
    private final StatePanel statePanel = new StatePanel();
    private DefaultTableModel tableModel;
    private JLabel lblMk;
    private JLabel lblSks;
    private JLabel lblRata;

    private static Color BG() { return AppTheme.bg(); }
    private static Color CARD() { return AppTheme.card(); }
    private static Color TABLE() { return AppTheme.table(); }
    private static Color ROW_ALT() { return AppTheme.rowAlt(); }
    private static Color BORDER() { return AppTheme.border(); }
    private static Color TEXT() { return AppTheme.text(); }
    private static Color MUTED() { return AppTheme.muted(); }
    private static Color BLUE() { return AppTheme.blue(); }
    private static Color GREEN() { return AppTheme.green(); }
    private static Color AMBER() { return AppTheme.yellow(); }
    private static Color RED() { return AppTheme.red(); }

    public NilaiSayaPanel() {
        setBackground(BG());
        setLayout(new BorderLayout());
        rootPanel.setBackground(BG());
        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(buildContent(), "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(BG());
        content.setBorder(new EmptyBorder(26, 28, 14, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Nilai Saya");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT());
        JLabel subtitle = new JLabel("Akademik / Nilai & Absensi / Nilai Saya");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED());
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);
        JButton refresh = button("Refresh", BLUE());
        refresh.addActionListener(e -> loadData());
        header.add(titleBlock, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        lblMk = statValue("0");
        lblSks = statValue("0");
        lblRata = statValue("0.00");
        stats.add(statCard("MK", "Total Mata Kuliah", lblMk));
        stats.add(statCard("SKS", "Total SKS", lblSks));
        stats.add(statCard("AVG", "Rata-rata Nilai", lblRata));

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

        JLabel title = new JLabel("Daftar Nilai Akademik");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT());
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        tableModel = new DefaultTableModel(new String[]{
                "Tahun Ajaran", "Kode", "Mata Kuliah", "SKS", "Semester", "Tugas", "UTS", "UAS", "Akhir", "Grade", "Status"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? TABLE() : ROW_ALT());
                return c;
            }
        };
        styleTable(table);
        table.getColumnModel().getColumn(9).setCellRenderer(new GradeRenderer());
        table.getColumnModel().getColumn(10).setCellRenderer(new StatusRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TABLE());

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel statCard(String icon, String label, JLabel value) {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI", Font.BOLD, 20));
        ic.setForeground(BLUE());
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED());
        text.add(value);
        text.add(Box.createVerticalStrut(3));
        text.add(l);
        panel.add(ic, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JLabel statValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(TEXT());
        return label;
    }

    private void loadData() {
        rootCard.show(rootPanel, "skeleton");
        skeleton.start();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception { return NilaiService.getMyNilai(); }
            @Override protected void done() {
                skeleton.stop();
                try {
                    JsonObject response = get();
                    if (!response.get("success").getAsBoolean()) throw new Exception(response.get("message").getAsString());
                    fill(response);
                    rootCard.show(rootPanel, "content");
                } catch (Exception e) {
                    statePanel.showState("!", "Gagal memuat nilai", e.getMessage(), "Muat ulang", NilaiSayaPanel.this::loadData);
                    rootCard.show(rootPanel, "state");
                }
            }
        }.execute();
    }

    private void fill(JsonObject response) {
        tableModel.setRowCount(0);
        JsonObject summary = response.getAsJsonObject("summary");
        lblMk.setText(s(summary, "total_mata_kuliah"));
        lblSks.setText(s(summary, "total_sks"));
        lblRata.setText(s(summary, "rata_rata"));

        JsonArray data = response.getAsJsonArray("data");
        for (JsonElement el : data) {
            JsonObject o = el.getAsJsonObject();
            String grade = s(o, "grade");
            tableModel.addRow(new Object[]{
                    s(o, "tahun_ajaran"),
                    s(o, "kode_mk"),
                    s(o, "nama_mk"),
                    s(o, "sks"),
                    s(o, "semester"),
                    score(o, "nilai_tugas"),
                    score(o, "nilai_uts"),
                    score(o, "nilai_uas"),
                    score(o, "nilai_akhir"),
                    grade,
                    status(grade)
            });
        }
    }

    private String status(String grade) {
        if ("A".equals(grade) || "B".equals(grade) || "C".equals(grade)) return "Lulus";
        if ("D".equals(grade) || "E".equals(grade)) return "Tidak Lulus";
        return "Belum Dinilai";
    }

    private String score(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? String.format(java.util.Locale.US, "%.2f", o.get(key).getAsDouble()) : "-";
    }

    private String s(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private void styleTable(JTable table) {
        table.setBackground(TABLE());
        table.setForeground(TEXT());
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(38);
        table.setGridColor(BORDER());
        table.setSelectionBackground(new Color(37, 99, 235, 80));
        table.setSelectionForeground(TEXT());
        table.setShowVerticalLines(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(AppTheme.header());
        header.setForeground(MUTED());
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER());
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
        btn.setForeground(TEXT());
        btn.setBackground(color);
        btn.setBorder(new EmptyBorder(9, 14, 9, 14));
        btn.setFocusPainted(false);
        return btn;
    }

    private class GradeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String grade = String.valueOf(v);
            label.setForeground("A".equals(grade) || "B".equals(grade) ? GREEN() : ("C".equals(grade) ? AMBER() : RED()));
            return label;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground("Lulus".equals(String.valueOf(v)) ? GREEN() : RED());
            return label;
        }
    }
}
