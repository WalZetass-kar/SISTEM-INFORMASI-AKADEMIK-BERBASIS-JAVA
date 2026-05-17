package com.siakad.views;

import com.siakad.utils.JwtHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * SplashTransition - Animasi transisi dari Login ke Dashboard
 * Urutan: LoginFrame fade-out → Splash screen (logo + loading bar) → MainFrame slide-in
 */
public class SplashTransition extends JWindow {

    private float alpha = 0f;
    private float barProgress = 0f;
    private int dotFrame = 0;
    private final String username;

    private static final Color BG       = new Color(10, 15, 30);
    private static final Color BLUE     = new Color(59, 130, 246);
    private static final Color INDIGO   = new Color(99, 102, 241);
    private static final Color CYAN     = new Color(34, 211, 238);
    private static final Color TEXT     = new Color(248, 250, 252);
    private static final Color MUTED    = new Color(148, 163, 184);

    public SplashTransition() {
        this.username = JwtHelper.getInstance().getUsername();
        setSize(480, 320);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        // Rounded window shape
        try {
            setShape(new RoundRectangle2D.Double(0, 0, 480, 320, 24, 24));
        } catch (Exception ignored) {}
    }

    @Override public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background with alpha
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(BG);
        g2.fillRoundRect(0, 0, w, h, 24, 24);

        // Decorative circles
        g2.setColor(new Color(59, 130, 246, 20));
        g2.fillOval(-40, -40, 200, 200);
        g2.setColor(new Color(99, 102, 241, 15));
        g2.fillOval(w - 120, h - 120, 220, 220);

        // Logo circle
        int cx = w / 2, cy = h / 2 - 40;
        g2.setColor(new Color(59, 130, 246, 30));
        g2.fillOval(cx - 44, cy - 44, 88, 88);
        GradientPaint gp = new GradientPaint(cx - 36, cy - 36, BLUE, cx + 36, cy + 36, INDIGO);
        g2.setPaint(gp);
        g2.fillOval(cx - 36, cy - 36, 72, 72);

        // Graduation icon
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("🎓", cx - fm.stringWidth("🎓") / 2, cy + 12);

        // App name
        g2.setColor(TEXT);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        fm = g2.getFontMetrics();
        g2.drawString("SIAKAD", cx - fm.stringWidth("SIAKAD") / 2, cy + 60);

        // Welcome text
        String welcome = "Selamat datang, " + username + "!";
        g2.setColor(MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fm = g2.getFontMetrics();
        g2.drawString(welcome, cx - fm.stringWidth(welcome) / 2, cy + 82);

        // Loading bar background
        int barW = 280, barH = 4;
        int bx = cx - barW / 2, by = cy + 100;
        g2.setColor(new Color(30, 41, 70));
        g2.fillRoundRect(bx, by, barW, barH, barH, barH);

        // Loading bar fill (animated)
        int fillW = (int)(barW * barProgress);
        if (fillW > 0) {
            GradientPaint barGp = new GradientPaint(bx, by, BLUE, bx + fillW, by, CYAN);
            g2.setPaint(barGp);
            g2.fillRoundRect(bx, by, fillW, barH, barH, barH);

            // Glow at tip
            if (fillW > 6) {
                g2.setColor(new Color(34, 211, 238, 80));
                g2.fillOval(bx + fillW - 6, by - 4, 12, 12);
            }
        }

        // Loading dots
        String[] dots = {"Memuat dashboard", "Memuat dashboard.", "Memuat dashboard..", "Memuat dashboard..."};
        String loadingText = dots[dotFrame % 4];
        g2.setColor(new Color(100, 116, 139));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fm = g2.getFontMetrics();
        g2.drawString(loadingText, cx - fm.stringWidth(loadingText) / 2, by + 22);

        g2.dispose();
    }

    /**
     * Jalankan animasi lengkap: fade-in → loading bar → fade-out → callback
     */
    public void animate(LoginFrame loginFrame, Runnable onComplete) {
        setVisible(true);

        // Phase 1: Fade in splash + fade out login (0 → 400ms)
        Timer fadeIn = new Timer(16, null);
        fadeIn.addActionListener(e -> {
            alpha = Math.min(1f, alpha + 0.06f);
            // Fade out login frame simultaneously
            float loginAlpha = 1f - alpha;
            loginFrame.setOpacity(Math.max(0f, loginAlpha));
            repaint();
            if (alpha >= 1f) {
                fadeIn.stop();
                loginFrame.setVisible(false);
                startLoadingBar(onComplete);
            }
        });
        fadeIn.start();
    }

    private void startLoadingBar(Runnable onComplete) {
        // Phase 2: Loading bar progress (400ms → 1200ms)
        Timer loadBar = new Timer(16, null);
        loadBar.addActionListener(e -> {
            barProgress = Math.min(1f, barProgress + 0.012f);
            dotFrame++;
            repaint();
            if (barProgress >= 1f) {
                loadBar.stop();
                fadeOut(onComplete);
            }
        });
        loadBar.start();
    }

    private void fadeOut(Runnable onComplete) {
        // Phase 3: Fade out splash (1200ms → 1600ms)
        Timer fadeOut = new Timer(16, null);
        fadeOut.addActionListener(e -> {
            alpha = Math.max(0f, alpha - 0.07f);
            repaint();
            if (alpha <= 0f) {
                fadeOut.stop();
                dispose();
                onComplete.run();
            }
        });
        fadeOut.start();
    }
}
