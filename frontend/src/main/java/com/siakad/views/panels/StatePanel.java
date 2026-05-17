package com.siakad.views.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * StatePanel - tampilan reusable untuk empty/error state dengan retry.
 */
public class StatePanel extends JPanel {
    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JLabel messageLabel;
    private final JButton actionButton;
    private Runnable action;

    private static final Color BG = new Color(13, 19, 38);
    private static final Color CARD_BG = new Color(18, 26, 48);
    private static final Color BORDER = new Color(25, 36, 65);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color BLUE = new Color(59, 130, 246);

    public StatePanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBackground(BG);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 34, 30, 34));
        card.setPreferredSize(new Dimension(420, 230));

        iconLabel = new JLabel("!");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        iconLabel.setForeground(BLUE);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLabel = new JLabel("Data belum tersedia");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(MUTED);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionButton = new JButton("Muat ulang") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? BLUE.brighter() : BLUE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        actionButton.setPreferredSize(new Dimension(130, 34));
        actionButton.setMaximumSize(new Dimension(130, 34));
        actionButton.setBorderPainted(false);
        actionButton.setContentAreaFilled(false);
        actionButton.setFocusPainted(false);
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButton.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(actionButton);

        add(card);
    }

    public void showState(String icon, String title, String message, String actionText, Runnable action) {
        iconLabel.setText(icon);
        titleLabel.setText(title);
        messageLabel.setText("<html><body style='width:320px;text-align:center'>" + message + "</body></html>");
        this.action = action;
        actionButton.setText(actionText == null ? "Muat ulang" : actionText);
        actionButton.setVisible(action != null);
    }
}
