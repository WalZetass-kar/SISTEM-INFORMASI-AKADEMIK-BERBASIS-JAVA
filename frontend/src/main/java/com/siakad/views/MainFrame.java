package com.siakad.views;

import com.siakad.services.AuthService;
import com.siakad.utils.AppTheme;
import com.siakad.utils.Config;
import com.siakad.utils.JwtHelper;
import com.siakad.views.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton activeBtn;
    private JButton btnThemeToggle;
    private JPanel topbarClock;
    private Timer clockTimer;

    public static final String PANEL_DASHBOARD  = "dashboard";
    public static final String PANEL_MAHASISWA  = "mahasiswa";
    public static final String PANEL_PEMBAYARAN = "pembayaran";
    public static final String PANEL_INPUT_KRS = "input_krs";
    public static final String PANEL_MATA_KULIAH = "mata_kuliah";
    public static final String PANEL_JADWAL_KULIAH = "jadwal_kuliah";
    public static final String PANEL_INPUT_NILAI = "akademik.inputNilai";
    public static final String PANEL_INPUT_KEHADIRAN = "akademik.inputKehadiran";
    public static final String PANEL_LIHAT_NILAI = "akademik.lihatNilai";
    public static final String PANEL_REKAP_ABSENSI = "akademik.rekapAbsensi";
    public static final String PANEL_PENGATURAN_AKADEMIK = "akademik.pengaturan";
    public static final String PANEL_NILAI_SAYA = "akademik.nilaiSaya";
    public static final String PANEL_KEHADIRAN_SAYA = "akademik.kehadiranSaya";
    public static final String PANEL_INFO_AKADEMIK = "akademik.infoAkademik";
    public static final String PANEL_LAPORAN    = "laporan";

    private static Color SIDEBAR_BG()     { return AppTheme.sidebar(); }
    private static Color SIDEBAR_HOVER()  { return AppTheme.sidebarHover(); }
    private static Color SIDEBAR_ACTIVE() { return AppTheme.sidebarActive(); }
    private static Color ACCENT_BLUE()    { return AppTheme.blue(); }
    private static Color TEXT_PRIMARY()    { return Color.WHITE; }
    private static Color TEXT_MUTED()     { return AppTheme.sidebarMuted(); }
    private static Color TEXT_DIM()       { return new Color(100, 116, 139); }
    private static Color BORDER_COLOR()   { return AppTheme.border(); }
    private static Color CONTENT_BG()     { return AppTheme.bg(); }
    private static Color TOPBAR_BG()      { return AppTheme.topbar(); }
    private static Color GREEN()          { return AppTheme.green(); }
    private static Color YELLOW()         { return AppTheme.yellow(); }
    private static Color CYAN()           { return AppTheme.cyan(); }

    public MainFrame() {
        setTitle(Config.APP_NAME + " — " + JwtHelper.getInstance().getUsername().toUpperCase());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Config.DEFAULT_WIDTH, Config.DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));
        initUI();
        startClock();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(CONTENT_BG());
        rightPanel.add(buildTopbar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG());
        contentPanel.add(new DashboardPanel(), PANEL_DASHBOARD);
        contentPanel.add(new MahasiswaPanel(), PANEL_MAHASISWA);
        contentPanel.add(new PembayaranPanel(), PANEL_PEMBAYARAN);
        contentPanel.add(new KrsJadwalPanel(KrsJadwalPanel.PageMode.INPUT_KRS), PANEL_INPUT_KRS);
        contentPanel.add(new KrsJadwalPanel(KrsJadwalPanel.PageMode.MATA_KULIAH), PANEL_MATA_KULIAH);
        contentPanel.add(new KrsJadwalPanel(KrsJadwalPanel.PageMode.JADWAL_KULIAH), PANEL_JADWAL_KULIAH);
        contentPanel.add(new AkademikPanel(), PANEL_INPUT_NILAI);
        contentPanel.add(new InputKehadiranPanel(), PANEL_INPUT_KEHADIRAN);
        contentPanel.add(new LihatNilaiMahasiswaPanel(), PANEL_LIHAT_NILAI);
        contentPanel.add(new RekapAbsensiPanel(), PANEL_REKAP_ABSENSI);
        contentPanel.add(new PengaturanAkademikPanel(), PANEL_PENGATURAN_AKADEMIK);
        contentPanel.add(new NilaiSayaPanel(), PANEL_NILAI_SAYA);
        contentPanel.add(new KehadiranSayaPanel(), PANEL_KEHADIRAN_SAYA);
        contentPanel.add(new InfoAkademikPanel(), PANEL_INFO_AKADEMIK);
        contentPanel.add(new LaporanPanel(), PANEL_LAPORAN);

        rightPanel.add(contentPanel, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
        showPanel(PANEL_DASHBOARD);
    }

    private JPanel buildTopbar() {
        JPanel topbar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(TOPBAR_BG());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 0, 0, AppTheme.isDark() ? 30 : 10));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        topbar.setOpaque(false);
        topbar.setLayout(new BorderLayout());
        topbar.setPreferredSize(new Dimension(0, 52));
        topbar.setBorder(new EmptyBorder(0, 24, 0, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel searchIcon = new JLabel("  ");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        left.add(searchIcon);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        topbarClock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        topbarClock.setOpaque(false);
        JLabel clockIcon = new JLabel(" \u23F0 ");
        clockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        clockIcon.setForeground(AppTheme.muted());
        JLabel clockLabel = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clockLabel.setForeground(AppTheme.text());
        clockLabel.setName("clockLabel");
        topbarClock.add(clockIcon);
        topbarClock.add(clockLabel);

        JButton btnTheme = buildTopbarBtn(AppTheme.toggleText());
        btnTheme.addActionListener(e -> {
            AppTheme.toggle();
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        });

        JPanel userChip = buildUserChip();

        right.add(topbarClock);
        right.add(btnTheme);
        right.add(userChip);

        topbar.add(left, BorderLayout.WEST);
        topbar.add(right, BorderLayout.EAST);
        return topbar;
    }

    private JPanel buildUserChip() {
        JwtHelper jwt = JwtHelper.getInstance();
        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(AppTheme.isDark() ? 30 : 241, AppTheme.isDark() ? 41 : 245, AppTheme.isDark() ? 59 : 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        chip.setBorder(new EmptyBorder(2, 2, 2, 12));

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = jwt.isAdmin() ? ACCENT_BLUE() : GREEN();
                g2.setColor(c);
                g2.fillOval(0, 0, 30, 30);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String init = jwt.getUsername().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init, (30 - fm.stringWidth(init)) / 2, 21);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(30, 30); }
            @Override public Dimension getMinimumSize() { return new Dimension(30, 30); }
        };

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(jwt.getUsername());
        name.setFont(new Font("Segoe UI", Font.BOLD, 11));
        name.setForeground(AppTheme.text());

        JLabel role = new JLabel(jwt.getRole().toUpperCase());
        role.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        role.setForeground(AppTheme.muted());

        info.add(name);
        info.add(role);

        chip.add(avatar);
        chip.add(info);
        return chip;
    }

    private JButton buildTopbarBtn(String text) {
        JButton btn = new JButton(text) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? new Color(AppTheme.isDark() ? 51 : 226, AppTheme.isDark() ? 65 : 232, AppTheme.isDark() ? 85 : 240) : new Color(0,0,0,0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(AppTheme.muted());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(120, 32));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void startClock() {
        clockTimer = new Timer(15000, e -> {
            if (topbarClock != null) {
                for (Component c : topbarClock.getComponents()) {
                    if ("clockLabel".equals(c.getName())) {
                        ((JLabel) c).setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
                        break;
                    }
                }
            }
        });
        clockTimer.start();
    }

    private enum NavIcon { DASHBOARD, MAHASISWA, PEMBAYARAN, KRS, AKADEMIK, LAPORAN, SETTINGS, NILAI, ABSENSI, INFO }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SIDEBAR_BG());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(232, 0));
        sidebar.setLayout(new BorderLayout());

        JPanel logoPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(51, 65, 85, 80));
                g2.fillRect(16, getHeight() - 1, getWidth() - 32, 1);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(22, 20, 18, 20));

        JLabel lblLogo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE(), 48, 48, new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 42, 42, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(10, 10, 22, 22, 6, 6);
                g2.fillRect(14, 14, 14, 2);
                g2.fillRect(14, 20, 10, 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(42, 42); }
            @Override public Dimension getMaximumSize() { return new Dimension(42, 42); }
        };
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAppName = new JLabel("SIAKAD");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAppName.setForeground(TEXT_PRIMARY());
        lblAppName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFullName = new JLabel("Sistem Informasi Akademik");
        lblFullName.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFullName.setForeground(TEXT_DIM());
        lblFullName.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(lblLogo);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(lblAppName);
        logoPanel.add(Box.createVerticalStrut(2));
        logoPanel.add(lblFullName);

        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(12, 10, 10, 10));

        JLabel lblMenuSection = makeMenuSection("NAVIGASI");
        navPanel.add(lblMenuSection);
        navPanel.add(Box.createVerticalStrut(6));

        JButton btnDashboard  = buildNavButton(NavIcon.DASHBOARD, "Dashboard", PANEL_DASHBOARD);
        JButton btnMahasiswa  = buildNavButton(NavIcon.MAHASISWA, "Data Mahasiswa", PANEL_MAHASISWA);
        JButton btnPembayaran = buildNavButton(NavIcon.PEMBAYARAN, "Pembayaran UKT", PANEL_PEMBAYARAN);
        JPanel krsJadwalSubmenu = buildKrsJadwalSubmenu();
        JButton btnKrsJadwal = buildNavButton(NavIcon.KRS, "KRS & Jadwal", null);
        setNavChevron(btnKrsJadwal, false);
        btnKrsJadwal.addActionListener(e -> {
            boolean expanded = !krsJadwalSubmenu.isVisible();
            krsJadwalSubmenu.setVisible(expanded);
            setNavChevron(btnKrsJadwal, expanded);
            navPanel.revalidate();
            navPanel.repaint();
        });
        JPanel akademikSubmenu = buildAkademikSubmenu();
        JButton btnAkademik   = buildNavButton(NavIcon.AKADEMIK, "Akademik", null);
        setNavChevron(btnAkademik, false);
        btnAkademik.addActionListener(e -> {
            boolean expanded = !akademikSubmenu.isVisible();
            akademikSubmenu.setVisible(expanded);
            setNavChevron(btnAkademik, expanded);
            navPanel.revalidate();
            navPanel.repaint();
        });
        JButton btnLaporan    = buildNavButton(NavIcon.LAPORAN, "Laporan & Cetak", PANEL_LAPORAN);

        navPanel.add(btnDashboard);
        navPanel.add(Box.createVerticalStrut(3));
        navPanel.add(btnMahasiswa);
        navPanel.add(Box.createVerticalStrut(3));
        navPanel.add(btnPembayaran);
        navPanel.add(Box.createVerticalStrut(3));
        navPanel.add(btnKrsJadwal);
        navPanel.add(krsJadwalSubmenu);
        navPanel.add(Box.createVerticalStrut(3));
        navPanel.add(btnAkademik);
        navPanel.add(akademikSubmenu);
        navPanel.add(Box.createVerticalStrut(3));
        if (JwtHelper.getInstance().isAdmin()) {
            navPanel.add(btnLaporan);
            navPanel.add(Box.createVerticalStrut(3));
        }
        btnThemeToggle = buildThemeButton();
        JButton btnLogoutNav = buildLogoutNavButton();

        JScrollPane navScroll = new JScrollPane(navPanel);
        navScroll.setBorder(null);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        navScroll.getVerticalScrollBar().setUnitIncrement(14);
        styleSidebarScrollBar(navScroll.getVerticalScrollBar());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(8, 10, 12, 10));
        bottomPanel.add(btnThemeToggle);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(btnLogoutNav);
        setActiveButton(btnDashboard);

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(navScroll, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private void styleSidebarScrollBar(JScrollBar scrollBar) {
        scrollBar.setPreferredSize(new Dimension(8, 0));
        scrollBar.setOpaque(false);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(100, 116, 139, 130);
                trackColor = SIDEBAR_BG();
            }

            @Override protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    private JLabel makeMenuSection(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(TEXT_DIM());
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildKrsJadwalSubmenu() {
        JPanel submenu = new JPanel();
        submenu.setOpaque(false);
        submenu.setLayout(new BoxLayout(submenu, BoxLayout.Y_AXIS));
        submenu.setBorder(new EmptyBorder(2, 18, 6, 0));
        submenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        submenu.add(buildSubNavButton(NavIcon.KRS, "Input KRS", PANEL_INPUT_KRS));
        submenu.add(Box.createVerticalStrut(2));
        submenu.add(buildSubNavButton(NavIcon.KRS, "Mata Kuliah", PANEL_MATA_KULIAH));
        submenu.add(Box.createVerticalStrut(2));
        submenu.add(buildSubNavButton(NavIcon.KRS, "Jadwal Kuliah", PANEL_JADWAL_KULIAH));
        submenu.setVisible(false);
        return submenu;
    }

    private JPanel buildAkademikSubmenu() {
        JPanel submenu = new JPanel();
        submenu.setOpaque(false);
        submenu.setLayout(new BoxLayout(submenu, BoxLayout.Y_AXIS));
        submenu.setBorder(new EmptyBorder(2, 18, 6, 0));
        submenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (JwtHelper.getInstance().isAdmin()) {
            submenu.add(buildSubNavButton(NavIcon.NILAI, "Input Nilai", PANEL_INPUT_NILAI));
            submenu.add(Box.createVerticalStrut(2));
            submenu.add(buildSubNavButton(NavIcon.ABSENSI, "Input Kehadiran", PANEL_INPUT_KEHADIRAN));
            submenu.add(Box.createVerticalStrut(2));
            submenu.add(buildSubNavButton(NavIcon.NILAI, "Lihat Nilai", PANEL_LIHAT_NILAI));
            submenu.add(Box.createVerticalStrut(2));
            submenu.add(buildSubNavButton(NavIcon.DASHBOARD, "Rekap Absensi", PANEL_REKAP_ABSENSI));
            submenu.add(Box.createVerticalStrut(2));
            submenu.add(buildSubNavButton(NavIcon.SETTINGS, "Pengaturan", PANEL_PENGATURAN_AKADEMIK));
        } else {
            submenu.add(buildSubNavButton(NavIcon.NILAI, "Nilai Saya", PANEL_NILAI_SAYA));
            submenu.add(Box.createVerticalStrut(2));
            submenu.add(buildSubNavButton(NavIcon.ABSENSI, "Kehadiran Saya", PANEL_KEHADIRAN_SAYA));
            submenu.add(Box.createVerticalStrut(2));
            submenu.add(buildSubNavButton(NavIcon.INFO, "Info Akademik", PANEL_INFO_AKADEMIK));
        }
        submenu.setVisible(false);
        return submenu;
    }

    private JButton buildSubNavButton(NavIcon icon, String label, String panelName) {
        JButton btn = new JButton() {
            boolean active = false;
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(new Color(79, 70, 229, 28));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(ACCENT_BLUE());
                    g2.fillRoundRect(0, 6, 3, getHeight() - 12, 3, 3);
                } else if (hover) {
                    g2.setColor(SIDEBAR_HOVER());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                
                drawNavIcon(g2, 10, getHeight() / 2 - 8, 16, icon, active ? ACCENT_BLUE() : TEXT_DIM());
                
                g2.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
                g2.setColor(active ? Color.WHITE : TEXT_MUTED());
                g2.drawString(label, 34, getHeight() / 2 + 5);
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
        btn.putClientProperty("chevron", expanded ? "\u2303" : "\u2304");
        btn.repaint();
    }

    private JButton buildNavButton(NavIcon icon, String label, String panelName) {
        JButton btn = new JButton() {
            boolean active = false;
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(SIDEBAR_ACTIVE());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(255, 255, 255, 24));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 10, 10);
                } else if (hover) {
                    g2.setColor(SIDEBAR_HOVER());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                
                drawNavIcon(g2, 14, getHeight() / 2 - 10, 20, icon, active ? Color.WHITE : TEXT_MUTED());
                
                g2.setFont(new Font("Segoe UI", Font.PLAIN + (active ? Font.BOLD : 0), 13));
                g2.setColor(active ? Color.WHITE : TEXT_MUTED());
                g2.drawString(label, 46, getHeight() / 2 + 5);
                
                Object chevron = getClientProperty("chevron");
                if (chevron != null) {
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(active || hover ? Color.WHITE : TEXT_DIM());
                    boolean expanded = "\u2303".equals(chevron);
                    int cx = getWidth() - 24, cy = getHeight() / 2;
                    if (expanded) {
                        g2.drawLine(cx - 4, cy + 2, cx, cy - 2);
                        g2.drawLine(cx, cy - 2, cx + 4, cy + 2);
                    } else {
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    }
                }
                g2.dispose();
            }
            public void setActive(boolean a) { active = a; repaint(); }
            public boolean isActive() { return active; }
        };
        btn.setPreferredSize(new Dimension(212, 40));
        btn.setMaximumSize(new Dimension(212, 40));
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

    private void drawNavIcon(Graphics2D g2, int x, int y, int size, NavIcon icon, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (icon) {
            case DASHBOARD -> {
                g2.drawRect(x, y + size/2, size/3 - 1, size/2);
                g2.drawRect(x + size/3 + 1, y, size/3 - 1, size);
                g2.drawRect(x + 2*size/3 + 2, y + size/3, size/3 - 1, 2*size/3);
            }
            case MAHASISWA -> {
                g2.drawOval(x + size/4, y, size/2, size/2);
                g2.drawArc(x, y + size/2, size, size/2, 0, 180);
            }
            case PEMBAYARAN -> {
                g2.drawRoundRect(x, y + 2, size, size - 4, 3, 3);
                g2.fillRect(x, y + 6, size, 3);
            }
            case KRS -> {
                g2.drawRoundRect(x + 2, y, size - 4, size, 2, 2);
                g2.drawLine(x + 5, y + 4, x + size - 5, y + 4);
                g2.drawLine(x + 5, y + 8, x + size - 5, y + 8);
                g2.drawLine(x + 5, y + 12, x + size - 5, y + 12);
            }
            case AKADEMIK -> {
                int[] px = {x, x + size/2, x + size, x + size/2};
                int[] py = {y + size/3, y, y + size/3, y + 2*size/3};
                g2.drawPolygon(px, py, 4);
                g2.drawLine(x, y + size/3, x, y + 2*size/3);
                g2.drawArc(x, y + size/3, size, size/2, 0, -180);
            }
            case LAPORAN -> {
                g2.drawRect(x + 2, y + 2, size - 4, size - 4);
                g2.drawLine(x + 5, y + 6, x + size - 5, y + 6);
                g2.drawLine(x + 5, y + 10, x + size - 5, y + 10);
            }
            case SETTINGS -> {
                g2.drawOval(x + size/4, y + size/4, size/2, size/2);
                for (int i = 0; i < 8; i++) {
                    double a = Math.toRadians(i * 45);
                    int x1 = (int)(x + size/2 + Math.cos(a) * (size/4));
                    int y1 = (int)(y + size/2 + Math.sin(a) * (size/4));
                    int x2 = (int)(x + size/2 + Math.cos(a) * (size/2));
                    int y2 = (int)(y + size/2 + Math.sin(a) * (size/2));
                    g2.drawLine(x1, y1, x2, y2);
                }
            }
            case NILAI -> {
                g2.drawRect(x, y, size, size);
                g2.drawLine(x + size/2, y + 4, x + size/2, y + size - 4);
                g2.drawLine(x + 4, y + size/2, x + size - 4, y + size/2);
            }
            case ABSENSI -> {
                g2.drawOval(x, y, size, size);
                g2.drawLine(x + size/2, y + size/2, x + size/2, y + 4);
                g2.drawLine(x + size/2, y + size/2, x + size - 4, y + size/2);
            }
            case INFO -> {
                g2.drawOval(x, y, size, size);
                g2.drawLine(x + size/2, y + size/2 - 2, x + size/2, y + size - 4);
                g2.fillOval(x + size/2 - 1, y + 4, 2, 2);
            }
        }
    }

    private JButton buildThemeButton() {
        JButton btn = new JButton() {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? new Color(51, 65, 85) : new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(100, 116, 139));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String text = AppTheme.toggleText();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(212, 36));
        btn.setMaximumSize(new Dimension(212, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            AppTheme.toggle();
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        });
        return btn;
    }

    private JButton buildLogoutNavButton() {
        JButton btn = new JButton() {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hover ? new Color(153, 27, 27) : new Color(127, 29, 29, 90);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(239, 68, 68, 60));
                g2.fillRoundRect(0, 2, 3, getHeight() - 4, 3, 3);
                drawDoorIcon(g2, 15, getHeight() / 2 - 8, 15, new Color(254, 202, 202));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(new Color(254, 226, 226));
                g2.drawString("Logout", 40, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(212, 40));
        btn.setMaximumSize(new Dimension(212, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setToolTipText("Keluar dari akun");
        btn.addActionListener(e -> doLogout());
        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeBtn != null) {
            try {
                java.lang.reflect.Method method = activeBtn.getClass().getDeclaredMethod("setActive", boolean.class);
                method.setAccessible(true);
                method.invoke(activeBtn, false);
            } catch (Exception ignored) {}
        }
        try {
            java.lang.reflect.Method method = btn.getClass().getDeclaredMethod("setActive", boolean.class);
            method.setAccessible(true);
            method.invoke(btn, true);
        } catch (Exception ignored) {}
        activeBtn = btn;
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }

    private void doLogout() {
        if (showLogoutDialog()) {
            AuthService.logout();
            if (clockTimer != null) clockTimer.stop();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private boolean showLogoutDialog() {
        final boolean[] confirmed = {false};
        JDialog dialog = new JDialog(this, "Konfirmasi Logout", true);
        dialog.setUndecorated(true);
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.isDark() ? new Color(15, 23, 42) : new Color(255, 255, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(239, 68, 68, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 24, 20, 24));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.NORTH;

        JPanel iconWrap = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(239, 68, 68, 40));
                g2.fillOval(9, 9, 48, 48);
                drawDoorIcon(g2, 22, 18, 22, new Color(254, 226, 226));
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
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppTheme.text());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea message = new JTextArea("Sesi akan ditutup dan Anda akan kembali ke halaman login.");
        message.setPreferredSize(new Dimension(270, 40));
        message.setMaximumSize(new Dimension(270, 40));
        message.setOpaque(false);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        message.setForeground(AppTheme.muted());
        message.setBorder(new EmptyBorder(6, 0, 0, 0));
        message.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBlock.add(title);
        textBlock.add(message);
        gc.gridx = 0;
        content.add(iconWrap, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        content.add(textBlock, gc);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(16, 72, 0, 0));
        JButton cancel = dialogButton("Batal", new Color(AppTheme.isDark() ? 30 : 241, AppTheme.isDark() ? 41 : 245, AppTheme.isDark() ? 59 : 249), AppTheme.text());
        JButton logout = dialogButton("Ya, Logout", new Color(220, 38, 38), new Color(254, 226, 226));
        cancel.addActionListener(e -> dialog.dispose());
        logout.addActionListener(e -> { confirmed[0] = true; dialog.dispose(); });
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
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(fg);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
