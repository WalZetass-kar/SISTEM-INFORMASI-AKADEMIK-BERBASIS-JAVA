package com.siakad.utils;

import java.awt.Color;

public final class AppTheme {
    private static boolean dark = false;

    private AppTheme() {}

    public static boolean isDark() { return dark; }
    public static void toggle() { dark = !dark; }
    public static String toggleText() { return dark ? "Mode Terang" : "Mode Gelap"; }

    public static Color bg()        { return dark ? new Color(15, 23, 42) : new Color(248, 250, 252); }
    public static Color card()      { return dark ? new Color(30, 41, 59) : Color.WHITE; }
    public static Color table()     { return dark ? new Color(22, 32, 52) : Color.WHITE; }
    public static Color header()    { return dark ? new Color(15, 23, 42) : new Color(248, 250, 252); }
    public static Color border()    { return dark ? new Color(51, 65, 85) : new Color(226, 232, 240); }
    public static Color rowAlt()    { return dark ? new Color(30, 41, 59) : new Color(248, 250, 252); }
    public static Color text()      { return dark ? new Color(248, 250, 252) : new Color(15, 23, 42); }
    public static Color muted()     { return dark ? new Color(148, 163, 184) : new Color(71, 85, 105); }
    public static Color dim()       { return dark ? new Color(71, 85, 105) : new Color(148, 163, 184); }

    public static Color blue()      { return dark ? new Color(79, 70, 229) : new Color(79, 70, 229); }
    public static Color green()     { return dark ? new Color(16, 185, 129) : new Color(16, 185, 129); }
    public static Color yellow()    { return dark ? new Color(245, 158, 11) : new Color(245, 158, 11); }
    public static Color red()       { return dark ? new Color(239, 68, 68) : new Color(239, 68, 68); }
    public static Color purple()    { return dark ? new Color(139, 92, 246) : new Color(139, 92, 246); }
    public static Color cyan()      { return dark ? new Color(6, 182, 212) : new Color(6, 182, 212); }

    public static Color sidebar()       { return dark ? new Color(10, 15, 30) : new Color(15, 23, 42); }
    public static Color sidebarHover()  { return dark ? new Color(30, 41, 59) : new Color(30, 41, 59); }
    public static Color sidebarActive() { return dark ? new Color(79, 70, 229) : new Color(79, 70, 229); }
    public static Color sidebarMuted()  { return new Color(148, 163, 184); }

    public static Color input()    { return dark ? new Color(15, 23, 42) : Color.WHITE; }

    public static Color shadow()    { return dark ? new Color(0, 0, 0, 60) : new Color(0, 0, 0, 18); }
    public static Color topbar()    { return dark ? new Color(15, 23, 42) : Color.WHITE; }
}
