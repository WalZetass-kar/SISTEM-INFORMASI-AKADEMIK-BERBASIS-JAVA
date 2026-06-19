package com.siakad.views.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

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
        return cardPanel(BORDER, CARD, 14);
    }

    static JPanel cardPanel(Color accent) {
        return cardPanel(accent, CARD, 14);
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
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
                g2.fillRoundRect(0, 0, 5, getHeight(), arc, arc);
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(MUTED);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitleLabel);
        header.add(text, BorderLayout.WEST);

        if (badgeText != null && !badgeText.isBlank()) {
            header.add(pill(badgeText, badgeColor), BorderLayout.EAST);
        }

        return header;
    }

    static JLabel pill(String text, Color color) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(226, 232, 240));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 110)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return label;
    }

    static JLabel metric(String text) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(203, 213, 225));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return label;
    }

    static JPanel sectionIntro(String title, String note) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT);
        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLabel.setForeground(MUTED);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(noteLabel);
        return panel;
    }

    static JScrollPane pageScroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    static void relayWheelToParentScroll(JScrollPane source, JScrollPane target) {
        if (source == null || target == null) {
            return;
        }
        source.setWheelScrollingEnabled(false);
        source.addMouseWheelListener(e -> {
            MouseWheelEvent forwarded = new MouseWheelEvent(
                    target,
                    MouseEvent.MOUSE_WHEEL,
                    e.getWhen(),
                    e.getModifiersEx(),
                    e.getX(),
                    e.getY(),
                    e.getClickCount(),
                    e.isPopupTrigger(),
                    e.getScrollType(),
                    e.getScrollAmount(),
                    e.getWheelRotation()
            );
            target.dispatchEvent(forwarded);
        });
    }

    static JComponent centeredWidth(JComponent component, int width) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        Dimension pref = component.getPreferredSize();
        int boundedWidth = Math.max(1, width);
        component.setPreferredSize(new Dimension(boundedWidth, pref.height));
        component.setMaximumSize(new Dimension(boundedWidth, Integer.MAX_VALUE));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        wrapper.add(component, c);
        return wrapper;
    }
}
