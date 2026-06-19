package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.PembayaranService;
import com.siakad.services.MahasiswaService;
import com.siakad.utils.AppTheme;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private JLabel lblTotalPendapatan, lblLunas, lblPending, lblGagal;
    private BarChartPanel chartPanel;
    private DonutChartPanel donutPanel;
    private JComboBox<String> cmbTahunAjaran;
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private CardLayout rootCard;
    private JPanel rootPanel;
    private SkeletonPanel skeleton;
    private StatePanel statePanel;

    private JLabel lblTotalMahasiswaPill;
    private JLabel lblStudentProfileSub;

    private static Color BG()          { return AppTheme.bg(); }
    private static Color CARD_BG()     { return AppTheme.card(); }
    private static Color BORDER_COLOR(){ return AppTheme.border(); }
    private static Color TEXT_PRIMARY(){ return AppTheme.text(); }
    private static Color TEXT_MUTED()  { return AppTheme.muted(); }
    private static Color TEXT_DIM()    { return AppTheme.dim(); }
    private static Color BLUE()        { return AppTheme.blue(); }
    private static Color GREEN()       { return AppTheme.green(); }
    private static Color YELLOW()      { return AppTheme.yellow(); }
    private static Color RED()         { return AppTheme.red(); }
    private static Color CYAN()        { return AppTheme.cyan(); }
    private static Color PURPLE()      { return AppTheme.purple(); }

    public DashboardPanel() {
        setBackground(BG());
        setLayout(new BorderLayout());
        rootCard = new CardLayout();
        rootPanel = new JPanel(rootCard);
        rootPanel.setBackground(BG());
        skeleton = new SkeletonPanel(SkeletonPanel.Type.DASHBOARD);
        statePanel = new StatePanel();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG());
        initUI(content);
        rootPanel.add(skeleton, "skeleton");
        rootPanel.add(content, "content");
        rootPanel.add(statePanel, "state");
        add(rootPanel, BorderLayout.CENTER);
        loadData();
    }

    private void initUI(JPanel target) {
        boolean admin = JwtHelper.getInstance().isAdmin();

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 20, 14, 20));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(admin ? "Dashboard Admin" : "Dashboard Mahasiswa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY());

        lblStudentProfileSub = new JLabel(admin
                ? "Pantau pendapatan, status pembayaran, dan aktivitas akademik dari satu layar."
                : "Ringkasan status pembayaran dan riwayat transaksi Anda");
        lblStudentProfileSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStudentProfileSub.setForeground(TEXT_MUTED());

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblStudentProfileSub);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JLabel lblTA = new JLabel("Tahun Ajaran:");
        lblTA.setForeground(TEXT_MUTED());
        lblTA.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        cmbTahunAjaran = new JComboBox<>(new String[]{"Semua", "2024/2025", "2023/2024", "2022/2023"});
        styleCombo(cmbTahunAjaran);
        cmbTahunAjaran.addActionListener(e -> loadData());

        JButton btnRefresh = buildIconBtn("Refresh", CARD_BG());
        btnRefresh.addActionListener(e -> loadData());

        if (admin) {
            controls.add(lblTA);
            controls.add(cmbTahunAjaran);
        }
        controls.add(btnRefresh);

        JPanel adminHero = admin ? buildAdminHero() : null;

        header.add(titleBlock, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(0, 20, 18, 20));

        lblTotalPendapatan = new JLabel("Rp 0");
        lblLunas           = new JLabel("0");
        lblPending         = new JLabel("0");
        lblGagal           = new JLabel("0");

        if (admin) {
            statsRow.add(buildStatCard(StatIcon.WALLET, "Total Pendapatan", lblTotalPendapatan, BLUE(), "Akumulasi pembayaran lunas", "+12%"));
            statsRow.add(buildStatCard(StatIcon.CHECK, "Lunas", lblLunas, GREEN(), "Transaksi berhasil", "+8%"));
            statsRow.add(buildStatCard(StatIcon.CLOCK, "Pending", lblPending, YELLOW(), "Menunggu verifikasi", "-3%"));
            statsRow.add(buildStatCard(StatIcon.WARNING, "Gagal / Refund", lblGagal, RED(), "Transaksi bermasalah", "0%"));
        } else {
            statsRow.add(buildStatCard(StatIcon.WALLET, "Total Dibayar", lblTotalPendapatan, BLUE(), "Akumulasi pembayaran lunas", ""));
            statsRow.add(buildStatCard(StatIcon.CHECK, "Lunas", lblLunas, GREEN(), "Pembayaran tervalidasi", ""));
            statsRow.add(buildStatCard(StatIcon.CLOCK, "Pending", lblPending, YELLOW(), "Menunggu verifikasi", ""));
            statsRow.add(buildStatCard(StatIcon.WARNING, "Bermasalah", lblGagal, RED(), "Gagal atau refund", ""));
        }

        JPanel chartsRow = new JPanel(new GridLayout(2, 1, 0, 14));
        chartsRow.setOpaque(false);
        chartsRow.setBorder(new EmptyBorder(0, 20, 20, 20));

        chartPanel = new BarChartPanel();
        donutPanel = new DonutChartPanel();

        chartsRow.add(buildChartCard(admin ? "Pembayaran per Bulan" : "Riwayat Pembayaran Anda", chartPanel));
        chartsRow.add(buildChartCard("Distribusi Status", donutPanel));

        ScrollablePanel body = new ScrollablePanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        if (adminHero != null) {
            adminHero.setMaximumSize(new Dimension(1200, 80));
            body.add(adminHero);
            body.add(Box.createVerticalStrut(10));
        }
        statsRow.setMaximumSize(new Dimension(1200, 130));
        body.add(statsRow);
        chartsRow.setMaximumSize(new Dimension(1200, 1000));
        body.add(chartsRow);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        target.add(header, BorderLayout.NORTH);
        target.add(scroll, BorderLayout.CENTER);
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private enum StatIcon { WALLET, CHECK, CLOCK, WARNING }

    private JPanel buildAdminHero() {
        JPanel hero = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229, 18), getWidth(), 0, new Color(6, 182, 212, 12));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(79, 70, 229, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(14, 20, 14, 20));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Selamat datang, " + JwtHelper.getInstance().getUsername().toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_PRIMARY());

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pills.setOpaque(false);
        pills.add(buildPill("Admin aktif", GREEN()));
        lblTotalMahasiswaPill = buildPill("Total Mahasiswa: -", BLUE());
        pills.add(lblTotalMahasiswaPill);

        text.add(title);
        text.add(Box.createVerticalStrut(6));
        text.add(pills);

        hero.add(text, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 28, 0, 28));
        wrap.add(hero, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel buildPill(String text, Color color) {
        JLabel pill = new JLabel("  " + text + "  ");
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(color.darker());
        pill.setOpaque(true);
        pill.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
        pill.setBorder(new EmptyBorder(4, 8, 4, 8));
        return pill;
    }

    private JPanel buildStatCard(StatIcon iconType, String title, JLabel valueLabel, Color accent, String subtitle, String trend) {
        JPanel card = new JPanel() {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_COLOR());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                if (hover) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 12));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                }
                GradientPaint gp = new GradientPaint(0, 0, accent, 0, 4, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                g2.fillRoundRect(0, 0, 44, 44, 14, 14);
                drawStatIcon(g2, iconType, accent, 44);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
            @Override public Dimension getMaximumSize() { return new Dimension(44, 44); }
        };

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(TEXT_MUTED());

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bottomRow.setOpaque(false);
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(TEXT_DIM());
        bottomRow.add(lblSub);
        if (trend != null && !trend.isEmpty()) {
            JLabel lblTrend = new JLabel(trend);
            lblTrend.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblTrend.setForeground(trend.startsWith("+") ? GREEN() : (trend.startsWith("-") ? RED() : TEXT_DIM()));
            bottomRow.add(lblTrend);
        }

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(bottomRow);

        card.add(iconPanel, BorderLayout.EAST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private void drawStatIcon(Graphics2D g2, StatIcon type, Color color, int size) {
        g2.setColor(color);
        int cx = size / 2, cy = size / 2;
        switch (type) {
            case WALLET -> {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(8, 10, 28, 22, 4, 4);
                g2.drawLine(22, 10, 22, 6);
                g2.drawRoundRect(20, 4, 10, 6, 3, 3);
            }
            case CHECK -> {
                g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(7, 7, 30, 30);
                g2.drawLine(15, 22, 20, 28);
                g2.drawLine(20, 28, 30, 15);
            }
            case CLOCK -> {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(7, 7, 30, 30);
                g2.drawLine(22, 14, 22, 22);
                g2.drawLine(22, 22, 28, 22);
            }
            case WARNING -> {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] tx = {22, 9, 35};
                int[] ty = {8, 36, 36};
                g2.drawPolygon(tx, ty, 3);
                g2.drawLine(22, 16, 22, 26);
                g2.fillOval(20, 29, 4, 4);
            }
        }
    }

    private JPanel buildChartCard(String title, JPanel chart) {
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
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(0, 300));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_PRIMARY());
        titleRow.add(lblTitle, BorderLayout.WEST);

        chart.setOpaque(false);
        card.add(titleRow, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private void loadData() {
        skeleton.start();
        rootCard.show(rootPanel, "skeleton");
        boolean admin = JwtHelper.getInstance().isAdmin();
        String nim = JwtHelper.getInstance().getNim();
        String ta = cmbTahunAjaran.getSelectedIndex() == 0 ? null : (String) cmbTahunAjaran.getSelectedItem();

        if (!admin && (nim == null || nim.isBlank())) {
            skeleton.stop();
            statePanel.showState("!", "Akun belum terhubung NIM",
                    "Profil mahasiswa Anda belum memiliki NIM, sehingga dashboard personal tidak bisa ditampilkan.",
                    "Coba lagi", this::loadData);
            rootCard.show(rootPanel, "state");
            return;
        }

        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                if (admin) {
                    JsonObject stats = PembayaranService.getDashboardStats(ta);
                    try {
                        JsonObject mhs = MahasiswaService.getAll(1, 1, "");
                        if (stats.has("data")) {
                            stats.getAsJsonObject("data").add("mahasiswa_stats", mhs);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    return stats;
                } else {
                    JsonObject result = PembayaranService.getByNim(nim);
                    try {
                        JsonObject mhsDetail = MahasiswaService.getByNim(nim);
                        if (mhsDetail.get("success").getAsBoolean() && result.has("data")) {
                            result.getAsJsonObject("data").add("mahasiswa_profile", mhsDetail.getAsJsonObject("data"));
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    return result;
                }
            }
            @Override protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        JsonObject dataObj = resp.getAsJsonObject("data");
                        if (admin) renderAdminDashboard(dataObj);
                        else renderMahasiswaDashboard(dataObj);
                    } else {
                        showErrorState(resp.has("message") ? resp.get("message").getAsString() : "Gagal memuat dashboard.");
                    }
                } catch (Exception e) {
                    showErrorState("Gagal memuat dashboard: " + e.getMessage());
                } finally { skeleton.stop(); }
            }
        }.execute();
    }

    private void renderAdminDashboard(JsonObject data) {
        JsonObject ring = data.getAsJsonObject("ringkasan");
        lblTotalPendapatan.setText(RUPIAH.format(ring.get("total_pendapatan").getAsDouble()));
        lblLunas.setText(String.valueOf(ring.get("lunas").getAsInt()));
        lblPending.setText(String.valueOf(ring.get("pending").getAsInt()));
        lblGagal.setText(String.valueOf(ring.get("gagal").getAsInt()));

        if (data.has("mahasiswa_stats")) {
            JsonObject mhsStats = data.getAsJsonObject("mahasiswa_stats");
            if (mhsStats.get("success").getAsBoolean() && mhsStats.has("pagination")) {
                int total = mhsStats.getAsJsonObject("pagination").get("total").getAsInt();
                if (lblTotalMahasiswaPill != null) {
                    lblTotalMahasiswaPill.setText("  Total Mahasiswa: " + total + "  ");
                }
            }
        }

        JsonArray bulanan = data.getAsJsonArray("chart_pendapatan_bulanan");
        int[] vals = new int[bulanan.size()];
        String[] lbls = new String[bulanan.size()];
        for (int i = 0; i < bulanan.size(); i++) {
            JsonObject b = bulanan.get(i).getAsJsonObject();
            vals[i] = b.get("total_transaksi").getAsInt();
            lbls[i] = b.get("bulan").getAsString();
        }
        chartPanel.setData(vals, lbls);

        int lunas = ring.get("lunas").getAsInt();
        int pending = ring.get("pending").getAsInt();
        int gagal = ring.get("gagal").getAsInt();
        donutPanel.setData(
                new int[]{lunas, pending, gagal},
                new String[]{"Lunas", "Pending", "Gagal"},
                new Color[]{GREEN(), YELLOW(), RED()}
        );
        rootCard.show(rootPanel, "content");
    }

    private void renderMahasiswaDashboard(JsonObject data) {
        JsonArray pembayaran = data.getAsJsonArray("pembayaran");
        double totalDibayar = 0;
        int lunas = 0, pending = 0, gagal = 0;
        int count = pembayaran == null ? 0 : pembayaran.size();
        int[] vals = new int[count];
        String[] lbls = new String[count];

        for (int i = 0; i < count; i++) {
            JsonObject p = pembayaran.get(i).getAsJsonObject();
            String status = safe(p, "status").toLowerCase();
            if ("lunas".equals(status)) { lunas++; totalDibayar += p.get("jumlah").getAsDouble(); }
            else if ("pending".equals(status)) pending++;
            else if ("gagal".equals(status) || "refund".equals(status)) gagal++;
            vals[i] = 1;
            String tanggal = safe(p, "tanggal_bayar");
            lbls[i] = tanggal.length() >= 10 ? tanggal.substring(5, 10) : safe(p, "jenis_pembayaran");
        }

        if (data.has("mahasiswa_profile")) {
            JsonObject profile = data.getAsJsonObject("mahasiswa_profile");
            String nama = profile.get("nama").getAsString();
            String nim = profile.get("nim").getAsString();
            String prodi = profile.has("program_studi") && !profile.get("program_studi").isJsonNull()
                    ? profile.get("program_studi").getAsString() : "-";
            if (lblStudentProfileSub != null) {
                lblStudentProfileSub.setText("Selamat datang kembali, " + nama + " (" + nim + ")  |  " + prodi);
            }
        }

        lblTotalPendapatan.setText(RUPIAH.format(totalDibayar));
        lblLunas.setText(String.valueOf(lunas));
        lblPending.setText(String.valueOf(pending));
        lblGagal.setText(String.valueOf(gagal));
        chartPanel.setData(vals, lbls);
        donutPanel.setData(
                new int[]{lunas, pending, gagal},
                new String[]{"Lunas", "Pending", "Bermasalah"},
                new Color[]{GREEN(), YELLOW(), RED()}
        );
        rootCard.show(rootPanel, "content");
    }

    private void showErrorState(String message) {
        statePanel.showState("!", "Dashboard tidak bisa dimuat", message, "Muat ulang", this::loadData);
        rootCard.show(rootPanel, "state");
    }

    private String safe(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "-";
    }

    static class BarChartPanel extends JPanel {
        private int[] values = {};
        private String[] labels = {};
        private static final Color[] GRADIENT_TOP = {
            new Color(79,70,229), new Color(99,102,241), new Color(6,182,212),
            new Color(16,185,129), new Color(245,158,11), new Color(239,68,68),
            new Color(139,92,246), new Color(236,72,153), new Color(249,115,22),
            new Color(20,184,166), new Color(132,204,22), new Color(251,191,36)
        };

        public void setData(int[] v, String[] l) { values = v; labels = l; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (values.length == 0) {
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String msg = "Tidak ada data";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose(); return;
            }

            int max = 0;
            for (int v : values) max = Math.max(max, v);
            if (max == 0) { g2.dispose(); return; }

            int padL = 40, padR = 20, padT = 22, padB = 32;
            int chartW = getWidth() - padL - padR;
            int chartH = getHeight() - padT - padB;
            int n = values.length;
            
            // Calculate bar width based on available space to prevent overflow
            int barGap = 16;
            int maxBarW = 80; // Cap maximum bar width
            int barW = Math.min(maxBarW, Math.max(10, (chartW - (barGap * (n + 1))) / n));
            int actualChartW = (barW * n) + (barGap * (n - 1));
            int startX = padL + (chartW - actualChartW) / 2;

            g2.setColor(new Color(226, 232, 240, AppTheme.isDark() ? 30 : 200));
            for (int i = 1; i <= 4; i++) {
                int y = padT + chartH - (chartH * i / 4);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setColor(AppTheme.dim());
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.valueOf(max * i / 4), 2, y + 4);
                g2.setColor(new Color(226, 232, 240, AppTheme.isDark() ? 30 : 200));
            }

            for (int i = 0; i < n; i++) {
                int barH = (int) ((double) values[i] / max * chartH);
                int x = startX + i * (barW + barGap);
                int y = padT + chartH - barH;

                Color c = GRADIENT_TOP[i % GRADIENT_TOP.length];
                GradientPaint gp = new GradientPaint(x, y, c, x, padT + chartH, new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                g2.setColor(AppTheme.text());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String val = String.valueOf(values[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 5);

                g2.setColor(AppTheme.dim());
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                if (i < labels.length) {
                    String raw = labels[i];
                    String lbl;
                    if (raw.length() <= 3) {
                        lbl = raw;
                    } else if (raw.matches("\\d{4}-\\d{2}.*")) {
                        String mm = raw.length() >= 7 ? raw.substring(5, 7) : raw;
                        lbl = getMonthShort(mm);
                    } else {
                        lbl = raw.length() > 8 ? raw.substring(0, 7) + "\u2026" : raw;
                    }
                    fm = g2.getFontMetrics();
                    g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, getHeight() - 8);
                }
            }

            g2.setColor(AppTheme.border());
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);
            g2.dispose();
        }

        private String getMonthShort(String mm) {
            return switch (mm) {
                case "01" -> "Jan"; case "02" -> "Feb"; case "03" -> "Mar";
                case "04" -> "Apr"; case "05" -> "Mei"; case "06" -> "Jun";
                case "07" -> "Jul"; case "08" -> "Agu"; case "09" -> "Sep";
                case "10" -> "Okt"; case "11" -> "Nov"; case "12" -> "Des";
                default -> mm;
            };
        }
    }

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
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString("Tidak ada data", getWidth() / 2 - 50, getHeight() / 2);
                g2.dispose(); return;
            }

            int w = getWidth() - 40;
            int h = getHeight() - 40;
            int legendH = values.length * 26 + 10;
            int chartAreaH = h - legendH;

            int size = Math.min(w, chartAreaH);
            int cx = 20 + (w - size) / 2;
            int cy = 20 + (chartAreaH - size) / 2;
            int hole = (int)(size * 0.58);
            int hx = cx + (size - hole) / 2;
            int hy = cy + (size - hole) / 2;

            double startAngle = -90;
            for (int i = 0; i < values.length; i++) {
                double sweep = (double) values[i] / total * 360;
                g2.setColor(colors[i]);
                g2.fillArc(cx, cy, size, size, (int) startAngle, (int) sweep);
                startAngle += sweep;
            }

            g2.setColor(CARD_BG());
            g2.fillOval(hx, hy, hole, hole);

            g2.setColor(AppTheme.text());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            String totalStr = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(totalStr, hx + (hole - fm.stringWidth(totalStr)) / 2, hy + hole / 2 + 4);
            g2.setColor(AppTheme.dim());
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String sub = "Total";
            fm = g2.getFontMetrics();
            g2.drawString(sub, hx + (hole - fm.stringWidth(sub)) / 2, hy + hole / 2 + 16);

            int ly = 20 + chartAreaH + 10;
            int colW = w / Math.min(values.length, 3);
            for (int i = 0; i < values.length; i++) {
                int col = i % 3;
                int lx = 20 + col * colW;
                int row = i / 3;
                int rowY = ly + row * 26;
                g2.setColor(colors[i]);
                g2.fillRoundRect(lx, rowY, 12, 12, 4, 4);
                g2.setColor(AppTheme.text());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                int pct = (int) Math.round((double) values[i] / total * 100);
                String legendText = labels[i] + "  " + values[i] + "  (" + pct + "%)";
                g2.drawString(legendText, lx + 18, rowY + 10);
            }
            g2.dispose();
        }
    }

    private JButton buildIconBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(TEXT_MUTED());
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR(), 1),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(CARD_BG());
        c.setForeground(TEXT_MUTED());
        c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        c.setPreferredSize(new Dimension(140, 32));
    }
}
