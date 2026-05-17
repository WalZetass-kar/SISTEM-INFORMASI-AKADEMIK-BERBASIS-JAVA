package com.siakad.views;

import com.siakad.services.AuthService;
import com.siakad.utils.Config;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * LoginFrame - Halaman Login SIAKAD
 * Layout 2-kolom: kiri = branding/deskripsi, kanan = form login
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private JCheckBox chkShowPassword;

    // Warna tema
    private static final Color BG_DARK      = new Color(10, 15, 30);
    private static final Color BG_CARD      = new Color(18, 26, 48);
    private static final Color BG_INPUT     = new Color(26, 36, 60);
    private static final Color ACCENT_BLUE  = new Color(59, 130, 246);
    private static final Color ACCENT_INDIGO = new Color(99, 102, 241);
    private static final Color ACCENT_CYAN  = new Color(34, 211, 238);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_MUTED   = new Color(148, 163, 184);
    private static final Color TEXT_DIM     = new Color(71, 85, 105);
    private static final Color BORDER_COLOR = new Color(30, 41, 70);

    public LoginFrame() {
        setTitle(Config.APP_NAME + " — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);

        root.add(buildLeftPanel());
        root.add(buildRightPanel());

        setContentPane(root);
    }

    // ── Panel Kiri: Branding & Deskripsi ──────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 60),
                        getWidth(), getHeight(), new Color(8, 12, 35));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles
                g2.setColor(new Color(59, 130, 246, 18));
                g2.fillOval(-60, -60, 280, 280);
                g2.setColor(new Color(99, 102, 241, 12));
                g2.fillOval(getWidth() - 150, getHeight() - 150, 300, 300);
                g2.setColor(new Color(34, 211, 238, 10));
                g2.fillOval(getWidth() / 2 - 80, getHeight() / 2 - 80, 200, 200);

                // Grid dots pattern
                g2.setColor(new Color(255, 255, 255, 8));
                for (int x = 20; x < getWidth(); x += 30) {
                    for (int y = 20; y < getHeight(); y += 30) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 40, 0, 40));

        // Logo icon
        JLabel lblIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Outer glow
                g2.setColor(new Color(59, 130, 246, 30));
                g2.fillOval(0, 0, 80, 80);
                // Inner circle
                GradientPaint gp = new GradientPaint(10, 10, ACCENT_BLUE, 70, 70, ACCENT_INDIGO);
                g2.setPaint(gp);
                g2.fillOval(8, 8, 64, 64);
                // Icon text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("🎓", (80 - fm.stringWidth("🎓")) / 2, 50);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(80, 80); }
        };
        lblIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        // App name
        JLabel lblAppName = new JLabel("SIAKAD");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblAppName.setForeground(TEXT_PRIMARY);
        lblAppName.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tagline
        JLabel lblTagline = new JLabel("Sistem Informasi Akademik");
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTagline.setForeground(ACCENT_CYAN);
        lblTagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), 0, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description
        JLabel lblDesc = new JLabel("<html><body style='width:280px; color:#94a3b8; font-family:Segoe UI; font-size:13px; line-height:1.6'>"
                + "Platform digital terintegrasi untuk pengelolaan data akademik dan keuangan mahasiswa secara efisien dan transparan."
                + "</body></html>");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Feature list
        String[][] features = {
            {"📊", "Dashboard Statistik", "Pantau pembayaran & data real-time"},
            {"👨‍🎓", "Manajemen Mahasiswa", "CRUD data mahasiswa lengkap"},
            {"💳", "Pembayaran UKT", "Input & verifikasi pembayaran"},
            {"📋", "Laporan & Cetak", "Generate laporan keuangan & akademik"},
        };

        JPanel featureList = new JPanel();
        featureList.setOpaque(false);
        featureList.setLayout(new BoxLayout(featureList, BoxLayout.Y_AXIS));
        featureList.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String[] f : features) {
            featureList.add(buildFeatureItem(f[0], f[1], f[2]));
            featureList.add(Box.createVerticalStrut(10));
        }

        // Version badge
        JLabel lblVersion = new JLabel("  v" + Config.APP_VERSION + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(59, 130, 246, 120));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lblVersion.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblVersion.setForeground(ACCENT_BLUE);
        lblVersion.setOpaque(false);
        lblVersion.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lblIcon);
        content.add(Box.createVerticalStrut(16));
        content.add(lblAppName);
        content.add(Box.createVerticalStrut(4));
        content.add(lblTagline);
        content.add(Box.createVerticalStrut(20));
        content.add(sep);
        content.add(Box.createVerticalStrut(20));
        content.add(lblDesc);
        content.add(Box.createVerticalStrut(24));
        content.add(featureList);
        content.add(Box.createVerticalStrut(24));
        content.add(lblVersion);

        panel.add(content);
        return panel;
    }

    private JPanel buildFeatureItem(String icon, String title, String desc) {
        JPanel item = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(new EmptyBorder(10, 12, 10, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(TEXT_MUTED);

        textPanel.add(lblTitle);
        textPanel.add(lblDesc);

        item.add(lblIcon, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        return item;
    }

    // ── Panel Kanan: Form Login ────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Left border accent
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, 0, getHeight(), ACCENT_INDIGO);
                g2.setPaint(gp);
                g2.fillRect(0, 0, 2, getHeight());
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 48, 0, 48));

        // Welcome text
        JLabel lblWelcome = new JLabel("Selamat Datang 👋");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblWelcome.setForeground(TEXT_PRIMARY);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Masuk ke akun Anda untuk melanjutkan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username field
        JLabel lblUser = makeLabel("Username / NIM");
        txtUsername = makePlaceholderField("Masukkan username atau NIM...");

        // Password field
        JLabel lblPass = makeLabel("Password");
        txtPassword = new JPasswordField();
        styleInputField(txtPassword);

        // Show password
        chkShowPassword = new JCheckBox("Tampilkan Password");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(TEXT_MUTED);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.addActionListener(e ->
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '●'));

        // Status label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(248, 113, 113));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        btnLogin = buildLoginButton();

        // Hint card
        JPanel hintCard = buildHintCard();

        form.add(lblWelcome);
        form.add(Box.createVerticalStrut(6));
        form.add(lblSub);
        form.add(Box.createVerticalStrut(32));
        form.add(lblUser);
        form.add(Box.createVerticalStrut(6));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(16));
        form.add(lblPass);
        form.add(Box.createVerticalStrut(6));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(8));
        form.add(chkShowPassword);
        form.add(Box.createVerticalStrut(8));
        form.add(lblStatus);
        form.add(Box.createVerticalStrut(4));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(20));
        form.add(hintCard);

        panel.add(form);

        // Key listeners
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) txtPassword.requestFocus();
            }
        });

        return panel;
    }

    private JPanel buildHintCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(59, 130, 246, 50));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lblHintTitle = new JLabel("ℹ️  Akun Default");
        lblHintTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHintTitle.setForeground(ACCENT_BLUE);
        lblHintTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint1 = new JLabel("Admin: admin / admin123");
        lblHint1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint1.setForeground(TEXT_MUTED);
        lblHint1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint2 = new JLabel("Mahasiswa: 2024001 / mhs123");
        lblHint2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint2.setForeground(TEXT_MUTED);
        lblHint2.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblHintTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(lblHint1);
        card.add(lblHint2);
        return card;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(203, 213, 225));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makePlaceholderField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(TEXT_DIM);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 12, getHeight() / 2 + 5);
                }
            }
        };
        styleInputField(field);
        return field;
    }

    private void styleInputField(JComponent field) {
        field.setPreferredSize(new Dimension(0, 44));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        if (field instanceof JTextField) ((JTextField) field).setCaretColor(TEXT_PRIMARY);
        if (field instanceof JPasswordField) ((JPasswordField) field).setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Focus border highlight
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Masuk ke Sistem") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(30, 41, 59));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(29, 78, 216));
                } else if (getModel().isRollover()) {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(96, 165, 250),
                            getWidth(), 0, new Color(129, 140, 248));
                    g2.setPaint(gp);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE,
                            getWidth(), 0, ACCENT_INDIGO);
                    g2.setPaint(gp);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Shine effect
                if (isEnabled() && !getModel().isPressed()) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 10, 10);
                }

                g2.setColor(isEnabled() ? Color.WHITE : TEXT_DIM);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(0, 46));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> doLogin());
        return btn;
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("❌ Username dan password wajib diisi.", false);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Memproses...");
        lblStatus.setText(" ");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                try {
                    return AuthService.loginWithMessage(username, password);
                } catch (Exception e) {
                    return "Koneksi gagal: " + e.getMessage();
                }
            }
            @Override protected void done() {
                try {
                    String result = get();
                    if ("success".equals(result)) {
                        dispose();
                        new MainFrame().setVisible(true);
                    } else {
                        setStatus("❌ " + result, false);
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Masuk ke Sistem");
                    }
                } catch (Exception e) {
                    setStatus("❌ Terjadi kesalahan.", false);
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Masuk ke Sistem");
                }
            }
        };
        worker.execute();
    }

    private void setStatus(String msg, boolean success) {
        lblStatus.setText(msg);
        lblStatus.setForeground(success ? new Color(74, 222, 128) : new Color(248, 113, 113));
    }
}
