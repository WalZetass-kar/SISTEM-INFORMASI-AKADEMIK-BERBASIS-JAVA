package com.siakad.views;

import com.siakad.services.AuthService;
import com.siakad.utils.Config;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame - Halaman Login Sistem Informasi Akademik
 * Mendukung role: admin, mahasiswa
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private JCheckBox chkShowPassword;

    public LoginFrame() {
        setTitle(Config.APP_NAME + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42),
                        0, getHeight(), new Color(30, 41, 59));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Logo/header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblIcon = new JLabel("🎓", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(Config.APP_NAME, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(248, 250, 252));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Masuk ke Sistem", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(148, 163, 184));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblIcon);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSubtitle);

        // Form panel (card style)
        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 41, 59, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        formCard.setOpaque(false);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(28, 28, 28, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Username
        JLabel lblUser = new JLabel("Username / NIM");
        lblUser.setForeground(new Color(203, 213, 225));
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        formCard.add(lblUser, gbc);

        txtUsername = createTextField("Masukkan username atau NIM...");
        gbc.gridy = 1;
        formCard.add(txtUsername, gbc);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setForeground(new Color(203, 213, 225));
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2; gbc.insets = new Insets(14, 0, 6, 0);
        formCard.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(0, 40));
        txtPassword.setBackground(new Color(51, 65, 85));
        txtPassword.setForeground(Color.WHITE);
        txtPassword.setCaretColor(Color.WHITE);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 3; gbc.insets = new Insets(6, 0, 6, 0);
        formCard.add(txtPassword, gbc);

        // Show password checkbox
        chkShowPassword = new JCheckBox("Tampilkan Password");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(new Color(148, 163, 184));
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('●');
            }
        });
        gbc.gridy = 4; gbc.insets = new Insets(2, 0, 12, 0);
        formCard.add(chkShowPassword, gbc);

        // Status label
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(248, 113, 113));
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 6, 0);
        formCard.add(lblStatus, gbc);

        // Login button
        btnLogin = createLoginButton();
        gbc.gridy = 6; gbc.insets = new Insets(6, 0, 0, 0);
        formCard.add(btnLogin, gbc);

        // Info hint
        JLabel lblHint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblHint.setForeground(new Color(100, 116, 139));
        gbc.gridy = 7; gbc.insets = new Insets(12, 0, 0, 0);
        formCard.add(lblHint, gbc);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(Box.createVerticalStrut(24), BorderLayout.CENTER);
        mainPanel.add(formCard, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Enter key trigger login
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
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(100, 116, 139));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 12, getHeight() / 2 + 5);
                }
            }
        };
        field.setPreferredSize(new Dimension(0, 40));
        field.setBackground(new Color(51, 65, 85));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return field;
    }

    private JButton createLoginButton() {
        JButton btn = new JButton("Masuk") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(37, 99, 235));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(96, 165, 250));
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(59, 130, 246),
                            getWidth(), 0, new Color(99, 102, 241));
                    g2.setPaint(gp);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                    } else {
                        setStatus("❌ " + result, false);
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Masuk");
                    }
                } catch (Exception e) {
                    setStatus("❌ Terjadi kesalahan.", false);
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Masuk");
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
