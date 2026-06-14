package com.siakad.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;

/**
 * Shared Swing UI compatibility settings used by the desktop app.
 */
public final class SwingUi {
    private static boolean popupFixInstalled = false;

    private SwingUi() {
    }

    public static synchronized void installPopupFixes() {
        if (popupFixInstalled) {
            return;
        }
        popupFixInstalled = true;

        applyPopupDefaults();

        AWTEventListener listener = event -> {
            if (event instanceof ContainerEvent containerEvent
                    && containerEvent.getID() == ContainerEvent.COMPONENT_ADDED) {
                configurePopups(containerEvent.getChild());
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.CONTAINER_EVENT_MASK);
    }

    public static void applyPopupDefaults() {
        PopupFactory.setSharedInstance(new PopupFactory());
        JPopupMenu.setDefaultLightWeightPopupEnabled(true);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        UIManager.put("Popup.forceHeavyWeight", false);
        UIManager.put("Popup.dropShadowPainted", false);
        UIManager.put("ComboBox.isEnterSelectablePopup", true);
    }

    public static void configurePopups(Component component) {
        if (component instanceof JComboBox<?> comboBox) {
            comboBox.setLightWeightPopupEnabled(true);
            comboBox.putClientProperty("Popup.forceHeavyWeight", Boolean.FALSE);
            comboBox.putClientProperty("Popup.dropShadowPainted", Boolean.FALSE);
        }
        if (component instanceof JPopupMenu popupMenu) {
            popupMenu.setLightWeightPopupEnabled(true);
            popupMenu.putClientProperty("Popup.forceHeavyWeight", Boolean.FALSE);
            popupMenu.putClientProperty("Popup.dropShadowPainted", Boolean.FALSE);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                configurePopups(child);
            }
        }
    }
}
