package com.siakad.views.panels;

import com.siakad.utils.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AkademikComingSoonPanel extends JPanel {

    private static Color BG() { return AppTheme.bg(); }
    private static Color CARD_BG() { return AppTheme.card(); }
    private static Color BORDER() { return AppTheme.border(); }
    private static Color TEXT() { return AppTheme.text(); }
    private static Color MUTED() { return AppTheme.muted(); }
    private static Color BLUE() { return AppTheme.blue(); }

    public AkademikComingSoonPanel(String featureName, String description) {
        setBackground(BG());
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(28, 28, 18, 28));

        JLabel title = new JLabel("Akademik");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT());
        JLabel subtitle = new JLabel("Nilai & Absensi");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED());
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(BLUE().getRed(), BLUE().getGreen(), BLUE().getBlue(), 70));
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(520, 220));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(34, 38, 34, 38));

        JLabel icon = new JLabel("A+");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 30));
        icon.setForeground(BLUE());
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel feature = new JLabel(featureName);
        feature.setFont(new Font("Segoe UI", Font.BOLD, 22));
        feature.setForeground(TEXT());
        feature.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel status = new JLabel("Segera hadir");
        status.setFont(new Font("Segoe UI", Font.BOLD, 12));
        status.setForeground(BLUE());
        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea detail = new JTextArea(description);
        detail.setOpaque(false);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detail.setForeground(MUTED());
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(14));
        card.add(feature);
        card.add(Box.createVerticalStrut(6));
        card.add(status);
        card.add(Box.createVerticalStrut(16));
        card.add(detail);

        center.add(card);
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }
}
