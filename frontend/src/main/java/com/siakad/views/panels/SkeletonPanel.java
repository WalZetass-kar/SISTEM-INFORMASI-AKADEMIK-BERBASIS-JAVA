package com.siakad.views.panels;

import javax.swing.*;
import java.awt.*;

/**
 * SkeletonPanel - Shimmer skeleton loading placeholder.
 * Tipe: DASHBOARD, TABLE, LAPORAN
 */
public class SkeletonPanel extends JPanel {

    public enum Type { DASHBOARD, TABLE, LAPORAN }

    private float shimmerPos = 0f; // 0..1
    private final Timer timer;
    private final Type type;

    private static final Color BASE   = new Color(22, 32, 58);
    private static final Color SHINE  = new Color(45, 62, 100);
    private static final Color BG     = new Color(13, 19, 38);
    private static final Color CARD   = new Color(18, 26, 48);
    private static final Color BORDER = new Color(25, 36, 65);

    public SkeletonPanel(Type type) {
        this.type = type;
        setBackground(BG);
        setLayout(null);
        timer = new Timer(18, e -> {
            shimmerPos += 0.022f;
            if (shimmerPos > 1.5f) shimmerPos = -0.5f;
            repaint();
        });
    }

    public void start() { shimmerPos = -0.5f; timer.start(); }
    public void stop()  { timer.stop(); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (type) {
            case DASHBOARD -> drawDashboard(g2);
            case TABLE     -> drawTable(g2);
            case LAPORAN   -> drawLaporan(g2);
        }
        g2.dispose();
    }

    // ── Shimmer paint helper ──────────────────────────────────────────────────
    private void block(Graphics2D g2, int x, int y, int w, int h) {
        block(g2, x, y, w, h, 6);
    }
    private void block(Graphics2D g2, int x, int y, int w, int h, int arc) {
        g2.setColor(BASE);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        // Shimmer sweep
        int sw = getWidth();
        int sx = (int)(shimmerPos * sw) - 120;
        GradientPaint gp = new GradientPaint(
            sx,       y, new Color(255,255,255,0),
            sx + 80,  y, new Color(255,255,255,22),
            false
        );
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        GradientPaint gp2 = new GradientPaint(
            sx + 80,  y, new Color(255,255,255,22),
            sx + 160, y, new Color(255,255,255,0),
            false
        );
        g2.setPaint(gp2);
        g2.fillRoundRect(x, y, w, h, arc, arc);
    }

    private void card(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(CARD);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(BORDER);
        g2.drawRoundRect(x, y, w-1, h-1, 12, 12);
    }

    private void circle(Graphics2D g2, int x, int y, int d) {
        g2.setColor(BASE);
        g2.fillOval(x, y, d, d);
        int sw = getWidth();
        int sx = (int)(shimmerPos * sw) - 120;
        GradientPaint gp = new GradientPaint(sx, y, new Color(255,255,255,0), sx+80, y, new Color(255,255,255,22), false);
        g2.setPaint(gp); g2.fillOval(x, y, d, d);
        GradientPaint gp2 = new GradientPaint(sx+80, y, new Color(255,255,255,22), sx+160, y, new Color(255,255,255,0), false);
        g2.setPaint(gp2); g2.fillOval(x, y, d, d);
    }

    // ── Dashboard skeleton ────────────────────────────────────────────────────
    private void drawDashboard(Graphics2D g2) {
        int p = 28, w = getWidth() - p*2;

        // Header
        block(g2, p, 28, 180, 28);
        block(g2, p, 64, 260, 14);

        // 4 stat cards
        int cw = (w - 42) / 4;
        for (int i = 0; i < 4; i++) {
            int x = p + i*(cw+14);
            card(g2, x, 96, cw, 100);
            block(g2, x+16, 112, 70, 11);
            block(g2, x+16, 131, 110, 22);
            block(g2, x+16, 161, 55, 10);
            circle(g2, x+cw-56, 112, 36);
        }

        // 2 chart cards
        int chartW = (w - 14) / 2;
        card(g2, p, 216, chartW, 250);
        card(g2, p+chartW+14, 216, chartW, 250);

        // Bar chart bars
        for (int i = 0; i < 9; i++) {
            int bh = 30 + (i*17) % 90;
            block(g2, p+20+i*28, 216+250-bh-28, 18, bh, 4);
        }

        // Donut placeholder
        int dc = p + chartW + 14 + chartW/2;
        circle(g2, dc-55, 216+40, 110);
        g2.setColor(BG); g2.fillOval(dc-33, 216+62, 66, 66);
        // Legend
        for (int i = 0; i < 3; i++) {
            block(g2, dc+65, 216+60+i*30, 12, 12, 3);
            block(g2, dc+83, 216+60+i*30, 80, 12);
        }
    }

    // ── Table skeleton ────────────────────────────────────────────────────────
    private void drawTable(Graphics2D g2) {
        int p = 28, w = getWidth() - p*2;

        // Header
        block(g2, p, 28, 180, 28);
        block(g2, p, 64, 220, 14);

        // Search + buttons
        block(g2, getWidth()-p-360, 34, 220, 34, 8);
        block(g2, getWidth()-p-130, 34, 60, 34, 8);
        block(g2, getWidth()-p-62, 34, 62, 34, 8);

        // Table header row
        card(g2, p, 96, w, 40);
        int[] cws = {70, 150, 110, 110, 70, 55, 75, 100};
        int cx = p+12;
        for (int cw : cws) { block(g2, cx, 108, cw-12, 14); cx += cw+4; }

        // Table rows
        for (int r = 0; r < 9; r++) {
            int ry = 136 + r*44;
            g2.setColor(r%2==0 ? new Color(15,22,42) : new Color(20,29,52));
            g2.fillRect(p, ry, w, 42);
            cx = p+12;
            for (int cw : cws) { block(g2, cx, ry+14, cw-12, 14); cx += cw+4; }
        }
    }

    // ── Laporan skeleton ──────────────────────────────────────────────────────
    private void drawLaporan(Graphics2D g2) {
        int p = 28, w = getWidth() - p*2;

        // Header
        block(g2, p, 28, 180, 28);
        block(g2, p, 64, 300, 14);

        // 3 generate cards
        int cw = (w - 28) / 3;
        for (int i = 0; i < 3; i++) {
            int x = p + i*(cw+14);
            card(g2, x, 96, cw, 165);
            circle(g2, x+16, 112, 44);
            block(g2, x+16, 168, 110, 16);
            block(g2, x+16, 192, cw-32, 12);
            block(g2, x+16, 210, cw-32, 12);
            block(g2, x+16, 232, 100, 30, 8);
        }

        // Table rows
        card(g2, p, 280, w, 40);
        int[] cws = {45, 200, 90, 120, 90, 50, 80, 90};
        int cx = p+12;
        for (int cw2 : cws) { block(g2, cx, 292, cw2-8, 14); cx += cw2+4; }
        for (int r = 0; r < 6; r++) {
            int ry = 320 + r*44;
            g2.setColor(r%2==0 ? new Color(15,22,42) : new Color(20,29,52));
            g2.fillRect(p, ry, w, 42);
            cx = p+12;
            for (int cw2 : cws) { block(g2, cx, ry+14, cw2-8, 14); cx += cw2+4; }
        }
    }
}
