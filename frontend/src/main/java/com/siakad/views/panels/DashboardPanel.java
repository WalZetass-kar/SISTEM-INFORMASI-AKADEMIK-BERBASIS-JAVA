package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.PembayaranService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DashboardPanel - Statistik & Ringkasan Pembayaran
 */
public class DashboardPanel extends JPanel {

    private JLabel lblTotalPendapatan, lblLunas, lblPending, lblGagal;
    private BarChartPanel chartPanel;
    private DonutChartPanel donutPanel;
    private JComboBox<String> cmbTahunAjaran;
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // Skeleton
    private CardLayout rootCard;
    private JPanel rootPanel;
    private SkeletonPanel skeleton;

    // Warna tema
    private static final Color BG           = new Color(13, 19, 38);
    private static final Color CARD_BG      = new Color(18, 26, 48);
    private static final Color BORDER_COLOR = new Color(25, 36, 65);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_MUTED   = new Color(148, 163, 184);
    private static final Color TEXT_DIM     = new Color(71, 85, 105);
    private static final Color BLUE         = new Color(59, 130, 246);
    private static final Color GREEN        = new Color(34, 197, 94);
    private static final Color YELLOW       = new Color(234, 179, 8);
    private static final Color RED          = new Color(239, 68, 68);

    public DashboardPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());

        rootCard = new CardLayout();
        rootPanel = new JPanel(rootCard);
        rootPanel.setBackground(BG);

        skeleton = new SkeletonPanel(SkeletonPanel.Type.DASHBOARD);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        initUI(content);

        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(content, "content");
        add(rootPanel, BorderLayout.CENTER);

        loadData();
    }

    private void initUI(JPanel target) {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 20, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Ringkasan statistik sistem pembayaran UKT");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JLabel lblTA = new JLabel("Tahun Ajaran:");
        lblTA.setForeground(TEXT_MUTED);
        lblTA.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cmbTahunAjaran = new JComboBox<>(new String[]{"Semua", "2024/2025", "2023/2024", "2022/2023"});
        styleCombo(cmbTahunAjaran);
        cmbTahunAjaran.addActionListener(e -> loadData());

        JButton btnRefresh = buildIconBtn("🔄  Refresh", CARD_BG);
        btnRefresh.addActionListener(e -> loadData());

        controls.add(lblTA);
        controls.add(cmbTahunAjaran);
        controls.add(btnRefresh);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        // ── Stat Cards ──
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(0, 28, 20, 28));

        lblTotalPendapatan = new JLabel("Rp 0");
        lblLunas           = new JLabel("0");
        lblPending         = new JLabel("0");
        lblGagal           = new JLabel("0");

        statsRow.add(buildStatCard("💰", "Total Pendapatan", lblTotalPendapatan, BLUE,   "Akumulasi pembayaran lunas"));
        statsRow.add(buildStatCard("✅", "Lunas",            lblLunas,           GREEN,  "Transaksi berhasil"));
        statsRow.add(buildStatCard("⏳", "Pending",          lblPending,         YELLOW, "Menunggu verifikasi"));
        statsRow.add(buildStatCard("❌", "Gagal / Refund",   lblGagal,           RED,    "Transaksi bermasalah"));

        // ── Charts Row ──
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsRow.setOpaque(false);
        chartsRow.setBorder(new EmptyBorder(0, 28, 28, 28));

        chartPanel = new BarChartPanel();
        donutPanel = new DonutChartPanel();

        chartsRow.add(buildChartCard("📊  Pembayaran per Bulan", chartPanel));
        chartsRow.add(buildChartCard("🍩  Distribusi Status", donutPanel));

        // ── Body ──
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(statsRow);
        body.add(chartsRow);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        target.add(header, BorderLayout.NORTH);
        target.add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildStatCard(String icon, String title, JLabel valueLabel, Color accent, String subtitle) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                // Top accent bar
                GradientPaint gp = new GradientPaint(0, 0, accent,
                        getWidth(), 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Icon circle
        JLabel lblIcon = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
                g2.fillOval(0, 0, 40, 40);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
        };
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);
        lblIcon.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(TEXT_DIM);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblSub);

        card.add(lblIcon, BorderLayout.EAST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildChartCard(String title, JPanel chart) {
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
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(0, 280));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_PRIMARY);

        chart.setOpaque(false);
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private void loadData() {
        String ta = cmbTahunAjaran.getSelectedIndex() == 0 ? null : (String) cmbTahunAjaran.getSelectedItem();
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return PembayaranService.getDashboardStats(ta);
            }
            @Override protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        JsonObject data = resp.getAsJsonObject("data");
                        JsonObject ring = data.getAsJsonObject("ringkasan");

                        lblTotalPendapatan.setText(RUPIAH.format(ring.get("total_pendapatan").getAsDouble()));
                        lblLunas.setText(String.valueOf(ring.get("lunas").getAsInt()));
                        lblPending.setText(String.valueOf(ring.get("pending").getAsInt()));
                        lblGagal.setText(String.valueOf(ring.get("gagal").getAsInt()));

                        // Bar chart data
                        JsonArray bulanan = data.getAsJsonArray("chart_pendapatan_bulanan");
                        int[] vals = new int[bulanan.size()];
                        String[] lbls = new String[bulanan.size()];
                        for (int i = 0; i < bulanan.size(); i++) {
                            JsonObject b = bulanan.get(i).getAsJsonObject();
                            vals[i] = b.get("total_transaksi").getAsInt();
                            lbls[i] = b.get("bulan").getAsString();
                        }
                        chartPanel.setData(vals, lbls);

                        // Donut chart data
                        int lunas   = ring.get("lunas").getAsInt();
                        int pending = ring.get("pending").getAsInt();
                        int gagal   = ring.get("gagal").getAsInt();
                        donutPanel.setData(
                            new int[]{lunas, pending, gagal},
                            new String[]{"Lunas", "Pending", "Gagal"},
                            new Color[]{GREEN, YELLOW, RED}
                        );
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ── Bar Chart ──────────────────────────────────────────────────────────────
    static class BarChartPanel extends JPanel {
        private int[] values = {};
        private String[] labels = {};
        private static final Color[] COLORS = {
            new Color(59,130,246), new Color(99,102,241), new Color(34,211,238),
            new Color(34,197,94),  new Color(234,179,8),  new Color(239,68,68),
            new Color(168,85,247), new Color(236,72,153), new Color(249,115,22),
            new Color(20,184,166), new Color(132,204,22), new Color(251,191,36)
        };

        public void setData(int[] v, String[] l) { values = v; labels = l; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (values.length == 0) {
                g2.setColor(new Color(71, 85, 105));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String msg = "Memuat data...";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose(); return;
            }

            int max = 0;
            for (int v : values) max = Math.max(max, v);
            if (max == 0) { g2.dispose(); return; }

            int padL = 36, padR = 12, padT = 20, padB = 36;
            int chartW = getWidth() - padL - padR;
            int chartH = getHeight() - padT - padB;
            int n = values.length;
            int barW = Math.max(8, chartW / n - 6);

            // Grid lines
            g2.setColor(new Color(255, 255, 255, 8));
            for (int i = 1; i <= 4; i++) {
                int y = padT + chartH - (chartH * i / 4);
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setColor(new Color(71, 85, 105));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.valueOf(max * i / 4), 2, y + 4);
                g2.setColor(new Color(255, 255, 255, 8));
            }

            // Bars
            for (int i = 0; i < n; i++) {
                int barH = (int) ((double) values[i] / max * chartH);
                int x = padL + i * (chartW / n) + (chartW / n - barW) / 2;
                int y = padT + chartH - barH;

                // Bar shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(x + 2, y + 2, barW, barH, 6, 6);

                // Bar gradient
                Color c = COLORS[i % COLORS.length];
                GradientPaint gp = new GradientPaint(x, y, c,
                        x, y + barH, new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                // Value on top
                g2.setColor(new Color(203, 213, 225));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String val = String.valueOf(values[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 4);

                // Label below
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                if (i < labels.length) {
                    String lbl = labels[i].length() > 6 ? labels[i].substring(0, 5) + "…" : labels[i];
                    fm = g2.getFontMetrics();
                    g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, getHeight() - 6);
                }
            }

            // X axis line
            g2.setColor(new Color(30, 41, 70));
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            g2.dispose();
        }
    }

    // ── Donut Chart ────────────────────────────────────────────────────────────
    static class DonutChartPanel extends JPanel {
        private int[] values = {};
        private String[] labels = {};
        private Color[] colors = {};

        public void setData(int[] v, String[] l, Color[] c) { values = v; labels = l; colors = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (values.length == 0) { g2.dispose(); return; }

            int total = 0;
            for (int v : values) total += v;
            if (total == 0) {
                g2.setColor(new Color(71, 85, 105));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString("Tidak ada data", getWidth() / 2 - 50, getHeight() / 2);
                g2.dispose(); return;
            }

            int size = Math.min(getWidth() / 2 - 20, getHeight() - 60);
            int x = (getWidth() / 2 - size) / 2;
            int y = (getHeight() - size) / 2;
            int hole = (int)(size * 0.55);
            int hx = x + (size - hole) / 2;
            int hy = y + (size - hole) / 2;

            double startAngle = -90;
            for (int i = 0; i < values.length; i++) {
                double sweep = (double) values[i] / total * 360;
                g2.setColor(colors[i]);
                g2.fillArc(x, y, size, size, (int) startAngle, (int) sweep);
                startAngle += sweep;
            }

            // Hole
            g2.setColor(new Color(18, 26, 48));
            g2.fillOval(hx, hy, hole, hole);

            // Center text
            g2.setColor(new Color(248, 250, 252));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            String totalStr = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(totalStr, hx + (hole - fm.stringWidth(totalStr)) / 2, hy + hole / 2 + 4);
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String sub = "Total";
            fm = g2.getFontMetrics();
            g2.drawString(sub, hx + (hole - fm.stringWidth(sub)) / 2, hy + hole / 2 + 16);

            // Legend
            int lx = getWidth() / 2 + 10;
            int ly = (getHeight() - values.length * 28) / 2;
            for (int i = 0; i < values.length; i++) {
                g2.setColor(colors[i]);
                g2.fillRoundRect(lx, ly + i * 28, 12, 12, 4, 4);
                g2.setColor(new Color(203, 213, 225));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(labels[i], lx + 18, ly + i * 28 + 11);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                int pct = (int) Math.round((double) values[i] / total * 100);
                g2.drawString(values[i] + "  (" + pct + "%)", lx + 18, ly + i * 28 + 24);
            }

            g2.dispose();
        }
    }

    private JButton buildIconBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(CARD_BG);
        c.setForeground(TEXT_MUTED);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setPreferredSize(new Dimension(150, 34));
    }
}
