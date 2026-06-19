package com.siakad.views;

import com.siakad.services.AuthService;
import com.siakad.utils.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame - Halaman Login SIAKAD dengan tema light/dark dan loading modern.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnThemeToggle;
    private JLabel lblStatus;
    private JCheckBox chkShowPassword;
    private Timer ambientTimer;
    private Timer loginSpinnerTimer;
    private float ambientPhase = 0f;
    private int spinnerAngle = 0;
    private boolean loginLoading = false;
    private boolean darkMode = true;

    private static final Theme DARK = new Theme(
            new Color(8, 13, 27),
            new Color(15, 23, 42),
            new Color(22, 32, 52),
            new Color(30, 41, 59),
            new Color(51, 65, 85),
            new Color(248, 250, 252),
            new Color(148, 163, 184),
            new Color(71, 85, 105),
            new Color(79, 70, 229),
            new Color(139, 92, 246),
            new Color(6, 182, 212),
            new Color(16, 185, 129),
            new Color(239, 68, 68),
            new Color(11, 18, 36),
            new Color(6, 11, 26),
            new Color(15, 23, 60),
            new Color(8, 12, 35),
            new Color(255, 255, 255, 10),
            new Color(7, 12, 28, 150),
            new Color(255, 255, 255, 8)
    );

    private static final Theme LIGHT = new Theme(
            new Color(248, 250, 252),
            new Color(255, 255, 255),
            new Color(248, 250, 252),
            new Color(255, 255, 255),
            new Color(226, 232, 240),
            new Color(15, 23, 42),
            new Color(71, 85, 105),
            new Color(148, 163, 184),
            new Color(79, 70, 229),
            new Color(139, 92, 246),
            new Color(6, 182, 212),
            new Color(16, 185, 129),
            new Color(239, 68, 68),
            new Color(255, 255, 255),
            new Color(226, 232, 240),
            new Color(219, 234, 254),
            new Color(238, 242, 255),
            new Color(79, 70, 229, 14),
            new Color(255, 255, 255, 210),
            new Color(15, 23, 42, 10)
    );

    public LoginFrame() {
        setTitle(Config.APP_NAME + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setResizable(true);
        initUI(null, null, false);
        startAmbientAnimation();
    }

    private Theme theme() {
        return darkMode ? DARK : LIGHT;
    }

    private void initUI(String usernameValue, char[] passwordValue, boolean showPassword) {
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(theme().windowBg);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);
        root.add(buildLeftPanel());
        root.add(buildRightPanel());
        setContentPane(root);

        if (usernameValue != null) {
            txtUsername.setText(usernameValue);
        }
        if (passwordValue != null) {
            txtPassword.setText(new String(passwordValue));
        }
        chkShowPassword.setSelected(showPassword);
        txtPassword.setEchoChar(showPassword ? (char) 0 : '\u2022');
        revalidate();
        repaint();
    }

    private void startAmbientAnimation() {
        ambientTimer = new Timer(33, e -> {
            ambientPhase += 0.018f;
            repaint();
        });
        // Animasi ambient dimatikan supaya ringan di laptop spek rendah.
    }

    private void stopAmbientAnimation() {
        if (ambientTimer != null) {
            ambientTimer.stop();
        }
    }

    @Override public void dispose() {
        stopAmbientAnimation();
        stopLoginLoading();
        super.dispose();
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint bg = new GradientPaint(0, 0, t.heroTop, getWidth(), getHeight(), t.heroBottom);
                g2.setPaint(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                int drift = (int) (Math.sin(ambientPhase) * 18);
                int slowDrift = (int) (Math.cos(ambientPhase * 0.7f) * 22);

                g2.setColor(withAlpha(t.accentCyan, darkMode ? 26 : 34));
                g2.fillOval(-95 + drift, -70 + slowDrift, 340, 340);
                g2.setColor(withAlpha(t.accentIndigo, darkMode ? 20 : 28));
                g2.fillOval(getWidth() - 200 - slowDrift, getHeight() - 190 + drift, 360, 360);
                g2.setColor(withAlpha(t.success, darkMode ? 13 : 20));
                g2.fillOval(getWidth() / 2 - 135 + slowDrift, getHeight() / 2 - 95, 250, 250);

                g2.setColor(t.dotColor);
                for (int x = 22; x < getWidth(); x += 32) {
                    for (int y = 22; y < getHeight(); y += 32) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }

                int scanY = (int) ((Math.sin(ambientPhase * 0.55f) + 1) * 0.5f * getHeight());
                g2.setColor(withAlpha(t.accentCyan, darkMode ? 28 : 40));
                g2.fillRoundRect(0, scanY, getWidth(), 2, 2, 2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 46, 0, 46));
        content.setMaximumSize(new Dimension(460, 640));

        JLabel lblIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(withAlpha(t.accentBlue, 36));
                g2.fillOval(0, 0, 86, 86);
                GradientPaint gp = new GradientPaint(10, 10, t.accentBlue, 76, 76, t.accentIndigo);
                g2.setPaint(gp);
                g2.fillOval(9, 9, 68, 68);
                g2.setColor(new Color(255, 255, 255, 46));
                g2.fillOval(22, 14, 36, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 31));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("🎓", (86 - fm.stringWidth("🎓")) / 2, 55);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(86, 86); }
        };
        lblIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAppName = new JLabel("SIAKAD");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblAppName.setForeground(theme().textPrimary);
        lblAppName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTagline = new JLabel("Sistem Informasi Akademik yang rapi, cepat, dan mudah dipakai");
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTagline.setForeground(theme().accentCyan);
        lblTagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, theme().accentBlue, getWidth(), 0, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><body style='width:330px; font-family:Segoe UI; font-size:13px; line-height:1.6'>"
                + "Kelola data mahasiswa, jadwal, nilai, pembayaran, dan laporan dari satu dashboard yang terasa lebih nyaman digunakan."
                + "</body></html>");
        lblDesc.setForeground(theme().textMuted);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel featureList = new JPanel();
        featureList.setOpaque(false);
        featureList.setLayout(new BoxLayout(featureList, BoxLayout.Y_AXIS));
        featureList.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[][] features = {
                {"📊", "Dashboard Ringkas", "Ringkasan akademik & pembayaran lebih cepat dibaca"},
                {"👨‍🎓", "Data Mahasiswa", "Kelola identitas mahasiswa dengan tampilan bersih"},
                {"💳", "Pembayaran UKT", "Pantau status pembayaran secara transparan"},
                {"📋", "Laporan", "Cetak laporan akademik dan keuangan kapan saja"}
        };
        for (String[] feature : features) {
            featureList.add(buildFeatureItem(feature[0], feature[1], feature[2]));
            featureList.add(Box.createVerticalStrut(10));
        }

        JLabel lblVersion = buildBadge("v" + Config.APP_VERSION);

        content.add(lblIcon);
        content.add(Box.createVerticalStrut(18));
        content.add(lblAppName);
        content.add(Box.createVerticalStrut(5));
        content.add(lblTagline);
        content.add(Box.createVerticalStrut(22));
        content.add(sep);
        content.add(Box.createVerticalStrut(22));
        content.add(lblDesc);
        content.add(Box.createVerticalStrut(24));
        content.add(featureList);
        content.add(Box.createVerticalStrut(16));
        content.add(lblVersion);

        panel.add(content);
        return panel;
    }

    private JPanel buildFeatureItem(String icon, String title, String desc) {
        JPanel item = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.glassFill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(withAlpha(t.border, darkMode ? 150 : 190));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setLayout(new BorderLayout(12, 0));
        item.setBorder(new EmptyBorder(11, 13, 11, 13));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 21));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(theme().textPrimary);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(theme().textMuted);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblDesc);

        item.add(lblIcon, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        return item;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.cardBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(withAlpha(t.accentBlue, darkMode ? 18 : 30));
                g2.fillOval(getWidth() - 220, -130, 330, 330);
                g2.setColor(withAlpha(t.accentCyan, darkMode ? 16 : 25));
                g2.fillOval(80, getHeight() - 150, 240, 240);
                GradientPaint accent = new GradientPaint(0, 0, t.accentBlue, 0, getHeight(), t.accentIndigo);
                g2.setPaint(accent);
                g2.fillRect(0, 0, 3, getHeight());
                g2.dispose();
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 34, 24, 34));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        btnThemeToggle = buildThemeToggle();
        top.add(btnThemeToggle);
        panel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(buildLoginForm());
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildLoginForm() {
        JPanel form = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(withAlpha(Color.BLACK, darkMode ? 44 : 12));
                g2.fillRoundRect(8, 10, getWidth() - 16, getHeight() - 14, 28, 28);
                g2.setColor(t.formFill);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 10, 28, 28);
                g2.setColor(withAlpha(t.border, darkMode ? 115 : 190));
                g2.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 11, 28, 28);
                int shineX = (int) ((Math.sin(ambientPhase * 0.8f) + 1f) * 0.5f * getWidth());
                GradientPaint shine = new GradientPaint(shineX - 90, 0, withAlpha(t.accentCyan, 0), shineX, 0, withAlpha(t.accentCyan, 60), true);
                g2.setPaint(shine);
                g2.fillRoundRect(Math.max(0, shineX - 120), 0, 190, 3, 3, 3);
                g2.dispose();
            }
        };
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(32, 34, 32, 42));
        form.setPreferredSize(new Dimension(448, 575));

        JLabel lblBadge = buildBadge(darkMode ? "Mode malam aktif" : "Mode terang aktif");

        JLabel lblWelcome = new JLabel("Selamat Datang 👋");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(theme().textPrimary);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Masuk untuk melanjutkan ke dashboard akademik");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(theme().textMuted);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUser = makeLabel("Username / NIM");
        txtUsername = makePlaceholderField("Contoh: admin atau 2024001");

        JLabel lblPass = makeLabel("Password");
        txtPassword = new JPasswordField();
        txtPassword.setEchoChar('\u2022');
        styleInputField(txtPassword);

        chkShowPassword = new JCheckBox("Tampilkan password");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(theme().textMuted);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkShowPassword.addActionListener(e -> txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '\u2022'));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(theme().danger);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnLogin = buildLoginButton();
        JPanel hintCard = buildHintCard();

        form.add(lblBadge);
        form.add(Box.createVerticalStrut(18));
        form.add(lblWelcome);
        form.add(Box.createVerticalStrut(6));
        form.add(lblSub);
        form.add(Box.createVerticalStrut(30));
        form.add(lblUser);
        form.add(Box.createVerticalStrut(7));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(16));
        form.add(lblPass);
        form.add(Box.createVerticalStrut(7));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(10));
        form.add(chkShowPassword);
        form.add(Box.createVerticalStrut(10));
        form.add(lblStatus);
        form.add(Box.createVerticalStrut(6));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(20));
        form.add(hintCard);

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

        return form;
    }

    private JButton buildThemeToggle() {
        JButton btn = new JButton(darkMode ? "☀ Mode Terang" : "🌙 Mode Gelap") {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.formFill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(withAlpha(t.border, 180));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.setColor(t.textPrimary);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(142, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> toggleTheme());
        return btn;
    }

    private void toggleTheme() {
        String usernameValue = txtUsername != null ? txtUsername.getText() : "";
        char[] passwordValue = txtPassword != null ? txtPassword.getPassword() : new char[0];
        boolean showPassword = chkShowPassword != null && chkShowPassword.isSelected();
        darkMode = !darkMode;
        initUI(usernameValue, passwordValue, showPassword);
        SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
    }

    private JPanel buildHintCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(withAlpha(t.accentBlue, darkMode ? 20 : 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(withAlpha(t.accentBlue, darkMode ? 80 : 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(13, 15, 13, 15));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JLabel lblHintTitle = new JLabel("ℹ Akun demo");
        lblHintTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHintTitle.setForeground(theme().accentBlue);
        lblHintTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint1 = new JLabel("Admin: admin / admin123");
        lblHint1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint1.setForeground(theme().textMuted);
        lblHint1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint2 = new JLabel("Mahasiswa: 2024001 / mhs123");
        lblHint2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint2.setForeground(theme().textMuted);
        lblHint2.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblHintTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(lblHint1);
        card.add(lblHint2);
        return card;
    }

    private JLabel buildBadge(String text) {
        JLabel badge = new JLabel("  " + text + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(withAlpha(t.accentBlue, darkMode ? 32 : 24));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(withAlpha(t.accentBlue, darkMode ? 110 : 95));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(theme().accentBlue);
        badge.setOpaque(false);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.setMaximumSize(new Dimension(190, 24));
        return badge;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(theme().textPrimary);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField makePlaceholderField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(theme().textDim);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 14, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        styleInputField(field);
        return field;
    }

    private void styleInputField(JComponent field) {
        Theme t = theme();
        field.setPreferredSize(new Dimension(0, 46));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setBackground(t.inputBg);
        field.setForeground(t.textPrimary);
        if (field instanceof JTextField textField) {
            textField.setCaretColor(t.textPrimary);
        }
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.border, 1),
                BorderFactory.createEmptyBorder(8, 13, 8, 13)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(theme().accentBlue, 1),
                        BorderFactory.createEmptyBorder(8, 13, 8, 13)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(theme().border, 1),
                        BorderFactory.createEmptyBorder(8, 13, 8, 13)));
            }
        });
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Masuk ke Sistem") {
            @Override protected void paintComponent(Graphics g) {
                Theme t = theme();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(darkMode ? new Color(30, 41, 59) : new Color(191, 219, 254));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(29, 78, 216));
                } else {
                    Color left = getModel().isRollover() ? brighter(t.accentBlue, 22) : t.accentBlue;
                    Color right = getModel().isRollover() ? brighter(t.accentIndigo, 18) : t.accentIndigo;
                    g2.setPaint(new GradientPaint(0, 0, left, getWidth(), 0, right));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                if (isEnabled() && !getModel().isPressed()) {
                    g2.setColor(new Color(255, 255, 255, 24));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 14, 14);
                }

                g2.setColor(isEnabled() ? Color.WHITE : withAlpha(t.textPrimary, darkMode ? 90 : 140));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int textW = fm.stringWidth(text);
                int x = (getWidth() - textW) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                if (loginLoading) {
                    x += 13;
                    g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(new Color(219, 234, 254));
                    g2.drawArc(x - 31, getHeight() / 2 - 8, 16, 16, spinnerAngle, 270);
                    g2.setColor(Color.WHITE);
                }
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(0, 48));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
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
            setStatus("Username dan password wajib diisi.", false);
            shakeFrame();
            return;
        }

        startLoginLoading();
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
                        stopLoginLoading();
                        stopAmbientAnimation();
                        LoginFrame.this.dispose();
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                    } else {
                        setStatus(result, false);
                        stopLoginLoading();
                        shakeFrame();
                    }
                } catch (Exception e) {
                    setStatus("Terjadi kesalahan.", false);
                    stopLoginLoading();
                }
            }
        };
        worker.execute();
    }

    private void startLoginLoading() {
        loginLoading = true;
        btnLogin.setEnabled(false);
        btnLogin.setText("Memverifikasi");
        spinnerAngle = 0;
        loginSpinnerTimer = new Timer(16, e -> {
            spinnerAngle = (spinnerAngle + 12) % 360;
            btnLogin.repaint();
        });
        loginSpinnerTimer.start();
    }

    private void stopLoginLoading() {
        loginLoading = false;
        if (loginSpinnerTimer != null) {
            loginSpinnerTimer.stop();
            loginSpinnerTimer = null;
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Masuk ke Sistem");
            btnLogin.repaint();
        }
    }

    private void shakeFrame() {
        Point origin = getLocation();
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        Timer shake = new Timer(30, null);
        int[] idx = {0};
        shake.addActionListener(e -> {
            setLocation(origin.x + offsets[idx[0]], origin.y);
            idx[0]++;
            if (idx[0] >= offsets.length) {
                shake.stop();
                setLocation(origin);
            }
        });
        shake.start();
    }

    private void setStatus(String msg, boolean success) {
        lblStatus.setText((success ? "✓ " : "⚠ ") + msg);
        lblStatus.setForeground(success ? theme().success : theme().danger);
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    private static Color brighter(Color color, int amount) {
        return new Color(
                Math.min(255, color.getRed() + amount),
                Math.min(255, color.getGreen() + amount),
                Math.min(255, color.getBlue() + amount));
    }

    private record Theme(
            Color windowBg,
            Color cardBg,
            Color formBg,
            Color inputBg,
            Color border,
            Color textPrimary,
            Color textMuted,
            Color textDim,
            Color accentBlue,
            Color accentIndigo,
            Color accentCyan,
            Color success,
            Color danger,
            Color formFill,
            Color formShadow,
            Color heroTop,
            Color heroBottom,
            Color glassFill,
            Color cardOverlay,
            Color dotColor
    ) {}
}
