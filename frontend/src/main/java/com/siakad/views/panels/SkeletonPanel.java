package com.siakad.views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * SkeletonPanel - Komponen skeleton loading dengan efek shimmer
 * Gunakan sebagai placeholder saat data sedang dimuat.
 *
 * Cara pakai:
 *   SkeletonPanel sk = new SkeletonPanel(SkeletonPanel.Type.DASHBOARD);
 *   sk.start();
 *   // saat data selesai:
 *   sk.stop();
 *   cardLayout.show(contentPanel, "realPanel");
 */
public class SkeletonPanel extends JPanel {

    public enum Type { DASHBOARD, TABLE, LAPORAN }

    private float shimmerX = -1f;   // 0..1 posisi shimmer
    private final Timer shimmerTimer;
    private final Type type;

    private static final Color BASE    = new Color(22, 32, 58);
    private static final Color SHINE   = new Color(35, 50, 85);
    private static final Color BG      = new Color(13, 19, 38);

    public SkeletonPanel(Type type) {
        this.type = type;
        setBackground(BG);
        setLayout(null);

        shimmerTimer = new Timer(20, e -> {
            shimmerX += 0.018f;
            if (shimmerX > 1.4f) shimmerX = -0.4f;
            repaint();
        });
    }

    public void start() { shimmerX = -0.4f; shimmerTimer.start(); }
    public void stop()  { shimmerTimer.stop(); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (type) {
            case DASHBOARD -> paintDashboardSkeleton(g2);
            case TABLE     -> paintTableSkeleton(g2);
            case LAPORAN   -> paintLaporanSkeleton(g2);
        }
        g2.dispose();
    }

    // ── Dashboard skeleton: 4 stat cards + 2 chart cards ─────────────────────
    private void paintDashboardSkeleton(Graphics2D g2) {
        int pad = 28;
        int w = getWidth() - pad * 2;

        // Header
        drawBlock(g2, pad, 28, 200, 28);
        drawBlock(g2, pad, 64, 280, 16);

        // 4 stat cards
        int cardW = (w - 42) / 4;
        for (int i = 0; i < 4; i++) {
            int x = pad + i * (cardW + 14);
            drawCard(g2, x, 100, cardW, 100);
            drawBlock(g2, x + 16, 116, 80, 12);
            drawBlock(g2, x + 16, 136, 120, 22);
            drawBlock(g2, x + 16, 166, 60, 10);
        }

        // 2 chart cards
        int chartW = (w - 14) / 2;
        drawCard(g2, pad, 220, chartW, 240);
        drawCard(g2, pad + chartW + 14, 220, chartW, 240);

        // Chart bars inside left card
        for (int i = 0; i < 8; i++) {
            int bh = 40 + (i % 3) * 30;
            drawBlock(g2, pad + 20 + i * 30, 220 + 240 - bh - 30, 20, bh);
        }
        // Donut placeholder
        int dc = pad + chartW + 14 + chartW / 2;
        drawCircle(g2, dc - 60, 220 + 50, 120, 120);
        g2.setColor(BG);
        g2.fillOval(dc - 38, 220 + 72, 76, 76);
    }

    // ── Table skeleton: header + rows ─────────────────────────────────────────
    private void paintTableSkeleton(Graphics2D g2) {
        int pad = 28;
        int w = getWidth() - pad * 2;

        // Header
        drawBlock(g2, pad, 28, 180, 28);
        drawBlock(g2, pad, 64, 240, 16);

        // Search + buttons
        drawBlock(g2, getWidth() - pad - 340, 36, 200, 34);
        drawBlock(g2, getWidth() - pad - 130, 36, 60, 34);
        drawBlock(g2, getWidth() - pad - 60, 36, 60, 34);

        // Table header
        drawCard(g2, pad, 100, w, 40);
        int[] colW = {80, 160, 120, 120, 80, 60, 80, 100};
        int cx = pad + 12;
        for (int cw : colW) {
            drawBlock(g2, cx, 112, cw - 16, 14);
            cx += cw + 4;
        }

        // Table rows
        for (int r = 0; r < 8; r++) {
            int ry = 140 + r * 44;
            g2.setColor(r % 2 == 0 ? new Color(15, 22, 42) : new Color(20, 29, 52));
            g2.fillRect(pad, ry, w, 42);
            cx = pad + 12;
            for (int cw : colW) {
                drawBlock(g2, cx, ry + 14, cw - 16, 14);
                cx += cw + 4;
            }
        }
    }

    // ── Laporan skeleton: 3 generate cards + table ────────────────────────────
    private void paintLaporanSkeleton(Graphics2D g2) {
        int pad = 28;
        int w = getWidth() - pad * 2;

        // Header
        drawBlock(g2, pad, 28, 180, 28);
        drawBlock(g2, pad, 64, 300, 16);

        // 3 generate cards
        int cardW = (w - 28) / 3;
        for (int i = 0; i < 3; i++) {
            int x = pad + i * (cardW + 14);
            drawCard(g2, x, 100, cardW, 160);
            drawCircle(g2, x + 16, 116, 44, 44);
            drawBlock(g2, x + 16, 172, 120, 16);
            drawBlock(g2, x + 16, 196, cardW - 32, 12);
            drawBlock(g2, x + 16, 212, cardW - 32, 12);
            drawBlock(g2, x + 16, 234, 100, 30);
        }

        // Table rows
        drawCard(g2, pad, 280, w, 40);
        for (int r = 0; r < 6; r++) {
            int ry = 320 + r * 44;
            g2.setColor(r % 2 == 0 ? new Color(15, 22, 42) : new Color(20, 29, 52));
            g2.fillRect(pad, ry, w, 42);
            drawBlock(g2, pad + 12, ry + 14, 40, 14);
            drawBlock(g2, pad + 70, ry + 14, 200, 14);
            drawBlock(g2, pad + 290, ry + 14, 80, 14);
            drawBlock(g2, pad + 390, ry + 14, 100, 14);
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────
    private void drawBlock(Graphics2D g2, int x, int y, int w, int h) {
        applyShimmer(g2, x, y, w, h);
        g2.fillRoundRect(x, y, w, h, 6, 6);
    }

    private void drawCard(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(BASE);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(new Color(25, 36, 65));
        g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
    }

    private void drawCircle(Graphics2D g2, int x, int y, int w, int h) {
        applyShimmer(g2, x, y, w, h);
        g2.fillOval(x, y, w, h);
    }

    private void applyShimmer(Graphics2D g2, int x, int y, int w, int h) {
        // Base color
        g2.setColor(BASE);
        g2.fillRoundRect(x, y, w, h, 6, 6);

        // Shimmer overlay
        if (shimmerX >= 0) {
            int totalW = getWidth();
            int sx = (int)(shimmerX * totalW) - 80;
            GradientPaint shimmer = new GradientPaint(
                sx, y, new Color(255, 255, 255, 0),
                sx + 80, y, new Color(255, 255, 255, 18),
                true
            );
            // Second gradient for smooth fade-out
            GradientPaint shimmer2 = new GradientPaint(
                sx + 80, y, new Color(255, 255, 255, 18),
                sx + 160, y, new Color(255, 255, 255, 0),
                true
            );
            g2.setPaint(shimmer);
            g2.fillRoundRect(x, y, w, h, 6, 6);
            g2.setPaint(shimmer2);
            g2.fillRoundRect(x, y, w, h, 6, 6);
        }
    }
}
