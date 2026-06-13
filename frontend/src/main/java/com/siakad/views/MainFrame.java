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
    public static final String PANEL_INPUT_NILAI = "akademik.inputNilai";
    public static final String PANEL_INPUT_KEHADIRAN = "akademik.inputKehadiran";
    public static final String PANEL_LIHAT_NILAI = "akademik.lihatNilai";
    public static final String PANEL_REKAP_ABSENSI = "akademik.rekapAbsensi";
    public static final String PANEL_PENGATURAN_AKADEMIK = "akademik.pengaturan";
    public static final String PANEL_NILAI_SAYA = "akademik.nilaiSaya";
    public static final String PANEL_KEHADIRAN_SAYA = "akademik.kehadiranSaya";
    public static final String PANEL_INFO_AKADEMIK = "akademik.infoAkademik";
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
        contentPanel.add(new AkademikPanel(), PANEL_INPUT_NILAI);
        contentPanel.add(new InputKehadiranPanel(), PANEL_INPUT_KEHADIRAN);
        contentPanel.add(new LihatNilaiMahasiswaPanel(), PANEL_LIHAT_NILAI);
        contentPanel.add(new RekapAbsensiPanel(), PANEL_REKAP_ABSENSI);
        contentPanel.add(new PengaturanAkademikPanel(), PANEL_PENGATURAN_AKADEMIK);
        contentPanel.add(new NilaiSayaPanel(), PANEL_NILAI_SAYA);
        contentPanel.add(new KehadiranSayaPanel(), PANEL_KEHADIRAN_SAYA);
        contentPanel.add(new InfoAkademikPanel(), PANEL_INFO_AKADEMIK);
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
        JPanel akademikSubmenu = buildAkademikSubmenu();
        JButton btnAkademik   = buildNavButton("A+", "Akademik",        null);
        setNavChevron(btnAkademik, false);
        btnAkademik.addActionListener(e -> {
            boolean expanded = !akademikSubmenu.isVisible();
            akademikSubmenu.setVisible(expanded);
            setNavChevron(btnAkademik, expanded);
            navPanel.revalidate();
            navPanel.repaint();
        });
        JButton btnLaporan    = buildNavButton("📋", "Laporan & Cetak", PANEL_LAPORAN);
        JButton btnLogoutNav  = buildLogoutNavButton();

        navPanel.add(btnDashboard);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(btnMahasiswa);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(btnPembayaran);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(btnKrsJadwal);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(btnAkademik);
        navPanel.add(akademikSubmenu);
        navPanel.add(Box.createVerticalStrut(4));

        if (JwtHelper.getInstance().isAdmin()) {
            navPanel.add(btnLaporan);
            navPanel.add(Box.createVerticalStrut(4));
        }
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(btnLogoutNav);
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

    private JPanel buildAkademikSubmenu() {
        JPanel submenu = new JPanel();
        submenu.setOpaque(false);
        submenu.setLayout(new BoxLayout(submenu, BoxLayout.Y_AXIS));
        submenu.setBorder(new EmptyBorder(2, 20, 6, 0));
        submenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (JwtHelper.getInstance().isAdmin()) {
            submenu.add(buildSubNavButton("A+", "Input Nilai", PANEL_INPUT_NILAI));
            submenu.add(Box.createVerticalStrut(3));
            submenu.add(buildSubNavButton("✓", "Input Kehadiran", PANEL_INPUT_KEHADIRAN));
            submenu.add(Box.createVerticalStrut(3));
            submenu.add(buildSubNavButton("★", "Lihat Nilai Mahasiswa", PANEL_LIHAT_NILAI));
            submenu.add(Box.createVerticalStrut(3));
            submenu.add(buildSubNavButton("Σ", "Rekap Absensi", PANEL_REKAP_ABSENSI));
            submenu.add(Box.createVerticalStrut(3));
            submenu.add(buildSubNavButton("⚙", "Pengaturan Akademik", PANEL_PENGATURAN_AKADEMIK));
        } else {
            submenu.add(buildSubNavButton("A+", "Nilai Saya", PANEL_NILAI_SAYA));
            submenu.add(Box.createVerticalStrut(3));
            submenu.add(buildSubNavButton("✓", "Kehadiran Saya", PANEL_KEHADIRAN_SAYA));
            submenu.add(Box.createVerticalStrut(3));
            submenu.add(buildSubNavButton("i", "Info Akademik", PANEL_INFO_AKADEMIK));
        }
        submenu.setVisible(false);
        return submenu;
    }

    private JButton buildSubNavButton(String icon, String label, String panelName) {
        JButton btn = new JButton() {
            boolean active = false;

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (active) {
                    g2.setColor(new Color(59, 130, 246, 18));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(SIDEBAR_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                g2.setColor(active ? new Color(147, 197, 253) : TEXT_DIM);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(icon, 10, getHeight() / 2 + 4);
                g2.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
                g2.setColor(active ? new Color(219, 234, 254) : TEXT_MUTED);
                g2.drawString(label, 30, getHeight() / 2 + 5);
                g2.dispose();
            }

            public void setActive(boolean a) { active = a; repaint(); }
            public boolean isActive() { return active; }
        };
        btn.setPreferredSize(new Dimension(196, 34));
        btn.setMaximumSize(new Dimension(196, 34));
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

    private void setNavChevron(JButton btn, boolean expanded) {
        btn.putClientProperty("chevron", expanded ? "⌃" : "⌄");
        btn.repaint();
    }

    private JButton buildLogoutNavButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getModel().isRollover() ? new Color(127, 29, 29, 190) : new Color(127, 29, 29, 90);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(239, 68, 68, 90));
                g2.fillRoundRect(0, 2, 3, getHeight() - 4, 3, 3);

                drawDoorIcon(g2, 15, getHeight() / 2 - 9, 16, new Color(254, 202, 202));

                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(new Color(254, 226, 226));
                g2.drawString("Logout", 42, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(216, 42));
        btn.setMaximumSize(new Dimension(216, 42));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setToolTipText("Keluar dari akun");
        btn.addActionListener(e -> doLogout());
        return btn;
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

                Object chevron = getClientProperty("chevron");
                if (chevron != null) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    g2.setColor(active || getModel().isRollover() ? new Color(147, 197, 253) : TEXT_DIM);
                    String arrow = String.valueOf(chevron);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(arrow, getWidth() - fm.stringWidth(arrow) - 16, getHeight() / 2 + 6);
                }

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
            if (panelName != null) {
                showPanel(panelName);
                setActiveButton(btn);
            }
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
                int iconSize = 14;
                String txt = "Keluar";
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int totalWidth = iconSize + 8 + fm.stringWidth(txt);
                int startX = (getWidth() - totalWidth) / 2;
                drawDoorIcon(g2, startX, getHeight() / 2 - 8, iconSize, new Color(254, 202, 202));
                g2.setColor(new Color(252, 165, 165));
                g2.drawString(txt, startX + iconSize + 8,
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
        if (showLogoutDialog()) {
            AuthService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private boolean showLogoutDialog() {
        final boolean[] confirmed = {false};
        JDialog dialog = new JDialog(this, "Konfirmasi Logout", true);
        dialog.setUndecorated(true);
        dialog.setSize(430, 248);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(8, 12, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(239, 68, 68, 85));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(239, 68, 68, 55));
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 26, 22, 26));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 16);
        gc.anchor = GridBagConstraints.NORTH;

        JPanel iconWrap = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(127, 29, 29, 105));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(239, 68, 68, 55));
                g2.fillOval(9, 9, 48, 48);
                drawDoorIcon(g2, 23, 19, 24, new Color(254, 226, 226));
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(66, 66); }
        };
        iconWrap.setOpaque(false);

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setBorder(new EmptyBorder(2, 0, 0, 0));

        JLabel title = new JLabel("Keluar dari akun?");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea message = new JTextArea("Sesi admin akan ditutup dan Anda akan kembali ke halaman login.");
        message.setPreferredSize(new Dimension(292, 52));
        message.setMaximumSize(new Dimension(292, 52));
        message.setOpaque(false);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        message.setForeground(TEXT_MUTED);
        message.setBorder(new EmptyBorder(7, 0, 0, 0));
        message.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBlock.add(title);
        textBlock.add(message);
        gc.gridx = 0;
        content.add(iconWrap, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, 0);
        content.add(textBlock, gc);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(18, 82, 0, 0));
        JButton cancel = dialogButton("Tetap di Dashboard", new Color(18, 26, 48), TEXT_PRIMARY);
        JButton logout = dialogButton("Ya, Logout", new Color(185, 28, 28), new Color(254, 226, 226));
        cancel.addActionListener(e -> dialog.dispose());
        logout.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });
        actions.add(cancel);
        actions.add(logout);

        root.add(content, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
        return confirmed[0];
    }

    private static void drawDoorIcon(Graphics2D g2, int x, int y, int size, Color color) {
        int w = Math.max(10, size);
        int h = Math.max(14, (int) (size * 1.25));
        g2.setStroke(new BasicStroke(Math.max(1.4f, size / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        g2.drawRoundRect(x + 2, y, w - 4, h, 2, 2);
        g2.drawLine(x + w - 4, y + 2, x + w - 4, y + h - 2);
        g2.fillOval(x + w - 7, y + h / 2 - 1, Math.max(2, size / 6), Math.max(2, size / 6));
        g2.drawLine(x, y + h, x + w + 2, y + h);
    }

    private JButton dialogButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color paint = getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(paint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.setColor(new Color(255, 255, 255, 24));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                g2.setColor(fg);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(150, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
