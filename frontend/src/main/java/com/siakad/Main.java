package com.siakad;

import com.formdev.flatlaf.FlatDarkLaf;
import com.siakad.views.LoginFrame;
import javax.swing.*;

/**
 * Main Entry Point - Sistem Informasi Akademik
 * Menggunakan FlatLaf Dark theme untuk tampilan modern
 */
public class Main {
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        } catch (Exception e) {
            System.err.println("Gagal set Look and Feel: " + e.getMessage());
        }

        // Launch GUI on EDT
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
