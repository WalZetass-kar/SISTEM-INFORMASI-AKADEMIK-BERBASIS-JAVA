package com.siakad.views;

import com.siakad.utils.JwtHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SplashTransition - Animasi loading saat login berhasil ke dashboard.
 * Menggunakan JDialog undecorated agar robust di semua platform Linux.
 */
public class SplashTransition extends JDialog {

    private float alpha       = 0f;
    private float barProgress = 0f;
    private float pulseRadius = 0f;
    private float pulseAlpha  = 0f;
    private int   counter     = 0;
    private int   tick        = 0;
    private String loadingMessage = "Menyiapkan sesi";
    private final String username;

    private final List<Particle> particles = new ArrayList<>();
    private static final Random RNG = new Random();

    private static final Color BG     = new Color(10, 15, 30);
    private static final Color BLUE   = new Color(59, 130, 246);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color CYAN   = new Color(34, 211, 238);
    private static final Color GREEN  = new Color(34, 197, 94);
    private static final Color TEXT   = new Color(248, 250, 252);
    private static final Color MUTED  = new Color(148, 163, 184);

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float easeOutCubic(float value) {
        float t = clamp(value);
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private static float easeInCubic(float value) {
        float t = clamp(value);
        return t * t * t;
    }

    private static class Particle {
        float x, y, vx, vy, size, life, maxLife;
        Color color;
        Particle(int w, int cy) {
            x = w / 2f + RNG.nextFloat() * 80 - 40;
            y = cy + RNG.nextFloat() * 80 - 40;
            float angle = RNG.nextFloat() * (float)(Math.PI * 2);
            float speed = 0.5f + RNG.nextFloat() * 1.5f;
            vx = (float) Math.cos(angle) * speed;
            vy = (float) Math.sin(angle) * speed - 0.6f;
            size = 3f + RNG.nextFloat() * 4f;
            maxLife = 50 + RNG.nextFloat() * 70;
            life = maxLife;
            Color[] palette = {BLUE, INDIGO, CYAN, GREEN, new Color(168, 85, 247)};
            color = palette[RNG.nextInt(palette.length)];
        }
        boolean update() { x += vx; y += vy; vy += 0.03f; life--; return life > 0; }
        float alpha() { return life / maxLife; }
    }

    public SplashTransition(Frame owner) {
        super(owner, true);
        this.username = JwtHelper.getInstance().getUsername();
        setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setLocation(0, 0);
        setBackground(BG);
    }

    @Override public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(BG);
        g2.fillRect(0, 0, w, h);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));

        int drift = (int) (Math.sin(tick * 0.035) * 28);
        int slowDrift = (int) (Math.cos(tick * 0.022) * 34);

        // Soft moving light fields
        g2.setColor(new Color(59, 130, 246, 24));
        g2.fillOval(-90 + drift, -80 + slowDrift, 500, 500);
        g2.setColor(new Color(99, 102, 241, 18));
        g2.fillOval(w - 420 - slowDrift, h - 420 + drift, 640, 640);
        g2.setColor(new Color(34, 197, 94, 12));
        g2.fillOval(w / 2 - 240 + slowDrift, h / 2 - 210, 520, 520);

        // Grid dots
        g2.setColor(new Color(255, 255, 255, 7));
        for (int x = 16; x < w; x += 28)
            for (int y = 16; y < h; y += 28)
                g2.fillOval(x, y, 2, 2);

        // Particles
        for (Particle p : particles) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha() * clamp(alpha)));
            g2.setColor(p.color);
            g2.fillOval((int) p.x, (int) p.y, (int) p.size, (int) p.size);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));

        float reveal = easeOutCubic(tick / 32f);
        int cx = w / 2, cy = h / 2 - 60 + (int) ((1f - reveal) * 28);

        // Pulse ring
        if (pulseAlpha > 0 && alpha > 0.5f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha * clamp(alpha)));
            g2.setColor(BLUE);
            int pr = (int) pulseRadius;
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(cx - pr, cy - pr, pr * 2, pr * 2);
            g2.setStroke(new BasicStroke(1f));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));
        }

        // Logo outer glow
        g2.setColor(new Color(59, 130, 246, 35));
        g2.fillOval(cx - 80, cy - 80, 160, 160);
        // Logo gradient circle
        GradientPaint logoGp = new GradientPaint(cx - 64, cy - 64, BLUE, cx + 64, cy + 64, INDIGO);
        g2.setPaint(logoGp);
        g2.fillOval(cx - 64, cy - 64, 128, 128);
        // Shine
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillOval(cx - 50, cy - 62, 80, 48);

        // Icon
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("🎓", cx - fm.stringWidth("🎓") / 2, cy + 20);

        // App name
        g2.setColor(TEXT);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
        fm = g2.getFontMetrics();
        g2.drawString("SIAKAD", cx - fm.stringWidth("SIAKAD") / 2, cy + 110);

        // Welcome
        String welcome = "Selamat datang, " + username + "!";
        g2.setColor(MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        fm = g2.getFontMetrics();
        g2.drawString(welcome, cx - fm.stringWidth(welcome) / 2, cy + 142);

        // Bar track
        int barW = 480, barH = 6;
        int bx = cx - barW / 2, by = cy + 168;
        g2.setColor(new Color(30, 41, 70));
        g2.fillRoundRect(bx, by, barW, barH, barH, barH);

        // Bar fill
        int fillW = (int)(barW * barProgress);
        if (fillW > 0) {
            GradientPaint barGp = new GradientPaint(bx, by, BLUE, bx + fillW, by, CYAN);
            g2.setPaint(barGp);
            g2.fillRoundRect(bx, by, fillW, barH, barH, barH);
            int shimmerX = bx + (int) ((barW + 80) * ((tick % 70) / 70f)) - 80;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f * clamp(alpha)));
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(shimmerX, by, 70, barH, barH, barH);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));
            if (fillW > 8) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f * clamp(alpha)));
                g2.setColor(CYAN);
                g2.fillOval(bx + fillW - 9, by - 6, 18, 18);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));
            }
        }

        // Counter
        g2.setColor(new Color(59, 130, 246, 200));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String pct = counter + "%";
        fm = g2.getFontMetrics();
        g2.drawString(pct, cx - fm.stringWidth(pct) / 2, by + 24);

        // Loading dots
        String[] dots = {loadingMessage, loadingMessage + ".", loadingMessage + "..", loadingMessage + "..."};
        g2.setColor(new Color(71, 85, 105));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String loadTxt = dots[tick / 8 % 4];
        fm = g2.getFontMetrics();
        g2.drawString(loadTxt, cx - fm.stringWidth(loadTxt) / 2, by + 44);

        g2.dispose();
    }

    /**
     * Jalankan animasi lalu panggil onComplete di EDT setelah selesai.
     * Dipanggil dari LoginFrame setelah login sukses.
     */
    public void animate(LoginFrame loginFrame, Runnable onComplete) {
        // Sembunyikan login dulu
        loginFrame.setVisible(false);

        Timer masterTimer = new Timer(16, null);
        masterTimer.addActionListener(e -> {
            tick++;

            // Fade in (tick 1-24)
            if (tick <= 24) alpha = easeOutCubic(tick / 24f);

            // Loading bar + particles (tick 24-110)
            if (tick > 24 && tick <= 110) {
                float progress = easeOutCubic((tick - 24f) / 86f);
                barProgress = Math.min(1f, progress);
                counter = Math.min(100, (int)(barProgress * 100));

                if (counter < 35) loadingMessage = "Menyiapkan sesi";
                else if (counter < 72) loadingMessage = "Menyusun dashboard";
                else loadingMessage = "Membuka aplikasi";

                pulseRadius += 2.5f;
                pulseAlpha = Math.max(0f, 0.5f - pulseRadius / 160f);
                if (pulseRadius > 160) { pulseRadius = 0; pulseAlpha = 0.5f; }

                if (tick % 2 == 0 && particles.size() < 80)
                    particles.add(new Particle(getWidth(), getHeight() / 2 - 60));
            }

            particles.removeIf(p -> !p.update());

            // Fade out (tick 110-136)
            if (tick > 110) {
                counter = 100;
                barProgress = 1f;
                alpha = Math.max(0f, 1f - easeInCubic((tick - 110f) / 26f));
            }

            repaint();

            if (tick > 136) {
                masterTimer.stop();
                dispose();
                onComplete.run();
            }
        });

        // Tampilkan dialog lalu mulai timer
        SwingUtilities.invokeLater(() -> {
            masterTimer.start();
            setVisible(true); // blocking karena modal, timer tetap jalan di EDT
        });
    }
}
