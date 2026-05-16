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
 * Berisi sidebar navigasi + content panel
 */
public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel lblUserInfo;
    private JButton activeBtn;

    // Panel names
    public static final String PANEL_DASHBOARD  = "dashboard";
    public static final String PANEL_MAHASISWA  = "mahasiswa";
    public static final String PANEL_PEMBAYARAN = "pembayaran";
    public static final String PANEL_LAPORAN    = "laporan";

    public MainFrame() {
        setTitle(Config.APP_NAME + " - " + JwtHelper.getInstance().getUsername().toUpperCase());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Config.DEFAULT_WIDTH, Config.DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // === SIDEBAR ===
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // === CONTENT ===
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(15, 23, 42));

        contentPanel.add(new DashboardPanel(), PANEL_DASHBOARD);
        contentPanel.add(new MahasiswaPanel(), PANEL_MAHASISWA);
        contentPanel.add(new PembayaranPanel(), PANEL_PEMBAYARAN);
        contentPanel.add(new LaporanPanel(), PANEL_LAPORAN);

        add(contentPanel, BorderLayout.CENTER);

        // Default panel sesuai role
        if (JwtHelper.getInstance().isAdmin()) {
            showPanel(PANEL_DASHBOARD);
        } else {
            showPanel(PANEL_PEMBAYARAN);
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(15, 23, 42));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border accent
                g2.setColor(new Color(30, 41, 59));
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BorderLayout());

        // Logo area
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel lblLogo = new JLabel("🎓");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAppName = new JLabel("SIAKAD");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAppName.setForeground(new Color(248, 250, 252));
        lblAppName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblVersion = new JLabel("v" + Config.APP_VERSION);
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVersion.setForeground(new Color(100, 116, 139));
        lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(lblLogo);
        logoPanel.add(Box.createVerticalStrut(6));
        logoPanel.add(lblAppName);
        logoPanel.add(Box.createVerticalStrut(2));
        logoPanel.add(lblVersion);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(30, 41, 59));
        sep.setBackground(new Color(30, 41, 59));

        // Nav menu
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(16, 12, 16, 12));

        JLabel lblMenu = new JLabel("  MENU");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenu.setForeground(new Color(100, 116, 139));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(lblMenu);
        navPanel.add(Box.createVerticalStrut(8));

        JwtHelper jwt = JwtHelper.getInstance();
        JButton btnDashboard  = createNavButton("📊  Dashboard",    PANEL_DASHBOARD);
        JButton btnMahasiswa  = createNavButton("👨‍🎓  Data Mahasiswa", PANEL_MAHASISWA);
        JButton btnPembayaran = createNavButton("💳  Pembayaran",   PANEL_PEMBAYARAN);
        JButton btnLaporan    = createNavButton("📋  Laporan",      PANEL_LAPORAN);

        if (jwt.isAdmin()) {
            navPanel.add(btnDashboard);
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnMahasiswa);
            navPanel.add(Box.createVerticalStrut(4));
        }
        navPanel.add(btnPembayaran);
        navPanel.add(Box.createVerticalStrut(4));

        if (jwt.isAdmin()) {
            navPanel.add(btnLaporan);
            navPanel.add(Box.createVerticalStrut(4));
        }

        // User info + logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(12, 12, 16, 12));

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(30, 41, 59));

        JPanel userCard = new JPanel();
        userCard.setOpaque(false);
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setBorder(new EmptyBorder(10, 0, 10, 0));

        lblUserInfo = new JLabel("👤 " + jwt.getUsername());
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserInfo.setForeground(new Color(203, 213, 225));
        lblUserInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRole = new JLabel("  " + jwt.getRole().toUpperCase());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblRole.setForeground(new Color(59, 130, 246));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        userCard.add(lblUserInfo);
        userCard.add(Box.createVerticalStrut(2));
        userCard.add(lblRole);

        JButton btnLogout = new JButton("  Keluar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(127, 29, 29) : new Color(69, 10, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(252, 165, 165));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText().trim(), 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btnLogout.setPreferredSize(new Dimension(196, 36));
        btnLogout.setMaximumSize(new Dimension(196, 36));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.addActionListener(e -> doLogout());

        bottomPanel.add(sep2, BorderLayout.NORTH);
        bottomPanel.add(userCard, BorderLayout.CENTER);
        bottomPanel.add(btnLogout, BorderLayout.SOUTH);

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String panelName) {
        JButton btn = new JButton(text) {
            boolean isActive = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2.setColor(new Color(37, 99, 235, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(59, 130, 246));
                    g2.fillRoundRect(0, 4, 3, getHeight() - 8, 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(51, 65, 85, 80));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(isActive ? new Color(147, 197, 253) : new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            public void setActive(boolean a) { isActive = a; repaint(); }
            public boolean getActiveState() { return isActive; }
        };
        btn.setPreferredSize(new Dimension(196, 40));
        btn.setMaximumSize(new Dimension(196, 40));
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

    private void setActiveButton(JButton btn) {
        if (activeBtn != null) {
            try {
                activeBtn.getClass().getMethod("setActive", boolean.class).invoke(activeBtn, false);
            } catch (Exception ignored) {}
        }
        try {
            btn.getClass().getMethod("setActive", boolean.class).invoke(btn, true);
        } catch (Exception ignored) {}
        activeBtn = btn;
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
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
