package com.siakad.views.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class AcademicUi {
    static final Color BG = new Color(13, 19, 38);
    static final Color CARD = new Color(18, 26, 48);
    static final Color CARD_SOFT = new Color(21, 31, 56);
    static final Color BORDER = new Color(31, 44, 78);
    static final Color TEXT = new Color(248, 250, 252);
    static final Color MUTED = new Color(148, 163, 184);
    static final Color BLUE = new Color(59, 130, 246);
    static final Color GREEN = new Color(34, 197, 94);
    static final Color AMBER = new Color(234, 179, 8);

    private AcademicUi() {
    }

    static JPanel cardPanel() {
        return cardPanel(BORDER, CARD, 12);
    }

    static JPanel cardPanel(Color accent) {
        return cardPanel(accent, CARD, 12);
    }

    static JPanel cardPanel(Color accent, Color fill, int arc) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
                g2.fillRoundRect(0, 0, 4, getHeight(), arc, arc);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    static JPanel pageHeader(String title, String subtitle, String badgeText, Color badgeColor) {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));
        text.add(subtitleLabel);
        header.add(text, BorderLayout.WEST);

        if (badgeText != null && !badgeText.isBlank()) {
            header.add(pill(badgeText, badgeColor), BorderLayout.EAST);
        }

        return header;
    }

    static JLabel pill(String text, Color color) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(226, 232, 240));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 110)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return label;
    }

    static JLabel metric(String text) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(203, 213, 225));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 8, 7, 8)
        ));
        return label;
    }

    static JPanel sectionIntro(String title, String note) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT);
        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteLabel.setForeground(MUTED);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(noteLabel);
        return panel;
    }
}
