package com.siakad.views;

import com.siakad.services.AuthService;
import com.siakad.utils.Config;
import com.siakad.utils.JwtHelper;
import com.siakad.views.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * MainFrame - Frame utama aplikasi setelah login
 * Sidebar navigasi modern + content panel
 */
public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private AnimatedContentPanel animatedContentPanel;
    private CardLayout cardLayout;
    private JButton activeBtn;

    public static final String PANEL_DASHBOARD  = "dashboard";
    public static final String PANEL_MAHASISWA  = "mahasiswa";
    public static final String PANEL_PEMBAYARAN = "pembayaran";
    public static final String PANEL_KRS_JADWAL = "krs_jadwal";
    public static final String PANEL_LAPORAN    = "laporan";

    // Warna tema
    private static final Color SIDEBAR_BG    = new Color(10, 15, 30);
    private static final Color SIDEBAR_HOVER = new Color(255, 255, 255, 10);
    private static final Color SIDEBAR_ACTIVE = new Color(59, 130, 246, 25);
    private static final Color ACCENT_BLUE   = new Color(59, 130, 246);
    private static final Color ACCENT_INDIGO = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY  = new Color(248, 250, 252);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color TEXT_DIM      = new Color(71, 85, 105);
    private static final Color BORDER_COLOR  = new Color(20, 30, 55);
    private static final Color CONTENT_BG    = new Color(13, 19, 38);

    public MainFrame() {
        setTitle(Config.APP_NAME + " — " + JwtHelper.getInstance().getUsername().toUpperCase());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Config.DEFAULT_WIDTH, Config.DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        animatedContentPanel = new AnimatedContentPanel(cardLayout);
        contentPanel = animatedContentPanel;
        contentPanel.setBackground(CONTENT_BG);
        contentPanel.add(new DashboardPanel(), PANEL_DASHBOARD);
        contentPanel.add(new MahasiswaPanel(), PANEL_MAHASISWA);
        contentPanel.add(new PembayaranPanel(), PANEL_PEMBAYARAN);
        contentPanel.add(new KrsJadwalPanel(), PANEL_KRS_JADWAL);
        contentPanel.add(new LaporanPanel(), PANEL_LAPORAN);
        add(contentPanel, BorderLayout.CENTER);

        showPanel(PANEL_DASHBOARD);
    }

    private static float easeOutCubic(float value) {
        float t = Math.max(0f, Math.min(1f, value));
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 15, 30),
                        0, getHeight(), new Color(8, 12, 25));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Right border
                GradientPaint border = new GradientPaint(0, 0, ACCENT_BLUE,
                        0, getHeight(), ACCENT_INDIGO);
                g2.setPaint(border);
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());

                // Subtle dot pattern
                g2.setColor(new Color(255, 255, 255, 5));
                for (int x = 10; x < getWidth(); x += 20) {
                    for (int y = 10; y < getHeight(); y += 20) {
                        g2.fillOval(x, y, 1, 1);
                    }
                }
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setLayout(new BorderLayout());

        // ── Logo area ──
        JPanel logoPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_COLOR);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(28, 20, 20, 20));

        // Logo icon with gradient circle
        JLabel lblLogo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246, 25));
                g2.fillOval(0, 0, 56, 56);
                GradientPaint gp = new GradientPaint(6, 6, ACCENT_BLUE, 50, 50, ACCENT_INDIGO);
                g2.setPaint(gp);
                g2.fillOval(6, 6, 44, 44);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("🎓", (56 - fm.stringWidth("🎓")) / 2, 36);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(56, 56); }
            @Override public Dimension getMaximumSize() { return new Dimension(56, 56); }
        };
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAppName = new JLabel("SIAKAD");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAppName.setForeground(TEXT_PRIMARY);
        lblAppName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFullName = new JLabel("Sistem Informasi Akademik");
        lblFullName.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFullName.setForeground(TEXT_MUTED);
        lblFullName.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(lblLogo);
        logoPanel.add(Box.createVerticalStrut(12));
        logoPanel.add(lblAppName);
        logoPanel.add(Box.createVerticalStrut(2));
        logoPanel.add(lblFullName);

        // ── Nav menu ──
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(16, 12, 16, 12));

        JLabel lblMenuSection = makeMenuSection("NAVIGASI");
        navPanel.add(lblMenuSection);
        navPanel.add(Box.createVerticalStrut(8));

        JButton btnDashboard  = buildNavButton("📊", "Dashboard",       PANEL_DASHBOARD);
        JButton btnMahasiswa  = buildNavButton("👨‍🎓", "Data Mahasiswa",  PANEL_MAHASISWA);
        JButton btnPembayaran = buildNavButton("💳", "Pembayaran UKT",  PANEL_PEMBAYARAN);
        JButton btnKrsJadwal  = buildNavButton("📚", "KRS & Jadwal Kuliah", PANEL_KRS_JADWAL);
        JButton btnLaporan    = buildNavButton("📋", "Laporan & Cetak", PANEL_LAPORAN);

        navPanel.add(btnDashboard);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(btnMahasiswa);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(btnPembayaran);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(btnKrsJadwal);
        navPanel.add(Box.createVerticalStrut(4));

        if (JwtHelper.getInstance().isAdmin()) {
            navPanel.add(btnLaporan);
            navPanel.add(Box.createVerticalStrut(4));
        }
        setActiveButton(btnDashboard);

        // ── Bottom: user card + logout ──
        JPanel bottomPanel = buildUserCard();

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JLabel makeMenuSection(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(TEXT_DIM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton buildNavButton(String icon, String label, String panelName) {
        JButton btn = new JButton() {
            boolean active = false;

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (active) {
                    // Active state: blue tinted background + left accent bar
                    g2.setColor(SIDEBAR_ACTIVE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, 0, getHeight(), ACCENT_INDIGO);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 2, 3, getHeight() - 4, 3, 3);
                } else if (getModel().isRollover()) {
                    g2.setColor(SIDEBAR_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                // Icon
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2.setColor(active ? new Color(147, 197, 253) : TEXT_MUTED);
                g2.drawString(icon, 14, getHeight() / 2 + 6);

                // Label
                g2.setFont(new Font("Segoe UI", Font.PLAIN + (active ? Font.BOLD : 0), 13));
                g2.setColor(active ? new Color(219, 234, 254) : TEXT_MUTED);
                g2.drawString(label, 42, getHeight() / 2 + 5);

                g2.dispose();
            }

            public void setActive(boolean a) { active = a; repaint(); }
            public boolean isActive() { return active; }
        };
        btn.setPreferredSize(new Dimension(216, 42));
        btn.setMaximumSize(new Dimension(216, 42));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            showPanel(panelName);
            setActiveButton(btn);
        });
        return btn;
    }

    private JPanel buildUserCard() {
        JwtHelper jwt = JwtHelper.getInstance();

        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_COLOR);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 14, 16, 14));

        // User avatar + info
        JPanel userRow = new JPanel(new BorderLayout(10, 0));
        userRow.setOpaque(false);
        userRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Avatar circle
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color avatarColor = jwt.isAdmin() ? new Color(59, 130, 246) : new Color(34, 197, 94);
                g2.setColor(avatarColor.darker());
                g2.fillOval(0, 0, 38, 38);
                g2.setColor(avatarColor);
                g2.fillOval(2, 2, 34, 34);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initial = jwt.getUsername().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (38 - fm.stringWidth(initial)) / 2, 25);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(38, 38); }
            @Override public Dimension getMinimumSize() { return new Dimension(38, 38); }
        };

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));

        JLabel lblUsername = new JLabel(jwt.getUsername());
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(TEXT_PRIMARY);

        // Role badge
        JLabel lblRole = new JLabel("  " + jwt.getRole().toUpperCase() + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color roleColor = jwt.isAdmin() ? new Color(59, 130, 246) : new Color(34, 197, 94);
                g2.setColor(new Color(roleColor.getRed(), roleColor.getGreen(), roleColor.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblRole.setForeground(jwt.isAdmin() ? ACCENT_BLUE : new Color(34, 197, 94));
        lblRole.setOpaque(false);

        userInfo.add(lblUsername);
        userInfo.add(Box.createVerticalStrut(2));
        userInfo.add(lblRole);

        userRow.add(avatar, BorderLayout.WEST);
        userRow.add(userInfo, BorderLayout.CENTER);

        // Logout button
        JButton btnLogout = new JButton("Keluar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? new Color(185, 28, 28) : new Color(127, 29, 29, 180);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(252, 165, 165));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "⏻  Keluar";
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnLogout.setPreferredSize(new Dimension(212, 34));
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.addActionListener(e -> doLogout());

        panel.add(userRow);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnLogout);
        return panel;
    }

    private void setActiveButton(JButton btn) {
        if (activeBtn != null) {
            try {
                java.lang.reflect.Method method = activeBtn.getClass().getDeclaredMethod("setActive", boolean.class);
                method.setAccessible(true);
                method.invoke(activeBtn, false);
            }
            catch (Exception ignored) {}
        }
        try {
            java.lang.reflect.Method method = btn.getClass().getDeclaredMethod("setActive", boolean.class);
            method.setAccessible(true);
            method.invoke(btn, true);
        }
        catch (Exception ignored) {}
        activeBtn = btn;
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        if (animatedContentPanel != null) {
            animatedContentPanel.playReveal();
        }
    }

    public void playEntranceAnimation() {
        StartupGlassPane glass = new StartupGlassPane();
        setGlassPane(glass);
        glass.setVisible(true);
        glass.play(() -> glass.setVisible(false));
        if (animatedContentPanel != null) {
            animatedContentPanel.playReveal();
        }
    }

    private static class AnimatedContentPanel extends JPanel {
        private float reveal = 1f;
        private Timer timer;

        AnimatedContentPanel(LayoutManager layout) {
            super(layout);
        }

        void playReveal() {
            if (timer != null) {
                timer.stop();
            }
            reveal = 0f;
            timer = new Timer(16, e -> {
                reveal = Math.min(1f, reveal + 0.07f);
                repaint();
                if (reveal >= 1f) {
                    timer.stop();
                }
            });
            timer.start();
        }

        @Override protected void paintChildren(Graphics g) {
            if (reveal >= 0.999f) {
                super.paintChildren(g);
                return;
            }

            float eased = easeOutCubic(reveal);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.12f, eased)));
            g2.translate((int) ((1f - eased) * 30), 0);
            super.paintChildren(g2);
            g2.dispose();

            Graphics2D overlay = (Graphics2D) g.create();
            overlay.setColor(new Color(13, 19, 38, (int) (130 * (1f - eased))));
            overlay.fillRect(0, 0, getWidth(), getHeight());
            overlay.setColor(new Color(34, 211, 238, (int) (90 * (1f - eased))));
            overlay.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            overlay.dispose();
        }
    }

    private static class StartupGlassPane extends JComponent {
        private float progress = 0f;
        private Timer timer;

        void play(Runnable onDone) {
            if (timer != null) {
                timer.stop();
            }
            progress = 0f;
            timer = new Timer(16, e -> {
                progress = Math.min(1f, progress + 0.045f);
                repaint();
                if (progress >= 1f) {
                    timer.stop();
                    onDone.run();
                }
            });
            timer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            float eased = easeOutCubic(progress);
            float out = 1f - eased;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, out));
            g2.setColor(CONTENT_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int sweepX = 240 + (int) ((getWidth() - 240) * eased);
            GradientPaint sweep = new GradientPaint(sweepX - 90, 0, new Color(34, 211, 238, 0),
                    sweepX, 0, new Color(34, 211, 238, 105));
            g2.setPaint(sweep);
            g2.fillRect(Math.max(240, sweepX - 90), 0, 120, getHeight());

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(0.75f, out + 0.15f)));
            g2.setColor(new Color(248, 250, 252));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            String text = "Dashboard siap";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, getWidth() - fm.stringWidth(text) - 34, getHeight() - 34);
            g2.dispose();
        }
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?", "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
