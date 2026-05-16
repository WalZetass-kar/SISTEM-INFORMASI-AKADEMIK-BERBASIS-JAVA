package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.PembayaranService;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DashboardPanel - Statistik & Ringkasan Pembayaran
 * Menampilkan: total pendapatan, chart status, chart bulanan
 */
public class DashboardPanel extends JPanel {

    private JLabel lblTotalPendapatan, lblLunas, lblPending, lblGagal, lblPersentase;
    private JPanel chartPanel;
    private JComboBox<String> cmbTahunAjaran;
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public DashboardPanel() {
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

        JLabel lblTitle = new JLabel("Dashboard Pembayaran");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(248, 250, 252));

        JLabel lblSub = new JLabel("Ringkasan statistik sistem pembayaran UKT");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(lblTitle);
        titleBlock.add(lblSub);

        // Tahun ajaran filter
        cmbTahunAjaran = new JComboBox<>(new String[]{"Semua", "2024/2025", "2023/2024", "2022/2023"});
        cmbTahunAjaran.setBackground(new Color(30, 41, 59));
        cmbTahunAjaran.setForeground(new Color(203, 213, 225));
        cmbTahunAjaran.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTahunAjaran.setPreferredSize(new Dimension(150, 36));
        cmbTahunAjaran.addActionListener(e -> loadData());

        JButton btnRefresh = createIconButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadData());

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);
        rightControls.add(new JLabel("Tahun Ajaran:") {{ setForeground(new Color(148,163,184)); setFont(new Font("Segoe UI", Font.PLAIN, 12)); }});
        rightControls.add(cmbTahunAjaran);
        rightControls.add(btnRefresh);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);

        // Stats Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(0, 28, 20, 28));

        lblTotalPendapatan = new JLabel("Rp 0");
        lblLunas            = new JLabel("0");
        lblPending          = new JLabel("0");
        lblGagal            = new JLabel("0");
        lblPersentase       = new JLabel("0%");

        statsPanel.add(createStatCard("💰 Total Pendapatan", lblTotalPendapatan, new Color(59, 130, 246), "Pembayaran Lunas"));
        statsPanel.add(createStatCard("✅ Lunas",            lblLunas,            new Color(34, 197, 94),  "Transaksi Berhasil"));
        statsPanel.add(createStatCard("⏳ Pending",          lblPending,          new Color(234, 179, 8),  "Menunggu Verifikasi"));
        statsPanel.add(createStatCard("❌ Gagal/Refund",     lblGagal,            new Color(239, 68, 68),  "Transaksi Bermasalah"));

        // Chart placeholder (simple bar chart drawn manually)
        chartPanel = new JPanel() {
            private int[] values = {};
            private String[] labels = {};
            public void setData(int[] v, String[] l) { values = v; labels = l; repaint(); }

            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (values.length == 0) {
                    g2.setColor(new Color(100, 116, 139));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2.drawString("Memuat data...", getWidth() / 2 - 50, getHeight() / 2);
                    return;
                }

                int max = 0;
                for (int v : values) max = Math.max(max, v);
                if (max == 0) return;

                int pad = 40, barW = Math.min(60, (getWidth() - 2 * pad) / Math.max(values.length, 1) - 8);
                int chartH = getHeight() - 80;

                Color[] colors = {new Color(59,130,246), new Color(34,197,94), new Color(234,179,8),
                        new Color(239,68,68), new Color(168,85,247), new Color(236,72,153)};

                for (int i = 0; i < values.length; i++) {
                    int barH = (int) ((double) values[i] / max * chartH);
                    int x = pad + i * (barW + 8);
                    int y = chartH - barH + 20;

                    // Bar gradient
                    g2.setColor(colors[i % colors.length]);
                    g2.fillRoundRect(x, y, barW, barH, 6, 6);

                    // Value on top
                    g2.setColor(new Color(203, 213, 225));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String val = String.valueOf(values[i]);
                    g2.drawString(val, x + barW / 2 - g2.getFontMetrics().stringWidth(val) / 2, y - 4);

                    // Label below
                    g2.setColor(new Color(100, 116, 139));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    if (i < labels.length) {
                        String lbl = labels[i].length() > 10 ? labels[i].substring(0, 9) + "…" : labels[i];
                        g2.drawString(lbl, x + barW / 2 - g2.getFontMetrics().stringWidth(lbl) / 2, getHeight() - 12);
                    }
                }

                // Y-axis label
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString("Jumlah Transaksi", 4, 16);
            }
        };
        chartPanel.setBackground(new Color(22, 33, 54));
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 41, 59), 1),
                new EmptyBorder(16, 16, 16, 16)));

        // Chart section
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setBorder(new EmptyBorder(0, 28, 28, 28));

        // Left: bar chart
        JPanel chartCard = createCard("📊 Pembayaran per Bulan");
        chartCard.setLayout(new BorderLayout());
        chartCard.add(chartPanel, BorderLayout.CENTER);

        // Right: status summary
        JPanel statusCard = createCard("📈 Ringkasan Status");
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));

        chartsRow.add(chartCard);
        chartsRow.add(statusCard);

        // Assemble
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(statsPanel);
        body.add(chartsRow);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(body) {{ setBorder(null); setOpaque(false); getViewport().setOpaque(false); }}, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String subtitle) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(22, 33, 54));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Left accent bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 16));
        card.setPreferredSize(new Dimension(0, 100));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(148, 163, 184));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(248, 250, 252));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(100, 116, 139));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        return card;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(22, 33, 54));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(203, 213, 225));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private JButton createIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(30, 41, 59));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private void loadData() {
        String ta = cmbTahunAjaran.getSelectedIndex() == 0 ? null
                : (String) cmbTahunAjaran.getSelectedItem();

        SwingWorker<JsonObject, Void> worker = new SwingWorker<>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return PembayaranService.getDashboardStats(ta);
            }
            @Override protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        JsonObject data = resp.getAsJsonObject("data");
                        JsonObject ring = data.getAsJsonObject("ringkasan");

                        double totalPendapatan = ring.get("total_pendapatan").getAsDouble();
                        int lunas   = ring.get("lunas").getAsInt();
                        int pending = ring.get("pending").getAsInt();
                        int gagal   = ring.get("gagal").getAsInt();
                        String pct  = ring.get("persentase_lunas").getAsString();

                        lblTotalPendapatan.setText(RUPIAH.format(totalPendapatan));
                        lblLunas.setText(String.valueOf(lunas));
                        lblPending.setText(String.valueOf(pending));
                        lblGagal.setText(String.valueOf(gagal));
                        lblPersentase.setText(pct + "%");

                        // Chart data
                        JsonArray bulanan = data.getAsJsonArray("chart_pendapatan_bulanan");
                        int[] vals = new int[bulanan.size()];
                        String[] lbls = new String[bulanan.size()];
                        for (int i = 0; i < bulanan.size(); i++) {
                            JsonObject b = bulanan.get(i).getAsJsonObject();
                            vals[i] = b.get("total_transaksi").getAsInt();
                            lbls[i] = b.get("bulan").getAsString();
                        }
                        try { chartPanel.getClass().getMethod("setData", int[].class, String[].class)
                                .invoke(chartPanel, vals, lbls); } catch (Exception ignored) {}
                        chartPanel.repaint();
                    }
                } catch (Exception e) {
                    // Show zero state silently
                }
            }
        };
        worker.execute();
    }
}
